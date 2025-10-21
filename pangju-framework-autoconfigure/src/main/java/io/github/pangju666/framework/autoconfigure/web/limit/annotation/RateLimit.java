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

package io.github.pangju666.framework.autoconfigure.web.limit.annotation;

import io.github.pangju666.framework.autoconfigure.web.limit.enums.RateLimitScope;
import io.github.pangju666.framework.autoconfigure.web.limit.source.RateLimitSourceExtractor;
import io.github.pangju666.framework.autoconfigure.web.limit.source.impl.IpRateLimitSourceExtractor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RateLimit {
	String prefix() default "";

	String key() default "";

	int interval() default 1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	int rate();

	RateLimitScope scope() default RateLimitScope.GLOBAL;

	Class<? extends RateLimitSourceExtractor> source() default IpRateLimitSourceExtractor.class;

	String message() default "请求次数已达上限，请稍候再试";
}
