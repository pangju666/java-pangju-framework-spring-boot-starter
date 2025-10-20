package io.github.pangju666.framework.autoconfigure.web.repeater;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Repeat {
	String key();

	boolean global() default false;

	int interval() default 1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	String message() default "请勿重复请求";
}
