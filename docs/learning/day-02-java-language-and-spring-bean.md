# Day 2：Java 语言基础与 Spring Bean

> 分支：`8.25`。本文件和 `learning-labs/day02-java-spring-basics/` 是本分支新增的 Day 2 内容；不修改 Day 1 练习，也不改 MiniPay 的正式业务逻辑。

## 1. 今天的目标

完成 Day 2 后，你应能用自己的话解释：

1. `String`、`int`、`Integer` 的基本区别，以及为什么 `Map<String, Integer>` 要这样写。
2. 什么是泛型；为什么它能让错误更早出现。
3. 异常是什么；`try/catch` 的作用；什么情况不能简单吞掉异常。
4. 注解是什么；`@SpringBootApplication`、`@RestController`、`@Service` 大概在告诉 Spring 什么。
5. 反射的直觉含义：程序在运行时读取类、方法、注解等信息。
6. Spring Bean 是什么；为什么 Controller 可以在构造方法里拿到 Service，而不是自己 `new` 一个。

今天的“学会”标准不是背 Spring 源码，而是：能运行练习、指出 MiniPay 的真实入口、说清 Bean 是谁创建和管理的。

## 2. 今天暂不要求掌握

以下内容会在后续课深入，今天看到名字即可跳过：

- Java 字节码、类加载器细节、反射性能和动态代理实现。
- Spring Bean 完整生命周期、三级缓存、循环依赖源码。
- AOP 代理实现、事务传播行为、Spring 自动配置源码。

## 3. 学习顺序（一次只做一小节）

| 小节 | 先学概念 | 动手练习 | MiniPay 对应位置 | 通过标准 |
|---|---|---|---|---|
| 2.1 | 变量、`String`、`int`、`Integer` | 写“用户昵称 + 积分”的变量和输出 | Day 1 的 `Map<String, Integer>` | 能说出文本与整数的区别 |
| 2.2 | 泛型 | 故意尝试把文本放进 `List<Integer>`，理解编译器为什么阻止 | Controller 的请求/响应类型 | 能解释 `<String>` 是对列表元素类型的限制 |
| 2.3 | 异常 | 用 `Integer.parseInt` 制造格式错误，再用 `try/catch` 处理 | 登录、验证码、参数校验失败 | 能区分“正常结果”和“异常情况” |
| 2.4 | 注解与反射直觉 | 读取一个类上的自定义注解（提供代码后再写） | `@RestController`、`@Service` | 能说明注解是给框架读取的标记信息 |
| 2.5 | Spring Bean 与依赖注入 | 只读 3 个源码入口，画出启动 → Controller → Service 的箭头 | Identity 服务 | 能说出“Spring 创建对象并注入依赖” |

## 4. 先建立三个直觉

### 4.1 `String`、`int`、`Integer`

```java
String nickname = "小明"; // 文本
int score = 95;           // 基本整数
Integer level = 2;        // 整数对象：可以放到泛型容器中，也可能为 null
```

初学时先记：

- `String` 放文本。
- `int` 放一定存在的整数。
- `Integer` 是可作为对象使用的整数；集合泛型中应写 `Integer`，不能写 `int`。

### 4.2 泛型

```java
List<String> tasks = new ArrayList<>();
```

`<String>` 的意思是：这个列表只能放文本。它的好处是把“类型放错”尽量在写代码或编译时就发现，而不是程序运行后才出问题。

### 4.3 异常

```java
int amount = Integer.parseInt("100"); // 正常：100
int invalid = Integer.parseInt("一百"); // 异常：无法把文本转成整数
```

异常不是“代码写得很差”的同义词，而是程序遇到无法按正常路径继续处理的情况。对可以预期并能恢复的问题，可用 `try/catch`；对支付、资金等关键错误，不能打印一句话后假装成功，必须记录、返回正确结果或交给统一异常处理。

### 4.4 注解、反射与 Spring Bean

```java
@RestController
public class ConsumerAuthController { }
```

- 注解：写在类、方法或字段上的标记信息，例如 `@RestController`。
- 反射：程序运行时能够查看“这个类有什么注解、方法和字段”的能力。
- Spring：启动时扫描这些标记，创建并管理需要的对象。
- Bean：被 Spring 创建和管理的对象。
- 依赖注入：Controller 需要某个 Service 时，通过构造方法声明需求；Spring 把已经创建好的 Service 交给它，而不是 Controller 自己 `new`。

