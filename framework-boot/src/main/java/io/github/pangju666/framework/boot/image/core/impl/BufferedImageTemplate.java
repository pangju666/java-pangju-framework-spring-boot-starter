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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import io.github.pangju666.commons.image.utils.ImageEditor;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.BufferedImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageInfo;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 基于TwelveMonkeys与 Metadata 的图像处理实现。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>读取图像元数据（尺寸、EXIF 方向）并封装为 {@link io.github.pangju666.framework.boot.image.model.ImageInfo}。</li>
 *   <li>提供图像尺寸调整与缩放：{@code resize} 不保持长宽比；{@code scale} 保持长宽比。</li>
 *   <li>基于 {@code TwelveMonkeys ImageIO} 能力集合判定读/写支持。</li>
 * </ul>
 *
 * <p><b>依赖</b></p>
 * <ul>
 *   <li>{@link com.drew.imaging.ImageMetadataReader}（metadata-extractor）：解析图像元数据。</li>
 *   <li>{@link io.github.pangju666.commons.image.utils.ImageEditor}：执行具体的缩放/调整与输出。</li>
 *   <li>{@link io.github.pangju666.framework.boot.image.lang.ImageConstants}：维护支持的 MIME 类型集合与默认 EXIF 方向。</li>
 * </ul>
 *
 * <p><b>异常与容错</b></p>
 * <ul>
 *   <li>当元数据解析失败时，使用 {@link io.github.pangju666.framework.boot.image.lang.ImageConstants#NORMAL_EXIF_ORIENTATION} 作为默认方向，并通过文件解码获取尺寸。</li>
 *   <li>处理过程中如发生解码错误，抛出 {@link io.github.pangju666.framework.boot.image.exception.ImageDamageException}，并附带原始根因。</li>
 *   <li>若输入文件类型不受支持，抛出 {@link io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class BufferedImageTemplate implements ImageTemplate<BufferedImage> {
	/**
	 * 读取图像信息（格式、大小、MIME、EXIF 方向与尺寸）。
	 *
	 * <p>优先使用 metadata 解析；失败则采用默认方向并通过文件解码获取尺寸。</p>
	 *
	 * @param imageFile 待解析图像文件
	 * @return 图像信息
	 * @throws IOException 文件读取或类型校验失败
	 */
	@Override
	public ImageInfo readImageInfo(File imageFile) throws IOException {
		String mimeType = FileUtils.getMimeType(imageFile);
		if (!ImageConstants.getSupportReadImageTypes().contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		ImageInfo imageInfo = new ImageInfo();
		imageInfo.setFormat(FilenameUtils.getExtension(imageFile.getName()));
		imageInfo.setFileSize(imageFile.length());
		imageInfo.setMimeType(mimeType);
		imageInfo.setFile(imageFile);
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
			imageInfo.setOrientation(ImageUtils.getExifOrientation(metadata));
			imageInfo.setSize(ImageUtils.getSize(metadata));
		} catch (ImageProcessingException | IOException ignored) {
			imageInfo.setOrientation(ImageConstants.NORMAL_EXIF_ORIENTATION);
			imageInfo.setSize(ImageUtils.getSize(imageFile, false));
		}
		return imageInfo;
	}

	@Override
	public void execute(File inputImage, File outputImageFile, ImageOperation operation, Consumer<BufferedImage> imageConsumer) throws IOException {
		ImageInfo imageInfo = readImageInfo(inputImage);
		String outputImageFormat = getOutputFormat(outputImageFile);
		doExecute(imageInfo, outputImageFile, outputImageFormat, operation, imageConsumer);
	}

	@Override
	public void execute(ImageInfo imageInfo, File outputImageFile, ImageOperation operation,
						Consumer<BufferedImage> imageConsumer) throws IOException {
		Assert.notNull(imageInfo, "imageInfo不可为 null");
		FileUtils.checkFile(imageInfo.getFile(), "输入图片不可为null");
		String mimeType = StringUtils.defaultIfBlank(imageInfo.getMimeType(), FileUtils.getMimeType(imageInfo.getFile()));
		if (!ImageConstants.getSupportReadImageTypes().contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		String outputImageFormat = getOutputFormat(outputImageFile);
		doExecute(imageInfo, outputImageFile, outputImageFormat, operation, imageConsumer);
	}

	protected String getOutputFormat(File outputImageFile) {
		FileUtils.checkFileIfExist(outputImageFile, "outputImageFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputImageFile.getName());
		if (!ImageConstants.getSupportWriteImageFormats().contains(outputImageFormat)) {
			throw new UnSupportImageTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		return outputImageFormat;
	}

	protected void doExecute(ImageInfo imageInfo, File outputImageFile, String outputFormat, ImageOperation operation,
						Consumer<BufferedImage> imageConsumer) throws IOException {
		ImageEditor imageEditor = ImageEditor.of(imageInfo.getFile())
			// 矫正方向
			.correctOrientation(imageInfo.getOrientation());

		// 判断是否需要执行缩放
		if (ObjectUtils.allNotNull(operation.getTargetWidth(), operation.getTargetHeight())) {
			if (operation.isForceScale()) {
				imageEditor.resize(operation.getTargetWidth(), operation.getTargetHeight());
			} else {
				imageEditor.scale(operation.getTargetWidth(), operation.getTargetHeight());
			}
		} else if (Objects.nonNull(operation.getScaleRatio())) {
			imageEditor.scale(operation.getScaleRatio());
		} else if (Objects.nonNull(operation.getTargetWidth())) {
			imageEditor.scaleByWidth(operation.getTargetWidth());
		} else if (Objects.nonNull(operation.getTargetHeight())) {
			imageEditor.scaleByHeight(operation.getTargetHeight());
		}

		// 判断是否需要修改缩放过滤器类型
		if (operation instanceof BufferedImageOperation bufferedImageOperation) {
			if (Objects.nonNull(bufferedImageOperation.getScaleFilterType())) {
				imageEditor.scaleFilterType(bufferedImageOperation.getScaleFilterType());
			}
		}

		// 判断是否需要添加水印
		if (Objects.nonNull(operation.getWatermarkDirection())) {
			if (Objects.nonNull(operation.getWatermarkText())) {
				imageEditor.addTextWatermark(operation.getWatermarkText(), operation.getTextWatermarkOption(),
					operation.getWatermarkDirection());
			} else if (Objects.nonNull(operation.getWatermarkFile())) {
				FileUtils.checkFile(operation.getWatermarkFile(), null);
				String watermarkFileType = FileUtils.getMimeType(operation.getWatermarkFile());
				if (!ImageConstants.getSupportReadImageTypes().contains(watermarkFileType)) {
					throw new UnSupportImageTypeException("不受支持的图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkFile(), operation.getImageWatermarkOption(),
					operation.getWatermarkDirection());
			}
		} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
			if (Objects.nonNull(operation.getWatermarkText())) {
				imageEditor.addTextWatermark(operation.getWatermarkText(), operation.getTextWatermarkOption(),
					operation.getWatermarkX(), operation.getWatermarkY());
			} else if (Objects.nonNull(operation.getWatermarkFile())) {
				FileUtils.checkFile(operation.getWatermarkFile(), null);
				String watermarkFileType = FileUtils.getMimeType(operation.getWatermarkFile());
				if (!ImageConstants.getSupportReadImageTypes().contains(watermarkFileType)) {
					throw new UnSupportImageTypeException("不受支持的图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkFile(), operation.getImageWatermarkOption(),
					operation.getWatermarkX(), operation.getWatermarkY());
			}
		}

		BufferedImage bufferedImage = imageEditor.toBufferedImage();
		if (Objects.nonNull(imageConsumer)) {
			imageConsumer.accept(bufferedImage);
		}
		ImageIO.write(bufferedImage, outputFormat, outputImageFile);
	}

	@Override
	public boolean canRead(String mimeType) {
		return ImageConstants.getSupportReadImageTypes().contains(mimeType);
	}

	@Override
	public boolean canWrite(String mimeType) {
		return ImageConstants.getSupportWriteImageTypes().contains(mimeType);
	}
}
