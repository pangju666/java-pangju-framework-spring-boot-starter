package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

public class DynamicMongoUtils {
	private static final String CONNECTION_DETAILS_BEAN_NAME_TEMPLATE = "mongo-%s-connection-details";
	private static final String CLIENT_SETTINGS_BEAN_NAME_TEMPLATE = "mongo-%s-client-settings";
	private static final String CLIENT_BEAN_NAME_TEMPLATE = "mongo-%s-client";
	private static final String CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE = "mongo-%s-custom-conversions";
	private static final String MAPPING_CONTEXT_BEAN_NAME_TEMPLATE = "mongo-%s-mapping-context";
	private static final String DATABASE_FACTORY_BEAN_NAME_TEMPLATE = "mongo-%s-database-factory";
	private static final String MAPPING_MONGO_CONVERTER_BEAN_NAME_TEMPLATE = "mongo-%s-mapping-mongo-converter";
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "mongo-%s-template";
	private static final String GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE = "mongo-%s-grid-fs-template";

	protected DynamicMongoUtils() {
	}

	public static String getMongoConnectionDetailsBeanName(String name) {
		return CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoConnectionDetails getMongoConnectionDetails(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CONNECTION_DETAILS_BEAN_NAME_TEMPLATE.formatted(name), MongoConnectionDetails.class);
	}

	public static String getMongoClientSettingsBeanName(String name) {
		return CLIENT_SETTINGS_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoClientSettings getMongoClientSettings(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CLIENT_SETTINGS_BEAN_NAME_TEMPLATE.formatted(name), MongoClientSettings.class);
	}

	public static String getMongoClientBeanName(String name) {
		return CLIENT_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoClient getMongoClient(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CLIENT_BEAN_NAME_TEMPLATE.formatted(name), MongoClient.class);
	}

	public static String getMongoCustomConversionsBeanName(String name) {
		return CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoCustomConversions getMongoCustomConversions(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE.formatted(name), MongoCustomConversions.class);
	}

	public static String getMongoMappingContextBeanName(String name) {
		return MAPPING_CONTEXT_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoMappingContext getMongoMappingContext(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(MAPPING_CONTEXT_BEAN_NAME_TEMPLATE.formatted(name), MongoMappingContext.class);
	}

	public static String getMongoDatabaseFactorySupportBeanName(String name) {
		return DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoDatabaseFactorySupport getMongoDatabaseFactorySupport(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), MongoDatabaseFactorySupport.class);
	}

	public static String getMappingMongoConverterBeanName(String name) {
		return MAPPING_MONGO_CONVERTER_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MappingMongoConverter getMappingMongoConverter(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(MAPPING_MONGO_CONVERTER_BEAN_NAME_TEMPLATE.formatted(name), MappingMongoConverter.class);
	}

	public static String getMongoTemplateBeanName(String name) {
		return TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoTemplate getMongoTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), MongoTemplate.class);
	}

	public static String getGridFsTemplateBeanName(String name) {
		return GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static GridFsTemplate getGridFsTemplate(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE.formatted(name), GridFsTemplate.class);
	}
}
