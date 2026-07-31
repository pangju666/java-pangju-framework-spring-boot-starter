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
 * 提供统一的图像操作接口，具体实现由不同图像引擎（GraphicsMagick、ImageIO、OpenCV）完成。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>缩放：支持按尺寸、按比例、按宽度、按高度等多种缩放方式</li>
 *   <li>旋转/翻转：支持角度旋转和方向翻转</li>
 *   <li>裁剪：支持中心裁剪、偏移裁剪、矩形裁剪</li>
 *   <li>滤镜：支持灰度化、透明度调整</li>
 *   <li>流式API：所有操作方法返回当前实例，支持链式调用</li>
 *   <li>配置复制：支持从现有操作对象复制配置</li>
 *   <li>配置重置：支持将所有配置恢复到默认值</li>
 * </ul>
 *
 * <p><strong>缩放模式</strong></p>
 * <ul>
 *   <li>强制缩放：不考虑原始宽高比，强制缩放到指定尺寸</li>
 *   <li>保持比例：保持原始宽高比，缩放到指定尺寸或按比例缩放</li>
 * </ul>
 *
 * <p><strong>裁剪模式</strong></p>
 * <ul>
 *   <li>中心裁剪：从图像中心裁剪指定尺寸的区域</li>
 *   <li>偏移裁剪：根据四边的偏移量裁剪图像</li>
 *   <li>矩形裁剪：根据指定的矩形区域裁剪图像</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 创建GraphicsMagick操作实例并设置缩放
 * ImageOperations<?> ops = ImageOperations.graphicsMagick()
 *     .scale(800, 600)
 *     .grayscale()
 *     .opacity(0.8f);
 *
 * // 从现有操作对象复制配置
 * ImageOperations<?> newOps = ImageOperations.graphicsMagick(ops);
 * }</pre>
 *
 * <p><strong>实现说明</strong></p>
 * <ul>
 *   <li>使用泛型T支持子类类型返回，实现流式API</li>
 *   <li>提供静态工厂方法创建不同实现类的实例</li>
 *   <li>所有配置参数都支持null值，null表示不应用该操作</li>
 *   <li>参数验证在设置时进行，无效参数会被忽略</li>
 * </ul>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>多个缩放操作会相互覆盖，最后一个生效</li>
 *   <li>多个裁剪操作会相互覆盖，最后一个生效</li>
 *   <li>旋转和翻转操作可以组合使用</li>
 *   <li>滤镜操作可以与其他操作组合使用</li>
 * </ul>
 *
 * @param <T> 子类类型，用于支持流式API的链式调用
 * @author pangju666
 * @see GenericImageOperations
 * @see GraphicsMagickOperations
 * @see ImageIOOperations
 * @see OpenCvOperations
 * @since 2.1.0
 */
