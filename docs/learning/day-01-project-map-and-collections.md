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

---

# Day 1 课堂复习总览（实际完成版）

> 这部分记录的是本次实际学习、敲过和运行过的内容。复习时优先看这里；前面的内容是后续会逐步深入的知识地图，不要求今天一次全部背会。

## 1. 今天完成了什么

1. 认识 MiniPay 的服务边界：重点区分 `payment`、`wallet`、`consumer-bff`。
2. 手写并运行 `HashMap`：新增、查询、同 key 覆盖、`size()`、查询不存在的 key。
3. 手写并运行 `Map<String, Integer>`，理解泛型中的 key/value 类型。
4. 手写并运行 `ConcurrentHashMap`，理解它和 `HashMap` 的真实区别，以及 `putIfAbsent`。
5. 手写并运行 `ArrayList`：新增、按下标读取、删除、遍历。
6. 主动制造并看到了 `ConcurrentModificationException`，理解 fail-fast。
7. 使用 `removeIf` 安全删除列表项目，再用 `for` 循环只负责读取和打印。

练习代码都在：

```text
learning-labs/day01-java-basics/
├── HashMapPractice.java
├── HashMapPractice2.java
├── ConcurrentHashMapPractice.java
└── ArrayListPractice.java
```

这些练习不属于 `services/`，不参与 MiniPay 正式构建，不会影响支付、钱包等业务代码。

## 2. 如何运行今天的练习

在 IDEA 终端进入练习目录后，运行单个文件：

```powershell
cd "C:\Users\hp\Documents\Codex\mini-pay-ai-learning\learning-labs\day01-java-basics"
java HashMapPractice.java
java HashMapPractice2.java
java ConcurrentHashMapPractice.java
java ArrayListPractice.java
```

注意：`cd` 后面只能是“文件夹路径”，不能把 `HashMapPractice.java` 这样的文件名放在 `cd` 后面。

## 3. HashMap：按 key 找 value

### 3.1 最核心的模型

```text
key → value

"name" → "小明"
"city" → "北京"
```

```java
Map<String, String> user = new HashMap<>();
user.put("name", "小明");
user.put("city", "上海");

String name = user.get("name"); // 小明
```

- `Map<String, String>`：key 和 value 都只能是文本。
- `put(key, value)`：放入一组对应关系。
- `get(key)`：必须用 **key** 查询 value，不能拿 value 当 key。
- 找不到 key 时，`get` 返回 `null`，不是报错。

### 3.2 今天做过的三个现象

```java
user.put("city", "上海");
user.put("city", "北京");
```

同一个 key 只能对应一个当前 value，所以第二次会覆盖第一次：

```text
"city" → "北京"
```

因此 `user.size()` 在放入 `name` 和 `city` 后是 `2`；即使更新 `city` 的 value，`size()` 仍然是 `2`。

```java
user.get("country"); // null
```

这是因为没有 `country` 这个 key。

### 3.3 `String` 和 `Integer` 分别是什么

