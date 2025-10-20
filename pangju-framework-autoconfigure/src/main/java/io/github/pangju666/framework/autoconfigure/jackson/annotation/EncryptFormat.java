package io.github.pangju666.framework.autoconfigure.jackson.annotation;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;
import io.github.pangju666.framework.autoconfigure.jackson.serializer.EncryptJsonSerializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = EncryptJsonSerializer.class)
public @interface EncryptFormat {
	String key() default "";

	Algorithm algorithm() default Algorithm.AES256;

	Encoding encoding() default Encoding.BASE64;
}
