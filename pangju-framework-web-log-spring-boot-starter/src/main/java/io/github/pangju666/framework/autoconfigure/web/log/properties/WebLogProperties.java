package io.github.pangju666.framework.autoconfigure.web.log.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chang-tech.web.log")
public class WebLogProperties {
	private Kafka kafka = new Kafka();
	private Mongo mongo = new Mongo();
	private boolean enabled = true;
	private Request request = new Request();
	private Response response = new Response();

	public Mongo getMongo() {
		return mongo;
	}

	public void setMongo(Mongo mongo) {
		this.mongo = mongo;
	}

	public Kafka getKafka() {
		return kafka;
	}

	public void setKafka(Kafka kafka) {
		this.kafka = kafka;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static class Mongo {
		private String templateBeanName;
		private String collectionPrefix = "request_log";

		public String getTemplateBeanName() {
			return templateBeanName;
		}

		public void setTemplateBeanName(String templateBeanName) {
			this.templateBeanName = templateBeanName;
		}

		public String getCollectionPrefix() {
			return collectionPrefix;
		}

		public void setCollectionPrefix(String collectionPrefix) {
			this.collectionPrefix = collectionPrefix;
		}
	}

	public static class Kafka {
		private String templateBeanName;
		private String topic;

		public String getTemplateBeanName() {
			return templateBeanName;
		}

		public void setTemplateBeanName(String templateBeanName) {
			this.templateBeanName = templateBeanName;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}
	}

	public static class Request {
		boolean headers = true;
		boolean queryParams = true;
		boolean body = true;
		boolean multipart = true;

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
	}

	public static class Response {
		boolean headers = true;
		boolean body = false;

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
	}
}
