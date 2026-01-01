package io.github.pangju666.framework.boot.data.mybatisplus

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [MybatisPlusPluginsAutoConfiguration.class, MybatisPlusInnerInterceptorAutoConfiguration.class],
	loader = SpringBootContextLoader.class)
class MybatisPlusInnerInterceptorSpec extends Specification {
	@Autowired
	MybatisPlusInterceptor defaultMybatisPlusInterceptor

	def "测试是否正确装配Bean"() {
		expect:
		defaultMybatisPlusInterceptor != null

		def pagination = false
		def optimisticLocker = false
		def blockAttack = false
		for (final def innerInterceptor in defaultMybatisPlusInterceptor.getInterceptors()) {
			if (innerInterceptor instanceof PaginationInnerInterceptor) {
				pagination = true
			}
			if (innerInterceptor instanceof OptimisticLockerInnerInterceptor) {
				optimisticLocker = true
			}
			if (innerInterceptor instanceof BlockAttackInnerInterceptor) {
				blockAttack = true
			}
		}
		pagination == true
		optimisticLocker == true
		blockAttack == true
	}
}
