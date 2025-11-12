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

import io.github.pangju666.framework.boot.web.log.model.WebLog;
import io.github.pangju666.framework.data.mongodb.lang.MongoConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
 * MongoDB Web 日志文档实体类
 * <p>
 * 该类用于表示存储在 MongoDB 中的 Web 日志记录，继承自 {@link WebLog}，
 * 并通过 Spring Data MongoDB 提供的注解映射为 MongoDB 的集合文档。
 * 该类添加了 MongoDB 的 `_id` 字段作为主键标识。
 * </p>
 *
 * @author pangju666
 * @see WebLog
 * @see Document
 * @since 1.0.0
 */
@Document
public class WebLogDocument extends WebLog {
	/**
	 * MongoDB 文档主键
	 * <p>
	 * 表示存储到 MongoDB 集合中的文档 `_id` 字段，使用 String 类型作为主键。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	@MongoId(value = FieldType.STRING)
	@Field(name = MongoConstants.ID_FIELD_NAME)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}