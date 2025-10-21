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

package io.github.pangju666.framework.autoconfigure.web.idempotent.validator.impl;

import io.github.pangju666.framework.autoconfigure.web.idempotent.annotation.Idempotent;
import io.github.pangju666.framework.autoconfigure.web.idempotent.validator.IdempotentValidator;
import net.jodah.expiringmap.ExpiringMap;

public class ExpireMapIdempotentValidator implements IdempotentValidator {
	private final ExpiringMap<String, Boolean> expiringMap;

	public ExpireMapIdempotentValidator() {
		this.expiringMap = ExpiringMap.builder()
			.variableExpiration()
			.build();
	}

	@Override
	public boolean validate(String key, Idempotent repeat) {
		if (expiringMap.containsKey(key)) {
			return false;
		}
		expiringMap.put(key, Boolean.TRUE, repeat.interval(), repeat.timeUnit());
		return true;
	}

	@Override
	public void remove(String key, Idempotent repeat) {
		expiringMap.remove(key);
	}
}
