package me.madeq.client.gui.menu;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class MenuTextField {
	private static final int MAX_LENGTH = 96;
	private final String placeholder;
	private int x;
	private int y;
	private int width;
	private int height;
	private String value = "";
	private boolean focused;
	private int cursor;
	private boolean allSelected;

	public MenuTextField(String placeholder) {
		this.placeholder = placeholder;
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = isInside(mouseX, mouseY);
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, new Color(3, 5, 9, 150).getRGB());
		graphics.fill(x, y, x + width, y + height, new Color(10, 13, 19, 236).getRGB());
		graphics.fill(x, y + height - 1, x + width, y + height, focused || hovered ? MenuTheme.ACCENT : new Color(48, 58, 74).getRGB());

		String rendered = value.isEmpty() && !focused ? placeholder : getRenderedValue();
		int color = value.isEmpty() && !focused ? MenuTheme.MUTED_TEXT : MenuTheme.TEXT;
		graphics.drawString(font, rendered, x + 8, y + (height - 8) / 2, color, true);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}

		focused = isInside(mouseX, mouseY);
		if (focused) {
			cursor = value.length();
			allSelected = false;
		}

		return focused;
	}

	public boolean keyPressed(int keyCode) {
		return keyPressed(keyCode, 0, 0);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused) {
			return false;
		}

		if (isControlDown(modifiers)) {
			return handleControlShortcut(keyCode);
		}

		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			deletePreviousCharacter();
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			deleteNextCharacter();
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			moveCursor(-1);
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			moveCursor(1);
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_HOME) {
			cursor = 0;
			allSelected = false;
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_END) {
			cursor = value.length();
			allSelected = false;
			return true;
		}

		return false;
	}

	public boolean charTyped(char codePoint) {
		if (!focused || Character.isISOControl(codePoint)) {
			return false;
		}

		if (value.length() >= MAX_LENGTH && !allSelected) {
			return false;
		}

		insertText(Character.toString(codePoint));
		return true;
	}

	public String getValue() {
		return value.trim();
	}

	public void setValue(String value) {
		this.value = limit(value == null ? "" : value);
		this.cursor = this.value.length();
		this.allSelected = false;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
		if (!focused) {
			allSelected = false;
		}
	}

	private boolean handleControlShortcut(int keyCode) {
		if (keyCode == GLFW.GLFW_KEY_A) {
			allSelected = true;
			cursor = value.length();
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_C) {
			if (!value.isEmpty()) {
				Minecraft.getInstance().keyboardHandler.setClipboard(value);
			}

			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_V) {
			insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
			return true;
		}

		return false;
	}

	private void insertText(String text) {
		if (text == null || text.isEmpty()) {
			return;
		}

		if (allSelected) {
			value = "";
			cursor = 0;
			allSelected = false;
		}

		String sanitized = sanitize(text);
		int available = MAX_LENGTH - value.length();
		if (available <= 0) {
			return;
		}

		String inserted = sanitized.substring(0, Math.min(available, sanitized.length()));
		value = value.substring(0, cursor) + inserted + value.substring(cursor);
		cursor += inserted.length();
	}

	private void deletePreviousCharacter() {
		if (allSelected) {
			clearSelection();
			return;
		}

		if (cursor <= 0 || value.isEmpty()) {
			return;
		}

		value = value.substring(0, cursor - 1) + value.substring(cursor);
		cursor--;
	}

	private void deleteNextCharacter() {
		if (allSelected) {
			clearSelection();
			return;
		}

		if (cursor >= value.length()) {
			return;
		}

		value = value.substring(0, cursor) + value.substring(cursor + 1);
	}

	private void clearSelection() {
		value = "";
		cursor = 0;
		allSelected = false;
	}

	private void moveCursor(int direction) {
		cursor = Math.max(0, Math.min(value.length(), cursor + direction));
		allSelected = false;
	}

	private String getRenderedValue() {
		if (allSelected && focused) {
			return "[" + value + "]";
		}

		if (!focused || (System.currentTimeMillis() / 480L) % 2L != 0L) {
			return value;
		}

		return value.substring(0, cursor) + "_" + value.substring(cursor);
	}

	private String sanitize(String text) {
		StringBuilder sanitized = new StringBuilder();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (!Character.isISOControl(character)) {
				sanitized.append(character);
			}
		}

		return sanitized.toString();
	}

	private String limit(String text) {
		return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
	}

	private boolean isControlDown(int modifiers) {
		return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
	}

	private boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
