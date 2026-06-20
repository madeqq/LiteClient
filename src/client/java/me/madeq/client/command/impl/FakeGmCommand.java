package me.madeq.client.command.impl;

import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public class FakeGmCommand extends Command {

    public FakeGmCommand() {
        super("fakegm", "Change gamemode (client-side)", ModuleType.COMMAND);
        addArgument("mode", ArgumentType.STRING);
    }

    @Override
    public void executeCommand(CommandContext context) {
        String mode = context.getString("mode");
        GameType type = switch (mode) {
            case "creative", "1" -> GameType.CREATIVE;
            case "adventure", "2" -> GameType.ADVENTURE;
            case "spectator", "3" -> GameType.SPECTATOR;
            default -> GameType.SURVIVAL;
        };
        Minecraft.getInstance().gameMode.setLocalMode(type);
        context.sendMessage("<white>Your gamemode was changed to: <aqua>" + type.getName());
    }
}

