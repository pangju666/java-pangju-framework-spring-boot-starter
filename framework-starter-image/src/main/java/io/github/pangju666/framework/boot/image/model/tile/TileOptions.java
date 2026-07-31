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
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

/**
 * 瓦片切分选项密封类。
 * <p>
 * 定义图像瓦片切分的通用配置选项，包括背景色、输出格式。
 * </p>
 * <p>
 * 使用sealed class限制继承，仅允许{@link GridTileOptions}和{@link SizeTileOptions}作为直接子类。
 * </p>
 *
 * <p><strong>通用配置</strong></p>
 * <ul>
 *   <li>背景色：用于填充画布扩展区域，默认为透明</li>
 *   <li>输出格式：瓦片文件的输出格式，默认为PNG</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public sealed abstract class TileOptions permits GridTileOptions, SizeTileOptions {
	/**
	 * 背景色。
	 * <p>
	 * 用于填充画布扩展区域，当图像尺寸不是瓦片尺寸的整数倍时使用。
	 * 支持颜色名称（如white、black）、十六进制颜色（如#ffffff、#000000）或透明（transparent）。
	 * 默认为透明。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String backgroundColor = "transparent";
	/**
	 * 输出格式。
	 * <p>
	 * 瓦片文件的输出格式，如png、jpg、webp等。
	 * 默认为PNG格式。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String outputFormat = "png";

	/**
	 * 默认构造函数。
	 * <p>
	 * 使用默认值初始化选项：背景色为透明，输出格式为PNG。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public TileOptions() {
	}

	/**
	 * 拷贝构造函数。
	 * <p>
	 * 从源选项对象复制配置，如果源对象为null则使用默认值。
	 * </p>
	 *
	 * @param options 源选项对象，可以为null
	 * @since 2.1.0
	 */
	public TileOptions(@Nullable TileOptions options) {
		if (Objects.nonNull(options)) {
			this.backgroundColor = options.backgroundColor;
			this.outputFormat = options.outputFormat;

		}
	}

	/**
	 * 获取背景色。
	 *
	 * @return 背景色字符串，如"transparent"、"#ffffff"、"white"等
	 * @since 2.1.0
	 */
	public String getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * 设置背景色。
	 * <p>
	 * 将Color对象转换为十六进制颜色字符串并设置。
	 * 如果color为null，则不修改背景色。
	 * </p>
	 *
	 * @param color 颜色对象，可以为null
	 * @since 2.1.0
	 */
	public void setBackgroundColor(@Nullable Color color) {
		if (Objects.nonNull(color)) {
			this.backgroundColor = ImageUtils.toHexColorWithAlpha(color);
		}
	}

	/**
	 * 设置背景色。
	 * <p>
	 * 支持颜色名称（如white、black）、十六进制颜色（如#ffffff、#000000）或透明（transparent）。
	 * 如果backgroundColor为null或空，则不修改背景色。
	 * </p>
	 *
	 * @param backgroundColor 背景色字符串，可以为null
	 * @since 2.1.0
	 */
	public void setBackgroundColor(@Nullable String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * 获取输出格式。
	 *
	 * @return 输出格式字符串，如"png"、"jpg"、"webp"等
	 * @since 2.1.0
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * 设置输出格式。
	 * <p>
	 * 设置瓦片文件的输出格式，如png、jpg、webp等。
	 * 输出格式会被自动转换为小写。
	 * 如果outputFormat为null或空，则不修改输出格式。
	 * </p>
	 *
	 * @param outputFormat 输出格式字符串，可以为null
	 * @since 2.1.0
	 */
	public void setOutputFormat(@Nullable String outputFormat) {
		if (StringUtils.isNotBlank(outputFormat)) {
			this.outputFormat = outputFormat.toLowerCase();
		}
	}
}
