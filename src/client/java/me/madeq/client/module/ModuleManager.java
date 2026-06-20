package me.madeq.client.module;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.madeq.client.LiteClient;
import me.madeq.client.command.CommandArgument;
import me.madeq.client.command.CommandParseException;
import me.madeq.client.chat.ChatHelper;
import me.madeq.client.notify.NotificationType;
import me.madeq.client.module.impl.crash.*;
import me.madeq.client.module.impl.exploit.*;

public class ModuleManager {
	private final Map<String, Module> modules = new LinkedHashMap<>();
	private boolean defaultsRegistered;

	public void registerDefaults() {
		if (defaultsRegistered) {
			return;
		}

		registerModules(
				// Crash modules
				new BookCrash(),
                new ChargedProjectilesCrash(),

				// Exploit modules
				new BundleExploit()

				// Other modules
		);

		defaultsRegistered = true;
	}

	public void registerModules(Module... modules) {
		Arrays.stream(modules).forEach(
				module -> this.modules.put(module.getName().toLowerCase(Locale.ROOT), module)
		);
	}

	public List<Module> getModules() {
		return List.copyOf(modules.values());
	}

	public void handleDisconnect() {
		for (Module module : modules.values()) {
			module.onDisconnect();
		}
	}

	public void executeModule(String name, List<String> arguments) {
		registerDefaults();

		Module module = modules.get(name.toLowerCase(Locale.ROOT));
		if (module == null) {
			sendClientMessage("Unknown module method: " + name);
			LiteClient.getNotificationManager().show(NotificationType.ERROR, "Module", "Unknown module: " + name);
			return;
		}

		if (arguments.size() != module.getArguments().size()) {
			sendClientMessage("Usage: " + module.getUsage(LiteClient.getCommandManager().getPrefix()));
			LiteClient.getNotificationManager().show(NotificationType.WARNING, module.getName(), "Invalid arguments");
			return;
		}

		LiteClient.getNotificationManager().show(NotificationType.INFO, module.getName(), "Executing module...");

		try {
			module.executeModule(new ModuleContext(this, module, parseArguments(module, arguments), List.copyOf(arguments)));
			LiteClient.getNotificationManager().show(NotificationType.SUCCESS, module.getName(), "Module executed successfully");
		} catch (CommandParseException exception) {
			sendClientMessage(exception.getMessage());
			sendClientMessage("Usage: " + module.getUsage(LiteClient.getCommandManager().getPrefix()));
			LiteClient.getNotificationManager().show(NotificationType.WARNING, module.getName(), exception.getMessage());
		} catch (RuntimeException exception) {
			String message = exception.getMessage() == null ? "Execution failed" : exception.getMessage();
			sendClientMessage("An error occurred while executing the module method: " + message);
			LiteClient.getNotificationManager().show(NotificationType.ERROR, module.getName(), message);
		}
	}

	private Map<String, Object> parseArguments(Module module, List<String> rawArguments) throws CommandParseException {
		Map<String, Object> parsedArguments = new LinkedHashMap<>();
		List<CommandArgument> definitions = module.getArguments();

		for (int index = 0; index < definitions.size(); index++) {
			CommandArgument definition = definitions.get(index);
			Object value = definition.type().parse(rawArguments.get(index));
			validateRange(definition, value);
			validateOptions(definition, value);
			parsedArguments.put(definition.name(), value);
		}

		return parsedArguments;
	}

	private void validateRange(CommandArgument definition, Object value) throws CommandParseException {
		if (!definition.hasRange() || !(value instanceof Number number)) {
			return;
		}

		double doubleValue = number.doubleValue();
		if (doubleValue < definition.min() || doubleValue > definition.max()) {
			throw new CommandParseException("'" + definition.name() + "' must be between " + definition.min() + " and " + definition.max());
		}
	}

	private void validateOptions(CommandArgument definition, Object value) throws CommandParseException {
		if (!definition.hasOptions() || !(value instanceof String stringValue)) {
			return;
		}

		if (!definition.options().contains(stringValue)) {
			throw new CommandParseException("'" + definition.name() + "' must be one of: " + String.join(", ", definition.options()));
		}
	}

	private void sendClientMessage(String message) {
		ChatHelper.send(message);
	}
}
