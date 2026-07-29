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
