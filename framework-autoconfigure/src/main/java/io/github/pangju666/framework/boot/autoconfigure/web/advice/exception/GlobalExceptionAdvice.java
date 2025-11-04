/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.autoconfigure.web.advice.exception;

import io.github.pangju666.framework.web.exception.base.BaseHttpException;
import io.github.pangju666.framework.web.model.common.Result;
import io.github.pangju666.framework.web.servlet.builder.HttpResponseBuilder;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.List;

/**
 * 全局异常处理器
 * <p>
 * 该类用于统一处理Web应用中抛出的各种异常。
 * 通过{@link RestControllerAdvice}和{@link ExceptionHandler}注解，
 * 为应用中的异常提供统一的错误响应格式和HTTP状态码映射。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>处理HTTP请求相关的异常</li>
 *     <li>处理参数验证异常</li>
 *     <li>处理文件上传异常</li>
 *     <li>处理系统级异常</li>
 *     <li>返回统一格式的错误响应</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>应用必须是Servlet类型的Web应用</li>
 *     <li>Classpath中必须存在Servlet和DispatcherServlet类</li>
 *     <li>配置属性{@code pangju.web.advice.exception}必须为true（默认为true）</li>
 * </ul>
 * </p>
 * <p>
 * 支持的异常类型和HTTP状态码映射：
 * <ul>
 *     <li>
 *         <strong>BaseHttpException</strong> - 自定义HTTP异常
 *         <p>直接返回异常中指定的HTTP状态码和错误信息</p>
 *     </li>
 *     <li>
 *         <strong>HttpRequestMethodNotSupportedException</strong> - 405 Method Not Allowed
 *         <p>请求使用了不支持的HTTP方法</p>
 *     </li>
 *     <li>
 *         <strong>HttpMediaTypeNotSupportedException</strong> - 415 Unsupported Media Type
 *         <p>请求的Content-Type不被支持</p>
 *     </li>
 *     <li>
 *         <strong>HttpMediaTypeNotAcceptableException</strong> - 406 Not Acceptable
 *         <p>Accept头指定的响应类型不被支持</p>
 *     </li>
 *     <li>
 *         <strong>MissingPathVariableException</strong> - 404 Not Found
 *         <p>路径中缺少必需的路径变量</p>
 *     </li>
 *     <li>
 *         <strong>MissingRequestHeaderException</strong> - 400 Bad Request
 *         <p>请求中缺少必需的请求头</p>
 *     </li>
 *     <li>
 *         <strong>MissingServletRequestParameterException</strong> - 400 Bad Request
 *         <p>请求中缺少必需的请求参数</p>
 *     </li>
 *     <li>
 *         <strong>MissingServletRequestPartException</strong> - 400 Bad Request
 *         <p>文件上传请求中缺少必需的文件</p>
 *     </li>
 *     <li>
 *         <strong>ServletRequestBindingException</strong> - 400 Bad Request
 *         <p>请求参数绑定异常</p>
 *     </li>
 *     <li>
 *         <strong>ConversionNotSupportedException</strong> - 400 Bad Request
 *         <p>请求参数类型转换失败</p>
 *     </li>
 *     <li>
 *         <strong>MethodArgumentTypeMismatchException</strong> - 400 Bad Request
 *         <p>方法参数类型不匹配</p>
 *     </li>
 *     <li>
 *         <strong>HttpMessageNotReadableException</strong> - 400 Bad Request
 *         <p>请求体内容读取失败</p>
 *     </li>
 *     <li>
 *         <strong>MultipartException</strong> - 400 Bad Request
 *         <p>文件上传处理失败</p>
 *     </li>
 *     <li>
 *         <strong>MethodArgumentNotValidException</strong> - 400 Bad Request
 *         <p>请求参数验证不合法（Bean Validation）</p>
 *     </li>
 *     <li>
 *         <strong>NoHandlerFoundException</strong> - 404 Not Found
 *         <p>请求路径对应的处理器不存在</p>
 *     </li>
 *     <li>
 *         <strong>NoResourceFoundException</strong> - 404 Not Found
 *         <p>请求资源不存在</p>
 *     </li>
 *     <li>
 *         <strong>AsyncRequestTimeoutException</strong> - 503 Service Unavailable
 *         <p>异步请求超时</p>
 *     </li>
 *     <li>
 *         <strong>HttpMessageNotWritableException</strong> - 500 Internal Server Error
 *         <p>响应体内容写入失败</p>
 *     </li>
 *     <li>
 *         <strong>IOException</strong> - 500 Internal Server Error
 *         <p>IO操作异常</p>
 *     </li>
 *     <li>
 *         <strong>RuntimeException</strong> - 500 Internal Server Error
 *         <p>运行时异常</p>
 *     </li>
 *     <li>
 *         <strong>Exception</strong> - 500 Internal Server Error
 *         <p>所有其他异常的兜底处理</p>
 *     </li>
 * </ul>
 * </p>
 * <p>
 * 配置示例：
 * <pre>
 * pangju:
 *   web:
 *     advice:
 *       exception: true  # 默认为true，启用全局异常处理
 * </pre>
 * </p>
 * <p>
 * 响应格式：
 * <p>
 * 所有异常处理方法都返回统一的错误响应格式：
 * <pre>
 * {
 *   "code": "错误代码",
 *   "message": "错误描述信息",
 *   "data": null
 * }
 * </pre>
 * 其中BaseHttpException会返回自定义的HTTP状态码。
 * </p>
 * </p>
 * <p>
 * 日志记录：
 * <p>
 * 该类会对某些异常进行日志记录，便于问题排查和监控。
 * 记录级别为ERROR，可通过日志系统进行追踪。
 * </p>
 * </p>
 *
 * @author pangju666
 * @see RestControllerAdvice
 * @see ExceptionHandler
 * @since 1.0.0
 */
