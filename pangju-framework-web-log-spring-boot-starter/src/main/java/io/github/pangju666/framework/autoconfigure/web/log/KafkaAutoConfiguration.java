package io.github.pangju666.framework.autoconfigure.web.log;

import io.github.pangju666.framework.autoconfigure.web.log.listener.WebLogKafkaListener;
import io.github.pangju666.framework.autoconfigure.web.log.properties.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.sender.WebLogSender;
import io.github.pangju666.framework.autoconfigure.web.log.sender.impl.KafkaWebLogSender;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class, KafkaTemplate.class})
@ConditionalOnProperty(prefix = "chang-tech.web.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaAutoConfiguration {
	@ConditionalOnProperty(prefix = "chang-tech.web.log", name = "kafka.topic")
	@ConditionalOnMissingBean(WebLogSender.class)
	@ConditionalOnBean(KafkaTemplate.class)
	@Bean
	public KafkaWebLogSender kafkaWebLogSender(WebLogProperties properties, BeanFactory beanFactory) {
		return new KafkaWebLogSender(properties, beanFactory);
	}

	@ConditionalOnProperty(prefix = "chang-tech.web.log", name = "kafka.topic")
	@ConditionalOnBean(KafkaWebLogSender.class)
	@Bean
	public WebLogKafkaListener webLogKafkaListener(BeanFactory beanFactory) {
		return new WebLogKafkaListener(beanFactory);
	}
}
