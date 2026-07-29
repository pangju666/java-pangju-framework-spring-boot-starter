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

package io.github.pangju666.framework.boot.ocr.factory;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.Objects;

/**
 * Tesseract CLI执行器工厂类。
 * <p>
 * 继承自{@link org.apache.commons.pool2.BasePooledObjectFactory}，
 * 用于创建和管理Tesseract CLI执行器的对象池。
 * 负责执行器的创建、包装、销毁和校验等生命周期管理。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>创建配置好的DefaultExecutor实例</li>
 *   <li>设置退出码为0表示成功</li>
 *   <li>销毁时清理进程监控器</li>
 *   <li>简单的对象有效性校验</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TesseractCliFactory extends BasePooledObjectFactory<Executor> {
	/**
	 * 创建新的Tesseract CLI执行器实例。
	 * <p>
	 * 使用DefaultExecutor.builder()创建执行器，
	 * 并设置退出码为0表示命令执行成功。
	 * </p>
	 *
	 * @return 新创建的执行器实例
	 * @since 2.1.0
	 */
	@Override
	public Executor create() {
		DefaultExecutor executor = DefaultExecutor.builder().get();
		executor.setExitValue(0);
		return executor;
	}

	/**
	 * 将执行器实例包装为池对象。
	 *
	 * @param obj 执行器实例
	 * @return 池对象包装
	 * @since 2.1.0
	 */
	@Override
	public PooledObject<Executor> wrap(Executor obj) {
		return new DefaultPooledObject<>(obj);
	}

	/**
	 * 销毁执行器实例。
	 * <p>
	 * 如果执行器存在进程监控器（Watchdog），则销毁其监控的进程。
	 * </p>
	 *
	 * @param p 池对象
	 * @since 2.1.0
	 */
	@Override
	public void destroyObject(PooledObject<Executor> p) {
		Executor executor = p.getObject();
		if (Objects.nonNull(executor.getWatchdog())) {
			executor.getWatchdog().destroyProcess();
		}
	}

	/**
	 * 校验执行器实例的有效性。
	 * <p>
	 * 简单校验执行器对象是否为null。
	 * </p>
	 *
	 * @param p 池对象
	 * @return 如果执行器对象不为null则返回true，否则返回false
	 * @since 2.1.0
	 */
	@Override
	public boolean validateObject(PooledObject<Executor> p) {
		return p.getObject() != null;
	}
}
