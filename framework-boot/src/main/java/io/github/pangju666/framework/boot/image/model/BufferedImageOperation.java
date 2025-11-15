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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Objects;

/**
 * 面向 {@link java.awt.image.BufferedImage} 的图像操作配置，扩展文字水印与缩放重采样能力。
 *
 * <p><strong>使用说明</strong>：通过构建器链式设置参数；不满足校验规则的参数将被忽略。</p>
 * <p><strong>互斥规则</strong>：图片水印与文字水印互斥；设置文字水印会清空图片水印，设置图片水印会清空文字水印。</p>
 * <p><strong>定位规则</strong>：沿用父类的方向/坐标互斥；坐标需为正数。</p>
 * <p><strong>缩放规则</strong>：支持重采样滤镜与 AWT 缩放提示映射；强制缩放与等比缩放的约束沿用父类。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class BufferedImageOperation extends ImageOperation {
	/**
	 * 缩放重采样滤镜类型，用于控制缩放质量与性能。
	 *
	 * <p>默认值：{@link ResampleFilter#LANCZOS}</p>
	 *
	 * @since 1.0.0
	 */
	protected ResampleFilter resampleFilterType = ResampleFilter.LANCZOS;
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
	 * 创建 {@link BuffedImageOperationBuilder} 构建器实例。
	 *
	 * @return 构建器实例
	 * @since 1.0.0
	 */
	public static BufferedImageOperation.BuffedImageOperationBuilder builder() {
		return new BufferedImageOperation.BuffedImageOperationBuilder();
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
	 * @return 重采样滤镜类型
	 * @since 1.0.0
	 */
	public ResampleFilter getResampleFilterType() {
		return resampleFilterType;
	}

	/**
	 * {@link BufferedImageOperation} 的构建器，提供缩放重采样与文字水印的链式配置。
	 *
	 * @since 1.0.0
	 */
	public static class BuffedImageOperationBuilder extends ImageOperationBuilder<BuffedImageOperationBuilder, BufferedImageOperation> {
		public BuffedImageOperationBuilder() {
			super(new BufferedImageOperation());
		}

		/**
		 * 设置缩放重采样滤镜类型。
		 *
		 * <p>参数校验规则：如果 {@code resampleFilter} 为 null，则不设置。</p>
		 *
		 * @param resampleFilter 重采样滤镜类型
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BuffedImageOperationBuilder scaleFilter(ResampleFilter resampleFilter) {
			if (Objects.nonNull(resampleFilter)) {
				imageOperation.resampleFilterType = resampleFilter;
			}
			return this;
		}

		/**
		 * 设置 AWT 缩放提示并映射为重采样滤镜类型。
		 *
		 * <p>参数校验规则：如果 {@code hints} 为 null，则不设置；
		 * 非预设值将不触发映射，保持现有滤镜不变。</p>
		 *
		 * @param hints AWT 缩放提示（如 {@link Image#SCALE_SMOOTH}）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public BuffedImageOperationBuilder scaleHints(Integer hints) {
			if (Objects.nonNull(hints)) {
				switch (hints) {
					case Image.SCALE_FAST:
					case Image.SCALE_REPLICATE:
						imageOperation.resampleFilterType = ResampleFilter.POINT;
						break;
					case Image.SCALE_AREA_AVERAGING:
						imageOperation.resampleFilterType = ResampleFilter.BOX;
						break;
					case Image.SCALE_SMOOTH:
						imageOperation.resampleFilterType = ResampleFilter.LANCZOS;
						break;
					default:
						break;
				}
			}
			return this;
		}

		/**
		 * 使用图片水印（与文字水印互斥）。
		 *
		 * <p>参数校验规则：如果 {@code watermarkImage} 为 null或文件不存在，则不设置；
		 * 设置后将文字水印设为 null。</p>
		 *
		 * @param watermarkImage 水印图片文件
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		@Override
		public BuffedImageOperationBuilder watermarkImage(File watermarkImage) {
			if (FileUtils.existFile(watermarkImage)) {
				imageOperation.watermarkImage = watermarkImage;
				imageOperation.watermarkText = null;
			}
			return this;
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
		public BuffedImageOperationBuilder watermarkText(String watermarkText) {
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
		public BuffedImageOperationBuilder watermarkTextOpacity(Float opacity) {
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
		public BuffedImageOperationBuilder watermarkTextFont(Font font) {
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
		public BuffedImageOperationBuilder watermarkTextFillColor(Color fillColor) {
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
		public BuffedImageOperationBuilder watermarkTextStrokeColor(Color strokeColor) {
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
		public BuffedImageOperationBuilder watermarkTextStrokeWidth(Float strokeWidth) {
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
		public BuffedImageOperationBuilder watermarkTextStroke(Boolean stroke) {
			if (Objects.nonNull(stroke)) {
				imageOperation.watermarkTextOption.setStroke(stroke);
			}
			return this;
		}
	}
}
