package io.github.pangju666.framework.autoconfigure.web.revceiver.impl;

import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.framework.autoconfigure.web.model.WebLog;
import io.github.pangju666.framework.autoconfigure.web.model.WebLogDocument;
import io.github.pangju666.framework.autoconfigure.web.properties.WebLogProperties;
import io.github.pangju666.framework.autoconfigure.web.revceiver.WebLogReceiver;
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
