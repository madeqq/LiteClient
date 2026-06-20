package me.madeq.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.command.CommandArgument;

public abstract class Module {
	private final String name;
	private final String description;
	private final ModuleType moduleType;
	private final boolean visibleInGui;
	private final List<CommandArgument> arguments = new ArrayList<>();
    private boolean enabled;

	protected Module(String name, String description) {
		this(name, description, ModuleType.CRASHER);
	}

	protected Module(String name, String description, ModuleType moduleType) {
		this(name, description, moduleType, true);
	}

	protected Module(String name, String description, ModuleType moduleType, boolean visibleInGui) {
		this.name = name;
		this.description = description;
		this.moduleType = moduleType;
		this.visibleInGui = visibleInGui;
        this.enabled = false;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final ModuleType getModuleType() {
		return moduleType;
	}

	public final boolean isVisibleInGui() {
		return visibleInGui;
	}

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public final List<CommandArgument> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	public final String getUsage(String prefix) {
		StringBuilder usage = new StringBuilder(prefix).append("module ").append(name);

		for (CommandArgument argument : arguments) {
			usage.append(" <").append(argument.name()).append(":").append(argument.type().getDisplayName()).append(">");
		}

		return usage.toString();
	}

	protected final void addArgument(String name, ArgumentType type) {
		arguments.add(new CommandArgument(name, type));
	}

	protected final void addArgument(String name, ArgumentType type, String defaultValue) {
		arguments.add(new CommandArgument(name, type, defaultValue));
	}

	protected final void addArgument(String name, ArgumentType type, double min, double max) {
		arguments.add(new CommandArgument(name, type, min, max));
	}

	protected final void addArgument(String name, ArgumentType type, double min, double max, String defaultValue) {
		arguments.add(new CommandArgument(name, type, min, max, defaultValue));
	}

	protected final void addArgument(String name, List<String> options) {
		arguments.add(new CommandArgument(name, options));
	}

	protected final void addArgument(String name, List<String> options, String defaultValue) {
		arguments.add(new CommandArgument(name, options, defaultValue));
	}

	public abstract void executeModule(ModuleContext context);

	public void onDisconnect() {
		setEnabled(false);
	}
}
