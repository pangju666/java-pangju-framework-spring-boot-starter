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
 * 继承自{@link ImageOperations}，使用OpenCV进行图像处理。
 * </p>
 * <p>
 * 扩展功能包括：
 * <ul>
 *     <li>缩放插值配置</li>
 *     <li>滤镜效果：模糊、高斯模糊、中值模糊、锐化、浮雕、阈值化、自适应阈值化</li>
 *     <li>图像水印</li>
 *     <li>文字水印</li>
 *     <li>对比度和亮度调整</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class OpenCvOperations extends ImageOperations<OpenCvOperations> {
	/**
	 * 缩放插值标志，默认为INTER_LANCZOS4
	 *
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @since 2.1.0
	 */
	protected int resizeInterpolationFlag = opencv_imgproc.INTER_LANCZOS4;

	/**
	 * 图像水印资源
	 *
	 * @since 2.1.0
	 */
	protected OpenCvResource watermarkImage;
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
	 * 模糊核大小
	 *
	 * @since 2.1.0
	 */
	protected Size blurKernelSize;
	/**
	 * 高斯模糊核参数（核大小和sigmaX）
	 *
	 * @since 2.1.0
	 */
	protected Pair<Size, Double> gaussianBlurKernel;
	/**
	 * 中值模糊核大小
	 *
	 * @since 2.1.0
	 */
	protected Integer medianBlurKernelSize;
	/**
	 * 锐化权重
	 *
	 * @since 2.1.0
	 */
	protected Float sharpenWeight;
	/**
	 * 浮雕强度
	 *
	 * @since 2.1.0
	 */
	protected Float embossStrength;
	/**
	 * 阈值参数
	 *
	 * @since 2.1.0
	 */
	protected ThresholdArgs thresholdArgs;
	/**
	 * 自适应阈值参数
	 *
	 * @since 2.1.0
	 */
	protected AdaptiveThresholdArgs adaptiveThresholdArgs;
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
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public OpenCvOperations() {
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param operations 源操作对象
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
	 *
	 * @return 缩放插值标志
	 * @since 2.1.0
	 */
	public int getResizeInterpolationFlag() {
		return resizeInterpolationFlag;
	}

	/**
	 * 获取图像水印资源。
	 *
	 * @return 图像水印资源
	 * @since 2.1.0
	 */
	public @Nullable OpenCvResource getWatermarkImage() {
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
	 * 获取模糊核大小。
	 *
	 * @return 模糊核大小
	 * @since 2.1.0
	 */
	public @Nullable Size getBlurKernelSize() {
		return blurKernelSize;
	}

	/**
	 * 获取高斯模糊核参数。
	 *
	 * @return 高斯模糊核参数（核大小和sigmaX）
	 * @since 2.1.0
	 */
	public @Nullable Pair<Size, Double> getGaussianBlurKernel() {
		return gaussianBlurKernel;
	}

	/**
	 * 获取中值模糊核大小。
	 *
	 * @return 中值模糊核大小
	 * @since 2.1.0
	 */
	public @Nullable Integer getMedianBlurKernelSize() {
		return medianBlurKernelSize;
	}

	/**
	 * 获取锐化权重。
	 *
	 * @return 锐化权重
	 * @since 2.1.0
	 */
	public @Nullable Float getSharpenWeight() {
		return sharpenWeight;
	}

	/**
	 * 获取浮雕强度。
	 *
	 * @return 浮雕强度
	 * @since 2.1.0
	 */
	public @Nullable Float getEmbossStrength() {
		return embossStrength;
	}

	/**
	 * 获取阈值参数。
	 *
	 * @return 阈值参数
	 * @since 2.1.0
	 */
	public @Nullable ThresholdArgs getThresholdArgs() {
		return thresholdArgs;
	}

	/**
	 * 获取自适应阈值参数。
	 *
	 * @return 自适应阈值参数
	 * @since 2.1.0
	 */
	public @Nullable AdaptiveThresholdArgs getAdaptiveThresholdArgs() {
		return adaptiveThresholdArgs;
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
	 * 设置缩放插值标志。
	 *
	 * @param interpolationFlag 缩放插值标志
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
	 *
	 * @param resource 图像资源
	 * @return 当前实例，支持链式调用
	 * @throws IOException IO异常
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
	 *
	 * @param option 图像水印选项
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
	 *
	 * @param text 文字水印文本
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
	 *
	 * @param option 文字水印选项
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
	 *
	 * @param ksize 模糊核大小
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
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations gaussianBlur() {
		return gaussianBlur(new Size(3, 3), 0d);
	}

	/**
	 * 设置高斯模糊效果。
	 *
	 * @param kernelSize 高斯核大小
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations gaussianBlur(@Nullable Size kernelSize) {
		return gaussianBlur(kernelSize, 0d);
	}

	/**
	 * 设置高斯模糊效果。
	 *
	 * @param kernelSize 高斯核大小
	 * @param sigmaX     高斯核在X方向的标准差
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
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations medianBlur() {
		return medianBlur(5);
	}

	/**
	 * 设置中值模糊效果。
	 *
	 * @param kernelSize 中值模糊核大小，必须大于1且为奇数
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
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations sharpen() {
		return sharpen(5f);
	}

	/**
	 * 设置锐化效果。
	 *
	 * @param weight 锐化权重，必须大于4
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
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations emboss() {
		return emboss(0.5f);
	}

	/**
	 * 设置浮雕效果。
	 *
	 * @param strength 浮雕强度，必须大于0
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
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public OpenCvOperations threshold() {
		return threshold(0d, 255d, opencv_imgproc.THRESH_BINARY + opencv_imgproc.THRESH_OTSU);
	}

	/**
	 * 设置阈值化效果。
	 *
	 * @param thresh 阈值，范围0-255
	 * @param maxVal 最大值，范围0-255
	 * @param type   阈值类型
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
	 *
	 * @param maxValue       最大值，范围0-255
	 * @param adaptiveMethod 自适应方法
	 * @param thresholdType  阈值类型
	 * @param blockSize      块大小，必须大于等于3且为奇数
	 * @param c              常数
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
	 *
	 * @param amount 对比度调整量，必须大于0
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
	 *
	 * @param amount 亮度调整量
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
