# Day 5：JVM、GC、类加载与 OOM

> 分支：`9.5`。状态：计划已初始化，尚未开始教学、练习或验收。

> 本讲义承接 [8 周核心执行计划](eight-week-core-plan.md) 和冻结的 [Day1--30 主计划](original-30-day-baseline.md)。完整 CPU/OOM/Full GC 故障演练仍属于 Day28；Day5 只做安全、可停止、仅作用于学习进程的基础观察。

## 0. 开场：15 分钟闭卷复习

不看 Day4 笔记，先用自己的话回答：

1. 数据库用户 Gate 与单 JVM `Semaphore` 分别限制什么，为什么不能互相替代？
2. `CountDownLatch.await()` 与 `Future.get()` 分别得到什么信息？
3. `shutdownNow()` 与 `Future.cancel(false)` 的作用范围和中断行为有什么不同？

答错时先记录原答案，再核对 Day4 资料；复习不替代本日 JVM 主线。

## 1. 完成标准

Day5 只有同时满足以下条件才能标记完成：

1. 能区分程序计数器、Java 虚拟机栈、堆、元空间和直接内存，并说出线程私有/共享边界及常见异常。
2. 能解释 GC Roots、可达性分析、标记-清除、复制、标记-整理、分代回收分别解决什么问题。
3. 能用初学者层次解释 G1 的 Region、回收阶段，以及 Minor、Mixed、Full GC 的区别；不把收集器细节背成固定生产参数。
4. 能口述加载、验证、准备、解析、初始化和双亲委派，并把类加载连接到 Spring Boot 启动入口。
5. 能对自己的学习进程安全使用 `jcmd`/`jstack` 观察内存、GC 和线程；能口述 CPU、OOM、死锁的第一轮排查顺序。
6. 完成 10 道口述题、2 道场景题、实际输出记录、错误记录和复习安排。

## 2. 固定学习顺序

| 顺序 | 内容 | 最小证据 |
|---|---|---|
| 5.0 | Day4 闭卷复习 | 记录 3 题中的错误和纠正 |
| 5.1 | JVM 运行时内存：PC、栈、堆、元空间、直接内存 | 内存区域练习输出 + 口述 |
| 5.2 | GC Roots、可达性、回收算法、分代 | 可达性练习输出 + 对象关系图 |
| 5.3 | G1、Minor/Mixed/Full GC | 说明触发背景和边界，不背绝对化结论 |
| 5.4 | 类加载过程、双亲委派、Spring Boot 启动 | 类加载顺序输出 + 启动链口述 |
| 5.5 | 安全观察：小堆 OOM、CPU、线程与死锁线索 | 只观察学习子进程，保留命令和现象 |
| 5.6 | Actuator/Prometheus 的 JVM、线程与 GC 指标边界 | 能区分指标暴露、采集、告警和诊断 |
| 5.7 | 口述、场景题、错误与复习安排 | 10 道口述题 + 2 道场景题 |

## 3. 最多三个真实源码入口

1. [根 pom.xml](../../pom.xml)：`java.version=21` 和 Maven Enforcer 的 `[21,22)` 是构建门禁；它规定项目使用的 Java 版本，不负责 JVM 运行时调优。
2. [AgentServiceApplication](../../services/agent-service/src/main/java/com/minipay/agent/AgentServiceApplication.java)：`main(...)` 是 Java 进程入口，`SpringApplication.run(...)` 再启动 Spring Boot 容器。
3. [Agent application.yml](../../services/agent-service/src/main/resources/application.yml)：当前只暴露 `health`、`info`、`prometheus`；暴露指标不等于已经部署 Prometheus、配置告警或完成故障诊断。

Day5 不修改这三个真实入口，只用它们连接“Java 21 门禁 → 类进入 JVM → Spring Boot 启动 → 指标暴露”的认识。

## 4. 按教学进度创建的安全练习

练习目录：[learning-labs/day05-jvm-gc](../../learning-labs/day05-jvm-gc/)。下列文件目前都未创建，只有学到对应小节时才逐个手写、运行和补注释：