@Order(Ordered.LOWEST_PRECEDENCE - 2)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalExceptionAdvice {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

	/**
	 * 处理自定义HTTP异常
	 * <p>
	 * 该方法处理应用中抛出的自定义HTTP异常。
	 * 异常中包含具体的HTTP状态码和错误信息，直接写入响应中。
	 * </p>
	 *
	 * @param e 自定义HTTP异常
	 * @param response HTTP响应对象
	 * @since 1.0.0
	 */
	@ExceptionHandler(value = BaseHttpException.class)
	public void handleBaseHttpException(BaseHttpException e, HttpServletResponse response) {
		HttpResponseBuilder.from(response).buffer(false).writeHttpException(e);
	}

	/**
	 * 处理HTTP请求方法不支持异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码405
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		return Result.fail("预期的请求方法类型：" + StringUtils.join(e.getSupportedMethods(), "、"));
	}

	/**
	 * 处理请求的Content-Type不支持异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码415
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
	public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
		return Result.fail("预期的请求数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	/**
	 * 处理Accept响应类型不支持异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码406
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
	public Result<Void> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
		return Result.fail("预期的响应数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	/**
	 * 处理缺少路径变量异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码404
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = MissingPathVariableException.class)
	public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
		LOGGER.error("缺少路径变量", e);
		return Result.fail("缺少路径变量: " + e.getVariableName());
	}

	/**
	 * 处理缺少请求头异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestHeaderException.class)
	public Result<Void> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
		return Result.fail("缺少请求头：" + e.getHeaderName());
	}

	/**
	 * 处理缺少请求参数异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		return Result.fail("缺少请求参数：" + e.getParameterName());
	}

	/**
	 * 处理缺少请求值异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestValueException.class)
	public Result<Void> handleMissingRequestValueException(MissingRequestValueException e) {
		return Result.fail(e.getMessage());
	}

	/**
	 * 处理缺少请求文件异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestPartException.class)
	public Result<Void> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
		return Result.fail("缺少请求文件：" + e.getRequestPartName());
	}

	/**
	 * 处理请求参数绑定异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ServletRequestBindingException.class)
	public Result<Void> handleServletRequestBindingException(ServletRequestBindingException e) {
		LOGGER.error("请求参数绑定异常", e);
		return Result.fail("请求参数不正确");
	}

	/**
	 * 处理请求参数转换不支持异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ConversionNotSupportedException.class)
	public Result<Void> handleConversionNotSupportedException(ConversionNotSupportedException e) {
		LOGGER.error("请求参数转换异常", e);
		return Result.fail("请求参数转换失败");
	}

	/**
	 * 处理方法参数类型不匹配异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		return Result.fail("参数：" + StringUtils.defaultString(e.getName()) + "类型不正确");
	}

	/**
	 * 处理HTTP请求内容不可读异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		LOGGER.error("请求内容读取失败", e);
		return Result.fail("请求内容读取失败");
	}

	/**
	 * 处理文件上传失败异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MultipartException.class)
	public Result<Void> handleMultipartException(MultipartException e) {
		LOGGER.error("上传文件失败", e);
		return Result.fail("上传文件失败");
	}

	/**
	 * 处理HTTP响应内容不可写异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码500
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = HttpMessageNotWritableException.class)
	public Result<Void> handleHttpMessageNotWritableException(HttpMessageNotWritableException e) {
		LOGGER.error("响应内容写入失败", e);
		return Result.fail("响应内容写入失败");
	}

	/**
	 * 处理方法参数验证异常（Bean Validation）
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码400
	 * @since 1.0.0
	 */
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

	/**
	 * 处理请求路径不存在异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码404
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoHandlerFoundException.class)
	public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
		return Result.fail("请求路径：" + e.getRequestURL() + "不存在");
	}

	/**
	 * 处理请求路径不存在异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码404
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoResourceFoundException.class)
	public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
		return Result.fail("请求资源：" + e.getResourcePath() + "不存在");
	}

	/**
	 * 处理异步请求超时异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码503
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ExceptionHandler(value = AsyncRequestTimeoutException.class)
	public Result<Void> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
		LOGGER.error("异步请求超时", e);
		return Result.fail("异步请求超时");
	}

	/**
	 * 处理IO异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码500
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = IOException.class)
	public Result<Void> handleIOException(IOException e) {
		LOGGER.error("IO异常", e);
		return Result.fail("服务器内部错误");
	}

	/**
	 * 处理运行时异常
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码500
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = RuntimeException.class)
	public Result<Void> handleRuntimeException(RuntimeException e) {
		LOGGER.error("运行时异常", e);
		return Result.fail("服务器内部错误");
	}

	/**
	 * 处理所有其他异常（兜底处理）
	 *
	 * @param e 异常实例
	 * @return 错误响应，HTTP状态码500
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = Exception.class)
	public Result<Void> handleException(Exception e) {
		LOGGER.error("系统级异常", e);
		return Result.fail("服务器内部错误");
	}
}
