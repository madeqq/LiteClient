package me.madeq.client.mixin;

import me.madeq.client.gui.menu.ConnectionProgress;
import me.madeq.client.gui.menu.ConnectionStage;
import me.madeq.client.gui.menu.ConnectionStageRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {

	@Inject(method = "startConnecting", at = @At("HEAD"))
	private static void liteclient$beginConnectionProgress(
			net.minecraft.client.gui.screens.Screen parent,
			net.minecraft.client.Minecraft minecraft,
			ServerAddress address,
			ServerData serverData,
			boolean isQuickPlay,
			net.minecraft.client.multiplayer.TransferState transferState,
			CallbackInfo callbackInfo
	) {
		ConnectionProgress.get().begin(
				serverData.name,
				address.getHost() + ":" + address.getPort()
		);
	}

	@Inject(method = "updateStatus", at = @At("HEAD"))
	private void liteclient$trackStatusUpdate(Component status, CallbackInfo callbackInfo) {
		ConnectionStage stage = mapStatus(status);
		if (stage != null) {
			ConnectionProgress.get().advanceTo(stage);
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void liteclient$renderConnectionStages(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
		ConnectScreen screen = (ConnectScreen) (Object) this;
		ConnectionStageRenderer.render(graphics, screen.getFont(), screen.width, screen.height, ConnectionProgress.get());

		for (GuiEventListener child : screen.children()) {
			if (child instanceof Renderable renderable) {
				renderable.render(graphics, mouseX, mouseY, partialTick);
			}
		}

		callbackInfo.cancel();
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void liteclient$repositionCancelButton(CallbackInfo callbackInfo) {
		ConnectScreen screen = (ConnectScreen) (Object) this;
		int panelWidth = Math.max(360, Math.min(440, screen.width - 80));
		int panelHeight = 96 + ConnectionStage.values().length * 34 + 36;
		int panelX = (screen.width - panelWidth) / 2;
		int panelY = (screen.height - panelHeight) / 2;
		int buttonY = panelY + panelHeight - 34;

		screen.children().stream()
				.filter(net.minecraft.client.gui.components.Button.class::isInstance)
				.map(net.minecraft.client.gui.components.Button.class::cast)
				.forEach(button -> button.setPosition(panelX + (panelWidth - 200) / 2, buttonY));
	}

	private static ConnectionStage mapStatus(Component status) {
		if (matches(status, "connect.negotiating")) {
			return ConnectionStage.HANDSHAKE;
		}

		if (matches(status, "connect.connecting") || matches(status, "connect.transferring")) {
			return ConnectionStage.CONNECTING;
		}

		return null;
	}

	private static boolean matches(Component status, String translationKey) {
		return status.getContents() instanceof TranslatableContents contents && translationKey.equals(contents.getKey());
	}
}
