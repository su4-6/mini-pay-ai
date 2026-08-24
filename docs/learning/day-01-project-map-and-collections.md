# Day 1：集合与 MiniPay 项目地图

## 今日目标

完成今天后，你应当能够：

- 说出 HashMap 的数据结构和 `put` 主流程。
- 解释 HashMap 为什么线程不安全、ConcurrentHashMap 如何降低锁竞争。
- 说出 ArrayList 的特点以及 fail-fast 的真实含义。
- 不看资料说出 MiniPay 八个后端模块、端口和数据归属。
- 知道后续读代码时从哪里进入，而不是在整个仓库里乱翻。

## 一、先做诊断题

不要查资料，先用自己的话回答：

1. `HashMap` 为什么不能保证线程安全？
2. 两个不同对象的 `hashCode()` 相同，是否一定代表它们 `equals()`？
3. `ConcurrentHashMap` 是不是给整个 Map 加了一把大锁？
4. `payment-service` 能否直接通过 SQL 修改钱包余额？为什么？
5. 浏览器访问商户后台时，是直接调用 Payment，还是先进入哪个模块？

答不上来是正常的；这些答案就是今天的学习目标。

## 二、HashMap：从一次 put 理解

Java 8 之后，HashMap 可以先理解为：

```text
Node 数组
├─ 空桶
├─ 链表：Node → Node → Node
└─ 红黑树：冲突很多时降低查找复杂度
```

`put(key, value)` 的主流程：

1. 对 key 的 `hashCode` 做扰动，让高位信息也参与桶下标计算。
2. 数组尚未初始化时先初始化。
3. 使用 `(capacity - 1) & hash` 定位桶，所以容量采用 2 的幂。
4. 桶为空时直接插入。
5. 桶不为空时依次判断：是否同 key、是否红黑树、否则遍历链表。
6. 同 key 更新 value；新 key 插入链表或树。
7. 链表达到树化阈值时尝试树化；数组容量太小时优先扩容。
8. 元素数量超过 `capacity × loadFactor` 后扩容。

默认负载因子 0.75 是时间与空间的折中。链表达到 8 个节点且数组容量至少 64 时才树化；容量不足时优先扩容。删除后节点数量降低时可以退化回链表。

### 为什么 HashMap 线程不安全

问题不只是“它没加锁”。复合操作会发生竞态：

```text
线程 A 读取桶状态 ─┐
                    ├─ 两者都基于旧状态写入，可能覆盖更新
线程 B 读取桶状态 ─┘
```

并发写入、扩容、遍历与修改之间都缺少正确的可见性和原子性保证。即使某次测试没有报错，也不能证明它是安全的。

## 三、ConcurrentHashMap

Java 8 的 ConcurrentHashMap 不再使用早期版本的固定 Segment 设计。可用以下模型理解：

- 读取大多不加互斥锁，依靠 volatile 可见性读取节点。
- 空桶插入常用 CAS 竞争。
- 桶已有节点时，更新会对桶头节点做较细粒度同步。
- 扩容时多个线程可以协助迁移桶。
- 计数采用分散竞争的思路，避免所有更新争抢一个计数变量。
- 不允许 `null` key/value，避免并发语境下无法区分“没有映射”和“映射值为 null”。

它保证单个方法的线程安全，不会自动让多个方法组成的业务流程原子化。例如：

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

仍然是竞态，应使用 `putIfAbsent`、`computeIfAbsent` 等原子复合方法，或在更高层建立同步边界。

## 四、ArrayList 与 fail-fast

ArrayList 是动态数组：随机访问快，中间插入/删除需要移动元素，扩容需要创建更大数组并复制。它不是线程安全集合。

fail-fast 是迭代器发现集合结构修改计数不符合预期后，尽快抛出 `ConcurrentModificationException`。它是发现错误的“尽力机制”，不是并发安全保证，也不能依赖它检测所有竞态。

## 五、把集合与 MiniPay 联系起来

集合底层属于独立面试基本功；MiniPay 能提供的是并发边界对照：

| 问题范围 | 典型工具 | MiniPay 对应场景 |
|---|---|---|
| 单个 JVM 内共享对象 | ConcurrentHashMap、Lock、CAS、线程池 | Agent 实例内并发控制 |
| 同一数据库记录竞争 | 行锁、条件更新、唯一约束 | Outbox 抢占、状态机推进 |
| 重复消息 | Inbox `eventId`、幂等状态迁移 | Payment/Commerce 事件消费 |
| 跨服务资金动作 | Seata TCC | Payment 编排 Wallet Debit/Credit |

因此，“用了 ConcurrentHashMap”不能解决 MQ 重复消费；“数据库加锁”也不能替代跨服务补偿协议。

## 六、今天只读三个入口

1. `pom.xml`：确认这是包含八个模块的 Maven 聚合工程，以及 Java/Spring Boot/Seata 版本。
2. `compose.yaml`：确认 MySQL、Redis、RabbitMQ、Seata 和应用服务的启动依赖、端口、服务名。
3. `docs/architecture.md`：确认服务 owner、调用方向、TCC 与 Saga 的选择。

阅读时只填写下面的表，不进入业务源码：

| 模块 | 端口 | 拥有的数据 | 主要调用对象 | 禁止行为 |
|---|---:|---|---|---|
| identity-service | | | | |
| payment-service | | | | |
| wallet-service | | | | |
| agent-service | | | | |
| commerce-service | | | | |
| consumer-bff | | | | |
| management-bff | | | | |
| admin-bff | | | | |

## 七、今日实操

在后端仓库根目录执行只读检查：

```powershell
git status --short --branch
Select-String -Path pom.xml -Pattern '<module>'
docker compose -f compose.yaml --profile apps config --services
```

如果 Docker 尚未运行，第三条失败不算未通过；记录错误现象即可，不要在今天启动或删除容器。

## 八、口述验收

关闭讲义，逐题回答：

1. HashMap 为什么用数组 + 链表 + 红黑树？
2. `put` 一个元素的大致过程是什么？
3. 为什么容量通常是 2 的幂？
4. 为什么链表达到 8 不一定马上树化？
5. HashMap 线程不安全体现在哪？
6. ConcurrentHashMap 如何降低并发写的锁竞争？
7. fail-fast 是否等于线程安全？
8. MiniPay 为什么把 Payment 和 Wallet 分开？
9. 三个 BFF 分别面向谁，为什么不能合成一个共享 Session？
10. TCC 与外卖 Saga 分别用于哪类场景？

场景题：

1. 两个 MQ 消费线程同时收到相同 `eventId`，使用 ConcurrentHashMap 去重是否足够？为什么？
2. 面试官说“你们没有 Nacos 和 Gateway，算什么微服务”，你怎么结合当前项目回答？

## 九、通过标准

- 10 道口述题至少 8 道能连续回答 60 秒。
- 两道场景题必须说明“作用范围”和“失败后如何恢复”。
- 八个服务表填写完整，且 Payment/Wallet/Commerce 的 owner 不混淆。
- 将不清楚的问题发给我；我逐题纠正后再进入 Day 2。
