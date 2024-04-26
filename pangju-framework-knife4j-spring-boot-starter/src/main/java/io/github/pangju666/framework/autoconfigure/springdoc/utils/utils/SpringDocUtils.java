package io.github.pangju666.framework.autoconfigure.springdoc.utils.utils;

import org.apache.commons.collections4.SetUtils;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.utils.Constants;

import java.util.*;

public final class SpringDocUtils {
	private SpringDocUtils() {
	}

	public static Set<String> getExcludePathPatternSet(SwaggerUiConfigProperties swaggerUiConfigProperties,
													   SpringDocConfigProperties springDocConfigProperties) {
		Set<String> excludePathPatterns = SetUtils.hashSet(
			"/doc.html",
			Constants.DEFAULT_WEB_JARS_PREFIX_URL + "/**",
			Constants.SWAGGER_UI_PREFIX + "/swagger-ui/**");
		Optional.ofNullable(springDocConfigProperties)
			.ifPresent(properties -> excludePathPatterns.add(properties.getApiDocs().getPath() + "/**"));
		Optional.ofNullable(swaggerUiConfigProperties)
			.map(SwaggerUiConfigProperties::getPath)
			.ifPresent(path -> excludePathPatterns.add(swaggerUiConfigProperties.getPath()));
		return excludePathPatterns;
	}

	public static List<String> getExcludePathPatternList(SwaggerUiConfigProperties swaggerUiConfigProperties,
														 SpringDocConfigProperties springDocConfigProperties) {
		List<String> excludePathPatterns = new ArrayList<>(Arrays.asList(
			"/doc.html",
			Constants.DEFAULT_WEB_JARS_PREFIX_URL + "/**",
			Constants.SWAGGER_UI_PREFIX + "/swagger-ui/**"));
		Optional.ofNullable(springDocConfigProperties)
			.ifPresent(properties -> excludePathPatterns.add(properties.getApiDocs().getPath() + "/**"));
		Optional.ofNullable(swaggerUiConfigProperties)
			.map(SwaggerUiConfigProperties::getPath)
			.ifPresent(path -> excludePathPatterns.add(swaggerUiConfigProperties.getPath()));
		return excludePathPatterns;
	}
}
