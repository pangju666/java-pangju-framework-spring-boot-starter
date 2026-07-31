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

package io.github.pangju666.framework.boot.image.model.tile;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 按尺寸切分瓦片选项。
 * <p>
 * 继承自{@link TileOptions}，增加了按指定瓦片尺寸进行切分的配置选项。
 * </p>
 * <p>
 * 支持单一模式和金字塔模式两种切分方式。
 * </p>
 *
 * <p><strong>切分模式</strong></p>
 * <ul>
 *   <li>单一模式：仅生成单一分辨率的瓦片，适用于简单的瓦片需求</li>
 *   <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景</li>
 * </ul>
 *
 * <p><strong>瓦片布局</strong></p>
 * <ul>
 *   <li>DeepZoom：瓦片文件命名为x_y格式，所有文件在同一目录</li>
 *   <li>XYZ：瓦片文件按x坐标组织到子目录，文件名为y坐标</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 * @see TileOptions
 * @see TileMode
 * @see TileLayout
 */
public final class SizeTileOptions extends TileOptions {
	/**
	 * 瓦片宽度。
	 * <p>
	 * 瓦片的宽度（像素），用于按尺寸切分图像。
	 * 默认为256像素。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private int tileWidth = 256;
	/**
	 * 瓦片高度。
	 * <p>
	 * 瓦片的高度（像素），用于按尺寸切分图像。
	 * 默认为256像素。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private int tileHeight = 256;
	/**
	 * 切分模式。
	 * <p>
	 * 指定瓦片切分的模式，支持单一模式和金字塔模式。
	 * 默认为单一模式。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private TileMode mode = TileMode.SINGLE;
	/**
	 * 瓦片布局方式。
	 * <p>
	 * 指定瓦片文件的布局格式，支持DeepZoom和XYZ两种格式。
	 * 默认为XYZ格式。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private TileLayout layout = TileLayout.XYZ;

	/**
	 * 默认构造函数。
	 * <p>
	 * 使用默认值初始化选项：瓦片尺寸为256x256，切分模式为金字塔模式，布局方式为XYZ。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public SizeTileOptions() {
		super();
	}

	/**
	 * 拷贝构造函数。
	 * <p>
	 * 从源选项对象复制配置。
	 * 如果源对象为null或不是SizeTileOptions类型，则使用默认值。
	 * </p>
	 *
	 * @param options 源选项对象，可以为null
	 * @since 2.1.0
	 */
	public SizeTileOptions(@Nullable TileOptions options) {
		super(options);

		if (Objects.nonNull(options) && options instanceof SizeTileOptions sizeTileOptions) {
			this.tileWidth = sizeTileOptions.tileWidth;
			this.tileHeight = sizeTileOptions.tileHeight;
			this.mode = sizeTileOptions.mode;
			this.layout = sizeTileOptions.layout;
		}
	}

	/**
	 * 获取瓦片宽度。
	 *
	 * @return 瓦片宽度（像素）
	 * @since 2.1.0
	 */
	public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * 设置瓦片宽度。
	 * <p>
	 * 如果tileWidth小于等于0，则不修改瓦片宽度。
	 * </p>
	 *
	 * @param tileWidth 瓦片宽度（像素），必须大于0
	 * @since 2.1.0
	 */
	public void setTileWidth(int tileWidth) {
		if (tileWidth > 0) {
			this.tileWidth = tileWidth;
		}
	}

	/**
	 * 获取瓦片高度。
	 *
	 * @return 瓦片高度（像素）
	 * @since 2.1.0
	 */
	public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * 设置瓦片高度。
	 * <p>
	 * 如果tileHeight小于等于0，则不修改瓦片高度。
	 * </p>
	 *
	 * @param tileHeight 瓦片高度（像素），必须大于0
	 * @since 2.1.0
	 */
	public void setTileHeight(int tileHeight) {
		if (tileHeight > 0) {
			this.tileHeight = tileHeight;
		}
	}

	/**
	 * 获取切分模式。
	 *
	 * @return 切分模式
	 * @since 2.1.0
	 */
	public TileMode getMode() {
		return mode;
	}

	/**
	 * 设置切分模式。
	 * <p>
	 * 如果mode为null，则不修改切分模式。
	 * </p>
	 *
	 * @param mode 切分模式，可以为null
	 * @since 2.1.0
	 */
	public void setMode(@Nullable TileMode mode) {
		if (Objects.nonNull(mode)) {
			this.mode = mode;
		}
	}

	/**
	 * 获取瓦片布局方式。
	 *
	 * @return 瓦片布局方式
	 * @since 2.1.0
	 */
	public TileLayout getLayout() {
		return layout;
	}

	/**
	 * 设置瓦片布局方式。
	 * <p>
	 * 如果layout为null，则不修改瓦片布局方式。
	 * </p>
	 *
	 * @param layout 瓦片布局方式，可以为null
	 * @since 2.1.0
	 */
	public void setLayout(@Nullable TileLayout layout) {
		if (Objects.nonNull(layout)) {
			this.layout = layout;
		}
	}

	/**
	 * 瓦片布局枚举
	 * <p>
	 * 定义了图像瓦片切分后的文件存储布局方式。
	 * 瓦片切分通常用于地图服务、深度缩放图像等场景，将大图切分成多个小瓦片以便于按需加载。
	 * </p>
	 * <p>
	 * 支持的布局方式：
	 * <ul>
	 *     <li>DEEP_ZOOM - DeepZoom风格布局，文件名格式为z/x_y.xxx</li>
	 *     <li>XYZ - XYZ地图风格布局，文件名格式为z/x/y.xxx</li>
	 * </ul>
	 * </p>
	 *
	 * @author pangju666
	 * @since 2.1.0
	 */
	public enum TileLayout {
		/**
		 * DeepZoom风格：z/x_y.xxx
		 *
		 * @since 2.1.0
		 */
		DEEP_ZOOM,
		/**
		 * XYZ地图风格：z/x/y.xxx
		 *
		 * @since 2.1.0
		 */
		XYZ
	}

	/**
	 * 瓦片模式枚举
	 * <p>
	 * 定义了图像瓦片切分的处理模式。
	 * 瓦片切分用于将大图分解为多个小瓦片，以便于在不同场景下使用。
	 * </p>
	 * <p>
	 * 支持的模式：
	 * <ul>
	 *     <li>PYRAMID - 金字塔模式，生成多级分辨率的瓦片，适用于深度缩放场景</li>
	 *     <li>SINGLE - 单一模式，仅生成单一分辨率的瓦片</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 金字塔模式会生成从原始分辨率到最低分辨率的多个层级，每个层级都是前一层级的降采样版本，
	 * 常用于地图服务、医学影像等需要平滑缩放的场景。
	 * </p>
	 *
	 * @author pangju666
	 * @since 2.1.0
	 */
	public enum TileMode {
		/**
		 * 金字塔模式：生成多级分辨率的瓦片
		 *
		 * @since 2.1.0
		 */
		PYRAMID,
		/**
		 * 单一模式：仅生成单一分辨率的瓦片
		 *
		 * @since 2.1.0
		 */
		SINGLE
	}
}
