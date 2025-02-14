package io.github.pangju666.framework.autoconfigure.cache.redis.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCacheable {
	String cache();

	String key() default "";

	String keyField() default "";

	String sortField() default "";

	String reverseOrder() default "";

	boolean allEntries() default false;

	String condition() default "";

	String unless() default "";
}
