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
 * 继承自{@link ImageOperations}，使用Java内置的ImageIO进行图像处理。
 * 适用于简单的图像处理场景，不需要外部依赖。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>继承自ImageOperations的所有功能：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
 *   <li>重采样滤镜配置：支持多种重采样算法</li>
 *   <li>滤镜效果：模糊、锐化、对比度、亮度</li>
 *   <li>自定义滤镜支持：支持添加自定义ImageFilter</li>
 *   <li>图像水印：支持图像水印添加和配置</li>
 *   <li>文字水印：支持文字水印添加和配置</li>
 *   <li>流式API：所有操作方法返回当前实例，支持链式调用</li>
 * </ul>
 *
 * <p><strong>滤镜效果说明</strong></p>
 * <ul>
 *   <li>模糊：使用卷积核对图像进行平滑处理</li>
 *   <li>锐化：增强图像边缘和细节</li>
 *   <li>对比度：调整图像的对比度</li>
 *   <li>亮度：调整图像的亮度</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 创建ImageIO操作实例并设置缩放和滤镜
 * ImageIOOperations ops = new ImageIOOperations()
 *     .scale(800, 600)
 *     .blur()
 *     .watermarkText("Copyright")
 *     .contrast(0.3f);
 *
 * // 从现有操作对象复制配置
 * ImageIOOperations newOps = new ImageIOOperations(ops);
 * }</pre>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>图像水印和文字水印互斥，设置其中一个会清除另一个</li>
 *   <li>滤镜效果可以组合使用，但顺序会影响最终效果</li>
 *   <li>自定义滤镜会按照添加顺序依次应用</li>
 *   <li>对比度调整量范围为-1.0到1.0</li>
 *   <li>亮度调整量范围为-2.0到2.0</li>
 * </ul>
 *
 * @author pangju666
 * @see ImageOperations
 * @see java.awt.image.ImageFilter
 * @see ImageWatermarkOption
 * @see TextWatermarkOption
 * @since 2.1.0
 */
public class ImageIOOperations extends ImageOperations<ImageIOOperations> {
	/**
	 * 重采样滤镜。
	 * <p>
	 * 用于指定图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 默认值为{@link ResampleFilter#LANCZOS}，该算法提供高质量的缩放效果。
	 * </p>
	 * <p>
	 * 常用的重采样算法包括：
	 * <ul>
	 *   <li>NEAREST：最近邻插值，速度最快但质量最低</li>
	 *   <li>BILINEAR：双线性插值，速度和质量平衡</li>
	 *   <li>BICUBIC：双三次插值，质量较高</li>
	 *   <li>LANCZOS：Lanczos插值，质量最高</li>
	 * </ul>
	 * </p>
	 *
	 * @see ResampleFilter
	 * @since 2.1.0
	 */
	protected ResampleFilter resampleFilter = ResampleFilter.LANCZOS;

