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
 * @author pangju666
 * @since 1.0.0
 */
public enum ResampleFilter {
	POINT(ResampleOp.FILTER_POINT, "Point"),
	BOX(ResampleOp.FILTER_BOX, "Box"),
	TRIANGLE(ResampleOp.FILTER_TRIANGLE, "Triangle"),
	HERMITE(ResampleOp.FILTER_HERMITE, "Hermite"),
	HANNING(ResampleOp.FILTER_HANNING, "Hanning"),
	HAMMING(ResampleOp.FILTER_HAMMING, "Hamming"),
	BLACKMAN(ResampleOp.FILTER_BLACKMAN, "Blackman"),
	GAUSSIAN(ResampleOp.FILTER_GAUSSIAN, "Gaussian"),
	QUADRATIC(ResampleOp.FILTER_QUADRATIC, "Quadratic"),
	CUBIC(ResampleOp.FILTER_CUBIC, "Cubic"),
	CATROM(ResampleOp.FILTER_CATROM, "Catrom"),
	MITCHELL(ResampleOp.FILTER_MITCHELL, "Mitchell"),
	LANCZOS(ResampleOp.FILTER_LANCZOS, "Lanczos"),
	BESSEL(ResampleOp.FILTER_BLACKMAN_BESSEL, "Bessel"),
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
