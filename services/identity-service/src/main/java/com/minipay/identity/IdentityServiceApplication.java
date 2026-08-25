package com.minipay.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication：告诉 Spring，“从这个位置启动并扫描、管理这个服务需要的组件”。
//@EnableScheduling：启用定时任务能力，今天先知道它是额外功能即可。
//main(...)：Java 程序开始执行的位置，和你练习文件里的 main 一样。
//SpringApplication.run(...)：真正启动 Identity 服务；Spring 会开始创建和管理很多对象，这些对象就叫 Bean。
@SpringBootApplication
@EnableScheduling
public class IdentityServiceApplication {
    public static void main(String[] args) {
        // IdentityServiceApplication.class 表示从本服务的启动类开始；args 是启动参数。
        // Spring 会在这里读取配置、扫描 @RestController/@Service 等注解并创建 Bean。
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
