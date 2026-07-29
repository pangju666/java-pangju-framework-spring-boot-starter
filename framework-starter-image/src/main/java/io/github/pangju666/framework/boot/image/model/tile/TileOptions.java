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

import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.framework.boot.image.enums.TileLayout;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

/**
 * 瓦片切分选项密封类。
 * <p>
 * 定义图像瓦片切分的通用配置选项，包括背景色、输出格式和布局方式。
 * </p>
 * <p>
 * 使用sealed class限制继承，仅允许{@link GridTileOptions}和{@link SizeTileOptions}作为直接子类。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public sealed abstract class TileOptions permits GridTileOptions, SizeTileOptions {
	/**
	 * 背景色，默认为透明
	 *
	 * @since 2.1.0
	 */
	protected String backgroundColor = "transparent";
	/**
	 * 输出格式，默认为png
	 *
	 * @since 2.1.0
	 */
	protected String outputFormat = "png";
	/**
	 * 瓦片布局方式，默认为DeepZoom
	 *
	 * @since 2.1.0
	 */
	protected TileLayout layout = TileLayout.DEEP_ZOOM;

	/**
	 * 默认构造函数。
	 *
	 * @since 2.1.0
	 */
	public TileOptions() {
	}

	/**
	 * 拷贝构造函数。
	 *
	 * @param options 源选项对象
	 * @since 2.1.0
	 */
	public TileOptions(@Nullable TileOptions options) {
		if (Objects.nonNull(options)) {
			this.backgroundColor = options.backgroundColor;
			this.outputFormat = options.outputFormat;
			this.layout = options.layout;
		}
	}

	/**
	 * 获取背景色。
	 *
	 * @return 背景色字符串，如"transparent"、"#ffffff"等
	 * @since 2.1.0
	 */
	public String getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * 设置背景色。
	 *
	 * @param color 颜色对象
	 * @since 2.1.0
	 */
	public void setBackgroundColor(@Nullable Color color) {
		if (Objects.nonNull(color)) {
			this.backgroundColor = ImageUtils.toHexColorWithAlpha(color);
		}
	}

	/**
	 * 设置背景色。
	 *
	 * @param backgroundColor 背景色字符串
	 * @since 2.1.0
	 */
	public void setBackgroundColor(@Nullable String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * 获取输出格式。
	 *
	 * @return 输出格式，如"png"、"jpg"等
	 * @since 2.1.0
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * 设置输出格式。
	 *
	 * @param outputFormat 输出格式字符串
	 * @since 2.1.0
	 */
	public void setOutputFormat(@Nullable String outputFormat) {
		if (StringUtils.isNotBlank(outputFormat)) {
			this.outputFormat = outputFormat;
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
	 *
	 * @param layout 瓦片布局方式
	 * @since 2.1.0
	 */
	public void setLayout(@Nullable TileLayout layout) {
		if (Objects.nonNull(layout)) {
			this.layout = layout;
		}
	}
}
