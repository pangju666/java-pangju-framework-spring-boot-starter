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

package io.github.pangju666.framework.boot.image.model;

import io.github.pangju666.commons.image.model.TextWatermarkOption;
import io.github.pangju666.framework.boot.image.core.impl.BufferedImageTemplate;
import io.github.pangju666.framework.boot.image.enums.FlipDirection;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * 面向 {@link BufferedImageTemplate} 的图像操作配置，扩展文字水印与缩放重采样策略能力，
 * 并提供模糊/锐化/对比度/亮度调整与自定义滤镜管线。
 * 使用构建器模式设置参数，供图像处理组件读取并执行。
 *
 * <p><strong>使用说明</strong>：通过构建器链式设置参数；不满足校验规则的参数将被忽略。</p>
 * <p><strong>互斥规则</strong>：水印方向与坐标互斥；设置其中之一会清空另一种配置。</p>
 * <p><strong>定位规则</strong>：可使用 {@code watermarkDirection} 或 {@code watermarkPosition(x,y)}；坐标需为正数。</p>
 * <p><strong>裁剪规则</strong>：支持中心裁剪、偏移裁剪与矩形裁剪；如果裁剪参数为空、非正数或越界，则不设置裁剪。</p>
 * <p><strong>缩放规则</strong>：{@code forceScale(width,height)} 强制缩放到指定尺寸；按比例/按宽/按高缩放为等比，并会关闭强制缩放且清空其它尺寸/比例。</p>
 * <p><strong>透明度范围</strong>：取值区间 [0,1]；水印透明度遵循该范围。</p>
 * <p><strong>旋转/翻转</strong>：旋转角度正数表示顺时针、负数表示逆时针；翻转方向由 {@link FlipDirection} 指定。</p>
 * <p><strong>灰度化</strong>：当开启灰度化时，输出图像为灰度模式。</p>
 * <p><strong>文字水印</strong>：提供文本内容与样式配置；与图片水印互斥。</p>
 * <p><strong>缩放重采样</strong>：支持重采样滤镜类型选择。</p>
 * <p><strong>图像增强</strong>：支持模糊、锐化、对比度与亮度调节。</p>
 * <p><strong>滤镜管线</strong>：支持自定义 {@link ImageFilter}，按添加顺序应用。</p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BufferedImageTemplate
 */
public class BufferedImageOperation extends ImageOperation {
	/**
	 * 缩放重采样滤镜类型，用于控制缩放质量与性能。
	 *
	 * @since 1.0.0
	 */
	protected Integer resampleFilterType;
	/**
	 * 水印文本（与图片水印互斥）。
	 *
	 * @since 1.0.0
	 */
	protected String watermarkText;
	/**
	 * 文字水印配置（与图片水印互斥）。
	 *
	 * @since 1.0.0
	 */
	protected TextWatermarkOption watermarkTextOption = new TextWatermarkOption();
	/**
	 * 模糊半径（像素）。
	 *
	 * <p>取值建议：{@code > 1}；当未设置时不进行模糊处理。</p>
	 *
	 * @since 1.0.0
	 */
	protected Float blurRadius;
	/**
	 * 锐化强度。
	 *
	 * <p>取值建议：{@code > 0}；当未设置时不进行锐化处理。</p>
	 *
	 * @since 1.0.0
	 */
	protected Float sharpenAmount;
	/**
	 * 对比度调整幅度。
	 *
	 * <p>取值建议：{@code > 0}；当未设置时不调整对比度。</p>
	 *
	 * @since 1.0.0
	 */
	protected Float contrastAmount;
	/**
	 * 亮度调整幅度。
	 *
	 * <p>取值建议：{@code > 0}；当未设置时不调整亮度。</p>
	 *
	 * @since 1.0.0
	 */
	protected Float brightnessAmount;
	/**
	 * 额外的图像滤镜集合。
	 *
	 * <p>用于承载自定义的 {@link ImageFilter}，按添加顺序应用。</p>
	 *
	 * @since 1.0.0
	 */
	protected Collection<ImageFilter> filters = new ArrayList<>();

	protected BufferedImageOperation() {
	}

