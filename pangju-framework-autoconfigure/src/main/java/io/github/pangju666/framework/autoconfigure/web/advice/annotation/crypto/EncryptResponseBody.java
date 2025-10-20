package io.github.pangju666.framework.autoconfigure.web.advice.annotation.crypto;


import io.github.pangju666.framework.autoconfigure.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.enums.Encoding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EncryptResponseBody {
	String key() default "";

	Algorithm algorithm() default Algorithm.AES256;

	Encoding encoding() default Encoding.BASE64;
}
