package io.github.pangju666.framework.autoconfigure.data.mongo.registrar;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.autoconfigure.data.mongo.factory.GridFsMongoDatabaseFactory;
import io.github.pangju666.framework.autoconfigure.data.mongo.properties.DynamicMongoProperties;
import io.github.pangju666.framework.autoconfigure.data.mongo.utils.DynamicMongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.mongo.*;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DynamicMongoRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {
	private static final Logger log = LoggerFactory.getLogger(DynamicMongoRegistrar.class);

	private Binder binder;

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
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
			log.error("配置动态MongoDB数据源失败，请检查是否存在相关配置");
			return;
		}

		Map<String, MongoProperties> mongoDatabases = dynamicMongoProperties.getDatabases();
		if (!CollectionUtils.isEmpty(mongoDatabases)) {
			mongoDatabases.forEach((name, properties) -> {
				// Mongo
				MongoConnectionDetails connectionDetails = new PropertiesMongoConnectionDetails(properties);
				MongoClientSettings clientSettings = MongoClientSettings.builder().build();
				MongoClientSettingsBuilderCustomizer settingsBuilderCustomizer = new StandardMongoClientSettingsBuilderCustomizer(
					connectionDetails.getConnectionString(), properties.getUuidRepresentation(), properties.getSsl(), null
				);
				MongoClient client = new MongoClientFactory(Collections.singletonList(settingsBuilderCustomizer)).createMongoClient(clientSettings);

				BeanDefinitionBuilder connectionDetailsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoConnectionDetails.class, () -> connectionDetails);
				AbstractBeanDefinition connectionDetailsBeanDefinition = connectionDetailsBeanBuilder.getRawBeanDefinition();
				String connectionDetailsBeanName = DynamicMongoUtils.getMongoConnectionDetailsBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(connectionDetailsBeanName, connectionDetailsBeanDefinition);

				BeanDefinitionBuilder clientSettingsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoClientSettings.class, () -> clientSettings);
				AbstractBeanDefinition clientSettingsBeanDefinition = clientSettingsBeanBuilder.getRawBeanDefinition();
				String clientSettingsBeanName = DynamicMongoUtils.getMongoClientSettingsBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(clientSettingsBeanName, clientSettingsBeanDefinition);

				BeanDefinitionBuilder clientBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoClient.class, () -> client);
				AbstractBeanDefinition clientBeanDefinition = clientBeanBuilder.getRawBeanDefinition();
				String clientBeanName = DynamicMongoUtils.getMongoClientBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(clientBeanName, clientBeanDefinition);

				// Mongo-Data
				MongoCustomConversions customConversions = new MongoCustomConversions(Collections.emptyList());
				MongoMappingContext mappingContext = createMongoMappingContext(properties, customConversions);
				MongoDatabaseFactorySupport<MongoClient> databaseFactorySupport = createMongoDatabaseFactory(client, properties, connectionDetails);
				MappingMongoConverter converter = createMappingMongoConverter(databaseFactorySupport, mappingContext, customConversions);
				MongoTemplate template = new MongoTemplate(databaseFactorySupport, converter);

				BeanDefinitionBuilder customConversionsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoCustomConversions.class, () -> customConversions);
				AbstractBeanDefinition customConversionsBeanDefinition = customConversionsBeanBuilder.getRawBeanDefinition();
				String customConversionsBeanName = DynamicMongoUtils.getMongoCustomConversionsBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(customConversionsBeanName, customConversionsBeanDefinition);

				BeanDefinitionBuilder mappingContextBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoMappingContext.class, () -> mappingContext);
				AbstractBeanDefinition mappingContextBeanDefinition = mappingContextBeanBuilder.getRawBeanDefinition();
				String mappingContextBeanName = DynamicMongoUtils.getMongoMappingContextBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(mappingContextBeanName, mappingContextBeanDefinition);

				BeanDefinitionBuilder databaseFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoDatabaseFactorySupport.class, () -> databaseFactorySupport);
				AbstractBeanDefinition databaseFactoryBeanDefinition = databaseFactoryBeanBuilder.getRawBeanDefinition();
				String databaseFactoryBeanName = DynamicMongoUtils.getMongoDatabaseFactorySupportBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(databaseFactoryBeanName, databaseFactoryBeanDefinition);

				BeanDefinitionBuilder mappingMongoConverterBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MappingMongoConverter.class, () -> converter);
				AbstractBeanDefinition mappingMongoConverterBeanDefinition = mappingMongoConverterBeanBuilder.getRawBeanDefinition();
				String mappingMongoConverterBeanName = DynamicMongoUtils.getMappingMongoConverterBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(mappingMongoConverterBeanName, mappingMongoConverterBeanDefinition);

				BeanDefinitionBuilder templateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(MongoTemplate.class, () -> template);
				AbstractBeanDefinition templateBeanDefinition = templateBeanBuilder.getRawBeanDefinition();
				String templateBeanName = DynamicMongoUtils.getMongoTemplateBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(templateBeanName, templateBeanDefinition);

				// GridFs
				GridFsMongoDatabaseFactory gridFsMongoDatabaseFactory = new GridFsMongoDatabaseFactory(databaseFactorySupport, connectionDetails);
				GridFsTemplate gridFsTemplate = new GridFsTemplate(gridFsMongoDatabaseFactory, template.getConverter(), (connectionDetails.getGridFs() != null) ? connectionDetails.getGridFs().getBucket() : null);

				BeanDefinitionBuilder gridFsTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(GridFsTemplate.class, () -> gridFsTemplate);
				AbstractBeanDefinition gridFsTemplateBeanDefinition = gridFsTemplateBeanBuilder.getRawBeanDefinition();
				String gridFsTemplateBeanName = DynamicMongoUtils.getGridFsTemplateBeanName(name);
				beanDefinitionRegistry.registerBeanDefinition(gridFsTemplateBeanName, gridFsTemplateBeanDefinition);

				if (dynamicMongoProperties.getPrimary().equals(name)) {
					clientSettingsBeanDefinition.setPrimary(true);
					clientBeanDefinition.setPrimary(true);
					databaseFactoryBeanDefinition.setPrimary(true);
					customConversionsBeanDefinition.setPrimary(true);
					mappingContextBeanDefinition.setPrimary(true);
					templateBeanDefinition.setPrimary(true);
					mappingMongoConverterBeanDefinition.setPrimary(true);
					gridFsTemplateBeanDefinition.setPrimary(true);
				}
				log.info("dynamic-mongodb - add a database named [{}] success", name);
			});
			log.info("dynamic-mongodb initial loaded [{}] database,primary database named [{}]", mongoDatabases.size(), dynamicMongoProperties.getPrimary());
		}
	}

	private MongoMappingContext createMongoMappingContext(MongoProperties properties, MongoCustomConversions conversions) {
		PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
		MongoMappingContext context = new MongoMappingContext();
		mapper.from(properties.isAutoIndexCreation()).to(context::setAutoIndexCreation);
		Class<?> strategyClass = properties.getFieldNamingStrategy();
		if (strategyClass != null) {
			context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
		}
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		return context;
	}

	private MappingMongoConverter createMappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context, MongoCustomConversions conversions) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
		mappingConverter.setCustomConversions(conversions);
		return mappingConverter;
	}

	private MongoDatabaseFactorySupport<MongoClient> createMongoDatabaseFactory(MongoClient mongoClient,
																				MongoProperties properties,
																				MongoConnectionDetails connectionDetails) {
		String database = properties.getDatabase();
        if (Objects.isNull(database)) {
			database = connectionDetails.getConnectionString().getDatabase();
		}
		return new SimpleMongoClientDatabaseFactory(mongoClient, database);
	}
}
