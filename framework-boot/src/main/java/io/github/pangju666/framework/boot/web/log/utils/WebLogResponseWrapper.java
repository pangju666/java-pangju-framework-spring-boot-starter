package io.github.pangju666.framework.boot.web.log.utils;

import io.github.pangju666.framework.boot.web.log.model.WebLog;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

/**
 * Web 日志响应包装器。
 *
 * <p><b>职责与特性</b></p>
 * <ul>
 *   <li>为响应提供内容缓存能力（继承 {@link ContentCachingResponseWrapper}），并携带 {@link WebLog} 以供后置组件读取和补充日志。</li>
 *   <li>若传入的响应已是 {@link ContentCachingResponseWrapper}，则仅保存其引用并委托所有 I/O 操作，避免重复包裹；同时仍承载 {@link WebLog}。</li>
 *   <li>为过滤器、拦截器等组件提供统一入口，以读取缓存响应体并进行日志增强。</li>
 * </ul>
 *
 * <p><b>使用约束</b></p>
 * <ul>
 *   <li>本类不负责日志派发或异常记录，仅承载 {@link WebLog} 与内容缓存。</li>
 *   <li>当后续组件读取了响应体缓存时，必须在链路结束前调用 {@link #copyBodyToResponse()} 将内容写回真实响应，否则客户端可能无法收到正文。</li>
 *   <li>线程安全由请求上下文保证：每个请求应使用独立实例；不保证跨线程共享安全。</li>
 * </ul>
 *
 * <p><b>典型用法</b></p>
 * <ul>
 *   <li>由过滤器在请求进入时包裹响应并创建/携带 {@link WebLog}；拦截器在前置阶段补充操作描述；过滤器在链路结束时调用 {@link #copyBodyToResponse()}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see ContentCachingResponseWrapper
 */
public class WebLogResponseWrapper extends ContentCachingResponseWrapper {
    /**
     * 可选的内部响应包装器引用。
     * <p>当输入的响应对象已被 {@link ContentCachingResponseWrapper} 包装时，优先委托给该实例，避免重复包裹。</p>
	 *
	 * @since 1.0.0
     */
    private ContentCachingResponseWrapper contentCachingResponseWrapper;
    /**
     * 携带的 Web 日志对象，用于在后置阶段补充或读取与响应相关的日志信息。
	 *
	 * @since 1.0.0
     */
    private final WebLog webLog;
	/**
	 * 目标控制器类引用。
	 * <p>用于记录定位信息，通常由上游组件在链路中设置；若未设置可能为 {@code null}。</p>
	 *
	 * @since 1.0.0
	 */
	private Class<?> targetClass;
	/**
	 * 目标控制器方法引用。
	 * <p>用于记录定位信息，通常由上游组件在链路中设置；若未设置可能为 {@code null}。</p>
	 *
	 * @since 1.0.0
	 */
	private Method targetMethod;

    /**
     * 构造方法。
     *
     * <p>行为：</p>
     * <ul>
     *   <li>若 {@code response} 已被 {@link ContentCachingResponseWrapper} 包装，则记录其引用并委托相关操作。</li>
     *   <li>否则调用父类构造完成内容缓存包装，提供响应体读取与写回能力。</li>
     * </ul>
     *
     * @param response 原始或已被缓存包装的响应对象
     * @param webLog   当前请求关联的 Web 日志对象
	 * @since 1.0.0
     */
    public WebLogResponseWrapper(HttpServletResponse response, WebLog webLog) {
		super(response);
		if (response instanceof ContentCachingResponseWrapper responseWrapper) {
			this.contentCachingResponseWrapper = responseWrapper;
		}
		this.webLog = webLog;
	}

	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public Method getTargetMethod() {
		return targetMethod;
	}

    public WebLog getWebLog() {
		return webLog;
	}

	@Override
	public void sendError(int sc) throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.sendError(sc);
		} else {
			super.sendError(sc);
		}
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.sendError(sc, msg);
		} else {
			super.sendError(sc, msg);
		}
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.sendRedirect(location);
		} else {
			super.sendRedirect(location);
		}
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getOutputStream();
		} else {
			return super.getOutputStream();
		}
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getWriter();
		} else {
			return super.getWriter();
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.flushBuffer();
		} else {
			super.flushBuffer();
		}
	}

	@Override
	public void setContentLength(int len) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.setContentLength(len);
		} else {
			super.setContentLength(len);
		}
	}

	@Override
	public void setContentLengthLong(long len) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.setContentLengthLong(len);
		} else {
			super.setContentLengthLong(len);
		}
	}

	@Override
	public boolean containsHeader(String name) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.containsHeader(name);
		} else {
			return super.containsHeader(name);
		}
	}

	@Override
	public void setHeader(String name, String value) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.setHeader(name, value);
		} else {
			super.setHeader(name, value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.addHeader(name, value);
		} else {
			super.addHeader(name, value);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.setIntHeader(name, value);
		} else {
			super.setIntHeader(name, value);
		}
	}

	@Override
	public void addIntHeader(String name, int value) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.addIntHeader(name, value);
		} else {
			super.addIntHeader(name, value);
		}
	}

	@Override
	@Nullable
	public String getHeader(String name) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getHeader(name);
		} else {
			return super.getHeader(name);
		}
	}

	@Override
	public Collection<String> getHeaders(String name) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getHeaders(name);
		} else {
			return super.getHeaders(name);
		}
	}

	@Override
	public Collection<String> getHeaderNames() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getHeaderNames();
		} else {
			return super.getHeaderNames();
		}
	}

	@Override
	public void setBufferSize(int size) {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.setBufferSize(size);
		} else {
			super.setBufferSize(size);
		}
	}

	@Override
	public void resetBuffer() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.resetBuffer();
		} else {
			super.resetBuffer();
		}
	}

	@Override
	public void reset() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.reset();
		} else {
			super.reset();
		}
	}

	@Override
    public byte[] getContentAsByteArray() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getContentAsByteArray();
		} else {
			return super.getContentAsByteArray();
		}
	}

	@Override
    public InputStream getContentInputStream() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getContentInputStream();
		} else {
			return super.getContentInputStream();
		}
	}

	@Override
    public int getContentSize() {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			return contentCachingResponseWrapper.getContentSize();
		} else {
			return super.getContentSize();
		}
	}

	@Override
    public void copyBodyToResponse() throws IOException {
		if (Objects.nonNull(contentCachingResponseWrapper)) {
			contentCachingResponseWrapper.copyBodyToResponse();
		} else {
			super.copyBodyToResponse();
		}
	}
}
