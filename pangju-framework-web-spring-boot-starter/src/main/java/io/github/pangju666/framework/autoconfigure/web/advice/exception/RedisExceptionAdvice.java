package io.github.pangju666.framework.autoconfigure.web.advice.exception;

import io.github.pangju666.framework.web.lang.pool.WebConstants;
import io.github.pangju666.framework.web.model.vo.Result;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, DataAccessException.class, RedisConnectionFailureException.class})
@RestControllerAdvice
public class RedisExceptionAdvice {
	private static final Logger log = LoggerFactory.getLogger(RedisExceptionAdvice.class);

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = RedisConnectionFailureException.class)
	public Result<Void> handleRedisConnectionFailureException(RedisConnectionFailureException e) {
		log.error("Redis连接超时异常", e);
		return Result.fail(WebConstants.DATA_ERROR_CODE, "Redis连接超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<Void> handleDataAccessException(DataAccessException e) {
		log.error("数据访问异常", e);
		return Result.fail(WebConstants.DATA_ERROR_CODE, "服务器内部错误");
	}
}
