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

package io.github.pangju666.framework.boot.ocr.core.impl;

import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.tesseract.io.resource.TesseractResource;
import io.github.pangju666.commons.tesseract.model.TessBaseAPIOption;
import io.github.pangju666.commons.tesseract.utils.TesseractUtils;
import io.github.pangju666.framework.boot.ocr.autoconfigure.OcrProperties;
import io.github.pangju666.framework.boot.ocr.core.OcrTemplate;
import io.github.pangju666.framework.boot.ocr.exception.OcrEngineException;
import io.github.pangju666.framework.boot.ocr.exception.OcrException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Objects;

/**
 * Tesseract OCR模板实现类。
 * <p>
 * 基于Tesseract TessBaseAPI实现OCR识别功能，使用对象池管理TessBaseAPI实例。
 * 通过JNI调用Tesseract C++库进行图像文字识别，支持自定义DPI和页面分割模式。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>使用对象池管理TessBaseAPI实例，提升性能</li>
 *   <li>支持自定义DPI参数优化识别精度</li>
 *   <li>支持配置页面分割模式（PSM）</li>
 *   <li>自动处理资源获取和释放</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TesseractOcrTemplate implements OcrTemplate {
	/**
	 * TessBaseAPI对象池。
	 *
	 * @since 2.1.0
	 */
	protected final GenericObjectPool<TessBaseAPI> pool;
	/**
	 * OCR配置属性。
	 *
	 * @since 2.1.0
	 */
	protected final OcrProperties properties;

	/**
	 * 构造函数。
	 *
	 * @param pool TessBaseAPI对象池
	 * @param properties OCR配置属性
	 * @since 2.1.0
	 */
	public TesseractOcrTemplate(GenericObjectPool<TessBaseAPI> pool, OcrProperties properties) {
		this.pool = pool;
		this.properties = properties;
	}

	/**
	 * 对图像资源进行OCR识别。
	 * <p>
	 * 使用TessBaseAPI进行图像文字识别，处理流程如下：
	 * </p>
	 * <ol>
	 *   <li>将IOResource转换为TesseractResource</li>
	 *   <li>从对象池中获取TessBaseAPI实例</li>
	 *   <li>配置DPI和页面分割模式参数</li>
	 *   <li>执行OCR识别</li>
	 *   <li>将TessBaseAPI实例归还到对象池</li>
	 * </ol>
	 *
	 * @param resource 图像资源
	 * @param dpi 每英寸点数（DPI），可为null表示使用默认值
	 * @return 识别出的文本内容
	 * @throws OcrException 读取图像资源失败时抛出
	 * @throws OcrEngineException 从对象池获取实例失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public String ocrImage(IOResource resource, @Nullable Integer dpi) {
		Assert.notNull(resource, "resource 不可为 null");

		TesseractResource tesseractResource;
		try {
			tesseractResource = new TesseractResource(resource);
		} catch (IOException e) {
			throw new OcrException("读取图像资源失败", e);
		}

		TessBaseAPI tessBaseAPI = null;
		try {
			try {
				tessBaseAPI = pool.borrowObject();
			} catch (Exception e) {
				throw new OcrEngineException("从对象池中获取 TessBaseAPI 实例失败", e);
			}

			TessBaseAPIOption options = new TessBaseAPIOption();
			options.setPpi(dpi);
			options.setPsm(properties.getTesseract().getPsm());

			try (PIX image = tesseractResource.getPix()) {
				return TesseractUtils.ocrImage(tessBaseAPI, image, options);
			}
		} finally {
			if (Objects.nonNull(tessBaseAPI)) {
				pool.returnObject(tessBaseAPI);
			}
		}
	}
}
