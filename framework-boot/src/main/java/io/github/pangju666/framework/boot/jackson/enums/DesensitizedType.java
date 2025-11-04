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

package io.github.pangju666.framework.boot.jackson.enums;

import io.github.pangju666.commons.lang.utils.DesensitizationUtils;
import io.github.pangju666.framework.boot.jackson.annotation.DesensitizeFormat;
import io.github.pangju666.framework.boot.jackson.serializer.DesensitizedJsonSerializer;
import org.springframework.core.convert.converter.Converter;

/**
 * 脱敏类型枚举，定义了支持的各种数据脱敏类型
 * <p>
 * 该枚举类用于在{@link DesensitizeFormat}注解中
 * 指定需要应用的脱敏策略类型。不同的脱敏类型对应不同的脱敏处理逻辑。
 * </p>
 *
 * @author pangju666
 * @see DesensitizeFormat
 * @see DesensitizedJsonSerializer
 * @since 1.0.0
 */
public enum DesensitizedType {
	/**
	 * 姓名脱敏
	 * <p>
	 * 对中文姓名进行脱敏，通常保留姓氏，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	CHINESE_NAME(DesensitizationUtils::hideChineseName),
	/**
	 * 军官证号码脱敏
	 * <p>
	 * 对军官证号码进行脱敏处理，保留关键部分，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	MILITARY_ID_NUMBER(DesensitizationUtils::hideMilitaryIdNumber),
	/**
	 * 护照号码脱敏
	 * <p>
	 * 对护照号码进行脱敏处理，通常保留前后部分，中间用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	PASSPORT_NUMBER(DesensitizationUtils::hidePassportNumber),
	/**
	 * 社会保障卡号脱敏
	 * <p>
	 * 对社会保障卡号进行脱敏处理，保留部分信息，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	SOCIAL_SECURITY_CARD_NUMBER(DesensitizationUtils::hideSocialSecurityCardNumber),
	/**
	 * 医保卡号脱敏
	 * <p>
	 * 对医保卡号进行脱敏处理，保留部分信息，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	MEDICAL_CARD_NUMBER(DesensitizationUtils::hideMedicalCardNumber),
	/**
	 * 身份证号脱敏
	 * <p>
	 * 对中国大陆身份证号码进行脱敏，通常保留前6位和后4位，中间用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	ID_CARD(DesensitizationUtils::hideIdCardNumber),
	/**
	 * 中国固定电话脱敏
	 * <p>
	 * 对固定电话号码进行脱敏，通常保留区号和后4位，中间用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	TEL_PHONE(DesensitizationUtils::hideTelPhone),
	/**
	 * 手机号脱敏
	 * <p>
	 * 对手机号码进行脱敏，通常保留前3位和后4位，中间用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	PHONE_NUMBER(DesensitizationUtils::hidePhoneNumber),
	/**
	 * 中国大陆地址脱敏
	 * <p>
	 * 对地址信息进行部分脱敏，通常保留省市区等行政区划信息，对详细地址进行隐藏
	 * </p>
	 *
	 * @since 1.0.0
	 */
	ADDRESS(DesensitizationUtils::hideAddress),
	/**
	 * 电子邮件脱敏
	 * <p>
	 * 对电子邮件地址进行脱敏，通常对用户名部分进行部分隐藏，保留域名部分
	 * </p>
	 *
	 * @since 1.0.0
	 */
	EMAIL(DesensitizationUtils::hideEmail),
	/**
	 * 密码脱敏
	 * <p>
	 * 对密码进行全部脱敏，通常替换为固定数量的*符号
	 * </p>
	 *
	 * @since 1.0.0
	 */
	PASSWORD(DesensitizationUtils::hidePassword),
	/**
	 * 车牌号脱敏
	 * <p>
	 * 对车牌号进行脱敏处理，通常保留部分特征信息，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	PLATE_NUMBER(DesensitizationUtils::hidePlateNumber),
	/**
	 * 车辆发动机号脱敏
	 * <p>
	 * 对车辆发动机号进行脱敏处理，保留部分信息，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	VEHICLE_ENGINE_NUMBER(DesensitizationUtils::hideVehicleEngineNumber),
	/**
	 * 车辆车架号脱敏
	 * <p>
	 * 对车辆车架号进行脱敏处理，保留部分信息，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	VEHICLE_FRAME_NUMBER(DesensitizationUtils::hideVehicleFrameNumber),
	/**
	 * 昵称脱敏
	 * <p>
	 * 对用户昵称进行脱敏处理，通常保留部分特征，其余用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	NICK_NAME(DesensitizationUtils::hideNickName),
	/**
	 * 银行卡号脱敏
	 * <p>
	 * 对银行卡号进行脱敏，通常保留前6位和后4位，中间用*代替
	 * </p>
	 *
	 * @since 1.0.0
	 */
	BANK_CARD(DesensitizationUtils::hideBankCard),
	/**
	 * 自定义脱敏类型
	 * <p>
	 * 当选择此类型时，需要通过{@link DesensitizeFormat}
	 * 的其他参数（如format、regex、prefix、suffix）来自定义脱敏规则
	 * </p>
	 *
	 * @since 1.0.0
	 */
	CUSTOM(value -> value);

	/**
	 * 字符串转换器，用于执行具体的脱敏操作
	 *
	 * @since 1.0.0
	 */
	private final Converter<String, String> converter;

	/**
	 * 构造方法
	 *
	 * @param converter 用于执行脱敏操作的字符串转换器
	 * @since 1.0.0
	 */
	DesensitizedType(Converter<String, String> converter) {
		this.converter = converter;
	}

	/**
	 * 获取当前脱敏类型对应的字符串转换器
	 *
	 * @return 字符串转换器，用于执行脱敏操作
	 * @since 1.0.0
	 */
	public Converter<String, String> getConverter() {
		return converter;
	}
}