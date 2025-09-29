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

@AutoConfiguration(before = org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration.class)
@ConditionalOnClass({ExecutableValidator.class, HibernateValidatorConfiguration.class})
@ConditionalOnResource(resources = "classpath:META-INF/services/jakarta.validation.spi.ValidationProvider")
public class ValidationAutoConfiguration {
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