package com.minipay.identity.infrastructure.persistence;

import java.nio.charset.StandardCharsets;
import com.minipay.identity.application.port.LoginAuditPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// @Repository 让 Spring 创建这个基础设施 Bean。Application Service 注入 LoginAuditPort 时，
// 当前实际收到的就是本实现；调用 audits.appendLogin(...) 最终会走到下面的 INSERT。
@Repository
public class LoginAuditRepository implements LoginAuditPort {
    private final JdbcTemplate jdbcTemplate;
    private final byte[] auditPepper;

    public LoginAuditRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${minipay.identity.audit-hash-pepper}") String auditPepper) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditPepper = auditPepper.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    // 独立开启一个新事务保存审计：外层登录事务之后即使失败/回滚，这条“失败过”的安全记录仍应留下。
    // 这正是 REQUIRES_NEW 在本登录用例中的目的，不是验证码对错的业务规则。
    // 这是 Spring AOP 在本类的具体用法：调用 appendLogin 前开启独立事务，正常结束提交，
    // 异常离开方法则回滚这一笔审计事务；无需在方法体手写 begin/commit/rollback。
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void appendLogin(UUID userId, String identifier, String method, String result,
                            String clientAddress, String userAgent, String requestId) {
        append("LOGIN", userId, identifier, method, result, clientAddress, userAgent, requestId);
    }

    public void appendLogout(UUID userId, String identifier, String clientAddress,
                             String userAgent, String requestId) {
        append("LOGOUT", userId, identifier, "OIDC", "SUCCESS",
                clientAddress, userAgent, requestId);
    }

    private void append(String eventType, UUID userId, String identifier, String method,
                        String result, String clientAddress, String userAgent, String requestId) {
        // 审计表不直接保存手机号、IP、浏览器标识明文，而是保存摘要；requestId 保留用来关联同一次请求的日志。
        // jdbcTemplate.update 执行 INSERT；它是“真正向 login_audit 表新增一行”的位置。
        // requestId 不是接口名，而是本条审计对应的某一次具体 HTTP 请求编号。
        jdbcTemplate.update("""
                INSERT INTO login_audit (
                  audit_id, event_type, user_id, login_identifier_hash, authentication_method,
                  result_code, client_address_hash, user_agent_hash, request_id, occurred_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, UTC_TIMESTAMP(6))
                """,
                AdminAccountRepository.uuidToBytes(UUID.randomUUID()),
                eventType,
                userId == null ? null : AdminAccountRepository.uuidToBytes(userId),
                digest(identifier),
                method,
                result,
                digest(clientAddress),
                digest(userAgent),
                requestId);
    }

    public List<LoginAuditItem> findPage(int page, int size) {
        return jdbcTemplate.query("""
                SELECT a.audit_id, a.occurred_at, a.authentication_method, a.result_code,
                       a.request_id, u.nickname
                FROM login_audit a
                LEFT JOIN user_profile u ON u.user_id = a.user_id
                WHERE a.event_type = 'LOGIN'
                ORDER BY a.occurred_at DESC
                LIMIT ? OFFSET ?
                """,
                (resultSet, rowNumber) -> new LoginAuditItem(
                        AdminAccountRepository.bytesToUuid(resultSet.getBytes("audit_id")).toString(),
                        resultSet.getTimestamp("occurred_at").toInstant(),
                        resultSet.getString("authentication_method"),
                        resultSet.getString("result_code"),
                        resultSet.getString("nickname"),
                        resultSet.getString("request_id")),
                size,
                page * size);
    }

    public long count() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(audit_id) FROM login_audit WHERE event_type = 'LOGIN'",
                Long.class);
        return value == null ? 0 : value;
    }

    private byte[] digest(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(auditPepper, "HmacSHA256"));
            return mac.doFinal((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create audit digest", exception);
        }
    }

    public record LoginAuditItem(
            String auditId,
            Instant occurredAt,
            String authenticationMethod,
            String result,
            String displayName,
            String requestId) {
    }
}
