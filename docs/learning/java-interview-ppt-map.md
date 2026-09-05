# Java 面试 PPT 覆盖地图

> 资料来源：`F:\BaiduNetdiskDownload\5.2023版Java面试教程`。PPT 是题目覆盖清单；不观看 MP4。课件为 2023 年版本，涉及 JDK 7/8、旧 Spring Cloud 或旧中间件的结论，教学时必须以 MiniPay 的 Java 21、Spring Boot 3.5、当前源码与官方文档为准。

## 使用方式

- 每天主线项目学习后安排 **40 分钟八股收尾**：2～4 题，按“概念 → 最小例子 → 口述”完成。
- 能结合项目的题进入当天“八股融合”；不能自然结合的题放在当天收尾，不为了八股改动业务代码。
- `P0`：能讲 2～5 分钟并回答两层追问；`P1`：能解释原理与适用场景；`P2`：理解概念并说明版本差异即可。
- `状态` 只使用：`已学待复习`、`需加深`、`未学`、`后续深度`。每次口述后填写真实证据和下次复习日。

当前继续点：Day5 计划已在 `9.5` 初始化。先复习 Day4，再按 09 JVM 篇第 2--134 页推进；下表中的 JVM 项仍为“未学”，只有完成口述与实际观察后才能更新状态。

## 已学折算（不机械重学）

| 来源 | 题目 | 级别 | 已有证据 | 状态 | 下次复习 |
|---|---|---:|---|---|---|
| 07 集合篇 | ArrayList、LinkedList、HashSet 的选择 | P1 | Day1/Day3 独立练习 | 已学待复习 | Day7 |
| 07 集合篇 | HashMap `put`、冲突、扩容、树化、2 的幂 | P0 | `HashMapCollisionPractice.java` + Day4 口述验收 | 已学待复习 | Day14 |
| 07 集合篇 | ConcurrentHashMap 与 HashMap 的边界 | P0 | Day1 + Day4 `ConcurrentHashMapPractice2.java` | 已学待复习 | Day14 |
| 04 框架篇 | IoC、构造器注入、Bean、AOP、事务 | P0 | Identity 登录/审计源码 | 已学待复习 | Day7 |
| 04 框架篇 | MVC 请求链、Filter、Controller 边界 | P0 | `RequestIdFilter`、`ConsumerAuthController` | 已学待复习 | Day7 |
| 08 并发篇 | 竞态、原子性、可见性、`volatile`、CAS、AtomicInteger | P0 | `CounterRacePractice.java`、`AtomicCounterPractice.java` | 已学待复习 | Day14 |

## 覆盖与排期

