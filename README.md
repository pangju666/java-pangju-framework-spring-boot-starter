<p align="center">
  <a href="https://github.com/pangju666/java-pangju-framework-spring-boot-starter/releases">
    <img alt="GitHub release" src="https://img.shields.io/github/release/pangju666/java-pangju-framework-spring-boot-starter.svg?style=flat-square&include_prereleases" />
  </a>

  <a href="https://central.sonatype.com/search?q=g:io.github.pangju666.framework.boot%20%20a:framework-starter-parent&smo=true">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666.framework.boot/framework-starter-parent.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# Pangju Framework Starter

Pangju Framework Starter 是一套基于 [`Pangju Framework`](https://github.com/pangju666/java-pangju-framework) 与 Spring Boot 4.x 构建的模块化 Starter 套件。通过自动装配机制，将 Web 增强、安全合规、数据持久化及图像处理等核心功能封装为开箱即用的能力。

## ✨ 核心特性

- **🚀 开箱即用**：提供完善的 Spring Boot 自动装配支持，引入依赖即可享受增强功能。
- **🛡️ 安全加解密**：原生支持请求/响应加解密、JSON 字段脱敏与加密，支持多种加解密算法（AES, RSA 等）。
- **📊 深度集成**：与 MyBatis Plus, Redis, MongoDB 深度集成，提供统一的异常处理、分页增强和多数据源支持。
- **📸 图像处理**：支持多种图像引擎（ImageIO, GraphicsMagick），提供裁剪、缩放、格式转换等统一接口。
- **📝 统一日志**：提供基于 Servlet Filter 的 Web 访问日志记录，支持多种异步写入（Disruptor, Kafka）和多种存储介质（SLF4J, MongoDB）。
- **🧪 完善测试**：集成 Spock 测试框架，提供针对 Spring Boot 环境的增强测试支持。

## 📦 模块矩阵

| 模块名称                                  | 说明                                   |
|:--------------------------------------|:-------------------------------------|
| `framework-starter-crypto`            | 提供通用的对称/非对称加解密服务与工具                  |
| `framework-starter-restclient`        | HTTP 客户端自动装配与工具类                     |
| `framework-starter-web`               | Web 基础增强，包含统一响应格式、全局异常拦截与参数解析        |
| `framework-starter-web-log`           | 基于 Servlet Filter 的 Web 访问日志记录       |
| `framework-starter-web-crypto`        | Web 请求参数与响应体加解密，支持多种加解密算法            |
| `framework-starter-web-limit`         | 基于 拦截器 的接口限流，支持多种策略与存储介质             |
| `framework-starter-web-signature`     | 完善的 API 签名校验机制，保障请求的完整性与安全性          |
| `framework-starter-jackson`           | Jackson 序列化增强与脱敏支持，提供常用的配置与工具类       |
| `framework-starter-jackson-crypto`    | Jackson 的加解密，保障数据安全                  |
| `framework-starter-data-mybatis-plus` | MyBatis Plus 增强，包含公共字段填充与通用基类        |
| `framework-starter-data-redis`        | Redis 自动配置增强，支持多数据源                  |
| `framework-starter-data-mongodb`      | MongoDB 自动配置增强，支持多数据源与 Repository 模式 |
| `framework-starter-image`             | 统一图像处理接口，封装 ImageIO 与外部图像引擎          |
| `framework-starter-validation`        | 参数校验增强，支持多国语言与自定义校验规则                |
| `framework-starter-spring`            | Spring 全局上下文工具与扩展点增强                 |
| `framework-starter-test`              | 集成 Spring Boot Test 与常用测试工具          |
| `framework-starter-test-spock`        | Spock 测试框架集成                         |

## 🚀 快速开始

### 1. 引入父项目（推荐）

在你的 `pom.xml` 中引入 `framework-starter-parent`：

```xml
<parent>
    <groupId>io.github.pangju666.framework.boot</groupId>
    <artifactId>framework-starter-parent</artifactId>
    <version>2.0.0</version>
</parent>
```

或者使用 BOM 管理依赖版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666.framework.boot</groupId>
            <artifactId>framework-starter-parent</artifactId>
            <version>2.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 添加具体依赖

根据业务需求引入相应的 Starter，例如开启 Web 日志与 MyBatis Plus：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-web-log</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-data-mybatis-plus</artifactId>
    </dependency>
</dependencies>
```

## 📖 文档

更多详细说明请参考：[在线文档](https://pangju666.github.io/pangju-java-doc/v1/starter/getting-started.html)

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

---
感谢所有为项目做出贡献的开发者，以及项目所使用的开源框架和工具。