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

import io.github.pangju666.framework.boot.image.enums.TileMode;
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
 * @author pangju666
 * @since 2.1.0
 */
public final class SizeTileOptions extends TileOptions {
	/**
	 * 瓦片宽度，默认为256
	 *
	 * @since 2.1.0
	 */
	private int tileWidth = 256;
	/**
	 * 瓦片高度，默认为256
	 *
	 * @since 2.1.0
	 */
	private int tileHeight = 256;
	/**
	 * 切分模式，默认为金字塔模式
	 *
	 * @since 2.1.0
	 */
	private TileMode mode = TileMode.PYRAMID;

	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public SizeTileOptions() {
		super();
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param options 源选项对象
	 * @since 2.1.0
	 */
	public SizeTileOptions(@Nullable TileOptions options) {
		super(options);

		if (Objects.nonNull(options) && options instanceof SizeTileOptions sizeTileOptions) {
			this.tileWidth = sizeTileOptions.tileWidth;
			this.tileHeight = sizeTileOptions.tileHeight;
			this.mode = sizeTileOptions.mode;
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
	 *
	 * @param mode 切分模式
	 * @since 2.1.0
	 */
	public void setMode(@Nullable TileMode mode) {
		if (Objects.nonNull(mode)) {
			this.mode = mode;
		}
	}
}
