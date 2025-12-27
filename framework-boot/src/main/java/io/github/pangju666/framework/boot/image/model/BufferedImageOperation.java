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

import io.github.pangju666.framework.boot.image.core.impl.BufferedImageTemplate;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * 面向 {@link BufferedImageTemplate} 的图像操作配置，继承通用操作并扩展增强处理能力。
 * <p>该类基于 {@link ImageOperation}，结合 Java 2D API 提供更丰富的图像处理功能。</p>
 *
 * <h3>1. 基础图像操作（继承自 {@link ImageOperation}）</h3>
 * <ul>
 *   <li><strong>裁剪 (Crop)</strong>：支持中心裁剪、偏移裁剪（上下左右指定像素）与矩形区域裁剪。</li>
 *   <li><strong>缩放 (Scale)</strong>：支持按比例、按宽/高、强制尺寸缩放；支持设置水印图片尺寸限制策略。</li>
 *   <li><strong>水印 (Watermark)</strong>：
 *     <ul>
 *       <li>支持<strong>图片水印</strong>与<strong>文字水印</strong>（互斥）；</li>
 *       <li>支持<strong>九宫格方位</strong>与<strong>坐标定位</strong>（互斥）；</li>
 *       <li>支持设置水印透明度、相对缩放比例；</li>
 *       <li>支持自定义水印文字大小计算策略与水印图片尺寸限制策略。</li>
 *     </ul>
 *   </li>
 *   <li><strong>旋转与翻转 (Rotate &amp; Flip)</strong>：支持自定义角度旋转（顺/逆时针）与水平/垂直翻转。</li>
 *   <li><strong>灰度化 (Grayscale)</strong>：支持将图像转换为灰度模式。</li>
 * </ul>
 *
 * <h3>2. 扩展增强操作（{@link BufferedImageOperation} 独有）</h3>
 * <ul>
 *   <li><strong>缩放优化</strong>：支持配置 {@link ResampleFilter}（如 Bicubic, Lanczos）以控制缩放质量。</li>
 *   <li><strong>图像增强</strong>：
 *     <ul>
 *       <li><strong>模糊</strong>：高斯模糊（支持自定义半径）；</li>
 *       <li><strong>锐化</strong>：非锐化掩模（USM，支持自定义强度）；</li>
 *       <li><strong>对比度/亮度</strong>：支持线性调整对比度与亮度。</li>
 *     </ul>
 *   </li>
 *   <li><strong>滤镜管线</strong>：支持添加标准 {@link ImageFilter}，按添加顺序依次处理。</li>
 * </ul>
 *
 * <p><strong>使用注意</strong>：通过构建器链式设置参数；不满足校验规则（如负数尺寸、无效坐标）的参数将被自动忽略。</p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see BufferedImageTemplate
 * @see ImageOperation
 */
public class BufferedImageOperation extends ImageOperation {
	/**
	 * 缩放重采样滤镜类型，用于控制缩放质量与性能。
	 *
	 * @since 1.0.0
	 */
	protected Integer resampleFilterType;

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
		 * 设置缩放重采样滤镜类型。
		 *
		 * <p>参数校验规则：如果 {@code resampleFilter} 为 null，则不设置。</p>
		 *
		 * <p>底层实现中我已经默认设置为{@link ResampleFilter#LANCZOS}，一般情况下不需要设置这个。</p>
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

		/**
		 * 开启模糊并设置默认半径。
		 *
		 * <p>默认半径：{@code 1.5} 像素。</p>
		 *
		 * <p><b>效果说明</b>：使用高斯模糊核，半径越大越模糊；1.5 像素提供轻微柔化，适合降噪或 UI 图标处理。</p>
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
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code radius ∈ (1, 2]}：轻微模糊，适用于轻微柔化或抗锯齿；</li>
		 *   <li>{@code radius ∈ (2, 4]}：中等模糊，适合背景虚化或隐私遮挡；</li>
		 *   <li>{@code radius > 4}：强烈模糊，计算开销显著增加，且可能显得“涂抹”；</li>
		 *   <li>注意：实际模糊核大小 ≈ {@code 2 * radius + 1}，过大会导致性能下降；</li>
		 *   <li>推荐范围：{@code 1.5 ~ 3.0}。</li>
		 * </ul></p>
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
		 * <p><b>效果说明</b>：基于非锐化掩模（Unsharp Mask）原理，0.3 提供自然清晰度提升，无明显光晕。</p>
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
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code amount ∈ (0, 0.5]}：安全锐化，提升细节而不引入伪影；</li>
		 *   <li>{@code amount ∈ (0.5, 1.0]}：较强锐化，边缘可能出现轻微白边（halo）；</li>
		 *   <li>{@code amount > 1.0}：过度锐化，放大噪点，图像失真；</li>
		 *   <li>负值表示“反向锐化”（即额外模糊），但通常不推荐；</li>
		 *   <li>推荐范围：{@code 0.2 ~ 0.6}。</li>
		 * </ul></p>
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
		 * <p><b>效果说明</b>：0.3 表示对比度提升约 30%，使明暗更分明，适用于灰蒙图像。</p>
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
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code amount ∈ (0, 0.5]}：适度增强对比度，自然观感；</li>
		 *   <li>{@code amount ∈ (0.5, 1.0]}：高对比度，适合艺术效果或低动态范围图像；</li>
		 *   <li>{@code amount ∈ [-0.5, 0)}：降低对比度，营造“朦胧”或“褪色”风格；</li>
		 *   <li>{@code amount = -1}：完全去对比度（灰度均一化）；</li>
		 *   <li>避免极端值（如 ±1），可能导致细节丢失；</li>
		 *   <li>推荐范围：{@code -0.3 ~ 0.6}。</li>
		 * </ul></p>
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
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code amount ∈ (0, 1]}：提亮图像，1.0 表示亮度翻倍（可能过曝）；</li>
		 *   <li>{@code amount ∈ [-1, 0)}：降低亮度，-1.0 表示完全变黑；</li>
		 *   <li>{@code amount > 1}：极度提亮，高光区域严重溢出（纯白）；</li>
		 *   <li>{@code amount < -1}：极度压暗，阴影细节完全丢失；</li>
		 *   <li>日常调整建议：{@code -0.5 ~ 0.8}；</li>
		 *   <li>注意：亮度调整是线性加法（RGB += amount），非感知均匀。</li>
		 * </ul></p>
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
