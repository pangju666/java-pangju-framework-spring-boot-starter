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
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.utils.ImageEditor;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.exception.UnSupportedTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.BufferedImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageFile;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 基于 ImageIO 的图像处理实现。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>使用 {@link Tika} 进行图像类型解析。</li>
 *   <li>使用 {@link ImageMetadataReader}和{@link ImageUtils} 读取图像尺寸与 EXIF 方向，并封装为 {@link ImageFile}。</li>
 *   <li>使用 {@link ImageEditor} 进行图像处理。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>线程安全与资源管理由底层 {@link ImageEditor} 与调用者确保。</li>
 * </ul>
 *
 * <p><b>执行顺序（本实现）</b></p>
 * <ul>
 *   <li>矫正方向（依据 EXIF 方向）。</li>
 *   <li>裁剪（按 {@code CENTER/OFFSET/RECT} 规则）。</li>
 *   <li>重采样滤镜类型设置（影响后续缩放质量与性能）。</li>
 *   <li>缩放（保持比例或强制尺寸）。</li>
 *   <li>旋转（角度）。</li>
 *   <li>翻转（{@code VERTICAL/HORIZONTAL}）。</li>
 *   <li>灰度化。</li>
 *   <li>图像增强（按顺序：亮度 → 对比度 → 锐化 → 模糊）。</li>
 *   <li>水印（优先使用文字水印，未配置则使用图片水印；按方向或坐标放置）。</li>
 *   <li>输出到文件。</li>
 * </ul>
 *
 * <p><b>格式支持</b></p>
 * <ul>
 *   <li>读取类型以 {@link ImageConstants#getSupportReadImageTypes()} 判定。</li>
 *   <li>写出格式以 {@link ImageConstants#getSupportWriteImageFormats()} 判定。</li>
 *   <li>输出格式通过扩展名解析并在必要时创建父目录。</li>
 * </ul>
 *
 * <p><b>异常与容错</b></p>
 * <ul>
 *   <li>元数据解析失败时使用默认方向 {@link ImageConstants#NORMAL_EXIF_ORIENTATION} 并通过文件解码获取尺寸。</li>
 *   <li>输入或输出类型不受支持时抛出 {@link UnSupportedTypeException}。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BufferedImageOperation
 */
public class BufferedImageTemplate implements ImageTemplate {
	/**
	 * 读取并返回图像信息（尺寸、格式、MIME 类型、文件大小等）。
	 *
	 * <p>优先使用 metadata 解析；失败则采用默认方向并通过文件解码获取尺寸。</p>
	 *
	 * @param file 待解析图像文件
	 * @return 图像信息
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
	 * @throws IOException                 文件读取或图像解码失败时抛出
	 */
 	@Override
 	public ImageFile read(File file) throws IOException {
		ImageFile imageFile = new ImageFile(file);
		if (!ImageConstants.getSupportReadImageTypes().contains(imageFile.getMimeType())) {
			throw new UnSupportedTypeException("不支持读取 " + imageFile.getMimeType() + " 类型图片");
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
 	 * @param inputFile 输入文件
 	 * @param outputFile 输出文件
 	 * @param operation 操作配置，可为 {@code null}
 	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
 	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
 	 * @throws ImageOperationException     ImageIO操作失败时抛出
 	 * @throws IOException                 文件读取或图像解码失败时抛出
 	 */
 	@Override
 	public void process(File inputFile, File outputFile, ImageOperation operation) throws IOException {
		 String outputImageFormat = getOutputFormat(outputFile);
		 ImageFile imageFile = read(inputFile);
		 try {
			 doProcess(imageFile, outputFile, outputImageFormat, ObjectUtils.getIfNull(
				 operation, ImageOperation.EMPTY));
		 } catch (IOException e) {
			throw new ImageOperationException("图像操作失败", e);
		 }
	}

 	/**
 	 * 执行图像操作（已解析信息形式）。
 	 *
 	 * @param imageFile 已解析的图像信息
 	 * @param outputFile 输出文件
 	 * @param operation 操作配置，可为 {@code null}
 	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
 	 * @throws ImageParsingException       图像类型、摘要或尺寸解析失败时抛出
 	 * @throws ImageOperationException     ImageIO操作失败时抛出
 	 * @throws IOException                 文件读取或图像解码失败时抛出
 	 */
 	@Override
 	public void process(ImageFile imageFile, File outputFile, ImageOperation operation) throws IOException {
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
			throw new UnSupportedTypeException("不支持读取 " + mimeType + " 类型图片");
		}

		if (imageFile.getOrientation() < 1 || imageFile.getOrientation() > 8 || Objects.isNull(imageFile.getImageSize())) {
			imageFile = read(imageFile.getFile());
		}
		doProcess(imageFile, outputFile, outputImageFormat, ObjectUtils.getIfNull(operation,
			ImageOperation.EMPTY));
	}

	/**
	 * 判断实现是否支持读取文件。
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
	 * 判断实现是否支持输出为指定的图像格式。
	 *
	 * @param format 待判定图像格式
	 * @return {@code true} 表示支持写出
	 */
	@Override
	public boolean canWrite(String format) {
		Assert.hasText(format, "format 不可为空");
		return ImageConstants.getSupportWriteImageFormats().contains(format);
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
			throw new UnSupportedTypeException("未知的输出格式");
		}
		if (!ImageConstants.getSupportWriteImageFormats().contains(outputImageFormat)) {
			throw new UnSupportedTypeException("不支持输出为" + outputImageFormat + "格式");
		}
		return outputImageFormat;
	}

	/**
	 * 执行具体处理逻辑。
	 *
	 * <p><b>执行顺序</b>：矫正方向 -> 设置输出格式 -> 裁剪 -> 重采样滤镜类型设置 -> 缩放 -> 旋转 -> 翻转 -> 灰度化 -> 图像增强（亮度 -> 对比度 ->
	 * 锐化 -> 模糊） -> 水印（文字优先，否则图片；按方向或坐标放置） -> 输出到文件。</p>
	 *
	 * @param imageFile 图像信息
	 * @param outputFile 输出文件
	 * @param outputFormat 输出格式扩展名
	 * @param operation 操作配置
	 * @throws UnSupportedTypeException 水印图片类型不受支持时抛出
	 * @throws IOException                 ImageIO操作失败时抛出
	 */
 	protected void doProcess(ImageFile imageFile, File outputFile, String outputFormat, ImageOperation operation) throws IOException {
		ImageEditor imageEditor = ImageEditor.of(imageFile.getFile())
			// 矫正方向
			.correctOrientation(imageFile.getOrientation())
			.outputFormat(outputFormat);

		BufferedImageOperation bufferedImageOperation = null;
		if (operation instanceof BufferedImageOperation) {
			bufferedImageOperation = (BufferedImageOperation) operation;
		}

		// 判断是否需要裁剪
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

		// 判断是否需要修改重采样过滤器类型
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getResampleFilterType())) {
			imageEditor.resampleFilterType(bufferedImageOperation.getResampleFilterType());
		}

		// 判断是否需要缩放
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

		// 判断是否需要旋转
		if (Objects.nonNull(operation.getRotateAngle())) {
			imageEditor.rotate(operation.getRotateAngle());
		}

		// 判断是否需要翻转
		if (Objects.nonNull(operation.getFlipDirection())) {
			switch (operation.getFlipDirection()) {
				case VERTICAL -> imageEditor.flip(ImageUtil.FLIP_VERTICAL);
				case HORIZONTAL -> imageEditor.flip(ImageUtil.FLIP_HORIZONTAL);
			}
		}

		// 判断是否需要灰度化
		if (operation.isGrayscale()) {
			imageEditor.grayscale();
		}

		// 判断是否需要修改亮度
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getBrightnessAmount())) {
			imageEditor.brightness(bufferedImageOperation.getBrightnessAmount());
		}

		// 判断是否需要修改对比度
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getContrastAmount())) {
			imageEditor.brightness(bufferedImageOperation.getContrastAmount());
		}

		// 判断是否需要锐化
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getSharpenAmount())) {
			imageEditor.brightness(bufferedImageOperation.getSharpenAmount());
		}

		// 判断是否需要模糊
		if (Objects.nonNull(bufferedImageOperation) && Objects.nonNull(bufferedImageOperation.getBlurRadius())) {
			imageEditor.brightness(bufferedImageOperation.getBlurRadius());
		}

		// 判断是否需要添加水印
		if (Objects.nonNull(operation.getWatermarkDirection())) {
			if (Objects.nonNull(bufferedImageOperation) &&
				ObjectUtils.allNotNull(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption())) {
				imageEditor.addTextWatermark(bufferedImageOperation.getWatermarkText(), bufferedImageOperation.getWatermarkTextOption(),
					operation.getWatermarkDirection());
			} else if (ObjectUtils.allNotNull(operation.getWatermarkImage(), operation.getWatermarkImageOption())) {
				if (!canRead(operation.getWatermarkImage())) {
					throw new UnSupportedTypeException("不受支持的水印图片类型");
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
					throw new UnSupportedTypeException("不受支持的水印图片类型");
				}
				imageEditor.addImageWatermark(operation.getWatermarkImage(), operation.getWatermarkImageOption(),
					operation.getWatermarkX(), operation.getWatermarkY());
			}
		}

		FileUtils.forceMkdirParent(outputFile);
		imageEditor.toFile(outputFile);
	}
}
