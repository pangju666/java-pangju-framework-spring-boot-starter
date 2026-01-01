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

package io.github.pangju666.framework.boot.data.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.github.pangju666.framework.boot.data.mybatisplus.annotation.TableLogicFill;
import io.github.pangju666.framework.data.mybatisplus.model.entity.BaseEntity;

import java.util.Date;

/**
 * 逻辑删除基础实体类
 * <p>
 * 在{@link BaseEntity}基础上增加了删除时间和删除状态字段，
 * 用于支持逻辑删除功能。
 * </p>
 *
 * @param <ID> ID的类型参数
 * @author pangju666
 * @since 1.0.0
 */
public abstract class LogicBaseEntity<ID> extends BaseEntity {
	/**
	 * 删除时间（逻辑删除时自动填充为当前时间）
	 *
	 * @since 1.0.0
	 */
	@TableLogicFill(value = "CURRENT_TIMESTAMP")
	@TableField("delete_time")
	private Date deleteTime;
	/**
	 * 删除状态，0表示未删除，删除时设置为表数据行ID
	 *
	 * @since 1.0.0
	 */
	@TableLogic(value = "0", delval = "id")
	@TableField("delete_status")
	private ID deleteStatus;

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public ID getDeleteStatus() {
		return deleteStatus;
	}

	public void setDeleteStatus(ID deleteStatus) {
		this.deleteStatus = deleteStatus;
	}
}
