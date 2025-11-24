/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.framework.boot.autoconfigure.image;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 图像处理自动配置属性。
 *
 * <p><strong>前缀</strong>：{@code pangju.image}</p>
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>选择图像处理实现类型（{@code GM} 或 {@code IMAGEIO}）。</li>
 *   <li>提供 GraphicsMagick 相关配置（可执行路径与连接池）。</li>
 *   <li>配置异步任务执行器引用，用于异步图像处理任务提交。</li>
 * </ul>
 *
 * <p><strong>属性映射</strong></p>
 * <ul>
 *   <li>{@code pangju.image.type}：处理实现类型，默认 {@code IMAGEIO}。</li>
 *   <li>{@code pangju.image.async-task-executor-ref}：异步任务执行器 Bean 名称引用，默认使用 Spring Boot 的
 *   {@link org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration#APPLICATION_TASK_EXECUTOR_BEAN_NAME}。</li>
 *   <li>{@code pangju.image.gm.path}：GraphicsMagick 可执行文件路径。</li>
 *   <li>{@code pangju.image.gm.pool.max-active}：连接池最大活跃数，默认 {@code 4}。</li>
 *   <li>{@code pangju.image.gm.pool.max-idle}：连接池最大空闲数，默认 {@code 4}。</li>
 *   <li>{@code pangju.image.gm.pool.min-idle}：连接池最小空闲数，默认 {@code 2}。</li>
 *   <li>{@code pangju.image.gm.pool.min-evictable-idle-time-millis}：资源最小空闲毫秒数，默认 {@code 300000}。</li>
 *   <li>{@code pangju.image.gm.pool.when-exhausted-action}：连接耗尽动作（{@code FAIL}/{@code BLOCK}/{@code GROW}），默认 {@code BLOCK}。</li>
 *   <li>{@code pangju.image.gm.pool.max-wait}：无对象返回时最大等待时间（{@link java.time.Duration}），默认 {@code PT5M}。</li>
 *   <li>{@code pangju.image.gm.pool.test-while-idle}：是否定期校验空闲连接，默认 {@code false}。</li>
 *   <li>{@code pangju.image.gm.pool.time-between-eviction-runs-millis}：空闲资源检测周期毫秒数，默认 {@code 10000}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "pangju.image")
public class ImageProperties {
	/**
	 * 图像处理实现类型（默认使用 {@code IMAGEIO}）。
	 *
	 * <p>对应属性：{@code pangju.image.type}</p>
	 *
	 * @since 1.0.0
	 */
	private Type type = Type.IMAGEIO;
	/**
	 * GraphicsMagick 相关配置。
	 *
	 * <p>配置组前缀：{@code pangju.image.gm}</p>
	 *
	 * @since 1.0.0
	 */
	private GM gm = new GM();
	/**
	 * 异步任务执行器 Bean 名称引用。
	 *
	 * <p>用于提交异步图像处理任务的执行器引用，默认使用 Spring Boot 提供的
	 * {@link org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration#APPLICATION_TASK_EXECUTOR_BEAN_NAME}
	 * （通常为 {@code applicationTaskExecutor}）。</p>
	 * <p>对应属性：{@code pangju.image.async-task-executor-ref}</p>
	 *
	 * @since 1.0.0
	 */
	private String asyncTaskExecutorRef = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

	public String getAsyncTaskExecutorRef() {
		return asyncTaskExecutorRef;
	}

	public void setAsyncTaskExecutorRef(String asyncTaskExecutorRef) {
		this.asyncTaskExecutorRef = asyncTaskExecutorRef;
	}

	public GM getGm() {
		return gm;
	}