| PPT / 主题 | 级别 | 项目结合点或收尾位置 | 学习日 | 状态 | 下次复习 |
|---|---:|---|---:|---|---|
| 01 准备篇：简历结构、项目表达、学习方法 | P0 | MiniPay 事实核验、10 分钟项目讲解 | Day29–30 | 未学 | Day37 |
| 02 Redis：使用场景、穿透/击穿/雪崩 | P0 | 验证码、Session、限流；Redis 不是余额事实 | Day7 | 未学 | Day14 |
| 02 Redis：双写一致性、延迟双删、持久化 | P1 | 缓存边界；Outbox 不能靠缓存替代 | Day7 收尾 | 未学 | Day21 |
| 02 Redis：过期/淘汰、RDB/AOF | P0 | 验证码 TTL 与内存边界 | Day7 | 未学 | Day21 |
| 02 Redis：分布式锁、Redisson | P1 | 对比 Redis 锁、DB 唯一约束、Seata；不用于资金真相 | Day15 收尾 | 未学 | Day28 |
| 02 Redis：主从、哨兵、Cluster、IO 多路复用 | P1 | 不强行部署；缓存高可用横向知识 | Day7 收尾 + Day31 | 未学 | Day37 |
| 03 MySQL：慢 SQL、Explain、索引选择 | P0 | Payment/Wallet/Outbox 查询 | Day6 | 未学 | Day14 |
| 03 MySQL：B+ 树、聚簇/二级索引、回表、覆盖索引 | P0 | 服务独立 Schema 与支付/账本查询 | Day6 | 未学 | Day21 |
| 03 MySQL：联合索引、最左匹配、失效、分页 | P0 | 列表 API 与后台查询 | Day6 | 未学 | Day21 |
| 03 MySQL：ACID、隔离级别、并发问题/锁 | P0 | 本地事务、条件更新、Inbox/Outbox | Day6 | 未学 | Day21 |
| 03 MySQL：Buffer Pool、Redo/Undo/Binlog | P1 | 事务恢复与面试追问 | Day6 收尾 | 未学 | Day28 |
| 04 框架：Bean 单例线程安全、生命周期、循环依赖 | P0 | Identity Bean 与 Day4 共享字段风险 | Day20 收尾 | 未学 | Day28 |
| 04 框架：AOP、事务实现/失效场景 | P0 | `@Transactional`、审计 `REQUIRES_NEW` | Day3 已学 + Day20 复习 | 已学待复习 | Day20 |
| 04 框架：MyBatis 执行、延迟加载、一级/二级缓存 | P1 | Mapper、Hikari、缓存边界 | Day6 收尾 | 未学 | Day28 |
| 05 微服务：注册发现、负载均衡 | P1 | Compose DNS / Kubernetes Service；不引入 Nacos | Day24 | 未学 | Day31 |
| 05 微服务：超时、重试、降级、熔断、限流、监控 | P0 | 内部 HTTP 调用、健康检查、指标 | Day21 | 未学 | Day28 |
| 05 微服务：CAP、BASE、幂等 | P0 | Outbox/Inbox、状态机 | Day15 | 未学 | Day21 |
| 05 微服务：Seata XA/AT/TCC、MQ 最终一致性 | P0 | Wallet TCC、Commerce Saga | Day16–17 | 未学 | Day28 |
| 05 微服务：调度路由 | P2 | 任务调度系统设计题 | Day29 收尾 | 未学 | Day37 |
| 06 MQ：生产确认、持久化、消费确认 | P0 | RabbitMQ 现状与 RocketMQ 迁移原则 | Day10 | 未学 | Day14 |
| 06 MQ：重试、DLQ、延迟、积压 | P0 | Outbox/Inbox、故障演练 | Day12、18 | 未学 | Day21 |
| 06 MQ：RabbitMQ 集群/仲裁队列 | P1 | 可靠性横向知识；本地不强行搭生产集群 | Day18 收尾 | 未学 | Day31 |
| 06 MQ：Kafka 分区、存储、清理 | P1 | 与 RabbitMQ/RocketMQ 模型对比 | Day12 收尾 | 未学 | Day31 |
| 07 集合：复杂度、数组/链表、ArrayList/LinkedList | P1 | 独立 Java 基本功 | Day4 收尾 | 已学待复习 | Day14 |
| 07 集合：JDK7/8 HashMap 差异与并发风险 | P1 | 明确版本差异；MiniPay 使用 Java 21 | Day4 收尾 | 需加深 | Day21 |
| 08 并发：进程/线程、并发/并行、状态、start/run、wait/sleep | P1 | 不强行映射业务；每日收尾 | Day8–9 收尾 | 未学 | Day21 |
| 08 并发：synchronized、Monitor、JMM、volatile、CAS | P0 | Agent Run 和共享数据实验 | Day4 | 已学待复习 | Day14 |
| 08 并发：AQS、ReentrantLock、Lock 对比、死锁诊断 | P0 | Semaphore 直觉、`shutdownNow()` 协作中断；死锁诊断仍在 Day5 | Day4–5 | 需加深 | Day21 |
| 08 并发：ConcurrentHashMap JDK8 模型 | P0 | `putIfAbsent` / `computeIfAbsent` 实验 | Day4 | 已学待复习 | Day14 |
| 08 并发：线程池、队列、拒绝策略、核心线程数 | P0 | 线程池、拒绝、取消、关闭练习 | Day4 | 已学待复习 | Day21 |
| 09 JVM：运行时内存、堆/栈/方法区/直接内存 | P0 | Java 服务指标与 OOM 排查 | Day5 | 未学 | Day14 |
| 09 JVM：类加载器、双亲委派、类加载过程 | P0 | Spring 启动与 Bean 前置知识 | Day5 | 未学 | Day21 |
| 09 JVM：垃圾算法、分代、Minor/Mixed/Full GC、G1 | P0 | Actuator/GC 指标和故障演练 | Day5 | 未学 | Day21 |
| 09 JVM：OOM、线程栈、Heap Dump、诊断工具 | P0 | Day28 故障演练 | Day5、28 | 未学 | Day28 |
| 10 设计模式：工厂、责任链 | P1 | Spring Bean 创建、Filter 链；只讲真实使用边界 | Day20 收尾 | 未学 | Day31 |
| 11 企业场景：SSO/JWT、RBAC | P0 | BFF/Identity、scope 与权限边界 | Day7、29 | 未学 | Day30 |
| 11 企业场景：日志、线上 Bug 排查 | P0 | requestId/traceId、故障演练 | Day26、28 | 未学 | Day30 |
| 11 企业场景：压测与性能指标 | P1 | 只使用真实环境和测量记录，不编造指标 | Day28–29 | 未学 | Day37 |

