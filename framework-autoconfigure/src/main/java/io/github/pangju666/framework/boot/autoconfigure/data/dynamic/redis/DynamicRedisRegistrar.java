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

import io.github.pangju666.framework.boot.data.dynamic.redis.DynamicRedisUtils;
import io.github.pangju666.framework.data.redis.core.ScanRedisTemplate;
import io.github.pangju666.framework.data.redis.core.StringScanRedisTemplate;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * 动态 Redis Bean 注册器。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>实现 {@link ImportBeanDefinitionRegistrar}，在容器启动阶段动态注册多个 Redis 相关 Bean。</li>
 *   <li>解析 {@link DynamicRedisProperties}，按数据源名称为每个数据源注册连接详情、连接工厂与多种模板。</li>
 *   <li>支持 Jedis 与 Lettuce 客户端库，并在满足条件时启用虚拟线程（Java 21+）。</li>
 *   <li>根据主数据源配置为相关 Bean 设置 {@code primary} 标志，提供默认注入指向。</li>
 * </ul>
 *
 * <p><strong>命名规则</strong>（与 {@link DynamicRedisUtils} 保持一致）</p>
 * <ul>
 *   <li>{name}RedisConnectionDetails：连接详情 Bean。</li>
 *   <li>{name}RedisConnectionFactory：连接工厂 Bean。</li>
 *   <li>{name}RedisTemplate：对象键值模板 Bean。</li>
 *   <li>{name}StringRedisTemplate：字符串键值模板 Bean。</li>
 *   <li>{name}ScanRedisTemplate：支持游标扫描的模板 Bean。</li>
 *   <li>{name}StringScanRedisTemplate：支持游标扫描的字符串模板 Bean。</li>
 * </ul>
 *
 * @author pangju666
 * @see DynamicRedisProperties
 * @see DynamicRedisUtils
 * @see DynamicRedisAutoConfiguration
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class DynamicRedisRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger log = LoggerFactory.getLogger(DynamicRedisRegistrar.class);
    /**
     * 连接详情 Bean 名称模板（{name}RedisConnectionDetails）。
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
     * 注册 Bean 定义。
     *
     * <p><b>流程</b>：解析 {@code spring.data.redis.dynamic} -> 校验配置（非空、主库存在）->
     * 为每个数据源注册连接详情 -> 选择 Jedis/Lettuce 创建连接工厂（按虚拟线程能力）->
     * 注册模板（对象/字符串/扫描/字符串扫描）并依赖连接工厂 -> 为主数据源设置 {@code primary} 标志 -> 记录日志。</p>
     * <p><b>约束</b>：当未配置属性或缺失绑定时不进行注册；命名与依赖遵循统一模板与注册顺序。</p>
     *
     * @param importingClassMetadata 导入类的注解元数据
     * @param beanDefinitionRegistry Bean 定义注册表
     */
	@SuppressWarnings("rawtypes")
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
				// 注册 RedisConnectionDetails
				Supplier<RedisConnectionDetails> connectionDetailsSupplier = () -> new PropertiesRedisConnectionDetails(
					redisProperties, beanFactory.getBeanProvider(SslBundles.class));
				String connectionDetailsBeanName = CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder connectionDetailsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					RedisConnectionDetails.class, connectionDetailsSupplier);
				AbstractBeanDefinition connectionDetailsBeanDefinition = connectionDetailsBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionDetailsBeanName, connectionDetailsBeanDefinition);

				// 注册 RedisConnectionFactory
				String connectionFactoryBeanName = DynamicRedisUtils.getRedisConnectionFactoryBeanName(name);
				BeanDefinitionBuilder connectionFactoryBeanBuilder;
				// JedisConnectionFactory
				if (redisProperties.getClientType() == DynamicRedisProperties.RedisProperties.ClientType.JEDIS) {
					Supplier<JedisConnectionFactory> connectionFactorySupplier = () -> {
						JedisConnectionFactory connectionFactory;
						JedisConnectionConfiguration connectionConfiguration = new JedisConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							beanFactory.getBean(connectionDetailsBeanName, RedisConnectionDetails.class));
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.redisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						} else {
							connectionFactory = connectionConfiguration.redisConnectionFactory(
								beanFactory.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
						}
						return connectionFactory;
					};
					connectionFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						JedisConnectionFactory.class, connectionFactorySupplier);
				} else { // LettuceConnectionFactory
					Supplier<LettuceConnectionFactory> connectionFactorySupplier = () -> {
						LettuceConnectionFactory connectionFactory;
						ClientResources clientResources = beanFactory.getBean(ClientResources.class);
						LettuceConnectionConfiguration connectionConfiguration = new LettuceConnectionConfiguration(redisProperties,
							beanFactory.getBeanProvider(RedisStandaloneConfiguration.class),
							beanFactory.getBeanProvider(RedisSentinelConfiguration.class),
							beanFactory.getBeanProvider(RedisClusterConfiguration.class),
							beanFactory.getBean(connectionDetailsBeanName, RedisConnectionDetails.class));
						if (isVirtualThreadSupported()) {
							connectionFactory = connectionConfiguration.redisConnectionFactoryVirtualThreads(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						} else {
							connectionFactory = connectionConfiguration.redisConnectionFactory(
								beanFactory.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
								beanFactory.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
								clientResources);
						}
						return connectionFactory;
					};
					connectionFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						LettuceConnectionFactory.class, connectionFactorySupplier);
				}
				connectionFactoryBeanBuilder.addDependsOn(connectionDetailsBeanName);
				AbstractBeanDefinition connectionFactoryBeanDefinition = connectionFactoryBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionFactoryBeanName, connectionFactoryBeanDefinition);

				// 注册 RedisTemplate
				Supplier<RedisTemplate> redisTemplateSupplier = () -> {
					RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
					redisTemplate.setKeySerializer(redisProperties.getKeySerializer().getSerializer());
					redisTemplate.setValueSerializer(redisProperties.getValueSerializer().getSerializer());
					redisTemplate.setHashKeySerializer(redisProperties.getHashKeySerializer().getSerializer());
					redisTemplate.setHashValueSerializer(redisProperties.getHashValueSerializer().getSerializer());
					redisTemplate.setConnectionFactory(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
					return redisTemplate;
				};
				BeanDefinitionBuilder redisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					RedisTemplate.class, redisTemplateSupplier);
				redisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition redisTemplateBeanDefinition = redisTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getRedisTemplateBeanName(name), redisTemplateBeanDefinition);

				// 注册 StringRedisTemplate
				Supplier<StringRedisTemplate> stringRedisTemplateSupplier = () ->
					new StringRedisTemplate(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
				BeanDefinitionBuilder stringRedisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					StringRedisTemplate.class, stringRedisTemplateSupplier);
				stringRedisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition stringRedisTemplateBeanDefinition = stringRedisTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getStringRedisTemplateBeanName(name), stringRedisTemplateBeanDefinition);

				// 注册 ScanRedisTemplate
				Supplier<ScanRedisTemplate> scanRedisTemplateSupplier = () -> {
					ScanRedisTemplate<Object> scanRedisTemplate = new ScanRedisTemplate<>();
					scanRedisTemplate.setValueSerializer(redisProperties.getValueSerializer().getSerializer());
					scanRedisTemplate.setHashValueSerializer(redisProperties.getHashValueSerializer().getSerializer());
					scanRedisTemplate.setConnectionFactory(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
					return scanRedisTemplate;
				};
				BeanDefinitionBuilder scanRedisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					ScanRedisTemplate.class, scanRedisTemplateSupplier);
				scanRedisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition scanRedisTemplateBeanDefinition = scanRedisTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getScanRedisTemplateBeanName(name), scanRedisTemplateBeanDefinition);

				// 注册 StringScanRedisTemplate
				Supplier<StringScanRedisTemplate> stringScanRedisTemplateSupplier = () ->
					new StringScanRedisTemplate(beanFactory.getBean(connectionFactoryBeanName, RedisConnectionFactory.class));
				BeanDefinitionBuilder stringScanRedisTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					StringScanRedisTemplate.class, stringScanRedisTemplateSupplier);
				stringScanRedisTemplateBeanBuilder.addDependsOn(connectionFactoryBeanName);
				AbstractBeanDefinition stringScanRedisTemplateBeanDefinition = stringScanRedisTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(DynamicRedisUtils.getStringRedisScanTemplateBeanName(name), stringScanRedisTemplateBeanDefinition);

				if (dynamicRedisProperties.getPrimary().equals(name)) {
					connectionDetailsBeanDefinition.setPrimary(true);
					connectionFactoryBeanDefinition.setPrimary(true);
					stringRedisTemplateBeanDefinition.setPrimary(true);
					stringScanRedisTemplateBeanDefinition.setPrimary(true);

					GenericBeanDefinition primaryRedisTemplateBeanDefinition = new GenericBeanDefinition(redisTemplateBeanDefinition);
					primaryRedisTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("redisTemplate", primaryRedisTemplateBeanDefinition);

					GenericBeanDefinition primaryScanRedisTemplateBeanDefinition = new GenericBeanDefinition(scanRedisTemplateBeanDefinition);
					primaryScanRedisTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("scanRedisTemplate", primaryScanRedisTemplateBeanDefinition);
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
