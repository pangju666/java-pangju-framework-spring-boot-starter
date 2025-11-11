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

package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo;

import io.github.pangju666.framework.boot.data.dynamic.mongo.DynamicMongoRepositoryFactoryBean;
import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * 动态 Mongo 仓库启用注册器
 * <p>
 * 通过继承 {@link AbstractRepositoryConfigurationSourceSupport}，指定用于启用仓库的注解、
 * 配置类与仓库配置扩展。在启用 Mongo 仓库时，使用自定义的
 * {@link DynamicMongoRepositoryFactoryBean} 以支持多数据源仓库创建。
 * </p>
 *
 * @author pangju666
 * @see EnableMongoRepositories
 * @see MongoRepositoryConfigurationExtension
 * @see DynamicMongoRepositoryFactoryBean
 * @since 1.0.0
 */
class DynamicMongoRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
	/**
	 * 返回用于启用仓库扫描的注解类型
	 *
	 * @return 注解类型（{@link EnableMongoRepositories}）
	 * @since 1.0.0
	 */
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableMongoRepositories.class;
	}

	/**
	 * 返回用于启用仓库的配置类
	 *
	 * @return 配置类（内部配置类 {@link EnableMongoRepositoriesConfiguration}）
	 * @since 1.0.0
	 */
	@Override
	protected Class<?> getConfiguration() {
		return EnableMongoRepositoriesConfiguration.class;
	}

	/**
	 * 返回 Mongo 仓库配置扩展
	 *
	 * @return 仓库配置扩展（{@link MongoRepositoryConfigurationExtension}）
	 * @since 1.0.0
	 */
	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new MongoRepositoryConfigurationExtension();
	}

	/**
	 * 启用动态 Mongo 仓库的配置类
	 * <p>
	 * 指定基础仓库实现类为 {@link SimpleBaseMongoRepository}，并使用
	 * {@link DynamicMongoRepositoryFactoryBean} 作为仓库工厂 Bean，以支持在仓库层实现数据源切换。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	@EnableMongoRepositories(repositoryFactoryBeanClass = DynamicMongoRepositoryFactoryBean.class,
		repositoryBaseClass = SimpleBaseMongoRepository.class)
	private static final class EnableMongoRepositoriesConfiguration {
	}
}
