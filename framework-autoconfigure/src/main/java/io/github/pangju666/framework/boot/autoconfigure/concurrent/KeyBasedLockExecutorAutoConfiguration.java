package io.github.pangju666.framework.boot.autoconfigure.concurrent;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 基于键的锁执行器自动配置入口。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>启用 {@link KeyBasedLockExecutorProperties} 属性绑定。</li>
 *   <li>按条件导入进程内实现（Guava Striped）与分布式实现（Redisson）。</li>
 *   <li>仅在未存在自定义 {@code KeyBasedLockExecutor} Bean 时进行配置。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(KeyBasedLockExecutorProperties.class)
@Import({StripedConfiguration.class, RedissonConfiguration.class})
public class KeyBasedLockExecutorAutoConfiguration {
}
