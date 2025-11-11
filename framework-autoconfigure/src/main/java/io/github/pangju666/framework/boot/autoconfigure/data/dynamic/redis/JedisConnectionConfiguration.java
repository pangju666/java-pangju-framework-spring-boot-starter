/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisSslClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.SSLParameters;

/**
 * Redis connection configuration using Jedis.
 *
 * <p>copy from org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration</p>
 *
 * @author Mark Paluch
 * @author Stephane Nicoll
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Scott Frederick
 */
class JedisConnectionConfiguration extends RedisConnectionConfiguration {

	JedisConnectionConfiguration(RedisProperties properties,
								 ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
								 ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
								 ObjectProvider<RedisClusterConfiguration> clusterConfiguration, RedisConnectionDetails connectionDetails) {
		super(properties, connectionDetails, standaloneConfigurationProvider, sentinelConfiguration,
			clusterConfiguration);
	}

	public JedisConnectionFactory redisConnectionFactory(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		return createJedisConnectionFactory(builderCustomizers);
	}

	public JedisConnectionFactory redisConnectionFactoryVirtualThreads(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisConnectionFactory factory = createJedisConnectionFactory(builderCustomizers);
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
		executor.setVirtualThreads(true);
		factory.setExecutor(executor);
		return factory;
	}

	private JedisConnectionFactory createJedisConnectionFactory(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(builderCustomizers);
		return switch (this.mode) {
			case STANDALONE -> new JedisConnectionFactory(getStandaloneConfig(), clientConfiguration);
			case CLUSTER -> new JedisConnectionFactory(getClusterConfiguration(), clientConfiguration);
			case SENTINEL -> new JedisConnectionFactory(getSentinelConfig(), clientConfiguration);
		};
	}

	private JedisClientConfiguration getJedisClientConfiguration(
		ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		JedisClientConfigurationBuilder builder = applyProperties(JedisClientConfiguration.builder());
		applySslIfNeeded(builder);
		RedisProperties.Pool pool = getProperties().getJedis().getPool();
		if (isPoolEnabled(pool)) {
			applyPooling(pool, builder);
		}
		if (StringUtils.hasText(getProperties().getUrl())) {
			customizeConfigurationFromUrl(builder);
		}
		builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private JedisClientConfigurationBuilder applyProperties(JedisClientConfigurationBuilder builder) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(getProperties().getTimeout()).to(builder::readTimeout);
		map.from(getProperties().getConnectTimeout()).to(builder::connectTimeout);
		map.from(getProperties().getClientName()).whenHasText().to(builder::clientName);
		return builder;
	}

	private void applySslIfNeeded(JedisClientConfigurationBuilder builder) {
		SslBundle sslBundle = getSslBundle();
		if (sslBundle == null) {
			return;
		}
		JedisSslClientConfigurationBuilder sslBuilder = builder.useSsl();
		sslBuilder.sslSocketFactory(sslBundle.createSslContext().getSocketFactory());
		SslOptions sslOptions = sslBundle.getOptions();
		SSLParameters sslParameters = new SSLParameters();
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(sslOptions.getCiphers()).to(sslParameters::setCipherSuites);
		map.from(sslOptions.getEnabledProtocols()).to(sslParameters::setProtocols);
		sslBuilder.sslParameters(sslParameters);
	}

	private void applyPooling(RedisProperties.Pool pool,
							  JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
		builder.usePooling().poolConfig(jedisPoolConfig(pool));
	}

	private JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(pool.getMaxActive());
		config.setMaxIdle(pool.getMaxIdle());
		config.setMinIdle(pool.getMinIdle());
		if (pool.getTimeBetweenEvictionRuns() != null) {
			config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
		}
		if (pool.getMaxWait() != null) {
			config.setMaxWait(pool.getMaxWait());
		}
		return config;
	}

	private void customizeConfigurationFromUrl(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
		if (urlUsesSsl()) {
			builder.useSsl();
		}
	}

}
