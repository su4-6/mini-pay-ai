# Day 5 JVM / GC 学习实验区

> 状态：仅初始化目录和计划，尚未创建练习答案或形成运行证据。

本目录只放 Day5 跟随教学逐个手写的独立 Java 小程序，不参与 MiniPay Maven 正式构建，也不修改任何业务服务。

## 创建顺序

1. `JvmMemoryPractice.java`
2. `GcReachabilityPractice.java`
3. `ClassLoadingPractice.java`
4. `SafeOomPractice.java`
5. `JvmObservationPractice.java`

每个练习只有学到对应概念后才创建，并补齐简洁中文注释：练习目的、输入、输出、对应 JVM 区域或工具边界、纠正的误区。

## 安全规则

- OOM 练习只在单独的短生命周期学习进程中以小堆运行，计划上限为 `-Xmx32m`；不生成 Heap Dump，不启动 MiniPay 服务。
- `jcmd`、`jstack` 只读取本目录启动的学习进程，不附加生产、IDE 或其他无关 Java 进程。
- 不写无限循环后台任务；观察结束后应能明确停止学习进程。
- 实验输出只说明本机本次现象，不作为生产容量、GC 参数或性能结论。

完整学习顺序、口述题、场景题和验收标准见 [Day5 讲义](../../docs/learning/day-05-jvm-gc-classloading-oom.md)。
