package io.github.pangju666.framework.autoconfigure.web.annotation.crypto;


import io.github.pangju666.framework.autoconfigure.web.enums.Algorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.Encoding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EncryptResponseBodyField {
	String key();

	Algorithm algorithm() default Algorithm.AES256;

	Encoding encoding() default Encoding.BASE64;
}
