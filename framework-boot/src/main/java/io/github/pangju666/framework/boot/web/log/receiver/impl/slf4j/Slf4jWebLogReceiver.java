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

package io.github.pangju666.framework.boot.web.log.receiver.impl.slf4j;

import com.google.gson.Gson;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 SLF4J 的 Web 日志接收器。
 *
 * <p>概述：将采集到的 {@link io.github.pangju666.framework.boot.web.log.model.WebLog} 以 JSON 格式写入指定的 Logger。</p>
 * <p>实现：使用 {@link com.google.gson.Gson} 进行美化且不转义的序列化，日志级别为 INFO。</p>
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
public class Slf4jWebLogReceiver implements WebLogReceiver {
	/**
	 * 目标日志记录器。
	 *
	 * <p>用于写入格式化后的 WebLog 文本。</p>
	 *
	 * @since 1.0.0
	 */
	private final Logger logger;
	/**
	 * JSON 序列化器。
	 *
	 * <p>开启 pretty printing 与禁用 HTML 转义，用于输出可读的日志文本。</p>
	 *
	 * @since 1.0.0
	 */
	private final Gson gson;

	/**
	 * 使用指定日志器名称构造接收器。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code loggerName} 为空，可能导致日志器创建失败；该参数应由配置保证有效。</p>
	 *
	 * @param loggerName 目标 Logger 名称
	 * @since 1.0.0
	 */
	public Slf4jWebLogReceiver(String loggerName) {
		this.gson = JsonUtils.createGsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	/**
	 * 接收并写入 Web 日志。
	 *
	 * <p>行为：当 {@code webLog} 非空时，序列化为 JSON 并以 INFO 级别写入；为空则忽略。</p>
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code webLog} 为空，则不进行写入。</p>
	 *
	 * @param webLog Web 日志对象
	 * @since 1.0.0
	 */
	@Override
	public void receive(WebLog webLog) {
		if (ObjectUtils.allNotNull(logger, webLog)) {
			logger.info(JsonUtils.toString(webLog, gson));
		}
	}
}
