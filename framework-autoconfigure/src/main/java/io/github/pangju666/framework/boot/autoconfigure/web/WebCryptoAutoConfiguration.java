package io.github.pangju666.framework.boot.autoconfigure.web;

import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.web.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class, RSAKeyPair.class, BaseHttpException.class})
@ConditionalOnBean(CryptoFactory.class)
public class WebCryptoAutoConfiguration {
	@Bean
	public EncryptRequestParamArgumentResolver encryptRequestParamArgumentResolver() {
		return new EncryptRequestParamArgumentResolver();
	}
}
