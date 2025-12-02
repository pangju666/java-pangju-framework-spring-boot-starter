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

package io.github.pangju666.framework.boot.autoconfigure.validation;

import jakarta.validation.executable.ExecutableValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Validation 自动配置。
 * <p>
 * 在 Spring Boot 默认验证自动配置之前执行，按条件启用 Hibernate Validator 的快速失败模式（fail-fast）。
 * 条件：当配置项 {@code pangju.validation.fail-fast=true} 或缺省时启用；
 * 仅在存在 {@code ExecutableValidator}、{@code HibernateValidatorConfiguration}，且发现验证提供者资源
 * {@code META-INF/services/jakarta.validation.spi.ValidationProvider} 时生效。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@AutoConfiguration(before = org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class)
@ConditionalOnBooleanProperty(prefix = "pangju.validation", name = "fail-fast", matchIfMissing = true)
@ConditionalOnClass({ExecutableValidator.class, HibernateValidatorConfiguration.class})
@ConditionalOnResource(resources = "classpath:META-INF/services/jakarta.validation.spi.ValidationProvider")
public class ValidationAutoConfiguration {
    /**
     * 注册验证配置自定义器，启用 Hibernate Validator 的 fail-fast。
     * <p>
     * 当存在 {@code HibernateValidatorConfiguration} 时设置 {@code failFast(true)}；
     * 使验证在遇到首个错误即返回，提高复杂对象的校验效率。
     * </p>
     *
     * @return 验证配置自定义器实例
     * @since 1.0.0
     */
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public ValidationConfigurationCustomizer hibernateValidationConfigurationCustomizer() {
		return configuration -> {
			if (configuration instanceof HibernateValidatorConfiguration hibernateValidatorConfiguration) {
				hibernateValidatorConfiguration.failFast(true);
			}
		};
	}
}
