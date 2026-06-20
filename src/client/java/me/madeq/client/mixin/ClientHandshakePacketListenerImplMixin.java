package me.madeq.client.mixin;

import me.madeq.client.gui.menu.ConnectionProgress;
import me.madeq.client.gui.menu.ConnectionStage;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientHandshakePacketListenerImplMixin {
	@Inject(method = "handleCustomQuery", at = @At("HEAD"))
	private void liteclient$trackHandshake(CallbackInfo callbackInfo) {
		ConnectionProgress.get().advanceTo(ConnectionStage.HANDSHAKE);
	}

	@Inject(method = "handleHello", at = @At("HEAD"))
	private void liteclient$trackAuthenticating(CallbackInfo callbackInfo) {
		ConnectionProgress.get().advanceTo(ConnectionStage.AUTHENTICATING);
	}

	@Inject(method = "setEncryption", at = @At("HEAD"))
	private void liteclient$trackEncrypting(CallbackInfo callbackInfo) {
		ConnectionProgress.get().advanceTo(ConnectionStage.ENCRYPTING);
	}

	@Inject(method = "handleLoginFinished", at = @At("HEAD"))
	private void liteclient$trackLoggingIn(CallbackInfo callbackInfo) {
		ConnectionProgress.get().advanceTo(ConnectionStage.LOGGING_IN);
	}
}
