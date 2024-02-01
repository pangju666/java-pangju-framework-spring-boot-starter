package io.github.pangju666.framework.autoconfigure.web.annotation.validation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestRepeat {
	String message() default "请勿重复请求";

	int duration();

	TimeUnit timeUnit() default TimeUnit.SECONDS;
}
