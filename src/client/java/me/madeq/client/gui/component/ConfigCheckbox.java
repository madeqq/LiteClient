package me.madeq.client.gui.component;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ConfigCheckbox implements ConfigComponent {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private boolean checked;
	private boolean focused;

	public ConfigCheckbox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isInside(mouseX, mouseY);
		int boxSize = Math.min(height, 16);
		int boxY = y + (height - boxSize) / 2;

		graphics.fill(x, boxY, x + boxSize, boxY + boxSize, new Color(8, 10, 14, 235).getRGB());
		graphics.fill(x, boxY, x + boxSize, boxY + 1, borderColor(hovered));
		graphics.fill(x, boxY + boxSize - 1, x + boxSize, boxY + boxSize, borderColor(hovered));
		graphics.fill(x, boxY, x + 1, boxY + boxSize, borderColor(hovered));
		graphics.fill(x + boxSize - 1, boxY, x + boxSize, boxY + boxSize, borderColor(hovered));

		if (checked) {
			graphics.fill(x + 4, boxY + 8, x + 6, boxY + 10, new Color(155, 203, 255).getRGB());
			graphics.fill(x + 6, boxY + 10, x + 8, boxY + 12, new Color(155, 203, 255).getRGB());
			graphics.fill(x + 8, boxY + 6, x + 10, boxY + 10, new Color(155, 203, 255).getRGB());
			graphics.fill(x + 10, boxY + 4, x + 12, boxY + 8, new Color(155, 203, 255).getRGB());
		}

		graphics.drawString(font, checked ? "true" : "false", x + boxSize + 7, y + 5, new Color(230, 235, 242).getRGB(), true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) {
			return false;
		}

		checked = !checked;
		focused = true;
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	public void mouseReleased() {
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused || keyCode != 32) {
			return false;
		}

		checked = !checked;
		return true;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return false;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public String getValue() {
		return Boolean.toString(checked);
	}

	@Override
	public void setValue(String value) {
		checked = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
	}

	@Override
	public int getY() {
		return y;
	}

	private int borderColor(boolean hovered) {
		return (focused || hovered ? new Color(102, 169, 255) : new Color(48, 58, 74)).getRGB();
	}

	private boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
