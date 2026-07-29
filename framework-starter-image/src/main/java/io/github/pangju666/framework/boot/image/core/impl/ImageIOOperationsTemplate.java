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

import io.github.pangju666.commons.image.io.resource.ImageIOResource;
import io.github.pangju666.commons.image.processor.ImageProcessor;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.core.ImageOperationsTemplate;
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.opeartions.ImageIOOperations;
import io.github.pangju666.framework.boot.image.model.opeartions.ImageOperations;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.awt.image.ImageFilter;
import java.awt.image.ImagingOpException;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * ImageIO图像操作模板实现类。
 * <p>
 * 基于Java ImageIO实现图像处理，支持基本的图像变换、滤镜和水印操作。
 * </p>
 *
 * @since 2.1.0
 * @see ImageProcessor
 */
public class ImageIOOperationsTemplate implements ImageOperationsTemplate {
	/**
	 * 处理图像资源并输出到输出流。
	 *
	 * @param resource 图像资源
	 * @param outputStream 输出流
	 * @param outputFormat 输出格式
	 * @param operations 图像操作配置
	 * @throws UnsupportedResourceException 不支持的资源异常
	 * @throws ImageParsingException 图像解析异常
	 * @throws ImageOperationException 图像操作异常
	 * @since 2.1.0
	 */
	@Override
	public void process(IOResource resource, OutputStream outputStream, String outputFormat, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException {
		Assert.hasText(outputFormat, "outputFormat 不可为 null");

		if (!canWrite(outputFormat)) {
			throw new UnsupportedResourceException("不支持输出为" + outputFormat + "格式");
		}

		try {
			processImageResource(resource, operations).toOutputStream(outputStream, outputFormat);
		} catch (IOException e) {
			throw new ImageOperationException("图像输出失败", e);
		}
	}

	/**
	 * 处理图像资源并输出到文件。
	 *
	 * @param resource 图像资源
	 * @param outputFile 输出文件
	 * @param operations 图像操作配置
	 * @throws UnsupportedResourceException 不支持的资源异常
	 * @throws ImageParsingException 图像解析异常
	 * @throws ImageOperationException 图像操作异常
	 * @since 2.1.0
	 */
	@Override
	public void process(IOResource resource, File outputFile, ImageOperations<?> operations)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		String outputFileExtension = FilenameUtils.getExtension(outputFile.getName());
		if (!canWrite(outputFileExtension)) {
			throw new UnsupportedResourceException("不支持输出为" + outputFileExtension + "格式");
		}

		try {
			processImageResource(resource, operations).toFile(outputFile, outputFileExtension);
		} catch (IOException e) {
			throw new ImageOperationException("图像输出失败", e);
		}
	}

	/**
	 * 检查是否支持读取指定资源。
	 *
	 * @param resource 图像资源
	 * @return 如果支持读取返回true，否则返回false
	 * @since 2.1.0
	 */
	@Override
	public boolean canRead(IOResource resource) {
		Assert.notNull(resource, "resource 不可为 null");

		if (resource instanceof ImageIOResource) {
			return true;
		} else {
			return resource.isImage() && ImageConstants.getSupportedReadImageTypes().contains(resource.getMimeType());
		}
	}

	/**
	 * 检查是否支持写入指定格式。
	 *
	 * @param format 图像格式
	 * @return 如果支持写入返回true，否则返回false
	 * @since 2.1.0
	 */
	@Override
	public boolean canWrite(String format) {
		Assert.hasText(format, "format 不可为空");

		return ImageConstants.getSupportedWriteImageFormats().contains(format.toUpperCase());
	}

