package me.madeq.client.command.impl;

import me.madeq.client.chat.ChatHelper;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Objects;

public class ServerInfoCommand extends Command {

    public ServerInfoCommand() {
        super("serverinfo", "Display info about the current server", ModuleType.COMMAND);
    }

    @Override
    public void executeCommand(CommandContext context) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();

        if (connection == null || mc.player == null) {
            context.sendMessage("<red>Not connected to a server.");
            return;
        }

        ServerData serverData = mc.getCurrentServer();

        String ip = serverData != null ? serverData.ip : "Unknown";
        String name = serverData != null && !serverData.name.isBlank() ? serverData.name : "Unknown";
        String version = serverData != null ? serverData.version.getString() : "Unknown";
        String brand = connection.serverBrand();
        String brandDisplay = (brand == null || brand.isBlank()) ? "Vanilla" : brand;
        int playerCount = connection.getOnlinePlayers().size();
        int maxPlayers = serverData != null ? serverData.players != null ? serverData.players.max() : -1 : -1;
        String playersDisplay = maxPlayers > 0 ? playerCount + " / " + maxPlayers : String.valueOf(playerCount);
        long ping = serverData != null ? serverData.ping : -1;
        String pingDisplay = ping >= 0 ? ping + "ms" : "Unknown";
        String gamemode = mc.player.gameMode.getGameModeForPlayer().getName();

        ChatHelper.send("<white>-------- <gradient:#4facfe:#00f2fe>Server Info</gradient> <white>--------");
        ChatHelper.send("<gray>Name:      <white>" + name);
        ChatHelper.send("<gray>IP:        <white>" + ip);
        ChatHelper.send("<gray>Brand:     <aqua>" + brandDisplay);
        ChatHelper.send("<gray>Version:   <white>" + version);
        ChatHelper.send("<gray>Players:   <green>" + playersDisplay);
        ChatHelper.send("<gray>Ping:      <yellow>" + pingDisplay);
        ChatHelper.send("<gray>Gamemode:  <white>" + capitalize(gamemode));
        ChatHelper.send("<white>--------------------------------");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
