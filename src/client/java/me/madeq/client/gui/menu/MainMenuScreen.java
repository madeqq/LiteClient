package me.madeq.client.gui.menu;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.madeq.client.LiteClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

public class MainMenuScreen extends Screen {
	private final List<MenuButton> buttons = new ArrayList<>();
	private final MenuButton altManagerButton = new MenuButton("Alt Manager", "Manage saved profiles", () -> minecraft.setScreen(new AltManagerScreen(this)));

	public MainMenuScreen() {
		super(Component.literal("LiteClient"));
	}

	@Override
	protected void init() {
		buttons.clear();
		buttons.add(new MenuButton("Singleplayer", "Local worlds", () -> minecraft.setScreen(new SelectWorldScreen(this))));
		buttons.add(new MenuButton("Multiplayer", "Server selector", () -> minecraft.setScreen(new MultiplayerScreen(this))));
		buttons.add(new MenuButton("Options", "Video, controls and client settings", () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
		buttons.add(new MenuButton("Quit", "Close the game", () -> minecraft.stop()));

		int panelWidth = getMenuWidth();
		int x = getMenuX();
		int y = getMenuY();
		int buttonY = y + 118;

		for (MenuButton button : buttons) {
			button.setBounds(x + 22, buttonY, panelWidth - 44, 42);
			buttonY += 50;
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		MenuTheme.renderBackground(graphics, width, height);
		renderBrand(graphics);
		renderActionPanel(graphics, mouseX, mouseY);
		renderInfoPanel(graphics, mouseX, mouseY);
		renderFooter(graphics);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (MenuButton menuButton : buttons) {
			if (menuButton.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}

		if (altManagerButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void renderBrand(GuiGraphics graphics) {
		int x = getMenuX();
		int y = getMenuY();
		MenuTheme.panel(graphics, x, y, getMenuWidth(), getPanelHeight());
		graphics.fill(x + 2, y, x + getMenuWidth(), y + 78, new Color(12, 18, 29, 224).getRGB());
		graphics.fill(x + 22, y + 84, x + getMenuWidth() - 22, y + 85, new Color(255, 255, 255, 28).getRGB());

		graphics.drawString(font, "LiteClient", x + 22, y + 24, MenuTheme.TEXT, true);
		graphics.drawString(font, "Version " + LiteClient.VERSION + " / Minecraft 1.21.4", x + 22, y + 40, MenuTheme.SOFT_TEXT, true);
		graphics.drawString(font, "Best Free Crash/Exploit Minecraft Client", x + 22, y + 58, MenuTheme.MUTED_TEXT, true);
	}

	private void renderActionPanel(GuiGraphics graphics, int mouseX, int mouseY) {
		for (MenuButton button : buttons) {
			button.render(graphics, font, mouseX, mouseY);
		}
	}

	private void renderInfoPanel(GuiGraphics graphics, int mouseX, int mouseY) {
		int panelWidth = Math.max(170, Math.min(230, width / 4));
		int panelHeight = 108;
		int x = Math.min(width - panelWidth - 18, getMenuX() + getMenuWidth() + 18);
		int y = getMenuY();

		if (width < 720) {
			return;
		}

		MenuTheme.panel(graphics, x, y, panelWidth, panelHeight);
		graphics.drawString(font, "Profile", x + 18, y + 18, MenuTheme.TEXT, true);
		renderInfoLine(graphics, x, y + 44, "Nickname", minecraft.getUser().getName());
		altManagerButton.setBounds(x + 18, y + 62, panelWidth - 36, 34);
		altManagerButton.render(graphics, font, mouseX, mouseY);
	}

	private void renderInfoLine(GuiGraphics graphics, int x, int y, String label, String value) {
		graphics.drawString(font, label, x + 18, y, MenuTheme.MUTED_TEXT, true);
		graphics.drawString(font, value, x + 92, y, MenuTheme.SOFT_TEXT, true);
	}

	private void renderFooter(GuiGraphics graphics) {
		String footer = "LiteClient | Created by madeq & 0WhiteDev";
		graphics.drawString(font, footer, 12, height - 18, new Color(210, 220, 235, 150).getRGB(), true);
	}

	private int getMenuWidth() {
		return Math.max(292, Math.min(384, width / 3));
	}

	private int getPanelHeight() {
		return 118 + buttons.size() * 50 + 22;
	}

	private int getMenuX() {
		if (width < 720) {
			return (width - getMenuWidth()) / 2;
		}

		return Math.max(28, width / 12);
	}

	private int getMenuY() {
		return Math.max(22, (height - getPanelHeight()) / 2);
	}
}
