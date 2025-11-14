package io.github.pangju666.framework.boot.autoconfigure.image;

import org.gm4java.engine.support.WhenExhaustedAction;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pangju.image")
public class ImageProperties {
	private Type type = Type.IMAGEIO;
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

	public enum Type {
		GM,
		IMAGEIO
	}

	public static class GM {
		private Pool pool = new Pool();
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

	public static class Pool {
		/**
		 * 连接池最大活跃数
		 */
		private int maxActive = 4;
		/**
		 * 连接池最大空闲连接数
		 */
		private int maxIdle = 4;
		/**
		 * 连接池最小空闲连接数
		 */
		private int minIdle = 2;
		/**
		 * 资源池中资源最小空闲时间(单位为毫秒)，达到此值后空闲资源将被移
		 */
		private long minEvictableIdleTimeMillis = 300000L;
		/**
		 * 连接池连接用尽后执行的动作
		 */
		private WhenExhaustedAction whenExhaustedAction = WhenExhaustedAction.BLOCK;
		/**
		 * 连接池没有对象返回时，最大等待时间(分钟)
		 */
		private Duration maxWait = Duration.ofMinutes(5);
		/**
		 * 定时对线程池中空闲的链接进行校验
		 */
		private boolean testWhileIdle = false;
		/**
		 * 空闲资源的检测周期(单位为毫秒)
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