## 每日增量节奏

| 阶段 | 主线保持不变 | 八股融合 | 每日收尾 |
|---|---|---|---|
| Day4 | Java 并发与 Agent Run | JMM、CAS、锁、CHM、线程池 | 线程状态、`wait/sleep`、版本差异 |
| Day5–7 | JVM、MySQL、Redis | 对应 PPT P0/P1 | 追问型底层原理 |
| Day8–19 | 网络/OS、MQ、分布式、RocketMQ | 微服务/MQ PPT | Kafka、调度、横向高可用 |
| Day20–27 | Docker、K8s、可观测性、CI | Spring/MyBatis/服务治理 | 设计模式与框架细节 |
| Day28–30 | 故障、系统设计、模拟面试 | 日志、压测、SSO/RBAC、项目表达 | 错题口述与简历核验 |
| Day31–37 | 只补漏和模拟 | 未完成 P0/P1 | 复习、追问、项目讲稿 |

## 日终验收模板

1. 更新当日 P0/P1 的真实状态、证据、下次复习日。
2. 写下 2～4 个口述答案；答错时记录“不会什么 → 正确答案 → 下次复习日”。
3. 对涉及旧版本的题，写明 MiniPay 当前版本的差异；不背过时结论。
4. Day7、14、21、30 做累计复习；Day31–37 只补地图中的未完成项。

## PPT 逐题目录（完整覆盖索引）

> 下表按“内容页范围”覆盖每份 PPT 的全部知识模块与问题页；重复的讲解图、答案页和案例演示与其所属问题合并，不单独重复排课。`资料补充` 表示先掌握题目，只有需要图示、源码或实操时才读取同目录资料。

### 01 面试准备篇（1–26）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–9 | HR、部门负责人如何筛选简历；筛选规则 | P0 | Day29：事实核验与简历改写 |
| 10–15 | 职业技能、项目经历、简历整体结构 | P0 | Day29：MiniPay/SpringAI 证据对应 |
| 16–20 | 应届生练手项目、GitHub/Gitee、模块吃透方法 | P1 | Day30：项目表达与学习复盘 |
| 21–24 | Java 面试流程、形式、准备方法 | P1 | Day30：两轮模拟面试 |
| 25 | 心态/经验内容 | P2 | Day37：面试复盘 |

### 02 Redis 篇（1–85）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–5、12 | 缓存定义、Redis 使用场景 | P0 | Day7：验证码、Session、限流 |
| 6–11 | 缓存穿透、空值缓存、布隆过滤器 | P0 | Day7：查询边界；资料补充按需读 |
| 13–17 | 缓存击穿、雪崩与治理 | P0 | Day7：热点与过期策略 |
| 18–28 | 先更新库还是缓存、双写一致、异步方案 | P0 | Day7：不与 Outbox 混淆 |
| 29–35 | RDB、AOF、二者对比、持久化面试题 | P1 | Day7 收尾 |
| 37–44 | 惰性/定期删除、过期与淘汰策略 | P0 | Day7：验证码 TTL 对照 |
| 46–52 | 抢券案例、Lua/原子扣减、集群部署 | P1 | Day15 收尾：与资金边界比较 |
| 53–60 | Redis 分布式锁、Redisson、可重入、主从一致性 | P1 | Day15 收尾：不替代 DB/TCC |
| 63–75 | 主从复制、哨兵、脑裂、分片集群 | P1 | Day31：高可用横向知识 |
| 76–83 | Redis 快、阻塞/非阻塞 IO、IO 多路复用 | P1 | Day9 收尾：OS/NIO 关联 |

