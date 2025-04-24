package io.github.pangju666.framework.autoconfigure.web.model;

import jakarta.servlet.http.Part;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public class WebLog {
	private String ip;
	private String url;
	private String method;
	private String date;
	private Long costMillis;
	private String operation;
	private Request request;
	private Response response;
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

	public static class Request {
		private HttpHeaders headers;
		private MultiValueMap<String, String> queryParams;
		private String characterEncoding;
		private Map<String, Part> formData;
		private int contentLength;
		private String contentType;
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

		public MultiValueMap<String, String> getQueryParams() {
			return queryParams;
		}

		public void setQueryParams(MultiValueMap<String, String> queryParams) {
			this.queryParams = queryParams;
		}

		public Object getBody() {
			return body;
		}

		public void setBody(Object body) {
			this.body = body;
		}
	}

	public static class Response {
		private Integer status;
		private String contentType;
		private String characterEncoding;
		private HttpHeaders headers;
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