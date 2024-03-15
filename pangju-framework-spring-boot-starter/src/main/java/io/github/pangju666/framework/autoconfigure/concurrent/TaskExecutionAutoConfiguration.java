package io.github.pangju666.framework.autoconfigure.concurrent;

import io.github.pangju666.framework.autoconfigure.concurrent.properties.CpuTaskExecutionProperties;
import io.github.pangju666.framework.autoconfigure.concurrent.properties.IOTaskExecutionProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@AutoConfiguration(before = org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.class)
@ConditionalOnThreading(Threading.PLATFORM)
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@EnableConfigurationProperties({CpuTaskExecutionProperties.class, IOTaskExecutionProperties.class})
public class TaskExecutionAutoConfiguration {
	public static final String IO_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME = "cpuApplicationTaskExecutor";
	public static final String CPU_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME = "ioApplicationTaskExecutor";

	@Primary
	@Bean(name = {org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME,
		AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME})
	ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder threadPoolTaskExecutorBuilder) {
		return threadPoolTaskExecutorBuilder.build();
	}

	@ConditionalOnProperty(prefix = "pangju.task.execution.cpu", name = "enabled", havingValue = "true")
	@Bean(CPU_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME)
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

	@ConditionalOnProperty(prefix = "pangju.task.execution.io", name = "enabled", havingValue = "true")
	@Bean(IO_THREAD_POOL_TASK_EXECUTOR_BEAN_NAME)
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
