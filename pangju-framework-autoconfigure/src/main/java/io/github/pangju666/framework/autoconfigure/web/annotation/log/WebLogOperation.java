package io.github.pangju666.framework.autoconfigure.web.annotation.log;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogOperation {
	String value();
}
