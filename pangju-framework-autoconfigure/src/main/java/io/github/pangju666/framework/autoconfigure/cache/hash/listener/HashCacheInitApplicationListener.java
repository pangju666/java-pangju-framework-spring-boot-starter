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

package io.github.pangju666.framework.autoconfigure.cache.hash.listener;

import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheManager;
import io.github.pangju666.framework.autoconfigure.cache.hash.HashCacheProcessor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

public class HashCacheInitApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		try {
			HashCacheManager hashCacheManager = event.getApplicationContext().getBean(HashCacheManager.class);
			Map<String, HashCacheProcessor> beanMap = event.getApplicationContext().getBeansOfType(HashCacheProcessor.class);
			for (HashCacheProcessor processor : beanMap.values()) {
				processor.init(hashCacheManager);
			}
		} catch (NoSuchBeanDefinitionException ignored) {
		}
	}
}
