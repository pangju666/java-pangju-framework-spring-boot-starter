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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * OCR自动配置类。
 * <p>
 * OCR功能的主自动配置入口，负责导入所有OCR相关的配置类，并启用OCR配置属性。
 * 支持基于JNI的Tesseract API和基于命令行的Tesseract CLI两种OCR引擎实现。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持多种OCR引擎：Tesseract API（JNI）和Tesseract CLI（命令行）</li>
 *   <li>自动配置对象池管理，提升性能</li>
 *   <li>支持自定义DPI、语言、页面分割模式等参数</li>
 *   <li>提供统一的OCR模板接口，简化使用</li>
 * </ul>
 *
 * <p><strong>OCR引擎选择</strong></p>
 * <ul>
 *   <li>{@code TESSERACT}：基于JNI的Tesseract API实现，性能更高，需要本地库支持</li>
 *   <li>{@code TESSERACT_CLI}：基于命令行的Tesseract CLI实现，兼容性更好，需要安装Tesseract CLI</li>
 * </ul>
 *
 * <p><strong>导入的配置类</strong></p>
 * <ul>
 *   <li>{@link TesseractConfiguration}：Tesseract API配置，当engine为TESSERACT时生效</li>
 *   <li>{@link TesseractCliConfiguration}：Tesseract CLI配置，当engine为TESSERACT_CLI时生效</li>
 * </ul>
 *
 * <p><strong>配置属性</strong></p>
 * <p>启用{@link OcrProperties}配置属性绑定，配置前缀为{@code pangju.ocr}。</p>
 * <ul>
 *   <li>{@code pangju.ocr.engine}：OCR引擎类型（默认：TESSERACT）</li>
 *   <li>{@code pangju.ocr.tesseract}：Tesseract API配置</li>
 *   <li>{@code pangju.ocr.tesseract-cli}：Tesseract CLI配置</li>
 * </ul>
 *
 * @since 2.1.0
 */
@AutoConfiguration
@Import({TesseractConfiguration.class, TesseractCliConfiguration.class})
@EnableConfigurationProperties(OcrProperties.class)
public class OcrAutoConfiguration {
}
