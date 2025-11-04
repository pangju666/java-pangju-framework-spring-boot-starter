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

package io.github.pangju666.framework.boot.data.dynamic.redis.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 动态Redis工具类
 * <p>
 * 提供用于生成和获取动态Redis Bean名称的工具方法。
 * 用于在运行时动态注册和访问多个Redis连接相关的Bean。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DynamicRedisUtils {
	/**
	 * Redis连接工厂Bean名称模板
	 * <p>
	 * 格式为：{name}RedisConnectionFactory
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String CONNECTION_FACTORY_BEAN_NAME_TEMPLATE = "%sRedisConnectionFactory";
	/**
	 * Redis模板Bean名称模板
	 * <p>
	 * 格式为：{name}RedisTemplate
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sRedisTemplate";

	protected DynamicRedisUtils() {
	}

	/**
	 * 根据数据源名称获取Redis连接工厂Bean名称
	 *
	 * @param name 数据源名称
	 * @return Redis连接工厂Bean名称
	 * @since 1.0.0
	 */
	public static String getConnectionFactoryBeanName(String name) {
		return CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 根据数据源名称获取RedisTemplateBean名称
	 *
	 * @param name 数据源名称
	 * @return RedisTemplate Bean名称
	 * @since 1.0.0
	 */
	public static String getTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 从Bean工厂中获取指定名称的Redis连接工厂
	 *
	 * @param name 数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return Redis连接工厂实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static RedisConnectionFactory getConnectionFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), RedisConnectionFactory.class);
	}

	/**
	 * 从Bean工厂中获取指定名称的RedisTemplate
	 *
	 * @param name 数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return RedisTemplate实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static RedisTemplate<Object, Object> getTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), RedisTemplate.class);
	}
}
