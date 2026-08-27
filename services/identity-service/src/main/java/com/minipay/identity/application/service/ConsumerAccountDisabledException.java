package com.minipay.identity.application.service;

/**
 * 账户已禁用时由账户能力抛出的稳定应用错误。
 * Application Service 捕获它后先写失败审计，再转换为前端可识别的 {@code ACCOUNT_DISABLED}；
 * 它不是 HTTP 响应，也不是 Controller 自己判断账户状态。
 */
public final class ConsumerAccountDisabledException extends RuntimeException {
}
