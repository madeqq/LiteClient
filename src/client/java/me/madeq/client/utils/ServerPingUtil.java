package me.madeq.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.madeq.client.utils.protocol.StatusProtocolPackets;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;

public final class ServerPingUtil {
    private static final int PING_TIMEOUT_MS = 3000;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "LiteClient-ServerPing");
        thread.setDaemon(true);
        return thread;
    });

    private ServerPingUtil() {
    }

    public record PingResult(boolean online, long ping, Component motd, byte[] iconBytes, ServerStatus.Players players) {
        public static PingResult offline() {
            return new PingResult(false, -1L, Component.literal("Offline"), null, null);
        }
    }

    public static CompletableFuture<PingResult> pingAsync(String address) {
        return CompletableFuture.supplyAsync(() -> ping(address), EXECUTOR);
    }

    public static PingResult ping(String address) {
        ServerAddress parsedAddress = ServerAddress.parseString(address);
        Optional<ResolvedServerAddress> resolvedAddress = ServerNameResolver.DEFAULT.resolveAddress(parsedAddress);
        InetSocketAddress socketAddress = resolvedAddress
                .map(ResolvedServerAddress::asInetSocketAddress)
                .orElseGet(() -> InetSocketAddress.createUnresolved(parsedAddress.getHost(), parsedAddress.getPort()));
        long startedAt = System.nanoTime();

        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, PING_TIMEOUT_MS);
            socket.setSoTimeout(PING_TIMEOUT_MS);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            StatusProtocolPackets.writeHandshake(output, parsedAddress);
            StatusProtocolPackets.writeStatusRequest(output);
            output.flush();

            StatusProtocolPackets.readVarInt(input);
            int packetId = StatusProtocolPackets.readVarInt(input);
            if (packetId != 0) {
                return PingResult.offline();
            }

            String response = StatusProtocolPackets.readString(input);
            long ping = Math.max(1L, (System.nanoTime() - startedAt) / 1_000_000L);
            return parseStatusResponse(response, ping);
        } catch (IOException | RuntimeException exception) {
            return PingResult.offline();
        }
    }

    public static void prepareForPing(ServerData server) {
        server.setState(ServerData.State.PINGING);
        server.ping = -1L;
        server.players = null;
        server.motd = Component.literal("Checking");
        server.status = Component.literal("");
    }

    public static void applyToServerData(ServerData server, PingResult result) {
        if (result.online()) {
            server.setState(ServerData.State.INITIAL);
            server.ping = result.ping();
            server.motd = result.motd();
            server.status = Component.empty();
            server.players = result.players();
            if (result.iconBytes() != null) {
                byte[] iconBytes = ServerData.validateIcon(result.iconBytes());
                if (iconBytes != null) {
                    server.setIconBytes(iconBytes);
                }
            }
            return;
        }

        server.setState(ServerData.State.UNREACHABLE);
        server.ping = -1L;
        server.players = null;
        server.motd = result.motd();
        server.status = Component.empty();
    }

    private static PingResult parseStatusResponse(String response, long ping) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        Component motd = parseDescription(root.get("description"));
        ServerStatus.Players players = null;

        if (root.has("players") && root.get("players").isJsonObject()) {
            JsonObject playersObject = root.getAsJsonObject("players");
            int online = playersObject.has("online") ? playersObject.get("online").getAsInt() : 0;
            int max = playersObject.has("max") ? playersObject.get("max").getAsInt() : 0;
            players = new ServerStatus.Players(max, online, Collections.emptyList());
        }

        byte[] iconBytes = null;
        if (root.has("favicon") && root.get("favicon").isJsonPrimitive()) {
            iconBytes = decodeFavicon(root.get("favicon").getAsString());
        }

        return new PingResult(true, ping, motd, iconBytes, players);
    }

    private static byte[] decodeFavicon(String value) {
        if (value == null || !value.startsWith("data:image/png;base64,")) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(value.substring("data:image/png;base64,".length()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Component parseDescription(JsonElement description) {
        if (description == null || description.isJsonNull()) {
            return Component.literal("No MOTD available");
        }

        if (description.isJsonPrimitive()) {
            return Component.literal(description.getAsString());
        }

        if (!description.isJsonObject()) {
            return Component.literal("No MOTD available");
        }

        JsonObject object = description.getAsJsonObject();
        StringBuilder builder = new StringBuilder();
        if (object.has("text")) {
            builder.append(object.get("text").getAsString());
        }

        if (object.has("extra") && object.get("extra").isJsonArray()) {
            appendDescriptionText(object.getAsJsonArray("extra"), builder);
        }

        if (builder.isEmpty()) {
            return Component.literal("No MOTD available");
        }

        return Component.literal(builder.toString());
    }

    private static void appendDescriptionText(JsonArray extra, StringBuilder builder) {
        for (JsonElement element : extra) {
            if (element.isJsonPrimitive()) {
                builder.append(element.getAsString());
                continue;
            }

            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject object = element.getAsJsonObject();
            if (object.has("text")) {
                builder.append(object.get("text").getAsString());
            }

            if (object.has("extra") && object.get("extra").isJsonArray()) {
                appendDescriptionText(object.getAsJsonArray("extra"), builder);
            }
        }
    }
}
