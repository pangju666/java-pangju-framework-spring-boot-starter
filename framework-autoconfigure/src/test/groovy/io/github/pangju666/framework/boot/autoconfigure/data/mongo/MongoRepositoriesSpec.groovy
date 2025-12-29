package io.github.pangju666.framework.boot.autoconfigure.data.mongo

import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.util.ProxyUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("mongodb")
@EnableMongoRepositories(basePackages = "io.github.pangju666.framework.boot.autoconfigure.data.mongo",
	repositoryBaseClass = SimpleBaseMongoRepository.class)
@ContextConfiguration(classes = [
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class,
	MongoRepositoriesAutoConfiguration.class
], loader = SpringBootContextLoader.class)
class MongoRepositoriesSpec extends Specification {
	@Autowired
	TestMongoRepository repository

	def "测试是否正确装配Bean"() {
		expect:
		repository != null
		ProxyUtils.getUserClass(repository) == SimpleBaseMongoRepository.class
	}
}