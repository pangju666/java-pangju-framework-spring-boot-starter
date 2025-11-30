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
import io.github.pangju666.framework.web.model.Result;
import io.github.pangju666.framework.web.servlet.HttpResponseBuilder;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
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

import java.util.stream.Collectors;

/**
 * 全局处理通用 HTTP 与系统异常。
 *
 * <p><strong>启用条件</strong></p>
 * <ul>
 *   <li>Servlet Web 应用，类路径存在 {@code Servlet}、{@code DispatcherServlet}</li>
 *   <li>配置项 {@code pangju.web.advice.exception=true}（默认启用）</li>
 * </ul>
 *
 * <p><strong>行为说明</strong></p>
 * <ul>
 *   <li>自定义 {@link BaseHttpException}：通过 {@link HttpResponseBuilder} 写入异常中的 HTTP 状态码与消息</li>
 *   <li>常见 Web 异常：返回对应的 HTTP 状态（如 400/404/415/503/500），消息来源于异常或统一文案</li>
 *   <li>统一错误响应结构：{@link Result#fail(String)}，不向客户端暴露服务端堆栈</li>
 * </ul>
 *
 * <p><strong>优先级</strong></p>
 * <ul>
 *   <li>{@link Ordered#HIGHEST_PRECEDENCE} + 3</li>
 * </ul>
 *
 * <p><strong>相关说明</strong></p>
 * <ul>
 *   <li>此处理器与其他专用异常处理器（如参数验证、文件上传）协同工作，提供兜底与通用映射</li>
 * </ul>
 *
 * @see ExceptionHandler
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, Result.class})
@ConditionalOnBooleanProperty(prefix = "pangju.web.advice", value = "exception", matchIfMissing = true)
@RestControllerAdvice
public class GlobalWebExceptionAdvice {
	/**
	 * 日志记录器。
	 *
	 * @since 1.0.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalWebExceptionAdvice.class);

	// ============ 自定义异常 ============
	/**
	 * 处理自定义 HTTP 异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>通过 {@link HttpResponseBuilder} 将异常的 HTTP 状态码与消息写入响应</li>
	 *   <li>不返回统一结构；直接输出自定义异常格式</li>
	 * </ul>
	 *
	 * @param e        自定义 HTTP 异常
	 * @param response HTTP 响应对象
	 * @since 1.0.0
	 */
	@ExceptionHandler(value = BaseHttpException.class)
	public void handleBaseHttpException(BaseHttpException e, HttpServletResponse response) {
		HttpResponseBuilder.from(response).writeHttpException(e);
	}

	// ============ 400 Bad Request: 参数绑定与校验 ============
	/**
	 * 处理缺少请求参数异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
	 *   <li>提示缺少的请求参数名称</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		return Result.fail("缺少请求参数：" + e.getParameterName());
	}

	/**
	 * 处理缺少请求头异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
	 *   <li>提示缺少的请求头名称</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestHeaderException.class)
	public Result<Void> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
		return Result.fail("缺少请求头：" + e.getHeaderName());
	}

    /**
     * 处理缺少路径变量异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>提示缺少的路径变量名称</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingPathVariableException.class)
	public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
		return Result.fail("缺少路径变量：" + e.getVariableName());
	}

    /**
     * 处理缺少请求值异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>返回统一文案提示：“缺少请求值”；并以 WARN 级别记录日志</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingRequestValueException.class)
	public Result<Void> handleMissingRequestValueException(MissingRequestValueException e) {
		LOGGER.warn("缺少请求值异常", e);
		return Result.fail("缺少请求值");
	}

	/**
	 * 处理缺少请求表单参数异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）。</li>
	 *   <li>提示缺少的表单部件（part）名称。</li>
	 * </ul>
	 *
	 * <p><strong>说明</strong></p>
	 * <ul>
	 *   <li>发生在 multipart 请求缺失必要的部件时（{@link MissingServletRequestPartException}）。</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MissingServletRequestPartException.class)
	public Result<Void> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
		return Result.fail("缺少表单参数：" + e.getRequestPartName());
	}

	/**
	 * 处理方法参数类型不匹配异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
	 *   <li>提示不匹配的参数名称</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
		String name = e.getName();
		if (StringUtils.isBlank(name)) {
			return Result.fail("请求参数类型不正确");
		} else {
			return Result.fail("请求参数：" + name + "类型不正确");
		}
	}

	/**
	 * 处理类型不匹配异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）。</li>
	 *   <li>当存在属性名时提示“参数：{propertyName} 类型不正确”，否则返回“请求参数类型不正确”。</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(TypeMismatchException.class)
	public Result<Void> handleTypeMismatchException(TypeMismatchException e) {
		String propertyName = e.getPropertyName();
		if (StringUtils.isNotBlank(propertyName)) {
			return Result.fail("参数：" + propertyName + "类型不正确");
		}
		return Result.fail("请求参数类型不正确");
	}

    /**
     * 处理请求参数绑定异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>记录 WARN 级别日志</li>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = ServletRequestBindingException.class)
	public Result<Void> handleServletRequestBindingException(ServletRequestBindingException e) {
		LOGGER.warn("请求参数绑定异常", e);
		return Result.fail("请求参数绑定错误");
	}

    /**
     * 处理请求内容不可读异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>记录 WARN 级别日志</li>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>若为 JSON 映射错误（{@link MappingJacksonInputMessage}），提示“请求数据格式错误，请检查是否为有效的 JSON”；否则提示“请求数据格式错误”</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		LOGGER.warn("请求内容不可读异常", e);
		HttpInputMessage httpInputMessage = e.getHttpInputMessage();
		if (httpInputMessage instanceof MappingJacksonInputMessage) {
			return Result.fail("请求数据格式错误，请检查是否为有效的 JSON");
		} else {
			return Result.fail("请求数据格式错误");
		}
	}

    /**
     * 处理方法参数验证异常（Bean Validation）。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     *   <li>聚合所有字段错误的默认消息（过滤空白），以分号连接</li>
     *   <li>无可用提示时返回“请求参数验证不合法”</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		String allMessages = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.joining("；"));
		return Result.fail(StringUtils.defaultIfBlank(allMessages, "请求参数验证不合法"));
	}

	/**
	 * 处理表单绑定异常（BindException）。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）。</li>
	 *   <li>聚合所有字段错误的默认消息（过滤空白），以分号连接；无提示时返回“请求参数验证不合法”。</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = BindException.class)
	public Result<Void> handleBindException(BindException e) {
		String allMessages = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(FieldError::getDefaultMessage)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.joining("；"));
		return Result.fail(StringUtils.defaultIfBlank(allMessages, "请求参数验证不合法"));
	}

	/**
	 * 处理上传文件大小超过限制异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）。</li>
	 *   <li>当 {@code e.getMaxUploadSize() == -1} 时返回“上传文件大小超过限制”；否则展示允许大小（MB，向下取整）。</li>
	 * </ul>
	 *
	 * @param e 上传大小超限异常
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MaxUploadSizeExceededException.class)
	public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		if (e.getMaxUploadSize() == -1) {
			return Result.fail("上传文件大小超过限制");
		}
		return Result.fail("上传文件大小超过" + DataSize.ofBytes(e.getMaxUploadSize()).toMegabytes() + "MB");
	}

    /**
     * 处理文件上传失败异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>记录 WARN 级别日志</li>
     *   <li>返回统一失败响应，HTTP 400（{@link HttpStatus#BAD_REQUEST}）</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = MultipartException.class)
	public Result<Void> handleMultipartException(MultipartException e) {
		LOGGER.warn("文件上传失败异常", e);
		return Result.fail("文件上传失败");
	}

	// ============ 404 Not Found: 路径与资源 ============
	/**
	 * 处理请求路径不存在异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 404（{@link HttpStatus#NOT_FOUND}）</li>
	 *   <li>提示不存在的请求路径</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoHandlerFoundException.class)
	public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
		return Result.fail("请求路径：" + e.getRequestURL() + "不存在");
	}

	/**
	 * 处理请求资源不存在异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 404（{@link HttpStatus#NOT_FOUND}）</li>
	 *   <li>提示不存在的资源路径</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = NoResourceFoundException.class)
	public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
		return Result.fail("请求资源：" + e.getResourcePath() + "不存在");
	}

	// ============ 405 Method Not Allowed ============
	/**
	 * 处理请求方法不支持异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 405（{@link HttpStatus#METHOD_NOT_ALLOWED}）</li>
	 *   <li>提示期望的请求方法集合</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
		return Result.fail("预期的请求方法类型：" + StringUtils.join(e.getSupportedMethods(), "、"));
	}

	// ============ 406 / 415 媒体类型不支持 ============
	/**
	 * 处理请求的 Content-Type 不支持异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 415（{@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}）</li>
	 *   <li>提示期望的媒体类型集合</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
	public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
		return Result.fail("预期的请求数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	/**
	 * 处理 Accept 响应类型不支持异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>返回统一失败响应，HTTP 406（{@link HttpStatus#NOT_ACCEPTABLE}）</li>
	 *   <li>提示期望的媒体类型集合</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
	public Result<Void> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
		return Result.fail("预期的响应数据类型：" + StringUtils.join(e.getSupportedMediaTypes(), "、"));
	}

	// ============ 5xx 服务器错误 ============
    /**
     * 处理响应内容不可写异常。
     *
     * <p><strong>行为</strong></p>
     * <ul>
     *   <li>记录 ERROR 级别日志</li>
     *   <li>返回统一失败响应，HTTP 500（{@link HttpStatus#INTERNAL_SERVER_ERROR}）</li>
     * </ul>
     *
     * @param e 异常实例
     * @return 统一失败响应
     * @since 1.0.0
     */
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = HttpMessageNotWritableException.class)
	public Result<Void> handleHttpMessageNotWritableException(HttpMessageNotWritableException e) {
		LOGGER.error("响应内容不可写异常", e);
		return Result.fail("响应内容写入失败");
	}

	/**
	 * 处理异步请求超时异常。
	 *
	 * <p><strong>行为</strong></p>
	 * <ul>
	 *   <li>记录 ERROR 级别日志</li>
	 *   <li>返回统一失败响应，HTTP 503（{@link HttpStatus#SERVICE_UNAVAILABLE}）</li>
	 * </ul>
	 *
	 * @param e 异常实例
	 * @return 统一失败响应
	 * @since 1.0.0
	 */
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ExceptionHandler(value = AsyncRequestTimeoutException.class)
	public Result<Void> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
		LOGGER.error("异步请求超时异常", e);
		return Result.fail("异步请求超时");
	}
}
