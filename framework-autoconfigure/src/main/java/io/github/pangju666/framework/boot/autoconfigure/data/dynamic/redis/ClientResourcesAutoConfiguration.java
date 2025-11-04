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

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Lettuce客户端资源自动配置类
 * <p>
 * 该类用于自动配置Lettuce Redis客户端所需的{@link ClientResources} Bean。
 * Lettuce是一个高性能、线程安全的Redis客户端库，该配置类为其提供全局的客户端资源管理。
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>Lettuce库必须在Classpath中（通过检查{@link RedisClient}类）</li>
 *     <li>Redis客户端类型必须为Lettuce或未指定（默认为Lettuce）
 *         <ul>
 *             <li>配置属性：{@code spring.data.redis.client-type=lettuce}</li>
 *             <li>如果未配置该属性，则默认使用Lettuce</li>
 *         </ul>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>创建全局的{@link DefaultClientResources}实例</li>
 *     <li>管理Lettuce的线程池和事件循环资源</li>
 *     <li>支持通过{@link ClientResourcesBuilderCustomizer}进行自定义配置</li>
 *     <li>自动在Spring容器关闭时释放资源</li>
 * </ul>
 * </p>
 * <p>
 * 资源管理：
 * <ul>
 *     <li>Bean的生命周期方法为{@code shutdown}，Spring容器关闭时自动调用</li>
 *     <li>该Bean是全局单例，所有Lettuce连接共享同一个ClientResources</li>
 *     <li>优化了线程使用和网络I/O效率</li>
 * </ul>
 * </p>
 * <p>
 * 与其他组件的关系：
 * <ul>
 *     <li>该配置类由{@link DynamicRedisAutoConfiguration}依赖</li>
 *     <li>在Spring Boot的{@code RedisAutoConfiguration}之前加载</li>
 *     <li>为所有Lettuce连接提供全局资源</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see ClientResources
 * @see DefaultClientResources
 * @see ClientResourcesBuilderCustomizer
 * @see DynamicRedisAutoConfiguration
 * @see io.lettuce.core.RedisClient
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = "spring.data.redis.client-type", havingValue = "lettuce", matchIfMissing = true)
public class ClientResourcesAutoConfiguration {
	/**
	 * 创建Lettuce客户端资源
	 * <p>
	 * 该Bean为全局单例，所有Lettuce Redis连接都将共享这个资源实例。
	 * Bean的销毁方法为{@code shutdown}，在Spring容器关闭时自动释放资源。
	 * </p>
	 * <p>
	 * 创建流程：
	 * <ol>
	 *     <li>使用{@link DefaultClientResources#builder()}创建构建器</li>
	 *     <li>应用所有{@link ClientResourcesBuilderCustomizer}的自定义配置</li>
	 *     <li>调用{@code build()}生成最终的ClientResources实例</li>
	 * </ol>
	 * </p>
	 * <p>
	 * 资源特性：
	 * <ul>
	 *     <li>管理I/O线程池 - 处理网络I/O操作</li>
	 *     <li>管理计算线程池 - 处理异步操作</li>
	 *     <li>支持事件循环组配置</li>
	 *     <li>可配置超时和重连策略</li>
	 * </ul>
	 * </p>
	 *
	 * @param customizers {@link ClientResourcesBuilderCustomizer}提供者
	 *                    用于对{@link DefaultClientResources.Builder}进行自定义配置。
	 *                    多个定制器会按顺序依次应用
	 * @return 配置完成的{@link DefaultClientResources}实例
	 * @see DefaultClientResources
	 * @see ClientResourcesBuilderCustomizer
	 * @since 1.0.0
	 */
	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean(ClientResources.class)
	DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		DefaultClientResources.Builder builder = DefaultClientResources.builder();
		customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}
}
