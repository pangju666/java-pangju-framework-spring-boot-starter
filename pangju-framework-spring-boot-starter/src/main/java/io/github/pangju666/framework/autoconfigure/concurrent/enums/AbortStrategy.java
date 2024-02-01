package io.github.pangju666.framework.autoconfigure.concurrent.enums;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public enum AbortStrategy {
	CALLER_RUNS(new ThreadPoolExecutor.CallerRunsPolicy()),
	ABORT(new ThreadPoolExecutor.AbortPolicy()),
	DISCARD(new ThreadPoolExecutor.DiscardPolicy()),
	DISCARD_OLDEST(new ThreadPoolExecutor.DiscardOldestPolicy());

	private final RejectedExecutionHandler executionHandler;

	AbortStrategy(RejectedExecutionHandler executionHandler) {
		this.executionHandler = executionHandler;
	}

	public RejectedExecutionHandler getExecutionHandler() {
		return executionHandler;
	}
}