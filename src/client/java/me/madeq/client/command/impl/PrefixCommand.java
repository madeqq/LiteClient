package me.madeq.client.command.impl;

import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class PrefixCommand extends Command {
	public PrefixCommand() {
		super("prefix", "Changes the client command prefix", ModuleType.COMMAND);
		addArgument("prefix", ArgumentType.STRING);
	}

	@Override
	public void executeCommand(CommandContext context) {
		String prefix = context.getString("prefix");

		context.getCommandManager().setPrefix(prefix);
		context.sendMessage("Client prefix changed to <aqua>" + prefix + "</aqua>");
	}
}
