# MiniPay 30 天项目驱动学习记录

本目录是 `codex/minipay-30day-learning` 分支的学习与改造证据。每天按“理论 → 源码 → 实操 → 口述 → 验收”推进；当天验收完成后才进入下一天。

## 使用规则

1. 先回答当天讲义的诊断题，不查资料。
2. 阅读讲义中指定的源码入口；一次最多跟踪三个文件。
3. 完成实操并记录现象，不只记录命令。
4. 口述验收题；答不清的内容进入错题区。
5. 涉及资金、消息、鉴权和数据库的改造必须先写测试，禁止直接修改余额或跨服务访问数据库。

## 每日分支规则

- `8.24` 对应 Day 1，提交只记录 Day 1 的学习成果。
- `8.25` 对应 Day 2，以 Day 1 为基线，但相对 `8.24` 只新增 Day 2 的计划、练习和验收记录。
- 后续每天沿用同一规则：当天完成并验收后，创建下一天分支和当天计划；不把新一天的练习混入前一天分支。
- 每天的业务改造如有发生，也只在它所属的当天分支提交，并写清测试或运行证据。

## 进度

| 天 | 主题 | 状态 | 主要证据 |
|---|---|---|---|
| 1 | 集合与项目地图 | 已完成 | [Day 1 复习与讲义](day-01-project-map-and-collections.md)、[系统地图](system-map.md)、[4 个可运行练习](../../learning-labs/day01-java-basics/) |
| 2 | Java 语言基础与 Spring Bean | 已创建，待学习 | [Day 2 计划](day-02-java-language-and-spring-bean.md)、[Day 2 练习区](../../learning-labs/day02-java-spring-basics/) |
| 3 | Spring IOC/AOP/MVC | 未开始 | — |
| 4 | Java 并发与 Agent Run | 未开始 | — |
| 5 | JVM、GC 与排障 | 未开始 | — |
| 6 | MySQL 与资金数据 ownership | 未开始 | — |
| 7 | Redis、Session 与 JWT | 未开始 | — |
| 8—14 | 网络、OS、微服务、RabbitMQ/RocketMQ | 未开始 | — |
| 15—21 | RocketMQ 迁移、分布式与 Docker | 未开始 | — |
| 22—27 | Kubernetes、可观测性与 CI | 未开始 | — |
| 28—30 | 故障演练、系统设计与模拟面试 | 未开始 | — |

## 每日输出模板

```text
今天我学会了：
1.
2.
3.

项目中的对应位置：
-

我仍然讲不清：
-

今天遇到的故障及证据：
- 现象：
- 原因：
- 解决：
- 如何避免：
```

## Day 1 完成记录（2026-08-24）

### 已学会并已运行验证

1. `HashMap`：`put`、`get`、相同 key 覆盖、`size()`、缺失 key 返回 `null`，以及 hash 冲突后还需比较 key 的原因。
2. `ConcurrentHashMap`：它与 HashMap 在单线程下用法相似；多线程共享 Map 时更安全；`putIfAbsent` 表示“只在 key 不存在时新增”，并会返回旧值或 `null`。
3. `ArrayList`：按顺序存数据、下标从 0 开始、删除中间元素后后续元素前移、增强 `for` 遍历与 `removeIf` 的基本用法。
4. MiniPay 服务边界：Payment 负责支付/退款/转账流程，Wallet 负责真实余额/冻结/账本；普通用户请求从 `consumer-bff` 进入。

### 代码与证据

- [HashMapPractice.java](../../learning-labs/day01-java-basics/HashMapPractice.java)：输出 `2`、`2`、`小明`、`北京`，验证同 key 覆盖后大小不变。
- [HashMapPractice2.java](../../learning-labs/day01-java-basics/HashMapPractice2.java)：输出 `95`，验证 `String → Integer` 映射。
- [ConcurrentHashMapPractice.java](../../learning-labs/day01-java-basics/ConcurrentHashMapPractice.java)：输出 `null`、`2`、`3`，验证 `putIfAbsent` 新增成功时的返回值。
- [ArrayListPractice.java](../../learning-labs/day01-java-basics/ArrayListPractice.java)：验证新增、按下标读取、删除补位、遍历、fail-fast 实验与 `removeIf` 安全删除。
- [Day 1 完整复习](day-01-project-map-and-collections.md)：包含今天所有问答、概念解释、代码意义与自测题。

### 仍暂不要求掌握（后续课程再学）

- HashMap 的扰动函数、位运算定位、负载因子、扩容与红黑树。
- `volatile`、CAS、锁粒度、ConcurrentHashMap 扩容协作。
- Seata TCC、Saga、Outbox/Inbox、MQ 幂等的底层实现。

### 今日故障与解决记录

| 现象 | 原因 | 解决方式 | 以后如何避免 |
|---|---|---|---|
| IDEA 不能直接按预期运行练习 | 练习目录不属于 Maven 正式源代码目录 | 在 IDEA 终端用 `java 文件名.java` 单文件运行 | 后续练习继续放 `learning-labs`，不混入 `services` |
| `cd` 后写入 `.java` 文件名时报路径不存在 | `cd` 只用于进入目录 | 先 `cd` 到练习目录，再执行 `java 文件名.java` | 区分“目录切换”和“执行文件” |
| 增强 `for` 中删除 `tasks` 后报 `ConcurrentModificationException` | 遍历时修改了同一个 ArrayList | 使用 `removeIf` 完成删除，再单独遍历打印 | 增强 `for` 中只读取，不直接增删同一列表 |

### Day 1 口述验收结果

- 能区分 `HashMap`、`ConcurrentHashMap` 和 `ArrayList` 的适用场景。
- 能说明 Payment 不能直接修改 Wallet 数据库的根本原因是数据 ownership 和服务边界，而非单纯“没有权限”。
- 能说出普通用户 BFF 是 `consumer-bff`。
