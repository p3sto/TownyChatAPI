package io.github.p3sto.chatbridge.config;

import io.github.p3sto.chatbridge.TownyChatBridge;
import io.github.p3sto.chatbridge.bridge.MessageBridge;
import io.github.p3sto.chatbridge.utils.TimeUtils;
import io.lettuce.core.RedisURI;

import java.time.Duration;

import static io.github.p3sto.chatbridge.config.Settings.ConfigurationNodes.*;

public final class Settings extends Configuration {
	private RedisURI redisURI;

	public Settings(TownyChatBridge plugin, String fileName) {
		super(plugin, fileName);
	}

	public MessageBridge.Type getBridgeType() {
		String bridge = getString(BRIDGE_METHOD);
		return MessageBridge.Type.from(bridge);
	}

	public RedisURI getRedisUri() {
		if (redisURI == null) {
			RedisURI.Builder builder = RedisURI.builder();
			builder.withHost(getString(REDIS_HOST));

			// Port
			if (has(REDIS_PORT))
				builder.withPort(getInt(REDIS_PORT));

			// Authentication
			if (has(REDIS_USERNAME) && has(REDIS_PASSWORD)) {
				String user = getString(REDIS_USERNAME);
				String password = getString(REDIS_PASSWORD);
				builder.withAuthentication(user, password);
			}

			// Database
			if (has(REDIS_DATABASE))
				builder.withDatabase(getInt(REDIS_DATABASE));

			// Timeout
			if (has(REDIS_TIMEOUT)) {
				String strDuration = getString(REDIS_TIMEOUT);
				Duration duration = TimeUtils.parseDuration(strDuration);
				if (duration != null)
					builder.withTimeout(duration);
			}

			if (has(REDIS_CLIENT))
				builder.withClientName(getString(REDIS_CLIENT));

			redisURI = builder.build();
		}
		return redisURI;
	}


	enum ConfigurationNodes implements ConfigurationNode {
		BRIDGE_METHOD("bridge_method"),
		REDIS_HOST("redis.host"),
		REDIS_PORT("redis.port"),
		REDIS_USERNAME("redis.username"),
		REDIS_PASSWORD("redis.password"),
		REDIS_DATABASE("redis.database"),
		REDIS_TIMEOUT("redis.timeout"),
		REDIS_CLIENT("redis.client"),
		;

		private final String path;

		ConfigurationNodes(String path) {
			this.path = path;
		}

		@Override
		public String path() {
			return path;
		}
	}
}
