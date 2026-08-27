# Day 3：补齐验证码校验链路 + IOC、AOP、Bean 生命周期、Spring MVC

> 分支：`8.26`。明天会先补齐 Day 2 尚未完成的验证码校验链路，再完整学习原定 Day 3 内容；不会因为补课减少 Day 3 的内容。

Day 1—2 的遗漏项目和后续落点见 [补课台账](learning-gap-ledger.md)。

## 0. 开场复习（30 分钟）

先不看笔记，依次回答：

1. `HashMap`、`ArrayList`、`ConcurrentHashMap` 分别解决什么问题？
2. 为什么 Payment 不能直接修改 Wallet 数据库？
3. Spring 为什么把 `ConsumerSmsChallengeService` 放进 `ConsumerAuthController` 的构造器，而不是 Controller 自己 `new`？

答完后再核对 Day 1/2 笔记。答错内容写入当天错题区；随后进行本日的 `LinkedList`/`HashSet` 收尾和验证码校验链，不减少 Day 3 原定内容。

## 1. 完成标准

结束时你能用自己的话说明：

1. 区分并画出两条真实请求链：Consumer BFF 的 Filter → Controller 链，以及 Identity 的 Filter → `ConsumerAuthController.verify(...)` 链；不虚构两个服务之间不存在的调用。
2. `@RequestBody`、`@Valid`、`@PostMapping`、返回对象和 HTTP 响应分别做什么。
3. `ConsumerSmsChallengeService.consume(challengeId, code)` 如何处理验证码过期、错误次数、锁定和成功。
4. IoC 是“对象由 Spring 创建和组装”；构造器注入是“类声明需要什么，Spring 提供什么”。
5. AOP 的直觉：在不把重复代码散落到每个业务方法里的前提下，为方法增加日志、鉴权或事务等横切能力。
6. Bean 生命周期先掌握简化顺序：扫描发现 → 创建对象 → 注入依赖 → 初始化 → 可使用 → 应用关闭时销毁。
7. 能指出 `ConsumerAuthController.verify(...)` 曾偏厚的原因，并说明它已在建立测试后提取到 Application Service。

## 2. 明天的完整顺序

| 顺序 | 内容 | MiniPay 入口 | 通过标准 |
|---|---|---|---|
| 3.0a | Day 1 集合收尾（额外） | `ArrayList`、`LinkedList`、`HashSet` | 能说出三者适用场景，不挤占原 Day 3 内容 |
| 3.0 | 补齐 Day 2：验证码提交与校验 | `ConsumerAuthController.verify`、`ConsumerSmsChallengeService.consume` | 能说出输入、规则、返回值 |
| 3.1 | Spring MVC 与 Filter 请求流程 | `RequestIdWebFilter`、`RequestIdFilter`、两个 Controller | 能画出两条真实请求链 |
| 3.2 | IoC 与构造器注入复习并加深 | `ConsumerAuthController` 构造器 | 不再把字段声明误解为创建对象 |
| 3.3 | Bean 生命周期 | 启动类与 Controller/Service Bean | 能说出简化生命周期与观察点 |
| 3.4 | AOP 与事务项目映射 | `AdminActionAuditService.record` 的 `@Transactional` | 能解释代理在方法前后做什么 |
| 3.5 | Controller 边界 | `verify(...)` | 能解释为什么它应当变薄 |
| 3.6 | 小实操、场景题与口述验收 | Day 3 练习目录、请求链图 | 完成练习、10 题与 2 个场景题 |

## 3. 先补齐：验证码校验链路

只跟三个入口，不试图一次读完整个身份服务：

1. `ConsumerAuthController.verify(...)`：HTTP 入口，取得 `challengeId`、验证码等请求内容。
2. `ConsumerSmsChallengeService.consume(...)`：读取验证码临时记录，判断规则，返回已验证手机号或抛出业务异常。
3. `VerifiedMobile`：成功时返回给上层的结果对象。

先用下面这条线理解：

```text
前端 JSON（challengeId、code）
  → Controller 的 body
  → challenges.consume(challengeId, code)
  → 成功：VerifiedMobile
  → Controller 继续调用后续能力并返回响应
```

验证码的详细 Redis Lua 写法、哈希算法和所有 OAuth 参数不是第一轮重点；先掌握“输入是什么、每条规则保护什么、成功/失败如何返回”。

## 3A. Day 1 额外收尾：LinkedList 与 HashSet

这 30 分钟是补课，不替换后面的 Spring 内容。

| 集合 | 先记住什么 | MiniPay 类比 |
|---|---|---|
| `ArrayList` | 按下标查找快，插入/删除中间项会移动元素 | 固定顺序的页面步骤 |
| `LinkedList` | 节点前后连接；先只知道它适合频繁在已知位置插入/删除 | 不作为业务数据库或 MQ 的替代品 |
| `HashSet` | 只保留不重复元素；底层可借助 HashMap 的 key 去重思想 | 单 JVM 内临时去重，不替代 Inbox/唯一约束 |

