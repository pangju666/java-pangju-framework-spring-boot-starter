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

package io.github.pangju666.framework.boot.web.log.revceiver.impl;

import io.github.pangju666.commons.lang.utils.DateFormatUtils;
import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.boot.web.log.revceiver.WebLogReceiver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.Nullable;

import java.util.Date;

/**
 * MongoDB Web 日志接收器
 * <p>
 * 该类是 {@link WebLogReceiver} 的实现，用于将接收到的 {@link WebLog} 日志数据存储到 MongoDB。
 * 通过动态创建集合名称和将日志映射为文档对象 {@link WebLogDocument}，实现对日志的持久化存储。
 * </p>
 *
 * <p>功能说明：</p>
 * <ul>
 *     <li>从配置中动态获取或创建 MongoTemplate，用于操作 MongoDB 数据库。</li>
 *     <li>支持根据当前日期动态生成集合名称，结构化存储日志数据。</li>
 *     <li>将接收到的 {@link WebLog} 数据映射为 MongoDB 文档对象并存储。</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *     <li>需要将 Web 日志数据持久化至 MongoDB，用于后续分析或归档。</li>
 *     <li>日志集合按日期分组存储，便于分类管理和查询。</li>
 * </ul>
 *
 * <p>实现逻辑：</p>
 * <ul>
 *     <li>根据 {@link WebLogProperties.Mongo} 动态确定目标集合的前缀和名称。</li>
 *     <li>检测目标集合是否存在，若不存在则自动创建集合。</li>
 *     <li>将日志从 {@link WebLog} 转换为 {@link WebLogDocument}，并存入目标集合。</li>
 * </ul>
 *
 * @author pangju666
 * @see WebLogReceiver
 * @see WebLog
 * @see WebLogDocument
 * @see MongoTemplate
 * @since 1.0.0
 */
public class MongoWebLogReceiver implements WebLogReceiver {
	/**
	 * MongoTemplate 实例
	 * <p>
	 * 用于执行与 MongoDB 相关的操作，例如插入日志文档、创建集合等。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final MongoTemplate mongoTemplate;
	private final String collectionPrefix;

	public MongoWebLogReceiver(MongoTemplate mongoTemplate, @Nullable String collectionPrefix) {
		this.mongoTemplate = mongoTemplate;
		this.collectionPrefix = collectionPrefix;
	}

	/**
	 * 接收并存储 Web 日志
	 * <p>
	 * 将传入的 {@link WebLog} 数据转换为 MongoDB 集合文档 {@link WebLogDocument} 并存储。
	 * 集合名称根据日期动态生成，并在集合不存在时自动创建。
	 * </p>
	 *
	 * <p>具体流程：</p>
	 * <ol>
	 *     <li>根据当前日期生成日志集合名称，集合前缀可由配置指定。</li>
	 *     <li>检测集合是否存在，若不存在则创建。</li>
	 *     <li>将日志数据转换为 {@link WebLogDocument} 并存储至集合。</li>
	 * </ol>
	 *
	 * @param webLog 接收到的日志数据 {@link WebLog}
	 */
	@Override
	public void receive(WebLog webLog) {
		String date = DateFormatUtils.formatDate(new Date());
		String collectionName = StringUtils.isNotBlank(collectionPrefix) ? collectionPrefix + "-" + date : date;
		if (!mongoTemplate.collectionExists(collectionName)) {
			mongoTemplate.createCollection(collectionName);
		}
		WebLogDocument document = new WebLogDocument();
		BeanUtils.copyProperties(webLog, document);
		mongoTemplate.save(document, collectionName);
	}
}
