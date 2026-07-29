package io.github.pangju666.framework.boot.image

import io.github.pangju666.commons.image.enums.FlipDirection
import io.github.pangju666.commons.image.enums.RotateDirection
import io.github.pangju666.commons.image.model.ImageSize
import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import io.github.pangju666.commons.io.utils.FileUtils
import io.github.pangju666.framework.boot.image.autoconfigure.ImageAutoConfiguration
import io.github.pangju666.framework.boot.image.core.impl.GraphicsMagickOperationsTemplate
import io.github.pangju666.framework.boot.image.enums.ResampleFilter
import io.github.pangju666.framework.boot.image.enums.TileLayout
import io.github.pangju666.framework.boot.image.io.resource.GraphicsMagickResource
import io.github.pangju666.framework.boot.image.model.opeartions.ImageOperations
import io.github.pangju666.framework.boot.image.model.tile.GridTileOptions
import io.github.pangju666.framework.boot.image.utils.GraphicsMagickUtils
import org.gm4java.engine.GMConnection
import org.gm4java.engine.support.PooledGMService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

@ActiveProfiles("gm")
@ContextConfiguration(classes = [ImageAutoConfiguration.class], loader = SpringBootContextLoader.class)
class GraphicsMagickOperationsTemplateSpec extends Specification {
	@Autowired
	GraphicsMagickOperationsTemplate template
	@Autowired
	PooledGMService gmService

	def "测试读取图片信息: #fileName"() {
		given:
		File file = new ClassPathResource("images/" + fileName).getFile()
		IOResource resource = new IOResource(file)
		boolean canRead = template.canRead(resource)

		GraphicsMagickResource graphicsMagickResource = null
		Exception exception = null

		when:
		try {
			GMConnection connection = gmService.getConnection()
			graphicsMagickResource = new GraphicsMagickResource(resource, connection)
		} catch (Exception e) {
			exception = e
		}

		then:
		if (canRead) {
			assert exception == null
			assert graphicsMagickResource != null
			assert graphicsMagickResource.file == file
			assert graphicsMagickResource.imageSize != null
			assert graphicsMagickResource.imageSize.getVisualSize().width == width
			assert graphicsMagickResource.imageSize.getVisualSize().height == height
			// 兼容 ico 的不同 mime type
			if (fileName == "test.ico" && graphicsMagickResource.mimeType == "image/x-icon") {
				assert true
			} else {
				assert graphicsMagickResource.mimeType == mimeType
			}
			assert graphicsMagickResource.getImageSize().getOrientation() == orientation
		} else {
			assert exception instanceof UnsupportedResourceException
		}

		where:
		fileName     | width | height | mimeType                   | orientation
		"camera.jpg" | 3016  | 4032   | "image/jpeg"               | 6
		"test.bmp"   | 71    | 96     | "image/bmp"                | 1
		"test.gif"   | 478   | 448    | "image/gif"                | 1
		"test.ico"   | 32    | 32     | "image/vnd.microsoft.icon" | 1
		"test.jpg"   | 1125  | 877    | "image/jpeg"               | 1
		"test.png"   | 4095  | 2559   | "image/png"                | 1
		"test.svg"   | 512   | 512    | "image/svg+xml"            | 1
		"test.tiff"  | 1200  | 1200   | "image/tiff"               | 1
		"test.webp"  | 1200  | 1200   | "image/webp"               | 1
	}

