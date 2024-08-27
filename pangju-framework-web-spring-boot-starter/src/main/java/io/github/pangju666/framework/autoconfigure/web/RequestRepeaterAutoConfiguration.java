package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.aspect.RequestRepeatAspect;
import io.github.pangju666.framework.autoconfigure.web.configuration.repeater.ExpireMapRequestRepeaterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.configuration.repeater.RedisRequestRepeaterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.properties.RequestRepeatProperties;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RequestRepeatProperties.class)
@Import({ExpireMapRequestRepeaterConfiguration.class, RedisRequestRepeaterConfiguration.class})
public class RequestRepeaterAutoConfiguration {
	@AutoConfiguration(after = AopAutoConfiguration.class)
	public static class RequestRepeatAopAutoConfiguration {
		@Bean
		public RequestRepeatAspect requestRepeatAspect(RequestRepeater requestRepeater) {
			return new RequestRepeatAspect(requestRepeater);
		}
	}
}
