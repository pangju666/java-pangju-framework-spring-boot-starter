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

package io.github.pangju666.framework.boot.autoconfigure.data.redis;

import io.github.pangju666.framework.data.redis.core.JsonScanRedisTemplate;
import io.github.pangju666.framework.data.redis.core.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.core.StringScanRedisTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Redis 自动配置
 * <p>
 * 在存在 Redis 相关类（{@link RedisOperations}、{@link ScanRedisTemplate}）时生效，
 * 并在 Spring Boot 默认的 {@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}
 * 之后进行配置。该自动配置按条件注册扫描能力的 RedisTemplate 实现，提供字符串模板和 JSON 序列化模板两种变体。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@ConditionalOnClass({RedisOperations.class, ScanRedisTemplate.class})
public class RedisAutoConfiguration {
	/**
	 * 注册支持扫描能力的字符串 RedisTemplate
	 * <p>
	 * 当上下文中不存在同类型 Bean 且存在唯一的 {@link RedisConnectionFactory} 候选时，
	 * 创建并注册 {@link StringScanRedisTemplate}。
	 * </p>
	 *
	 * @param connectionFactory Redis 连接工厂（唯一候选）
	 * @return 字符串扫描模板 Bean
	 * @since 1.0.0
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public StringScanRedisTemplate stringScanRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringScanRedisTemplate(connectionFactory);
	}

	/**
	 * 注册支持扫描能力的通用 RedisTemplate（JSON 序列化）
	 * <p>
	 * 当上下文中不存在名为 <code>scanRedisTemplate</code> 的 Bean 且存在唯一的
	 * {@link RedisConnectionFactory} 候选时，创建并注册使用 JSON 序列化的
	 * {@link JsonScanRedisTemplate}，以 {@link ScanRedisTemplate} 形式暴露。
	 * </p>
	 *
	 * @param connectionFactory Redis 连接工厂（唯一候选）
	 * @return 通用扫描模板 Bean（JSON 序列化）
	 * @since 1.0.0
	 */
	@Bean
	@ConditionalOnMissingBean(name = "scanRedisTemplate")
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public ScanRedisTemplate<Object> scanRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new JsonScanRedisTemplate(connectionFactory);
	}
}
