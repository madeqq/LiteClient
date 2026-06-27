package me.madeq.client.gui.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.madeq.client.LiteClient;
import me.madeq.client.command.Command;
import me.madeq.client.module.Module;
import me.madeq.client.gui.DropdownPanel;
import me.madeq.client.gui.GuiModuleEntry;
import me.madeq.client.gui.GuiModuleExecutor;
import me.madeq.client.gui.SearchBar;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
	private static final int SEARCH_BAR_HEIGHT  = 20;
	private static final int SEARCH_BAR_PADDING = 6;
	private static final int SEARCH_BAR_WIDTH   = 160;

	private final List<DropdownPanel> panels = new ArrayList<>();
	private final List<GuiModuleEntry> searchResults = new ArrayList<>();
	private SearchBar searchBar;

	public ClickGuiScreen() {
		super(Component.literal("FreeClient ClickGUI"));
		buildPanels();
	}

	@Override
	protected void init() {
		super.init();
		int barX = width - SEARCH_BAR_WIDTH - SEARCH_BAR_PADDING;
		int barY = SEARCH_BAR_PADDING;
		searchBar = new SearchBar(barX, barY, SEARCH_BAR_WIDTH, SEARCH_BAR_HEIGHT);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderTransparentBackground(graphics);

		String query = searchBar.getValue();

		if (query.isEmpty()) {
			for (DropdownPanel panel : panels) {
				panel.render(graphics, font, mouseX, mouseY);
			}
		} else {
			rebuildSearchResults(query);
			renderSearchResults(graphics, mouseX, mouseY);
		}

		searchBar.render(graphics, font, mouseX, mouseY);
	}

	private void rebuildSearchResults(String query) {
		searchResults.clear();
		String lower = query.toLowerCase(Locale.ROOT);

		for (DropdownPanel panel : panels) {
			for (GuiModuleEntry entry : panel.getAllEntries()) {
				if (entry.name().toLowerCase(Locale.ROOT).contains(lower)) {
					searchResults.add(entry);
				}
			}
		}
	}

	private void renderSearchResults(GuiGraphics graphics, int mouseX, int mouseY) {
		if (searchResults.isEmpty()) {
			String msg = "No results";
			int cx = width / 2 - font.width(msg) / 2;
			graphics.drawString(font, msg, cx, height / 2, new Color(130, 140, 160).getRGB(), true);
			return;
		}

		int panelX = 16;
		int panelY = 24;
		int panelWidth = 160;
		int headerHeight = 18;
		int entryHeight = 16;
		int totalHeight = headerHeight + searchResults.size() * entryHeight;

		graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + totalHeight + 1,
				new Color(5, 6, 8, 185).getRGB());
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + headerHeight,
				new Color(25, 92, 210).getRGB());
		graphics.fill(panelX, panelY + headerHeight - 1, panelX + panelWidth, panelY + headerHeight,
				new Color(102, 169, 255).getRGB());
		graphics.drawString(font, "Results (" + searchResults.size() + ")",
				panelX + 6, panelY + 5, new Color(250, 252, 255).getRGB(), true);

		graphics.fill(panelX, panelY + headerHeight, panelX + panelWidth, panelY + totalHeight,
				new Color(16, 18, 23, 218).getRGB());

		for (int i = 0; i < searchResults.size(); i++) {
			GuiModuleEntry entry = searchResults.get(i);
			int entryY = panelY + headerHeight + i * entryHeight;
			boolean hovered = mouseX >= panelX && mouseX <= panelX + panelWidth
					&& mouseY >= entryY && mouseY <= entryY + entryHeight;

			graphics.fill(panelX, entryY, panelX + panelWidth, entryY + entryHeight,
					hovered ? new Color(30, 36, 48, 235).getRGB()
							: new Color(20, 23, 30, 210).getRGB());

			String badge = entry.moduleType().getDisplayName();
			int badgeX = panelX + panelWidth - font.width(badge) - 6;
			graphics.drawString(font, badge, badgeX, entryY + 4,
					new Color(74, 130, 200).getRGB(), true);

			String label = truncate(entry.name(), font, badgeX - (panelX + 8) - 4);
			graphics.drawString(font, label, panelX + 8, entryY + 4,
					new Color(220, 226, 235).getRGB(), true);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (searchBar.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		String query = searchBar.getValue();

		if (!query.isEmpty()) {
			if (button == 0 || button == 1) {
				GuiModuleEntry entry = getSearchResultAt(mouseX, mouseY);
				if (entry != null) {
					if (button == 1) {
						openDetails(entry);
					} else {
						GuiModuleExecutor.execute(entry);
					}
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

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
		if (button == 0 && searchBar.getValue().isEmpty()) {
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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && !searchBar.getValue().isEmpty()) {
			searchBar.clear();
			return true;
		}

		if (searchBar.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (searchBar.charTyped(codePoint)) {
			return true;
		}

		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private GuiModuleEntry getSearchResultAt(double mouseX, double mouseY) {
		int panelX = 16;
		int panelY = 24;
		int panelWidth = 160;
		int headerHeight = 18;
		int entryHeight = 16;

		for (int i = 0; i < searchResults.size(); i++) {
			int entryY = panelY + headerHeight + i * entryHeight;
			if (mouseX >= panelX && mouseX <= panelX + panelWidth
					&& mouseY >= entryY && mouseY <= entryY + entryHeight) {
				return searchResults.get(i);
			}
		}

		return null;
	}

	private String truncate(String text, net.minecraft.client.gui.Font f, int maxPixels) {
		if (f.width(text) <= maxPixels) {
			return text;
		}

		String ellipsis = "…";
		int ellipsisWidth = f.width(ellipsis);

		while (!text.isEmpty() && f.width(text) + ellipsisWidth > maxPixels) {
			text = text.substring(0, text.length() - 1);
		}

		return text + ellipsis;
	}

	private void buildPanels() {
		Map<ModuleType, List<GuiModuleEntry>> groupedEntries = new LinkedHashMap<>();

		for (Command command : LiteClient.getCommandManager().getCommands()) {
			if (command.isVisibleInGui()) {
				groupedEntries.computeIfAbsent(command.getModuleType(), ignored -> new ArrayList<>())
						.add(GuiModuleEntry.command(command));
			}
		}

		for (Module module : LiteClient.getModuleManager().getModules()) {
			if (module.isVisibleInGui()) {
				groupedEntries.computeIfAbsent(module.getModuleType(), ignored -> new ArrayList<>())
						.add(GuiModuleEntry.module(module));
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