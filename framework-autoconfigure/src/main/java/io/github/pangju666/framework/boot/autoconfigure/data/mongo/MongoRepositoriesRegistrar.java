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

package io.github.pangju666.framework.boot.autoconfigure.data.mongo;

import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Mongo 仓库启用注册器
 * <p>
 * 通过继承 {@link AbstractRepositoryConfigurationSourceSupport}，指定用于启用仓库的注解、
 * 配置类与仓库配置扩展，从而在应用中启用 Spring Data Mongo 仓库支持。
 * </p>
 *
 * @author pangju666
 * @see EnableMongoRepositories
 * @see MongoRepositoryConfigurationExtension
 * @since 1.0.0
 */
class MongoRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
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
	 * 启用 Mongo 仓库的配置类
	 * <p>
	 * 指定基础仓库实现类为 {@link SimpleBaseMongoRepository}。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	@EnableMongoRepositories(repositoryBaseClass = SimpleBaseMongoRepository.class)
	private static final class EnableMongoRepositoriesConfiguration {
	}
}
