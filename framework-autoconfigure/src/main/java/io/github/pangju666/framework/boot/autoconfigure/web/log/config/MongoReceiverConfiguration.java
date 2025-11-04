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

package io.github.pangju666.framework.boot.autoconfigure.web.log.config;

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.boot.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.revceiver.impl.MongoWebLogReceiver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

/**
 * MongoDB 日志接收器自动配置类
 * <p>
 * 该类用于自动配置基于 MongoDB 的 Web 日志接收器 {@link MongoWebLogReceiver}。
 * 当应用环境满足相关条件时（如启用 Web 日志功能、存在 MongoTemplate Bean 等），
 * 自动注册 MongoDB 日志接收器，实现日志的持久化存储。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>自动注册 {@link MongoWebLogReceiver}，用于将 Web 日志存储到 MongoDB。</li>
 *     <li>通过配置和条件注解控制组件的动态加载，保证在合适的环境下启用 MongoDB 日志接收功能。</li>
 * </ul>
 *
 * <p>配置条件：</p>
 * <ul>
 *     <li>要求类路径中存在 {@link MongoClient} 和 {@link MongoTemplate} 类。</li>
 *     <li>未定义其他类型的 {@link WebLogReceiver} Bean（受 {@link ConditionalOnMissingBean} 限制）。</li>
 *     <li>存在 {@link MongoTemplate} Bean（受 {@link ConditionalOnBean} 限制）。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>需要将应用中 Web 请求和响应日志存储到 MongoDB。</li>
 *     <li>结合 Spring Data MongoDB 进行日志的高效查询、分类管理和分析。</li>
 * </ul>
 *
 * <p>关键配置项：</p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true                 # 是否启用 Web 日志功能（默认启用）
 *       mongo:
 *         mongo-template-bean-name:  # 指定使用的 MongoTemplate Bean 名称（可选）
 *         collection-prefix: web_log  # MongoDB 集合前缀
 * </pre>
 *
 * @author pangju666
 * @see MongoWebLogReceiver
 * @see MongoTemplate
 * @see WebLogReceiver
 * @since 1.0.0
 */
@AutoConfiguration(after = MongoDataAutoConfiguration.class)
@ConditionalOnClass({MongoClient.class, MongoTemplate.class})
public class MongoReceiverConfiguration {
	/**
	 * 注册 MongoDB Web 日志接收器
	 * <p>
	 * 自动注册 {@link MongoWebLogReceiver}，用于将 Web 请求与响应日志存储到 MongoDB。
	 * 仅在满足所有条件注解时生效，例如启用了 Web 日志功能、存在 MongoTemplate Bean 等。
	 * </p>
	 *
	 * @param properties  Web 日志属性配置 {@link WebLogProperties}
	 * @param beanFactory Spring Bean 工厂，用于动态获取 MongoTemplate Bean
	 * @return MongoDB Web 日志接收器 {@link MongoWebLogReceiver}
	 * @since 1.0.0
	 */
	@ConditionalOnMissingBean(WebLogReceiver.class)
	@ConditionalOnBean(MongoTemplate.class)
	@Bean
	public WebLogReceiver mongoWebLogReceiver(WebLogProperties properties, BeanFactory beanFactory) {
		MongoTemplate mongoTemplate;
		if (StringUtils.hasText(properties.getMongo().getMongoTemplateBeanName())) {
			mongoTemplate = beanFactory.getBean(properties.getMongo().getMongoTemplateBeanName(), MongoTemplate.class);
		} else {
			mongoTemplate = beanFactory.getBean(MongoTemplate.class);
		}
		return new MongoWebLogReceiver(mongoTemplate, properties.getMongo().getCollectionPrefix());
	}
}
