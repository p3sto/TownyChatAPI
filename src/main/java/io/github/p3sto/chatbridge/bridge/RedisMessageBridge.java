package io.github.p3sto.chatbridge.bridge;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.util.StringMgmt;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.bukkit.entity.Player;

public class RedisMessageBridge implements MessageBridge {
	private final RedisURI uri;
	private RedisClient client;
	private StatefulRedisPubSubConnection<String, String> pubSub;

	public RedisMessageBridge(RedisURI uri) {
		this.uri = uri;
	}

	@Override
	public void connect() {
		client = RedisClient.create(uri);
		pubSub = client.connectPubSub();
	}

	/**
	 * Listen for incoming messages from Redis
	 *
	 * @param channel The channel id
	 */
	@Override
	public void subscribe(String channel) {
		RedisPubSubAsyncCommands<String, String> async = pubSub.async();
		pubSub.addListener(new RedisPubSubAdapter<>() {
			@Override
			public void message(String channel, String message) {
				String[] identifiers = channel.split(":");

				if (identifiers.length < 2)
					throw new IllegalArgumentException("Channel name is invalid");

				String group = identifiers[0];
				String id = identifiers[1];

				// Check if channel is town, nation, alliance chat
				TownyAPI api = TownyAPI.getInstance();
				switch (group) {
					case "town" -> {
						Town town = api.getTown(id);
						if (town != null)
							TownyMessaging.sendPrefixedTownMessage(town, message);
					}
					case "nation" -> {
						Nation nation = api.getNation(id);
						if (nation != null)
							TownyMessaging.sendPrefixedNationMessage(nation, message);
					}
					case "ally" -> {
						Nation nation = api.getNation(id);
						if (nation != null) {
							for (Player p : api.getOnlinePlayersAlliance(nation))
								TownyMessaging.sendMessage(p, Translation.of(
										"default_nation_prefix",
										StringMgmt.remUnderscore(nation.getName())
								) + message);
						}
					}
					default -> throw new IllegalStateException("Unexpected value: " + identifiers[0]);
				}
			}
		});
		async.subscribe(channel);
	}

	/**
	 * Send outgoing messages to Redis
	 *
	 * @param channel The channel id
	 * @param message the message to send
	 */
	@Override
	public void publish(String channel, String message) {
		RedisPubSubAsyncCommands<String, String> async = pubSub.async();
		async.publish(channel, message);
	}

	/**
	 * Close the Redis pub-sub and client instances
	 */
	@Override
	public void disconnect() {
		pubSub.closeAsync().thenRun(() -> client.close());
	}
}
