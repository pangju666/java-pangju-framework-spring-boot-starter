package io.github.pangju666.framework.boot.autoconfigure.image

import io.github.pangju666.commons.image.enums.FlipDirection
import io.github.pangju666.commons.image.enums.RotateDirection
import io.github.pangju666.commons.io.utils.FileUtils
import io.github.pangju666.framework.boot.image.core.impl.BufferedImageTemplate
import io.github.pangju666.framework.boot.image.enums.ResampleFilter
import io.github.pangju666.framework.boot.image.exception.UnSupportedTypeException
import io.github.pangju666.framework.boot.image.model.ImageFile
import io.github.pangju666.framework.boot.image.utils.ImageOperationBuilders
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Subject

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@ContextConfiguration(classes = [ImageAutoConfiguration.class], loader = SpringBootContextLoader.class)
class BufferedImageTemplateSpec extends Specification {
	@Subject
	BufferedImageTemplate template = new BufferedImageTemplate()

	def "测试读取图片信息: #fileName"() {
		given:
		File file = new ClassPathResource("images/" + fileName).getFile()
		boolean canRead = template.canRead(file)
		ImageFile imageFile = null
		Exception exception = null

		when:
		try {
			imageFile = template.read(file)
		} catch (Exception e) {
			exception = e
		}

		then:
		if (canRead) {
			assert exception == null
			assert imageFile != null
			assert imageFile.file == file
			assert imageFile.imageSize != null
			assert imageFile.imageSize.getVisualSize().width == width
			assert imageFile.imageSize.getVisualSize().height == height
			// 兼容 ico 的不同 mime type
			if (fileName == "test.ico" && imageFile.mimeType == "image/x-icon") {
				assert true
			} else {
				assert imageFile.mimeType == mimeType
			}
			assert imageFile.getImageSize().getOrientation() == orientation
		} else {
			assert exception instanceof UnSupportedTypeException
		}

		where:
		fileName     | width | height | mimeType                   | orientation
		"camera.jpg" | 3016  | 4032   | "image/jpeg"               | 6
		"test.bmp"   | 71    | 96     | "image/bmp"                | null
		"test.gif"   | 478   | 448    | "image/gif"                | null
		"test.ico"   | 32    | 32     | "image/vnd.microsoft.icon" | null
		"test.jpg"   | 1125  | 877    | "image/jpeg"               | null
		"test.png"   | 4095  | 2559   | "image/png"                | null
		"test.svg"   | 512   | 512    | "image/svg+xml"            | null
		"test.tiff"  | 1200  | 1200   | "image/tiff"               | null
		"test.webp"  | 550   | 368    | "image/webp"               | null
	}

