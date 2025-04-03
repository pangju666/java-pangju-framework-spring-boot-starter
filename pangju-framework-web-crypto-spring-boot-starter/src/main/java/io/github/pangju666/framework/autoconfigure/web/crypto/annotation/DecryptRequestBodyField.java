package io.github.pangju666.framework.autoconfigure.web.crypto.annotation;

import io.github.pangju666.framework.autoconfigure.web.crypto.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.crypto.enums.Encoding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecryptRequestBodyField {
	String key();

	Algorithm algorithm() default Algorithm.AES256;

	Encoding encoding() default Encoding.BASE64;
}
