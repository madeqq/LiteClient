package me.madeq.client.protocol.components.data;

import io.netty.buffer.ByteBuf;

import java.util.Map;

public interface DataComponent {
    Map<Integer, Integer> getIds();
    void write(ByteBuf buf);
}
