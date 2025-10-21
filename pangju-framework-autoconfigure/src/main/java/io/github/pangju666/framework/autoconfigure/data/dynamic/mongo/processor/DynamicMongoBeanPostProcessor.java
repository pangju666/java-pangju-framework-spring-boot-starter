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

package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.processor;

import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.DynamicMongoRegistrar;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.annotation.DynamicMongo;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils.DynamicMongoUtils;
import io.github.pangju666.framework.data.mongodb.repository.BaseRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Objects;

/**
 * 动态MongoDB Bean后处理器
 * <p>
 * 该类实现了Spring的{@link BeanPostProcessor}接口，
 * 用于在Bean初始化前处理标注了{@link DynamicMongo}注解的Repository实例。
 * 为这些Repository自动注入对应的{@link MongoTemplate}实例。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>扫描所有{@link BaseRepository}实例</li>
 *     <li>检查是否标注了{@link DynamicMongo}注解</li>
 *     <li>根据注解指定的数据源名称获取对应的{@link MongoTemplate}</li>
 *     <li>将{@link MongoTemplate}注入到Repository中</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <p>
 * 当应用使用多个MongoDB数据源时，可以为不同的Repository指定不同的数据源。
 * 通过{@link DynamicMongo}注解，框架会自动为这些Repository注入相应的MongoTemplate。
 * </p>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Repository
 * &#64;DynamicMongo("mongodb-secondary")
 * public class UserSecondaryRepository extends BaseRepository&lt;User, String&gt; {
 *     // Repository实现
 * }
 * </pre>
 * </p>
 * <p>
 * 处理流程：
 * <ol>
 *     <li>Spring容器在实例化Bean时，调用该后处理器的postProcessBeforeInitialization方法</li>
 *     <li>检查Bean是否为{@link BaseRepository}实例</li>
 *     <li>如果是，检查该Bean的类是否标注了{@link DynamicMongo}注解</li>
 *     <li>如果标注了注解，从Spring容器中获取注解指定的MongoTemplate</li>
 *     <li>调用{@link BaseRepository#setMongoOperations(MongoOperations)}方法注入MongoTemplate</li>
 * </ol>
 * </p>
 * <p>
 * 依赖关系：
 * <ul>
 *     <li>依赖于{@link DynamicMongoRegistrar}注册的MongoTemplate Bean</li>
 *     <li>依赖于{@link BaseRepository}的实现类</li>
 *     <li>依赖于{@link DynamicMongo}注解的标注</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see BeanPostProcessor
 * @see DynamicMongo
 * @see BaseRepository
 * @see MongoTemplate
 * @see DynamicMongoUtils
 * @since 1.0.0
 */
public class DynamicMongoBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
	/**
	 * Spring Bean工厂
	 * <p>
	 * 用于从Spring容器中获取MongoTemplate实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Bean初始化前处理
	 * <p>
	 * 该方法在Bean初始化前被调用。处理流程如下：
	 * </p>
	 * <ol>
	 *     <li>检查Bean是否为{@link BaseRepository}的实例</li>
	 *     <li>如果是Repository，检查其类是否标注了{@link DynamicMongo}注解</li>
	 *     <li>如果标注了注解，获取注解中指定的数据源名称</li>
	 *     <li>从Spring容器中获取对应数据源的{@link MongoTemplate}</li>
	 *     <li>调用{@link BaseRepository#setMongoOperations(MongoOperations)}方法注入MongoTemplate</li>
	 * </ol>
	 * <p>
	 * 如果没有标注{@link DynamicMongo}注解，则不进行任何处理，
	 * Repository将使用默认的MongoTemplate（通常是主数据源的MongoTemplate）。
	 * </p>
	 *
	 * @param bean 待处理的Bean实例
	 * @param beanName Bean的名称
	 * @return 处理后的Bean实例（通常返回原Bean实例）
	 * @throws BeansException Bean操作异常
	 * @since 1.0.0
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof BaseRepository<?, ?> baseRepository) {
			DynamicMongo annotation = bean.getClass().getAnnotation(DynamicMongo.class);
			if (Objects.nonNull(annotation)) {
				MongoTemplate mongoTemplate = DynamicMongoUtils.getMongoTemplate(annotation.value(), beanFactory);
				baseRepository.setMongoOperations(mongoTemplate);
			}
		}
		return bean;
	}
}