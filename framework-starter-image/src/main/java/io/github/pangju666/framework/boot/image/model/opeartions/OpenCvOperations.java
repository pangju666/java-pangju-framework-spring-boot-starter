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

import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.opencv.io.resource.OpenCvResource;
import io.github.pangju666.commons.opencv.model.ImageWatermarkOption;
import io.github.pangju666.commons.opencv.model.TextWatermarkOption;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * OpenCV图像操作类。
 * <p>
 * 继承自{@link ImageOperations}，使用OpenCV库进行图像处理。
 * 提供丰富的图像处理功能，适用于需要高级图像处理功能的场景。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>继承自ImageOperations的所有功能：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
 *   <li>缩放插值配置：支持多种OpenCV插值算法</li>
 *   <li>滤镜效果：模糊、高斯模糊、中值模糊、锐化、浮雕、阈值化、自适应阈值化</li>
 *   <li>图像水印：支持图像水印添加和配置</li>
 *   <li>文字水印：支持文字水印添加和配置</li>
 *   <li>对比度和亮度调整：支持图像对比度和亮度的调整</li>
 *   <li>流式API：所有操作方法返回当前实例，支持链式调用</li>
 * </ul>
 *
 * <p><strong>滤镜效果说明</strong></p>
 * <ul>
 *   <li>模糊：使用均值模糊对图像进行平滑处理</li>
 *   <li>高斯模糊：使用高斯核对图像进行平滑处理，效果更自然</li>
 *   <li>中值模糊：使用中值滤波去除椒盐噪声</li>
 *   <li>锐化：增强图像边缘和细节</li>
 *   <li>浮雕：创建浮雕效果，使图像具有立体感</li>
 *   <li>阈值化：将图像转换为二值图像</li>
 *   <li>自适应阈值化：根据局部区域自适应计算阈值</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 创建OpenCV操作实例并设置缩放和滤镜
 * OpenCvOperations ops = new OpenCvOperations()
 *     .scale(800, 600)
 *     .gaussianBlur()
 *     .watermarkText("Copyright")
 *     .contrast(1.2f);
 *
 * // 从现有操作对象复制配置
 * OpenCvOperations newOps = new OpenCvOperations(ops);
 * }</pre>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>图像水印和文字水印互斥，设置其中一个会清除另一个</li>
 *   <li>滤镜效果可以组合使用，但顺序会影响最终效果</li>
 *   <li>阈值化和自适应阈值化互斥，设置其中一个会清除另一个</li>
 *   <li>中值模糊核大小必须为奇数且大于1</li>
 *   <li>自适应阈值的块大小必须为奇数且大于等于3</li>
 * </ul>
 *
 * @author pangju666
 * @see ImageOperations
 * @see ImageWatermarkOption
 * @see TextWatermarkOption
 * @since 2.1.0
 */
