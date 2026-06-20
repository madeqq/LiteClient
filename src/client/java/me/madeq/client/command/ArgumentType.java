package me.madeq.client.command;

public enum ArgumentType {
	INT("int"),
	STRING("string"),
	BOOLEAN("boolean"),
	DOUBLE("double"),
	LIST("list");

	private final String displayName;

	ArgumentType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Object parse(String rawValue) throws CommandParseException {
		return switch (this) {
			case INT -> parseInt(rawValue);
			case STRING, LIST -> rawValue;
			case BOOLEAN -> parseBoolean(rawValue);
			case DOUBLE -> parseDouble(rawValue);
        };
	}

	private int parseInt(String rawValue) throws CommandParseException {
		try {
			return Integer.parseInt(rawValue);
		} catch (NumberFormatException exception) {
			throw new CommandParseException("'" + rawValue + "' is not an integer");
		}
	}

	private boolean parseBoolean(String rawValue) throws CommandParseException {
		if (rawValue.equalsIgnoreCase("true") || rawValue.equalsIgnoreCase("yes")) {
			return true;
		}

		if (rawValue.equalsIgnoreCase("false") || rawValue.equalsIgnoreCase("no")) {
			return false;
		}

		throw new CommandParseException("'" + rawValue + "' is not a boolean");
	}

	private double parseDouble(String rawValue) throws CommandParseException {
		try {
			return Double.parseDouble(rawValue);
		} catch (NumberFormatException exception) {
			throw new CommandParseException("'" + rawValue + "' is not a number");
		}
	}
}
