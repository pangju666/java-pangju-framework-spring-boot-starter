package io.github.pangju666.framework.boot.data.dynamic.mongo.factory;

import io.github.pangju666.framework.boot.data.dynamic.mongo.annotation.DynamicMongo;
import io.github.pangju666.framework.boot.data.dynamic.mongo.utils.DynamicMongoUtils;
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

public class DynamicMongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
	extends MongoRepositoryFactoryBean<T, S, ID> implements BeanFactoryAware {
	private final Class<? extends T> repositoryInterface;
	private BeanFactory beanFactory;

	public DynamicMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
		this.repositoryInterface = repositoryInterface;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

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
