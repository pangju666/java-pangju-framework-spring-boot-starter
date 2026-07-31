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

import org.springframework.util.Assert;

/**
 * 按网格切分瓦片选项。
 * <p>
 * 继承自{@link TileOptions}，增加了按指定行列数进行均匀切分的配置选项。
 * </p>
 * <p>
 * 将图像按照指定的行数和列数切分成均匀的瓦片，瓦片尺寸根据图像实际尺寸和行列数自动计算。
 * </p>
 *
 * <p><strong>切分特点</strong></p>
 * <ul>
 *   <li>瓦片尺寸根据图像实际尺寸和行列数自动计算</li>
 *   <li>画布会扩展到瓦片尺寸的整数倍，使用背景色填充</li>
 *   <li>瓦片尺寸可能不完全相等，最后一行/列可能较小</li>
 *   <li>自动处理EXIF方向，确保瓦片方向正确</li>
 * </ul>
 *
 * <p><strong>使用场景</strong></p>
 * <ul>
 *   <li>需要将图像均匀分割为指定行列数的场景</li>
 *   <li>不需要精确控制瓦片尺寸的场景</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 * @see TileOptions
 */
public final class GridTileOptions extends TileOptions {
	/**
	 * 行数。
	 * <p>
	 * 图像切分的行数，必须大于0。
	 * 瓦片高度根据图像实际高度和行数自动计算。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private final int rows;
	/**
	 * 列数。
	 * <p>
	 * 图像切分的列数，必须大于0。
	 * 瓦片宽度根据图像实际宽度和列数自动计算。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	private final int cols;

	/**
	 * 构造函数。
	 * <p>
	 * 创建按网格切分瓦片的选项，指定行数和列数。
	 * </p>
	 *
	 * @param rows 行数，必须大于0
	 * @param cols 列数，必须大于0
	 * @throws IllegalArgumentException 如果rows或cols不大于0
	 * @since 2.1.0
	 */
	public GridTileOptions(int rows, int cols) {
		Assert.isTrue(rows > 0 && cols > 0, "rows 和 cols 必须大于 0");

		this.cols = cols;
		this.rows = rows;
	}

	/**
	 * 获取行数。
	 *
	 * @return 行数
	 * @since 2.1.0
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * 获取列数。
	 *
	 * @return 列数
	 * @since 2.1.0
	 */
	public int getCols() {
		return cols;
	}
}
