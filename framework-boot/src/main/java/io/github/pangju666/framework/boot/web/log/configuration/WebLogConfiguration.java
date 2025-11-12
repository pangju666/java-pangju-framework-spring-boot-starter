package io.github.pangju666.framework.boot.web.log.configuration;

import java.util.Set;

/**
 * Web 日志采集配置。
 *
 * <p>用于定义请求与响应日志采集的范围与策略，包括是否记录头、查询参数、
 * 请求/响应体，以及可接受的媒体类型集合。</p>
 *
 * <p><b>结构</b></p>
 * <ul>
 *   <li>{@link #request}：请求采集配置。</li>
 *   <li>{@link #response}：响应采集配置。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class WebLogConfiguration {
	/**
	 * 请求记录配置
	 * <p>
	 * 定义记录 HTTP 请求数据的范围，包括请求头、查询参数、请求体等。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Request request = new Request();
	/**
	 * 响应记录配置
	 * <p>
	 * 定义记录 HTTP 响应数据的范围，包括响应头、响应体等。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Response response = new Response();

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * 请求记录配置内部类
	 * <p>
	 * 配置需要记录的 HTTP 请求数据范围。
	 * </p>
	 * <ul>
	 *     <li>{@link #headers}：是否记录请求头信息。</li>
	 *     <li>{@link #queryParams}：是否记录 URL 查询参数。</li>
	 *     <li>{@link #body}：是否记录请求体内容。</li>
	 *     <li>{@link #multipart}：是否记录 Multipart 数据（如文件上传）。</li>
	 * </ul>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Request {
		/**
		 * 是否记录请求头
		 * <p>
		 * 默认值为 {@code true}，表示记录请求头信息。
		 * 如果设置为 {@code false}，则忽略所有请求头记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean headers = true;
		/**
		 * 是否记录查询参数
		 * <p>
		 * 默认值为 {@code true}，表示记录 URL 查询参数。
		 * 如果设置为 {@code false}，则忽略查询参数记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean queryParams = true;
		/**
		 * 是否记录请求体
		 * <p>
		 * 默认值为 {@code true}，表示记录请求体内容。
		 * 如果设置为 {@code false}，则忽略请求体内容记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean body = true;
		/**
		 * 是否记录 Multipart 数据
		 * <p>
		 * 默认值为 {@code true}，表示记录文件上传的 Multipart 数据。
		 * 如果设置为 {@code false}，则忽略 Multipart 数据记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean multipart = true;
		/**
		 * 允许采集的请求体媒体类型集合。
		 *
		 * <p>仅当请求的 {@code Content-Type} 属于该集合，且 {@link #body} 开关为真时，才记录请求体。</p>
		 *
		 * @since 1.0.0
		 */
		private Set<String> acceptableMediaTypes;

		public boolean isHeaders() {
			return headers;
		}

		public void setHeaders(boolean headers) {
			this.headers = headers;
		}

		public boolean isQueryParams() {
			return queryParams;
		}

		public void setQueryParams(boolean queryParams) {
			this.queryParams = queryParams;
		}

		public boolean isBody() {
			return body;
		}

		public void setBody(boolean body) {
			this.body = body;
		}

		public boolean isMultipart() {
			return multipart;
		}

		public void setMultipart(boolean multipart) {
			this.multipart = multipart;
		}

		public Set<String> getAcceptableMediaTypes() {
			return acceptableMediaTypes;
		}

		public void setAcceptableMediaTypes(Set<String> acceptableMediaTypes) {
			this.acceptableMediaTypes = acceptableMediaTypes;
		}
	}

	/**
	 * 响应记录配置内部类
	 * <p>
	 * 配置需要记录的 HTTP 响应数据范围。
	 * </p>
	 * <ul>
	 *     <li>{@link #headers}：是否记录响应头信息。</li>
	 *     <li>{@link #body}：是否记录响应体内容。</li>
	 *     <li>{@link #resultData}：是否记录 Result 类型的附加数据。</li>
	 * </ul>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Response {
		/**
		 * 是否记录响应头
		 * <p>
		 * 默认值为 {@code true}，表示记录响应头信息。
		 * 如果设置为 {@code false}，则忽略所有响应头记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean headers = true;
		/**
		 * 是否记录响应体
		 * <p>
		 * 默认值为 {@code true}，表示记录响应体内容。
		 * 如果设置为 {@code false}，则忽略响应体内容记录。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean body = true;
		/**
		 * 是否记录附加数据（Result 类型）
		 * <p>
		 * 默认值为 {@code false}。仅在响应内容为统一结果封装类型时可用。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean resultData = false;
		/**
		 * 允许采集的响应体媒体类型集合。
		 *
		 * <p>仅当响应的 {@code Content-Type} 属于该集合，且 {@link #body} 开关为真时，才记录响应体。</p>
		 *
		 * @since 1.0.0
		 */
		private Set<String> acceptableMediaTypes;

		public boolean isHeaders() {
			return headers;
		}

		public void setHeaders(boolean headers) {
			this.headers = headers;
		}

		public boolean isBody() {
			return body;
		}

		public void setBody(boolean body) {
			this.body = body;
		}

		public boolean isResultData() {
			return resultData;
		}

		public void setResultData(boolean resultData) {
			this.resultData = resultData;
		}

		public Set<String> getAcceptableMediaTypes() {
			return acceptableMediaTypes;
		}

		public void setAcceptableMediaTypes(Set<String> acceptableMediaTypes) {
			this.acceptableMediaTypes = acceptableMediaTypes;
		}
	}
}
