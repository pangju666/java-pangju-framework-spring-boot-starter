package io.github.pangju666.framework.autoconfigure.web.limit;

import io.github.pangju666.framework.autoconfigure.web.WebMvcAutoConfiguration;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RequestRateLimitProperties.class)
@Import({Resilience4jRequestRateLimiterConfiguration.class, RedissonRequestRateLimiterConfiguration.class})
public class RequestRateLimiterAutoConfiguration {
}
