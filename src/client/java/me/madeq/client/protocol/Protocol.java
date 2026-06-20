package me.madeq.client.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    private static final List<ByteBuf> OUT_PACKETS = new ArrayList<>();
    private static boolean sendingEnabled = true;
    private static long networkSessionId;
    public static final List<Integer> SUPPORTED_PROTOCOLS = List.of(766, 767, 768, 769);

    public void addPacketsToSend(List<ByteBuf> packets){
        addQueuedPackets(packets);
    }

    public static synchronized void enableSending() {
        sendingEnabled = true;
        networkSessionId++;
    }

    public static synchronized void disableSendingAndClearQueue() {
        sendingEnabled = false;
        networkSessionId++;
        releasePackets(OUT_PACKETS);
        OUT_PACKETS.clear();
    }

    public static synchronized boolean isSendingEnabled() {
        return sendingEnabled;
    }

    public static synchronized long getNetworkSessionId() {
        return networkSessionId;
    }

    public static synchronized boolean hasQueuedPackets() {
        return !OUT_PACKETS.isEmpty();
    }

    public static synchronized List<ByteBuf> drainQueuedPackets(int maxPackets) {
        int amount = Math.min(maxPackets, OUT_PACKETS.size());
        List<ByteBuf> packets = new ArrayList<>(OUT_PACKETS.subList(0, amount));
        OUT_PACKETS.subList(0, amount).clear();
        return packets;
    }

    private static synchronized void addQueuedPackets(List<ByteBuf> packets) {
        if (!sendingEnabled) {
            releasePackets(packets);
            return;
        }

        OUT_PACKETS.addAll(packets);
    }

    private static void releasePackets(List<ByteBuf> packets) {
        for (ByteBuf packet : packets) {
            if (packet != null && packet.refCnt() > 0) {
                packet.release(packet.refCnt());
            }
        }
    }
}
