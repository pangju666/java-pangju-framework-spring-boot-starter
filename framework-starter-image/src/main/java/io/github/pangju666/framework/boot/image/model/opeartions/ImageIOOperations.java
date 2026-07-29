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

package io.github.pangju666.framework.boot.image.model.opeartions;

import io.github.pangju666.commons.image.io.resource.ImageIOResource;
import io.github.pangju666.commons.image.model.ImageWatermarkOption;
import io.github.pangju666.commons.image.model.TextWatermarkOption;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.awt.image.ImageFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * ImageIO图像操作类。
 * <p>
 * 继承自{@link ImageOperations}，使用Java ImageIO进行图像处理。
 * </p>
 * <p>
 * 扩展功能包括：
 * <ul>
 *     <li>重采样滤镜配置</li>
 *     <li>滤镜效果：模糊、锐化、对比度、亮度</li>
 *     <li>自定义滤镜支持</li>
 *     <li>图像水印</li>
 *     <li>文字水印</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class ImageIOOperations extends ImageOperations<ImageIOOperations> {
	/**
	 * 重采样滤镜，默认为LANCZOS
	 *
	 * @since 2.1.0
	 */
	protected ResampleFilter resampleFilter = ResampleFilter.LANCZOS;

	/**
	 * 模糊半径
	 *
	 * @since 2.1.0
	 */
	protected Float blurRadius;
	/**
	 * 锐化程度
	 *
	 * @since 2.1.0
	 */
	protected Float sharpenAmount;
	/**
	 * 对比度调整量
	 *
	 * @since 2.1.0
	 */
	protected Float contrastAmount;
	/**
	 * 亮度调整量
	 *
	 * @since 2.1.0
	 */
	protected Float brightnessAmount;
	/**
	 * 自定义滤镜列表
	 *
	 * @since 2.1.0
	 */
	protected List<ImageFilter> filters = new ArrayList<>();

	/**
	 * 图像水印资源
	 *
	 * @since 2.1.0
	 */
	protected ImageIOResource watermarkImage;
	/**
	 * 图像水印选项
	 *
	 * @since 2.1.0
	 */
	protected ImageWatermarkOption watermarkImageOption = new ImageWatermarkOption();

	/**
	 * 文字水印文本
	 *
	 * @since 2.1.0
	 */
	protected String watermarkText;
	/**
	 * 文字水印选项
	 *
	 * @since 2.1.0
	 */
	protected TextWatermarkOption watermarkTextOption = new TextWatermarkOption();

	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public ImageIOOperations() {
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param operations 源操作对象
	 * @since 2.1.0
	 */
	public ImageIOOperations(@Nullable ImageOperations<?> operations) {
		super(operations);

		if (Objects.nonNull(operations) && operations instanceof ImageIOOperations imageIOOperations) {
			/* 缩放相关配置 */
			this.resampleFilter = imageIOOperations.resampleFilter;

			/* 图像水印相关配置 */
			this.watermarkImage = imageIOOperations.watermarkImage;
			this.watermarkImageOption = imageIOOperations.watermarkImageOption;

			/* 文字水印相关配置 */
			this.watermarkText = imageIOOperations.watermarkText;
			this.watermarkTextOption = imageIOOperations.watermarkTextOption;

			/* 滤镜相关配置 */
			this.blurRadius = imageIOOperations.blurRadius;
			this.sharpenAmount = imageIOOperations.sharpenAmount;
			this.contrastAmount = imageIOOperations.contrastAmount;
			this.brightnessAmount = imageIOOperations.brightnessAmount;
			this.filters = imageIOOperations.filters;
		}
	}

	/**
	 * 获取重采样滤镜。
	 *
	 * @return 重采样滤镜
	 * @since 2.1.0
	 */
	public ResampleFilter getResampleFilter() {
		return resampleFilter;
	}

	/**
	 * 获取模糊半径。
	 *
	 * @return 模糊半径
	 * @since 2.1.0
	 */
	public @Nullable Float getBlurRadius() {
		return blurRadius;
	}

	/**
	 * 获取锐化程度。
	 *
	 * @return 锐化程度
	 * @since 2.1.0
	 */
	public @Nullable Float getSharpenAmount() {
		return sharpenAmount;
	}

	/**
	 * 获取对比度调整量。
	 *
	 * @return 对比度调整量
	 * @since 2.1.0
	 */
	public @Nullable Float getContrastAmount() {
		return contrastAmount;
	}

	/**
	 * 获取亮度调整量。
	 *
	 * @return 亮度调整量
	 * @since 2.1.0
	 */
	public @Nullable Float getBrightnessAmount() {
		return brightnessAmount;
	}

	/**
	 * 获取图像水印资源。
	 *
	 * @return 图像水印资源
	 * @since 2.1.0
	 */
	public @Nullable ImageIOResource getWatermarkImage() {
		return watermarkImage;
	}

	/**
	 * 获取图像水印选项。
	 *
	 * @return 图像水印选项
	 * @since 2.1.0
	 */
	public ImageWatermarkOption getWatermarkImageOption() {
		return watermarkImageOption;
	}

	/**
	 * 获取文字水印文本。
	 *
	 * @return 文字水印文本
	 * @since 2.1.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 获取文字水印选项。
	 *
	 * @return 文字水印选项
	 * @since 2.1.0
	 */
	public TextWatermarkOption getWatermarkTextOption() {
		return watermarkTextOption;
	}

	/**
	 * 获取自定义滤镜列表。
	 *
	 * @return 自定义滤镜列表
	 * @since 2.1.0
	 */
	public Collection<ImageFilter> getFilters() {
		return filters;
	}

	/**
	 * 设置重采样滤镜。
	 *
	 * @param resampleFilter 重采样滤镜
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations resampleFilter(@Nullable ResampleFilter resampleFilter) {
		if (Objects.nonNull(resampleFilter)) {
			this.resampleFilter = resampleFilter;
		}
		return this;
	}

	/**
	 * 设置图像水印。
	 *
	 * @param resource 图像资源
	 * @return 当前实例，支持链式调用
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	public ImageIOOperations watermarkImage(@Nullable IOResource resource) throws IOException {
		if (Objects.nonNull(resource)) {
			if (resource instanceof ImageIOResource imageIOResource) {
				this.watermarkImage = imageIOResource;
			} else {
				this.watermarkImage = new ImageIOResource(resource);
			}
			this.watermarkText = null;
		}
		return this;
	}

	/**
	 * 设置图像水印选项。
	 *
	 * @param option 图像水印选项
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations watermarkImageOption(@Nullable ImageWatermarkOption option) {
		if (Objects.nonNull(option)) {
			this.watermarkImageOption = option;
		}
		return this;
	}

	/**
	 * 设置文字水印文本。
	 *
	 * @param text 文字水印文本
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations watermarkText(@Nullable String text) {
		if (StringUtils.isNotBlank(text)) {
			this.watermarkText = text;
			this.watermarkImage = null;
		}
		return this;
	}

	/**
	 * 设置文字水印选项。
	 *
	 * @param option 文字水印选项
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations textWatermarkOption(@Nullable TextWatermarkOption option) {
		if (Objects.nonNull(option)) {
			this.watermarkTextOption = option;
		}
		return this;
	}

	/**
	 * 设置模糊效果（默认半径）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations blur() {
		this.blurRadius = 1.5f;
		return this;
	}

	/**
	 * 设置模糊效果。
	 *
	 * @param radius 模糊半径，大于1
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations blur(@Nullable Float radius) {
		if (Objects.nonNull(radius) && radius > 1) {
			this.blurRadius = radius;
		}
		return this;
	}

	/**
	 * 设置锐化效果（默认程度）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations sharpen() {
		this.sharpenAmount = 0.3f;
		return this;
	}

	/**
	 * 设置锐化效果。
	 *
	 * @param amount 锐化程度，不为0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations sharpen(@Nullable Float amount) {
		if (Objects.nonNull(amount) && amount != 0) {
			this.sharpenAmount = amount;
		}
		return this;
	}

	/**
	 * 设置对比度调整（默认值）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations contrast() {
		this.contrastAmount = 0.3f;
		return this;
	}

	/**
	 * 设置对比度调整。
	 *
	 * @param amount 对比度调整量，范围-1.0到1.0，不为0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations contrast(@Nullable Float amount) {
		if (Objects.nonNull(amount) && amount != 0 && amount <= 1.0 && amount >= -1.0) {
			this.contrastAmount = amount;
		}
		return this;
	}

	/**
	 * 设置亮度调整。
	 *
	 * @param amount 亮度调整量，范围-2.0到2.0，不为0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations brightness(@Nullable Float amount) {
		if (Objects.nonNull(amount) && amount != 0 && amount <= 2.0 && amount >= -2.0) {
			this.brightnessAmount = amount;
		}
		return this;
	}

	/**
	 * 添加自定义滤镜。
	 *
	 * @param imageFilter 图像滤镜
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public ImageIOOperations addFilter(@Nullable ImageFilter imageFilter) {
		if (Objects.nonNull(imageFilter)) {
			this.filters.add(imageFilter);
		}
		return this;
	}

	/**
	 * 重置所有配置。
	 *
	 * @since 2.1.0
	 */
	public void reset() {
		super.reset();

		/* 缩放相关配置 */
		this.resampleFilter = ResampleFilter.LANCZOS;

		/* 滤镜相关配置 */
		this.blurRadius = null;
		this.sharpenAmount = null;
		this.contrastAmount = null;
		this.brightnessAmount = null;
		this.filters = new ArrayList<>();

		/* 文字水印相关配置 */
		this.watermarkText = null;
		this.watermarkTextOption = new TextWatermarkOption();

		/* 图像水印相关配置 */
		this.watermarkImage = null;
		this.watermarkImageOption = new ImageWatermarkOption();

	}
}
