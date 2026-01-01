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

package io.github.pangju666.framework.boot.image.lang;

import java.util.Set;

/**
 * 图像能力常量集合。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>在框架层面聚合不同图像处理引擎/库的读写能力，提供统一的类型集合。</li>
 *   <li>涵盖 GraphicsMagick 的支持范围，并与上游组件能力保持一致。</li>
 *   <li>便于在运行时进行能力判定与类型校验（例如选择可用的编解码方案）。</li>
 * </ul>
 *
 * <p><b>字段</b></p>
 * <ul>
 *   <li>{@link #GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMAT_SET}：GraphicsMagick 可写出的图像格式扩展名集合。</li>
 *   <li>{@link #GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET}：GraphicsMagick 可读取的图像格式扩展名集合。</li>
 * </ul>
 *
 * <p><b>备注</b></p>
 * <ul>
 *   <li>不同平台与安装的依赖库版本可能影响具体支持范围，请以实际环境为准。</li>
 *   <li>以上集合均为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型。</li>
 *   <li>本类仅聚合能力信息，不参与具体的读写实现，具体处理由上层服务/工具决定。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageConstants extends io.github.pangju666.commons.image.lang.ImageConstants {
	/**
	 * GraphicsMagick 可写出的图像格式扩展名集合。
	 *
	 * <p>参考：<a href="http://www.graphicsmagick.org/formats.html">GraphicsMagick 支持格式文档</a></p>
	 * <p>说明：集合元素为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型。</p>
	 * @since 1.0.0
	 */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORTED_WRITE_IMAGE_FORMAT_SET = Set.of(
		"aai", "art", "avs", "bmp", "cmyk", "dcx", "dib", "dpx", "epdf", "epi", "eps", "eps2", "eps3", "epsf", "epsi",
		"ept", "fax", "fits", "fpx", "gif", "gray", "graya", "html", "hrz", "jbig", "bie", "jpg", "jng",  "jp2", "jpc",
		"jpeg", "jxl", "mat", "miff", "mono", "mng", "mpeg", "m2v", "mpc", "msl", "mtv", "mvg", "otb", "p7", "palm",
		"pam", "pbm", "pcd", "pcds", "pcl", "pcx", "pdb", "pdf", "pgm", "picon", "pict", "png", "pnm", "ppm", "ps",
		"ps2", "ps3", "psd", "ptif", "rgb", "rgba", "sgi", "shtml", "sun", "svg", "tga", "icb", "vda", "vst", "tiff",
		"tif", "txt", "uil", "uyvy", "vicar", "viff", "wbmp", "webp", "xbm", "xpm", "xwd", "yuv", "AAI", "ART", "AVS",
		"BMP", "CMYK", "DCX", "DIB", "DPX", "EPDF", "EPI", "EPS", "EPS2", "EPS3", "EPSF", "EPSI", "EPT", "FAX", "FITS",
		"FPX", "GIF", "GRAY", "GRAYA", "HTML", "HRZ", "JBIG", "BIE", "JPG", "JNG",  "JP2", "JPC", "JPEG", "JXL", "MAT",
		"MIFF", "MONO", "MNG", "MPEG", "M2V", "MPC", "MSL", "MTV", "MVG", "OTB", "P7", "PALM", "PAM", "PBM", "PCD",
		"PCDS", "PCL", "PCX", "PDB", "PDF", "PGM", "PICON", "PICT", "PNG", "PNM", "PPM", "PS", "PS2", "PS3", "PSD",
		"PTIF", "RGB", "RGBA", "SGI", "SHTML", "SUN", "SVG", "TGA", "ICB", "VDA", "VST", "TIFF", "TIF", "TXT", "UIL",
		"UYVY", "VICAR", "VIFF", "WBMP", "WEBP", "XBM", "XPM", "XWD", "YUV"
	);

	/**
	 * GraphicsMagick 可读取的图像格式扩展名集合。
	 *
	 * <p>参考：<a href="http://www.graphicsmagick.org/formats.html">GraphicsMagick 支持格式文档</a></p>
	 * <p>说明：集合元素为文件扩展名（如 {@code jpg}、{@code png}），非 MIME 类型。</p>
	 * @since 1.0.0
	 */
	public static final Set<String> GRAPHICS_MAGICK_SUPPORTED_READ_IMAGE_FORMAT_SET = Set.of(
		"aai", "art", "avif", "avs", "bmp", "cals", "cin", "cgm", "cmyk", "cur", "cut", "dcm", "dcx", "dib", "dpx",
		"emf", "epdf", "epi", "eps", "epsf", "epsi", "ept", "fax", "fig", "fits", "fpx", "gif", "gray", "graya", "heif",
		"hpgl", "html", "hrz", "ico", "jbig", "bie", "jpg", "jng",  "jp2", "jpc", "jpeg", "jxl", "man", "mat", "miff",
		"mono", "mng", "mpeg", "m2v", "mpc", "msl", "mtv", "mvg", "otb", "p7", "palm", "pam", "pbm", "pcd", "pcds",
		"pcx", "pdb", "pdf", "pfa", "pfb", "pgm", "picon", "pict", "pix", "png", "pnm", "ppm", "ps", "ps2", "ps3",
		"psd", "ptif", "pwp", "ras", "rad", "rgb", "rgba", "rla", "rle", "sct", "sfw", "sgi", "shtml", "sun", "svg",
		"tga", "icb", "vda", "vst", "tiff", "tif", "tim", "ttf", "txt", "uyvy", "vicar", "viff", "wbmp", "webp", "wpg",
		"xbm", "xcf", "xpm", "xwd", "yuv", "AAI", "ART", "AVIF", "AVS", "BMP", "CALS", "CIN", "CGM", "CMYK", "CUR",
		"CUT", "DCM", "DCX", "DIB", "DPX", "EMF", "EPDF", "EPI", "EPS", "EPSF", "EPSI", "EPT", "FAX", "FIG", "FITS",
		"FPX", "GIF", "GRAY", "GRAYA", "HEIF", "HPGL", "HTML", "HRZ", "ICO", "JBIG", "BIE", "JPG", "JNG",  "JP2", "JPC",
		"JPEG", "JXL", "MAN", "MAT", "MIFF", "MONO", "MNG", "MPEG", "M2V", "MPC", "MSL", "MTV", "MVG", "OTB", "P7",
		"PALM", "PAM", "PBM", "PCD", "PCDS",  "PCX", "PDB", "PDF", "PFA", "PFB", "PGM", "PICON", "PICT", "PIX", "PNG",
		"PNM", "PPM", "PS", "PS2", "PS3", "PSD", "PTIF", "PWP", "RAS", "RAD", "RGB", "RGBA", "RLA", "RLE", "SCT", "SFW",
		"SGI", "SHTML", "SUN", "SVG", "TGA", "ICB", "VDA", "VST", "TIFF", "TIF", "TIM", "TTF", "TXT", "UYVY", "VICAR",
		"VIFF", "WBMP", "WEBP", "WPG", "XBM", "XCF", "XPM", "XWD", "YUV"
	);

	protected ImageConstants() {
	}
}
