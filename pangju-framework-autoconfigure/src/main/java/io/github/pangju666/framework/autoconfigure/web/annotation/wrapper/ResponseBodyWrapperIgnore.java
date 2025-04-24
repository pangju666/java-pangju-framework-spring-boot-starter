package io.github.pangju666.framework.autoconfigure.web.annotation.wrapper;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ResponseBodyWrapperIgnore {
}