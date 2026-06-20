package me.madeq.client.mixin;

import me.madeq.client.chat.ChatHelper;
import me.madeq.client.command.impl.HideEntityCommand;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiplayerGamemodeMixin {

    @Inject(method = "attack", at = @At("TAIL"))
    private void hideEntityOnHit(Player player, Entity target, CallbackInfo ci) {
        if (HideEntityCommand.isEnabled()) {
            target.remove(Entity.RemovalReason.DISCARDED);
            ChatHelper.send("Entity has been <aqua>hidden");
        }
    }
}
