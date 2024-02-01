package io.github.pangju666.framework.autoconfigure.concurrent;

import io.github.pangju666.framework.autoconfigure.concurrent.properties.CpuTaskExecutionProperties;
import io.github.pangju666.framework.autoconfigure.concurrent.properties.IOTaskExecutionProperties;
import io.github.pangju666.framework.core.lang.pool.ConstantPool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@AutoConfiguration(after = org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.class)
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@EnableConfigurationProperties({CpuTaskExecutionProperties.class, IOTaskExecutionProperties.class})
public class TaskExecutionAutoConfiguration {
	@Primary
	@Bean(name = {org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME,
		AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME})
	@ConditionalOnMissingBean(Executor.class)
	public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutorBuilder builder) {
		return builder.build();
	}

	@ConditionalOnProperty(prefix = "chang-tech.task.execution.cpu", name = "enabled", havingValue = "true")
	@Bean(ConstantPool.CPU_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME)
	public ThreadPoolTaskExecutor cpuTaskExecutor(CpuTaskExecutionProperties properties) {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(cpuCount + 1);
		executor.setCorePoolSize(cpuCount + 1);
		executor.setQueueCapacity(properties.getQueueCapacity());
		executor.setKeepAliveSeconds((int) properties.getKeepAlive().toSeconds());
		executor.setAllowCoreThreadTimeOut(properties.getAllowCoreThreadTimeOut());
		executor.setPrestartAllCoreThreads(properties.getPreStartAllCoreThreads());
		executor.setRejectedExecutionHandler(properties.getAbortStrategy().getExecutionHandler());
		return executor;
	}

	@ConditionalOnProperty(prefix = "chang-tech.task.execution.io", name = "enabled", havingValue = "true")
	@Bean(ConstantPool.IO_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME)
	public ThreadPoolTaskExecutor ioTaskExecutor(IOTaskExecutionProperties properties) {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(cpuCount * 2);
		executor.setCorePoolSize(cpuCount * 2);
		executor.setQueueCapacity(properties.getQueueCapacity());
		executor.setKeepAliveSeconds((int) properties.getKeepAlive().toSeconds());
		executor.setAllowCoreThreadTimeOut(properties.getAllowCoreThreadTimeOut());
		executor.setPrestartAllCoreThreads(properties.getPreStartAllCoreThreads());
		executor.setRejectedExecutionHandler(properties.getAbortStrategy().getExecutionHandler());
		return executor;
	}
}
