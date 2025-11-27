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

package io.github.pangju666.framework.boot.image.core.impl;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.core.ImageTemplate;
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.exception.UnSupportedTypeException;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import io.github.pangju666.framework.boot.image.model.GMImageOperation;
import io.github.pangju666.framework.boot.image.model.ImageFile;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import io.github.pangju666.framework.boot.image.utils.ImageOperationBuilders;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.GMServiceException;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 基于 <a href="http://www.graphicsmagick.org/index.html">GraphicsMagick</a> 的图像处理实现。
 *
 * <p>GraphicsMagick 版本需要 &ge; 1.30</p>
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>使用 {@link Tika} 进行图像类型解析。</li>
 *   <li>使用 GM 命令（identify/convert/composite）进行信息读取与图像处理。</li>
 * </ul>
 *
 * <p><b>约束</b></p>
 * <ul>
 *   <li>连接管理：通过 {@link PooledGMService} 获取连接，始终在 {@code finally} 中关闭。</li>
 *   <li>线程安全：调用方与连接池共同确保资源正确使用。</li>
 *   <li>裁剪限制：绘制“图片水印”路径下不支持裁剪（在执行顺序中说明）。</li>
 *   <li>双阶段执行：当图片水印与下列操作并存时采用 {@code convert + composite} 两次命令：裁剪、翻转、EXIF 非正常方向、模糊。</li>
 *   <li>路径限制：GM 输入/输出文件路径不支持中文或非 ASCII 字符，需要使用纯英文路径，否则可能导致命令执行失败。</li>
 *   <li>异常策略：GM 命令或通信错误统一抛出 {@link ImageOperationException}。</li>
 * </ul>
 *
 * <p><b>执行顺序（本实现）</b></p>
 * <p>整体处理逻辑根据是否包含图片水印以及是否存在复杂变换（如裁剪、旋转、EXIF 矫正等），
 * 动态选择使用 {@code convert}、{@code composite} 或两者组合的方式执行。</p>
 *
 * <h2>主流程</h2>
 * <ol>
 *   <li><b>计算目标尺寸</b>：基于原始图像尺寸与用户指定的缩放/裁剪策略，确定输出图像的目标宽高。</li>
 *   <li><b>判断是否需要添加图像水印</b>：
 *     <ul>
 *       <li>若 <b>不需要</b> 图像水印，则直接执行 {@code convert} 命令完成全部处理。</li>
 *     <li>若 <b>需要</b> 图像水印，则进入下一步判断。</li>
 *     </ul>
 *   </li>
 *   <li><b>判断是否存在高级图像操作</b>（满足任一即视为“存在”）：
 *     <ul>
 *       <li>裁剪（Crop）</li>
 *       <li>旋转（非 0°）</li>
 *       <li>EXIF 方向非正常（Orientation ≠ 1）</li>
 *       <li>模糊（Blur）</li>
 *     </ul>
 *     <ul>
 *       <li>若 <b>不存在</b> 上述操作，则直接使用 {@code composite} 命令叠加图片水印并输出。</li>
 *       <li>若 <b>存在</b> 上述任一操作，则需分两阶段处理（见下文）。</li>
 *     </ul>
 *   </li>
 *   <li><b>两阶段处理（{@code convert} + {@code composite}）</b>：
 *     <ol>
 *       <li>先通过 {@code convert} 命令完成所有基础图像变换，并输出至临时文件；</li>
 *       <li>再通过 {@code composite} 命令将图片水印叠加到临时文件上；</li>
 *       <li>最终输出结果文件，并清理临时文件。</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <h2>{@code convert} 命令操作执行顺序</h2>
 * <p>当仅使用 {@code convert}（无图片水印或无需复合处理）时，操作按以下顺序执行：</p>
 * <ol>
 *   <li>依据 EXIF Orientation 矫正图像方向；</li>
 *   <li>设置重采样滤镜类型（Resample Filter）；</li>
 *   <li>执行裁剪（Crop）；</li>
 *   <li>执行缩放（Resize）；</li>
 *   <li>执行旋转（Rotate）；</li>
 *   <li>执行翻转（Flip / Flop，垂直或水平）；</li>
 *   <li>灰度化（Grayscale）；</li>
 *   <li>图像增强（按顺序）：
 *     <ol type="a">
 *       <li>锐化（Sharpen）</li>
 *       <li>模糊（Blur）</li>
 *     </ol>
 *   </li>
 *   <li>添加文字水印（按指定方向或坐标放置）；</li>
 *   <li>设置输出图像质量（Quality）；</li>
 *   <li>设置输出 DPI；</li>
 *   <li>根据配置决定是否剥离元数据（Strip Metadata）；</li>
 *   <li>输出到目标文件。</li>
 * </ol>
 *
 * <h2>{@code composite} 命令操作执行顺序</h2>
 * <p>当仅需叠加图片水印且无复杂变换时，使用 {@code composite} 命令，操作顺序如下：</p>
 * <ol>
 *   <li>添加图片水印（按指定方向或坐标放置）；</li>
 *   <li>设置重采样滤镜类型；</li>
 *   <li>执行缩放；</li>
 *   <li>执行旋转；</li>
 *   <li>灰度化；</li>
 *   <li>锐化；</li>
 *   <li>设置输出图像质量；</li>
 *   <li>设置输出 DPI；</li>
 *   <li>根据配置决定是否剥离元数据；</li>
 *   <li>输出到目标文件。</li>
 * </ol>
 *
 * <h2>{@code convert} + {@code composite} 联合操作流程</h2>
 * <p>当同时存在图片水印与复杂图像操作时，采用两阶段处理：</p>
 * <ol>
 *   <li>执行完整的 {@code convert} 操作流程（见上文），输出至临时文件；</li>
 *   <li>将临时文件作为输入，执行 {@code composite} 操作，仅保留以下步骤：
 *     <ol>
 *       <li>添加图片水印（按方向或坐标放置）；</li>
 *       <li>输出到最终目标文件。</li>
 *     </ol>
 *   </li>
 *   <li>删除临时文件。</li>
 * </ol>
 *
 * <p><b>格式支持</b></p>
 * <ul>
 *   <li>读取格式：{@link ImageConstants#GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET}。</li>
 *   <li>写出格式：{@link ImageConstants#GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMAT_SET}。</li>
 * </ul>
 *
 * <p><b>异常与容错</b></p>
 * <ul>
 *   <li>输入或输出类型不受支持：抛出 {@link UnSupportedTypeException}。</li>
 *   <li>GM 命令失败或通信错误：抛出 {@link ImageOperationException}。</li>
 * </ul>
 *
 * @author pangju666
 * @see GMImageOperation
 * @since 1.0.0
 */
public class GMImageTemplate implements ImageTemplate {
	/**
	 * 日志记录器。
	 *
	 * @since 1.0.0
	 */
	protected static final Logger LOGGER = LoggerFactory.getLogger(GMImageTemplate.class);

	/**
	 * GM 文字绘制参数格式（坐标与文本）。
	 *
	 * <p>示例：text x y 'content'</p>
	 *
	 * @since 1.0.0
	 */
	protected static final String DRAW_TEXT_ARG_FORMAT = "\"text %d %d '%s'\"";
	/**
	 * RGBA 填充颜色格式字符串。
	 *
	 * <p>示例：rgba(r,g,b,opacity)，透明度范围 0-1。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final String FILL_COLOR_FORMAT = "rgba(%d,%d,%d,%.1f)";

	/**
	 * GM 连接池服务，用于执行 GM 命令。
	 *
	 * @since 1.0.0
	 */
	private final PooledGMService pooledGMService;

	public GMImageTemplate(PooledGMService pooledGMService) {
		this.pooledGMService = pooledGMService;
	}

	/**
	 * 读取并返回图像信息（尺寸、格式、MIME 类型、文件大小等）。
	 *
	 * @param file 待解析图像文件
	 * @return 图像信息
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException    尺寸解析失败时抛出
	 * @throws ImageOperationException  GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException              文件读取失败时抛出
	 */
	@Override
	public ImageFile read(File file) throws IOException {
		ImageFile imageFile = ImageFile.fromFile(file);
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET.contains(imageFile.getFormat())) {
			throw new UnSupportedTypeException("不支持读取 " + imageFile.getFormat() + " 格式图片");
		}

		GMOperation operation = new GMOperation();
		operation.addRawArg("identify");
		operation.verbose();
		operation.addImage(file);

		String result = execute(operation);
		for (String metaData : result.lines().toList()) {
			String metaDataStrip = metaData.strip();
			if (metaDataStrip.startsWith("Geometry:")) {
				String geometry = StringUtils.substringAfter(metaDataStrip, "Geometry:").strip();
				if (StringUtils.isBlank(geometry)) {
					throw new ImageParsingException(file, "尺寸解析失败");
				}
				try {
					String[] geometryValue = geometry.split("x");
					imageFile.setImageSize(new ImageSize(Integer.parseInt(geometryValue[0]),
						Integer.parseInt(geometryValue[1])));
				} catch (NumberFormatException e) {
					throw new ImageParsingException(file, "尺寸解析失败");
				}
			} else if (metaDataStrip.startsWith("Orientation:")) {
				try {
					String orientationStr = StringUtils.substringAfter(metaDataStrip.strip(), "Orientation:").strip();
					imageFile.setOrientation(Integer.parseInt(orientationStr));
				} catch (NumberFormatException ignored) {
				}
			}
		}

		// 返回经 EXIF Orientation 矫正后的显示尺寸（视觉尺寸）
		if (imageFile.getOrientation() >= 5 && imageFile.getOrientation() <= 8) {
			imageFile.setImageSize(new ImageSize(imageFile.getImageSize().getHeight(), imageFile.getImageSize().getWidth()));
		}
		return imageFile;
	}

	/**
	 * 执行图像操作（输入文件形式）。
	 *
	 * <p><b>执行顺序（本实现）</b></p>
	 * <p>整体处理逻辑根据是否包含图片水印以及是否存在复杂变换（如裁剪、旋转、EXIF 矫正等），
	 * 动态选择使用 {@code convert}、{@code composite} 或两者组合的方式执行。</p>
	 *
	 * <h2>主流程</h2>
	 * <ol>
	 *   <li><b>计算目标尺寸</b>：基于原始图像尺寸与用户指定的缩放/裁剪策略，确定输出图像的目标宽高。</li>
	 *   <li><b>判断是否需要添加图像水印</b>：
	 *     <ul>
	 *       <li>若 <b>不需要</b> 图像水印，则直接执行 {@code convert} 命令完成全部处理。</li>
	 *     <li>若 <b>需要</b> 图像水印，则进入下一步判断。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>判断是否存在高级图像操作</b>（满足任一即视为“存在”）：
	 *     <ul>
	 *       <li>裁剪（Crop）</li>
	 *       <li>旋转（非 0°）</li>
	 *       <li>EXIF 方向非正常（Orientation ≠ 1）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ul>
	 *     <ul>
	 *       <li>若 <b>不存在</b> 上述操作，则直接使用 {@code composite} 命令叠加图片水印并输出。</li>
	 *       <li>若 <b>存在</b> 上述任一操作，则需分两阶段处理（见下文）。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>两阶段处理（{@code convert} + {@code composite}）</b>：
	 *     <ol>
	 *       <li>先通过 {@code convert} 命令完成所有基础图像变换，并输出至临时文件；</li>
	 *       <li>再通过 {@code composite} 命令将图片水印叠加到临时文件上；</li>
	 *       <li>最终输出结果文件，并清理临时文件。</li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 *
	 * <h2>{@code convert} 命令操作执行顺序</h2>
	 * <p>当仅使用 {@code convert}（无图片水印或无需复合处理）时，操作按以下顺序执行：</p>
	 * <ol>
	 *   <li>依据 EXIF Orientation 矫正图像方向；</li>
	 *   <li>设置重采样滤镜类型（Resample Filter）；</li>
	 *   <li>执行裁剪（Crop）；</li>
	 *   <li>执行缩放（Resize）；</li>
	 *   <li>执行旋转（Rotate）；</li>
	 *   <li>执行翻转（Flip / Flop，垂直或水平）；</li>
	 *   <li>灰度化（Grayscale）；</li>
	 *   <li>图像增强（按顺序）：
	 *     <ol type="a">
	 *       <li>锐化（Sharpen）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ol>
	 *   </li>
	 *   <li>添加文字水印（按指定方向或坐标放置）；</li>
	 *   <li>设置输出图像质量（Quality）；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据（Strip Metadata）；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code composite} 命令操作执行顺序</h2>
	 * <p>当仅需叠加图片水印且无复杂变换时，使用 {@code composite} 命令，操作顺序如下：</p>
	 * <ol>
	 *   <li>添加图片水印（按指定方向或坐标放置）；</li>
	 *   <li>设置重采样滤镜类型；</li>
	 *   <li>执行缩放；</li>
	 *   <li>执行旋转；</li>
	 *   <li>灰度化；</li>
	 *   <li>锐化；</li>
	 *   <li>设置输出图像质量；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code convert} + {@code composite} 联合操作流程</h2>
	 * <p>当同时存在图片水印与复杂图像操作时，采用两阶段处理：</p>
	 * <ol>
	 *   <li>执行完整的 {@code convert} 操作流程（见上文），输出至临时文件；</li>
	 *   <li>将临时文件作为输入，执行 {@code composite} 操作，仅保留以下步骤：
	 *     <ol>
	 *       <li>添加图片水印（按方向或坐标放置）；</li>
	 *       <li>输出到最终目标文件。</li>
	 *     </ol>
	 *   </li>
	 *   <li>删除临时文件。</li>
	 * </ol>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param operation  操作配置，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException    图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException  GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException              文件读取失败时抛出
	 */
	@Override
	public void process(File inputFile, File outputFile, ImageOperation operation) throws IOException {
		checkOutputFile(outputFile);

		ImageFile imageFile = read(inputFile);
		doProcess(imageFile, outputFile, ObjectUtils.getIfNull(operation,
			ImageOperationBuilders.EMPTY));
	}

	/**
	 * 执行图像操作（已解析信息形式）。
	 *
	 * <p><b>执行顺序（本实现）</b></p>
	 * <p>整体处理逻辑根据是否包含图片水印以及是否存在复杂变换（如裁剪、旋转、EXIF 矫正等），
	 * 动态选择使用 {@code convert}、{@code composite} 或两者组合的方式执行。</p>
	 *
	 * <h2>主流程</h2>
	 * <ol>
	 *   <li><b>计算目标尺寸</b>：基于原始图像尺寸与用户指定的缩放/裁剪策略，确定输出图像的目标宽高。</li>
	 *   <li><b>判断是否需要添加图像水印</b>：
	 *     <ul>
	 *       <li>若 <b>不需要</b> 图像水印，则直接执行 {@code convert} 命令完成全部处理。</li>
	 *     <li>若 <b>需要</b> 图像水印，则进入下一步判断。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>判断是否存在高级图像操作</b>（满足任一即视为“存在”）：
	 *     <ul>
	 *       <li>裁剪（Crop）</li>
	 *       <li>旋转（非 0°）</li>
	 *       <li>EXIF 方向非正常（Orientation ≠ 1）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ul>
	 *     <ul>
	 *       <li>若 <b>不存在</b> 上述操作，则直接使用 {@code composite} 命令叠加图片水印并输出。</li>
	 *       <li>若 <b>存在</b> 上述任一操作，则需分两阶段处理（见下文）。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>两阶段处理（{@code convert} + {@code composite}）</b>：
	 *     <ol>
	 *       <li>先通过 {@code convert} 命令完成所有基础图像变换，并输出至临时文件；</li>
	 *       <li>再通过 {@code composite} 命令将图片水印叠加到临时文件上；</li>
	 *       <li>最终输出结果文件，并清理临时文件。</li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 *
	 * <h2>{@code convert} 命令操作执行顺序</h2>
	 * <p>当仅使用 {@code convert}（无图片水印或无需复合处理）时，操作按以下顺序执行：</p>
	 * <ol>
	 *   <li>依据 EXIF Orientation 矫正图像方向；</li>
	 *   <li>设置重采样滤镜类型（Resample Filter）；</li>
	 *   <li>执行裁剪（Crop）；</li>
	 *   <li>执行缩放（Resize）；</li>
	 *   <li>执行旋转（Rotate）；</li>
	 *   <li>执行翻转（Flip / Flop，垂直或水平）；</li>
	 *   <li>灰度化（Grayscale）；</li>
	 *   <li>图像增强（按顺序）：
	 *     <ol type="a">
	 *       <li>锐化（Sharpen）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ol>
	 *   </li>
	 *   <li>添加文字水印（按指定方向或坐标放置）；</li>
	 *   <li>设置输出图像质量（Quality）；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据（Strip Metadata）；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code composite} 命令操作执行顺序</h2>
	 * <p>当仅需叠加图片水印且无复杂变换时，使用 {@code composite} 命令，操作顺序如下：</p>
	 * <ol>
	 *   <li>添加图片水印（按指定方向或坐标放置）；</li>
	 *   <li>设置重采样滤镜类型；</li>
	 *   <li>执行缩放；</li>
	 *   <li>执行旋转；</li>
	 *   <li>灰度化；</li>
	 *   <li>锐化；</li>
	 *   <li>设置输出图像质量；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code convert} + {@code composite} 联合操作流程</h2>
	 * <p>当同时存在图片水印与复杂图像操作时，采用两阶段处理：</p>
	 * <ol>
	 *   <li>执行完整的 {@code convert} 操作流程（见上文），输出至临时文件；</li>
	 *   <li>将临时文件作为输入，执行 {@code composite} 操作，仅保留以下步骤：
	 *     <ol>
	 *       <li>添加图片水印（按方向或坐标放置）；</li>
	 *       <li>输出到最终目标文件。</li>
	 *     </ol>
	 *   </li>
	 *   <li>删除临时文件。</li>
	 * </ol>
	 *
	 * @param imageFile  已解析的图像信息
	 * @param outputFile 输出文件
	 * @param operation  操作配置，可为 {@code null}
	 * @throws UnSupportedTypeException 图像类型不受支持时抛出
	 * @throws ImageParsingException    图像类型、摘要或尺寸解析失败时抛出
	 * @throws ImageOperationException  GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException              文件读取失败时抛出
	 */
	@Override
	public void process(ImageFile imageFile, File outputFile, ImageOperation operation) throws IOException {
		Assert.notNull(imageFile, "imageFile 不可为 null");
		checkOutputFile(outputFile);

		String format = StringUtils.defaultIfBlank(imageFile.getFormat(),
			FilenameUtils.getExtension(imageFile.getFile().getName()).toUpperCase());
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET.contains(format)) {
			throw new UnSupportedTypeException("不支持读取 " + format + " 格式图片");
		}

		if (imageFile.getOrientation() < 1 || imageFile.getOrientation() > 8 || Objects.isNull(imageFile.getImageSize())) {
			imageFile = read(imageFile.getFile());
		}

		doProcess(imageFile, outputFile, ObjectUtils.getIfNull(operation,
			ImageOperationBuilders.EMPTY));
	}

	/**
	 * 判断实现是否支持读取文件。
	 *
	 * @param file 待判定文件
	 * @return {@code true} 表示支持读取
	 * @throws IOException 文件读取失败时抛出
	 */
	@Override
	public boolean canRead(File file) throws IOException {
		FileUtils.check(file, "file 不可为 null");

		String fileFormat = FilenameUtils.getExtension(file.getName()).toUpperCase();
		return ImageConstants.GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET.contains(fileFormat);
	}

	/**
	 * 判断实现是否支持输出为指定的图像格式。
	 *
	 * @param format 待判定图像格式
	 * @return {@code true} 表示支持写出
	 */
	@Override
	public boolean canWrite(String format) {
		Assert.hasText(format, "format 不可为空");
		return ImageConstants.GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMAT_SET.contains(format.toUpperCase());
	}

	/**
	 * 执行 GM 命令并返回输出文本。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code operation} 为空，则不执行并抛出异常。</p>
	 * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
	 *
	 * @param operation GM 操作对象
	 * @return 命令执行输出
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	public String execute(GMOperation operation) {
		Assert.notNull(operation, "operation 不可为 null");

		GMConnection connection = null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(operation.toString());
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + operation + " 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行 GM 命令。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code command} 为空，则不执行并抛出异常；如果 {@code arguments} 为空，则不设置附加参数。</p>
	 * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
	 *
	 * @param command   GM 命令
	 * @param arguments GM 命令附加参数，可为空
	 * @return 命令执行结果输出
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	public String execute(String command, String... arguments) {
		Assert.hasText(command, "command 不可为空");

		GMConnection connection = null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(command, arguments);
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + command + StringUtils.SPACE +
				StringUtils.joinWith(StringUtils.SPACE, command) + " 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行 GM 命令列表。
	 *
	 * <p>参数校验规则：</p>
	 * <p>如果 {@code command} 为空，则不执行并返回空字符串。</p>
	 * <p>释放策略：使用连接池获取连接，始终在 {@code finally} 中关闭连接。</p>
	 *
	 * @param command GM 命令及参数列表
	 * @return 命令执行结果输出；当输入为空时返回空字符串
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @since 1.0.0
	 */
	public String execute(List<String> command) {
		if (CollectionUtils.isEmpty(command)) {
			return StringUtils.EMPTY;
		}

		GMConnection connection = null;
		try {
			connection = pooledGMService.getConnection();
			return connection.execute(command);
		} catch (GMServiceException e) {
			throw new ImageOperationException("与GM进程通信时出现错误", e);
		} catch (GMException | IOException e) {
			throw new ImageOperationException("GM命令: " + StringUtils.join(command, StringUtils.SPACE) +
				" 执行失败", e);
		} finally {
			if (Objects.nonNull(connection)) {
				try {
					connection.close();
				} catch (GMServiceException e) {
					LOGGER.error("GM进程关闭时出现错误", e);
				}
			}
		}
	}

	/**
	 * 执行具体处理逻辑。
	 *
	 * <p><b>执行顺序（本实现）</b></p>
	 * <p>整体处理逻辑根据是否包含图片水印以及是否存在复杂变换（如裁剪、旋转、EXIF 矫正等），
	 * 动态选择使用 {@code convert}、{@code composite} 或两者组合的方式执行。</p>
	 *
	 * <h2>主流程</h2>
	 * <ol>
	 *   <li><b>计算目标尺寸</b>：基于原始图像尺寸与用户指定的缩放/裁剪策略，确定输出图像的目标宽高。</li>
	 *   <li><b>判断是否需要添加图像水印</b>：
	 *     <ul>
	 *       <li>若 <b>不需要</b> 图像水印，则直接执行 {@code convert} 命令完成全部处理。</li>
	 *     <li>若 <b>需要</b> 图像水印，则进入下一步判断。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>判断是否存在高级图像操作</b>（满足任一即视为“存在”）：
	 *     <ul>
	 *       <li>裁剪（Crop）</li>
	 *       <li>旋转（非 0°）</li>
	 *       <li>EXIF 方向非正常（Orientation ≠ 1）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ul>
	 *     <ul>
	 *       <li>若 <b>不存在</b> 上述操作，则直接使用 {@code composite} 命令叠加图片水印并输出。</li>
	 *       <li>若 <b>存在</b> 上述任一操作，则需分两阶段处理（见下文）。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>两阶段处理（{@code convert} + {@code composite}）</b>：
	 *     <ol>
	 *       <li>先通过 {@code convert} 命令完成所有基础图像变换，并输出至临时文件；</li>
	 *       <li>再通过 {@code composite} 命令将图片水印叠加到临时文件上；</li>
	 *       <li>最终输出结果文件，并清理临时文件。</li>
	 *     </ol>
	 *   </li>
	 * </ol>
	 *
	 * <h2>{@code convert} 命令操作执行顺序</h2>
	 * <p>当仅使用 {@code convert}（无图片水印或无需复合处理）时，操作按以下顺序执行：</p>
	 * <ol>
	 *   <li>依据 EXIF Orientation 矫正图像方向；</li>
	 *   <li>设置重采样滤镜类型（Resample Filter）；</li>
	 *   <li>执行裁剪（Crop）；</li>
	 *   <li>执行缩放（Resize）；</li>
	 *   <li>执行旋转（Rotate）；</li>
	 *   <li>执行翻转（Flip / Flop，垂直或水平）；</li>
	 *   <li>灰度化（Grayscale）；</li>
	 *   <li>图像增强（按顺序）：
	 *     <ol type="a">
	 *       <li>锐化（Sharpen）</li>
	 *       <li>模糊（Blur）</li>
	 *     </ol>
	 *   </li>
	 *   <li>添加文字水印（按指定方向或坐标放置）；</li>
	 *   <li>设置输出图像质量（Quality）；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据（Strip Metadata）；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code composite} 命令操作执行顺序</h2>
	 * <p>当仅需叠加图片水印且无复杂变换时，使用 {@code composite} 命令，操作顺序如下：</p>
	 * <ol>
	 *   <li>添加图片水印（按指定方向或坐标放置）；</li>
	 *   <li>设置重采样滤镜类型；</li>
	 *   <li>执行缩放；</li>
	 *   <li>执行旋转；</li>
	 *   <li>灰度化；</li>
	 *   <li>锐化；</li>
	 *   <li>设置输出图像质量；</li>
	 *   <li>设置输出 DPI；</li>
	 *   <li>根据配置决定是否剥离元数据；</li>
	 *   <li>输出到目标文件。</li>
	 * </ol>
	 *
	 * <h2>{@code convert} + {@code composite} 联合操作流程</h2>
	 * <p>当同时存在图片水印与复杂图像操作时，采用两阶段处理：</p>
	 * <ol>
	 *   <li>执行完整的 {@code convert} 操作流程（见上文），输出至临时文件；</li>
	 *   <li>将临时文件作为输入，执行 {@code composite} 操作，仅保留以下步骤：
	 *     <ol>
	 *       <li>添加图片水印（按方向或坐标放置）；</li>
	 *       <li>输出到最终目标文件。</li>
	 *     </ol>
	 *   </li>
	 *   <li>删除临时文件。</li>
	 * </ol>
	 *
	 * @param imageFile  图像信息
	 * @param outputFile 输出文件
	 * @param operation  操作配置
	 * @throws ImageOperationException GM命令执行失败或与GM进程通信错误时抛出
	 * @throws IOException             输出文件父级目录创建失败时抛出
	 * @since 1.0.0
	 */
	protected void doProcess(ImageFile imageFile, File outputFile, ImageOperation operation) throws IOException {
		ImageSize imageSize = imageFile.getImageSize();
		if (ObjectUtils.allNotNull(operation.getTargetWidth(), operation.getTargetHeight())) {
			if (operation.isForceScale()) {
				imageSize = new ImageSize(operation.getTargetWidth(), operation.getTargetHeight());
			} else {
				imageSize = imageSize.scale(operation.getTargetWidth(), operation.getTargetHeight());
			}
		} else if (Objects.nonNull(operation.getScaleRatio())) {
			imageSize = imageSize.scale(operation.getScaleRatio());
		} else if (Objects.nonNull(operation.getTargetWidth())) {
			imageSize = imageSize.scaleByWidth(operation.getTargetWidth());
		} else if (Objects.nonNull(operation.getTargetHeight())) {
			imageSize = imageSize.scaleByWidth(operation.getTargetHeight());
		}

		GMImageOperation gmImageOperation = null;
		if (operation instanceof GMImageOperation) {
			gmImageOperation = (GMImageOperation) operation;
		}

		// 如果不需要添加图片水印，则直接执行convert命令
		if (Objects.isNull(operation.getWatermarkImage())) {
			executeConvert(imageFile.getFile(), imageSize, outputFile, operation, gmImageOperation);
		} else {
			// 不需要裁剪、翻转、矫正方向和模糊时可以直接执行composite命令完成操作
			if (ObjectUtils.allNull(operation.getCropType(), operation.getFlipDirection()) &&
				imageFile.getOrientation() == ImageConstants.NORMAL_EXIF_ORIENTATION &&
				(Objects.isNull(gmImageOperation) || Objects.isNull(gmImageOperation.getBlurPair()))) {
				executeComposite(imageFile.getFile(), imageSize, outputFile, operation, gmImageOperation);
			} else {
				File tmpOuputFile = new File(FileUtils.getTempDirectory(), outputFile.getName());
				// 先执行convert命令
				executeConvert(imageFile.getFile(), imageSize, tmpOuputFile, operation, gmImageOperation);

				// 再执行composite命令添加图片水印
				GMOperation watermarkGMOperation = createCompositeGMOperation(imageSize, operation);
				// 传入临时输出文件
				watermarkGMOperation.addImage(tmpOuputFile);
				// 传入输出文件
				watermarkGMOperation.addImage(outputFile);
				FileUtils.forceMkdirParent(outputFile);

				try {
					execute(watermarkGMOperation);
				} finally {
					// 删除临时输出文件
					FileUtils.forceDeleteIfExist(tmpOuputFile);
				}
			}
		}
	}

	/**
	 * 执行图片水印合成（composite）。
	 *
	 * <p><b>流程</b>：创建合成命令 -> 可选设置重采样滤镜 -> 统一缩放到输入尺寸
	 * -> 旋转 -> 灰度化 -> 锐化 -> 写入输出参数（质量/DPI/元数据剥离）
	 * -> 追加输出文件 -> 执行命令。</p>
	 * <p><b>适用场景</b>：使用图片作为水印进行叠加；适用于单次 {@code composite} 路径。</p>
	 * <p><b>双阶段说明</b>：当与裁剪/翻转/EXIF 非正常方向/模糊并存时，不直接使用本方法完成全部处理，应先执行 {@code convert} 再执行 {@code composite}。</p>
	 *
	 * @param inputFile        输入文件
	 * @param imageSize        输入图像尺寸
	 * @param outputFile       输出文件
	 * @param operation        基础操作配置
	 * @param gmImageOperation GM 扩展配置（滤镜/灰度/锐化/质量/DPI/剥离等）
	 * @throws IOException 输出文件父级目录创建失败时抛出
	 */
	protected void executeComposite(File inputFile, ImageSize imageSize, File outputFile, ImageOperation operation,
									GMImageOperation gmImageOperation) throws IOException {
		GMOperation gmOperation = createCompositeGMOperation(imageSize, operation);

		// 判断是否需要修改重采样过滤器
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getResizeFilter())) {
			gmOperation.filter(gmImageOperation.getResizeFilter());
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.anyNotNull(operation.getTargetWidth(), operation.getTargetHeight(), operation.getScaleRatio())) {
			gmOperation.resize(imageSize.getWidth(), imageSize.getHeight(), '!');
		}

		// 判断是否需要旋转
		if (Objects.nonNull(operation.getRotateAngle())) {
			gmOperation.rotate(operation.getRotateAngle());
		}

		// 判断是否需要灰度化
		if (Objects.nonNull(gmImageOperation) && gmImageOperation.isGrayscale()) {
			gmOperation.colorspace("Gray");
		}

		// 判断是否需要锐化
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getSharpenPair())) {
			gmOperation.sharpen(gmImageOperation.getSharpenPair().getLeft(), gmImageOperation.getSharpenPair().getRight());
		}

		// 设置输出参数
		setOutputArgs(inputFile, gmImageOperation, gmOperation);

		// 传入输出文件
		gmOperation.addImage(outputFile);

		// 执行命令
		execute(gmOperation);
	}

	/**
	 * 执行图像转换（convert）。
	 *
	 * <p><b>流程</b>：初始化命令 -> 方向矫正 -> 添加输入文件 -> 设置重采样滤镜 -> 裁剪
	 * -> 统一缩放到输入尺寸-> 旋转 -> 翻转（垂直/水平）
	 * -> 灰度化 -> 锐化/模糊 -> 添加文字水印（方向或坐标）
	 * -> 写入输出参数（质量/DPI/元数据剥离）-> 执行命令。</p>
	 *
	 * @param inputFile        输入文件
	 * @param imageSize        输入图像尺寸
	 * @param outputFile       输出文件
	 * @param operation        基础操作配置（裁剪/缩放/旋转/翻转/水印等）
	 * @param gmImageOperation GM 扩展配置（滤镜/灰度/锐化/模糊/质量/DPI/剥离等）
	 * @throws IOException 输出文件父级目录创建失败时抛出
	 */
	protected void executeConvert(File inputFile, ImageSize imageSize, File outputFile,
								  ImageOperation operation, GMImageOperation gmImageOperation) throws IOException {
		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("convert");
		// 传入输入文件
		gmOperation.addImage(inputFile);
		// 方向矫正
		gmOperation.addRawArg("-auto-orient");

		// 判断是否需要修改重采样过滤器
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getResizeFilter())) {
			gmOperation.filter(gmImageOperation.getResizeFilter());
		}

		// 判断是否需要裁剪
		if (Objects.nonNull(operation.getCropType())) {
			setCropArgs(imageSize, operation, gmOperation);
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.anyNotNull(operation.getTargetWidth(), operation.getTargetHeight(), operation.getScaleRatio())) {
			gmOperation.resize(imageSize.getWidth(), imageSize.getHeight(), '!');
		}

		// 判断是否需要旋转
		if (Objects.nonNull(operation.getRotateAngle())) {
			gmOperation.rotate(operation.getRotateAngle());
		}

		// 判断是否需要翻转
		if (Objects.nonNull(operation.getFlipDirection())) {
			switch (operation.getFlipDirection()) {
				case VERTICAL -> gmOperation.flip();
				case HORIZONTAL -> gmOperation.flop();
			}
		}

		// 判断是否需要灰度化
		if (Objects.nonNull(gmImageOperation) && gmImageOperation.isGrayscale()) {
			gmOperation.colorspace("Gray");
		}

		// 判断是否需要锐化
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getSharpenPair())) {
			gmOperation.sharpen(gmImageOperation.getSharpenPair().getLeft(), gmImageOperation.getSharpenPair().getRight());
		}

		// 判断是否需要模糊
		if (Objects.nonNull(gmImageOperation) && Objects.nonNull(gmImageOperation.getBlurPair())) {
			gmOperation.blur(gmImageOperation.getBlurPair().getLeft(), gmImageOperation.getBlurPair().getRight());
		}

		// 判断是否需要添加文字水印
		if (Objects.nonNull(gmImageOperation) && ObjectUtils.allNotNull(
			gmImageOperation.getWatermarkTextFontName(), gmImageOperation.getWatermarkText())) {
			if (Objects.nonNull(operation.getWatermarkDirection())) {
				setGravityArg(operation, gmOperation);
				setTextWatermarkArgs(imageSize, gmImageOperation, gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, 20, 20, gmImageOperation.getWatermarkText()));
			} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
				// 设置左上角为原点
				gmOperation.gravity(GMOperation.Gravity.NorthWest);
				setTextWatermarkArgs(imageSize, gmImageOperation, gmOperation);
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, operation.getWatermarkX(),
					operation.getWatermarkY(), gmImageOperation.getWatermarkText()));
			}
		}

		// 设置输出参数
		setOutputArgs(outputFile, gmImageOperation, gmOperation);

		// 执行命令
		execute(gmOperation);
	}

	/**
	 * 构建图片水印合成命令。
	 *
	 * <p><b>作用</b>：初始化 {@code composite} 模式，按照水印透明度与位置（方向或坐标）生成合成所需参数。</p>
	 * <p><b>流程</b>：初始化命令 -> 设置透明度（{@code -dissolve opacity%}） ->
	 * 方向放置：设置 {@code -gravity} 并使用默认偏移 {@code (10,10)} ->
	 * 坐标放置：先追加水印图、设置原点为左上角（{@code NorthWest}）并写入 {@code -geometry} ->
	 * 追加水印图以参与合成。</p>
	 * <p><b>参数约束</b>：透明度范围 [0,1]；坐标为非空像素值；方向与坐标互斥（设置其一）。</p>
	 *
	 * @param imageSize 输入图像尺寸
	 * @param operation 操作配置
	 * @return 合成命令对象
	 * @since 1.0.0
	 */
	protected GMOperation createCompositeGMOperation(ImageSize imageSize, ImageOperation operation) {
		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("composite");
		gmOperation.addRawArg("-dissolve " + (int) (operation.getWatermarkImageOption().getOpacity() * 100));

		if (Objects.nonNull(operation.getWatermarkDirection())) {
			setGravityArg(operation, gmOperation);
			setWatermarkImageGeometryArg(imageSize, operation, 10, 10, gmOperation);
		} else if (ObjectUtils.allNotNull(operation.getWatermarkX(), operation.getWatermarkY())) {
			gmOperation.addImage(operation.getWatermarkImage());
			// 设置左上角为原点
			gmOperation.gravity(GMOperation.Gravity.NorthWest);
			setWatermarkImageGeometryArg(imageSize, operation, operation.getWatermarkX(), operation.getWatermarkY(),
				gmOperation);
		}

		gmOperation.addImage(operation.getWatermarkImage());

		return gmOperation;
	}

	/**
	 * 校验输出文件格式是否支持。
	 *
	 * @param outputFile 输出文件
	 * @since 1.0.0
	 */
	protected void checkOutputFile(File outputFile) {
		FileUtils.checkFileIfExist(outputFile, "outputImageFile 不可为 null");
		String outputImageFormat = FilenameUtils.getExtension(outputFile.getName()).toUpperCase();
		if (StringUtils.isBlank(outputImageFormat)) {
			throw new UnSupportedTypeException("未知的输出格式");
		}
		if (!ImageConstants.GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMAT_SET.contains(outputImageFormat)) {
			throw new UnSupportedTypeException("不支持输出为" + outputImageFormat + "格式");
		}
	}

	/**
	 * 设置裁剪相关参数
	 *
	 * @param imageSize   原始图片尺寸
	 * @param operation   操作配置
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setCropArgs(ImageSize imageSize, ImageOperation operation, GMOperation gmOperation) {
		if (operation.getCropType() == CropType.CENTER) {
			if (ObjectUtils.allNotNull(operation.getCenterCropWidth(), operation.getCenterCropHeight()) &&
				operation.getCenterCropWidth() < imageSize.getWidth() &&
				operation.getCenterCropHeight() < imageSize.getHeight()) {
				int posX = (imageSize.getWidth() - operation.getCenterCropWidth()) / 2;
				int posY = (imageSize.getHeight() - operation.getCenterCropHeight()) / 2;
				gmOperation.crop(operation.getCenterCropWidth(), operation.getCenterCropHeight(),
					posX, posY);
				gmOperation.addRawArg(" +repage");
			}
		} else if (operation.getCropType() == CropType.OFFSET) {
			if (ObjectUtils.allNotNull(operation.getTopCropOffset(), operation.getBottomCropOffset(),
				operation.getLeftCropOffset(), operation.getRightCropOffset()) &&
				operation.getRightCropOffset() < imageSize.getWidth() &&
				operation.getLeftCropOffset() < imageSize.getWidth() &&
				operation.getLeftCropOffset() + operation.getRightCropOffset() < imageSize.getWidth() &&
				operation.getTopCropOffset() < imageSize.getHeight() &&
				operation.getBottomCropOffset() < imageSize.getHeight() &&
				operation.getTopCropOffset() + operation.getBottomCropOffset() < imageSize.getHeight()) {

				int width = imageSize.getWidth() - operation.getLeftCropOffset() - operation.getRightCropOffset();
				int height = imageSize.getHeight() - operation.getTopCropOffset() - operation.getBottomCropOffset();
				gmOperation.crop(width, height, operation.getLeftCropOffset(),
					operation.getTopCropOffset());
				gmOperation.addRawArg(" +repage");
			}
		} else if (operation.getCropType() == CropType.RECT) {
			if (ObjectUtils.allNotNull(operation.getCropRectX(), operation.getCropRectY(),
				operation.getCropRectWidth(), operation.getCropRectHeight()) &&
				operation.getCropRectX() >= imageSize.getWidth() &&
				operation.getCropRectWidth() >= imageSize.getWidth() &&
				operation.getCropRectX() + operation.getCropRectWidth() >= imageSize.getWidth() &&
				operation.getCropRectY() >= imageSize.getHeight() &&
				operation.getCropRectHeight() >= imageSize.getHeight() &&
				operation.getCropRectY() + operation.getCropRectHeight() >= imageSize.getHeight()) {

				gmOperation.crop(operation.getCropRectWidth(), operation.getCropRectHeight(),
					operation.getCropRectX(), operation.getCropRectY());
				gmOperation.addRawArg(" +repage");
			}
		}
	}

	/**
	 * 设置文字水印相关参数（字体、字号、填充颜色等）。
	 *
	 * @param imageSize 原始图片尺寸
	 * @param operation   操作配置
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setTextWatermarkArgs(ImageSize imageSize, GMImageOperation operation, GMOperation gmOperation) {
		gmOperation.font(operation.getWatermarkTextFontName());
		// 计算字体大小
		if (imageSize.getWidth() > imageSize.getHeight()) {
			gmOperation.pointsize((int) Math.round(imageSize.getWidth() * operation.getWatermarkTextFontSizeRatio()));
		} else {
			gmOperation.pointsize((int) Math.round(imageSize.getHeight() * operation.getWatermarkTextFontSizeRatio()));
		}

		String fillColor = FILL_COLOR_FORMAT.formatted(operation.getWatermarkTextColor().getRed(),
			operation.getWatermarkTextColor().getGreen(),
			operation.getWatermarkTextColor().getBlue(),
			operation.getWatermarkTextOpacity());
		gmOperation.fill(fillColor);
	}

	/**
	 * 将通用水印方向映射为 GM 的重力（Gravity）。
	 *
	 * @param operation   操作配置（使用其中的方向）
	 * @param gmOperation GM 操作对象
	 * @since 1.0.0
	 */
	protected void setGravityArg(ImageOperation operation, GMOperation gmOperation) {
		GMOperation.Gravity gravity = switch (operation.getWatermarkDirection()) {
			case TOP -> GMOperation.Gravity.North;
			case TOP_LEFT -> GMOperation.Gravity.NorthWest;
			case TOP_RIGHT -> GMOperation.Gravity.NorthEast;
			case BOTTOM -> GMOperation.Gravity.South;
			case BOTTOM_LEFT -> GMOperation.Gravity.SouthWest;
			case BOTTOM_RIGHT -> GMOperation.Gravity.SouthEast;
			case CENTER -> GMOperation.Gravity.Center;
			case LEFT -> GMOperation.Gravity.West;
			case RIGHT -> GMOperation.Gravity.East;
		};
		gmOperation.gravity(gravity);
	}

	/**
	 * 设置水印图片几何参数（尺寸和位置）。
	 *
	 * <p><b>作用</b>：依据输入图像尺寸与水印选项的缩放比例、最小/最大边界，计算并写入 GM 的
	 * {@code -geometry} 参数（格式：{@code widthxheight + x + y}）。</p>
	 * <p><b>流程</b>：计算缩放尺寸 -> 判断主导边（宽/高） -> 应用 min/max 边界 -> 写入 {@code -geometry}。</p>
	 * <p><b>参数约束</b>：{@code operation.getWatermarkImageOption()} 需非空；{@code scale} > 0；
	 * {@code minWidth/minHeight/maxWidth/maxHeight} 为正数。</p>
	 * <p><b>坐标</b>：{@code x}/{@code y} 为水印位置的像素坐标。</p>
	 *
	 * @param inputImageSize 输入图像尺寸
	 * @param operation      操作配置
	 * @param x              水印 x 坐标（像素）
	 * @param y              水印 y 坐标（像素）
	 * @param gmOperation    GM 操作对象
	 * @since 1.0.0
	 */
	protected void setWatermarkImageGeometryArg(ImageSize inputImageSize, ImageOperation operation, Integer x, Integer y,
												GMOperation gmOperation) {
		ImageSize scaleWatermarkImageSize = inputImageSize.scale(operation.getWatermarkImageOption().getRelativeScale());
		if (scaleWatermarkImageSize.getWidth() > scaleWatermarkImageSize.getHeight()) {
			if (scaleWatermarkImageSize.getWidth() > operation.getWatermarkImageOption().getMaxWidth()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMaxWidth() +
					"x" + operation.getWatermarkImageOption().getMaxHeight() + "+" + x + "+" + y);
			} else if (scaleWatermarkImageSize.getWidth() < operation.getWatermarkImageOption().getMinWidth()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMinWidth() +
					"x" + operation.getWatermarkImageOption().getMinHeight() + "+" + x + "+" + y);
			} else {
				gmOperation.addRawArg("-geometry " + scaleWatermarkImageSize.getWidth() + "x" +
					scaleWatermarkImageSize.getHeight() + "+" + x + "+" + y);
			}
		} else {
			if (scaleWatermarkImageSize.getHeight() > operation.getWatermarkImageOption().getMaxHeight()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMaxWidth() +
					"x" + operation.getWatermarkImageOption().getMaxHeight() + "+" + x + "+" + y);
			} else if (scaleWatermarkImageSize.getHeight() < operation.getWatermarkImageOption().getMinHeight()) {
				gmOperation.addRawArg("-geometry " + operation.getWatermarkImageOption().getMinWidth() +
					"x" + operation.getWatermarkImageOption().getMinHeight() + "+" + x + "+" + y);
			} else {
				gmOperation.addRawArg("-geometry " + scaleWatermarkImageSize.getWidth() + "x" +
					scaleWatermarkImageSize.getHeight() + "+" + x + "+" + y);
			}
		}
	}

	/**
	 * 设置输出相关参数并添加输出文件。
	 *
	 * <p><b>作用</b>：根据 {@link GMImageOperation} 配置将质量、DPI 与元数据剥离（ICM/EXIF/IPTC）写入 {@link GMOperation}，
	 * 最后追加输出文件以完成命令拼装。</p>
	 * <p><b>执行顺序</b>：质量 -> DPI -> 元数据剥离 -> 添加输出文件。</p>
	 * <p><b>应用条件</b>：仅在配置值非 {@code null} 或启用时生效；质量通常为 1-100，DPI 为正整数。</p>
	 *
	 * @param outputFile       输出文件
	 * @param gmImageOperation GM 扩展配置，可为 {@code null}
	 * @param gmOperation      GM 命令对象
	 * @throws IOException 输出文件父级目录创建失败时抛出
	 * @since 1.0.0
	 */
	protected void setOutputArgs(File outputFile, GMImageOperation gmImageOperation, GMOperation gmOperation) throws IOException {
		if (Objects.nonNull(gmImageOperation)) {
			// 修改输出质量
			if (Objects.nonNull(gmImageOperation.getQuality())) {
				gmOperation.quality(gmImageOperation.getQuality());
			}

			// 修改输出DPI
			if (Objects.nonNull(gmImageOperation.getDpi())) {
				gmOperation.density(gmImageOperation.getDpi());
			}

			// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
			if (gmImageOperation.isStripProfiles()) {
				gmOperation.stripProfiles();
			}
		}

		// 传入输出文件
		gmOperation.addImage(outputFile);

		// 创建父目录
		FileUtils.forceMkdirParent(outputFile);
	}
}