	/**
	 * 处理图像资源。
	 * <p>
	 * 将资源转换为ImageProcessor，执行变换、滤镜和水印操作。
	 * </p>
	 *
	 * @param resource 图像资源
	 * @param operations 图像操作配置
	 * @return ImageProcessor对象
	 * @throws ImageParsingException 图像解析异常
	 * @throws ImageOperationException 图像操作异常
	 * @since 2.1.0
	 */
	protected ImageProcessor processImageResource(IOResource resource, ImageOperations<?> operations) {
		Assert.notNull(resource, "resource 不可为 null");
		Assert.notNull(operations, "operations 不可为 null");


		ImageIOResource imageResource;
		if (resource instanceof ImageIOResource imageIOResource) {
			imageResource = imageIOResource;
		} else {
			try {
				imageResource = new ImageIOResource(resource);
			} catch (IOException e) {
				throw new ImageParsingException("图像读取失败", e);
			}
		}

		ImageIOOperations imageOperations;
		if (operations instanceof ImageIOOperations imageIOOperations) {
			imageOperations = imageIOOperations;
		} else {
			imageOperations = new ImageIOOperations(operations);
		}

		ImageProcessor imageProcessor;
		try {
			imageProcessor = ImageProcessor.of(imageResource);
		} catch (IOException e) {
			throw new ImageParsingException("图像读取失败", e);
		}

		try {
			// 执行变换操作
			processTransform(imageProcessor, imageOperations);

			// 执行滤镜操作
			processFilter(imageProcessor, imageOperations);

			// 执行水印操作
			processWatermark(imageProcessor, imageOperations);

			return imageProcessor;
		} catch (ImagingOpException | RasterFormatException e) {
			throw new ImageOperationException("图像处理失败", e);
		}
	}

	/**
	 * 处理图像变换操作。
	 * <p>
	 * 包括裁剪、缩放、旋转和翻转。
	 * </p>
	 *
	 * @param imageProcessor 图像处理器
	 * @param imageOperations ImageIO操作配置
	 * @since 2.1.0
	 */
	protected void processTransform(ImageProcessor imageProcessor, ImageIOOperations imageOperations) {
		Assert.notNull(imageProcessor, "imageProcessor 不可为 null");
		Assert.notNull(imageOperations, "imageOperations 不可为 null");

		// 判断是否需要裁剪
		if (Objects.nonNull(imageOperations.getCropType())) {
			if (imageOperations.getCropType() == CropType.CENTER) {
				if (ObjectUtils.allNotNull(imageOperations.getCropCenterWidth(), imageOperations.getCropCenterHeight())) {
					imageProcessor.cropByCenter(imageOperations.getCropCenterWidth(), imageOperations.getCropCenterHeight());
				}
			} else if (imageOperations.getCropType() == CropType.OFFSET) {
				if (ObjectUtils.allNotNull(imageOperations.getCropTopOffset(), imageOperations.getCropBottomOffset(),
					imageOperations.getCropLeftOffset(), imageOperations.getCropRightOffset())) {
					imageProcessor.cropByOffset(imageOperations.getCropTopOffset(), imageOperations.getCropBottomOffset(),
						imageOperations.getCropLeftOffset(), imageOperations.getCropRightOffset());
				}
			} else if (imageOperations.getCropType() == CropType.RECT) {
				if (ObjectUtils.allNotNull(imageOperations.getCropRectX(), imageOperations.getCropRectY(),
					imageOperations.getCropRectWidth(), imageOperations.getCropRectHeight())) {
					imageProcessor.cropByRect(imageOperations.getCropRectX(), imageOperations.getCropRectY(),
						imageOperations.getCropRectWidth(), imageOperations.getCropRectHeight());
				}
			}
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.allNotNull(imageOperations.getTargetWidth(), imageOperations.getTargetHeight())) {
			if (imageOperations.isForceScale()) {
				imageProcessor.resize(imageOperations.getTargetWidth(), imageOperations.getTargetHeight(),
					imageOperations.getResampleFilter().twelveMonkeysFilterType);
			} else {
				imageProcessor.scale(imageOperations.getTargetWidth(), imageOperations.getTargetHeight(),
					imageOperations.getResampleFilter().twelveMonkeysFilterType);
			}
		} else if (Objects.nonNull(imageOperations.getScalingFactor())) {
			imageProcessor.scale(imageOperations.getScalingFactor(),
				imageOperations.getResampleFilter().twelveMonkeysFilterType);
		} else if (Objects.nonNull(imageOperations.getTargetWidth())) {
			imageProcessor.scaleByWidth(imageOperations.getTargetWidth(),
				imageOperations.getResampleFilter().twelveMonkeysFilterType);
		} else if (Objects.nonNull(imageOperations.getTargetHeight())) {
			imageProcessor.scaleByHeight(imageOperations.getTargetHeight(),
				imageOperations.getResampleFilter().twelveMonkeysFilterType);
		}

		// 判断是否需要旋转
		if (Objects.nonNull(imageOperations.getRotateAngle())) {
			imageProcessor.rotate(imageOperations.getRotateAngle());
		}

		// 判断是否需要翻转
		if (Objects.nonNull(imageOperations.getFlipDirection())) {
			imageProcessor.flip(imageOperations.getFlipDirection());
		}
	}