public class OpenCvOperations extends ImageOperations<OpenCvOperations> {
	/**
	 * 缩放插值标志。
	 * <p>
	 * 用于指定图像缩放时使用的插值算法。
	 * </p>
	 * <p>
	 * 默认值为{@link org.bytedeco.opencv.global.opencv_imgproc#INTER_LANCZOS4}，
	 * 该算法提供高质量的缩放效果，但计算开销较大。
	 * </p>
	 * <p>
	 * 常用的插值算法包括：
	 * <ul>
	 *   <li>INTER_NEAREST：最近邻插值，速度最快但质量最低</li>
	 *   <li>INTER_LINEAR：双线性插值，速度和质量平衡</li>
	 *   <li>INTER_CUBIC：双三次插值，质量较高</li>
	 *   <li>INTER_LANCZOS4：Lanczos插值，质量最高</li>
	 * </ul>
	 * </p>
	 *
	 * @see org.bytedeco.opencv.global.opencv_imgproc#INTER_NEAREST
	 * @see org.bytedeco.opencv.global.opencv_imgproc#INTER_LINEAR
	 * @see org.bytedeco.opencv.global.opencv_imgproc#INTER_CUBIC
	 * @see org.bytedeco.opencv.global.opencv_imgproc#INTER_LANCZOS4
	 * @since 2.1.0
	 */
	protected int resizeInterpolationFlag = opencv_imgproc.INTER_LANCZOS4;

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
	protected OpenCvResource watermarkImage;
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
	 * 模糊核大小。
	 * <p>
	 * 用于指定均值模糊的核大小。
	 * </p>
	 * <p>
	 * 核大小决定了模糊的程度，核越大，模糊效果越明显。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Size blurKernelSize;
	/**
	 * 高斯模糊核参数。
	 * <p>
	 * 用于指定高斯模糊的核大小和sigmaX参数。
	 * </p>
	 * <p>
	 * Pair的左值为核大小（Size），右值为sigmaX（Double）。
	 * </p>
	 * <p>
	 * 核大小决定了模糊的范围，sigmaX决定了高斯核在X方向的标准差，影响模糊的平滑程度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置高斯模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Pair<Size, Double> gaussianBlurKernel;
	/**
	 * 中值模糊核大小。
	 * <p>
	 * 用于指定中值模糊的核大小。
	 * </p>
	 * <p>
	 * 核大小必须为奇数且大于1，决定了中值滤波的窗口大小。
	 * 中值模糊对去除椒盐噪声特别有效。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中值模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer medianBlurKernelSize;
	/**
	 * 锐化权重。
	 * <p>
	 * 用于指定锐化效果的强度。
	 * </p>
	 * <p>
	 * 权重必须大于4，值越大，锐化效果越明显。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float sharpenWeight;
	/**
	 * 浮雕强度。
	 * <p>
	 * 用于指定浮雕效果的强度。
	 * </p>
	 * <p>
	 * 强度必须大于0，值越大，浮雕效果越明显。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置浮雕效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float embossStrength;
	/**
	 * 阈值参数。
	 * <p>
	 * 用于指定阈值化操作的参数，包括阈值、最大值和阈值类型。
	 * </p>
	 * <p>
	 * 阈值化将图像转换为二值图像，像素值大于阈值的设置为最大值，小于等于阈值的设置为0。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置阈值化效果。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>阈值化与自适应阈值化互斥</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ThresholdArgs thresholdArgs;
	/**
	 * 自适应阈值参数。
	 * <p>
	 * 用于指定自适应阈值化操作的参数，包括最大值、自适应方法、阈值类型、块大小和常数。
	 * </p>
	 * <p>
	 * 自适应阈值化根据图像的局部区域自适应计算阈值，适用于光照不均匀的图像。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置自适应阈值化效果。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>自适应阈值化与阈值化互斥</li>
	 *   <li>块大小必须为奇数且大于等于3</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected AdaptiveThresholdArgs adaptiveThresholdArgs;
	/**
	 * 对比度调整量。
	 * <p>
	 * 用于指定图像对比度的调整量。
	 * </p>
	 * <p>
	 * 调整量必须大于0，值大于1表示增强对比度，值小于1表示降低对比度。
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
	 * 默认构造函数。
	 * <p>
	 * 创建一个空的OpenCV图像操作实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 默认值：
	 * <ul>
	 *   <li>缩放插值标志：INTER_LANCZOS4</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印选项：新的ImageWatermarkOption实例</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印选项：新的TextWatermarkOption实例</li>
	 *   <li>所有滤镜参数：null</li>
	 *   <li>对比度调整量：null</li>
	 *   <li>亮度调整量：null</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 继承自ImageOperations的配置也会设置为默认值。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public OpenCvOperations() {
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
	 * 如果源操作对象不是OpenCV操作实例，则只复制继承自ImageOperations的配置。
	 * </p>
	 * <p>
	 * 复制的配置包括：
	 * <ul>
	 *   <li>继承自ImageOperations的配置：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
	 *   <li>缩放插值标志</li>
	 *   <li>图像水印资源和选项</li>
	 *   <li>文字水印文本和选项</li>
	 *   <li>所有滤镜参数</li>
	 *   <li>对比度和亮度调整量</li>
	 * </ul>
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @since 2.1.0
	 */
	public OpenCvOperations(@Nullable ImageOperations<?> operations) {
		super(operations);

		if (Objects.nonNull(operations) && operations instanceof OpenCvOperations openCvOperations) {
			/* 缩放相关配置 */
			this.resizeInterpolationFlag = openCvOperations.resizeInterpolationFlag;

			/* 图像水印相关配置 */
			this.watermarkImage = openCvOperations.watermarkImage;
			this.watermarkImageOption = openCvOperations.watermarkImageOption;

			/* 文字水印相关配置 */
			this.watermarkText = openCvOperations.watermarkText;
			this.watermarkTextOption = openCvOperations.watermarkTextOption;

			/* 滤镜相关配置 */
			this.blurKernelSize = openCvOperations.blurKernelSize;
			this.gaussianBlurKernel = openCvOperations.gaussianBlurKernel;
			this.medianBlurKernelSize = openCvOperations.medianBlurKernelSize;
			this.sharpenWeight = openCvOperations.sharpenWeight;
			this.embossStrength = openCvOperations.embossStrength;
			this.thresholdArgs = openCvOperations.thresholdArgs;
			this.adaptiveThresholdArgs = openCvOperations.adaptiveThresholdArgs;
			this.contrastAmount = openCvOperations.contrastAmount;
			this.brightnessAmount = openCvOperations.brightnessAmount;
		}
	}

