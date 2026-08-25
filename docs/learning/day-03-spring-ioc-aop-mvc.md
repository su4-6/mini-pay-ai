# Day 3：补齐验证码校验链路 + IOC、AOP、Bean 生命周期、Spring MVC

> 分支：`8.26`。明天会先补齐 Day 2 尚未完成的验证码校验链路，再完整学习原定 Day 3 内容；不会因为补课减少 Day 3 的内容。

## 1. 完成标准

结束时你能用自己的话说明：

1. 浏览器发出 `POST /api/v1/auth/consumer/code/verify` 后，请求如何进入 `ConsumerAuthController.verify(...)`。
2. `@RequestBody`、`@Valid`、`@PostMapping`、返回对象和 HTTP 响应分别做什么。
3. `ConsumerSmsChallengeService.consume(challengeId, code)` 如何处理验证码过期、错误次数、锁定和成功。
4. IoC 是“对象由 Spring 创建和组装”；构造器注入是“类声明需要什么，Spring 提供什么”。
5. AOP 的直觉：在不把重复代码散落到每个业务方法里的前提下，为方法增加日志、鉴权或事务等横切能力。
6. Bean 生命周期先掌握简化顺序：扫描发现 → 创建对象 → 注入依赖 → 初始化 → 可使用 → 应用关闭时销毁。
7. 能指出 `ConsumerAuthController.verify(...)` 目前偏厚的原因，并说明应在建立测试后迁到 Application Service，而不是今天直接重构。

## 2. 明天的完整顺序

| 顺序 | 内容 | MiniPay 入口 | 通过标准 |
|---|---|---|---|
| 3.0 | 补齐 Day 2：验证码提交与校验 | `ConsumerAuthController.verify`、`ConsumerSmsChallengeService.consume` | 能说出输入、规则、返回值 |
| 3.1 | Spring MVC 请求流程 | `@PostMapping`、`@RequestBody`、`@Valid` | 能画出请求到响应的链路 |
| 3.2 | IoC 与构造器注入复习并加深 | `ConsumerAuthController` 构造器 | 不再把字段声明误解为创建对象 |
| 3.3 | Bean 生命周期 | 启动类与 Controller/Service Bean | 能说出简化生命周期 |
| 3.4 | AOP 直觉与项目映射 | 日志、鉴权、事务注解的未来入口 | 能区分横切能力和业务规则 |
| 3.5 | Controller 边界 | `verify(...)` | 能解释为什么它应当变薄 |
| 3.6 | 小实操和口述验收 | Day 3 练习目录 | 用输入 → Service → 返回值复述 |

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

## 4. 原计划 Day 3：Spring MVC

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

### 4.2 参数与返回值

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

## 6. 原计划 Day 3：AOP 直觉

AOP 解决的是“很多地方都需要、但不是某一条业务独有规则”的能力。

| 能力 | 为什么属于横切能力 | MiniPay 的对应理解 |
|---|---|---|
| 日志 | 多个接口都需要记录请求与结果 | 不在每个业务分支复制日志模板 |
| 鉴权 | 多个受保护接口都要先判断身份 | 统一在请求进入业务前处理 |
| 事务 | 一段业务写入必须一起成功或失败 | 应由 Application Service 的事务边界管理 |

不能把所有代码都塞进 AOP：验证码是否正确、余额是否足够等，仍是明确的业务规则，应留在 Service/领域能力中。

## 7. 架构观察：厚 Controller

`ConsumerAuthController.verify(...)` 目前直接编排验证码校验、账户处理、授权码签发和审计。根据项目规范，Controller 应主要负责协议转换、输入校验和调用用例；未来应提取一个 Application Service。

明天只学习并画出边界，不重构。正确顺序是：先读懂 → 补测试/验证 → 再做小范围提取 → 验证回归。

## 8. 明日口述题

1. 前端的 JSON 是怎样变成 `VerifyCodeRequest body` 的？
2. `consume` 成功与失败各返回什么？
3. `@Valid` 和业务验证码校验有什么区别？
4. 什么是 IoC？为什么 Controller 不直接 `new` Service？
5. 构造器注入发生在 Bean 生命周期的哪个阶段？
6. AOP 适合放验证码“是否正确”的规则吗？为什么？
7. 为什么说 `verify(...)` 偏厚？之后应该移动到哪里？

