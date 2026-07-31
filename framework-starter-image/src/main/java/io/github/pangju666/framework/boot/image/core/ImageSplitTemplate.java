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

package io.github.pangju666.framework.boot.image.core;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.model.tile.GridTileOptions;
import io.github.pangju666.framework.boot.image.model.tile.SizeTileOptions;

import java.io.File;

/**
 * 图像瓦片切分模板接口。
 * <p>
 * 定义了图像瓦片切分的标准方法，支持按网格和按尺寸两种切分方式。
 * </p>
 *
 * <p><strong>切分方式</strong></p>
 * <ul>
 *   <li>按网格切分：将图像按照指定的行数和列数切分成均匀的瓦片，瓦片尺寸自动计算</li>
 *   <li>按尺寸切分：将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式</li>
 * </ul>
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
 * <p><strong>使用场景</strong></p>
 * <ul>
 *   <li>地图瓦片生成：将大地图图像切分为瓦片以便于网络传输</li>
 *   <li>深度缩放：生成金字塔瓦片支持平滑的深度缩放体验</li>
 *   <li>图像分割：将大图像分割为多个小图像以便于并行处理</li>
 * </ul>
 *
 * <p><strong>实现说明</strong></p>
 * <ul>
 *   <li>具体实现类负责处理不同图像引擎的切分逻辑</li>
 *   <li>支持IOResource作为输入源，可以处理文件、流等多种资源类型</li>
 *   <li>支持失败时保留已生成的瓦片文件</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public interface ImageSplitTemplate {
	/**
	 * 按尺寸切分图像瓦片（失败时保留已生成文件）。
	 * <p>
	 * 将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式。
	 * 如果切分过程中出现异常，会保留已生成的瓦片文件。
	 * </p>
	 *
	 * <p><strong>切分模式</strong></p>
	 * <ul>
	 *   <li>单一模式：仅生成单一分辨率的瓦片，适用于简单的瓦片需求</li>
	 *   <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景</li>
	 * </ul>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>识别图像尺寸和方向信息</li>
	 *   <li>根据切分模式计算画布尺寸和层级数</li>
	 *   <li>执行切分操作</li>
	 *   <li>根据布局格式组织瓦片文件</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>画布会扩展到瓦片尺寸的整数倍，使用背景色填充</li>
	 *   <li>自动处理EXIF方向，确保瓦片方向正确</li>
	 *   <li>金字塔模式会生成多级目录，注意磁盘空间占用</li>
	 *   <li>失败时保留已生成的瓦片文件，便于调试和恢复</li>
	 * </ul>
	 *
	 * @param resource  图像资源，不能为null
	 * @param outputDir 输出目录，不能为null
	 * @param options   瓦片切分选项，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像读取失败时抛出
	 * @throws ImageOperationException      图像操作异常，当切分操作失败时抛出
	 * @see SizeTileOptions
	 * @see #splitTilesBySize(IOResource, File, SizeTileOptions, boolean)
	 * @since 2.1.0
	 */
	default void splitTilesBySize(IOResource resource, File outputDir, SizeTileOptions options)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException {
		splitTilesBySize(resource, outputDir, options, true);
	}

	/**
	 * 按尺寸切分图像瓦片。
	 * <p>
	 * 将图像按照指定的瓦片尺寸进行切分，支持单一模式和金字塔模式。
	 * </p>
	 *
	 * <p><strong>切分模式</strong></p>
	 * <ul>
	 *   <li>单一模式：仅生成单一分辨率的瓦片，适用于简单的瓦片需求</li>
	 *   <li>金字塔模式：生成多级分辨率的瓦片，每级分辨率为上一级的1/2，适用于深度缩放场景</li>
	 * </ul>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>识别图像尺寸和方向信息</li>
	 *   <li>根据切分模式计算画布尺寸和层级数</li>
	 *   <li>执行切分操作</li>
	 *   <li>根据布局格式组织瓦片文件</li>
	 * </ol>
	 *
	 * <p><strong>失败处理</strong></p>
	 * <ul>
	 *   <li>retainOnFailure为true时，保留已生成的瓦片文件</li>
	 *   <li>retainOnFailure为false时，删除所有已生成的瓦片文件</li>
	 * </ul>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>画布会扩展到瓦片尺寸的整数倍，使用背景色填充</li>
	 *   <li>自动处理EXIF方向，确保瓦片方向正确</li>
	 *   <li>金字塔模式会生成多级目录，注意磁盘空间占用</li>
	 * </ul>
	 *
	 * @param resource        图像资源，不能为null
	 * @param outputDir       输出目录，不能为null
	 * @param options         瓦片切分选项，不能为null
	 * @param retainOnFailure 失败时是否保留已生成的瓦片文件，true保留，false删除
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像读取失败时抛出
	 * @throws ImageOperationException      图像操作异常，当切分操作失败时抛出
	 * @see SizeTileOptions
	 * @since 2.1.0
	 */
	void splitTilesBySize(IOResource resource, File outputDir, SizeTileOptions options, boolean retainOnFailure)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;

	/**
	 * 按网格切分图像瓦片。
	 * <p>
	 * 将图像按照指定的行数和列数切分成均匀的瓦片。
	 * 瓦片尺寸根据图像实际尺寸和行列数自动计算，确保覆盖整个图像区域。
	 * </p>
	 *
	 * <p><strong>处理步骤</strong></p>
	 * <ol>
	 *   <li>验证参数有效性</li>
	 *   <li>识别图像尺寸和方向信息</li>
	 *   <li>根据行列数计算瓦片宽度和高度</li>
	 *   <li>计算画布尺寸，确保瓦片完全覆盖图像</li>
	 *   <li>转换为按尺寸切分模式并执行切分</li>
	 * </ol>
	 *
	 * <p><strong>注意事项</strong></p>
	 * <ul>
	 *   <li>瓦片尺寸可能不完全相等，最后一行/列可能较小</li>
	 *   <li>画布会扩展到瓦片尺寸的整数倍，使用背景色填充</li>
	 *   <li>自动处理EXIF方向，确保瓦片方向正确</li>
	 *   <li>内部转换为单一模式的按尺寸切分</li>
	 * </ul>
	 *
	 * @param resource  图像资源，不能为null
	 * @param outputDir 输出目录，不能为null
	 * @param options   瓦片切分选项，不能为null
	 * @throws UnsupportedResourceException 不支持的资源异常，当资源类型不支持时抛出
	 * @throws ImageParsingException        图像解析异常，当图像读取失败时抛出
	 * @throws ImageOperationException      图像操作异常，当切分操作失败时抛出
	 * @see GridTileOptions
	 * @see SizeTileOptions
	 * @since 2.1.0
	 */
	void splitTilesByGrid(IOResource resource, File outputDir, GridTileOptions options)
		throws UnsupportedResourceException, ImageParsingException, ImageOperationException;
}
