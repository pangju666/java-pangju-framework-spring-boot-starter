package io.github.pangju666.framework.autoconfigure.web.annotation.crypto;

import io.github.pangju666.framework.autoconfigure.web.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.Encoding;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptRequestParam {
	String key();

	Algorithm algorithm() default Algorithm.AES256;
	Encoding encoding() default Encoding.BASE64;
	String name() default "";
	boolean required() default true;
	String defaultValue() default "";
}
