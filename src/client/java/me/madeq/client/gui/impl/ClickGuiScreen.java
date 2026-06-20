package me.madeq.client.gui.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.madeq.client.LiteClient;
import me.madeq.client.command.Command;
import me.madeq.client.module.Module;
import me.madeq.client.gui.DropdownPanel;
import me.madeq.client.gui.GuiModuleEntry;
import me.madeq.client.gui.GuiModuleExecutor;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private final List<DropdownPanel> panels = new ArrayList<>();

	public ClickGuiScreen() {
		super(Component.literal("FreeClient ClickGUI"));

		buildPanels();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderTransparentBackground(graphics);

		for (DropdownPanel panel : panels) {
			panel.render(graphics, font, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 1) {
			for (DropdownPanel panel : panels) {
				GuiModuleEntry entry = panel.getEntryAt(mouseX, mouseY);
				if (entry != null) {
					openDetails(entry);
					return true;
				}
			}
		}

		if (button == 0) {
			for (DropdownPanel panel : panels) {
				GuiModuleEntry entry = panel.getEntryAt(mouseX, mouseY);
				if (entry != null) {
					GuiModuleExecutor.execute(entry);
					return true;
				}
			}

			for (DropdownPanel panel : panels) {
				if (panel.mouseClicked(mouseX, mouseY)) {
					return true;
				}
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0) {
			for (DropdownPanel panel : panels) {
				if (panel.mouseDragged(mouseX, mouseY)) {
					return true;
				}
			}
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (DropdownPanel panel : panels) {
				panel.mouseReleased();
			}
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void buildPanels() {
		Map<ModuleType, List<GuiModuleEntry>> groupedEntries = new LinkedHashMap<>();

		for (Command command : LiteClient.getCommandManager().getCommands()) {
			if (command.isVisibleInGui()) {
				groupedEntries.computeIfAbsent(command.getModuleType(), ignored -> new ArrayList<>()).add(GuiModuleEntry.command(command));
			}
		}

		for (Module module : LiteClient.getModuleManager().getModules()) {
			if (module.isVisibleInGui()) {
				groupedEntries.computeIfAbsent(module.getModuleType(), ignored -> new ArrayList<>()).add(GuiModuleEntry.module(module));
			}
		}

		int x = 16;
		for (Map.Entry<ModuleType, List<GuiModuleEntry>> entry : groupedEntries.entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}

			panels.add(new DropdownPanel(entry.getKey().getDisplayName(), x, 24, entry.getValue()));
			x += 126;
		}
	}

	private void openDetails(GuiModuleEntry entry) {
		if (entry.isCommand()) {
			minecraft.setScreen(ModuleDetailsScreen.forCommand(entry.command()));
			return;
		}

		if (entry.isModule()) {
			minecraft.setScreen(ModuleDetailsScreen.forModules(entry.module()));
		}
	}
}
