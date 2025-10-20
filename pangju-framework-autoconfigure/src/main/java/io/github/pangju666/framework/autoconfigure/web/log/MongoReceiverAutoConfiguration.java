package io.github.pangju666.framework.autoconfigure.web.log;

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.impl.MongoWebLogReceiver;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(after = MongoDataAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, MongoClient.class, MongoTemplate.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MongoReceiverAutoConfiguration {
	@ConditionalOnMissingBean(WebLogReceiver.class)
	@ConditionalOnBean(MongoTemplate.class)
	@Bean
	public WebLogReceiver mongoWebLogReceiver(WebLogProperties properties, BeanFactory beanFactory) {
		return new MongoWebLogReceiver(properties, beanFactory);
	}
}
