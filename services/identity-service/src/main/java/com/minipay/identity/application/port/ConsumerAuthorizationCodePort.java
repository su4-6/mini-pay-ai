package com.minipay.identity.application.port;

import com.minipay.identity.domain.model.ConsumerPrincipal;
import java.time.Instant;

/**
 * 短信登录成功后签发 OAuth 授权码的应用层约定。
 *
 * <p>登录用例只提交已确认身份的消费者与前端 OAuth 参数，不直接依赖 Spring Security 的保存细节；
 * 当前由 {@code ConsumerAuthorizationCodeService} 实现。</p>
 */
public interface ConsumerAuthorizationCodePort {
    // 成功时返回短期授权码和过期时间。它不同于短信验证码：短信验证码用于核验手机号，授权码用于后续 OAuth 流程。
    IssuedAuthorizationCode issue(
            ConsumerPrincipal consumer,
            String clientId,
            String redirectUri,
            String codeChallenge,
            String codeChallengeMethod,
            String deviceId);

    // 这是基础设施能力返回给 Application Service 的内部结果；Controller 会再组织为面对前端的 Response JSON。
    record IssuedAuthorizationCode(String authorizationCode, Instant expiresAt) {
    }
}
