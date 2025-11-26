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

import com.twelvemonkeys.image.ResampleOp;

/**
 * 重采样滤镜枚举，映射 TwelveMonkeys 与 GraphicsMagick 的滤镜常量。
 *
 * <p><strong>使用说明</strong>：在缩放操作中选择不同滤镜以平衡质量与性能；
 * 低阶滤镜（如 {@code POINT}、{@code BOX}）速度更快但细节较差，高阶滤镜（如 {@code LANCZOS}、{@code SINC}）质量更好但计算更慢。</p>
 * <p><strong>适配</strong>：每个枚举项包含 {@code filterType}（TwelveMonkeys 常量值）与 {@code filterName}（GraphicsMagick 滤镜名称），
 * 便于进行参数传递。</p>
 *
 * <p><strong>术语说明</strong>：</p>
 * <p>· 振铃（ringing）：在锐利边缘附近出现明暗交替的细条纹/光晕，常见于使用理想低通近似（如 {@code Sinc}、{@code Lanczos}）时的过冲/欠冲，并非锯齿。</p>
 * <p>· 锯齿（aliasing/jaggies）：斜线或曲线边缘呈台阶状的块状边缘，多由采样不足或缩小时未进行足够预滤导致，与振铃不同。</p>
 *
 * <p><strong>选择建议</strong>：</p>
 * <p>· 质量优先且可接受轻微振铃：优选 {@code LANCZOS}、{@code MITCHELL}。</p>
 * <p>· 需要降低振铃、获得更平滑边缘：优选窗函数类 {@code BLACKMAN}/{@code HANNING}/{@code HAMMING} 或 {@code BESSEL}，但细节会更柔和。</p>
 * <p>· 性能或像素风格：使用 {@code POINT}/{@code BOX}，注意锯齿明显。</p>
 * <p>· 通用均衡：{@code CUBIC} 或 {@code QUADRATIC} 在锐度与平滑之间有良好折中。</p>
 * <p>· 小幅缩放与锯齿抑制：{@code HERMITE}、{@code TRIANGLE}，边缘更柔和。</p>
 * <p>· 边缘更锐利但可能振铃：{@code CATROM}。</p>
 * <p>· 极致细节且可接受明显振铃与较慢计算：{@code SINC}。</p>
 * <p>· 柔化伪影/噪声（整体更模糊）：{@code GAUSSIAN}。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum ResampleFilter {
    /**
	 * 速度最快、锯齿明显；适合像素风或极少量缩放。
	 *
     * @since 1.0.0
     */
    POINT(ResampleOp.FILTER_POINT, "Point"),
    /**
	 * 快速但偏模糊；适合缩小时的粗略平滑。
	 *
     * @since 1.0.0
     */
    BOX(ResampleOp.FILTER_BOX, "Box"),
    /**
	 * 速度与质量折中；边缘较柔和。
	 *
     * @since 1.0.0
     */
    TRIANGLE(ResampleOp.FILTER_TRIANGLE, "Triangle"),
    /**
	 * 相对柔和，抑制锯齿；适合小幅缩放。
	 *
     * @since 1.0.0
     */
    HERMITE(ResampleOp.FILTER_HERMITE, "Hermite"),
    /**
	 * 抑制振铃，平滑；适合缩小。
	 *
     * @since 1.0.0
     */
    HANNING(ResampleOp.FILTER_HANNING, "Hanning"),
    /**
	 * 与 Hanning 类似，抑制振铃；适合缩小。
	 *
     * @since 1.0.0
     */
    HAMMING(ResampleOp.FILTER_HAMMING, "Hamming"),
    /**
	 * 更强的振铃抑制，较平滑；细节略损失。
	 *
     * @since 1.0.0
     */
    BLACKMAN(ResampleOp.FILTER_BLACKMAN, "Blackman"),
    /**
	 * 柔和平滑，避免伪影；可能偏模糊。
	 *
     * @since 1.0.0
     */
    GAUSSIAN(ResampleOp.FILTER_GAUSSIAN, "Gaussian"),
    /**
	 * 质量介于线性与立方之间；稳健。
	 *
     * @since 1.0.0
     */
    QUADRATIC(ResampleOp.FILTER_QUADRATIC, "Quadratic"),
    /**
	 * 通用高质量；锐度与平滑度平衡良好。
	 *
     * @since 1.0.0
     */
    CUBIC(ResampleOp.FILTER_CUBIC, "Cubic"),
    /**
	 * 更锐利，边缘保留好；可能出现振铃。
	 *
     * @since 1.0.0
     */
    CATROM(ResampleOp.FILTER_CATROM, "Catrom"),
    /**
	 * 平衡锐度与平滑；放大/缩小均适用。
     *
	 * @since 1.0.0
     */
    MITCHELL(ResampleOp.FILTER_MITCHELL, "Mitchell"),
    /**
	 * 高质量与高锐度；计算较慢，可能振铃。
	 *
     * @since 1.0.0
     */
    LANCZOS(ResampleOp.FILTER_LANCZOS, "Lanczos"),
    /**
	 * 平滑且细腻；细节保留一般。
	 *
     * @since 1.0.0
     */
    BESSEL(ResampleOp.FILTER_BLACKMAN_BESSEL, "Bessel"),
    /**
	 * 细节最好但振铃明显；最慢。
	 *
     * @since 1.0.0
     */
    SINC(ResampleOp.FILTER_BLACKMAN_SINC, "Sinc");

	/**
	 * TwelveMonkeys 滤镜常量值，用于与 {@link ResampleOp} 进行适配。
	 *
	 * @since 1.0.0
	 */
	private final int filterType;
	/**
	 * GraphicsMagick 滤镜名称。
	 *
	 * @since 1.0.0
	 */
	private final String filterName;

    /**
     * 绑定 TwelveMonkeys 滤镜常量值与 GraphicsMagick 滤镜名称。
     *
     * @param filterType TwelveMonkeys 滤镜常量值
     * @param filterName GraphicsMagick 滤镜名称
     * @since 1.0.0
     */
    ResampleFilter(int filterType, String filterName) {
        this.filterType = filterType;
        this.filterName = filterName;
    }

    /**
     * 获取 TwelveMonkeys 滤镜常量值。
     *
     * @return 滤镜常量值
     * @since 1.0.0
     */
    public int getFilterType() {
        return filterType;
    }

    /**
     * 获取 GraphicsMagick 滤镜名称。
     *
     * @return 友好名称
     * @since 1.0.0
     */
    public String getFilterName() {
        return filterName;
    }
}
