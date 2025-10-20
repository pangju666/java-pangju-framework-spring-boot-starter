package io.github.pangju666.framework.autoconfigure.web.advice.annotation.wrapper;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResponseBodyWrapper {
}