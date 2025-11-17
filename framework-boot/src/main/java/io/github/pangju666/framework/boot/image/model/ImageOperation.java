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

import io.github.pangju666.commons.image.enums.WatermarkDirection;
import io.github.pangju666.commons.image.model.ImageWatermarkOption;
import io.github.pangju666.framework.boot.image.enums.CropType;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 * 图像处理操作配置模型，支持裁剪、缩放及图片水印。
 * 使用构建器模式设置参数，供图像处理组件读取并执行。
 *
 * <p><strong>使用说明</strong>：通过构建器链式设置参数；不满足校验规则的参数将被忽略。</p>
 * <p><strong>互斥规则</strong>：水印方向与坐标互斥；设置其中之一会清空另一种配置。</p>
 * <p><strong>定位规则</strong>：可使用 {@code watermarkDirection} 或 {@code watermarkPosition(x,y)}；坐标需为正数。</p>
 * <p><strong>裁剪规则</strong>：支持中心裁剪、偏移裁剪与矩形裁剪；如果裁剪参数为空、非正数或越界，则不设置裁剪。</p>
 * <p><strong>缩放规则</strong>：{@code forceScale(width,height)} 强制缩放到指定尺寸；按比例/按宽/按高缩放为等比，并会关闭强制缩放且清空其它尺寸/比例。</p>
 * <p><strong>透明度范围</strong>：取值区间 [0,1]；水印透明度遵循该范围。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public abstract class ImageOperation {
	/**
	 * 水印方向（当未设置具体坐标时生效，与坐标互斥）。
	 *
	 * <p>默认值：{@link WatermarkDirection#TOP_RIGHT}</p>
	 *
	 * @since 1.0.0
	 */
	protected WatermarkDirection watermarkDirection = WatermarkDirection.TOP_RIGHT;
	/**
	 * 水印位置的 X 坐标（与方向互斥）。
	 *
	 * @since 1.0.0
	 */
	protected Integer watermarkX;
	/**
	 * 水印位置的 Y 坐标（与方向互斥）。
	 *
	 * @since 1.0.0
	 */
	protected Integer watermarkY;
	/**
	 * 目标宽度（按宽度缩放或强制缩放时设置）。
	 *
	 * @since 1.0.0
	 */
	protected Integer targetWidth;
	/**
	 * 缩放比例（>0）。
	 *
	 * @since 1.0.0
	 */
	protected Float scaleRatio;
	/**
	 * 目标高度（按高度缩放或强制缩放时设置）。
	 *
	 * @since 1.0.0
	 */
	protected Integer targetHeight;
	/**
	 * 是否强制缩放（忽略比例，直接缩放到指定宽高）。
	 *
	 * <p>默认值：{@code false}</p>
	 *
	 * @since 1.0.0
	 */
	protected boolean forceScale = false;
	/**
	 * 图片水印配置。
	 *
	 * <p>默认值：使用默认配置的新实例（非空）。</p>
	 *
	 * @since 1.0.0
	 */
	protected ImageWatermarkOption watermarkImageOption = new ImageWatermarkOption();
	/**
	 * 水印图片文件。
	 *
	 * @since 1.0.0
	 */
	protected File watermarkImage;
	/**
	 * 裁剪类型。
	 *
	 * @since 1.0.0
	 */
	protected CropType cropType;
	/**
	 * 中心裁剪的目标宽度。
	 *
	 * @since 1.0.0
	 */
	protected Integer centerCropWidth;
	/**
	 * 中心裁剪的目标高度。
	 *
	 * @since 1.0.0
	 */
	protected Integer centerCropHeight;
	/**
	 * 偏移裁剪的上边偏移量。
	 *
	 * @since 1.0.0
	 */
	protected Integer topCropOffset;
	/**
	 * 偏移裁剪的下边偏移量。
	 *
	 * @since 1.0.0
	 */
	protected Integer bottomCropOffset;
	/**
	 * 偏移裁剪的左边偏移量。
	 *
	 * @since 1.0.0
	 */
	protected Integer leftCropOffset;
	/**
	 * 偏移裁剪的右边偏移量。
	 *
	 * @since 1.0.0
	 */
	protected Integer rightCropOffset;
	/**
	 * 矩形裁剪的左上角 X 坐标。
	 *
	 * @since 1.0.0
	 */
	protected Integer cropRectX;
	/**
	 * 矩形裁剪的左上角 Y 坐标。
	 *
	 * @since 1.0.0
	 */
	protected Integer cropRectY;
	/**
	 * 矩形裁剪的宽度。
	 *
	 * @since 1.0.0
	 */
	protected Integer cropRectWidth;
	/**
	 * 矩形裁剪的高度。
	 *
	 * @since 1.0.0
	 */
	protected Integer cropRectHeight;

	/**
	 * 获取裁剪类型。
	 *
	 * @return 裁剪类型，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable CropType getCropType() {
		return cropType;
	}

	/**
	 * 获取中心裁剪的目标宽度。
	 *
	 * @return 目标宽度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCenterCropWidth() {
		return centerCropWidth;
	}

	/**
	 * 获取中心裁剪的目标高度。
	 *
	 * @return 目标高度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCenterCropHeight() {
		return centerCropHeight;
	}

	/**
	 * 获取偏移裁剪的上边偏移量。
	 *
	 * @return 上边偏移量，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getTopCropOffset() {
		return topCropOffset;
	}

	/**
	 * 获取偏移裁剪的下边偏移量。
	 *
	 * @return 下边偏移量，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getBottomCropOffset() {
		return bottomCropOffset;
	}

	/**
	 * 获取偏移裁剪的左边偏移量。
	 *
	 * @return 左边偏移量，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getLeftCropOffset() {
		return leftCropOffset;
	}

	/**
	 * 获取偏移裁剪的右边偏移量。
	 *
	 * @return 右边偏移量，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getRightCropOffset() {
		return rightCropOffset;
	}

	/**
	 * 获取矩形裁剪的左上角 X 坐标。
	 *
	 * @return X 坐标，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCropRectX() {
		return cropRectX;
	}

	/**
	 * 获取矩形裁剪的左上角 Y 坐标。
	 *
	 * @return Y 坐标，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCropRectY() {
		return cropRectY;
	}

	/**
	 * 获取矩形裁剪的宽度。
	 *
	 * @return 宽度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCropRectWidth() {
		return cropRectWidth;
	}

	/**
	 * 获取矩形裁剪的高度。
	 *
	 * @return 高度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getCropRectHeight() {
		return cropRectHeight;
	}

	/**
	 * 受保护的构造方法，避免直接实例化。
	 *
	 * @since 1.0.0
	 */
	protected ImageOperation() {
	}

	/**
	 * 获取水印方向（当未设置具体坐标时生效）。
	 *
	 * @return 水印方向
	 * @since 1.0.0
	 */
	public @Nullable WatermarkDirection getWatermarkDirection() {
		return watermarkDirection;
	}

	/**
	 * 获取水印位置的 X 坐标（当设置了具体坐标时生效）。
	 *
	 * @return X 坐标，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getWatermarkX() {
		return watermarkX;
	}

	/**
	 * 获取水印位置的 Y 坐标（当设置了具体坐标时生效）。
	 *
	 * @return Y 坐标，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getWatermarkY() {
		return watermarkY;
	}

	/**
	 * 获取目标宽度（当按宽度缩放或强制缩放时设置）。
	 *
	 * @return 目标宽度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getTargetWidth() {
		return targetWidth;
	}

	/**
	 * 获取缩放比例。
	 *
	 * @return 缩放比例，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getScaleRatio() {
		return scaleRatio;
	}

	/**
	 * 获取目标高度（当按高度缩放或强制缩放时设置）。
	 *
	 * @return 目标高度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Integer getTargetHeight() {
		return targetHeight;
	}

	/**
	 * 是否进行强制缩放（忽略比例，直接缩放到指定宽高）。
	 *
	 * @return {@code true} 表示强制缩放，{@code false} 表示保持比例缩放
	 * @since 1.0.0
	 */
	public boolean isForceScale() {
		return forceScale;
	}

	/**
	 * 获取图片水印的配置选项。
	 *
	 * @return 图片水印配置
	 * @since 1.0.0
	 */
	public ImageWatermarkOption getWatermarkImageOption() {
		return watermarkImageOption;
	}

	/**
	 * 获取图片水印文件。
	 *
	 * @return 水印图片文件，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable File getWatermarkImage() {
		return watermarkImage;
	}

	/**
	 * {@link ImageOperation} 的构建器，提供链式 API 设置各参数。
	 *
	 * @author pangju666
	 * @since 1.0.0
	 */
	public static abstract class ImageOperationBuilder<T extends ImageOperationBuilder<T, V>, V extends ImageOperation> {
		/**
		 * 目标操作对象实例，供构建器写入配置。
		 *
		 * @since 1.0.0
		 */
		protected final V imageOperation;

		protected ImageOperationBuilder(V imageOperation) {
			this.imageOperation = imageOperation;
		}

		/**
		 * 返回构建器自身，用于链式调用。
		 *
		 * @return 构建器自身
		 * @since 1.0.0
		 */
		@SuppressWarnings("unchecked")
		protected T self() {
			return (T) this;
		}

		/**
		 * 以图片中心为基准进行裁剪。
		 *
		 * <p>参数校验规则：如果 {@code width} 或 {@code height} 为空或非正数，则不设置。</p>
		 *
		 * @param width  目标宽度
		 * @param height 目标高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T cropByCenter(Integer width, Integer height) {
			if (ObjectUtils.allNotNull(width, height) && width > 0 && height > 0) {
				this.imageOperation.centerCropWidth = width;
				this.imageOperation.centerCropHeight = height;
				this.imageOperation.cropType = CropType.CENTER;
			}
			return self();
		}

		/**
		 * 根据四边偏移量进行裁剪。
		 *
		 * <p>参数校验规则：如果任一偏移量为空或非正数，则不设置。</p>
		 *
		 * @param topOffset    上边偏移量
		 * @param bottomOffset 下边偏移量
		 * @param leftOffset   左边偏移量
		 * @param rightOffset  右边偏移量
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T cropByOffset(Integer topOffset, Integer bottomOffset, Integer leftOffset, Integer rightOffset) {
			if (ObjectUtils.allNotNull(topOffset, bottomOffset, leftOffset, rightOffset) &&
				topOffset > 0 && bottomOffset > 0 && leftOffset > 0 && rightOffset > 0) {
				this.imageOperation.topCropOffset = topOffset;
				this.imageOperation.bottomCropOffset = bottomOffset;
				this.imageOperation.leftCropOffset = leftOffset;
				this.imageOperation.rightCropOffset = rightOffset;
				this.imageOperation.cropType = CropType.OFFSET;
			}
			return self();
		}

		/**
		 * 按给定矩形区域进行裁剪。
		 *
		 * <p>参数校验规则：如果 {@code x}、{@code y}、{@code width} 或 {@code height} 为空或非正数，则不设置。</p>
		 *
		 * @param x      左上角 X 坐标
		 * @param y      左上角 Y 坐标
		 * @param width  矩形宽度
		 * @param height 矩形高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T cropByRect(Integer x, Integer y, Integer width, Integer height) {
			if (ObjectUtils.allNotNull(x, y, width, height) &&
				x > 0 && y > 0 && width > 0 && height > 0) {
				this.imageOperation.cropRectX = x;
				this.imageOperation.cropRectY = y;
				this.imageOperation.cropRectWidth = width;
				this.imageOperation.cropRectHeight = height;
				this.imageOperation.cropType = CropType.RECT;
			}
			return self();
		}

		/**
		 * 强制缩放到指定宽高（忽略比例）。
		 *
		 * <p>参数校验规则：如果 {@code width} 或 {@code height} 为 null，则不设置；设置后开启强制缩放。</p>
		 *
		 * @param width  目标宽度
		 * @param height 目标高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T forceScale(Integer width, Integer height) {
			if (ObjectUtils.allNotNull(width, height)) {
				this.imageOperation.targetWidth = width;
				this.imageOperation.targetHeight = height;
				this.imageOperation.forceScale = true;
			}
			return self();
		}

		/**
		 * 设置目标宽度和高度进行等比缩放。
		 *
		 * <p>参数校验规则：如果 {@code targetWidth} 为 null则不设置宽度；如果 {@code targetHeight} 为 null则不设置高度；
		 * 任一设置都会关闭强制缩放并清空比例。</p>
		 *
		 * @param targetWidth  目标宽度
		 * @param targetHeight 目标高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T scaleByRange(Integer targetWidth, Integer targetHeight) {
			if (Objects.nonNull(targetWidth)) {
				this.imageOperation.targetWidth = targetWidth;
				this.imageOperation.forceScale = false;
				this.imageOperation.scaleRatio = null;
			}
			if (Objects.nonNull(targetHeight)) {
				this.imageOperation.targetHeight = targetHeight;
				this.imageOperation.forceScale = false;
				this.imageOperation.scaleRatio = null;
			}
			return self();
		}

		/**
		 * 按比例缩放。
		 *
		 * <p>参数校验规则：如果 {@code ratio} 为 null 或 &le; 0，则不设置；设置后关闭强制缩放并清空宽高。</p>
		 *
		 * @param ratio 缩放比例（>0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T scale(Float ratio) {
			if (Objects.nonNull(ratio) && ratio > 0) {
				this.imageOperation.forceScale = false;
				this.imageOperation.scaleRatio = ratio;
				this.imageOperation.targetWidth = null;
				this.imageOperation.targetHeight = null;
			}
			return self();
		}

		/**
		 * 按目标宽度进行等比缩放。
		 *
		 * <p>参数校验规则：如果 {@code targetWidth} 为 null，则不设置；设置后关闭强制缩放并清空高度与比例。</p>
		 *
		 * @param targetWidth 目标宽度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T scaleByWidth(Integer targetWidth) {
			if (Objects.nonNull(targetWidth)) {
				this.imageOperation.forceScale = false;
				this.imageOperation.scaleRatio = null;
				this.imageOperation.targetWidth = targetWidth;
				this.imageOperation.targetHeight = null;
			}
			return self();
		}

		/**
		 * 按目标高度进行等比缩放。
		 *
		 * <p>参数校验规则：如果 {@code targetHeight} 为 null，则不设置；设置后关闭强制缩放并清空宽度与比例。</p>
		 *
		 * @param targetHeight 目标高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T scaleByHeight(Integer targetHeight) {
			if (Objects.nonNull(targetHeight)) {
				this.imageOperation.forceScale = false;
				this.imageOperation.scaleRatio = null;
				this.imageOperation.targetWidth = null;
				this.imageOperation.targetHeight = targetHeight;
			}
			return self();
		}

		/**
		 * 设置水印方向（如居中、左上等），与坐标互斥。
		 *
		 * <p>参数校验规则：如果 {@code direction} 为 null，则不设置；设置后清空坐标（X/Y）。</p>
		 *
		 * @param direction 水印方向
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkDirection(WatermarkDirection direction) {
			if (Objects.nonNull(direction)) {
				this.imageOperation.watermarkX = null;
				this.imageOperation.watermarkY = null;
				this.imageOperation.watermarkDirection = direction;
			}
			return self();
		}

		/**
		 * 设置水印的具体坐标位置（与方向互斥）。
		 *
		 * <p>参数校验规则：如果 {@code x} 或 {@code y} 为 null，或 ≤ 0，则不设置；设置后清空方向。</p>
		 *
		 * @param x X 坐标
		 * @param y Y 坐标
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkPosition(Integer x, Integer y) {
			if (ObjectUtils.allNotNull(x, y) && x > 0 && y > 0) {
				this.imageOperation.watermarkX = x;
				this.imageOperation.watermarkY = y;
				this.imageOperation.watermarkDirection = null;
			}
			return self();
		}

		/**
		 * 设置图片水印文件。
		 *
		 * @param watermarkImage 水印图片
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkImage(File watermarkImage) {
			this.imageOperation.watermarkImage = watermarkImage;
			return self();
		}

		/**
		 * 设置图片水印的缩放比例。
		 *
		 * <p>参数校验规则：如果 {@code ratio} 为 null 或者 &le; 0，则不设置。</p>
		 *
		 * @param ratio 缩放比例
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkImageScale(Float ratio) {
			if (Objects.nonNull(ratio)) {
				this.imageOperation.watermarkImageOption.setScale(ratio);
			}
			return self();
		}

		/**
		 * 设置图片水印的透明度。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null 或者不在 [0,1] 的范围内则不设置。</p>
		 *
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkImageOpacity(Float opacity) {
			if (Objects.nonNull(opacity)) {
				this.imageOperation.watermarkImageOption.setOpacity(opacity);
			}
			return self();
		}

		/**
		 * 限制图片水印的宽度范围。
		 *
		 * <p>参数校验规则：如果 {@code minWidth} 或 {@code maxWidth} 为 null，则不设置。</p>
		 *
		 * @param minWidth 最小宽度
		 * @param maxWidth 最大宽度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkImageWidthRange(Integer minWidth, Integer maxWidth) {
			if (ObjectUtils.allNotNull(minWidth, maxWidth)) {
				this.imageOperation.watermarkImageOption.setWidthRange(minWidth, maxWidth);
			}
			return self();
		}

		/**
		 * 限制图片水印的高度范围。
		 *
		 * <p>参数校验规则：如果 {@code minHeight} 或 {@code maxHeight} 为 null，则不设置。</p>
		 *
		 * @param minHeight 最小高度
		 * @param maxHeight 最大高度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public T watermarkImageHeightRange(Integer minHeight, Integer maxHeight) {
			if (ObjectUtils.allNotNull(minHeight, maxHeight)) {
				this.imageOperation.watermarkImageOption.setHeightRange(minHeight, maxHeight);
			}
			return self();
		}

		/**
		 * 构建并返回 {@link ImageOperation} 对象。
		 *
		 * @return 构建完成的配置对象
		 * @since 1.0.0
		 */
		public V build() {
			return this.imageOperation;
		}
	}
}
