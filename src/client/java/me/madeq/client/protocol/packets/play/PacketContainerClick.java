package me.madeq.client.protocol.packets.play;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.madeq.client.protocol.components.ComponentsCodec;
import me.madeq.client.protocol.components.objects.HashStack;
import me.madeq.client.protocol.components.objects.ItemStack;
import me.madeq.client.protocol.packets.Packet;
import me.madeq.client.protocol.packets.PacketCodec;

import java.util.Map;

public class PacketContainerClick implements Packet {
    private static final Map<Integer, Integer> PACKET_IDS = Map.of(
            766, 14,
            767, 14,
            768, 16,
            769, 16,
            770, 16,
            771, 17,
            772, 17,
            773, 17,
            774, 17
    );

    private final int protocolId;
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ContainerActionType action;
    private final ContainerAction param;
    private ItemStack carriedItem;
    private Int2ObjectMap<ItemStack> changedSlots;
    private int mapSize;
    private HashStack hashStack;

    public PacketContainerClick(int protocolId, int containerId, int stateId, int slot, ContainerActionType action, ContainerAction param, ItemStack carriedItem, Int2ObjectMap<ItemStack> changedSlots) {
        this.protocolId = protocolId;
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.action = action;
        this.param = param;
        this.carriedItem = carriedItem;
        this.changedSlots = changedSlots;
    }

    public PacketContainerClick(int protocolId, int containerId, int stateId, int slot, PacketContainerClick.ContainerActionType action, PacketContainerClick.ContainerAction param, HashStack hashStack, int mapSize) {
        this.protocolId = protocolId;
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.action = action;
        this.param = param;
        this.hashStack = hashStack;
        this.mapSize = mapSize;
    }

    @Override
    public void write(ByteBuf buf) {
        PacketCodec.writeVarInt(buf, PACKET_IDS.getOrDefault(protocolId, 16));
        PacketCodec.writeVarInt(buf, containerId);
        PacketCodec.writeVarInt(buf, stateId);
        buf.writeShort(slot);
        int param = this.param.ordinal();
        if (this.action == ContainerActionType.DROP_ITEM) {
            param %= 2;
        }

        buf.writeByte(param);
        buf.writeByte(action.ordinal());

        if(protocolId <= 769) {
            PacketCodec.writeVarInt(buf, changedSlots.size());
            changedSlots.forEach((changeSlot, item) -> {
                buf.writeShort(changeSlot);
                ComponentsCodec.writeItem(buf, item);
            });
            ComponentsCodec.writeItem(buf, carriedItem);
        }else{
            PacketCodec.writeVarInt(buf, mapSize);
            for (int i = 0; i < mapSize; i++) {
                buf.writeShort(i);
                PacketCodec.writeNullable(buf, this.hashStack, ComponentsCodec::writeHashedStack);
            }

            PacketCodec.writeNullable(buf, this.hashStack, ComponentsCodec::writeHashedStack);
        }
    }

    public enum ContainerActionType {
        CLICK_ITEM, SHIFT_CLICK_ITEM, MOVE_TO_HOTBAR_SLOT, CREATIVE_GRAB_MAX_STACK, DROP_ITEM, SPREAD_ITEM, FILL_STACK;
    }

    public enum ContainerAction {
        LEFT_CLICK, RIGHT_CLICK;
    }
}


