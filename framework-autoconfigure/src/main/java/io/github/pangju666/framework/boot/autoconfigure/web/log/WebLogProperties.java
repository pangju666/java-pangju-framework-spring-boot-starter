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
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Set;

/**
 * Web 日志配置属性。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>定义 Web 日志的整体行为，包括发送通道、持久化参数与请求/响应记录范围。</li>
 *   <li>配置前缀：{@code pangju.web.log}。</li>
 * </ul>
 *
 * <p><b>能力</b></p>
 * <ul>
 *   <li>发送通道：支持 {@code KAFKA} 与 {@code DISRUPTOR} 两种模式。</li>
 *   <li>接收器类型：支持 {@code SLF4J}、{@code MONGODB} 等接收器，用于持久化或转发。</li>
 *   <li>记录范围：请求（头/查询参数/体/multipart）与响应（头/体/附加数据）。</li>
 *   <li>目标参数：提供 Kafka、MongoDB、Disruptor 和 Slf4J 的目标配置。</li>
 *   <li>路径排除：通过 {@link #excludePathPatterns} 排除无需记录的请求路径。</li>
 * </ul>
 *
 * <p><b>示例（application.yml）</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true
 *       sender-type: kafka
 *       receiver-type: slf4j
 *       kafka:
 *         kafka-template-ref: myKafkaTemplate
 *         topic: web-log
 *       mongo:
 *         mongo-template-ref: myMongoTemplate
 *         base-collection-name: web-log
 *       disruptor:
 *         buffer-size: 1024
 *       slf4j:
 *         logger: WebLogLogger
 *       request:
 *         headers: true
 *         query-params: true
 *         body: true
 *         multipart: true
 *         acceptable-media-types:
 *           - application/json
 *           - text/plain
 *       response:
 *         headers: true
 *         body: true
 *         result-data: true
 *         acceptable-media-types:
 *           - application/json
 *           - text/plain
 *       exclude-path-patterns:
 *         - /actuator/**
 *         - /swagger-ui/**
 *         - /v3/api-docs/**
 *         - /favicon.ico
 * </pre>
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
     * 日志接收器类型
     * <p>
     * 指定 Web 日志的落地或转发目标。默认值为 {@link ReceiverType#SLF4J}。
     * 当选择不同类型时，分别使用对应的配置段（如 {@link #slf4j}、{@link #mongo}）。
     * </p>
     *
     * @since 1.0.0
     */
	private ReceiverType receiverType = ReceiverType.SLF4J;
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
     * Slf4j 接收器配置
     * <p>
     * 当 {@link #receiverType} 为 {@link ReceiverType#SLF4J} 时生效，用于指定目标日志记录器名称，
     * 将采集的 {@link io.github.pangju666.framework.boot.web.log.model.WebLog} 写入日志系统（SLF4J 兼容实现，如 Logback、Log4j2）。
     * </p>
     *
     * @since 1.0.0
     */
	private Slf4j slf4j = new Slf4j();
	/**
	 * Web 日志功能开关
	 * <p>
	 * 是否启用 Web 日志功能。默认为 {@code false}。
	 * 如果设置为 {@code false}，则不记录任何 Web 日志数据。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private boolean enabled = false;
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
	/**
	 * 日志采集排除路径模式集合。
	 *
	 * <p><b>用途</b></p>
	 * <ul>
	 *   <li>用于排除不需要记录的请求路径（如健康检查、监控与文档访问）。</li>
	 *   <li>当请求路径匹配任一模式时，该请求的 Web 日志将不会被采集。</li>
	 * </ul>
	 *
	 * <p><b>格式</b></p>
	 * <ul>
	 *   <li>支持常见通配符路径模式，例如 {@code /actuator/**}、{@code /static/*}。</li>
	 *   <li>配置示例（application.yml）：
	 *   <pre>
	 *   pangju:
	 *     web:
	 *       log:
	 *         exclude-path-patterns:
	 *           - /actuator/**
	 *           - /swagger-ui/**
	 *           - /v3/api-docs/**
	 *           - /favicon.ico
	 *   </pre>
	 *   </li>
	 *   <li>默认值为空集合，表示不过滤任何路径。</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	private Set<String> excludePathPatterns = Collections.emptySet();

	public Slf4j getSlf4j() {
		return slf4j;
	}

	public void setDisk(Slf4j slf4j) {
		this.slf4j = slf4j;
	}

	public ReceiverType getReceiverType() {
		return receiverType;
	}

	public void setReceiverType(ReceiverType receiverType) {
		this.receiverType = receiverType;
	}

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

	public Set<String> getExcludePathPatterns() {
		return excludePathPatterns;
	}

	public void setExcludePathPatterns(Set<String> excludePathPatterns) {
		this.excludePathPatterns = excludePathPatterns;
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
     * 日志接收器类型枚举。
     *
     * <p>用于指定 Web 日志的最终处理位置或介质。</p>
     * <ul>
     *   <li>{@link #SLF4J}：写入日志系统（SLF4J 兼容实现，如 Logback、Log4j2），适用于本地归档或集中采集。</li>
     *   <li>{@link #MONGODB}：写入 MongoDB 集合，适用于检索与分析。</li>
     * </ul>
     *
     * @author pangju666
     * @since 1.0.0
     */
    public enum ReceiverType {
		SLF4J,
        MONGODB
    }

    /**
     * MongoDB 配置。
     *
     * <p><b>字段</b></p>
     * <ul>
     *   <li>{@link #mongoTemplateRef} 指定使用的 {@code MongoTemplate} Bean 名称。</li>
     *   <li>{@link #baseCollectionName} 集合名称基础前缀，用于生成实际集合名。</li>
     * </ul>
     *
     * <p><b>示例（application.yml）</b></p>
     * <pre>
     * pangju:
     *   web:
     *     log:
     *       mongo:
     *         mongo-template-ref: myMongoTemplate
     *         base-collection-name: web-log
     * </pre>
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
		private String mongoTemplateRef;
        /**
         * 集合名称基础前缀。
         * <p>
         * MongoDB 日志集合的基础名称，默认 {@code web-log}。实际集合名可据模块或业务在此基础上扩展。
         * </p>
         *
         * @since 1.0.0
         */
		private String baseCollectionName = "web-log";

		public String getMongoTemplateRef() {
			return mongoTemplateRef;
		}

		public void setMongoTemplateRef(String mongoTemplateRef) {
			this.mongoTemplateRef = mongoTemplateRef;
		}

		public String getBaseCollectionName() {
			return baseCollectionName;
		}

		public void setBaseCollectionName(String baseCollectionName) {
			this.baseCollectionName = baseCollectionName;
		}
	}

    /**
     * Kafka 配置。
     *
     * <p><b>字段</b></p>
     * <ul>
     *   <li>{@link #kafkaTemplateRef} 指定 {@code KafkaTemplate} Bean 名称。</li>
     *   <li>{@link #topic} 发送目标 Topic 名称。</li>
     * </ul>
     *
     * <p><b>示例（application.yml）</b></p>
     * <pre>
     * pangju:
     *   web:
     *     log:
     *       sender-type: kafka
     *       kafka:
     *         kafka-template-ref: myKafkaTemplate
     *         topic: web-log
     * </pre>
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
		private String kafkaTemplateRef;
		/**
		 * 日志发送目标 Topic
		 * <p>
		 * 定义日志数据发送到的 Kafka Topic 名称。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private String topic;

		public String getKafkaTemplateRef() {
			return kafkaTemplateRef;
		}

		public void setKafkaTemplateRef(String kafkaTemplateRef) {
			this.kafkaTemplateRef = kafkaTemplateRef;
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
     * Slf4j 接收器配置。
     *
     * <p><b>概述</b></p>
     * <ul>
     *   <li>配置目标日志记录器名称，用于将采集的 {@link io.github.pangju666.framework.boot.web.log.model.WebLog} 写入日志系统。</li>
     *   <li>适配任意 SLF4J 兼容实现（Logback、Log4j2 等）。</li>
     * </ul>
	 *
	 * <p>logback配置示例</p>
	 * <pre>{@code
	 * 	<!-- 请求日志专用 Appender -->
	 * 	<appender name="WEB_LOG_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
	 * 		<file>E:/logs/web/web.log</file>
	 * 		<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
	 * 			<fileNamePattern>E:/logs/web/web.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
	 * 			<maxFileSize>500MB</maxFileSize>
	 * 			<maxHistory>30</maxHistory>
	 * 			<totalSizeCap>10GB</totalSizeCap>
	 * 		</rollingPolicy>
	 * 		<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
	 * 			<pattern>%msg%n</pattern>
	 * 			<charset>UTF-8</charset>
	 * 		</encoder>
	 * 	</appender>
	 * 	<!-- 绑定专用 Logger -->
	 * 	<logger name="WebLogLogger" level="INFO" additivity="false">
	 * 		<appender-ref ref="WEB_LOG_FILE"/>
	 * 	</logger>
	 * }</pre>
	 *
	 * <p>log4j2 xml配置示例</p>
	 * <pre>{@code
	 * <Appenders>
	 * 		<!-- 请求日志专用 Appender -->
	 *      <RollingFile name="WEB_LOG_FILE" fileName="E:/logs/web/web.log" filePattern="E:/logs/web/web.%d{yyyy-MM-dd}.%i.log">
	 *     	 	<PatternLayout pattern="%msg%n" charset="UTF-8"/>
	 *     		 <Policies>
	 *      		<SizeBasedTriggeringPolicy size="500MB"/>
	 *          	<TimeBasedTriggeringPolicy interval="1" modulate="true"/>
	 *      		</Policies>
	 *     		 <DefaultRolloverStrategy max="30">
	 *      		<!-- totalSizeCap 在 Log4j2 中需通过自定义删除动作实现 -->
	 *          	<Delete basePath="E:/logs/web" maxDepth="1">
	 *          	<IfFileName glob="web.*.log"/>
	 *              <IfAccumulatedFileSize exceeds="10GB"/>
	 *            </Delete>
	 *       	  </DefaultRolloverStrategy>
	 *      </RollingFile>
	 * </Appenders>
	 *
	 * <Loggers>
	 * 		<!-- 绑定专用 Logger -->
	 *      <Logger name="WebLogLogger" level="info" additivity="false">
	 *      	<AppenderRef ref="WEB_LOG_FILE"/>
	 *      </Logger>
	 *     </Loggers>
	 * }</pre>
	 *
	 * <p>log4j2 yaml配置示例</p>
	 * <pre>{@code
	 *   Appenders:
	 *     RollingFile:
	 *       - name: webLogFile
	 *         fileName: E:/logs/web/web.log
	 *         filePattern: E:/logs/web/web.%d{yyyy-MM-dd}.%i.log
	 *         PatternLayout:
	 *           pattern: "%msg%n"
	 *           charset: UTF-8
	 *         Policies:
	 *           SizeBasedTriggeringPolicy:
	 *             size: 500MB
	 *           TimeBasedTriggeringPolicy:
	 *             interval: 1
	 *             modulate: true
	 *         DefaultRolloverStrategy:
	 *           max: 30
	 *           Delete:
	 *             basePath: E:/logs/web/
	 *             maxDepth: 1
	 *             IfFileName:
	 *               glob: "web.*.log"
	 *             IfAccumulatedFileSize:
	 *               exceeds: 10GB
	 *  Loggers:
	 *    Logger:
	 *      - name: WebLogLogger
	 *        level: info
	 *        additivity: false
	 *        AppenderRef:
	 *          - ref: webLogFile
	 * }</pre>
     *
     * @author pangju666
     * @since 1.0.0
     */
    public static class Slf4j {
		private String logger;

		public String getLogger() {
			return logger;
		}

		public void setLogger(String logger) {
			this.logger = logger;
		}
	}

	/**
	 * 请求记录配置。
	 *
	 * <p>定义需要采集的 HTTP 请求数据范围与条件，包括请求头、查询参数、请求体、
	 * Multipart 数据，以及可接受的请求体媒体类型集合（字符串形式）。</p>
	 *
	 * <p><b>示例（application.yml）</b></p>
	 * <pre>
	 * pangju:
	 *   web:
	 *     log:
	 *       request:
	 *         headers: true
	 *         query-params: true
	 *         body: true
	 *         multipart: true
	 *         acceptable-media-types:
	 *           - application/json
	 *           - text/plain
	 * </pre>
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
		 * <p><b>默认值</b></p>
		 * <ul>
		 *   <li>{@code text/plain}（{@link MediaType#TEXT_PLAIN_VALUE}）</li>
		 *   <li>{@code application/json}（{@link MediaType#APPLICATION_JSON_VALUE}）</li>
		 * </ul>
		 *
		 * <p><b>示例（application.yml）</b></p>
		 * <pre>
		 * pangju:
		 *   web:
		 *     log:
		 *       request:
		 *         acceptable-media-types:
		 *           - application/json
		 *           - text/plain
		 * </pre>
		 *
		 * @since 1.0.0
		 */
		private Set<String> acceptableMediaTypes = Set.of(MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE);

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
	 * 响应记录配置。
	 *
	 * <p>定义需要采集的 HTTP 响应数据范围与条件，包括响应头、响应体、
	 * 统一结果结构（Result）的附加数据，以及可接受的响应体媒体类型集合（字符串形式）。</p>
	 *
	 * <p><b>示例（application.yml）</b></p>
	 * <pre>
	 * pangju:
	 *   web:
	 *     log:
	 *       response:
	 *         headers: true
	 *         body: true
	 *         result-data: true
	 *         acceptable-media-types:
	 *           - application/json
	 *           - text/plain
	 * </pre>
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
		 * 默认值为 {@code true}。仅在响应内容为统一结果封装类型时可用。
		 * </p>
		 *
		 * @since 1.0.0
		 */
		private boolean resultData = true;
		/**
		 * 允许采集的响应体媒体类型集合。
		 *
		 * <p>仅当响应的 {@code Content-Type} 属于该集合，且 {@link #body} 开关为真时，才记录响应体。</p>
		 *
		 * <p><b>默认值</b></p>
		 * <ul>
		 *   <li>{@code text/plain}（{@link MediaType#TEXT_PLAIN_VALUE}）</li>
		 *   <li>{@code application/json}（{@link MediaType#APPLICATION_JSON_VALUE}）</li>
		 * </ul>
		 *
		 * <p><b>示例（application.yml）</b></p>
		 * <pre>
		 * pangju:
		 *   web:
		 *     log:
		 *       response:
		 *         acceptable-media-types:
		 *           - application/json
		 *           - text/plain
		 * </pre>
		 *
		 * @since 1.0.0
		 */
		private Set<String> acceptableMediaTypes = Set.of(MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE);

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
