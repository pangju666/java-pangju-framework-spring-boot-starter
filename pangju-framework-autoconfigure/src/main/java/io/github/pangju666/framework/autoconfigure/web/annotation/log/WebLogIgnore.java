package io.github.pangju666.framework.autoconfigure.web.annotation.log;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogIgnore {
}
