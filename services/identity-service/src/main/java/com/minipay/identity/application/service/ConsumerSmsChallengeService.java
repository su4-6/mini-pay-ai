package com.minipay.identity.application.service;

import com.minipay.identity.application.port.SmsSender;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

// Spring 会创建这个业务 Service Bean。Controller 调用它来完成“发送登录验证码”这个用例。
// Controller 负责 HTTP 协议转换；Service 负责业务规则。
@Service
public class ConsumerSmsChallengeService {
    private static final int MAX_ATTEMPTS = 5;
    // Redis 键前缀按“数据用途”区分；写入、读取、删除同一种数据时必须使用同一个前缀。
    // challenge:<challengeId>：某一次验证码请求的完整档案，使用请求编号定位。
    private static final String CHALLENGE_PREFIX = "minipay:auth:consumer:challenge:";
    // latest:<phoneHash>：某手机号最新的是哪一次请求，使用手机号标记定位。
    private static final String LATEST_PREFIX = "minipay:auth:consumer:latest:";
    // resend:<phoneHash>：某手机号是否仍在重发等待期。
    private static final String RESEND_PREFIX = "minipay:auth:consumer:resend:";
    // lock:<phoneHash>：某手机号是否因输错过多而被锁定。
    private static final String LOCK_PREFIX = "minipay:auth:consumer:lock:";
    private static final DefaultRedisScript<String> VERIFY_SCRIPT = new DefaultRedisScript<>("""
            -- KEYS[1] 是 challenge:<challengeId> 的完整验证码档案。
            -- 整段脚本在 Redis 内一次执行：读取、比较、次数加一、锁定或删除不会被并发请求插队。
            if redis.call('EXISTS', KEYS[1]) == 0 then
              -- 档案可能自然过期、已被成功消费，或新验证码发送时已删除旧档案。
              return 'EXPIRED'
            end
            local phoneHash = redis.call('HGET', KEYS[1], 'phoneHash')
            local lockKey = ARGV[4] .. phoneHash
            if redis.call('EXISTS', lockKey) == 1 then
              -- 锁按手机号而非 challengeId 建立：换一个旧编号也不能绕过输错次数限制。
              return 'LOCKED'
            end
            if redis.call('HGET', KEYS[1], 'codeDigest') ~= ARGV[1] then
              local attempts = redis.call('HINCRBY', KEYS[1], 'attempts', 1)
              if attempts >= tonumber(ARGV[2]) then
                -- 达到最大错误次数：写手机号锁、删除本次档案和“最新编号”指针。
                redis.call('SET', lockKey, '1', 'EX', tonumber(ARGV[3]))
                redis.call('DEL', KEYS[1])
                redis.call('DEL', ARGV[5] .. phoneHash)
                return 'LOCKED'
              end
              return 'INVALID'
            end
            local mobile = redis.call('HGET', KEYS[1], 'mobile')
            -- 正确验证码只能成功一次：立即删除档案和 latest 指针，避免重复提交两次都成功。
            redis.call('DEL', KEYS[1])
            redis.call('DEL', ARGV[5] .. phoneHash)
            return 'OK:' .. mobile
            """, String.class);

    // 这些依赖和配置会由下面构造器保存，之后 create(...) 可以直接使用。
    private final StringRedisTemplate redis;
    private final PhoneNumberService phoneNumbers;
    private final SmsSender smsSender;
    private final AuthRateLimitService rateLimits;
    private final byte[] pepper;
    private final Duration ttl;
    private final Duration resendAfter;
    private final Duration lockDuration;
    private final SecureRandom random = new SecureRandom();

    public ConsumerSmsChallengeService(
            StringRedisTemplate redis,
            PhoneNumberService phoneNumbers,
            SmsSender smsSender,
            AuthRateLimitService rateLimits,
            @Value("${minipay.identity.captcha-pepper}") String pepper,
            @Value("${minipay.identity.sms.code-ttl}") Duration ttl,
            @Value("${minipay.identity.sms.resend-after}") Duration resendAfter,
            @Value("${minipay.identity.sms.consumer-lock-duration:10m}") Duration lockDuration) {
        // @Value 从 application.yml 读取配置，例如验证码有效期 ttl 和重发等待 resendAfter。
        // 左边 this.xxx 是字段，右边 xxx 是构造器参数；赋值后这个 Service Bean 能持续使用它们。
        this.redis = redis;
        this.phoneNumbers = phoneNumbers;
        this.smsSender = smsSender;
        this.rateLimits = rateLimits;
        this.pepper = pepper.getBytes(StandardCharsets.UTF_8);
        this.ttl = ttl;
        this.resendAfter = resendAfter;
        this.lockDuration = lockDuration;
    }

