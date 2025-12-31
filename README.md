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

## 简介

Pangju Framework Starter 是基于 Spring Boot 的模块化 Starter 套件，提供开箱即用的自动装配与最佳实践，涵盖 Web、校验、加密、日志，以及 Redis、MongoDB、MyBatis Plus 等组件，支持父 POM 与 BOM 管理，助你快速构建生产级应用。

## [文档](https://pangju666.github.io/pangju-java-doc/starter/getting-started.html)

## 快速开始

1. 使用父 POM（推荐）

```xml
<parent>
  <groupId>io.github.pangju666.framework.boot</groupId>
  <artifactId>framework-starter-parent</artifactId>
  <version>最新版本</version>
</parent>
```

或使用 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666.framework.boot</groupId>
            <artifactId>framework-starter-parent</artifactId>
            <version>最新版本</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

2. 引入 Starter 模块

```xml
<dependencies>
    <!-- Spring 扩展与工具 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-spring</artifactId>
    </dependency>
    <!-- 通用加解密支持 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-crypto</artifactId>
    </dependency>
    <!-- MongoDB 自动配置与多数据源支持 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-data-mongodb</artifactId>
    </dependency>
    <!-- MyBatis Plus 集成增强 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-data-mybatis-plus</artifactId>
    </dependency>
    <!-- Redis 自动配置与多数据源支持 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-data-redis</artifactId>
    </dependency>
    <!-- 图像处理能力（GM/ImageMagick 等） -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-image</artifactId>
    </dependency>
    <!-- JSON 实用工具与配置 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-json</artifactId>
    </dependency>
    <!-- JSON 字段加解密支持 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-json-crypto</artifactId>
    </dependency>
    <!-- Spock 测试 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-test-spock</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- 校验增强（Hibernate Validator 等） -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-validation</artifactId>
    </dependency>
    <!-- Web 基础增强（拦截器、解析器等） -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-web</artifactId>
    </dependency>
    <!-- Web 加解密（请求参数、响应体等） -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-web-crypto</artifactId>
    </dependency>
    <!-- Web 访问日志能力 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-web-log</artifactId>
    </dependency>
    <!-- Web 校验 -->
    <dependency>
        <groupId>io.github.pangju666.framework.boot</groupId>
        <artifactId>framework-starter-web-validation</artifactId>
    </dependency>
</dependencies>
```

## 许可证

本项目采用 Apache License 2.0 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

感谢所有为项目做出贡献的开发者，以及项目所使用的开源框架和工具。
