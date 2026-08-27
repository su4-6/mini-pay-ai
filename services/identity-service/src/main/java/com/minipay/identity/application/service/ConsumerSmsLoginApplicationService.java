package com.minipay.identity.application.service;

import com.minipay.identity.application.port.ConsumerAccountPort;
import com.minipay.identity.application.port.ConsumerAuthorizationCodePort;
import com.minipay.identity.application.port.ConsumerAuthorizationCodePort.IssuedAuthorizationCode;
import com.minipay.identity.application.port.LoginAuditPort;
import com.minipay.identity.application.service.ConsumerSmsChallengeService.VerifiedMobile;
import com.minipay.identity.domain.model.ConsumerPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费者短信登录这一整个用例的编排位置。
 *
 * <p>它不处理 HTTP、JSON 或注解校验；Controller 把已接收的参数整理成 Command 后交给它。
 * 这里依次完成验证码核验、账号获取、授权码签发和登录审计。</p>
 */
@Service
public class ConsumerSmsLoginApplicationService {
    private final ConsumerSmsChallengeService challenges;
    private final PhoneNumberService phoneNumbers;
    private final ConsumerAccountPort accounts;
    private final ConsumerAuthorizationCodePort authorizationCodes;
    private final LoginAuditPort audits;
    private final MerchantLoginPasswordService merchantPasswords;

    public ConsumerSmsLoginApplicationService(
            ConsumerSmsChallengeService challenges,
            PhoneNumberService phoneNumbers,
            ConsumerAccountPort accounts,
            ConsumerAuthorizationCodePort authorizationCodes,
            LoginAuditPort audits,
            MerchantLoginPasswordService merchantPasswords) {
        // 这同样是构造器注入：Spring 先准备好验证码 Service、手机号工具和各个 Port 的实际实现，
        // 再创建本 Application Service。这里依赖 Port 而不是直接依赖 Repository/JDBC，目的是让
        // “登录用例编排”只说明自己需要账户、授权码和审计能力，不关心它们具体怎样访问 MySQL 或安全组件。
        this.challenges = challenges;
        this.phoneNumbers = phoneNumbers;
        this.accounts = accounts;
        this.authorizationCodes = authorizationCodes;
        this.audits = audits;
        this.merchantPasswords = merchantPasswords;
    }

    // Spring AOP 看到此注解后，会在 verify 进入时开启登录事务：账号、已验证手机号和授权码
    // 这些数据库修改要么一起提交，要么某一步抛出异常后一起回滚。它不替业务判断失败原因。
    @Transactional
    public ConsumerSmsLoginResult verify(ConsumerSmsLoginCommand command) {
        // 在成功核验前还不知道真实手机号；所以先用前端提交的 challengeId 当失败审计的临时标识。
        // 它不是“最新验证码”，也不会用于重试；consume 成功后才替换为已验证手机号。
        // 它只用于“本次失败关联哪次验证码请求”，不是验证码本身，也不是重新执行登录的凭据。
        String auditIdentifier = command.challengeId();
        try {
            // consume 会原子地校验并一次性消费验证码；失败时抛出 LoginRejectedException。
            VerifiedMobile verified = challenges.consume(command.challengeId(), command.code());
            // 验证码正确后才获得手机号；后续审计改用手机号作为更有意义的登录标识。
            auditIdentifier = verified.mobile();

            // 账户能力：按手机号摘要查找账户；第一次登录时创建账户。返回的 consumer 是后续步骤统一使用的用户对象。
            // command.requestId 不是拿来查账户的条件，而是把这次创建/查询动作与同一 HTTP 请求关联起来。
            ConsumerPrincipal consumer = accounts.findOrCreate(
                    phoneNumbers.hash(verified.mobile()), command.requestId());
            // 把本次“已验证”的真实手机号及其脱敏展示值写回账户资料。只有验证码已核验成功才允许到这一步。
            // Application Service 负责决定调用顺序；具体怎样写 MySQL 由 ConsumerAccountRepository 实现。
            accounts.recordVerifiedPhone(
                    consumer.userId(), verified.mobile(), phoneNumbers.mask(verified.mobile()));

            // 授权码能力：以已经确认身份的 consumer 和前端提交的 OAuth 参数签发一次性的授权码。
            // 这里先把它理解为“登录成功后交给前端继续换取授权的短期凭据”；验证码对错仍由 challenges.consume 决定。
            IssuedAuthorizationCode code = authorizationCodes.issue(
                    consumer,
                    command.clientId(),
                    command.redirectUri(),
                    command.codeChallenge(),
                    command.codeChallengeMethod(),
                    command.deviceId());
            // 只有账号和授权码都已完成，才记录 SUCCESS；审计实现使用独立事务，供排错和安全追溯。
            audits.appendLogin(
                    consumer.userId(),
                    auditIdentifier,
                    "CONSUMER_SMS",
                    "SUCCESS",
                    command.clientAddress(),
                    command.userAgent(),
                    command.requestId());

            // 到这里整个登录用例已得到授权码、用户状态和已核验手机号；把它们封装成 Result 返回 Controller。
            // Result 不是直接给浏览器的 JSON；Controller 会在自己的边界上把它转换为 AuthorizationCodeResponse。
            return new ConsumerSmsLoginResult(
                    code.authorizationCode(),
                    code.expiresAt(),
                    consumer.userId(),
                    consumer.payPasswordSet(),
                    !consumer.onboardingCompleted(),
                    consumer.realNameStatus(),
                    consumer.realNameVerified(),
                    verified.mobile(),
                    merchantPasswords.configured(consumer.userId()));
        } catch (ConsumerAccountDisabledException exception) {
            // “账号禁用”是业务语义：先记失败审计，再转换为前端可识别的登录拒绝原因并抛出。
            // 异常离开 verify 后，Spring 才会回滚上面的外层登录事务。
            auditFailure(auditIdentifier, "DISABLED", command);
            throw new LoginRejectedException("ACCOUNT_DISABLED");
        } catch (LoginRejectedException exception) {
            // 验证码错误、过期、锁定等都在此汇总：审计使用便于统计的大类，前端仍收到具体 code。
            auditFailure(auditIdentifier, auditResult(exception.code()), command);
            throw exception;
        }
    }

