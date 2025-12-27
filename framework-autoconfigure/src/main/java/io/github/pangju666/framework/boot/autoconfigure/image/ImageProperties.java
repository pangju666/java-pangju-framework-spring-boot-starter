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

import org.gm4java.engine.GMConnection;
import org.gm4java.engine.support.PooledGMService;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
 * <p><strong>属性映射（含默认值）</strong></p>
 * <ul>
 *   <li>{@code pangju.image.type}：处理实现类型，默认 {@code IMAGEIO}。</li>
 *   
 *   <li>{@code pangju.image.gm.path}：GraphicsMagick 可执行文件路径，默认 {@code gm}（从系统环境解析）。</li>
 *   <li>{@code pangju.image.gm.pool.max-idle}：最大空闲连接数，默认 {@code 0}。</li>
 *   <li>{@code pangju.image.gm.pool.min-idle}：最小空闲连接数，默认 {@code 0}。</li>
 *   <li>{@code pangju.image.gm.pool.max-active}：最大活跃连接数，默认 {@code 16}（负数表示不限制）。</li>
 *   <li>{@code pangju.image.gm.pool.max-wait-mills}：连接耗尽时阻塞等待毫秒数，默认 {@code 5000}（≤0 表示无限期）。</li>
 *   <li>{@code pangju.image.gm.pool.when-exhausted-action}：耗尽动作（{@code FAIL}/{@code BLOCK}/{@code GROW}），默认 {@code FAIL}。</li>
 *   <li>{@code pangju.image.gm.pool.test-on-get}：获取连接时是否校验，默认 {@code true}。</li>
 *   <li>{@code pangju.image.gm.pool.test-on-return}：归还连接时是否校验，默认 {@code false}。</li>
 *   <li>{@code pangju.image.gm.pool.test-while-idle}：空闲时是否定期校验，默认 {@code false}。</li>
 *   <li>{@code pangju.image.gm.pool.time-between-eviction-runs-millis}：空闲资源检测周期毫秒数，默认 {@code 30000}。</li>
 *   <li>{@code pangju.image.gm.pool.num-tests-per-eviction-run}：每次检测的最大连接数，默认 {@code 3}（负数表示按比例）。</li>
 *   <li>{@code pangju.image.gm.pool.min-evictable-idle-time-millis}：空闲最短驱逐毫秒数，默认 {@code 1800000}（30 分钟）。</li>
 *   <li>{@code pangju.image.gm.pool.soft-min-evictable-idle-time-millis}：软驱逐空闲毫秒数，默认 {@code -1}（禁用）。</li>
 *   <li>{@code pangju.image.gm.pool.lifo}：是否启用 LIFO，默认 {@code true}。</li>
 *   <li>{@code pangju.image.gm.pool.evict-after-number-of-use}：进程最大使用次数后驱逐，默认 {@code 100}（≤0 表示禁用）。</li>
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
	private GraphicsMagick gm = new GraphicsMagick();

	public GraphicsMagick getGm() {
		return gm;
	}

	public void setGm(GraphicsMagick graphicsMagick) {
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
	 * GraphicsMagick 相关配置
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static class GraphicsMagick {
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
		 * <p>默认值：{@code gm}（表示从系统环境变量读取）</p>
		 *
		 * @since 1.0.0
		 */
		private String path = "gm";

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
		 * 连接池中允许的最大空闲 {@link GMConnection} 实例数。
		 *
		 * <p>在高负载系统中，若该值设置过低，可能出现连接被销毁后几乎立即又创建新连接的情况。
		 * 这是因为活跃线程暂时归还连接的速度快于请求速度，导致空闲连接数短暂超过此上限。
		 * 对于高负载系统，最佳值需根据实际情况调整，默认值是一个良好的起点。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-idle}</p>
		 * <p>默认值：{@code 0}</p>
		 *
		 * @since 1.0.0
		 */
		private int maxIdle = 0;

		/**
		 * 连接池中允许的最小空闲 {@link GMConnection} 实例数。
		 *
		 * <p>当空闲连接数低于此值时（且驱逐线程已启用），驱逐线程会尝试创建新连接以维持最小空闲数量。
		 * 注意：若当前活跃连接数与空闲连接数之和已达到 {@link #maxActive}，则不会创建新连接。
		 * 此设置仅在驱逐线程启用时生效（即 {@link #timeBetweenEvictionRunsMillis} > 0）。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.min-idle}</p>
		 * <p>默认值：{@code 0}</p>
		 *
		 * @since 1.0.0
		 */
		private int minIdle = 0;

		/**
		 * 连接池在同一时间可分配的 {@link GMConnection} 实例总数上限（包括正在使用和空闲等待的）。
		 *
		 * <p>设为负数表示无限制。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-active}</p>
		 * <p>默认值：{@code 16}</p>
		 *
		 * @since 1.0.0
		 */
		private int maxActive = 16;

		/**
		 * 当连接池耗尽且 {@link #whenExhaustedAction} 为 {@link org.gm4java.engine.support.WhenExhaustedAction#BLOCK} 时，
		 * {@link PooledGMService#getConnection()} 方法最多阻塞等待的时间（毫秒）。
		 *
		 * <p>若该值 ≤ 0，则表示无限期阻塞，直到有可用连接。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.max-wait}</p>
		 * <p>默认值：{@code 5000}（表示 5 秒）</p>
		 *
		 * @since 1.0.0
		 */
		private long maxWaitMills = 5000;

		/**
		 * 当连接池耗尽（即活跃连接数已达 {@link #maxActive}）时，{@link PooledGMService#getConnection()} 应采取的行为。
		 *
		 * <p>可选值包括：阻塞（BLOCK）、抛异常（FAIL）、创建新连接（GROW）等。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.when-exhausted-action}</p>
		 * <p>默认值：{@link org.gm4java.engine.support.WhenExhaustedAction#FAIL}</p>
		 *
		 * @since 1.0.0
		 */
		private WhenExhaustedAction whenExhaustedAction = WhenExhaustedAction.FAIL;

		/**
		 * 是否在从连接池获取 {@link GMConnection} 时进行有效性校验。
		 *
		 * <p>若启用且校验失败，该连接将被丢弃，并尝试获取下一个有效连接。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.test-on-get}</p>
		 * <p>默认值：{@code true}</p>
		 *
		 * @since 1.0.0
		 */
		private boolean testOnGet = true;

		/**
		 * 是否在将 {@link GMConnection} 归还到连接池时进行有效性校验。
		 *
		 * <p>若启用且校验失败，该连接将不会被放回池中，而是直接销毁。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.test-on-return}</p>
		 * <p>默认值：{@code false}</p>
		 *
		 * @since 1.0.0
		 */
		private boolean testOnReturn = false;

		/**
		 * 是否在空闲连接驱逐线程运行时对空闲的 {@link GMConnection} 进行有效性校验。
		 *
		 * <p>若启用且校验失败，该连接将从池中移除。此功能依赖驱逐线程启用（即 {@link #timeBetweenEvictionRunsMillis} > 0）。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.test-while-idle}</p>
		 * <p>默认值：{@code false}</p>
		 *
		 * @since 1.0.0
		 */
		private boolean testWhileIdle = false;

		/**
		 * 空闲连接驱逐线程两次运行之间的间隔时间（毫秒）。
		 *
		 * <p>若该值 ≤ 0，则不启动驱逐线程。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.time-between-eviction-runs-millis}</p>
		 * <p>默认值：{@code 30000}（表示 30 秒）</p>
		 *
		 * @since 1.0.0
		 */
		private long timeBetweenEvictionRunsMillis = 30000;

		/**
		 * 每次运行空闲连接驱逐线程时，最多检查的空闲 {@link GMConnection} 数量。
		 *
		 * <p>若为负数（如 -3），则每次检查约 1/3 的空闲连接；若为正数，则取该值与当前空闲连接数的较小者。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.num-tests-per-eviction-run}</p>
		 * <p>默认值：{@code 3}</p>
		 *
		 * @since 1.0.0
		 */
		private int numTestsPerEvictionRun = 3;

		/**
		 * 一个 {@link GMConnection} 在池中空闲的最短时间（毫秒），超过此时间才可能被驱逐线程回收。
		 *
		 * <p>若该值 ≤ 0，则不会因空闲时间过长而被驱逐。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.min-evictable-idle-time-millis}</p>
		 * <p>默认值：{@code 1800000}（即 30 分钟）</p>
		 *
		 * @since 1.0.0
		 */
		private long minEvictableIdleTimeMillis = 1000L * 60L * 30L;

		/**
		 * 一个 {@link GMConnection} 在池中空闲的最短时间（毫秒），超过此时间才可能被驱逐线程回收，
		 * 但前提是池中空闲连接数仍不少于 {@link #minIdle}。
		 *
		 * <p>此策略比 {@link #minEvictableIdleTimeMillis} 更“温和”，用于避免在低负载时过度回收连接。
		 * 若该值 ≤ 0，则此策略不生效。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.soft-min-evictable-idle-time-millis}</p>
		 * <p>默认值：{@code -1}（表示禁用软驱逐策略）</p>
		 *
		 * @since 1.0.0
		 */
		private long softMinEvictableIdleTimeMillis = -1;

		/**
		 * 是否启用 LIFO（后进先出）策略。
		 *
		 * <p>若为 {@code true}，则从池中获取空闲连接时优先返回最近归还的连接；
		 * 若为 {@code false}，则按 FIFO（先进先出）顺序返回连接。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.lifo}</p>
		 * <p>默认值：{@code true}</p>
		 *
		 * @since 1.0.0
		 */
		private boolean lifo = true;

		/**
		 * 每个 GraphicsMagick 进程在被销毁前最多可执行命令的次数。
		 *
		 * <p>设为非正数表示禁用此功能（默认行为）。
		 * 注意：此限制并非严格保证，因为连接一旦被客户端获取，可在归还前执行任意多次命令；
		 * 实际的驱逐仅发生在连接被获取或归还时检查使用次数。</p>
		 *
		 * <p>对应属性：{@code pangju.image.gm.pool.evict-after-number-of-use}</p>
		 * <p>默认值：{@code 100}</p>
		 *
		 * @since 1.0.0
		 */
		private int evictAfterNumberOfUse = 100;

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

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}

		public long getMaxWaitMills() {
			return maxWaitMills;
		}

		public void setMaxWaitMills(long maxWaitMills) {
			this.maxWaitMills = maxWaitMills;
		}

		public WhenExhaustedAction getWhenExhaustedAction() {
			return whenExhaustedAction;
		}

		public void setWhenExhaustedAction(WhenExhaustedAction whenExhaustedAction) {
			this.whenExhaustedAction = whenExhaustedAction;
		}

		public boolean isTestOnGet() {
			return testOnGet;
		}

		public void setTestOnGet(boolean testOnGet) {
			this.testOnGet = testOnGet;
		}

		public boolean isTestOnReturn() {
			return testOnReturn;
		}

		public void setTestOnReturn(boolean testOnReturn) {
			this.testOnReturn = testOnReturn;
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

		public int getNumTestsPerEvictionRun() {
			return numTestsPerEvictionRun;
		}

		public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
			this.numTestsPerEvictionRun = numTestsPerEvictionRun;
		}

		public long getMinEvictableIdleTimeMillis() {
			return minEvictableIdleTimeMillis;
		}

		public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
			this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		}

		public long getSoftMinEvictableIdleTimeMillis() {
			return softMinEvictableIdleTimeMillis;
		}

		public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
			this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
		}

		public boolean isLifo() {
			return lifo;
		}

		public void setLifo(boolean lifo) {
			this.lifo = lifo;
		}

		public int getEvictAfterNumberOfUse() {
			return evictAfterNumberOfUse;
		}

		public void setEvictAfterNumberOfUse(int evictAfterNumberOfUse) {
			this.evictAfterNumberOfUse = evictAfterNumberOfUse;
		}
	}

	public enum WhenExhaustedAction {
		/**
		 * 抛出一个 {@link java.util.NoSuchElementException}.
		 */
		FAIL,
		/**
		 * 阻塞直到有新的或空闲的连接可用。或者如果 maxWait 为正且通过则失败。
		 */
		BLOCK,
		/**
		 * 创建一个新连接并返回它（本质上使 maxActive 变得毫无意义）。
		 */
		GROW
	}
}
