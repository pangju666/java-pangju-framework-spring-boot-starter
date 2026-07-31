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

package io.github.pangju666.framework.boot.image.utils;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.CompressionType;
import io.github.pangju666.framework.boot.image.exception.ImageEngineException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.gm.IdentifyResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;
import org.gm4java.im4java.GMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

/**
 * GraphicsMagick工具类。
 * <p>
 * 提供使用GraphicsMagick引擎进行图像处理的工具方法，包括图像信息识别、资源转换等功能。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>图像信息识别：获取图像尺寸、格式、压缩类型、质量、EXIF方向等元数据</li>
 *   <li>资源转换：将IOResource转换为GraphicsMagickResource以便于GraphicsMagick处理</li>
 *   <li>自定义格式识别：支持使用GraphicsMagick格式占位符获取自定义图像信息</li>
 * </ul>
 *
 * <p><strong>使用场景</strong></p>
 * <ul>
 *   <li>图像元数据提取：获取图像的详细属性信息</li>
 *   <li>图像预处理：获取图像尺寸和方向信息以便于后续处理</li>
 *   <li>资源适配：将通用图像资源转换为GraphicsMagick专用资源</li>
 * </ul>
 *
 * <p><strong>使用注意事项</strong></p>
 * <ul>
 *   <li>需要GraphicsMagick引擎支持</li>
 *   <li>需要有效的GMConnection连接对象</li>
 *   <li>处理大图像时可能需要较长时间和较多内存</li>
 *   <li>GraphicsMagick输入文件路径不支持中文或非ASCII字符，需要使用纯英文路径，否则可能导致命令执行失败</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class GraphicsMagickUtils {
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsMagickUtils.class);

	/**
	 * 私有构造函数，防止实例化。
	 *
	 * @since 2.1.0
	 */
	protected GraphicsMagickUtils() {
	}

	/**
	 * 将IOResource转换为GraphicsMagickResource。
	 * <p>
	 * 如果资源已经是GraphicsMagickResource类型，直接返回。
	 * 否则创建新的GraphicsMagickResource实例。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>检查资源类型</li>
	 *   <li>如果是GraphicsMagickResource，直接返回</li>
	 *   <li>否则创建新的GraphicsMagickResource实例</li>
	 * </ol>
	 *
	 * @param resource   图像资源，不能为null
	 * @param connection GraphicsMagick连接，不能为null
	 * @return GraphicsMagickResource实例
	 * @throws ImageEngineException 图像引擎异常，当进程通信失败时抛出
	 * @throws ImageParsingException 图像解析异常，当图像读取失败时抛出
	 * @since 2.1.0
	 */
	public static GraphicsMagickResource toGraphicsMagickResource(final IOResource resource, final GMConnection connection) {
		Assert.notNull(resource, "resource 不可为 null");
		Assert.notNull(connection, "connection 不可为 null");

		if (resource instanceof GraphicsMagickResource graphicsMagickResource) {
			return graphicsMagickResource;
		} else {
			try {
				return new GraphicsMagickResource(resource, connection);
			} catch (GMServiceException e) {
				throw new ImageEngineException("与 GraphicsMagick 进程通信时出现错误", e);
			} catch (IOException e) {
				throw new ImageParsingException("图像读取失败", e);
			}
		}
	}

	/**
	 * 识别图像尺寸。
	 * <p>
	 * 使用GraphicsMagick的identify命令获取图像的宽度和高度，并解析EXIF方向信息。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>执行identify命令，获取宽度、高度和EXIF方向</li>
	 *   <li>解析EXIF方向，如果解析失败则使用默认值</li>
	 *   <li>解析宽度和高度，如果解析失败则抛出异常</li>
	 *   <li>返回包含尺寸和方向信息的ImageSize对象</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>返回的尺寸是图像的物理尺寸，不考虑EXIF方向</li>
	 *   <li>EXIF方向用于后续的自动方向校正</li>
	 *   <li>如果图像没有EXIF方向信息，使用默认值1（正常方向）</li>
	 * </ul>
	 *
	 * @param file       图像文件，不能为null
	 * @param connection GraphicsMagick连接对象，不能为null
	 * @return 图像尺寸对象，包含宽度、高度和EXIF方向
	 * @throws IOException           IO异常，当文件操作失败时抛出
	 * @throws GMServiceException    GraphicsMagick服务异常，当服务不可用时抛出
	 * @throws GMException           GraphicsMagick执行异常，当命令执行失败时抛出
	 * @throws ImageParsingException 图像解析异常，当尺寸解析失败时抛出
	 * @see ImageSize
	 * @see #executeIdentifyByFormat(File, GMConnection, String...)
	 * @since 2.1.0
	 */
	public static ImageSize identifySize(final File file, final GMConnection connection) throws IOException, GMServiceException, GMException {
		String[] result = executeIdentifyByFormat(file, connection, "%w", "%h", "%[EXIF:Orientation]");

		int orientation;
		try {
			orientation = Integer.parseInt(ArrayUtils.get(result, 2));
		} catch (NumberFormatException ignored) {
			orientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		}

		try {
			int width = Integer.parseInt(ArrayUtils.get(result, 0));
			int height = Integer.parseInt(ArrayUtils.get(result, 1));
			return new ImageSize(width, height, orientation);
		} catch (NumberFormatException e) {
			throw new ImageParsingException(file, "尺寸解析失败");
		}
	}

	/**
	 * 识别图像详细信息。
	 * <p>
	 * 使用GraphicsMagick的identify命令获取图像的完整信息，包括尺寸、格式、签名、
	 * 透明通道、位深度、压缩类型和质量等。
	 * </p>
	 *
	 * <p><strong>获取的信息</strong></p>
	 * <ul>
	 *   <li>尺寸：宽度、高度、EXIF方向</li>
	 *   <li>格式：图像格式（如JPEG、PNG等）</li>
	 *   <li>签名：图像的唯一标识符</li>
	 *   <li>透明通道：是否存在透明通道</li>
	 *   <li>位深度：图像的位深度（如8、16、32等）</li>
	 *   <li>压缩类型：图像使用的压缩算法</li>
	 *   <li>质量：图像质量（适用于有损压缩格式）</li>
	 * </ul>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>执行identify命令，获取所有格式化信息</li>
	 *   <li>解析格式、签名、透明通道、压缩类型等字符串信息</li>
	 *   <li>解析EXIF方向，如果解析失败则使用默认值</li>
	 *   <li>解析尺寸、位深度、质量等数值信息，如果解析失败则设为null</li>
	 *   <li>返回包含所有信息的IdentifyResult对象</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>某些信息可能不存在，对应的字段将为null</li>
	 *   <li>位深度和质量信息可能不适用于所有格式</li>
	 *   <li>压缩类型枚举会忽略大小写</li>
	 * </ul>
	 *
	 * @param file       图像文件，不能为null
	 * @param connection GraphicsMagick连接对象，不能为null
	 * @return 图像识别结果对象，包含所有可获取的图像信息
	 * @throws IOException        IO异常，当文件操作失败时抛出
	 * @throws GMServiceException GraphicsMagick服务异常，当服务不可用时抛出
	 * @throws GMException        GraphicsMagick执行异常，当命令执行失败时抛出
	 * @see IdentifyResult
	 * @see CompressionType
	 * @see #executeIdentifyByFormat(File, GMConnection, String...)
	 * @since 2.1.0
	 */
	public static IdentifyResult identify(final File file, final GMConnection connection) throws IOException, GMServiceException, GMException {
		/*
		 字段顺序索引：
		 0: %w        width
		 1: %h        height
		 2: %m        format
		 3: %[EXIF:Orientation] orientation
		 4: %#        signature
		 5: %A        Alpha exists
		 6: %q        depth
		 7: %C        compression
		 8: %Q        quality
		*/
		String[] result = executeIdentifyByFormat(file, connection, "%w", "%h", "%m", "%[EXIF:Orientation]",
			"%#", "%A", "%q", "%C", "%Q");

		String format = ArrayUtils.get(result, 2);
		String signature = ArrayUtils.get(result, 4);
		Boolean hasAlpha = BooleanUtils.toBooleanObject(ArrayUtils.get(result, 5));
		CompressionType compression = EnumUtils.getEnumIgnoreCase(CompressionType.class,
			ArrayUtils.get(result, 6), CompressionType.NONE);

		int orientation;
		try {
			orientation = Integer.parseInt(ArrayUtils.get(result, 3));
		} catch (NumberFormatException ignored) {
			orientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		}

		ImageSize imageSize = null;
		Integer quality = null;
		Integer depth = null;
		try {
			int width = Integer.parseInt(ArrayUtils.get(result, 0));
			int height = Integer.parseInt(ArrayUtils.get(result, 1));
			imageSize = new ImageSize(width, height, orientation);

			depth = Integer.parseInt(ArrayUtils.get(result, 6));
			quality = Integer.parseInt(ArrayUtils.get(result, 8));
		} catch (NumberFormatException ignored) {
		}

		return new IdentifyResult(format, signature, imageSize, hasAlpha, depth, compression, quality);
	}

	/**
	 * 执行自定义格式的identify命令。
	 * <p>
	 * 使用指定的格式字符串执行GraphicsMagick的identify命令，返回解析后的结果数组。
	 * </p>
	 *
	 * <p><strong>格式占位符</strong></p>
	 * <ul>
	 *   <li>%w：图像宽度</li>
	 *   <li>%h：图像高度</li>
	 *   <li>%m：图像格式</li>
	 *   <li>%[EXIF:Orientation]：EXIF方向</li>
	 *   <li>%#：图像签名</li>
	 *   <li>%A：是否存在透明通道</li>
	 *   <li>%q：位深度</li>
	 *   <li>%C：压缩类型</li>
	 *   <li>%Q：图像质量</li>
	 * </ul>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>构建GMOperation命令，添加identify和格式参数</li>
	 *   <li>执行GraphicsMagick命令</li>
	 *   <li>按竖线分隔符解析结果</li>
	 *   <li>返回结果数组</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>格式字符串使用竖线（|）作为分隔符</li>
	 *   <li>结果数组长度与格式字符串数组长度一致</li>
	 *   <li>如果某个格式占位符无法解析，对应位置可能为空字符串</li>
	 * </ul>
	 *
	 * @param file       图像文件，不能为null，必须是图像类型
	 * @param connection GraphicsMagick连接对象，不能为null
	 * @param formats    格式字符串数组，不能为空，支持GraphicsMagick的格式占位符
	 * @return 解析后的结果数组，每个元素对应一个格式字符串的输出
	 * @throws IOException        IO异常，当文件操作失败时抛出
	 * @throws GMServiceException GraphicsMagick服务异常，当服务不可用时抛出
	 * @throws GMException        GraphicsMagick执行异常，当命令执行失败时抛出
	 * @since 2.1.0
	 */
	public static String[] executeIdentifyByFormat(final File file, final GMConnection connection,
	                                               final String... formats) throws IOException, GMServiceException, GMException {

		Assert.notEmpty(formats, "formats 不可为空");
		Assert.notNull(connection, "connection 不可为 null");
		Assert.isTrue(FileUtils.isImageType(file), "file 不是图像类型文件");

		GMOperation operation = new GMOperation();
		operation.addRawArg("identify");
		operation.addRawArg("-format \"" + StringUtils.join(formats, "|") + "\"");
		operation.addImage(file);

		String result = connection.execute(operation.toString());
		LOGGER.info("GraphicsMagick 进程执行成功，命令：{}，结果：{}", operation, result);
		return StringUtils.splitPreserveAllTokens(result, '|');
	}
}
