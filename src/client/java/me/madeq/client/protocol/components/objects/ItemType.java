package me.madeq.client.protocol.components.objects;

import java.util.Map;

public enum ItemType {
    WRITABLE_BOOK(Map.of(766, 1091, 767, 1091, 768, 1132, 769, 1141)),
    WRITTEN_BOOK(Map.of(766, 1092, 767, 1092, 768, 1133, 769, 1142));

    private final Map<Integer, Integer> byVersion;

    ItemType(Map<Integer, Integer> byVersion) {
        this.byVersion = byVersion;
    }

    public int getId(int protocolVersion) {
        return byVersion.getOrDefault(protocolVersion, 0);
    }
}
