# Day 4：Java 并发与 Agent Run 练习区

这里只放 Day 4 的独立小练习，不参与 MiniPay 的正式 Maven 构建，也不修改 Agent、支付或钱包的真实规则。

## 顺序

1. `MvcServiceFlowPractice.java`：补做 Day 3 的“输入对象 → Service → 返回对象”边界练习。
2. `CounterRacePractice.java`：观察普通共享计数、`synchronized`、原子类的差异。
3. `ConcurrentHashMapPractice2.java`：比较普通复合操作与 `putIfAbsent` / `computeIfAbsent` 的单键原子操作。
4. `ThreadPoolPractice.java`：理解任务、队列、超时和拒绝策略。

每个文件都要在写完后补充：目的、运行命令、实际输出和自己的观察。不要提前复制答案。
