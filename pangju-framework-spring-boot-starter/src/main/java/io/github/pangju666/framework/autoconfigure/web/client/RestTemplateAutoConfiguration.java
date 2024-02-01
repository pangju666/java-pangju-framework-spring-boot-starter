package io.github.pangju666.framework.autoconfigure.web.client;

import io.github.pangju666.framework.autoconfigure.web.client.properties.RestTemplateProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration.class,
	after = HttpMessageConvertersAutoConfiguration.class)
@EnableConfigurationProperties(RestTemplateProperties.class)
@ConditionalOnClass(RestTemplate.class)
public class RestTemplateAutoConfiguration {
	@Bean
	@Lazy
	@ConditionalOnMissingBean
	public RestTemplateBuilder restTemplateBuilder(RestTemplateProperties properties,
												   ObjectProvider<SslBundle> sslBundleObjectProvider,
												   ObjectProvider<HttpMessageConverters> messageConverters,
												   ObjectProvider<RestTemplateCustomizer> restTemplateCustomizers,
												   ObjectProvider<RestTemplateRequestCustomizer<?>> restTemplateRequestCustomizers) {
		List<HttpMessageConverter<?>> httpMessageConverterList = Optional.ofNullable(messageConverters.getIfUnique())
			.map(HttpMessageConverters::getConverters)
			.orElse(Collections.emptyList());
		RestTemplateBuilder builder = new RestTemplateBuilder()
			.additionalMessageConverters(httpMessageConverterList)
			.additionalCustomizers(restTemplateCustomizers.orderedStream().toList())
			.additionalRequestCustomizers(restTemplateRequestCustomizers.orderedStream().toList())
			.setReadTimeout(properties.getReadTimeout())
			.setConnectTimeout(properties.getConnectTimeout())
			.setSslBundle(sslBundleObjectProvider.getIfAvailable());
		if (StringUtils.hasText(properties.getRootUri())) {
			builder = builder.rootUri(properties.getRootUri());
		}
		return builder;
	}

	@Bean
	@Lazy
	@ConditionalOnMissingBean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
}
