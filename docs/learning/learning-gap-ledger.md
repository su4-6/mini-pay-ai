# MiniPay 学习补课台账

> 本台账不代表 Day 1 或 Day 2 做错了。它记录的是当日刻意不深挖、但 30 天内必须有明确去处的内容。旧分支保持冻结；补课只在后续分支进行。

> 使用顺序：从 [学习首页](README.md) 进入，按 [8 周核心执行计划](eight-week-core-plan.md) 确定当前学习单元，再阅读对应 `day-*.md`。本台账只记录未完成项的后续落点和证据，不得用补课替代 [第一代 30 天主计划](original-30-day-baseline.md) 的原主题。

## 已完成，不再重复安排

| 来源 | 已有证据 |
|---|---|
| Day 1 | `HashMap` 的 `put/get`、覆盖、`null`、冲突直觉；`ArrayList` 的下标/删除/遍历/fail-fast；`ConcurrentHashMap.putIfAbsent`；Payment/Wallet 边界 |
| Day 2 | `String/int/Integer`、泛型、`NumberFormatException`；注解/Bean/构造器注入直觉；启动类 → Controller → `create(...)` 的验证码发送链 |

## 必须补齐的内容与落点

| 来源 | 尚未系统学习的内容 | 安排到哪天 | 完成证据 |
|---|---|---|---|
| Day 1 | `LinkedList`、`HashSet`，以及它们与 `ArrayList`/`HashMap` 的选择 | Day 3 开场的 30 分钟集合收尾 | 写出一张选择表并运行最小练习 |
| Day 1 | HashMap 扰动、2 的幂、负载因子、扩容、树化、复杂度 | Day 4 的并发课前置复习 | 能按 `put` 流程画图并回答追问 |
| Day 1 | ConcurrentHashMap 的可见性、CAS、桶级同步、复合操作原子性 | Day 4 原计划并发课 | 用 `putIfAbsent/computeIfAbsent` 说明单操作与业务原子性的区别 |
| Day 1 | 8 服务、3 个 BFF、数据 owner 的脱稿表达 | Day 7 周验收 | 不看文档完成服务地图口述 |
| Day 2 | `/code/verify`、`consume(...)`、请求绑定、返回响应 | Day 3 开场补齐 | 画出输入 → Service → 返回/异常链 |
| Day 2 | Spring MVC 的 Filter/Controller/响应转换 | Day 3 原计划 | 区分 BFF 与 Identity 的两条真实请求链 |
| Day 2 | Bean 生命周期、AOP、`@Transactional` 的直觉 | Day 3 原计划 | 能解释代理为何在方法前后加入事务能力 |
| Day 2 | 类加载、字节码、反射性能、动态代理细节 | Day 5 JVM 课 | 完成“类加载 → Bean 可用”的简化口述 |
| Day 2 | Spring 自动配置源码、事务传播细节、循环依赖/三级缓存 | Day 30 红黄绿知识审计 | 标为黄/红并形成后续学习清单，不假装已掌握 |

## Day 3 收尾审计（2026-08-27 / `8.26`）

| 来源 | 项目 | 分类 | 去处与证据 |
|---|---|---|---|
| Day 1 补课 | `LinkedList`、`HashSet` 及其与 `ArrayList`/`HashMap` 的选择 | completed | 已运行 `learning-labs/day03-spring-ioc-aop-mvc/CollectionReviewPractice.java`，输出“支付”“2”。 |
| Day 2 补课 | `/code/verify`、`consume(...)`、请求绑定和异常/响应链 | completed | 已沿 `ConsumerAuthController` → `ConsumerSmsLoginApplicationService` → `ConsumerSmsChallengeService.consume(...)` 口述并完成问答。 |
| Day 3 原计划 | Filter/Controller 请求链、IoC/构造器注入、Bean 生命周期、AOP/事务、Controller 边界 | completed | 已完成 10 道口述题、2 道场景题，并有身份服务编译/定向测试证据。 |
| Day 3 原计划 | `MvcServiceFlowPractice.java`：输入对象 → Service → 返回对象最小练习 | scheduled carryover | Day 4 开场复习先完成并记录运行输出；Day 4 的并发新内容不减少。 |

## Day 4 收尾审计（2026-09-05 / 收尾 checkout `interview-sprint`）

| 来源 | 项目 | 分类 | 去处与复习证据 |
|---|---|---|---|
| Day 3 转入 | MVC 最小练习 | completed | `MvcServiceFlowPractice.java` 已输出“小明支付100元”。 |
| Day 4 原计划 | HashMap 深入、ConcurrentHashMap 复合操作 | completed | 完成口述；`ConcurrentHashMapPractice2.java` 已验证 `computeIfAbsent` 只创建一次会话。Day14 复习。 |
| Day 4 原计划 | 竞态、`volatile`、CAS、AtomicInteger、锁和 AQS 直觉 | completed | `AtomicCounterPractice.java` 结果为 `20000/20000`；锁与 AQS 深度诊断保留 Day21 复习。 |
| Day 4 原计划 | 线程池、队列、拒绝、关闭、Future/CompletableFuture | completed | 已运行线程池、取消、拒绝、支付页组装练习；Day14/Day21 复习。 |
| Day 4 原计划 | Agent Run Gate、实例内 Semaphore、虚拟线程职责 | completed | 已跟读三处源码并通过 `AgentRunApplicationServiceTest`（6/0/0）。Day21 复习。 |
| MQ 未来主线 | Inbox、`eventId` 持久化幂等的完整实现 | intentional later-depth | Day10--12 保留原计划；Day4 只建立“JVM 内 Map 不足以跨实例去重”的边界。 |

## Day 5 初始化（2026-09-05 / `9.5`）

| 来源 | 项目 | 当前分类 | Day5 安排与完成证据 |
|---|---|---|---|
| Day 2 转入 | 类加载过程、双亲委派，以及“类加载 → Spring Boot 启动 → Bean 可用”的简化链路 | scheduled carryover | 纳入 Day5 主线；完成类加载顺序练习和脱稿口述后才能转为 completed。 |
| Day 4 转入 | 死锁条件与 `jcmd`/`jstack` 的安全观察 | scheduled carryover | Day5 只观察学习进程的线程状态和线程栈；完整故障演练保留 Day28。 |
| Day 5 提升项 | JVM 参数、Heap Dump 分析和生产级调优 | intentional later-depth | Day5 只掌握安全的小堆实验和基础观察；Day28 再完成故障复盘，不接触生产进程。 |

Day5 当前只是初始化，以上项目尚未验收；不得因文档和目录已经存在而标记为 completed。

## Day 4 初始化存档（`8.28`）

Day 4 开场必须先完成 `MvcServiceFlowPractice.java`，随后按原 Day 4 范围学习 HashMap 深入、Java 并发与 Agent Run；不得因为补做这一个小练习而删减并发内容。

## 使用规则

1. 每天开始前先查看本台账中安排到当天的项目。
2. 每天结束前逐项标为“已完成”或重新指定具体日期；不能删除未完成项。
3. 若当天任务较多，优先保留原计划，再把补课作为额外前置/复习，不用补课替代原计划。
