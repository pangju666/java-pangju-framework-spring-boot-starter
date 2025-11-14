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
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.exception.ImageDamageException;
import io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.GMImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageInfo;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import io.github.pangju666.framework.web.exception.base.ServerException;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.Objects;
import java.util.function.Consumer;

public class GMImageTemplate implements ImageTemplate<GMOperation> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GMImageTemplate.class);

	private final PooledGMService pooledGMService;

	public GMImageTemplate(PooledGMService pooledGMService) {
		this.pooledGMService = pooledGMService;
	}

	@Override
	public ImageInfo readImageInfo(File imageFile) throws IOException {
		String mimeType = FileUtils.getMimeType(imageFile);
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_TYPE_SET.contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		ImageInfo imageInfo = new ImageInfo();
		imageInfo.setFormat(FilenameUtils.getExtension(imageFile.getName()));
		imageInfo.setFileSize(imageFile.length());
		imageInfo.setMimeType(mimeType);
		imageInfo.setFile(imageFile);
		imageInfo.setOrientation(0);

		GMOperation operation = new GMOperation();
		operation.addRawArg("identify");
		operation.verbose();
		operation.addImage(imageFile);

		String result = execute(operation);
		for (String metaData : result.lines().toList()) {
			String metaDataStrip = metaData.strip();
			try {
				if (metaDataStrip.startsWith("Geometry:")) {
					String geometry = StringUtils.substringAfter(metaDataStrip, "Geometry:").strip();
					if (StringUtils.isBlank(geometry)) {
						throw new ImageDamageException(imageFile, "尺寸读取失败");
					}
					String[] geometryValue = geometry.split("x");
					imageInfo.setSize(new ImageSize(Integer.parseInt(geometryValue[0]),
						Integer.parseInt(geometryValue[1])));
				} else if (metaDataStrip.startsWith("Orientation:")) {
					String orientationStr = StringUtils.substringAfter(metaDataStrip.strip(), "Orientation:").strip();
					imageInfo.setOrientation(Integer.parseInt(orientationStr));
				}
			} catch (NumberFormatException ignored) {
			}
		}

		if (imageInfo.getOrientation() >= 5 && imageInfo.getOrientation() <= 8) {
			imageInfo.setSize(new ImageSize(imageInfo.getSize().getHeight(), imageInfo.getSize().getWidth()));
		}
		return imageInfo;
	}

	@Override
	public void execute(File inputImage, File outputImageFile, ImageOperation operation, Consumer<GMOperation> imageConsumer) throws IOException {
		ImageInfo imageInfo = readImageInfo(inputImage);
		String outputImageFormat = getOutputFormat(outputImageFile);
		doExecute(imageInfo, outputImageFile, outputImageFormat, operation, imageConsumer);
	}

	@Override
	public void execute(ImageInfo imageInfo, File outputImageFile, ImageOperation operation, Consumer<GMOperation> imageConsumer) throws IOException {
		Assert.notNull(imageInfo, "imageInfo不可为 null");
		FileUtils.checkFile(imageInfo.getFile(), "输入图片不可为null");
		String mimeType = StringUtils.defaultIfBlank(imageInfo.getMimeType(), FileUtils.getMimeType(imageInfo.getFile()));
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_TYPE_SET.contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}
		Assert.isTrue(imageInfo.getOrientation() >= 1 && imageInfo.getOrientation() <= 8, "无效的exif方向");
		Assert.notNull(imageInfo.getSize(), "图像尺寸信息不存在");

		String outputImageFormat = getOutputFormat(outputImageFile);
		doExecute(imageInfo, outputImageFile, outputImageFormat, operation, imageConsumer);
	}

	@Override
	public boolean canRead(String mimeType) {
		return ImageConstants.GRAPHICS_MAGICK_SUPPORT_READ_IMAGE_TYPE_SET.contains(mimeType);
	}

	@Override
	public boolean canWrite(String mimeType) {
		return ImageConstants.GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_TYPE_SET.contains(mimeType);
	}

	protected void doExecute(ImageInfo imageInfo, File outputImageFile, String outputFormat, ImageOperation operation,
							 Consumer<GMOperation> imageConsumer) throws IOException {
		GMOperation gmOperation = new GMOperation();

		// 矫正方向
		switch (imageInfo.getOrientation()) {
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

		// 判断是否需要添加水印
		if (Objects.nonNull(operation.getWatermarkDirection())) {
			if (Objects.nonNull(operation.getWatermarkText())) {
				setGravity(operation, gmOperation);

				gmOperation.font(operation.getTextWatermarkOption().getFont().getFontName());
				gmOperation.pointsize(operation.getTextWatermarkOption().getFont().getSize());
				gmOperation.fill(operation.getTextWatermarkOption().getFillColor().toString());
				gmOperation.undercolor(operation.getTextWatermarkOption().getStrokeColor().toString());
				gmOperation.draw(operation.getWatermarkText());
			} else if (Objects.nonNull(operation.getWatermarkFile())) {
				FileUtils.checkFile(operation.getWatermarkFile(), null);
				String watermarkFileType = FileUtils.getMimeType(operation.getWatermarkFile());
				if (!ImageConstants.getSupportReadImageTypes().contains(watermarkFileType)) {
					throw new UnSupportImageTypeException("不受支持的图片类型");
				}
				ImageSize watermarkImageSize = ImageUtils.getSize(operation.getWatermarkFile(), false);

				gmOperation.addRawArg("composite");
				setGravity(operation, gmOperation);
				scaleWatermarkImage(imageInfo.getSize(), watermarkImageSize, operation, gmOperation);
				gmOperation.addRawArg("-dissolve" + (int) (operation.getImageWatermarkOption().getOpacity() * 100));
				gmOperation.addImage(operation.getWatermarkFile());
			}
		} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
			if (Objects.nonNull(operation.getWatermarkText())) {
				gmOperation.font(operation.getTextWatermarkOption().getFont().getFontName());
				gmOperation.pointsize(operation.getTextWatermarkOption().getFont().getSize());
				gmOperation.fill(operation.getTextWatermarkOption().getFillColor().toString());
				gmOperation.undercolor(operation.getTextWatermarkOption().getStrokeColor().toString());
				gmOperation.drawText(operation.getWatermarkText(), operation.getWatermarkX(), operation.getWatermarkY());
			} else if (Objects.nonNull(operation.getWatermarkFile())) {
				FileUtils.checkFile(operation.getWatermarkFile(), null);
				String watermarkFileType = FileUtils.getMimeType(operation.getWatermarkFile());
				if (!ImageConstants.getSupportReadImageTypes().contains(watermarkFileType)) {
					throw new UnSupportImageTypeException("不受支持的图片类型");
				}
				ImageSize watermarkImageSize = ImageUtils.getSize(operation.getWatermarkFile(), false);

				gmOperation.geometry(operation.getWatermarkX(), operation.getWatermarkY());
				scaleWatermarkImage(imageInfo.getSize(), watermarkImageSize, operation, gmOperation);
				gmOperation.addRawArg("-dissolve" + (int) (operation.getImageWatermarkOption().getOpacity() * 100));
				gmOperation.addImage(operation.getWatermarkFile());
			}
		}

		if (Objects.nonNull(operation.getOpacity())) {
			gmOperation.addRawArg("-dissolve" + (int) (operation.getOpacity() * 100));
		}
		gmOperation.addImage(imageInfo.getFile());

		// 判断是否需要执行缩放
		if (ObjectUtils.allNotNull(operation.getTargetWidth(), operation.getTargetHeight())) {
			if (operation.isForceScale()) {
				gmOperation.resize(operation.getTargetWidth(), operation.getTargetHeight(), '!');
			} else {
				gmOperation.resize(operation.getTargetWidth(), operation.getTargetHeight());
			}
		} else if (Objects.nonNull(operation.getScaleRatio())) {
			gmOperation.resize((int) (operation.getScaleRatio() * 100), null, '%');
		} else if (Objects.nonNull(operation.getTargetWidth())) {
			gmOperation.resize(operation.getTargetWidth(), null, 'x');
		} else if (Objects.nonNull(operation.getTargetHeight())) {
			gmOperation.resize(null, operation.getTargetHeight(), 'x');
		}


		if (Objects.nonNull(imageConsumer)) {
			imageConsumer.accept(gmOperation);
		}

		// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
		if (operation instanceof GMImageOperation gmImageOperation) {
			if (Objects.nonNull(gmImageOperation.getStripProfiles()) && gmImageOperation.getStripProfiles()) {
				gmOperation.stripProfiles();
			}
			if (Objects.nonNull(gmImageOperation.getQuality())) {
				gmOperation.quality(gmImageOperation.getQuality());
			}
		}

		gmOperation.addImage(outputImageFile);

		execute(gmOperation);
	}

	protected String getOutputFormat(File outputImageFile) {
		FileUtils.checkFileIfExist(outputImageFile, "outputImageFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputImageFile.getName());
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORT_WRITE_IMAGE_TYPE_SET.contains(outputImageFormat)) {
			throw new UnSupportImageTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		return outputImageFormat;
	}

	public String execute(GMOperation operation) {
		GMConnection connection = null;
		try {
			connection = pooledGMService.getConnection();
			connection.execute(operation.toString());
			return connection.execute(operation.toString());
		} catch (GMServiceException e) {
			throw new ServerException("GM进程连接出现错误", e);
		} catch (IOException | GMException e) {
			throw new ServerException("GM命令执行失败", e);
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

	protected void setGravity(ImageOperation operation, GMOperation gmOperation) {
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

	protected void scaleWatermarkImage(ImageSize inputImageSize, ImageSize waterImageSize,
									   ImageOperation operation, GMOperation gmOperation) {
		ImageSize watermarkImageSize = waterImageSize.scale(inputImageSize.scale(operation.getImageWatermarkOption().getScale()));
		if (watermarkImageSize.getWidth() > watermarkImageSize.getHeight()) {
			if (watermarkImageSize.getWidth() > operation.getImageWatermarkOption().getMaxWidth()) {
				gmOperation.resize(operation.getImageWatermarkOption().getMaxWidth(), null, 'x');
			} else if (watermarkImageSize.getWidth() < operation.getImageWatermarkOption().getMinWidth()) {
				gmOperation.resize(operation.getImageWatermarkOption().getMinWidth(), null, 'x');
			} else {
				gmOperation.resize(watermarkImageSize.getWidth(), watermarkImageSize.getHeight(), '!');
			}
		} else {
			if (watermarkImageSize.getHeight() > operation.getImageWatermarkOption().getMaxHeight()) {
				gmOperation.resize(null, operation.getImageWatermarkOption().getMaxHeight(), 'x');
			} else if (watermarkImageSize.getHeight() < operation.getImageWatermarkOption().getMinHeight()) {
				gmOperation.resize(null, operation.getImageWatermarkOption().getMinHeight(), 'x');
			} else {
				gmOperation.resize(watermarkImageSize.getWidth(), watermarkImageSize.getHeight(), '!');
			}
		}
	}
}
