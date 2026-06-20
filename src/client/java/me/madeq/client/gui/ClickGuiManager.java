package me.madeq.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;

import me.madeq.client.LiteClient;
import me.madeq.client.gui.impl.ClickGuiScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class ClickGuiManager {
	private static final int GLFW_KEY_RIGHT_SHIFT = 344;
	private final Map<String, String> configs = new HashMap<>();
	private KeyMapping openKey;

	public void initialize() {
		openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.freeclient.click_gui",
				InputConstants.Type.KEYSYM,
				GLFW_KEY_RIGHT_SHIFT,
				"category.freeclient"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.consumeClick()) {
				toggle(client);
			}
		});
	}

	private void toggle(Minecraft client) {
		if (client.screen instanceof ClickGuiScreen) {
			client.setScreen(null);
			return;
		}

		client.setScreen(new ClickGuiScreen());
	}

	public String getConfig(String key) {
		return configs.getOrDefault(key, "");
	}

	public void saveConfig(String key, String value) {
		configs.put(key, value);
		LiteClient.getConfigManager().saveCurrentServer();
	}

	public void clearConfig(String key) {
		configs.remove(key);
		LiteClient.getConfigManager().saveCurrentServer();
	}

	public Map<String, String> getConfigs() {
		return new HashMap<>(configs);
	}

	public void setConfigs(Map<String, String> configs) {
		this.configs.clear();

		if (configs != null) {
			this.configs.putAll(configs);
		}
	}
}
