# Day 4：Java 并发与 Agent Run 练习区

## 2026-09-05 收尾运行记录

| 文件 | 实际观察 |
|---|---|
| `MvcServiceFlowPractice.java` | 输出“小明支付100元”，复习输入对象 → Service → 返回对象。 |
| `AtomicCounterPractice.java` | 期望次数与实际次数均为 `20000`，对照普通共享计数的竞态。 |
| `ConcurrentHashMapPractice2.java` | 两线程均得到 `session-for-user1`，但只有一个线程打印“真正创建会话”。 |
| `CompletableFuturePractice.java` | 钱包失败使支付页失败；推荐失败可使用 `exceptionally` 返回降级值。 |
| `ThreadPoolPractice.java` | `shutdownNow()` 发出中断请求；任务通过捕获 `InterruptedException` 后返回来协作停止。 |
| `FutureCancelPractice.java` | `cancel(false)` 只取消排队中的任务 3，任务 1、2、4 仍完成。 |
| `CountDownLatchPractice.java` | `await` 正常、超时或中断都通过 `finally` 关闭线程池，程序可以正常退出。 |

运行命令（在本目录）：`java 文件名.java`。这些小程序只用于观察并发语义，不属于正式 Maven 构建。

这里只放 Day 4 的独立小练习，不参与 MiniPay 的正式 Maven 构建，也不修改 Agent、支付或钱包的真实规则。

## 顺序

1. `MvcServiceFlowPractice.java`：补做 Day 3 的“输入对象 → Service → 返回对象”边界练习。
2. `CounterRacePractice.java`：观察普通共享计数、`synchronized`、原子类的差异。
3. `ConcurrentHashMapPractice2.java`：比较普通复合操作与 `putIfAbsent` / `computeIfAbsent` 的单键原子操作。
4. `ThreadPoolPractice.java`：理解任务、队列、超时和拒绝策略。

5. `CountDownLatchPractice.java`：理解多个任务结束后，主线程再继续；`countDown()` 只代表任务结束，不代表任务成功。
6. `CountDownLatchFuturePractice.java`：把三项并行查询用于“支付确认页”组装。`CountDownLatch` 等整体结束，`Future` 保存每项结果或异常；必要数据任何一项失败/超时都不能组装页面。练习中特别记录：不要用是否出现“查询完成”日志判断成功，必须以 `Future.get()` 的返回值或异常为准。

### 本轮重点复习

`CountDownLatchFuturePractice.java` 同时连接三个知识点：

1. `finally { latch.countDown(); }`：保证每项任务退出时都通知等待方，防止卡住；不表示成功。
2. `latch.await(timeout)`：只判断是否全部结束，超时不会取消后台任务。
3. `Future.get()`：逐项取得结果；失败任务会在这里以 `ExecutionException` 暴露根因。

`CountDownLatch` 的等待/唤醒内部由 AQS 协调。AQS 管理计数与等待队列，不知道业务数据是否正确；“失败后是否拒绝页面组装或允许可选数据降级”必须由上层业务代码明确决定。

每个文件都要在写完后补充：目的、运行命令、实际输出和自己的观察。不要提前复制答案。
