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

package io.github.pangju666.framework.boot.web.log.model;

import io.github.pangju666.framework.boot.web.log.annotation.WebLogOperation;
import jakarta.servlet.http.Part;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

/**
 * Web 请求日志实体类
 * <p>
 * 表示一次 Web 请求的完整日志记录，包括请求的基本信息、请求内容、响应内容、
 * 以及自定义的扩展日志数据。此类用于统一记录和存储 HTTP 请求与响应的信息。
 * </p>
 *
 * <p>关键功能：</p>
 * <ul>
 *     <li>记录 Web 请求的来源 IP、URL、HTTP 方法及耗时。</li>
 *     <li>存储请求和响应的详细数据（如头信息、体内容等）。</li>
 *     <li>支持自定义扩展字段。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class WebLog {
	/**
	 * 访问来源 IP 地址
	 * <p>
	 * 表示发起请求客户端的 IP 地址信息。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String ip;
	/**
	 * 被访问的 URL 地址
	 * <p>
	 * 表示客户端请求的目标资源 URL 地址，例如：/api/user。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String url;
	/**
	 * HTTP 请求方式
	 * <p>
	 * 表示请求中使用的 HTTP 方法，例如：GET、POST 等。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String method;
	/**
	 * 请求日期和时间
	 * <p>
	 * 表示请求到达服务端的时间戳，格式化为字符串，通常为 ISO8601 格式。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String date;
	/**
	 * 请求处理耗时（毫秒）
	 * <p>
	 * 存储服务端从接收到请求到处理完成所消耗的时间（单位：毫秒）。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Long costMillis;
	/**
	 * 操作描述
	 * <p>
	 * 对请求操作的功能描述，由 {@link WebLogOperation} 注解指定。
	 * 一般用于标识操作功能（例如：查询用户信息）。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String operation;
	/**
	 * HTTP 请求详细内容
	 * <p>
	 * 存储客户端请求的详细数据，包括请求头、查询参数、请求体等。
	 * </p>
	 *
	 * @see Request
	 * @since 1.0.0
	 */
	private Request request;
	/**
	 * HTTP 响应详细内容
	 * <p>
	 * 存储服务端返回给客户端的详细响应信息，包括响应头、响应体等。
	 * </p>
	 *
	 * @see Response
	 * @since 1.0.0
	 */
	private Response response;
	/**
	 * 自定义扩展数据
	 * <p>
	 * 存储额外的日志扩展信息，键值对形式用于满足特定的业务需求。
	 * 例如，记录自定义的业务字段或调试信息。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Map<String, Object> expandData;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getCostMillis() {
		return costMillis;
	}

	public void setCostMillis(Long costMillis) {
		this.costMillis = costMillis;
	}

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

	public Map<String, Object> getExpandData() {
		return expandData;
	}

	public void setExpandData(Map<String, Object> expandData) {
		this.expandData = expandData;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * 请求内容内部类
	 * <p>
	 * 表示 HTTP 请求的详细数据，包括请求头、请求参数、请求体等字段。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Request {
		/**
		 * 请求头信息
		 * <p>
		 * 存储所有 HTTP 请求头的键值对集合。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private HttpHeaders headers;
		/**
		 * 查询参数
		 * <p>
		 * 存储 HTTP URL 查询参数的键值对集合。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private Map<String, List<String>> queryParams;
		/**
		 * 请求体的字符编码
		 * <p>
		 * 表示请求体数据的编码方式，例如：UTF-8。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String characterEncoding;
		/**
		 * Multipart 表单数据
		 * <p>
		 * 存储 Multipart 数据表单中所有字段的键值对集合。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private Map<String, Part> formData;
		/**
		 * 请求体的长度
		 * <p>
		 * 记录 HTTP 请求体的字节长度。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private int contentLength;
		/**
		 * 请求体的内容类型
		 * <p>
		 * 表示 HTTP 请求的 Content-Type，例如：application/json。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String contentType;
		/**
		 * 请求体数据
		 * <p>
		 * 记录完整的请求体内容，可为字符串或 JSON 对象。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private Object body;

		public Map<String, Part> getFormData() {
			return formData;
		}

		public void setFormData(Map<String, Part> formData) {
			this.formData = formData;
		}

		public int getContentLength() {
			return contentLength;
		}

		public void setContentLength(int contentLength) {
			this.contentLength = contentLength;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public String getCharacterEncoding() {
			return characterEncoding;
		}

		public void setCharacterEncoding(String characterEncoding) {
			this.characterEncoding = characterEncoding;
		}

		public HttpHeaders getHeaders() {
			return headers;
		}

		public void setHeaders(HttpHeaders headers) {
			this.headers = headers;
		}

		public Map<String, List<String>> getQueryParams() {
			return queryParams;
		}

		public void setQueryParams(Map<String, List<String>> queryParams) {
			this.queryParams = queryParams;
		}

		public Object getBody() {
			return body;
		}

		public void setBody(Object body) {
			this.body = body;
		}
	}

	/**
	 * 响应内容内部类
	 * <p>
	 * 表示 HTTP 响应的详细数据，包括响应头、响应体等字段。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Response {
		/**
		 * 响应状态码
		 * <p>
		 * 表示 HTTP 响应的状态码，例如：200、404 等。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private Integer status;
		/**
		 * 响应体的内容类型
		 * <p>
		 * 表示 HTTP 响应的 Content-Type，例如：application/json。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String contentType;
		/**
		 * 响应体的字符编码
		 * <p>
		 * 表示响应体的字符编码方式，例如：UTF-8。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String characterEncoding;
		/**
		 * 响应头信息
		 * <p>
		 * 存储所有 HTTP 响应头的键值对集合。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private HttpHeaders headers;
		/**
		 * 响应体数据
		 * <p>
		 * 记录完整的响应体内容，可为字符串或 JSON 对象。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private Object body;

		public String getCharacterEncoding() {
			return characterEncoding;
		}

		public void setCharacterEncoding(String characterEncoding) {
			this.characterEncoding = characterEncoding;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}

		public HttpHeaders getHeaders() {
			return headers;
		}

		public void setHeaders(HttpHeaders headers) {
			this.headers = headers;
		}

		public Object getBody() {
			return body;
		}

		public void setBody(Object body) {
			this.body = body;
		}
	}
}