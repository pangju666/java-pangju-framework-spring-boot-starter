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

package io.github.pangju666.framework.boot.data.dynamic.mongo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;
import java.util.Objects;

/**
 * 动态 MongoRepository 工厂 Bean
 * <p>
 * 基于仓库接口上的 {@link DynamicMongo} 注解，在运行时选择对应数据源的
 * {@link MongoOperations}（即不同名称的 {@code MongoTemplate}），从而支持多数据源仓库。
 * 未标注注解时回退到默认数据源行为。
 * </p>
 *
 * @author pangju666
 * @see DynamicMongo
 * @see DynamicMongoUtils
 * @see MongoRepositoryFactoryBean
 * @see MongoRepositoryFactory
 * @since 1.0.0
 */
public class DynamicMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
	extends MongoRepositoryFactoryBean<T, S, ID> implements BeanFactoryAware {
	/**
	 * 仓库接口类型
	 * <p>
	 * 用于读取 {@link DynamicMongo} 注解以确定数据源。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final Class<? extends T> repositoryInterface;
	/**
	 * Spring BeanFactory 引用
	 *
	 * @since 1.0.0
	 */
	private BeanFactory beanFactory;

	/**
	 * 构造函数
	 * <p>
	 * 记录仓库接口类型，并传递给父类初始化。
	 * </p>
	 *
	 * @param repositoryInterface 仓库接口类型
	 * @since 1.0.0
	 */
	public DynamicMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
		this.repositoryInterface = repositoryInterface;
	}

	/**
	 * 注入 BeanFactory 引用
	 *
	 * @param beanFactory BeanFactory
	 * @since 1.0.0
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * 获取仓库工厂实例
	 * <p>
	 * 若仓库接口标注了 {@link DynamicMongo}，则根据注解值解析对应的
	 * {@code MongoTemplate} Bean 名称，并获取其 {@link MongoOperations}
	 * 创建 {@link MongoRepositoryFactory}；否则使用默认的 {@link MongoOperations}
	 * 回退到父类实现。
	 * </p>
	 *
	 * @param operations 默认的 MongoOperations（主数据源）
	 * @return 仓库工厂实例
	 * @throws IllegalStateException 当指定数据源的 {@code MongoOperations} 未注册或类型不匹配
	 * @since 1.0.0
	 */
	@Override
	protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
		DynamicMongo annotation = repositoryInterface.getAnnotation(DynamicMongo.class);
		if (Objects.isNull(annotation)) {
			return super.getFactoryInstance(operations);
		}
		String beanName = DynamicMongoUtils.getMongoTemplateBeanName(annotation.value());
		try {
			MongoOperations dynamicMongoOperations = beanFactory.getBean(beanName, MongoOperations.class);
			return new MongoRepositoryFactory(dynamicMongoOperations);
		} catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
			throw new IllegalStateException("No MongoOperations registered for store: " + beanName);
		}
	}
}
