# Day 3：Spring MVC、IoC、AOP 与 Bean 生命周期练习区

这里仅放 Day 3 的最小练习；不把练习写进 `services/`，不修改正式业务逻辑。

学习顺序：

1. 先补齐验证码校验链路的输入、规则和返回值。
2. 画两条真实请求链：Consumer BFF 的 `RequestIdWebFilter → SecuritySessionController`，以及 Identity 的 `RequestIdFilter → ConsumerAuthController → Service`；不虚构服务调用。
3. 用项目中的 `ConsumerAuthController` 理解 Spring MVC 请求进入、参数绑定和响应返回。
4. 用构造器注入复习 IoC 和 Bean 生命周期。
5. 以 `AdminActionAuditService.record(...)` 的 `@Transactional` 学习 AOP 的用途和边界。

计划中的最小练习：`MvcServiceFlowPractice.java`。讲到“输入 → Service → 返回值”时再创建；文件将记录目的、运行命令和实际输出。

明天每写一个练习文件都会在这里补充文件名、目的和运行结果。
