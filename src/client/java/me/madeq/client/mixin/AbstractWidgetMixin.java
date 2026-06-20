package me.madeq.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void customRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractWidget widget = (AbstractWidget)(Object)this;

        if (!(widget instanceof Button)) return;

        boolean hovered = widget.isHovered();

        int bg = hovered ? 0xAAFFFFFF : 0x88000000;

        graphics.fill(
                widget.getX(),
                widget.getY(),
                widget.getX() + widget.getWidth(),
                widget.getY() + widget.getHeight(),
                bg
        );

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                widget.getMessage(),
                widget.getX() + widget.getWidth() / 2,
                widget.getY() + (widget.getHeight() - 8) / 2,
                0xFFFFFF
        );

        ci.cancel();
    }
}
