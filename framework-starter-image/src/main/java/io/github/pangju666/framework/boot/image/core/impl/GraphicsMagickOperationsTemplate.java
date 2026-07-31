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

package io.github.pangju666.framework.boot.image.core.impl;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.core.ImageOperationsTemplate;
import io.github.pangju666.framework.boot.image.exception.ImageEngineException;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.opeartions.GraphicsMagickOperations;
import io.github.pangju666.framework.boot.image.model.opeartions.ImageOperations;
import io.github.pangju666.framework.boot.image.utils.GraphicsMagickUtils;
import org.apache.commons.io.FilenameUtils;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * GraphicsMagick图像操作模板实现类。
 * <p>
 * 基于GraphicsMagick实现图像处理，支持丰富的图像变换、滤镜、水印和输出配置操作。
 * 使用连接池管理GraphicsMagick进程，提高性能和资源利用率。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>支持多种图像变换：自动方向、裁剪、缩放、旋转、翻转</li>
 *   <li>支持丰富的滤镜效果：模糊、高斯模糊、锐化、反锐化掩模</li>
 *   <li>支持水印功能：图像水印和文字水印</li>
 *   <li>支持输出配置：DPI设置、去除元数据、压缩质量和压缩方法</li>
 * </ul>
 *
 * <p><strong>使用注意事项</strong></p>
 * <ul>
 *   <li>需要安装GraphicsMagick软件</li>
 *   <li>使用{@link GraphicsMagickResource}作为图像资源类型</li>
 *   <li>通过临时文件实现输出流输出，处理完成后自动清理临时文件</li>
 *   <li>如果需要图像水印且需要其他变换操作，会先生成中间文件再执行composite命令</li>
 *   <li>GraphicsMagick输入/输出文件路径不支持中文或非ASCII字符，需要使用纯英文路径，否则可能导致命令执行失败</li>
 * </ul>
 *
 * @see PooledGMService
 * @see GraphicsMagickResource
 * @see GraphicsMagickOperations
 * @see GraphicsMagickUtils
 * @since 2.1.0
 */
