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

public class StaticSpringContext implements ApplicationListener<ApplicationReadyEvent> {
	private static ApplicationContext CONTEXT;
	private static Environment ENVIRONMENT;
	private static BeanFactory BEAN_FACTORY;

	public static ApplicationContext getContext() {
		return CONTEXT;
	}

	public static Environment getEnvironment() {
		return ENVIRONMENT;
	}

	public static BeanFactory getBeanFactory() {
		return BEAN_FACTORY;
	}

	public static String getProperty(String key) {
		String propName = key;
		if (propName.startsWith("${") && propName.endsWith("}")) {
			propName = key.substring(2, key.length() - 1);
			return ENVIRONMENT.getProperty(propName);
		}
		return null;
	}

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