package me.madeq.client.protocol.components.data.impl;

import io.netty.buffer.ByteBuf;
import me.madeq.client.protocol.components.data.DataComponent;
import me.madeq.client.protocol.packets.PacketCodec;

import java.util.List;
import java.util.Map;

public class DataWritableBookContent implements DataComponent {
    private final List<Filterable<String>> pages;

    public DataWritableBookContent(List<Filterable<String>> pages) {
        this.pages = pages;
    }

    @Override
    public Map<Integer, Integer> getIds() {
        return Map.of(
                766, 33, 767, 33,
                768, 43, 769, 43,
                770, 43
        );
    }
    @Override
    public void write(ByteBuf buf) {
        PacketCodec.writeVarInt(buf, this.pages.size());
        for (Filterable<String> page : this.pages) {
            page.write(buf, s -> PacketCodec.writeUtf(buf, s));
        }
    }
}