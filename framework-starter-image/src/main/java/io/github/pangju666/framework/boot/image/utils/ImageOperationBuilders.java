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

package io.github.pangju666.framework.boot.image.utils;

import io.github.pangju666.framework.boot.image.model.BufferedImageOperation;
import io.github.pangju666.framework.boot.image.model.GMImageOperation;
import io.github.pangju666.framework.boot.image.model.GenericImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageOperation;

import java.util.Objects;

/**
 * 图像操作构建器快捷工具。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>提供创建通用（Generic）、GM、Buffered 三类构建器的便捷入口。</li>
 *   <li>内置空操作常量 {@code EMPTY} 用于纯格式转换场景。</li>
 * </ul>
 *
 * <p><b>方法入口</b></p>
 * <ul>
 *   <li>{@code generic()} -> 创建仅包含通用字段的构建器。</li>
 *   <li>{@code gm()} / {@code gm(operation)} -> 创建 GM 构建器（可选合并基础操作）。</li>
 *   <li>{@code buffered()} / {@code buffered(operation)} -> 创建 Buffered 构建器（可选合并基础操作）。</li>
 *   <li>{@code EMPTY} -> 仅执行格式转换，不含其它操作。</li>
 * </ul>
 *
 * <p><b>使用方式</b></p>
 * <ul>
 *   <li>创建构建器 -> 链式设置参数 -> 构建；可选：传入基础操作以合并通用字段。</li>
 * </ul>
 *
 * <p><b>约束与合并规则</b></p>
 * <ul>
 *   <li>合并仅复制 {@link ImageOperation} 的通用字段，子类扩展字段需单独设置。</li>
 *   <li>{@code gm(operation)} 与 {@code buffered(operation)} 支持 {@code null} 入参：为 {@code null} 时仅初始化不合并。</li>
 *   <li>合并会覆盖同名字段；互斥关系（文字水印 vs 图片水印、方向 vs 坐标）由具体实现处理。</li>
 * </ul>
 *
 * <p><b>执行顺序</b></p>
 * <ul>
 *   <li>创建构建器 ->（可选）合并基础配置 -> 返回构建器。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see ImageOperation
 * @see GMImageOperation
 * @see BufferedImageOperation
 */
public class ImageOperationBuilders {
    /**
     * 空图像操作。
     *
     * <p><b>用途</b>：仅进行图片格式转换；不包含裁剪/缩放/图片水印/旋转/翻转/灰度；不含任何实现特有扩展参数。</p>
     *
     * @since 1.0.0
     */
    public static final ImageOperation EMPTY = new GenericImageOperation.GenericImageOperationBuilder().build();

    /**
     * 创建通用（Generic）构建器。
     *
     * <p><b>流程</b>：初始化通用构建器 -> 返回。</p>
     * <p><b>用途</b>：用于仅依赖 {@link ImageOperation} 通用字段的场景，或先构建通用配置再与 GM/Buffered 构建器合并。</p>
     * <p><b>约束</b>：不包含质量/DPI/滤镜等实现特有参数。</p>
     *
     * @return 通用构建器
     * @since 1.0.0
     */
    public static GenericImageOperation.GenericImageOperationBuilder generic() {
        return new GenericImageOperation.GenericImageOperationBuilder();
    }

    /**
     * 创建 GM 构建器。
     *
     * <p><b>流程</b>：初始化 GM 构建器 -> 返回。</p>
     *
     * @return GM 构建器
     * @since 1.0.0
     */
    public static GMImageOperation.GMImageOperationBuilder gm() {
        return new GMImageOperation.GMImageOperationBuilder();
    }

    /**
     * 创建 Buffered 构建器。
     *
     * <p><b>流程</b>：初始化 Buffered 构建器 -> 返回。</p>
     *
     * @return Buffered 构建器
     * @since 1.0.0
     */
    public static BufferedImageOperation.BufferedImageOperationBuilder buffered() {
        return new BufferedImageOperation.BufferedImageOperationBuilder();
    }

    /**
     * 创建 GM 构建器并合并基础操作配置。
     *
     * <p><b>流程</b>：初始化 GM 构建器 ->（可选）合并通用字段（灰度/翻转/裁剪/缩放/定位/尺寸/比例/强制缩放/图片水印）-> 返回。</p>
     * <p><b>约束</b>：{@code operation} 可为 {@code null}（为空时仅初始化不合并）；合并将覆盖同名字段；不处理子类扩展字段。</p>
     *
     * @param operation 基础操作配置（可为 {@code null}）
     * @return GM 构建器
     * @since 1.0.0
     */
    public static GMImageOperation.GMImageOperationBuilder gm(ImageOperation operation) {
		if (Objects.isNull(operation)) {
			return new GMImageOperation.GMImageOperationBuilder();
		}
        return new GMImageOperation.GMImageOperationBuilder().addOperation(operation);
    }

    /**
     * 创建 Buffered 构建器并合并基础操作配置。
     *
     * <p><b>流程</b>：初始化 Buffered 构建器 ->（可选）合并通用字段（灰度/翻转/裁剪/缩放/定位/尺寸/比例/强制缩放/图片水印）-> 返回。</p>
     * <p><b>约束</b>：{@code operation} 可为 {@code null}（为空时仅初始化不合并）；合并将覆盖同名字段；不处理子类扩展字段。</p>
     *
     * @param operation 基础操作配置（可为 {@code null}）
     * @return Buffered 构建器
     * @since 1.0.0
     */
    public static BufferedImageOperation.BufferedImageOperationBuilder buffered(ImageOperation operation) {
		if (Objects.isNull(operation)) {
			return new BufferedImageOperation.BufferedImageOperationBuilder();
		}
        return new BufferedImageOperation.BufferedImageOperationBuilder().addOperation(operation);
    }
}
