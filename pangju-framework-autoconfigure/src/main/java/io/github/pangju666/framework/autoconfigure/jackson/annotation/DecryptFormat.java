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

package io.github.pangju666.framework.autoconfigure.jackson.annotation;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;
import io.github.pangju666.framework.autoconfigure.jackson.deserializer.DecryptJsonDeserializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptJsonDeserializer.class)
public @interface DecryptFormat {
	String key() default "";

	Algorithm algorithm() default Algorithm.AES256;

	Encoding encoding() default Encoding.BASE64;
}
