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

package io.github.pangju666.framework.autoconfigure.data.dynamic.redis.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisUtils {
	private static final String CONNECTION_FACTORY_BEAN_NAME_TEMPLATE = "%sRedisConnectionFactory";
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sRedisTemplate";

	protected DynamicRedisUtils() {
	}

	public static String getRedisConnectionFactoryBeanName(String name) {
		return CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static RedisConnectionFactory getRedisConnectionFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), RedisConnectionFactory.class);
	}

	public static String getRedisTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static RedisTemplate<Object, Object> getRedisTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), RedisTemplate.class);
	}
}