public abstract class ImageOperations<T extends ImageOperations<T>> {
	/**
	 * 目标宽度。
	 * <p>
	 * 用于指定图像缩放后的目标宽度，单位为像素。
	 * </p>
	 * <p>
	 * 与{@link #targetHeight}配合使用时，根据{@link #forceScale}决定缩放模式：
	 * <ul>
	 *   <li>forceScale为true：强制缩放到指定尺寸，不考虑原始宽高比</li>
	 *   <li>forceScale为false：保持原始宽高比，缩放到指定尺寸</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 单独使用时，保持原始宽高比，根据目标宽度计算高度。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer targetWidth;
	/**
	 * 目标高度。
	 * <p>
	 * 用于指定图像缩放后的目标高度，单位为像素。
	 * </p>
	 * <p>
	 * 与{@link #targetWidth}配合使用时，根据{@link #forceScale}决定缩放模式：
	 * <ul>
	 *   <li>forceScale为true：强制缩放到指定尺寸，不考虑原始宽高比</li>
	 *   <li>forceScale为false：保持原始宽高比，缩放到指定尺寸</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 单独使用时，保持原始宽高比，根据目标高度计算宽度。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer targetHeight;
	/**
	 * 缩放因子。
	 * <p>
	 * 用于按比例缩放图像，缩放因子大于0表示放大，小于1表示缩小。
	 * 例如：缩放因子为0.5表示缩小到原来的50%，缩放因子为2表示放大到原来的200%。
	 * </p>
	 * <p>
	 * 设置缩放因子会覆盖{@link #targetWidth}和{@link #targetHeight}。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Double scalingFactor;
	/**
	 * 是否强制缩放到指定尺寸。
	 * <p>
	 * 当设置为true时，图像会强制缩放到{@link #targetWidth}和{@link #targetHeight}指定的尺寸，
	 * 不考虑原始宽高比，可能导致图像变形。
	 * </p>
	 * <p>
	 * 当设置为false时，图像会保持原始宽高比，缩放到指定尺寸。
	 * </p>
	 * <p>
	 * 默认值为false。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected boolean forceScale;

	/**
	 * 旋转角度。
	 * <p>
	 * 用于指定图像的旋转角度，单位为度。
	 * 正值表示顺时针旋转，负值表示逆时针旋转。
	 * </p>
	 * <p>
	 * 可以通过{@link #rotate(RotateDirection)}方法设置预定义的旋转方向，
	 * 也可以通过{@link #rotate(Double)}方法直接指定角度。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Double rotateAngle;
	/**
	 * 翻转方向。
	 * <p>
	 * 用于指定图像的翻转方向，支持水平翻转和垂直翻转。
	 * </p>
	 * <p>
	 * 翻转操作与旋转操作可以组合使用。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected FlipDirection flipDirection;

	/**
	 * 裁剪类型。
	 * <p>
	 * 用于指定图像的裁剪方式，支持以下类型：
	 * <ul>
	 *   <li>中心裁剪：从图像中心裁剪指定尺寸的区域</li>
	 *   <li>偏移裁剪：根据四边的偏移量裁剪图像</li>
	 *   <li>矩形裁剪：根据指定的矩形区域裁剪图像</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 裁剪类型决定了使用哪些裁剪参数。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected CropType cropType;
	/**
	 * 中心裁剪宽度。
	 * <p>
	 * 用于指定中心裁剪的宽度，单位为像素。
	 * </p>
	 * <p>
	 * 与{@link #cropCenterHeight}配合使用，当{@link #cropType}为{@link CropType#CENTER}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropCenterWidth;
	/**
	 * 中心裁剪高度。
	 * <p>
	 * 用于指定中心裁剪的高度，单位为像素。
	 * </p>
	 * <p>
	 * 与{@link #cropCenterWidth}配合使用，当{@link #cropType}为{@link CropType#CENTER}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropCenterHeight;
	/**
	 * 顶部偏移量。
	 * <p>
	 * 用于指定偏移裁剪的顶部偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 与其他偏移量配合使用，当{@link #cropType}为{@link CropType#OFFSET}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropTopOffset;
	/**
	 * 底部偏移量。
	 * <p>
	 * 用于指定偏移裁剪的底部偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 与其他偏移量配合使用，当{@link #cropType}为{@link CropType#OFFSET}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropBottomOffset;
	/**
	 * 左侧偏移量。
	 * <p>
	 * 用于指定偏移裁剪的左侧偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 与其他偏移量配合使用，当{@link #cropType}为{@link CropType#OFFSET}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropLeftOffset;
	/**
	 * 右侧偏移量。
	 * <p>
	 * 用于指定偏移裁剪的右侧偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 与其他偏移量配合使用，当{@link #cropType}为{@link CropType#OFFSET}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRightOffset;
	/**
	 * 矩形裁剪X坐标。
	 * <p>
	 * 用于指定矩形裁剪区域的左上角X坐标，单位为像素。
	 * </p>
	 * <p>
	 * 与其他矩形参数配合使用，当{@link #cropType}为{@link CropType#RECT}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectX;
	/**
	 * 矩形裁剪Y坐标。
	 * <p>
	 * 用于指定矩形裁剪区域的左上角Y坐标，单位为像素。
	 * </p>
	 * <p>
	 * 与其他矩形参数配合使用，当{@link #cropType}为{@link CropType#RECT}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectY;
	/**
	 * 矩形裁剪宽度。
	 * <p>
	 * 用于指定矩形裁剪区域的宽度，单位为像素。
	 * </p>
	 * <p>
	 * 与其他矩形参数配合使用，当{@link #cropType}为{@link CropType#RECT}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectWidth;
	/**
	 * 矩形裁剪高度。
	 * <p>
	 * 用于指定矩形裁剪区域的高度，单位为像素。
	 * </p>
	 * <p>
	 * 与其他矩形参数配合使用，当{@link #cropType}为{@link CropType#RECT}时生效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer cropRectHeight;

	/**
	 * 是否灰度化。
	 * <p>
	 * 用于指定是否将图像转换为灰度图。
	 * </p>
	 * <p>
	 * 灰度化会移除图像的色彩信息，只保留亮度信息。
	 * </p>
	 * <p>
	 * 默认值为false。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected boolean grayscale;
	/**
	 * 全局透明度。
	 * <p>
	 * 用于指定图像的全局透明度，取值范围为0-1。
	 * </p>
	 * <p>
	 * 0表示完全透明，1表示完全不透明。
	 * </p>
	 * <p>
	 * 透明度调整会影响整个图像的显示效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Float globalOpacity;

	/**
	 * 默认构造函数。
	 * <p>
	 * 创建一个空的图像操作实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 默认值：
	 * <ul>
	 *   <li>目标宽度：null</li>
	 *   <li>目标高度：null</li>
	 *   <li>缩放因子：null</li>
	 *   <li>强制缩放：false</li>
	 *   <li>旋转角度：null</li>
	 *   <li>翻转方向：null</li>
	 *   <li>裁剪类型：null</li>
	 *   <li>灰度化：false</li>
	 *   <li>全局透明度：null</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public ImageOperations() {
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
	 * 复制的配置包括：
	 * <ul>
	 *   <li>缩放相关：目标宽度、目标高度、缩放因子、强制缩放</li>
	 *   <li>旋转/翻转：旋转角度、翻转方向</li>
	 *   <li>裁剪相关：裁剪类型及所有裁剪参数</li>
	 *   <li>滤镜相关：灰度化、全局透明度</li>
	 * </ul>
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
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
	 * <p>
	 * 创建一个新的GenericImageOperations实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 通用图像操作实例可以用于定义通用的图像处理流程，
	 * 不绑定特定的图像引擎实现。
	 * </p>
	 *
	 * @return GenericImageOperations实例
	 * @see GenericImageOperations
	 * @since 2.1.0
	 */
	public static GenericImageOperations generic() {
		return new GenericImageOperations();
	}

