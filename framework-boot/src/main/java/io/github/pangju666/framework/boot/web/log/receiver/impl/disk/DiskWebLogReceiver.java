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

package io.github.pangju666.framework.boot.web.log.receiver.impl.disk;

import com.google.gson.Gson;
import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.receiver.WebLogReceiver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 基于磁盘文件的 Web 日志接收器。
 *
 * <p><b>职责</b></p>
 * <ul>
 *   <li>接收 {@link WebLog} 对象，序列化为 JSON 行文本。</li>
 *   <li>将序列化后的日志通过阻塞队列交给后台写线程异步落盘。</li>
 *   <li>根据日期自动滚动日志文件，避免单文件过大。</li>
 * </ul>
 *
 * <p><b>线程与背压</b></p>
 * <ul>
 *   <li>内部维护一个 {@code LinkedBlockingQueue} 作为背压队列，满时调用方会阻塞，防止 OOM。</li>
 *   <li>单独的后台写线程（非守护线程）轮询队列并写入文件，确保在 JVM 退出前能够正常 flush。</li>
 *   <li>通过 {@code running} 标志控制线程生命周期，退出时会尝试写完剩余消息并关闭文件句柄。</li>
 * </ul>
 *
 * <p><b>文件滚动策略</b></p>
 * <ul>
 *   <li>以当前日期（格式见 {@link Constants#DATE_FORMAT}）作为文件名的一部分，并追加扩展名 {@code .log}。</li>
 *   <li>当检测到日期变更时，主动关闭旧文件并创建新文件。</li>
 *   <li>可选的基础文件名用于附加区分后缀，具体拼接规则见 {@link #rotateFile(LocalDate)}。</li>
 * </ul>
 *
 * <p><b>资源管理</b></p>
 * <ul>
 *   <li>构造器会确保日志目录存在，不存在则尝试创建。</li>
 *   <li>调用 {@link #shutdown()} 以停止写线程并等待其结束，确保资源释放。</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DiskWebLogReceiver implements WebLogReceiver {
	protected static final Logger logger = LoggerFactory.getLogger(DiskWebLogReceiver.class);

    /**
     * 日期格式（来自公共常量），用于构造日志文件名。
	 *
	 * @since 1.0.0
	 */
	protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    /**
     * 日志文件扩展名。
	 *
	 * @since 1.0.0
	 */
	protected static final String FILE_EXTENSION = ".log";

    /**
     * 当前写入文件对应的日期，用于判断是否需要滚动文件。
	 *
	 * @since 1.0.0
	 */
	protected volatile LocalDate currentDate = null;
    /**
     * 当前打开的文件写入器。
	 *
	 * @since 1.0.0
	 */
	protected volatile BufferedWriter currentWriter = null;
    /**
     * 当前写入文件的路径。
	 *
	 * @since 1.0.0
	 */
	protected volatile Path currentFilePath = null;
    /**
     * 写线程运行标志；置为 {@code false} 后将尝试退出循环并清理资源。
	 *
	 * @since 1.0.0
	 */
	protected volatile boolean running = true;

    /**
     * 文件名基础后缀，用于区分不同日志来源或类型。
	 *
	 * @since 1.0.0
	 */
	protected final String baseFilename;
    /**
     * 日志文件写入目录。
	 *
	 * @since 1.0.0
	 */
	protected final Path directory;
    /**
     * 文件写入缓冲区大小（字节）。
	 *
	 * @since 1.0.0
	 */
	protected final int bufferSize;
    /**
     * 写线程关闭等待时长（毫秒）。
	 *
	 * @since 1.0.0
	 */
	protected final int writeThreadDestroyWaitMills;
    /**
     * 背压队列，保存待写入的 网络日志JSON文本。
	 *
	 * @since 1.0.0
	 */
	protected final BlockingQueue<String> queue; // 背压保护
    /**
     * 后台写线程。
	 *
	 * @since 1.0.0
	 */
	protected final Thread writerThread;
    /**
     * JSON 序列化器。
	 *
	 * @since 1.0.0
     */
    protected final Gson gson;

    /**
     * 构造一个基于磁盘的日志接收器。
     *
     * <p>会确保写入目录存在，并启动后台写线程。</p>
     *
     * @param directory 日志写入目录路径（将自动创建）
     * @param baseFilename 基础文件名后缀（可为空或空白）
     * @param bufferSize 写入缓冲区大小（字节）
     * @param queueSize 背压队列容量（条）
     * @param writeThreadDestroyWaitMills 关闭时等待写线程结束的时长（毫秒）
     * @throws IOException 当目录创建失败或文件 IO 初始化失败时抛出
	 * @since 1.0.0
     */
    public DiskWebLogReceiver(String directory, String baseFilename, int bufferSize, int queueSize,
                              int writeThreadDestroyWaitMills) throws IOException {
		this.directory = Paths.get(directory);
		try {
			Files.createDirectories(this.directory);
		} catch (IOException e) {
			throw new IOException("网络日志文件目录: " + directory + " 创建失败", e);
		}

		this.writeThreadDestroyWaitMills = writeThreadDestroyWaitMills;
		this.gson = JsonUtils.createGsonBuilder().setPrettyPrinting().create();
		this.queue = new LinkedBlockingQueue<>(queueSize);
		this.baseFilename = baseFilename;
		this.bufferSize = bufferSize;

		this.writerThread = new Thread(this::writeLoop, "webLogWriteThread");
		// 确保 JVM 退出前能 flush
		this.writerThread.setDaemon(false);
		this.writerThread.start();
	}

    /**
     * 接收一条 Web 日志并入队等待写入。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>当接收器已关闭（{@code running == false}）时抛出 {@link IllegalStateException}。</li>
     *   <li>队列满时阻塞调用线程，形成背压以防止内存膨胀。</li>
     *   <li>入参为 {@code null} 时忽略该日志。</li>
     * </ul>
     *
     * @param webLog 待写入的日志对象，可为 {@code null}
     */
    @Override
    public void receive(WebLog webLog) {
		if (Objects.nonNull(webLog)) {
			if (!running) {
				throw new IllegalStateException("网络日志文件写入线程已关闭");
			}
			// 队列满时会阻塞，防止 OOM
			try {
				queue.put(JsonUtils.toString(webLog, gson));
			} catch (InterruptedException e) {
				logger.error("网络日志放入文件写入队列失败", e);
			}
		}
	}

    /**
     * 后台写线程主循环。
     *
     * <p>
     * 轮询队列，按日期变更滚动文件，并将消息逐行写入。异常将被记录，
     * 最终确保关闭当前写入器以释放资源。
     * </p>
	 *
	 * @since 1.0.0
     */
    protected void writeLoop() {
		try {
			while (running || !queue.isEmpty()) {
				String message = queue.poll(1, TimeUnit.SECONDS);
				if (message != null) {
					// 检查是否需要切换日期
					LocalDate today = LocalDate.now();
					if (!today.equals(currentDate)) {
						rotateFile(today);
					}
					currentWriter.write(message);
					currentWriter.newLine();
				}
			}
		} catch (InterruptedException e) {
			logger.error("文件写入队列获取网络日志失败", e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			logger.error("网络日志写入文件失败", e);
		} finally {
			closeCurrentWriter();
		}
	}

    /**
     * 滚动到指定日期对应的日志文件。
     *
     * <p>
     * 会关闭旧的写入器并创建新的文件与写入器。文件名由日期与扩展名组成，
     * 若配置了基础文件名则附加区分后缀。
     * </p>
     *
     * @param newDate 新的日期
     * @throws IOException 当文件创建或打开失败时抛出
	 * @since 1.0.0
     */
    protected synchronized void rotateFile(LocalDate newDate) throws IOException {
		// 关闭旧 writer
		closeCurrentWriter();

		// 创建新文件
		String fileName = newDate.format(DATE_FORMAT) + FILE_EXTENSION;
		if (StringUtils.isNotBlank(baseFilename)) {
			fileName = baseFilename + "-" + fileName;
		}
		currentFilePath = directory.resolve(fileName);
		currentWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentFilePath.toFile(),
			true), StandardCharsets.UTF_8), bufferSize);
		currentDate = newDate;
	}

    /**
     * 关闭并清理当前写入器（若存在）。
     *
     * <p>会尝试 flush 后再关闭，异常将被记录。</p>
	 *
	 * @since 1.0.0
     */
    protected synchronized void closeCurrentWriter() {
		if (Objects.nonNull(currentWriter)) {
			try {
				currentWriter.flush();
				currentWriter.close();
			} catch (IOException e) {
				logger.error("网络日志文件写入器关闭失败", e);
			} finally {
				currentWriter = null;
			}
		}
	}

    /**
     * 停止后台写线程并尝试处理完剩余消息。
     *
     * @throws InterruptedException 当等待写线程结束时被中断
	 * @since 1.0.0
     */
    public void shutdown() throws InterruptedException {
		running = false;
		// 等待指定时间处理完剩余消息
		writerThread.join(writeThreadDestroyWaitMills);
		if (writerThread.isAlive()) {
			logger.error("网络日志文件写入线程没有正常终止");
		}
	}
}
