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

package io.github.pangju666.framework.autoconfigure.spring.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Bean工具类，提供对象属性复制、集合转换等常用操作
 * <p>
 * 该工具类优化了Spring的BeanUtils功能，提供了null安全的对象转换和集合处理方法。
 * 主要功能包括：
 * <ul>
 *     <li>对象属性复制</li>
 *     <li>对象转换</li>
 *     <li>集合转换</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see org.springframework.beans.BeanUtils
 * @since 1.0.0
 */
public class BeanUtils {
	protected BeanUtils() {
	}

	/**
	 * 复制源对象属性到目标对象
	 * <p>
	 * 该方法使用Spring的BeanUtils.copyProperties方法复制属性，
	 * 会复制所有名称相同且类型兼容的属性。如果源对象或目标对象为null，则不执行复制操作。
	 * </p>
	 *
	 * @param source 源对象
	 * @param target 目标对象
	 * @param <S>    源对象类型
	 * @param <T>    目标对象类型
	 * @see org.springframework.beans.BeanUtils#copyProperties(Object, Object)
	 * @since 1.0.0
	 */
	public static <S, T> void copyProperties(final S source, final T target) {
		copyProperties(source, target, null);
	}

	/**
	 * 复制源对象属性到目标对象，并在复制后执行自定义操作
	 * <p>
	 * 该方法首先使用Spring的BeanUtils.copyProperties方法复制属性，
	 * 然后通过提供的BiConsumer回调函数执行自定义操作，可用于处理特殊字段或复杂对象。
	 * 如果源对象或目标对象为null，则不执行复制操作和自定义操作。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * User source = new User("张三", 25);
	 * UserDTO target = new UserDTO();
	 * BeanUtils.copyProperties(source, target, (s, t) -> {
	 *     t.setFullName(s.getName());
	 *     t.setDisplayAge(s.getAge() + "岁");
	 * });
	 * }</pre>
	 * </p>
	 *
	 * @param source   源对象
	 * @param target   目标对象
	 * @param consumer 复制后的自定义操作，可为null
	 * @param <S>      源对象类型
	 * @param <T>      目标对象类型
	 * @see org.springframework.beans.BeanUtils#copyProperties(Object, Object)
	 * @since 1.0.0
	 */
	public static <S, T> void copyProperties(final S source, final T target, final BiConsumer<S, T> consumer) {
		if (Objects.nonNull(source) && Objects.nonNull(target)) {
			org.springframework.beans.BeanUtils.copyProperties(source, target);
			if (Objects.nonNull(consumer)) {
				consumer.accept(source, target);
			}
		}
	}

	/**
	 * 将源集合转换为目标类型的列表，过滤掉null元素
	 * <p>
	 * 该方法使用提供的转换器将源集合中的每个非null元素转换为目标类型，
	 * 并返回转换后的列表。默认会过滤掉源集合中的null元素。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * List<UserDTO> users = getUserList();
	 * List<UserDTO> dtos = BeanUtils.convertCollection(users, user -> {
	 *     UserDTO dto = new UserDTO();
	 *     BeanUtils.copyProperties(user, dto);
	 *     return dto;
	 * });
	 * }</pre>
	 * </p>
	 *
	 * @param source    源集合
	 * @param converter 转换器，用于将源类型转换为目标类型
	 * @param <S>       源类型
	 * @param <T>       目标类型
	 * @return 转换后的目标类型列表，如果源集合为空则返回空列表
	 * @since 1.0.0
	 */
	public static <S, T> List<T> convertCollection(final Collection<S> source, final Converter<S, T> converter) {
		return convertCollection(source, true, converter);
	}

	/**
	 * 将源集合转换为目标类型的列表，可选是否过滤null元素
	 * <p>
	 * 该方法使用提供的转换器将源集合中的元素转换为目标类型，
	 * 并返回转换后的列表。可以通过filterNull参数控制是否过滤掉源集合中的null元素。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * List<User> users = Arrays.asList(new User("John", 25), null, new User("Jane", 30));
	 * // 保留null元素，转换后的列表中对应位置为null
	 * List<UserDTO> dtosWithNull = BeanUtils.convertCollection(users, false, user -> {
	 *     if (user == null) return null;
	 *     UserDTO dto = new UserDTO();
	 *     BeanUtils.copyProperties(user, dto);
	 *     return dto;
	 * });
	 * }</pre>
	 * </p>
	 *
	 * @param source     源集合
	 * @param filterNull 是否过滤null元素，true表示过滤，false表示保留
	 * @param converter  转换器，用于将源类型转换为目标类型
	 * @param <S>        源类型
	 * @param <T>        目标类型
	 * @return 转换后的目标类型列表，如果源集合为空则返回空列表
	 * @since 1.0.0
	 */
	public static <S, T> List<T> convertCollection(final Collection<S> source, boolean filterNull, final Converter<S, T> converter) {
		if (CollectionUtils.isEmpty(source)) {
			return Collections.emptyList();
		}
		Stream<S> stream = source.stream();
		if (filterNull) {
			stream = stream.filter(Objects::nonNull);
		}
		return stream.map(value -> convert(value, converter)).toList();
	}

	/**
	 * 将单个源对象转换为目标类型
	 * <p>
	 * 该方法使用提供的转换器将源对象转换为目标类型。
	 * 如果源对象为null，则返回null。
	 * </p>
	 *
	 * <p>
	 * 示例:
	 * <pre>{@code
	 * User user = getUser();
	 * UserDTO dto = BeanUtils.convert(user, u -> {
	 *     UserDTO result = new UserDTO();
	 *     BeanUtils.copyProperties(u, result);
	 *     return result;
	 * });
	 * }</pre>
	 * </p>
	 *
	 * @param source    源对象
	 * @param converter 转换器，用于将源类型转换为目标类型
	 * @param <S>       源类型
	 * @param <T>       目标类型
	 * @return 转换后的目标类型对象，如果源对象为null则返回null
	 * @since 1.0.0
	 */
	public static <S, T> T convert(final S source, final Converter<S, T> converter) {
		if (Objects.isNull(source)) {
			return null;
		}
		return converter.convert(source);
	}
}