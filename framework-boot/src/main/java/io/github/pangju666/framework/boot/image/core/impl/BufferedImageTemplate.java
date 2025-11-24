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
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.UnSupportImageTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.BufferedImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageFile;
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
 * 基于 TwelveMonkeys 与 metadata-extractor 的图像处理实现。
 *
 * <p><strong>概述</strong></p>
 * <ul>
 *   <li>读取图像元数据（尺寸、EXIF 方向）并封装为 {@link ImageFile}。</li>
 *   <li>提供缩放与尺寸调整能力：{@code resize} 不保持长宽比，{@code scale} 保持长宽比。</li>
 *   <li>结合能力集合进行读/写支持判定。</li>
 * </ul>
 *
 * <p><strong>约束</strong></p>
 * <ul>
 *   <li>线程安全与资源管理由底层 {@link ImageEditor} 与调用者确保。</li>
 * </ul>
 *
 * <p><strong>执行顺序（本实现）</strong></p>
 * <ul>
 *   <li>矫正方向 → 缩放 → 裁剪 → 绘制文字/图片水印 → 输出。</li>
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
public class BufferedImageTemplate implements ImageTemplate<BufferedImage> {
 	/**
 	 * 读取图像信息（格式、大小、MIME、EXIF 方向与尺寸）。
 	 *
 	 * <p>优先使用 metadata 解析；失败则采用默认方向并通过文件解码获取尺寸。</p>
 	 *
 	 * @param file 待解析图像文件
 	 * @return 图像信息
	 * @throws UnSupportImageTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws IOException                 文件读取或图像解码失败时抛出
 	 */
 	@Override
 	public ImageFile readImage(File file) throws IOException {
		ImageFile imageFile = new ImageFile(file);
		if (!ImageConstants.getSupportReadImageTypes().contains(imageFile.getMimeType())) {
			throw new UnSupportImageTypeException("不支持读取 " + imageFile.getMimeType() + " 类型图片");
		}

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			imageFile.setOrientation(ImageUtils.getExifOrientation(metadata));
			imageFile.setImageSize(ImageUtils.getSize(metadata));
		} catch (ImageProcessingException | IOException ignored) {
			imageFile.setOrientation(ImageConstants.NORMAL_EXIF_ORIENTATION);
			try {
				imageFile.setImageSize(ImageUtils.getSize(file, false));
			} catch (IOException e) {
				throw new ImageParsingException(file, "尺寸解析失败");
			}
		}
		return imageFile;
	}

 	/**
 	 * 执行图像操作（输入文件形式）。
 	 *
 	 * <p>执行顺序：矫正方向 → 缩放 → 裁剪 → 绘制文字/图片水印 → 输出。</p>
 	 *
 	 * @param inputFile 输入文件
 	 * @param outputFile 输出文件
 	 * @param operation 操作配置
 	 * @param imageConsumer 中间处理回调，可为 {@code null}
 	 * @throws UnSupportImageTypeException 图像类型不受支持时抛出
 	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
 	 * @throws ImageOperationException     ImageIO操作失败时抛出
 	 * @throws IOException                 文件读取或图像解码失败时抛出
 	 */
 	@Override
 	public void execute(File inputFile, File outputFile, ImageOperation operation, Consumer<BufferedImage> imageConsumer) throws IOException {
		String outputImageFormat = getOutputFormat(outputFile);
		ImageFile imageFile = readImage(inputFile);
		try {
			doExecute(imageFile, outputFile, outputImageFormat, operation, imageConsumer);
		} catch (IOException e) {
			throw new ImageOperationException("图像操作失败", e);
		}
	}

 	/**
 	 * 执行图像操作（已解析信息形式）。
 	 *
 	 * <p>执行顺序：矫正方向 → 缩放 → 裁剪 → 绘制文字/图片水印 → 输出。</p>
 	 *
 	 * @param imageFile 已解析的图像信息
 	 * @param outputFile 输出文件
 	 * @param operation 操作配置
 	 * @param imageConsumer 中间处理回调，可为 {@code null}
 	 * @throws UnSupportImageTypeException 图像类型不受支持时抛出
 	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
 	 * @throws ImageOperationException     ImageIO操作失败时抛出
 	 * @throws IOException                 文件读取或图像解码失败时抛出
 	 */
 	@Override
 	public void execute(ImageFile imageFile, File outputFile, ImageOperation operation, Consumer<BufferedImage> imageConsumer) throws IOException {
		Assert.notNull(imageFile, "imageFile 不可为 null");
		FileUtils.checkFile(imageFile.getFile(), "imageFile 未设置 file 属性");

		String outputImageFormat = getOutputFormat(outputFile);
		String mimeType = imageFile.getMimeType();
		try {
			if (StringUtils.isBlank(mimeType)) {
				mimeType = FileUtils.getMimeType(imageFile.getFile());
			}
		} catch (IOException e) {
			throw new ImageParsingException("图像类型解析失败", e);
		}
		if (!ImageConstants.getSupportReadImageTypes().contains(mimeType)) {
			throw new UnSupportImageTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		if (imageFile.getOrientation() < 1 || imageFile.getOrientation() > 8 || Objects.isNull(imageFile.getImageSize())) {
			imageFile = readImage(imageFile.getFile());
		}
		doExecute(imageFile, outputFile, outputImageFormat, operation, imageConsumer);
	}

	/**
	 * 判断是否支持读取指定文件类型。
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException 文件读取或图像类型解析失败时抛出
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

 	/**
 	 * 解析并校验输出格式。
 	 *
 	 * <p>参数校验规则：{@code outputFile} 不可为 {@code null}；扩展名不能为空且需在可写集合中。</p>
 	 * <p>副作用：在必要时创建输出文件父目录。</p>
 	 *
 	 * @param outputFile 输出文件
 	 * @return 输出格式扩展名
 	 * @throws UnSupportImageTypeException 输出格式未知或不受支持时抛出
 	 * @throws IOException                 输出文件目录创建失败或 I/O 错误时抛出
 	 */
 	protected String getOutputFormat(File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputFile.getName());
		if (StringUtils.isBlank(outputImageFormat)) {
			throw new UnSupportImageTypeException("未知的输出格式");
		}
		if (!ImageConstants.getSupportWriteImageFormats().contains(outputImageFormat)) {
			throw new UnSupportImageTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		FileUtils.forceMkdirParent(outputFile);
		return outputImageFormat;
	}

 	/**
 	 * 执行具体处理逻辑。
 	 *
 	 * <p>执行顺序：矫正方向 → 缩放 → 裁剪 → 水印 → 输出。</p>
 	 *
 	 * @param imageFile 图像信息
 	 * @param outputFile 输出文件
 	 * @param outputFormat 输出格式扩展名
 	 * @param operation 操作配置
 	 * @param imageConsumer 中间处理回调，可为 {@code null}
 	 * @throws UnSupportImageTypeException 水印图片类型不受支持时抛出
 	 * @throws IOException                 ImageIO操作失败时抛出
 	 */
 	protected void doExecute(ImageFile imageFile, File outputFile, String outputFormat, ImageOperation operation,
 						 Consumer<BufferedImage> imageConsumer) throws IOException {
		ImageEditor imageEditor = ImageEditor.of(imageFile.getFile())
			// 矫正方向
			.correctOrientation(imageFile.getOrientation());

		BufferedImageOperation bufferedImageOperation = null;
		if (operation instanceof BufferedImageOperation) {
			bufferedImageOperation = (BufferedImageOperation) operation;
		}

		// 判断是否需要修改缩放过滤器类型
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getResampleFilterType())) {
			imageEditor.resampleFilterType(bufferedImageOperation.getResampleFilterType().getFilterType());
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

		// 判断是否需要执行裁剪
		if (Objects.nonNull(operation.getCropType())) {
			if (operation.getCropType() == CropType.CENTER) {
				if (ObjectUtils.allNotNull(operation.getCenterCropWidth(), operation.getCenterCropHeight())) {
					imageEditor.cropByCenter(operation.getCenterCropWidth(), operation.getCenterCropHeight());
				}
			} else if (operation.getCropType() == CropType.OFFSET) {
				if (ObjectUtils.allNotNull(operation.getTopCropOffset(), operation.getBottomCropOffset(),
					operation.getLeftCropOffset(), operation.getRightCropOffset())) {
					imageEditor.cropByOffset(operation.getTopCropOffset(),
						operation.getBottomCropOffset(), operation.getLeftCropOffset(),
						operation.getRightCropOffset());
				}
			} else if (operation.getCropType() == CropType.RECT) {
				if (ObjectUtils.allNotNull(operation.getCropRectX(), operation.getCropRectY(),
					operation.getCropRectWidth(), operation.getCropRectHeight())) {
					imageEditor.cropByRect(operation.getCropRectX(), operation.getCropRectY(),
						operation.getCropRectWidth(), operation.getCropRectHeight());
				}
			}
		}

		// 判断是否需要添加水印
		if (Objects.nonNull(operation.getWatermarkDirection())) {
			if (Objects.nonNull(bufferedImageOperation) &&
				ObjectUtils.allNotNull(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption())) {
				imageEditor.addTextWatermark(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption(),
					operation.getWatermarkDirection());
			} else if (ObjectUtils.allNotNull(operation.getWatermarkImage(), operation.getWatermarkImageOption())) {
				if (!canRead(operation.getWatermarkImage())) {
					throw new UnSupportImageTypeException("不受支持的水印图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkImage(), operation.getWatermarkImageOption(),
					operation.getWatermarkDirection());
			}
		} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
			if (Objects.nonNull(bufferedImageOperation) &&
				ObjectUtils.allNotNull(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption())) {
				imageEditor.addTextWatermark(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption(),
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
}
