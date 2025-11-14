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
import io.github.pangju666.commons.image.model.TextWatermarkOption;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Objects;

 /**
  * 图像处理操作配置模型，支持缩放以及图片/文字水印。
  * 使用构建器模式设置参数，供图像处理组件读取并执行。
  *
  * <p><strong>使用说明</strong>：通过构建器链式设置参数；不满足校验规则的参数将被忽略。</p>
  * <p><strong>互斥规则</strong>：图片水印与文字水印互斥；水印方向与坐标互斥；设置其中之一会清空另一种配置。</p>
  * <p><strong>定位规则</strong>：可使用 {@code watermarkDirection} 或 {@code watermarkPosition(x,y)}；坐标需为正数。</p>
  * <p><strong>缩放规则</strong>：{@code forceScale(width,height)} 强制缩放到指定尺寸；比例/宽高/按宽/按高缩放为等比，并会关闭强制缩放且清空其它尺寸/比例。</p>
  * <p><strong>透明度范围</strong>：取值区间 [0,1]；整体透明度以及水印透明度遵循该范围。</p>
  * <p><strong>执行顺序</strong>：缩放 → 调整不透明度 → 文字水印 → 图片水印。</p>
  *
  * @since 1.0.0
  * @author pangju666
  */
 public class ImageOperation {
    /** 
	 * 水印方向（当未设置具体坐标时生效）。
	 * 
     * @since 1.0.0 
	 */
	private WatermarkDirection watermarkDirection;
    /** 
	 * 水印位置的 X 坐标（与方向互斥）。
	 * 
     * @since 1.0.0 
	 */
	private Integer watermarkX;
    /** 
	 * 水印位置的 Y 坐标（与方向互斥）。
	 * 
     * @since 1.0.0 
	 */
	private Integer watermarkY;
    /** 
	 * 目标宽度（按宽度缩放或强制缩放时设置）。
     * 
	 * @since 1.0.0
	 */
	private Integer targetWidth;
    /** 缩放比例（>0）。
     * 
	 * @since 1.0.0 
	 */
	private Float scaleRatio;
    /** 
	 * 目标高度（按高度缩放或强制缩放时设置）。
     * 
	 * @since 1.0.0
	 */
	private Integer targetHeight;
    /** 
	 * 是否强制缩放（忽略比例，直接缩放到指定宽高）。
	 * 
     * @since 1.0.0
	 */
	private boolean forceScale = false;
    /**
	 * 图片水印配置（与文字水印互斥）。
	 * 
     * @since 1.0.0 
	 */
	private ImageWatermarkOption imageWatermarkOption;
    /**
	 * 文字水印配置（与图片水印互斥）。
	 * 
     * @since 1.0.0 
	 */
	private TextWatermarkOption textWatermarkOption;
    /** 
	 * 水印图片文件（与文字水印互斥）。
	 * 
     * @since 1.0.0 
	 */
	private File watermarkFile;
    /** 
	 * 水印文本（与图片水印互斥）。
	 * 
     * @since 1.0.0
	 */
	private String watermarkText;
    /** 
	 * 整体透明度（0-1，非必须）。
	 * 
     * @since 1.0.0 
	 */
	private Float opacity;

	protected ImageOperation() {
	}

	/**
	 * 创建并返回 {@code ImageOperationBuilder} 构建器。
	 *
	 * @return 构建器实例
	 * @since 1.0.0
	 */
	public static ImageOperationBuilder builder() {
		return new ImageOperationBuilder();
	}

	/**
	 * 获取水印方向（当未设置具体坐标时生效）。
	 *
	 * @return 水印方向，未设置则为 {@code null}
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
	 * @return 图片水印配置，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable ImageWatermarkOption getImageWatermarkOption() {
		return imageWatermarkOption;
	}

	/**
	 * 获取文字水印的配置选项。
	 *
	 * @return 文字水印配置，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable TextWatermarkOption getTextWatermarkOption() {
		return textWatermarkOption;
	}

	/**
	 * 获取图片水印文件。
	 *
	 * @return 水印图片文件，未设置或使用文字水印时为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable File getWatermarkFile() {
		return watermarkFile;
	}

	/**
	 * 获取文字水印文本。
	 *
	 * @return 水印文本，未设置或使用图片水印时为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 获取整体透明度（0-1，非必须）。
	 *
	 * @return 透明度，未设置则为 {@code null}
	 * @since 1.0.0
	 */
	public @Nullable Float getOpacity() {
		return opacity;
	}

    /**
     * {@link ImageOperation} 的构建器，提供链式 API 设置各参数。
	 * 
	 * @author pangju666
     * @since 1.0.0
     */
    public static class ImageOperationBuilder {
		private final ImageOperation imageOperation = new ImageOperation();

		/**
		 * 设置整体透明度（0-1），仅在提供有效值时生效。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null或不在 (0,1) 范围，则不设置。</p>
		 * 
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder opacity(Float opacity) {
			if (Objects.nonNull(opacity) && opacity > 0 && opacity < 1) {
				imageOperation.opacity = opacity;
			}
			return this;
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
		public ImageOperationBuilder forceScale(Integer width, Integer height) {
			if (ObjectUtils.allNotNull(width, height)) {
				imageOperation.targetWidth = width;
				imageOperation.targetHeight = height;
				imageOperation.forceScale = true;
			}
			return this;
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
		public ImageOperationBuilder scale(Integer targetWidth, Integer targetHeight) {
			if (Objects.nonNull(targetWidth)) {
				imageOperation.targetWidth = targetWidth;
				imageOperation.forceScale = false;
				imageOperation.scaleRatio = null;
			}
			if (Objects.nonNull(targetHeight)) {
				imageOperation.targetHeight = targetHeight;
				imageOperation.forceScale = false;
				imageOperation.scaleRatio = null;
			}
			return this;
		}

		/**
		 * 按比例缩放。
		 *
		 * <p>参数校验规则：如果 {@code ratio} 为 null或 ≤ 0，则不设置；设置后关闭强制缩放并清空宽高。</p>
		 * 
		 * @param ratio 缩放比例（>0）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder scaleByRatio(Float ratio) {
			if (Objects.nonNull(ratio) && ratio > 0) {
				imageOperation.forceScale = false;
				imageOperation.scaleRatio = ratio;
				imageOperation.targetWidth = null;
				imageOperation.targetHeight = null;
			}
			return this;
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
		public ImageOperationBuilder scaleByWidth(Integer targetWidth) {
			if (Objects.nonNull(targetWidth)) {
				imageOperation.forceScale = false;
				imageOperation.scaleRatio = null;
				imageOperation.targetWidth = targetWidth;
				imageOperation.targetHeight = null;
			}
			return this;
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
		public ImageOperationBuilder scaleByHeight(Integer targetHeight) {
			if (Objects.nonNull(targetHeight)) {
				imageOperation.forceScale = false;
				imageOperation.scaleRatio = null;
				imageOperation.targetWidth = null;
				imageOperation.targetHeight = targetHeight;
			}
			return this;
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
		public ImageOperationBuilder watermarkDirection(WatermarkDirection direction) {
			if (Objects.nonNull(direction)) {
				imageOperation.watermarkX = null;
				imageOperation.watermarkY = null;
				imageOperation.watermarkDirection = direction;
			}
			return this;
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
		public ImageOperationBuilder watermarkPosition(Integer x, Integer y) {
			if (ObjectUtils.allNotNull(x, y) && x > 0 && y > 0) {
				imageOperation.watermarkX = x;
				imageOperation.watermarkY = y;
				imageOperation.watermarkDirection = null;
			}
			return this;
		}

		/**
		 * 使用图片水印文件并初始化相关选项（与文字水印互斥）。
		 *
		 * <p>参数校验规则：如果 {@code watermarkFile} 为 null或文件不存在，则不设置；设置后清空文字水印内容与配置。</p>
		 * 
		 * @param watermarkFile 水印图片文件
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder imageWatermark(File watermarkFile) {
			if (FileUtils.existFile(watermarkFile)) {
				imageOperation.watermarkFile = watermarkFile;
				if (Objects.isNull(imageOperation.imageWatermarkOption)) {
					imageOperation.imageWatermarkOption = new ImageWatermarkOption();
				}
				imageOperation.watermarkText = null;
				imageOperation.textWatermarkOption = null;
			}
			return this;
		}

		/**
		 * 设置图片水印的缩放比例。
		 *
		 * <p>参数校验规则：如果 {@code ratio} 为 null，则不设置；若图片水印配置不存在将自动初始化。</p>
		 * 
		 * @param ratio 缩放比例
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder imageWatermarkScaleRatio(Float ratio) {
			if (Objects.nonNull(ratio)) {
				if (Objects.isNull(imageOperation.imageWatermarkOption)) {
					imageOperation.imageWatermarkOption = new ImageWatermarkOption();
				}
				imageOperation.imageWatermarkOption.setScale(ratio);
			}
			return this;
		}

		/**
		 * 设置图片水印的透明度。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null，则不设置；取值范围 0-1。</p>
		 * 
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder imageWatermarkOpacity(Float opacity) {
			if (Objects.nonNull(opacity)) {
				if (Objects.isNull(imageOperation.imageWatermarkOption)) {
					imageOperation.imageWatermarkOption = new ImageWatermarkOption();
				}
				imageOperation.imageWatermarkOption.setOpacity(opacity);
			}
			return this;
		}

		/**
		 * 限制图片水印的宽度范围。
		 *
		 * <p>参数校验规则：如果 {@code minWidth} 或 {@code maxWidth} 为 null，则不设置}。</p>
		 * 
		 * @param minWidth 最小宽度
		 * @param maxWidth 最大宽度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder imageWatermarkWidthRange(Integer minWidth, Integer maxWidth) {
			if (ObjectUtils.allNotNull(minWidth, maxWidth)) {
				if (Objects.isNull(imageOperation.imageWatermarkOption)) {
					imageOperation.imageWatermarkOption = new ImageWatermarkOption();
				}
				imageOperation.imageWatermarkOption.setWidthRange(minWidth, maxWidth);
			}
			return this;
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
		public ImageOperationBuilder imageWatermarkHeightRange(Integer minHeight, Integer maxHeight) {
			if (ObjectUtils.allNotNull(minHeight, maxHeight)) {
				if (Objects.isNull(imageOperation.imageWatermarkOption)) {
					imageOperation.imageWatermarkOption = new ImageWatermarkOption();
				}
				imageOperation.imageWatermarkOption.setHeightRange(minHeight, maxHeight);
			}
			return this;
		}

		/**
		 * 使用文字水印并初始化相关选项（与图片水印互斥）。
		 *
		 * <p>参数校验规则：如果 {@code watermarkText} 为空白，则不设置；设置后清空图片水印内容与配置。</p>
		 * 
		 * @param watermarkText 水印文本
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermark(String watermarkText) {
			if (StringUtils.isNotBlank(watermarkText)) {
				imageOperation.watermarkText = watermarkText;
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.watermarkFile = null;
				imageOperation.imageWatermarkOption = null;
			}
			return this;
		}

		/**
		 * 设置文字水印的透明度。
		 *
		 * <p>参数校验规则：如果 {@code opacity} 为 null，则不设置；取值范围 0-1。</p>
		 * 
		 * @param opacity 透明度（0-1）
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkOpacity(Float opacity) {
			if (Objects.nonNull(opacity)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setOpacity(opacity);
			}
			return this;
		}

		/**
		 * 设置文字水印的字体。
		 *
		 * <p>参数校验规则：如果 {@code font} 为 null，则不设置。</p>
		 * 
		 * @param font 字体
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkFont(Font font) {
			if (Objects.nonNull(font)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setFont(font);
			}
			return this;
		}

		/**
		 * 设置文字水印的填充颜色。
		 *
		 * <p>参数校验规则：如果 {@code fillColor} 为 null，则不设置。</p>
		 * 
		 * @param fillColor 填充颜色
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkFillColor(Color fillColor) {
			if (Objects.nonNull(fillColor)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setFillColor(fillColor);
			}
			return this;
		}

		/**
		 * 设置文字水印的描边颜色，并开启描边。
		 *
		 * <p>参数校验规则：如果 {@code strokeColor} 为 null，则不设置；设置后自动开启描边。</p>
		 * 
		 * @param strokeColor 描边颜色
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkStrokeColor(Color strokeColor) {
			if (Objects.nonNull(strokeColor)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setStrokeColor(strokeColor);
				imageOperation.textWatermarkOption.setStroke(true);
			}
			return this;
		}

		/**
		 * 设置文字水印的描边宽度，并开启描边。
		 *
		 * <p>参数校验规则：如果 {@code strokeWidth} 为 null 或 ≤ 0，则不设置；设置后自动开启描边。</p>
		 * 
		 * @param strokeWidth 描边宽度
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkStrokeWidth(Float strokeWidth) {
			if (Objects.nonNull(strokeWidth)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setStrokeWidth(strokeWidth);
				imageOperation.textWatermarkOption.setStroke(true);
			}
			return this;
		}

		/**
		 * 设置是否启用文字水印的描边效果。
		 *
		 * <p>参数校验规则：如果 {@code stroke} 为 null，则不设置。</p>
		 * 
		 * @param stroke 是否描边
		 * @return 构建器本身
		 * @since 1.0.0
		 */
		public ImageOperationBuilder textWatermarkStroke(Boolean stroke) {
			if (Objects.nonNull(stroke)) {
				if (Objects.isNull(imageOperation.textWatermarkOption)) {
					imageOperation.textWatermarkOption = new TextWatermarkOption();
				}
				imageOperation.textWatermarkOption.setStroke(stroke);
			}
			return this;
		}

		/**
		 * 构建并返回 {@link ImageOperation} 对象。
		 *
		 * @return 构建完成的配置对象
		 * @since 1.0.0
		 */
		public ImageOperation build() {
			return imageOperation;
		}
	}
}
