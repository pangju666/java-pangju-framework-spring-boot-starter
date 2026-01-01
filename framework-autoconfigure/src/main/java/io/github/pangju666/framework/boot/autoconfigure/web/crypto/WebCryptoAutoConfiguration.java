package io.github.pangju666.framework.boot.autoconfigure.web.crypto;

import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.framework.boot.autoconfigure.web.WebMvcConfigurerAutoConfiguration;
import io.github.pangju666.framework.boot.crypto.factory.CryptoFactory;
import io.github.pangju666.framework.boot.web.resolver.EncryptRequestParamArgumentResolver;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.List;

/**
 * Web 加密自动配置。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>自动配置 Web 层的加密组件，如请求参数解密解析器 {@link EncryptRequestParamArgumentResolver}。</li>
 *   <li>依赖 {@link CryptoFactory} 提供具体的加解密实现。</li>
 * </ul>
 *
 * <p><strong>条件说明</strong></p>
 * <ul>
 *   <li>Web 类型：仅在 Servlet 环境下生效。</li>
 *   <li>类条件：需存在 {@link Servlet}、{@link DispatcherServlet}、{@link Result} 及 {@link RSAKeyPair}。</li>
 *   <li>Bean 条件：必须存在至少一个 {@link CryptoFactory} Bean 才能启用。</li>
 * </ul>
 *
 * <p><strong>配置顺序</strong></p>
 * <ul>
 *   <li>在 {@link WebMvcConfigurerAutoConfiguration} 之前执行，确保参数解析器能被后续 MVC 配置引用或注册。</li>
 * </ul>
 *
 * @author pangju666
 * @see EncryptRequestParamArgumentResolver
 * @see CryptoFactory
 * @since 1.0.0
 */
@AutoConfiguration(before = WebMvcConfigurerAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class, RSAKeyPair.class})
@ConditionalOnBean(CryptoFactory.class)
public class WebCryptoAutoConfiguration {
	/**
	 * 注册加密请求参数解析器。
	 *
	 * <p>该解析器用于处理带有 {@code @EncryptRequestParam} 注解的控制器参数，
	 * 利用注入的 {@link CryptoFactory} 列表进行自动解密。</p>
	 *
	 * @param cryptoFactories 加密工厂列表，用于支持多种算法或密钥策略
	 * @return {@link EncryptRequestParamArgumentResolver} 实例
	 * @since 1.0.0
	 */
	@Bean
	public EncryptRequestParamArgumentResolver encryptRequestParamArgumentResolver(List<CryptoFactory> cryptoFactories) {
		return new EncryptRequestParamArgumentResolver(cryptoFactories);
	}
}
