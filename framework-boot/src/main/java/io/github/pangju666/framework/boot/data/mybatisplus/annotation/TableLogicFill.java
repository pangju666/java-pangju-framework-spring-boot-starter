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

package io.github.pangju666.framework.boot.data.mybatisplus.annotation;

import java.lang.annotation.*;

/**
 * 逻辑删除字段填充注解
 * <p>
 * 用于标注需要在逻辑删除时自动填充值的字段
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TableLogicFill {
	/**
	 * 逻辑删除时需要填充的值
	 *
	 * @return 填充值
	 * @since 1.0.0
	 */
	String value();
}