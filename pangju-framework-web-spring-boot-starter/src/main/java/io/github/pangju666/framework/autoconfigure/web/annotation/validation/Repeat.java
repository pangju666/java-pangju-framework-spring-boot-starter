package io.github.pangju666.framework.autoconfigure.web.annotation.validation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Repeat {
	String[] headers() default {};

	boolean allHeaders() default false;

	String[] params() default {};

	boolean allParams() default false;

	String[] bodyJsonPaths() default {};

	boolean allBody() default false;

	int interval() default 1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	String message() default "请勿重复请求";
}
