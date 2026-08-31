# MiniPay 项目待修正台账

> 用于记录学习过程中发现、但**不应在当下随手修改**的项目问题。
>
> 每条问题都要保留：事实证据、影响范围、建议处理时机、验证方式和状态。这样后续改造有依据，也避免把“发现了问题”误说成“已经完成优化”。

## 记录规则

1. 只记录有源码、接口文档、测试或运行现象作为证据的问题；不把猜测当结论。
2. 先记录，再决定是否改造；涉及接口、数据或资金链路的修改必须先补测试并单独评审。
3. 状态只允许为：`待确认前端使用`、`待规划`、`实施中`、`已验证完成`、`不处理（说明原因）`。
4. 每天结束前检查当天是否发现新的待修正项；若没有，也记录“本日无新增”。

## 待修正项

| ID | 发现日期/分支 | 事实与证据 | 影响 | 建议处理时机与方式 | 验证方式 | 状态 |
|---|---|---|---|---|---|---|
| IMP-001 | 2026-08-26 / `8.26` | 身份服务的接口契约 [identity-api-v1.yaml](../contracts/openapi/identity-api-v1.yaml) 中，`ConsumerAuthorizationCode` 只声明 `authorizationCode`、`expiresAt`、`payPasswordSet`、`onboardingRequired`、`realNameStatus`、`realNameVerified` 六类字段，且设置 `additionalProperties: false`；实际 [ConsumerAuthController.AuthorizationCodeResponse](../../services/identity-service/src/main/java/com/minipay/identity/interfaces/rest/ConsumerAuthController.java) 还会返回 `userId`、`phone`、`merchantPasswordConfigured`。 | 接口文档、后端实际 JSON 和前端预期可能不一致；若未来按 OpenAPI 生成客户端或做严格校验，可能出现兼容问题。 | 先查前端真实调用和是否需要这三个额外字段；再选择“补齐 OpenAPI 契约”或“从实际响应移除未约定字段”。这是接口兼容性改造，不能凭感觉删除字段。另需复核 `authorizationCode` 标为 `writeOnly` 是否符合“服务端响应返回授权码”的真实语义。 | 用集成测试调用 `/api/v1/auth/consumer/code/verify`，比对实际 JSON、OpenAPI schema、前端调用字段；确认兼容策略后更新契约测试。 | 待确认前端使用 |

## 每日检查模板

| 日期/分支 | 新发现 | 已处理 | 仍待处理 | 下次复查 |
|---|---|---|---|---|
| 2026-08-26 / `8.26` | IMP-001 | 无 | IMP-001 | Day 3 接口契约复习后，先查前端调用证据 |
| 2026-08-27 / `8.26` | 本日无新增 | 无 | IMP-001 | Day 4 前先补 MVC 小练习；接口契约问题仍等前端调用证据 |

## Spring AI 增量改造（计划，尚未实施）

| 编号 | 源码证据与问题边界 | 计划处理 | 验证方式 | 状态 |
|---|---|---|---|---|
| AI-001 | `SpringAiModelGateway` 的 `streamText` 与 `classifyMemory` 会申请 `modelPermits`，但 `classifyPendingTransferTurn` 未复用该保护；该路径当前失败时安全降级为 `NEW_TOPIC`，不应误继续转账。 | Day38：先补模型忙/超时单测，再统一调用保护与异常转换。 | 模型许可耗尽或超时时，测试应断言结果为 `NEW_TOPIC`，且不会触发业务继续动作。 | 待规划 |
