package me.madeq.client.gui.menu;

import java.awt.Color;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import me.madeq.client.LiteClient;
import me.madeq.client.alt.AltProfile;
import me.madeq.client.utils.PlayerHeadUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AltManagerScreen extends Screen {
	private static final int ROW_HEIGHT = 52;
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
	private final Screen parent;
	private final MenuTextField nameInput = new MenuTextField("offline nickname");
	private int selectedIndex = -1;
	private int scrollOffset;

	public AltManagerScreen(Screen parent) {
		super(Component.literal("LiteClient Alt Manager"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		nameInput.setBounds(getSideX() + 18, getSideY() + 82, getSideWidth() - 36, 28);
		List<AltProfile> profiles = LiteClient.getAltManager().getProfiles();
		if (selectedIndex >= profiles.size()) {
			selectedIndex = profiles.isEmpty() ? -1 : profiles.size() - 1;
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		MenuTheme.renderBackground(graphics, width, height);
		renderHeader(graphics);
		renderProfiles(graphics, mouseX, mouseY);
		renderSidePanel(graphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return super.mouseClicked(mouseX, mouseY, button);
		}

		if (nameInput.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		int clickedProfile = getClickedProfileIndex(mouseX, mouseY);
		if (clickedProfile >= 0) {
			if (selectedIndex == clickedProfile && hasShiftDown()) {
				loginSelected();
				return true;
			}

			selectedIndex = clickedProfile;
			nameInput.setFocused(false);
			return true;
		}

		if (clickAction(mouseX, mouseY, getActionX(0), getActionY(0), getActionWidth(), 28, this::addOffline)) {
			return true;
		}

		if (hasSelectedProfile() && clickAction(mouseX, mouseY, getActionX(1), getActionY(1), getActionWidth(), 28, this::loginSelected)) {
			return true;
		}

		if (hasSelectedProfile() && clickAction(mouseX, mouseY, getActionX(2), getActionY(2), getActionWidth(), 28, this::removeSelected)) {
			return true;
		}

		if (clickAction(mouseX, mouseY, getActionX(3), getActionY(3), getActionWidth(), 28, this::startMicrosoftLogin)) {
			return true;
		}

		if (clickAction(mouseX, mouseY, getActionX(4), getActionY(4), getActionWidth(), 28, () -> minecraft.setScreen(parent))) {
			return true;
		}

		nameInput.setFocused(false);
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!isInside(mouseX, mouseY, getListX(), getListY(), getListWidth(), getListHeight())) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}

		int maxScroll = Math.max(0, LiteClient.getAltManager().getProfiles().size() - getVisibleRows());
		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(scrollY)));
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			minecraft.setScreen(parent);
			return true;
		}

		if (nameInput.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (nameInput.charTyped(codePoint)) {
			return true;
		}

		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void renderHeader(GuiGraphics graphics) {
		int x = getListX();
		int y = Math.max(16, getListY() - 46);
		graphics.drawString(font, "Alt Manager", x, y, MenuTheme.TEXT, true);
		graphics.drawString(font, "Manage your saved profiles and switch between them", x, y + 16, MenuTheme.MUTED_TEXT, true);
	}

	private void renderProfiles(GuiGraphics graphics, int mouseX, int mouseY) {
		int x = getListX();
		int y = getListY();
		int listWidth = getListWidth();
		int listHeight = getListHeight();
		List<AltProfile> profiles = LiteClient.getAltManager().getProfiles();
		MenuTheme.panel(graphics, x, y, listWidth, listHeight);

		if (profiles.isEmpty()) {
			MenuTheme.centered(graphics, font, "No profiles saved", x, y + listHeight / 2 - 8, listWidth, MenuTheme.MUTED_TEXT);
			return;
		}

		for (int index = 0; index < getVisibleRows(); index++) {
			int profileIndex = index + scrollOffset;
			if (profileIndex >= profiles.size()) {
				break;
			}

			AltProfile profile = profiles.get(profileIndex);
			int rowY = y + 12 + index * ROW_HEIGHT;
			boolean hovered = isInside(mouseX, mouseY, x + 14, rowY, listWidth - 28, ROW_HEIGHT - 8);
			boolean selected = selectedIndex == profileIndex;
			MenuTheme.card(graphics, x + 14, rowY, listWidth - 28, ROW_HEIGHT - 8, hovered, selected);
			renderProfileRow(graphics, profile, x + 28, rowY + 10, listWidth - 56);
		}
	}

	private void renderProfileRow(GuiGraphics graphics, AltProfile profile, int x, int y, int width) {
		int avatarY = y - 4;
		int avatarSize = 32;
		graphics.fill(x, avatarY, x + avatarSize, avatarY + avatarSize, new Color(18, 28, 43, 238).getRGB());
		PlayerHeadUtil.draw(graphics, minecraft, profile, x, avatarY, avatarSize);
		
		int textX = x + avatarSize + 12;
		graphics.drawString(font, trimToWidth(profile.getName(), width - avatarSize - 70), textX, y, MenuTheme.TEXT, true);
		graphics.drawString(font, profile.getType().name().toLowerCase(), textX, y + 14, MenuTheme.MUTED_TEXT, true);
		graphics.drawString(font, "Last used " + TIME_FORMAT.format(Instant.ofEpochMilli(profile.getLastUsed())), x + width - 88, y + 7, MenuTheme.SOFT_TEXT, true);
	}

	private void renderSidePanel(GuiGraphics graphics, int mouseX, int mouseY) {
		int x = getSideX();
		int y = getSideY();
		int sideWidth = getSideWidth();
		MenuTheme.panel(graphics, x, y, sideWidth, getSideHeight());
		graphics.drawString(font, "Profile Login", x + 18, y + 18, MenuTheme.TEXT, true);
		graphics.drawString(font, trimToWidth(LiteClient.getAltManager().getStatus(), sideWidth - 36), x + 18, y + 36, MenuTheme.SOFT_TEXT, true);
		nameInput.render(graphics, font, mouseX, mouseY);

		renderSmallButton(graphics, getActionX(0), getActionY(0), getActionWidth(), 28, "Add Offline", mouseX, mouseY, true);
		renderSmallButton(graphics, getActionX(1), getActionY(1), getActionWidth(), 28, "Login", mouseX, mouseY, hasSelectedProfile());
		renderSmallButton(graphics, getActionX(2), getActionY(2), getActionWidth(), 28, "Remove", mouseX, mouseY, hasSelectedProfile());
		renderSmallButton(graphics, getActionX(3), getActionY(3), getActionWidth(), 28, "Microsoft", mouseX, mouseY, !LiteClient.getAltManager().isMicrosoftLoginInProgress());
		renderSmallButton(graphics, getActionX(4), getActionY(4), getActionWidth(), 28, "Back", mouseX, mouseY, true);

	}

	private void renderSmallButton(GuiGraphics graphics, int x, int y, int width, int height, String label, int mouseX, int mouseY, boolean enabled) {
		boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, height);
		int background = !enabled ? new Color(22, 25, 31, 150).getRGB() : hovered ? new Color(38, 121, 255, 235).getRGB() : new Color(18, 28, 43, 225).getRGB();
		graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, new Color(3, 5, 9, 150).getRGB());
		graphics.fill(x, y, x + width, y + height, background);
		graphics.fill(x, y + height - 1, x + width, y + height, enabled ? MenuTheme.ACCENT : new Color(70, 78, 90).getRGB());
		MenuTheme.centered(graphics, font, label, x, y + 10, width, enabled ? MenuTheme.TEXT : MenuTheme.MUTED_TEXT);
	}

	private void addOffline() {
		LiteClient.getAltManager().addOfflineProfile(nameInput.getValue());
		nameInput.setValue("");
		selectedIndex = LiteClient.getAltManager().getProfiles().size() - 1;
	}

	private void loginSelected() {
		if (hasSelectedProfile()) {
			LiteClient.getAltManager().login(LiteClient.getAltManager().getProfiles().get(selectedIndex));
		}
	}

	private void removeSelected() {
		if (!hasSelectedProfile()) {
			return;
		}

		AltProfile profile = LiteClient.getAltManager().getProfiles().get(selectedIndex);
		LiteClient.getAltManager().remove(profile);
		PlayerHeadUtil.invalidate(profile);
		selectedIndex = Math.min(selectedIndex, LiteClient.getAltManager().getProfiles().size() - 1);
	}

	private void startMicrosoftLogin() {
		LiteClient.getAltManager().startMicrosoftLogin(() -> selectedIndex = LiteClient.getAltManager().getProfiles().size() - 1);
	}

	private int getClickedProfileIndex(double mouseX, double mouseY) {
		for (int index = 0; index < getVisibleRows(); index++) {
			int profileIndex = index + scrollOffset;
			int rowY = getListY() + 12 + index * ROW_HEIGHT;

			if (profileIndex < LiteClient.getAltManager().getProfiles().size() && isInside(mouseX, mouseY, getListX() + 14, rowY, getListWidth() - 28, ROW_HEIGHT - 8)) {
				return profileIndex;
			}
		}

		return -1;
	}

	private boolean clickAction(double mouseX, double mouseY, int x, int y, int width, int height, Runnable action) {
		if (!isInside(mouseX, mouseY, x, y, width, height)) {
			return false;
		}

		action.run();
		return true;
	}

	private boolean hasSelectedProfile() {
		return selectedIndex >= 0 && selectedIndex < LiteClient.getAltManager().getProfiles().size();
	}

	private String trimToWidth(String value, int maxWidth) {
		if (value == null || font.width(value) <= maxWidth) {
			return value == null ? "" : value;
		}

		String trimmed = value;
		while (!trimmed.isEmpty() && font.width(trimmed + "...") > maxWidth) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}

		return trimmed + "...";
	}

	private int getListX() {
		return Math.max(18, width / 14);
	}

	private int getListY() {
		if (isCompactLayout()) {
			return getSideY() + getSideHeight() + 14;
		}

		return Math.max(68, height / 8);
	}

	private int getListWidth() {
		if (isCompactLayout()) {
			return width - 36;
		}

		return Math.max(360, width - getListX() - getSideWidth() - 54);
	}

	private int getListHeight() {
		if (isCompactLayout()) {
			return Math.max(120, height - getListY() - 20);
		}

		return height - getListY() - 28;
	}

	private int getSideWidth() {
		if (isCompactLayout()) {
			return width - 36;
		}

		return Math.max(230, Math.min(292, width / 4));
	}

	private int getSideX() {
		if (isCompactLayout()) {
			return getListX();
		}

		return width - getSideWidth() - 26;
	}

	private int getSideY() {
		if (isCompactLayout()) {
			return Math.max(56, height / 8);
		}

		return getListY();
	}

	private int getSideHeight() {
		if (isCompactLayout()) {
			return 172;
		}

		return Math.min(330, height - getSideY() - 28);
	}

	private int getVisibleRows() {
		return Math.max(1, (getListHeight() - 24) / ROW_HEIGHT);
	}

	private int getActionWidth() {
		if (!isCompactLayout()) {
			return getSideWidth() - 36;
		}

		return (getSideWidth() - 48) / 2;
	}

	private int getActionX(int index) {
		if (!isCompactLayout()) {
			return getSideX() + 18;
		}

		return getSideX() + 18 + (index % 2) * (getActionWidth() + 12);
	}

	private int getActionY(int index) {
		if (!isCompactLayout()) {
			return getSideY() + 126 + index * 38;
		}

		return getSideY() + 118 + (index / 2) * 34;
	}

	private boolean isCompactLayout() {
		return width < 820 || height < 430;
	}

	private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
