package me.madeq.client.gui.menu;

import java.util.List;
import me.madeq.client.mixin.DisconnectedScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class LiteDisconnectedScreen extends Screen {
	private final Component reason;
	private final MenuButton serverListButton = new MenuButton("Server List", "Return to multiplayer", () ->
			minecraft.setScreen(new MultiplayerScreen(new MainMenuScreen(), true)));
	private final MenuButton mainMenuButton = new MenuButton("Main Menu", "Back to LiteClient home", () ->
			minecraft.setScreen(new MainMenuScreen()));

	public LiteDisconnectedScreen(Component title, Component reason) {
		super(title);
		this.reason = reason == null ? Component.empty() : reason;
	}

	public static LiteDisconnectedScreen fromVanilla(DisconnectedScreen screen) {
		Component reason = ((DisconnectedScreenAccessor) screen).liteclient$getDetails().reason();
		return new LiteDisconnectedScreen(screen.getTitle(), reason);
	}

	@Override
	protected void init() {
		int panelWidth = getPanelWidth();
		int panelHeight = getPanelHeight();
		int x = (width - panelWidth) / 2;
		int y = (height - panelHeight) / 2;
		int buttonWidth = panelWidth - 44;
		int buttonX = x + 22;
		int buttonY = y + panelHeight - 108;

		serverListButton.setBounds(buttonX, buttonY, buttonWidth, 42);
		mainMenuButton.setBounds(buttonX, buttonY + 50, buttonWidth, 42);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		MenuTheme.renderBackground(graphics, width, height);

		int panelWidth = getPanelWidth();
		int panelHeight = getPanelHeight();
		int x = (width - panelWidth) / 2;
		int y = (height - panelHeight) / 2;

		MenuTheme.panel(graphics, x, y, panelWidth, panelHeight);
		graphics.fill(x + 2, y, x + panelWidth, y + 72, MenuTheme.PANEL_STRONG);

		graphics.drawString(font, getTitle().getString(), x + 22, y + 24, MenuTheme.DANGER, true);
		graphics.drawString(font, "Connection lost", x + 22, y + 44, MenuTheme.MUTED_TEXT, true);

		renderReason(graphics, x + 22, y + 88, panelWidth - 44);

		serverListButton.render(graphics, font, mouseX, mouseY);
		mainMenuButton.render(graphics, font, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (serverListButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		if (mainMenuButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void renderReason(GuiGraphics graphics, int x, int y, int maxWidth) {
		List<FormattedCharSequence> lines = font.split(reason, maxWidth);

		if (lines.isEmpty()) {
			graphics.drawString(font, "No reason provided", x, y, MenuTheme.SOFT_TEXT, true);
			return;
		}

		int lineY = y;
		for (FormattedCharSequence line : lines) {
			if (lineY > y + 120) {
				graphics.drawString(font, "...", x, lineY, MenuTheme.MUTED_TEXT, true);
				break;
			}

			graphics.drawString(font, line, x, lineY, MenuTheme.SOFT_TEXT, true);
			lineY += 12;
		}
	}

	private int getPanelWidth() {
		return Math.max(320, Math.min(460, width - 80));
	}

	private int getPanelHeight() {
		return Math.max(280, Math.min(360, height - 120));
	}
}
