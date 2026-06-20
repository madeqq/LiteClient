package me.madeq.client.protocol.netty;

import io.netty.channel.ChannelPipeline;
import me.madeq.client.mixin.ConnectionAccessor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;

public class PipelineManager {
    private static final String ENCODER_NAME = "lite_encoder";

    public void addEncoder(ClientPacketListener handler) {
        Connection connection = handler.getConnection();
        ChannelPipeline pipeline = ((ConnectionAccessor) connection).getChannel().pipeline();
        if (pipeline.get(ENCODER_NAME) != null) return;
        pipeline.addBefore(
                "encoder",
                ENCODER_NAME,
                new EncodeHandler()
        );
    }

    public void removeEncoder(ClientPacketListener handler) {
        Connection connection = handler.getConnection();
        ChannelPipeline pipeline = ((ConnectionAccessor) connection).getChannel().pipeline();

        try {
            if (pipeline.get(ENCODER_NAME) != null) {
                pipeline.remove(ENCODER_NAME);
            }
        } catch (RuntimeException ignored) {
        }
    }
}
