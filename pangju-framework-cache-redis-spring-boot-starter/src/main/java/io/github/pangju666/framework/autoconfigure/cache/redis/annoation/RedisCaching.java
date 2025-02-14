package io.github.pangju666.framework.autoconfigure.cache.redis.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCaching {
	RedisCachePut[] puts() default {};

	RedisCacheEvict[] evicts() default {};
}
