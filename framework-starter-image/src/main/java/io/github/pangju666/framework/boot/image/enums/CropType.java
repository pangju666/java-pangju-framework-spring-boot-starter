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

package io.github.pangju666.framework.boot.image.enums;

/**
 * 裁剪类型。
 *
 * <ul>
 *   <li>{@code CENTER}：以图片中心为基准按目标尺寸进行裁剪。</li>
 *   <li>{@code OFFSET}：根据偏移量（起点坐标）与目标尺寸进行裁剪。</li>
 *   <li>{@code RECT}：按给定矩形区域（左上角坐标与宽高）进行裁剪。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum CropType {
	/**
	 * 中心裁剪。
	 *
	 * @since 1.0.0
	 */
	CENTER,
	/**
	 * 偏移裁剪（按起点坐标与尺寸）。
	 *
	 * @since 1.0.0
	 */
	OFFSET,
	/**
	 * 矩形区域裁剪。
	 *
	 * @since 1.0.0
	 */
	RECT
}