	/**
	 * 创建通用图像操作实例，并复制配置。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新的GenericImageOperations实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例。
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @return GenericImageOperations实例
	 * @see GenericImageOperations
	 * @since 2.1.0
	 */
	public static GenericImageOperations generic(@Nullable ImageOperations<?> operations) {
		return new GenericImageOperations(operations);
	}

	/**
	 * 创建GraphicsMagick图像操作实例。
	 * <p>
	 * 创建一个新的GraphicsMagickOperations实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * GraphicsMagick图像操作实例使用GraphicsMagick引擎进行图像处理，
	 * 适用于需要高性能图像处理的场景。
	 * </p>
	 *
	 * @return GraphicsMagickOperations实例
	 * @see GraphicsMagickOperations
	 * @since 2.1.0
	 */
	public static GraphicsMagickOperations graphicsMagick() {
		return new GraphicsMagickOperations();
	}

	/**
	 * 创建GraphicsMagick图像操作实例，并复制配置。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新的GraphicsMagickOperations实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例。
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @return GraphicsMagickOperations实例
	 * @see GraphicsMagickOperations
	 * @since 2.1.0
	 */
	public static GraphicsMagickOperations graphicsMagick(@Nullable ImageOperations<?> operations) {
		return new GraphicsMagickOperations(operations);
	}

