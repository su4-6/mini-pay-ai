# MiniPay 学习实验区

> 每个实验日还会在项目主线后追加 40 分钟 PPT 八股收尾；题目状态、复习日和项目结合关系统一维护在 [PPT 覆盖地图](../docs/learning/java-interview-ppt-map.md)。

## 当前进度

### Day4 收尾状态（2026-09-05）

Day 4「Java 并发与 Agent Run」已完成：练习输出、口述检查和 Agent Service 定向测试均已记录在 [Day4 计划](../docs/learning/day-04-java-concurrency-and-agent-run.md) 与 [Day4 练习记录](day04-java-concurrency/README.md)。Day5 尚未初始化。

- Day 1「集合与项目地图」已完成：[day01-java-basics](day01-java-basics/)。
- Day 2「Java 语言基础与 Spring Bean」已完成基础练习：[day02-java-spring-basics](day02-java-spring-basics/)。验证码校验链路会在 Day 3 开始时先补齐。
- Day 3「IOC、AOP、Bean 生命周期、Spring MVC」核心学习已完成：[day03-spring-ioc-aop-mvc](day03-spring-ioc-aop-mvc/)。已运行 `CollectionReviewPractice.java`；原计划的 MVC 输入→Service→返回值小练习转入 Day 4 开场复习。
- Day 4「Java 并发与 Agent Run」已完成：[day04-java-concurrency](day04-java-concurrency/)。运行证据、口述验收和后续复习日均已记录；下一步在 `9.5` 分支初始化 Day 5。
- 每个 `.java` 文件都是独立小程序，不参与 MiniPay 正式 Maven 构建。

运行当天任一练习的通用方式：

```powershell
cd "C:\Users\hp\Documents\Codex\mini-pay-ai-learning\learning-labs\day02-java-spring-basics"
java ExceptionPractice.java
```

这里专门放每天跟着课程手敲的 Java 小练习。它不属于 `services/`，不参与 MiniPay 的 Maven 构建，也不会影响支付、钱包或其他正式业务代码。

每一天创建一个独立目录，例如：

```text
learning-labs/
├─ day01-java-basics/
├─ day02-java-spring-basics/
├─ day03-spring-ioc-aop-mvc/
└─ day04-java-concurrency/
```

在 IntelliJ IDEA 中打开单个 `.java` 文件后，点击 `main` 方法左侧的绿色运行图标即可运行当天练习。

