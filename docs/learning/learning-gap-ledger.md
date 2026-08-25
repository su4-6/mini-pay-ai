# Day 1—2 补课台账

> 本台账不代表 Day 1 或 Day 2 做错了。它记录的是当日刻意不深挖、但 30 天内必须有明确去处的内容。旧分支保持冻结；补课只在后续分支进行。

## 已完成，不再重复安排

| 来源 | 已有证据 |
|---|---|
| Day 1 | `HashMap` 的 `put/get`、覆盖、`null`、冲突直觉；`ArrayList` 的下标/删除/遍历/fail-fast；`ConcurrentHashMap.putIfAbsent`；Payment/Wallet 边界 |
| Day 2 | `String/int/Integer`、泛型、`NumberFormatException`；注解/Bean/构造器注入直觉；启动类 → Controller → `create(...)` 的验证码发送链 |

## 必须补齐的内容与落点

| 来源 | 尚未系统学习的内容 | 安排到哪天 | 完成证据 |
|---|---|---|---|
| Day 1 | `LinkedList`、`HashSet`，以及它们与 `ArrayList`/`HashMap` 的选择 | Day 3 开场的 30 分钟集合收尾 | 写出一张选择表并运行最小练习 |
| Day 1 | HashMap 扰动、2 的幂、负载因子、扩容、树化、复杂度 | Day 4 的并发课前置复习 | 能按 `put` 流程画图并回答追问 |
| Day 1 | ConcurrentHashMap 的可见性、CAS、桶级同步、复合操作原子性 | Day 4 原计划并发课 | 用 `putIfAbsent/computeIfAbsent` 说明单操作与业务原子性的区别 |
| Day 1 | 8 服务、3 个 BFF、数据 owner 的脱稿表达 | Day 7 周验收 | 不看文档完成服务地图口述 |
| Day 2 | `/code/verify`、`consume(...)`、请求绑定、返回响应 | Day 3 开场补齐 | 画出输入 → Service → 返回/异常链 |
| Day 2 | Spring MVC 的 Filter/Controller/响应转换 | Day 3 原计划 | 区分 BFF 与 Identity 的两条真实请求链 |
| Day 2 | Bean 生命周期、AOP、`@Transactional` 的直觉 | Day 3 原计划 | 能解释代理为何在方法前后加入事务能力 |
| Day 2 | 类加载、字节码、反射性能、动态代理细节 | Day 5 JVM 课 | 完成“类加载 → Bean 可用”的简化口述 |
| Day 2 | Spring 自动配置源码、事务传播细节、循环依赖/三级缓存 | Day 30 红黄绿知识审计 | 标为黄/红并形成后续学习清单，不假装已掌握 |

## 使用规则

1. 每天开始前先查看本台账中安排到当天的项目。
2. 每天结束前逐项标为“已完成”或重新指定具体日期；不能删除未完成项。
3. 若当天任务较多，优先保留原计划，再把补课作为额外前置/复习，不用补课替代原计划。