	/**
	 * 模糊半径。
	 * <p>
	 * 用于指定模糊效果的半径。
	 * </p>
	 * <p>
	 * 半径决定了模糊的程度，半径越大，模糊效果越明显。
	 * 半径必须大于1。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float blurRadius;
	/**
	 * 锐化程度。
	 * <p>
	 * 用于指定锐化效果的强度。
	 * </p>
	 * <p>
	 * 程度不能为0，值越大，锐化效果越明显。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float sharpenAmount;
	/**
	 * 对比度调整量。
	 * <p>
	 * 用于指定图像对比度的调整量。
	 * </p>
	 * <p>
	 * 调整量范围为-1.0到1.0，不能为0。
	 * 正值表示增强对比度，负值表示降低对比度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置对比度调整。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float contrastAmount;
	/**
	 * 亮度调整量。
	 * <p>
	 * 用于指定图像亮度的调整量。
	 * </p>
	 * <p>
	 * 调整量范围为-2.0到2.0，不能为0。
	 * 正值表示增加亮度，负值表示降低亮度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置亮度调整。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float brightnessAmount;
	/**
	 * 自定义滤镜列表。
	 * <p>
	 * 用于存储自定义的ImageFilter实例。
	 * </p>
	 * <p>
	 * 自定义滤镜会按照添加顺序依次应用到图像上。
	 * </p>
	 * <p>
	 * 默认值为空的ArrayList。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>自定义滤镜与内置滤镜可以组合使用</li>
	 *   <li>滤镜的应用顺序会影响最终效果</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected List<ImageFilter> filters = new ArrayList<>();

	/**
	 * 图像水印资源。
	 * <p>
	 * 用于指定作为水印的图像资源。
	 * </p>
	 * <p>
	 * 水印图像会按照{@link #watermarkImageOption}中指定的位置、透明度等参数叠加到目标图像上。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置图像水印。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>图像水印与文字水印互斥，设置图像水印会清除文字水印</li>
	 *   <li>水印图像应该使用支持透明度的格式（如PNG）</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ImageIOResource watermarkImage;
	/**
	 * 图像水印选项。
	 * <p>
	 * 用于配置图像水印的显示参数，包括位置、透明度、缩放等。
	 * </p>
	 * <p>
	 * 默认值为新的{@link ImageWatermarkOption}实例。
	 * </p>
	 * <p>
	 * 选项包括：
	 * <ul>
	 *   <li>水印位置：左上、右上、左下、右下、中心等</li>
	 *   <li>水印透明度：控制水印的透明程度</li>
	 *   <li>水印缩放：控制水印相对于目标图像的大小</li>
	 *   <li>水印边距：控制水印与图像边缘的距离</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ImageWatermarkOption watermarkImageOption = new ImageWatermarkOption();

	/**
	 * 文字水印文本。
	 * <p>
	 * 用于指定作为水印的文本内容。
	 * </p>
	 * <p>
	 * 文字水印会按照{@link #watermarkTextOption}中指定的字体、颜色、位置等参数渲染到目标图像上。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置文字水印。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>文字水印与图像水印互斥，设置文字水印会清除图像水印</li>
	 *   <li>空字符串或空白字符串会被忽略</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String watermarkText;
	/**
	 * 文字水印选项。
	 * <p>
	 * 用于配置文字水印的显示参数，包括字体、颜色、位置、大小等。
	 * </p>
	 * <p>
	 * 默认值为新的{@link TextWatermarkOption}实例。
	 * </p>
	 * <p>
	 * 选项包括：
	 * <ul>
	 *   <li>字体：指定水印文字的字体</li>
	 *   <li>字体大小：指定水印文字的大小</li>
	 *   <li>字体颜色：指定水印文字的颜色</li>
	 *   <li>水印位置：指定水印在图像中的位置</li>
	 *   <li>水印透明度：控制水印的透明程度</li>
	 *   <li>水印边距：控制水印与图像边缘的距离</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected TextWatermarkOption watermarkTextOption = new TextWatermarkOption();

	/**
	 * 默认构造函数。
	 * <p>
	 * 创建一个空的ImageIO图像操作实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 默认值：
	 * <ul>
	 *   <li>重采样滤镜：LANCZOS</li>
	 *   <li>模糊半径：null</li>
	 *   <li>锐化程度：null</li>
	 *   <li>对比度调整量：null</li>
	 *   <li>亮度调整量：null</li>
	 *   <li>自定义滤镜列表：空的ArrayList</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印选项：新的ImageWatermarkOption实例</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印选项：新的TextWatermarkOption实例</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 继承自ImageOperations的配置也会设置为默认值。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public ImageIOOperations() {
	}

	/**
	 * 拷贝构造函数。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 如果源操作对象不是ImageIO操作实例，则只复制继承自ImageOperations的配置。
	 * </p>
	 * <p>
	 * 复制的配置包括：
	 * <ul>
	 *   <li>继承自ImageOperations的配置：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
	 *   <li>重采样滤镜</li>
	 *   <li>图像水印资源和选项</li>
	 *   <li>文字水印文本和选项</li>
	 *   <li>所有滤镜参数</li>
	 *   <li>自定义滤镜列表</li>
	 * </ul>
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
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
	 * <p>
	 * 返回图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 默认值为LANCZOS。
	 * </p>
	 *
	 * @return 重采样滤镜
	 * @since 2.1.0
	 */
	public ResampleFilter getResampleFilter() {
		return resampleFilter;
	}

