package me.madeq.client.command.impl;

import com.viaversion.viafabricplus.ViaFabricPlus;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

import java.util.List;

public class TestCommand extends Command {

	public TestCommand() {
		super("test", "Example command with typed arguments", ModuleType.COMMAND);
		addArgument("number", ArgumentType.INT);
		addArgument("text", ArgumentType.STRING);
		addArgument("list", List.of("test1", "test2", "test3"));
	}

	@Override
	public void executeCommand(CommandContext context) {
		int number = context.getInt("number");
		String text = context.getString("text");
		String selected = context.getList("list");

        context.sendMessage(selected);
        context.sendMessage(String.valueOf(ViaFabricPlus.getImpl().getTargetVersion().getVersion()));
		context.sendMessage("<gradient:#4facfe:#00f2fe>TestCommand</gradient> <gray>number:</gray> <aqua>" + number + "</aqua> <gray>text:</gray> <white>" + text + "</white>");
	}
}
