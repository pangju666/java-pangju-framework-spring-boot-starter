package io.github.pangju666.framework.boot.data.mongo.dynamic

import io.github.pangju666.framework.boot.data.dynamic.mongo.DynamicMongoRepositoryFactoryBean
import io.github.pangju666.framework.data.mongodb.repository.SimpleBaseMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.util.ProxyUtils
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("dynamic-mongodb")
@EnableMongoRepositories(basePackages = "io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo",
	repositoryFactoryBeanClass = DynamicMongoRepositoryFactoryBean.class, repositoryBaseClass = SimpleBaseMongoRepository.class)
@ContextConfiguration(classes = [DynamicMongoAutoConfiguration.class, DynamicMongoRepositoriesAutoConfiguration.class],
loader = SpringBootContextLoader.class)
class DynamicMongoRepositoriesSpec extends Specification {
	@Autowired
	TestMongoRepository repository

	@Autowired
	Test1MongoRepository repository1

	@Autowired
	Test2MongoRepository repository2

	def "测试是否正确装配Bean"() {
		expect:
		repository != null
		ProxyUtils.getUserClass(repository) == SimpleBaseMongoRepository.class

		repository1 != null
		ProxyUtils.getUserClass(repository1) == SimpleBaseMongoRepository.class

		repository2 != null
		ProxyUtils.getUserClass(repository2) == SimpleBaseMongoRepository.class
	}
}
