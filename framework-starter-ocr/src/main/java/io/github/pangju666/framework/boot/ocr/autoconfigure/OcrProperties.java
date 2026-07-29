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

import io.github.pangju666.commons.tesseract.enums.OcrEngineMode;
import io.github.pangju666.commons.tesseract.enums.PageSegmentationMode;
import io.github.pangju666.framework.boot.ocr.lang.TesseractConstants;
import org.apache.commons.exec.Executor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bytedeco.tesseract.TessBaseAPI;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OCR配置属性类。
 * <p>
 * 配置OCR引擎的相关参数，包括引擎类型、Tesseract配置和Tesseract CLI配置。
 * 支持基于JNI的Tesseract API和基于命令行的Tesseract CLI两种实现方式。
 * </p>
 *
 * <p><strong>配置前缀</strong></p>
 * <p>配置组前缀：{@code pangju.ocr}</p>
 *
 * <p><strong>主要配置项</strong></p>
 * <ul>
 *   <li>{@code engine}：OCR引擎类型（TESSERACT、TESSERACT_CLI）</li>
 *   <li>{@code tesseract}：Tesseract API配置（数据路径、语言、OEM、PSM、对象池配置）</li>
 *   <li>{@code tesseract-cli}：Tesseract CLI配置（路径、语言、OEM、PSM、对象池配置等）</li>
 * </ul>
 *
 * @since 2.1.0
 */
@ConfigurationProperties(prefix = "pangju.ocr")
public class OcrProperties {
	/**
	 * OCR引擎类型。
	 * <p>默认值：{@code TESSERACT}</p>
	 * <p>对应属性：{@code pangju.ocr.engine}</p>
	 *
	 * @since 2.1.0
	 */
	private Engine engine = Engine.TESSERACT;
	/**
	 * Tesseract API配置。
	 *
	 * @since 2.1.0
	 */
	private Tesseract tesseract = new Tesseract();
	/**
	 * Tesseract CLI配置。
	 *
	 * @since 2.1.0
	 */
	private TesseractCli tesseractCli = new TesseractCli();

	/**
	 * 获取OCR引擎类型。
	 *
	 * @return OCR引擎类型
	 * @since 2.1.0
	 */
	public Engine getEngine() {
		return engine;
	}

	/**
	 * 设置OCR引擎类型。
	 *
	 * @param engine OCR引擎类型
	 * @since 2.1.0
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * 获取Tesseract API配置。
	 *
	 * @return Tesseract API配置
	 * @since 2.1.0
	 */
	public Tesseract getTesseract() {
		return tesseract;
	}

	/**
	 * 设置Tesseract API配置。
	 *
	 * @param tesseract Tesseract API配置
	 * @since 2.1.0
	 */
	public void setTesseract(Tesseract tesseract) {
		this.tesseract = tesseract;
	}

	/**
	 * 获取Tesseract CLI配置。
	 *
	 * @return Tesseract CLI配置
	 * @since 2.1.0
	 */
	public TesseractCli getTesseractCli() {
		return tesseractCli;
	}

	/**
	 * 设置Tesseract CLI配置。
	 *
	 * @param tesseractCli Tesseract CLI配置
	 * @since 2.1.0
	 */
	public void setTesseractCli(TesseractCli tesseractCli) {
		this.tesseractCli = tesseractCli;
	}

