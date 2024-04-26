package io.github.pangju666.framework.autoconfigure.web.log.handler;

import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

public interface WebLogHandler {
	void handle(WebLog webLog,
				HttpServletRequest request, HttpServletResponse response,
				@Nullable Class<?> targetClass, @Nullable Method targetMethod);
}
