package io.github.pangju666.framework.autoconfigure.data.mybatisplus.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface TableLogicFill {
	String value();
}
