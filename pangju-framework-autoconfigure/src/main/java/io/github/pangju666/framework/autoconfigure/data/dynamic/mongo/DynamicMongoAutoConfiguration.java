package io.github.pangju666.framework.autoconfigure.data.dynamic.mongo;

import com.mongodb.client.MongoClient;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.processor.DynamicMongoBeanPostProcessor;
import io.github.pangju666.framework.autoconfigure.data.dynamic.mongo.registrar.DynamicMongoRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@AutoConfiguration(before = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@ConditionalOnClass({MongoClient.class, MongoTemplate.class})
@EnableConfigurationProperties(DynamicMongoProperties.class)
@Import({DynamicMongoRegistrar.class})
public class DynamicMongoAutoConfiguration {
	@Bean
	public DynamicMongoBeanPostProcessor dynamicMongoDataBaseBeanPostProcessor() {
		return new DynamicMongoBeanPostProcessor();
	}
}
