package me.madeq.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Unique
    private final ResourceLocation CUSTOM_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("liteclient", "textures/gui/background.png");

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderBg(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return;
        Screen self = (Screen) (Object) this;
        graphics.blit(
                RenderType::guiTextured,
                CUSTOM_BACKGROUND,
                0, 0,
                0f, 0f,
                self.width,
                self.height,
                self.width,
                self.height
        );
        ci.cancel();
    }
}