	public void setGm(GM graphicsMagick) {
		this.gm = graphicsMagick;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * 图像处理实现类型。
	 *
	 * <p>可选值：</p>
	 * <ul>
	 *   <li>{@code GM}：使用 GraphicsMagick 进行处理。</li>
	 *   <li>{@code IMAGEIO}：使用 ImageIO 进行处理。</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	public enum Type {
		GRAPHICS_MAGICK,
		IMAGEIO
	}

	/**
	 * GraphicsMagick 配置。
	 *
	 * <p>前缀：{@code pangju.image.gm}</p>
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class GM {
		/**
		 * GraphicsMagick 的连接池配置。
		 *
		 * <p>前缀：{@code pangju.image.gm.pool}</p>
		 *
		 * @since 1.0.0
		 */
		private GMPool pool = new GMPool();
		/**
		 * GraphicsMagick 可执行文件路径。
		 *
		 * <p>对应属性：{@code pangju.image.gm.path}</p>
		 *
		 * @since 1.0.0
		 */
		private String path;

		public GMPool getPool() {
			return pool;
		}

		public void setPool(GMPool pool) {
			this.pool = pool;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}

	/**
	 * GraphicsMagick 进程池配置。
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class GMPool {
		/**
		 * 连接池最大活跃数。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-active}</p>
		 * <p>默认值：{@code 4}</p>
		 *
		 * @since 1.0.0
		 */
		private int maxActive = 4;
		/**
		 * 连接池最大空闲连接数。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-idle}</p>
		 * <p>默认值：{@code 4}</p>
		 *
		 * @since 1.0.0
		 */
		private int maxIdle = 4;
		/**
		 * 连接池最小空闲连接数。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.min-idle}</p>
		 * <p>默认值：{@code 2}</p>
		 *
		 * @since 1.0.0
		 */
		private int minIdle = 2;
		/**
		 * 资源最小空闲时间（毫秒）。达到此值后空闲资源将被移除。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.min-evictable-idle-time-millis}</p>
		 * <p>默认值：{@code 300000}</p>
		 *
		 * @since 1.0.0
		 */
		private long minEvictableIdleTimeMillis = 300000L;
		/**
		 * 连接池连接用尽后的动作。
		 *
		 * <p>可选值：{@code FAIL}/{@code BLOCK}/{@code GROW}</p>
		 * <p>对应属性：{@code pangju.image.gm.pool.when-exhausted-action}</p>
		 * <p>默认值：{@code BLOCK}</p>
		 *
		 * @since 1.0.0
		 */
		private WhenExhaustedAction whenExhaustedAction = WhenExhaustedAction.BLOCK;
		/**
		 * 无对象返回时的最大等待时间（{@link java.time.Duration}）。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-wait}</p>
		 * <p>默认值：{@code PT5M}</p>
		 *
		 * @since 1.0.0
		 */
		private Duration maxWait = Duration.ofMinutes(5);
		/**
		 * 是否定期对连接池中空闲连接进行校验。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.test-while-idle}</p>
		 * <p>默认值：{@code false}</p>
		 *
		 * @since 1.0.0
		 */
		private boolean testWhileIdle = false;
		/**
		 * 空闲资源的检测周期（毫秒）。
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.time-between-eviction-runs-millis}</p>
		 * <p>默认值：{@code 10000}</p>
		 *
		 * @since 1.0.0
		 */
		private long timeBetweenEvictionRunsMillis = 10000L;

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}

		public int getMaxIdle() {
			return maxIdle;
		}

		public void setMaxIdle(int maxIdle) {
			this.maxIdle = maxIdle;
		}

		public int getMinIdle() {
			return minIdle;
		}

		public void setMinIdle(int minIdle) {
			this.minIdle = minIdle;
		}

		public long getMinEvictableIdleTimeMillis() {
			return minEvictableIdleTimeMillis;
		}

		public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
			this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		}

		public WhenExhaustedAction getWhenExhaustedAction() {
			return whenExhaustedAction;
		}

		public void setWhenExhaustedAction(WhenExhaustedAction whenExhaustedAction) {
			this.whenExhaustedAction = whenExhaustedAction;
		}

		public Duration getMaxWait() {
			return maxWait;
		}

		public void setMaxWait(Duration maxWait) {
			this.maxWait = maxWait;
		}

		public boolean isTestWhileIdle() {
			return testWhileIdle;
		}

		public void setTestWhileIdle(boolean testWhileIdle) {
			this.testWhileIdle = testWhileIdle;
		}

		public long getTimeBetweenEvictionRunsMillis() {
			return timeBetweenEvictionRunsMillis;
		}

		public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
			this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		}
	}

	public enum WhenExhaustedAction {
		/**
		 * Throw a {@link java.util.NoSuchElementException}.
		 */
		FAIL,
		/**
		 * 阻塞直到有新的或空闲的连接可用。或者如果 maxWait 为正且通过则失败。
		 */
		BLOCK,
		/**
		 * 创建一个新连接并返回它（本质上使 maxActive 变得毫无意义）。
		 */
		GROW;
	}
}
