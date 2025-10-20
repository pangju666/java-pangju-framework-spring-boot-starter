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

package io.github.pangju666.framework.autoconfigure.web.log.revceiver.impl;

import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.framework.autoconfigure.web.log.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.log.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.log.revceiver.WebLogReceiver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class MongoWebLogReceiver implements WebLogReceiver {
	private final MongoTemplate mongoTemplate;
	private final WebLogProperties properties;

	public MongoWebLogReceiver(WebLogProperties properties, BeanFactory beanFactory) {
		if (StringUtils.hasText(properties.getMongo().getTemplateBeanName())) {
			this.mongoTemplate = beanFactory.getBean(properties.getMongo().getTemplateBeanName(), MongoTemplate.class);
		} else {
			this.mongoTemplate = beanFactory.getBean(MongoTemplate.class);
		}
		this.properties = properties;
	}

	@Override
	public void receive(WebLog webLog) {
		String date = DateFormatUtils.formatDate(new Date());
		String prefix = Optional.ofNullable(properties.getMongo())
			.map(WebLogProperties.Mongo::getCollectionPrefix)
			.orElse(null);
		String collectionName = Objects.nonNull(prefix) ? prefix + "-" + date : date;
		if (!mongoTemplate.collectionExists(collectionName)) {
			mongoTemplate.createCollection(collectionName);
		}
		WebLogDocument document = new WebLogDocument();
		BeanUtils.copyProperties(webLog, document);
		mongoTemplate.save(document, collectionName);
	}
}
