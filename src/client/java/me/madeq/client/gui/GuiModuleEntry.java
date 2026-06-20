package me.madeq.client.gui;

import me.madeq.client.command.Command;
import me.madeq.client.module.Module;
import me.madeq.client.module.ModuleType;

public record GuiModuleEntry(String name, ModuleType moduleType, Command command, Module module) {
	public static GuiModuleEntry command(Command command) {
		return new GuiModuleEntry(command.getName(), command.getModuleType(), command, null);
	}

	public static GuiModuleEntry module(Module module) {
		return new GuiModuleEntry(module.getName(), module.getModuleType(), null, module);
	}

	public boolean isCommand() {
		return command != null;
	}

	public boolean isModule() {
		return module != null;
	}
}
