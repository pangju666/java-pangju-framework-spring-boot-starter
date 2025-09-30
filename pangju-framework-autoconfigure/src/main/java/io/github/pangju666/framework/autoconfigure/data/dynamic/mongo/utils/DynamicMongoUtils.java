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

package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.utils;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

public class DynamicMongoUtils {
	private static final String CONNECTION_DETAILS_BEAN_NAME_TEMPLATE = "%sMongoConnectionDetails";
	private static final String CLIENT_SETTINGS_BEAN_NAME_TEMPLATE = "%sMongoClientSettings";
	private static final String MAPPING_CONTEXT_BEAN_NAME_TEMPLATE = "%sMongoMappingContext";
	private static final String CLIENT_BEAN_NAME_TEMPLATE = "%sMongoClient";
	private static final String CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE = "%sMongoCustomConversions";
	private static final String MONGO_CONVERTER_BEAN_NAME_TEMPLATE = "%sMongoConverter";
	private static final String DATABASE_FACTORY_BEAN_NAME_TEMPLATE = "%sMongoDatabaseFactory";
	private static final String TEMPLATE_BEAN_NAME_TEMPLATE = "%sMongoTemplate";
	private static final String GRID_FS_TEMPLATE_BEAN_NAME_TEMPLATE = "%sGridFsTemplate";

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

	public static String getMongoMappingContextBeanName(String name) {
		return MAPPING_CONTEXT_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoMappingContext getMongoMappingContext(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(MAPPING_CONTEXT_BEAN_NAME_TEMPLATE.formatted(name), MongoMappingContext.class);
	}

	public static String getMongoCustomConversionsBeanName(String name) {
		return CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoCustomConversions getMongoCustomConversions(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CUSTOM_CONVERSIONS_BEAN_NAME_TEMPLATE.formatted(name), MongoCustomConversions.class);
	}

	public static String getMongoClientBeanName(String name) {
		return CLIENT_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoClient getMongoClient(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(CLIENT_BEAN_NAME_TEMPLATE.formatted(name), MongoClient.class);
	}

	public static String getMongoDatabaseFactoryBeanName(String name) {
		return DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoDatabaseFactory getMongoDatabaseFactory(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(DATABASE_FACTORY_BEAN_NAME_TEMPLATE.formatted(name), MongoDatabaseFactory.class);
	}

	public static String getMongoConverterBeanName(String name) {
		return MONGO_CONVERTER_BEAN_NAME_TEMPLATE.formatted(name);
	}

	public static MongoConverter getMongoConverter(String name, BeanFactory beanFactory) {
		return beanFactory.getBean(MONGO_CONVERTER_BEAN_NAME_TEMPLATE.formatted(name), MongoConverter.class);
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
