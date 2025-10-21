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

package io.github.pangju666.framework.autoconfigure.data.redis;

import io.github.pangju666.framework.data.redis.bean.JavaScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.JsonScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.bean.StringScanRedisTemplate;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis自动配置类
 * <p>
 * 该类用于自动配置Redis相关的Template Bean。
 * 配置优先级在{@code org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}之后执行。
 * </p>
 * <p>
 * 提供的Bean包括：
 * <ul>
 *     <li>{@link StringScanRedisTemplate} - 字符串类型的Redis模板</li>
 *     <li>{@link JavaScanRedisTemplate} - Java对象序列化的Redis模板</li>
 *     <li>{@link JsonScanRedisTemplate} - JSON格式序列化的Redis模板</li>
 *     <li>{@link ScanRedisTemplate} - 通用的Redis扫描模板，支持自定义序列化方式</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see RedisProperties
 * @see ScanRedisTemplate
 * @see StringScanRedisTemplate
 * @see JavaScanRedisTemplate
 * @see JsonScanRedisTemplate
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@ConditionalOnClass({RedisOperations.class, ScanRedisTemplate.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {
	/**
	 * 创建字符串类型的Redis扫描模板
	 * <p>
	 * 该模板使用String作为key和value的序列化方式，适用于字符串数据的Redis操作。
	 * </p>
	 *
	 * @param connectionFactory Redis连接工厂
	 * @return 字符串Redis扫描模板实例
	 * @since 1.0.0
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public StringScanRedisTemplate stringScanRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringScanRedisTemplate(connectionFactory);
	}

	/**
	 * 创建Java对象序列化的Redis扫描模板
	 * <p>
	 * 该模板使用Java对象序列化方式存储数据，适用于需要保存复杂Java对象的场景。
	 * </p>
	 *
	 * @param redisConnectionFactory Redis连接工厂
	 * @return Java对象序列化的Redis扫描模板实例
	 * @since 1.0.0
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public JavaScanRedisTemplate javaScanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new JavaScanRedisTemplate(redisConnectionFactory);
	}

	/**
	 * 创建JSON格式序列化的Redis扫描模板
	 * <p>
	 * 该模板使用JSON作为序列化格式，适用于需要与其他系统交互或易于调试的场景。
	 * 相比Java序列化，JSON格式更具通用性和可读性。
	 * </p>
	 *
	 * @param redisConnectionFactory Redis连接工厂
	 * @return JSON格式序列化的Redis扫描模板实例
	 * @since 1.0.0
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public JsonScanRedisTemplate jsonScanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new JsonScanRedisTemplate(redisConnectionFactory);
	}

	/**
	 * 创建通用的Redis扫描模板
	 * <p>
	 * 该模板为通用型，支持自定义的序列化方式。根据{@link RedisProperties}配置，
	 * 可以灵活选择value和hashValue的序列化器。
	 * 默认key和hashKey使用String序列化方式。
	 * </p>
	 *
	 * @param redisProperties Redis序列化器配置属性
	 * @param connectionFactory Redis连接工厂
	 * @return 通用Redis扫描模板实例，key和hashKey为String，value和hashValue根据配置确定
	 * @since 1.0.0
	 * @see RedisProperties
	 */
	@Bean
	@ConditionalOnMissingBean(name = "scanRedisTemplate")
	@ConditionalOnSingleCandidate(RedisConnectionFactory.class)
	public ScanRedisTemplate<String, Object> scanRedisTemplate(RedisProperties redisProperties,
															   RedisConnectionFactory connectionFactory) {
		ScanRedisTemplate<String, Object> scanRedisTemplate = new ScanRedisTemplate<>();
		scanRedisTemplate.setConnectionFactory(connectionFactory);
		scanRedisTemplate.setKeySerializer(RedisSerializer.string());
		scanRedisTemplate.setHashKeySerializer(RedisSerializer.string());
		scanRedisTemplate.setValueSerializer(RedisUtils.getSerializer(redisProperties.getValue()));
		scanRedisTemplate.setHashValueSerializer(RedisUtils.getSerializer(redisProperties.getHashValue()));
		return scanRedisTemplate;
	}
}
