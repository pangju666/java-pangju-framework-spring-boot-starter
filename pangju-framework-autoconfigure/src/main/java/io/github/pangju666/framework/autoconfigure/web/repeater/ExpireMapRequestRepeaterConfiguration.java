package io.github.pangju666.framework.autoconfigure.web.repeater;

import io.github.pangju666.framework.autoconfigure.web.repeater.impl.ExpireMapRequestRepeater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

//@Configuration(proxyBeanMethods = false)
//@ConditionalOnProperty(prefix = "pangju.web.request.repeat", value = "type", havingValue = "EXPIRE_MAP", matchIfMissing = true)
public class ExpireMapRequestRepeaterConfiguration {
	@ConditionalOnMissingBean(RequestRepeater.class)
	@Bean
	public ExpireMapRequestRepeater expireMapRequestRepeater() {
		return new ExpireMapRequestRepeater();
	}
}
