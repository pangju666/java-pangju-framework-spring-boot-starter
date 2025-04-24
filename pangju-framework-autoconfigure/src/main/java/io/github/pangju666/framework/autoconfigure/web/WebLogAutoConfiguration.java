package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.autoconfigure.web.filter.WebLogFilter;
import io.github.pangju666.framework.autoconfigure.web.handler.WebLogHandler;
import io.github.pangju666.framework.autoconfigure.web.properties.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.sender.WebLogSender;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@ConditionalOnProperty(prefix = "pangju.web.log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebLogProperties.class)
public class WebLogAutoConfiguration {
	private Set<String> excludePathPatterns = Collections.emptySet();

	/*@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternsProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternsProvider::getExcludePaths)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}*/

	@ConditionalOnBean({WebLogSender.class})
	@Bean
	public FilterRegistrationBean<WebLogFilter> webLogFilterRegistrationBean(WebLogProperties properties,
																			 WebLogSender webLogSender,
																			 List<WebLogHandler> webLogHandlers,
																			 RequestMappingHandlerMapping requestMappingHandlerMapping) {
		WebLogFilter webLogFilter = new WebLogFilter(properties, webLogSender, excludePathPatterns, webLogHandlers, requestMappingHandlerMapping);
		FilterRegistrationBean<WebLogFilter> filterRegistrationBean = new FilterRegistrationBean<>(webLogFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
		return filterRegistrationBean;
	}
}
