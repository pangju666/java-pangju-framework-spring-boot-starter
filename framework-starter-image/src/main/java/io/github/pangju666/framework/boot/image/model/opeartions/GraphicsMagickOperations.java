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

package io.github.pangju666.framework.boot.image.model.opeartions;

import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.framework.boot.image.enums.CompressionType;
import io.github.pangju666.framework.boot.image.enums.CropType;
import io.github.pangju666.framework.boot.image.enums.Direction;
import io.github.pangju666.framework.boot.image.enums.ResampleFilter;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource;
import io.github.pangju666.framework.boot.image.lang.ImageConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gm4java.im4java.GMOperation;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * GraphicsMagick图像操作类。
 * <p>
 * 继承自{@link ImageOperations}，使用GraphicsMagick进行图像处理。
 * 适用于需要高性能图像处理的场景，支持丰富的滤镜效果和输出配置。
 * </p>
 *
 * <p><strong>功能特性</strong></p>
 * <ul>
 *   <li>继承自ImageOperations的所有功能：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
 *   <li>重采样滤镜配置：支持多种重采样算法</li>
 *   <li>滤镜效果：模糊、高斯模糊、中值模糊、锐化、反锐化掩模、浮雕、阈值化</li>
 *   <li>色彩调整：亮度、饱和度、色相</li>
 *   <li>图像水印：支持方向定位、尺寸限制、透明度、边距等配置</li>
 *   <li>文字水印：支持字体、颜色、描边、透明度、边距等配置</li>
 *   <li>输出配置：质量、DPI、压缩类型、去除元数据等</li>
 *   <li>流式API：所有操作方法返回当前实例，支持链式调用</li>
 *   <li>命令转换：提供将配置转换为GraphicsMagick命令的方法</li>
 * </ul>
 *
 * <p><strong>滤镜效果说明</strong></p>
 * <ul>
 *   <li>模糊：使用卷积核对图像进行平滑处理</li>
 *   <li>高斯模糊：使用高斯核对图像进行平滑处理，效果更自然</li>
 *   <li>中值模糊：使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效</li>
 *   <li>锐化：增强图像边缘和细节</li>
 *   <li>反锐化掩模：更高级的锐化技术，可以控制锐化强度和阈值</li>
 *   <li>浮雕：创建立体感，使图像具有浮雕的外观</li>
 *   <li>阈值化：将图像转换为二值图像</li>
 * </ul>
 *
 * <p><strong>使用示例</strong></p>
 * <pre>{@code
 * // 创建GraphicsMagick操作实例并设置缩放和滤镜
 * GraphicsMagickOperations ops = new GraphicsMagickOperations()
 *     .scale(800, 600)
 *     .blur()
 *     .watermarkImage(resource, size)
 *     .brightness(110);
 *
 * // 从现有操作对象复制配置
 * GraphicsMagickOperations newOps = new GraphicsMagickOperations(ops);
 *
 * // 转换为GraphicsMagick命令
 * GMOperation gmOp = ops.toConvertGMOperation(resource, outputFile);
 * }</pre>
 *
 * <p><strong>注意事项</strong></p>
 * <ul>
 *   <li>图像水印和文字水印互斥，设置其中一个会清除另一个</li>
 *   <li>滤镜效果可以组合使用，但顺序会影响最终效果</li>
 *   <li>亮度、饱和度、色相的默认值为100，表示不调整</li>
 *   <li><b>强烈建议使用图像水印而非文字水印</b>。由于无法获取图像执行其他操作（如缩放、裁剪）后的实际尺寸，无法动态计算文字水印的字体大小以适配图像尺寸，必须自行设置文字水印的字体大小</li>
 *   <li>如果必须使用文字水印，建议手动传入坐标而非使用方向定位。由于无法获取文字渲染后的实际高度，无法准确计算上下方位的坐标偏移，可能导致水印位置不准确</li>
 *   <li>水印方向和坐标互斥，设置其中一个会清除另一个</li>
 *   <li>滤镜操作会自动触发convert命令</li>
 *   <li>GraphicsMagick输入/输出文件路径不支持中文或非ASCII字符，需要使用纯英文路径，否则可能导致命令执行失败</li>
 * </ul>
 *
 * @author pangju666
 * @see ImageOperations
 * @see org.gm4java.im4java.GMOperation
 * @see ResampleFilter
 * @since 2.1.0
 */
