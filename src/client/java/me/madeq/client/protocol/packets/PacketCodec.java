package me.madeq.client.protocol.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.madeq.client.protocol.Protocol;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class PacketCodec {
    private static final Protocol protocol = new Protocol();

    public static void sendPacket(Packet packet, int packetAmount) {
        if (!Protocol.isSendingEnabled() || packetAmount <= 0) {
            return;
        }

        long sessionId = Protocol.getNetworkSessionId();
        ByteBuf buf = Unpooled.buffer();
        packet.write(buf);
        List<ByteBuf> buffs = new ArrayList<>();

        try {
            try {
                for (int i = 0; i < packetAmount; i++) {
                    if (!isCurrentSession(sessionId)) {
                        releaseBuffers(buffs);
                        return;
                    }

                    buffs.add(buf.copy());
                }

                protocol.addPacketsToSend(buffs);
            } catch (OutOfMemoryError e) {
                releaseBuffers(buffs);
                final int BATCH_SIZE = 100;

                CompletableFuture.runAsync(() -> {
                    if (!isCurrentSession(sessionId)) {
                        return;
                    }

                    ByteBuf buf2 = Unpooled.buffer();
                    packet.write(buf2);

                    try {
                        for (int processed = 0; processed < packetAmount; processed += BATCH_SIZE) {
                            if (!isCurrentSession(sessionId)) {
                                return;
                            }

                            int currentBatchSize = Math.min(BATCH_SIZE, packetAmount - processed);
                            List<ByteBuf> batch = new ArrayList<>(currentBatchSize);

                            for (int i = 0; i < currentBatchSize; i++) {
                                if (!isCurrentSession(sessionId)) {
                                    releaseBuffers(batch);
                                    return;
                                }

                                batch.add(buf2.copy());
                            }

                            protocol.addPacketsToSend(batch);

                            if (processed % (BATCH_SIZE * 5) == 0) System.gc();
                        }
                    } finally {
                        buf2.release();
                    }
                });
            }
        } finally {
            buf.release();
        }
    }

    private static boolean isCurrentSession(long sessionId) {
        return Protocol.isSendingEnabled() && Protocol.getNetworkSessionId() == sessionId;
    }

    private static void releaseBuffers(List<ByteBuf> buffers) {
        for (ByteBuf buffer : buffers) {
            if (buffer != null && buffer.refCnt() > 0) {
                buffer.release(buffer.refCnt());
            }
        }
    }


    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static void writeUtf(ByteBuf buf, String string) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, abyte.length);
        buf.writeBytes(abyte);
    }

    public static void writeUUID(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static <T> void writeNullable(ByteBuf buf, @Nullable T value, BiConsumer<ByteBuf, T> ifPresent) {
        if (value != null) {
            buf.writeBoolean(true);
            ifPresent.accept(buf, value);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static void writeArray(ByteBuf buf, int[] array) {
        PacketCodec.writeVarInt(buf, array.length + 1);
        for (int holder : array) {
            PacketCodec.writeVarInt(buf, holder);
        }
    }

    public static <T> void writeList(ByteBuf buf, List<T> value, BiConsumer<ByteBuf, T> writer) {
        PacketCodec.writeVarInt(buf, value.size());
        for (T t : value) {
            writer.accept(buf, t);
        }
    }

    public static <K> void writeSet(ByteBuf buf, Set<K> set, BiConsumer<ByteBuf, K> writer) {
        PacketCodec.writeVarInt(buf, set.size());
        for (K key : set) {
            writer.accept(buf, key);
        }
    }
}
