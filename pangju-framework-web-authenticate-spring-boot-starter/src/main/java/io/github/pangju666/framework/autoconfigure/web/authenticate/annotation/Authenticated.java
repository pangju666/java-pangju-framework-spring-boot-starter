package io.github.pangju666.framework.autoconfigure.web.authenticate.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Authenticated {
	String[] roles() default {};

	boolean anyMatchRole() default true;

	boolean anonymous() default false;
}