	/**
	 * 获取模糊半径。
	 * <p>
	 * 返回模糊效果的半径。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @return 模糊半径，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getBlurRadius() {
		return blurRadius;
	}

	/**
	 * 获取锐化程度。
	 * <p>
	 * 返回锐化效果的强度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @return 锐化程度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getSharpenAmount() {
		return sharpenAmount;
	}

	/**
	 * 获取对比度调整量。
	 * <p>
	 * 返回图像对比度的调整量。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置对比度调整。
	 * </p>
	 *
	 * @return 对比度调整量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getContrastAmount() {
		return contrastAmount;
	}

	/**
	 * 获取亮度调整量。
	 * <p>
	 * 返回图像亮度的调整量。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置亮度调整。
	 * </p>
	 *
	 * @return 亮度调整量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getBrightnessAmount() {
		return brightnessAmount;
	}

	/**
	 * 获取图像水印资源。
	 * <p>
	 * 返回作为水印的图像资源。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置图像水印。
	 * </p>
	 *
	 * @return 图像水印资源，可能为null
	 * @since 2.1.0
	 */
	public @Nullable ImageIOResource getWatermarkImage() {
		return watermarkImage;
	}

	/**
	 * 获取图像水印选项。
	 * <p>
	 * 返回图像水印的显示配置选项。
	 * </p>
	 * <p>
	 * 默认值为新的ImageWatermarkOption实例。
	 * </p>
	 *
	 * @return 图像水印选项
	 * @since 2.1.0
	 */
	public ImageWatermarkOption getWatermarkImageOption() {
		return watermarkImageOption;
	}

	/**
	 * 获取文字水印文本。
	 * <p>
	 * 返回作为水印的文本内容。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置文字水印。
	 * </p>
	 *
	 * @return 文字水印文本，可能为null
	 * @since 2.1.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 获取文字水印选项。
	 * <p>
	 * 返回文字水印的显示配置选项。
	 * </p>
	 * <p>
	 * 默认值为新的TextWatermarkOption实例。
	 * </p>
	 *
	 * @return 文字水印选项
	 * @since 2.1.0
	 */
	public TextWatermarkOption getWatermarkTextOption() {
		return watermarkTextOption;
	}

	/**
	 * 获取自定义滤镜列表。
	 * <p>
	 * 返回自定义滤镜的列表。
	 * </p>
	 * <p>
	 * 返回的列表为不可修改的集合视图。
	 * </p>
	 *
	 * @return 自定义滤镜列表
	 * @since 2.1.0
	 */
	public Collection<ImageFilter> getFilters() {
		return filters;
	}

