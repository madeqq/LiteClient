package me.madeq.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.madeq.client.LiteClient;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandArgument;
import me.madeq.client.module.Module;

public final class GuiModuleExecutor {
	private GuiModuleExecutor() {
	}

	public static void execute(GuiModuleEntry entry) {
		if (entry.isCommand()) {
			executeCommand(entry.command());
			return;
		}

		if (entry.isModule()) {
			executeModule(entry.module());
		}
	}

	public static void executeCommand(Command command) {
		String arguments = buildArgumentLine(command.getArguments(), getConfig(getCommandConfigKey(command)));
		String commandLine = LiteClient.getCommandManager().getPrefix() + command.getName() + (arguments.isBlank() ? "" : " " + arguments);
		LiteClient.getCommandManager().handleChatMessage(commandLine);
	}

	public static void executeModule(Module module) {
		String arguments = buildArgumentLine(module.getArguments(), getConfig(getModuleConfigKey(module)));
		LiteClient.getModuleManager().executeModule(module.getName(), splitArguments(arguments));
	}

	public static String getCommandConfigKey(Command command) {
		return "command:" + command.getName();
	}

	public static String getModuleConfigKey(Module module) {
		return "module:" + module.getName();
	}

    private static String getConfig(String key) {
		return LiteClient.getClickGuiManager().getConfig(key);
	}

	private static String buildArgumentLine(List<CommandArgument> arguments, String config) {
		Map<String, String> values = deserializeConfig(config);
		List<String> parts = new ArrayList<>();

		for (CommandArgument argument : arguments) {
			String value = values.getOrDefault(argument.name(), argument.defaultValue());
			parts.add(formatArgumentValue(value));
		}

		return String.join(" ", parts).trim();
	}

	private static String formatArgumentValue(String value) {
		String trimmed = value.trim();

		if (trimmed.contains(" ")) {
			return "\"" + trimmed.replace("\"", "") + "\"";
		}

		return trimmed;
	}

	private static Map<String, String> deserializeConfig(String config) {
		Map<String, String> values = new LinkedHashMap<>();

		if (config.isBlank()) {
			return values;
		}

		for (String entry : config.split(";", -1)) {
			int separator = entry.indexOf('=');

			if (separator <= 0) {
				continue;
			}

			String key = entry.substring(0, separator);
			String value = entry.substring(separator + 1).replace("\\;", ";");
			values.put(key, value);
		}

		return values;
	}

	private static List<String> splitArguments(String input) {
		if (input.isBlank()) {
			return List.of();
		}

		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean insideQuotes = false;

		for (int index = 0; index < input.length(); index++) {
			char character = input.charAt(index);

			if (character == '"') {
				insideQuotes = !insideQuotes;
				continue;
			}

			if (Character.isWhitespace(character) && !insideQuotes) {
				addPart(parts, current);
				continue;
			}

			current.append(character);
		}

		addPart(parts, current);
		return parts;
	}

	private static void addPart(List<String> parts, StringBuilder current) {
		if (!current.isEmpty()) {
			parts.add(current.toString());
			current.setLength(0);
		}
	}
}
