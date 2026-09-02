# MiniPay 重点资料库

> 这里收录已经学过、容易混淆、需要反复口述的结论。它不是新课程；每一条都要能对应到练习代码或真实 MiniPay 源码。

## Day 4：并发工具先按“保护目标”区分

### 1. 先判断：线程之间到底有没有共享东西

大多数方法参数、局部变量和本次临时创建的结果对象，都只属于当前这一次调用。多个线程可以各自使用它们，通常互不影响。

```text
线程 A 调用 create("user1")：有自己的一份 userId、session 局部变量
线程 B 调用 create("user2")：也有自己的一份 userId、session 局部变量
```

需要并发控制的是共享状态或有限资源，例如：同一个 Map、同一个计数器、同一条数据库记录、同一个模型接口并发名额。

### 2. 已敲练习不是一条完整业务流程

下面的文件是刻意拆开的独立实验。不要把它们默认理解成“登录必须依次经过线程池、锁、Semaphore”。

| 练习 | 代码实际要说明什么 | 没有使用什么 |
|---|---|---|
| `ConcurrentHashMapPractice2.java` | 两个线程同时处理同一个 `user1` 时，在线标记只首次写入、会话只真正创建一次 | 线程池、`ReentrantLock`、`Semaphore` |
| `ThreadPoolPractice.java` / `CustomThreadPoolPractice.java` | 任务如何获得线程执行、排队或被拒绝 | 在线标记、会话、`Semaphore` |
| `TryLockPractice.java` | 两个线程竞争同一把显式锁时，是等待还是快速拒绝 | Map 按 key 去重、线程池、`Semaphore` |
| `SemaphorePractice.java`（后续） | 已执行的任务走到模型调用时，最多多少个能同时进入 | 登录标记、会话创建 |

### 3. 每种工具限制什么

| 工具 | 限制对象 | 目的 | MiniPay/练习对应 |
|---|---|---|---|
| 线程池 | 整份任务可使用的工作线程和等待队列 | 防止无限创建线程；定义排队与拒绝边界 | `ThreadPoolPractice`、`CustomThreadPoolPractice` |
| `ConcurrentHashMap` | 一张共享 Map 的单个 key 操作 | 防止并发下重复放入、重复创建、读写不安全 | `onlineUsers.putIfAbsent`、`sessions.computeIfAbsent` |
| `AtomicInteger` / CAS | 一个共享数值的读改写 | 防止 `count++` 丢失更新 | `AtomicCounterPractice` |
| `synchronized` / `ReentrantLock` | 一段必须整体执行的共享代码 | 同一时刻只允许一个线程修改这段临界区 | `SynchronizedCounterPractice`、`TryLockPractice` |
| `Semaphore` | 某种有限资源的同时使用人数 | 不让下游资源被同时压垮 | 后续：模型调用并发数 |

### 4. `user1` 登录与创建会话：只涉及 ConcurrentHashMap

`ConcurrentHashMapPractice2.java` 的真实逻辑是：

```text
线程 A、线程 B 都处理 user1
        ↓
onlineUsers.putIfAbsent(user1, ONLINE)
        ↓
只有第一个成功写入；另一个读到旧值，不重复标记
        ↓
sessions.computeIfAbsent(user1, 创建会话函数)
        ↓
只有一个线程真正执行“创建会话函数”；两者最终拿到同一个会话
```

它不受 `Semaphore` 影响。因为这个练习没有调用 AI 模型，也没有任何“总名额有限”的资源。

### 5. 线程池与 Semaphore 的准确关系

```text
线程池：任务由谁处理？没有空闲线程时是排队还是拒绝？
Semaphore：已经在处理任务的线程，能否进入某个有限步骤？
```

它们可以组合，但不是必须组合。例如真实 Agent Run 可以是：

```text
请求被服务线程处理
  ↓
执行参数校验、查状态等普通步骤
  ↓
到“调用 AI 模型”这一步
  ↓
Semaphore(2) 只允许两个线程同时调用模型
  ↓
调用完成后 release() 归还许可
```

如果线程池为 4、`Semaphore(2)`：最多 4 个线程能处理完整请求，但同一时刻只有 2 个线程正在调用模型。

### 6. 一句面试答案

> 线程池限制任务执行的线程数量、队列与拒绝边界；ConcurrentHashMap 等工具保护共享数据的一次操作；锁保护必须整体执行的临界区；Semaphore 不创建或调度线程，而是限制已执行线程对某个有限资源的并发访问数。

### 7. 工具选择快问快答

