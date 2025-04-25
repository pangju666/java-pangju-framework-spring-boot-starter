package io.github.pangju666.framework.autoconfigure.web;

import io.github.pangju666.framework.web.filter.ContentCachingWrapperFilter;
import io.github.pangju666.framework.web.filter.CorsFilter;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Collections;
import java.util.Set;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
public class FilterAutoConfiguration {
	private Set<String> excludePathPatterns = Collections.emptySet();

	/*@Autowired(required = false)
	public void setExcludePathPatterns(Map<String, ExcludePathPatternsProvider> excludePathPatternProviderMap) {
		this.excludePathPatterns = excludePathPatternProviderMap.values()
			.stream()
			.map(ExcludePathPatternsProvider::getExcludePaths)
			.flatMap(Set::stream)
			.collect(Collectors.toSet());
	}
*/
	@ConditionalOnClass(CorsFilter.class)
	@ConditionalOnMissingFilterBean
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(source, excludePathPatterns));
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return filterRegistrationBean;
	}

	@ConditionalOnClass(ContentCachingWrapperFilter.class)
	@Bean
	public FilterRegistrationBean<ContentCachingWrapperFilter> contentCachingWrapperFilterRegistrationBean() {
		ContentCachingWrapperFilter contentCachingWrapperFilter = new ContentCachingWrapperFilter(excludePathPatterns);
		FilterRegistrationBean<ContentCachingWrapperFilter> filterRegistrationBean = new FilterRegistrationBean<>(contentCachingWrapperFilter);
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
		return filterRegistrationBean;
	}
}
