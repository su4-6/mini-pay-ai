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
