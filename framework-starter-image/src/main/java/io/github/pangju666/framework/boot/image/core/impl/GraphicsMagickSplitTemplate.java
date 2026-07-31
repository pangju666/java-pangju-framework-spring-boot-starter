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

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.core.ImageSplitTemplate;
import io.github.pangju666.framework.boot.image.exception.ImageEngineException;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.tile.GridTileOptions;
import io.github.pangju666.framework.boot.image.model.tile.SizeTileOptions;
import io.github.pangju666.framework.boot.image.utils.GraphicsMagickUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GraphicsMagick图像瓦片切分模板实现。
 * <p>
 * 基于GraphicsMagick引擎实现图像瓦片切分功能，支持按网格和按尺寸两种切分方式。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>按网格切分：将图像按照指定的行数和列数切分成均匀的瓦片</li>
 *   <li>按尺寸切分：将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式</li>
 *   <li>自动处理EXIF方向：自动校正图像方向，确保瓦片方向正确</li>
 *   <li>画布扩展：自动扩展画布到瓦片尺寸的整数倍，使用背景色填充</li>
 *   <li>失败处理：支持失败时保留或删除已生成的瓦片文件</li>
 *   <li>资源管理：自动管理GraphicsMagick连接和图像资源，确保资源正确释放</li>
 * </ul>
 *
 * <p><strong>切分模式</strong></p>
 * <ul>
 *   <li>单一模式：仅生成单一分辨率的瓦片，适用于简单的瓦片需求</li>
 *   <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景</li>
 * </ul>
 *
 * <p><strong>瓦片布局</strong></p>
 * <ul>
 *   <li>DeepZoom：瓦片文件命名为x_y格式，所有文件在同一目录</li>
 *   <li>XYZ：瓦片文件按x坐标组织到子目录，文件名为y坐标</li>
 * </ul>
 *
 * <p><strong>使用场景</strong></p>
 * <ul>
 *   <li>地图瓦片生成：将大地图图像切分为瓦片以便于网络传输</li>
 *   <li>深度缩放：生成金字塔瓦片支持平滑的深度缩放体验</li>
 *   <li>图像分割：将大图像分割为多个小图像以便于并行处理</li>
 * </ul>
 *
 * <p><strong>使用注意事项</strong></p>
 * <ul>
 *   <li>需要GraphicsMagick引擎支持</li>
 *   <li>需要有效的PooledGMService连接池服务</li>
 *   <li>瓦片切分会创建临时文件，确保输出目录有足够的权限和空间</li>
 *   <li>金字塔模式会生成多级目录，注意磁盘空间占用</li>
 *   <li>处理大图像时可能需要较长时间和较多内存</li>
 *   <li>GraphicsMagick输入文件路径不支持中文或非ASCII字符，需要使用纯英文路径</li>
 * </ul>
 *
 * <p><strong>资源管理</strong></p>
 * <ul>
 *   <li>使用try-finally确保GraphicsMagick连接正确关闭</li>
 *   <li>使用try-finally确保图像资源正确关闭</li>
 *   <li>关闭失败时记录错误日志，不影响主流程</li>
 * </ul>
 *
 * @author pangju666
 * @see PooledGMService
 * @see GraphicsMagickResource
 * @see GraphicsMagickUtils
 * @see ImageSplitTemplate
 * @since 2.1.0
 */
