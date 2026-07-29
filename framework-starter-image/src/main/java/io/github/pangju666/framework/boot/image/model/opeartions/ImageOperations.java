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

import io.github.pangju666.commons.image.enums.FlipDirection;
import io.github.pangju666.commons.image.enums.RotateDirection;
import io.github.pangju666.framework.boot.image.enums.CropType;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 图像操作抽象基类。
 * <p>
 * 定义图像处理的通用操作接口，采用流式API设计，支持链式调用。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *     <li>缩放：支持按尺寸、按比例、按宽度、按高度等多种缩放方式</li>
 *     <li>旋转/翻转：支持角度旋转和方向翻转</li>
 *     <li>裁剪：支持中心裁剪、偏移裁剪、矩形裁剪</li>
 *     <li>滤镜：支持灰度化、透明度调整</li>
 * </ul>
 * </p>
 * <p>
 * 提供静态工厂方法创建不同实现类的实例：{@link #generic()}、{@link #graphicsMagick()}、{@link #imageIO()}、{@link #opencv()}
 * </p>
 *
 * @param <T> 子类类型，用于支持流式API的链式调用
 * @author pangju666
 * @since 2.1.0
 */
public abstract class ImageOperations<T extends ImageOperations<T>> {
	/**
	 * 目标宽度
	 *
	 * @since 2.1.0
	 */
	protected Integer targetWidth;
	/**
	 * 目标高度
	 *
	 * @since 2.1.0
	 */
	protected Integer targetHeight;
	/**
	 * 缩放因子
	 *
	 * @since 2.1.0
	 */
	protected Double scalingFactor;
	/**
	 * 是否强制缩放到指定尺寸
	 *
	 * @since 2.1.0
	 */
	protected boolean forceScale;

	/**
	 * 旋转角度
	 *
	 * @since 2.1.0
	 */
	protected Double rotateAngle;
	/**
	 * 翻转方向
	 *
	 * @since 2.1.0
	 */
	protected FlipDirection flipDirection;

	/**
	 * 裁剪类型
	 *
	 * @since 2.1.0
	 */
	protected CropType cropType;
	/**
	 * 中心裁剪宽度
	 *
	 * @since 2.1.0
	 */
	protected Integer cropCenterWidth;
	/**
	 * 中心裁剪高度
	 *
	 * @since 2.1.0
	 */
	protected Integer cropCenterHeight;
	/**
	 * 顶部偏移量
	 *
	 * @since 2.1.0
	 */
	protected Integer cropTopOffset;
	/**
	 * 底部偏移量
	 *
	 * @since 2.1.0
	 */
	protected Integer cropBottomOffset;
	/**
	 * 左侧偏移量
	 *
	 * @since 2.1.0
	 */
	protected Integer cropLeftOffset;
	/**
	 * 右侧偏移量
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRightOffset;
	/**
	 * 矩形裁剪X坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectX;
	/**
	 * 矩形裁剪Y坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectY;
	/**
	 * 矩形裁剪宽度
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectWidth;
	/**
	 * 矩形裁剪高度
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectHeight;

	/**
	 * 是否灰度化
	 *
	 * @since 2.1.0
	 */
	protected boolean grayscale;
	/**
	 * 全局透明度
	 *
	 * @since 2.1.0
	 */
	protected Float globalOpacity;

	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public ImageOperations() {
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param operations 源操作对象
	 * @since 2.1.0
	 */
	public ImageOperations(@Nullable ImageOperations<?> operations) {
		if (Objects.nonNull(operations)) {
			/* 缩放相关配置 */
			this.targetWidth = operations.targetWidth;
			this.scalingFactor = operations.scalingFactor;
			this.targetHeight = operations.targetHeight;
			this.forceScale = operations.forceScale;

			/* 旋转/翻转配置 */
			this.flipDirection = operations.flipDirection;
			this.rotateAngle = operations.rotateAngle;

			/* 裁剪相关配置 */
			this.cropType = operations.cropType;
			this.cropCenterWidth = operations.cropCenterWidth;
			this.cropCenterHeight = operations.cropCenterHeight;
			this.cropTopOffset = operations.cropTopOffset;
			this.cropBottomOffset = operations.cropBottomOffset;
			this.cropLeftOffset = operations.cropLeftOffset;
			this.cropRightOffset = operations.cropRightOffset;
			this.cropRectX = operations.cropRectX;
			this.cropRectY = operations.cropRectY;
			this.cropRectWidth = operations.cropRectWidth;
			this.cropRectHeight = operations.cropRectHeight;

			/* 滤镜相关配置 */
			this.grayscale = operations.grayscale;
			this.globalOpacity = operations.globalOpacity;
		}
	}

	/**
	 * 创建通用图像操作实例。
	 *
	 * @return GenericImageOperations实例
	 * @since 2.1.0
	 */
	public static GenericImageOperations generic() {
		return new GenericImageOperations();
	}

	/**
	 * 创建通用图像操作实例，并复制配置。
	 *
	 * @param operations 源操作对象
	 * @return GenericImageOperations实例
	 * @since 2.1.0
	 */
	public static GenericImageOperations generic(@Nullable ImageOperations<?> operations) {
		return new GenericImageOperations(operations);
	}

	/**
	 * 创建GraphicsMagick图像操作实例。
	 *
	 * @return GraphicsMagickOperations实例
	 * @since 2.1.0
	 */
	public static GraphicsMagickOperations graphicsMagick() {
		return new GraphicsMagickOperations();
	}

	/**
	 * 创建GraphicsMagick图像操作实例，并复制配置。
	 *
	 * @param operations 源操作对象
	 * @return GraphicsMagickOperations实例
	 * @since 2.1.0
	 */
	public static GraphicsMagickOperations graphicsMagick(@Nullable ImageOperations<?> operations) {
		return new GraphicsMagickOperations(operations);
	}

	/**
	 * 创建ImageIO图像操作实例。
	 *
	 * @return ImageIOOperations实例
	 * @since 2.1.0
	 */
	public static ImageIOOperations imageIO() {
		return new ImageIOOperations();
	}

	/**
	 * 创建ImageIO图像操作实例，并复制配置。
	 *
	 * @param operations 源操作对象
	 * @return ImageIOOperations实例
	 * @since 2.1.0
	 */
	public static ImageIOOperations imageIO(@Nullable ImageOperations<?> operations) {
		return new ImageIOOperations(operations);
	}

	/**
	 * 创建OpenCV图像操作实例。
	 *
	 * @return OpenCvOperations实例
	 * @since 2.1.0
	 */
	public static OpenCvOperations opencv() {
		return new OpenCvOperations();
	}

	/**
	 * 创建OpenCV图像操作实例，并复制配置。
	 *
	 * @param operations 源操作对象
	 * @return OpenCvOperations实例
	 * @since 2.1.0
	 */
	public static OpenCvOperations opencv(@Nullable ImageOperations<?> operations) {
		return new OpenCvOperations(operations);
	}

	/**
	 * 获取目标宽度。
	 *
	 * @return 目标宽度
	 * @since 2.1.0
	 */
	public @Nullable Integer getTargetWidth() {
		return targetWidth;
	}

	/**
	 * 获取目标高度。
	 *
	 * @return 目标高度
	 * @since 2.1.0
	 */
	public @Nullable Integer getTargetHeight() {
		return targetHeight;
	}

	/**
	 * 获取缩放因子。
	 *
	 * @return 缩放因子
	 * @since 2.1.0
	 */
	public @Nullable Double getScalingFactor() {
		return scalingFactor;
	}

	/**
	 * 是否强制缩放。
	 *
	 * @return 如果强制缩放返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isForceScale() {
		return forceScale;
	}

	/**
	 * 获取旋转角度。
	 *
	 * @return 旋转角度（度）
	 * @since 2.1.0
	 */
	public @Nullable Double getRotateAngle() {
		return rotateAngle;
	}

	/**
	 * 获取翻转方向。
	 *
	 * @return 翻转方向
	 * @since 2.1.0
	 */
	public @Nullable FlipDirection getFlipDirection() {
		return flipDirection;
	}

	/**
	 * 获取裁剪类型。
	 *
	 * @return 裁剪类型
	 * @since 2.1.0
	 */
	public @Nullable CropType getCropType() {
		return cropType;
	}

	/**
	 * 获取中心裁剪宽度。
	 *
	 * @return 中心裁剪宽度
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropCenterWidth() {
		return cropCenterWidth;
	}

	/**
	 * 获取中心裁剪高度。
	 *
	 * @return 中心裁剪高度
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropCenterHeight() {
		return cropCenterHeight;
	}

	/**
	 * 获取顶部偏移量。
	 *
	 * @return 顶部偏移量
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropTopOffset() {
		return cropTopOffset;
	}

	/**
	 * 获取底部偏移量。
	 *
	 * @return 底部偏移量
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropBottomOffset() {
		return cropBottomOffset;
	}

	/**
	 * 获取左侧偏移量。
	 *
	 * @return 左侧偏移量
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropLeftOffset() {
		return cropLeftOffset;
	}

	/**
	 * 获取右侧偏移量。
	 *
	 * @return 右侧偏移量
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRightOffset() {
		return cropRightOffset;
	}

	/**
	 * 获取矩形裁剪X坐标。
	 *
	 * @return 矩形裁剪X坐标
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectX() {
		return cropRectX;
	}

	/**
	 * 获取矩形裁剪Y坐标。
	 *
	 * @return 矩形裁剪Y坐标
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectY() {
		return cropRectY;
	}

	/**
	 * 获取矩形裁剪宽度。
	 *
	 * @return 矩形裁剪宽度
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectWidth() {
		return cropRectWidth;
	}

	/**
	 * 获取矩形裁剪高度。
	 *
	 * @return 矩形裁剪高度
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectHeight() {
		return cropRectHeight;
	}

	/**
	 * 是否灰度化。
	 *
	 * @return 如果灰度化返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isGrayscale() {
		return grayscale;
	}

	/**
	 * 获取全局透明度。
	 *
	 * @return 全局透明度（0-1）
	 * @since 2.1.0
	 */
	public @Nullable Float getGlobalOpacity() {
		return globalOpacity;
	}

	/**
	 * 强制缩放到指定尺寸。
	 * <p>
	 * 强制将图像缩放到指定的宽度和高度，不考虑原始宽高比。
	 * </p>
	 *
	 * @param targetWidth 目标宽度
	 * @param targetHeight 目标高度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T forceScale(@Nullable Integer targetWidth, @Nullable Integer targetHeight) {
		if (ObjectUtils.allNotNull(targetWidth, targetWidth)) {
			this.targetWidth = targetWidth;
			this.targetHeight = targetHeight;
			this.scalingFactor = null;
			this.forceScale = true;
		}
		return self();
	}

	/**
	 * 缩放到指定尺寸。
	 * <p>
	 * 将图像缩放到指定的宽度和高度，保持原始宽高比。
	 * </p>
	 *
	 * @param targetWidth 目标宽度
	 * @param targetHeight 目标高度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T scale(@Nullable Integer targetWidth, @Nullable Integer targetHeight) {
		if (Objects.nonNull(targetWidth) && targetWidth > 0) {
			this.targetWidth = targetWidth;
			this.forceScale = false;
			this.scalingFactor = null;
		}
		if (Objects.nonNull(targetHeight) && targetHeight > 0) {
			this.targetHeight = targetHeight;
			this.forceScale = false;
			this.scalingFactor = null;
		}
		return self();
	}

	/**
	 * 按比例缩放。
	 * <p>
	 * 按指定的缩放因子缩放图像。
	 * </p>
	 *
	 * @param scalingFactor 缩放因子，大于0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T scale(@Nullable Double scalingFactor) {
		if (Objects.nonNull(scalingFactor) && scalingFactor > 0) {
			this.forceScale = false;
			this.scalingFactor = scalingFactor;
			this.targetWidth = null;
			this.targetHeight = null;
		}
		return self();
	}

	/**
	 * 按宽度缩放。
	 * <p>
	 * 根据目标宽度缩放图像，保持原始宽高比。
	 * </p>
	 *
	 * @param targetWidth 目标宽度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T scaleByWidth(@Nullable Integer targetWidth) {
		if (Objects.nonNull(targetWidth) && targetWidth > 0) {
			this.forceScale = false;
			this.targetWidth = targetWidth;
			this.targetHeight = null;
			this.scalingFactor = null;
		}
		return self();
	}

	/**
	 * 按高度缩放。
	 * <p>
	 * 根据目标高度缩放图像，保持原始宽高比。
	 * </p>
	 *
	 * @param targetHeight 目标高度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T scaleByHeight(@Nullable Integer targetHeight) {
		if (Objects.nonNull(targetHeight) && targetHeight > 0) {
			this.forceScale = false;
			this.targetWidth = null;
			this.targetHeight = targetHeight;
			this.scalingFactor = null;
		}
		return self();
	}

	/**
	 * 按方向旋转。
	 * <p>
	 * 按指定的旋转方向旋转图像。
	 * </p>
	 *
	 * @param direction 旋转方向
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T rotate(@Nullable RotateDirection direction) {
		if (Objects.nonNull(direction)) {
			this.rotateAngle = direction.getAngle();
		}
		return self();
	}

	/**
	 * 按角度旋转。
	 * <p>
	 * 按指定的角度旋转图像。
	 * </p>
	 *
	 * @param angle 旋转角度（度）
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T rotate(@Nullable Double angle) {
		if (Objects.nonNull(angle)) {
			this.rotateAngle = angle;
		}
		return self();
	}

	/**
	 * 翻转图像。
	 * <p>
	 * 按指定的方向翻转图像。
	 * </p>
	 *
	 * @param direction 翻转方向
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T flip(@Nullable FlipDirection direction) {
		if (Objects.nonNull(direction)) {
			this.flipDirection = direction;
		}
		return self();
	}

	/**
	 * 按中心裁剪。
	 * <p>
	 * 从图像中心裁剪指定尺寸的区域。
	 * </p>
	 *
	 * @param width 裁剪宽度
	 * @param height 裁剪高度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T cropByCenter(@Nullable Integer width, @Nullable Integer height) {
		if (ObjectUtils.allNotNull(width, height) && width > 0 && height > 0) {
			this.cropCenterWidth = width;
			this.cropCenterHeight = height;
			this.cropType = CropType.CENTER;
		}
		return self();
	}

	/**
	 * 按偏移量裁剪。
	 * <p>
	 * 根据四边的偏移量裁剪图像。
	 * </p>
	 *
	 * @param topOffset 顶部偏移量
	 * @param bottomOffset 底部偏移量
	 * @param leftOffset 左侧偏移量
	 * @param rightOffset 右侧偏移量
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T cropByOffset(@Nullable Integer topOffset, @Nullable Integer bottomOffset, @Nullable Integer leftOffset,
	                      @Nullable Integer rightOffset) {
		if (ObjectUtils.allNotNull(topOffset, bottomOffset, leftOffset, rightOffset) &&
			topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0) {
			this.cropTopOffset = topOffset;
			this.cropBottomOffset = bottomOffset;
			this.cropLeftOffset = leftOffset;
			this.cropRightOffset = rightOffset;
			this.cropType = CropType.OFFSET;
		}
		return self();
	}

	/**
	 * 按矩形裁剪。
	 * <p>
	 * 根据指定的矩形区域裁剪图像。
	 * </p>
	 *
	 * @param x 矩形左上角X坐标
	 * @param y 矩形左上角Y坐标
	 * @param width 矩形宽度
	 * @param height 矩形高度
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T cropByRect(@Nullable Integer x, @Nullable Integer y, @Nullable Integer width, @Nullable Integer height) {
		if (ObjectUtils.allNotNull(x, y, width, height) && x >= 0 && y >= 0 && width > 0 && height > 0) {
			this.cropRectX = x;
			this.cropRectY = y;
			this.cropRectWidth = width;
			this.cropRectHeight = height;
			this.cropType = CropType.RECT;
		}
		return self();
	}

	/**
	 * 灰度化。
	 * <p>
	 * 将图像转换为灰度图。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T grayscale() {
		this.grayscale = true;
		return self();
	}

	/**
	 * 设置灰度化。
	 * <p>
	 * 设置是否将图像转换为灰度图。
	 * </p>
	 *
	 * @param grayscale 是否灰度化
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T grayscale(@Nullable Boolean grayscale) {
		if (Objects.nonNull(grayscale)) {
			this.grayscale = grayscale;
		}
		return self();
	}

	/**
	 * 设置透明度。
	 * <p>
	 * 设置图像的全局透明度。
	 * </p>
	 *
	 * @param opacity 透明度（0-1）
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public T opacity(@Nullable Float opacity) {
		if (Objects.nonNull(opacity) && opacity >= 0 && opacity <= 1) {
			this.globalOpacity = opacity;
		}
		return self();
	}

	/**
	 * 重置所有配置。
	 * <p>
	 * 将所有配置恢复到默认值。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public void reset() {
		/* 缩放相关配置 */
		this.targetWidth = null;
		this.targetHeight = null;
		this.scalingFactor = null;
		this.forceScale = false;

		/* 旋转/翻转配置 */
		this.rotateAngle = null;
		this.flipDirection = null;

		/* 裁剪相关配置 */
		this.cropType = null;
		this.cropCenterWidth = null;
		this.cropCenterHeight = null;
		this.cropTopOffset = null;
		this.cropBottomOffset = null;
		this.cropLeftOffset = null;
		this.cropRightOffset = null;
		this.cropRectX = null;
		this.cropRectY = null;
		this.cropRectWidth = null;
		this.cropRectHeight = null;

		/* 滤镜相关配置 */
		this.grayscale = false;
		this.globalOpacity = null;

	}

	/**
	 * 返回当前实例。
	 * <p>
	 * 用于支持流式API的链式调用。
	 * </p>
	 *
	 * @return 当前实例
	 * @since 2.1.0
	 */
	@SuppressWarnings("unchecked")
	private T self() {
		return (T) this;
	}
}
