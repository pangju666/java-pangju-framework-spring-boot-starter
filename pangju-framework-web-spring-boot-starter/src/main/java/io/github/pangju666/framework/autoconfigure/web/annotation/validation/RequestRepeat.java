package io.github.pangju666.framework.autoconfigure.web.annotation.validation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestRepeat {
	String message() default "请勿重复提交请求";

	String[] headers() default {};

	String[] params() default {};

	String[] bodyJsonPaths() default {};

	int duration() default 5;
	TimeUnit timeUnit() default TimeUnit.SECONDS;
}