### 03 MySQL 篇（1–91）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–12 | 慢查询定位、慢 SQL 分析、Explain | P0 | Day6：真实查询与 Explain |
| 13–17 | MySQL 体系结构、存储引擎 | P1 | Day6 收尾 |
| 18–35 | B+ 树、聚簇索引、二级索引、回表、覆盖索引、深分页 | P0 | Day6：Payment/Wallet 查询 |
| 36–48 | 建索引原则、联合索引、最左匹配、失效、SQL 优化 | P0 | Day6：索引设计与验证 |
| 50–59 | 事务、ACID、并发问题、锁与隔离 | P0 | Day6：本地事务/条件更新 |
| 60–79 | Buffer Pool、Redo、Undo、MVCC | P1 | Day6 收尾 |
| 80–89 | 主从同步、读写分离、垂直/水平分库分表 | P1 | Day31：规模化横向知识 |

### 04 框架篇（1–53）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–5 | Spring 单例 Bean 与线程安全 | P0 | Day4 + Day20 收尾 |
| 6–16 | AOP、事务实现、事务失效、异常处理 | P0 | Day3 已学；Day20 复习 |
| 18–30 | BeanDefinition、构造过程、循环依赖、三级缓存与构造器循环依赖 | P1 | Day20 收尾 |
| 31–41 | MVC 演变、DispatcherServlet、`@SpringBootApplication`、常用 Spring/MVC/Boot 注解 | P0 | Day3 已学；Day20 复习 |
| 43–52 | MyBatis 执行流程、延迟加载、一级/二级缓存 | P1 | Day6 收尾 |

### 05 微服务篇（1–53）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–13 | Spring Cloud、注册中心、Eureka/Nacos、Ribbon 与负载均衡 | P1 | Day24：Compose DNS/K8s Service 版本差异 |
| 16–28 | 降级、熔断、雪崩、监控、SkyWalking、Nginx/网关限流 | P0 | Day21、26：超时重试、指标、Trace |
| 29–35 | CAP、BASE | P0 | Day15：最终一致性取舍 |
| 37–42 | Seata 架构、XA/AT/TCC、MQ 分布式事务 | P0 | Day16–17：Saga/TCC/Outbox |
| 43–47 | 幂等、Token+Redis、分布式锁 | P0 | Day15：Inbox/幂等状态机 |
| 48–52 | XXL-JOB 路由、失败处理、大任务分片 | P2 | Day29 收尾：调度系统设计 |

### 06 消息中间件篇（1–46）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–9 | RabbitMQ Exchange、生产确认、持久化、消费确认、投递丢失 | P0 | Day10：现有消息拓扑 |
| 10–18 | TTL/死信、延迟队列、消息堆积、惰性队列 | P0 | Day12：失败、重试、DLQ、积压实验 |
| 20–37 | 普通/镜像/仲裁集群、三层可靠性、复制/备份 | P1 | Day18 收尾：版本与生产差异 |
| 39–45 | Kafka 文件存储、清理、分区、零拷贝 | P1 | Day12 收尾：与 RocketMQ 对比 |

### 07 常见集合篇（1–100）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–15 | 集合框架、数据结构、时间/空间复杂度 | P1 | Day4 收尾 |
| 16–25 | 数组、索引从 0、数组读写/插入删除复杂度 | P1 | Day4 收尾 |
| 27–39 | ArrayList 成员、构造、扩容、数组/List 转换 | P1 | Day4 收尾 |
| 41–51 | 单/双向链表、复杂度、ArrayList 与 LinkedList | P1 | Day4 收尾 |
| 54–76 | 二叉树、二叉搜索树、红黑树、散列表、拉链法 | P1 | Day4：HashMap 深入 |
| 78–94 | HashMap 原理、JDK7/8 差异、put、扩容、寻址、2 的幂 | P0 | Day4：已学待复习 |
| 95–99 | JDK7 HashMap 多线程死循环与安全替代 | P1 | Day4 收尾：版本差异 |

