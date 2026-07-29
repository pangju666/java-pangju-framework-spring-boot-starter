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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.ImageCompressionType;
import io.github.pangju666.framework.boot.image.enums.TileLayout;
import io.github.pangju666.framework.boot.image.enums.TileMode;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.gm.IdentifyResult;
import io.github.pangju666.framework.boot.image.model.tile.GridTileOptions;
import io.github.pangju666.framework.boot.image.model.tile.SizeTileOptions;
import org.apache.commons.io.FilenameUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GraphicsMagick工具类。
 * <p>
 * 提供使用GraphicsMagick引擎进行图像处理的工具方法，包括图像信息识别、瓦片切分等功能。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *     <li>图像信息识别：获取图像尺寸、格式、压缩类型、质量等元数据</li>
 *     <li>瓦片切分：支持按网格和按尺寸两种切分方式，支持单一模式和金字塔模式</li>
 *     <li>支持DeepZoom和XYZ两种瓦片布局格式</li>
 * </ul>
 * </p>
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
	 * 瓦片输出文件前缀
	 *
	 * @since 2.1.0
	 */
	protected static final String TILE_OUTPUT_FILE_PREFIX = "tile_";
	/**
	 * 金字塔模式基数
	 *
	 * @since 2.1.0
	 */
	protected static final int TILE_PYRAMID_BASE = 2;

	/**
	 * 私有构造函数，防止实例化。
	 *
	 * @since 2.1.0
	 */
	protected GraphicsMagickUtils() {
	}

	/**
	 * 按网格切分图像。
	 * <p>
	 * 将图像按照指定的行数和列数切分成均匀的瓦片。
	 * 瓦片尺寸根据图像实际尺寸和行列数自动计算，确保覆盖整个图像区域。
	 * </p>
	 *
	 * @param inputFile   输入图像文件
	 * @param outputDir   输出目录
	 * @param options     瓦片切分选项
	 * @param connection  GraphicsMagick连接对象
	 * @throws IOException         IO异常
	 * @throws GMServiceException  GraphicsMagick服务异常
	 * @throws GMException         GraphicsMagick执行异常
	 * @since 2.1.0
	 * @see GridTileOptions
	 */
	public static void splitByGrid(final File inputFile, final File outputDir, final GridTileOptions options,
	                               final GMConnection connection) throws IOException, GMServiceException, GMException {
		Assert.notNull(outputDir, "outputDir 不可为 null");
		Assert.notNull(options, "options 不可为 null");

		ImageSize imageSize = identifySize(inputFile, connection);
		ImageSize visualImageSize = imageSize.getVisualSize();

		int tileWidth = (int) Math.ceil((double) visualImageSize.getWidth() / options.getCols());
		int tileHeight = (int) Math.ceil((double) visualImageSize.getHeight() / options.getRows());

		int canvasWidth = tileWidth * options.getCols();
		int canvasHeight = tileHeight * options.getRows();
		ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

		SizeTileOptions sizeTileOptions = new SizeTileOptions(options);
		sizeTileOptions.setMode(TileMode.SINGLE);
		sizeTileOptions.setTileWidth(tileWidth);
		sizeTileOptions.setTileHeight(tileHeight);

		doSplitTiles(inputFile, outputDir, imageSize, canvasSize, sizeTileOptions, 0, connection);
	}

	/**
	 * 按尺寸切分图像瓦片。
	 * <p>
	 * 将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式。
	 * </p>
	 * <p>
	 * <ul>
	 * <li>单一模式：仅生成单一分辨率的瓦片。</li>
	 * <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputFile   输入图像文件
	 * @param outputDir   输出目录
	 * @param options     瓦片切分选项
	 * @param connection  GraphicsMagick连接对象
	 * @throws IOException         IO异常
	 * @throws GMServiceException  GraphicsMagick服务异常
	 * @throws GMException         GraphicsMagick执行异常
	 * @since 2.1.0
	 * @see SizeTileOptions
	 */
	public static void splitTilesBySize(final File inputFile, final File outputDir, final SizeTileOptions options,
	                                    final GMConnection connection) throws IOException, GMServiceException, GMException {
		Assert.notNull(outputDir, "outputDir 不可为 null");
		Assert.notNull(options, "options 不可为 null");

		ImageSize imageSize = identifySize(inputFile, connection);
		ImageSize visualImageSize = imageSize.getVisualSize();

		if (options.getMode() == TileMode.SINGLE) {
			int canvasWidth = (int) Math.ceil((double) visualImageSize.getWidth() / options.getTileWidth()) *
				options.getTileWidth();
			int canvasHeight = (int) Math.ceil((double) visualImageSize.getHeight() / options.getTileHeight()) *
				options.getTileHeight();
			ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

			doSplitTiles(inputFile, outputDir, imageSize, canvasSize, options, 0, connection);
		} else {
			int maxLevel = 0;
			long candidateWidth = options.getTileWidth();
			long candidateHeight = options.getTileHeight();
			while (candidateWidth < visualImageSize.getWidth() || candidateHeight < visualImageSize.getHeight()) {
				candidateWidth *= TILE_PYRAMID_BASE;
				candidateHeight *= TILE_PYRAMID_BASE;
				maxLevel++;
			}

			List<File> levelOutputDirs = new ArrayList<>(maxLevel + 1);
			try {
				for (int level = 0; level <= maxLevel; level++) {
					double scaleFactor = Math.pow(TILE_PYRAMID_BASE, level) / Math.pow(TILE_PYRAMID_BASE, maxLevel);
					ImageSize layerTargetSize = imageSize.scale(scaleFactor);

					int canvasWidth = ((layerTargetSize.getWidth() + options.getTileWidth() - 1) /
						options.getTileWidth()) * options.getTileWidth();
					int canvasHeight = ((layerTargetSize.getHeight() + options.getTileHeight() - 1) /
						options.getTileHeight()) * options.getTileHeight();
					ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

					File levelOutputDir = doSplitTiles(inputFile, outputDir, layerTargetSize, canvasSize, options,
						level, connection);
					if (Objects.isNull(levelOutputDir)) {
						throw new IOException();
					} else {
						levelOutputDirs.add(levelOutputDir);
					}
				}
			} catch (Exception e) {
				for (File dir : levelOutputDirs) {
					FileUtils.forceDeleteIfExist(dir);
				}

				throw e;
			}
		}
	}

	/**
	 * 识别图像尺寸。
	 * <p>
	 * 使用GraphicsMagick的identify命令获取图像的宽度和高度，并解析EXIF方向信息。
	 * </p>
	 *
	 * @param file       图像文件
	 * @param connection GraphicsMagick连接对象
	 * @return 图像尺寸对象，包含宽度、高度和EXIF方向
	 * @throws IOException              IO异常
	 * @throws GMServiceException      GraphicsMagick服务异常
	 * @throws GMException              GraphicsMagick执行异常
	 * @throws ImageParsingException   图像解析异常
	 * @since 2.1.0
	 * @see ImageSize
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
	 * @param file       图像文件
	 * @param connection GraphicsMagick连接对象
	 * @return 图像识别结果对象
	 * @throws IOException         IO异常
	 * @throws GMServiceException  GraphicsMagick服务异常
	 * @throws GMException         GraphicsMagick执行异常
	 * @since 2.1.0
	 * @see IdentifyResult
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
		ImageCompressionType compression = EnumUtils.getEnumIgnoreCase(ImageCompressionType.class,
			ArrayUtils.get(result, 6), ImageCompressionType.NONE);

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
	 * @param file       图像文件
	 * @param connection GraphicsMagick连接对象
	 * @param formats    格式字符串数组，支持GraphicsMagick的格式占位符
	 * @return 解析后的结果数组，每个元素对应一个格式字符串的输出
	 * @throws IOException              IO异常
	 * @throws GMServiceException      GraphicsMagick服务异常
	 * @throws GMException              GraphicsMagick执行异常
	 * @since 2.1.0
	 */
	public static String[] executeIdentifyByFormat(final File file, final GMConnection connection, final String... formats)
		throws IOException, GMServiceException, GMException {

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

	/**
	 * 执行瓦片切分操作。
	 * <p>
	 * 内部方法，实际执行图像的切分、重命名和目录组织操作。
	 * </p>
	 * <p>
	 * 处理流程：
	 * <ul>
	 *     <li>根据EXIF方向自动校正图像方向</li>
	 *     <li>如果是金字塔模式，按层级缩放图像</li>
	 *     <li>扩展画布到指定尺寸（使用背景色填充）</li>
	 *     <li>按瓦片尺寸裁剪图像</li>
	 *     <li>根据布局格式重命名和组织瓦片文件</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputFile   输入图像文件
	 * @param outputDir   输出目录
	 * @param layerSize   当前层级的图像尺寸
	 * @param canvasSize  画布尺寸
	 * @param options     瓦片切分选项
	 * @param level       当前层级（金字塔模式使用）
	 * @param connection  GraphicsMagick连接对象
	 * @return 当前层级的输出目录
	 * @throws IOException         IO异常
	 * @throws GMServiceException  GraphicsMagick服务异常
	 * @throws GMException         GraphicsMagick执行异常
	 * @since 2.1.0
	 */
	protected static File doSplitTiles(final File inputFile, final File outputDir, final ImageSize layerSize,
	                                   final ImageSize canvasSize, final SizeTileOptions options, final int level,
	                                   final GMConnection connection) throws IOException, GMServiceException, GMException {

		GMOperation operation = new GMOperation();
		operation.addRawArg("convert");
		operation.addImage(inputFile);

		if (layerSize.getOrientation() != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			operation.addRawArg("-auto-orient");
		}

		ImageSize layerVisualImageSize = layerSize.getVisualSize();

		if (options.getMode() == TileMode.PYRAMID) {
			operation.resize(layerVisualImageSize.getWidth(),
				layerVisualImageSize.getHeight(), '!');
		}

		if (canvasSize.getHeight() != layerVisualImageSize.getHeight() ||
			canvasSize.getWidth() != layerVisualImageSize.getWidth()) {
			operation.background(options.getBackgroundColor());
			operation.addRawArg("-extent");
			operation.addRawArg(canvasSize.getWidth() + "x" + canvasSize.getHeight());
		}

		operation.crop(options.getTileWidth(), options.getTileHeight());
		operation.addRawArg("+adjoin");

		File levelOutputDir = outputDir;
		if (options.getMode() == TileMode.PYRAMID) {
			levelOutputDir = new File(outputDir.getAbsolutePath(), String.valueOf(level));
		}
		operation.addRawArg(FilenameUtils.separatorsToUnix(levelOutputDir.getAbsolutePath()) + "/" +
			TILE_OUTPUT_FILE_PREFIX + "%d." + options.getOutputFormat());

		FileUtils.forceMkdir(levelOutputDir);

		connection.execute(operation.toString());
		LOGGER.info("GraphicsMagick 进程执行成功，命令：{}", operation);

		int totalCols = canvasSize.getWidth() / options.getTileWidth();
		File[] tileFiles = levelOutputDir.listFiles();
		if (Objects.isNull(tileFiles)) {
			return null;
		}
		try {
			for (File tileFile : tileFiles) {
				String indexStr = StringUtils.substringAfter(FilenameUtils.getBaseName(tileFile.getName()),
					TILE_OUTPUT_FILE_PREFIX);
				int index = Integer.parseInt(indexStr);
				int x = index % totalCols;
				int y = index / totalCols;

				if (options.getLayout() == TileLayout.DEEP_ZOOM) {
					FileUtils.replaceBaseName(tileFile, x + "_" + y);
				} else {
					File newTileFile = FileUtils.replaceBaseName(tileFile, String.valueOf(y));
					File newTileDir = new File(FilenameUtils.separatorsToUnix(
						levelOutputDir.getAbsolutePath()) + "/" + x);
					FileUtils.moveFileToDirectory(newTileFile, newTileDir, true);
				}
			}
		} catch (Exception e) {
			FileUtils.forceDelete(levelOutputDir);
			throw e;
		}
		return levelOutputDir;
	}
}
