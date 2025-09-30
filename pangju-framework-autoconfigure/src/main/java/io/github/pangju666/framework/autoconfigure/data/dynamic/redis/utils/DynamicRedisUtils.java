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
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class DynamicRedisUtils {
	private static final String CONNECTION_FACTORY_BEAN_NAME_TEMPLATE = "%sRedisConnectionFactory";
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sRedisTemplate";
	private static final String CONNECTION_DETAILS_BEAN_NAME_TEMPLATE = "%sRedisConnectionDetails";

	protected DynamicRedisUtils() {
	}

	public static String getConnectionDetailsBeanName(String name) {
		return CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static String getConnectionFactoryBeanName(String name) {
		return CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static String getTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static RedisConnectionDetails getConnectionDetails(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name), RedisConnectionDetails.class);
	}

	public static RedisConnectionFactory getConnectionFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), RedisConnectionFactory.class);
	}

	public static RedisTemplate<Object, Object> getTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), RedisTemplate.class);
	}
}
