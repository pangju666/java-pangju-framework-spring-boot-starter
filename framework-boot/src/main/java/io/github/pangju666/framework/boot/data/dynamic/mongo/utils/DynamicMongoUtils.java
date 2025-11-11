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

package io.github.pangju666.framework.boot.data.dynamic.mongo.utils;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * 动态MongoDB工具类
 * <p>
 * 提供用于生成和获取动态MongoDB Bean名称的工具方法。
 * 用于在运行时动态注册和访问多个MongoDB连接相关的Bean。
 * </p>
 * <p>
 * 支持的Bean类型：
 * <ul>
 *     <li>{@link MongoClient} - MongoDB客户端实例</li>
 *     <li>{@link MongoDatabaseFactory} - MongoDB数据库工厂</li>
 *     <li>{@link MongoTemplate} - MongoDB操作模板</li>
 *     <li>{@link GridFsTemplate} - MongoDB GridFS操作模板</li>
 * </ul>
 * </p>
 * <p>
 * Bean命名规则：{name}{BeanType}
 * <ul>
 *     <li>{name}MongoClient</li>
 *     <li>{name}MongoDatabaseFactory</li>
 *     <li>{name}MongoTemplate</li>
 *     <li>{name}GridFsTemplate</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DynamicMongoUtils {
	/**
	 * MongoDB客户端Bean名称模板
	 * <p>
	 * 格式为：{name}MongoClient
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String CLIENT_BEAN_NAME_TEMPLATE = "%sMongoClient";
	/**
	 * MongoDB数据库工厂Bean名称模板
	 * <p>
	 * 格式为：{name}MongoDatabaseFactory
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String DATABASE_FACTORY_BEAN_NAME_TEMPLATE = "%sMongoDatabaseFactory";
	/**
	 * MongoDB操作模板Bean名称模板
	 * <p>
	 * 格式为：{name}MongoTemplate
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sMongoTemplate";
	/**
	 * MongoDB GridFS操作模板Bean名称模板
	 * <p>
	 * 格式为：{name}GridFsTemplate
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE = "%sGridFsTemplate";

	protected DynamicMongoUtils() {
	}

	/**
	 * 根据数据源名称获取MongoDB客户端Bean名称
	 *
	 * @param name 数据源名称
	 * @return MongoDB客户端Bean名称
	 * @since 1.0.0
	 */
	public static String getMongoClientBeanName(String name) {
		return CLIENT_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 从Bean工厂中获取指定名称的MongoDB客户端
	 *
	 * @param name        数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return MongoDB客户端实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static MongoClient getMongoClient(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CLIENT_BEAN_NAME_TEMPLATE.formatted(name), MongoClient.class);
	}

	/**
	 * 根据数据源名称获取MongoDB数据库工厂Bean名称
	 *
	 * @param name 数据源名称
	 * @return MongoDB数据库工厂Bean名称
	 * @since 1.0.0
	 */
	public static String getMongoDatabaseFactoryBeanName(String name) {
		return DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 从Bean工厂中获取指定名称的MongoDB数据库工厂
	 *
	 * @param name        数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return MongoDB数据库工厂实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static MongoDatabaseFactory getMongoDatabaseFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), MongoDatabaseFactory.class);
	}

	/**
	 * 根据数据源名称获取MongoDB操作模板Bean名称
	 *
	 * @param name 数据源名称
	 * @return MongoDB操作模板Bean名称
	 * @since 1.0.0
	 */
	public static String getMongoTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 从Bean工厂中获取指定名称的MongoDB操作模板
	 *
	 * @param name        数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return MongoDB操作模板实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static MongoTemplate getMongoTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), MongoTemplate.class);
	}

	/**
	 * 根据数据源名称获取MongoDB GridFS操作模板Bean名称
	 *
	 * @param name 数据源名称
	 * @return MongoDB GridFS操作模板Bean名称
	 * @since 1.0.0
	 */
	public static String getGridFsTemplateBeanName(String name) {
		return GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	/**
	 * 从Bean工厂中获取指定名称的MongoDB GridFS操作模板
	 *
	 * @param name        数据源名称
	 * @param beanFactory Spring Bean工厂
	 * @return MongoDB GridFS操作模板实例
	 * @throws NoSuchBeanDefinitionException 当指定名称的Bean不存在时抛出
	 * @since 1.0.0
	 */
	public static GridFsTemplate getGridFsTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), GridFsTemplate.class);
	}
}
