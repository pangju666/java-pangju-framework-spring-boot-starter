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

/**
 * 通用图像操作配置。
 *
 * <p><b>概述</b>：仅使用 {@link ImageOperation} 的通用字段（裁剪/缩放/旋转/翻转/灰度/水印定位/图片水印/文字水印），不包含特定实现的扩展参数。</p>
 * <p><b>适用场景</b>：既可用于 GM，也可用于 Buffered 的通用场景，或作为基础配置被具体实现的构建器合并。</p>
 * <p><b>使用方式</b>：
 * 构建器初始化 -> 链式设置参数 -> 构建 -> 传入模板执行。</p>
 * <p><b>约束</b>：
 * 不包含质量/DPI/滤镜等实现特有参数；水印方向与坐标互斥；图片水印与文字水印的互斥由具体实现处理。</p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see ImageOperation
 */
public class GenericImageOperation extends ImageOperation {
	/**
     * 通用图像操作构建器。
     *
     * <p><b>流程</b>：初始化 -> 设置通用字段 -> 构建。</p>
     * <p><b>合并</b>：可与具体实现的构建器配合，作为基础配置进行合并。</p>
     *
     * @since 1.0.0
     */
	public static class GenericImageOperationBuilder extends ImageOperationBuilder<GenericImageOperationBuilder, GenericImageOperation> {
		/**
		 * 初始化构建器。
		 *
		 * @since 1.0.0
		 */
		public GenericImageOperationBuilder() {
			super(new GenericImageOperation());
		}
	}
}
