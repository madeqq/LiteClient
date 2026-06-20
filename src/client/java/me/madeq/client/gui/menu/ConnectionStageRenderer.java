package me.madeq.client.gui.menu;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class ConnectionStageRenderer {
	private static final int ROW_HEIGHT = 34;

	private ConnectionStageRenderer() {
	}

	public static void render(GuiGraphics graphics, Font font, int width, int height, ConnectionProgress progress) {
		if (!progress.isActive()) {
			return;
		}

		MenuTheme.renderBackground(graphics, width, height);

		int panelWidth = Math.max(360, Math.min(440, width - 80));
		int panelHeight = 96 + ConnectionStage.values().length * ROW_HEIGHT + 36;
		int panelX = (width - panelWidth) / 2;
		int panelY = (height - panelHeight) / 2;

		MenuTheme.panel(graphics, panelX, panelY, panelWidth, panelHeight);
		graphics.fill(panelX + 2, panelY, panelX + panelWidth, panelY + 78, MenuTheme.PANEL_STRONG);

		String title = progress.getServerName().isBlank() ? "Connecting" : progress.getServerName();
		graphics.drawString(font, title, panelX + 22, panelY + 22, MenuTheme.TEXT, true);
		graphics.drawString(font, progress.getServerAddress(), panelX + 22, panelY + 40, MenuTheme.MUTED_TEXT, true);
		graphics.drawString(font, "Joining server", panelX + 22, panelY + 58, MenuTheme.SOFT_TEXT, true);

		int listX = panelX + 22;
		int listY = panelY + 92;
		int listWidth = panelWidth - 44;
		long time = System.currentTimeMillis();

		for (ConnectionStage stage : ConnectionStage.values()) {
			renderStageRow(graphics, font, listX, listY, listWidth, stage, progress.getState(stage), time);
			listY += ROW_HEIGHT;
		}
	}

	private static void renderStageRow(
			GuiGraphics graphics,
			Font font,
			int x,
			int y,
			int width,
			ConnectionStage stage,
			ConnectionProgress.StageState state,
			long time
	) {
		int indicatorX = x + 6;
		int indicatorY = y + 10;
		int textX = x + 28;
		int labelColor = switch (state) {
			case ACTIVE -> MenuTheme.TEXT;
			case COMPLETED -> MenuTheme.SUCCESS;
			case SKIPPED -> new Color(120, 130, 145).getRGB();
			case PENDING -> MenuTheme.MUTED_TEXT;
		};

		renderIndicator(graphics, indicatorX, indicatorY, state, time);
		graphics.drawString(font, stage.label(), textX, y + 4, labelColor, true);

		String statusText = switch (state) {
			case ACTIVE -> "In progress...";
			case COMPLETED -> "Done";
			case SKIPPED -> "Skipped";
			case PENDING -> "Waiting";
		};

		graphics.drawString(font, statusText, textX, y + 16, MenuTheme.MUTED_TEXT, true);

		if (state == ConnectionProgress.StageState.ACTIVE) {
			int barWidth = width - 28;
			int pulse = (int) ((Math.sin(time / 220.0) + 1.0) * 0.5 * (barWidth - 40));
			graphics.fill(textX, y + 28, textX + barWidth, y + 30, new Color(255, 255, 255, 18).getRGB());
			graphics.fill(textX, y + 28, textX + 40 + pulse, y + 30, MenuTheme.ACCENT);
		} else if (state == ConnectionProgress.StageState.COMPLETED) {
			graphics.fill(textX, y + 28, textX + width - 28, y + 30, new Color(87, 255, 173, 80).getRGB());
		}
	}

	private static void renderIndicator(
			GuiGraphics graphics,
			int x,
			int y,
			ConnectionProgress.StageState state,
			long time
	) {
		switch (state) {
			case COMPLETED -> {
				graphics.fill(x, y, x + 10, y + 10, new Color(87, 255, 173, 40).getRGB());
				graphics.fill(x + 2, y + 4, x + 8, y + 6, MenuTheme.SUCCESS);
			}
			case ACTIVE -> {
				float pulse = (float) ((Math.sin(time / 180.0) + 1.0) * 0.5);
				int size = 6 + Math.round(2.0F * pulse);
				int offset = (10 - size) / 2;
				graphics.fill(x + offset, y + offset, x + offset + size, y + offset + size, MenuTheme.ACCENT);
			}
			case SKIPPED -> graphics.fill(x + 3, y + 4, x + 7, y + 6, new Color(120, 130, 145).getRGB());
			case PENDING -> graphics.fill(x + 2, y + 2, x + 8, y + 8, new Color(255, 255, 255, 28).getRGB());
		}
	}
}