	/**
	 * 设置重采样滤镜。
	 * <p>
	 * 设置图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 常用的重采样算法包括：
	 * <ul>
	 *   <li>NEAREST：最近邻插值，速度最快但质量最低</li>
	 *   <li>BILINEAR：双线性插值，速度和质量平衡</li>
	 *   <li>BICUBIC：双三次插值，质量较高</li>
	 *   <li>LANCZOS：Lanczos插值，质量最高</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果重采样滤镜为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的重采样滤镜设置</li>
	 *   <li>重采样算法的选择会影响缩放质量和性能</li>
	 * </ul>
	 * </p>
	 *
	 * @param resampleFilter 重采样滤镜，可以为null
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
	 * <p>
	 * 设置作为水印的图像资源。
	 * </p>
	 * <p>
	 * 如果资源是ImageIOResource实例，直接使用；否则创建新的ImageIOResource。
	 * </p>
	 * <p>
	 * 设置图像水印会清除文字水印。
	 * </p>
	 * <p>
	 * 如果资源为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>图像水印与文字水印互斥</li>
	 *   <li>水印图像应该使用支持透明度的格式（如PNG）</li>
	 *   <li>此方法会抛出IOException如果资源读取失败</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource 图像资源，可以为null
	 * @return 当前实例，支持链式调用
	 * @throws IOException IO异常，当资源读取失败时抛出
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
	 * <p>
	 * 设置图像水印的显示配置选项。
	 * </p>
	 * <p>
	 * 选项包括水印位置、透明度、缩放、边距等。
	 * </p>
	 * <p>
	 * 如果选项为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的图像水印选项设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param option 图像水印选项，可以为null
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
	 * <p>
	 * 设置作为水印的文本内容。
	 * </p>
	 * <p>
	 * 设置文字水印会清除图像水印。
	 * </p>
	 * <p>
	 * 如果文本为null或空白字符串，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>文字水印与图像水印互斥</li>
	 *   <li>空字符串或空白字符串会被忽略</li>
	 * </ul>
	 * </p>
	 *
	 * @param text 文字水印文本，可以为null
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
	 * <p>
	 * 设置文字水印的显示配置选项。
	 * </p>
	 * <p>
	 * 选项包括字体、颜色、位置、大小、透明度、边距等。
	 * </p>
	 * <p>
	 * 如果选项为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的文字水印选项设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param option 文字水印选项，可以为null
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
	 * <p>
	 * 使用默认半径（1.5）应用模糊效果。
	 * </p>
	 * <p>
	 * 模糊效果使用卷积核对图像进行平滑处理，可以减少噪声。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurRadius}为1.5。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 * </ul>
	 * </p>
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
	 * <p>
	 * 使用指定的半径应用模糊效果。
	 * </p>
	 * <p>
	 * 模糊效果使用卷积核对图像进行平滑处理，可以减少噪声。
	 * 半径决定了模糊的程度，半径越大，模糊效果越明显。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurRadius}。
	 * </p>
	 * <p>
	 * 如果半径为null或小于等于1，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 *   <li>半径必须大于1</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 模糊半径，必须大于1，可以为null
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
	 * <p>
	 * 使用默认程度（0.3）应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #sharpenAmount}为0.3。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 * </ul>
	 * </p>
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
	 * <p>
	 * 使用指定的程度应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * 程度不能为0，值越大，锐化效果越明显。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #sharpenAmount}。
	 * </p>
	 * <p>
	 * 如果程度为null或为0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 *   <li>程度不能为0</li>
	 * </ul>
	 * </p>
	 *
	 * @param amount 锐化程度，不能为0，可以为null
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
	 * <p>
	 * 使用默认值（0.3）调整图像对比度。
	 * </p>
	 * <p>
	 * 对比度调整会增强或降低图像的对比度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #contrastAmount}为0.3。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的对比度调整设置</li>
	 * </ul>
	 * </p>
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
	 * <p>
	 * 使用指定的调整量调整图像对比度。
	 * </p>
	 * <p>
	 * 调整量范围为-1.0到1.0，不能为0。
	 * 正值表示增强对比度，负值表示降低对比度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #contrastAmount}。
	 * </p>
	 * <p>
	 * 如果调整量为null、为0或超出范围，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的对比度调整设置</li>
	 *   <li>调整量范围为-1.0到1.0</li>
	 *   <li>调整量不能为0</li>
	 * </ul>
	 * </p>
	 *
	 * @param amount 对比度调整量，范围-1.0到1.0，不能为0，可以为null
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
	 * <p>
	 * 使用指定的调整量调整图像亮度。
	 * </p>
	 * <p>
	 * 调整量范围为-2.0到2.0，不能为0。
	 * 正值表示增加亮度，负值表示降低亮度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #brightnessAmount}。
	 * </p>
	 * <p>
	 * 如果调整量为null、为0或超出范围，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的亮度调整设置</li>
	 *   <li>调整量范围为-2.0到2.0</li>
	 *   <li>调整量不能为0</li>
	 * </ul>
	 * </p>
	 *
	 * @param amount 亮度调整量，范围-2.0到2.0，不能为0，可以为null
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
	 * <p>
	 * 向自定义滤镜列表中添加一个ImageFilter实例。
	 * </p>
	 * <p>
	 * 自定义滤镜会按照添加顺序依次应用到图像上。
	 * </p>
	 * <p>
	 * 如果滤镜为null，则不添加。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>自定义滤镜与内置滤镜可以组合使用</li>
	 *   <li>滤镜的应用顺序会影响最终效果</li>
	 *   <li>可以添加多个自定义滤镜</li>
	 * </ul>
	 * </p>
	 *
	 * @param imageFilter 图像滤镜，可以为null
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
	 * <p>
	 * 将所有配置恢复到默认值。
	 * </p>
	 * <p>
	 * 重置后的默认值：
	 * <ul>
	 *   <li>重采样滤镜：LANCZOS</li>
	 *   <li>模糊半径：null</li>
	 *   <li>锐化程度：null</li>
	 *   <li>对比度调整量：null</li>
	 *   <li>亮度调整量：null</li>
	 *   <li>自定义滤镜列表：空的ArrayList</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印选项：新的ImageWatermarkOption实例</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印选项：新的TextWatermarkOption实例</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 继承自ImageOperations的配置也会被重置。
	 * </p>
	 * <p>
	 * 此方法可以用于清除之前的所有操作配置，重新开始配置。
	 * </p>
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
