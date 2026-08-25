# MiniPay 学习实验区

## 当前进度

- Day 1「集合与项目地图」已完成：[day01-java-basics](day01-java-basics/)。
- Day 2「Java 语言基础与 Spring Bean」已完成基础练习：[day02-java-spring-basics](day02-java-spring-basics/)。验证码校验链路会在 Day 3 开始时先补齐。
- Day 3「IOC、AOP、Bean 生命周期、Spring MVC」学习计划已创建：[day03-spring-ioc-aop-mvc](day03-spring-ioc-aop-mvc/)。
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
└─ day03-spring-ioc-aop-mvc/
```

在 IntelliJ IDEA 中打开单个 `.java` 文件后，点击 `main` 方法左侧的绿色运行图标即可运行当天练习。

