package io.github.pangju666.framework.boot.autoconfigure.data.mybatisplus

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import io.github.pangju666.framework.boot.autoconfigure.Application
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("mybatisplus")
@ContextConfiguration(classes = Application.class, loader = SpringBootContextLoader.class)
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
