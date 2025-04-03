package io.github.pangju666.framework.autoconfigure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pangju666.framework.autoconfigure.http.jackson.deserializer.*;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.DateJsonSerializer;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.LocalDateJsonSerializer;
import io.github.pangju666.framework.autoconfigure.http.jackson.serializer.LocalDateTimeJsonSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class)
@ConditionalOnClass(RestClient.class)
public class RestClientAutoConfiguration {
	@ConditionalOnBean(RestClient.Builder.class)
	@ConditionalOnMissingBean(RestClient.class)
	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		return builder.build();
	}
}
