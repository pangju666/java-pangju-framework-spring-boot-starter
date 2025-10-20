package io.github.pangju666.framework.autoconfigure.web.signature;

import io.github.pangju666.framework.autoconfigure.web.WebMvcAutoConfiguration;
import io.github.pangju666.framework.autoconfigure.web.signature.store.SignatureSecretKeyStore;
import io.github.pangju666.framework.autoconfigure.web.signature.store.impl.DefaultSignatureSecretKeyStore;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@EnableConfigurationProperties(RequestSignatureProperties.class)
public class RequestSignatureAutoConfiguration {
	@ConditionalOnMissingBean
	@Bean
	public SignatureSecretKeyStore signatureSecretKeyStore(RequestSignatureProperties properties) {
		return new DefaultSignatureSecretKeyStore(properties);
	}
}
