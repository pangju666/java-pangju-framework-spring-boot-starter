[中文](README.md) | [English](README_EN.md)

<p align="center">
  <a href="https://github.com/pangju666/java-pangju-framework-spring-boot-starter/releases">
    <img alt="GitHub release" src="https://img.shields.io/github/release/pangju666/java-pangju-framework-spring-boot-starter.svg?style=flat-square&include_prereleases" />
  </a>

  <a href="https://central.sonatype.com/search?q=g:io.github.pangju666%20%20a:java-pangju-framework-spring-boot-starter&smo=true">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666/java-pangju-framework-spring-boot-starter.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="license" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# Pangju Framework Spring Boot Starter

Pangju Framework 是一个面向 Spring Boot 的多模块扩展框架，聚焦于 Web 增强、数据存储、日志治理、加密安全、验证与测试等场景。通过一组可独立启用的
Starter 与自动配置，帮助你快速构建高质量的分布式与高性能应用。

## 特性总览

- Web 增强：全局异常、参数绑定（含枚举解析）、请求/响应加密、接口签名、幂等、限流、统一日志。
- 数据存储：MongoDB 动态多数据源、MyBatis‑Plus 扩展与拦截器、Redis 多数据源与模板增强。
- 日志治理：基于 Disruptor 的高性能异步日志、Kafka 分发、MongoDB 持久化存储。
- 校验与安全：自定义验证注解、注解式请求体/字段加解密。
- 测试支持：常用测试工具集成、Spock 与 Spring 测试生态融合。

## 模块概览

- `pangju-framework-autoconfigure`：核心自动配置，其他模块基础依赖。
- `pangju-framework-web-spring-boot-starter`：Web 层增强（异常、参数绑定、拦截器等）。
- `pangju-framework-web-crypto-spring-boot-starter`：请求参数与响应体加密（AES/RSA）。
- `pangju-framework-web-log-spring-boot-starter`：统一 Web 日志，支持 Disruptor/Kafka/MongoDB。
- `pangju-framework-web-validation-spring-boot-starter`：Web 请求字段级参数校验增强。
- `pangju-framework-data-mongodb-spring-boot-starter`：基于 Spring Data MongoDB 的增强与动态多数据源。
- `pangju-framework-data-mybatis-plus-spring-boot-starter`：MyBatis‑Plus 自动配置与拦截器扩展。
- `pangju-framework-data-redis-spring-boot-starter`：Redis 自动配置与增强模板。
- `pangju-framework-validation-spring-boot-starter`：通用验证注解与自动配置。
- `pangju-framework-test-spring-boot-starter`：测试工具整合（JUnit、JSON 断言等）。
- `pangju-framework-spock-test-spring-boot-starter`：Spock + Spring 测试集成。
- `pangju-framework-spring-boot-starter`：统一基础能力与依赖管理。

## 安装与使用

在你的 Spring Boot 项目中引入 BOM 与所需 Starter（建议使用 Maven）：

```xml
<!-- 在父 POM 或项目 POM 的 dependencyManagement 中导入 BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666</groupId>
            <artifactId>pangju-framework-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```xml
<!-- 挑选所需的 Starter 依赖即可使用 -->
<dependencies>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-web-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-web-crypto-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-web-log-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-data-mongodb-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-data-redis-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-framework-data-mybatis-plus-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## 快速开始

示例：在 Controller 中使用加密、幂等、限流与签名能力。

```java
import io.github.pangju666.framework.autoconfigure.web.crypto.advice.DecryptRequestBody;
import io.github.pangju666.framework.autoconfigure.web.crypto.advice.EncryptResponseBody;
import io.github.pangju666.framework.autoconfigure.web.crypto.resolver.EncryptRequestParam;
import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.limit.annotation.RateLimit;
import io.github.pangju666.framework.autoconfigure.web.signature.annotation.Signature;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/echo")
    @RateLimit(qps = 10)
    @Signature(appId = {"app1"})
    public String echo(@EncryptRequestParam String text) {
        return text; // 加密请求参数将被自动解密
    }

    @PostMapping("/submit")
    @Idempotent(key = "#payload.id")
    @EncryptResponseBody // 返回值将自动加密
    public Object submit(@DecryptRequestBody @RequestBody DemoPayload payload) {
        return payload;
    }
}
```

常用配置示例（`application.yml`）：

```yaml
# Web 增强：异常处理与参数绑定（时间戳自动转 Date/LocalDate/LocalDateTime）
pangju:
  web:
    advice:
      binding: true
      exception: true

    # 统一 Web 日志（默认启用 Disruptor 异步链路）
    log:
      enabled: true

    # 接口签名：为不同 appId 配置密钥
    signature:
      secret-keys:
        app1: your-secret-key-1
        app2: your-secret-key-2

spring:
  data:
    mongodb:
      dynamic:
        primary: mongodb-primary
        databases:
          mongodb-primary:
            uri: mongodb://user:password@localhost:27017/primary_db
            auto-index-creation: true
          mongodb-secondary:
            uri: mongodb://user:password@192.168.1.100:27017/secondary_db
            auto-index-creation: true
          mongodb-tertiary:
            host: 192.168.1.101
            port: 27017
            database: tertiary_db
            username: user
            password: password
            auto-index-creation: false
```

> 说明：
> - Web 日志将根据环境自动选择组件：存在 `KafkaTemplate` 时可启用 Kafka 发送；存在 `MongoTemplate` 时可启用 MongoDB
    接收；否则默认使用 Disruptor 异步处理。
> - MongoDB 动态数据源支持为不同 `Repository` 指定数据源（见 `@DynamicMongo` 注解与自动配置）。

## 关键能力与用法

- 枚举参数解析：自动解析请求参数中的枚举值，支持必填校验与错误提示。
- 请求/响应加密：通过注解声明字段或响应体加密，支持 `AES-256` 与 `RSA`，编码支持 `Base64/Hex`。
- 接口签名拦截：在方法或类上标注 `@Signature`，从参数/请求头中提取签名进行校验，支持时效与算法配置。
- 幂等验证：在方法上使用 `@Idempotent`，支持内存与 Redis 实现，防重复提交。
- 限流拦截：在方法上使用 `@RateLimit`，支持基于 Redis 或 Resilience4J 的限流实现，按 IP 或自定义维度限流。
- 动态 MongoDB：基于 `spring.data.mongodb.dynamic` 前缀配置多个数据源并自动选择主数据源，生成
  `MongoTemplate/GridFsTemplate` 等 Bean。

## 版本与兼容

- JDK 17+（建议）
- Spring Boot 3.x（建议 3.2+）
- 可选：Kafka、MongoDB、Redis 依赖按需添加

## 变更日志

详见 [CHANGELOG.md](CHANGELOG.md)

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源发布，可自由商用与修改（需保留版权与许可声明）。