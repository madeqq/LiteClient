package me.madeq.client.notify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.madeq.client.gui.menu.MenuTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class NotificationManager {
	private static final int MAX_NOTIFICATIONS = 5;
	private static final int MARGIN_X = 12;
	private static final int MARGIN_BOTTOM = 16;
	private static final int SPACING = 8;
	private static final int PANEL_WIDTH = 248;
	private static final int BADGE_SIZE = 22;
	private static final int SLIDE_IN_MS = 280;
	private static final int SLIDE_OUT_MS = 320;
	private static final int DEFAULT_DURATION_MS = 3600;
	private static final int PROGRESS_BAR_HEIGHT = 2;

	private final List<ActiveNotification> notifications = new ArrayList<>();

	public void show(NotificationType type, String title, String message) {
		show(type, title, message, DEFAULT_DURATION_MS);
	}

	public void show(NotificationType type, String title, String message, int durationMs) {
		Minecraft minecraft = Minecraft.getInstance();
		Runnable action = () -> addNotification(type, title, message, durationMs);

		if (minecraft != null) {
			minecraft.execute(action);
		} else {
			action.run();
		}
	}

	public void render(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || notifications.isEmpty()) {
			return;
		}

		long now = System.currentTimeMillis();
		Font font = minecraft.font;
		List<RenderEntry> entries = new ArrayList<>();

		Iterator<ActiveNotification> iterator = notifications.iterator();
		while (iterator.hasNext()) {
			ActiveNotification notification = iterator.next();
			if (notification.isExpired(now)) {
				iterator.remove();
				continue;
			}

			entries.add(new RenderEntry(notification, measureHeight(font, notification)));
		}

		if (entries.isEmpty()) {
			return;
		}

		int totalHeight = entries.stream().mapToInt(entry -> entry.height + SPACING).sum() - SPACING;
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		int targetX = screenWidth - MARGIN_X - PANEL_WIDTH;
		int y = screenHeight - MARGIN_BOTTOM - totalHeight;

		for (RenderEntry entry : entries) {
			renderNotification(graphics, font, entry.notification, targetX, y, entry.height, now);
			y += entry.height + SPACING;
		}
	}

	private void addNotification(NotificationType type, String title, String message, int durationMs) {
		notifications.add(0, new ActiveNotification(type, title, message, System.currentTimeMillis(), durationMs));

		while (notifications.size() > MAX_NOTIFICATIONS) {
			notifications.remove(notifications.size() - 1);
		}
	}

	private int measureHeight(Font font, ActiveNotification notification) {
		List<FormattedCharSequence> lines = font.split(Component.literal(notification.message()), PANEL_WIDTH - 54);
		int contentHeight = Math.max(14, lines.size() * 10);
		return 36 + contentHeight + PROGRESS_BAR_HEIGHT + 4;
	}

	private void renderNotification(
			GuiGraphics graphics,
			Font font,
			ActiveNotification notification,
			int targetX,
			int y,
			int height,
			long now
	) {
		float slideIn = notification.getSlideInProgress(now, SLIDE_IN_MS);
		float slideOut = notification.getSlideOutProgress(now, SLIDE_OUT_MS);
		float slideProgress = slideIn * (1.0F - slideOut);
		int x = targetX + Math.round((1.0F - easeOutCubic(slideProgress)) * (PANEL_WIDTH + 24));
		int alpha = Math.round(255 * slideProgress);

		List<FormattedCharSequence> lines = font.split(Component.literal(notification.message()), PANEL_WIDTH - 54);

		int background = withAlpha(new Color(12, 16, 24), alpha * 0.92F).getRGB();
		int border = withAlpha(new Color(255, 255, 255), alpha * 0.18F).getRGB();
		int accent = withAlpha(getAccentColor(notification.type()), alpha).getRGB();
		int titleColor = withAlpha(new Color(242, 247, 255), alpha).getRGB();
		int messageColor = withAlpha(new Color(198, 210, 228), alpha).getRGB();
		int badgeColor = withAlpha(getAccentColor(notification.type()), alpha * 0.22F).getRGB();

		graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + height + 1, withAlpha(new Color(2, 4, 8), alpha * 0.55F).getRGB());
		graphics.fill(x, y, x + PANEL_WIDTH, y + height, background);
		graphics.fill(x + PANEL_WIDTH - 3, y, x + PANEL_WIDTH, y + height, accent);
		graphics.fill(x, y, x + PANEL_WIDTH, y + 1, border);

		int badgeX = x + 12;
		int badgeY = y + 10;
		graphics.fill(badgeX, badgeY, badgeX + BADGE_SIZE, badgeY + BADGE_SIZE, badgeColor);
		graphics.fill(badgeX + 1, badgeY + 1, badgeX + BADGE_SIZE - 1, badgeY + BADGE_SIZE - 1, withAlpha(new Color(12, 16, 24), alpha * 0.65F).getRGB());
		drawBadgeGlyph(graphics, font, notification.type(), badgeX, badgeY, accent);

		graphics.drawString(font, notification.title(), x + 42, y + 11, titleColor, true);
		graphics.drawString(font, notification.type().label(), x + 42, y + 22, withAlpha(getAccentColor(notification.type()), alpha).getRGB(), true);

		int messageY = y + 38;
		for (FormattedCharSequence line : lines) {
			graphics.drawString(font, line, x + 14, messageY, messageColor, true);
			messageY += 10;
			if (messageY > y + height - PROGRESS_BAR_HEIGHT - 8) {
				break;
			}
		}

		renderDurationBar(graphics, notification, x, y, height, now, alpha, accent);
	}

	private void renderDurationBar(
			GuiGraphics graphics,
			ActiveNotification notification,
			int x,
			int y,
			int height,
			long now,
			int alpha,
			int accent
	) {
		float timeProgress = notification.getRemainingProgress(now, SLIDE_IN_MS);
		if (timeProgress <= 0.0F) {
			return;
		}

		int barX = x + 6;
		int barY = y + height - PROGRESS_BAR_HEIGHT - 3;
		int barWidth = PANEL_WIDTH - 12;
		int fillWidth = Math.max(1, Math.round(barWidth * timeProgress));
		int trackColor = withAlpha(new Color(255, 255, 255), alpha * 0.12F).getRGB();
		int fillColor = withAlpha(getAccentColor(notification.type()), alpha * (0.45F + 0.55F * timeProgress)).getRGB();

		graphics.fill(barX, barY, barX + barWidth, barY + PROGRESS_BAR_HEIGHT, trackColor);
		graphics.fill(barX, barY, barX + fillWidth, barY + PROGRESS_BAR_HEIGHT, fillColor);
	}

	private void drawBadgeGlyph(GuiGraphics graphics, Font font, NotificationType type, int badgeX, int badgeY, int color) {
		String glyph = switch (type) {
			case SUCCESS -> "+";
			case INFO -> "i";
			case WARNING -> "!";
			case ERROR -> "x";
		};

		int glyphWidth = font.width(glyph);
		int textX = badgeX + (BADGE_SIZE - glyphWidth) / 2;
		int textY = badgeY + (BADGE_SIZE - 8) / 2;
		graphics.drawString(font, glyph, textX, textY, color, true);
	}

	private Color getAccentColor(NotificationType type) {
		return switch (type) {
			case SUCCESS -> new Color(MenuTheme.SUCCESS);
			case INFO -> new Color(MenuTheme.ACCENT);
			case WARNING -> new Color(MenuTheme.WARNING);
			case ERROR -> new Color(MenuTheme.DANGER);
		};
	}

	private static float easeOutCubic(float value) {
		float clamped = Math.max(0.0F, Math.min(1.0F, value));
		return 1.0F - (float) Math.pow(1.0F - clamped, 3.0);
	}

	private static Color withAlpha(Color color, float alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, Math.round(alpha))));
	}

	private record RenderEntry(ActiveNotification notification, int height) {
	}

	private static final class ActiveNotification {
		private final NotificationType type;
		private final String title;
		private final String message;
		private final long createdAt;
		private final long durationMs;

		private ActiveNotification(NotificationType type, String title, String message, long createdAt, int durationMs) {
			this.type = type;
			this.title = title == null ? "" : title;
			this.message = message == null ? "" : message;
			this.createdAt = createdAt;
			this.durationMs = durationMs;
		}

		private NotificationType type() {
			return type;
		}

		private String title() {
			return title;
		}

		private String message() {
			return message;
		}

		private float getSlideInProgress(long now, int slideInMs) {
			return Math.min(1.0F, (now - createdAt) / (float) slideInMs);
		}

		private float getSlideOutProgress(long now, int slideOutMs) {
			long hideStart = createdAt + durationMs;
			if (now <= hideStart) {
				return 0.0F;
			}

			return Math.min(1.0F, (now - hideStart) / (float) slideOutMs);
		}

		private float getRemainingProgress(long now, int slideInMs) {
			long visibleStart = createdAt + slideInMs;
			long visibleEnd = createdAt + durationMs;

			if (now < visibleStart) {
				return 1.0F;
			}

			if (now >= visibleEnd) {
				return 0.0F;
			}

			return 1.0F - (now - visibleStart) / (float) (visibleEnd - visibleStart);
		}

		private boolean isExpired(long now) {
			return now > createdAt + durationMs + SLIDE_OUT_MS;
		}
	}
}
