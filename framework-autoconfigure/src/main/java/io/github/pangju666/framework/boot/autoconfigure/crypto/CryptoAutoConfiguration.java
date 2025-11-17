/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.autoconfigure.crypto;

import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.framework.boot.crypto.factory.impl.AES256CryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.BasicCryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.RSACryptoFactory;
import io.github.pangju666.framework.boot.crypto.factory.impl.StrongCryptoFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 加密工厂自动配置。
 * <p>
 * 在 Spring Boot 应用启动时，按需向容器注册加密工厂 Bean，包括
 * {@link AES256CryptoFactory}、{@link RSACryptoFactory}、
 * {@link StrongCryptoFactory}、{@link BasicCryptoFactory}。
 * 当容器中不存在同类型的 Bean 时，通过各自的 {@code @Bean} 方法提供默认实现。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RSAKey.class)
public class CryptoAutoConfiguration {
	/**
	 * 注册 AES‑256 算法加密工厂。
	 * <p>当容器中不存在同类型 Bean 时生效。</p>
	 *
	 * @return AES‑256 加密工厂实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(AES256CryptoFactory.class)
	@Bean
	public AES256CryptoFactory aes256CryptoFactory() {
		return new AES256CryptoFactory();
	}

	/**
	 * 注册 RSA 算法加密工厂。
	 * <p>当容器中不存在同类型 Bean 时生效。</p>
	 *
	 * @return RSA 加密工厂实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(RSACryptoFactory.class)
	@Bean
	public RSACryptoFactory rsaCryptoFactory() {
		return new RSACryptoFactory();
	}

	/**
	 * 注册高强度加密工厂。
	 * <p>当容器中不存在同类型 Bean 时生效。</p>
	 *
	 * @return Strong 加密工厂实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(StrongCryptoFactory.class)
	@Bean
	public StrongCryptoFactory strongCryptoFactory() {
		return new StrongCryptoFactory();
	}

	/**
	 * 注册基础强度加密工厂。
	 * <p>当容器中不存在同类型 Bean 时生效。</p>
	 *
	 * @return Basic 加密工厂实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(BasicCryptoFactory.class)
	@Bean
	public BasicCryptoFactory basicCryptoFactory() {
		return new BasicCryptoFactory();
	}
}
