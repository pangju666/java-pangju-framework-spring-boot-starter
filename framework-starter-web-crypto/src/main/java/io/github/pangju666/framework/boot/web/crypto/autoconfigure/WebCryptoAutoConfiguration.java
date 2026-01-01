package io.github.pangju666.framework.boot.web.crypto.autoconfigure;


import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.web.autoconfigure.WebMvcConfigurerAutoConfiguration;
import io.github.pangju666.framework.boot.web.crypto.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.List;

@AutoConfiguration(before = WebMvcConfigurerAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class, RSAKeyPair.class})
@ConditionalOnBean(CryptoFactory.class)
public class WebCryptoAutoConfiguration {
	@Bean
	public EncryptRequestParamArgumentResolver encryptRequestParamArgumentResolver(List<CryptoFactory> cryptoFactories) {
		return new EncryptRequestParamArgumentResolver(cryptoFactories);
	}
}
