package io.github.p3sto.chatbridge.bridge;

public interface MessageBridge {
	void connect();

	void subscribe(String channel);

	void publish(String channel, String message);

	void disconnect();

	@FunctionalInterface
	interface MessageHandler {

		void onMessage();
	}

	enum Type {
		REDIS, WEBSOCKET,
		;

		public static Type from(String type) {
			return switch (type) {
				case "redis" -> REDIS;
				case "websocket" -> WEBSOCKET;
				default -> null;
			};
		}
	}
}


