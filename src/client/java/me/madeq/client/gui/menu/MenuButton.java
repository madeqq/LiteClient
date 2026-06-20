package me.madeq.client.gui.menu;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class MenuButton {
	private final String label;
	private final String hint;
	private final Runnable action;
	private int x;
	private int y;
	private int width;
	private int height;
	private float hoverProgress;

	public MenuButton(String label, String hint, Runnable action) {
		this.label = label;
		this.hint = hint;
		this.action = action;
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = isInside(mouseX, mouseY);
		hoverProgress += ((hovered ? 1.0F : 0.0F) - hoverProgress) * 0.22F;
		int accentWidth = 3 + Math.round(8.0F * hoverProgress);
		int background = blend(new Color(13, 17, 25, 224), new Color(24, 36, 54, 238), hoverProgress).getRGB();

		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, new Color(3, 5, 9, 130).getRGB());
		graphics.fill(x, y, x + width, y + height, background);
		graphics.fill(x, y, x + accentWidth, y + height, MenuTheme.ACCENT);
		graphics.fill(x, y, x + width, y + 1, new Color(255, 255, 255, 24 + Math.round(34.0F * hoverProgress)).getRGB());

		graphics.drawString(font, label, x + 16, y + 8, MenuTheme.TEXT, true);
		graphics.drawString(font, hint, x + 16, y + 21, MenuTheme.MUTED_TEXT, true);
		graphics.drawString(font, ">", x + width - 18, y + 14, blend(new Color(120, 148, 180), new Color(210, 235, 255), hoverProgress).getRGB(), true);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) {
			return false;
		}

		action.run();
		return true;
	}

	private boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	private Color blend(Color from, Color to, float progress) {
		float clamped = Math.max(0.0F, Math.min(1.0F, progress));
		int red = Math.round(from.getRed() + (to.getRed() - from.getRed()) * clamped);
		int green = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clamped);
		int blue = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clamped);
		int alpha = Math.round(from.getAlpha() + (to.getAlpha() - from.getAlpha()) * clamped);
		return new Color(red, green, blue, alpha);
	}
}
