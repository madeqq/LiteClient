package me.madeq.client.mixin;

import me.madeq.client.command.impl.HideNickCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @ModifyVariable(method = "addMessage*", at = @At("HEAD"), argsOnly = true)
    private Component hideNick(Component message) {
        if (!HideNickCommand.isEnabled()) {
            return message;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return message;
        }

        String realName = mc.player.getGameProfile().getName();
        return replaceInComponent(message, realName);
    }

    @Unique
    private static Component replaceInComponent(Component component, String search) {
        String serialized = Component.Serializer.toJson(component, Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : net.minecraft.core.RegistryAccess.EMPTY);
        if (!serialized.contains(search)) {
            return component;
        }

        String replaced = serialized.replace(
                "\"" + search + "\"",
                "\"" + "LiteClient" + "\""
        );
        Component result = Component.Serializer.fromJson(replaced, Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : net.minecraft.core.RegistryAccess.EMPTY);
        return result != null ? result : component;
    }
}
