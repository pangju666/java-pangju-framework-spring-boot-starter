package io.github.pangju666.framework.autoconfigure.cache.hash.annoation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HashCacheEvict {
	String[] caches() default {};

	String key() default "";

	String keyField() default "";

	String condition() default "";

	boolean allEntries() default false;
}
