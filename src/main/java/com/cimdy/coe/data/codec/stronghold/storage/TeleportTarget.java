package com.cimdy.coe.data.codec.stronghold.storage;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;

public record TeleportTarget(String name, GlobalPos pos) {

    /** 列表里显示的一行：维度名 + 名称 + 坐标。 */
    public Component label() {
        return Component.translatable("dimension." + pos.dimension().identifier().toLanguageKey())
                .append(Component.literal(" "))
                .append(Component.literal(name + "("
                        + pos.pos().getX() + ", " + pos.pos().getY() + ", " + pos.pos().getZ() + ")"));
    }
}
