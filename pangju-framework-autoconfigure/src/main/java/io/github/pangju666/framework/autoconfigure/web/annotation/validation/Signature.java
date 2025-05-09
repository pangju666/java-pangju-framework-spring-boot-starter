package io.github.pangju666.framework.autoconfigure.web.annotation.validation;


import io.github.pangju666.framework.autoconfigure.web.enums.SignatureAlgorithm;
import io.github.pangju666.framework.autoconfigure.web.enums.SignatureType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Signature {
	String[] appId() default {};

	SignatureType type() default SignatureType.ANY;

	SignatureAlgorithm algorithm() default SignatureAlgorithm.SHA1;

	long timeout() default 1;

	TimeUnit timeUnit() default TimeUnit.MINUTES;
}