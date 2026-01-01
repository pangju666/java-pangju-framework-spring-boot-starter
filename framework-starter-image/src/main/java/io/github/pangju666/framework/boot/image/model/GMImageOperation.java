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

import io.github.pangju666.framework.boot.image.core.impl.GMImageTemplate;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 面向 {@link GMImageTemplate} 的图像操作配置，继承通用操作并扩展 GraphicsMagick 专属能力。
 * <p>该类基于 {@link ImageOperation}，结合 GraphicsMagick 命令行特性提供高性能图像处理功能。</p>
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
 * <h3>2. 扩展增强操作（{@link GMImageOperation} 独有）</h3>
 * <ul>
 *   <li><strong>输出控制</strong>：
 *     <ul>
 *       <li><strong>质量 (Quality)</strong>：支持设置 JPEG 等格式的压缩质量（1-100）。</li>
 *       <li><strong>DPI</strong>：支持设置输出图像的分辨率（每英寸点数）。</li>
 *       <li><strong>元数据 (Profile)</strong>：支持移除 EXIF、IPTC 等元数据信息以减小文件体积。</li>
 *     </ul>
 *   </li>
 *   <li><strong>缩放优化</strong>：支持指定 GraphicsMagick 缩放滤镜（如 Lanczos, Triangle 等）以控制缩放质量。</li>
 *   <li><strong>图像增强</strong>：
 *     <ul>
 *       <li><strong>模糊 (Blur)</strong>：高斯模糊（支持自定义半径与标准差）。</li>
 *       <li><strong>锐化 (Sharpen)</strong>：支持自定义半径与标准差的锐化处理。</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>3. GM 兼容性与限制</h3>
 * <ul>
 *   <li><strong>路径限制</strong>：水印图片路径建议使用纯英文，避免中文路径导致的兼容性问题。</li>
 *   <li><strong>中文字体</strong>：若需使用中文水印，必须确保系统安装了相应字体，并指定正确的英文字体名称（如 "simhei"）。</li>
 *   <li><strong>中文编码</strong>：建议优先使用英文水印文本，避免环境编码问题导致的乱码。</li>
 *   <li><strong>字体设置</strong>：绘制文字水印必须设置字体，否则将不生效。</li>
 *   <li><strong>文字样式</strong>：不支持设置文字样式（如粗体、斜体等），相关配置将被忽略。</li>
 * </ul>
 *
 * <p><strong>使用注意</strong>：通过构建器链式设置参数；不满足校验规则（如负数尺寸、无效坐标）的参数将被自动忽略。</p>
 *
 * @author pangju666
 * @see GMImageTemplate
 * @see ImageOperation
 * @since 1.0.0
 */
public class GMImageOperation extends ImageOperation {
	/**
	 * 输出质量（部分格式支持，如 JPEG）。
	 *
	 * <p>GraphicsMagick 默认为{@code 75}</p>
	 *
	 * @since 1.0.0
	 */
	protected Integer quality;
	/**
	 * 是否移除图像的 Profile/元数据（如 EXIF）。
	 *
	 * <p>默认值：{@code false}</p>
	 *
	 * @since 1.0.0
	 */
	protected boolean stripProfiles = false;
	/**
	 * GM 缩放滤镜类型，用于控制缩放质量与性能。
	 *
	 * @since 1.0.0
	 */
	protected String resizeFilter;
	/**
	 * 文字水印字体名称。
	 *
	 * @since 1.0.0
	 */
	protected String watermarkTextFontName;
	/**
	 * 高斯模糊参数对（半径, 标准差）。
	 *
	 * <p>语义：{@code left} 为半径（≥0），{@code right} 为标准差；当仅指定标准差时半径为 {@code 0}。</p>
	 *
	 * @since 1.0.0
	 */
	protected Pair<Double, Double> blurPair;
	/**
	 * 锐化参数对（半径, 标准差）。
	 *
	 * <p>语义：{@code left} 为半径（≥0），{@code right} 为标准差；仅指定标准差时半径为 {@code 0}。</p>
	 *
	 * @since 1.0.0
	 */
	protected Pair<Double, Double> sharpenPair;
	/**
	 * 输出图像的 DPI（每英寸点数）。
	 *
	 * <p>说明：仅部分格式支持；未设置为 {@code null}。</p>
	 *
	 * @since 1.0.0
	 */
	protected Integer dpi;

	protected GMImageOperation() {
	}

