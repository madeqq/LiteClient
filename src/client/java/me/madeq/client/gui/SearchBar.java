package me.madeq.client.gui;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class SearchBar {

	private static final int ICON_WIDTH    = 14;
	private static final int PADDING_X     = 5;
	private static final int MAX_LENGTH    = 64;

	private static final int COL_BG           = new Color(10,  13,  19,  236).getRGB();
	private static final int COL_BG_SHADOW    = new Color( 3,   5,   9,  150).getRGB();
	private static final int COL_BORDER_IDLE  = new Color(48,  58,  74      ).getRGB();
	private static final int COL_BORDER_ACTIVE = new Color(74, 163, 255     ).getRGB();
	private static final int COL_TEXT         = new Color(220, 226, 235     ).getRGB();
	private static final int COL_PLACEHOLDER  = new Color(110, 122, 140     ).getRGB();
	private static final int COL_ICON         = new Color( 90, 110, 140     ).getRGB();
	private static final int COL_CLEAR_IDLE   = new Color(110, 122, 140     ).getRGB();
	private static final int COL_CLEAR_HOVER  = new Color(220, 100, 100     ).getRGB();

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	private String value = "";
	private int cursor = 0;
	private boolean allSelected = false;
	private boolean focused = true;

	public SearchBar(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public String getValue() {
		return value.trim();
	}

	public void clear() {
		value = "";
		cursor = 0;
		allSelected = false;
	}

	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		boolean hovered = isInside(mouseX, mouseY);

		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, COL_BG_SHADOW);
		graphics.fill(x, y, x + width, y + height, COL_BG);
		int borderCol = (focused || hovered) ? COL_BORDER_ACTIVE : COL_BORDER_IDLE;
		graphics.fill(x, y + height - 1, x + width, y + height, borderCol);

		graphics.drawString(font, "\uD83D\uDD0D", x + PADDING_X, y + (height - 8) / 2, COL_ICON, false);

		int textX = x + PADDING_X + ICON_WIDTH;
		int clearButtonWidth = value.isEmpty() ? 0 : font.width("×") + PADDING_X;
		int textAreaWidth = width - PADDING_X - ICON_WIDTH - clearButtonWidth - PADDING_X;

		if (value.isEmpty() && !focused) {
			graphics.drawString(font, "Search modules…", textX, y + (height - 8) / 2, COL_PLACEHOLDER, false);
		} else {
			String displayed = getDisplayedValue(font, textAreaWidth);
			graphics.drawString(font, displayed, textX, y + (height - 8) / 2, COL_TEXT, false);
		}

		if (!value.isEmpty()) {
			boolean clearHovered = isClearButtonHovered(mouseX, mouseY);
			int clearX = x + width - font.width("×") - PADDING_X;
			int clearY = y + (height - 8) / 2;
			graphics.drawString(font, "×", clearX, clearY,
					clearHovered ? COL_CLEAR_HOVER : COL_CLEAR_IDLE, false);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}

		if (!value.isEmpty() && isClearButtonHovered(mouseX, mouseY)) {
			clear();
			focused = true;
			return true;
		}

		if (isInside(mouseX, mouseY)) {
			focused = true;
			cursor = value.length();
			allSelected = false;
			return true;
		}

		focused = false;
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused) {
			return false;
		}

		boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0
				|| (modifiers & GLFW.GLFW_MOD_SUPER) != 0;

		if (ctrl) {
			return handleControlShortcut(keyCode);
		}

		switch (keyCode) {
			case GLFW.GLFW_KEY_BACKSPACE -> { deletePrevious(); return true; }
			case GLFW.GLFW_KEY_DELETE    -> { deleteNext();     return true; }
			case GLFW.GLFW_KEY_LEFT      -> { moveCursor(-1);   return true; }
			case GLFW.GLFW_KEY_RIGHT     -> { moveCursor(+1);   return true; }
			case GLFW.GLFW_KEY_HOME      -> { cursor = 0; allSelected = false; return true; }
			case GLFW.GLFW_KEY_END       -> { cursor = value.length(); allSelected = false; return true; }
		}

		return false;
	}

	public boolean charTyped(char codePoint) {
		if (Character.isISOControl(codePoint)) {
			return false;
		}

		focused = true;

		if (value.length() >= MAX_LENGTH && !allSelected) {
			return false;
		}

		insert(Character.toString(codePoint));
		return true;
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
			insert(Minecraft.getInstance().keyboardHandler.getClipboard());
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_X) {
			if (!value.isEmpty()) {
				Minecraft.getInstance().keyboardHandler.setClipboard(value);
				clear();
			}
			return true;
		}
		return false;
	}

	private void insert(String text) {
		if (text == null || text.isEmpty()) {
			return;
		}

		if (allSelected) {
			value = "";
			cursor = 0;
			allSelected = false;
		}

		StringBuilder clean = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (!Character.isISOControl(c)) {
				clean.append(c);
			}
		}

		int available = MAX_LENGTH - value.length();
		if (available <= 0) {
			return;
		}

		String chunk = clean.substring(0, Math.min(available, clean.length()));
		value = value.substring(0, cursor) + chunk + value.substring(cursor);
		cursor += chunk.length();
	}

	private void deletePrevious() {
		if (allSelected) { clear(); return; }
		if (cursor <= 0 || value.isEmpty()) return;
		value = value.substring(0, cursor - 1) + value.substring(cursor);
		cursor--;
	}

	private void deleteNext() {
		if (allSelected) { clear(); return; }
		if (cursor >= value.length()) return;
		value = value.substring(0, cursor) + value.substring(cursor + 1);
	}

	private void moveCursor(int delta) {
		cursor = Math.max(0, Math.min(value.length(), cursor + delta));
		allSelected = false;
	}

	private String getDisplayedValue(Font font, int maxPixels) {
		String full = value;
		int start = 0;

		if (font.width(full.substring(start, cursor)) > maxPixels - 8) {
			while (start < cursor && font.width(full.substring(start, cursor)) > maxPixels - 8) {
				start++;
			}
		}

		String visible = full.substring(start);

		if (focused) {
			boolean showCursor = (System.currentTimeMillis() / 480L) % 2L == 0L;
			if (showCursor || allSelected) {
				int localCursor = Math.max(0, Math.min(cursor - start, visible.length()));
				if (allSelected) {
					visible = "[" + visible + "]";
				} else {
					visible = visible.substring(0, localCursor) + "_" + visible.substring(localCursor);
				}
			}
		}

		return visible;
	}

	private boolean isInside(double mx, double my) {
		return mx >= x && mx <= x + width && my >= y && my <= y + height;
	}

	private boolean isClearButtonHovered(double mx, double my) {
		Font f = Minecraft.getInstance().font;
		int clearW = f.width("×") + PADDING_X;
		return mx >= x + width - clearW && mx <= x + width && my >= y && my <= y + height;
	}
}