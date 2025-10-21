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

package io.github.pangju666.framework.autoconfigure.validation;

import jakarta.validation.executable.ExecutableValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 验证自动配置类
 * <p>
 * 该配置类在Spring Boot的标准验证配置之前执行，用于自定义Hibernate Validator的配置。
 * 主要功能是启用快速失败模式，当发现第一个验证错误时立即返回，不再继续验证其他属性。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class)
@ConditionalOnClass({ExecutableValidator.class, HibernateValidatorConfiguration.class})
@ConditionalOnResource(resources = "classpath:META-INF/services/jakarta.validation.spi.ValidationProvider")
public class ValidationAutoConfiguration {
	/**
	 * 创建Hibernate验证配置自定义器
	 * <p>
	 * 该Bean用于自定义Hibernate Validator的配置，启用快速失败模式。
	 * 当验证过程中发现第一个错误时，验证过程将立即停止并返回错误，不再继续验证其他属性。
	 * 这有助于提高验证性能，特别是在处理大型对象时。
	 * </p>
	 * <p>
	 * 仅在未定义同类型bean的情况下生效。
	 * </p>
	 *
	 * @return ValidationConfigurationCustomizer 验证配置自定义器实例
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(ValidationConfigurationCustomizer.class)
	@Bean
	public ValidationConfigurationCustomizer hibernateValidationConfigurationCustomizer() {
		return configuration -> {
			if (configuration instanceof HibernateValidatorConfiguration hibernateValidatorConfiguration) {
				hibernateValidatorConfiguration.failFast(true);
			}
		};
	}
}