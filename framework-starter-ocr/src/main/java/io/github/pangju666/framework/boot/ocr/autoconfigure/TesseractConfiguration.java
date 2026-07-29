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

import io.github.pangju666.commons.tesseract.factory.TessBaseAPIFactory;
import io.github.pangju666.framework.boot.ocr.core.OcrTemplate;
import io.github.pangju666.framework.boot.ocr.core.impl.TesseractOcrTemplate;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.tesseract.TessBaseAPI;
import org.bytedeco.tesseract.Tesseract;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Tesseract API自动配置类。
 * <p>
 * 配置基于JNI的Tesseract API的Bean注册，包括TessBaseAPI工厂、对象池和OCR模板。
 * </p>
 *
 * <p><strong>生效条件</strong></p>
 * <ul>
 *   <li>类路径中存在TessBaseAPIFactory、GenericObjectPool和Tesseract类</li>
 *   <li>配置{@code pangju.ocr.engine}为{@code TESSERACT}（默认值）</li>
 *   <li>容器中不存在对应的Bean实例</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({TessBaseAPIFactory.class, GenericObjectPool.class, Tesseract.class})
@ConditionalOnProperty(prefix = "pangju.ocr", name = "engine", havingValue = "TESSERACT", matchIfMissing = true)
class TesseractConfiguration {
	/**
	 * 配置TessBaseAPI工厂Bean。
	 * <p>
	 * 根据配置的数据路径和语言创建TessBaseAPI工厂实例。
	 * 如果配置了数据路径，则使用自定义数据路径和语言；
	 * 否则使用中文语言类型和默认配置。
	 * </p>
	 *
	 * @param properties OCR配置属性
	 * @return TessBaseAPI工厂实例
	 * @throws IOException 初始化失败时抛出
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(TessBaseAPIFactory.class)
	@Bean
	public TessBaseAPIFactory tessBaseAPIFactory(OcrProperties properties) throws IOException {
		if (StringUtils.hasText(properties.getTesseract().getDataPath())) {
			return new TessBaseAPIFactory(properties.getTesseract().getDataPath(), properties.getTesseract().getLanguage(),
				properties.getTesseract().getOem());
		} else {
			return new TessBaseAPIFactory(TessBaseAPIFactory.LanguageType.CHINESE,
				properties.getTesseract().getOem());
		}
	}

	/**
	 * 配置TessBaseAPI对象池Bean。
	 * <p>
	 * 使用TessBaseAPI工厂和配置的对象池配置创建对象池实例。
	 * </p>
	 *
	 * @param factory    TessBaseAPI工厂
	 * @param properties OCR配置属性
	 * @return TessBaseAPI对象池实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(name = "tessBaseAPIPool")
	@ConditionalOnBean(TessBaseAPIFactory.class)
	@Bean("tessBaseAPIPool")
	public GenericObjectPool<TessBaseAPI> tessBaseApiPool(TessBaseAPIFactory factory, OcrProperties properties) {
		return new GenericObjectPool<>(factory, properties.getTesseract().getPoolConfig());
	}

	/**
	 * 配置Tesseract OCR模板Bean。
	 * <p>
	 * 使用TessBaseAPI对象池和配置属性创建OCR模板实例。
	 * </p>
	 *
	 * @param pool       TessBaseAPI对象池
	 * @param properties OCR配置属性
	 * @return Tesseract OCR模板实例
	 * @since 2.1.0
	 */
	@ConditionalOnMissingBean(OcrTemplate.class)
	@ConditionalOnBean(name = "tessBaseAPIPool")
	@Bean
	public TesseractOcrTemplate tesseractOcrTemplate(GenericObjectPool<TessBaseAPI> pool, OcrProperties properties) {
		return new TesseractOcrTemplate(pool, properties);
	}
}