	/**
	 * 获取缩放插值标志。
	 * <p>
	 * 返回图像缩放时使用的插值算法标志。
	 * </p>
	 * <p>
	 * 默认值为INTER_LANCZOS4。
	 * </p>
	 *
	 * @return 缩放插值标志
	 * @since 2.1.0
	 */
	public int getResizeInterpolationFlag() {
		return resizeInterpolationFlag;
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
	public @Nullable OpenCvResource getWatermarkImage() {
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
	 * 获取模糊核大小。
	 * <p>
	 * 返回均值模糊的核大小。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @return 模糊核大小，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Size getBlurKernelSize() {
		return blurKernelSize;
	}

	/**
	 * 获取高斯模糊核参数。
	 * <p>
	 * 返回高斯模糊的核大小和sigmaX参数。
	 * </p>
	 * <p>
	 * Pair的左值为核大小（Size），右值为sigmaX（Double）。
	 * 如果返回null，表示未设置高斯模糊效果。
	 * </p>
	 *
	 * @return 高斯模糊核参数（核大小和sigmaX），可能为null
	 * @since 2.1.0
	 */
	public @Nullable Pair<Size, Double> getGaussianBlurKernel() {
		return gaussianBlurKernel;
	}

	/**
	 * 获取中值模糊核大小。
	 * <p>
	 * 返回中值模糊的核大小。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中值模糊效果。
	 * </p>
	 *
	 * @return 中值模糊核大小，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getMedianBlurKernelSize() {
		return medianBlurKernelSize;
	}

	/**
	 * 获取锐化权重。
	 * <p>
	 * 返回锐化效果的强度权重。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @return 锐化权重，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getSharpenWeight() {
		return sharpenWeight;
	}

	/**
	 * 获取浮雕强度。
	 * <p>
	 * 返回浮雕效果的强度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置浮雕效果。
	 * </p>
	 *
	 * @return 浮雕强度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Float getEmbossStrength() {
		return embossStrength;
	}

	/**
	 * 获取阈值参数。
	 * <p>
	 * 返回阈值化操作的参数。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置阈值化效果。
	 * </p>
	 *
	 * @return 阈值参数，可能为null
	 * @since 2.1.0
	 */
	public @Nullable ThresholdArgs getThresholdArgs() {
		return thresholdArgs;
	}

	/**
	 * 获取自适应阈值参数。
	 * <p>
	 * 返回自适应阈值化操作的参数。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置自适应阈值化效果。
	 * </p>
	 *
	 * @return 自适应阈值参数，可能为null
	 * @since 2.1.0
	 */
	public @Nullable AdaptiveThresholdArgs getAdaptiveThresholdArgs() {
		return adaptiveThresholdArgs;
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
	 * 设置缩放插值标志。
	 * <p>
	 * 设置图像缩放时使用的插值算法。
	 * </p>
	 * <p>
	 * 常用的插值算法包括：
	 * <ul>
	 *   <li>INTER_NEAREST：最近邻插值，速度最快但质量最低</li>
	 *   <li>INTER_LINEAR：双线性插值，速度和质量平衡</li>
	 *   <li>INTER_CUBIC：双三次插值，质量较高</li>
	 *   <li>INTER_LANCZOS4：Lanczos插值，质量最高</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果插值标志为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的插值标志设置</li>
	 *   <li>插值算法的选择会影响缩放质量和性能</li>
	 * </ul>
	 * </p>
	 *
	 * @param interpolationFlag 缩放插值标志，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations resizeInterpolation(@Nullable Integer interpolationFlag) {
		if (Objects.nonNull(interpolationFlag)) {
			this.resizeInterpolationFlag = interpolationFlag;
		}
		return this;
	}

	/**
	 * 设置图像水印。
	 * <p>
	 * 设置作为水印的图像资源。
	 * </p>
	 * <p>
	 * 如果资源是OpenCvResource实例，直接使用；否则创建新的OpenCvResource。
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
	public OpenCvOperations watermarkImage(@Nullable IOResource resource) throws IOException {
		if (Objects.nonNull(resource)) {
			if (resource instanceof OpenCvResource openCvResource) {
				this.watermarkImage = openCvResource;
			} else {
				this.watermarkImage = new OpenCvResource(resource);
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
	public OpenCvOperations watermarkImageOption(@Nullable ImageWatermarkOption option) {
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
	public OpenCvOperations watermarkText(@Nullable String text) {
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
	public OpenCvOperations textWatermarkOption(@Nullable TextWatermarkOption option) {
		if (Objects.nonNull(option)) {
			this.watermarkTextOption = option;
		}
		return this;
	}

	/**
	 * 设置模糊效果（默认核大小）。
	 * <p>
	 * 使用默认核大小（3x3）应用均值模糊效果。
	 * </p>
	 * <p>
	 * 均值模糊使用均值核对图像进行平滑处理，可以减少噪声。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurKernelSize}为Size(3, 3)。
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
	public OpenCvOperations blur() {
		this.blurKernelSize = new Size(3, 3);
		return this;
	}

	/**
	 * 设置模糊效果。
	 * <p>
	 * 使用指定的核大小应用均值模糊效果。
	 * </p>
	 * <p>
	 * 均值模糊使用均值核对图像进行平滑处理，可以减少噪声。
	 * 核大小决定了模糊的程度，核越大，模糊效果越明显。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurKernelSize}。
	 * </p>
	 * <p>
	 * 如果核大小为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param ksize 模糊核大小，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations blur(@Nullable Size ksize) {
		if (Objects.nonNull(ksize)) {
			this.blurKernelSize = ksize;
		}
		return this;
	}

	/**
	 * 设置高斯模糊效果（默认参数）。
	 * <p>
	 * 使用默认参数（核大小3x3，sigmaX=0）应用高斯模糊效果。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比均值模糊更自然。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #gaussianBlur(Size, Double)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations gaussianBlur() {
		return gaussianBlur(new Size(3, 3), 0d);
	}

	/**
	 * 设置高斯模糊效果。
	 * <p>
	 * 使用指定的核大小应用高斯模糊效果，sigmaX默认为0。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比均值模糊更自然。
	 * 核大小决定了模糊的范围。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #gaussianBlur(Size, Double)}并传入sigmaX=0。
	 * </p>
	 * <p>
	 * 如果核大小为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param kernelSize 高斯核大小，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations gaussianBlur(@Nullable Size kernelSize) {
		return gaussianBlur(kernelSize, 0d);
	}

	/**
	 * 设置高斯模糊效果。
	 * <p>
	 * 使用指定的核大小和sigmaX参数应用高斯模糊效果。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比均值模糊更自然。
	 * 核大小决定了模糊的范围，sigmaX决定了高斯核在X方向的标准差，影响模糊的平滑程度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #gaussianBlurKernel}。
	 * </p>
	 * <p>
	 * 如果任一参数为null或sigmaX为负数，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 *   <li>sigmaX必须大于等于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param kernelSize 高斯核大小，可以为null
	 * @param sigmaX     高斯核在X方向的标准差，必须大于等于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations gaussianBlur(@Nullable Size kernelSize, @Nullable Double sigmaX) {
		if (ObjectUtils.allNotNull(kernelSize, sigmaX) && sigmaX >= 0) {
			this.gaussianBlurKernel = Pair.of(kernelSize, sigmaX);
		}

		return this;
	}

	/**
	 * 设置中值模糊效果（默认核大小）。
	 * <p>
	 * 使用默认核大小（5）应用中值模糊效果。
	 * </p>
	 * <p>
	 * 中值模糊使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #medianBlur(Integer)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的中值模糊设置</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations medianBlur() {
		return medianBlur(5);
	}

	/**
	 * 设置中值模糊效果。
	 * <p>
	 * 使用指定的核大小应用中值模糊效果。
	 * </p>
	 * <p>
	 * 中值模糊使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效。
	 * 核大小决定了中值滤波的窗口大小。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #medianBlurKernelSize}。
	 * </p>
	 * <p>
	 * 如果核大小为null、小于等于1或为偶数，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的中值模糊设置</li>
	 *   <li>核大小必须大于1且为奇数</li>
	 * </ul>
	 * </p>
	 *
	 * @param kernelSize 中值模糊核大小，必须大于1且为奇数，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations medianBlur(@Nullable Integer kernelSize) {
		if (Objects.nonNull(kernelSize) && kernelSize > 1 && kernelSize % 2 != 0) {
			this.medianBlurKernelSize = kernelSize;
		}

		return this;
	}

	/**
	 * 设置锐化效果（默认权重）。
	 * <p>
	 * 使用默认权重（5）应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #sharpen(Float)}并传入默认参数。
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
	public OpenCvOperations sharpen() {
		return sharpen(5f);
	}

	/**
	 * 设置锐化效果。
	 * <p>
	 * 使用指定的权重应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * 权重必须大于4，值越大，锐化效果越明显。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #sharpenWeight}。
	 * </p>
	 * <p>
	 * 如果权重为null或小于等于4，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 *   <li>权重必须大于4</li>
	 * </ul>
	 * </p>
	 *
	 * @param weight 锐化权重，必须大于4，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations sharpen(@Nullable Float weight) {
		if (Objects.nonNull(weight) && weight > 4) {
			this.sharpenWeight = weight;
		}

		return this;
	}

	/**
	 * 设置浮雕效果（默认强度）。
	 * <p>
	 * 使用默认强度（0.5）应用浮雕效果。
	 * </p>
	 * <p>
	 * 浮雕效果创建立体感，使图像具有浮雕的外观。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #emboss(Float)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的浮雕设置</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations emboss() {
		return emboss(0.5f);
	}

	/**
	 * 设置浮雕效果。
	 * <p>
	 * 使用指定的强度应用浮雕效果。
	 * </p>
	 * <p>
	 * 浮雕效果创建立体感，使图像具有浮雕的外观。
	 * 强度必须大于0，值越大，浮雕效果越明显。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #embossStrength}。
	 * </p>
	 * <p>
	 * 如果强度为null或小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的浮雕设置</li>
	 *   <li>强度必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param strength 浮雕强度，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations emboss(@Nullable Float strength) {
		if (Objects.nonNull(strength) && strength > 0) {
			this.embossStrength = strength;
		}

		return this;
	}

	/**
	 * 设置阈值化效果（默认参数）。
	 * <p>
	 * 使用默认参数应用阈值化效果，将图像转换为二值图像。
	 * </p>
	 * <p>
	 * 默认参数使用Otsu算法自动计算最佳阈值。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #threshold(Double, Double, Integer)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的阈值化设置</li>
	 *   <li>阈值化与自适应阈值化互斥</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations threshold() {
		return threshold(0d, 255d, opencv_imgproc.THRESH_BINARY + opencv_imgproc.THRESH_OTSU);
	}

	/**
	 * 设置阈值化效果。
	 * <p>
	 * 使用指定的参数应用阈值化效果，将图像转换为二值图像。
	 * </p>
	 * <p>
	 * 阈值化将图像转换为二值图像，像素值大于阈值的设置为最大值，小于等于阈值的设置为0。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #thresholdArgs}。
	 * </p>
	 * <p>
	 * 如果任一参数为null、超出范围或阈值等于最大值，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的阈值化设置</li>
	 *   <li>阈值化与自适应阈值化互斥</li>
	 *   <li>阈值和最大值必须在0-255范围内</li>
	 *   <li>阈值不能等于最大值</li>
	 * </ul>
	 * </p>
	 *
	 * @param thresh 阈值，范围0-255，可以为null
	 * @param maxVal 最大值，范围0-255，可以为null
	 * @param type   阈值类型，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations threshold(@Nullable Double thresh, @Nullable Double maxVal, @Nullable Integer type) {
		if (ObjectUtils.allNotNull(thresh, maxVal, type) && thresh >= 0 && thresh <= 255 && maxVal >= 0 &&
			maxVal <= 255 && !thresh.equals(maxVal)) {
			this.thresholdArgs = new ThresholdArgs(thresh, maxVal, type);
		}

		return this;
	}

	/**
	 * 设置自适应阈值化效果（默认参数）。
	 * <p>
	 * 使用默认参数应用自适应阈值化效果。
	 * </p>
	 * <p>
	 * 自适应阈值化根据图像的局部区域自适应计算阈值，适用于光照不均匀的图像。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #adaptiveThreshold(Double, Integer, Integer, Integer, Double)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的自适应阈值化设置</li>
	 *   <li>自适应阈值化与阈值化互斥</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations adaptiveThreshold() {
		return adaptiveThreshold(255d, opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
			opencv_imgproc.THRESH_BINARY, 11, 2d);
	}

	/**
	 * 设置自适应阈值化效果。
	 * <p>
	 * 使用指定的参数应用自适应阈值化效果。
	 * </p>
	 * <p>
	 * 自适应阈值化根据图像的局部区域自适应计算阈值，适用于光照不均匀的图像。
	 * 块大小决定了计算阈值的局部区域大小，常数用于调整阈值。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #adaptiveThresholdArgs}。
	 * </p>
	 * <p>
	 * 如果任一参数为null、超出范围或块大小不符合要求，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的自适应阈值化设置</li>
	 *   <li>自适应阈值化与阈值化互斥</li>
	 *   <li>最大值必须在0-255范围内</li>
	 *   <li>块大小必须大于等于3且为奇数</li>
	 * </ul>
	 * </p>
	 *
	 * @param maxValue       最大值，范围0-255，可以为null
	 * @param adaptiveMethod 自适应方法，可以为null
	 * @param thresholdType  阈值类型，可以为null
	 * @param blockSize      块大小，必须大于等于3且为奇数，可以为null
	 * @param c              常数，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations adaptiveThreshold(@Nullable Double maxValue, @Nullable Integer adaptiveMethod,
	                                          @Nullable Integer thresholdType, @Nullable Integer blockSize,
	                                          @Nullable Double c) {
		if (ObjectUtils.allNotNull(maxValue, adaptiveMethod, thresholdType, blockSize, c) &&
			maxValue >= 0 && maxValue <= 255 && blockSize >= 3 && blockSize % 2 == 1) {
			this.adaptiveThresholdArgs = new AdaptiveThresholdArgs(maxValue, adaptiveMethod, thresholdType, blockSize, c);
		}

		return this;
	}

	/**
	 * 设置对比度调整。
	 * <p>
	 * 设置图像对比度的调整量。
	 * </p>
	 * <p>
	 * 调整量必须大于0，值大于1表示增强对比度，值小于1表示降低对比度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #contrastAmount}。
	 * </p>
	 * <p>
	 * 如果调整量为null或小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的对比度调整设置</li>
	 *   <li>调整量必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param amount 对比度调整量，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations contrast(@Nullable Float amount) {
		if (Objects.nonNull(amount) && amount > 0) {
			this.brightnessAmount = amount;
		}

		return this;
	}

	/**
	 * 设置亮度调整。
	 * <p>
	 * 设置图像亮度的调整量。
	 * </p>
	 * <p>
	 * 正值表示增加亮度，负值表示降低亮度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #brightnessAmount}。
	 * </p>
	 * <p>
	 * 如果调整量为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的亮度调整设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param amount 亮度调整量，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations brightness(@Nullable Float amount) {
		if (Objects.nonNull(amount)) {
			this.brightnessAmount = amount;
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
	 *   <li>缩放插值标志：INTER_LANCZOS4</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印选项：新的ImageWatermarkOption实例</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印选项：新的TextWatermarkOption实例</li>
	 *   <li>所有滤镜参数：null</li>
	 *   <li>对比度调整量：null</li>
	 *   <li>亮度调整量：null</li>
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
		this.resizeInterpolationFlag = opencv_imgproc.INTER_LANCZOS4;

		/* 滤镜相关配置 */
		this.blurKernelSize = null;
		this.gaussianBlurKernel = null;
		this.medianBlurKernelSize = null;
		this.sharpenWeight = null;
		this.embossStrength = null;
		this.thresholdArgs = null;
		this.adaptiveThresholdArgs = null;
		this.contrastAmount = null;
		this.brightnessAmount = null;

		/* 文字水印相关配置 */
		this.watermarkText = null;
		this.watermarkTextOption = new TextWatermarkOption();

		/* 图像水印相关配置 */
		this.watermarkImage = null;
		this.watermarkImageOption = new ImageWatermarkOption();

	}

	/**
	 * 阈值参数记录。
	 * <p>
	 * 用于记录阈值化操作的参数。
	 * </p>
	 * <p>
	 * 包含阈值、最大值和阈值类型三个参数。
	 * </p>
	 * <p>
	 * <strong>参数说明</strong></p>
	 * <ul>
	 *   <li>thresh：阈值，用于将图像像素分为两类</li>
	 *   <li>maxVal：最大值，大于阈值的像素将被设置为此值</li>
	 *   <li>type：阈值类型，决定阈值化的方式</li>
	 * </ul>
	 * </p>
	 *
	 * @param thresh 阈值
	 * @param maxVal 最大值
	 * @param type   阈值类型
	 * @see opencv_imgproc#threshold(Mat, Mat, double, double, int)
	 * @since 2.1.0
	 */
	public record ThresholdArgs(double thresh, double maxVal, int type) {
	}

	/**
	 * 自适应阈值参数记录。
	 * <p>
	 * 用于记录自适应阈值化操作的参数。
	 * </p>
	 * <p>
	 * 包含最大值、自适应方法、阈值类型、块大小和常数五个参数。
	 * </p>
	 * <p>
	 * <strong>参数说明</strong></p>
	 * <ul>
	 *   <li>maxValue：最大值，大于阈值的像素将被设置为此值</li>
	 *   <li>adaptiveMethod：自适应方法，决定如何计算局部阈值</li>
	 *   <li>thresholdType：阈值类型，决定阈值化的方式</li>
	 *   <li>blockSize：块大小，用于计算局部阈值的窗口大小</li>
	 *   <li>c：常数，用于调整计算出的阈值</li>
	 * </ul>
	 * </p>
	 *
	 * @param maxValue       最大值
	 * @param adaptiveMethod 自适应方法
	 * @param thresholdType  阈值类型
	 * @param blockSize      块大小
	 * @param c              常数
	 * @see opencv_imgproc#adaptiveThreshold(Mat, Mat, double, int, int, int, double)
	 * @since 2.1.0
	 */
	public record AdaptiveThresholdArgs(double maxValue, int adaptiveMethod, int thresholdType, int blockSize,
	                                    double c) {
	}
}