	def "测试图片缩放: 目标宽 #targetW"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_width_${targetW}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperations.graphicsMagick()
			.scaleByWidth(targetW)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == targetW
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | targetW
		"camera.jpg"  | "camera.jpg"   | 500
		"test.bmp"    | "test.bmp"     | 50
		"test.gif"    | "test.gif"     | 50
		"test.ico"    | "test.jpg"     | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 500
		"test.png"    | "test.png"     | 500
		"test.svg"    | "test.jpg"     | 10
		"test.tiff"   | "test.tiff"    | 500
		"test.webp"   | "test.webp"    | 200
	}

	def "测试图片缩放: 目标高 #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_height_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperations.graphicsMagick()
			.scaleByHeight(targetH)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.height == targetH
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | targetH
		"camera.jpg"  | "camera.jpg"   | 500
		"test.bmp"    | "test.bmp"     | 50
		"test.gif"    | "test.gif"     | 50
		"test.ico"    | "test.jpg"     | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 500
		"test.png"    | "test.png"     | 500
		"test.svg"    | "test.jpg"     | 10
		"test.tiff"   | "test.tiff"    | 500
		"test.webp"   | "test.webp"    | 200
	}

	def "测试范围缩放: #targetW x #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_${targetW}_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperations.graphicsMagick()
			.scale(targetW, targetH)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == targetW || outputImage.height == targetH
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | targetW | targetH
		"camera.jpg"  | "camera.jpg"   | 500     | 500
		"test.bmp"    | "test.bmp"     | 50      | 50
		"test.gif"    | "test.gif"     | 50      | 50
		"test.ico"    | "test.jpg"     | 20      | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 500     | 500
		"test.png"    | "test.png"     | 500     | 500
		"test.svg"    | "test.jpg"     | 10      | 10
		"test.tiff"   | "test.tiff"    | 500     | 500
		"test.webp"   | "test.webp"    | 200     | 200
	}

	def "测试强制缩放: #targetW x #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "force_scale_${targetW}_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperations.graphicsMagick()
			.forceScale(targetW, targetH)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == targetW && outputImage.height == targetH
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | targetW | targetH
		"camera.jpg"  | "camera.jpg"   | 500     | 500
		"test.bmp"    | "test.bmp"     | 50      | 50
		"test.gif"    | "test.gif"     | 50      | 50
		"test.ico"    | "test.jpg"     | 20      | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 500     | 500
		"test.png"    | "test.png"     | 500     | 500
		"test.svg"    | "test.jpg"     | 10      | 10
		"test.tiff"   | "test.tiff"    | 500     | 500
		"test.webp"   | "test.webp"    | 200     | 200
	}

	def "测试图片中心裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.cropByCenter(width, height)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == width && outputImage.height == height
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height
		"camera.jpg"  | "camera.jpg"   | 500   | 500
		"test.bmp"    | "test.bmp"     | 50    | 50
		//"test.gif"    | "test.gif"     | 50    | 50 // gif不支持裁剪
		"test.ico"    | "test.jpg"     | 20    | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 500   | 500
		"test.png"    | "test.png"     | 500   | 500
		"test.svg"    | "test.jpg"     | 10    | 10
		"test.tiff"   | "test.tiff"    | 500   | 500
		"test.webp"   | "test.webp"    | 1200  | 1200
	}

	def "测试图片矩形裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.cropByRect(x, y, width, height)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == width && outputImage.height == height
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | x   | y   | width | height
		"camera.jpg"  | "camera.jpg"   | 100 | 100 | 500   | 500
		"test.bmp"    | "test.bmp"     | 10  | 10  | 50    | 50
		//"test.gif"    | "test.gif"     | 10  | 10  | 50    | 50 // gif不支持裁剪
		"test.ico"    | "test.jpg"     | 10  | 10  | 20    | 20 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 100 | 100 | 500   | 500
		"test.png"    | "test.png"     | 100 | 100 | 500   | 500
		"test.svg"    | "test.jpg"     | 10  | 10  | 10    | 10
		"test.tiff"   | "test.tiff"    | 100 | 100 | 500   | 500
		"test.webp"   | "test.webp"    | 100 | 100 | 200   | 200
	}

	def "测试图片偏移裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.cropByOffset(top, bottom, left, right)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == (width - left - right) && outputImage.height == (height - top - bottom)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height | top | bottom | left | right
		"camera.jpg"  | "camera.jpg"   | 3016  | 4032   | 500 | 500    | 500  | 500
		"test.bmp"    | "test.bmp"     | 71    | 96     | 20  | 20     | 20   | 20
		//"test.gif"    | "test.gif"     | 478   | 448    | 100 | 100    | 100  | 100 // gif不支持裁剪
		"test.ico"    | "test.jpg"     | 32    | 32     | 10  | 10     | 10   | 10 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 1125  | 877    | 200 | 200    | 200  | 200
		"test.png"    | "test.png"     | 4095  | 2559   | 500 | 500    | 500  | 500
		"test.svg"    | "test.jpg"     | 512   | 512    | 10  | 5      | 10   | 5
		"test.tiff"   | "test.tiff"    | 1200  | 1200   | 500 | 500    | 500  | 200
		"test.webp"   | "test.webp"    | 1200  | 1200   | 100 | 100    | 300  | 100
	}

	def "测试旋转"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "rotate_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.rotate(direction)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == height && outputImage.height == width
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height | direction
		"camera.jpg"  | "camera.jpg"   | 3016  | 4032   | RotateDirection.CLOCKWISE_90
		"test.bmp"    | "test.bmp"     | 71    | 96     | RotateDirection.COUNTER_CLOCKWISE_90
		"test.gif"    | "test.gif"     | 478   | 448    | RotateDirection.COUNTER_CLOCKWISE_90
		"test.ico"    | "test.jpg"     | 32    | 32     | RotateDirection.COUNTER_CLOCKWISE_90 //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | 1125  | 877    | RotateDirection.CLOCKWISE_90
		"test.png"    | "test.png"     | 4095  | 2559   | RotateDirection.CLOCKWISE_90
		"test.svg"    | "test.jpg"     | 512   | 512    | RotateDirection.CLOCKWISE_90
		"test.tiff"   | "test.tiff"    | 1200  | 1200   | RotateDirection.CLOCKWISE_90
		"test.webp"   | "test.webp"    | 1200  | 1200   | RotateDirection.CLOCKWISE_90
	}

	def "测试翻转"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "flip_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.flip(direction)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | direction
		"camera.jpg"  | "camera.jpg"   | FlipDirection.HORIZONTAL
		"test.bmp"    | "test.bmp"     | FlipDirection.HORIZONTAL
		"test.gif"    | "test.gif"     | FlipDirection.HORIZONTAL
		"test.ico"    | "test.jpg"     | FlipDirection.HORIZONTAL //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"     | FlipDirection.VERTICAL
		"test.png"    | "test.png"     | FlipDirection.VERTICAL
		"test.svg"    | "test.jpg"     | FlipDirection.VERTICAL
		"test.tiff"   | "test.tiff"    | FlipDirection.VERTICAL
		"test.webp"   | "test.webp"    | FlipDirection.VERTICAL
	}

	def "测试图像灰度化"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "grayscale_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.grayscale()

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.webp"
	}

	def "测试图像调整锐化"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "sharpen_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.sharpen(0.8)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		//FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		//"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.webp"
	}

	def "测试图像调整模糊"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "blur_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.blur(2.0)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.webp"
	}

	def "测试文字水印"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "text_watermark_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkText("测试水印")
			.textWatermarkFontName("simhei")

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.webp"
	}

	def "测试图片水印"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "image_watermark_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()),
				new ImageSize(800, 206))

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.webp"
	}

	def "测试组合操作: 裁剪 -> 缩放 -> 旋转 -> 翻转 -> 灰度化 -> 亮度 -> 对比度 -> 锐化 -> 模糊 -> 图像水印 -> 转格式"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + filename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), outputFilename)

		and: "构建裁剪配置"
		def genericOperation = ImageOperations.generic()
			.cropByCenter(cropWidth, cropHeight)
			.scale(targetW, targetH)
			.rotate(RotateDirection.CLOCKWISE_90)
			.flip(FlipDirection.VERTICAL)
			.grayscale()
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()))
		def bufferedOperation = ImageOperations.graphicsMagick(genericOperation)
			.resizeFilter(ResampleFilter.LANCZOS)
			.sharpen(1.2)
			.blur(2.0)
			.dpi(200)
			.stripProfiles()
			.quality(75)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, bufferedOperation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		filename     | outputFilename | cropWidth | cropHeight | targetW | targetH
		"camera.jpg" | "camera.jpg"   | 500       | 500        | 500     | 500
		"test.bmp"   | "test.bmp"     | 50        | 50         | 50      | 50
		//"test.gif"   | "test.gif"      | 50        | 50         | 50      | 50
		"test.ico"   | "test.jpg"     | 20        | 20         | 20      | 20 //只支持读取，不支持写入
		"test.jpg"   | "test.jpg"     | 500       | 500        | 500     | 500
		"test.png"   | "test.png"     | 500       | 500        | 500     | 500
		"test.svg"   | "test.jpg"     | 300       | 300        | 400     | 400
		"test.tiff"  | "test.tiff"    | 500       | 500        | 500     | 500
		"test.webp"  | "test.webp"    | 200       | 200        | 200     | 200
	}

	def "测试不支持的文件类型异常"() {
		given: "一个不存在或不支持的文件"
		File txtFile = new ClassPathResource("test.txt").getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "test.jpg")

		and: "构建裁剪配置"
		def operation = ImageOperations.graphicsMagick()

		when: "执行处理"
		template.process(new IOResource(txtFile), outputFile, operation)

		then: "抛出异常"
		thrown(UnsupportedResourceException)
	}

	def "测试图片透明度"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "opacity_${outputFilename}")

		and: "构建透明度配置"
		def operation = ImageOperations.graphicsMagick()
			.opacity(0.8f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.png" // 转为PNG支持透明度
		"test.png"    | "test.png"
	}

	def "测试缩放因子"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_factor_${factor}_${outputFilename}")

		and: "构建缩放因子配置"
		def operation = ImageOperations.graphicsMagick()
			.scale(factor)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		def expectImageSize = new ImageSize(width, height).scale(factor)
		outputImage.width == expectImageSize.width || outputImage.height == expectImageSize.height
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height | factor
		"camera.jpg"  | "camera.jpg"   | 3016  | 4032   | 0.5
		"test.jpg"    | "test.jpg"     | 1125  | 877    | 0.5
		"test.png"    | "test.png"     | 4095  | 2559   | 0.5
	}

	def "测试按角度旋转"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "rotate_angle_${angle}_${outputFilename}")

		and: "构建旋转配置"
		def operation = ImageOperations.graphicsMagick()
			.rotate(angle)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | angle
		"camera.jpg"  | "camera.jpg"   | 45.0
		"test.jpg"    | "test.jpg"     | 90.0
		"test.png"    | "test.png"     | 180.0
	}

	def "测试图片质量"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "quality_${quality}_${outputFilename}")

		and: "构建质量配置"
		def operation = ImageOperations.graphicsMagick()
			.quality(quality)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | quality
		"camera.jpg"  | "camera.jpg"   | 50
		"test.jpg"    | "test.jpg"     | 90
		"test.png"    | "test.png"     | 75
	}

	def "测试去除元数据"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "strip_${outputFilename}")

		and: "构建去除元数据配置"
		def operation = ImageOperations.graphicsMagick()
			.stripProfiles()

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试DPI设置"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "dpi_${dpi}_${outputFilename}")

		and: "构建DPI配置"
		def operation = ImageOperations.graphicsMagick()
			.dpi(dpi)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | dpi
		"camera.jpg"  | "camera.jpg"   | 150
		"test.jpg"    | "test.jpg"     | 300
		"test.png"    | "test.png"     | 72
	}

	def "测试重采样滤镜"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "filter_${filter}_${outputFilename}")

		and: "构建重采样滤镜配置"
		def operation = ImageOperations.graphicsMagick()
			.scale(500, 500)
			.resizeFilter(filter)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | filter
		"camera.jpg"  | "camera.jpg"   | ResampleFilter.LANCZOS
		"test.jpg"    | "test.jpg"     | ResampleFilter.CUBIC
		"test.png"    | "test.png"     | ResampleFilter.TRIANGLE
	}

	def "测试文字水印透明度"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "text_opacity_${outputFilename}")

		and: "构建文字水印透明度配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkText("测试水印")
			.textWatermarkFontName("simhei")
			.textWatermarkOpacity(0.6f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试文字水印颜色"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "text_color_${outputFilename}")

		and: "构建文字水印颜色配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkText("测试水印")
			.textWatermarkFontName("simhei")
			.textWatermarkColor(Color.RED)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试文字水印描边"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "text_stroke_${outputFilename}")

		and: "构建文字水印描边配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkText("测试水印")
			.textWatermarkFontName("simhei")
			.textWatermarkStroke(true)
			.textWatermarkStrokeColor(Color.BLACK)
			.textWatermarkStrokeWidth(2)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试图片水印透明度"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "image_opacity_${outputFilename}")

		and: "构建图片水印透明度配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()),
				new ImageSize(800, 206))
			.watermarkImageOpacity(0.7f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试图片水印相对缩放因子"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "image_scale_${outputFilename}")

		and: "构建图片水印相对缩放因子配置"
		def operation = ImageOperations.graphicsMagick()
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()),
				new ImageSize(800, 206))
			.watermarkImageRelativeScaleFactor(0.2)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
	}

	def "测试特殊组合1: 不添加图片水印的组合所有操作"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + filename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), outputFilename)

		and: "构建所有操作配置（不包含图片水印）"
		def genericOperation = ImageOperations.generic()
			.cropByCenter(cropWidth, cropHeight)
			.scale(targetW, targetH)
			.rotate(RotateDirection.CLOCKWISE_90)
			.flip(FlipDirection.VERTICAL)
			.grayscale()
		def bufferedOperation = ImageOperations.graphicsMagick(genericOperation)
			.resizeFilter(ResampleFilter.LANCZOS)
			.watermarkText("测试水印")
			.watermarkTextFont("simhei")
			.watermarkTextFillColor(Color.RED)
			.watermarkImageOpacity(0.7f)
			.watermarkTextStroke()
			.watermarkTextStrokeColor(Color.BLACK)
			.watermarkTextStrokeWidth(2)
			.sharpen(1.2)
			.blur(2.0)
			.dpi(200)
			.stripProfiles()
			.quality(75)
			.opacity(0.9f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, bufferedOperation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		filename     | outputFilename | cropWidth | cropHeight | targetW | targetH
		"camera.jpg" | "camera.jpg"   | 500       | 500        | 500     | 500
		"test.bmp"   | "test.bmp"     | 50        | 50         | 50      | 50
		"test.ico"   | "test.jpg"     | 20        | 20         | 20      | 20 //只支持读取，不支持写入
		"test.jpg"   | "test.jpg"     | 500       | 500        | 500     | 500
		"test.png"   | "test.png"     | 500       | 500        | 500     | 500
		"test.svg"   | "test.jpg"     | 300       | 300        | 400     | 400
		"test.tiff"  | "test.tiff"    | 500       | 500        | 500     | 500
		"test.webp"  | "test.webp"    | 200       | 200        | 200     | 200
	}

	def "测试特殊组合2: 不加裁剪、翻转、矫正方向、模糊和文字水印的基础上组合剩余操作并添加图片水印"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + filename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), outputFilename)

		and: "构建操作配置（排除裁剪、翻转、旋转、模糊、文字水印，包含图片水印）"
		def genericOperation = ImageOperations.generic()
			.scale(targetW, targetH)
			.grayscale()
		def bufferedOperation = ImageOperations.graphicsMagick(genericOperation)
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()),
				new ImageSize(800, 206))
			.watermarkImageOpacity(0.6f)
			.watermarkImageRelativeScaleFactor(0.15)
			.resizeFilter(ResampleFilter.LANCZOS)
			.sharpen(1.0)
			.dpi(150)
			.stripProfiles()
			.quality(80)
			.opacity(0.95f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, bufferedOperation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		filename     | outputFilename | targetW | targetH
		"camera.jpg" | "camera.jpg"   | 500     | 500
		"test.bmp"   | "test.bmp"     | 50      | 50
		"test.ico"   | "test.jpg"     | 20      | 20 //只支持读取，不支持写入
		"test.jpg"   | "test.jpg"     | 500     | 500
		"test.png"   | "test.png"     | 500     | 500
		"test.svg"   | "test.jpg"     | 400     | 400
		"test.tiff"  | "test.tiff"    | 500     | 500
		"test.webp"  | "test.webp"    | 200     | 200
	}

	def "测试特殊组合3: 用图片水印组合所有操作"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + filename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), outputFilename)

		and: "构建所有操作配置（包含图片水印）"
		def genericOperation = ImageOperations.generic()
			.cropByCenter(cropWidth, cropHeight)
			.scale(targetW, targetH)
			.rotate(RotateDirection.CLOCKWISE_90)
			.flip(FlipDirection.VERTICAL)
			.grayscale()
		def bufferedOperation = ImageOperations.graphicsMagick(genericOperation)
			.watermarkImage(new IOResource(new ClassPathResource("images/watermark.png").getFile()),
				new ImageSize(800, 206))
			.watermarkImageOpacity(0.6f)
			.watermarkImageRelativeScaleFactor(0.15)
			.resizeFilter(ResampleFilter.LANCZOS)
			.sharpen(1.2)
			.blur(2.0)
			.dpi(200)
			.stripProfiles()
			.quality(75)
			.opacity(0.9f)

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, bufferedOperation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		filename     | outputFilename | cropWidth | cropHeight | targetW | targetH
		"camera.jpg" | "camera.jpg"   | 350       | 350        | 500     | 500
		"test.bmp"   | "test.bmp"     | 20        | 20         | 50      | 50
		"test.ico"   | "test.jpg"     | 10        | 10         | 20      | 20 //只支持读取，不支持写入
		"test.jpg"   | "test.jpg"     | 350       | 350        | 500     | 500
		"test.png"   | "test.png"     | 350       | 350        | 500     | 500
		"test.svg"   | "test.jpg"     | 300       | 300        | 400     | 400
		"test.tiff"  | "test.tiff"    | 350       | 350        | 500     | 500
		"test.webp"  | "test.webp"    | 100       | 100        | 200     | 200
	}

	def "测试特殊组合4: 不执行任何操作，只转换格式"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "convert_${outputFilename}")

		and: "构建空操作配置（只转换格式）"
		def operation = ImageOperations.graphicsMagick()

		when: "执行处理"
		template.process(new IOResource(sourceFile), outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.png"
		"test.bmp"    | "test.jpg"
		"test.gif"    | "test.jpg"
		"test.ico"    | "test.jpg" //只支持读取，不支持写入
		"test.jpg"    | "test.png"
		"test.png"    | "test.jpg"
		"test.svg"    | "test.jpg"
		"test.tiff"   | "test.jpg"
		"test.webp"   | "test.jpg"
	}

	def "test2"() {
		setup:
		//File sourceFile = new ClassPathResource("images/" + "watermark.png").getFile()
		File sourceFile = new File("E:\\Roaming\\test.png")
		File outputFile = new File("E:\\Roaming", "output.png")

		def genericOperation = ImageOperations.generic()
		//.cropByCenter(350, 350)
		//.scale(500, 500)
		//.rotate(36)
		//.flip(FlipDirection.VERTICAL)
		//.grayscale()
		def operations = ImageOperations.graphicsMagick(genericOperation)
		//.resizeFilter(ResampleFilter.LANCZOS)
		//.watermarkText("测试水印")
		//.watermarkTextFont(new File("E:\\Roaming\\test.ttf"))
		//.watermarkTextFillColor(Color.RED)
		//.watermarkTextOpacity(0.7f)
		//.watermarkTextStroke()
		//.watermarkDirection(Direction.BOTTOM_RIGHT)
		//.watermarkTextMargin(20)
		//.watermarkTextStrokeColor(Color.WHITE)
		//.watermarkTextStrokeWidth(1)
		//.watermarkTextFontSize(55)
		//.watermarkImage(new GraphicsMagickResource(new ClassPathResource("images/" + "watermark.png").getFile(),gmService.getConnection()))
			.threshold()
		//.brightness(120)
		//.opacity(0.5f)
		//.blur(2.0)
		//.median()
		//.unsharp()
		//.sharpen()
		//.emboss()
		//.dpi(200)
			.stripProfiles()
		//.quality(75)


		template.process(new IOResource(sourceFile), outputFile, operations)
	}

	def "test"() {
		setup:
		GridTileOptions options = new GridTileOptions(20, 20)
		options.setLayout(TileLayout.XYZ)

		GraphicsMagickUtils.splitByGrid(new File("E:\\Roaming\\camera.jpg"),
			new File("E:\\Roaming\\tiles"), options, gmService.getConnection())
	}
}