package io.github.pangju666.framework.autoconfigure.web.security.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Role {
	String[] value() default {};
}
