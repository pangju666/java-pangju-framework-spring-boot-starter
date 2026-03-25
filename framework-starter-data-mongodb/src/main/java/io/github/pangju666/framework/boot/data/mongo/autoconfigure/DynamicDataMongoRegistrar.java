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

package io.github.pangju666.framework.boot.data.mongo.autoconfigure;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.boot.data.mongo.DynamicMongoUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.mongodb.autoconfigure.MongoClientFactory;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.boot.mongodb.autoconfigure.StandardMongoClientSettingsBuilderCustomizer;
import org.springframework.boot.persistence.autoconfigure.EntityScanner;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * 动态MongoDB Bean注册器
 * <p>
 * 该类实现了Spring的{@link ImportBeanDefinitionRegistrar}接口，
 * 用于在运行时动态注册多个MongoDB连接相关的Bean。
 * 支持多个MongoDB数据源的配置和管理，实现了MongoDB的完整自动配置链。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>解析{@link DynamicDataMongoProperties}配置属性</li>
 *     <li>为每个数据源注册{@link MongoConnectionDetails} Bean</li>
 *     <li>为每个数据源注册{@link MongoClientSettings} Bean</li>
 *     <li>为每个数据源注册{@link MongoClient} Bean</li>
 *     <li>为每个数据源注册{@link MongoCustomConversions} Bean</li>
 *     <li>为每个数据源注册{@link MongoMappingContext} Bean</li>
 *     <li>为每个数据源注册{@link MongoDatabaseFactory} Bean</li>
 *     <li>为每个数据源注册{@link MongoConverter} Bean</li>
 *     <li>为每个数据源注册{@link MongoTemplate} Bean</li>
 *     <li>为每个数据源注册{@link GridFsTemplate} Bean</li>
 *     <li>根据主数据源配置创建主Bean</li>
 * </ul>
 * </p>
 * <p>
 * Bean注册顺序和依赖关系：
 * <ol>
 *     <li>MongoConnectionDetails - 基础连接详情</li>
 *     <li>MongoClientSettings - 客户端设置</li>
 *     <li>MongoClient（依赖于1、2）- MongoDB客户端</li>
 *     <li>MongoMappingContext（依赖于4）- 映射上下文</li>
 *     <li>MongoDatabaseFactory（依赖于1、3）- 数据库工厂</li>
 *     <li>MongoConverter（依赖于5、4）- 数据转换器</li>
 *     <li>MongoTemplate（依赖于6、7）- 操作模板</li>
 *     <li>GridFsTemplate（依赖于1、6、8）- GridFS操作模板</li>
 * </ol>
 * </p>
 * <p>
 * Bean命名规则：
 * <ul>
 *     <li>{name}MongoConnectionDetails - MongoDB连接详情Bean</li>
 *     <li>{name}MongoClientSettings - MongoDB客户端设置Bean</li>
 *     <li>{name}MongoClient - MongoDB客户端Bean</li>
 *     <li>{name}MongoCustomConversions - MongoDB自定义类型转换Bean</li>
 *     <li>{name}MongoMappingContext - MongoDB映射上下文Bean</li>
 *     <li>{name}MongoDatabaseFactory - MongoDB数据库工厂Bean</li>
 *     <li>{name}MongoConverter - MongoDB数据转换器Bean</li>
 *     <li>{name}MongoTemplate - MongoDB操作模板Bean</li>
 *     <li>{name}GridFsTemplate - MongoDB GridFS操作模板Bean</li>
 *     <li>mongoTemplate - 主数据源对应的MongoTemplate Bean（primary=true）</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see DynamicDataMongoProperties
 * @see DynamicMongoUtils
 * @see DynamicDataMongoAutoConfiguration
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class DynamicDataMongoRegistrar implements EnvironmentAware, BeanFactoryAware, ApplicationContextAware, ImportBeanDefinitionRegistrar {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger log = LoggerFactory.getLogger(DynamicDataMongoRegistrar.class);
	/**
	 * MongoDB连接详情Bean名称模板
	 * <p>
	 * 格式为：{name}MongoConnectionDetails
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String CONNECTION_DETAILS_BEAN_NAME_TEMPLATE = "%sMongoConnectionDetails";
	/**
	 * MongoDB数据转换器Bean名称模板
	 * <p>
	 * 格式为：{name}MongoConverter
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private static final String MONGO_CONVERTER_BEAN_NAME_TEMPLATE = "%sMongoConverter";
	private static final String MONGO_MAPPING_CONTEXT_BEAN_NAME_TEMPLATE = "%sMongoMappingContext";
	private static final String MONGO_CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE = "%sMongoCustomConversions";

	/**
	 * Spring属性绑定器
	 * <p>
	 * 用于从环境中解析配置属性
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Binder binder;
	/**
	 * Spring Bean工厂
	 * <p>
	 * 用于获取容器中已有的Bean实例
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private BeanFactory beanFactory;
	private MongoManagedTypes mongoManagedTypes;

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		try {
			this.mongoManagedTypes = MongoManagedTypes.fromIterable(new EntityScanner(applicationContext).scan(Document.class));
		} catch (ClassNotFoundException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 注册Bean定义
	 * <p>
	 * 该方法在Spring容器初始化时被调用，用于动态注册MongoDB相关Bean。
	 * 执行流程如下：
	 * </p>
	 * <ol>
	 *     <li>从配置中解析{@link DynamicDataMongoProperties}
	 *         <ul>
	 *             <li>如果配置不存在，直接返回</li>
	 *         </ul>
	 *     </li>
	 *     <li>验证配置的有效性
	 *         <ul>
	 *             <li>数据源集合不可为空</li>
	 *             <li>主数据源名称不可为空</li>
	 *             <li>主数据源必须存在于数据源集合中</li>
	 *         </ul>
	 *     </li>
	 *     <li>为每个配置的MongoDB数据源注册完整的Bean定义链
	 *         <ul>
	 *             <li>按照依赖顺序注册各个Bean</li>
	 *             <li>配置Bean之间的依赖关系</li>
	 *             <li>设置Bean的初始化参数</li>
	 *         </ul>
	 *     </li>
	 *     <li>为主数据源的Bean设置primary标志
	 *         <ul>
	 *             <li>将主数据源的大多数Bean标记为primary=true</li>
	 *             <li>确保自动注入时优先使用主数据源</li>
	 *         </ul>
	 *     </li>
	 * </ol>
	 *
	 * @param importingClassMetadata 导入类的注解元数据
	 * @param beanDefinitionRegistry Bean定义注册表，用于注册新的Bean定义
	 * @since 1.0.0
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		DynamicDataMongoProperties dynamicDataMongoProperties;
		try {
			dynamicDataMongoProperties = binder.bind(DynamicDataMongoProperties.PREFIX, DynamicDataMongoProperties.class).get();
			Assert.notEmpty(dynamicDataMongoProperties.getDatabases(), "动态MongoDB配置：数据源集合不可为空");
			Assert.hasText(dynamicDataMongoProperties.getPrimary(), "动态MongoDB配置：主数据源不可为空");
			if (!dynamicDataMongoProperties.getDatabases().containsKey(dynamicDataMongoProperties.getPrimary())) {
				throw new IllegalArgumentException("动态MongoDB配置：主数据源必须为存在的数据源");
			}
		} catch (NoSuchElementException e) {
			return;
		}

		Map<String, DynamicDataMongoProperties.MongoProperties> mongoDatabases = dynamicDataMongoProperties.getDatabases();
		if (!CollectionUtils.isEmpty(mongoDatabases)) {
			mongoDatabases.forEach((name, mongoProperties) -> {
				// 注册 MongoConnectionDetails
				Supplier<MongoConnectionDetails> connectionDetailsSupplier = () -> new DynamicPropertiesMongoConnectionDetails(
					mongoProperties, beanFactory.getBeanProvider(SslBundles.class));
				String connectionDetailsBeanName = CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder connectionDetailsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoConnectionDetails.class, connectionDetailsSupplier);
				AbstractBeanDefinition connectionDetailsBeanDefinition = connectionDetailsBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(connectionDetailsBeanName, connectionDetailsBeanDefinition);

				// 注册 MongoClient
				Supplier<MongoClient> mongoClientSupplier = () -> {
					MongoConnectionDetails connectionDetails = beanFactory.getBean(connectionDetailsBeanName,
						MongoConnectionDetails.class);
					MongoClientSettingsBuilderCustomizer customizer = new StandardMongoClientSettingsBuilderCustomizer(
						connectionDetails, mongoProperties.getRepresentation().getUuid());
					MongoClientSettings clientSettings = MongoClientSettings.builder().build();
					return new MongoClientFactory(Collections.singletonList(customizer))
						.createMongoClient(clientSettings);
				};
				String mongoClientBeanName = DynamicMongoUtils.getMongoClientBeanName(name);
				BeanDefinitionBuilder mongoClientBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						MongoClient.class, mongoClientSupplier)
					.addDependsOn(connectionDetailsBeanName);
				AbstractBeanDefinition mongoClientBeanDefinition = mongoClientBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoClientBeanName, mongoClientBeanDefinition);

				// 注册 MongoCustomConversions
				Supplier<MongoCustomConversions> mongoCustomConversionsSupplier = () -> MongoCustomConversions.create(
					configurer -> PropertyMapper.get()
						.from(mongoProperties.getData().getRepresentation()::getBigDecimal)
						.to(configurer::bigDecimal)
				);
				String mongoCustomConversionsBeanName = MONGO_CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder mongoCustomConversionsBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
					MongoCustomConversions.class, mongoCustomConversionsSupplier);
				AbstractBeanDefinition mongoCustomConversionsBeanDefinition = mongoCustomConversionsBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoCustomConversionsBeanName, mongoCustomConversionsBeanDefinition);

				// 注册 MongoMappingContext
				Supplier<MongoMappingContext> mongoMappingContextSupplier = () -> {
					PropertyMapper map = PropertyMapper.get();
					MongoMappingContext context = new MongoMappingContext();
					map.from(mongoProperties.getData().isAutoIndexCreation()).to(context::setAutoIndexCreation);
					context.setManagedTypes(getMongoManagedTypes());
					Class<?> strategyClass = mongoProperties.getData().getFieldNamingStrategy();
					if (strategyClass != null) {
						context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
					}
					context.setSimpleTypeHolder(beanFactory.getBean(mongoCustomConversionsBeanName,
						MongoCustomConversions.class).getSimpleTypeHolder());
					return context;
				};
				String mongoMappingContextBeanName = MONGO_MAPPING_CONTEXT_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder mongoMappingContextBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						MongoMappingContext.class, mongoMappingContextSupplier)
					.addDependsOn(mongoCustomConversionsBeanName);
				AbstractBeanDefinition mongoMappingContextBeanDefinition = mongoMappingContextBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoMappingContextBeanName, mongoMappingContextBeanDefinition);

				// 注册 MongoDatabaseFactory
				Supplier<MongoDatabaseFactorySupport> mongoDatabaseFactorySupplier = () -> {
					String database = mongoProperties.getDatabase();
					if (database == null) {
						database = beanFactory.getBean(connectionDetailsBeanName, MongoConnectionDetails.class)
							.getConnectionString().getDatabase();
					}
					Assert.hasText(database, "Database name must not be empty");
					return new SimpleMongoClientDatabaseFactory(beanFactory.getBean(mongoClientBeanName,
						MongoClient.class), database);
				};
				String mongoDatabaseFactoryBeanName = DynamicMongoUtils.getMongoDatabaseFactoryBeanName(name);
				BeanDefinitionBuilder mongoDatabaseFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						MongoDatabaseFactorySupport.class, mongoDatabaseFactorySupplier)
					.addDependsOn(connectionDetailsBeanName)
					.addDependsOn(mongoClientBeanName);
				AbstractBeanDefinition mongoDatabaseFactoryBeanDefinition = mongoDatabaseFactoryBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoDatabaseFactoryBeanName, mongoDatabaseFactoryBeanDefinition);

				// 注册 MappingMongoConverter
				Supplier<MappingMongoConverter> mongoConverterSupplier = () -> {
					MongoDatabaseFactory mongoDatabaseFactory = beanFactory.getBean(
						DynamicMongoUtils.getMongoDatabaseFactoryBeanName(name), MongoDatabaseFactory.class);
					DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
					MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver,
						beanFactory.getBean(mongoMappingContextBeanName, MongoMappingContext.class));
					mappingConverter.setCustomConversions(beanFactory.getBean(mongoCustomConversionsBeanName,
						MongoCustomConversions.class));
					return mappingConverter;
				};
				String mongoConverterBeanName = MONGO_CONVERTER_BEAN_NAME_TEMPLATE.formatted(name);
				BeanDefinitionBuilder mongoConverterBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						MappingMongoConverter.class, mongoConverterSupplier)
					.addDependsOn(mongoMappingContextBeanName)
					.addDependsOn(mongoCustomConversionsBeanName)
					.addDependsOn(DynamicMongoUtils.getMongoDatabaseFactoryBeanName(name));
				AbstractBeanDefinition mongoConverterBeanDefinition = mongoConverterBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoConverterBeanName, mongoConverterBeanDefinition);

				// 注册 MongoTemplate
				Supplier<MongoTemplate> mongoTemplateSupplier = () -> new MongoTemplate(
					beanFactory.getBean(mongoDatabaseFactoryBeanName, MongoDatabaseFactory.class),
					beanFactory.getBean(mongoConverterBeanName, MongoConverter.class));
				String mongoTemplateBeanName = DynamicMongoUtils.getMongoTemplateBeanName(name);
				BeanDefinitionBuilder mongoTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						MongoTemplate.class, mongoTemplateSupplier)
					.addDependsOn(mongoDatabaseFactoryBeanName)
					.addDependsOn(mongoConverterBeanName);
				AbstractBeanDefinition mongoTemplateBeanDefinition = mongoTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(mongoTemplateBeanName, mongoTemplateBeanDefinition);

				// 注册 GridFsTemplate
				Supplier<GridFsTemplate> gridFsTemplateSupplier = () -> {
					MongoDatabaseFactory mongoDatabaseFactory = beanFactory.getBean(
						DynamicMongoUtils.getMongoDatabaseFactoryBeanName(name), MongoDatabaseFactory.class);
					GridFsMongoDatabaseFactory gridFsMongoDatabaseFactory = new GridFsMongoDatabaseFactory(
						mongoDatabaseFactory, mongoProperties.getData());
					MongoConverter converter = beanFactory.getBean(DynamicMongoUtils.getMongoTemplateBeanName(name),
						MongoTemplate.class).getConverter();
					return new GridFsTemplate(gridFsMongoDatabaseFactory, converter,
						mongoProperties.getData().getGridfs().getBucket());
				};
				String gridFsTemplateBeanName = DynamicMongoUtils.getGridFsTemplateBeanName(name);
				BeanDefinitionBuilder gridFsTemplateBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
						GridFsTemplate.class, gridFsTemplateSupplier)
					.addDependsOn(mongoDatabaseFactoryBeanName)
					.addDependsOn(mongoTemplateBeanName);
				AbstractBeanDefinition gridFsTemplateBeanDefinition = gridFsTemplateBeanBuilder.getBeanDefinition();
				beanDefinitionRegistry.registerBeanDefinition(gridFsTemplateBeanName, gridFsTemplateBeanDefinition);

				if (dynamicDataMongoProperties.getPrimary().equals(name)) {
					connectionDetailsBeanDefinition.setPrimary(true);
					mongoCustomConversionsBeanDefinition.setPrimary(true);
					mongoMappingContextBeanDefinition.setPrimary(true);
					mongoClientBeanDefinition.setPrimary(true);
					mongoDatabaseFactoryBeanDefinition.setPrimary(true);
					mongoConverterBeanDefinition.setPrimary(true);

					GenericBeanDefinition primaryMongoTemplateBeanDefinition = new GenericBeanDefinition(mongoTemplateBeanDefinition);
					primaryMongoTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("mongoTemplate", primaryMongoTemplateBeanDefinition);

					GenericBeanDefinition primaryGridFsTemplateBeanDefinition = new GenericBeanDefinition(gridFsTemplateBeanDefinition);
					primaryGridFsTemplateBeanDefinition.setPrimary(true);
					beanDefinitionRegistry.registerBeanDefinition("gridFsTemplate", primaryGridFsTemplateBeanDefinition);
				}
				log.info("dynamic-mongodb - add a database named [{}] success", name);
			});
			log.info("dynamic-mongodb initial loaded [{}] database,primary database named [{}]", mongoDatabases.size(),
				dynamicDataMongoProperties.getPrimary());
		}
	}

	private MongoManagedTypes getMongoManagedTypes() {
		MongoManagedTypes types = beanFactory.getBeanProvider(MongoManagedTypes.class).getIfAvailable();
		return ObjectUtils.getIfNull(types, mongoManagedTypes);
	}
}
