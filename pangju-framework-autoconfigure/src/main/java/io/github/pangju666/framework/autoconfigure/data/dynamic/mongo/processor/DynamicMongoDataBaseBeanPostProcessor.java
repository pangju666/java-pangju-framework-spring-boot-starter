package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.processor;

import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.annotation.DynamicMongoDataBase;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils.DynamicMongoUtils;
import io.github.pangju666.framework.data.mongodb.repository.BaseRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Objects;

public class DynamicMongoDataBaseBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof BaseRepository<?, ?> baseRepository) {
			DynamicMongoDataBase annotation = bean.getClass().getAnnotation(DynamicMongoDataBase.class);
			if (Objects.nonNull(annotation)) {
				MongoTemplate mongoTemplate = DynamicMongoUtils.getMongoTemplate(annotation.value(), beanFactory);
				baseRepository.setMongoOperations(mongoTemplate);
			}
		}
		return bean;
	}
}