1. `JvmMemoryPractice.java`：观察局部变量、对象和线程栈的基本边界。
2. `GcReachabilityPractice.java`：通过对象引用关系理解“可达/不可达”，不依赖强制 GC 结果做绝对结论。
3. `ClassLoadingPractice.java`：记录静态字段、静态代码块和实例初始化的可观察顺序。
4. `SafeOomPractice.java`：仅在单独学习进程使用小堆（计划 `-Xmx32m`），捕获并记录现象后退出；不生成 Heap Dump，不作用于 MiniPay 服务。
5. `JvmObservationPractice.java`：保持一个可识别的学习进程，用 `jcmd`/`jstack` 只读取该进程的信息。

安全边界：不附加生产或无关进程，不运行无限后台任务，不修改系统 JVM 参数，不用实验结果冒充生产容量结论。

## 5. Actuator/Prometheus 边界

- Actuator/Micrometer 负责在应用内产生并暴露 JVM、线程和 GC 等指标入口。
- Prometheus 是否抓取、抓取频率、数据保留和告警规则属于后续可观测性配置；仅看到 `/actuator/prometheus` 不能证明告警已经生效。
- 指标适合发现趋势和缩小范围；OOM 根因、CPU 热点和死锁仍要结合线程栈、内存信息、日志和复现证据。
- Day5 建立观察方法，Day25--26 完成监控/链路主线，Day28 才做完整故障演练。

## 6. 10 道口述题

1. JVM、JRE、JDK 的关系是什么？
2. 程序计数器、栈、堆分别存放或跟踪什么，哪些是线程私有的？
3. 元空间和直接内存为什么不能简单说成“都在堆里”？
4. `StackOverflowError` 与 `OutOfMemoryError` 的典型区别是什么？
5. 什么是 GC Roots，可达性分析为什么比单纯引用计数更可靠？
6. 标记-清除、复制、标记-整理各有什么主要取舍？
7. 分代回收的基本依据是什么？Minor、Mixed、Full GC 有什么区别？
8. G1 为什么把堆划分为 Region，它的 Mixed GC 大致回收哪些区域？
9. 类从加载到初始化经历哪些阶段，双亲委派解决什么问题？
10. Actuator 指标、`jcmd` 和 `jstack` 各能提供哪一类线索，为什么单独一个工具不能直接给出全部根因？

## 7. 两道场景题

1. Agent Service 某实例 CPU 持续升高，但请求量没有同步上涨：第一轮要保留哪些指标、线程和日志证据，怎样避免一上来就重启导致证据丢失？
2. 某实例频繁 Full GC 后出现 OOM：如何区分“堆太小”“对象被长期引用”“堆外/线程过多”等方向，并说明 Day5 能验证到哪、Day28 还要补什么？

## 8. 证据与错误记录（学习后填写）

### 实际输出

- 尚无。每完成一个练习，记录运行命令、关键输出、现象和边界，不只写“运行成功”。

### 错误记录

| 不会或答错的点 | 正确理解 | 证据 | 下次复习 |
|---|---|---|---|
| 待学习后填写 | — | — | Day7 / Day14 / Day21 / Day28 |

### 复习安排

- Day7：闭卷复述内存区域、GC Roots、类加载顺序。
- Day14：复述算法、分代、G1 和 Minor/Mixed/Full GC。
- Day21：结合服务并发与线程状态复习 JVM 观察。
- Day28：完成 CPU、OOM/Full GC、线程和死锁的完整故障演练与复盘。

## 9. PPT 对照与当前继续点

- 09 JVM 篇第 2--31 页：运行流程与运行时内存。
- 第 34--49 页：类加载器、双亲委派、加载到初始化。
- 第 52--98 页：可达性、GC Roots、算法、分代、收集器、G1 和引用类型。
- 第 101--134 页：参数、观察工具、内存泄漏和 CPU 排查；Day5 建立基础，完整深度保留 Day28。
- 08 并发篇第 85--90 页：死锁条件及 `jps`/`jstack` 诊断，与 Day5 线程观察连接。

当前继续点：先进行第 0 节的 15 分钟闭卷复习，从第 1 题开始；尚未进入 JVM 新课。
