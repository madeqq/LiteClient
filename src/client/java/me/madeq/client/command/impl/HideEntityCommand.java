package me.madeq.client.command.impl;

import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class HideEntityCommand extends Command {
    private static boolean enabled = false;

    public HideEntityCommand() {
        super("hideentity", "Hide entity after hit (client-side)", ModuleType.COMMAND);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public void executeCommand(CommandContext context) {
        enabled = !enabled;
        context.sendMessage("Hide entity mode: " + (enabled ? "<green>enabled" : "<red>disabled"));
    }
}
