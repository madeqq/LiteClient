package me.madeq.client.mixin;

import me.madeq.client.gui.menu.ConnectionProgress;
import me.madeq.client.gui.menu.ConnectionStage;
import me.madeq.client.gui.menu.ConnectionStageRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReceivingLevelScreen.class)
public class ReceivingLevelScreenMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void liteclient$trackJoiningWorld(CallbackInfo callbackInfo) {
		ConnectionProgress.get().advanceTo(ConnectionStage.JOINING_WORLD);
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void liteclient$renderConnectionStages(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
		ReceivingLevelScreen screen = (ReceivingLevelScreen) (Object) this;
		ConnectionStageRenderer.render(graphics, screen.getFont(), screen.width, screen.height, ConnectionProgress.get());
		callbackInfo.cancel();
	}

	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void liteclient$skipVanillaBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
		callbackInfo.cancel();
	}
}
