package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo;

import io.github.pangju666.framework.boot.data.dynamic.mongo.factory.DynamicMongoRepositoryFactoryBean;
import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

class DynamicMongoRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableMongoRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableMongoRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new MongoRepositoryConfigurationExtension();
	}

	@EnableMongoRepositories(repositoryFactoryBeanClass = DynamicMongoRepositoryFactoryBean.class,
		repositoryBaseClass = SimpleBaseMongoRepository.class)
	private static final class EnableMongoRepositoriesConfiguration {
	}
}
