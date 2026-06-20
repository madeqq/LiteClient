package me.madeq.client.mixin;

import me.madeq.client.LiteClient;
import me.madeq.client.gui.menu.MainMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private final ResourceLocation CUSTOM_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("liteclient", "textures/gui/background.png");

    @Inject(method = "init", at = @At("TAIL"))
    private void openLiteMenu(CallbackInfo callbackInfo) {
        Minecraft.getInstance().setScreen(new MainMenuScreen());
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;renderPanorama(Lnet/minecraft/client/gui/GuiGraphics;F)V",
                    shift = At.Shift.AFTER))
    private void renderBg(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen)(Object)this;
        graphics.blit(
                RenderType::guiTextured,
                CUSTOM_BACKGROUND,
                0, 0,
                0f, 0f,
                screen.width,
                screen.height,
                screen.width,
                screen.height
        );
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/SplashRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/client/gui/Font;I)V"))
    private void removeSplash(SplashRenderer instance, GuiGraphics graphics, int x, Font font, int alpha) {
    }

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"),
            index = 1)
    private String replaceVersionText(String text) {
        return "LiteClient 1.21.4 (Version: " + LiteClient.VERSION + ")";
    }
}
