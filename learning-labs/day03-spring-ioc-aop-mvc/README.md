# Day 3：Spring MVC、IoC、AOP 与 Bean 生命周期练习区

这里仅放 Day 3 的最小练习；不把练习写进 `services/`，不修改正式业务逻辑。

学习顺序：

1. 先补齐验证码校验链路的输入、规则和返回值。
2. 画两条真实请求链：Consumer BFF 的 `RequestIdWebFilter → SecuritySessionController`，以及 Identity 的 `RequestIdFilter → ConsumerAuthController → Service`；不虚构服务调用。
3. 用项目中的 `ConsumerAuthController` 理解 Spring MVC 请求进入、参数绑定和响应返回。
4. 用构造器注入复习 IoC 和 Bean 生命周期。
5. 以 `AdminActionAuditService.record(...)` 的 `@Transactional` 学习 AOP 的用途和边界。

## 实际练习记录

| 文件 | 目的 | 实际运行结果 |
|---|---|---|
| `CollectionReviewPractice.java` | 回顾 `LinkedList` 的按顺序取值与 `HashSet` 的去重；不参与 MiniPay 正式构建。 | 输出 `支付`、`2`：第二个流程步骤为“支付”，重复加入的 `Pay` 权限只保留一份。 |

## 明确转入 Day 4 开场复习

原计划中的 `MvcServiceFlowPractice.java` 尚未创建。它会在 Day 4 新内容开始前补做，用最小代码模拟“输入对象 → Service 方法 → 返回对象”；Day 4 原有的并发内容不减少。