	/**
	 * 获取模糊半径。
	 *
	 * @return 模糊半径（像素），未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getBlurRadius() {
		return blurRadius;
	}

	/**
	 * 获取锐化强度。
	 *
	 * @return 锐化强度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getSharpenAmount() {
		return sharpenAmount;
	}

	/**
	 * 获取对比度调整幅度。
	 *
	 * @return 对比度调整幅度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getContrastAmount() {
		return contrastAmount;
	}

	/**
	 * 获取亮度调整幅度。
	 *
	 * @return 亮度调整幅度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getBrightnessAmount() {
		return brightnessAmount;
	}

	/**
	 * 获取图像滤镜集合。
	 *
	 * @return 过滤器集合（按添加顺序应用）
	 * @since 1.0.0
	 */
	public Collection<ImageFilter> getFilters() {
		return filters;
	}

	/**
	 * 获取文字水印内容（与图片水印互斥）。
	 *
	 * @return 文字水印文本，未设置或被图片水印清空时为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 获取文字水印配置选项。
	 *
	 * @return 文字水印配置（非空）
	 * @since 1.0.0
	 */
	public TextWatermarkOption getWatermarkTextOption() {
		return watermarkTextOption;
	}

	/**
	 * 获取缩放重采样滤镜类型。
	 *
	 * @return 重采样滤镜类型，未设置时为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getResampleFilterType() {
		return resampleFilterType;
	}

	/**
	 * {@link BufferedImageOperation} 的构建器，提供缩放重采样与文字水印的链式配置。
	 *
	 * @since 1.0.0
	 */
	public static class BufferedImageOperationBuilder extends ImageOperationBuilder<BufferedImageOperationBuilder, BufferedImageOperation> {
		public BufferedImageOperationBuilder() {
			super(new BufferedImageOperation());
		}

