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

package io.github.pangju666.framework.boot.image.core;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.framework.boot.image.exception.ImageOperationException;
import io.github.pangju666.framework.boot.image.exception.ImageParsingException;
import io.github.pangju666.framework.boot.image.exception.ImageTaskExecutionException;
import io.github.pangju666.framework.boot.image.exception.UnSupportedTypeException;
import io.github.pangju666.framework.boot.image.model.ImageFile;
import io.github.pangju666.framework.boot.image.model.ImageOperation;
import io.github.pangju666.framework.boot.task.OnceTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 图像处理任务执行器。
 * <p>
 * 封装图像处理的统一执行流程：通过 {@link OnceTaskExecutor} 控制同一任务键的单次执行，
 * 支持超时与异常处理；具体的图像操作由 {@link ImageTemplate} 提供，实现输入/输出文件的
 * 处理逻辑。
 * </p>
 * <p>
 * 特点：
 * <ul>
 * <li>任务键：以输出文件绝对路径（系统分隔符规范化）为唯一键，确保同一路径的任务不会并发执行。</li>
 * <li>超时控制：调用执行器的带超时方法，超时抛出 {@link TimeoutException} 交由调用方处理。</li>
 * <li>异常语义：对业务异常（解析、类型不支持、操作失败、I/O）向上抛出；中断异常捕获后恢复中断标记并抛出
 *   {@link ImageTaskExecutionException}；其它未知异常记录错误日志。</li>
 * <li>执行形态：支持同步执行（无超时/带超时）与异步执行（基于 {@link AsyncTaskExecutor}）。</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageTaskExecutor {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ImageTaskExecutor.class);

	/**
	 * 图像处理模板。
	 * <p>封装具体的图像处理策略（解析、操作、输出）。</p>
	 *
	 * @since 1.0.0
	 */
	protected final ImageTemplate<?> template;
	/**
	 * 单次任务执行器。
	 * <p>按任务键保证同一键在同一时间仅执行一次，并提供超时控制。</p>
	 *
	 * @since 1.0.0
	 */
	protected final OnceTaskExecutor executor;
	/**
	 * 异步任务执行器。
	 * <p>用于以异步方式提交图像处理任务并返回 {@link CompletableFuture}。</p>
	 *
	 * @since 1.0.0
	 */
	protected final AsyncTaskExecutor asyncTaskExecutor;

	/**
	 * 构造图像任务执行器。
	 *
	 * @param template 图像处理模板
	 * @param executor 单次任务执行器
	 * @param asyncTaskExecutor 异步任务执行器
	 * @since 1.0.0
	 */
	public ImageTaskExecutor(ImageTemplate<?> template, OnceTaskExecutor executor, AsyncTaskExecutor asyncTaskExecutor) {
		this.template = template;
		this.executor = executor;
		this.asyncTaskExecutor = asyncTaskExecutor;
	}

	/**
	 * 同步执行图像处理（不设置超时）。
	 * <p>
	 * 以输出文件路径构造任务键后，使用 {@link OnceTaskExecutor#execute(String, java.util.concurrent.Callable)}
	 * 提交并同步等待完成。
	 * </p>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @throws IOException               I/O 异常
	 * @throws UnSupportedTypeException  不支持的图像类型
	 * @throws ImageParsingException     图像解析失败
	 * @throws ImageOperationException   图像操作失败
	 * @throws ImageTaskExecutionException 当前线程被中断时抛出，已恢复中断标记
	 * @since 1.0.0
	 */
	public void process(File inputFile, File outputFile, ImageOperation operation)
		throws IOException, UnSupportedTypeException, ImageParsingException, ImageOperationException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try {
			executor.execute("image_operation_task_" +
				FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
				template.process(inputFile, outputFile, operation);
				return null;
			});
		} catch (InterruptedException e) {
			// 恢复中断状态
			Thread.currentThread().interrupt();
			throw new ImageTaskExecutionException("因为当前线程被中断，导致图像处理任务执行失败", e);
		} catch (IOException | UnSupportedTypeException | ImageParsingException | ImageOperationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("图像处理任务执行失败，输入文件：{}", inputFile.getAbsolutePath(), e);
		}
	}

	/**
	 * 同步执行图像处理（设置超时）。
	 * <p>
	 * 以纳秒为单位转换 {@link Duration} 并调用带超时的执行方法；超时将抛出 {@link TimeoutException}。
	 * </p>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @param timeout    超时时长
	 * @throws IOException               I/O 异常
	 * @throws UnSupportedTypeException  不支持的图像类型
	 * @throws ImageParsingException     图像解析失败
	 * @throws ImageOperationException   图像操作失败
	 * @throws TimeoutException          执行超时
	 * @throws ImageTaskExecutionException 当前线程被中断时抛出，已恢复中断标记
	 * @since 1.0.0
	 */
	public void process(File inputFile, File outputFile, ImageOperation operation, Duration timeout)
		throws IOException, UnSupportedTypeException, ImageParsingException, ImageOperationException, TimeoutException {
		Assert.notNull(timeout, "timeout 不可为 null");
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try {
			executor.execute("image_operation_task_" +
				FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
				template.process(inputFile, outputFile, operation);
				return null;
			}, timeout.toNanos(), TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			// 恢复中断状态
			Thread.currentThread().interrupt();
			throw new ImageTaskExecutionException("因为当前线程被中断，导致图像处理任务执行失败", e);
		} catch (IOException | UnSupportedTypeException | ImageParsingException | ImageOperationException | TimeoutException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("图像处理任务执行失败，输入文件：{}", inputFile.getAbsolutePath(), e);
		}
	}

	/**
	 * 同步执行图像处理（不设置超时，使用已解析的 {@link ImageFile}）。
	 *
	 * @param imageFile  已解析的图像文件对象
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @throws IOException               I/O 异常
	 * @throws UnSupportedTypeException  不支持的图像类型
	 * @throws ImageParsingException     图像解析失败
	 * @throws ImageOperationException   图像操作失败
	 * @throws ImageTaskExecutionException 当前线程被中断时抛出，已恢复中断标记
	 * @since 1.0.0
	 */
	public void process(ImageFile imageFile, File outputFile, ImageOperation operation)
		throws IOException, UnSupportedTypeException, ImageParsingException, ImageOperationException {
		Assert.notNull(imageFile, "imageFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try {
			executor.execute("image_operation_task_" +
				FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
				template.process(imageFile, outputFile, operation);
				return null;
			});
		} catch (InterruptedException e) {
			// 恢复中断状态
			Thread.currentThread().interrupt();
			throw new ImageTaskExecutionException("因为当前线程被中断，导致图像处理任务执行失败", e);
		} catch (IOException | UnSupportedTypeException | ImageParsingException | ImageOperationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("图像处理任务执行失败，输入文件：{}", imageFile.getFile().getAbsolutePath(), e);
		}
	}

	/**
	 * 同步执行图像处理（设置超时，使用已解析的 {@link ImageFile}）。
	 *
	 * @param imageFile  已解析的图像文件对象
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @param timeout    超时时长
	 * @throws IOException               I/O 异常
	 * @throws UnSupportedTypeException  不支持的图像类型
	 * @throws ImageParsingException     图像解析失败
	 * @throws ImageOperationException   图像操作失败
	 * @throws TimeoutException          执行超时
	 * @throws ImageTaskExecutionException 当前线程被中断时抛出，已恢复中断标记
	 * @since 1.0.0
	 */
	public void process(ImageFile imageFile, File outputFile, ImageOperation operation, Duration timeout)
		throws IOException, UnSupportedTypeException, ImageParsingException, ImageOperationException, TimeoutException {
		Assert.notNull(timeout, "timeout 不可为 null");
		Assert.notNull(imageFile, "imageFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try {
			executor.execute("image_operation_task_" +
				FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
				template.process(imageFile, outputFile, operation);
				return null;
			}, timeout.toNanos(), TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			// 恢复中断状态
			Thread.currentThread().interrupt();
			throw new ImageTaskExecutionException("因为当前线程被中断，导致图像处理任务执行失败", e);
		} catch (IOException | UnSupportedTypeException | ImageParsingException | ImageOperationException | TimeoutException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("图像处理任务执行失败，输入文件：{}", imageFile.getFile().getAbsolutePath(), e);
		}
	}

	/**
	 * 异步执行图像处理（原始输入文件）。
	 * <p>
	 * 使用 {@link AsyncTaskExecutor} 提交任务并返回 {@link CompletableFuture}；在任务体中执行参数校验与处理。
	 * 成功结果为 {@code outputFile}；异常将封装在 {@link CompletableFuture} 中按约定传播。
	 * </p>
	 *
	 * @param inputFile  输入文件
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @return 异步结果，成功时为 {@code outputFile}
	 * @since 1.0.0
	 */
	public CompletableFuture<File> processAsync(File inputFile, File outputFile, ImageOperation operation) {
		return executor.submitToAsyncExecutor(asyncTaskExecutor, "image_operation_task_" +
			FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
			FileUtils.checkFile(inputFile, "inputFile 不可为 null");
			FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

			template.process(inputFile, outputFile, operation);
			return outputFile;
		});
	}

	/**
	 * 异步执行图像处理（已解析的 {@link ImageFile}）。
	 *
	 * @param imageFile  已解析的图像文件对象
	 * @param outputFile 输出文件
	 * @param operation  图像操作
	 * @return 异步结果，成功时为 {@code outputFile}
	 * @since 1.0.0
	 */
	public CompletableFuture<File> processAsync(ImageFile imageFile, File outputFile, ImageOperation operation) {
		return executor.submitToAsyncExecutor(asyncTaskExecutor, "image_operation_task_" +
			FilenameUtils.separatorsToSystem(outputFile.getAbsolutePath()), () -> {
			Assert.notNull(imageFile, "imageFile 不可为 null");
			FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

			template.process(imageFile, outputFile, operation);
			return outputFile;
		});
	}
}
