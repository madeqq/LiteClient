package me.madeq.client.gui.menu;

import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

public final class MenuRedirectState {
	private MenuRedirectState() {
	}

	public static Screen replaceVanillaMenuScreen(Screen screen) {
		if (screen instanceof DisconnectedScreen disconnected) {
			return LiteDisconnectedScreen.fromVanilla(disconnected);
		}

		if (screen instanceof JoinMultiplayerScreen) {
			return new MultiplayerScreen(new MainMenuScreen(), true);
		}

		return screen;
	}
}
