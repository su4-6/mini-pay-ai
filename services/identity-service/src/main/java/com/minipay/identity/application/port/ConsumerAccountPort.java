package com.minipay.identity.application.port;

import com.minipay.identity.domain.model.ConsumerPrincipal;
import java.util.UUID;

/**
 * 消费者短信登录用例需要的“账户能力”约定。
 *
 * <p>Application Service 只知道自己可以查找/创建账户、记录已验证手机号；它不应知道 SQL、表名或 JDBC。
 * 当前 {@code ConsumerAccountRepository} 实现这个接口，Spring 会把该实现按构造器注入给登录用例。</p>
 */
public interface ConsumerAccountPort {
    // phoneHash 是已验证手机号的摘要，用于账户匹配；traceId 关联本次请求。返回后续登录步骤统一使用的账户对象。
    ConsumerPrincipal findOrCreate(byte[] phoneHash, String traceId);

    // 只有验证码核验成功后才由 Application Service 调用：写入展示用脱敏手机号和受保护的真实手机号资料。
    void recordVerifiedPhone(UUID userId, String mobile, String maskedPhone);
}