	/**
	 * OCR引擎类型枚举。
	 * <p>
	 * 定义支持的OCR引擎实现方式。
	 * </p>
	 *
	 * <p><strong>可选值</strong></p>
	 * <ul>
	 *   <li>{@code TESSERACT}：基于JNI的Tesseract API实现</li>
	 *   <li>{@code TESSERACT_CLI}：基于命令行的Tesseract CLI实现</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public enum Engine {
		/**
		 * 基于JNI的Tesseract API实现。
		 *
		 * @since 2.1.0
		 */
		TESSERACT,
		/**
		 * 基于命令行的Tesseract CLI实现。
		 *
		 * @since 2.1.0
		 */
		TESSERACT_CLI,
	}

	/**
	 * Tesseract CLI配置类。
	 * <p>
	 * 配置Tesseract命令行工具的相关参数。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.ocr.tesseract-cli.path}：Tesseract可执行文件路径（默认：tesseract）</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.language}：识别语言</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.oem}：OCR引擎模式</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.psm}：页面分割模式</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.data-path}：tessdata数据目录路径</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.user-words-file-path}：用户词典文件路径</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.user-patterns-file-path}：用户模式文件路径</li>
	 *   <li>{@code pangju.ocr.tesseract-cli.pool-config}：执行器对象池配置</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class TesseractCli {
		/**
		 * Tesseract可执行文件路径。
		 * <p>默认值：{@code tesseract}</p>
		 *
		 * @since 2.1.0
		 */
		private String path = "tesseract";
		/**
		 * 识别语言。
		 *
		 * @since 2.1.0
		 */
		private @Nullable String language;
		/**
		 * OCR引擎模式。
		 *
		 * @since 2.1.0
		 */
		private @Nullable OcrEngineMode oem;
		/**
		 * 页面分割模式。
		 *
		 * @since 2.1.0
		 */
		private @Nullable PageSegmentationMode psm;
		/**
		 * tessdata数据目录路径。
		 *
		 * @since 2.1.0
		 */
		private @Nullable String dataPath;
		/**
		 * 用户词典文件路径。
		 *
		 * @since 2.1.0
		 */
		private @Nullable String userWordsFilePath;
		/**
		 * 用户模式文件路径。
		 *
		 * @since 2.1.0
		 */
		private @Nullable String userPatternsFilePath;
		/**
		 * 执行器对象池配置。
		 *
		 * @since 2.1.0
		 */
		private GenericObjectPoolConfig<Executor> poolConfig = TesseractConstants.DEFAULT_TESSERACT_POOL_CONFIG.clone();

		/**
		 * 获取Tesseract可执行文件路径。
		 *
		 * @return Tesseract可执行文件路径
		 * @since 2.1.0
		 */
		public String getPath() {
			return path;
		}

		/**
		 * 设置Tesseract可执行文件路径。
		 *
		 * @param path Tesseract可执行文件路径
		 * @since 2.1.0
		 */
		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * 获取识别语言。
		 *
		 * @return 识别语言
		 * @since 2.1.0
		 */
		public @Nullable String getLanguage() {
			return language;
		}

		/**
		 * 设置识别语言。
		 *
		 * @param language 识别语言
		 * @since 2.1.0
		 */
		public void setLanguage(@Nullable String language) {
			this.language = language;
		}

		/**
		 * 获取OCR引擎模式。
		 *
		 * @return OCR引擎模式
		 * @since 2.1.0
		 */
		public @Nullable OcrEngineMode getOem() {
			return oem;
		}

		/**
		 * 设置OCR引擎模式。
		 *
		 * @param oem OCR引擎模式
		 * @since 2.1.0
		 */
		public void setOem(@Nullable OcrEngineMode oem) {
			this.oem = oem;
		}

		/**
		 * 获取页面分割模式。
		 *
		 * @return 页面分割模式
		 * @since 2.1.0
		 */
		public @Nullable PageSegmentationMode getPsm() {
			return psm;
		}

		/**
		 * 设置页面分割模式。
		 *
		 * @param psm 页面分割模式
		 * @since 2.1.0
		 */
		public void setPsm(@Nullable PageSegmentationMode psm) {
			this.psm = psm;
		}

		/**
		 * 获取tessdata数据目录路径。
		 *
		 * @return tessdata数据目录路径
		 * @since 2.1.0
		 */
		public @Nullable String getDataPath() {
			return dataPath;
		}

		/**
		 * 设置tessdata数据目录路径。
		 *
		 * @param dataPath tessdata数据目录路径
		 * @since 2.1.0
		 */
		public void setDataPath(@Nullable String dataPath) {
			this.dataPath = dataPath;
		}

		/**
		 * 获取用户词典文件路径。
		 *
		 * @return 用户词典文件路径
		 * @since 2.1.0
		 */
		public @Nullable String getUserWordsFilePath() {
			return userWordsFilePath;
		}

		/**
		 * 设置用户词典文件路径。
		 *
		 * @param userWordsFilePath 用户词典文件路径
		 * @since 2.1.0
		 */
		public void setUserWordsFilePath(@Nullable String userWordsFilePath) {
			this.userWordsFilePath = userWordsFilePath;
		}

		/**
		 * 获取用户模式文件路径。
		 *
		 * @return 用户模式文件路径
		 * @since 2.1.0
		 */
		public @Nullable String getUserPatternsFilePath() {
			return userPatternsFilePath;
		}

		/**
		 * 设置用户模式文件路径。
		 *
		 * @param userPatternsFilePath 用户模式文件路径
		 * @since 2.1.0
		 */
		public void setUserPatternsFilePath(@Nullable String userPatternsFilePath) {
			this.userPatternsFilePath = userPatternsFilePath;
		}

		/**
		 * 获取执行器对象池配置。
		 *
		 * @return 执行器对象池配置
		 * @since 2.1.0
		 */
		public GenericObjectPoolConfig<Executor> getPoolConfig() {
			return poolConfig;
		}

		/**
		 * 设置执行器对象池配置。
		 *
		 * @param poolConfig 执行器对象池配置
		 * @since 2.1.0
		 */
		public void setPoolConfig(GenericObjectPoolConfig<Executor> poolConfig) {
			this.poolConfig = poolConfig;
		}
	}

	/**
	 * Tesseract API配置类。
	 * <p>
	 * 配置基于JNI的Tesseract API的相关参数。
	 * </p>
	 *
	 * <p><strong>配置属性</strong></p>
	 * <ul>
	 *   <li>{@code pangju.ocr.tesseract.data-path}：tessdata数据目录路径</li>
	 *   <li>{@code pangju.ocr.tesseract.language}：识别语言（默认：eng）</li>
	 *   <li>{@code pangju.ocr.tesseract.oem}：OCR引擎模式（默认：DEFAULT）</li>
	 *   <li>{@code pangju.ocr.tesseract.psm}：页面分割模式</li>
	 *   <li>{@code pangju.ocr.tesseract.pool-config}：TessBaseAPI对象池配置</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	public static class Tesseract {
		/**
		 * tessdata数据目录路径。
		 *
		 * @since 2.1.0
		 */
		private @Nullable String dataPath;
		/**
		 * 识别语言。
		 *
		 * @since 2.1.0
		 */
		private String language = "eng";
		/**
		 * OCR引擎模式。
		 * <p>默认值：{@code DEFAULT}</p>
		 *
		 * @since 2.1.0
		 */
		private OcrEngineMode oem = OcrEngineMode.DEFAULT;
		/**
		 * 页面分割模式。
		 *
		 * @since 2.1.0
		 */
		private @Nullable PageSegmentationMode psm;
		/**
		 * TessBaseAPI对象池配置。
		 *
		 * @since 2.1.0
		 */
		private GenericObjectPoolConfig<TessBaseAPI> poolConfig = TesseractConstants.DEFAULT_TESS_BASE_API_POOL_CONFIG.clone();

		/**
		 * 获取TessBaseAPI对象池配置。
		 *
		 * @return TessBaseAPI对象池配置
		 * @since 2.1.0
		 */
		public GenericObjectPoolConfig<TessBaseAPI> getPoolConfig() {
			return poolConfig;
		}

		/**
		 * 设置TessBaseAPI对象池配置。
		 *
		 * @param poolConfig TessBaseAPI对象池配置
		 * @since 2.1.0
		 */
		public void setPoolConfig(GenericObjectPoolConfig<TessBaseAPI> poolConfig) {
			this.poolConfig = poolConfig;
		}

		/**
		 * 获取tessdata数据目录路径。
		 *
		 * @return tessdata数据目录路径
		 * @since 2.1.0
		 */
		public @Nullable String getDataPath() {
			return dataPath;
		}

		/**
		 * 设置tessdata数据目录路径。
		 *
		 * @param dataPath tessdata数据目录路径
		 * @since 2.1.0
		 */
		public void setDataPath(@Nullable String dataPath) {
			this.dataPath = dataPath;
		}

		/**
		 * 获取识别语言。
		 *
		 * @return 识别语言
		 * @since 2.1.0
		 */
		public String getLanguage() {
			return language;
		}

		/**
		 * 设置识别语言。
		 *
		 * @param language 识别语言
		 * @since 2.1.0
		 */
		public void setLanguage(String language) {
			this.language = language;
		}

		/**
		 * 获取OCR引擎模式。
		 *
		 * @return OCR引擎模式
		 * @since 2.1.0
		 */
		public OcrEngineMode getOem() {
			return oem;
		}

		/**
		 * 设置OCR引擎模式。
		 *
		 * @param oem OCR引擎模式
		 * @since 2.1.0
		 */
		public void setOem(OcrEngineMode oem) {
			this.oem = oem;
		}

		/**
		 * 获取页面分割模式。
		 *
		 * @return 页面分割模式
		 * @since 2.1.0
		 */
		public @Nullable PageSegmentationMode getPsm() {
			return psm;
		}

		/**
		 * 设置页面分割模式。
		 *
		 * @param psm 页面分割模式
		 * @since 2.1.0
		 */
		public void setPsm(@Nullable PageSegmentationMode psm) {
			this.psm = psm;
		}
	}
}
