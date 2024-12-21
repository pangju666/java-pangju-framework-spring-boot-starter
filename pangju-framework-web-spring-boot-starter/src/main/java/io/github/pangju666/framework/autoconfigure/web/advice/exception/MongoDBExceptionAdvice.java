package io.github.pangju666.framework.autoconfigure.web.advice.exception;

import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import io.github.pangju666.framework.core.lang.pool.Constants;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, MongoException.class})
@RestControllerAdvice
public class MongoDBExceptionAdvice {
	private static final Logger log = LoggerFactory.getLogger(MongoDBExceptionAdvice.class);

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = MongoExecutionTimeoutException.class)
	public Result<Void> handleMongoExecutionTimeoutException(MongoExecutionTimeoutException e) {
		log.error("MongoDB执行超时", e);
		return Result.fail(Constants.DATA_ERROR_RESPONSE_CODE, "MongoDB执行超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = MongoTimeoutException.class)
	public Result<Void> handleMongoTimeoutException(MongoTimeoutException e) {
		log.error("MongoDB连接超时", e);
		return Result.fail(Constants.DATA_ERROR_RESPONSE_CODE, "MongoDB连接超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = MongoSocketReadTimeoutException.class)
	public Result<Void> handleMongoSocketReadTimeoutException(MongoSocketReadTimeoutException e) {
		log.error("MongoDB读取超时", e);
		return Result.fail(Constants.DATA_ERROR_RESPONSE_CODE, "MongoDB读取超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = MongoException.class)
	public Result<Void> handleMongoException(MongoException e) {
		log.error("MongoDB抛出异常", e);
		return Result.fail(Constants.DATA_ERROR_RESPONSE_CODE, "MongoDB错误");
	}
}
