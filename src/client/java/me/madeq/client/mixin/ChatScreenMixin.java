package me.madeq.client.mixin;

import me.madeq.client.command.suggestion.CommandSuggestionManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    @Unique
    private final CommandSuggestionManager liteclient$suggestionManager = new CommandSuggestionManager();

    @Inject(method = "render", at = @At("HEAD"))
    private void liteclient$updateSuggestions(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        liteclient$suggestionManager.updateIfChanged(input.getValue());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void liteclient$renderSuggestions(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ChatScreen self = (ChatScreen)(Object)this;
        liteclient$suggestionManager.render(graphics, input, self.width, self.height);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void liteclient$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (liteclient$suggestionManager.handleKeyPress(keyCode, input)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void liteclient$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        ChatScreen self = (ChatScreen)(Object)this;
        if (liteclient$suggestionManager.handleMouseClick(mouseX, mouseY, input, self.width, self.height)) {
            cir.setReturnValue(true);
        }
    }
}
