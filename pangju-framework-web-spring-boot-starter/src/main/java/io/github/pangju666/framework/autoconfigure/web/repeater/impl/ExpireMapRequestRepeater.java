package io.github.pangju666.framework.autoconfigure.web.repeater.impl;

import io.github.pangju666.framework.autoconfigure.web.annotation.validation.Repeat;
import io.github.pangju666.framework.autoconfigure.web.repeater.RequestRepeater;
import jakarta.servlet.http.HttpServletRequest;
import net.jodah.expiringmap.ExpiringMap;

public class ExpireMapRequestRepeater extends RequestRepeater {
	private final ExpiringMap<String, Boolean> expiringMap;

	public ExpireMapRequestRepeater() {
		super("_");
		this.expiringMap = ExpiringMap.builder()
			.variableExpiration()
			.build();
	}

	@Override
	public boolean tryAcquire(Repeat repeat, HttpServletRequest request) {
		String key = generateKey(repeat, request);
		if (expiringMap.containsKey(key)) {
			return false;
		}
		expiringMap.put(key, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}
}
