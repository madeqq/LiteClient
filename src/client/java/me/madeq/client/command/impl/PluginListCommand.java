package me.madeq.client.command.impl;

import com.mojang.brigadier.suggestion.Suggestion;
import me.madeq.client.chat.ChatHelper;
import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PluginListCommand extends Command {

    private static final List<String> COMMANDS = Arrays.asList(
            "/plugins ", "/pl ", "/ver ", "/version ",
            "/bukkit:plugins ", "/", "/? "
    );

    private static final Set<String> found = new HashSet<>();
    private static final Set<Integer> pendingIds = new HashSet<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static int requestId = 1000;
    private static Consumer<Set<String>> finishCallback;
    private static volatile boolean active = false;
    private static ScheduledFuture<?> timeoutTask;

    public PluginListCommand() {
        super("pluginlist", "Check plugin list (tabcomplete method)", ModuleType.COMMAND);
    }

    @Override
    public void executeCommand(CommandContext context) {
        if (active) {
            context.sendMessage("<red>Already scanning, please wait...");
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        if (mc.getConnection() == null) return;

        context.sendMessage("<white>Scanning plugins...");
        scan(mc, plugins -> {
            if (plugins.isEmpty()) {
                ChatHelper.send("<red>No plugins found.");
                return;
            }
            showPlugins(plugins);
        });
    }

    private static void scan(Minecraft mc, Consumer<Set<String>> callback) {
        active = true;
        found.clear();
        pendingIds.clear();
        finishCallback = callback;

        for (String cmd : COMMANDS) {
            int id = ++requestId;
            pendingIds.add(id);
            mc.getConnection().send(new ServerboundCommandSuggestionPacket(id, cmd));
        }

        if (timeoutTask != null) timeoutTask.cancel(false);
        timeoutTask = SCHEDULER.schedule(() -> {
            if (active) finish();
        }, 2, TimeUnit.SECONDS);
    }

    public static synchronized void handleSuggestions(ClientboundCommandSuggestionsPacket packet) {
        if (!active) return;
        if (!pendingIds.remove(Integer.valueOf(packet.id()))) return;

        for (Suggestion suggestion : packet.toSuggestions().getList()) {
            String value = suggestion.getText().trim();
            if (value.matches("[A-Za-z0-9_\\-]{2,}")
                    && !value.matches("plugins|pl|help|version|ver|bukkit|\\?")) {
                found.add(value);
            }
        }

        if (pendingIds.isEmpty()) {
            if (timeoutTask != null) timeoutTask.cancel(false);
            finish();
        }
    }

    private static synchronized void finish() {
        active = false;
        Consumer<Set<String>> cb = finishCallback;
        finishCallback = null;
        Set<String> results = new HashSet<>(found);
        Minecraft.getInstance().execute(() -> { if (cb != null) cb.accept(results); });
    }

    private static void showPlugins(Set<String> plugins) {
        List<String> sorted = new ArrayList<>(plugins);
        Collections.sort(sorted);

        StringBuilder sb = new StringBuilder();
        sb.append("<white>Plugins (").append(sorted.size()).append("): <green>");
        for (String plugin : sorted) {
            sb.append(plugin).append(", ");
        }
        ChatHelper.send(sb.toString());
    }
}
