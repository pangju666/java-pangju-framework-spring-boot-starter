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
 *     <li>解析{@link DynamicMongoProperties}配置属性</li>
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
 *     <li>MongoCustomConversions - 自定义类型转换</li>
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
 * @see DynamicMongoProperties
 * @see DynamicMongoUtils
 * @see DynamicMongoAutoConfiguration
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class DynamicMongoRegistrar implements EnvironmentAware, BeanFactoryAware, ImportBeanDefinitionRegistrar {
	/**
	 * 日志记录器
	 *
	 * @since 1.0.0
	 */
	private static final Logger log = LoggerFactory.getLogger(DynamicMongoRegistrar.class);

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

	@Override
	public void setEnvironment(Environment environment) {
		this.binder = Binder.get(environment);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * 注册Bean定义
	 * <p>
	 * 该方法在Spring容器初始化时被调用，用于动态注册MongoDB相关Bean。
	 * 执行流程如下：
	 * </p>
	 * <ol>
	 *     <li>从配置中解析{@link DynamicMongoProperties}
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

	/**
	 * MongoDB GridFS数据库工厂内部类，来源于 org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration.GridFsMongoDatabaseFactory
	 *
	 * @since 1.0.0
	 */
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