在下面的代码里：

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("math", 95);
scores.put("english", 80);
```

- `String`：文本，例如 `"math"`。
- `Integer`：整数对象，例如 `95`。
- 所以这张表表示：`科目名称 → 分数`。

输出 `scores.get("math")` 会得到 `95`。

### 3.4 HashMap 为什么能较快找到数据

初学阶段可以把它理解为“一排格子”：

1. key 会先计算出一个 hash（可理解为稳定的数字特征）。
2. 根据这个数字定位到某个格子（bucket，桶）。
3. 在那个格子里再比较真正的 key，找到目标 value。

你问过：“为什么要算余数，例如除以 13？”

回答：这是为了把很大的 hash 数字映射到有限个格子的某一个位置。教学中可用 `hash % 13` 举例；真正的 Java `HashMap` 容量通常是 2 的幂，采用更快的位运算来定位，而不是简单写 `% 13`。

你还问过：“两个余数一样怎么办？”

这叫 **hash 冲突**：不同 key 可能落到同一个格子。HashMap 会在该格子里继续比较 key 是否相等（本质上依赖 `equals`），所以不会因为余数相同就把两个 key 当成同一个。

它不是把所有 value 都输出；它会在同一个格子中比对到你传给 `get(...)` 的那个 key，再返回对应的 value。

## 4. ConcurrentHashMap：多线程下更安全的 Map

### 4.1 练习的含义

```java
Map<String, Integer> onlineUsers = new ConcurrentHashMap<>();
onlineUsers.put("user1", 1);
onlineUsers.put("user2", 1);
```

可以理解成一张“用户 ID → 在线状态”的临时表。

```text
user1 → 1
user2 → 1
```

`get("user1")` 得到 `1`，`size()` 得到 `2`。

### 4.2 它和 HashMap 的区别如何体现

你问过：“这段代码的意义是什么，和 HashMap 区别怎么体现？”

答案是：**在当前 `main` 方法中，只有一个线程按顺序执行，所以两者的 `put/get/size` 用法和输出几乎一样。看不出差异是正常的。**

真正区别在“多个线程同时读写同一份 Map”时：

| 场景 | HashMap | ConcurrentHashMap |
|---|---|---|
| 一个线程顺序执行 | 可以用 | 可以用 |
| 多线程同时修改 | 可能出现竞态或数据异常 | 对单个 Map 操作提供并发保护 |
| 多线程临时在线状态/缓存 | 不建议直接使用 | 更适合 |

可以类比为：

- `HashMap`：普通登记本，多个人同时抢着写可能混乱。
- `ConcurrentHashMap`：带协调规则的登记本，多人同时操作时更安全。

但它只保护 **一个 Java 进程内存中** 的这张 Map；它不能代替数据库唯一约束、分布式锁、MQ 幂等或跨服务事务。

### 4.3 `putIfAbsent`：存在就不覆盖，不存在才新增

```java
Integer oldValue = onlineUsers.putIfAbsent("user1", 2);
```

当 `user1 → 1` 已经存在时：

```text
oldValue = 1
onlineUsers.get("user1") = 1
```

它不会把 1 改成 2。

当 `user3` 原先不存在时：

```java
Integer oldValue = onlineUsers.putIfAbsent("user3", 2);
```

运行结果是：

```text
oldValue = null
onlineUsers.get("user3") = 2
size = 3
```

`oldValue` 指“写入之前已有的旧值”。此前没有 `user3`，所以旧值是 `null`；随后才成功放入 `user3 → 2`。

`putIfAbsent` 的价值是把“检查是否存在 + 新增”作为一个不应被中途插队的动作。它适合本地内存中的简单场景；MiniPay 日后的 MQ 重复消费，会使用数据库 Inbox 和 `eventId` 做持久化幂等，不能只依赖它。

## 5. ArrayList：按顺序保存一串数据

### 5.1 最核心的模型

```java
List<String> tasks = new ArrayList<>();
tasks.add("登录");
tasks.add("查看钱包");
tasks.add("发起支付");
```

```text
下标 0：登录
下标 1：查看钱包
下标 2：发起支付
```

- `List`：表示“有顺序的列表”。
- `ArrayList`：这个列表的具体实现方式。
- `<String>`：列表里只能放文本。
- 下标从 **0** 开始，因此 `tasks.get(0)` 是“登录”。
- `tasks.size()` 是项目总数。

新增“申请退款”后：

```text
下标 0：登录
下标 1：查看钱包
下标 2：发起支付
下标 3：申请退款
```

所以 `tasks.get(3)` 输出“申请退款”，`size()` 输出 `4`。

### 5.2 删除后，后面的元素会补位

```java
tasks.remove(1);
```

删除原下标 1 的“查看钱包”后：

```text
下标 0：登录
下标 1：发起支付
下标 2：申请退款
```

因此 `tasks.get(1)` 输出“发起支付”，总数为 `3`。

### 5.3 遍历列表

```java
for (String task : tasks) {
    System.out.println(task);
}
```

意思是：从 `tasks` 中依次取出每一项，临时命名为 `task`，打印它。每次 `println` 都会换行。

### 5.4 fail-fast：遍历时不要直接改同一个列表

这段代码会主动制造学习用的错误：

```java
for (String task : tasks) {
    if (task.equals("登录")) {
        tasks.remove(task);
    }
}
```

它不是删除 `for` 循环，而是：循环取到“登录”时，尝试从 `tasks` 列表中删除“登录”。问题是循环还想按原顺序继续读取，但列表已经被中途修改。

Java 因此抛出了：

```text
ConcurrentModificationException
```

这就是 **fail-fast（快速失败）**：发现集合在遍历过程中被不安全地修改，就尽早报错，而不是悄悄产生难发现的错误结果。

本节先记住安全规则：

> `for (String task : tasks)` 里只负责读取，不直接 `add` 或 `remove` 同一个 `tasks`。

这次使用的安全写法：

```java
tasks.removeIf(task -> task.equals("登录"));

