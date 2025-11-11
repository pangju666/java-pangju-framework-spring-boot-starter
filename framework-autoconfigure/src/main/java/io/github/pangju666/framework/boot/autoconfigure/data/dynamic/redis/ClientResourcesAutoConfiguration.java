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
 *
 * <p>copy from org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration#lettuceClientResources</p>
 *
 * @author pangju666
 * @see ClientResources
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
	public DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		DefaultClientResources.Builder builder = DefaultClientResources.builder();
		customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}
}