	/**
	 * 获取输出图像的 DPI（每英寸点数）。
	 *
	 * @return DPI 值，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getDpi() {
		return dpi;
	}

	/**
	 * 获取高斯模糊参数对（半径, 标准差）。
	 *
	 * <p>语义：{@code left} 为半径（≥0），{@code right} 为标准差；未设置为 {@code null}。</p>
	 *
	 * @return 参数对（radius, sigma），未设置为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Pair<Double, Double> getBlurPair() {
		return blurPair;
	}

	/**
	 * 获取锐化参数对（半径, 标准差）。
	 *
	 * <p>语义：{@code left} 为半径（≥0），{@code right} 为标准差；未设置为 {@code null}。</p>
	 *
	 * @return 参数对（radius, sigma），未设置为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Pair<Double, Double> getSharpenPair() {
		return sharpenPair;
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
	 * 获取输出质量。
	 *
	 * @return 质量值，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getQuality() {
		return quality;
	}

	/**
	 * 获取 GM 缩放滤镜类型。
	 *
	 * @return 滤镜类型，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable String getResizeFilter() {
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
		 * 启用移除图像的 Profile/元数据（如 EXIF/IPTC）。
		 *
		 * <p>语义：等价于调用 {@code stripProfiles(true)}。</p>
		 *
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder stripProfiles() {
			imageOperation.stripProfiles = true;
			return this;
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
		 * 设置文字水印字体英文引用名称，例如：simhei（黑体）。
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
		 * 设置 GM 缩放滤镜类型。
		 *
		 * <p>参数校验规则：如果 {@code resampleFilter} 为 null，则不设置。</p>
		 *
		 * <p>GraphicsMagick 默认为{@link ResampleFilter#LANCZOS}，一般情况下不需要设置这个。</p>
		 *
		 * @param resampleFilter 滤镜类型
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder resizeFilter(ResampleFilter resampleFilter) {
			if (Objects.nonNull(resampleFilter)) {
				imageOperation.resizeFilter = resampleFilter.getFilterName();
			}
			return this;
		}

		/**
		 * 设置输出质量（部分格式支持，如 JPEG）。
		 *
		 * <p>参数校验规则：如果 {@code quality} 为 null 或不在 [1,100]，则不设置。</p>
		 *
		 * <p>GraphicsMagick 默认为 75，一般情况下不需要设置这个。</p>
		 *
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code quality = 1~30}：低质量，文件极小，适用于缩略图或带宽受限场景，但可见压缩伪影（块状、模糊）；</li>
		 *   <li>{@code quality = 31~60}：中等质量，平衡文件大小与视觉效果，适合网页展示；</li>
		 *   <li>{@code quality = 61~85}：<b>推荐范围</b>，高质量且文件合理，人眼难以察觉失真；</li>
		 *   <li>{@code quality = 86~95}：接近无损，适用于对画质要求较高的场景（如电商主图）；</li>
		 *   <li>{@code quality = 96~100}：最高质量，文件显著增大，但提升有限（JPEG 本质是有损格式）；</li>
		 *   <li><b>注意</b>：PNG、GIF 等无损格式忽略此参数；实际效果还受图像内容影响（如照片 vs 线条图）。</li>
		 * </ul></p>
		 *
		 * @param quality 质量值（1-100）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder quality(Integer quality) {
			if (Objects.nonNull(quality) && quality > 0 && quality <= 100) {
				imageOperation.quality = quality;
			}
			return this;
		}

		/**
		 * 设置高斯模糊（仅指定标准差）。
		 *
		 * <p>参数校验规则：如果 {@code sigma} 为 null 或 &lt; 0，则不设置；
		 * 半径将使用 {@code 0} 以由 GM 自动推导。</p>
		 *
		 * <p><b>标准差取值建议</b>：
		 * <ul>
		 *   <li>{@code sigma ∈ (0, 0.5)}：极轻微模糊，几乎不可见；</li>
		 *   <li>{@code sigma ∈ [0.5, 1.5]}：自然柔化，适用于人像或降噪；</li>
		 *   <li>{@code sigma ∈ (1.5, 3.0]}：明显模糊，适合背景虚化；</li>
		 *   <li>{@code sigma > 3.0}：强烈模糊，可能产生“雾化”效果；</li>
		 *   <li>通常推荐范围：{@code 0.5 ~ 2.0}。</li>
		 * </ul></p>
		 *
		 * @param sigma 标准差（>=0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder blur(Double sigma) {
			if (Objects.nonNull(sigma) && sigma >= 0) {
				imageOperation.blurPair = Pair.of(0d, sigma);
			}
			return this;
		}

		/**
		 * 设置高斯模糊（指定半径与标准差）。
		 *
		 * <p>参数校验规则：如果任一参数为 null，或 {@code sigma} &lt; 0，或 {@code radius} &lt; 0，则不设置。</p>
		 *
		 * <p><b>半径取值建议</b>：
		 * <ul>
		 *   <li>一般情况下，建议使用单参数版本（radius=0），让 GM 自动计算核大小；</li>
		 *   <li>若需手动指定，{@code radius} 应 ≥ {@code sigma}，且通常为整数（如 2, 3）；</li>
		 *   <li>过大的 {@code radius} 会显著增加处理时间。</li>
		 * </ul></p>
		 *
		 * <p><b>标准差取值建议</b>：
		 * <ul>
		 *   <li>{@code sigma ∈ (0, 0.5)}：极轻微模糊，几乎不可见；</li>
		 *   <li>{@code sigma ∈ [0.5, 1.5]}：自然柔化，适用于人像或降噪；</li>
		 *   <li>{@code sigma ∈ (1.5, 3.0]}：明显模糊，适合背景虚化；</li>
		 *   <li>{@code sigma > 3.0}：强烈模糊，可能产生“雾化”效果；</li>
		 *   <li>通常推荐范围：{@code 0.5 ~ 2.0}。</li>
		 * </ul></p>
		 *
		 * @param radius 半径（≥0）
		 * @param sigma  标准差（>=0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder blur(Double radius, Double sigma) {
			if (ObjectUtils.allNotNull(radius, sigma) && sigma >= 0 && radius >= 0) {
				imageOperation.blurPair = Pair.of(radius, sigma);
			}
			return this;
		}

		/**
		 * 设置锐化（仅指定标准差）。
		 *
		 * <p>参数校验规则：如果 {@code sigma} 为 null 或 &lt; 0，则不设置；
		 * 半径将使用 {@code 0} 以由 GM 自动推导。</p>
		 *
		 * <p><b>标准差取值建议</b>：
		 * <ul>
		 *   <li>{@code sigma ∈ (0, 0.8]}：轻微锐化，安全无 artifacts；</li>
		 *   <li>{@code sigma ∈ (0.8, 1.5]}：中等锐化，提升清晰度，推荐默认值；</li>
		 *   <li>{@code sigma ∈ (1.5, 2.0]}：较强锐化，可能出现边缘光晕（halo）；</li>
		 *   <li>{@code sigma > 2.0}：过度锐化，放大噪点，图像失真；</li>
		 *   <li>通常推荐范围：{@code 0.6 ~ 1.2}。</li>
		 * </ul></p>
		 *
		 * @param sigma 标准差（>1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder sharpen(Double sigma) {
			if (Objects.nonNull(sigma) && sigma >= 0) {
				imageOperation.sharpenPair = Pair.of(0d, sigma);
			}
			return this;
		}

		/**
		 * 设置锐化（指定半径与标准差）。
		 *
		 * <p>参数校验规则：如果任一参数为 null，或 {@code sigma} &lt; 0，或 {@code radius} &lt; 0，则不设置。</p>
		 *
		 * <p><b>半径取值建议</b>：
		 * <ul>
		 *   <li>绝大多数场景应使用单参数版本（radius=0）；</li>
		 *   <li>手动指定 {@code radius} 仅在特殊需求下使用（如控制边缘响应宽度）；</li>
		 *   <li>{@code radius} 通常取 1~3；</li>
		 *   <li>过大的组合会导致严重伪影。</li>
		 * </ul></p>
		 *
		 * <p><b>标准差取值建议</b>：
		 * <ul>
		 *   <li>{@code sigma ∈ (0, 0.8]}：轻微锐化，安全无 artifacts；</li>
		 *   <li>{@code sigma ∈ (0.8, 1.5]}：中等锐化，提升清晰度，推荐默认值；</li>
		 *   <li>{@code sigma ∈ (1.5, 2.0]}：较强锐化，可能出现边缘光晕（halo）；</li>
		 *   <li>{@code sigma > 2.0}：过度锐化，放大噪点，图像失真；</li>
		 *   <li>通常推荐范围：{@code 0.6 ~ 1.2}。</li>
		 * </ul></p>
		 *
		 * @param radius 半径（≥0）
		 * @param sigma  标准差（>1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder sharpen(Double radius, Double sigma) {
			if (ObjectUtils.allNotNull(radius, sigma) && sigma >= 0 && radius >= 0) {
				imageOperation.sharpenPair = Pair.of(radius, sigma);
			}
			return this;
		}

		/**
		 * 设置输出图像的 DPI（每英寸点数）。
		 *
		 * <p>参数校验规则：如果 {@code dpi} 为 null 或 ≤ 0，则不设置。</p>
		 *
		 * <p>如果不是明确需要修改输出图像的DPI，请不要设置这个。</p>
		 *
		 * <p><b>取值建议</b>：
		 * <ul>
		 *   <li>{@code dpi = 72}：标准屏幕显示（Web 图像默认）；</li>
		 *   <li>{@code dpi = 150}：普通打印质量；</li>
		 *   <li>{@code dpi = 300}：高质量印刷（推荐用于照片/出版物）；</li>
		 *   <li>{@code dpi ∈ [72, 600]}：常见有效范围；</li>
		 *   <li>超过 600 通常无实际意义，且可能增大文件体积；</li>
		 *   <li>注意：DPI 不改变像素尺寸，仅影响物理打印尺寸。</li>
		 * </ul></p>
		 *
		 * @param dpi 每英寸点数（>0），典型值 72（屏幕）、150（普通打印）、300（高清印刷）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public GMImageOperationBuilder dpi(Integer dpi) {
			if (Objects.nonNull(dpi) && dpi > 0) {
				imageOperation.dpi = dpi;
			}
			return this;
		}
	}
}
