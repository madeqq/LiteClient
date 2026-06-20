package me.madeq.client.gui.component;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ConfigListSelect implements ConfigComponent {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final List<String> options;
	private String value;
	private boolean focused;
	private boolean open;

	public ConfigListSelect(int x, int y, int width, int height, List<String> options) {
		if (options == null || options.isEmpty()) {
			throw new IllegalArgumentException("List select requires at least one option");
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.options = List.copyOf(options);
		this.value = this.options.getFirst();
	}

	@Override
	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isInside(mouseX, mouseY, x, y, width, height);
		graphics.fill(x, y, x + width, y + height, new Color(8, 10, 14, 235).getRGB());
		graphics.fill(x, y + height - 1, x + width, y + height, (focused || hovered ? new Color(102, 169, 255) : new Color(48, 58, 74)).getRGB());
		graphics.drawString(font, value, x + 5, y + 5, new Color(230, 235, 242).getRGB(), true);
		graphics.drawString(font, open ? "-" : "+", x + width - 12, y + 5, new Color(180, 210, 255).getRGB(), true);

		if (!open) {
			return;
		}

		int dropdownTop = y + height;
		int dropdownBottom = dropdownTop + options.size() * height;
		graphics.fill(x - 1, dropdownTop - 1, x + width + 1, dropdownBottom + 1, new Color(3, 4, 7, 245).getRGB());
		graphics.fill(x, dropdownTop, x + width, dropdownBottom, new Color(10, 12, 17, 255).getRGB());

		for (int index = 0; index < options.size(); index++) {
			int optionY = y + height + index * height;
			String option = options.get(index);
			boolean optionHovered = isInside(mouseX, mouseY, x, optionY, width, height);
			boolean selected = option.equals(value);
			int background = selected ? new Color(25, 92, 210, 255).getRGB() : optionHovered ? new Color(30, 36, 48, 255).getRGB() : new Color(15, 18, 24, 255).getRGB();

			graphics.fill(x, optionY, x + width, optionY + height, background);
			graphics.fill(x, optionY + height - 1, x + width, optionY + height, new Color(42, 50, 64, 255).getRGB());
			graphics.drawString(font, option, x + 5, optionY + 5, new Color(230, 235, 242).getRGB(), true);
		}
	}

	public boolean isOpen() {
		return open;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}

		if (open) {
			for (int index = 0; index < options.size(); index++) {
				int optionY = y + height + index * height;

				if (isInside(mouseX, mouseY, x, optionY, width, height)) {
					value = options.get(index);
					focused = true;
					open = false;
					return true;
				}
			}
		}

		if (!isInside(mouseX, mouseY, x, y, width, height)) {
			return false;
		}

		focused = true;
		open = !open;
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

		if (keyCode == 256) {
			open = false;
			return true;
		}

		return false;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return false;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;

		if (!focused) {
			open = false;
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
		if (value != null && options.contains(value)) {
			this.value = value;
			return;
		}

		this.value = options.getFirst();
	}

	@Override
	public int getY() {
		return y;
	}

	private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