public class GraphicsMagickOperationsTemplate implements ImageOperationsTemplate {
	/**
	 * 临时文件前缀。
	 *
	 * @since 2.1.0
	 */
	protected static final String TMP_FILE_PREFIX = "graphics-magick-tmp-";
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsMagickOperationsTemplate.class);

	/**
	 * GraphicsMagick连接池服务。
	 *
	 * @since 2.1.0
	 */
	protected final PooledGMService pooledGMService;

	/**
	 * 构造函数。
	 * <p>
	 * 使用GraphicsMagick连接池服务初始化图像操作模板。
	 * </p>
	 *
	 * @param pooledGMService GraphicsMagick连接池服务，不能为null
	 * @since 2.1.0
	 */
	public GraphicsMagickOperationsTemplate(PooledGMService pooledGMService) {
		this.pooledGMService = pooledGMService;
	}

	/**
	 * 处理图像资源并输出到输出流。
	 * <p>
	 * 通过临时文件实现输出流输出，处理完成后自动清理临时文件。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>创建临时文件用于存储处理结果</li>
	 *   <li>调用process方法处理图像资源到临时文件</li>
	 *   <li>将临时文件内容传输到输出流</li>
	 *   <li>清理临时文件</li>
	 * </ol>
	 *
	 * @param resource     图像资源，不能为null
	 * @param outputStream 输出流，不能为null
	 * @param outputFormat 输出格式，不能为空
	 * @param operations   图像操作配置，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不被支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像解析失败时抛出
	 * @throws ImageOperationException      图像操作异常，当图像操作失败时抛出
	 * @throws ImageEngineException         图像引擎异常，当GraphicsMagick执行失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public void process(IOResource resource, OutputStream outputStream, String outputFormat, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException, ImageEngineException {

		Assert.notNull(resource, "resource 不可为 null");
		Assert.notNull(operations, "operations 不可为 null");
		Assert.notNull(outputStream, "outputStream 不可为 null");
		Assert.hasText(outputFormat, "outputFormat 不可为空");

		File tmpOutputFile = new File(FileUtils.getTempDirectory(), TMP_FILE_PREFIX +
			UUID.randomUUID() + FilenameUtils.EXTENSION_SEPARATOR + outputFormat);

		process(resource, tmpOutputFile, operations);

		try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tmpOutputFile)) {
			inputStream.transferTo(outputStream);
		} catch (IOException e) {
			throw new ImageOperationException("图像输出失败", e);
		} finally {
			if (!FileUtils.deleteQuietly(tmpOutputFile)) {
				LOGGER.error("临时输出文件删除失败，路径：{}", tmpOutputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * 处理图像资源并输出到文件。
	 * <p>
	 * 根据是否需要图像水印，执行convert命令或composite命令。
	 * 如果需要图像水印且需要其他变换操作，会先生成中间文件再执行composite命令。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>检查输出格式是否支持</li>
	 *   <li>获取GraphicsMagick连接</li>
	 *   <li>将资源转换为{@link GraphicsMagickResource}</li>
	 *   <li>判断是否需要图像水印</li>
	 *   <li>如果需要图像水印且需要其他变换操作：
	 *     <ul>
	 *       <li>先生成中间文件执行变换操作</li>
	 *       <li>再执行composite命令添加水印</li>
	 *     </ul>
	 *   <li>如果只需要图像水印：直接执行composite命令</li>
	 *   <li>如果不需要图像水印：直接执行convert命令</li>
	 * </ol>
	 *
	 * @param resource   图像资源，不能为null
	 * @param outputFile 输出文件，不能为null
	 * @param operations 图像操作配置，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不被支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像解析失败时抛出
	 * @throws ImageOperationException      图像操作异常，当图像操作失败时抛出
	 * @throws ImageEngineException         图像引擎异常，当GraphicsMagick执行失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public void process(IOResource resource, File outputFile, ImageOperations<?> operations) throws UnsupportedResourceException,
		ImageParsingException, ImageOperationException, ImageEngineException {

		Assert.notNull(resource, "resource 不可为 null");
		Assert.notNull(operations, "operations 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		String outputFileExtension = FilenameUtils.getExtension(outputFile.getName());
		if (!canWrite(outputFileExtension)) {
			throw new UnsupportedResourceException("不支持输出为" + outputFileExtension + "格式");
		}

		GMConnection connection;
		try {
			connection = pooledGMService.getConnection();
		} catch (GMServiceException e) {
			throw new ImageEngineException("获取 GraphicsMagick 进程失败", e);
		}

		GraphicsMagickResource imageResource = null;
		try {
			imageResource = GraphicsMagickUtils.toGraphicsMagickResource(resource, connection);

			GraphicsMagickOperations imageOperations;
			if (operations instanceof GraphicsMagickOperations graphicsMagickOperations) {
				imageOperations = graphicsMagickOperations;
			} else {
				imageOperations = new GraphicsMagickOperations(operations);
			}

			// 如果不需要添加图片水印，则直接执行convert命令
			if (Objects.isNull(imageOperations.getWatermarkImage())) {
				try {
					GMOperation convertGMOperation = imageOperations.toConvertGMOperation(imageResource, outputFile);
					execute(connection, convertGMOperation);
				} catch (IOException e) {
					throw new ImageParsingException("图像读取失败", e);
				}
			} else {
				// 判断是否需要先执行convert命令输出中间文件
				if (imageOperations.isConvertRequired()) {
					File tmpOutputFile = new File(FileUtils.getTempDirectory(), TMP_FILE_PREFIX +
						UUID.randomUUID() + FilenameUtils.EXTENSION_SEPARATOR +
						FilenameUtils.getExtension(outputFile.getName()));

					try {
						GMOperation convertGMOperation = imageOperations.toConvertGMOperation(imageResource, tmpOutputFile,
							true);
						execute(connection, convertGMOperation);

						GMOperation compositeGMOperation = imageOperations.toCompositeGMOperation(
							new GraphicsMagickResource(tmpOutputFile, connection), outputFile);
						execute(connection, compositeGMOperation);
					} catch (GMServiceException e) {
						throw new ImageEngineException("与 GraphicsMagick 进程通信时出现错误", e);
					} catch (IOException e) {
						throw new ImageParsingException("图像读取失败", e);
					} finally {
						if (!FileUtils.deleteQuietly(tmpOutputFile)) {
							LOGGER.error("临时输出文件删除失败，路径：{}", tmpOutputFile.getAbsolutePath());
						}
					}
				} else {
					try {
						GMOperation compositeGMOperation = imageOperations.toCompositeGMOperation(imageResource, outputFile);
						execute(connection, compositeGMOperation);
					} catch (IOException e) {
						throw new ImageParsingException("图像读取失败", e);
					}
				}
			}
		} finally {
			try {
				connection.close();
			} catch (GMServiceException e) {
				LOGGER.error("GraphicsMagick 进程关闭失败", e);
			}

			try {
				if (Objects.nonNull(imageResource)) {
					imageResource.close();
				}
			} catch (IOException e) {
				LOGGER.error("GraphicsMagick 图像资源关闭失败", e);
			}
		}
	}

	/**
	 * 检查是否支持读取指定资源。
	 * <p>
	 * 支持读取GraphicsMagickResource类型的资源，以及可以转换为GraphicsMagickResource的资源。
	 * </p>
	 *
	 * @param resource 图像资源，不能为null
	 * @return 如果支持读取返回true，否则返回false
	 * @since 2.1.0
	 */
	@Override
	public boolean canRead(IOResource resource) {
		Assert.notNull(resource, "resource 不可为 null");

		if (resource instanceof GraphicsMagickResource) {
			return true;
		} else {
			try {
				new GraphicsMagickResource(resource, pooledGMService.getConnection());
				return true;
			} catch (IOException | GMServiceException | UnsupportedResourceException e) {
				return false;
			}
		}
	}

	/**
	 * 检查是否支持写入指定格式。
	 * <p>
	 * 检查格式是否在GraphicsMagick支持的写入格式列表中。
	 * </p>
	 *
	 * @param format 图像格式，不能为null
	 * @return 如果支持写入返回true，否则返回false
	 * @since 2.1.0
	 */
	@Override
	public boolean canWrite(String format) {
		Assert.hasText(format, "format 不可为空");

		return ImageConstants.GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMATS.contains(format.toLowerCase());
	}

	/**
	 * 执行GraphicsMagick命令。
	 * <p>
	 * 通过GMConnection执行GraphicsMagick命令，并记录执行日志。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>执行GraphicsMagick命令</li>
	 *   <li>记录执行结果日志</li>
	 * </ol>
	 *
	 * @param connection GraphicsMagick连接，不能为null
	 * @param operation  GMOperation对象，不能为null
	 * @throws IOException             IO异常，当文件操作失败时抛出
	 * @throws ImageEngineException    图像引擎异常，当进程通信失败时抛出
	 * @throws ImageOperationException 图像操作异常，当命令执行失败时抛出
	 * @since 2.1.0
	 */
	protected void execute(GMConnection connection, GMOperation operation) throws IOException {
		Assert.notNull(operation, "operation 不可为 null");
		Assert.notNull(connection, "connection 不可为 null");

		try {
			String result = connection.execute(operation.toString());
			LOGGER.info("GraphicsMagick 进程执行成功，命令：{}，结果：{}", operation, result);
		} catch (GMServiceException e) {
			throw new ImageEngineException("与 GraphicsMagick 进程通信时出现错误", e);
		} catch (GMException e) {
			throw new ImageOperationException("GraphicsMagick 命令: " + operation + " 执行失败", e);
		}
	}
}
