package io.github.pangju666.framework.autoconfigure.core.context;

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
