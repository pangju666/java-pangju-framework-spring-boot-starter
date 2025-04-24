package io.github.pangju666.framework.autoconfigure.cache.hash.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HashCaching {
	HashCachePut[] puts() default {};

	HashCacheEvict[] evicts() default {};
}
