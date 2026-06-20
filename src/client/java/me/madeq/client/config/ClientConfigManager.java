package me.madeq.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import me.madeq.client.LiteClient;
import me.madeq.client.command.impl.HideNickCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class ClientConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DEFAULT_SERVER_KEY = "global";
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("freeclient").resolve("server-configs.json");
	private ConfigFile configFile = new ConfigFile();
	private String activeServerKey = DEFAULT_SERVER_KEY;

	public void initialize() {
		loadFile();
		loadCurrentServer();
	}

	public void loadCurrentServer() {
		activeServerKey = getCurrentServerKey();
		ServerConfig serverConfig = configFile.servers.computeIfAbsent(activeServerKey, ignored -> new ServerConfig());
		LiteClient.getCommandManager().applyPrefix(serverConfig.prefix);
		LiteClient.getClickGuiManager().setConfigs(serverConfig.moduleConfigs);
        HideNickCommand.setEnabled(serverConfig.hideNick);
    }

	public void saveCurrentServer() {
		ServerConfig serverConfig = configFile.servers.computeIfAbsent(activeServerKey, ignored -> new ServerConfig());
		serverConfig.prefix = LiteClient.getCommandManager().getPrefix();
		serverConfig.moduleConfigs = LiteClient.getClickGuiManager().getConfigs();
        serverConfig.hideNick = HideNickCommand.isEnabled();
        saveFile();
	}

	private void loadFile() {
		if (!Files.exists(configPath)) {
			configFile = new ConfigFile();
			return;
		}

		try (Reader reader = Files.newBufferedReader(configPath)) {
			ConfigFile loadedConfig = GSON.fromJson(reader, ConfigFile.class);
			configFile = loadedConfig == null ? new ConfigFile() : loadedConfig;

			if (configFile.servers == null) {
				configFile.servers = new LinkedHashMap<>();
			}
		} catch (IOException | JsonSyntaxException exception) {
			configFile = new ConfigFile();
		}
	}

	private void saveFile() {
		try {
			Files.createDirectories(configPath.getParent());

			try (Writer writer = Files.newBufferedWriter(configPath)) {
				GSON.toJson(configFile, writer);
			}
		} catch (IOException ignored) {
		}
	}

	private String getCurrentServerKey() {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.hasSingleplayerServer()) {
			return "singleplayer";
		}

		ServerData serverData = minecraft.getCurrentServer();
		if (serverData != null && !serverData.ip.isBlank()) {
			return serverData.ip.toLowerCase();
		}

		return DEFAULT_SERVER_KEY;
	}

	private static class ConfigFile {
		private Map<String, ServerConfig> servers = new LinkedHashMap<>();
	}

	private static class ServerConfig {
		private String prefix = "!";
		private Map<String, String> moduleConfigs = new LinkedHashMap<>();
        private boolean hideNick = false;
    }
}