HashMap 的位运算、扩容、树化会放到 Day 4；不要在 Day 3 抢学并发底层。

## 4. 原计划 Day 3：Spring MVC 与 Filter

### 4.1 一次请求的简化路径

```text
HTTP 请求
  → DispatcherServlet（Spring MVC 总入口）
  → 根据 @PostMapping 找到 Controller 方法
  → JSON 转成 @RequestBody 参数，@Valid 校验
  → Controller 调用 Service
  → 返回对象转成 JSON HTTP 响应
```

今天不需要背 `DispatcherServlet` 内部源码。你需要能指出：Controller 是 HTTP 边界；Service 是业务规则的位置。

### 4.2 两条真实 MiniPay 请求链

不要把“有 BFF”误解为“所有 Identity 登录接口都经过 BFF”。先分别追踪项目中真实存在的两条链：

```text
浏览器 → consumer-bff 的 RequestIdWebFilter → SecuritySessionController(/api/v1/csrf)

浏览器或调用方 → identity-service 的 RequestIdFilter
  → ConsumerAuthController(/api/v1/auth/consumer/code/verify)
  → ConsumerSmsChallengeService.consume(...)
```

`RequestIdFilter` 的作用是给一次请求准备或透传 `X-Request-Id`，让后续 Controller、日志和错误响应可以关联同一请求；它不是业务登录规则。

### 4.3 当天只读源码入口

按以下顺序阅读，每次只打开一个文件：

1. `services/consumer-bff/src/main/java/com/minipay/consumerbff/infrastructure/security/RequestIdWebFilter.java`：认识 BFF 的 Filter 概念。
2. `services/consumer-bff/src/main/java/com/minipay/consumerbff/interfaces/rest/SecuritySessionController.java`：认识 BFF 的一个真实 Controller；它是 CSRF 会话入口，不冒充为验证码登录接口。
3. `services/identity-service/src/main/java/com/minipay/identity/infrastructure/security/RequestIdFilter.java`：认识 Identity 服务如何在进入 Controller 前准备 requestId。

验证码校验仍沿用 Day 2 的 `ConsumerAuthController.verify(...)` 与 `ConsumerSmsChallengeService.consume(...)`，只在补齐环节阅读。

### 4.4 参数与返回值

- `@RequestBody VerifyCodeRequest body`：把请求 JSON 交给 Java 对象 `body`。
- `@Valid`：按请求对象已经定义的规则检查输入。
- `HttpServletRequest request`：读取请求附带信息，例如请求地址或请求 ID。
- `return new XxxResponse(...)`：Service 或 Controller 准备响应对象，Spring 将它转成 JSON。

## 5. 原计划 Day 3：IoC 与 Bean 生命周期

`ConsumerAuthController` 不用 `new ConsumerSmsChallengeService()`；它只在构造器中声明需要 `ConsumerSmsChallengeService`。Spring 启动时先创建可用的 Service Bean，再创建 Controller Bean，把同一个 Service 对象引用传进构造器。

简化生命周期：

```text
扫描带注解的类
  → 创建 Bean
  → 注入构造器需要的 Bean
  → 初始化
  → 对外提供使用
  → 程序关闭时销毁
```

暂不深挖循环依赖、三级缓存与源码细节。

当天会做一个不改业务代码的观察：在启动类、`@RestController` 和 `@Service` 上分别标出“被扫描发现”“被创建并注入”“开始可接收请求”三个时刻。它用来建立顺序感，不要求调试 Spring 源码。

## 6. 原计划 Day 3：AOP 直觉

AOP 解决的是“很多地方都需要、但不是某一条业务独有规则”的能力。

| 能力 | 为什么属于横切能力 | MiniPay 的对应理解 |
|---|---|---|
| 日志 | 多个接口都需要记录请求与结果 | 不在每个业务分支复制日志模板 |
| 鉴权 | 多个受保护接口都要先判断身份 | 统一在请求进入业务前处理 |
| 事务 | 一段业务写入必须一起成功或失败 | 应由 Application Service 的事务边界管理 |

不能把所有代码都塞进 AOP：验证码是否正确、余额是否足够等，仍是明确的业务规则，应留在 Service/领域能力中。

项目中的第一处 AOP/事务证据使用：`AdminActionAuditService.record(...)` 上的 `@Transactional(propagation = REQUIRES_NEW)`。Spring 会通过代理在调用该方法前后建立并完成独立事务；先理解“方法执行前后自动加能力”，不深挖传播级别或代理源码。项目目前不需要人为添加一个 `@Aspect` 才能学习 AOP。

## 7. 架构观察：厚 Controller

> 本日发现但暂不修改的接口契约问题，统一记录在 [项目待修正台账](project-improvement-ledger.md)。它与“补课台账”不同：前者记录项目代码/文档将来要修正的事项，后者记录个人尚未学完的知识点。

