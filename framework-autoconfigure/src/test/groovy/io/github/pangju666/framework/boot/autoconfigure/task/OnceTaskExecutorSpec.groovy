package io.github.pangju666.framework.boot.autoconfigure.task

import io.github.pangju666.framework.boot.task.OnceTaskExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.task.AsyncTaskExecutor
import spock.lang.Specification

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(classes = [OnceTaskExecutorAutoConfiguration.class, TaskExecutionAutoConfiguration.class])
class OnceTaskExecutorSpec extends Specification {
	@Autowired
    OnceTaskExecutor executor
	@Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
	@Autowired
	AsyncTaskExecutor asyncExecutor

	def "同步执行按键去重且只运行一次（注入）"() {
		setup:
		def pool = Executors.newFixedThreadPool(8)
		def barrier = new CountDownLatch(1)
		def started = new CountDownLatch(8)
		def counter = new AtomicInteger(0)
		Callable<String> task = {
			started.countDown()
			barrier.await()
			counter.incrementAndGet()
			"成功"
		} as Callable<String>

		List<Future<String>> futures = (1..8).collect {
			pool.submit({ executor.execute("键-同步-注入", task) } as Callable<String>)
		}
		started.await(1, TimeUnit.SECONDS)
		barrier.countDown()
		def results = futures.collect { it.get(1, TimeUnit.SECONDS) }

		assert results.every { it == "成功" }
		assert counter.get() == 1

		pool.shutdownNow()
	}

	def "同键任务执行中，带超时调用抛出超时（注入）"() {
		def longTaskStarted = new CountDownLatch(1)
		Callable<Integer> longTask = {
			longTaskStarted.countDown()
			Thread.sleep(300)
			7
		} as Callable<Integer>
		def bg = Executors.newSingleThreadExecutor()
		Future<Integer> f1 = bg.submit({ executor.execute("键-超时-注入", longTask) } as Callable<Integer>)
		longTaskStarted.await(1, TimeUnit.SECONDS)

		when:
		executor.execute("键-超时-注入", { 1 } as Callable<Integer>, 50, TimeUnit.MILLISECONDS)

		then:
		thrown(TimeoutException)

		and:
		assert f1.get(2, TimeUnit.SECONDS) == 7

		cleanup:
		bg.shutdownNow()
	}

	def "异步提交按键去重并返回同一Future（注入）"() {
		setup:
		def counter = new AtomicInteger(0)
		Callable<String> task = { counter.incrementAndGet(); Thread.sleep(50); "成功" } as Callable<String>

		def f1 = executor.submitToAsyncExecutor(asyncExecutor, "键-异步-注入", task)
		def f2 = executor.submitToAsyncExecutor(asyncExecutor, "键-异步-注入", { "忽略" } as Callable<String>)
		def r = f1.get(2, TimeUnit.SECONDS)

		assert f1.is(f2)
		assert r == "成功"
		assert counter.get() == 1

		def f3 = executor.submitToAsyncExecutor(asyncExecutor, "键-异步-注入", { "成功2" } as Callable<String>)
		assert !f3.is(f1)
		assert f3.get(2, TimeUnit.SECONDS) == "成功2"
	}

	def "异步提交异常完成并传播错误（注入）"() {
		Callable<String> exTask = { throw new IllegalArgumentException("错误参数") } as Callable<String>

		when:
		def fut = executor.submitToAsyncExecutor(asyncExecutor, "键-异步-异常-注入", exTask)
		fut.get(2, TimeUnit.SECONDS)

		then:
		thrown(ExecutionException)
		assert fut.isCompletedExceptionally()
	}

	def "同步 空键 抛 IllegalArgumentException（注入）"() {
		when: executor.execute("", { 1 } as Callable<Integer>)
		then: thrown(IllegalArgumentException)
	}

	def "同步 任务为空 抛 IllegalArgumentException（注入）"() {
		when: executor.execute("键", null)
		then: thrown(IllegalArgumentException)
	}

	def "超时 非正数 抛 IllegalArgumentException（注入）"() {
		when: executor.execute("键", { 1 } as Callable<Integer>, 0, TimeUnit.MILLISECONDS)
		then: thrown(IllegalArgumentException)
	}

	def "超时 单位为空 抛 IllegalArgumentException（注入）"() {
		when: executor.execute("键", { 1 } as Callable<Integer>, 1, null)
		then: thrown(IllegalArgumentException)
	}

	def "异步 执行器为空 抛 IllegalArgumentException（注入）"() {
		when: executor.submitToAsyncExecutor(null, "键", { 1 } as Callable<Integer>)
		then: thrown(IllegalArgumentException)
	}

