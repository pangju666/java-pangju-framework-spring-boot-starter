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

package io.github.pangju666.framework.autoconfigure.http.enums;

import io.github.pangju666.commons.lang.utils.DesensitizationUtils;
import org.springframework.core.convert.converter.Converter;

public enum DesensitizedType {
	CHINESE_NAME(DesensitizationUtils::hideChineseName),
	MILITARY_ID_NUMBER(DesensitizationUtils::hideMilitaryIdNumber),
	PASSPORT_NUMBER(DesensitizationUtils::hidePassportNumber),
	SOCIAL_SECURITY_CARD_NUMBER(DesensitizationUtils::hideSocialSecurityCardNumber),
	MEDICAL_CARD_NUMBER(DesensitizationUtils::hideMedicalCardNumber),
	ID_CARD(DesensitizationUtils::hideIdCardNumber),
	TEL_PHONE(DesensitizationUtils::hideTelPhone),
	PHONE_NUMBER(DesensitizationUtils::hidePhoneNumber),
	ADDRESS(DesensitizationUtils::hideAddress),
	EMAIL(DesensitizationUtils::hideEmail),
	PASSWORD(DesensitizationUtils::hidePassword),
	PLATE_NUMBER(DesensitizationUtils::hidePlateNumber),
	VEHICLE_ENGINE_NUMBER(DesensitizationUtils::hideVehicleEngineNumber),
	VEHICLE_FRAME_NUMBER(DesensitizationUtils::hideVehicleFrameNumber),
	NICK_NAME(DesensitizationUtils::hideNickName),
	BANK_CARD(DesensitizationUtils::hideBankCard),
	CUSTOM(value -> value);

	private final Converter<String, String> converter;

	DesensitizedType(Converter<String, String> converter) {
		this.converter = converter;
	}

	public Converter<String, String> getConverter() {
		return converter;
	}
}