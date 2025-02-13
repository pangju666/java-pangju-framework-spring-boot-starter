package io.github.pangju666.framework.autoconfigure.web.security.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Anonymous {
}
