package me.madeq.client.command.impl;

import me.madeq.client.LiteClient;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class HideNickCommand extends Command {
    private static boolean enabled = false;

    public HideNickCommand() {
        super("hidenick", "Change your displayed name to LiteClient (client-side)", ModuleType.COMMAND);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void executeCommand(CommandContext context) {
        enabled = !enabled;
        LiteClient.getConfigManager().saveCurrentServer();
        context.sendMessage("Hide nick: " + (enabled ? "<green>enabled" : "<red>disabled"));
    }
}
