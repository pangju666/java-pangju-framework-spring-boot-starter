package io.github.pangju666.framework.boot.data.redis

import io.github.pangju666.framework.data.redis.core.JsonScanRedisTemplate
import io.github.pangju666.framework.data.redis.core.StringScanRedisTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("redis")
@ContextConfiguration(classes = [RedisAutoConfiguration.class, RedisScanAutoConfiguration.class],
	loader = SpringBootContextLoader.class)
class RedisScanSpec extends Specification {
    @Autowired
	StringScanRedisTemplate stringScanRedisTemplate
	@Autowired
	JsonScanRedisTemplate jsonScanRedisTemplate

    def "测试是否正确装配Bean"() {
        expect:
		stringScanRedisTemplate != null
		jsonScanRedisTemplate != null
    }
}