	/**
	 * 创建ImageIO图像操作实例。
	 * <p>
	 * 创建一个新的ImageIOOperations实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * ImageIO图像操作实例使用Java内置的ImageIO进行图像处理，
	 * 适用于简单的图像处理场景，不需要外部依赖。
	 * </p>
	 *
	 * @return ImageIOOperations实例
	 * @see ImageIOOperations
	 * @since 2.1.0
	 */
	public static ImageIOOperations imageIO() {
		return new ImageIOOperations();
	}

	/**
	 * 创建ImageIO图像操作实例，并复制配置。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新的ImageIOOperations实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例。
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @return ImageIOOperations实例
	 * @see ImageIOOperations
	 * @since 2.1.0
	 */
	public static ImageIOOperations imageIO(@Nullable ImageOperations<?> operations) {
		return new ImageIOOperations(operations);
	}

	/**
	 * 创建OpenCV图像操作实例。
	 * <p>
	 * 创建一个新的OpenCvOperations实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * OpenCV图像操作实例使用OpenCV库进行图像处理，
	 * 适用于需要高级图像处理功能的场景。
	 * </p>
	 *
	 * @return OpenCvOperations实例
	 * @see OpenCvOperations
	 * @since 2.1.0
	 */
	public static OpenCvOperations opencv() {
		return new OpenCvOperations();
	}

	/**
	 * 创建OpenCV图像操作实例，并复制配置。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新的OpenCvOperations实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例。
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @return OpenCvOperations实例
	 * @see OpenCvOperations
	 * @since 2.1.0
	 */
	public static OpenCvOperations opencv(@Nullable ImageOperations<?> operations) {
		return new OpenCvOperations(operations);
	}

	/**
	 * 获取目标宽度。
	 * <p>
	 * 返回图像缩放后的目标宽度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置目标宽度。
	 * </p>
	 *
	 * @return 目标宽度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getTargetWidth() {
		return targetWidth;
	}

	/**
	 * 获取目标高度。
	 * <p>
	 * 返回图像缩放后的目标高度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置目标高度。
	 * </p>
	 *
	 * @return 目标高度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getTargetHeight() {
		return targetHeight;
	}

	/**
	 * 获取缩放因子。
	 * <p>
	 * 返回图像的缩放因子。
	 * </p>
	 * <p>
	 * 缩放因子大于0表示放大，小于1表示缩小。
	 * 如果返回null，表示未设置缩放因子。
	 * </p>
	 *
	 * @return 缩放因子，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Double getScalingFactor() {
		return scalingFactor;
	}

	/**
	 * 是否强制缩放。
	 * <p>
	 * 返回是否强制缩放到指定尺寸。
	 * </p>
	 * <p>
	 * 当返回true时，图像会强制缩放到指定尺寸，不考虑原始宽高比。
	 * 当返回false时，图像会保持原始宽高比。
	 * </p>
	 *
	 * @return 如果强制缩放返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isForceScale() {
		return forceScale;
	}

	/**
	 * 获取旋转角度。
	 * <p>
	 * 返回图像的旋转角度，单位为度。
	 * </p>
	 * <p>
	 * 正值表示顺时针旋转，负值表示逆时针旋转。
	 * 如果返回null，表示未设置旋转角度。
	 * </p>
	 *
	 * @return 旋转角度（度），可能为null
	 * @since 2.1.0
	 */
	public @Nullable Double getRotateAngle() {
		return rotateAngle;
	}

	/**
	 * 获取翻转方向。
	 * <p>
	 * 返回图像的翻转方向。
	 * </p>
	 * <p>
	 * 支持水平翻转和垂直翻转。
	 * 如果返回null，表示未设置翻转方向。
	 * </p>
	 *
	 * @return 翻转方向，可能为null
	 * @since 2.1.0
	 */
	public @Nullable FlipDirection getFlipDirection() {
		return flipDirection;
	}

	/**
	 * 获取裁剪类型。
	 * <p>
	 * 返回图像的裁剪类型。
	 * </p>
	 * <p>
	 * 支持中心裁剪、偏移裁剪、矩形裁剪。
	 * 如果返回null，表示未设置裁剪类型。
	 * </p>
	 *
	 * @return 裁剪类型，可能为null
	 * @since 2.1.0
	 */
	public @Nullable CropType getCropType() {
		return cropType;
	}

