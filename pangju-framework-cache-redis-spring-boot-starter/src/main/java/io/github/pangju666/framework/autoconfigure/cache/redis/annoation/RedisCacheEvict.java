package io.github.pangju666.framework.autoconfigure.cache.redis.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCacheEvict {
    String[] caches() default {};

    String key();

    String condition() default "";

    boolean allEntries() default false;
}
