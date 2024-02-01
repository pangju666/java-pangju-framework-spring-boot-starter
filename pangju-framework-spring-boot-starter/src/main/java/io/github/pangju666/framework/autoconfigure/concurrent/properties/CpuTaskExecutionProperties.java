package io.github.pangju666.framework.autoconfigure.concurrent.properties;

import io.github.pangju666.framework.autoconfigure.concurrent.enums.AbortStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "chang-tech.task.execution.cpu")
public class CpuTaskExecutionProperties {
	private boolean enabled = false;
	private Duration keepAlive = Duration.ofSeconds(60);
	private Integer queueCapacity = Integer.MAX_VALUE;
	private Boolean allowCoreThreadTimeOut = true;
	private Boolean preStartAllCoreThreads = false;
	private AbortStrategy abortStrategy = AbortStrategy.ABORT;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Duration getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Duration keepAlive) {
		this.keepAlive = keepAlive;
	}

	public Integer getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(Integer queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public Boolean getAllowCoreThreadTimeOut() {
		return allowCoreThreadTimeOut;
	}

	public void setAllowCoreThreadTimeOut(Boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	public Boolean getPreStartAllCoreThreads() {
		return preStartAllCoreThreads;
	}

	public void setPreStartAllCoreThreads(Boolean preStartAllCoreThreads) {
		this.preStartAllCoreThreads = preStartAllCoreThreads;
	}

	public AbortStrategy getAbortStrategy() {
		return abortStrategy;
	}

	public void setAbortStrategy(AbortStrategy abortStrategy) {
		this.abortStrategy = abortStrategy;
	}
}
