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

package io.github.pangju666.framework.autoconfigure.web.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pangju.web.log")
public class WebLogProperties {
	private SenderType senderType = SenderType.DISRUPTOR;
	private Kafka kafka = new Kafka();
	private Mongo mongo = new Mongo();
	private Disruptor disruptor = new Disruptor();
	private boolean enabled = true;
	private Request request = new Request();
	private Response response = new Response();

	public SenderType getSenderType() {
		return senderType;
	}

	public void setSenderType(SenderType senderType) {
		this.senderType = senderType;
	}

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

	public Disruptor getDisruptor() {
		return disruptor;
	}

	public void setDisruptor(Disruptor disruptor) {
		this.disruptor = disruptor;
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

	public enum SenderType {
		KAFKA,
		DISRUPTOR
	}

	public static class Mongo {
		private String mongoTemplateBeanName;
		private String collectionPrefix = "web_log";

		public String getMongoTemplateBeanName() {
			return mongoTemplateBeanName;
		}

		public void setMongoTemplateBeanName(String mongoTemplateBeanName) {
			this.mongoTemplateBeanName = mongoTemplateBeanName;
		}

		public String getCollectionPrefix() {
			return collectionPrefix;
		}

		public void setCollectionPrefix(String collectionPrefix) {
			this.collectionPrefix = collectionPrefix;
		}
	}

	public static class Kafka {
		private String kafkaTemplateBeanName;
		private String topic;

		public String getKafkaTemplateBeanName() {
			return kafkaTemplateBeanName;
		}

		public void setKafkaTemplateBeanName(String kafkaTemplateBeanName) {
			this.kafkaTemplateBeanName = kafkaTemplateBeanName;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}
	}

	public static class Disruptor {
		private int bufferSize = 1024;

		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}
	}

	public static class Request {
		private boolean headers = true;
		private boolean queryParams = true;
		private boolean body = true;
		private boolean multipart = true;

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
		private boolean headers = true;
		private boolean body = true;
		private boolean resultData = false;

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
	}
}
