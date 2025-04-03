package io.github.pangju666.framework.autoconfigure.data.mybatisplus;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import io.github.pangju666.framework.autoconfigure.data.mybatisplus.properties.MybatisPlusInterceptorProperties;
import io.github.pangju666.framework.data.mybatisplus.injector.DeleteInjector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@AutoConfiguration(after = {com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class})
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({MybatisPlusInterceptorProperties.class})
public class MybatisPlusAutoConfiguration implements BeanFactoryAware {
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@ConditionalOnMissingBean(MybatisPlusInterceptor.class)
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusInterceptorProperties properties) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		if (properties.getDataPermission().isEnabled()) {
			try {
				Class<? extends DataPermissionHandler> clazz = properties.getDataPermission().getHandler();
				DataPermissionHandler dataPermissionHandler;
				if (Objects.nonNull(clazz)) {
					dataPermissionHandler = clazz.getDeclaredConstructor().newInstance();
				} else {
					dataPermissionHandler = beanFactory.getBean(DataPermissionHandler.class);
				}
				interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
			} catch (NoSuchBeanDefinitionException | NoSuchMethodException |
					 InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
				interceptor.addInnerInterceptor(new DataPermissionInterceptor());
			}
		}
		if (properties.getTenantLine().isEnabled()) {
			try {
				Class<? extends TenantLineHandler> clazz = properties.getTenantLine().getHandler();
				TenantLineHandler tenantLineHandler;
				if (Objects.nonNull(clazz)) {
					tenantLineHandler = clazz.getDeclaredConstructor().newInstance();
				} else {
					tenantLineHandler = beanFactory.getBean(TenantLineHandler.class);
				}
				interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
			} catch (NoSuchBeanDefinitionException | NoSuchMethodException |
					 InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
				interceptor.addInnerInterceptor(new TenantLineInnerInterceptor());
			}
		}
		if (properties.getDynamicTableName().isEnabled()) {
			try {
				Class<? extends TableNameHandler> clazz = properties.getDynamicTableName().getHandler();
				TableNameHandler tableNameHandler;
				if (Objects.nonNull(clazz)) {
					tableNameHandler = clazz.getDeclaredConstructor().newInstance();
				} else {
					tableNameHandler = beanFactory.getBean(TableNameHandler.class);
				}
				interceptor.addInnerInterceptor(new DynamicTableNameInnerInterceptor(tableNameHandler));
			} catch (NoSuchBeanDefinitionException | NoSuchMethodException |
					 InvocationTargetException | InstantiationException | IllegalAccessException ignored) {
				interceptor.addInnerInterceptor(new DynamicTableNameInnerInterceptor());
			}
		}
		if (properties.getPagination().isEnabled()) {
			try {
				Class<? extends IDialect> clazz = properties.getPagination().getDialect();
				if (Objects.nonNull(clazz)) {
					IDialect dialect = clazz.getDeclaredConstructor().newInstance();
					interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dialect));
				} else {
					interceptor.addInnerInterceptor(new PaginationInnerInterceptor(properties.getPagination().getDbType()));
				}
			} catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
					 IllegalAccessException ignored) {
				interceptor.addInnerInterceptor(new PaginationInnerInterceptor(properties.getPagination().getDbType()));
			}
		}
		if (properties.getOptimisticLocker().isEnabled()) {
			interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(properties.getOptimisticLocker().isWrapperMode()));
		}
		if (properties.getBlockAttack().isEnabled()) {
			interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
		}
		return interceptor;
	}

	@ConditionalOnMissingBean(DeleteInjector.class)
	@Bean
	public DeleteInjector deleteInjector() {
		return new DeleteInjector();
	}
}
