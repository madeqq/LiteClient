package me.madeq.client.protocol.components.objects;

import me.madeq.client.protocol.components.data.DataComponents;

public record ItemStack(int id, int amount, DataComponents dataComponents) {
}
