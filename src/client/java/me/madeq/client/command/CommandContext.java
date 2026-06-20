package me.madeq.client.command;

import java.util.List;
import java.util.Map;
import me.madeq.client.chat.ChatHelper;

public class CommandContext {
	private final CommandManager commandManager;
	private final Command command;
	private final Map<String, Object> arguments;
	private final List<String> rawArguments;

	public CommandContext(CommandManager commandManager, Command command, Map<String, Object> arguments, List<String> rawArguments) {
		this.commandManager = commandManager;
		this.command = command;
		this.arguments = arguments;
		this.rawArguments = rawArguments;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public Command getCommand() {
		return command;
	}

	public List<String> getRawArguments() {
		return rawArguments;
	}

	public List<String> getAdditionalArguments() {
		int fixedArguments = command.getArguments().size();

		if (rawArguments.size() <= fixedArguments) {
			return List.of();
		}

		return rawArguments.subList(fixedArguments, rawArguments.size());
	}

	public int getInt(String name) {
		return getArgument(name, Integer.class);
	}

	public String getString(String name) {
		return getArgument(name, String.class);
	}

	public String getList(String name) {
		return getArgument(name, String.class);
	}

	public boolean getBoolean(String name) {
		return getArgument(name, Boolean.class);
	}

	public double getDouble(String name) {
		return getArgument(name, Double.class);
	}

	public void sendMessage(String message) {
		ChatHelper.send(message);
	}

	private <T> T getArgument(String name, Class<T> type) {
		Object argument = arguments.get(name);

		if (argument == null) {
			throw new IllegalArgumentException("Argument '" + name + "' does not exist");
		}

		return type.cast(argument);
	}
}
