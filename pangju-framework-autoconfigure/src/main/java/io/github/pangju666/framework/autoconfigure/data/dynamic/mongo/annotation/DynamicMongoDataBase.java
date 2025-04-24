package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicMongoDataBase {
	String value();
}
