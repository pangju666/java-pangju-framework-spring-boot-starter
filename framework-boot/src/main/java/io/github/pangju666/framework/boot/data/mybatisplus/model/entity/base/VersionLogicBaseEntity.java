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

package io.github.pangju666.framework.boot.data.mybatisplus.model.entity.base;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.pangju666.framework.data.mybatisplus.model.entity.VersionBaseEntity;

/**
 * 乐观锁和逻辑删除基础实体类
 * <p>
 * 结合了{@link VersionBaseEntity}和{@link LogicBaseEntity}的功能，
 * 同时支持乐观锁和完整的逻辑删除功能。
 * </p>
 *
 * @param <ID> ID的类型参数
 * @author pangju666
 * @since 1.0.0
 */
public abstract class VersionLogicBaseEntity<ID> extends LogicBaseEntity<ID> {
	/**
	 * 版本号，用于乐观锁控制
	 *
	 * @since 1.0.0
	 */
	@TableField("version")
	@Version
	private Integer version;

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
