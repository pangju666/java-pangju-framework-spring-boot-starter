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
 * MongoDB Web 日志接收器。
 *
 * <p><b>概述</b></p>
 * <ul>
 *   <li>{@link WebLogReceiver} 的实现，将接收到的 {@link WebLog} 持久化到 MongoDB。</li>
 *   <li>按日期动态命名集合，并将日志映射为文档对象 {@link WebLogDocument} 后保存。</li>
 * </ul>
 *
 * <p><b>行为</b></p>
 * <ul>
 *   <li>基于当前日期生成集合名；可选集合前缀通过构造参数提供。</li>
 *   <li>在集合不存在时自动创建集合；随后将日志保存至该集合。</li>
 * </ul>
 *
 * <p><b>注意事项</b></p>
 * <ul>
 *   <li>本实现不负责索引管理或写入可靠性策略（例如重复写入、事务）；如需这些能力请在外部进行配置。</li>
 *   <li>集合命名规则为：{@code [prefix-]yyyy-MM-dd}；确保命名与归档策略符合运维规范。</li>
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
     * MongoTemplate 实例。
     *
     * <p><b>说明</b></p>
     * <ul>
     *   <li>用于执行 MongoDB 操作（如集合检测/创建、文档保存）。</li>
     *   <li>应在配置中预先完成连接参数与映射转换器设置。</li>
     * </ul>
     *
     * @since 1.0.0
     */
    private final MongoTemplate mongoTemplate;
    /**
     * 集合名前缀。
     *
     * <p><b>说明</b></p>
     * <ul>
     *   <li>用于生成按日归档的集合名称，最终格式为 {@code [prefix-]yyyy-MM-dd}。</li>
     *   <li>可为空；为空时仅使用日期作为集合名。</li>
     * </ul>
     */
    private final String collectionPrefix;

	public MongoWebLogReceiver(MongoTemplate mongoTemplate, @Nullable String collectionPrefix) {
		this.mongoTemplate = mongoTemplate;
		this.collectionPrefix = collectionPrefix;
	}

    /**
     * 接收并存储 Web 日志。
     *
     * <p><b>行为</b></p>
     * <ul>
     *   <li>生成集合名（含可选前缀与当前日期），若集合不存在则创建。</li>
     *   <li>将 {@link WebLog} 映射为 {@link WebLogDocument} 并保存至目标集合。</li>
     * </ul>
     *
     * @param webLog 接收到的日志数据
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
