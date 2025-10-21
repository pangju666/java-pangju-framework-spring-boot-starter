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

package io.github.pangju666.framework.autoconfigure.web.idempotent.config;

import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl.ExpireMapIdempotentValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "pangju.web.idempotent", value = "type", havingValue = "EXPIRE_MAP", matchIfMissing = true)
public class ExpireMapRequestRepeaterConfiguration {
	@ConditionalOnMissingBean(IdempotentValidator.class)
	@Bean
	public ExpireMapIdempotentValidator expireMapIdempotentValidator() {
		return new ExpireMapIdempotentValidator();
	}
}
