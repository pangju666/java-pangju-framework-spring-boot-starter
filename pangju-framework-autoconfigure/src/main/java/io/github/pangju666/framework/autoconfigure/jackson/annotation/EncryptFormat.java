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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;
import io.github.pangju666.framework.autoconfigure.jackson.serializer.EncryptJsonSerializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JSON字段加密注解，用于在JSON序列化过程中对指定字段进行加密操作
 * <p>
 * 该注解可以应用于类的字段上，指定字段在JSON序列化时将使用{@link EncryptJsonSerializer}
 * 进行加密处理。加密过程将根据配置的密钥、算法和编码方式进行。
 * </p>
 *
 * @author pangju666
 * @see EncryptJsonSerializer
 * @see Algorithm
 * @see Encoding
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptJsonSerializer.class)
public @interface EncryptFormat {
	/**
	 * 用于加密的密钥，只有算法需要密钥时才生效
	 *
	 * @return 加密密钥
	 * @since 1.0.0
	 */
	String key() default "";

	/**
	 * 用于加密的算法
	 * <p>
	 * 默认使用AES256算法
	 * </p>
	 *
	 * @return 加密算法
	 * @since 1.0.0
	 */
	Algorithm algorithm() default Algorithm.AES256;

	/**
	 * 加密内容的编码方式
	 * <p>
	 * 默认使用BASE64编码
	 * </p>
	 *
	 * @return 编码方式
	 * @since 1.0.0
	 */
	Encoding encoding() default Encoding.BASE64;
}
