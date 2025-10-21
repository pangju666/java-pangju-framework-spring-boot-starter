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

package io.github.pangju666.framework.autoconfigure.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Spring上下文静态访问工具类
 * <p>
 * 提供静态方法访问Spring容器中的ApplicationContext、Environment和BeanFactory。
 * 该类实现了ApplicationListener接口，在应用程序准备就绪时初始化静态引用。
 * 主要用于在非Spring管理的类中获取Spring容器中的Bean和配置信息。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class StaticSpringContext implements ApplicationListener<ApplicationReadyEvent> {
	/**
	 * 应用上下文静态引用
	 *
	 * @since 1.0.0
	 */
	private static ApplicationContext CONTEXT;
	/**
	 * 环境配置静态引用
	 *
	 * @since 1.0.0
	 */
	private static Environment ENVIRONMENT;
	/**
	 * Bean工厂静态引用
	 *
	 * @since 1.0.0
	 */
	private static BeanFactory BEAN_FACTORY;

	/**
	 * 获取Spring应用上下文
	 *
	 * @return Spring应用上下文
	 * @since 1.0.0
	 */
	public static ApplicationContext getContext() {
		return CONTEXT;
	}

	/**
	 * 获取Spring环境配置
	 *
	 * @return Spring环境配置
	 * @since 1.0.0
	 */
	public static Environment getEnvironment() {
		return ENVIRONMENT;
	}

	/**
	 * 获取Spring Bean工厂
	 *
	 * @return Spring Bean工厂
	 * @since 1.0.0
	 */
	public static BeanFactory getBeanFactory() {
		return BEAN_FACTORY;
	}

	/**
	 * 获取配置属性值
	 * <p>
	 * 支持${property}格式的属性名解析
	 * </p>
	 *
	 * @param key 属性键名，可以是${property}格式
	 * @return 属性值，如果未找到则返回null
	 * @since 1.0.0
	 */
	public static String getProperty(String key) {
		String propName = key;
		if (propName.startsWith("${") && propName.endsWith("}")) {
			propName = key.substring(2, key.length() - 1);
			return ENVIRONMENT.getProperty(propName);
		}
		return null;
	}

	/**
	 * 应用程序准备就绪事件处理方法
	 * <p>
	 * 当Spring应用上下文准备就绪时，初始化静态引用
	 * </p>
	 *
	 * @param event 应用程序准备就绪事件
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if (Objects.isNull(CONTEXT)) {
			StaticSpringContext.CONTEXT = event.getApplicationContext();
		}
		if (Objects.isNull(ENVIRONMENT)) {
			StaticSpringContext.ENVIRONMENT = event.getApplicationContext().getEnvironment();
		}
		if (Objects.isNull(BEAN_FACTORY)) {
			StaticSpringContext.BEAN_FACTORY = event.getApplicationContext().getBeanFactory();
		}
	}
}