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

package io.github.pangju666.framework.boot.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.pangju666.framework.boot.enums.Algorithm;
import io.github.pangju666.framework.boot.enums.Encoding;
import io.github.pangju666.framework.boot.jackson.deserializer.DecryptJsonDeserializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JSON字段解密注解，用于在JSON反序列化过程中对指定字段进行解密操作
 * <p>
 * 该注解可以应用于类的字段上，指定字段在JSON反序列化时将使用{@link DecryptJsonDeserializer}
 * 进行解密处理。解密过程将根据配置的密钥、算法和编码方式进行。
 * </p>
 *
 * @author pangju666
 * @see DecryptJsonDeserializer
 * @see Algorithm
 * @see Encoding
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = DecryptJsonDeserializer.class)
public @interface DecryptFormat {
	/**
	 * 用于解密的密钥，只有算法需要密钥时才生效
	 *
	 * @return 解密密钥
	 * @since 1.0.0
	 */
	String key() default "";

	/**
	 * 用于解密的算法
	 * <p>
	 * 默认使用AES256算法
	 * </p>
	 *
	 * @return 解密算法
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
