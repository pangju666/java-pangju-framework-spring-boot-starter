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

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import io.github.pangju666.framework.boot.web.log.revceiver.impl.mongo.MongoWebLogReceiver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

/**
 * 基于 MongoDB 的 Web 日志接收器自动配置。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在满足条件时自动注册 {@link MongoWebLogReceiver}，用于持久化存储 Web 日志。</li>
 *   <li>与 Spring Data MongoDB 集成，支持高效查询与按前缀归档存储。</li>
 * </ul>
 *
 * <p><b>条件</b></p>
 * <ul>
 *   <li>在 {@link MongoDataAutoConfiguration} 之后加载。</li>
 *   <li>类路径存在 {@link MongoClient} 与 {@link MongoTemplate}。</li>
 *   <li>容器中存在 {@link MongoTemplate}，且当前未存在 {@link WebLogReceiver} Bean。</li>
 * </ul>
 *
 * <p><b>关键配置</b></p>
 * <pre>
 * pangju:
 *   web:
 *     log:
 *       enabled: true
 *       mongo:
 *         mongo-template-ref: myMongoTemplate  # 可选，指定 MongoTemplate Bean 名称
 *         collection-prefix: web_log           # 集合名前缀（实际集合名可按日派生）
 * </pre>
 *
 * <p><b>说明</b></p>
 * <ul>
 *   <li>若未指定 {@code mongo-template-ref}，将回退到容器默认的 {@link MongoTemplate}。</li>
 *   <li>集合命名通常按格式 {@code [prefix-]yyyy-MM-dd} 进行日级归档。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogProperties
 * @see MongoWebLogReceiver
 * @see MongoTemplate
 * @see WebLogReceiver
 * @since 1.0.0
 */
@AutoConfiguration(after = MongoDataAutoConfiguration.class)
@ConditionalOnBooleanProperty(prefix = "pangju.web.log", name = "enabled")
@ConditionalOnClass({MongoClient.class, MongoTemplate.class})
class MongoReceiverConfiguration {
    /**
     * 注册 MongoDB Web 日志接收器。
     *
     * <p><b>条件</b></p>
     * <ul>
     *   <li>容器中存在 {@link MongoTemplate}。</li>
     *   <li>当前未存在 {@link WebLogReceiver} Bean。</li>
     * </ul>
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>优先按 {@code mongo-template-ref} 指定的 Bean 名称获取 {@link MongoTemplate}；否则回退默认 Bean。</li>
     *   <li>使用配置的集合前缀创建 {@link MongoWebLogReceiver} 实例。</li>
     * </ul>
     *
     * @param properties  Web 日志属性配置
     * @param beanFactory BeanFactory，用于按名称或类型获取 {@link MongoTemplate}
     * @return 接收器实例
     * @since 1.0.0
     */
	@ConditionalOnMissingBean(WebLogReceiver.class)
	@ConditionalOnBean(MongoTemplate.class)
	@Bean
	public WebLogReceiver mongoWebLogReceiver(WebLogProperties properties, BeanFactory beanFactory) {
		MongoTemplate mongoTemplate;
		if (StringUtils.hasText(properties.getMongo().getMongoTemplateRef())) {
			mongoTemplate = beanFactory.getBean(properties.getMongo().getMongoTemplateRef(), MongoTemplate.class);
		} else {
			mongoTemplate = beanFactory.getBean(MongoTemplate.class);
		}
		return new MongoWebLogReceiver(mongoTemplate, properties.getMongo().getCollectionPrefix());
	}
}
