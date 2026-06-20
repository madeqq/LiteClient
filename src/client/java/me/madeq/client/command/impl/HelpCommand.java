package me.madeq.client.command.impl;

import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Commands list", ModuleType.COMMAND, true, false);
    }

    @Override
    public void executeCommand(CommandContext context) {
        String prefix = context.getCommandManager().getPrefix();

        context.sendMessage("<aqua>Commands list: ");
        for (Command command : context.getCommandManager().getCommands()) {
            if (!command.isVisibleInHelp()) continue;
            context.sendMessage("<aqua>" + command.getUsage(prefix) + " <dark_gray>- <white>" + command.getDescription());
        }
    }
}
