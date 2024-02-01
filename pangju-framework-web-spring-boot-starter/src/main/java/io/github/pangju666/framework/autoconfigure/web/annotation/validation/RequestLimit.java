package io.github.pangju666.framework.autoconfigure.web.annotation.validation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestLimit {
	int duration();

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	int count();

	boolean global() default false;

	String message() default "请求次数已达上限，请稍候再试";
}
