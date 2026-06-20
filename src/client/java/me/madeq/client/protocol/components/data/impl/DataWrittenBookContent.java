package me.madeq.client.protocol.components.data.impl;

import io.netty.buffer.ByteBuf;
import me.madeq.client.protocol.components.ComponentsCodec;
import me.madeq.client.protocol.components.data.DataComponent;
import me.madeq.client.protocol.packets.PacketCodec;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Map;

public class DataWrittenBookContent implements DataComponent {
    private final Filterable<String> title;
    private final String author;
    private final int generation;
    private final List<Filterable<Tag>> pages;
    private final boolean resolved;

    public DataWrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Tag>> pages, boolean resolved) {
        this.title = title;
        this.author = author;
        this.generation = generation;
        this.pages = pages;
        this.resolved = resolved;
    }

    @Override
    public Map<Integer, Integer> getIds() {
        return Map.of(
                766, 34, 767, 34,
                768, 44, 769, 44,
                770, 44
        );
    }
    @Override
    public void write(ByteBuf buf) {
        this.title.write(buf, string -> PacketCodec.writeUtf(buf, string));
        PacketCodec.writeUtf(buf, this.author);
        PacketCodec.writeVarInt(buf, this.generation);
        PacketCodec.writeVarInt(buf, this.pages.size());
        for (Filterable<Tag> page : this.pages) {
            page.write(buf, tag -> ComponentsCodec.writeTag(buf, tag));
        }
        buf.writeBoolean(this.resolved);
    }
}
