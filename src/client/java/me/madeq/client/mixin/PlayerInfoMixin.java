package me.madeq.client.mixin;

import com.mojang.authlib.GameProfile;
import me.madeq.client.command.impl.HideNickCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Final
    @Shadow
    private GameProfile profile;

    @Inject(method = "getProfile", at = @At("RETURN"), cancellable = true)
    private void hideNick(CallbackInfoReturnable<GameProfile> cir) {
        if (!HideNickCommand.isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !profile.getId().equals(mc.player.getUUID())) {
            return;
        }

        cir.setReturnValue(new GameProfile(profile.getId(), "LiteClient"));
    }
}