| 问题 | 优先考虑 |
|---|---|
| 任务太多，不能无限创建线程 | 线程池、队列、拒绝策略 |
| 同一个 Map 的 `user1` 只能放入一次 | `ConcurrentHashMap.putIfAbsent` |
| key 不存在时才创建其对应值 | `ConcurrentHashMap.computeIfAbsent` |
| 一个计数器必须准确加一 | `AtomicInteger` |
| 多行代码必须作为整体执行 | `synchronized` 或 `ReentrantLock` |
| 只能同时调用两个模型接口 | `Semaphore(2)` |
| 多实例同时修改同一笔资金或业务状态 | 数据库事务、行锁/条件更新、幂等协议；不能只靠 JVM 工具 |

### 8. 重点锚点：并行查询、`CountDownLatch`、`Future` 与 AQS

本锚点对应练习：`learning-labs/day04-java-concurrency/CountDownLatchFuturePractice.java`。

场景：组装“支付确认页”前，同时查询用户信息、钱包信息和支付方式。它们是三项互不依赖的查询任务；主线程只能在规定时间内确认页面所需的必要数据是否完整。

```text
主线程提交 3 个查询任务
        ↓
每个任务结束时（成功、业务失败、被中断都算结束）在 finally 执行 latch.countDown()
        ↓
latch.await(timeout) 只回答：规定时间内，三个任务是否都已经结束？
        ↓
若为 true，逐个 Future.get() 取真实结果
        ↓
get() 正常返回 → 得到该查询数据；get() 抛 ExecutionException → 得到该任务真实失败原因
```

必须区分四件事：

| 机制 | 这段代码里解决什么 | 它**不能**证明什么 |
|---|---|---|
| `finally { latch.countDown(); }` | 无论查询如何退出，都通知“这项任务结束了”，避免主线程一直等 | 不能证明任务成功 |
| `latch.await(5, TimeUnit.SECONDS)` | 在 5 秒内等待三个任务都结束；返回 `false` 表示到点还没结束 | 不能指出是哪项任务失败，也不会主动取消仍在运行的任务 |
| `Future<String>` | 保存每个异步任务将来的结果或异常 | 不会替你决定页面是否允许降级组装 |
| `future.get()` | 取得具体数据；任务失败时抛 `ExecutionException`，根因在 `getCause()` | 不等于所有其它 Future 也失败 |

#### 这次纠正：日志不是任务成功的判据

我曾疏忽地把 `System.out.println(taskName + "查询完成")` 当成“任务成功”的证据。正确理解是：日志只说明程序走到了**写日志的那一行**。

- 写完“查询完成”日志后，后续数据转换、校验、组装仍可能失败。
- 没看到日志，也可能是日志组件/输出链路异常，不能单独用来证明业务失败。
- 真实结果必须依靠明确的返回值、异常和状态：本练习中就是 `Future.get()` 的返回值或异常。

因此，钱包查询即使在 `finally` 中把 latch 减到了 0，主线程仍会在 `walletFuture.get()` 看到“钱包服务暂时不可用”的真实异常；这时必要数据不完整，正确行为是不组装支付页并返回失败/降级响应。

#### 必要数据与可选数据：不是所有失败都同一种处理

```text
支付确认页：用户、钱包、支付方式 = 必要数据
任一项失败或超时 → 不组装页面，返回明确错误

推荐商品：可选数据
推荐查询失败 → 可显示“推荐暂不可用”，但不能伪造成功数据
```

是否允许降级是业务规则，不由 `CountDownLatch` 或 `Future` 自动决定；支付、钱包等权威数据默认按必要数据处理。

#### AQS 在这里实际做什么

`CountDownLatch` 的底层协调建立在 AQS（`AbstractQueuedSynchronizer`）上。可把它理解为 Java 并发工具内部复用的“状态与等待队列管理员”，不是业务规则制定者。

```text
初始计数 = 3
任务完成一次 → countDown() 让内部状态减一
主线程 await() → 当状态不是 0 时进入等待
状态减到 0 → AQS 唤醒等待中的主线程
```

AQS 不知道“钱包服务”“支付页”“成功/失败”；它只协调“计数何时归零、谁该等待、何时唤醒”。所以 `CountDownLatch` 要和 `Future` 配合：前者负责**整体结束信号**，后者负责**逐项结果与错误**。

后续必须回扣本锚点的主题：`CompletableFuture` 并行聚合与异常降级、线程池超时/取消、下游 HTTP 超时、可观测性中的失败码与 `traceId`。
