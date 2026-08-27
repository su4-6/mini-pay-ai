package com.minipay.identity.interfaces.rest;

import com.minipay.identity.application.service.ConsumerSmsChallengeService;
import com.minipay.identity.application.service.ConsumerSmsChallengeService.ConsumerSmsChallenge;
import com.minipay.identity.application.service.ConsumerSmsLoginApplicationService;
import com.minipay.identity.application.service.ConsumerSmsLoginApplicationService.ConsumerSmsLoginCommand;
import com.minipay.identity.application.service.ConsumerSmsLoginApplicationService.ConsumerSmsLoginResult;
import com.minipay.identity.infrastructure.security.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Spring 会把这个类创建并管理为 Bean；它负责接收 HTTP 请求，不在这里写短信业务规则。
@RestController
// 这个 Controller 下所有接口共用的路径前缀。
@RequestMapping("/api/v1/auth/consumer")
public class ConsumerAuthController {
    // 这些字段是 Controller 长期保存的“依赖”。它们现在只是字段声明，不是在这里创建对象。
    // Spring 创建 Controller Bean 时会把已经准备好的其他 Bean 传入构造器，再保存进这些字段。
    // challenges 专门负责短信验证码业务；后面的 send 方法只把请求数据转交给它。
    private final ConsumerSmsChallengeService challenges;
    // verify 接口把完整的“短信登录用例”交给它：验证码核验、账号、授权码和审计都不留在 Controller。
    private final ConsumerSmsLoginApplicationService consumerSmsLogin;

    public ConsumerAuthController(
            ConsumerSmsChallengeService challenges,
            ConsumerSmsLoginApplicationService consumerSmsLogin) {
        // 构造器参数是 Spring 按类型传进来的实际对象；这种方式叫构造器注入。
        // 左边 this.challenges 是本 Controller 的字段；右边 challenges 是构造器收到的 Service Bean。
        // 这一句不是创建 Service，而是把 Spring 已经准备好的对象保存下来，供本 Controller 后续使用。
        this.challenges = challenges;
        this.consumerSmsLogin = consumerSmsLogin;
    }

    // 与类上的前缀拼接后，完整路由是：POST /api/v1/auth/consumer/code/send。
    // App/前端通过 HTTP 请求触发它，不是前端直接调用 Java 方法。
    @PostMapping("/code/send")
    public ResponseEntity<ConsumerSmsChallenge> send(
            @Valid @RequestBody SendCodeRequest body,
            HttpServletRequest request) {
        // @RequestBody：把前端 JSON 转成 body；@Valid：先校验手机号等输入格式。
        // request 代表本次 HTTP 请求，request.getRemoteAddr() 能得到客户端 IP。
        // Java 先执行内层 create(...)：手机号和 IP 是传给 Service 的输入；其返回结果才会交给外层 body(...)
        // Java 先执行内层 create(...)：body.mobile()、request.getRemoteAddr() 是传给 Service 的两个输入。
        // create(...) 完成后返回 ConsumerSmsChallenge；外层 body(...) 接到的才是这个返回结果，不是手机号和 IP。
        // Controller 只负责接收、校验、转交；真正的“发验证码”规则在 Service 中。
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(challenges.create(body.mobile(), request.getRemoteAddr()));
    }

    @PostMapping("/code/verify")
    public AuthorizationCodeResponse verify(
            @Valid @RequestBody VerifyCodeRequest body,
            HttpServletRequest request) {
        // Controller 只做两件事：把 HTTP 输入整理为 Command，再把业务 Result 整理为 HTTP 响应。
        // body 中的 challengeId/code 是前端提交的输入；IP、User-Agent、requestId 是本次 HTTP 请求的附带信息。
        // 真正的验证码校验、账号处理、授权码签发和审计都交给 Application Service，不在接口层展开。
        ConsumerSmsLoginResult result = consumerSmsLogin.verify(new ConsumerSmsLoginCommand(
                body.challengeId(),
                body.code(),
                body.clientId(),
                body.redirectUri(),
                body.codeChallenge(),
                body.codeChallengeMethod(),
                body.deviceId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                RequestIdFilter.get(request)));
        // result 是服务内部的登录结果；这里明确挑选并排列接口需要返回给前端的字段，Spring 再把它转为 JSON。
        return new AuthorizationCodeResponse(
                result.authorizationCode(),
                result.expiresAt(),
                result.userId(),
                result.payPasswordSet(),
                result.onboardingRequired(),
                result.realNameStatus(),
                result.realNameVerified(),
                result.phone(),
                result.merchantPasswordConfigured());
    }

    public record SendCodeRequest(
            @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String mobile,
            @NotBlank @Pattern(regexp = "^LOGIN$") String purpose) {
    }

    public record VerifyCodeRequest(
            @NotBlank String challengeId,
            @NotBlank @Pattern(regexp = "^\\d{6}$") String code,
            @NotBlank String clientId,
            @NotBlank String redirectUri,
            @NotBlank @Size(min = 43, max = 128) String codeChallenge,
            @NotBlank @Pattern(regexp = "^S256$") String codeChallengeMethod,
            @NotBlank @Size(max = 128) String deviceId) {
    }

    // 这组字段是接口对前端的合同：是否需要或允许某字段，应由 OpenAPI、前端实际使用和接口测试共同确认。
    public record AuthorizationCodeResponse(
            String authorizationCode,
            Instant expiresAt,
            java.util.UUID userId,
            boolean payPasswordSet,
            boolean onboardingRequired,
            String realNameStatus,
            boolean realNameVerified,
            String phone,
            boolean merchantPasswordConfigured) {
    }
}
