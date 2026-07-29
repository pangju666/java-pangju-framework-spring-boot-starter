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

package io.github.pangju666.framework.boot.ocr.autoconfigure;

import io.github.pangju666.framework.boot.ocr.core.OcrTemplate;
import io.github.pangju666.framework.boot.ocr.core.impl.TesseractCliOcrTemplate;
import io.github.pangju666.framework.boot.ocr.factory.TesseractCliFactory;
import org.apache.commons.exec.Executor;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Tesseract CLI自动配置类。
 * <p>
 * 配置基于命令行的Tesseract CLI的Bean注册，包括CLI工厂、对象池和OCR模板。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>类路径中存在Executor类</li>
 *   <li>配置{@code pangju.ocr.engine}为{@code TESSERACT_CLI}</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Executor.class)
@ConditionalOnProperty(prefix = "pangju.ocr", name = "engine", havingValue = "TESSERACT_CLI")
class TesseractCliConfiguration {
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TesseractCliConfiguration.class);

	/**
	 * 配置Tesseract CLI工厂Bean。
	 * <p>
	 * 创建Tesseract CLI执行器工厂实例，用于管理命令行执行器的生命周期。
	 * </p>
	 *
	 * @return Tesseract CLI工厂实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(TesseractCliFactory.class)
	@Bean
	public TesseractCliFactory tesseractCliFactory() {
		return new TesseractCliFactory();
	}

	/**
	 * 配置Tesseract CLI执行器对象池Bean。
	 * <p>
	 * 使用Tesseract CLI工厂和配置的对象池配置创建执行器对象池实例。
	 * </p>
	 *
	 * @param factory Tesseract CLI工厂
	 * @param properties OCR配置属性
	 * @return 执行器对象池实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(name = "tesseractCliPool")
	@ConditionalOnBean(TesseractCliFactory.class)
	@Bean("tesseractCliPool")
	public GenericObjectPool<Executor> tesseractCliPool(TesseractCliFactory factory, OcrProperties properties) {
		return new GenericObjectPool<>(factory, properties.getTesseractCli().getPoolConfig());
	}

	/**
	 * 配置Tesseract CLI OCR模板Bean。
	 * <p>
	 * 使用执行器对象池和配置属性创建OCR模板实例。
	 * 在创建前验证Tesseract可执行文件路径是否已配置。
	 * 如果未配置有效路径，则记录错误日志并返回null，跳过Bean创建。
	 * </p>
	 *
	 * @param tesseractCliPool 执行器对象池
	 * @param properties OCR配置属性
	 * @return Tesseract CLI OCR模板实例，如果未配置路径则返回null
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(OcrTemplate.class)
	@ConditionalOnBean(name = "tesseractCliPool")
	@Bean
	public TesseractCliOcrTemplate tesseractCliOcrTemplate(GenericObjectPool<Executor> tesseractCliPool, OcrProperties properties) {
		if (!StringUtils.hasText(properties.getTesseractCli().getPath())) {
			LOGGER.error("未配置 Tesseract 进程可执行路径");
			// 未配置有效 Tesseract 路径，跳过创建
			return null;
		}
		return new TesseractCliOcrTemplate(tesseractCliPool, properties);
	}
}
