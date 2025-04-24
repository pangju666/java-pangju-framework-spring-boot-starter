package io.github.pangju666.framework.autoconfigure.web.configuration;

import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import io.github.pangju666.framework.autoconfigure.web.repeater.impl.ExpireMapRequestRepeater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.request.repeat", value = "type", havingValue = "EXPIRE_MAP", matchIfMissing = true)
public class ExpireMapRequestRepeaterConfiguration {
	@ConditionalOnMissingBean(RequestRepeater.class)
	@Bean
	public ExpireMapRequestRepeater expireMapRequestRepeater() {
		return new ExpireMapRequestRepeater();
	}
}
