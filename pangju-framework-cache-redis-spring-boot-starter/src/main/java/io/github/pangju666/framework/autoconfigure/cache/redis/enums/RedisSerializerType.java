package io.github.pangju666.framework.autoconfigure.cache.redis.enums;

import org.springframework.data.redis.serializer.RedisSerializer;

public enum RedisSerializerType {
	STRING(RedisSerializer.string()),
	JAVA(RedisSerializer.java()),
	JSON(RedisSerializer.json()),
	BYTE_ARRAY(RedisSerializer.byteArray());

	private final RedisSerializer<?> serializer;

	RedisSerializerType(RedisSerializer<?> serializer) {
		this.serializer = serializer;
	}

	public RedisSerializer<?> getSerializer() {
		return serializer;
	}
}
