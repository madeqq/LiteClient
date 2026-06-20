package me.madeq.client;

import me.madeq.client.alt.AltManager;
import me.madeq.client.config.ClientConfigManager;
import me.madeq.client.command.CommandManager;
import me.madeq.client.module.ModuleManager;
import me.madeq.client.gui.ClickGuiManager;
import me.madeq.client.gui.menu.ConnectionProgress;
import me.madeq.client.hud.HudManager;
import me.madeq.client.notify.NotificationManager;
import me.madeq.client.protocol.Protocol;
import me.madeq.client.protocol.netty.PipelineManager;
import me.madeq.client.utils.DiscordRP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.lwjgl.glfw.GLFW;

public class LiteClient implements ClientModInitializer {

    private static final CommandManager COMMAND_MANAGER = new CommandManager();
    private static final ModuleManager MODULE_MANAGER = new ModuleManager();
    private static final ClickGuiManager CLICK_GUI_MANAGER = new ClickGuiManager();
    private static final HudManager HUD_MANAGER = new HudManager();
    private static final NotificationManager NOTIFICATION_MANAGER = new NotificationManager();
    private static final ClientConfigManager CONFIG_MANAGER = new ClientConfigManager();
    private static final PipelineManager PIPELINE_MANAGER = new PipelineManager();
    private static final AltManager ALT_MANAGER = new AltManager();
    public static final String VERSION = "1.0";

    @Override
    public void onInitializeClient() {
        MODULE_MANAGER.registerDefaults();
        COMMAND_MANAGER.registerDefaults();
        CLICK_GUI_MANAGER.initialize();
        CONFIG_MANAGER.initialize();
        ALT_MANAGER.initialize();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Protocol.enableSending();
            CONFIG_MANAGER.loadCurrentServer();
            ConnectionProgress.get().complete();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Protocol.disableSendingAndClearQueue();
            PIPELINE_MANAGER.removeEncoder(handler);
            MODULE_MANAGER.handleDisconnect();
            CONFIG_MANAGER.saveCurrentServer();
            ConnectionProgress.get().reset();
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> PIPELINE_MANAGER.addEncoder(handler));
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> { //For now we are doing this, in the future we will add responsiveness
            GLFW.glfwMaximizeWindow(client.getWindow().getWindow());
            client.options.guiScale().set(2);
            client.resizeDisplay();
        });
        new DiscordRP().runDiscordRP();
    }

    public static CommandManager getCommandManager() {
        return COMMAND_MANAGER;
    }

    public static ModuleManager getModuleManager() {
        return MODULE_MANAGER;
    }

    public static ClickGuiManager getClickGuiManager() {
        return CLICK_GUI_MANAGER;
    }

    public static HudManager getHudManager() {
        return HUD_MANAGER;
    }

    public static NotificationManager getNotificationManager() {
        return NOTIFICATION_MANAGER;
    }

    public static ClientConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public static AltManager getAltManager() {
        return ALT_MANAGER;
    }
}