public class GraphicsMagickOperations extends ImageOperations<GraphicsMagickOperations> {
	/**
	 * 绘制文本参数格式。
	 * <p>
	 * 用于格式化GraphicsMagick的draw命令参数。
	 * </p>
	 * <p>
	 * 格式为："text x y 'text'"，其中x和y为坐标，text为文本内容。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final String DRAW_TEXT_ARG_FORMAT = "\"text %d %d '%s'\"";
	/**
	 * 颜色格式。
	 * <p>
	 * 用于格式化RGBA颜色值。
	 * </p>
	 * <p>
	 * 格式为：rgba(r,g,b,a)，其中r、g、b为0-255的整数值，a为0.0-1.0的浮点值。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final String COLOR_FORMAT = "rgba(%d,%d,%d,%.1f)";

	/**
	 * 重采样滤镜。
	 * <p>
	 * 用于指定图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置重采样滤镜，使用GraphicsMagick默认算法。
	 * </p>
	 * <p>
	 * 常用的重采样算法包括：
	 * <ul>
	 *   <li>POINT：最近邻插值，速度最快但质量最低</li>
	 *   <li>BOX：盒式滤波器</li>
	 *   <li>TRIANGLE：三角形滤波器</li>
	 *   <li>HERMITE：Hermite滤波器</li>
	 *   <li>HANNING：Hanning滤波器</li>
	 *   <li>HAMMING：Hamming滤波器</li>
	 *   <li>BLACKMAN：Blackman滤波器</li>
	 *   <li>GAUSSIAN：高斯滤波器</li>
	 *   <li>QUADRATIC：二次滤波器</li>
	 *   <li>CUBIC：三次滤波器</li>
	 *   <li>CATROM：Catmull-Rom滤波器</li>
	 *   <li>MITCHELL：Mitchell滤波器</li>
	 *   <li>LANCZOS：Lanczos滤波器，质量最高</li>
	 *   <li>BESSEL：Bessel滤波器</li>
	 *   <li>SINC：Sinc滤波器</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ResampleFilter resizeFilter;

	/**
	 * 亮度。
	 * <p>
	 * 用于调整图像的亮度。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整亮度。
	 * 值大于100表示增加亮度，值小于100表示降低亮度。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int brightness = 100;
	/**
	 * 饱和度。
	 * <p>
	 * 用于调整图像的饱和度。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整饱和度。
	 * 值大于100表示增加饱和度，值小于100表示降低饱和度。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int saturation = 100;
	/**
	 * 色相。
	 * <p>
	 * 用于调整图像的色相。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整色相。
	 * 值大于100表示色相偏移，值小于100表示反向偏移。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int hue = 100;
	/**
	 * 模糊核参数。
	 * <p>
	 * 用于存储模糊效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * 半径决定了模糊的范围，sigma决定了模糊的平滑程度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> blurKernel;
	/**
	 * 高斯模糊核参数。
	 * <p>
	 * 用于存储高斯模糊效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比普通模糊更自然。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置高斯模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> gaussianKernel;
	/**
	 * 中值模糊半径。
	 * <p>
	 * 用于指定中值模糊的半径。
	 * </p>
	 * <p>
	 * 中值模糊使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效。
	 * 半径决定了中值滤波的窗口大小。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中值模糊效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Double medianRadius;
	/**
	 * 锐化核参数。
	 * <p>
	 * 用于存储锐化效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Pair<Double, Double> sharpenKernel;
	/**
	 * 反锐化掩模核参数。
	 * <p>
	 * 用于存储反锐化掩模效果的参数。
	 * </p>
	 * <p>
	 * 反锐化掩模是一种更高级的锐化技术，可以控制锐化强度和阈值。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置反锐化掩模效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected UnsharpKernelArgs unsharpKernel;
	/**
	 * 浮雕半径。
	 * <p>
	 * 用于指定浮雕效果的半径。
	 * </p>
	 * <p>
	 * 浮雕效果创建立体感，使图像具有浮雕的外观。
	 * 半径决定了浮雕效果的强度。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置浮雕效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Double embossRadius;
	/**
	 * 阈值百分比。
	 * <p>
	 * 用于指定阈值化的阈值百分比。
	 * </p>
	 * <p>
	 * 阈值化将图像转换为二值图像。
	 * 百分比范围为0-1，值越大，阈值越高。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置阈值化效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Double thresholdPercent;

	/**
	 * 水印方向。
	 * <p>
	 * 用于指定水印在图像中的位置方向。
	 * </p>
	 * <p>
	 * 默认值为右上角（TOP_RIGHT）。
	 * </p>
	 * <p>
	 * 可选方向包括：
	 * <ul>
	 *   <li>TOP：顶部居中</li>
	 *   <li>TOP_LEFT：左上角</li>
	 *   <li>TOP_RIGHT：右上角</li>
	 *   <li>BOTTOM：底部居中</li>
	 *   <li>BOTTOM_LEFT：左下角</li>
	 *   <li>BOTTOM_RIGHT：右下角</li>
	 *   <li>CENTER：中心</li>
	 *   <li>LEFT：左侧居中</li>
	 *   <li>RIGHT：右侧居中</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>水印方向与坐标互斥，设置其中一个会清除另一个</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Direction watermarkDirection = Direction.TOP_RIGHT;
	/**
	 * 水印X坐标。
	 * <p>
	 * 用于指定水印在图像中的X坐标。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置水印X坐标，使用水印方向定位。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>水印坐标与方向互斥，设置坐标会清除方向</li>
	 *   <li>坐标原点为图像左上角</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer watermarkX;
	/**
	 * 水印Y坐标。
	 * <p>
	 * 用于指定水印在图像中的Y坐标。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置水印Y坐标，使用水印方向定位。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>水印坐标与方向互斥，设置坐标会清除方向</li>
	 *   <li>坐标原点为图像左上角</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer watermarkY;

	/**
	 * 文字水印文本。
	 * <p>
	 * 用于指定作为水印的文本内容。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置文字水印。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>文字水印与图像水印互斥，设置文字水印会清除图像水印</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String watermarkText;
	/**
	 * 文字水印字体。
	 * <p>
	 * 用于指定文字水印的字体。
	 * </p>
	 * <p>
	 * 可以是字体文件的绝对路径，也可以是系统字体名称。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置字体。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String watermarkTextFont;
	/**
	 * 文字水印透明度。
	 * <p>
	 * 用于指定文字水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * 范围为0-1，值越小，透明度越高。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected float watermarkTextOpacity = 0.4f;
	/**
	 * 文字水印填充颜色。
	 * <p>
	 * 用于指定文字水印的填充颜色。
	 * </p>
	 * <p>
	 * 默认值为白色。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Color watermarkTextFillColor = Color.WHITE;
	/**
	 * 文字水印描边颜色。
	 * <p>
	 * 用于指定文字水印的描边颜色。
	 * </p>
	 * <p>
	 * 默认值为黑色。
	 * </p>
	 * <p>
	 * 描边可以增强文字在水印中的可读性。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Color watermarkTextStrokeColor = Color.BLACK;
	/**
	 * 文字水印描边宽度。
	 * <p>
	 * 用于指定文字水印的描边宽度。
	 * </p>
	 * <p>
	 * 默认值为1。
	 * 值越大，描边越粗。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextStrokeWidth = 1;
	/**
	 * 是否启用文字水印描边。
	 * <p>
	 * 用于控制是否对文字水印进行描边。
	 * </p>
	 * <p>
	 * 默认值为true。
	 * </p>
	 * <p>
	 * 描边可以增强文字在水印中的可读性。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected boolean watermarkTextStroke = true;
	/**
	 * 文字水印字体大小。
	 * <p>
	 * 用于指定文字水印的字体大小。
	 * </p>
	 * <p>
	 * 默认值为24。
	 * 值越大，字体越大。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextFontSize = 24;
	/**
	 * 文字水印边距。
	 * <p>
	 * 用于指定文字水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 默认值为10。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int watermarkTextMargin = 10;

	/**
	 * 图像水印资源。
	 * <p>
	 * 用于指定作为水印的图像资源。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置图像水印。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>图像水印与文字水印互斥，设置图像水印会清除文字水印</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected IOResource watermarkImage;
	/**
	 * 图像水印相对缩放因子。
	 * <p>
	 * 用于指定图像水印相对于目标图像的缩放比例。
	 * </p>
	 * <p>
	 * 默认值为0.15，表示水印尺寸为目标图像的15%。
	 * 值必须大于0，值越大，水印越大。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected double watermarkImageRelativeScaleFactor = 0.15;
	/**
	 * 图像水印透明度。
	 * <p>
	 * 用于指定图像水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * 范围为0-1，值越小，透明度越高。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected float watermarkImageOpacity = 0.4f;
	/**
	 * 图像水印尺寸。
	 * <p>
	 * 用于指定图像水印的尺寸。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置固定尺寸，使用相对缩放因子计算。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ImageSize watermarkImageSize;
	/**
	 * 图像水印边距。
	 * <p>
	 * 用于指定图像水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 默认值为20。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected int watermarkImageMargin = 20;
	/**
	 * 图像水印尺寸限制策略。
	 * <p>
	 * 用于根据目标图像尺寸计算水印的尺寸范围。
	 * </p>
	 * <p>
	 * 返回一个Pair，左值为最小尺寸，右值为最大尺寸。
	 * </p>
	 * <p>
	 * 默认策略：
	 * <ul>
	 *   <li>小图（短边<600）：120x120到150x150</li>
	 *   <li>中等图（600<=短边<1920）：150x150到250x250</li>
	 *   <li>大图（短边>=1920）：250x250到400x400</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Function<ImageSize, Pair<ImageSize, ImageSize>> watermarkImageSizeLimitStrategy = imageSize -> {
		int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
		if (shorter < 600) { // 小图
			return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
		} else { // 中等图
			return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
		}
	};

	/**
	 * 是否去除元数据。
	 * <p>
	 * 用于控制是否去除图像的元数据（如EXIF、IPTC、ICM等）。
	 * </p>
	 * <p>
	 * 去除元数据可以减小文件大小，保护隐私。
	 * </p>
	 * <p>
	 * 默认值为false。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected boolean stripProfiles;
	/**
	 * 输出DPI。
	 * <p>
	 * 用于指定输出图像的DPI（每英寸点数）。
	 * </p>
	 * <p>
	 * 如果返回null，表示不修改DPI。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer dpi;
	/**
	 * 输出质量。
	 * <p>
	 * 用于指定输出图像的质量。
	 * </p>
	 * <p>
	 * 质量范围为1-100，值越大，质量越高，文件越大。
	 * 如果返回null，表示不修改质量。
	 * </p>
	 * <p>
	 * 仅对支持质量控制的格式（如JPEG）有效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected Integer quality;
	/**
	 * 压缩类型。
	 * <p>
	 * 用于指定输出图像的压缩类型。
	 * </p>
	 * <p>
	 * 如果返回null，表示不修改压缩类型。
	 * </p>
	 * <p>
	 * 仅对支持压缩类型控制的格式有效。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected CompressionType compression;

	/**
	 * 是否触发convert命令。
	 * <p>
	 * 用于标记是否需要执行GraphicsMagick的convert命令。
	 * </p>
	 * <p>
	 * 滤镜操作会自动设置为true。
	 * </p>
	 * <p>
	 * 默认值为false。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected boolean triggerConvert;

	/**
	 * 默认构造函数。
	 * <p>
	 * 创建一个空的GraphicsMagick图像操作实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 默认值：
	 * <ul>
	 *   <li>重采样滤镜：null</li>
	 *   <li>亮度：100</li>
	 *   <li>饱和度：100</li>
	 *   <li>色相：100</li>
	 *   <li>所有滤镜参数：null</li>
	 *   <li>水印方向：TOP_RIGHT</li>
	 *   <li>水印坐标：null</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印字体：null</li>
	 *   <li>文字水印透明度：0.4</li>
	 *   <li>文字水印填充颜色：白色</li>
	 *   <li>文字水印描边颜色：黑色</li>
	 *   <li>文字水印描边宽度：1</li>
	 *   <li>文字水印描边：true</li>
	 *   <li>文字水印字体大小：24</li>
	 *   <li>文字水印边距：10</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印相对缩放因子：0.15</li>
	 *   <li>图像水印透明度：0.4</li>
	 *   <li>图像水印尺寸：null</li>
	 *   <li>图像水印边距：20</li>
	 *   <li>输出质量：null</li>
	 *   <li>去除元数据：false</li>
	 *   <li>输出DPI：null</li>
	 *   <li>压缩类型：null</li>
	 *   <li>触发convert命令：false</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 继承自ImageOperations的配置也会设置为默认值。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations() {
		super();
	}

	/**
	 * 拷贝构造函数。
	 * <p>
	 * 从现有的图像操作对象复制所有配置参数到新实例。
	 * </p>
	 * <p>
	 * 如果源操作对象为null，则创建一个空的实例，所有配置参数都设置为默认值。
	 * </p>
	 * <p>
	 * 如果源操作对象不是GraphicsMagick操作实例，则只复制继承自ImageOperations的配置。
	 * </p>
	 * <p>
	 * 复制的配置包括：
	 * <ul>
	 *   <li>继承自ImageOperations的配置：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
	 *   <li>重采样滤镜</li>
	 *   <li>所有滤镜参数</li>
	 *   <li>色彩调整参数</li>
	 *   <li>水印坐标相关配置</li>
	 *   <li>文字水印相关配置</li>
	 *   <li>图像水印相关配置</li>
	 *   <li>输出配置</li>
	 *   <li>触发convert命令标志</li>
	 * </ul>
	 * </p>
	 *
	 * @param operations 源操作对象，可以为null
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations(@Nullable ImageOperations<?> operations) {
		super(operations);

		if (Objects.nonNull(operations) && operations instanceof GraphicsMagickOperations graphicsMagickOperations) {
			/* 缩放相关配置 */
			this.resizeFilter = graphicsMagickOperations.resizeFilter;

			/* 滤镜相关配置 */
			this.blurKernel = graphicsMagickOperations.blurKernel;
			this.gaussianKernel = graphicsMagickOperations.gaussianKernel;
			this.medianRadius = graphicsMagickOperations.medianRadius;
			this.sharpenKernel = graphicsMagickOperations.sharpenKernel;
			this.unsharpKernel = graphicsMagickOperations.unsharpKernel;
			this.embossRadius = graphicsMagickOperations.embossRadius;
			this.thresholdPercent = graphicsMagickOperations.thresholdPercent;
			this.brightness = graphicsMagickOperations.brightness;
			this.saturation = graphicsMagickOperations.saturation;
			this.hue = graphicsMagickOperations.hue;

			/* 水印坐标相关配置 */
			this.watermarkDirection = graphicsMagickOperations.watermarkDirection;
			this.watermarkX = graphicsMagickOperations.watermarkX;
			this.watermarkY = graphicsMagickOperations.watermarkY;

			/* 文字水印相关配置 */
			this.watermarkText = graphicsMagickOperations.watermarkText;
			this.watermarkTextOpacity = graphicsMagickOperations.watermarkTextOpacity;
			this.watermarkTextFillColor = graphicsMagickOperations.watermarkTextFillColor;
			this.watermarkTextFont = graphicsMagickOperations.watermarkTextFont;
			this.watermarkTextStrokeColor = graphicsMagickOperations.watermarkTextStrokeColor;
			this.watermarkTextStrokeWidth = graphicsMagickOperations.watermarkTextStrokeWidth;
			this.watermarkTextStroke = graphicsMagickOperations.watermarkTextStroke;
			this.watermarkTextFontSize = graphicsMagickOperations.watermarkTextFontSize;
			this.watermarkTextMargin = graphicsMagickOperations.watermarkTextMargin;

			/* 图像水印相关配置 */
			this.watermarkImage = graphicsMagickOperations.watermarkImage;
			this.watermarkImageRelativeScaleFactor = graphicsMagickOperations.watermarkImageRelativeScaleFactor;
			this.watermarkImageOpacity = graphicsMagickOperations.watermarkImageOpacity;
			this.watermarkImageSizeLimitStrategy = graphicsMagickOperations.watermarkImageSizeLimitStrategy;
			this.watermarkImageSize = graphicsMagickOperations.watermarkImageSize;
			this.watermarkImageMargin = graphicsMagickOperations.watermarkImageMargin;

			/* 输出配置 */
			this.quality = graphicsMagickOperations.quality;
			this.stripProfiles = graphicsMagickOperations.stripProfiles;
			this.dpi = graphicsMagickOperations.dpi;
			this.compression = graphicsMagickOperations.compression;