	def "测试图片缩放: 目标宽 #targetW"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_width_${targetW}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperationBuilders.buffered()
			.scaleByWidth(targetW)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

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
		"test.ico"    | "test.ico"     | 20
		"test.jpg"    | "test.jpg"     | 500
		"test.png"    | "test.png"     | 500
		"test.svg"    | "test.jpg"     | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 500
		"test.webp"   | "test.jpg"     | 200 //只支持读取，不支持写入
	}

	def "测试图片缩放: 目标高 #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_height_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperationBuilders.buffered()
			.scaleByHeight(targetH)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

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
		"test.ico"    | "test.ico"     | 20
		"test.jpg"    | "test.jpg"     | 500
		"test.png"    | "test.png"     | 500
		"test.svg"    | "test.jpg"     | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 500
		"test.webp"   | "test.jpg"     | 200 //只支持读取，不支持写入
	}

	def "测试范围缩放: #targetW x #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "scale_${targetW}_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperationBuilders.buffered()
			.scaleByRange(targetW, targetH)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

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
		"test.ico"    | "test.ico"     | 20      | 20
		"test.jpg"    | "test.jpg"     | 500     | 500
		"test.png"    | "test.png"     | 500     | 500
		"test.svg"    | "test.jpg"     | 10      | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 500     | 500
		"test.webp"   | "test.jpg"     | 200     | 200 //只支持读取，不支持写入
	}

	def "测试强制缩放: #targetW x #targetH"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "force_scale_${targetW}_${targetH}_${outputFilename}")

		and: "构建缩放操作配置"
		def operation = ImageOperationBuilders.buffered()
			.forceScale(targetW, targetH)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

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
		"test.ico"    | "test.ico"     | 20      | 20
		"test.jpg"    | "test.jpg"     | 500     | 500
		"test.png"    | "test.png"     | 500     | 500
		"test.svg"    | "test.jpg"     | 10      | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 500     | 500
		"test.webp"   | "test.jpg"     | 200     | 200 //只支持读取，不支持写入
	}

	def "测试图片中心裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.cropByCenter(width, height)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == width && outputImage.height == height
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height
		"camera.jpg"  | "camera.jpg"   | 500   | 500
		"test.bmp"    | "test.bmp"     | 50    | 50
		"test.gif"    | "test.gif"     | 50    | 50
		"test.ico"    | "test.ico"     | 20    | 20
		"test.jpg"    | "test.jpg"     | 500   | 500
		"test.png"    | "test.png"     | 500   | 500
		"test.svg"    | "test.jpg"     | 10    | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 500   | 500
		"test.webp"   | "test.jpg"     | 200   | 200 //只支持读取，不支持写入
	}

	def "测试图片矩形裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.cropByRect(x, y, width, height)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == width && outputImage.height == height
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | x   | y   | width | height
		"camera.jpg"  | "camera.jpg"   | 100 | 100 | 500   | 500
		"test.bmp"    | "test.bmp"     | 10  | 10  | 50    | 50
		"test.gif"    | "test.gif"     | 10  | 10  | 50    | 50
		"test.ico"    | "test.ico"     | 10  | 10  | 20    | 20
		"test.jpg"    | "test.jpg"     | 100 | 100 | 500   | 500
		"test.png"    | "test.png"     | 100 | 100 | 500   | 500
		"test.svg"    | "test.jpg"     | 10  | 10  | 10    | 10 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 100 | 100 | 500   | 500
		"test.webp"   | "test.jpg"     | 100 | 100 | 200   | 200 //只支持读取，不支持写入
	}

	def "测试图片偏移裁剪"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "crop_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.cropByOffset(top, bottom, left, right)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证裁剪后尺寸"
		outputFile.exists()
		BufferedImage outputImage = ImageIO.read(outputFile)
		outputImage.width == (width - left - right) && outputImage.height == (height - top - bottom)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | width | height | top | bottom | left | right
		"camera.jpg"  | "camera.jpg"   | 3016  | 4032   | 500 | 500    | 500  | 500
		"test.bmp"    | "test.bmp"     | 71    | 96     | 20  | 20     | 20   | 20
		"test.gif"    | "test.gif"     | 478   | 448    | 100 | 100    | 100  | 100
		"test.ico"    | "test.ico"     | 32    | 32     | 10  | 10     | 10   | 10
		"test.jpg"    | "test.jpg"     | 1125  | 877    | 200 | 200    | 200  | 200
		"test.png"    | "test.png"     | 4095  | 2559   | 500 | 500    | 500  | 500
		"test.svg"    | "test.jpg"     | 512   | 512    | 10  | 5      | 10   | 5 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 1200  | 1200   | 500 | 500    | 500  | 200
		"test.webp"   | "test.jpg"     | 550   | 368    | 100 | 100    | 300  | 100 //只支持读取，不支持写入
	}

	def "测试旋转"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "rotate_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.rotate(direction)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

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
		"test.ico"    | "test.jpg"     | 32    | 32     | RotateDirection.COUNTER_CLOCKWISE_90 // ico 不支持旋转
		"test.jpg"    | "test.jpg"     | 1125  | 877    | RotateDirection.CLOCKWISE_90
		"test.png"    | "test.png"     | 4095  | 2559   | RotateDirection.CLOCKWISE_90
		"test.svg"    | "test.jpg"     | 512   | 512    | RotateDirection.CLOCKWISE_90 //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | 1200  | 1200   | RotateDirection.CLOCKWISE_90
		"test.webp"   | "test.jpg"     | 550   | 368    | RotateDirection.CLOCKWISE_90 //只支持读取，不支持写入
	}

	def "测试翻转"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "flip_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.flip(direction)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename | direction
		"camera.jpg"  | "camera.jpg"   | FlipDirection.HORIZONTAL
		"test.bmp"    | "test.bmp"     | FlipDirection.HORIZONTAL
		"test.gif"    | "test.gif"     | FlipDirection.HORIZONTAL
		"test.ico"    | "test.ico"     | FlipDirection.HORIZONTAL
		"test.jpg"    | "test.jpg"     | FlipDirection.VERTICAL
		"test.png"    | "test.png"     | FlipDirection.VERTICAL
		"test.svg"    | "test.jpg"     | FlipDirection.VERTICAL    //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"    | FlipDirection.VERTICAL
		"test.webp"   | "test.jpg"     | FlipDirection.VERTICAL   //只支持读取，不支持写入
	}

	def "测试图像灰度化"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "grayscale_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.grayscale()
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试图像调整亮度"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "brightness_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.brightness(0.8)
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试图像调整对比度"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "contrast_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.contrast()
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试图像调整锐化"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "sharpen_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.sharpen()
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试图像调整模糊"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "blur_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.blur()
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试文字水印"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "text_watermark_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.watermarkText("测试水印")
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试图片水印"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + inputFilename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "image_watermark_${outputFilename}")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.buffered()
			.watermarkImage(new ClassPathResource("images/watermark.png").getFile())
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, operation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		inputFilename | outputFilename
		"camera.jpg"  | "camera.jpg"
		"test.bmp"    | "test.bmp"
		"test.gif"    | "test.gif"
		"test.ico"    | "test.jpg"
		"test.jpg"    | "test.jpg"
		"test.png"    | "test.png"
		"test.svg"    | "test.jpg" //只支持读取，不支持写入
		"test.tiff"   | "test.tiff"
		"test.webp"   | "test.jpg" //只支持读取，不支持写入
	}

	def "测试组合操作: 裁剪 -> 缩放 -> 旋转 -> 翻转 -> 灰度化 -> 亮度 -> 对比度 -> 锐化 -> 模糊 -> 图像水印 -> 转格式"() {
		given: "准备源文件和输出文件"
		File sourceFile = new ClassPathResource("images/" + filename).getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), outputFilename)

		and: "构建裁剪配置"
		def genericOperation = ImageOperationBuilders.generic()
			.cropByCenter(cropWidth, cropHeight)
			.scaleByRange(targetW, targetH)
			.rotate(RotateDirection.CLOCKWISE_90)
			.flip(FlipDirection.VERTICAL)
			.grayscale()
			.watermarkImage(new ClassPathResource("images/watermark.png").getFile())
			.build()
		def bufferedOperation = ImageOperationBuilders.buffered(genericOperation)
			.resampleFilter(ResampleFilter.LANCZOS)
			.brightness(0.8)
			.contrast()
			.sharpen()
			.blur()
			.build()

		when: "执行处理"
		template.process(sourceFile, outputFile, bufferedOperation)

		then: "验证输出文件"
		outputFile.exists()
		ImageIO.read(outputFile)
		FileUtils.forceDelete(outputFile)

		where:
		filename     | outputFilename  | cropWidth | cropHeight | targetW | targetH
		"camera.jpg" | "camera.jpg"    | 500       | 500        | 500     | 500
		"test.bmp"   | "test.bmp"      | 50        | 50         | 50      | 50
		"test.gif"   | "test.gif"      | 50        | 50         | 50      | 50
		"test.ico"   | "test.jpg"      | 20        | 20         | 20      | 20
		"test.jpg"   | "test.jpg"      | 500       | 500        | 500     | 500
		"test.png"   | "test.png"      | 500       | 500        | 500     | 500
		"test.svg"   | "test.svg.jpg"  | 300       | 300        | 400     | 400 //只支持读取，不支持写入
		"test.tiff"  | "test.tiff"     | 500       | 500        | 500     | 500
		"test.webp"  | "test.webp.jpg" | 200       | 200        | 200     | 200 //只支持读取，不支持写入
	}

	def "测试不支持的文件类型异常"() {
		given: "一个不存在或不支持的文件"
		File txtFile = new ClassPathResource("test.txt").getFile()
		File outputFile = new File(FileUtils.getTempDirectoryPath(), "test.jpg")

		and: "构建裁剪配置"
		def operation = ImageOperationBuilders.EMPTY;

		when: "执行处理"
		template.process(txtFile, outputFile, operation)

		then: "抛出异常"
		thrown(UnSupportedTypeException)
	}
}