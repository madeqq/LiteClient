package me.madeq.client.module;

import java.util.Locale;
import java.util.Objects;

public final class ModuleType {
	public static final ModuleType COMMAND = new ModuleType("command", "Commands");
	public static final ModuleType CRASHER = new ModuleType("crasher", "Crashers");
	public static final ModuleType EXPLOIT = new ModuleType("exploit", "Exploits");
	public static final ModuleType STYLE = new ModuleType("style", "Style");

	private final String id;
	private final String displayName;

	private ModuleType(String id, String displayName) {
		this.id = normalizeId(id);
		this.displayName = displayName;
	}

	public static ModuleType of(String name) {
		return new ModuleType(name, toDisplayName(name));
	}

	public static ModuleType of(String id, String displayName) {
		return new ModuleType(id, displayName);
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ModuleType moduleType)) {
			return false;
		}

		return id.equals(moduleType.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	private static String normalizeId(String value) {
		return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
	}

	private static String toDisplayName(String value) {
		String normalized = value.trim().replace('_', ' ').replace('-', ' ');

		if (normalized.isBlank()) {
			return "Modules";
		}

		String[] parts = normalized.split("\\s+");
		StringBuilder builder = new StringBuilder();

		for (String part : parts) {
			if (!builder.isEmpty()) {
				builder.append(' ');
			}

			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				builder.append(part.substring(1).toLowerCase(Locale.ROOT));
			}
		}

		return builder.toString();
	}
}