public class GraphicsMagickSplitTemplate implements ImageSplitTemplate {
	/**
	 * 瓦片输出文件前缀。
	 * <p>
	 * 用于标识切分后的瓦片文件，在重命名前使用此前缀。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final String TILE_OUTPUT_FILE_PREFIX = "tile_";
	/**
	 * 金字塔模式基数。
	 * <p>
	 * 金字塔模式下，每级分辨率为上一级的1/2，即基数为2。
	 * 用于计算金字塔层级数和各级缩放比例。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final int TILE_PYRAMID_BASE = 2;
	/**
	 * 日志记录器
	 *
	 * @since 2.1.0
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsMagickSplitTemplate.class);
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
	public GraphicsMagickSplitTemplate(PooledGMService pooledGMService) {
		this.pooledGMService = pooledGMService;
	}

	/**
	 * 执行瓦片切分操作。
	 * <p>
	 * 私有方法，实际执行图像的切分、重命名和目录组织操作。
	 * </p>
	 *
	 * <p><strong>处理流程</strong></p>
	 * <ol>
	 *   <li>确定当前层级的输出目录</li>
	 *   <li>检查目录是否存在，根据skipExistingLevels决定是否跳过</li>
	 *   <li>创建输出目录（如果不存在）</li>
	 *   <li>构建GraphicsMagick convert命令</li>
	 *   <li>根据EXIF方向自动校正图像方向（如果需要）</li>
	 *   <li>如果是金字塔模式，按层级缩放图像到目标尺寸</li>
	 *   <li>扩展画布到指定尺寸（使用背景色填充）</li>
	 *   <li>去除图像元数据（stripProfiles）</li>
	 *   <li>按瓦片尺寸裁剪图像，生成多个瓦片文件</li>
	 *   <li>根据布局格式重命名和组织瓦片文件</li>
	 *   <li>返回当前层级的输出目录</li>
	 * </ol>
	 *
	 * <p><strong>瓦片布局格式</strong></p>
	 * <ul>
	 *   <li>DeepZoom：瓦片文件命名为x_y格式，所有文件在同一目录</li>
	 *   <li>XYZ：瓦片文件按x坐标组织到子目录，文件名为y坐标</li>
	 * </ul>
	 *
	 * <p><strong>金字塔模式</strong></p>
	 * <ul>
	 *   <li>每个层级创建独立的子目录，目录名为层级编号</li>
	 *   <li>层级0为最高分辨率，层级递增分辨率递减</li>
	 *   <li>最低层级可能只包含一个瓦片</li>
	 * </ul>
	 *
	 * <p><strong>目录处理</strong></p>
	 * <ul>
	 *   <li>如果目录已存在且非空，根据skipExistingLevels决定是否跳过</li>
	 *   <li>如果目录创建失败，抛出ImageOperationException</li>
	 *   <li>如果瓦片文件读取失败，删除目录并抛出异常</li>
	 *   <li>如果瓦片文件重命名失败，删除目录并抛出异常</li>
	 * </ul>
	 *
	 * @param inputFile          输入图像文件，不能为null
	 * @param outputDir          输出目录，不能为null
	 * @param layerSize          当前层级的图像尺寸，不能为null
	 * @param canvasSize         画布尺寸，不能为null
	 * @param options            瓦片切分选项，不能为null
	 * @param level              当前层级（金字塔模式使用），单一模式为0
	 * @param skipExistingLevels 是否跳过已存在的层级，true跳过，false重新生成
	 * @param connection         GraphicsMagick连接对象，不能为null
	 * @return 当前层级的输出目录
	 * @throws IOException             IO异常，当文件操作失败时抛出
	 * @throws GMServiceException      GraphicsMagick服务异常，当服务不可用时抛出
	 * @throws GMException             GraphicsMagick执行异常，当命令执行失败时抛出
	 * @throws ImageOperationException 图像操作异常，当切分操作失败时抛出
	 * @since 2.1.0
	 */
	protected static File doSplitTiles(File inputFile, File outputDir, ImageSize layerSize, ImageSize canvasSize,
	                                   SizeTileOptions options, int level, boolean skipExistingLevels,
	                                   final GMConnection connection) throws IOException, GMServiceException, GMException {
		File levelOutputDir = outputDir;
		if (options.getMode() == SizeTileOptions.TileMode.PYRAMID) {
			levelOutputDir = new File(outputDir.getAbsolutePath(), String.valueOf(level));
		}

		if (levelOutputDir.exists() && levelOutputDir.isDirectory()) {
			if (skipExistingLevels && !FileUtils.isEmptyDirectory(levelOutputDir)) {
				return levelOutputDir;
			}
		} else {
			try {
				FileUtils.forceMkdir(levelOutputDir);
			} catch (IOException e) {
				LOGGER.error("层级瓦片输出目录：{} 无法被创建", levelOutputDir.getAbsolutePath(), e);

				throw new ImageOperationException(inputFile, "瓦片文件生成失败，输出目录：" +
					levelOutputDir.getAbsolutePath() + "无法被创建", e);
			}
		}

		GMOperation operation = new GMOperation();
		operation.addRawArg("convert");
		operation.addImage(inputFile);

		if (layerSize.getOrientation() != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			operation.addRawArg("-auto-orient");
		}

		ImageSize layerVisualImageSize = layerSize.getVisualSize();

		if (options.getMode() == SizeTileOptions.TileMode.PYRAMID) {
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
		operation.stripProfiles();
		operation.addRawArg(FilenameUtils.separatorsToUnix(levelOutputDir.getAbsolutePath()) + "/" +
			TILE_OUTPUT_FILE_PREFIX + "%d." + options.getOutputFormat());

		connection.execute(operation.toString());
		LOGGER.info("GraphicsMagick 进程执行成功，命令：{}", operation);

		int totalCols = canvasSize.getWidth() / options.getTileWidth();
		File[] tileFiles = levelOutputDir.listFiles();
		if (ArrayUtils.isEmpty(tileFiles)) {
			LOGGER.error("层级瓦片输出目录：{} 读取失败", levelOutputDir.getAbsolutePath());

			if (!FileUtils.deleteQuietly(levelOutputDir)) {
				LOGGER.error("瓦片文件目录：{} 删除失败", levelOutputDir.getAbsolutePath());
			}

			throw new ImageOperationException(inputFile, "瓦片文件生成失败，输出目录：" + levelOutputDir.getAbsolutePath() +
				"无法被读取");
		}

		try {
			for (File tileFile : tileFiles) {
				String indexStr = StringUtils.substringAfter(FilenameUtils.getBaseName(tileFile.getName()),
					TILE_OUTPUT_FILE_PREFIX);
				int index = Integer.parseInt(indexStr);
				int x = index % totalCols;
				int y = index / totalCols;

				if (options.getLayout() == SizeTileOptions.TileLayout.DEEP_ZOOM) {
					FileUtils.replaceBaseName(tileFile, x + "_" + y);
				} else {
					File newTileFile = FileUtils.replaceBaseName(tileFile, String.valueOf(y));
					File newTileDir = new File(FilenameUtils.separatorsToUnix(
						levelOutputDir.getAbsolutePath()) + "/" + x);
					FileUtils.moveFileToDirectory(newTileFile, newTileDir, true);
				}
			}

			LOGGER.error("输入文件：{}，第{}级瓦片切分完成，输出目录：{}", inputFile.getAbsolutePath(), level,
				levelOutputDir.getAbsolutePath());

			return levelOutputDir;
		} catch (IOException e) {
			LOGGER.error("瓦片文件：{} 重命名失败", levelOutputDir.getAbsolutePath(), e);

			if (!FileUtils.deleteQuietly(levelOutputDir)) {
				LOGGER.error("瓦片文件目录：{} 删除失败", levelOutputDir.getAbsolutePath());
			}

			throw new ImageOperationException(inputFile, "瓦片文件生成失败，输出文件重命名失败");
		}
	}

	/**
	 * 按网格切分图像瓦片。
	 * <p>
	 * 将图像按照指定的行数和列数切分成均匀的瓦片。
	 * 瓦片尺寸根据图像实际尺寸和行列数自动计算，确保覆盖整个图像区域。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>获取GraphicsMagick连接</li>
	 *   <li>将资源转换为GraphicsMagickResource</li>
	 *   <li>识别图像尺寸和方向信息</li>
	 *   <li>根据行列数计算瓦片宽度和高度</li>
	 *   <li>计算画布尺寸，确保瓦片完全覆盖图像</li>
	 *   <li>转换为单一模式的按尺寸切分选项</li>
	 *   <li>调用doSplitTiles执行切分操作</li>
	 *   <li>关闭GraphicsMagick连接和图像资源</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>瓦片尺寸可能不完全相等，最后一行/列可能较小</li>
	 *   <li>画布会扩展到瓦片尺寸的整数倍，使用背景色填充</li>
	 *   <li>自动处理EXIF方向，确保瓦片方向正确</li>
	 *   <li>内部转换为单一模式的按尺寸切分</li>
	 *   <li>使用try-finally确保资源正确释放</li>
	 * </ul>
	 *
	 * @param resource  图像资源，不能为null
	 * @param outputDir 输出目录，不能为null
	 * @param options   瓦片切分选项，不能为null
	 * @throws ImageEngineException    图像引擎异常，当GraphicsMagick执行失败时抛出
	 * @throws ImageOperationException 图像操作异常，当切分操作失败时抛出
	 * @throws ImageParsingException   图像解析异常，当图像读取失败时抛出
	 * @see GridTileOptions
	 * @see SizeTileOptions
	 * @see #doSplitTiles(File, File, ImageSize, ImageSize, SizeTileOptions, int, boolean, GMConnection)
	 * @since 2.1.0
	 */
	public void splitTilesByGrid(IOResource resource, File outputDir, GridTileOptions options) {
		Assert.notNull(outputDir, "outputDir 不可为 null");
		Assert.notNull(options, "options 不可为 null");
		Assert.notNull(resource, "resource 不可为 null");

		GMConnection connection;
		try {
			connection = pooledGMService.getConnection();
		} catch (GMServiceException e) {
			throw new ImageEngineException("获取 GraphicsMagick 进程失败", e);
		}

		GraphicsMagickResource imageResource = null;
		try {
			imageResource = GraphicsMagickUtils.toGraphicsMagickResource(resource, connection);
			ImageSize visualImageSize = imageResource.getImageSize().getVisualSize();

			int tileWidth = (int) Math.ceil((double) visualImageSize.getWidth() / options.getCols());
			int tileHeight = (int) Math.ceil((double) visualImageSize.getHeight() / options.getRows());

			int canvasWidth = tileWidth * options.getCols();
			int canvasHeight = tileHeight * options.getRows();
			ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

			SizeTileOptions sizeTileOptions = new SizeTileOptions(options);
			sizeTileOptions.setMode(SizeTileOptions.TileMode.SINGLE);
			sizeTileOptions.setTileWidth(tileWidth);
			sizeTileOptions.setTileHeight(tileHeight);

			doSplitTiles(imageResource.getFile(), outputDir, visualImageSize, canvasSize,
				sizeTileOptions, 0, false, connection);
		} catch (IOException e) {
			throw new ImageParsingException("图像读取失败", e);
		} catch (GMServiceException e) {
			throw new ImageEngineException("与 GraphicsMagick 进程通信时出现错误", e);
		} catch (GMException e) {
			throw new ImageOperationException("GraphicsMagick 切片操作执行失败", e);
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
	 * 按尺寸切分图像瓦片。
	 * <p>
	 * 将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式。
	 * </p>
	 *
	 * <p><strong>切分模式</strong></p>
	 * <ul>
	 *   <li>单一模式：仅生成单一分辨率的瓦片，适用于简单的瓦片需求</li>
	 *   <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景</li>
	 * </ul>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>获取GraphicsMagick连接</li>
	 *   <li>将资源转换为GraphicsMagickResource</li>
	 *   <li>识别图像尺寸和方向信息</li>
	 *   <li>根据切分模式执行不同逻辑：</li>
	 *   <ul>
	 *     <li>单一模式：计算画布尺寸，执行单次切分</li>
	 *     <li>金字塔模式：计算最大层级数，逐级切分</li>
	 *   </ul>
	 *   <li>关闭GraphicsMagick连接和图像资源</li>
	 * </ol>
	 *
	 * <p><strong>金字塔模式处理</strong></p>
	 * <ul>
	 *   <li>计算最大层级数，确保覆盖原始图像尺寸</li>
	 *   <li>从最高层级到最低层级依次处理</li>
	 *   <li>每个层级创建独立的子目录</li>
	 *   <li>如果任一层级失败，根据retainOnFailure决定是否清理</li>
	 * </ul>
	 *
	 * <p><strong>失败处理</strong></p>
	 * <ul>
	 *   <li>retainOnFailure为true时，保留已生成的瓦片文件</li>
	 *   <li>retainOnFailure为false时，删除所有已生成的瓦片文件</li>
	 * </ul>
	 *
	 * @param resource        图像资源，不能为null
	 * @param outputDir       输出目录，不能为null
	 * @param options         瓦片切分选项，不能为null
	 * @param retainOnFailure 失败时是否保留已生成的瓦片文件，true保留，false删除
	 * @throws ImageEngineException    图像引擎异常，当GraphicsMagick执行失败时抛出
	 * @throws ImageOperationException 图像操作异常，当切分操作失败时抛出
	 * @throws ImageParsingException   图像解析异常，当图像读取失败时抛出
	 * @see SizeTileOptions
	 * @see #doSplitTiles(File, File, ImageSize, ImageSize, SizeTileOptions, int, boolean, GMConnection)
	 * @since 2.1.0
	 */
	public void splitTilesBySize(IOResource resource, File outputDir, SizeTileOptions options, boolean retainOnFailure) {
		Assert.notNull(outputDir, "outputDir 不可为 null");
		Assert.notNull(options, "options 不可为 null");
		Assert.notNull(resource, "resource 不可为 null");

		GMConnection connection;
		try {
			connection = pooledGMService.getConnection();
		} catch (GMServiceException e) {
			throw new ImageEngineException("获取 GraphicsMagick 进程失败", e);
		}

		GraphicsMagickResource imageResource = null;
		try {
			imageResource = GraphicsMagickUtils.toGraphicsMagickResource(resource, connection);
			ImageSize visualImageSize = imageResource.getImageSize().getVisualSize();

			if (options.getMode() == SizeTileOptions.TileMode.SINGLE) {
				int canvasWidth = (int) Math.ceil((double) visualImageSize.getWidth() / options.getTileWidth()) *
					options.getTileWidth();
				int canvasHeight = (int) Math.ceil((double) visualImageSize.getHeight() / options.getTileHeight()) *
					options.getTileHeight();
				ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

				doSplitTiles(imageResource.getFile(), outputDir, visualImageSize, canvasSize,
					options, 0, false, connection);
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
						ImageSize layerTargetSize = visualImageSize.scale(scaleFactor);

						int canvasWidth = ((layerTargetSize.getWidth() + options.getTileWidth() - 1) /
							options.getTileWidth()) * options.getTileWidth();
						int canvasHeight = ((layerTargetSize.getHeight() + options.getTileHeight() - 1) /
							options.getTileHeight()) * options.getTileHeight();
						ImageSize canvasSize = new ImageSize(canvasWidth, canvasHeight);

						File levelOutputDir = doSplitTiles(imageResource.getFile(), outputDir,
							layerTargetSize, canvasSize, options, level, retainOnFailure, connection);
						levelOutputDirs.add(levelOutputDir);
					}
				} catch (ImageOperationException e) {
					if (!retainOnFailure) {
						for (File levelOutputDir : levelOutputDirs) {
							if (!FileUtils.deleteQuietly(levelOutputDir)) {
								LOGGER.error("瓦片文件目录：{} 删除失败", levelOutputDir.getAbsolutePath());
							}
						}
					}

					throw e;
				}
			}
		} catch (IOException e) {
			throw new ImageParsingException("图像读取失败", e);
		} catch (GMServiceException e) {
			throw new ImageEngineException("与 GraphicsMagick 进程通信时出现错误", e);
		} catch (GMException e) {
			throw new ImageOperationException("GraphicsMagick 切片操作执行失败", e);
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
}
