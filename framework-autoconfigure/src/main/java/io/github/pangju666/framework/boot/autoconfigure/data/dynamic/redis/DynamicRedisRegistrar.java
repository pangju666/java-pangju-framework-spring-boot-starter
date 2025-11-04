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

import io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis.config.JedisConnectionConfiguration;
import io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis.config.LettuceConnectionConfiguration;
import io.github.pangju666.framework.boot.autoconfigure.data.dynamic.redis.config.PropertiesRedisConnectionDetails;
import io.github.pangju666.framework.boot.data.dynamic.redis.utils.DynamicRedisUtils;
import io.github.pangju666.framework.data.redis.core.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.utils.RedisUtils;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * 动态Redis Bean注册器
 * <p>
 * 该类实现了Spring的{@link ImportBeanDefinitionRegistrar}接口，
 * 用于在运行时动态注册多个Redis连接相关的Bean。
 * 支持多个Redis数据源的配置和管理，可自动选择Jedis或Lettuce作为客户端库。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>解析{@link DynamicRedisProperties}配置属性</li>
 *     <li>为每个数据源注册{@link RedisConnectionDetails} Bean</li>
 *     <li>为每个数据源注册{@link RedisConnectionFactory} Bean</li>
 *     <li>为每个数据源注册{@link ScanRedisTemplate} Bean</li>
 *     <li>根据主数据源配置创建主Bean</li>
 *     <li>支持虚拟线程（Java 21+）</li>
 *     <li>支持Jedis和Lettuce两种客户端</li>
 * </ul>
 * </p>
 * <p>
 * Bean命名规则：
 * <ul>
 *     <li>{name}RedisConnectionDetails - Redis连接详情Bean</li>
 *     <li>{name}RedisConnectionFactory - Redis连接工厂Bean</li>
 *     <li>{name}RedisTemplate - Redis模板Bean</li>
 *     <li>redisTemplate - 主数据源对应的RedisTemplate Bean（primary=true）</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DynamicRedisProperties
 * @see DynamicRedisUtils
 * @see DynamicRedisAutoConfiguration
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class DynamicRedisRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger log = LoggerFactory.getLogger(DynamicRedisRegistrar.class);
	/**
	 * Redis连接详情Bean名称模板
	 * <p>
	 * 格式为：{name}RedisConnectionDetails
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String CONNECTION_DETAILS_BEAN_NAME_TEMPLATE = "%sRedisConnectionDetails";

	/**
	 * Spring Bean工厂
	 * <p>
	 * 用于获取容器中已有的Bean实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private BeanFactory beanFactory;
	/**
	 * Spring属性绑定器
	 * <p>
	 * 用于从环境中解析配置属性
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Binder binder;

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * 注册Bean定义
	 * <p>
	 * 该方法在Spring容器初始化时被调用，用于动态注册Redis相关Bean。
	 * 执行流程如下：
	 * </p>
	 * <ol>
	 *     <li>从配置中解析{@link DynamicRedisProperties}
	 *         <ul>
	 *             <li>如果配置不存在，直接返回</li>
	 *         </ul>
	 *     </li>
	 *     <li>验证配置的有效性
	 *         <ul>
	 *             <li>数据源集合不可为空</li>
	 *             <li>主数据源名称不可为空</li>
	 *             <li>主数据源必须存在于数据源集合中</li>
	 *         </ul>
	 *     </li>
	 *     <li>为每个配置的Redis数据源注册Bean定义
	 *         <ul>
	 *             <li>注册{@link RedisConnectionDetails} Bean</li>
	 *             <li>根据客户端类型选择Jedis或Lettuce创建{@link RedisConnectionFactory} Bean</li>
	 *             <li>根据序列化器配置创建{@link ScanRedisTemplate} Bean</li>
	 *         </ul>
	 *     </li>
	 *     <li>为主数据源的Bean设置primary标志
	 *         <ul>
	 *             <li>为主RedisConnectionDetails设置primary=true</li>
	 *             <li>为主RedisConnectionFactory设置primary=true</li>
	 *             <li>创建"redisTemplate"的primary Bean指向主数据源</li>
	 *         </ul>
	 *     </li>
	 * </ol>
	 *
	 * @param importingClassMetadata 导入类的注解元数据
	 * @param beanDefinitionRegistry Bean定义注册表，用于注册新的Bean定义
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		DynamicRedisProperties dynamicRedisProperties;

		try {
			dynamicRedisProperties = binder.bind(DynamicRedisProperties.PREFIX, DynamicRedisProperties.class).get();
			Assert.notEmpty(dynamicRedisProperties.getDatabases(), "动态Redis配置：数据源集合不可为空");
			Assert.hasText(dynamicRedisProperties.getPrimary(), "动态Redis配置：主数据源不可为空");
			if (!dynamicRedisProperties.getDatabases().containsKey(dynamicRedisProperties.getPrimary())) {
				throw new IllegalArgumentException("动态Redis配置：主数据源必须为存在的数据源");
			}
		} catch (NoSuchElementException e) {
			return;
		}

		Map<String, DynamicRedisProperties.RedisProperties> redisDatabases = dynamicRedisProperties.getDatabases();
		if (!CollectionUtils.isEmpty(redisDatabases)) {
			redisDatabases.forEach((name, redisProperties) -> {
				Supplier<RedisConnectionDetails> connectionDetailsSupplier = () -> new PropertiesRedisConnectionDetails(
					redisProperties, beanFactory.getBeanProvider(SslBundles.class).getIfAvailable());
				String connectionDetailsBeanName = CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder connectionDetailsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					RedisConnectionDetails.class, connectionDetailsSupplier);
				AbstractBeanDefinition connectionDetailsBeanDefinition = connectionDetailsBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionDetailsBeanName, connectionDetailsBeanDefinition);

				Supplier<RedisConnectionFactory> connectionFactorySupplier = () -> {
					RedisConnectionFactory connectionFactory;
					if (redisProperties.getClientType() == DynamicRedisProperties.RedisProperties.ClientType.JEDIS) {
						JedisConnectionConfiguration connectionConfiguration = new JedisConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							beanFactory.getBean(connectionDetailsBeanName, RedisConnectionDetails.class));
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.createRedisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						} else {
							connectionFactory = connectionConfiguration.createRedisConnectionFactory(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						}
					} else {
						ClientResources clientResources = beanFactory.getBean(ClientResources.class);
						LettuceConnectionConfiguration connectionConfiguration = new LettuceConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							beanFactory.getBean(connectionDetailsBeanName, RedisConnectionDetails.class));
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.createRedisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						} else {
							connectionFactory = connectionConfiguration.createRedisConnectionFactory(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						}
					}
					return connectionFactory;
				};
				String connectionFactoryBeanName = DynamicRedisUtils.getConnectionFactoryBeanName(name);
				BeanDefinitionBuilder connectionFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					RedisConnectionFactory.class, connectionFactorySupplier);
				connectionFactoryBeanBuilder.addDependsOn(connectionDetailsBeanName);
				AbstractBeanDefinition connectionFactoryBeanDefinition = connectionFactoryBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionFactoryBeanName, connectionFactoryBeanDefinition);

				Supplier<ScanRedisTemplate> redisTemplateSupplier = () -> {
					ScanRedisTemplate<Object, Object> redisTemplate = new ScanRedisTemplate<>();
					redisTemplate.setKeySerializer(RedisUtils.getSerializer(redisProperties.getKeySerializer()));
					redisTemplate.setValueSerializer(RedisUtils.getSerializer(redisProperties.getValueSerializer()));
					redisTemplate.setHashKeySerializer(RedisUtils.getSerializer(redisProperties.getHashKeySerializer()));
					redisTemplate.setHashValueSerializer(RedisUtils.getSerializer(redisProperties.getHashValueSerializer()));
					redisTemplate.setConnectionFactory(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
					return redisTemplate;
				};
				BeanDefinitionBuilder redisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					ScanRedisTemplate.class, redisTemplateSupplier);
				redisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition redisTemplateBeanDefinition = redisTemplateBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getTemplateBeanName(name), redisTemplateBeanDefinition);

				if (dynamicRedisProperties.getPrimary().equals(name)) {
					connectionDetailsBeanDefinition.setPrimary(true);
					connectionFactoryBeanDefinition.setPrimary(true);

					GenericBeanDefinition primaryRedisTemplateBeanDefinition = new GenericBeanDefinition(redisTemplateBeanDefinition);
					primaryRedisTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("redisTemplate", primaryRedisTemplateBeanDefinition);
				}

				log.info("dynamic-redis - add a database named [{}] success", name);
			});
			log.info("dynamic-redis initial loaded [{}] database,primary database named [{}]", redisDatabases.size(),
				dynamicRedisProperties.getPrimary());
		}
	}

	/**
	 * 检查是否支持虚拟线程
	 * <p>
	 * 虚拟线程需要满足以下条件：
	 * <ul>
	 *     <li>Java版本 >= 21</li>
	 *     <li>Thread类包含ofVirtual()方法</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果支持虚拟线程，会使用虚拟线程池来处理Redis连接，提高并发性能。
	 * </p>
	 *
	 * @return 如果支持虚拟线程返回true，否则返回false
	 * @since 1.0.0
	 */
	private boolean isVirtualThreadSupported() {
		// Java 版本必须 >= 21
		String version = System.getProperty("java.version");
		if (Integer.parseInt(StringUtils.substringBefore(version, ".")) < 21) {
			return false;
		}

		try {
			// 检查 Thread 中是否存在 ofVirtual() 方法
			Class<?> threadClass = Class.forName("java.lang.Thread");
			threadClass.getMethod("ofVirtual");
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			return false;
		}
	}
}
