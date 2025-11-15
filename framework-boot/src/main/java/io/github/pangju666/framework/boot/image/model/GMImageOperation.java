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

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Objects;

/**
 * 面向 GraphicsMagick 的图像操作配置，扩展质量、元数据处理与文字水印能力。
 *
 * <p><strong>使用说明</strong>：通过构建器链式设置参数；不满足校验规则的参数将被忽略。</p>
 * <p><strong>互斥规则</strong>：图片水印与文字水印互斥；设置文字水印会清空图片水印，设置图片水印会清空文字水印。</p>
 * <p><strong>定位规则</strong>：沿用父类的方向/坐标互斥；坐标需为正数。</p>
 * <p><strong>缩放规则</strong>：支持 GM 的重采样滤镜。</p>
 *
 * <p>注意事项：绘制文字水印必须设置字体，否则将不生效</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class GMImageOperation extends ImageOperation {
	/**
	 * 输出质量（部分格式支持，如 JPEG）。
	 *
	 * <p>默认值：{@code 75}</p>
	 *
	 * @since 1.0.0
	 */
	protected int quality = 75;
	/**
	 * 是否移除图像的 Profile/元数据（如 EXIF）。
	 *
	 * <p>默认值：{@code true}</p>
	 *
	 * @since 1.0.0
	 */
	protected boolean stripProfiles = true;
	/**
	 * GM 缩放滤镜类型，用于控制缩放质量与性能。
	 *
	 * @since 1.0.0
	 */
	protected ResampleFilter resizeFilter;
	/**
	 * 文字水印字体名称。
	 *
	 * @since 1.0.0
	 */
	protected String watermarkTextFontName;
	/**
	 * 文字水印字体大小。
	 *
	 * <p>默认值：{@code 12}</p>
	 *
	 * @since 1.0.0
	 */
	protected int watermarkTextFontSize = 12;
	/**
	 * 文字水印颜色。
	 *
	 * <p>默认值：{@link Color#WHITE}</p>
	 *
	 * @since 1.0.0
	 */
	protected Color watermarkTextColor = Color.WHITE;
	/**
	 * 文字水印透明度（0-1）。
	 *
	 * <p>默认值：{@code 0.4f}</p>
	 *
	 * @since 1.0.0
	 */
	protected float watermarkTextOpacity = 0.4f;
	/**
	 * 文字水印文本（与图片水印互斥）。
	 *
	 * @since 1.0.0
	 */
	protected String watermarkText;

	/**
	 * 创建 {@link GMImageOperationBuilder} 构建器实例。
	 *
	 * @return 构建器实例
	 * @since 1.0.0
	 */
	public static GMImageOperation.GMImageOperationBuilder builder() {
		return new GMImageOperation.GMImageOperationBuilder();
	}

	/**
	 * 获取文字水印颜色。
	 *
	 * @return 颜色
	 * @since 1.0.0
	 */
	public Color getWatermarkTextColor() {
		return watermarkTextColor;
	}

	/**
	 * 获取文字水印透明度。
	 *
	 * @return 透明度（0-1）
	 * @since 1.0.0
	 */
	public float getWatermarkTextOpacity() {
		return watermarkTextOpacity;
	}

	/**
	 * 获取文字水印文本。
	 *
	 * @return 文本，未设置或被图片水印清空时为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 是否移除图像的 Profile/元数据。
	 *
	 * @return {@code true} 表示移除
	 * @since 1.0.0
	 */
	public boolean isStripProfiles() {
		return stripProfiles;
	}

	/**
	 * 获取文字水印字体名称。
	 *
	 * @return 字体名称，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable String getWatermarkTextFontName() {
		return watermarkTextFontName;
	}

	/**
	 * 获取文字水印字体大小。
	 *
	 * @return 字体大小
	 * @since 1.0.0
	 */
	public int getWatermarkTextFontSize() {
		return watermarkTextFontSize;
	}

	/**
	 * 获取输出质量。
	 *
	 * @return 质量值
	 * @since 1.0.0
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * 获取 GM 缩放滤镜类型。
	 *
	 * @return 滤镜类型，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable ResampleFilter getResizeFilter() {
		return resizeFilter;
	}

	/**
	 * {@link GMImageOperation} 的构建器，提供质量、元数据以及文字水印的链式配置。
	 *
	 * @since 1.0.0
	 */
	public static class GMImageOperationBuilder extends ImageOperationBuilder<GMImageOperationBuilder, GMImageOperation> {
		public GMImageOperationBuilder() {
			super(new GMImageOperation());
		}

		/**
		 * 设置是否移除图像的 Profile/元数据。
		 *
		 * <p>参数校验规则：如果 {@code stripProfiles} 为 null，则不设置。</p>
		 *
		 * @param stripProfiles 是否移除元数据
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder stripProfiles(Boolean stripProfiles) {
			if (Objects.nonNull(stripProfiles)) {
				imageOperation.stripProfiles = stripProfiles;
			}
			return this;
		}

		/**
		 * 设置文字水印颜色。
		 *
		 * <p>参数校验规则：如果 {@code color} 为 null，则不设置。</p>
		 *
		 * @param color 颜色
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder watermarkTextColor(Color color) {
			if (Objects.nonNull(color)) {
				imageOperation.watermarkTextColor = color;
			}
			return this;
		}

		/**
		 * 设置文字水印透明度。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null或不在 [0,1] 范围，则不设置。</p>
		 *
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder watermarkTextOpacity(Float opacity) {
			if (Objects.nonNull(opacity) && opacity >= 0f && opacity <= 1) {
				imageOperation.watermarkTextOpacity = opacity;
			}
			return this;
		}

		/**
		 * 设置文字水印字体名称。
		 *
		 * <p>参数校验规则：如果 {@code fontName} 为空白，则不设置。</p>
		 *
		 * @param fontName 字体名称
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder watermarkTextFontName(String fontName) {
			if (StringUtils.isNotBlank(fontName)) {
				imageOperation.watermarkTextFontName = fontName;
			}
			return this;
		}

		/**
		 * 设置文字水印字体大小。
		 *
		 * <p>参数校验规则：如果 {@code fontSize} 为 null或 ≤ 0，则不设置。</p>
		 *
		 * @param fontSize 字体大小
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder watermarkTextFontSize(Integer fontSize) {
			if (Objects.nonNull(fontSize) && fontSize > 0) {
				imageOperation.watermarkTextFontSize = fontSize;
			}
			return this;
		}

		/**
		 * 使用文字水印（与图片水印互斥）。
		 *
		 * <p>参数校验规则：如果 {@code watermarkText} 为空白，则不设置；设置后清空图片水印。</p>
		 *
		 * @param watermarkText 文本内容
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder watermarkText(String watermarkText) {
			if (StringUtils.isNotBlank(watermarkText)) {
				imageOperation.watermarkText = watermarkText;
				imageOperation.watermarkImage = null;
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
		public GMImageOperationBuilder watermarkImage(File watermarkImage) {
			if (FileUtils.existFile(watermarkImage)) {
				imageOperation.watermarkImage = watermarkImage;
				imageOperation.watermarkText = null;
			}
			return this;
		}

		/**
		 * 设置 GM 缩放滤镜类型。
		 *
		 * <p>参数校验规则：如果 {@code resampleFilter} 为 null，则不设置。</p>
		 *
		 * @param resampleFilter 滤镜类型
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder resizeFilter(ResampleFilter resampleFilter) {
			if (Objects.nonNull(resampleFilter)) {
				imageOperation.resizeFilter = resampleFilter;
			}
			return this;
		}

		/**
		 * 按比例缩放（传入比例将转换为 GM 的百分比）。
		 *
		 * <p>参数校验规则：如果 {@code ratio} 为 null或 ≤ 0，则不设置；
		 * 设置后关闭强制缩放并清空宽高（继承父类行为）。</p>
		 *
		 * @param ratio 缩放比例（>0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		@Override
		public GMImageOperationBuilder scale(Float ratio) {
			if (Objects.nonNull(ratio) && ratio > 0) {
				super.scale(ratio);
				super.imageOperation.scaleRatio *= 100;
			}
			return this;
		}

		/**
		 * 设置输出质量（部分格式支持，如 JPEG）。
		 *
		 * <p>参数校验规则：如果 {@code quality} 为 null或 ≤ 0，则不设置；建议范围 1-100。</p>
		 *
		 * @param quality 质量值（建议 1-100）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder quality(Integer quality) {
			if (Objects.nonNull(quality) && quality > 0) {
				imageOperation.quality = quality;
			}
			return this;
		}
	}
}
