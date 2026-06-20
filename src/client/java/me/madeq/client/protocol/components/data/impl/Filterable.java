package me.madeq.client.protocol.components.data.impl;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

public class Filterable<T> {
    private final T raw;
    private final T filtered;

    public Filterable(T raw, T filtered) {
        this.raw = raw;
        this.filtered = filtered;
    }

    public void write(ByteBuf buf, Consumer<T> writer) {
        writer.accept(this.raw);
        if (this.filtered != null) {
            buf.writeBoolean(true);
            writer.accept(this.filtered);
        } else {
            buf.writeBoolean(false);
        }
    }
}
