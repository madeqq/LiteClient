package me.madeq.client.protocol.components;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.ViaFabricPlusBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.madeq.client.protocol.components.data.DataComponent;
import me.madeq.client.protocol.components.data.DataComponents;
import me.madeq.client.protocol.components.objects.HashStack;
import me.madeq.client.protocol.components.objects.ItemStack;
import me.madeq.client.protocol.packets.PacketCodec;
import net.minecraft.nbt.Tag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ComponentsCodec {

    public static void writeItem(ByteBuf buf, ItemStack item) {
        int protocol = ViaFabricPlus.getImpl().getTargetVersion().getVersion();
        boolean empty = item == null || item.amount() <= 0;
        PacketCodec.writeVarInt(buf, !empty ? item.amount() : 0);
        if (!empty) {
            PacketCodec.writeVarInt(buf, item.id());
            writeDataComponents(buf, item.dataComponents(), protocol);
        }
    }

    public static void writeItemUntrusted(ByteBuf buf, ItemStack item, boolean lengthPrefixed) {
        boolean empty = item == null || item.amount() <= 0;
        if(empty){
            buf.writeByte(0);
            return;
        }
        PacketCodec.writeVarInt(buf, item.amount());
        PacketCodec.writeVarInt(buf, item.id());
        writePatchableComponents(buf, item, lengthPrefixed);
    }

    public static void writePatchableComponents(ByteBuf buf, ItemStack item, boolean lengthPrefixed) {
        int protocol = ViaFabricPlus.getImpl().getTargetVersion().getVersion();
        DataComponents dataComponents = item.dataComponents();

        if (dataComponents == null || dataComponents.getDataComponents().isEmpty()) {
            PacketCodec.writeVarInt(buf, 0);
            PacketCodec.writeVarInt(buf, 0);
            return;
        }

        PacketCodec.writeVarInt(buf, dataComponents.getDataComponents().size());
        PacketCodec.writeVarInt(buf, 0);

        for (DataComponent dataComponent : dataComponents.getDataComponents()) {
            PacketCodec.writeVarInt(buf, dataComponent.getIds().getOrDefault(protocol, 0));
            if (lengthPrefixed) {
                ByteBuf componentBuf = Unpooled.buffer();
                try {
                    dataComponent.write(componentBuf);
                    PacketCodec.writeVarInt(buf, componentBuf.readableBytes());
                    buf.writeBytes(componentBuf);
                } finally {
                    componentBuf.release();
                }
            } else {
                dataComponent.write(buf);
            }
        }
    }

    public static void writeDataComponents(ByteBuf buf, DataComponents dataComponents, int protocol) {
        if(dataComponents != null) {
            PacketCodec.writeVarInt(buf, dataComponents.getDataComponents().size());
            PacketCodec.writeVarInt(buf, 0);
            for (DataComponent dataComponent : dataComponents.getDataComponents()) {
                PacketCodec.writeVarInt(buf, dataComponent.getIds().getOrDefault(protocol, 0));
                dataComponent.write(buf);
            }
        }else{
            PacketCodec.writeVarInt(buf, 0);
            PacketCodec.writeVarInt(buf, 0);
        }
    }

    public static void writeTag(ByteBuf buf, Tag tag) {
        try {
            buf.writeByte(tag.getId());
            if (tag.getId() == Tag.TAG_END) return;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            tag.write(dos);
            buf.writeBytes(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeHashedStack(ByteBuf buf, HashStack hashedStack) {
        PacketCodec.writeVarInt(buf, 1);
        PacketCodec.writeVarInt(buf, 1);

        PacketCodec.writeVarInt(buf, hashedStack.compSize());
        for (int i = 0; i < hashedStack.compSize(); i++) {
            PacketCodec.writeVarInt(buf, 1);
            buf.writeInt(1);
        }

        PacketCodec.writeVarInt(buf, hashedStack.delCompSize());
        for (int i = 0; i < hashedStack.delCompSize(); i++) {
            PacketCodec.writeVarInt(buf, 1);
        }
    }
}