	/**
	 * 获取中心裁剪宽度。
	 * <p>
	 * 返回中心裁剪的宽度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中心裁剪宽度。
	 * </p>
	 *
	 * @return 中心裁剪宽度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropCenterWidth() {
		return cropCenterWidth;
	}

	/**
	 * 获取中心裁剪高度。
	 * <p>
	 * 返回中心裁剪的高度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中心裁剪高度。
	 * </p>
	 *
	 * @return 中心裁剪高度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropCenterHeight() {
		return cropCenterHeight;
	}

	/**
	 * 获取顶部偏移量。
	 * <p>
	 * 返回偏移裁剪的顶部偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置顶部偏移量。
	 * </p>
	 *
	 * @return 顶部偏移量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropTopOffset() {
		return cropTopOffset;
	}

	/**
	 * 获取底部偏移量。
	 * <p>
	 * 返回偏移裁剪的底部偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置底部偏移量。
	 * </p>
	 *
	 * @return 底部偏移量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropBottomOffset() {
		return cropBottomOffset;
	}

	/**
	 * 获取左侧偏移量。
	 * <p>
	 * 返回偏移裁剪的左侧偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置左侧偏移量。
	 * </p>
	 *
	 * @return 左侧偏移量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropLeftOffset() {
		return cropLeftOffset;
	}

	/**
	 * 获取右侧偏移量。
	 * <p>
	 * 返回偏移裁剪的右侧偏移量，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置右侧偏移量。
	 * </p>
	 *
	 * @return 右侧偏移量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRightOffset() {
		return cropRightOffset;
	}

	/**
	 * 获取矩形裁剪X坐标。
	 * <p>
	 * 返回矩形裁剪区域的左上角X坐标，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置矩形裁剪X坐标。
	 * </p>
	 *
	 * @return 矩形裁剪X坐标，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectX() {
		return cropRectX;
	}

	/**
	 * 获取矩形裁剪Y坐标。
	 * <p>
	 * 返回矩形裁剪区域的左上角Y坐标，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置矩形裁剪Y坐标。
	 * </p>
	 *
	 * @return 矩形裁剪Y坐标，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectY() {
		return cropRectY;
	}

	/**
	 * 获取矩形裁剪宽度。
	 * <p>
	 * 返回矩形裁剪区域的宽度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置矩形裁剪宽度。
	 * </p>
	 *
	 * @return 矩形裁剪宽度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectWidth() {
		return cropRectWidth;
	}

	/**
	 * 获取矩形裁剪高度。
	 * <p>
	 * 返回矩形裁剪区域的高度，单位为像素。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置矩形裁剪高度。
	 * </p>
	 *
	 * @return 矩形裁剪高度，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getCropRectHeight() {
		return cropRectHeight;
	}

	/**
	 * 是否灰度化。
	 * <p>
	 * 返回是否将图像转换为灰度图。
	 * </p>
	 * <p>
	 * 当返回true时，图像会转换为灰度图。
	 * </p>
	 *
	 * @return 如果灰度化返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isGrayscale() {
		return grayscale;
	}

	/**
	 * 获取全局透明度。
	 * <p>
	 * 返回图像的全局透明度，取值范围为0-1。
	 * </p>
	 * <p>
	 * 0表示完全透明，1表示完全不透明。
	 * 如果返回null，表示未设置透明度。
	 * </p>
	 *
	 * @return 全局透明度（0-1），可能为null
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
	 * <p>
	 * 此方法会设置{@link #forceScale}为true，并清除{@link #scalingFactor}。
	 * </p>
	 * <p>
	 * 如果任一参数为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>强制缩放可能导致图像变形</li>
	 *   <li>此方法会覆盖之前的缩放设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param targetWidth  目标宽度，单位为像素，必须大于0
	 * @param targetHeight 目标高度，单位为像素，必须大于0
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
	 * <p>
	 * 此方法会设置{@link #forceScale}为false，并清除{@link #scalingFactor}。
	 * </p>
	 * <p>
	 * 可以单独设置宽度或高度，另一个维度会根据原始宽高比自动计算。
	 * 如果同时设置宽度和高度，图像会缩放到能完全包含在指定尺寸内的最大尺寸。
	 * </p>
	 * <p>
	 * 如果参数为null或无效，则不修改对应的配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>此方法会覆盖之前的缩放设置</li>
	 *   <li>保持宽高比可能导致输出尺寸与指定尺寸不完全一致</li>
	 * </ul>
	 * </p>
	 *
	 * @param targetWidth  目标宽度，单位为像素，必须大于0，可以为null
	 * @param targetHeight 目标高度，单位为像素，必须大于0，可以为null
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
	 * <p>
	 * 缩放因子大于1表示放大，小于1表示缩小。
	 * 例如：缩放因子为0.5表示缩小到原来的50%，缩放因子为2表示放大到原来的200%。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #forceScale}为false，并清除{@link #targetWidth}和{@link #targetHeight}。
	 * </p>
	 * <p>
	 * 如果缩放因子为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>此方法会覆盖之前的缩放设置</li>
	 *   <li>缩放因子必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param scalingFactor 缩放因子，必须大于0，可以为null
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
	 * <p>
	 * 高度会根据原始宽高比自动计算。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #forceScale}为false，清除{@link #targetHeight}和{@link #scalingFactor}。
	 * </p>
	 * <p>
	 * 如果目标宽度为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>此方法会覆盖之前的缩放设置</li>
	 *   <li>目标宽度必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param targetWidth 目标宽度，单位为像素，必须大于0，可以为null
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
	 * <p>
	 * 宽度会根据原始宽高比自动计算。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #forceScale}为false，清除{@link #targetWidth}和{@link #scalingFactor}。
	 * </p>
	 * <p>
	 * 如果目标高度为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>此方法会覆盖之前的缩放设置</li>
	 *   <li>目标高度必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param targetHeight 目标高度，单位为像素，必须大于0，可以为null
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
	 * <p>
	 * 支持的旋转方向包括：
	 * <ul>
	 *   <li>顺时针90度</li>
	 *   <li>逆时针90度</li>
	 *   <li>180度</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 此方法会根据旋转方向设置{@link #rotateAngle}。
	 * </p>
	 * <p>
	 * 如果旋转方向为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong>
	 * <ul>
	 *   <li>此方法会覆盖之前的旋转角度设置</li>
	 *   <li>旋转操作可以与翻转操作组合使用</li>
	 * </ul>
	 * </p>
	 *
	 * @param direction 旋转方向，可以为null
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
	 * <p>
	 * 正值表示顺时针旋转，负值表示逆时针旋转。
	 * 例如：90表示顺时针旋转90度，-90表示逆时针旋转90度。
	 * </p>
	 * <p>
	 * 此方法会直接设置{@link #rotateAngle}。
	 * </p>
	 * <p>
	 * 如果旋转角度为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的旋转角度设置</li>
	 *   <li>旋转操作可以与翻转操作组合使用</li>
	 * </ul>
	 *
	 * @param angle 旋转角度（度），可以为null
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
	 * <p>
	 * 支持的翻转方向包括：
	 * <ul>
	 *   <li>水平翻转：左右镜像</li>
	 *   <li>垂直翻转：上下镜像</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 此方法会设置{@link #flipDirection}。
	 * </p>
	 * <p>
	 * 如果翻转方向为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的翻转方向设置</li>
	 *   <li>翻转操作可以与旋转操作组合使用</li>
	 * </ul>
	 *
	 * @param direction 翻转方向，可以为null
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
	 * <p>
	 * 裁剪区域以图像中心为基准，向四周扩展指定宽度和高度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #cropType}为{@link CropType#CENTER}，
	 * 并设置{@link #cropCenterWidth}和{@link #cropCenterHeight}。
	 * </p>
	 * <p>
	 * 如果任一参数为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的裁剪设置</li>
	 *   <li>如果裁剪尺寸大于图像尺寸，裁剪区域会被限制在图像范围内</li>
	 * </ul>
	 *
	 * @param width  裁剪宽度，单位为像素，必须大于0，可以为null
	 * @param height 裁剪高度，单位为像素，必须大于0，可以为null
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
	 * <p>
	 * 从图像的顶部、底部、左侧、右侧分别裁剪指定偏移量的区域。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #cropType}为{@link CropType#OFFSET}，
	 * 并设置四个偏移量参数。
	 * </p>
	 * <p>
	 * 如果任一参数为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的裁剪设置</li>
	 *   <li>偏移量不能为负数</li>
	 *   <li>如果偏移量总和大于图像尺寸，裁剪结果可能为空</li>
	 * </ul>
	 *
	 * @param topOffset    顶部偏移量，单位为像素，不能为负数，可以为null
	 * @param bottomOffset 底部偏移量，单位为像素，不能为负数，可以为null
	 * @param leftOffset   左侧偏移量，单位为像素，不能为负数，可以为null
	 * @param rightOffset  右侧偏移量，单位为像素，不能为负数，可以为null
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
	 * <p>
	 * 从图像的指定位置裁剪一个矩形区域。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #cropType}为{@link CropType#RECT}，
	 * 并设置矩形的位置和尺寸参数。
	 * </p>
	 * <p>
	 * 如果任一参数为null或无效，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的裁剪设置</li>
	 *   <li>坐标不能为负数</li>
	 *   <li>宽度和高度必须大于0</li>
	 *   <li>如果矩形区域超出图像范围，裁剪结果可能不完整</li>
	 * </ul>
	 *
	 * @param x      矩形左上角X坐标，单位为像素，不能为负数，可以为null
	 * @param y      矩形左上角Y坐标，单位为像素，不能为负数，可以为null
	 * @param width  矩形宽度，单位为像素，必须大于0，可以为null
	 * @param height 矩形高度，单位为像素，必须大于0，可以为null
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
	 * <p>
	 * 灰度化会移除图像的色彩信息，只保留亮度信息。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #grayscale}为true。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的灰度化设置</li>
	 *   <li>灰度化操作不可逆</li>
	 * </ul>
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
	 * <p>
	 * 当设置为true时，图像会转换为灰度图。
	 * 当设置为false时，图像保持原色。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #grayscale}。
	 * </p>
	 * <p>
	 * 如果参数为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的灰度化设置</li>
	 * </ul>
	 *
	 * @param grayscale 是否灰度化，可以为null
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
	 * <p>
	 * 透明度取值范围为0-1，0表示完全透明，1表示完全不透明。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #globalOpacity}。
	 * </p>
	 * <p>
	 * 如果透明度为null或超出范围，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的透明度设置</li>
	 *   <li>透明度必须在0-1范围内</li>
	 *   <li>透明度调整会影响整个图像的显示效果</li>
	 * </ul>
	 *
	 * @param opacity 透明度（0-1），可以为null
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
	 * <p>
	 * 重置后的默认值：
	 * <ul>
	 *   <li>目标宽度：null</li>
	 *   <li>目标高度：null</li>
	 *   <li>缩放因子：null</li>
	 *   <li>强制缩放：false</li>
	 *   <li>旋转角度：null</li>
	 *   <li>翻转方向：null</li>
	 *   <li>裁剪类型：null</li>
	 *   <li>所有裁剪参数：null</li>
	 *   <li>灰度化：false</li>
	 *   <li>全局透明度：null</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 此方法可以用于清除之前的所有操作配置，重新开始配置。
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
	 * <p>
	 * 此方法将当前实例强制转换为子类类型T，
	 * 使得所有操作方法都能返回子类实例，支持链式调用。
	 * </p>
	 * <p>
	 * <strong>实现说明</strong></p>
	 * <ul>
	 *   <li>使用@SuppressWarnings抑制未检查的转换警告</li>
	 *   <li>泛型T确保类型安全</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，转换为子类类型T
	 * @since 2.1.0
	 */
	@SuppressWarnings("unchecked")
	private T self() {
		return (T) this;
	}
}