## 5. 今天只读三个 MiniPay 源码入口

不要试图读完整个 Identity 服务。按下面顺序，每次只打开一个文件：

1. [IdentityServiceApplication.java](../../services/identity-service/src/main/java/com/minipay/identity/IdentityServiceApplication.java)
   - 找 `@SpringBootApplication` 和 `SpringApplication.run(...)`。
   - 先理解：这是 Identity 服务启动入口，Spring 从这里开始创建和管理 Bean。
2. [ConsumerAuthController.java](../../services/identity-service/src/main/java/com/minipay/identity/interfaces/rest/ConsumerAuthController.java)
   - 找 `@RestController`、`@RequestMapping`、构造方法参数和 `@PostMapping`。
   - 先理解：它接收 HTTP 请求；构造方法里列出的对象是它依赖的能力。
3. [ConsumerSmsChallengeService.java](../../services/identity-service/src/main/java/com/minipay/identity/application/service/ConsumerSmsChallengeService.java)
   - 找 `@Service` 与 `create(...)` / `consume(...)`。
   - 先理解：这是登录验证码相关业务能力的一个候选 Bean，由 Controller 调用。

Day 2 的最小调用链：

```text
浏览器 / App
    ↓ HTTP 请求
ConsumerAuthController（接收请求）
    ↓ 调用
ConsumerSmsChallengeService（处理验证码业务）
```

今天不追 Redis、数据库、JWT 和短信供应商；它们分别留到 Redis、鉴权和基础设施课程。

## 6. 今日练习目录与规则

练习目录：`learning-labs/day02-java-spring-basics/`。

今天会按顺序创建这些独立练习文件：

```text
StringIntegerPractice.java      # 变量与类型
GenericPractice.java            # 泛型
ExceptionPractice.java          # 异常
AnnotationReflectionPractice.java # 注解与反射（讲到这一节再创建）
```

每个文件都可以在该目录终端执行：

```powershell
java 文件名.java
```

不把这些练习添加到 `services/`，不启动整个项目，不修改正式业务代码。

## 7. Day 2 口述验收题

1. `String`、`int`、`Integer` 分别适合存什么？
2. 为什么 `List<int>` 不可以，而 `List<Integer>` 可以？
3. 泛型解决了什么问题？
4. `null` 与异常是一回事吗？
5. `try/catch` 应该处理哪些类型的问题？为什么支付异常不能直接忽略？
6. 注解本身会自动执行代码吗？谁来读取它？
7. 什么是反射？用一句话说即可。
8. 什么是 Spring Bean？
9. 为什么 `ConsumerAuthController` 不自己 `new ConsumerSmsChallengeService()`？
10. 从 Identity 服务启动到一个 Controller 收到请求，至少说出两个关键注解。

## 8. 阶段完成记录（2026-08-25）

### 已完成的练习与源码阅读

1. 已运行 `StringIntegerPractice.java`、`GenericPractice.java`、`ExceptionPractice.java`。
2. 未单独创建“注解与反射”练习；改为直接阅读 MiniPay 的真实 `@SpringBootApplication`、`@RestController`、`@Service`，学习效果更贴近项目。
3. 已从启动类跟到 `ConsumerAuthController.send(...)`，再跟到 `ConsumerSmsChallengeService.create(...)`。
4. 已在这三个真实源码文件中增加学习注释，覆盖 Bean、构造器注入、请求参数、返回值、Redis 临时状态和异常路径；只添加注释，不改变业务逻辑。
5. 阅读时发现 `verify(...)` Controller 偏厚：它负责登录业务编排，未来应迁移到 Application Service；本日只记录问题，不在未建立测试前重构。

### 明日先补齐，再进入原计划 Day 3

验证码提交和校验的 `consume(...)` 链路、`/code/verify` 的 HTTP 处理、以及最小化的“输入 → Service → return → 响应”练习，明日先补齐；随后继续原计划 Day 3 的 IOC、AOP、Bean 生命周期和 Spring MVC，不缩减原内容。

## 9. 当天完成后更新什么

Day 2 结束时：

1. 在本文件补充实际完成的练习、运行输出和仍不清楚的问题。
2. 更新 `docs/learning/README.md` 的 Day 2 状态为“已完成”。
3. 提交并推送 `8.25`。
4. 创建下一天分支 `8.26`，只新增 Day 3 计划和 Day 3 练习目录。
