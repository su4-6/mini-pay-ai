# 第一代 30 天主计划（冻结基线）

> 用途：这是 MiniPay 学习计划的“地基”。后续任何 PPT、Spring AI、补课或项目改造安排都只能**追加**到本文件对应日期，或放到 Day31 以后；不得删除、压缩、替换或把原主题挪走。实际学习进度可以延期，但原主题与验收标准不变。

## Day 1—7：基础与核心项目地图

| 天 | 原主题 | 项目实践/验收核心 |
|---:|---|---|
| 1 | 集合、HashMap、ConcurrentHashMap | 8 服务地图、端口、数据 owner；能解释模块边界 |
| 2 | 异常、泛型、反射、注解 | 跟踪 Spring Bean 装配到调用；理解依赖注入 |
| 3 | IoC、AOP、Bean 生命周期、Spring MVC | 跟踪登录请求经过 Filter、BFF、Controller；画 Web 请求链 |
| 4 | `synchronized`、`volatile`、CAS、Lock、线程池 | Agent Run Gate、信号量、任务准入；区分 JVM 锁、数据库锁、分布式幂等 |
| 5 | JVM、GC、类加载、OOM | 观察 JVM/线程/GC 指标；完成 CPU、OOM 排查口述 |
| 6 | MySQL 索引、MVCC、事务、锁 | Payment/Wallet 表和关键查询、`Explain`；说明数据 owner |
| 7 | Redis、Cookie、Session、JWT | 跟踪验证码/BFF Session；说明 Redis 不是余额事实 |

## Day 8—14：网络、微服务、MQ 与 RocketMQ 起步

| 天 | 原主题 | 项目实践/验收核心 |
|---:|---|---|
| 8 | TCP、HTTP、TLS、DNS、TIME_WAIT | 跟踪跨服务 HTTP 调用；解释超时与级联故障 |
| 9 | OS、Linux、epoll、NIO、Netty、RPC | 容器进程/端口/连接/日志；基础 Linux 排障清单 |
| 10 | RabbitMQ 模型与可靠消息 | Outbox Publisher、Listener、DLQ、Inbox；画现有消息拓扑 |
| 11 | RocketMQ 架构 | 独立启动 NameServer、Broker、Proxy；普通消息、Tag、消费组实验 |
| 12 | 重试、DLQ、重复、积压、顺序 | 制造失败、重复投递、积压；解释“不能保证业务恰好一次” |
| 13 | 消息传输边界设计 | 为 4 服务定义 broker-neutral 发布端口与消费适配器 |
| 14 | 迁移第一批低风险链路 | 迁移商户统计/通知；重复消息不产生重复业务效果 |

## Day 15—21：完成 MQ 迁移、分布式与 Docker

| 天 | 原主题 | 项目实践/验收核心 |
|---:|---|---|
| 15 | CAP、BASE、幂等、分布式 ID | 迁移 Identity → Commerce 授权事件；重复/乱序测试 |
| 16 | Saga 与补偿 | 迁移 Commerce ↔ Payment 外卖事件；下单、失败、退款、重复事件 |
| 17 | TCC、空回滚、防悬挂 | 阅读 Payment/Wallet TCC；钱包生命周期事件；资金和账本测试 |
| 18 | RocketMQ 故障语义 | Broker 重启、未知发送结果、DLQ 恢复；Outbox/Inbox 可恢复 |
| 19 | RabbitMQ 下线 | 清除 Rabbit 运行依赖；核心链路通过 |
| 20 | Docker、Namespace、Cgroups、UnionFS | Dockerfile、Compose 网络、卷、健康检查；解释容器 `localhost` |
| 21 | 服务治理 | 超时、有限重试、熔断、限流决策表 |

## Day 22—27：Kubernetes、可观测性与 CI

| 天 | 原主题 | 项目实践/验收核心 |
|---:|---|---|
| 22 | Pod、Deployment、ReplicaSet、Service | 创建 K8s 学习目录与本地集群；一个服务可访问 |
| 23 | ConfigMap、Secret、Probe、resources | Identity、Wallet、Payment、Consumer BFF readiness 正常 |
| 24 | Ingress、服务发现、滚动更新 | 外部 Compose 中间件与 K8s 核心服务打通；升级/回滚 |
| 25 | Prometheus、Grafana、指标模型 | JVM、HTTP、连接池、RocketMQ/Outbox 指标与核心看板 |
| 26 | 日志、Trace、OpenTelemetry | OTel Collector/Trace 后端；`traceId` 关联日志 |
| 27 | CI/CD、Registry、发布策略 | `mvn verify`、镜像构建、清单校验；不连生产 |

## Day 28—30：故障、面试与最终输出

| 天 | 原主题 | 项目实践/验收核心 |
|---:|---|---|
| 28 | 故障演练 | CPU、OOM/Full GC、慢 SQL、Redis 延迟、MQ 积压、超时、CrashLoopBackOff 的完整复盘 |
| 29 | 系统设计与 AI Coding | 配置中心、任务调度、消息通知；需求/边界/Diff/测试/安全证据 |
| 30 | 模拟面试与材料 | 两轮模拟面试、红黄绿审计、10 分钟项目讲解、3 分钟自我介绍、简历事实检查 |

## 不可改变的原约束

- RocketMQ 固定迁移设计：首版 Topic 为 `minipay-events-v1`；Message Key 为 `eventId`；Tag 使用稳定的大写事件枚举、信封中的 `eventType` 保持不变；Consumer Group 按外卖支付、支付结果、身份授权、钱包生命周期、商户统计、商户通知等业务副作用隔离。
- 保留 Outbox/Inbox 与事件幂等，不以 RocketMQ 事务消息替代。迁移期间每类事件只能有一个产生真实副作用的 active broker；Wallet 最后迁移，只有 RabbitMQ 旧积压归零且 RocketMQ 恢复测试通过后才允许删除 RabbitMQ。
- MySQL、Redis、RocketMQ、Seata 在 30 天版本中仍由 Compose 承载；不强行把有状态中间件迁入 Kubernetes。
- 不引入 Nacos 或 Spring Cloud Gateway；Compose DNS/Kubernetes Service 是服务发现方式，三个 BFF 保持独立安全边界。
- 不直接修余额、不跨服务读写数据库、不在日志写入敏感信息、不编造指标。

## 版本变更规则

1. PPT 融合：只为当天增加“八股融合 + 40 分钟收尾”，不替换本表主题。
2. Day31—37：仅补 P0/P1、错题、复习、模拟面试和项目表达，不回填替代本表。
3. Day38—44：仅为 Spring AI 专项追加期；不能占用或删除本表的 MQ、Docker、Kubernetes、可观测性、CI 内容。
4. 如果未来确需改变本表，必须在变更记录中写清：原条目、用户授权、替代原因、补偿学习日和验收证据。
