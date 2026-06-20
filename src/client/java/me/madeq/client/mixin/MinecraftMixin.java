package me.madeq.client.mixin;

import me.madeq.client.LiteClient;
import me.madeq.client.gui.menu.MenuRedirectState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "updateTitle", at = @At("HEAD"), cancellable = true)
    private void setCustomTitle(CallbackInfo callbackInfo) {
        Minecraft client = Minecraft.getInstance();

        client.getWindow().setTitle("LiteClient 1.21.4 (Version: " + LiteClient.VERSION + ") DOWNLOAD: github.com/madeqq/LiteClient");

        callbackInfo.cancel();
    }

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen replaceVanillaMenuScreens(Screen screen) {
        return MenuRedirectState.replaceVanillaMenuScreen(screen);
    }
}
