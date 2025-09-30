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

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils.DynamicMongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.mongo.*;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class DynamicMongoRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	private static final Logger log = LoggerFactory.getLogger(DynamicMongoRegistrar.class);

	private Binder binder;
	private BeanFactory beanFactory;

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		DynamicMongoProperties dynamicMongoProperties;
		try {
			dynamicMongoProperties = binder.bind(DynamicMongoProperties.PREFIX, DynamicMongoProperties.class).get();
			Assert.notEmpty(dynamicMongoProperties.getDatabases(), "动态MongoDB配置：数据源集合不可为空");
			Assert.hasText(dynamicMongoProperties.getPrimary(), "动态MongoDB配置：主数据源不可为空");
			if (!dynamicMongoProperties.getDatabases().containsKey(dynamicMongoProperties.getPrimary())) {
				throw new IllegalArgumentException("动态MongoDB配置：主数据源必须为存在的数据源");
			}
		} catch (NoSuchElementException e) {
			return;
		}

		Map<String, MongoProperties> mongoDatabases = dynamicMongoProperties.getDatabases();
		if (!CollectionUtils.isEmpty(mongoDatabases)) {
			mongoDatabases.forEach((name, mongoProperties) -> {
				// MongoConnectionDetails
				Supplier<MongoConnectionDetails> connectionDetailsSupplier = () -> new PropertiesMongoConnectionDetails(
					mongoProperties, beanFactory.getBeanProvider(SslBundles.class).getIfAvailable());
				String connectionDetailsBeanName = DynamicMongoUtils.getMongoConnectionDetailsBeanName(name);
				BeanDefinitionBuilder connectionDetailsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoConnectionDetails.class, connectionDetailsSupplier);
				AbstractBeanDefinition connectionDetailsBeanDefinition = connectionDetailsBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionDetailsBeanName, connectionDetailsBeanDefinition);

				// MongoClientSettings
				Supplier<MongoClientSettings> mongoClientSettingsSupplier = () -> MongoClientSettings.builder().build();
				String mongoClientSettingsBeanName = DynamicMongoUtils.getMongoClientSettingsBeanName(name);
				BeanDefinitionBuilder mongoClientSettingsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoClientSettings.class, mongoClientSettingsSupplier);
				AbstractBeanDefinition mongoClientSettingsBeanDefinition = mongoClientSettingsBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoClientSettingsBeanName, mongoClientSettingsBeanDefinition);

				// MongoClient
				Supplier<MongoClient> mongoClientSupplier = () -> {
					List<MongoClientSettingsBuilderCustomizer> mongoClientSettingsBuilderCustomizers = Collections.singletonList(
						new StandardMongoClientSettingsBuilderCustomizer(beanFactory.getBean(connectionDetailsBeanName,
							MongoConnectionDetails.class), mongoProperties.getUuidRepresentation()));
					return new MongoClientFactory(mongoClientSettingsBuilderCustomizers)
						.createMongoClient(beanFactory.getBean(mongoClientSettingsBeanName, MongoClientSettings.class));
				};
				String mongoClientBeanName = DynamicMongoUtils.getMongoClientBeanName(name);
				BeanDefinitionBuilder mongoClientBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoClient.class, mongoClientSupplier);
				mongoClientBeanBuilder.addDependsOn(connectionDetailsBeanName);
				mongoClientBeanBuilder.addDependsOn(mongoClientSettingsBeanName);
				AbstractBeanDefinition mongoClientBeanDefinition = mongoClientBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoClientBeanName, mongoClientBeanDefinition);

				// MongoCustomConversions
				Supplier<MongoCustomConversions> mongoCustomConversionsSupplier = () -> new MongoCustomConversions(
					Collections.emptyList());
				String mongoCustomConversionsBeanName = DynamicMongoUtils.getMongoCustomConversionsBeanName(name);
				BeanDefinitionBuilder mongoCustomConversionsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoCustomConversions.class, mongoCustomConversionsSupplier);
				AbstractBeanDefinition mongoCustomConversionsBeanDefinition = mongoCustomConversionsBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoCustomConversionsBeanName, mongoCustomConversionsBeanDefinition);

				// MongoMappingContext
				Supplier<MongoMappingContext> mongoMappingContextSupplier = () -> {
					PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
					MongoMappingContext context = new MongoMappingContext();
					map.from(mongoProperties.isAutoIndexCreation()).to(context::setAutoIndexCreation);
					context.setManagedTypes(beanFactory.getBean(MongoManagedTypes.class));
					Class<?> strategyClass = mongoProperties.getFieldNamingStrategy();
					if (strategyClass != null) {
						context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
					}
					context.setSimpleTypeHolder(beanFactory.getBean(mongoCustomConversionsBeanName,
						MongoCustomConversions.class).getSimpleTypeHolder());
					return context;
				};
				String mongoMappingContextBeanName = DynamicMongoUtils.getMongoMappingContextBeanName(name);
				BeanDefinitionBuilder mongoMappingContextBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoMappingContext.class, mongoMappingContextSupplier);
				mongoMappingContextBeanBuilder.addDependsOn(mongoCustomConversionsBeanName);
				//mongoMappingContextBeanBuilder.addDependsOn("mongoManagedTypes");
				AbstractBeanDefinition mongoMappingContextBeanDefinition = mongoMappingContextBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoMappingContextBeanName, mongoMappingContextBeanDefinition);

				// MongoDatabaseFactory
				Supplier<MongoDatabaseFactory> mongoDatabaseFactorySupplier = () -> {
					String database = mongoProperties.getDatabase();
					if (database == null) {
						database = beanFactory.getBean(connectionDetailsBeanName, MongoConnectionDetails.class)
							.getConnectionString().getDatabase();
					}
					return new SimpleMongoClientDatabaseFactory(beanFactory.getBean(mongoClientBeanName,
						MongoClient.class), database);
				};
				String mongoDatabaseFactoryBeanName = DynamicMongoUtils.getMongoDatabaseFactoryBeanName(name);
				BeanDefinitionBuilder mongoDatabaseFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoDatabaseFactory.class, mongoDatabaseFactorySupplier);
				mongoDatabaseFactoryBeanBuilder.addDependsOn(connectionDetailsBeanName);
				mongoDatabaseFactoryBeanBuilder.addDependsOn(mongoClientBeanName);
				AbstractBeanDefinition mongoDatabaseFactoryBeanDefinition = mongoDatabaseFactoryBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoDatabaseFactoryBeanName, mongoDatabaseFactoryBeanDefinition);

				// MongoConverter
				Supplier<MongoConverter> mongoConverterSupplier = () -> {
					MongoDatabaseFactory mongoDatabaseFactory = beanFactory.getBean(mongoDatabaseFactoryBeanName,
						MongoDatabaseFactory.class);
					DbRefResolver dbRefResolver = (mongoDatabaseFactory != null) ? new DefaultDbRefResolver(mongoDatabaseFactory)
						: NoOpDbRefResolver.INSTANCE;
					MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver,
						beanFactory.getBean(mongoMappingContextBeanName, MongoMappingContext.class));
					mappingConverter.setCustomConversions(beanFactory.getBean(mongoCustomConversionsBeanName,
						MongoCustomConversions.class));
					return mappingConverter;
				};
				String mongoConverterBeanName = DynamicMongoUtils.getMongoConverterBeanName(name);
				BeanDefinitionBuilder mongoConverterBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoConverter.class, mongoConverterSupplier);
				mongoConverterBeanBuilder.addDependsOn(mongoMappingContextBeanName);
				mongoConverterBeanBuilder.addDependsOn(mongoCustomConversionsBeanName);
				AbstractBeanDefinition mongoConverterBeanDefinition = mongoConverterBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoConverterBeanName, mongoConverterBeanDefinition);

				// MongoTemplate
				Supplier<MongoTemplate> mongoTemplateSupplier = () -> new MongoTemplate(
					beanFactory.getBean(mongoDatabaseFactoryBeanName, MongoDatabaseFactory.class),
					beanFactory.getBean(mongoConverterBeanName, MongoConverter.class));
				String mongoTemplateBeanName = DynamicMongoUtils.getMongoTemplateBeanName(name);
				BeanDefinitionBuilder mongoTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoTemplate.class, mongoTemplateSupplier);
				mongoTemplateBeanBuilder.addDependsOn(mongoDatabaseFactoryBeanName);
				mongoTemplateBeanBuilder.addDependsOn(mongoConverterBeanName);
				AbstractBeanDefinition mongoTemplateBeanDefinition = mongoTemplateBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoTemplateBeanName, mongoTemplateBeanDefinition);

				// GridFsTemplate
				Supplier<GridFsTemplate> gridFsTemplateSupplier = () -> {
					MongoConnectionDetails connectionDetails = beanFactory.getBean(connectionDetailsBeanName,
						MongoConnectionDetails.class);
					MongoDatabaseFactory databaseFactory = beanFactory.getBean(mongoDatabaseFactoryBeanName,
						MongoDatabaseFactory.class);
					GridFsMongoDatabaseFactory gridFsDatabaseFactory = new GridFsMongoDatabaseFactory(databaseFactory,
						connectionDetails);
					return new GridFsTemplate(gridFsDatabaseFactory,
						beanFactory.getBean(mongoTemplateBeanName, MongoTemplate.class).getConverter(),
						(connectionDetails.getGridFs() != null) ? connectionDetails.getGridFs().getBucket() : null);
				};
				String gridFsTemplateBeanName = DynamicMongoUtils.getGridFsTemplateBeanName(name);
				BeanDefinitionBuilder gridFsTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					GridFsTemplate.class, gridFsTemplateSupplier);
				gridFsTemplateBeanBuilder.addDependsOn(connectionDetailsBeanName);
				gridFsTemplateBeanBuilder.addDependsOn(mongoDatabaseFactoryBeanName);
				gridFsTemplateBeanBuilder.addDependsOn(mongoTemplateBeanName);
				AbstractBeanDefinition gridFsTemplateBeanDefinition = gridFsTemplateBeanBuilder.getRawBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(gridFsTemplateBeanName, gridFsTemplateBeanDefinition);

				if (dynamicMongoProperties.getPrimary().equals(name)) {
					connectionDetailsBeanDefinition.setPrimary(true);
					mongoClientSettingsBeanDefinition.setPrimary(true);
					mongoClientBeanDefinition.setPrimary(true);
					mongoCustomConversionsBeanDefinition.setPrimary(true);
					mongoMappingContextBeanDefinition.setPrimary(true);
					mongoConverterBeanDefinition.setPrimary(true);
					mongoDatabaseFactoryBeanDefinition.setPrimary(true);
					mongoTemplateBeanDefinition.setPrimary(true);
					//gridFsTemplateBeanDefinition.setPrimary(true);
				}
				log.info("dynamic-mongodb - add a database named [{}] success", name);
			});
			log.info("dynamic-mongodb initial loaded [{}] database,primary database named [{}]", mongoDatabases.size(),
				dynamicMongoProperties.getPrimary());
		}
	}

	static class GridFsMongoDatabaseFactory implements MongoDatabaseFactory {

		private final MongoDatabaseFactory mongoDatabaseFactory;

		private final MongoConnectionDetails connectionDetails;

		GridFsMongoDatabaseFactory(MongoDatabaseFactory mongoDatabaseFactory,
								   MongoConnectionDetails connectionDetails) {
			Assert.notNull(mongoDatabaseFactory, "'mongoDatabaseFactory' must not be null");
			Assert.notNull(connectionDetails, "'connectionDetails' must not be null");
			this.mongoDatabaseFactory = mongoDatabaseFactory;
			this.connectionDetails = connectionDetails;
		}

		@Override
		public MongoDatabase getMongoDatabase() throws DataAccessException {
			String gridFsDatabase = getGridFsDatabase(this.connectionDetails);
			if (StringUtils.hasText(gridFsDatabase)) {
				return this.mongoDatabaseFactory.getMongoDatabase(gridFsDatabase);
			}
			return this.mongoDatabaseFactory.getMongoDatabase();
		}

		@Override
		public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
			return this.mongoDatabaseFactory.getMongoDatabase(dbName);
		}

		@Override
		public PersistenceExceptionTranslator getExceptionTranslator() {
			return this.mongoDatabaseFactory.getExceptionTranslator();
		}

		@Override
		public ClientSession getSession(ClientSessionOptions options) {
			return this.mongoDatabaseFactory.getSession(options);
		}

		@Override
		public MongoDatabaseFactory withSession(ClientSession session) {
			return this.mongoDatabaseFactory.withSession(session);
		}

		private String getGridFsDatabase(MongoConnectionDetails connectionDetails) {
			return (connectionDetails.getGridFs() != null) ? connectionDetails.getGridFs().getDatabase() : null;
		}

	}
}
