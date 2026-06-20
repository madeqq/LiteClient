package me.madeq.client.utils.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class StatusProtocolPackets {
    private StatusProtocolPackets() {
    }

    public static void writeHandshake(DataOutputStream output, ServerAddress address) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        DataOutputStream packet = new DataOutputStream(payload);
        writeVarInt(packet, 0);
        writeVarInt(packet, SharedConstants.RELEASE_NETWORK_PROTOCOL_VERSION);
        writeString(packet, address.getHost());
        packet.writeShort(address.getPort());
        writeVarInt(packet, 1);

        writeFramedPacket(output, payload.toByteArray());
    }

    public static void writeStatusRequest(DataOutputStream output) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        DataOutputStream packet = new DataOutputStream(payload);
        writeVarInt(packet, 0);

        writeFramedPacket(output, payload.toByteArray());
    }

    public static String readString(DataInputStream input) throws IOException {
        int length = readVarInt(input);
        byte[] bytes = input.readNBytes(length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static int readVarInt(DataInputStream input) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        do {
            currentByte = input.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;

            if (position >= 32) {
                throw new IOException("VarInt is too big");
            }
        } while ((currentByte & 0x80) == 0x80);

        return value;
    }

    private static void writeFramedPacket(DataOutputStream output, byte[] payload) throws IOException {
        writeVarInt(output, payload.length);
        output.write(payload);
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(output, bytes.length);
        output.write(bytes);
    }

    private static void writeVarInt(DataOutputStream output, int value) throws IOException {
        while ((value & -128) != 0) {
            output.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        output.writeByte(value);
    }
}
