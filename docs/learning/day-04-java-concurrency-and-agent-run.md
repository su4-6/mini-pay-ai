# Day 4：HashMap 深入、Java 并发与 Agent Run

> 复习重点已同步至 [重点资料库](key-concepts.md#day-4并发工具先按保护目标区分)：不要把本日独立练习误认为同一条登录流程；尤其要区分线程池、`ConcurrentHashMap`、锁与 `Semaphore` 各自限制的对象。

> 分支：`8.28`。实际学习日期与分支日期分开记录：2026-08-27 用于收尾 Day 3，本分支才开始 Day 4。

> 本日是 [8 周核心执行计划](eight-week-core-plan.md) 第 1 周的当前学习单元；开始前先按 [学习首页](README.md) 的固定顺序阅读本讲义、[补课台账](learning-gap-ledger.md) 和 [Java 面试 PPT 覆盖地图](java-interview-ppt-map.md)。

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

## Spring AI 并发增量（追加，不替代 Day4）

今天的 `Semaphore` 也对应 Agent Service 的三层并发边界：

1. `SpringAiModelGateway` 的 `Semaphore`：限制单个实例向模型发出的调用数。
2. `AgentRunApplicationService` 的数据库 Gate：避免多个实例为同用户/同会话创建冲突 Run。
3. `AgentRuntimeConfiguration` 的虚拟线程执行器：负责异步运行任务，不是并发上限本身。

Day4 只建立三层边界认识，不提前改业务代码。Day38 将在测试保护下处理 `classifyPendingTransferTurn` 未统一申请模型许可的改进项（AI-001）。完整台账见 [spring-ai-learning-map.md](spring-ai-learning-map.md)。

## Day4 收尾记录（2026-09-05）

当前 checkout：`interview-sprint`。Day4 的历史分支标签仍按计划记录为 `8.28`；本次只完成 Day4 收尾，不初始化 Day5。

### 今日学习清单

| 计划项 | 实际证据 | 分类 |
|---|---|---|
| Day3 转入的 MVC 小练习 | `MvcServiceFlowPractice.java` 输出“小明支付100元” | completed |
| HashMap 与集合收尾 | 完成 `put`、哈希/桶、冲突、2 的幂、0.75、扩容和树化的口述检查 | completed |
| 共享状态、JMM、CAS、原子类 | `AtomicCounterPractice.java` 输出期望次数 `20000`、实际次数 `20000` | completed |
| ConcurrentHashMap 复合操作 | `ConcurrentHashMapPractice2.java` 显示只有一个线程真正创建会话，两个线程取得同一会话 | completed |
| CompletableFuture 与结果边界 | 钱包失败会使支付页组装失败；推荐查询可经 `exceptionally` 降级；`get(timeout)` 不取消后台工作 | completed |
| 线程池、拒绝、关闭和取消 | 已观察队列等待、CallerRuns、`shutdownNow()` 协作中断，以及只取消任务 3 的 `Future.cancel(false)` | completed |
| Agent Run 并发边界 | 已跟读 Application Service、持久化 Gate、Model Gateway，并运行 Agent 定向测试 | completed |
| MQ Inbox / eventId 的完整实现 | 仅完成“JVM 内 Map 不能替代跨实例持久化幂等”的边界判断；保留 Day10--12 原计划 | intentional later-depth |

### 实际运行与测试证据

- `CompletableFuturePractice.java`：钱包任务抛出异常后，最终输出“支付页组装失败…钱包服务暂时不可用”；钱包是必需输入，推荐降级不会掩盖钱包失败。
- `ThreadPoolPractice.java`：两个任务开始后主线程 500ms 即执行 `shutdownNow()`；工作线程在 `sleep` 收到中断后恢复中断标记并返回，排队任务 3、4 没有开始。
- `FutureCancelPractice.java`：输出“任务3取消结果：true”，任务 1、2、4 完成；取消句柄只影响任务 3。
- `CustomThreadPoolPractice.java`：任务 7 被拒绝；CallerRuns 是提交任务的线程自己执行，不是悄悄丢掉资金相关动作。
- `CountDownLatchPractice.java`：全量复跑发现正常完成分支遗漏线程池关闭；已改为 `finally` 统一 `shutdown()`，避免程序打印完成后仍不退出。
- `mvn -f services/agent-service/pom.xml -Dtest=AgentRunApplicationServiceTest test`：6 tests、0 failures、0 errors，BUILD SUCCESS。

### 纠正并保留的理解锚点

- `CountDownLatch` 归零表示任务结束，不表示业务成功；逐项结果由 `Future.get()` 或异常决定。
- `submit` 不会阻塞主线程；因此后面的 `shutdownNow()` 会立刻运行。中断只是请求，任务要在可中断点捕获后自行停止。
- 线程池释放不能只写在超时或异常分支；正常、超时和中断路径都应收敛到统一清理逻辑。
- JVM 内 `Semaphore` 限制当前实例的模型调用；数据库用户 Gate 才能让多个实例共同遵守同一用户的 Run 准入上限。虚拟线程负责调度，不是全局配额。
- `ConcurrentHashMap` 的桶级并发控制是实现方式；业务“查再创建”仍需原子复合操作或数据库事务/约束。
- Spring 单例 Service 可被多个请求线程同时调用；请求专属数据放参数或局部变量，不放可变成员字段。

### 口述、场景与下一步

Day4 的 10 道口述题和 2 道场景题已完成；薄弱点转为 Day7、Day14、Day21 复习。`wait()` 与虚拟线程等待的比较是计划外问题，已记录在 `optional-extension-backlog.md`，不阻塞 Day4。

Day4 已关闭。Day5 尚未创建计划、目录或分支；下次开始前先做 Day4 闭卷回忆，再按原计划进入 Day5 JVM。

## 完成前的重点锚点与继续位置（存档）

已完成并列为重点复习：`CountDownLatchFuturePractice.java` 的并行支付页查询实验。

- 已验证：`CountDownLatch` 只等待所有任务结束；`countDown()` 放在 `finally` 中，所以失败任务也会让计数归零。
- 已验证：超时返回 `false` 不会自动取消后台任务；之后它们仍可能继续输出“查询完成”。
- 已验证：`Future.get()` 才能得到每一项的返回值，或通过 `ExecutionException.getCause()` 看到具体失败原因。
- 已纠正误区：不能凭“是否打印成功日志”判断任务成功；日志位置不是结果契约。
- AQS 连接：`CountDownLatch` 使用 AQS 管理剩余计数和 `await()` 等待/唤醒；AQS 不判断业务成功失败。

完成状态：此处原定的 `CompletableFuture`、线程池超时/取消和项目并发边界复盘均已完成；本段仅保留为学习过程存档，不再作为继续位置。

详细复习材料见 [重点资料库](key-concepts.md#8-重点锚点并行查询countdownlatchfuture-与-aqs)。
