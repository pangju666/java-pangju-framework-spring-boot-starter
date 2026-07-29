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

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.*;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gm4java.im4java.GMOperation;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * GraphicsMagick图像操作类。
 * <p>
 * 继承自{@link ImageOperations}，使用GraphicsMagick进行图像处理。
 * </p>
 * <p>
 * 扩展功能包括：
 * <ul>
 *     <li>重采样滤镜配置</li>
 *     <li>滤镜效果：模糊、高斯模糊、中值模糊、锐化、反锐化掩模、浮雕、阈值化</li>
 *     <li>色彩调整：亮度、饱和度、色相</li>
 *     <li>图像水印：支持方向定位、尺寸限制、透明度等配置</li>
 *     <li>文字水印：支持字体、颜色、描边、透明度等配置</li>
 *     <li>输出配置：质量、DPI、压缩类型、去除元数据等</li>
 * </ul>
 * </p>
 * <p>
 * 提供将配置转换为GraphicsMagick命令的方法：{@link #toConvertGMOperation}、{@link #toCompositeGMOperation}
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class GraphicsMagickOperations extends ImageOperations<GraphicsMagickOperations> {
	/**
	 * 绘制文本参数格式
	 *
	 * @since 2.1.0
	 */
	protected static final String DRAW_TEXT_ARG_FORMAT = "\"text %d %d '%s'\"";
	/**
	 * 颜色格式
	 *
	 * @since 2.1.0
	 */
	protected static final String COLOR_FORMAT = "rgba(%d,%d,%d,%.1f)";

	/**
	 * 重采样滤镜
	 *
	 * @since 2.1.0
	 */
	protected ResampleFilter resizeFilter;

	/**
	 * 亮度，默认为100
	 *
	 * @since 2.1.0
	 */
	protected int brightness = 100;
	/**
	 * 饱和度，默认为100
	 *
	 * @since 2.1.0
	 */
	protected int saturation = 100;
	/**
	 * 色相，默认为100
	 *
	 * @since 2.1.0
	 */
	protected int hue = 100;
	/**
	 * 模糊核参数（半径和sigma）
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> blurKernel;
	/**
	 * 高斯模糊核参数（半径和sigma）
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> gaussianKernel;
	/**
	 * 中值模糊半径
	 *
	 * @since 2.1.0
	 */
	protected Double medianRadius;
	/**
	 * 锐化核参数（半径和sigma）
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> sharpenKernel;
	/**
	 * 反锐化掩模核参数
	 *
	 * @since 2.1.0
	 */
	protected UnsharpKernel unsharpKernel;
	/**
	 * 浮雕半径
	 *
	 * @since 2.1.0
	 */
	protected Double embossRadius;
	/**
	 * 阈值百分比
	 *
	 * @since 2.1.0
	 */
	protected Double thresholdPercent;

	/**
	 * 水印方向，默认为右上角
	 *
	 * @since 2.1.0
	 */
	protected Direction watermarkDirection = Direction.TOP_RIGHT;
	/**
	 * 水印X坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer watermarkX;
	/**
	 * 水印Y坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer watermarkY;

	/**
	 * 文字水印文本
	 *
	 * @since 2.1.0
	 */
	protected String watermarkText;
	/**
	 * 文字水印字体
	 *
	 * @since 2.1.0
	 */
	protected String watermarkTextFont;
	/**
	 * 文字水印透明度，默认为0.4
	 *
	 * @since 2.1.0
	 */
	protected float watermarkTextOpacity = 0.4f;
	/**
	 * 文字水印填充颜色，默认为白色
	 *
	 * @since 2.1.0
	 */
	protected Color watermarkTextFillColor = Color.WHITE;
	/**
	 * 文字水印描边颜色，默认为黑色
	 *
	 * @since 2.1.0
	 */
	protected Color watermarkTextStrokeColor = Color.BLACK;
	/**
	 * 文字水印描边宽度，默认为1
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextStrokeWidth = 1;
	/**
	 * 是否启用文字水印描边，默认为true
	 *
	 * @since 2.1.0
	 */
	protected boolean watermarkTextStroke = true;
	/**
	 * 文字水印字体大小，默认为24
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextFontSize = 24;
	/**
	 * 文字水印边距，默认为10
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextMargin = 10;

	/**
	 * 图像水印资源
	 *
	 * @since 2.1.0
	 */
	protected IOResource watermarkImage;
	/**
	 *  图像水印相对缩放因子，默认为0.15
	 *
	 * @since 2.1.0
	 */
	protected double watermarkImageRelativeScaleFactor = 0.15;
	/**
	 * 图像水印透明度，默认为0.4
	 *
	 * @since 2.1.0
	 */
	protected float watermarkImageOpacity = 0.4f;
	/**
	 * 图像水印尺寸
	 *
	 * @since 2.1.0
	 */
	protected ImageSize watermarkImageSize;
	/**
	 * 图像水印边距，默认为20
	 *
	 * @since 2.1.0
	 */
	protected int watermarkImageMargin = 20;
	/**
	 * 图像水印尺寸限制策略
	 *
	 * @since 2.1.0
	 */
	protected Function<ImageSize, Pair<ImageSize, ImageSize>> watermarkImageSizeLimitStrategy = imageSize -> {
		int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
		if (shorter < 600) { // 小图
			return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
		} else { // 中等图
			return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
		}
	};

	/**
	 * 是否去除元数据
	 *
	 * @since 2.1.0
	 */
	protected boolean stripProfiles;
	/**
	 * 输出DPI
	 *
	 * @since 2.1.0
	 */
	protected Integer dpi;
	/**
	 * 输出质量
	 *
	 * @since 2.1.0
	 */
	protected Integer quality;
	/**
	 * 压缩类型
	 *
	 * @since 2.1.0
	 */
	protected ImageCompressionType compression;

	/**
	 * 是否触发convert命令
	 *
	 * @since 2.1.0
	 */
	protected boolean triggerConvert;

	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations() {
		super();
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param operations 源操作对象
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations(@Nullable ImageOperations<?> operations) {
		super(operations);

		if (Objects.nonNull(operations) && operations instanceof GraphicsMagickOperations graphicsMagickOperations) {
			/* 缩放相关配置 */
			this.resizeFilter = graphicsMagickOperations.resizeFilter;

			/* 滤镜相关配置 */
			this.blurKernel = graphicsMagickOperations.blurKernel;
			this.gaussianKernel = graphicsMagickOperations.gaussianKernel;
			this.medianRadius = graphicsMagickOperations.medianRadius;
			this.sharpenKernel = graphicsMagickOperations.sharpenKernel;
			this.unsharpKernel = graphicsMagickOperations.unsharpKernel;
			this.embossRadius = graphicsMagickOperations.embossRadius;
			this.thresholdPercent = graphicsMagickOperations.thresholdPercent;
			this.brightness = graphicsMagickOperations.brightness;
			this.saturation = graphicsMagickOperations.saturation;
			this.hue = graphicsMagickOperations.hue;

			/* 水印坐标相关配置 */
			this.watermarkDirection = graphicsMagickOperations.watermarkDirection;
			this.watermarkX = graphicsMagickOperations.watermarkX;
			this.watermarkY = graphicsMagickOperations.watermarkY;

			/* 文字水印相关配置 */
			this.watermarkText = graphicsMagickOperations.watermarkText;
			this.watermarkTextOpacity = graphicsMagickOperations.watermarkTextOpacity;
			this.watermarkTextFillColor = graphicsMagickOperations.watermarkTextFillColor;
			this.watermarkTextFont = graphicsMagickOperations.watermarkTextFont;
			this.watermarkTextStrokeColor = graphicsMagickOperations.watermarkTextStrokeColor;
			this.watermarkTextStrokeWidth = graphicsMagickOperations.watermarkTextStrokeWidth;
			this.watermarkTextStroke = graphicsMagickOperations.watermarkTextStroke;
			this.watermarkTextFontSize = graphicsMagickOperations.watermarkTextFontSize;
			this.watermarkTextMargin = graphicsMagickOperations.watermarkTextMargin;

			/* 图像水印相关配置 */
			this.watermarkImage = graphicsMagickOperations.watermarkImage;
			this.watermarkImageRelativeScaleFactor = graphicsMagickOperations.watermarkImageRelativeScaleFactor;
			this.watermarkImageOpacity = graphicsMagickOperations.watermarkImageOpacity;
			this.watermarkImageSizeLimitStrategy = graphicsMagickOperations.watermarkImageSizeLimitStrategy;
			this.watermarkImageSize = graphicsMagickOperations.watermarkImageSize;
			this.watermarkImageMargin = graphicsMagickOperations.watermarkImageMargin;

			/* 输出配置 */
			this.quality = graphicsMagickOperations.quality;
			this.stripProfiles = graphicsMagickOperations.stripProfiles;
			this.dpi = graphicsMagickOperations.dpi;
			this.compression = graphicsMagickOperations.compression;

			this.triggerConvert = graphicsMagickOperations.triggerConvert;
		}
	}

	/**
	 * 获取重采样滤镜。
	 *
	 * @return 重采样滤镜
	 * @since 2.1.0
	 */
	public @Nullable ResampleFilter getResizeFilter() {
		return resizeFilter;
	}

	/**
	 * 获取高斯模糊核参数。
	 *
	 * @return 高斯模糊核参数（半径和sigma）
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getGaussianKernel() {
		return gaussianKernel;
	}

	/**
	 * 获取模糊核参数。
	 *
	 * @return 模糊核参数（半径和sigma）
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getBlurKernel() {
		return blurKernel;
	}

	/**
	 * 获取中值模糊半径。
	 *
	 * @return 中值模糊半径
	 * @since 2.1.0
	 */
	public @Nullable Double getMedianRadius() {
		return medianRadius;
	}

	/**
	 * 获取锐化核参数。
	 *
	 * @return 锐化核参数（半径和sigma）
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getSharpenKernel() {
		return sharpenKernel;
	}

	/**
	 * 获取反锐化掩模核参数。
	 *
	 * @return 反锐化掩模核参数
	 * @since 2.1.0
	 */
	public @Nullable UnsharpKernel getUnsharpKernel() {
		return unsharpKernel;
	}

	/**
	 * 获取浮雕半径。
	 *
	 * @return 浮雕半径
	 * @since 2.1.0
	 */
	public @Nullable Double getEmbossRadius() {
		return embossRadius;
	}

	/**
	 * 获取阈值百分比。
	 *
	 * @return 阈值百分比
	 * @since 2.1.0
	 */
	public @Nullable Double getThresholdPercent() {
		return thresholdPercent;
	}

	/**
	 * 是否启用文字水印描边。
	 *
	 * @return 如果启用返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isWatermarkTextStroke() {
		return watermarkTextStroke;
	}

	/**
	 * 获取水印方向。
	 *
	 * @return 水印方向
	 * @since 2.1.0
	 */
	public @Nullable Direction getWatermarkDirection() {
		return watermarkDirection;
	}

	/**
	 * 获取水印X坐标。
	 *
	 * @return 水印X坐标
	 * @since 2.1.0
	 */
	public @Nullable Integer getWatermarkX() {
		return watermarkX;
	}

	/**
	 * 获取水印Y坐标。
	 *
	 * @return 水印Y坐标
	 * @since 2.1.0
	 */
	public @Nullable Integer getWatermarkY() {
		return watermarkY;
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
	 * 获取文字水印字体。
	 *
	 * @return 文字水印字体
	 * @since 2.1.0
	 */
	public @Nullable String getWatermarkTextFont() {
		return watermarkTextFont;
	}

	/**
	 * 获取文字水印透明度。
	 *
	 * @return 文字水印透明度
	 * @since 2.1.0
	 */
	public float getWatermarkTextOpacity() {
		return watermarkTextOpacity;
	}

	/**
	 * 获取文字水印填充颜色。
	 *
	 * @return 文字水印填充颜色
	 * @since 2.1.0
	 */
	public Color getWatermarkTextFillColor() {
		return watermarkTextFillColor;
	}

	/**
	 * 获取文字水印描边颜色。
	 *
	 * @return 文字水印描边颜色
	 * @since 2.1.0
	 */
	public Color getWatermarkTextStrokeColor() {
		return watermarkTextStrokeColor;
	}

	/**
	 * 获取文字水印描边宽度。
	 *
	 * @return 文字水印描边宽度
	 * @since 2.1.0
	 */
	public int getWatermarkTextStrokeWidth() {
		return watermarkTextStrokeWidth;
	}

	/**
	 * 获取文字水印字体大小。
	 *
	 * @return 文字水印字体大小
	 * @since 2.1.0
	 */
	public int getWatermarkTextFontSize() {
		return watermarkTextFontSize;
	}

	/**
	 * 获取文字水印边距。
	 *
	 * @return 文字水印边距
	 * @since 2.1.0
	 */
	public int getWatermarkTextMargin() {
		return watermarkTextMargin;
	}

	/**
	 * 获取图像水印资源。
	 *
	 * @return 图像水印资源
	 * @since 2.1.0
	 */
	public @Nullable IOResource getWatermarkImage() {
		return watermarkImage;
	}

	/**
	 * 获取图像水印相对缩放因子。
	 *
	 * @return 图像水印相对缩放因子
	 * @since 2.1.0
	 */
	public double getWatermarkImageRelativeScaleFactor() {
		return watermarkImageRelativeScaleFactor;
	}

	/**
	 * 获取图像水印透明度。
	 *
	 * @return 图像水印透明度
	 * @since 2.1.0
	 */
	public float getWatermarkImageOpacity() {
		return watermarkImageOpacity;
	}

	/**
	 * 获取图像水印尺寸。
	 *
	 * @return 图像水印尺寸
	 * @since 2.1.0
	 */
	public @Nullable ImageSize getWatermarkImageSize() {
		return watermarkImageSize;
	}

	/**
	 * 获取图像水印边距。
	 *
	 * @return 图像水印边距
	 * @since 2.1.0
	 */
	public int getWatermarkImageMargin() {
		return watermarkImageMargin;
	}

	/**
	 * 获取图像水印尺寸限制策略。
	 *
	 * @return 图像水印尺寸限制策略
	 * @since 2.1.0
	 */
	public Function<ImageSize, Pair<ImageSize, ImageSize>> getWatermarkImageSizeLimitStrategy() {
		return watermarkImageSizeLimitStrategy;
	}

	/**
	 * 获取输出质量。
	 *
	 * @return 输出质量
	 * @since 2.1.0
	 */
	public @Nullable Integer getQuality() {
		return quality;
	}

	/**
	 * 是否去除元数据。
	 *
	 * @return 如果去除返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isStripProfiles() {
		return stripProfiles;
	}

	/**
	 * 获取输出DPI。
	 *
	 * @return 输出DPI
	 * @since 2.1.0
	 */
	public @Nullable Integer getDpi() {
		return dpi;
	}

	/**
	 * 获取压缩类型。
	 *
	 * @return 压缩类型
	 * @since 2.1.0
	 */
	public @Nullable ImageCompressionType getCompression() {
		return compression;
	}

	/**
	 * 获取亮度。
	 *
	 * @return 亮度
	 * @since 2.1.0
	 */
	public int getBrightness() {
		return brightness;
	}

	/**
	 * 获取饱和度。
	 *
	 * @return 饱和度
	 * @since 2.1.0
	 */
	public int getSaturation() {
		return saturation;
	}

	/**
	 * 获取色相。
	 *
	 * @return 色相
	 * @since 2.1.0
	 */
	public int getHue() {
		return hue;
	}

	/* 缩放相关配置 */

	/**
	 * 设置重采样滤镜。
	 *
	 * @param resizeFilter 重采样滤镜
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations resizeFilter(@Nullable ResampleFilter resizeFilter) {
		if (Objects.nonNull(resizeFilter)) {
			this.resizeFilter = resizeFilter;
		}
		return this;
	}

	/* 滤镜相关配置 */

	/**
	 * 设置模糊效果（默认参数）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur() {
		return blur(0d, 3d);
	}

	/**
	 * 设置模糊效果（仅sigma）。
	 *
	 * @param sigma 模糊sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur(@Nullable Double sigma) {
		return blur(0d, sigma);
	}

	/**
	 * 设置模糊效果。
	 *
	 * @param radius 模糊半径
	 * @param sigma 模糊sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.blurKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置高斯模糊效果（默认参数）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian() {
		return gaussian(0d, 5d);
	}

	/**
	 * 设置高斯模糊效果（仅sigma）。
	 *
	 * @param sigma 高斯模糊sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian(@Nullable Double sigma) {
		return gaussian(0d, sigma);
	}

	/**
	 * 设置高斯模糊效果。
	 *
	 * @param radius 高斯模糊半径
	 * @param sigma 高斯模糊sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.gaussianKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置中值模糊效果（默认半径）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations median() {
		return median(3d);
	}

	/**
	 * 设置中值模糊效果。
	 *
	 * @param radius 中值模糊半径
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations median(@Nullable Double radius) {
		if (Objects.nonNull(radius) && radius >= 1) {
			this.medianRadius = radius;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置锐化效果（默认参数）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen() {
		return sharpen(0d, 1d);
	}

	/**
	 * 设置锐化效果（仅sigma）。
	 *
	 * @param sigma 锐化sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen(@Nullable Double sigma) {
		return sharpen(0d, sigma);
	}

	/**
	 * 设置锐化效果。
	 *
	 * @param radius 锐化半径
	 * @param sigma 锐化sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.sharpenKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置反锐化掩模效果（默认参数）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp() {
		return unsharp(0d, 1d, 1d, 0.05);
	}

	/**
	 * 设置反锐化掩模效果（仅sigma）。
	 *
	 * @param sigma sigma值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp(@Nullable Double sigma) {
		return unsharp(0d, sigma, 1d, 0.05);
	}

	/**
	 * 设置反锐化掩模效果。
	 *
	 * @param radius 半径
	 * @param sigma sigma值
	 * @param amount 强度
	 * @param threshold 阈值
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp(@Nullable Double radius, @Nullable Double sigma, @Nullable Double amount,
	                                        @Nullable Double threshold) {
		if (ObjectUtils.allNotNull(radius, sigma, amount, threshold) && sigma > 0 && radius >= 0 &&
			amount > 0 && threshold > 0) {
			this.unsharpKernel = new UnsharpKernel(radius, sigma, amount, threshold);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置浮雕效果（默认半径）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations emboss() {
		return emboss(0d);
	}

	/**
	 * 设置浮雕效果。
	 *
	 * @param radius 浮雕半径
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations emboss(@Nullable Double radius) {
		if (Objects.nonNull(radius) && radius >= 0) {
			this.embossRadius = radius;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置阈值化效果（默认百分比）。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations threshold() {
		return threshold(0.5);
	}

	/**
	 * 设置阈值化效果。
	 *
	 * @param percent 阈值百分比，范围0-100
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations threshold(@Nullable Double percent) {
		if (Objects.nonNull(percent) && percent >= 0 && percent <= 1) {
			this.thresholdPercent = percent;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置亮度。
	 *
	 * @param brightness 亮度值，默认为100
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations brightness(@Nullable Integer brightness) {
		if (Objects.nonNull(brightness)) {
			this.brightness = brightness;
			this.triggerConvert = brightness != 100;
		}
		return this;
	}

	/**
	 * 设置饱和度。
	 *
	 * @param saturation 饱和度值，默认为100
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations saturation(@Nullable Integer saturation) {
		if (Objects.nonNull(saturation)) {
			this.saturation = saturation;
			this.triggerConvert = saturation != 100;
		}
		return this;
	}

	/**
	 * 设置色相。
	 *
	 * @param hue 色相值，默认为100
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations hue(@Nullable Integer hue) {
		if (Objects.nonNull(hue)) {
			this.hue = hue;
			this.triggerConvert = hue != 100;
		}
		return this;
	}

	/* 水印坐标相关配置 */

	/**
	 * 设置水印方向。
	 *
	 * @param direction 水印方向
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkDirection(@Nullable Direction direction) {
		if (Objects.nonNull(direction)) {
			this.watermarkX = null;
			this.watermarkY = null;
			this.watermarkDirection = direction;
		}
		return this;
	}

	/**
	 * 设置水印位置坐标。
	 *
	 * @param x 水印X坐标
	 * @param y 水印Y坐标
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkPosition(@Nullable Integer x, Integer y) {
		if (ObjectUtils.allNotNull(x, y) && x >= 0 && y >= 0) {
			this.watermarkX = x;
			this.watermarkY = y;
			this.watermarkDirection = null;
		}
		return this;
	}

	/* 图像水印相关配置 */

	/**
	 * 设置图像水印。
	 *
	 * @param watermarkImage 图像水印资源
	 * @param watermarkImageSize 图像水印尺寸
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImage(@Nullable IOResource watermarkImage, @Nullable ImageSize watermarkImageSize) {
		if (ObjectUtils.allNotNull(watermarkImage, watermarkImageSize)) {
			if (watermarkImage.isImage()) {
				this.watermarkImage = watermarkImage;
				this.watermarkImageSize = watermarkImageSize;
				this.watermarkText = null;
				this.triggerConvert = true;
			}
		}
		return this;
	}

	/**
	 * 设置图像水印（GraphicsMagick资源）。
	 *
	 * @param resource GraphicsMagick资源
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImage(@Nullable GraphicsMagickResource resource) {
		if (Objects.nonNull(resource)) {
			this.watermarkImage = resource;
			this.watermarkImageSize = resource.getImageSize();
			this.watermarkText = null;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置图像水印边距。
	 *
	 * @param margin 边距
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageMargin(@Nullable Integer margin) {
		if (Objects.nonNull(margin) && margin >= 0) {
			this.watermarkImageMargin = margin;
		}
		return this;
	}

	/**
	 * 设置图像水印尺寸限制策略。
	 *
	 * @param imageSizeLimitStrategy 尺寸限制策略
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageSizeLimitStrategy(
		@Nullable Function<ImageSize, Pair<ImageSize, ImageSize>> imageSizeLimitStrategy) {
		if (Objects.nonNull(imageSizeLimitStrategy)) {
			this.watermarkImageSizeLimitStrategy = imageSizeLimitStrategy;
		}
		return this;
	}

	/**
	 * 设置图像水印相对缩放因子。
	 *
	 * @param relativeScaleFactor 相对缩放因子，必须大于0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageRelativeScaleFactor(@Nullable Double relativeScaleFactor) {
		if (Objects.nonNull(relativeScaleFactor) && relativeScaleFactor > 0) {
			this.watermarkImageRelativeScaleFactor = relativeScaleFactor;
		}
		return this;
	}

	/**
	 * 设置图像水印透明度。
	 *
	 * @param opacity 透明度，范围0-1
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageOpacity(@Nullable Float opacity) {
		if (Objects.nonNull(opacity) && opacity >= 0 && opacity <= 1) {
			this.watermarkImageOpacity = opacity;
		}
		return this;
	}

	/**
	 * 设置文字水印文本和字体文件。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param text 文字水印文本
	 * @param fontFile 字体文件（TTF格式）
	 * @return 当前实例，支持链式调用
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkText(@Nullable String text, @Nullable File fontFile) throws IOException {
		if (StringUtils.isNotBlank(text) && FileUtils.isMimeType(fontFile, ImageConstants.TTF_FONT_MIME_TYPE)) {
			this.watermarkText = text;
			this.watermarkTextFont = fontFile.getAbsolutePath();
			this.watermarkImage = null;
			this.watermarkImageSize = null;
		}
		return this;
	}

	/**
	 * 设置文字水印文本和字体名称。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param text 文字水印文本
	 * @param fontName 字体名称
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkText(@Nullable String text, @Nullable String fontName) {
		if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(fontName)) {
			this.watermarkText = text;
			this.watermarkTextFont = fontName;
			this.watermarkImage = null;
			this.watermarkImageSize = null;
		}
		return this;
	}

	/**
	 * 设置文字水印描边。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param stroke 是否启用描边
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStroke(@Nullable Boolean stroke) {
		if (Objects.nonNull(stroke)) {
			this.watermarkTextStroke = stroke;
		}
		return this;
	}

	/**
	 * 设置文字水印字体大小。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param fontSize 字体大小
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextFontSize(@Nullable Integer fontSize) {
		if (Objects.nonNull(fontSize)) {
			this.watermarkTextFontSize = fontSize;
		}
		return this;
	}

	/**
	 * 设置文字水印描边颜色。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param strokeColor 描边颜色
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStrokeColor(@Nullable Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.watermarkTextStrokeColor = strokeColor;
		}
		return this;
	}

	/**
	 * 设置文字水印描边宽度。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param strokeWidth 描边宽度，必须大于0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStrokeWidth(@Nullable Integer strokeWidth) {
		if (Objects.nonNull(strokeWidth) && strokeWidth > 0) {
			this.watermarkTextStrokeWidth = strokeWidth;
		}
		return this;
	}

	/**
	 * 启用文字水印描边。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStroke() {
		this.watermarkTextStroke = true;
		return this;
	}

	/**
	 * 设置文字水印透明度。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param opacity 透明度，范围0-1
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextOpacity(@Nullable Float opacity) {
		if (Objects.nonNull(opacity) && opacity >= 0 && opacity <= 1) {
			this.watermarkTextOpacity = opacity;
		}
		return this;
	}

	/**
	 * 设置文字水印填充颜色。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param color 填充颜色
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextFillColor(@Nullable Color color) {
		if (Objects.nonNull(color)) {
			this.watermarkTextFillColor = color;
		}
		return this;
	}

	/**
	 * 设置文字水印边距。
	 *
	 * <p>尽量使用图像水印</p>
	 *
	 * @param margin 边距
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextMargin(@Nullable Integer margin) {
		if (Objects.nonNull(margin) && margin >= 0) {
			this.watermarkTextMargin = margin;
		}
		return this;
	}

	/**
	 * 去除元数据。
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations stripProfiles() {
		this.stripProfiles = true;
		return this;
	}

	/**
	 * 设置是否去除元数据。
	 *
	 * @param stripProfiles 是否去除元数据
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations stripProfiles(@Nullable Boolean stripProfiles) {
		if (Objects.nonNull(stripProfiles)) {
			this.stripProfiles = stripProfiles;
		}
		return this;
	}

	/**
	 * 设置输出DPI。
	 *
	 * @param dpi DPI值，必须大于0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations dpi(@Nullable Integer dpi) {
		if (Objects.nonNull(dpi) && dpi > 0) {
			this.dpi = dpi;
		}
		return this;
	}

	/**
	 * 设置输出质量。
	 *
	 * @param quality 质量值，范围1-100
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations quality(@Nullable Integer quality) {
		if (Objects.nonNull(quality) && quality > 0 && quality <= 100) {
			this.quality = quality;
		}
		return this;
	}

	/**
	 * 设置压缩类型。
	 *
	 * @param compression 压缩类型
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations compression(@Nullable ImageCompressionType compression) {
		if (Objects.nonNull(compression)) {
			this.compression = compression;
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
		this.resizeFilter = null;

		/* 滤镜相关配置 */
		this.brightness = 100;
		this.saturation = 100;
		this.hue = 100;
		this.blurKernel = null;
		this.gaussianKernel = null;
		this.medianRadius = null;
		this.sharpenKernel = null;
		this.unsharpKernel = null;
		this.embossRadius = null;
		this.thresholdPercent = null;

		/* 水印坐标相关配置 */
		this.watermarkDirection = Direction.TOP_RIGHT;
		this.watermarkX = null;
		this.watermarkY = null;

		/* 文字水印相关配置 */
		this.watermarkText = null;
		this.watermarkTextFont = null;
		this.watermarkTextOpacity = 0.4f;
		this.watermarkTextFillColor = Color.WHITE;
		this.watermarkTextStrokeColor = Color.BLACK;
		this.watermarkTextStrokeWidth = 1;
		this.watermarkTextStroke = true;
		this.watermarkTextFontSize = 24;
		this.watermarkTextMargin = 10;

		/* 图像水印相关配置 */
		this.watermarkImage = null;
		this.watermarkImageMargin = 10;
		this.watermarkImageRelativeScaleFactor = 0.15;
		this.watermarkImageOpacity = 0.4f;
		this.watermarkImageSize = null;
		this.watermarkImageSizeLimitStrategy = imageSize -> {
			int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
			if (shorter < 600) { // 小图
				return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
			} else if (shorter >= 1920) { // 大图（注意：>=1920）
				return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
			} else { // 中等图
				return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
			}
		};

		/* 输出配置 */
		this.stripProfiles = false;
		this.dpi = null;
		this.quality = null;
		this.compression = null;

		this.triggerConvert = false;

    }

	/**
	 * 判断是否需要执行convert命令。
	 *
	 * @return 如果需要执行convert命令返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isConvertRequired() {
		return ObjectUtils.anyNotNull(targetWidth, targetHeight, scalingFactor, rotateAngle, flipDirection,
			cropType, globalOpacity) || this.triggerConvert || this.grayscale;
	}

	/* 转换为 GMOperation */

	/**
	 * 转换为GraphicsMagick的convert命令操作。
	 *
	 * @param resource 图像资源
	 * @param outputFile 输出文件
	 * @return GMOperation对象
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	public GMOperation toConvertGMOperation(GraphicsMagickResource resource, File outputFile) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");

		return toConvertGMOperation(resource, outputFile, false);
	}

	/**
	 * 转换为GraphicsMagick的convert命令操作。
	 *
	 * @param resource 图像资源
	 * @param outputFile 输出文件
	 * @param isIntermediate 是否为中间文件
	 * @return GMOperation对象
	 * @throws IOException IO异常
	 * @since 2.1.0
	 */
	public GMOperation toConvertGMOperation(GraphicsMagickResource resource, File outputFile, boolean isIntermediate) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("convert");
		gmOperation.addImage(resource.getFile());

		// 设置变换参数
		setTransformArgs(resource.getImageSize().getVisualSize(), gmOperation);

		// 设置滤镜参数
		setFilterArgs(gmOperation);

		// 设置文字水印参数
		setTextWatermarkArgs(gmOperation);

		// 设置输出质量（一般不需要设置，且只有特定格式生效，如果是输出中间文件则不设置）
		if (Objects.nonNull(quality) && !isIntermediate) {
			gmOperation.quality(quality);
		}

		// 设置压缩格式（一般不需要设置，且只有特定格式生效，如果是输出中间文件则不设置）
		if (Objects.nonNull(compression) && !isIntermediate) {
			gmOperation.compress(compression.graphicsMagickCompressionType);
		}

		// 设置输出参数
		setOutputArgs(outputFile, gmOperation);

		// 创建父目录
		FileUtils.forceMkdirParent(outputFile);

		return gmOperation;
	}

	/**
	 * 转换为GraphicsMagick的composite命令操作。
	 *
	 * @param resource 图像资源
	 * @param outputFile 输出文件
	 * @return GMOperation对象
	 * @throws IOException IO异常
	 * @throws ImageOperationException 图像操作异常
	 * @since 2.1.0
	 */
	public GMOperation toCompositeGMOperation(GraphicsMagickResource resource, File outputFile) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		if (Objects.isNull(watermarkImageSize)) {
			throw new ImageOperationException("未配置水印图片尺寸");
		}
		if (Objects.isNull(watermarkImage)) {
			throw new ImageOperationException("未配置水印图片");
		}

		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("composite");

		// 设置水印图片不透明度
		gmOperation.addRawArg("-dissolve " + (int) (watermarkImageOpacity * 100));

		ImageSize originalWatermarkSize = watermarkImageSize;

		Pair<ImageSize, ImageSize> watermarkImageSizeRange = watermarkImageSizeLimitStrategy.apply(resource.getImageSize());
		ImageSize targetWatermarkImageSize = resource.getImageSize().scale(watermarkImageRelativeScaleFactor);

		if (originalWatermarkSize.getWidth() > originalWatermarkSize.getHeight()) {
			int targetWidth = Math.min(watermarkImageSizeRange.getRight().getWidth(),
				Math.max(watermarkImageSizeRange.getLeft().getWidth(), targetWatermarkImageSize.getWidth()));
			if (targetWidth != targetWatermarkImageSize.getWidth()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByWidth(targetWidth);
			}
		} else {
			int targetHeight = Math.min(watermarkImageSizeRange.getRight().getHeight(),
				Math.max(watermarkImageSizeRange.getLeft().getHeight(), targetWatermarkImageSize.getHeight()));
			if (targetHeight != targetWatermarkImageSize.getHeight()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByHeight(targetHeight);
			}
		}

		if (Objects.nonNull(watermarkDirection)) {
			setWatermarkDirectionArg(gmOperation);
			gmOperation.addRawArg("-geometry " + targetWatermarkImageSize.getWidth() + "x" +
				targetWatermarkImageSize.getHeight() + "+" + watermarkImageMargin + "+" + watermarkImageMargin);
			gmOperation.addImage(watermarkImage.getFile());
		} else if (ObjectUtils.allNotNull(watermarkX, watermarkY)) {
			gmOperation.addImage(watermarkImage.getFile());
			// 设置左上角为原点
			gmOperation.gravity(GMOperation.Gravity.NorthWest);

			int x = Math.max(0, Math.min(resource.getImageSize().getWidth() - targetWatermarkImageSize.getWidth(),
				watermarkX + watermarkImageMargin));
			int y = Math.max(0, Math.min(resource.getImageSize().getHeight() - targetWatermarkImageSize.getHeight(),
				watermarkY + watermarkImageMargin));
			gmOperation.addRawArg("-geometry " + targetWatermarkImageSize.getWidth() + "x" +
				targetWatermarkImageSize.getHeight() + "+" + x + "+" + y);
		}

		//gmOperation.addImage(operations.getWatermarkImage().getFile());

		gmOperation.addImage(resource.getFile());

		// 设置输出质量（一般不需要设置，且只有特定格式生效）
		if (Objects.nonNull(quality)) {
			gmOperation.quality(quality);
		}

		// 设置压缩格式（一般不需要设置，且只有特定格式生效）
		if (Objects.nonNull(compression)) {
			gmOperation.compress(compression.graphicsMagickCompressionType);
		}

		// 设置输出参数
		setOutputArgs(outputFile, gmOperation);

		// 创建父目录
		FileUtils.forceMkdirParent(outputFile);

		return gmOperation;
	}

	/**
	 * 设置变换参数到GMOperation。
	 * <p>
	 * 包括自动方向、裁剪、缩放、旋转和翻转。
	 * </p>
	 *
	 * @param imageSize 图像尺寸
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setTransformArgs(ImageSize imageSize, GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");
		Assert.notNull(imageSize, "imageSize 不可为 null");

		if (!imageSize.isNormalOrientation()) {
			gmOperation.addRawArg("-auto-orient");
		}

		ImageSize visualImageSize = imageSize.getVisualSize();

		// 判断是否需要裁剪
		if (Objects.nonNull(cropType)) {
			setCropArgs(visualImageSize, gmOperation);
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.allNotNull(targetWidth, targetHeight)) {
			if (forceScale) {
				visualImageSize = visualImageSize.resize(targetWidth, targetHeight);
			} else {
				visualImageSize = visualImageSize.scale(targetWidth, targetHeight);
			}
		} else if (Objects.nonNull(scalingFactor)) {
			visualImageSize = visualImageSize.scale(scalingFactor);
		} else if (Objects.nonNull(targetWidth)) {
			visualImageSize = visualImageSize.scaleByWidth(targetWidth);
		} else if (Objects.nonNull(targetHeight)) {
			visualImageSize = visualImageSize.scaleByHeight(targetHeight);
		}

		if (ObjectUtils.anyNotNull(targetWidth, targetHeight, scalingFactor)) {
			if (Objects.nonNull(resizeFilter)) {
				gmOperation.filter(resizeFilter.graphicsMagickFilterName);
			}

			gmOperation.resize(visualImageSize.getWidth(), visualImageSize.getHeight(), '!');
		}

		// 判断是否需要旋转
		if (Objects.nonNull(rotateAngle)) {
			gmOperation.rotate(rotateAngle);
		}

		// 判断是否需要翻转
		if (Objects.nonNull(flipDirection)) {
			switch (flipDirection) {
				case VERTICAL -> gmOperation.flip();
				case HORIZONTAL -> gmOperation.flop();
			}
		}
	}

	/**
	 * 设置滤镜参数到GMOperation。
	 * <p>
	 * 包括灰度化、亮度/饱和度/色相调整、透明度、锐化、模糊、浮雕和阈值化。
	 * </p>
	 *
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setFilterArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		// 判断是否需要灰度化
		if (grayscale) {
			gmOperation.colorspace("Gray");
		}

		// 判断是否需要调整亮度
		if (brightness != 100) {
			if (hue != 100) {
				gmOperation.addRawArg("-modulate " + brightness + "," + saturation + "," + hue);
			} else if (saturation != 100) {
				gmOperation.addRawArg("-modulate " + brightness + "," + saturation);
			} else {
				gmOperation.addRawArg("-modulate " + brightness);
			}
		}

		// 判断是否需要调整透明度
		if (Objects.nonNull(globalOpacity)) {
			gmOperation.addRawArg("-matte");
			gmOperation.addRawArg("-operator Opacity Negate 0");
			gmOperation.addRawArg("-operator Opacity Multiply " + globalOpacity);
			gmOperation.addRawArg("-operator Opacity Negate 0");
		}

		// 判断是否需要锐化
		if (Objects.nonNull(unsharpKernel)) {
			gmOperation.unsharp(unsharpKernel.radius(), unsharpKernel.sigma(),
				unsharpKernel.amount(), unsharpKernel.threshold());
		} else if (Objects.nonNull(sharpenKernel)) {
			gmOperation.sharpen(sharpenKernel.getLeft(), sharpenKernel.getRight());
		}

		// 判断是否需要模糊
		if (Objects.nonNull(blurKernel)) {
			gmOperation.blur(blurKernel.getLeft(), blurKernel.getRight());
		} else if (Objects.nonNull(gaussianKernel)) {
			gmOperation.gaussian(gaussianKernel.getLeft(), gaussianKernel.getRight());
		} else if (Objects.nonNull(medianRadius)) {
			gmOperation.median(medianRadius);
		}

		// 判断是否需要浮雕
		if (Objects.nonNull(embossRadius)) {
			gmOperation.emboss(embossRadius);
		}

		// 判断是否需要二值化
		if (Objects.nonNull(thresholdPercent)) {
			gmOperation.threshold((int) (thresholdPercent * 65535));
		}
	}

	/**
	 * 设置裁剪参数到GMOperation。
	 * <p>
	 * 支持中心裁剪、偏移裁剪和矩形裁剪。
	 * </p>
	 *
	 * @param imageSize 图像尺寸
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setCropArgs(ImageSize imageSize, GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(cropType)) {
			return;
		}

		if (cropType == CropType.CENTER) {
			if (ObjectUtils.allNotNull(cropCenterWidth, cropCenterHeight) &&
				cropCenterWidth < imageSize.getWidth() && cropCenterHeight < imageSize.getHeight()) {
				int posX = (imageSize.getWidth() - cropCenterWidth) / 2;
				int posY = (imageSize.getHeight() - cropCenterHeight) / 2;
				gmOperation.crop(cropCenterWidth, cropCenterHeight, posX, posY);
			}
		} else if (cropType == CropType.OFFSET) {
			if (ObjectUtils.allNotNull(cropTopOffset, cropBottomOffset, cropLeftOffset, cropRightOffset) &&
				cropLeftOffset + cropRightOffset < imageSize.getWidth() &&
				cropTopOffset + cropBottomOffset < imageSize.getHeight()) {

				int width = imageSize.getWidth() - cropLeftOffset - cropRightOffset;
				int height = imageSize.getHeight() - cropTopOffset - cropBottomOffset;
				gmOperation.crop(width, height, cropLeftOffset, cropTopOffset);
			}
		} else if (cropType == CropType.RECT) {
			if (ObjectUtils.allNotNull(cropRectX, cropRectY, cropRectWidth, cropRectHeight) &&
				cropRectX + cropRectWidth < imageSize.getWidth() && cropRectY + cropRectHeight < imageSize.getHeight()) {

				gmOperation.crop(cropRectWidth, cropRectHeight, cropRectX, cropRectY);
			}
		}

		gmOperation.addRawArg(" +repage");
	}

	/**
	 * 设置水印方向参数到GMOperation。
	 *
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setWatermarkDirectionArg(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(watermarkDirection)) {
			return;
		}

		GMOperation.Gravity gravity = switch (watermarkDirection) {
			case TOP -> GMOperation.Gravity.North;
			case TOP_LEFT -> GMOperation.Gravity.NorthWest;
			case TOP_RIGHT -> GMOperation.Gravity.NorthEast;
			case BOTTOM -> GMOperation.Gravity.South;
			case BOTTOM_LEFT -> GMOperation.Gravity.SouthWest;
			case BOTTOM_RIGHT -> GMOperation.Gravity.SouthEast;
			case CENTER -> GMOperation.Gravity.Center;
			case LEFT -> GMOperation.Gravity.West;
			case RIGHT -> GMOperation.Gravity.East;
		};
		gmOperation.gravity(gravity);
	}

	/**
	 * 设置文字水印参数到GMOperation。
	 *
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setTextWatermarkArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (ObjectUtils.allNotNull(watermarkTextFont, watermarkText)) {
			int marginY = (int) (watermarkTextFontSize * 1.4) + watermarkTextMargin;
			if (Objects.nonNull(watermarkDirection)) {
				// 设置水印方位
				setWatermarkDirectionArg(gmOperation);
				setFontArgs(gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, watermarkTextMargin, marginY, watermarkText));
			} else if (ObjectUtils.allNotNull(watermarkX, watermarkY)) {
				// 设置左上角为原点
				gmOperation.gravity(GMOperation.Gravity.NorthWest);
				setFontArgs(gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, watermarkX + watermarkTextMargin,
					watermarkY + marginY, watermarkText));
			}
		}
	}

	/**
	 * 设置字体参数到GMOperation。
	 * <p>
	 * 包括填充颜色、描边颜色、描边宽度、字体和字体大小。
	 * </p>
	 *
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setFontArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(watermarkTextFont)) {
			return;
		}

		String fillColor = COLOR_FORMAT.formatted(watermarkTextFillColor.getRed(),
			watermarkTextFillColor.getGreen(), watermarkTextFillColor.getBlue(), watermarkTextOpacity);
		gmOperation.fill(fillColor);

		if (watermarkTextStroke) {
			String strokeColor = COLOR_FORMAT.formatted(watermarkTextStrokeColor.getRed(),
				watermarkTextStrokeColor.getGreen(), watermarkTextStrokeColor.getBlue(), watermarkTextOpacity);
			gmOperation.stroke(strokeColor);
			gmOperation.strokewidth(watermarkTextStrokeWidth);
		}

		gmOperation.font(watermarkTextFont);
		gmOperation.pointsize(watermarkTextFontSize);
	}

	/**
	 * 设置输出参数到GMOperation。
	 * <p>
	 * 包括DPI、去除元数据和输出文件。
	 * </p>
	 *
	 * @param outputFile 输出文件
	 * @param gmOperation GMOperation对象
	 * @since 2.1.0
	 */
	public void setOutputArgs(File outputFile, GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		// 修改输出DPI
		if (Objects.nonNull(dpi)) {
			gmOperation.density(dpi);
		}

		// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
		if (stripProfiles) {
			gmOperation.stripProfiles();
		}

		// 传入输出文件
		gmOperation.addImage(outputFile);
	}

	/**
	 * 反锐化掩模核参数记录。
	 *
	 * @param radius 半径
	 * @param sigma sigma值
	 * @param amount 强度
	 * @param threshold 阈值
	 * @since 2.1.0
	 */
	public record UnsharpKernel(double radius, double sigma, double amount, double threshold) {
	}
}