	/**
	 * 处理图像滤镜操作。
	 * <p>
	 * 包括灰度化、亮度调整、对比度调整、透明度、锐化、模糊和自定义滤镜。
	 * </p>
	 *
	 * @param imageProcessor 图像处理器
	 * @param imageOperations ImageIO操作配置
	 * @since 2.1.0
	 */
	protected void processFilter(ImageProcessor imageProcessor, ImageIOOperations imageOperations) {
		Assert.notNull(imageProcessor, "imageProcessor 不可为 null");
		Assert.notNull(imageOperations, "imageOperations 不可为 null");

		if (imageOperations.isGrayscale()) {
			imageProcessor.grayscale();
		}

		if (Objects.nonNull(imageOperations.getBrightnessAmount())) {
			imageProcessor.brightness(imageOperations.getBrightnessAmount());
		}

		if (Objects.nonNull(imageOperations.getContrastAmount())) {
			imageProcessor.contrast(imageOperations.getContrastAmount());
		}

		if (Objects.nonNull(imageOperations.getGlobalOpacity())) {
			imageProcessor.opacity(imageOperations.getGlobalOpacity());
		}

		if (Objects.nonNull(imageOperations.getSharpenAmount())) {
			imageProcessor.sharpen(imageOperations.getSharpenAmount());
		}

		if (Objects.nonNull(imageOperations.getBlurRadius())) {
			imageProcessor.blur(imageOperations.getBlurRadius());
		}

		if (!CollectionUtils.isEmpty(imageOperations.getFilters())) {
			for (ImageFilter filter : imageOperations.getFilters()) {
				if (Objects.nonNull(filter)) {
					imageProcessor.filter(filter);
				}
			}
		}
	}

	/**
	 * 处理图像水印操作。
	 * <p>
	 * 支持图像水印和文字水印。
	 * </p>
	 *
	 * @param imageProcessor 图像处理器
	 * @param imageOperations ImageIO操作配置
	 * @throws ImageParsingException 图像解析异常
	 * @since 2.1.0
	 */
	protected void processWatermark(ImageProcessor imageProcessor, ImageIOOperations imageOperations) {
		Assert.notNull(imageProcessor, "imageProcessor 不可为 null");
		Assert.notNull(imageOperations, "imageOperations 不可为 null");

		if (Objects.nonNull(imageOperations.getWatermarkImage())) {
			try {
				imageProcessor.addImageWatermark(imageOperations.getWatermarkImage().getBufferedImage(),
					imageOperations.getWatermarkImageOption());
			} catch (IOException e) {
				throw new ImageParsingException("图像读取失败", e);
			}
		} else if (Objects.nonNull(imageOperations.getWatermarkText())) {
			imageProcessor.addTextWatermark(imageOperations.getWatermarkText(), imageOperations.getWatermarkTextOption());
		}
	}
}
