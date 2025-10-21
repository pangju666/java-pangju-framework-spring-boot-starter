/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.autoconfigure.web.limit.config;

import io.github.pangju666.framework.autoconfigure.web.limit.RateLimitProperties;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.RateLimiter;
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.impl.Resilience4JRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j限流器自动配置类
 * <p>
 * 该配置类用于在Spring Boot应用启动时自动配置基于Resilience4j的限流实现。
 * 通过条件注解判断是否应该启用Resilience4j限流器，并将其注册为Spring Bean。
 * </p>
 * <p>
 * 激活条件：
 * <ul>
 *     <li>配置属性 pangju.web.rate-limit.type = RESILIENCE4J，或</li>
 *     <li>未指定type属性（默认使用RESILIENCE4J）</li>
 * </ul>
 * </p>
 * <p>
 * 配置特性：
 * <ul>
 *     <li>条件激活 - 仅在满足配置条件时才加载此配置</li>
 *     <li>Bean代理禁用 - 设置proxyBeanMethods=false提高性能</li>
 *     <li>自动降级 - 如果已存在其他RateLimiter实现则不加载</li>
 *     <li>简化配置 - 无需手动配置，自动创建Resilience4J限流器</li>
 * </ul>
 * </p>
 * <p>
 * 与其他配置的关系：
 * <ul>
 *     <li>与RedissonRateLimiterConfiguration互斥 - 根据type配置选择其中一个</li>
 *     <li>与RateLimiterAutoConfiguration协调 - 提供具体的RateLimiter实现</li>
 *     <li>所有限流配置基于RateLimitProperties进行配置</li>
 * </ul>
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *     <li>单机应用 - 无需Redis依赖</li>
 *     <li>开发和测试环境 - 快速集成，无需外部服务</li>
 *     <li>对性能要求极高的场景 - 内存操作，低延迟</li>
 *     <li>不需要跨应用共享限流配额的应用</li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * {@code
 * # application.yml 配置示例
 * pangju:
 *   web:
 *     rate-limit:
 *       type: RESILIENCE4J  # 指定使用Resilience4j实现
 *
 * # 如果不指定type，默认使用RESILIENCE4J
 * pangju:
 *   web:
 *     rate-limit: {}
 * }
 * </pre>
 * </p>
 * <p>
 * 与RedissonRateLimiterConfiguration的切换：
 * <pre>
 * {@code
 * # 切换到Redisson实现（需要Redis）
 * pangju:
 *   web:
 *     rate-limit:
 *       type: REDISSON
 *       redisson:
 *         redisson-client-bean-name: redissonClient
 *         key-prefix: my-app-rate-limit
 * }
 * </pre>
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>Spring Boot启动时扫描条件注解</li>
 *     <li>检查配置属性 pangju.web.rate-limit.type 的值</li>
 *     <li>如果值为 RESILIENCE4J 或未指定，加载此配置类</li>
 *     <li>检查Spring容器中是否存在RateLimiter Bean</li>
 *     <li>如果不存在，创建Resilience4JRateLimiter Bean并注册到容器</li>
 *     <li>其他组件通过自动注入获取RateLimiter实例</li>
 * </ol>
 * </p>
 * <p>
 * Bean代理禁用说明：
 * <p>
 * {@code @Configuration(proxyBeanMethods = false)}表示不为Bean方法创建代理。
 * 这提高了性能，避免额外的代理开销。在这种情况下，多次调用Bean方法会创建多个实例，
 * 但由于{@code @Bean}方法通常只调用一次，这不是问题。
 * </p>
 * </p>
 * <p>
 * 条件注解优先级：
 * <ul>
 *     <li>优先级1：@ConditionalOnProperty - 检查配置属性</li>
 *     <li>优先级2：@ConditionalOnMissingBean - 检查容器中是否已存在Bean</li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由Spring Boot自动配置机制自动加载</li>
 *     <li>创建的Resilience4JRateLimiter被RateLimitInterceptor使用</li>
 *     <li>通过RateLimitProperties获取配置信息</li>
 *     <li>与RedissonRateLimiterConfiguration组成可选择的实现</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see Resilience4JRateLimiter
 * @see RateLimiter
 * @see RateLimitProperties
 * @see RedissonRateLimiterConfiguration
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(io.github.resilience4j.ratelimiter.RateLimiter.class)
@ConditionalOnProperty(prefix = "pangju.web.rate-limit", value = "type", havingValue = "RESILIENCE4J", matchIfMissing = true)
public class Resilience4jRateLimiterConfiguration {
	/**
	 * 创建Resilience4J限流器Bean
	 * <p>
	 * 该方法在满足以下条件时被调用：
	 * <ul>
	 *     <li>配置属性 pangju.web.rate-limit.type = RESILIENCE4J 或未指定</li>
	 *     <li>Spring容器中不存在RateLimiter类型的Bean</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 创建后的Bean：
	 * <ul>
	 *     <li>自动注册到Spring容器</li>
	 *     <li>可通过{@code @Autowired}或{@code @Inject}注入到其他组件</li>
	 *     <li>作为RateLimiter接口的具体实现</li>
	 *     <li>被RateLimitInterceptor自动注入使用</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 该Bean的生命周期：
	 * <ol>
	 *     <li>应用启动时创建</li>
	 *     <li>在RateLimitInterceptor中注入</li>
	 *     <li>在请求拦截时被调用进行限流检查</li>
	 *     <li>应用关闭时销毁</li>
	 * </ol>
	 * </p>
	 *
	 * @return 新创建的Resilience4JRateLimiter实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(RateLimiter.class)
	@Bean
	public Resilience4JRateLimiter resilience4JRateLimiter() {
		return new Resilience4JRateLimiter();
	}
}
