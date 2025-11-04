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

package io.github.pangju666.framework.boot.autoconfigure.web.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 日志配置属性类
 * <p>
 * 该配置类用于定义和控制 Web 日志功能的具体行为，包括日志的处理方式、
 * 日志传输的目标、以及需要记录的请求和响应数据的范围。
 * 配置前缀：{@code pangju.web.log}
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>支持多种日志传递方式（Kafka、Disruptor）。</li>
 *     <li>灵活配置要记录的请求头、请求体和响应数据。</li>
 *     <li>支持 MongoDB 和 Kafka 的日志存储。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.web.log")
public class WebLogProperties {
	/**
	 * 日志传递方式
	 * <p>
	 * 定义日志的发送方式。支持两种模式：
	 * <ul>
	 *     <li>{@link SenderType#DISRUPTOR}（默认）：通过高性能的 RingBuffer 异步处理日志。</li>
	 *     <li>{@link SenderType#KAFKA}：通过 Kafka Topic 将日志发送到消息中间件。</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private SenderType senderType = SenderType.DISRUPTOR;
	/**
	 * Kafka 配置
	 * <p>
	 * 定义当 {@link #senderType} 为 {@link SenderType#KAFKA} 时，
	 * Kafka 相关的日志发送配置，如 KafkaTemplate Bean 名称和目标 Topic。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Kafka kafka = new Kafka();
	/**
	 * MongoDB 配置
	 * <p>
	 * 定义当使用 MongoDB 存储日志时的相关参数，例如 MongoTemplate Bean 名称和
	 * 集合名称的前缀。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Mongo mongo = new Mongo();
	/**
	 * Disruptor 配置
	 * <p>
	 * 定义当使用 Disruptor 作为日志传递方式时的环形缓冲区大小。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Disruptor disruptor = new Disruptor();
	/**
	 * Web 日志功能开关
	 * <p>
	 * 是否启用 Web 日志功能。默认为 {@code true}。
	 * 如果设置为 {@code false}，则不记录任何 Web 日志数据。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private boolean enabled = true;
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

	/**
	 * 日志传递方式枚举
	 * <p>
	 * 定义日志数据的传递方式。目前支持以下两种模式：
	 * <ul>
	 *     <li>{@link #KAFKA}：通过 Kafka 发送日志到指定 Topic。</li>
	 *     <li>{@link #DISRUPTOR}：使用 Disruptor 提供高性能异步日志处理。</li>
	 * </ul>
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public enum SenderType {
		KAFKA,
		DISRUPTOR
	}

	/**
	 * MongoDB 配置内部类
	 * <p>
	 * 定义使用 MongoDB 存储日志时的相关配置信息。
	 * </p>
	 * <ul>
	 *     <li>{@link #mongoTemplateBeanName}：指定使用的 MongoTemplate Bean 名称。</li>
	 *     <li>{@link #collectionPrefix}：日志集合名称的前缀。</li>
	 * </ul>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Mongo {
		/**
		 * MongoTemplate Bean 名称
		 * <p>
		 * 用于指定应用中某个 MongoTemplate 实例来操作 MongoDB 集合。
		 * 如果未指定，则使用默认的 MongoTemplate。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String mongoTemplateBeanName;
		/**
		 * 集合名称前缀
		 * <p>
		 * MongoDB 日志数据集合的名称前缀。默认值为 {@code web_log}。
		 * 实际的集合名称可以根据模块或业务动态生成。
		 * </p>
		 *
		 * @since 1.0.0
		 */
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

	/**
	 * Kafka 配置内部类
	 * <p>
	 * 当选择 Kafka 作为日志传递方式时，配置 Kafka 相关参数。
	 * </p>
	 * <ul>
	 *     <li>{@link #kafkaTemplateBeanName}：KafkaTemplate Bean 名称。</li>
	 *     <li>{@link #topic}：Kafka Topic 名称，日志将发送到指定的 Topic。</li>
	 * </ul>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Kafka {
		/**
		 * KafkaTemplate Bean 名称
		 * <p>
		 * 用于指定要使用的 KafkaTemplate 实例。如果未指定，
		 * 则默认使用应用中存在的 KafkaTemplate。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String kafkaTemplateBeanName;
		/**
		 * 日志发送目标 Topic
		 * <p>
		 * 定义日志数据发送到的 Kafka Topic 名称。
		 * </p>
		 *
		 * @since 1.0.0
		 */
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

	/**
	 * Disruptor 配置内部类
	 * <p>
	 * 配置 Disruptor 的缓冲区大小，用于控制日志数据的异步处理性能。
	 * </p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Disruptor {
		/**
		 * 环形缓冲区大小
		 * <p>
		 * 定义 Disruptor 使用的缓冲区大小。默认为 1024。
		 * 缓冲区大小应为 2 的指数倍，以获得最佳性能。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private int bufferSize = 1024;

		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}
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
