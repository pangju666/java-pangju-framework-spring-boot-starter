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
import io.github.pangju666.framework.autoconfigure.web.limit.limiter.impl.RedissonRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson限流器自动配置类
 * <p>
 * 该配置类用于在Spring Boot应用启动时自动配置基于Redisson的分布式限流实现。
 * 通过条件注解判断是否应该启用Redisson限流器，并将其注册为Spring Bean。
 * </p>
 * <p>
 * 激活条件（必须全部满足）：
 * <ul>
 *     <li>类路径中存在RedissonClient类（Redisson库必须存在）</li>
 *     <li>配置属性 pangju.web.rate-limit.type = REDISSON</li>
 *     <li>Spring容器中不存在其他RateLimiter实现</li>
 * </ul>
 * </p>
 * <p>
 * 配置特性：
 * <ul>
 *     <li>条件激活 - 仅在满足所有配置条件时才加载此配置</li>
 *     <li>Bean代理禁用 - 设置proxyBeanMethods=false提高性能</li>
 *     <li>自动降级 - 如果已存在其他RateLimiter实现则不加载</li>
 *     <li>依赖注入 - 自动注入RateLimitProperties和BeanFactory</li>
 *     <li>分布式支持 - 支持多个应用实例共享限流配额</li>
 * </ul>
 * </p>
 * <p>
 * 与其他配置的关系：
 * <ul>
 *     <li>与Resilience4jRateLimiterConfiguration互斥 - 根据type配置选择其中一个</li>
 *     <li>与RateLimiterAutoConfiguration协调 - 提供具体的RateLimiter实现</li>
 *     <li>依赖于Redis和Redisson客户端的正确配置</li>
 *     <li>所有限流配置基于RateLimitProperties进行配置</li>
 * </ul>
 * </p>
 * <p>
 * 适用场景：
 * <ul>
 *     <li>分布式应用 - 多个应用实例需要共享限流配额</li>
 *     <li>微服务架构 - 跨服务的全局限流控制</li>
 *     <li>云原生应用 - 容器化应用的动态扩缩容</li>
 *     <li>SaaS多租户应用 - 按租户的全局限流</li>
 *     <li>需要全局一致性的场景 - 防止单个实例突破总体限流</li>
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
 *       type: REDISSON  # 指定使用Redisson实现
 *       redisson:
 *         redisson-client-bean-name: redissonClient  # 可选，Redisson客户端Bean名称
 *         key-prefix: my-app-rate-limit  # Redis键前缀
 *
 * # Redis配置
 * spring:
 *   data:
 *     redis:
 *       url: redis://localhost:6379
 * }
 * </pre>
 * </p>
 * <p>
 * 依赖关系：
 * <ul>
 *     <li>Redisson库必须在项目依赖中</li>
 *     <li>Redis服务必须可用且正确配置</li>
 *     <li>RedissonClient Bean必须存在于Spring容器中</li>
 * </ul>
 * </p>
 * <p>
 * 与Resilience4jRateLimiterConfiguration的区别：
 * <table border="1" cellpadding="5">
 *   <tr>
 *     <th>特性</th>
 *     <th>RedissonRateLimiterConfiguration</th>
 *     <th>Resilience4jRateLimiterConfiguration</th>
 *   </tr>
 *   <tr>
 *     <td>限流范围</td>
 *     <td>分布式全局限流</td>
 *     <td>单机本地限流</td>
 *   </tr>
 *   <tr>
 *     <td>存储方式</td>
 *     <td>Redis存储</td>
 *     <td>内存存储</td>
 *   </tr>
 *   <tr>
 *     <td>延迟</td>
 *     <td>1-5ms（网络I/O）</td>
 *     <td>&lt;1ms（本地）</td>
 *   </tr>
 *   <tr>
 *     <td>外部依赖</td>
 *     <td>需要Redis</td>
 *     <td>无外部依赖</td>
 *   </tr>
 *   <tr>
 *     <td>激活配置</td>
 *     <td>type=REDISSON</td>
 *     <td>type=RESILIENCE4J或默认</td>
 *   </tr>
 * </table>
 * </p>
 * <p>
 * 工作流程：
 * <ol>
 *     <li>Spring Boot启动时扫描条件注解</li>
 *     <li>检查类路径中是否存在RedissonClient类</li>
 *     <li>检查配置属性 pangju.web.rate-limit.type 的值是否为 REDISSON</li>
 *     <li>如果两个条件都满足，加载此配置类</li>
 *     <li>检查Spring容器中是否存在RateLimiter Bean</li>
 *     <li>如果不存在，创建RedissonRateLimiter Bean并注册到容器</li>
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
 * 条件注解执行顺序：
 * <ol>
 *     <li>@ConditionalOnClass - 首先检查类路径中是否存在RedissonClient</li>
 *     <li>@ConditionalOnProperty - 然后检查配置属性值</li>
 *     <li>@ConditionalOnMissingBean - 最后检查容器中是否已存在Bean</li>
 * </ol>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>由Spring Boot自动配置机制自动加载</li>
 *     <li>创建的RedissonRateLimiter被RateLimitInterceptor使用</li>
 *     <li>通过RateLimitProperties和BeanFactory获取依赖</li>
 *     <li>与Resilience4jRateLimiterConfiguration组成可选择的实现</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RedissonRateLimiter
 * @see RateLimiter
 * @see RateLimitProperties
 * @see Resilience4jRateLimiterConfiguration
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({RedissonClient.class})
@ConditionalOnProperty(prefix = "pangju.web.rate-limit", value = "type", havingValue = "REDISSON")
public class RedissonRateLimiterConfiguration {
	/**
	 * 创建Redisson限流器Bean
	 * <p>
	 * 该方法在满足以下条件时被调用：
	 * <ul>
	 *     <li>类路径中存在RedissonClient类</li>
	 *     <li>配置属性 pangju.web.rate-limit.type = REDISSON</li>
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
	 *     <li>支持分布式环境下多个应用实例的限流协调</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 依赖关系：
	 * <ul>
	 *     <li>RateLimitProperties - 提供限流配置信息（Redis键前缀等）</li>
	 *     <li>BeanFactory - 用于获取RedissonClient实例</li>
	 * </ul>
	 * </p>
	 * <p>
	 * RedissonClient获取策略：
	 * <ol>
	 *     <li>如果RateLimitProperties中指定了redisson-client-bean-name，使用指定的Bean</li>
	 *     <li>否则使用容器中的默认RedissonClient Bean</li>
	 * </ol>
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
	 * @param properties  限流配置属性，包含Redis相关配置和Redisson客户端配置
	 * @param beanFactory Spring Bean工厂，用于获取RedissonClient实例
	 * @return 新创建的RedissonRateLimiter实例，已完全初始化并准备使用
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(RateLimiter.class)
	@Bean
	public RedissonRateLimiter redissonRateLimiter(RateLimitProperties properties,
												   BeanFactory beanFactory) {
		return new RedissonRateLimiter(properties, beanFactory);
	}
}
