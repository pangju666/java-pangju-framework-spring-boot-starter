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
