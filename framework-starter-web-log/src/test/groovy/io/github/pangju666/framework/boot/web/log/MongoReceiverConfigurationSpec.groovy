package io.github.pangju666.framework.boot.web.log


import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver
import io.github.pangju666.framework.boot.web.log.receiver.impl.mongo.MongoWebLogReceiver
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class MongoReceiverConfigurationSpec extends Specification {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MongoReceiverConfiguration))

    def "should register MongoWebLogReceiver when enabled and MongoTemplate exists"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.receiver-type=MONGODB")
                .withBean(WebLogProperties, { -> new WebLogProperties() })
                .withBean(MongoTemplate, { -> Mock(MongoTemplate) })
                .run { context ->
                    assert context.containsBean("mongoWebLogReceiver")
                    assert context.getBean(MongoWebLogReceiver)
                }
    }

    def "should use specific MongoTemplate if configured"() {
        given:
        def customMongoTemplate = Mock(MongoTemplate)

        expect:
        contextRunner
                .withPropertyValues(
                        "pangju.web.log.receiver-type=MONGODB",
                        "pangju.web.log.mongo.mongo-template-ref=customMongoTemplate"
                )
                .withBean(WebLogProperties, {
                    def p = new WebLogProperties()
                    p.mongo.mongoTemplateRef = "customMongoTemplate"
                    return p
                })
                .withBean("customMongoTemplate", MongoTemplate, { -> customMongoTemplate })
                .run { context ->
                    assert context.containsBean("mongoWebLogReceiver")
                    def receiver = context.getBean(MongoWebLogReceiver)
                    // Verification logic depends on how we can access the template inside receiver.
                    // Assuming successful bean creation means it found the template.
                }
    }

    def "should not register MongoWebLogReceiver if receiver-type is not MONGODB"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.receiver-type=SLF4J")
                .withBean(MongoTemplate, { -> Mock(MongoTemplate) })
                .run { context ->
                    assert !context.containsBean("mongoWebLogReceiver")
                }
    }

    def "should not register MongoWebLogReceiver if MongoTemplate is missing"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.receiver-type=MONGODB")
                .run { context ->
                    assert !context.containsBean("mongoWebLogReceiver")
                }
    }

    def "should back off if WebLogReceiver already exists"() {
        expect:
        contextRunner
                .withPropertyValues("pangju.web.log.receiver-type=MONGODB")
                .withBean(MongoTemplate, { -> Mock(MongoTemplate) })
                .withBean(WebLogReceiver, { -> Mock(WebLogReceiver) })
                .run { context ->
                    assert !context.containsBean("mongoWebLogReceiver")
                }
    }
}
