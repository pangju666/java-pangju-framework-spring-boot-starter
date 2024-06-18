package io.github.pangju666.framework.autoconfigure.cache.redis.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCachePut {
    String cache();

    String key() default "";

    String value();

    String condition() default "";
}
