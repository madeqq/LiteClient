package me.madeq.client.gui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface ConfigComponent {
	void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick);

	boolean mouseClicked(double mouseX, double mouseY, int button);

	boolean mouseDragged(double mouseX, double mouseY, int button);

	void mouseReleased();

	boolean keyPressed(int keyCode, int scanCode, int modifiers);

	boolean charTyped(char codePoint, int modifiers);

	void setFocused(boolean focused);

	boolean isFocused();

	String getValue();

	void setValue(String value);

	int getY();
}
