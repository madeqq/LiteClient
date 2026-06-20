package me.madeq.client.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import me.madeq.client.protocol.Protocol;

import java.util.List;

public class EncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private static final int MAX_PACKETS_PER_WRITE = 8;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out){
        if (Protocol.hasQueuedPackets()) {
            out.addAll(Protocol.drainQueuedPackets(MAX_PACKETS_PER_WRITE));
        }
        out.add(bytebuf.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Protocol.disableSendingAndClearQueue();
        super.exceptionCaught(ctx, cause);
    }
}
