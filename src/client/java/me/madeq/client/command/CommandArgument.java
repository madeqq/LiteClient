package me.madeq.client.command;

import java.util.List;

public record CommandArgument(String name, ArgumentType type, Double min, Double max, String defaultValue, List<String> options) {
	public CommandArgument {
		options = options == null ? List.of() : List.copyOf(options);

		if (type == ArgumentType.LIST && options.isEmpty()) {
			throw new IllegalArgumentException("List arguments must define at least one option");
		}
	}

	public CommandArgument(String name, ArgumentType type) {
		this(name, type, null, null, "", List.of());
	}

	public CommandArgument(String name, ArgumentType type, String defaultValue) {
		this(name, type, null, null, defaultValue, List.of());
	}

	public CommandArgument(String name, ArgumentType type, Double min, Double max) {
		this(name, type, min, max, "", List.of());
	}

	public CommandArgument(String name, ArgumentType type, Double min, Double max, String defaultValue) {
		this(name, type, min, max, defaultValue, List.of());
	}

	public CommandArgument(String name, List<String> options) {
		this(name, ArgumentType.LIST, null, null, getFirstOption(options), options);
	}

	public CommandArgument(String name, List<String> options, String defaultValue) {
		this(name, ArgumentType.LIST, null, null, defaultValue, options);
	}

	public boolean hasRange() {
		return min != null && max != null;
	}

	public boolean hasOptions() {
		return !options.isEmpty();
	}

	private static String getFirstOption(List<String> options) {
		if (options == null || options.isEmpty()) {
			throw new IllegalArgumentException("List arguments must define at least one option.");
		}

		return options.getFirst();
	}
}