			this.triggerConvert = graphicsMagickOperations.triggerConvert;
		}
	}

	/**
	 * 获取重采样滤镜。
	 * <p>
	 * 返回图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置重采样滤镜，使用GraphicsMagick默认算法。
	 * </p>
	 *
	 * @return 重采样滤镜，可能为null
	 * @since 2.1.0
	 */
	public @Nullable ResampleFilter getResizeFilter() {
		return resizeFilter;
	}

	/**
	 * 获取高斯模糊核参数。
	 * <p>
	 * 返回高斯模糊效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置高斯模糊效果。
	 * </p>
	 *
	 * @return 高斯模糊核参数（半径和sigma），可能为null
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getGaussianKernel() {
		return gaussianKernel;
	}

	/**
	 * 获取模糊核参数。
	 * <p>
	 * 返回模糊效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置模糊效果。
	 * </p>
	 *
	 * @return 模糊核参数（半径和sigma），可能为null
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getBlurKernel() {
		return blurKernel;
	}

	/**
	 * 获取中值模糊半径。
	 * <p>
	 * 返回中值模糊的半径。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置中值模糊效果。
	 * </p>
	 *
	 * @return 中值模糊半径，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Double getMedianRadius() {
		return medianRadius;
	}

	/**
	 * 获取锐化核参数。
	 * <p>
	 * 返回锐化效果的半径和sigma参数。
	 * </p>
	 * <p>
	 * Pair的左值为半径，右值为sigma。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置锐化效果。
	 * </p>
	 *
	 * @return 锐化核参数（半径和sigma），可能为null
	 * @since 2.1.0
	 */
	public @Nullable Pair<Double, Double> getSharpenKernel() {
		return sharpenKernel;
	}

	/**
	 * 获取反锐化掩模核参数。
	 * <p>
	 * 返回反锐化掩模效果的参数。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置反锐化掩模效果。
	 * </p>
	 *
	 * @return 反锐化掩模核参数，可能为null
	 * @since 2.1.0
	 */
	public @Nullable UnsharpKernelArgs getUnsharpKernel() {
		return unsharpKernel;
	}

	/**
	 * 获取浮雕半径。
	 * <p>
	 * 返回浮雕效果的半径。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置浮雕效果。
	 * </p>
	 *
	 * @return 浮雕半径，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Double getEmbossRadius() {
		return embossRadius;
	}

	/**
	 * 获取阈值百分比。
	 * <p>
	 * 返回阈值化的阈值百分比。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置阈值化效果。
	 * </p>
	 *
	 * @return 阈值百分比，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Double getThresholdPercent() {
		return thresholdPercent;
	}

	/**
	 * 是否启用文字水印描边。
	 * <p>
	 * 返回是否对文字水印进行描边。
	 * </p>
	 * <p>
	 * 默认值为true。
	 * </p>
	 *
	 * @return 如果启用返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isWatermarkTextStroke() {
		return watermarkTextStroke;
	}

	/**
	 * 获取水印方向。
	 * <p>
	 * 返回水印在图像中的位置方向。
	 * </p>
	 * <p>
	 * 默认值为右上角（TOP_RIGHT）。
	 * </p>
	 *
	 * @return 水印方向
	 * @since 2.1.0
	 */
	public @Nullable Direction getWatermarkDirection() {
		return watermarkDirection;
	}

	/**
	 * 获取水印X坐标。
	 * <p>
	 * 返回水印在图像中的X坐标。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置水印X坐标，使用水印方向定位。
	 * </p>
	 *
	 * @return 水印X坐标，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getWatermarkX() {
		return watermarkX;
	}

	/**
	 * 获取水印Y坐标。
	 * <p>
	 * 返回水印在图像中的Y坐标。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置水印Y坐标，使用水印方向定位。
	 * </p>
	 *
	 * @return 水印Y坐标，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getWatermarkY() {
		return watermarkY;
	}

	/**
	 * 获取文字水印文本。
	 * <p>
	 * 返回作为水印的文本内容。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置文字水印。
	 * </p>
	 *
	 * @return 文字水印文本，可能为null
	 * @since 2.1.0
	 */
	public @Nullable String getWatermarkText() {
		return watermarkText;
	}

	/**
	 * 获取文字水印字体。
	 * <p>
	 * 返回文字水印的字体。
	 * </p>
	 * <p>
	 * 可以是字体文件的绝对路径，也可以是系统字体名称。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置字体。
	 * </p>
	 *
	 * @return 文字水印字体，可能为null
	 * @since 2.1.0
	 */
	public @Nullable String getWatermarkTextFont() {
		return watermarkTextFont;
	}

	/**
	 * 获取文字水印透明度。
	 * <p>
	 * 返回文字水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * </p>
	 *
	 * @return 文字水印透明度
	 * @since 2.1.0
	 */
	public float getWatermarkTextOpacity() {
		return watermarkTextOpacity;
	}

	/**
	 * 获取文字水印填充颜色。
	 * <p>
	 * 返回文字水印的填充颜色。
	 * </p>
	 * <p>
	 * 默认值为白色。
	 * </p>
	 *
	 * @return 文字水印填充颜色
	 * @since 2.1.0
	 */
	public Color getWatermarkTextFillColor() {
		return watermarkTextFillColor;
	}

	/**
	 * 获取文字水印描边颜色。
	 * <p>
	 * 返回文字水印的描边颜色。
	 * </p>
	 * <p>
	 * 默认值为黑色。
	 * </p>
	 *
	 * @return 文字水印描边颜色
	 * @since 2.1.0
	 */
	public Color getWatermarkTextStrokeColor() {
		return watermarkTextStrokeColor;
	}

	/**
	 * 获取文字水印描边宽度。
	 * <p>
	 * 返回文字水印的描边宽度。
	 * </p>
	 * <p>
	 * 默认值为1。
	 * </p>
	 *
	 * @return 文字水印描边宽度
	 * @since 2.1.0
	 */
	public int getWatermarkTextStrokeWidth() {
		return watermarkTextStrokeWidth;
	}

	/**
	 * 获取文字水印字体大小。
	 * <p>
	 * 返回文字水印的字体大小。
	 * </p>
	 * <p>
	 * 默认值为24。
	 * </p>
	 *
	 * @return 文字水印字体大小
	 * @since 2.1.0
	 */
	public int getWatermarkTextFontSize() {
		return watermarkTextFontSize;
	}

	/**
	 * 获取文字水印边距。
	 * <p>
	 * 返回文字水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 默认值为10。
	 * </p>
	 *
	 * @return 文字水印边距
	 * @since 2.1.0
	 */
	public int getWatermarkTextMargin() {
		return watermarkTextMargin;
	}

	/**
	 * 获取图像水印资源。
	 * <p>
	 * 返回作为水印的图像资源。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置图像水印。
	 * </p>
	 *
	 * @return 图像水印资源，可能为null
	 * @since 2.1.0
	 */
	public @Nullable IOResource getWatermarkImage() {
		return watermarkImage;
	}

	/**
	 * 获取图像水印相对缩放因子。
	 * <p>
	 * 返回图像水印相对于目标图像的缩放比例。
	 * </p>
	 * <p>
	 * 默认值为0.15。
	 * </p>
	 *
	 * @return 图像水印相对缩放因子
	 * @since 2.1.0
	 */
	public double getWatermarkImageRelativeScaleFactor() {
		return watermarkImageRelativeScaleFactor;
	}

	/**
	 * 获取图像水印透明度。
	 * <p>
	 * 返回图像水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * </p>
	 *
	 * @return 图像水印透明度
	 * @since 2.1.0
	 */
	public float getWatermarkImageOpacity() {
		return watermarkImageOpacity;
	}

	/**
	 * 获取图像水印尺寸。
	 * <p>
	 * 返回图像水印的尺寸。
	 * </p>
	 * <p>
	 * 如果返回null，表示未设置固定尺寸，使用相对缩放因子计算。
	 * </p>
	 *
	 * @return 图像水印尺寸，可能为null
	 * @since 2.1.0
	 */
	public @Nullable ImageSize getWatermarkImageSize() {
		return watermarkImageSize;
	}

	/**
	 * 获取图像水印边距。
	 * <p>
	 * 返回图像水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 默认值为20。
	 * </p>
	 *
	 * @return 图像水印边距
	 * @since 2.1.0
	 */
	public int getWatermarkImageMargin() {
		return watermarkImageMargin;
	}

	/**
	 * 获取图像水印尺寸限制策略。
	 * <p>
	 * 返回根据目标图像尺寸计算水印尺寸范围的策略。
	 * </p>
	 * <p>
	 * 返回一个Pair，左值为最小尺寸，右值为最大尺寸。
	 * </p>
	 *
	 * @return 图像水印尺寸限制策略
	 * @since 2.1.0
	 */
	public Function<ImageSize, Pair<ImageSize, ImageSize>> getWatermarkImageSizeLimitStrategy() {
		return watermarkImageSizeLimitStrategy;
	}

	/**
	 * 获取输出质量。
	 * <p>
	 * 返回输出图像的质量。
	 * </p>
	 * <p>
	 * 如果返回null，表示不修改质量。
	 * </p>
	 *
	 * @return 输出质量，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getQuality() {
		return quality;
	}

	/**
	 * 是否去除元数据。
	 * <p>
	 * 返回是否去除图像的元数据。
	 * </p>
	 * <p>
	 * 默认值为false。
	 * </p>
	 *
	 * @return 如果去除返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isStripProfiles() {
		return stripProfiles;
	}

	/**
	 * 获取输出DPI。
	 * <p>
	 * 返回输出图像的DPI。
	 * </p>
	 * <p>
	 * 如果返回null，表示不修改DPI。
	 * </p>
	 *
	 * @return 输出DPI，可能为null
	 * @since 2.1.0
	 */
	public @Nullable Integer getDpi() {
		return dpi;
	}

	/**
	 * 获取压缩类型。
	 * <p>
	 * 返回输出图像的压缩类型。
	 * </p>
	 * <p>
	 * 如果返回null，表示不修改压缩类型。
	 * </p>
	 *
	 * @return 压缩类型，可能为null
	 * @since 2.1.0
	 */
	public @Nullable CompressionType getCompression() {
		return compression;
	}

	/**
	 * 获取亮度。
	 * <p>
	 * 返回图像的亮度值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整亮度。
	 * </p>
	 *
	 * @return 亮度
	 * @since 2.1.0
	 */
	public int getBrightness() {
		return brightness;
	}

	/**
	 * 获取饱和度。
	 * <p>
	 * 返回图像的饱和度值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整饱和度。
	 * </p>
	 *
	 * @return 饱和度
	 * @since 2.1.0
	 */
	public int getSaturation() {
		return saturation;
	}

	/**
	 * 获取色相。
	 * <p>
	 * 返回图像的色相值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整色相。
	 * </p>
	 *
	 * @return 色相
	 * @since 2.1.0
	 */
	public int getHue() {
		return hue;
	}

	/**
	 * 设置重采样滤镜。
	 * <p>
	 * 设置图像缩放时使用的重采样算法。
	 * </p>
	 * <p>
	 * 常用的重采样算法包括：
	 * <ul>
	 *   <li>POINT：最近邻插值，速度最快但质量最低</li>
	 *   <li>BOX：盒式滤波器</li>
	 *   <li>TRIANGLE：三角形滤波器</li>
	 *   <li>HERMITE：Hermite滤波器</li>
	 *   <li>HANNING：Hanning滤波器</li>
	 *   <li>HAMMING：Hamming滤波器</li>
	 *   <li>BLACKMAN：Blackman滤波器</li>
	 *   <li>GAUSSIAN：高斯滤波器</li>
	 *   <li>QUADRATIC：二次滤波器</li>
	 *   <li>CUBIC：三次滤波器</li>
	 *   <li>CATROM：Catmull-Rom滤波器</li>
	 *   <li>MITCHELL：Mitchell滤波器</li>
	 *   <li>LANCZOS：Lanczos滤波器，质量最高</li>
	 *   <li>BESSEL：Bessel滤波器</li>
	 *   <li>SINC：Sinc滤波器</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果重采样滤镜为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的重采样滤镜设置</li>
	 *   <li>重采样算法的选择会影响缩放质量和性能</li>
	 * </ul>
	 * </p>
	 *
	 * @param resizeFilter 重采样滤镜，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations resizeFilter(@Nullable ResampleFilter resizeFilter) {
		if (Objects.nonNull(resizeFilter)) {
			this.resizeFilter = resizeFilter;
		}
		return this;
	}

	/**
	 * 设置模糊效果（默认参数）。
	 * <p>
	 * 使用默认参数（半径0，sigma 3）应用模糊效果。
	 * </p>
	 * <p>
	 * 模糊效果使用卷积核对图像进行平滑处理，可以减少噪声。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur() {
		return blur(0d, 3d);
	}

	/**
	 * 设置模糊效果（仅sigma）。
	 * <p>
	 * 使用指定的sigma值和默认半径（0）应用模糊效果。
	 * </p>
	 * <p>
	 * 模糊效果使用卷积核对图像进行平滑处理，可以减少噪声。
	 * Sigma决定了模糊的平滑程度。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #blur(Double, Double)}并传入默认半径。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param sigma 模糊sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur(@Nullable Double sigma) {
		return blur(0d, sigma);
	}

	/**
	 * 设置模糊效果。
	 * <p>
	 * 使用指定的半径和sigma值应用模糊效果。
	 * </p>
	 * <p>
	 * 模糊效果使用卷积核对图像进行平滑处理，可以减少噪声。
	 * 半径决定了模糊的范围，sigma决定了模糊的平滑程度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #blurKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果半径或sigma为null、sigma小于等于0或半径小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的模糊设置</li>
	 *   <li>半径必须大于等于0</li>
	 *   <li>sigma必须大于0</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 模糊半径，必须大于等于0，可以为null
	 * @param sigma 模糊sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations blur(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.blurKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置高斯模糊效果（默认参数）。
	 * <p>
	 * 使用默认参数（半径0，sigma 5）应用高斯模糊效果。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比普通模糊更自然。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #gaussianKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian() {
		return gaussian(0d, 5d);
	}

	/**
	 * 设置高斯模糊效果（仅sigma）。
	 * <p>
	 * 使用指定的sigma值和默认半径（0）应用高斯模糊效果。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比普通模糊更自然。
	 * Sigma决定了模糊的平滑程度。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #gaussian(Double, Double)}并传入默认半径。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param sigma 高斯模糊sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian(@Nullable Double sigma) {
		return gaussian(0d, sigma);
	}

	/**
	 * 设置高斯模糊效果。
	 * <p>
	 * 使用指定的半径和sigma值应用高斯模糊效果。
	 * </p>
	 * <p>
	 * 高斯模糊使用高斯核对图像进行平滑处理，效果比普通模糊更自然。
	 * 半径决定了模糊的范围，sigma决定了模糊的平滑程度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #gaussianKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果半径或sigma为null、sigma小于等于0或半径小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的高斯模糊设置</li>
	 *   <li>半径必须大于等于0</li>
	 *   <li>sigma必须大于0</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 高斯模糊半径，必须大于等于0，可以为null
	 * @param sigma 高斯模糊sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations gaussian(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.gaussianKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置中值模糊效果（默认半径）。
	 * <p>
	 * 使用默认半径（3）应用中值模糊效果。
	 * </p>
	 * <p>
	 * 中值模糊使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #medianRadius}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的中值模糊设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations median() {
		return median(3d);
	}

	/**
	 * 设置中值模糊效果。
	 * <p>
	 * 使用指定的半径应用中值模糊效果。
	 * </p>
	 * <p>
	 * 中值模糊使用中值滤波对图像进行平滑处理，对去除椒盐噪声特别有效。
	 * 半径决定了中值滤波的窗口大小。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #medianRadius}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果半径为null或小于1，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的中值模糊设置</li>
	 *   <li>半径必须大于等于1</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 中值模糊半径，必须大于等于1，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations median(@Nullable Double radius) {
		if (Objects.nonNull(radius) && radius >= 1) {
			this.medianRadius = radius;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置锐化效果（默认参数）。
	 * <p>
	 * 使用默认参数（半径0，sigma 1）应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #sharpenKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen() {
		return sharpen(0d, 1d);
	}

	/**
	 * 设置锐化效果（仅sigma）。
	 * <p>
	 * 使用指定的sigma值和默认半径（0）应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * Sigma决定了锐化的强度。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #sharpen(Double, Double)}并传入默认半径。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param sigma 锐化sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen(@Nullable Double sigma) {
		return sharpen(0d, sigma);
	}

	/**
	 * 设置锐化效果。
	 * <p>
	 * 使用指定的半径和sigma值应用锐化效果。
	 * </p>
	 * <p>
	 * 锐化效果增强图像边缘和细节，使图像更清晰。
	 * 半径决定了锐化的范围，sigma决定了锐化的强度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #sharpenKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果半径或sigma为null、sigma小于等于0或半径小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的锐化设置</li>
	 *   <li>半径必须大于等于0</li>
	 *   <li>sigma必须大于0</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 锐化半径，必须大于等于0，可以为null
	 * @param sigma 锐化sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations sharpen(@Nullable Double radius, @Nullable Double sigma) {
		if (ObjectUtils.allNotNull(radius, sigma) && sigma > 0 && radius >= 0) {
			this.sharpenKernel = Pair.of(radius, sigma);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置反锐化掩模效果（默认参数）。
	 * <p>
	 * 使用默认参数（半径0，sigma 1，强度1，阈值0.05）应用反锐化掩模效果。
	 * </p>
	 * <p>
	 * 反锐化掩模是一种更高级的锐化技术，可以控制锐化强度和阈值。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #unsharpKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的反锐化掩模设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp() {
		return unsharp(0d, 1d, 1d, 0.05);
	}

	/**
	 * 设置反锐化掩模效果（仅sigma）。
	 * <p>
	 * 使用指定的sigma值和默认参数（半径0，强度1，阈值0.05）应用反锐化掩模效果。
	 * </p>
	 * <p>
	 * 反锐化掩模是一种更高级的锐化技术，可以控制锐化强度和阈值。
	 * Sigma决定了锐化的强度。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #unsharp(Double, Double, Double, Double)}并传入默认参数。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的反锐化掩模设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param sigma sigma值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp(@Nullable Double sigma) {
		return unsharp(0d, sigma, 1d, 0.05);
	}

	/**
	 * 设置反锐化掩模效果。
	 * <p>
	 * 使用指定的参数应用反锐化掩模效果。
	 * </p>
	 * <p>
	 * 反锐化掩模是一种更高级的锐化技术，可以控制锐化强度和阈值。
	 * 半径决定了锐化的范围，sigma决定了锐化的强度，
	 * 强度控制锐化程度，阈值控制锐化的敏感度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #unsharpKernel}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果任何参数为null、sigma小于等于0、半径小于0、强度小于等于0或阈值小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的反锐化掩模设置</li>
	 *   <li>半径必须大于等于0</li>
	 *   <li>sigma必须大于0</li>
	 *   <li>强度必须大于0</li>
	 *   <li>阈值必须大于0</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 半径，必须大于等于0，可以为null
	 * @param sigma sigma值，必须大于0，可以为null
	 * @param amount 强度，必须大于0，可以为null
	 * @param threshold 阈值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations unsharp(@Nullable Double radius, @Nullable Double sigma, @Nullable Double amount,
	                                        @Nullable Double threshold) {
		if (ObjectUtils.allNotNull(radius, sigma, amount, threshold) && sigma > 0 && radius >= 0 &&
			amount > 0 && threshold > 0) {
			this.unsharpKernel = new UnsharpKernelArgs(radius, sigma, amount, threshold);
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置浮雕效果（默认半径）。
	 * <p>
	 * 使用默认半径（0）应用浮雕效果。
	 * </p>
	 * <p>
	 * 浮雕效果创建立体感，使图像具有浮雕的外观。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #embossRadius}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的浮雕设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations emboss() {
		return emboss(0d);
	}

	/**
	 * 设置浮雕效果。
	 * <p>
	 * 使用指定的半径应用浮雕效果。
	 * </p>
	 * <p>
	 * 浮雕效果创建立体感，使图像具有浮雕的外观。
	 * 半径决定了浮雕效果的强度。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #embossRadius}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果半径为null或小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的浮雕设置</li>
	 *   <li>半径必须大于等于0</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 浮雕半径，必须大于等于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations emboss(@Nullable Double radius) {
		if (Objects.nonNull(radius) && radius >= 0) {
			this.embossRadius = radius;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置阈值化效果（默认百分比）。
	 * <p>
	 * 使用默认百分比（0.5）应用阈值化效果。
	 * </p>
	 * <p>
	 * 阈值化将图像转换为二值图像。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #thresholdPercent}并触发convert命令。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的阈值化设置</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations threshold() {
		return threshold(0.5);
	}

	/**
	 * 设置阈值化效果。
	 * <p>
	 * 使用指定的百分比应用阈值化效果。
	 * </p>
	 * <p>
	 * 阈值化将图像转换为二值图像。
	 * 百分比范围为0-1，值越大，阈值越高。
	 * </p>
	 * <p>
	 * 此方法会设置{@link #thresholdPercent}并触发convert命令。
	 * </p>
	 * <p>
	 * 如果百分比为null、小于0或大于1，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的阈值化设置</li>
	 *   <li>百分比范围为0-1</li>
	 *   <li>滤镜操作会自动触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param percent 阈值百分比，范围0-1，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations threshold(@Nullable Double percent) {
		if (Objects.nonNull(percent) && percent >= 0 && percent <= 1) {
			this.thresholdPercent = percent;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置亮度。
	 * <p>
	 * 设置图像的亮度值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整亮度。
	 * 值大于100表示增加亮度，值小于100表示降低亮度。
	 * </p>
	 * <p>
	 * 如果亮度值不为100，会自动触发convert命令。
	 * </p>
	 * <p>
	 * 如果亮度为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的亮度设置</li>
	 *   <li>亮度值为100时不触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param brightness 亮度值，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations brightness(@Nullable Integer brightness) {
		if (Objects.nonNull(brightness)) {
			this.brightness = brightness;
			this.triggerConvert = brightness != 100;
		}
		return this;
	}

	/**
	 * 设置饱和度。
	 * <p>
	 * 设置图像的饱和度值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整饱和度。
	 * 值大于100表示增加饱和度，值小于100表示降低饱和度。
	 * </p>
	 * <p>
	 * 如果饱和度值不为100，会自动触发convert命令。
	 * </p>
	 * <p>
	 * 如果饱和度为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的饱和度设置</li>
	 *   <li>饱和度值为100时不触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param saturation 饱和度值，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations saturation(@Nullable Integer saturation) {
		if (Objects.nonNull(saturation)) {
			this.saturation = saturation;
			this.triggerConvert = saturation != 100;
		}
		return this;
	}

	/**
	 * 设置色相。
	 * <p>
	 * 设置图像的色相值。
	 * </p>
	 * <p>
	 * 默认值为100，表示不调整色相。
	 * 值大于100表示色相偏移，值小于100表示反向偏移。
	 * </p>
	 * <p>
	 * 如果色相值不为100，会自动触发convert命令。
	 * </p>
	 * <p>
	 * 如果色相为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的色相设置</li>
	 *   <li>色相值为100时不触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param hue 色相值，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations hue(@Nullable Integer hue) {
		if (Objects.nonNull(hue)) {
			this.hue = hue;
			this.triggerConvert = hue != 100;
		}
		return this;
	}

	/**
	 * 设置水印方向。
	 * <p>
	 * 设置水印在图像中的位置方向。
	 * </p>
	 * <p>
	 * 可选方向包括：
	 * <ul>
	 *   <li>TOP：顶部居中</li>
	 *   <li>TOP_LEFT：左上角</li>
	 *   <li>TOP_RIGHT：右上角</li>
	 *   <li>BOTTOM：底部居中</li>
	 *   <li>BOTTOM_LEFT：左下角</li>
	 *   <li>BOTTOM_RIGHT：右下角</li>
	 *   <li>CENTER：中心</li>
	 *   <li>LEFT：左侧居中</li>
	 *   <li>RIGHT：右侧居中</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果方向为null，则不修改配置。
	 * </p>
	 * <p>
	 * 设置方向会清除水印坐标。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>水印方向与坐标互斥，设置方向会清除坐标</li>
	 *   <li>此方法会覆盖之前的水印方向设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param direction 水印方向，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkDirection(@Nullable Direction direction) {
		if (Objects.nonNull(direction)) {
			this.watermarkX = null;
			this.watermarkY = null;
			this.watermarkDirection = direction;
		}
		return this;
	}

	/**
	 * 设置水印位置坐标。
	 * <p>
	 * 设置水印在图像中的精确坐标位置。
	 * </p>
	 * <p>
	 * 坐标原点为图像左上角。
	 * </p>
	 * <p>
	 * 如果x或y为null、小于0，则不修改配置。
	 * </p>
	 * <p>
	 * 设置坐标会清除水印方向。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>水印坐标与方向互斥，设置坐标会清除方向</li>
	 *   <li>坐标必须大于等于0</li>
	 *   <li>此方法会覆盖之前的水印坐标设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param x 水印X坐标，必须大于等于0，可以为null
	 * @param y 水印Y坐标，必须大于等于0
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkPosition(@Nullable Integer x, Integer y) {
		if (ObjectUtils.allNotNull(x, y) && x >= 0 && y >= 0) {
			this.watermarkX = x;
			this.watermarkY = y;
			this.watermarkDirection = null;
		}
		return this;
	}

	/**
	 * 设置图像水印。
	 * <p>
	 * 设置作为水印的图像资源和尺寸。
	 * </p>
	 * <p>
	 * 设置图像水印会清除文字水印。
	 * </p>
	 * <p>
	 * 如果资源不是图像或尺寸为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>图像水印与文字水印互斥，设置图像水印会清除文字水印</li>
	 *   <li>资源必须是图像类型</li>
	 *   <li>此方法会触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param watermarkImage 图像水印资源，可以为null
	 * @param watermarkImageSize 图像水印尺寸，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImage(@Nullable IOResource watermarkImage, @Nullable ImageSize watermarkImageSize) {
		if (ObjectUtils.allNotNull(watermarkImage, watermarkImageSize)) {
			if (watermarkImage.isImage()) {
				this.watermarkImage = watermarkImage;
				this.watermarkImageSize = watermarkImageSize;
				this.watermarkText = null;
				this.triggerConvert = true;
			}
		}
		return this;
	}

	/**
	 * 设置图像水印（GraphicsMagick资源）。
	 * <p>
	 * 设置作为水印的GraphicsMagick资源。
	 * </p>
	 * <p>
	 * GraphicsMagick资源会自动获取图像尺寸。
	 * </p>
	 * <p>
	 * 设置图像水印会清除文字水印。
	 * </p>
	 * <p>
	 * 如果资源为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>图像水印与文字水印互斥，设置图像水印会清除文字水印</li>
	 *   <li>此方法会触发convert命令</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource GraphicsMagick资源，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImage(@Nullable GraphicsMagickResource resource) {
		if (Objects.nonNull(resource)) {
			this.watermarkImage = resource;
			this.watermarkImageSize = resource.getImageSize();
			this.watermarkText = null;
			this.triggerConvert = true;
		}
		return this;
	}

	/**
	 * 设置图像水印边距。
	 * <p>
	 * 设置图像水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 如果边距为null或小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的边距设置</li>
	 *   <li>边距必须大于等于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param margin 边距，必须大于等于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageMargin(@Nullable Integer margin) {
		if (Objects.nonNull(margin) && margin >= 0) {
			this.watermarkImageMargin = margin;
		}
		return this;
	}

	/**
	 * 设置图像水印尺寸限制策略。
	 * <p>
	 * 设置根据目标图像尺寸计算水印尺寸范围的策略。
	 * </p>
	 * <p>
	 * 返回一个Pair，左值为最小尺寸，右值为最大尺寸。
	 * </p>
	 * <p>
	 * 如果策略为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的尺寸限制策略设置</li>
	 * </ul>
	 * </p>
	 *
	 * @param imageSizeLimitStrategy 尺寸限制策略，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageSizeLimitStrategy(
		@Nullable Function<ImageSize, Pair<ImageSize, ImageSize>> imageSizeLimitStrategy) {
		if (Objects.nonNull(imageSizeLimitStrategy)) {
			this.watermarkImageSizeLimitStrategy = imageSizeLimitStrategy;
		}
		return this;
	}

	/**
	 * 设置图像水印相对缩放因子。
	 * <p>
	 * 设置图像水印相对于目标图像的缩放比例。
	 * </p>
	 * <p>
	 * 默认值为0.15，表示水印尺寸为目标图像的15%。
	 * 值必须大于0，值越大，水印越大。
	 * </p>
	 * <p>
	 * 如果缩放因子为null或小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的相对缩放因子设置</li>
	 *   <li>缩放因子必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param relativeScaleFactor 相对缩放因子，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageRelativeScaleFactor(@Nullable Double relativeScaleFactor) {
		if (Objects.nonNull(relativeScaleFactor) && relativeScaleFactor > 0) {
			this.watermarkImageRelativeScaleFactor = relativeScaleFactor;
		}
		return this;
	}

	/**
	 * 设置图像水印透明度。
	 * <p>
	 * 设置图像水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * 范围为0-1，值越小，透明度越高。
	 * </p>
	 * <p>
	 * 如果透明度为null、小于0或大于1，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的透明度设置</li>
	 *   <li>透明度范围为0-1</li>
	 * </ul>
	 * </p>
	 *
	 * @param opacity 透明度，范围0-1，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkImageOpacity(@Nullable Float opacity) {
		if (Objects.nonNull(opacity) && opacity >= 0 && opacity <= 1) {
			this.watermarkImageOpacity = opacity;
		}
		return this;
	}

	/**
	 * 设置文字水印文本和字体文件。
	 *
	 * <p><b>强烈建议使用图像水印而非文字水印！！！</b></p>
	 *
	 * <p>
	 * 设置作为水印的文本内容和字体文件。
	 * </p>
	 * <p>
	 * 字体文件必须是TTF格式。
	 * </p>
	 * <p>
	 * 设置文字水印会清除图像水印。
	 * </p>
	 * <p>
	 * 如果文本为空或字体文件不是TTF格式，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>文字水印与图像水印互斥，设置文字水印会清除图像水印</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 *   <li>字体文件必须是TTF格式</li>
	 *   <li>文本不能为空</li>
	 * </ul>
	 * </p>
	 *
	 * @param text 文字水印文本，不能为空
	 * @param fontFile 字体文件（TTF格式），可以为null
	 * @return 当前实例，支持链式调用
	 * @throws IOException 如果字体文件读取失败
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkText(@Nullable String text, @Nullable File fontFile) throws IOException {
		if (StringUtils.isNotBlank(text) && FileUtils.isMimeType(fontFile, ImageConstants.TTF_FONT_MIME_TYPE)) {
			this.watermarkText = text;
			this.watermarkTextFont = fontFile.getAbsolutePath();
			this.watermarkImage = null;
			this.watermarkImageSize = null;
		}
		return this;
	}

	/**
	 * 设置文字水印文本和字体名称。
	 *
	 * <p><b>强烈建议使用图像水印而非文字水印！！！</b></p>
	 *
	 * <p>
	 * 设置作为水印的文本内容和系统字体名称。
	 * </p>
	 * <p>
	 * 字体名称可以是系统已安装的字体名称。
	 * </p>
	 * <p>
	 * 设置文字水印会清除图像水印。
	 * </p>
	 * <p>
	 * 如果文本或字体名称为空，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>文字水印与图像水印互斥，设置文字水印会清除图像水印</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 *   <li>字体名称必须是系统已安装的字体</li>
	 *   <li>文本不能为空</li>
	 * </ul>
	 * </p>
	 *
	 * @param text 文字水印文本，不能为空
	 * @param fontName 字体名称，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkText(@Nullable String text, @Nullable String fontName) {
		if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(fontName)) {
			this.watermarkText = text;
			this.watermarkTextFont = fontName;
			this.watermarkImage = null;
			this.watermarkImageSize = null;
		}
		return this;
	}

	/**
	 * 设置文字水印描边。
	 * <p>
	 * 设置是否对文字水印进行描边。
	 * </p>
	 * <p>
	 * 描边可以增强文字在水印中的可读性。
	 * </p>
	 * <p>
	 * 如果描边为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的描边设置</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param stroke 是否启用描边，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStroke(@Nullable Boolean stroke) {
		if (Objects.nonNull(stroke)) {
			this.watermarkTextStroke = stroke;
		}
		return this;
	}

	/**
	 * 设置文字水印字体大小。
	 * <p>
	 * 设置文字水印的字体大小。
	 * </p>
	 * <p>
	 * 默认值为24。
	 * 值越大，字体越大。
	 * </p>
	 * <p>
	 * 如果字体大小为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的字体大小设置</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param fontSize 字体大小，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextFontSize(@Nullable Integer fontSize) {
		if (Objects.nonNull(fontSize)) {
			this.watermarkTextFontSize = fontSize;
		}
		return this;
	}

	/**
	 * 设置文字水印描边颜色。
	 * <p>
	 * 设置文字水印的描边颜色。
	 * </p>
	 * <p>
	 * 默认值为黑色。
	 * 描边可以增强文字在水印中的可读性。
	 * </p>
	 * <p>
	 * 如果描边颜色为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的描边颜色设置</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param strokeColor 描边颜色，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStrokeColor(@Nullable Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.watermarkTextStrokeColor = strokeColor;
		}
		return this;
	}

	/**
	 * 设置文字水印描边宽度。
	 * <p>
	 * 设置文字水印的描边宽度。
	 * </p>
	 * <p>
	 * 默认值为1。
	 * 值越大，描边越粗。
	 * </p>
	 * <p>
	 * 如果描边宽度为null或小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的描边宽度设置</li>
	 *   <li>描边宽度必须大于0</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param strokeWidth 描边宽度，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStrokeWidth(@Nullable Integer strokeWidth) {
		if (Objects.nonNull(strokeWidth) && strokeWidth > 0) {
			this.watermarkTextStrokeWidth = strokeWidth;
		}
		return this;
	}

	/**
	 * 启用文字水印描边。
	 * <p>
	 * 启用文字水印的描边效果。
	 * </p>
	 * <p>
	 * 描边可以增强文字在水印中的可读性。
	 * </p>
	 * <p>
	 * 此方法会将描边设置为true。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的描边设置</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextStroke() {
		this.watermarkTextStroke = true;
		return this;
	}

	/**
	 * 设置文字水印透明度。
	 * <p>
	 * 设置文字水印的透明度。
	 * </p>
	 * <p>
	 * 默认值为0.4。
	 * 范围为0-1，值越小，透明度越高。
	 * </p>
	 * <p>
	 * 如果透明度为null、小于0或大于1，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的透明度设置</li>
	 *   <li>透明度范围为0-1</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param opacity 透明度，范围0-1，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextOpacity(@Nullable Float opacity) {
		if (Objects.nonNull(opacity) && opacity >= 0 && opacity <= 1) {
			this.watermarkTextOpacity = opacity;
		}
		return this;
	}

	/**
	 * 设置文字水印填充颜色。
	 * <p>
	 * 设置文字水印的填充颜色。
	 * </p>
	 * <p>
	 * 默认值为白色。
	 * </p>
	 * <p>
	 * 如果填充颜色为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的填充颜色设置</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param color 填充颜色，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextFillColor(@Nullable Color color) {
		if (Objects.nonNull(color)) {
			this.watermarkTextFillColor = color;
		}
		return this;
	}

	/**
	 * 设置文字水印边距。
	 * <p>
	 * 设置文字水印与图像边缘的距离。
	 * </p>
	 * <p>
	 * 默认值为10。
	 * </p>
	 * <p>
	 * 如果边距为null或小于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的边距设置</li>
	 *   <li>边距必须大于等于0</li>
	 *   <li>建议使用图像水印替代，效果更好</li>
	 * </ul>
	 * </p>
	 *
	 * @param margin 边距，必须大于等于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations watermarkTextMargin(@Nullable Integer margin) {
		if (Objects.nonNull(margin) && margin >= 0) {
			this.watermarkTextMargin = margin;
		}
		return this;
	}

	/**
	 * 去除元数据。
	 * <p>
	 * 启用去除图像元数据的功能。
	 * </p>
	 * <p>
	 * 去除元数据可以减小文件大小，保护隐私。
	 * 元数据包括EXIF、IPTC、ICM等。
	 * </p>
	 * <p>
	 * 此方法会将去除元数据设置为true。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的去除元数据设置</li>
	 *   <li>去除元数据后无法恢复</li>
	 * </ul>
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations stripProfiles() {
		this.stripProfiles = true;
		return this;
	}

	/**
	 * 设置是否去除元数据。
	 * <p>
	 * 设置是否去除图像的元数据。
	 * </p>
	 * <p>
	 * 去除元数据可以减小文件大小，保护隐私。
	 * 元数据包括EXIF、IPTC、ICM等。
	 * </p>
	 * <p>
	 * 如果去除元数据为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的去除元数据设置</li>
	 *   <li>去除元数据后无法恢复</li>
	 * </ul>
	 * </p>
	 *
	 * @param stripProfiles 是否去除元数据，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations stripProfiles(@Nullable Boolean stripProfiles) {
		if (Objects.nonNull(stripProfiles)) {
			this.stripProfiles = stripProfiles;
		}
		return this;
	}

	/**
	 * 设置输出DPI。
	 * <p>
	 * 设置输出图像的DPI（每英寸点数）。
	 * </p>
	 * <p>
	 * DPI影响图像的打印质量。
	 * </p>
	 * <p>
	 * 如果DPI为null或小于等于0，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的DPI设置</li>
	 *   <li>DPI必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param dpi DPI值，必须大于0，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations dpi(@Nullable Integer dpi) {
		if (Objects.nonNull(dpi) && dpi > 0) {
			this.dpi = dpi;
		}
		return this;
	}

	/**
	 * 设置输出质量。
	 * <p>
	 * 设置输出图像的质量。
	 * </p>
	 * <p>
	 * 质量范围为1-100，值越大，质量越高，文件越大。
	 * </p>
	 * <p>
	 * 仅对支持质量控制的格式（如JPEG）有效。
	 * </p>
	 * <p>
	 * 如果质量为null、小于等于0或大于100，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的质量设置</li>
	 *   <li>质量范围为1-100</li>
	 *   <li>仅对支持质量控制的格式有效</li>
	 * </ul>
	 * </p>
	 *
	 * @param quality 质量值，范围1-100，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations quality(@Nullable Integer quality) {
		if (Objects.nonNull(quality) && quality > 0 && quality <= 100) {
			this.quality = quality;
		}
		return this;
	}

	/**
	 * 设置压缩类型。
	 * <p>
	 * 设置输出图像的压缩类型。
	 * </p>
	 * <p>
	 * 不同的压缩类型会影响文件大小和质量。
	 * </p>
	 * <p>
	 * 仅对支持压缩类型控制的格式有效。
	 * </p>
	 * <p>
	 * 如果压缩类型为null，则不修改配置。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>此方法会覆盖之前的压缩类型设置</li>
	 *   <li>仅对支持压缩类型控制的格式有效</li>
	 * </ul>
	 * </p>
	 *
	 * @param compression 压缩类型，可以为null
	 * @return 当前实例，支持链式调用
	 * @since 2.1.0
	 */
	public GraphicsMagickOperations compression(@Nullable CompressionType compression) {
		if (Objects.nonNull(compression)) {
			this.compression = compression;
		}
		return this;
	}

	/**
	 * 重置所有配置。
	 * <p>
	 * 将所有配置参数重置为默认值。
	 * </p>
	 * <p>
	 * 重置的配置包括：
	 * <ul>
	 *   <li>继承自ImageOperations的配置：缩放、旋转、翻转、裁剪、灰度化、透明度</li>
	 *   <li>重采样滤镜：null</li>
	 *   <li>亮度：100</li>
	 *   <li>饱和度：100</li>
	 *   <li>色相：100</li>
	 *   <li>所有滤镜参数：null</li>
	 *   <li>水印方向：TOP_RIGHT</li>
	 *   <li>水印坐标：null</li>
	 *   <li>文字水印文本：null</li>
	 *   <li>文字水印字体：null</li>
	 *   <li>文字水印透明度：0.4</li>
	 *   <li>文字水印填充颜色：白色</li>
	 *   <li>文字水印描边颜色：黑色</li>
	 *   <li>文字水印描边宽度：1</li>
	 *   <li>文字水印描边：true</li>
	 *   <li>文字水印字体大小：24</li>
	 *   <li>文字水印边距：10</li>
	 *   <li>图像水印资源：null</li>
	 *   <li>图像水印边距：10</li>
	 *   <li>图像水印相对缩放因子：0.15</li>
	 *   <li>图像水印透明度：0.4</li>
	 *   <li>图像水印尺寸：null</li>
	 *   <li>图像水印尺寸限制策略：默认策略</li>
	 *   <li>输出质量：null</li>
	 *   <li>去除元数据：false</li>
	 *   <li>输出DPI：null</li>
	 *   <li>压缩类型：null</li>
	 *   <li>触发convert命令：false</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 默认图像水印尺寸限制策略：
	 * <ul>
	 *   <li>小图（短边<600）：120x120到150x150</li>
	 *   <li>中等图（600<=短边<1920）：150x150到250x250</li>
	 *   <li>大图（短边>=1920）：250x250到400x400</li>
	 * </ul>
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public void reset() {
		super.reset();

		/* 缩放相关配置 */
		this.resizeFilter = null;

		/* 滤镜相关配置 */
		this.brightness = 100;
		this.saturation = 100;
		this.hue = 100;
		this.blurKernel = null;
		this.gaussianKernel = null;
		this.medianRadius = null;
		this.sharpenKernel = null;
		this.unsharpKernel = null;
		this.embossRadius = null;
		this.thresholdPercent = null;

		/* 水印坐标相关配置 */
		this.watermarkDirection = Direction.TOP_RIGHT;
		this.watermarkX = null;
		this.watermarkY = null;

		/* 文字水印相关配置 */
		this.watermarkText = null;
		this.watermarkTextFont = null;
		this.watermarkTextOpacity = 0.4f;
		this.watermarkTextFillColor = Color.WHITE;
		this.watermarkTextStrokeColor = Color.BLACK;
		this.watermarkTextStrokeWidth = 1;
		this.watermarkTextStroke = true;
		this.watermarkTextFontSize = 24;
		this.watermarkTextMargin = 10;

		/* 图像水印相关配置 */
		this.watermarkImage = null;
		this.watermarkImageMargin = 10;
		this.watermarkImageRelativeScaleFactor = 0.15;
		this.watermarkImageOpacity = 0.4f;
		this.watermarkImageSize = null;
		this.watermarkImageSizeLimitStrategy = imageSize -> {
			int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
			if (shorter < 600) { // 小图
				return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
			} else if (shorter >= 1920) { // 大图（注意：>=1920）
				return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
			} else { // 中等图
				return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
			}
		};

		/* 输出配置 */
		this.stripProfiles = false;
		this.dpi = null;
		this.quality = null;
		this.compression = null;

		this.triggerConvert = false;

	}

	/**
	 * 判断是否需要执行convert命令。
	 * <p>
	 * 根据当前配置判断是否需要执行GraphicsMagick的convert命令。
	 * </p>
	 * <p>
	 * 如果满足以下任一条件，则返回true：
	 * <ul>
	 *   <li>设置了目标宽度或高度</li>
	 *   <li>设置了缩放因子</li>
	 *   <li>设置了旋转角度</li>
	 *   <li>设置了翻转方向</li>
	 *   <li>设置了裁剪类型</li>
	 *   <li>设置了全局透明度</li>
	 *   <li>触发了convert命令（滤镜操作）</li>
	 *   <li>启用了灰度化</li>
	 * </ul>
	 * </p>
	 *
	 * @return 如果需要执行convert命令返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isConvertRequired() {
		return ObjectUtils.anyNotNull(targetWidth, targetHeight, scalingFactor, rotateAngle, flipDirection,
			cropType, globalOpacity) || this.triggerConvert || this.grayscale;
	}

	/**
	 * 转换为GraphicsMagick的convert命令操作。
	 * <p>
	 * 将当前配置转换为GraphicsMagick的convert命令操作。
	 * </p>
	 * <p>
	 * 此方法会调用{@link #toConvertGMOperation(GraphicsMagickResource, File, boolean)}并传入isIntermediate=false。
	 * </p>
	 * <p>
	 * 转换的配置包括：
	 * <ul>
	 *   <li>变换参数：自动方向、裁剪、缩放、旋转、翻转</li>
	 *   <li>滤镜参数：灰度化、亮度/饱和度/色相调整、透明度、锐化、模糊、浮雕、阈值化</li>
	 *   <li>文字水印参数</li>
	 *   <li>输出参数：质量、压缩类型、DPI、去除元数据</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>资源不能为null</li>
	 *   <li>输出文件不能为null</li>
	 *   <li>会自动创建输出文件的父目录</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource 图像资源，不能为null
	 * @param outputFile 输出文件，不能为null
	 * @return GMOperation对象
	 * @throws IOException 如果IO操作失败
	 * @since 2.1.0
	 */
	public GMOperation toConvertGMOperation(GraphicsMagickResource resource, File outputFile) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");

		return toConvertGMOperation(resource, outputFile, false);
	}

	/**
	 * 转换为GraphicsMagick的convert命令操作。
	 * <p>
	 * 将当前配置转换为GraphicsMagick的convert命令操作。
	 * </p>
	 * <p>
	 * 转换的配置包括：
	 * <ul>
	 *   <li>变换参数：自动方向、裁剪、缩放、旋转、翻转</li>
	 *   <li>滤镜参数：灰度化、亮度/饱和度/色相调整、透明度、锐化、模糊、浮雕、阈值化</li>
	 *   <li>文字水印参数</li>
	 *   <li>输出参数：质量、压缩类型、DPI、去除元数据</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果isIntermediate为true，则不设置质量、DPI和压缩类型。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>资源不能为null</li>
	 *   <li>输出文件不能为null</li>
	 *   <li>会自动创建输出文件的父目录</li>
	 *   <li>中间文件不设置质量和压缩类型</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource 图像资源，不能为null
	 * @param outputFile 输出文件，不能为null
	 * @param isIntermediate 是否为中间文件，中间文件不设置质量、DPI和压缩类型
	 * @return GMOperation对象
	 * @throws IOException 如果IO操作失败
	 * @since 2.1.0
	 */
	public GMOperation toConvertGMOperation(GraphicsMagickResource resource, File outputFile, boolean isIntermediate) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("convert");
		gmOperation.addImage(resource.getFile());

		// 设置变换参数
		setTransformArgs(resource.getImageSize().getVisualSize(), gmOperation);

		// 设置滤镜参数
		setFilterArgs(gmOperation);

		// 设置文字水印参数
		setTextWatermarkArgs(gmOperation);

		// 设置输出质量（一般不需要设置，且只有特定格式生效，如果是输出中间文件则不设置）
		if (Objects.nonNull(quality) && !isIntermediate) {
			gmOperation.quality(quality);
		}

		// 设置压缩格式（一般不需要设置，且只有特定格式生效，如果是输出中间文件则不设置）
		if (Objects.nonNull(compression) && !isIntermediate) {
			gmOperation.compress(compression.graphicsMagickCompressionType);
		}

		// 修改输出DPI
		if (Objects.nonNull(dpi) && !isIntermediate) {
			gmOperation.density(dpi);
		}

		// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
		if (stripProfiles) {
			gmOperation.stripProfiles();
		}

		// 传入输出文件
		gmOperation.addImage(outputFile);

		// 创建父目录
		FileUtils.forceMkdirParent(outputFile);

		return gmOperation;
	}

	/**
	 * 转换为GraphicsMagick的composite命令操作。
	 * <p>
	 * 将当前配置转换为GraphicsMagick的composite命令操作，用于图像水印合成。
	 * </p>
	 * <p>
	 * 此方法会根据目标图像尺寸和水印尺寸限制策略计算水印的最终尺寸。
	 * </p>
	 * <p>
	 * 合成的配置包括：
	 * <ul>
	 *   <li>水印透明度</li>
	 *   <li>水印尺寸（根据相对缩放因子和尺寸限制策略计算）</li>
	 *   <li>水印位置（方向或坐标）</li>
	 *   <li>水印边距</li>
	 *   <li>输出参数：质量、压缩类型、DPI、去除元数据</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>资源不能为null</li>
	 *   <li>输出文件不能为null</li>
	 *   <li>必须配置水印图片尺寸</li>
	 *   <li>必须配置水印图片</li>
	 *   <li>会自动创建输出文件的父目录</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource 图像资源，不能为null
	 * @param outputFile 输出文件，不能为null
	 * @return GMOperation对象
	 * @throws IOException 如果IO操作失败
	 * @throws ImageOperationException 如果未配置水印图片尺寸或水印图片
	 * @since 2.1.0
	 */
	public GMOperation toCompositeGMOperation(GraphicsMagickResource resource, File outputFile) throws IOException {
		Assert.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		if (Objects.isNull(watermarkImageSize)) {
			throw new ImageOperationException("未配置水印图片尺寸");
		}
		if (Objects.isNull(watermarkImage)) {
			throw new ImageOperationException("未配置水印图片");
		}

		GMOperation gmOperation = new GMOperation();
		gmOperation.addRawArg("composite");

		// 设置水印图片不透明度
		gmOperation.addRawArg("-dissolve " + (int) (watermarkImageOpacity * 100));

		ImageSize originalWatermarkSize = watermarkImageSize;

		Pair<ImageSize, ImageSize> watermarkImageSizeRange = watermarkImageSizeLimitStrategy.apply(resource.getImageSize());
		ImageSize targetWatermarkImageSize = resource.getImageSize().scale(watermarkImageRelativeScaleFactor);

		if (originalWatermarkSize.getWidth() > originalWatermarkSize.getHeight()) {
			int targetWidth = Math.min(watermarkImageSizeRange.getRight().getWidth(),
				Math.max(watermarkImageSizeRange.getLeft().getWidth(), targetWatermarkImageSize.getWidth()));
			if (targetWidth != targetWatermarkImageSize.getWidth()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByWidth(targetWidth);
			}
		} else {
			int targetHeight = Math.min(watermarkImageSizeRange.getRight().getHeight(),
				Math.max(watermarkImageSizeRange.getLeft().getHeight(), targetWatermarkImageSize.getHeight()));
			if (targetHeight != targetWatermarkImageSize.getHeight()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByHeight(targetHeight);
			}
		}

		if (Objects.nonNull(watermarkDirection)) {
			setWatermarkDirectionArg(gmOperation);
			gmOperation.addRawArg("-geometry " + targetWatermarkImageSize.getWidth() + "x" +
				targetWatermarkImageSize.getHeight() + "+" + watermarkImageMargin + "+" + watermarkImageMargin);
			gmOperation.addImage(watermarkImage.getFile());
		} else if (ObjectUtils.allNotNull(watermarkX, watermarkY)) {
			gmOperation.addImage(watermarkImage.getFile());
			// 设置左上角为原点
			gmOperation.gravity(GMOperation.Gravity.NorthWest);

			int x = Math.max(0, Math.min(resource.getImageSize().getWidth() - targetWatermarkImageSize.getWidth(),
				watermarkX + watermarkImageMargin));
			int y = Math.max(0, Math.min(resource.getImageSize().getHeight() - targetWatermarkImageSize.getHeight(),
				watermarkY + watermarkImageMargin));
			gmOperation.addRawArg("-geometry " + targetWatermarkImageSize.getWidth() + "x" +
				targetWatermarkImageSize.getHeight() + "+" + x + "+" + y);
		}

		//gmOperation.addImage(operations.getWatermarkImage().getFile());

		gmOperation.addImage(resource.getFile());

		// 设置输出质量（一般不需要设置，且只有特定格式生效）
		if (Objects.nonNull(quality)) {
			gmOperation.quality(quality);
		}

		// 设置压缩格式（一般不需要设置，且只有特定格式生效）
		if (Objects.nonNull(compression)) {
			gmOperation.compress(compression.graphicsMagickCompressionType);
		}

		// 修改输出DPI
		if (Objects.nonNull(dpi)) {
			gmOperation.density(dpi);
		}

		// 判断是否需要删除 ICM, EXIF, IPTC 等配置文件
		if (stripProfiles) {
			gmOperation.stripProfiles();
		}

		// 传入输出文件
		gmOperation.addImage(outputFile);

		// 创建父目录
		FileUtils.forceMkdirParent(outputFile);

		return gmOperation;
	}

	/**
	 * 设置变换参数到GMOperation。
	 * <p>
	 * 设置图像的变换参数，包括自动方向、裁剪、缩放、旋转 and 翻转。
	 * </p>
	 * <p>
	 * 变换顺序：
	 * <ol>
	 *   <li>自动方向（如果图像方向不正常）</li>
	 *   <li>裁剪（如果设置了裁剪类型）</li>
	 *   <li>缩放（如果设置了目标尺寸或缩放因子）</li>
	 *   <li>旋转（如果设置了旋转角度）</li>
	 *   <li>翻转（如果设置了翻转方向）</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>imageSize不能为null</li>
	 * </ul>
	 * </p>
	 *
	 * @param imageSize 图像尺寸，不能为null
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setTransformArgs(ImageSize imageSize, GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");
		Assert.notNull(imageSize, "imageSize 不可为 null");

		if (!imageSize.isNormalOrientation()) {
			gmOperation.addRawArg("-auto-orient");
		}

		ImageSize visualImageSize = imageSize.getVisualSize();

		// 判断是否需要裁剪
		if (Objects.nonNull(cropType)) {
			setCropArgs(visualImageSize, gmOperation);
		}

		// 判断是否需要执行缩放
		if (ObjectUtils.allNotNull(targetWidth, targetHeight)) {
			if (forceScale) {
				visualImageSize = visualImageSize.resize(targetWidth, targetHeight);
			} else {
				visualImageSize = visualImageSize.scale(targetWidth, targetHeight);
			}
		} else if (Objects.nonNull(scalingFactor)) {
			visualImageSize = visualImageSize.scale(scalingFactor);
		} else if (Objects.nonNull(targetWidth)) {
			visualImageSize = visualImageSize.scaleByWidth(targetWidth);
		} else if (Objects.nonNull(targetHeight)) {
			visualImageSize = visualImageSize.scaleByHeight(targetHeight);
		}

		if (ObjectUtils.anyNotNull(targetWidth, targetHeight, scalingFactor)) {
			if (Objects.nonNull(resizeFilter)) {
				gmOperation.filter(resizeFilter.graphicsMagickFilterName);
			}

			gmOperation.resize(visualImageSize.getWidth(), visualImageSize.getHeight(), '!');
		}

		// 判断是否需要旋转
		if (Objects.nonNull(rotateAngle)) {
			gmOperation.rotate(rotateAngle);
		}

		// 判断是否需要翻转
		if (Objects.nonNull(flipDirection)) {
			switch (flipDirection) {
				case VERTICAL -> gmOperation.flip();
				case HORIZONTAL -> gmOperation.flop();
			}
		}
	}

	/**
	 * 设置滤镜参数到GMOperation。
	 * <p>
	 * 设置图像的滤镜参数，包括灰度化、亮度/饱和度/色相调整、透明度、锐化、模糊、浮雕和阈值化。
	 * </p>
	 * <p>
	 * 滤镜应用顺序：
	 * <ol>
	 *   <li>灰度化</li>
	 *   <li>亮度/饱和度/色相调整（使用-modulate参数）</li>
	 *   <li>透明度调整</li>
	 *   <li>锐化（反锐化掩模优先于普通锐化）</li>
	 *   <li>模糊（普通模糊、高斯模糊、中值模糊按优先级）</li>
	 *   <li>浮雕</li>
	 *   <li>阈值化</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>亮度/饱和度/色相调整使用-modulate参数，格式为brightness,saturation,hue</li>
	 * </ul>
	 * </p>
	 *
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setFilterArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		// 判断是否需要灰度化
		if (grayscale) {
			gmOperation.colorspace("Gray");
		}

		// 判断是否需要调整亮度
		if (brightness != 100) {
			if (hue != 100) {
				gmOperation.addRawArg("-modulate " + brightness + "," + saturation + "," + hue);
			} else if (saturation != 100) {
				gmOperation.addRawArg("-modulate " + brightness + "," + saturation);
			} else {
				gmOperation.addRawArg("-modulate " + brightness);
			}
		}

		// 判断是否需要调整透明度
		if (Objects.nonNull(globalOpacity)) {
			gmOperation.addRawArg("-matte");
			gmOperation.addRawArg("-operator Opacity Negate 0");
			gmOperation.addRawArg("-operator Opacity Multiply " + globalOpacity);
			gmOperation.addRawArg("-operator Opacity Negate 0");
		}

		// 判断是否需要锐化
		if (Objects.nonNull(unsharpKernel)) {
			gmOperation.unsharp(unsharpKernel.radius(), unsharpKernel.sigma(),
				unsharpKernel.amount(), unsharpKernel.threshold());
		} else if (Objects.nonNull(sharpenKernel)) {
			gmOperation.sharpen(sharpenKernel.getLeft(), sharpenKernel.getRight());
		}

		// 判断是否需要模糊
		if (Objects.nonNull(blurKernel)) {
			gmOperation.blur(blurKernel.getLeft(), blurKernel.getRight());
		} else if (Objects.nonNull(gaussianKernel)) {
			gmOperation.gaussian(gaussianKernel.getLeft(), gaussianKernel.getRight());
		} else if (Objects.nonNull(medianRadius)) {
			gmOperation.median(medianRadius);
		}

		// 判断是否需要浮雕
		if (Objects.nonNull(embossRadius)) {
			gmOperation.emboss(embossRadius);
		}

		// 判断是否需要二值化
		if (Objects.nonNull(thresholdPercent)) {
			gmOperation.threshold((int) (thresholdPercent * 65535));
		}
	}

	/**
	 * 设置裁剪参数到GMOperation。
	 * <p>
	 * 设置图像的裁剪参数，支持中心裁剪、偏移裁剪和矩形裁剪。
	 * </p>
	 * <p>
	 * 裁剪类型：
	 * <ul>
	 *   <li>CENTER：中心裁剪，需要设置cropCenterWidth和cropCenterHeight</li>
	 *   <li>OFFSET：偏移裁剪，需要设置cropOffsetX和cropOffsetY</li>
	 *   <li>RECTANGLE：矩形裁剪，需要设置cropRectangleWidth和cropRectangleHeight</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>如果裁剪类型为null，则不执行任何操作</li>
	 *   <li>裁剪尺寸不能超过图像尺寸</li>
	 * </ul>
	 * </p>
	 *
	 * @param imageSize 图像尺寸
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setCropArgs(ImageSize imageSize, GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(cropType)) {
			return;
		}

		if (cropType == CropType.CENTER) {
			if (ObjectUtils.allNotNull(cropCenterWidth, cropCenterHeight) &&
				cropCenterWidth < imageSize.getWidth() && cropCenterHeight < imageSize.getHeight()) {
				int posX = (imageSize.getWidth() - cropCenterWidth) / 2;
				int posY = (imageSize.getHeight() - cropCenterHeight) / 2;
				gmOperation.crop(cropCenterWidth, cropCenterHeight, posX, posY);
			}
		} else if (cropType == CropType.OFFSET) {
			if (ObjectUtils.allNotNull(cropTopOffset, cropBottomOffset, cropLeftOffset, cropRightOffset) &&
				cropLeftOffset + cropRightOffset < imageSize.getWidth() &&
				cropTopOffset + cropBottomOffset < imageSize.getHeight()) {

				int width = imageSize.getWidth() - cropLeftOffset - cropRightOffset;
				int height = imageSize.getHeight() - cropTopOffset - cropBottomOffset;
				gmOperation.crop(width, height, cropLeftOffset, cropTopOffset);
			}
		} else if (cropType == CropType.RECT) {
			if (ObjectUtils.allNotNull(cropRectX, cropRectY, cropRectWidth, cropRectHeight) &&
				cropRectX + cropRectWidth < imageSize.getWidth() && cropRectY + cropRectHeight < imageSize.getHeight()) {

				gmOperation.crop(cropRectWidth, cropRectHeight, cropRectX, cropRectY);
			}
		}

		gmOperation.addRawArg(" +repage");
	}

	/**
	 * 设置水印方向参数到GMOperation。
	 * <p>
	 * 将水印方向转换为GraphicsMagick的gravity参数。
	 * </p>
	 * <p>
	 * 方向映射：
	 * <ul>
	 *   <li>TOP -> North</li>
	 *   <li>TOP_LEFT -> NorthWest</li>
	 *   <li>TOP_RIGHT -> NorthEast</li>
	 *   <li>BOTTOM -> South</li>
	 *   <li>BOTTOM_LEFT -> SouthWest</li>
	 *   <li>BOTTOM_RIGHT -> SouthEast</li>
	 *   <li>CENTER -> Center</li>
	 *   <li>LEFT -> West</li>
	 *   <li>RIGHT -> East</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>如果水印方向为null，则不执行任何操作</li>
	 * </ul>
	 * </p>
	 *
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setWatermarkDirectionArg(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(watermarkDirection)) {
			return;
		}

		GMOperation.Gravity gravity = switch (watermarkDirection) {
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
	 * 设置文字水印参数到GMOperation。
	 * <p>
	 * 设置文字水印的绘制参数，包括字体、颜色、位置等。
	 * </p>
	 * <p>
	 * 如果设置了水印方向，则使用方向定位；如果设置了水印坐标，则使用坐标定位。
	 * </p>
	 * <p>
	 * 边距计算：
	 * <ul>
	 *   <li>方向定位：垂直边距为字体大小的1.4倍加上水印边距</li>
	 *   <li>坐标定位：直接使用水印边距</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>必须设置水印字体和文本</li>
	 *   <li>水印方向与坐标互斥</li>
	 * </ul>
	 * </p>
	 *
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setTextWatermarkArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (ObjectUtils.allNotNull(watermarkTextFont, watermarkText)) {
			if (Objects.nonNull(watermarkDirection)) {
				// 设置水印方位
				setWatermarkDirectionArg(gmOperation);
				setFontArgs(gmOperation);

				int marginY = (int) (watermarkTextFontSize * 1.4) + watermarkTextMargin;
				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, watermarkTextMargin, marginY, watermarkText));
			} else if (ObjectUtils.allNotNull(watermarkX, watermarkY)) {
				// 设置左上角为原点
				gmOperation.gravity(GMOperation.Gravity.NorthWest);
				setFontArgs(gmOperation);

				gmOperation.draw(String.format(DRAW_TEXT_ARG_FORMAT, watermarkX + watermarkTextMargin,
					watermarkY + watermarkTextMargin, watermarkText));
			}
		}
	}

	/**
	 * 设置字体参数到GMOperation。
	 * <p>
	 * 设置文字水印的字体参数，包括填充颜色、描边颜色、描边宽度、字体和字体大小。
	 * </p>
	 * <p>
	 * 颜色格式使用RGBA格式，透明度与水印透明度一致。
	 * </p>
	 * <p>
	 * 如果启用了描边，则设置描边颜色和描边宽度。
	 * </p>
	 * <p>
	 * <strong>注意事项</strong></p>
	 * <ul>
	 *   <li>gmOperation不能为null</li>
	 *   <li>如果水印字体为null，则不执行任何操作</li>
	 * </ul>
	 * </p>
	 *
	 * @param gmOperation GMOperation对象，不能为null
	 * @since 2.1.0
	 */
	public void setFontArgs(GMOperation gmOperation) {
		Assert.notNull(gmOperation, "gmOperation 不可为 null");

		if (Objects.isNull(watermarkTextFont)) {
			return;
		}

		String fillColor = COLOR_FORMAT.formatted(watermarkTextFillColor.getRed(),
			watermarkTextFillColor.getGreen(), watermarkTextFillColor.getBlue(), watermarkTextOpacity);
		gmOperation.fill(fillColor);

		if (watermarkTextStroke) {
			String strokeColor = COLOR_FORMAT.formatted(watermarkTextStrokeColor.getRed(),
				watermarkTextStrokeColor.getGreen(), watermarkTextStrokeColor.getBlue(), watermarkTextOpacity);
			gmOperation.stroke(strokeColor);
			gmOperation.strokewidth(watermarkTextStrokeWidth);
		}

		gmOperation.font(watermarkTextFont);
		gmOperation.pointsize(watermarkTextFontSize);
	}

	/**
	 * 反锐化掩模核参数记录。
	 * <p>
	 * 记录反锐化掩模滤镜的参数。
	 * </p>
	 * <p>
	 * 反锐化掩模是一种高级锐化技术，可以控制锐化强度和阈值。
	 * </p>
	 * <p>
	 * 参数说明：
	 * <ul>
	 *   <li>radius：锐化半径，决定锐化的范围，必须大于等于0</li>
	 *   <li>sigma：锐化强度，决定锐化的程度，必须大于0</li>
	 *   <li>amount：锐化强度系数，决定锐化的强度，必须大于0</li>
	 *   <li>threshold：锐化阈值，决定锐化的敏感度，必须大于0</li>
	 * </ul>
	 * </p>
	 *
	 * @param radius 半径，必须大于等于0
	 * @param sigma sigma值，必须大于0
	 * @param amount 强度，必须大于0
	 * @param threshold 阈值，必须大于0
	 * @since 2.1.0
	 */
	public record UnsharpKernelArgs(double radius, double sigma, double amount, double threshold) {
	}
}
