/*
 *   Copyright 2025 pangju666
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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.exception.*;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.GMImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageFile;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 基于 GraphicsMagick 的图像处理实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>使用 Tika 进行图像类型解析。</li>
 *   <li>使用 GM 命令（identify/convert/composite）进行图像处理与信息读取。</li>
 *   <li>读取图像元数据（尺寸、EXIF 方向）并封装为 {@link ImageFile}。</li>
 *   <li>提供缩放与尺寸调整能力：{@code resize} 不保持长宽比，{@code scale} 保持长宽比。</li>
 *   <li>结合能力集合进行读/写支持判定。</li>
 * </ul>
 *
 * <p><strong>约束</strong></p>
 * <ul>
 *   <li>连接管理：通过 {@link PooledGMService} 获取连接，方法内始终在 {@code finally} 中关闭连接。</li>
 *   <li>线程安全：调用方与连接池共同确保多线程下的资源正确使用。</li>
 *   <li>裁剪限制：绘制“图片水印”路径下不支持裁剪（已在执行顺序中说明）。</li>
 *   <li>异常策略：GM 命令或通信错误统一抛出 {@link ImageOperationException}。</li>
 * </ul>
 *
 * <p><strong>执行顺序（本实现）</strong></p>
 * <ul>
 *   <li>>计算目标尺寸 → 裁剪（绘制图片水印时不支持裁剪） → 绘制文字/图片水印 → 方向矫正 → 缩放 → 去除元数据 → 设置质量 → 输出。</li>
 * </ul>
 *
 * <p><strong>依赖</strong></p>
 * <ul>
 *   <li>{@link Tika}：图像类型解析。</li>
 *   <li>{@link GMOperation}：GM 命令组装。</li>
 *   <li>{@link PooledGMService}：GM 命令执行。</li>
 * </ul>
 *
 * <p><strong>异常与容错</strong></p>
 * <ul>
 *   <li>元数据解析失败：使用 {@link io.github.pangju666.framework.boot.image.lang.ImageConstants#NORMAL_EXIF_ORIENTATION} 作为默认方向，并通过文件解码获取尺寸。</li>
 *   <li>输入或输出类型不受支持：抛出 {@link UnSupportedTypeException}。</li>
 * </ul>
 *
 * @since 1.0.0
 * @author pangju666
 */
public class GMImageTemplate implements ImageTemplate<GMOperation> {
    /**
     * 日志记录器。
     *
     * @since 1.0.0
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(GMImageTemplate.class);

    /**
     * GM 文字绘制参数格式（坐标与文本）。
     *
     * <p>示例：text x y 'content'</p>
     *
     * @since 1.0.0
     */
    protected static final String DRAW_TEXT_ARG_FORMAT = "\"text %d %d '%s'\"";
    /**
     * RGBA 填充颜色格式字符串。
     *
     * <p>示例：rgba(r,g,b,opacity)，透明度范围 0-1。</p>
     *
     * @since 1.0.0
     */
    protected static final String FILL_COLOR_FORMAT = "rgba(%d,%d,%d,%.1f)";

    /**
     * GM 连接池服务，用于执行 GM 命令。
     *
     * @since 1.0.0
     */
    private final PooledGMService pooledGMService;

    public GMImageTemplate(PooledGMService pooledGMService) {
        this.pooledGMService = pooledGMService;
    }

	/**
	 * 读取图像信息（格式、大小、MIME、方向与尺寸）。
	 *
	 * @param file 待解析图像文件
	 * @return 图像信息
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException     GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException                 文件读取失败时抛出
	 */
    @Override
    public ImageFile read(File file) throws IOException {
		ImageFile imageFile = new ImageFile(file);
        if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_FORMAT_SET.contains(imageFile.getFormat())) {
            throw new UnSupportedTypeException("不支持读取 " + imageFile.getFormat() + " 格式图片");
        }

		GMOperation operation = new GMOperation();
		operation.addRawArg("identify");
		operation.verbose();
		operation.addImage(file);