### 08 并发编程篇（1–144）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–13 | 线程/进程、并发/并行 | P1 | Day8 收尾 |
| 14–23 | 创建线程、Runnable/Callable、`run`/`start` | P1 | Day8 收尾 |
| 24–35 | 线程状态、顺序执行、`notify`/`notifyAll`、`wait`/`sleep`、停止线程 | P1 | Day8–9 收尾 |
| 37–52 | synchronized、Monitor、对象头、轻量/重量锁、版本差异 | P0 | Day4：核心；旧偏向锁细节 P2 |
| 53–71 | JMM、CAS、乐观/悲观锁、volatile 与有序性 | P0 | Day4：核心 |
| 72–84 | AQS、公平/非公平、ReentrantLock、Lock 对比 | P0 | Day4：锁与 Semaphore 前置 |
| 85–90 | 死锁条件、`jps`/`jstack`/VisualVM 诊断 | P0 | Day5、28：排障 |
| 91–96 | ConcurrentHashMap JDK7/8、CAS/桶同步 | P0 | Day4：已学待复习 |
| 97–103 | 原子性、可见性、有序性与工具选择 | P0 | Day4：核心 |
| 105–124 | 线程池执行、参数、队列、核心数、种类、拒绝、Future/CountDownLatch | P0 | Day4：线程池段 |
| 125–134 | 批量、汇总、异步场景、并发度控制/Semaphore | P1 | Day4：Agent Run 映射 |
| 135–143 | ThreadLocal 原理与内存泄漏 | P0 | Day4 收尾 + Day26：请求上下文边界 |

### 09 JVM 篇（1–135）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–6 | JVM 定义、组成、运行流程 | P0 | Day5 |
| 8–31 | PC、堆、栈、方法区、常量池、直接内存、堆栈区别/溢出 | P0 | Day5 |
| 34–49 | 类加载器、双亲委派、加载/验证/准备/解析/初始化 | P0 | Day5 |
| 52–58 | 可回收对象、引用计数、可达性、GC Roots | P1 | Day5 收尾 |
| 60–72 | GC 算法、分代、Minor/Mixed/Full GC | P0 | Day5 |
| 74–93 | 收集器、CMS、G1 与回收阶段 | P0 | Day5 |
| 95–98 | 强软弱虚引用 | P1 | Day5 收尾 |
| 101–111 | JVM 参数与调优原则 | P1 | Day5、28 |
| 113–121 | JVM 工具、内存/线程观察 | P0 | Day5、28 |
| 123–134 | 内存泄漏、CPU 飙高排查 | P0 | Day5、28 |

### 10 企业场景：设计模式（1–28）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 2–13 | 简单工厂、工厂方法、抽象工厂 | P1 | Day20 收尾：Spring 创建对象的边界 |
| 14–22 | 策略模式、登录案例、模式选择 | P1 | Day20 收尾：验证码渠道/适配器对照 |
| 23–27 | 责任链、优缺点、应用场景 | P1 | Day20 收尾：Filter 链对照 |

### 11 企业场景：常见技术场景（1–28）

| 页码 | 题目/内容 | 级别 | 安排 |
|---:|---|---:|---|
| 4–7 | SSO、JWT 方案 | P0 | Day7、29：Identity/BFF |
| 8–11 | RBAC、后台权限开发 | P0 | Day29：管理员边界 |
| 13–15 | 对称/非对称加密、后台服务安全 | P1 | Day29 收尾 |
| 16–25 | 日志采集、ELK、日志命令、线上 Bug 排查、远程调试 | P0 | Day26、28；ELK 部署为后续深度 |
| 26–27 | 压测、性能指标 | P1 | Day28–29：只基于真实测量 |

## Spring AI 补充轨道（不替代 11 份 Java PPT）

Spring AI 不属于第 12 份 Java PPT，也不计入 118 条 PPT 目录完成数。它作为 MiniPay 项目工程补充：Day4/8–9/15–18/20–21/25–30 追加源码证据，Day38–44 完成专项安全改造、测试、观测和表达。详见 [spring-ai-learning-map.md](spring-ai-learning-map.md)。

所有 Spring AI 结论以 Java 21、Spring Boot 3.5 与本仓库源码/测试为准；显式记忆不等于 RAG，未实施的指标、向量库或改造不得写进简历。
