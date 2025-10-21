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

package io.github.pangju666.framework.autoconfigure.web.idempotent;

import io.github.pangju666.framework.autoconfigure.web.idempotent.aspect.IdempotentAspect;
import io.github.pangju666.framework.autoconfigure.web.idempotent.config.ExpireMapRequestRepeaterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.idempotent.config.RedisRequestRepeaterConfiguration;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import org.aspectj.weaver.Advice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration(after = AopAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "spring.aop.auto", matchIfMissing = true)
@ConditionalOnClass(Advice.class)
@Import({ExpireMapRequestRepeaterConfiguration.class, RedisRequestRepeaterConfiguration.class})
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfiguration {
	@ConditionalOnBean(IdempotentValidator.class)
	@Bean
	public IdempotentAspect idempotentAspect(IdempotentValidator idempotentValidator) {
		return new IdempotentAspect(idempotentValidator);
	}
}
