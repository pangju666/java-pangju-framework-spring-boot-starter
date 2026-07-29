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

package io.github.pangju666.framework.boot.image.enums;

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