	def "异步 空键 抛 IllegalArgumentException（注入）"() {
		when: executor.submitToAsyncExecutor(asyncExecutor, "", { 1 } as Callable<Integer>)
		then: thrown(IllegalArgumentException)
	}

	def "异步 任务为空 抛 IllegalArgumentException（注入）"() {
		when: executor.submitToAsyncExecutor(asyncExecutor, "键", null)
		then: thrown(IllegalArgumentException)
	}

	def "同步高并发：64 线程同键仅执行一次"() {
		setup:
		int threads = 64
		def pool = Executors.newFixedThreadPool(threads)
		def start = new CountDownLatch(1)
		def ready = new CountDownLatch(threads)
		def counter = new AtomicInteger(0)
		def jitter = new Random()
		Callable<String> task = {
			Thread.sleep(jitter.nextInt(5))
			counter.incrementAndGet()
			"成功"
		} as Callable<String>

		List<Future<String>> futures = (1..threads).collect {
			pool.submit({
				ready.countDown()
				start.await()
				executor.execute("键-同步-压力", task)
			} as Callable<String>)
		}
		assert ready.await(1, TimeUnit.SECONDS)
		start.countDown()
		def results = futures.collect { it.get(3, TimeUnit.SECONDS) }

		assert results.every { it == "成功" }
		assert counter.get() == 1

		pool.shutdownNow()
	}

	def "同步多键并发：100 键并发，每键仅执行一次"() {
		setup:
		int keys = 100
		int perKeyCalls = 8
		def pool = Executors.newFixedThreadPool(64)
		Map<Integer, AtomicInteger> counters = (0..<keys).collectEntries { [(it): new AtomicInteger(0)] }

		List<Future<Integer>> firstRound = (0..<keys).collect { k ->
			def key = "键-多-${k}"
			Callable<Integer> slowTask = {
				Thread.sleep(100)
				counters[k].incrementAndGet()
				1
			} as Callable<Integer>
			pool.submit({ executor.execute(key, slowTask) } as Callable<Integer>)
		}

		Thread.sleep(50)

		List<Future<Integer>> secondRound = []
		(0..<keys).each { k ->
			def key = "键-多-${k}"
			Callable<Integer> fastTask = { 1 } as Callable<Integer>
			(0..<(perKeyCalls - 1)).each {
				secondRound << pool.submit({ executor.execute(key, fastTask) } as Callable<Integer>)
			}
		}

		def results = (firstRound + secondRound).collect { it.get(10, TimeUnit.SECONDS) }

		assert results.size() == keys * perKeyCalls
		assert results.sum() == keys * perKeyCalls
		assert counters.values().every { it.get() == 1 }

		pool.shutdown()
		pool.awaitTermination(5, TimeUnit.SECONDS)
	}

	def "异步高并发：同键仅返回同一 Future 且仅执行一次"() {
		setup:
		int calls = 64
		def counter = new AtomicInteger(0)
		def jitter = new Random()
		Callable<String> task = {
			Thread.sleep(20 + jitter.nextInt(10))
			counter.incrementAndGet()
			"成功"
		} as Callable<String>

		def first = executor.submitToAsyncExecutor(asyncExecutor, "键-异步-压力", task)
		List<CompletableFuture<String>> futures = (1..(calls-1)).collect {
			executor.submitToAsyncExecutor(asyncExecutor, "键-异步-压力", { "忽略" } as Callable<String>)
		}
		def all = [first] + futures
		assert all.every { it.is(first) }
		def results = all.collect { it.get(4, TimeUnit.SECONDS) }

		assert results.every { it == "成功" }
		assert counter.get() == 1
	}

	def "异步多键并发：100 键并发，每键仅执行一次"() {
		setup:
		int keys = 100
		int perKeyCalls = 8
		Map<Integer, AtomicInteger> counters = (0..<keys).collectEntries { [(it): new AtomicInteger(0)] }
		def jitter = new Random()

		List<CompletableFuture<Integer>> futures = []
		(0..<keys).each { k ->
			def key = "键-异步-多-${k}"
			Callable<Integer> task = {
				Thread.sleep(10 + jitter.nextInt(10))
				counters[k].incrementAndGet()
				1
			} as Callable<Integer>
			futures << executor.submitToAsyncExecutor(asyncExecutor, key, task)
			(1..<perKeyCalls).each {
				futures << executor.submitToAsyncExecutor(asyncExecutor, key, { 1 } as Callable<Integer>)
			}
		}
		def results = futures.collect { it.get(6, TimeUnit.SECONDS) }

		assert results.sum() == keys * perKeyCalls
		assert counters.values().every { it.get() == 1 }
	}
}