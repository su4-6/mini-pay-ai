# Day 4：HashMap 深入、Java 并发与 Agent Run

> 分支：`8.28`。实际学习日期与分支日期分开记录：2026-08-27 用于收尾 Day 3，本分支才开始 Day 4。

## 0. 开场复习与补课（不减少 Day 4）

### 15 分钟闭卷回忆

不看笔记回答：

1. 一次验证码校验请求从 Filter 到 Controller、Application Service、Redis 的顺序是什么？
2. 为什么 `LoginAuditRepository.appendLogin(...)` 用 `REQUIRES_NEW`？
3. `HashMap`、`ArrayList`、`ConcurrentHashMap` 各适合什么问题？

### 必做补课：MVC 最小练习

在 `learning-labs/day04-java-concurrency/MvcServiceFlowPractice.java` 手写：

```text
输入对象 Request → Service.calculate(...) → 返回对象 Result → main 打印结果
```

它只帮助复习 Controller 把输入交给 Service、再拿 Result 组织响应的边界；不是 Spring Bean 练习，也不替代后面的并发课程。

## 1. Day 4 完成标准

结束时能够：

1. 画出 `HashMap.put(...)` 的简化过程，说明哈希、桶、冲突、扩容、负载因子、树化为何存在。
2. 区分“线程各自的局部变量”与“多个线程共享的对象字段”；能解释竞态条件、原子性与可见性。
3. 区分 `synchronized`、`volatile`、CAS、`AtomicInteger`、`ReentrantLock` 的第一层用途和边界。
4. 说明 `ConcurrentHashMap` 的单操作安全不等于整个业务流程原子；能用 `putIfAbsent` 或 `computeIfAbsent` 举例。
5. 解释线程池为什么需要核心线程、队列、最大线程、拒绝策略和超时边界。
6. 指出 MiniPay 中三类不同并发边界：JVM 内信号量、同库行锁、跨实例/跨服务的幂等或协议。
7. 能沿真实源码解释 Agent Run 准入：用户级数据库 Gate 与模型调用 `Semaphore` 分别解决什么问题。

## 2. 学习顺序

| 顺序 | 知识点 | MiniPay 入口/练习 | 验收 |
|---|---|---|---|
| 4.0 | Day 3 MVC 补课 | `MvcServiceFlowPractice.java` | 运行并说清输入、Service、结果三者关系 |
| 4.1 | HashMap 深入 | Day 1 笔记 + 最小冲突示意 | 能口述 `put`、扩容、树化的目的 |
| 4.2 | 线程、共享数据、竞态 | `CounterRacePractice.java` | 能说明为什么“读取→加一→写回”不是原子操作 |
| 4.3 | `synchronized`、`volatile`、CAS、原子类 | `CounterRacePractice.java` | 能选出正确工具并说出不解决什么 |
| 4.4 | `ConcurrentHashMap` 与复合操作 | `ConcurrentMapPractice.java` | 能区分 `putIfAbsent` 与 `containsKey + put` |
| 4.5 | 线程池、超时与任务隔离 | `ThreadPoolPractice.java` | 能解释队列、拒绝策略和超时的意义 |
| 4.6 | Agent Run 准入与信号量 | `AgentRunApplicationService`、`MyBatisAiAgentRepository`、`SpringAiModelGateway` | 能区分行锁 Gate 与 JVM `Semaphore` |
| 4.7 | 口述、场景题、记录 | 本文档与练习目录 | 10 道口述题、2 道场景题、运行证据 |

## 3. 只读三个真实入口

1. `services/agent-service/src/main/java/com/minipay/agent/application/service/AgentRunApplicationService.java`：谁在发起 Agent Run、何时请求准入。
2. `services/agent-service/src/main/java/com/minipay/agent/infrastructure/persistence/ai/MyBatisAiAgentRepository.java`：用户级 Gate 怎样通过数据库行锁跨实例生效。
3. `services/agent-service/src/main/java/com/minipay/agent/infrastructure/model/SpringAiModelGateway.java`：`Semaphore` 怎样限制当前 JVM 内同时进行的模型调用。

