package me.madeq.client.mixin;

import me.madeq.client.chat.ChatHelper;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void freeClient$injectTabListHeaderFooter(GuiGraphics graphics, int screenWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        PlayerTabOverlay self = (PlayerTabOverlay) (Object) this;

        self.setHeader(ChatHelper.format("\n \n"
                + "  <gradient:#4facfe:#00f2fe>LiteClient <dark_gray>| <white>The Best Free Client  \n"
                + "  <white>Powered by: <aqua>Xynis Team  "
                + "\n \n"
        ));

        self.setFooter(ChatHelper.format("\n \n"
                + "  <gradient:#4facfe:#00f2fe>⬇ DOWNLOAD LITECLIENT ⬇  \n"
                + "  <gray>github.com/madeqq/LiteClient  "
                + "\n \n"
        ));
    }
}
