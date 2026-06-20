package me.madeq.client.gui.menu;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class MenuTheme {
	public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("liteclient", "textures/gui/background.png");
	public static final int TEXT = new Color(242, 247, 255).getRGB();
	public static final int MUTED_TEXT = new Color(150, 162, 180).getRGB();
	public static final int SOFT_TEXT = new Color(198, 210, 228).getRGB();
	public static final int PANEL = new Color(10, 13, 19, 210).getRGB();
	public static final int PANEL_STRONG = new Color(12, 16, 24, 235).getRGB();
	public static final int PANEL_SOFT = new Color(255, 255, 255, 14).getRGB();
	public static final int BORDER = new Color(255, 255, 255, 38).getRGB();
	public static final int ACCENT = new Color(74, 163, 255).getRGB();
	public static final int ACCENT_DARK = new Color(25, 92, 210).getRGB();
	public static final int SUCCESS = new Color(87, 255, 173).getRGB();
	public static final int WARNING = new Color(255, 196, 87).getRGB();
	public static final int DANGER = new Color(255, 94, 110).getRGB();

	private MenuTheme() {
	}

	public static void renderBackground(GuiGraphics graphics, int width, int height) {
		graphics.blit(RenderType::guiTextured, BACKGROUND, 0, 0, 0.0F, 0.0F, width, height, width, height);
		graphics.fill(0, 0, width, height, new Color(0, 0, 0, 88).getRGB());
		graphics.fill(0, 0, width, height, new Color(5, 8, 14, 72).getRGB());
	}

	public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, new Color(2, 4, 8, 150).getRGB());
		graphics.fill(x, y, x + width, y + height, PANEL);
		graphics.fill(x, y, x + width, y + 1, BORDER);
		graphics.fill(x, y, x + 2, y + height, ACCENT);
	}

	public static void card(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered, boolean selected) {
		int background = selected ? new Color(20, 35, 55, 232).getRGB() : hovered ? new Color(18, 23, 33, 232).getRGB() : PANEL_STRONG;
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, selected ? ACCENT : new Color(3, 5, 9, 140).getRGB());
		graphics.fill(x, y, x + width, y + height, background);
		graphics.fill(x, y, x + width, y + 1, new Color(255, 255, 255, hovered || selected ? 48 : 24).getRGB());
	}

	public static void label(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
		graphics.drawString(font, text, x, y, color, true);
	}

	public static void centered(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
		graphics.drawString(font, text, x + (width - font.width(text)) / 2, y, color, true);
	}
}