`ConsumerAuthController.verify(...)` 原先直接编排验证码校验、账户处理、授权码签发和审计。根据项目规范，Controller 应主要负责协议转换、输入校验和调用用例。

用户已确认采用小范围改造，按“先读懂 → 补测试/验证 → 小范围提取 → 验证回归”完成了本次提取。

### 7.1 本次小范围提取（8.26）

消费者短信验证码登录的用例编排已提取到：

`services/identity-service/src/main/java/com/minipay/identity/application/service/ConsumerSmsLoginApplicationService.java`

现在的职责边界：

```text
ConsumerAuthController
  HTTP 路由、JSON/@Valid 校验、读取 IP/User-Agent/requestId、响应 JSON
  ↓ ConsumerSmsLoginCommand
ConsumerSmsLoginApplicationService
  验证码核验 → 查找/创建账号 → 记录验证手机号 → 签发授权码 → 登录审计
  （该方法是本登录用例的本地事务边界）
  ↓ ConsumerSmsLoginResult
ConsumerAuthController
  AuthorizationCodeResponse
```

应用服务只依赖三个 `application.port`：`ConsumerAccountPort`、`ConsumerAuthorizationCodePort`、`LoginAuditPort`；MySQL Repository 与 OAuth 安全组件在 `infrastructure` 中实现这些 Port。这样依赖方向保持为 `interfaces → application ← infrastructure`。

本次没有改变验证码、账号或授权码规则。`verify(...)` 现在是本登录用例的本地事务边界；失败登录审计由 `LoginAuditRepository.appendLogin(...)` 的 `REQUIRES_NEW` 独立事务保存，避免外层登录失败时审计也被回滚。

验证证据：`ConsumerSmsLoginApplicationServiceTest` 覆盖成功登录链路与验证码失败审计链路；`ConsumerAuthorizationCodeServiceTest` 覆盖授权码与 PKCE 规则，共 4/4 通过。项目要求 JDK 21；本机只有 JDK 26，本次临时跳过版本检查、仍以 `release 21` 完成编译和测试，未修改项目配置。

## 8. Day 3 收尾与验收（2026-08-27）

### 已完成的源码与口述证据

1. 已区分两条真实请求链：浏览器访问 BFF 时走 `RequestIdWebFilter → SecuritySessionController`；验证码登录请求进入 Identity 时走 `RequestIdFilter → ConsumerAuthController → ConsumerSmsLoginApplicationService → ConsumerSmsChallengeService`。两条链不是彼此必经的远程调用。
2. 已完成验证码失败场景口述：`consume(...)` 抛出 `LoginRejectedException`，外层 `try-catch` 记录失败审计后重新抛出；Filter 只负责请求编号，AOP 只负责事务的开始/提交/回滚，不能代替验证码规则。
3. 已完成 IoC、构造器注入、Bean 生命周期、Port/Repository 边界、`@Transactional` 与 `REQUIRES_NEW` 的口述校验。
4. 已完成 10 道口述题与 2 道场景题。首次遗漏了 Filter，已纠正为“前端请求 → Filter → Controller → Application Service → 返回 HTTP 响应”。
5. 已运行 `CollectionReviewPractice.java`，输出“支付”“2”。

### 不伪装为完成的项目

1. `MvcServiceFlowPractice.java` 尚未创建和运行；已转为 Day 4 开场复习，先做“输入对象 → Service → 返回对象”最小练习，再进入 Day 4 原有并发课程。
2. Day 3 没有新增待修正架构问题；`IMP-001` 的 OpenAPI 与实际响应字段不一致仍等待前端调用证据，不能凭感觉修改。

## 9. Day 3 口述题（已完成）

1. 前端的 JSON 是怎样变成 `VerifyCodeRequest body` 的？
2. `consume` 成功与失败各返回什么？
3. `@Valid` 和业务验证码校验有什么区别？
4. 什么是 IoC？为什么 Controller 不直接 `new` Service？
5. 构造器注入发生在 Bean 生命周期的哪个阶段？
6. AOP 适合放验证码“是否正确”的规则吗？为什么？
7. `RequestIdFilter` 为什么不是“登录业务代码”？
8. Bean 生命周期中，构造器注入发生在什么时候？
9. `@Transactional` 为什么能作为 AOP 的例子？
10. 为什么说 `verify(...)` 偏厚？之后应该移动到哪里？

## 10. 场景题（已完成）

1. 前端拿到验证码提交接口的失败响应，但日志难以关联。你从请求进入到 Controller，怎样利用 `X-Request-Id` 判断这是不是同一次请求？
2. 审计记录必须独立保存，即使外层业务之后失败也希望保留审计。为什么 `REQUIRES_NEW` 可能合适？先说目标和边界，不要求背传播级别源码。

