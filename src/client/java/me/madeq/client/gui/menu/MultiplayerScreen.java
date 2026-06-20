package me.madeq.client.gui.menu;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import me.madeq.client.utils.ServerPingUtil;
import me.madeq.client.utils.ServerPingUtil.PingResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MultiplayerScreen extends Screen {
    private static final int ROW_HEIGHT = 66;
    private static final boolean VIA_LOADED = FabricLoader.getInstance().isModLoaded("viafabricplus");
    private final Screen parent;
    private final MenuTextField directAddress = new MenuTextField("server address");
    private final List<ServerData> servers = new ArrayList<>();
    private final Map<String, ResourceLocation> serverIcons = new HashMap<>();
    private final AtomicInteger pingGeneration = new AtomicInteger();
    private ServerList serverList;
    private int selectedIndex = -1;
    private int scrollOffset;
    private final long ignoreClicksUntil;

    public MultiplayerScreen(Screen parent) {
        this(parent, false);
    }

    public MultiplayerScreen(Screen parent, boolean ignoreInitialClick) {
        super(Component.literal("LiteClient Multiplayer"));
        this.parent = parent;
        this.ignoreClicksUntil = ignoreInitialClick ? System.currentTimeMillis() + 750L : 0L;
    }

    @Override
    protected void init() {
        serverList = new ServerList(minecraft);
        reloadServers();
        directAddress.setBounds(getSideX() + 18, getSideY() + getDirectInputY(), getSideWidth() - 36, 28);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        MenuTheme.renderBackground(graphics, width, height);
        renderHeader(graphics);
        renderServerList(graphics, mouseX, mouseY);
        renderSidePanel(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (System.currentTimeMillis() < ignoreClicksUntil) {
            return true;
        }

        if (button != 0) {
            return safeSuperMouseClicked(mouseX, mouseY, button);
        }

        if (directAddress.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int clickedIndex = getClickedServerIndex(mouseX, mouseY);
        if (clickedIndex >= 0) {
            if (selectedIndex == clickedIndex && hasShiftDown()) {
                connectSelected();
                return true;
            }

            selectedIndex = clickedIndex;
            directAddress.setFocused(false);
            return true;
        }

        if (clickAction(mouseX, mouseY, getActionX(0), getActionY(0), getActionWidth(), 28, this::connectDirect)) {
            return true;
        }

        if (clickAction(mouseX, mouseY, getActionX(1), getActionY(1), getActionWidth(), 28, this::saveDirect)) {
            return true;
        }

        if (hasSelectedServer() && clickAction(mouseX, mouseY, getActionX(2), getActionY(2), getActionWidth(), 28, this::connectSelected)) {
            return true;
        }

        if (hasSelectedServer() && clickAction(mouseX, mouseY, getActionX(3), getActionY(3), getActionWidth(), 28, this::removeSelected)) {
            return true;
        }

        if (clickAction(mouseX, mouseY, getActionX(4), getActionY(4), getActionWidth(), 28, this::reloadServers)) {
            return true;
        }

        if (clickAction(mouseX, mouseY, getActionX(5), getActionY(5), getActionWidth(), 28, () -> minecraft.setScreen(parent))) {
            return true;
        }

        if (VIA_LOADED && clickAction(mouseX, mouseY, getActionX(6), getActionY(6), getActionWidth(), 28, this::openViaVersionScreen)) {
            return true;
        }

        directAddress.setFocused(false);
        return safeSuperMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isInside(mouseX, mouseY, getListX(), getListY(), getListWidth(), getListHeight())) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int maxScroll = Math.max(0, servers.size() - getVisibleRows());
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(scrollY)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            minecraft.setScreen(parent);
            return true;
        }

        if (directAddress.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (directAddress.charTyped(codePoint)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        pingGeneration.incrementAndGet();
    }

    private void renderHeader(GuiGraphics graphics) {
        int x = getListX();
        int y = Math.max(16, getListY() - 46);
        graphics.drawString(font, "Multiplayer", x, y, MenuTheme.TEXT, true);
        graphics.drawString(font, "Select a saved server or connect directly", x, y + 16, MenuTheme.MUTED_TEXT, true);
    }

    private void renderServerList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = getListX();
        int y = getListY();
        int listWidth = getListWidth();
        int listHeight = getListHeight();
        MenuTheme.panel(graphics, x, y, listWidth, listHeight);

        if (servers.isEmpty()) {
            MenuTheme.centered(graphics, font, "No saved servers yet", x, y + listHeight / 2 - 8, listWidth, MenuTheme.MUTED_TEXT);
            return;
        }

        int visibleRows = getVisibleRows();
        for (int index = 0; index < visibleRows; index++) {
            int serverIndex = index + scrollOffset;
            if (serverIndex >= servers.size()) {
                break;
            }

            ServerData server = servers.get(serverIndex);
            int rowY = y + 12 + index * ROW_HEIGHT;
            boolean hovered = isInside(mouseX, mouseY, x + 14, rowY, listWidth - 28, ROW_HEIGHT - 8);
            boolean selected = selectedIndex == serverIndex;
            MenuTheme.card(graphics, x + 14, rowY, listWidth - 28, ROW_HEIGHT - 8, hovered, selected);
            renderServerRow(graphics, server, x + 28, rowY + 9, listWidth - 56, selected);
        }
    }

    private void renderServerRow(GuiGraphics graphics, ServerData server, int x, int y, int width, boolean selected) {
        String name = server.name.isBlank() ? "Unnamed Server" : server.name;
        String ping = getPingText(server);
        String playerCount = getPlayerCountText(server);
        String motd = server.motd.getString();
        int iconSize = 38;
        int textX = x + iconSize + 12;
        int rightColumnWidth = Math.max(font.width(ping), playerCount.isBlank() ? 0 : font.width(playerCount));
        int textWidth = width - iconSize - 24 - rightColumnWidth - 8;

        renderServerIcon(graphics, server, x, y, iconSize);
        graphics.drawString(font, trimToWidth(name, textWidth), textX, y, MenuTheme.TEXT, true);
        if(motd.contains("\n")){
            String[] motdArray = motd.split("\n");
            graphics.drawString(font, trimToWidth(motdArray[0], textWidth), textX, y + 15, MenuTheme.SOFT_TEXT, true);
            graphics.drawString(font, trimToWidth(motdArray[1], textWidth), textX, y + 30, MenuTheme.SOFT_TEXT, true);

        }else {
            graphics.drawString(font, trimToWidth(motd, textWidth), textX, y + 15, MenuTheme.SOFT_TEXT, true);
        }
        graphics.drawString(font, ping, x + width - font.width(ping), y + 2, getPingColor(server), true);
        if (!playerCount.isBlank()) {
            graphics.drawString(font, playerCount, x + width - font.width(playerCount), y + 15, MenuTheme.SOFT_TEXT, true);
        }
    }

    private void renderSidePanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = getSideX();
        int y = getSideY();
        int sideWidth = getSideWidth();
        MenuTheme.panel(graphics, x, y, sideWidth, getSideHeight());
        graphics.drawString(font, "Direct Connect", x + 18, y + 20, MenuTheme.TEXT, true);
        graphics.drawString(font, "Paste an address, connect, or save it", x + 18, y + 38, MenuTheme.MUTED_TEXT, true);
        directAddress.render(graphics, font, mouseX, mouseY);

        renderSmallButton(graphics, getActionX(0), getActionY(0), getActionWidth(), 28, "Connect", mouseX, mouseY, true);
        renderSmallButton(graphics, getActionX(1), getActionY(1), getActionWidth(), 28, "Save", mouseX, mouseY, true);
        renderActionSeparator(graphics);
        graphics.drawString(font, "Server Actions", x + 18, getServerActionsLabelY(), MenuTheme.MUTED_TEXT, true);
        renderSmallButton(graphics, getActionX(2), getActionY(2), getActionWidth(), 28, "Join", mouseX, mouseY, hasSelectedServer());
        renderSmallButton(graphics, getActionX(3), getActionY(3), getActionWidth(), 28, "Remove", mouseX, mouseY, hasSelectedServer());
        renderSmallButton(graphics, getActionX(4), getActionY(4), getActionWidth(), 28, "Refresh", mouseX, mouseY, true);
        renderSmallButton(graphics, getActionX(5), getActionY(5), getActionWidth(), 28, "Back", mouseX, mouseY, true);
        if (VIA_LOADED) {
            renderSmallButton(graphics, getActionX(6), getActionY(6), getActionWidth(), 28, "Version", mouseX, mouseY, true);
        }
    }

    private void renderActionSeparator(GuiGraphics graphics) {
        int x = getSideX() + 18;
        int y = getActionSeparatorY();
        int width = getSideWidth() - 36;
        graphics.fill(x, y, x + width, y + 1, new Color(255, 255, 255, 28).getRGB());
        graphics.fill(x, y + 1, x + width, y + 2, new Color(0, 0, 0, 70).getRGB());
    }

    private void renderSmallButton(GuiGraphics graphics, int x, int y, int width, int height, String label, int mouseX, int mouseY, boolean enabled) {
        boolean hovered = enabled && isInside(mouseX, mouseY, x, y, width, height);
        int background = !enabled ? new Color(22, 25, 31, 150).getRGB() : hovered ? new Color(38, 121, 255, 235).getRGB() : new Color(18, 28, 43, 225).getRGB();
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, new Color(3, 5, 9, 150).getRGB());
        graphics.fill(x, y, x + width, y + height, background);
        graphics.fill(x, y + height - 1, x + width, y + height, enabled ? MenuTheme.ACCENT : new Color(70, 78, 90).getRGB());
        MenuTheme.centered(graphics, font, label, x, y + 11, width, enabled ? MenuTheme.TEXT : MenuTheme.MUTED_TEXT);
    }

    private boolean clickAction(double mouseX, double mouseY, int x, int y, int width, int height, Runnable action) {
        if (!isInside(mouseX, mouseY, x, y, width, height)) {
            return false;
        }

        try {
            action.run();
        } catch (RuntimeException exception) {
            return true;
        }

        return true;
    }

    private boolean safeSuperMouseClicked(double mouseX, double mouseY, int button) {
        try {
            return super.mouseClicked(mouseX, mouseY, button);
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private void reloadServers() {
        servers.clear();
        serverList.load();

        for (int index = 0; index < serverList.size(); index++) {
            servers.add(serverList.get(index));
        }

        if (selectedIndex >= servers.size()) {
            selectedIndex = servers.isEmpty() ? -1 : servers.size() - 1;
        }

        scrollOffset = Math.min(scrollOffset, Math.max(0, servers.size() - getVisibleRows()));
        pingServers();
    }

    private void connectDirect() {
        String address = directAddress.getValue();
        if (address.isBlank()) {
            return;
        }

        ServerData server = new ServerData(address, address, ServerData.Type.OTHER);
        connect(server);
    }

    private void saveDirect() {
        String address = directAddress.getValue();
        if (address.isBlank()) {
            return;
        }

        ServerData server = new ServerData(address, address, ServerData.Type.OTHER);
        serverList.add(server, false);
        serverList.save();
        reloadServers();
        selectedIndex = servers.size() - 1;
    }

    private void connectSelected() {
        if (!hasSelectedServer()) {
            return;
        }

        connect(servers.get(selectedIndex));
    }

    private void removeSelected() {
        if (!hasSelectedServer()) {
            return;
        }

        serverList.remove(servers.get(selectedIndex));
        serverList.save();
        reloadServers();
    }

    private void openViaVersionScreen() {
        try {
            Class<?> screenClass = Class.forName("com.viaversion.viafabricplus.screen.impl.ProtocolSelectionScreen");
            Object instance = screenClass.getField("INSTANCE").get(null);
            Method open = screenClass.getMethod("open", Screen.class);
            open.invoke(instance, this);
        } catch (Exception ignored) {
        }
    }

    private void connect(ServerData server) {
        ServerAddress address = ServerAddress.parseString(server.ip);
        minecraft.execute(() -> {
            try {
                ConnectScreen.startConnecting(this, minecraft, address, server, false, null);
            } catch (RuntimeException exception) {
                minecraft.setScreen(this);
            }
        });
    }

    private void pingServers() {
        int generation = pingGeneration.incrementAndGet();

        for (ServerData server : servers) {
            ServerPingUtil.prepareForPing(server);
            String serverIp = server.ip;
            ServerPingUtil.pingAsync(serverIp).whenComplete((result, error) -> {
                PingResult pingResult = error == null ? result : PingResult.offline();
                if (minecraft == null) {
                    return;
                }

                minecraft.execute(() -> applyPingResult(serverIp, pingResult, generation));
            });
        }
    }

    private void applyPingResult(String serverIp, PingResult result, int generation) {
        if (generation != pingGeneration.get() || minecraft == null || minecraft.screen != this) {
            return;
        }

        ServerData server = findServer(serverIp);
        if (server == null) {
            return;
        }

        try {
            ServerPingUtil.applyToServerData(server, result);
        } catch (RuntimeException ignored) {
            server.setState(ServerData.State.UNREACHABLE);
            server.ping = -1L;
            server.players = null;
        }
    }

    private ServerData findServer(String serverIp) {
        for (ServerData server : servers) {
            if (serverIp.equals(server.ip)) {
                return server;
            }
        }

        return null;
    }

    private void renderServerIcon(GuiGraphics graphics, ServerData server, int x, int y, int size) {
        ResourceLocation icon = getServerIcon(server);

        if (icon != null) {
            graphics.blit(RenderType::guiTextured, icon, x, y, 0.0F, 0.0F, size, size, size, size);
            return;
        }

        graphics.fill(x, y, x + size, y + size, new Color(18, 28, 43, 238).getRGB());
        graphics.fill(x, y, x + size, y + 1, new Color(255, 255, 255, 40).getRGB());
        graphics.fill(x, y, x + 2, y + size, MenuTheme.ACCENT);
        String letter = getServerLetter(server);
        MenuTheme.centered(graphics, font, letter, x, y + size / 2 - 4, size, MenuTheme.TEXT);
    }

    private ResourceLocation getServerIcon(ServerData server) {
        byte[] iconBytes = server.getIconBytes();
        if (iconBytes == null || iconBytes.length == 0) {
            return null;
        }

        String key = server.ip + ":" + iconBytes.length;
        ResourceLocation cached = serverIcons.get(key);
        if (cached != null) {
            return cached;
        }

        try {
            NativeImage image = NativeImage.read(iconBytes);
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath("liteclient", "server_icons/" + Integer.toHexString(key.hashCode()));
            minecraft.getTextureManager().register(location, new DynamicTexture(image));
            serverIcons.put(key, location);
            return location;
        } catch (IOException exception) {
            return null;
        }
    }

    private String getServerLetter(ServerData server) {
        String name = server.name == null || server.name.isBlank() ? server.ip : server.name;
        return name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();
    }

    private String getPlayerCountText(ServerData server) {
        if (server.players == null || server.state() != ServerData.State.INITIAL) {
            return "";
        }

        return server.players.online() + "/" + server.players.max();
    }

    private String getPingText(ServerData server) {
        if (server.ping > 0L) {
            return server.ping + " ms";
        }

        if (server.state() == ServerData.State.PINGING) {
            return "Pinging";
        }

        if (server.state() == ServerData.State.UNREACHABLE) {
            return "Offline";
        }

        return "Unknown";
    }

    private int getPingColor(ServerData server) {
        if (server.ping > 0L && server.ping < 120L) {
            return MenuTheme.SUCCESS;
        }

        if (server.ping > 0L && server.ping < 260L) {
            return MenuTheme.WARNING;
        }

        return server.state() == ServerData.State.UNREACHABLE ? MenuTheme.DANGER : MenuTheme.SOFT_TEXT;
    }

    private int getClickedServerIndex(double mouseX, double mouseY) {
        int x = getListX();
        int y = getListY();
        int listWidth = getListWidth();
        int visibleRows = getVisibleRows();

        for (int index = 0; index < visibleRows; index++) {
            int serverIndex = index + scrollOffset;
            if (serverIndex >= servers.size()) {
                break;
            }

            int rowY = y + 12 + index * ROW_HEIGHT;
            if (isInside(mouseX, mouseY, x + 14, rowY, listWidth - 28, ROW_HEIGHT - 8)) {
                return serverIndex;
            }
        }

        return -1;
    }

    private boolean hasSelectedServer() {
        return selectedIndex >= 0 && selectedIndex < servers.size();
    }

    private String trimToWidth(String value, int maxWidth) {
        if (value == null) {
            return "";
        }

        if (font.width(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        String trimmed = value;
        while (!trimmed.isEmpty() && font.width(trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed + ellipsis;
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
            return Math.max(72, height - getListY() - 20);
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
            return VIA_LOADED ? 258 : 226;
        }

        return Math.min(420, height - getSideY() - 28);
    }

    private int getVisibleRows() {
        return Math.max(1, (getListHeight() - 24) / ROW_HEIGHT);
    }

    private int getDirectInputY() {
        return isCompactLayout() ? 62 : 92;
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
            if (index < 2) {
                return getSideY() + 132 + index * 38;
            }

            return getSideY() + 224 + (index - 2) * 38;
        }

        if (index < 2) {
            return getSideY() + 102;
        }

        return getSideY() + 156 + ((index - 2) / 2) * 34;
    }

    private int getActionSeparatorY() {
        return isCompactLayout() ? getSideY() + 142 : getSideY() + 202;
    }

    private int getServerActionsLabelY() {
        return isCompactLayout() ? getSideY() + 148 : getSideY() + 210;
    }

    private boolean isCompactLayout() {
        return width < 820 || height < 430;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