		String result = execute(operation);
		for (String metaData : result.lines().toList()) {
			String metaDataStrip = metaData.strip();
			if (metaDataStrip.startsWith("Geometry:")) {
				String geometry = StringUtils.substringAfter(metaDataStrip, "Geometry:").strip();
				if (StringUtils.isBlank(geometry)) {
					throw new ImageParsingException(file, "尺寸解析失败");
				}
				try {
					String[] geometryValue = geometry.split("x");
					imageFile.setImageSize(new ImageSize(Integer.parseInt(geometryValue[0]),
						Integer.parseInt(geometryValue[1])));
				} catch (NumberFormatException e) {
					throw new ImageParsingException(file, "尺寸解析失败");
				}
			} else if (metaDataStrip.startsWith("Orientation:")) {
				try {
					String orientationStr = StringUtils.substringAfter(metaDataStrip.strip(), "Orientation:").strip();
					imageFile.setOrientation(Integer.parseInt(orientationStr));
				} catch (NumberFormatException ignored) {
				}
			}
		}

		if (imageFile.getOrientation() >= 5 && imageFile.getOrientation() <= 8) {
			imageFile.setImageSize(new ImageSize(imageFile.getImageSize().getHeight(), imageFile.getImageSize().getWidth()));
		}
		return imageFile;
	}

	/**
	 * 执行图像操作（输入文件形式）。
	 *
	 * <p>执行顺序：计算目标尺寸 → 裁剪（绘制图片水印时不支持裁剪） → 绘制文字/图片水印 → 方向矫正 → 缩放 → 去除元数据 → 设置质量 → 输出。</p>
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @param operation 操作配置，可为 {@code null}
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException     GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException                 文件读取失败时抛出
	 */
    @Override
    public void process(File inputFile, File outputFile, ImageOperation operation, Consumer<GMOperation> imageConsumer) throws IOException {
        validateOutputFile(outputFile);

        ImageFile imageFile = read(inputFile);
        doExecute(imageFile, outputFile, ObjectUtils.getIfNull(operation,
			ImageOperation.EMPTY), imageConsumer);
    }

	/**
	 * 执行图像操作（已解析信息形式）。
	 *
	 * <p>执行顺序：计算目标尺寸 → 裁剪（绘制图片水印时不支持裁剪） → 绘制文字/图片水印 → 方向矫正 → 缩放 → 去除元数据 → 设置质量 → 输出。</p>
	 *
	 * @param imageFile 已解析的图像信息
	 * @param outputFile 输出文件
	 * @param operation 操作配置，可为 {@code null}
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException     GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException                 文件读取失败时抛出
	 */
    @Override
    public void process(ImageFile imageFile, File outputFile, ImageOperation operation, Consumer<GMOperation> imageConsumer) throws IOException {
        Assert.notNull(imageFile, "imageFile 不可为 null");
		FileUtils.checkFile(imageFile.getFile(), "imageFile 未设置 file 属性");
        validateOutputFile(outputFile);

		String format = StringUtils.defaultIfBlank(imageFile.getFormat(),
			FilenameUtils.getExtension(imageFile.getFile().getName()));
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_FORMAT_SET.contains(format)) {
			throw new UnSupportedTypeException("不支持读取 " + format + " 格式图片");
		}

		if (imageFile.getOrientation() < 1 || imageFile.getOrientation() > 8 || Objects.isNull(imageFile.getImageSize())) {
			imageFile = read(imageFile.getFile());
		}

		doExecute(imageFile, outputFile, ObjectUtils.getIfNull(operation,
			ImageOperation.EMPTY), imageConsumer);
	}

	/**
	 * 判断实现是否支持读取文件。
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException 文件读取失败时抛出
	 */
    @Override
    public boolean canRead(File file) throws IOException {
        FileUtils.check(file, "file 不可为 null");

        String fileFormat = FilenameUtils.getExtension(file.getName());
        return ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_FORMAT_SET.contains(fileFormat);
    }

	/**
	 * 判断实现是否支持输出为指定的图像格式。
	 *
	 * @param format 待判定图像格式
	 * @return {@code true} 表示支持写出
	 */
    @Override
    public boolean canWrite(String format) {
		Assert.hasText(format, "format 不可为空");
        return ImageConstants.GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_FORMAT_SET.contains(format);
    }

    /**
     * 执行 GM 命令并返回输出文本。
     *
     * <p>参数校验规则：</p>
     * <p>如果 {@code operation} 为空，则不执行并抛出异常。</p>
     * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
     *
     * @param operation GM 操作对象
     * @return 命令执行输出
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
     * @since 1.0.0
     */
    public String execute(GMOperation operation) {
        Assert.notNull(operation, "operation 不可为 null");

		GMConnection connection= null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(operation.toString());
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + operation + " 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行 GM 命令。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code command} 为空，则不执行并抛出异常；如果 {@code arguments} 为空，则不设置附加参数。</p>
	 * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
	 *
	 * @param command   GM 命令
	 * @param arguments GM 命令附加参数，可为空
	 * @return 命令执行结果输出
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	public String execute(String command, String... arguments) {
		Assert.hasText(command, "command 不可为空");

		GMConnection connection= null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(command, arguments);
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + command + StringUtils.SPACE +
				StringUtils.joinWith(StringUtils.SPACE, command) + " 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行 GM 命令列表。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code command} 为空，则不执行并返回空字符串。</p>
	 * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
	 *
	 * @param command GM 命令及参数列表
	 * @return 命令执行结果输出；当输入为空时返回空字符串
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	public String execute(List<String> command) {
		if (CollectionUtils.isEmpty(command)) {
			return StringUtils.EMPTY;
		}

		GMConnection connection= null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(command);
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + StringUtils.join(command, StringUtils.SPACE) +
				" 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行具体处理逻辑。
	 *
	 * <p>执行顺序：计算目标尺寸 → 裁剪（绘制图片水印时不支持裁剪） → 绘制文字/图片水印 → 方向矫正 → 缩放 → 去除元数据 → 设置质量 → 输出。</p>
	 *
	 * @param imageFile 图像信息
	 * @param outputFile 输出文件
	 * @param operation 操作配置
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws ImageOperationException     GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	protected void doExecute(ImageFile imageFile, File outputFile, ImageOperation operation,
							 Consumer<GMOperation> imageConsumer) {
		GMOperation gmOperation = new GMOperation();

		ImageSize imageSize = imageFile.getImageSize();
		if (ObjectUtils.allNotNull(operation.getTargetWidth(), operation.getTargetHeight())) {
			if (operation.isForceScale()) {
				imageSize = new ImageSize(operation.getTargetWidth(), operation.getTargetHeight());
			} else {
				imageSize = imageSize.scale(operation.getTargetWidth(), operation.getTargetHeight());
			}
		} else if (Objects.nonNull(operation.getScaleRatio())) {
			imageSize = imageSize.scale(operation.getScaleRatio());
		} else if (Objects.nonNull(operation.getTargetWidth())) {
			imageSize = imageSize.scaleByWidth(operation.getTargetWidth());
		} else if (Objects.nonNull(operation.getTargetHeight())) {
			imageSize = imageSize.scaleByWidth(operation.getTargetHeight());
		}

		GMImageOperation gmImageOperation = null;
		if (operation instanceof GMImageOperation) {
			gmImageOperation = (GMImageOperation) operation;
		}

		boolean needCorrectOrientation = Objects.nonNull(gmImageOperation) && gmImageOperation.isStripProfiles();
		boolean needInputImageArgs = true;
		boolean needConvertCommand = true;
		boolean needCropArgs = true;
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getWatermarkText())) {
			if (Objects.nonNull(operation.getWatermarkDirection())) {
				// 根据方位绘制文字水印
				gmOperation.addRawArg("convert");
				setCropArgs(imageSize, gmImageOperation, gmOperation);
				setInputImageArgs(imageFile.getFile(), imageSize, imageFile.getOrientation(),
					true, gmImageOperation, gmOperation);

				setGravityArg(operation, gmOperation);
				setTextWatermarkArgs(gmImageOperation, gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, 20, 20, gmImageOperation.getWatermarkText()));

				needConvertCommand = false;
				needInputImageArgs = false;
				needCropArgs = false;
			} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
				// 根据坐标绘制文字水印
				gmOperation.addRawArg("convert");
				setCropArgs(imageSize, gmImageOperation, gmOperation);
				setInputImageArgs(imageFile.getFile(), imageSize, imageFile.getOrientation(),
					true, gmImageOperation, gmOperation);

				// 设置左上角为原点
				gmOperation.gravity(GMOperation.Gravity.NorthWest);
				setTextWatermarkArgs(gmImageOperation, gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, operation.getWatermarkX(),
					operation.getWatermarkY(), gmImageOperation.getWatermarkText()));

				needConvertCommand = false;
				needInputImageArgs = false;
				needCropArgs = false;
			}
		} else if (Objects.nonNull(operation.getWatermarkImage())) {
			if (Objects.nonNull(operation.getWatermarkDirection())) {
				// 根据方位绘制图片水印
				gmOperation.addRawArg("composite");
				setGravityArg(operation, gmOperation);
				setWatermarkImageArgs(imageSize, operation, 10,10, gmOperation);
				gmOperation.addRawArg("-dissolve " + (int) (operation.getWatermarkImageOption().getOpacity() * 100));
				gmOperation.addImage(operation.getWatermarkImage());

				needCorrectOrientation = true;
				needConvertCommand = false;
				needCropArgs = false;
			} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
				// 根据坐标绘制图片水印
				gmOperation.addRawArg("composite");
				// 设置左上角为原点
				gmOperation.gravity(GMOperation.Gravity.NorthWest);
				setWatermarkImageArgs(imageSize, operation, operation.getWatermarkX(), operation.getWatermarkY(),
					gmOperation);
				gmOperation.addRawArg("-dissolve " + (int) (operation.getWatermarkImageOption().getOpacity() * 100));
				gmOperation.addImage(operation.getWatermarkImage());

				needCorrectOrientation = true;
				needConvertCommand = false;
				needCropArgs = false;
			}
		}

		if (needConvertCommand) {
			gmOperation.addRawArg("convert");
		}
		if (needCropArgs) {
			setCropArgs(imageSize, operation, gmOperation);
		}
		if (needInputImageArgs) {
			setInputImageArgs(imageFile.getFile(), imageSize, imageFile.getOrientation(),
				needCorrectOrientation, operation, gmOperation);
		}

		// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
		if (Objects.nonNull(gmImageOperation) && (needCorrectOrientation || gmImageOperation.isStripProfiles())) {
			gmOperation.stripProfiles();
		}

		// 判断是否存在自定义处理
		if (Objects.nonNull(imageConsumer)) {
			imageConsumer.accept(gmOperation);
		}

		// 修改输出质量
		if (Objects.nonNull(gmImageOperation)) {
			gmOperation.quality(gmImageOperation.getQuality());
		}
		// 传入输出文件
		gmOperation.addImage(outputFile);
		// 执行命令
		execute(gmOperation);
	}

	/**
	 * 校验输出文件与格式是否受支持。
	 *
	 *
	 * @param outputFile 输出文件
	 * @throws UnSupportedTypeException 当输出文件格式不受支持时抛出
	 * @throws IOException                 输出文件目录创建失败或 I/O 错误时抛出
	 * @since 1.0.0
	 */
	protected void validateOutputFile(File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputImageFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputFile.getName());
		if (StringUtils.isBlank(outputImageFormat)) {
			throw new UnSupportedTypeException("未知的输出格式");
		}
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_FORMAT_SET.contains(outputImageFormat)) {
			throw new UnSupportedTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		FileUtils.forceMkdirParent(outputFile);
	}

	/**
	 * 设置输入图像相关参数（方向矫正、输入文件、重采样滤镜与缩放）。
	 *
	 * @param inputImageFile 输入文件
	 * @param imageSize 目标尺寸（已根据操作计算）
	 * @param orientation EXIF 方向码
	 * @param correctOrientation 是否矫正方向
	 * @param operation 操作配置
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setInputImageArgs(File inputImageFile, ImageSize imageSize, int orientation, boolean correctOrientation,
									 ImageOperation operation, GMOperation gmOperation) {
		// 方向矫正
		if (correctOrientation) {
			setCorrectOrientationArgs(orientation, gmOperation);
		}

		// 传入输入文件
		gmOperation.addImage(inputImageFile);

		// 判断是否需要修改重采样过滤器
		if (operation instanceof GMImageOperation gmImageOperation && Objects.nonNull(gmImageOperation.getResizeFilter())) {
			gmOperation.filter(gmImageOperation.getResizeFilter().getFilterName());
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.anyNotNull(operation.getTargetWidth(), operation.getTargetHeight(), operation.getScaleRatio())) {
			gmOperation.resize(imageSize.getWidth(), imageSize.getHeight(), '!');
		}
	}

	/**
	 * 设置裁剪相关参数
	 *
	 * @param imageSize 原始图片尺寸
	 * @param operation 操作配置
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setCropArgs(ImageSize imageSize, ImageOperation operation, GMOperation gmOperation) {
		// 判断是否需要执行裁剪
		if (Objects.nonNull(operation.getCropType())) {
			if (operation.getCropType() == CropType.CENTER) {
				if (ObjectUtils.allNotNull(operation.getCenterCropWidth(), operation.getCenterCropHeight()) &&
					operation.getCenterCropWidth() < imageSize.getWidth() &&
					operation.getCenterCropHeight() < imageSize.getHeight() ) {
					int posX = (imageSize.getWidth() - operation.getCenterCropWidth()) / 2;
					int posY = (imageSize.getHeight() - operation.getCenterCropHeight()) / 2;
					gmOperation.crop(operation.getCenterCropWidth(), operation.getCenterCropHeight(),
						posX, posY);
					gmOperation.addRawArg(" +repage");
				}
			} else if (operation.getCropType() == CropType.OFFSET) {
				if (ObjectUtils.allNotNull(operation.getTopCropOffset(), operation.getBottomCropOffset(),
					operation.getLeftCropOffset(), operation.getRightCropOffset()) &&
					operation.getRightCropOffset() < imageSize.getWidth() &&
					operation.getLeftCropOffset() < imageSize.getWidth() &&
					operation.getLeftCropOffset() + operation.getRightCropOffset() < imageSize.getWidth() &&
					operation.getTopCropOffset() < imageSize.getHeight() &&
					operation.getBottomCropOffset() < imageSize.getHeight() &&
					operation.getTopCropOffset() + operation.getBottomCropOffset() < imageSize.getHeight()) {

					int width = imageSize.getWidth() - operation.getLeftCropOffset() - operation.getRightCropOffset();
					int height = imageSize.getHeight() - operation.getTopCropOffset() - operation.getBottomCropOffset();
					gmOperation.crop(width, height, operation.getLeftCropOffset(),
						operation.getTopCropOffset());
					gmOperation.addRawArg(" +repage");
				}
			} else if (operation.getCropType() == CropType.RECT) {
				if (ObjectUtils.allNotNull(operation.getCropRectX(), operation.getCropRectY(),
					operation.getCropRectWidth(), operation.getCropRectHeight()) &&
					operation.getCropRectX() >= imageSize.getWidth() &&
					operation.getCropRectWidth() >= imageSize.getWidth() &&
					operation.getCropRectX() + operation.getCropRectWidth() >= imageSize.getWidth() &&
					operation.getCropRectY() >= imageSize.getHeight() &&
					operation.getCropRectHeight() >= imageSize.getHeight() &&
					operation.getCropRectY() + operation.getCropRectHeight() >= imageSize.getHeight()) {

					gmOperation.crop(operation.getCropRectWidth(), operation.getCropRectHeight(),
						operation.getCropRectX(), operation.getCropRectY());
					gmOperation.addRawArg(" +repage");
				}
			}
		}
	}

    /**
     * 根据 EXIF 方向设置 GM 方向矫正指令。
     *
     * @param orientation EXIF 方向码（1-8）
     * @param gmOperation GM 操作对象
     * @since 1.0.0
     */
    protected void setCorrectOrientationArgs(int orientation, GMOperation gmOperation) {
        // 矫正方向
        switch (orientation) {
            case 2:
                gmOperation.flop();
                break;
			case 3:
				gmOperation.rotate(180d);
				break;
			case 4:
				gmOperation.flip();
				break;
			case 5:
				gmOperation.rotate(90d);
				gmOperation.flip();
				break;
			case 6:
				gmOperation.rotate(90d);
				break;
			case 7:
				gmOperation.rotate(-90d);
				gmOperation.flip();
				break;
			case 8:
				gmOperation.rotate(-90d);
				break;
			default:
				break;
		}
	}

    /**
     * 设置文字水印相关参数（字体、字号、填充颜色等）。
     *
     * @param operation 操作配置
     * @param gmOperation GM 操作对象
     * @since 1.0.0
     */
    protected void setTextWatermarkArgs(GMImageOperation operation, GMOperation gmOperation) {
        if (Objects.nonNull(operation.getWatermarkTextFontName())) {
            gmOperation.font(operation.getWatermarkTextFontName());
            gmOperation.pointsize(operation.getWatermarkTextFontSize());

			String fillColor = FILL_COLOR_FORMAT.formatted(operation.getWatermarkTextColor().getRed(),
				operation.getWatermarkTextColor().getGreen(),
				operation.getWatermarkTextColor().getBlue(),
				operation.getWatermarkTextOpacity());
			gmOperation.fill(fillColor);
		}
	}

    /**
     * 将通用水印方向映射为 GM 的重力（Gravity）。
     *
     * @param operation 操作配置（使用其中的方向）
     * @param gmOperation GM 操作对象
     * @since 1.0.0
     */
    protected void setGravityArg(ImageOperation operation, GMOperation gmOperation) {
        if (Objects.nonNull(operation.getWatermarkDirection())) {
			GMOperation.Gravity gravity = switch (operation.getWatermarkDirection()) {
				case TOP -> GMOperation.Gravity.North;
				case TOP_LEFT -> GMOperation.Gravity.NorthWest;
				case TOP_RIGHT -> GMOperation.Gravity.NorthEast;
				case BOTTOM -> GMOperation.Gravity.South;
				case BOTTOM_LEFT -> GMOperation.Gravity.SouthWest;
				case BOTTOM_RIGHT -> GMOperation.Gravity.SouthEast;
				case CENTER -> GMOperation.Gravity.Center;
				case LEFT -> GMOperation.Gravity.West;
				case RIGHT -> GMOperation.Gravity.East;
			};
			gmOperation.gravity(gravity);
		}
	}

	/**
	 * 设置水印图片相关参数
	 *
	 * @param inputImageSize 输入图像尺寸
	 * @param operation 操作配置
	 * @param x 水印x坐标
	 * @param y 水印y坐标
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setWatermarkImageArgs(ImageSize inputImageSize, ImageOperation operation, Integer x, Integer y,
										 GMOperation gmOperation) {
		ImageSize scaleWatermarkImageSize = inputImageSize.scale(operation.getWatermarkImageOption().getScale());
		if (scaleWatermarkImageSize.getWidth() > scaleWatermarkImageSize.getHeight()) {
			if (scaleWatermarkImageSize.getWidth() > operation.getWatermarkImageOption().getMaxWidth()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMaxWidth() +
					"x+" + x + "+" + y);
			} else if (scaleWatermarkImageSize.getWidth() < operation.getWatermarkImageOption().getMinWidth()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMinWidth() +
					"x+" + x + "+" + y);
			} else {
				gmOperation.addRawArg("-geometry " + scaleWatermarkImageSize.getWidth() + "x" +
					scaleWatermarkImageSize.getHeight() + "+" + x + "+" + y);
			}
		} else {
			if (scaleWatermarkImageSize.getHeight() > operation.getWatermarkImageOption().getMaxHeight()) {
				gmOperation.addRawArg("-geometry " + "x" + operation.getWatermarkImageOption().getMaxHeight() +
					"+" + x + "+" + y);
			} else if (scaleWatermarkImageSize.getHeight() < operation.getWatermarkImageOption().getMinHeight()) {
				gmOperation.addRawArg("-geometry " + "x" + operation.getWatermarkImageOption().getMinHeight() +
					"+" + x + "+" + y);
			} else {
				gmOperation.addRawArg("-geometry " + scaleWatermarkImageSize.getWidth() + "x" +
					scaleWatermarkImageSize.getHeight() + "+" + x + "+" + y);
			}
		}
	}
}
