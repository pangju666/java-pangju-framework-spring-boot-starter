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

package io.github.pangju666.framework.autoconfigure.data.dynamic.redis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis connection configuration using Lettuce.
 *
 * @author Mark Paluch
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 * @author Phillip Webb
 * @author Scott Frederick
 */
public class LettuceConnectionConfiguration extends RedisConnectionConfiguration {
	public LettuceConnectionConfiguration(RedisProperties properties,
										  ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
										  ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
										  ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
										  RedisConnectionDetails connectionDetails) {
		super(properties, connectionDetails, standaloneConfigurationProvider, sentinelConfigurationProvider,
			clusterConfigurationProvider);
	}

	public LettuceConnectionFactory createRedisConnectionFactory(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		return createConnectionFactory(clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers,
			clientResources);
	}

	public LettuceConnectionFactory createRedisConnectionFactoryVirtualThreads(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		LettuceConnectionFactory factory = createConnectionFactory(clientConfigurationBuilderCustomizers,
			clientOptionsBuilderCustomizers, clientResources);
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
		executor.setVirtualThreads(true);
		factory.setExecutor(executor);
		return factory;
	}

	private LettuceConnectionFactory createConnectionFactory(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources) {
		LettuceClientConfiguration clientConfiguration = getLettuceClientConfiguration(
			clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers, clientResources,
			getProperties().getLettuce().getPool());
		return switch (this.mode) {
			case STANDALONE -> new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
			case CLUSTER -> new LettuceConnectionFactory(getClusterConfiguration(), clientConfiguration);
			case SENTINEL -> new LettuceConnectionFactory(getSentinelConfig(), clientConfiguration);
		};
	}

	private LettuceClientConfiguration getLettuceClientConfiguration(
		ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
		ClientResources clientResources, Pool pool) {
		LettuceClientConfigurationBuilder builder = createBuilder(pool);
		SslBundle sslBundle = getSslBundle();
		applyProperties(builder, sslBundle);
		if (StringUtils.hasText(getProperties().getUrl())) {
			customizeConfigurationFromUrl(builder);
		}
		builder.clientOptions(createClientOptions(clientOptionsBuilderCustomizers, sslBundle));
		builder.clientResources(clientResources);
		clientConfigurationBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private LettuceClientConfigurationBuilder createBuilder(Pool pool) {
		if (isPoolEnabled(pool)) {
			return new PoolBuilderFactory().createBuilder(pool);
		}
		return LettuceClientConfiguration.builder();
	}

	private void applyProperties(LettuceClientConfigurationBuilder builder, SslBundle sslBundle) {
		if (sslBundle != null) {
			builder.useSsl();
		}
		if (getProperties().getTimeout() != null) {
			builder.commandTimeout(getProperties().getTimeout());
		}
		if (getProperties().getLettuce() != null) {
			RedisProperties.Lettuce lettuce = getProperties().getLettuce();
			if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
				builder.shutdownTimeout(getProperties().getLettuce().getShutdownTimeout());
			}
			String readFrom = lettuce.getReadFrom();
			if (readFrom != null) {
				builder.readFrom(getReadFrom(readFrom));
			}
		}
		if (StringUtils.hasText(getProperties().getClientName())) {
			builder.clientName(getProperties().getClientName());
		}
	}

	private ReadFrom getReadFrom(String readFrom) {
		int index = readFrom.indexOf(':');
		if (index == -1) {
			return ReadFrom.valueOf(getCanonicalReadFromName(readFrom));
		}
		String name = getCanonicalReadFromName(readFrom.substring(0, index));
		String value = readFrom.substring(index + 1);
		return ReadFrom.valueOf(name + ":" + value);
	}

	private String getCanonicalReadFromName(String name) {
		StringBuilder canonicalName = new StringBuilder(name.length());
		name.chars()
			.filter(Character::isLetterOrDigit)
			.map(Character::toLowerCase)
			.forEach((c) -> canonicalName.append((char) c));
		return canonicalName.toString();
	}

	private ClientOptions createClientOptions(
		ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientConfigurationBuilderCustomizers,
		SslBundle sslBundle) {
		ClientOptions.Builder builder = initializeClientOptionsBuilder();
		Duration connectTimeout = getProperties().getConnectTimeout();
		if (connectTimeout != null) {
			builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
		}
		if (sslBundle != null) {
			io.lettuce.core.SslOptions.Builder sslOptionsBuilder = io.lettuce.core.SslOptions.builder();
			sslOptionsBuilder.keyManager(sslBundle.getManagers().getKeyManagerFactory());
			sslOptionsBuilder.trustManager(sslBundle.getManagers().getTrustManagerFactory());
			SslOptions sslOptions = sslBundle.getOptions();
			if (sslOptions.getCiphers() != null) {
				sslOptionsBuilder.cipherSuites(sslOptions.getCiphers());
			}
			if (sslOptions.getEnabledProtocols() != null) {
				sslOptionsBuilder.protocols(sslOptions.getEnabledProtocols());
			}
			builder.sslOptions(sslOptionsBuilder.build());
		}
		builder.timeoutOptions(TimeoutOptions.enabled());
		clientConfigurationBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

	private ClientOptions.Builder initializeClientOptionsBuilder() {
		if (getProperties().getCluster() != null) {
			ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
			Refresh refreshProperties = getProperties().getLettuce().getCluster().getRefresh();
			Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
				.dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
			if (refreshProperties.getPeriod() != null) {
				refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
			}
			if (refreshProperties.isAdaptive()) {
				refreshBuilder.enableAllAdaptiveRefreshTriggers();
			}
			return builder.topologyRefreshOptions(refreshBuilder.build());
		}
		return ClientOptions.builder();
	}

	private void customizeConfigurationFromUrl(LettuceClientConfigurationBuilder builder) {
		if (urlUsesSsl()) {
			builder.useSsl();
		}
	}

	/**
	 * Inner class to allow optional commons-pool2 dependency.
	 */
	private static final class PoolBuilderFactory {

		LettuceClientConfigurationBuilder createBuilder(Pool properties) {
			return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
		}

		private GenericObjectPoolConfig<StatefulConnection<?, ?>> getPoolConfig(Pool properties) {
			GenericObjectPoolConfig<StatefulConnection<?, ?>> config = new GenericObjectPoolConfig<>();
			config.setMaxTotal(properties.getMaxActive());
			config.setMaxIdle(properties.getMaxIdle());
			config.setMinIdle(properties.getMinIdle());
			if (properties.getTimeBetweenEvictionRuns() != null) {
				config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
			}
			if (properties.getMaxWait() != null) {
				config.setMaxWait(properties.getMaxWait());
			}
			return config;
		}

	}
}
