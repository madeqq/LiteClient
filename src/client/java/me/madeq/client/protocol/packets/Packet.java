package me.madeq.client.protocol.packets;

import io.netty.buffer.ByteBuf;

public interface Packet {
    void write(ByteBuf buf);
}
