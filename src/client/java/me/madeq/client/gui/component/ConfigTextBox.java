package me.madeq.client.gui.component;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class ConfigTextBox implements ConfigComponent {
	private static final int MAX_LENGTH = 128;

	public enum InputMode {
		TEXT,
		INT,
		DOUBLE
	}

	protected final int x;
	protected final int y;
	protected final int width;
	protected final int height;
	protected final InputMode inputMode;
	protected String value = "";
	protected boolean focused;
	protected int cursor;
	protected boolean allSelected;

	public ConfigTextBox(int x, int y, int width, int height) {
		this(x, y, width, height, InputMode.TEXT);
	}

	public ConfigTextBox(int x, int y, int width, int height, InputMode inputMode) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.inputMode = inputMode;
	}

	@Override
	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isInside(mouseX, mouseY);
		graphics.fill(x, y, x + width, y + height, new Color(8, 10, 14, 235).getRGB());
		graphics.fill(x, y + height - 1, x + width, y + height, (focused || hovered ? new Color(102, 169, 255) : new Color(48, 58, 74)).getRGB());

		String renderedValue = getRenderedValue();
		graphics.drawString(font, renderedValue, x + 5, y + 5, new Color(230, 235, 242).getRGB(), true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isInside(mouseX, mouseY)) {
			return false;
		}

		focused = true;
		cursor = value.length();
		allSelected = false;
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

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (!focused || Character.isISOControl(codePoint)) {
			return false;
		}

		if (!canAppend(codePoint)) {
			return false;
		}

		insertText(Character.toString(codePoint));
		return true;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
		if (!focused) {
			allSelected = false;
		}
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = limit(sanitize(value == null ? "" : value, ""));
		this.cursor = this.value.length();
		this.allSelected = false;
	}

	@Override
	public int getY() {
		return y;
	}

	protected boolean isInside(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	private boolean canAppend(char codePoint) {
		String currentValue = allSelected ? "" : value;
		return switch (inputMode) {
			case TEXT -> true;
			case INT -> isValidIntegerCharacter(codePoint, currentValue);
			case DOUBLE -> isValidDoubleCharacter(codePoint, currentValue);
		};
	}

	private boolean isValidIntegerCharacter(char codePoint, String currentValue) {
		if (Character.isDigit(codePoint)) {
			return true;
		}

		return codePoint == '-' && currentValue.isEmpty();
	}

	private boolean isValidDoubleCharacter(char codePoint, String currentValue) {
		if (Character.isDigit(codePoint)) {
			return true;
		}

		if (codePoint == '-' && currentValue.isEmpty()) {
			return true;
		}

		return codePoint == '.' && !currentValue.contains(".");
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

		String sanitized = sanitize(text, allSelected ? "" : value);
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

		if (!focused || (System.currentTimeMillis() / 450L) % 2L != 0L) {
			return value;
		}

		return value.substring(0, cursor) + "_" + value.substring(cursor);
	}

	private String sanitize(String text, String baseValue) {
		StringBuilder sanitized = new StringBuilder();
		String simulatedValue = baseValue;

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (Character.isISOControl(character)) {
				continue;
			}

			if (canInsert(character, simulatedValue)) {
				sanitized.append(character);
				simulatedValue += character;
			}
		}

		return sanitized.toString();
	}

	private boolean canInsert(char codePoint, String currentValue) {
		return switch (inputMode) {
			case TEXT -> true;
			case INT -> Character.isDigit(codePoint) || codePoint == '-' && currentValue.isEmpty();
			case DOUBLE -> Character.isDigit(codePoint)
					|| codePoint == '-' && currentValue.isEmpty()
					|| codePoint == '.' && !currentValue.contains(".");
		};
	}

	private String limit(String text) {
		return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
	}

	private boolean isControlDown(int modifiers) {
		return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
	}
}
