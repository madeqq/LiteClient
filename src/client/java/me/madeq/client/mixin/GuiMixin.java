package me.madeq.client.mixin;

import me.madeq.client.LiteClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void freeClient$renderHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		LiteClient.getHudManager().render(graphics);
		LiteClient.getNotificationManager().render(graphics);
	}
}
