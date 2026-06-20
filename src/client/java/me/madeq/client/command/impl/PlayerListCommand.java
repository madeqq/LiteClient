package me.madeq.client.command.impl;

import com.mojang.brigadier.suggestion.Suggestion;
import me.madeq.client.chat.ChatHelper;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerListCommand extends Command {

    private static final AtomicInteger requestId = new AtomicInteger(1000);
    private static volatile boolean active = false;

    public PlayerListCommand() {
        super("playerlist", "Check player list (tabcomplete method)", ModuleType.COMMAND);
    }

    @Override
    public void executeCommand(CommandContext context) {
        if (active) {
            context.sendMessage("<red>Already scanning, please wait...");
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        if (mc.getConnection() == null) return;

        active = true;
        mc.getConnection().send(new ServerboundCommandSuggestionPacket(requestId.incrementAndGet(), "/msg "));
        context.sendMessage("<white>Scanning player list...");
    }

    public static synchronized void handleSuggestions(ClientboundCommandSuggestionsPacket packet) {
        if (!active || packet.id() != requestId.get()) return;
        active = false;

        List<String> players = new ArrayList<>();
        for (Suggestion suggestion : packet.toSuggestions().getList()) {
            String value = suggestion.getText().trim();
            if (value.matches("^[A-Za-z0-9_]{3,16}$")) {
                players.add(value);
            }
        }

        Collections.sort(players);
        ChatHelper.send("<white>Players (" + players.size() + "): <green>" + String.join("<white>, <green>", players));
    }
}

