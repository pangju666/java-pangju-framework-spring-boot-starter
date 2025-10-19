package io.github.pangju666.framework.autoconfigure.web.advice.exception;

import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.common.Result;
import io.github.pangju666.framework.web.utils.ServletResponseUtils;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalExceptionAdvice {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

	@ExceptionHandler(value = BaseHttpException.class)
	public void handleBaseRuntimeException(BaseHttpException e, HttpServletResponse response) {
		ServletResponseUtils.writeHttpExceptionToResponse(e, response);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		return Result.fail("预期的请求方法类型：" + StringUtils.join(e.getSupportedMethods(), "、"));
	}

	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
	public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
		return Result.fail("预期的请求数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
	public Result<Void> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
		return Result.fail("预期的响应数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = MissingPathVariableException.class)
	public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
		LOGGER.error("缺少路径变量", e);
		return Result.fail("缺少路径变量: " + e.getVariableName());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestHeaderException.class)
	public Result<Void> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
		return Result.fail("缺少请求头：" + e.getHeaderName());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		return Result.fail("缺少请求参数：" + e.getParameterName());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestValueException.class)
	public Result<Void> handleMissingRequestValueException(MissingRequestValueException e) {
		return Result.fail(e.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestPartException.class)
	public Result<Void> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
		return Result.fail("缺少请求文件：" + e.getRequestPartName());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ServletRequestBindingException.class)
	public Result<Void> handleServletRequestBindingException(ServletRequestBindingException e) {
		LOGGER.error("请求参数绑定异常", e);
		return Result.fail("请求参数不正确");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ConversionNotSupportedException.class)
	public Result<Void> handleConversionNotSupportedException(ConversionNotSupportedException e) {
		LOGGER.error("请求参数转换异常", e);
		return Result.fail("请求参数转换失败");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		return Result.fail("参数：" + StringUtils.defaultString(e.getName()) + "类型不正确");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		LOGGER.error("请求内容读取失败", e);
		return Result.fail("请求内容读取失败");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		LOGGER.error("上传文件大小超过限制", e);
		if (e.getMaxUploadSize() == -1) {
			if (Objects.nonNull(e.getCause()) &&
				e.getCause() instanceof IllegalStateException illegalStateException &&
				Objects.nonNull(illegalStateException.getCause()) &&
				illegalStateException.getCause() instanceof SizeLimitExceededException sizeLimitExceededException) {
				return Result.fail("上传文件大小超过" + DataSize.ofBytes(sizeLimitExceededException.getPermittedSize()).toMegabytes() + "MB");
			}
			return Result.fail("上传文件大小超过限制");
		}
		return Result.fail("上传文件大小超过" + DataSize.ofBytes(e.getMaxUploadSize()).toMegabytes() + "MB");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MultipartException.class)
	public Result<Void> handleMultipartException(MultipartException e) {
		LOGGER.error("上传文件失败", e);
		return Result.fail("上传文件失败");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = HttpMessageNotWritableException.class)
	public Result<Void> handleHttpMessageNotWritableException(HttpMessageNotWritableException e) {
		LOGGER.error("响应内容写入失败", e);
		return Result.fail("响应内容写入失败");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		BindingResult bindingResult = e.getBindingResult();
		List<ObjectError> objectErrors = bindingResult.getAllErrors();
		if (!objectErrors.isEmpty()) {
			FieldError fieldError = (FieldError) objectErrors.iterator().next();
			return Result.fail(StringUtils.defaultString(fieldError.getDefaultMessage()));
		}
		return Result.fail("请求参数验证不合法");
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ConstraintViolationException.class)
	public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
		Set<ConstraintViolation<?>> constraints = e.getConstraintViolations();
		if (!constraints.isEmpty()) {
			ConstraintViolation<?> constraint = constraints.iterator().next();
			return Result.fail(StringUtils.defaultString(constraint.getMessage()));
		}
		return Result.fail("请求参数验证不合法");
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoHandlerFoundException.class)
	public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
		return Result.fail("请求路径：" + e.getRequestURL() + "不存在");
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoResourceFoundException.class)
	public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
		return Result.fail("请求资源：" + e.getResourcePath() + "不存在");
	}

	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ExceptionHandler(value = AsyncRequestTimeoutException.class)
	public Result<Void> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
		LOGGER.error("异步请求超时", e);
		return Result.fail("异步请求超时");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = DataAccessException.class)
	public Result<Void> handleDataAccessException(DataAccessException e) {
		LOGGER.error("数据访问异常", e);
		return Result.fail("数据访问错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = IOException.class)
	public Result<Void> handleIOException(IOException e) {
		LOGGER.error("IO异常", e);
		return Result.fail("服务器内部错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = RuntimeException.class)
	public Result<Void> handleRuntimeException(RuntimeException e) {
		LOGGER.error("运行时异常", e);
		return Result.fail("服务器内部错误");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public Result<Void> handleException(Exception e) {
		LOGGER.error("系统级异常", e);
		return Result.fail("服务器内部错误");
	}
}