    public ConsumerSmsChallenge create(String rawMobile, String clientAddress) {
        // rawMobile 接收 Controller 传来的第 1 个值：用户输入的手机号。
        // clientAddress 接收第 2 个值：本次请求的 IP 地址。参数名可以与 Controller 不同，按位置传值。
        String mobile;
        try {
            // 先校验并整理手机号格式；成功后把标准化结果放入 mobile。
            mobile = phoneNumbers.normalize(rawMobile);
        } catch (IllegalArgumentException exception) {
            // 手机号不合法时，把底层异常转换成接口可识别的业务错误，并停止本次发送流程。
            // throw 会让方法提前结束，后面的锁定检查、短信发送和 Redis 写入都不会执行。
            throw new LoginRejectedException("MOBILE_INVALID");
        }
        // 不直接用完整手机号作为 Redis 键，先得到处理后的标记，降低敏感信息暴露风险。
        String phoneHash = phoneNumbers.hashHex(mobile);
        // Redis 在这里可先理解为“会自动过期的临时信息柜”。
        // 如果发现该手机号存在锁定标记，直接拒绝发送；后面的限流和短信发送都不会执行。
        if (Boolean.TRUE.equals(redis.hasKey(LOCK_PREFIX + phoneHash))) {
            // remainingSeconds(...) 查询锁定记录还剩多久过期，并随 SMS_LOCKED 一起交给上层。
            throw new LoginRejectedException("SMS_LOCKED", remainingSeconds(LOCK_PREFIX + phoneHash));
        }
        // 同时按手机号和请求 IP 做频率限制；超过限制会抛出异常并停止本次流程。
        rateLimits.checkSmsRequest(mobile, clientAddress);
        // RESEND_PREFIX + phoneHash 是“刚发送过”的临时标记。
        // setIfAbsent 与 Day 1 的 putIfAbsent 思路相近：键不存在才写入；返回 true 才允许发送。
        // resendAfter 到期后 Redis 自动删除该标记，用户才能再次发送。
        Boolean resendAllowed = redis.opsForValue()
                .setIfAbsent(RESEND_PREFIX + phoneHash, "1", resendAfter);
        if (!Boolean.TRUE.equals(resendAllowed)) {
            throw new LoginRejectedException(
                    "SMS_RESEND_TOO_SOON", remainingSeconds(RESEND_PREFIX + phoneHash));
        }

        // 先准备一个本地备用验证码：非演示模式随机生成六位数；演示模式使用配置的演示码。
        // ?: 是 if-else 的简写；String.format("%06d", ...) 保证不足六位时前面补 0。
        // 后面如果短信渠道自己生成验证码，会用渠道返回的值覆盖这个备用值。
        String code = smsSender.demoCodeForView() == null
                ? String.format("%06d", random.nextInt(1_000_000))
                : smsSender.demoCodeForView();
        // 每次发送都生成一个新的请求编号。它不是验证码，而是后续验证时定位本次临时档案的键的一部分。
        String challengeId = UUID.randomUUID().toString();
        try {
            // 有些渠道会自行生成并下发验证码；generated 为 null 仅表示渠道不生成，不表示发送失败。
            String generated = smsSender.sendAndGetConsumerLoginCode(mobile);
            if (generated != null) {
                // 渠道（如阿里云号码认证）自行生成并下发验证码，用云端返回的码落库
                code = generated;
            } else {
                // 渠道不生成验证码时，使用上面已准备好的本地 code 发送。
                smsSender.sendConsumerLoginCode(mobile, code);
            }
            // LATEST_PREFIX + phoneHash：某手机号“最新一次”请求编号的指针。
            // getAndSet 先返回旧编号 previous，再把 Redis 中的最新编号更新为当前 challengeId。
            String previous = redis.opsForValue().getAndSet(LATEST_PREFIX + phoneHash, challengeId);
            // expire 不是立刻删除；它给“手机号 -> 最新编号”设置 ttl 倒计时，到期才由 Redis 自动删除。
            redis.expire(LATEST_PREFIX + phoneHash, ttl);
            if (previous != null && !previous.isBlank()) {
                // previous 是旧编号，例如 A；这句删除的是完整 Redis 键 challenge:A，而不是只删除字符串 A。
                // 新请求编号例如 B 仍会在后面保存为 challenge:B，因此旧验证码立即失效而新验证码保留。
                redis.delete(CHALLENGE_PREFIX + previous);
            }
            // CHALLENGE_PREFIX + challengeId：本次请求的详细档案。
            // 与 LATEST 的“手机号 -> 最新编号”不同，它保存后续校验验证码所需的多项资料。
            // create 这里只记录正确答案的摘要和尝试次数；真正判断用户输入对错在 consume(...) 中进行。
            redis.opsForHash().putAll(CHALLENGE_PREFIX + challengeId, Map.of(
                    "codeDigest", digest(code),
                    "mobile", mobile,
                    "phoneHash", phoneHash,
                    "attempts", "0"));
            // expire 不是现在删；这份 challenge:当前编号 档案会保留 ttl，再由 Redis 自动删除，验证码随之过期。
            redis.expire(CHALLENGE_PREFIX + challengeId, ttl);

            // 所有规则、短信发送和 Redis 记录成功后，返回一份不含真实验证码的结果给 Controller。
            // Controller 会把这个对象直接放进 ResponseEntity.body(...)，Spring 再转换为 JSON 给前端。
            // 四项分别是：请求编号、脱敏手机号、验证码过期时间、允许重发前还需等待的秒数。
            return new ConsumerSmsChallenge(
                    challengeId,
                    phoneNumbers.mask(mobile),
                    Instant.now().plus(ttl),
                    resendAfter.toSeconds());
        } catch (RuntimeException exception) {
            // 中途失败时清理当前请求留下的三类临时状态：详细档案、最新编号指针、重发等待标记。
            // delete 不存在的键也安全；清理后继续抛出原异常，不能把失败伪装成发送成功。
            // create 涉及外部短信渠道和多个 Redis 写入，无法作为一个 Redis 脚本或一个数据库事务整体回滚。
            // 因此失败后用 delete 做补偿清理；它不能撤回已经发到手机上的短信，只清理服务端临时状态。
            redis.delete(CHALLENGE_PREFIX + challengeId);
            redis.delete(LATEST_PREFIX + phoneHash);
            redis.delete(RESEND_PREFIX + phoneHash);
            throw exception;
        }
    }

