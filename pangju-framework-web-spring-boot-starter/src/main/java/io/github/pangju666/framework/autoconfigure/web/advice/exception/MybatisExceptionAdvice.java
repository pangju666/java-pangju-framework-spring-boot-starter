package io.github.pangju666.framework.autoconfigure.web.advice.exception;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import io.github.pangju666.framework.web.model.Result;
import jakarta.servlet.Servlet;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Objects;
import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, MybatisPlusException.class,
	PersistenceException.class, DataAccessException.class, CannotGetJdbcConnectionException.class})
@RestControllerAdvice
public class MybatisExceptionAdvice {
	private static final Logger log = LoggerFactory.getLogger(MybatisExceptionAdvice.class);

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = CannotGetJdbcConnectionException.class)
	public Result<Void> handleCannotGetJdbcConnectionException(CannotGetJdbcConnectionException e) {
		log.error("JDBC连接超时", e);
		return Result.fail(ConstantPool.DATA_ERROR_RESPONSE_CODE, "数据库连接超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = MybatisPlusException.class)
	public Result<Void> handleMybatisPlusException(MybatisPlusException e) {
		log.error("mybatis-plus抛出异常", e);
		return Result.fail(ConstantPool.DATA_ERROR_RESPONSE_CODE, "服务器内部错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = PersistenceException.class)
	public Result<Void> handlePersistenceException(PersistenceException e) {
		log.error("mybatis抛出异常", e);
		return Result.fail(ConstantPool.DATA_ERROR_RESPONSE_CODE, "服务器内部错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<Void> handleDataAccessException(DataAccessException e) {
		log.error("数据访问异常", e);
		if (Objects.nonNull(e.getCause())) {
			Class<?> clz = Optional.ofNullable(e.getCause())
				.map(Throwable::getCause)
				.map(Throwable::getClass)
				.orElse(null);
			if (Objects.nonNull(clz) && CannotGetJdbcConnectionException.class.isAssignableFrom(clz)) {
				return Result.fail(ConstantPool.DATA_ERROR_RESPONSE_CODE, "数据库连接超时");
			}
		}
		return Result.fail(ConstantPool.DATA_ERROR_RESPONSE_CODE, "服务器内部错误");
	}
}
