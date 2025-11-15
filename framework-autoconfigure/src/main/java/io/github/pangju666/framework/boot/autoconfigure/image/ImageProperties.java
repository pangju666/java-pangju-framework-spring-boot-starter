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

import org.gm4java.engine.support.WhenExhaustedAction;
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
 * </ul>
 *
 * <p><strong>字段</strong></p>
 * <ul>
 *   <li>{@code type}：处理实现类型，默认 {@code IMAGEIO}。</li>
 *   <li>{@code gm}：GraphicsMagick 配置组，包含 {@code path} 与 {@code pool}。</li>
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
	 * @since 1.0.0
	 */
	private Type type = Type.IMAGEIO;
	/**
	 * GraphicsMagick 相关配置。
	 *
	 * @since 1.0.0
	 */
	private GM gm = new GM();

	public GM getGm() {
		return gm;
	}

	public void setGm(GM gm) {
		this.gm = gm;
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
		GM,
		IMAGEIO
	}

	/**
	 * GraphicsMagick 配置
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class GM {
		/**
		 * GraphicsMagick 的连接池配置。
		 *
		 * @since 1.0.0
		 */
		private Pool pool = new Pool();
		/**
		 * GraphicsMagick 可执行文件路径。
		 *
		 * @since 1.0.0
		 */
		private String path;

		public Pool getPool() {
			return pool;
		}

		public void setPool(Pool pool) {
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
	 * GraphicsMagick 进程池配置
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class Pool {
		/**
		 * 连接池最大活跃数
		 *
		 * @since 1.0.0
		 */
		private int maxActive = 4;
		/**
		 * 连接池最大空闲连接数
		 *
		 * @since 1.0.0
		 */
		private int maxIdle = 4;
		/**
		 * 连接池最小空闲连接数
		 *
		 * @since 1.0.0
		 */
		private int minIdle = 2;
		/**
		 * 资源池中资源最小空闲时间(单位为毫秒)，达到此值后空闲资源将被移
		 *
		 * @since 1.0.0
		 */
		private long minEvictableIdleTimeMillis = 300000L;
		/**
		 * 连接池连接用尽后执行的动作
		 *
		 * @since 1.0.0
		 */
		private WhenExhaustedAction whenExhaustedAction = WhenExhaustedAction.BLOCK;
		/**
		 * 连接池没有对象返回时，最大等待时间(分钟)
		 *
		 * @since 1.0.0
		 */
		private Duration maxWait = Duration.ofMinutes(5);
		/**
		 * 定时对连接池中空闲的连接进行校验
		 *
		 * @since 1.0.0
		 */
		private boolean testWhileIdle = false;
		/**
		 * 空闲资源的检测周期(单位为毫秒)
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
}
