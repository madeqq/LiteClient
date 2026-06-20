package me.madeq.client.mixin;

import me.madeq.client.LiteClient;
import me.madeq.client.command.impl.PlayerListCommand;
import me.madeq.client.command.impl.PluginListCommand;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
	private void freeClient$handleClientCommand(String message, CallbackInfo callbackInfo) {
		if (LiteClient.getCommandManager().handleChatMessage(message)) {
			callbackInfo.cancel();
		}
	}
    @Inject(method = "handleCommandSuggestions", at = @At("HEAD"))
    private void freeClient$handleCommandSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
        PluginListCommand.handleSuggestions(packet);
        PlayerListCommand.handleSuggestions(packet);
    }
}
