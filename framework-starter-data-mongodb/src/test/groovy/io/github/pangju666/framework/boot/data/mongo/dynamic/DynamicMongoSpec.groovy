package io.github.pangju666.framework.boot.data.mongo.dynamic

import io.github.pangju666.framework.boot.data.mongo.autoconfigure.DynamicDataMongoAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("dynamic-mongodb")
@ContextConfiguration(classes = [DynamicDataMongoAutoConfiguration.class], loader = SpringBootContextLoader .class)
class DynamicMongoSpec extends Specification {
	@Autowired
	MongoTemplate mongoTemplate

	@Qualifier("test1MongoTemplate")
	@Autowired
	MongoTemplate mongoTemplate1

	@Qualifier("test2MongoTemplate")
	@Autowired
	MongoTemplate mongoTemplate2

	def "测试是否正确装配Bean"() {
		expect:
		mongoTemplate != null
		mongoTemplate1 != null
		mongoTemplate2 != null
	}
}
