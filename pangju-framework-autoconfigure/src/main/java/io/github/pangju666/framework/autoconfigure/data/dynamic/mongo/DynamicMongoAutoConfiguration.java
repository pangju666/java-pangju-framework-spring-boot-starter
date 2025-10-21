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

package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo;

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.annotation.DynamicMongo;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.processor.DynamicMongoBeanPostProcessor;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils.DynamicMongoUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * 动态MongoDB自动配置类
 * <p>
 * 该类用于在Spring Boot应用启动时自动配置多数据源MongoDB功能。
 * 通过导入{@link DynamicMongoRegistrar}，实现动态注册多个MongoDB连接相关的Bean。
 * 同时注册{@link DynamicMongoBeanPostProcessor}用于处理Repository的数据源注入。
 * </p>
 * <p>
 * 配置优先级：
 * <ul>
 *     <li>在{@code org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration}之前加载</li>
 *     <li>确保在Spring Data MongoDB的自动配置前完成多数据源设置</li>
 * </ul>
 * </p>
 * <p>
 * 配置条件：
 * <ul>
 *     <li>Classpath中必须存在{@link MongoClient}类（即MongoDB驱动库）</li>
 *     <li>Classpath中必须存在{@link MongoTemplate}类（即Spring Data MongoDB库）</li>
 *     <li>Classpath中必须存在{@link GridFsTemplate}类（GridFS支持）</li>
 * </ul>
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>启用{@link DynamicMongoProperties}配置属性的支持</li>
 *     <li>导入{@link DynamicMongoRegistrar}以实现动态Bean注册</li>
 *     <li>创建{@link DynamicMongoBeanPostProcessor} Bean用于处理Repository</li>
 *     <li>支持多个MongoDB数据源的配置和管理</li>
 *     <li>自动选择主数据源</li>
 * </ul>
 * </p>
 * <p>
 * 组件协作：
 * <ul>
 *     <li>{@link DynamicMongoProperties} - 配置属性类</li>
 *     <li>{@link DynamicMongoRegistrar} - Bean动态注册器</li>
 *     <li>{@link DynamicMongoBeanPostProcessor} - Repository处理器</li>
 *     <li>{@link DynamicMongoUtils} - 工具类</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DynamicMongoProperties
 * @see DynamicMongoRegistrar
 * @see DynamicMongoBeanPostProcessor
 * @see DynamicMongoUtils
 * @see MongoAutoConfiguration
 * @since 1.0.0
 */
@AutoConfiguration(before = MongoAutoConfiguration.class)
@ConditionalOnClass({MongoClient.class, MongoTemplate.class, GridFsTemplate.class})
@EnableConfigurationProperties(DynamicMongoProperties.class)
@Import(DynamicMongoRegistrar.class)
public class DynamicMongoAutoConfiguration {
	/**
	 * 创建动态MongoDB Bean后处理器
	 * <p>
	 * 该Bean用于处理标注了{@link DynamicMongo}注解的Repository实例。
	 * 自动为这些Repository注入对应的{@link MongoTemplate}。
	 * </p>
	 *
	 * @return 动态MongoDB Bean后处理器实例
	 * @see DynamicMongoBeanPostProcessor
	 * @since 1.0.0
	 */
	@Bean
	public DynamicMongoBeanPostProcessor dynamicMongoDataBaseBeanPostProcessor() {
		return new DynamicMongoBeanPostProcessor();
	}
}