    private void auditFailure(String identifier, String result, ConsumerSmsLoginCommand command) {
        // 两个失败分支共用同一种审计写入。LoginAuditRepository.appendLogin 的 REQUIRES_NEW 会暂停
        // 当前登录事务、单独提交这条记录，因此随后 throw 导致的外层回滚不会抹掉失败证据。
        audits.appendLogin(
                null,
                identifier,
                "CONSUMER_SMS",
                result,
                command.clientAddress(),
                command.userAgent(),
                command.requestId());
    }

    private String auditResult(String code) {
        // 前端需要 SMS_INVALID、SMS_EXPIRED 等具体失败原因；审计为了统计，会把它们归并为 SMS_FAILED
        // 等大类。归类不会改变随后 throw 给前端的原始 exception.code()。
        return switch (code) {
            case "SMS_LOCKED", "AUTH_RATE_LIMITED", "SMS_RESEND_TOO_SOON" -> "RATE_LIMITED";
            case "ACCOUNT_DISABLED" -> "DISABLED";
            case "PKCE_INVALID", "OAUTH_CLIENT_INVALID" -> "CLIENT_REJECTED";
            default -> "SMS_FAILED";
        };
    }

    /**
     * Controller 转交给登录用例的输入，不依赖 HttpServletRequest。
     * 前半部分是前端提交的验证码/OAuth 参数；后半部分是 Controller 从本次 HTTP 请求读取的 IP、
     * User-Agent、requestId。它们被装进同一个 Command，Application Service 才能不认识 HTTP 也完成登录。
     */
    public record ConsumerSmsLoginCommand(
            String challengeId,
            String code,
            String clientId,
            String redirectUri,
            String codeChallenge,
            String codeChallengeMethod,
            String deviceId,
            String clientAddress,
            String userAgent,
            String requestId) {
    }

    /**
     * 登录用例成功后的业务结果。它包含登录流程产生或查询到的完整业务信息；
     * Controller 再明确挑选这些值，组装为对前端公开的 AuthorizationCodeResponse JSON。
     */
    public record ConsumerSmsLoginResult(
            String authorizationCode,
            Instant expiresAt,
            UUID userId,
            boolean payPasswordSet,
            boolean onboardingRequired,
            String realNameStatus,
            boolean realNameVerified,
            String phone,
            boolean merchantPasswordConfigured) {
    }
}
