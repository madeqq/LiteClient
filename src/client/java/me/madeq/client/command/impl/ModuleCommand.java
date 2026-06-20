package me.madeq.client.command.impl;

import me.madeq.client.LiteClient;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class ModuleCommand extends Command {
	public ModuleCommand() {
		super("module", "Send module packets", ModuleType.COMMAND, false, true);
		addArgument("name", ArgumentType.STRING);
	}

	@Override
	public boolean acceptsAdditionalArguments() {
		return true;
	}

	@Override
	public void executeCommand(CommandContext context) {
		LiteClient.getModuleManager().executeModule(context.getString("name"), context.getAdditionalArguments());
	}
}
