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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.tesseract.io.resource.TesseractResource;
import io.github.pangju666.framework.boot.ocr.autoconfigure.OcrProperties;
import io.github.pangju666.framework.boot.ocr.core.OcrTemplate;
import io.github.pangju666.framework.boot.ocr.exception.OcrEngineException;
import io.github.pangju666.framework.boot.ocr.exception.OcrException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Tesseract CLI OCR模板实现类。
 * <p>
 * 基于Tesseract命令行工具实现OCR识别功能，使用对象池管理命令行执行器实例。
 * 通过调用Tesseract CLI进程进行图像文字识别，支持丰富的命令行参数配置。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>使用对象池管理命令行执行器实例，提升性能</li>
 *   <li>支持自定义DPI参数优化识别精度</li>
 *   <li>支持配置语言、页面分割模式（PSM）、OCR引擎模式（OEM）等参数</li>
 *   <li>支持自定义tessdata目录、用户词典和用户模式文件</li>
 *   <li>使用临时文件处理输入输出，自动清理临时文件</li>
 * </ul>
 *
 * @since 2.1.0
 */
public class TesseractCliOcrTemplate implements OcrTemplate {
	/**
	 * 日志记录器。
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TesseractCliOcrTemplate.class);

	/**
	 * 临时文件前缀。
	 *
	 * @since 2.1.0
	 */
	protected static final String TEMP_FILE_PREFIX = "tesseract-tmp-";
	/**
	 * 输出文件扩展名。
	 *
	 * @since 2.1.0
	 */
	protected static final String OUTPUT_FILE_EXTENSION = "txt";

	/**
	 * 命令行执行器对象池。
	 *
	 * @since 2.1.0
	 */
	protected final GenericObjectPool<Executor> pool;
	/**
	 * OCR配置属性。
	 *
	 * @since 2.1.0
	 */
	protected final OcrProperties properties;

	/**
	 * 构造函数。
	 *
	 * @param pool       命令行执行器对象池
	 * @param properties OCR配置属性
	 * @since 2.1.0
	 */
	public TesseractCliOcrTemplate(GenericObjectPool<Executor> pool, OcrProperties properties) {
		this.pool = pool;
		this.properties = properties;
	}

	/**
	 * 对图像资源进行OCR识别。
	 * <p>
	 * 使用Tesseract CLI进行图像文字识别，处理流程如下：
	 * </p>
	 * <ol>
	 *   <li>将IOResource转换为TesseractResource并获取文件路径</li>
	 *   <li>创建临时输出文件路径</li>
	 *   <li>构建Tesseract命令行参数（DPI、语言、PSM、OEM等）</li>
	 *   <li>从对象池中获取执行器实例</li>
	 *   <li>执行Tesseract命令行进程</li>
	 *   <li>读取识别结果文本</li>
	 *   <li>将执行器实例归还到对象池</li>
	 *   <li>清理临时输出文件</li>
	 * </ol>
	 *
	 * @param resource 图像资源
	 * @param dpi      每英寸点数（DPI），可为null表示不设置
	 * @return 识别出的文本内容
	 * @throws OcrException       读取图像资源失败、进程执行失败或读取结果失败时抛出
	 * @throws OcrEngineException 从对象池获取实例失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public String ocrImage(IOResource resource, @Nullable Integer dpi) {
		Executor executor = null;
		CommandLine commandLine = new CommandLine(FilenameUtils.separatorsToUnix(properties.getTesseractCli().getPath()));

		try (TesseractResource tesseractResource = new TesseractResource(resource)) {
			commandLine.addArgument(FilenameUtils.separatorsToUnix(tesseractResource.getFile().getAbsolutePath()));

			String outputFilePath = FileUtils.getTempDirectoryPath() + TEMP_FILE_PREFIX + UUID.randomUUID();
			File outputFile = new File(outputFilePath + FilenameUtils.EXTENSION_SEPARATOR + OUTPUT_FILE_EXTENSION);
			commandLine.addArgument(FilenameUtils.separatorsToUnix(outputFilePath));

			try {
				if (Objects.nonNull(dpi) && dpi > 0) {
					commandLine.addArgument("--dpi")
						.addArgument(dpi.toString());
				}
				if (StringUtils.isNotBlank(properties.getTesseractCli().getLanguage())) {
					commandLine.addArgument("-l")
						.addArgument(properties.getTesseractCli().getLanguage());
				}
				if (Objects.nonNull(properties.getTesseractCli().getPsm())) {
					commandLine.addArgument("--psm")
						.addArgument(String.valueOf(properties.getTesseractCli().getPsm().mode));
				}
				if (Objects.nonNull(properties.getTesseractCli().getOem())) {
					commandLine.addArgument("--oem")
						.addArgument(String.valueOf(properties.getTesseractCli().getOem().mode));
				}
				if (StringUtils.isNotBlank(properties.getTesseractCli().getDataPath())) {
					commandLine.addArgument("--tessdata-dir")
						.addArgument(FilenameUtils.separatorsToUnix(properties.getTesseractCli().getDataPath()));
				}
				if (StringUtils.isNotBlank(properties.getTesseractCli().getUserPatternsFilePath())) {
					commandLine.addArgument("--user-patterns")
						.addArgument(FilenameUtils.separatorsToUnix(properties.getTesseractCli().getUserPatternsFilePath()));
				}
				if (StringUtils.isNotBlank(properties.getTesseractCli().getUserWordsFilePath())) {
					commandLine.addArgument("--user-words")
						.addArgument(FilenameUtils.separatorsToUnix(properties.getTesseractCli().getUserWordsFilePath()));
				}

				try {
					executor = pool.borrowObject();
				} catch (Exception e) {
					throw new OcrEngineException("从对象池中获取 Tesseract 进程实例失败", e);
				}

				String executable = commandLine.getExecutable() + StringUtils.SPACE +
					StringUtils.join(commandLine.getArguments(), StringUtils.SPACE);

				try {
					int exitValue = executor.execute(commandLine);
					if (executor.isFailure(exitValue)) {
						return StringUtils.EMPTY;
					}
				} catch (IOException e) {
					throw new OcrException("Tesseract 进程执行失败，命令：" + executable, e);
				}

				LOGGER.info("Tesseract 进程执行成功，命令：{}", executable);
				return FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new OcrException("Tesseract 识别结果读取失败", e);
			} finally {
				if (!FileUtils.deleteQuietly(outputFile)) {
					LOGGER.error("临时输出文件删除失败，路径：{}", outputFile.getAbsolutePath());
				}

				if (Objects.nonNull(executor)) {
					pool.returnObject(executor);
				}
			}
		} catch (IOException e) {
			throw new OcrException("读取图像资源失败", e);
		}
	}
}