		/**
		 * 设置缩放重采样滤镜类型（一般不建议设置这个，底层实现中我已经设置了合适的默认值）。
		 *
		 * <p>参数校验规则：如果 {@code resampleFilter} 为 null，则不设置。</p>
		 *
		 * @param resampleFilter 重采样滤镜类型
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder resampleFilter(ResampleFilter resampleFilter) {
			if (Objects.nonNull(resampleFilter)) {
				imageOperation.resampleFilterType = resampleFilter.getFilterType();
			}
			return this;
		}

		@Override
		protected void onSetWatermarkImage() {
			imageOperation.watermarkText = null;
		}

		/**
		 * 使用文字水印（与图片水印互斥）。
		 *
		 * <p>参数校验规则：如果 {@code watermarkText} 为空白，则不设置；设置后将图片水印设为 null。</p>
		 *
		 * @param watermarkText 水印文本
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkText(String watermarkText) {
			if (StringUtils.isNotBlank(watermarkText)) {
				imageOperation.watermarkText = watermarkText;
				imageOperation.watermarkImage = null;
			}
			return this;
		}

		/**
		 * 设置文字水印的透明度。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null，则不设置；取值范围 0-1。</p>
		 *
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextOpacity(Float opacity) {
			if (Objects.nonNull(opacity)) {
				imageOperation.watermarkTextOption.setOpacity(opacity);
			}
			return this;
		}

		/**
		 * 设置文字水印的字体。
		 *
		 * <p>参数校验规则：如果 {@code font} 为 null，则不设置。</p>
		 *
		 * @param font 字体
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextFont(Font font) {
			if (Objects.nonNull(font)) {
				imageOperation.watermarkTextOption.setFont(font);
			}
			return this;
		}

		/**
		 * 设置文字水印的填充颜色。
		 *
		 * <p>参数校验规则：如果 {@code fillColor} 为 null，则不设置。</p>
		 *
		 * @param fillColor 填充颜色
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextFillColor(Color fillColor) {
			if (Objects.nonNull(fillColor)) {
				imageOperation.watermarkTextOption.setFillColor(fillColor);
			}
			return this;
		}

		/**
		 * 设置文字水印的描边颜色。
		 *
		 * <p>参数校验规则：如果 {@code strokeColor} 为 null，则不设置。</p>
		 *
		 * @param strokeColor 描边颜色
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextStrokeColor(Color strokeColor) {
			if (Objects.nonNull(strokeColor)) {
				imageOperation.watermarkTextOption.setStrokeColor(strokeColor);
			}
			return this;
		}

		/**
		 * 设置文字水印的描边宽度。
		 *
		 * <p>参数校验规则：如果 {@code strokeWidth} 为 null 或 ≤ 0，则不设置。</p>
		 *
		 * @param strokeWidth 描边宽度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextStrokeWidth(Float strokeWidth) {
			if (Objects.nonNull(strokeWidth)) {
				imageOperation.watermarkTextOption.setStrokeWidth(strokeWidth);
			}
			return this;
		}

		/**
		 * 设置是否启用文字水印的描边效果。
		 *
		 * <p>参数校验规则：如果 {@code stroke} 为 null，则不设置。</p>
		 *
		 * @param stroke 是否描边
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder watermarkTextStroke(Boolean stroke) {
			if (Objects.nonNull(stroke)) {
				imageOperation.watermarkTextOption.setStroke(stroke);
			}
			return this;
		}

		/**
		 * 开启模糊并设置默认半径。
		 *
		 * <p>默认半径：{@code 1.5} 像素。</p>
		 *
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder blur() {
			imageOperation.blurRadius = 1.5f;
			return this;
		}

		/**
		 * 设置模糊半径。
		 *
		 * <p>参数校验规则：如果 {@code radius} 为 null 或 ≤ 1，则不设置。</p>
		 *
		 * @param radius 模糊半径
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder blur(Float radius) {
			if (Objects.nonNull(radius) && radius > 1) {
				imageOperation.blurRadius = radius;
			}
			return this;
		}

		/**
		 * 开启锐化并设置默认强度。
		 *
		 * <p>默认强度：{@code 0.3}。</p>
		 *
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder sharpen() {
			imageOperation.sharpenAmount = 0.3f;
			return this;
		}

		/**
		 * 设置锐化强度。
		 *
		 * <p>参数校验规则：如果 {@code amount} 为 null 或 = 0，则不设置。</p>
		 *
		 * @param amount 锐化强度（≠ 0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder sharpen(Float amount) {
			if (Objects.nonNull(amount) && amount != 0) {
				imageOperation.sharpenAmount = amount;
			}
			return this;
		}

		/**
		 * 开启对比度调整并设置默认幅度。
		 *
		 * <p>默认幅度：{@code 0.3}。</p>
		 *
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder contrast() {
			imageOperation.contrastAmount = 0.3f;
			return this;
		}

		/**
		 * 设置对比度调整幅度。
		 *
		 * <p>参数校验规则：如果 {@code amount} 为 null，则不设置；取值范围为 [-1, 1]，且不等于 0。</p>
		 *
		 * @param amount 对比度调整幅度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder contrast(Float amount) {
			if (Objects.nonNull(amount) && amount != 0 && amount <= 1.0 && amount >= -1.0) {
				imageOperation.contrastAmount = amount;
			}
			return this;
		}

		/**
		 * 设置亮度调整幅度。
		 *
		 * <p>参数校验规则：如果 {@code amount} 为 null，则不设置；取值范围为 [-2, 2]，且不等于 0。</p>
		 *
		 * @param amount 亮度调整幅度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder brightness(Float amount) {
			if (Objects.nonNull(amount) && amount != 0 && amount <= 2.0 && amount >= -2.0) {
				imageOperation.brightnessAmount = amount;
			}
			return this;
		}

		/**
		 * 添加单个图像滤镜。
		 *
		 * <p>参数校验规则：如果 {@code imageFilter} 为 null，则不设置。</p>
		 *
		 * @param imageFilter 图像滤镜
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder addFilter(ImageFilter imageFilter) {
			if (Objects.nonNull(imageFilter)) {
				imageOperation.filters.add(imageFilter);
			}
			return this;
		}

		/**
		 * 批量添加图像滤镜。
		 *
		 * <p>参数校验规则：如果集合为空或为 null，则不设置。</p>
		 *
		 * @param imageFilters 图像滤镜集合
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder addFilters(Collection<ImageFilter> imageFilters) {
			if (!CollectionUtils.isEmpty(imageFilters)) {
				imageOperation.filters.addAll(imageFilters);
			}
			return this;
		}

		/**
		 * 覆盖设置图像滤镜集合。
		 *
		 * <p>参数校验规则：如果集合为 null，则不设置。</p>
		 *
		 * @param imageFilters 图像滤镜集合
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BufferedImageOperationBuilder setFilters(Collection<ImageFilter> imageFilters) {
			if (Objects.nonNull(imageFilters)) {
				imageOperation.filters = imageFilters;
			}
			return this;
		}
	}
}
