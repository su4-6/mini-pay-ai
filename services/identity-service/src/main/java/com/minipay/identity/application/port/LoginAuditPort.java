package com.minipay.identity.application.port;

import java.util.UUID;

/**
 * 登录安全审计的应用层约定。
 * Application Service 只依赖这个“需要记录哪些信息”的接口，不直接依赖 JDBC 或某张数据库表；
 * LoginAuditRepository 才是当前把这份约定落实为 INSERT 的基础设施实现。
 */
public interface LoginAuditPort {
    // 这是方法签名，不会在这里写库：userId 是成功时的用户（失败可为 null），identifier 是手机号或 challengeId 的
    // 审计标识，method/result 说明登录方式与结果，IP/User-Agent/requestId 用于追踪这一次具体请求。
    void appendLogin(
            UUID userId,
            String identifier,
            String method,
            String result,
            String clientAddress,
            String userAgent,
            String requestId);
}
