package me.madeq.client.utils;

import me.madeq.client.LiteClient;
import me.madeq.client.logger.Logger;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRP implements ReadyCallback {

    private final String DISCORD_APP_ID = "1515832814707150858";
    private final String IMAGE = "liteclient";
    private final String GITHUB_URL = "Download: github.com/madeqq/LiteClient";

    private final Minecraft MC = Minecraft.getInstance();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private DiscordRichPresence richPresence;
    private boolean enabled = true;

    public boolean rpSwitch = true;

    public void runDiscordRP() {
        try {
            Logger.info("Starting Discord Rich Presence initialization...");
            initializeRPC();
            startUpdateTask();
            updatePresence();
            Logger.info("Discord Rich Presence started successfully!");
        } catch (Exception e) {
            Logger.error("Failed to initialize Discord Rich Presence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void apply(DiscordUser user) {
        Logger.info("Discord RPC Ready - Connected to %s#%s (%s)",
                user.username, user.discriminator, user.userId);
    }

    private void initializeRPC() {
        Logger.info("Initializing Discord RPC with Application ID: " + DISCORD_APP_ID);

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler(this)
                .build();

        DiscordRPC.discordInitialize(DISCORD_APP_ID, handlers, true);

        richPresence = new DiscordRichPresence.Builder("Client Initialization")
                .setBigImage(IMAGE, "LiteClient " + LiteClient.VERSION)
                .setDetails("Loading...")
                .setStartTimestamps(System.currentTimeMillis())
                .build();

        Logger.info("Discord RPC initialized successfully!");
    }

    private void startUpdateTask() {
        scheduler.scheduleWithFixedDelay(this::updatePresenceTask,
                10L, 10L, TimeUnit.SECONDS);
    }

    private void updatePresenceTask() {
        if (rpSwitch) {
            if (!enabled) {
                Logger.info("Re-enabling Discord RPC...");
                reinitializeRPC();
                enabled = true;
            }
            updatePresence();

        } else {
            DiscordRPC.discordShutdown();
            enabled = false;
        }
    }

    private void reinitializeRPC() {
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler(this)
                .build();
        DiscordRPC.discordInitialize(DISCORD_APP_ID, handlers, true);
    }

    private void updatePresence() {
        if (richPresence == null) return;

        try {
            String details = MC.player == null ? "Disconnected" : "Connected";
            richPresence.details = details;
            richPresence.state = GITHUB_URL;

            DiscordRPC.discordUpdatePresence(richPresence);
        } catch (Exception e) {
            Logger.error("Failed to update Discord presence: " + e.getMessage());
        }
    }
}