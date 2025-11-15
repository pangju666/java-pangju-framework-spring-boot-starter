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
 * 基于 TwelveMonkeys 与 metadata-extractor 的图像处理实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>读取图像元数据（尺寸、EXIF 方向）并封装为 {@link io.github.pangju666.framework.boot.image.model.ImageInfo}。</li>
 *   <li>提供缩放与尺寸调整能力：{@code resize} 不保持长宽比，{@code scale} 保持长宽比。</li>
 *   <li>结合能力集合进行读/写支持判定。</li>
 * </ul>
 *
 * <p><strong>执行顺序（本实现）</strong></p>
 * <ul>
 *   <li>矫正方向 → 缩放 → 添加水印 → 写出文件。</li>
 * </ul>
 *
 * <p><strong>依赖</strong></p>
 * <ul>
 *   <li>{@link com.drew.imaging.ImageMetadataReader}：解析图像元数据。</li>
 *   <li>{@link io.github.pangju666.commons.image.utils.ImageEditor}：执行缩放/调整与输出。</li>
 *   <li>{@link io.github.pangju666.framework.boot.image.lang.ImageConstants}：维护支持的类型集合与默认 EXIF 方向。</li>
 * </ul>
 *
 * <p><strong>异常与容错</strong></p>
 * <ul>
 *   <li>元数据解析失败：使用 {@link io.github.pangju666.framework.boot.image.lang.ImageConstants#NORMAL_EXIF_ORIENTATION} 作为默认方向，并通过文件解码获取尺寸。</li>
 *   <li>输入或输出类型不受支持：抛出 {@link io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class BufferedImageTemplate implements ImageTemplate<BufferedImageOperation, BufferedImage> {
	/**
	 * 读取图像信息（格式、大小、MIME、EXIF 方向与尺寸）。
	 *
	 * <p>优先使用 metadata 解析；失败则采用默认方向并通过文件解码获取尺寸。</p>
	 * <p>参数校验规则：如果 {@code imageFile} 为 {@code null} 或不存在，将在底层探测时抛出 I/O 异常。</p>
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

	/**
	 * 执行图像操作（输入文件形式）。
	 *
	 * <p>参数校验规则：如果 {@code inputFile} 为 {@code null} 或不可读，将在信息读取时抛出异常；
	 * 如果 {@code outputFile} 扩展名为空或不在支持集合，抛出不受支持类型异常。</p>
	 *
	 * @param inputFile 输入文件
	 * @param outputFile 输出文件
	 * @param operation 操作配置
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws IOException I/O 或解码错误
	 */
	@Override
	public void execute(File inputFile, File outputFile, BufferedImageOperation operation, Consumer<BufferedImage> imageConsumer) throws IOException {
		String outputImageFormat = getOutputFormat(outputFile);
		ImageInfo imageInfo = readImageInfo(inputFile);
		doExecute(imageInfo, outputFile, outputImageFormat, operation, imageConsumer);
	}

	/**
	 * 执行图像操作（已解析信息形式）。
	 *
	 * <p>参数校验规则：{@code imageInfo} 不可为 {@code null}；{@code imageInfo.file} 不可为 {@code null}；
	 * 输出文件扩展名需在支持集合，否则抛出不受支持类型异常。</p>
	 *
	 * @param imageInfo 已解析的图像信息
	 * @param outputFile 输出文件
	 * @param operation 操作配置
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws IOException I/O 或解码错误
	 */
	@Override
	public void execute(ImageInfo imageInfo, File outputFile, BufferedImageOperation operation,
						Consumer<BufferedImage> imageConsumer) throws IOException {
		Assert.notNull(imageInfo, "imageInfo不可为 null");
		FileUtils.checkFile(imageInfo.getFile(), "输入图片不可为null");
		String outputImageFormat = getOutputFormat(outputFile);

		String mimeType = StringUtils.defaultIfBlank(imageInfo.getMimeType(),
			FileUtils.getMimeType(imageInfo.getFile()));
		if (!ImageConstants.getSupportReadImageTypes().contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		if (imageInfo.getOrientation() < 1 || imageInfo.getOrientation() > 8 || Objects.isNull(imageInfo.getSize())) {
			imageInfo = readImageInfo(imageInfo.getFile());
		}
		doExecute(imageInfo, outputFile, outputImageFormat, operation, imageConsumer);
	}

	/**
	 * 解析并校验输出格式。
	 *
	 * <p>参数校验规则：{@code outputFile} 不可为 {@code null}；扩展名不能为空且需在可写集合中。</p>
	 *
	 * @param outputFile 输出文件
	 * @return 输出格式扩展名
	 */
	protected String getOutputFormat(File outputFile) {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputFile.getName());
		if (StringUtils.isBlank(outputImageFormat)) {
			throw new UnSupportImageTypeException("未知的输出格式");
		}
		if (!ImageConstants.getSupportWriteImageFormats().contains(outputImageFormat)) {
			throw new UnSupportImageTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		return outputImageFormat;
	}

	/**
	 * 执行具体处理逻辑。
	 *
	 * <p>执行顺序：矫正方向 → 缩放 → 水印 → 输出。</p>
	 * <p>参数校验规则：{
	 * imageInfo} 的尺寸或方向异常时将回退为重新读取信息；当输出类型不受支持时已在上游进行校验。</p>
	 *
	 * @param imageInfo 图像信息
	 * @param outputFile 输出文件
	 * @param outputFormat 输出格式扩展名
	 * @param operation 操作配置
	 * @param imageConsumer 中间处理回调，可为 {@code null}
	 * @throws IOException I/O 或解码错误
	 */
	protected void doExecute(ImageInfo imageInfo, File outputFile, String outputFormat,
						 BufferedImageOperation operation, Consumer<BufferedImage> imageConsumer) throws IOException {
		ImageEditor imageEditor = ImageEditor.of(imageInfo.getFile())
			// 矫正方向
			.correctOrientation(imageInfo.getOrientation());

		// 判断是否需要修改缩放过滤器类型
		if (Objects.nonNull(operation.getResampleFilterType())) {
			imageEditor.resampleFilterType(operation.getResampleFilterType().getFilterType());
		}

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

		// 判断是否需要添加水印
		if (Objects.nonNull(operation.getWatermarkDirection())) {
			if (ObjectUtils.allNotNull(operation.getWatermarkText(), operation.getWatermarkTextOption())) {
				imageEditor.addTextWatermark(operation.getWatermarkText(), operation.getWatermarkTextOption(),
					operation.getWatermarkDirection());
			} else if (ObjectUtils.allNotNull(operation.getWatermarkImage(), operation.getWatermarkImageOption())) {
				if (!canRead(operation.getWatermarkImage())) {
					throw new UnSupportImageTypeException("不受支持的水印图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkImage(), operation.getWatermarkImageOption(),
					operation.getWatermarkDirection());
			}
		} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
			if (ObjectUtils.allNotNull(operation.getWatermarkText(), operation.getWatermarkTextOption())) {
				imageEditor.addTextWatermark(operation.getWatermarkText(), operation.getWatermarkTextOption(),
					operation.getWatermarkX(), operation.getWatermarkY());
			} else if (ObjectUtils.allNotNull(operation.getWatermarkImage(), operation.getWatermarkImageOption())) {
				if (!canRead(operation.getWatermarkImage())) {
					throw new UnSupportImageTypeException("不受支持的水印图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkImage(), operation.getWatermarkImageOption(),
					operation.getWatermarkX(), operation.getWatermarkY());
			}
		}

		BufferedImage bufferedImage = imageEditor.toBufferedImage();
		if (Objects.nonNull(imageConsumer)) {
			imageConsumer.accept(bufferedImage);
		}
		ImageIO.write(bufferedImage, outputFormat, outputFile);
	}

	/**
	 * 判断是否支持读取指定文件类型。
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException I/O 或类型探测错误
	 */
	@Override
	public boolean canRead(File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		return ImageConstants.getSupportReadImageTypes().contains(mimeType);
	}

	/**
	 * 判断是否支持写出指定文件类型。
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持写出
	 */
	@Override
	public boolean canWrite(File file) {
		FileUtils.checkFileIfExist(file, "file 不可为 null");
		
		String fileFormat = FilenameUtils.getExtension(file.getName());
		return ImageConstants.getSupportWriteImageFormats().contains(fileFormat);
	}
}