for (String task : tasks) {
    System.out.println(task);
}
```

先删除所有“登录”，再单纯遍历打印。`task -> task.equals("登录")` 可以先整体理解成：“当前项等于登录时，删掉它”。不需要今天就深入箭头语法。

## 6. HashMap、ConcurrentHashMap、ArrayList 一次区分

| 问题 | 选择 | 原因 |
|---|---|---|
| 用 `userId` 查用户昵称 | `HashMap` | 用唯一 key 找 value |
| 按顺序记录“登录、查看钱包、支付” | `ArrayList` | 需要顺序和下标 |
| 多线程同时更新用户在线状态 | `ConcurrentHashMap` | 单个 Map 操作更适合并发 |

一句话记忆：

```text
Map：按名字找数据。
ArrayList：按顺序找数据。
ConcurrentHashMap：多线程共享 Map 时的更安全选择。
```

## 7. MiniPay 服务边界复习

本项目共有 8 个后端模块：

| 模块 | 主要职责 |
|---|---|
| `identity-service` | 登录、身份认证、授权、用户与权限相关能力 |
| `payment-service` | 支付、退款、转账订单的创建与流程推进 |
| `wallet-service` | 真实余额变动、冻结金额、账本记录 |
| `commerce-service` | 商户、商品、购物车、地址、外卖订单等交易业务 |
| `agent-service` | AI Agent 能力；只能走允许的接口，不能直连业务数据库 |
| `consumer-bff` | 普通用户客户端的后端入口 |
| `management-bff` | 商户和运营端的后端入口 |
| `admin-bff` | 系统管理员端的后端入口 |

今天重点口述题及答案：

1. 支付、退款、转账等订单流程主要由谁负责？——`payment-service`。
2. 真实余额、冻结和账本由谁负责？——`wallet-service`。
3. 普通用户请求先进入哪个 BFF？——`consumer-bff`。
4. `payment` 为什么不能直接 `UPDATE wallet` 的数据库？——不是简单的“权限不够”，根本原因是 **数据 owner 和服务边界**。

可用于面试的完整回答：

> Wallet 是余额和账本的唯一负责人。Payment 负责支付、退款、转账订单和流程编排，但不能跨服务直接修改 Wallet 数据库。否则多个服务共同控制资金数据，容易产生不一致，也难追踪和修复。Payment 应通过 Wallet 的接口或约定的分布式事务流程请求扣款、加款或冻结。

## 8. 今天最容易混淆的点

1. `get("小明")` 不是按 value 查；Map 默认按 key 查。
2. 同 key 的第二次 `put` 是覆盖，不会让 `size()` 增加。
3. `get` 不存在的 key 返回 `null`。
4. 两个 key 可能 hash 到同一个桶，但仍会继续比较 key，不会混为同一条数据。
5. `HashMap` 与 `ConcurrentHashMap` 在单线程下看起来相同，真正差异在并发读写。
6. `ConcurrentHashMap` 不是“解决所有并发和分布式问题”的工具。
7. ArrayList 的第一项是下标 `0`，不是 `1`。
8. 删除 ArrayList 中间项后，后面的元素会前移。
9. fail-fast 不是线程安全保证，而是尽早暴露不安全修改。
10. `removeIf` 删除列表项目，不会删除后面的 `for` 循环。

## 9. Day 1 自测题（附答案）

1. `Map<String, Integer>` 中 key/value 分别是什么类型？——文本 / 整数。
2. `scores.get("math")` 的结果是什么？——对应的分数，例如 `95`。
3. 相同 key 再 `put` 会怎样？——更新 value，不增加条目数。
4. `putIfAbsent` 遇到已有 key 会怎样？——保留旧值并返回旧值。
5. `putIfAbsent` 新增成功时为什么返回 `null`？——此前没有旧值。
6. `tasks.get(0)` 代表什么？——列表第一个项目。
7. ArrayList 删除下标 1 后，下标 2 的元素会怎样？——向前补到下标 1。
8. 为什么增强 `for` 中不直接删除同一个列表？——会破坏遍历预期，可能触发 fail-fast。
9. 普通用户访问 MiniPay 的入口？——`consumer-bff`。
10. 为什么 Payment 不能直接改 Wallet 数据库？——Wallet 对资金数据有唯一 ownership，必须维护服务边界。

## 10. Day 1 已达成的标准

- 能写出并运行三类集合的最小代码。
- 能解释 `null`、覆盖、冲突、旧值、下标从 0 开始、元素补位、fail-fast。
- 能分清 Payment 与 Wallet 的职责，并知道消费者入口是 `consumer-bff`。
- 能使用自己的话说出：跨服务不能直接改对方数据库，因为需要保持数据 ownership 和系统一致性。
