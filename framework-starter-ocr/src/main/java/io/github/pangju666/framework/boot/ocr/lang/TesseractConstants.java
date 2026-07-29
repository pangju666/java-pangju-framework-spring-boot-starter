/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.framework.boot.ocr.lang;

import org.apache.commons.exec.Executor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * Tesseract OCR常量类。
 * <p>
 * 继承自{@link io.github.pangju666.commons.tesseract.lang.TesseractConstants}，
 * 提供Tesseract OCR引擎的默认配置常量，特别是对象池配置。
 * </p>
 *
 * <p><strong>默认对象池配置</strong></p>
 * <ul>
 *   <li>最大总实例数：CPU核心数（CPU密集型OCR优化）</li>
 *   <li>最大空闲实例数：CPU核心数</li>
 *   <li>最小空闲实例数：0（服务预热）</li>
 *   <li>最大等待时间：3秒（防止线程堆积）</li>
 *   <li>最小驱逐空闲时间：5分钟（内存管控）</li>
 *   <li>驱逐扫描间隔：1分钟</li>
 *   <li>借出前校验：关闭（提升性能）</li>
 *   <li>归还后校验：关闭（提升性能）</li>
 *   <li>空闲时校验：开启（兜底失效实例）</li>
 *   <li>池耗尽时阻塞：开启（默认行为）</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TesseractConstants extends io.github.pangju666.commons.tesseract.lang.TesseractConstants {
	/**
	 * 默认Tesseract对象池配置。
	 * <p>
	 * 基于CPU核心数进行优化配置，适用于CPU密集型的OCR场景。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public static final GenericObjectPoolConfig<Executor> DEFAULT_TESSERACT_POOL_CONFIG = new GenericObjectPoolConfig<>();

	static {
		int cpuCoreCount = Runtime.getRuntime().availableProcessors();
		// 最大总实例数：CPU密集型OCR，常规服务器推荐8
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxTotal(cpuCoreCount);
		// 最大空闲实例数
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxIdle(cpuCoreCount);
		// 最小常驻空闲实例（服务预热）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMinIdle(0);

		// 无可用实例时，等待1分钟后超时（防止线程堆积）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxWait(Duration.ofSeconds(3));
		// 空闲实例60分钟未使用则回收（常规内存管控）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMinEvictableIdleDuration(Duration.ofMinutes(5));
		// 每30秒执行一次空闲实例扫描淘汰
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTimeBetweenEvictionRuns(Duration.ofMinutes(1));
		// 关闭软空闲时间，只用固定时长驱逐
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setSoftMinEvictableIdleDuration(null);

		// 借出前校验实例有效性
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestOnBorrow(false);
		// 归还后不额外校验，提升性能
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestOnReturn(false);
		// 定时空闲校验，兜底失效实例，不影响主流程性能
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestWhileIdle(true);
		// 池耗尽时阻塞请求（对象池默认行为）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setBlockWhenExhausted(true);
	}

	/**
	 * 私有构造函数，防止实例化。
	 *
	 * @since 2.1.0
	 */
	protected TesseractConstants() {
	}
}
