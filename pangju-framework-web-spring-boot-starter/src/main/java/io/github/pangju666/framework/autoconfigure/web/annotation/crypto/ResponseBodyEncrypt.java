package io.github.pangju666.framework.autoconfigure.web.annotation.crypto;

import io.github.pangju666.framework.autoconfigure.web.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.Encoding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ResponseBodyEncrypt {
	String key();

	Algorithm algorithm() default Algorithm.AES;

	Encoding encoding() default Encoding.BASE64;

	String transformation() default "";
}