先记职责，不进入所有实现细节：

```text
用户发起 Run
  → Application Service 请求用户级 Gate（数据库行锁，跨实例）
  → 允许后创建/推进 Run
  → 模型 Gateway 取得 Semaphore 许可（当前 JVM 内）
  → 调用模型并释放许可
```

`Semaphore` 不能代替数据库 Gate；`ConcurrentHashMap` 也不能代替消息幂等或数据库唯一约束。

## 4. 今日安全边界

- 先读源码、做独立练习和补充注释，不修改 Agent Run 的真实准入规则。
- 不用 `ConcurrentHashMap` 假装解决多实例、MQ 重复消费或资金一致性。
- 不持有数据库行锁时调用远程模型；真正修改并发策略前必须先补测试并记录到项目改进台账。

## 5. Day 4 口述题（当天完成）

1. `HashMap.put` 为什么先算哈希再找桶？
2. 为什么容量通常是 2 的幂？
3. 负载因子 0.75 解决什么取舍？
4. 什么是竞态条件？
5. `volatile` 为什么不能让 `count++` 原子？
6. CAS 适合解决什么问题，失败时会怎样？
7. `synchronized` 与 `ReentrantLock` 的最初区别是什么？
8. 为什么 `ConcurrentHashMap` 不允许 `null` 值？
9. `putIfAbsent` 为什么比 `containsKey + put` 更合适？
10. Agent 的数据库 Gate 与 `Semaphore` 分别保护哪一层？

## 6. Day 4 场景题（当天完成）

1. 两个请求同时让同一用户创建 Agent Run，为什么只靠一个 JVM 的 `Semaphore` 不够？
2. 两个 MQ 消费者收到同一个事件，为什么不能只用 `ConcurrentHashMap` 记录 `eventId` 去重？

## 7. 收尾要求

1. 每个练习记录实际输出与观察；不能只写计划。
2. 将 HashMap 深入、并发工具、Agent 准入分别标记为 completed、scheduled carryover 或 intentional later-depth。
3. 记录误区：局部变量隔离不等于所有并发安全；单 JVM 工具不等于分布式一致性。
4. 更新总进度、练习索引、补课台账和项目改进台账；通过测试后才提交。

## 8. Java 面试 PPT 融合（不替换 Day 4 主线）

对应总表：[Java 面试 PPT 覆盖地图](java-interview-ppt-map.md)。本日只处理并发篇与集合篇的关联内容；JVM、MySQL、Redis 等留在原定日期。

### 八股融合

1. JMM 三特性：原子性、可见性、有序性；用 `CounterRacePractice` 和 `AtomicCounterPractice` 对照说明。
2. `synchronized`、`volatile`、CAS、`AtomicInteger`：每个工具解决什么、不解决什么。
3. AQS、`ReentrantLock`、`Semaphore`：先建立用途直觉，再回到 Agent Run 的 JVM 内信号量。
4. `ConcurrentHashMap`：JDK 8+ 的桶级同步/CAS 思路，以及“单个 Map 操作安全不等于业务流程原子”。
5. 线程池：核心参数、阻塞队列、拒绝策略、超时；后续再结合下游并行调用。

### 八股收尾（40 分钟）

- 进程与线程、并发与并行。
- `start()` 与 `run()`、线程状态、`wait()` 与 `sleep()`。
- JDK 7 `ConcurrentHashMap` Segment 与 JDK 8+ 实现的区别；MiniPay 使用 Java 21，不把旧实现当作当前源码结论。

### 本日额外验收

- 对上述每个 P0 题用自己的话回答 2 分钟；答错项写入 `learning-gap-ledger.md`，并标记到 Day7/Day14 复习。
- 完成 `ConcurrentHashMapPractice2.java` 后，记录两线程均调用 `computeIfAbsent`、但只有一个线程真正创建会话的输出证据。