    public VerifiedMobile consume(String challengeId, String code) {
        // Java 不在这里手工拆开“查询 -> 比较 -> 计数 -> 删除”四步；统一交给上面的 Redis 脚本原子完成。
        // challengeId 用于拼出验证码档案键；code 会先去空格、再取摘要，绝不把明文验证码保存或比较到日志中。
        String result = redis.execute(
                VERIFY_SCRIPT,
                java.util.List.of(CHALLENGE_PREFIX + challengeId),
                digest(code == null ? "" : code.trim()),
                Integer.toString(MAX_ATTEMPTS),
                Long.toString(lockDuration.toSeconds()),
                LOCK_PREFIX,
                LATEST_PREFIX);
        // Redis 脚本只返回简短状态；Java 负责把状态转换成上层能使用的结果对象或业务异常。
        if (result != null && result.startsWith("OK:")) {
            // 返回的是已通过验证码校验的手机号，供登录用例继续查找/创建账户；不是直接返回给前端。
            return new VerifiedMobile(result.substring(3));
        }
        if ("LOCKED".equals(result)) {
            // 统一用 LoginRejectedException 往上交；Controller/全局异常处理随后会变成 HTTP 失败响应。
            throw new LoginRejectedException("SMS_LOCKED", lockDuration.toSeconds());
        }
        if ("INVALID".equals(result)) {
            throw new LoginRejectedException("SMS_INVALID");
        }
        throw new LoginRejectedException("SMS_EXPIRED");
    }

    private String digest(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to digest consumer SMS code", exception);
        }
    }

    private long remainingSeconds(String key) {
        Long seconds = redis.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
        return seconds == null || seconds < 0 ? 0 : seconds;
    }

    public record ConsumerSmsChallenge(
            String challengeId,
            String maskedMobile,
            Instant expiresAt,
            long resendAfterSeconds) {
    }

    public record VerifiedMobile(String mobile) {
    }
}
