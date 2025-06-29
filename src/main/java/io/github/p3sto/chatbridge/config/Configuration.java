package io.github.p3sto.chatbridge.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.Settings;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.github.p3sto.chatbridge.TownyChatBridge;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public abstract class Configuration {
	private final Logger log = Logger.getGlobal();
	protected YamlDocument config;
	private final String fileName;

	public Configuration(TownyChatBridge plugin, String fileName) {
		this.fileName = fileName;
		initConfig(plugin, fileName);
	}

	private final Settings[] defaultSettings = new Settings[]{
			GeneralSettings.builder().setUseDefaults(false).build(),
			LoaderSettings.builder().setAutoUpdate(true).build(),
			DumperSettings.DEFAULT,
			UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()
	};

	/**
	 * Attempts to load the configuration file from the plugin's data folder.
	 * This will fall back to creating the config file from the plugin's resources.
	 *
	 * @param plugin   This plugin instance
	 * @param fileName The name of the configuration file (should match the resource file name)
	 */
	private void initConfig(Plugin plugin, String fileName) {
		try (InputStream is = plugin.getResource(fileName)) {
			if (is == null) {
				throw new IOException("Missing configuration file from plugin resources");
			}

			File file = new File(plugin.getDataFolder(), fileName);
			config = YamlDocument.create(file, is, defaultSettings);
		} catch (IOException e) {
			log.severe(e.getMessage());
		}
	}

	public boolean isLoaded() {
		return config != null;
	}


	public void reload(Plugin plugin) {
		try {
			if (config.reload()) {
				initConfig(plugin, fileName);
			}
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
	}

	public boolean getBoolean(ConfigurationNode node) {
		return config.getBoolean(node.path(), false);
	}

	public int getInt(ConfigurationNode node) {
		return config.getInt(node.path(), 0);
	}

	public double getDouble(ConfigurationNode node) {
		return config.getDouble(node.path(), 0.0);
	}

	public float getFloat(ConfigurationNode node) {
		return config.getFloat(node.path(), 0.0f);
	}

	public String getString(ConfigurationNode node) {
		return config.getString(node.path(), "");
	}

	public boolean has(ConfigurationNode node) {
		return config.contains(node.path());
	}

	public YamlDocument getConfig() {
		return config;
	}
}