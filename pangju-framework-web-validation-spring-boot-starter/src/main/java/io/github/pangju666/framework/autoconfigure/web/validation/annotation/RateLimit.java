package io.github.pangju666.framework.autoconfigure.web.validation.annotation;

import io.github.pangju666.framework.autoconfigure.web.validation.enums.RateLimitMethod;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RateLimit {
	String key() default "";

	int interval() default 1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	int rate();

	boolean global() default false;

	RateLimitMethod method() default RateLimitMethod.REQUEST;

	String message() default "请求次数已达上限，请稍候再试";
}
