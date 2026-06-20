package me.madeq.client.gui.component;

import java.awt.Color;
import java.util.Locale;
import me.madeq.client.command.ArgumentType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ConfigSliderField implements ConfigComponent {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final double min;
	private final double max;
	private final ArgumentType type;
	private final ConfigTextBox input;
	private boolean dragging;

	public ConfigSliderField(int x, int y, int width, int height, double min, double max, ArgumentType type) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.min = min;
		this.max = max;
		this.type = type;
		this.input = new ConfigTextBox(x + width - 48, y, 48, height, getInputMode(type));
		setValue(formatValue(min));
	}

	@Override
	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
		int sliderWidth = width - 56;
		int trackY = y + height / 2 - 2;
		int trackHeight = 4;
		double normalized = (getNumericValue() - min) / (max - min);
		normalized = clamp(normalized, 0.0D, 1.0D);
		int fillWidth = (int) Math.round(sliderWidth * normalized);
		int knobX = x + fillWidth;
		boolean hovered = mouseX >= x && mouseX <= x + sliderWidth && mouseY >= y && mouseY <= y + height;

		graphics.fill(x, y + 2, x + sliderWidth, y + height - 2, new Color(10, 12, 17, 180).getRGB());
		graphics.fill(x + 5, trackY, x + sliderWidth - 5, trackY + trackHeight, new Color(44, 50, 63).getRGB());
		graphics.fill(x + 5, trackY, x + Math.max(5, fillWidth), trackY + trackHeight, new Color(31, 119, 255).getRGB());
		graphics.fill(knobX - 3, y + 3, knobX + 3, y + height - 3, (hovered || dragging ? new Color(155, 203, 255) : new Color(102, 169, 255)).getRGB());
		graphics.fill(knobX - 1, y + 2, knobX + 1, y + height - 2, new Color(235, 246, 255).getRGB());
		input.render(graphics, font, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (input.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		if (button != 0 || !isInsideSlider(mouseX, mouseY)) {
			return false;
		}

		dragging = true;
		updateFromMouse(mouseX);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button) {
		if (button != 0 || !dragging) {
			return false;
		}

		updateFromMouse(mouseX);
		return true;
	}

	@Override
	public void mouseReleased() {
		dragging = false;
		input.mouseReleased();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean handled = input.keyPressed(keyCode, scanCode, modifiers);
		if (handled) {
			normalizeInputValue();
		}

		return handled;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		boolean handled = input.charTyped(codePoint, modifiers);
		if (handled) {
			normalizeInputValue();
		}

		return handled;
	}

	@Override
	public void setFocused(boolean focused) {
		input.setFocused(focused);
	}

	@Override
	public boolean isFocused() {
		return input.isFocused();
	}

	@Override
	public String getValue() {
		return input.getValue();
	}

	@Override
	public void setValue(String value) {
		input.setValue(formatValue(parseValue(value)));
	}

	@Override
	public int getY() {
		return y;
	}

	private void updateFromMouse(double mouseX) {
		int sliderWidth = width - 56;
		double normalized = clamp((mouseX - x) / sliderWidth, 0.0D, 1.0D);
		double next = min + (max - min) * normalized;
		input.setValue(formatValue(next));
	}

	private void normalizeInputValue() {
		String value = input.getValue();
		if (value.isBlank() || "-".equals(value) || ".".equals(value)) {
			return;
		}

		input.setValue(formatValue(parseValue(value)));
	}

	private double getNumericValue() {
		return parseValue(input.getValue());
	}

	private double parseValue(String rawValue) {
		try {
			return clamp(Double.parseDouble(rawValue), min, max);
		} catch (NumberFormatException exception) {
			return min;
		}
	}

	private String formatValue(double value) {
		if (type == ArgumentType.INT) {
			return Integer.toString((int) Math.round(clamp(value, min, max)));
		}

		return String.format(Locale.ROOT, "%.2f", clamp(value, min, max));
	}

	private boolean isInsideSlider(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width - 56 && mouseY >= y && mouseY <= y + height;
	}

	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private ConfigTextBox.InputMode getInputMode(ArgumentType type) {
		return switch (type) {
			case INT -> ConfigTextBox.InputMode.INT;
			case DOUBLE -> ConfigTextBox.InputMode.DOUBLE;
			default -> ConfigTextBox.InputMode.TEXT;
		};
	}
}
