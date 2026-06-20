package me.madeq.client.gui;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class DropdownPanel {
	private static final int WIDTH = 112;
	private static final int HEADER_HEIGHT = 18;
	private static final int ENTRY_HEIGHT = 16;

	private final String title;
	private int x;
	private int y;
	private final List<GuiModuleEntry> entries;
	private boolean open = true;
	private boolean dragging;
	private boolean movedWhileDragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private float animation = 1.0F;

	public DropdownPanel(String title, int x, int y, List<GuiModuleEntry> entries) {
		this.title = title;
		this.x = x;
		this.y = y;
		this.entries = entries;
	}

	public String getTitle() {
		return title;
	}

	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		updateAnimation();

		int animatedContentHeight = Math.round(entries.size() * ENTRY_HEIGHT * animation);
		int totalHeight = HEADER_HEIGHT + animatedContentHeight;
		boolean hoveringHeader = isInside(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT);

		graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + totalHeight + 1, new Color(5, 6, 8, 185).getRGB());
		graphics.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, (hoveringHeader ? new Color(38, 121, 255) : new Color(25, 92, 210)).getRGB());
		graphics.fill(x, y + HEADER_HEIGHT - 1, x + WIDTH, y + HEADER_HEIGHT, new Color(102, 169, 255).getRGB());

		graphics.drawString(font, title, x + 6, y + 5, new Color(250, 252, 255).getRGB(), true);
		graphics.drawString(font, open ? "-" : "+", x + WIDTH - 12, y + 5, new Color(250, 252, 255).getRGB(), true);

		if (animatedContentHeight <= 0) {
			return;
		}

		graphics.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + animatedContentHeight, new Color(16, 18, 23, 218).getRGB());

		int visibleEntries = Math.min(entries.size(), (int) Math.ceil(animatedContentHeight / (float) ENTRY_HEIGHT));
		for (int index = 0; index < visibleEntries; index++) {
			int entryY = y + HEADER_HEIGHT + index * ENTRY_HEIGHT;
			if (entryY + ENTRY_HEIGHT > y + HEADER_HEIGHT + animatedContentHeight) {
				continue;
			}

			boolean hoveringEntry = isInside(mouseX, mouseY, x, entryY, WIDTH, ENTRY_HEIGHT);
			int entryColor = hoveringEntry ? new Color(30, 36, 48, 235).getRGB() : new Color(20, 23, 30, 210).getRGB();
			graphics.fill(x, entryY, x + WIDTH, entryY + ENTRY_HEIGHT, entryColor);
			graphics.drawString(font, entries.get(index).name(), x + 8, entryY + 4, new Color(220, 226, 235).getRGB(), true);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY) {
		if (!isInside(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT)) {
			return false;
		}

		dragging = true;
		movedWhileDragging = false;
		dragOffsetX = mouseX - x;
		dragOffsetY = mouseY - y;
		return true;
	}

	public GuiModuleEntry getEntryAt(double mouseX, double mouseY) {
		if (animation < 0.95F) {
			return null;
		}

		for (int index = 0; index < entries.size(); index++) {
			int entryY = y + HEADER_HEIGHT + index * ENTRY_HEIGHT;

			if (isInside(mouseX, mouseY, x, entryY, WIDTH, ENTRY_HEIGHT)) {
				return entries.get(index);
			}
		}

		return null;
	}

	public boolean mouseDragged(double mouseX, double mouseY) {
		if (!dragging) {
			return false;
		}

		int nextX = (int) Math.round(mouseX - dragOffsetX);
		int nextY = (int) Math.round(mouseY - dragOffsetY);

		if (nextX != x || nextY != y) {
			movedWhileDragging = true;
		}

		x = nextX;
		y = nextY;
		return true;
	}

	public void mouseReleased() {
		if (!dragging) {
			return;
		}

		if (!movedWhileDragging) {
			open = !open;
		}

		dragging = false;
	}

	private void updateAnimation() {
		float target = open ? 1.0F : 0.0F;
		animation += (target - animation) * 0.28F;

		if (Math.abs(target - animation) < 0.02F) {
			animation = target;
		}
	}

	private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
