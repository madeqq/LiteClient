package me.madeq.client.hud;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.madeq.client.LiteClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

public class HudManager {
	private static final int X = 8;
	private static final int Y = 8;
	private static final int PADDING = 7;
	private static final int HEADER_HEIGHT = 18;
	private static final int ROW_HEIGHT = 13;
	private static final int MIN_WIDTH = 156;

	private long lastPacketTimeMillis;

	public void markPacketReceived() {
		lastPacketTimeMillis = System.currentTimeMillis();
	}

	public void render(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.options.hideGui || minecraft.player == null) {
			return;
		}

		Font font = minecraft.font;
		List<HudLine> lines = List.of(
				new HudLine("IP", getServerIp(minecraft)),
				new HudLine("Engine", getEngine(minecraft)),
				new HudLine("Last Packet", getLastPacketText())
		);
		int width = getPanelWidth(font, lines);
		int height = HEADER_HEIGHT + PADDING + lines.size() * ROW_HEIGHT + PADDING;

		renderPanel(graphics, width, height);
		renderHeader(graphics, font, width);
		renderLines(graphics, font, lines, width);
	}

	private void renderPanel(GuiGraphics graphics, int width, int height) {
		graphics.fill(X - 1, Y - 1, X + width + 1, Y + height + 1, new Color(3, 4, 7, 160).getRGB());
		graphics.fill(X, Y, X + width, Y + height, new Color(12, 14, 20, 205).getRGB());
		graphics.fill(X, Y, X + 2, Y + height, new Color(72, 160, 255, 235).getRGB());
		graphics.fill(X + 2, Y, X + width, Y + 1, new Color(255, 255, 255, 24).getRGB());
		graphics.fill(X + 2, Y + HEADER_HEIGHT, X + width, Y + HEADER_HEIGHT + 1, new Color(72, 160, 255, 120).getRGB());
	}

	private void renderHeader(GuiGraphics graphics, Font font, int width) {
		graphics.fill(X + 2, Y, X + width, Y + HEADER_HEIGHT, new Color(18, 23, 33, 210).getRGB());
		graphics.drawString(font, "LiteClient " + LiteClient.VERSION, X + PADDING, Y + 5, new Color(238, 247, 255).getRGB(), true);
	}

	private void renderLines(GuiGraphics graphics, Font font, List<HudLine> lines, int width) {
		int labelColor = new Color(143, 153, 168).getRGB();
		int valueColor = new Color(235, 239, 246).getRGB();
		int valueX = X + width - PADDING;

		for (int index = 0; index < lines.size(); index++) {
			HudLine line = lines.get(index);
			int rowY = Y + HEADER_HEIGHT + PADDING + index * ROW_HEIGHT;

			graphics.drawString(font, line.label(), X + PADDING, rowY, labelColor, true);
			graphics.drawString(font, line.value(), valueX - font.width(line.value()), rowY, valueColor, true);
		}
	}

	private int getPanelWidth(Font font, List<HudLine> lines) {
		int width = MIN_WIDTH;

		for (HudLine line : lines) {
			width = Math.max(width, font.width(line.label()) + font.width(line.value()) + PADDING * 3 + 18);
		}

		return width;
	}

	private String getServerIp(Minecraft minecraft) {
		if (minecraft.isLocalServer()) {
			return "Singleplayer";
		}

		ServerData serverData = minecraft.getCurrentServer();
		if (serverData == null || serverData.ip.isBlank()) {
			return "Unknown";
		}

		return serverData.ip;
	}

	private String getEngine(Minecraft minecraft) {
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null || connection.serverBrand() == null || Objects.requireNonNull(connection.serverBrand()).isBlank()) {
			return "Unknown";
		}

		return connection.serverBrand();
	}

	private String getLastPacketText() {
		if (lastPacketTimeMillis == 0L) {
			return "never";
		}

		double seconds = getPacketAgeSeconds();
		return String.format(Locale.ROOT, "%.1f", seconds) + " sec";
	}

	private double getPacketAgeSeconds() {
		if (lastPacketTimeMillis == 0L) {
			return Double.MAX_VALUE;
		}

		return (System.currentTimeMillis() - lastPacketTimeMillis) / 1000.0D;
	}

	private record HudLine(String label, String value) {
	}
}
