package io.github.pangju666.framework.autoconfigure.springdoc.utils;

import io.github.pangju666.framework.autoconfigure.springdoc.utils.utils.SpringDocUtils;
import io.github.pangju666.framework.web.provider.ExcludePathPatternProvider;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.utils.Constants.SPRINGDOC_SWAGGER_UI_ENABLED;

@AutoConfiguration
@ConditionalOnProperty(name = {SPRINGDOC_SWAGGER_UI_ENABLED, SPRINGDOC_ENABLED}, havingValue = "true")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SpringDocAutoConfiguration {
	@ConditionalOnMissingBean
	@Bean
	public ExcludePathPatternProvider springdocPathPatternProvider(SwaggerUiConfigProperties swaggerUiConfigProperties,
																   SpringDocConfigProperties springDocConfigProperties) {
		List<String> knife4jPathExcludePath = SpringDocUtils.getExcludePathPatternList(swaggerUiConfigProperties, springDocConfigProperties);
		return new ExcludePathPatternProvider(knife4jPathExcludePath);
	}
}
