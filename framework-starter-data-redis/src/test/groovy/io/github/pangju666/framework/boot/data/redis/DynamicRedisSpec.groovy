package io.github.pangju666.framework.boot.data.redis

import io.github.pangju666.framework.boot.data.redis.autoconfigure.DynamicDataRedisProperties
import io.github.pangju666.framework.data.redis.core.ScanRedisTemplate
import io.github.pangju666.framework.data.redis.core.StringScanRedisTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("dynamic-redis")
@ContextConfiguration(classes = [DynamicDataRedisProperties.class], loader = SpringBootContextLoader .class)
class DynamicRedisSpec extends Specification {
	@Autowired
	RedisTemplate<Object, Object> redisTemplate

	@Qualifier("redis1RedisTemplate")
	@Autowired
	RedisTemplate<Object, Object> redisTemplate1

	@Qualifier("redis2RedisTemplate")
	@Autowired
	RedisTemplate<Object, Object> redisTemplate2

	@Autowired
	StringRedisTemplate stringRedisTemplate

	@Qualifier("redis1StringRedisTemplate")
	@Autowired
	StringRedisTemplate stringRedisTemplate1

	@Qualifier("redis2StringRedisTemplate")
	@Autowired
	StringRedisTemplate stringRedisTemplate2

	@Autowired
	ScanRedisTemplate<Object> scanRedisTemplate

	@Qualifier("redis1ScanRedisTemplate")
	@Autowired
	ScanRedisTemplate<Object> scanRedisTemplate1

	@Qualifier("redis2ScanRedisTemplate")
	@Autowired
	ScanRedisTemplate<Object> scanRedisTemplate2

	@Autowired
	StringScanRedisTemplate stringScanRedisTemplate

	@Qualifier("redis1StringScanRedisTemplate")
	@Autowired
	StringScanRedisTemplate stringScanRedisTemplate1

	@Qualifier("redis2StringScanRedisTemplate")
	@Autowired
	StringScanRedisTemplate stringScanRedisTemplate2

	def "测试是否正确装配Bean"() {
		expect:
		redisTemplate != null
		redisTemplate1 != null
		redisTemplate2 != null

		stringRedisTemplate != null
		stringRedisTemplate1 != null
		stringRedisTemplate2 != null

		scanRedisTemplate != null
		scanRedisTemplate1 != null
		scanRedisTemplate2 != null

		stringScanRedisTemplate != null
		stringScanRedisTemplate1 != null
		stringScanRedisTemplate2 != null
	}
}
