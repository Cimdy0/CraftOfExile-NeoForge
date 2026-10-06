package com.cimdy.coe.data.codec.support.meta;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public class Labels {
    public static Codec<String> CODEC = Codec.STRING;

    public static final String PROJECTILE = "projectile";
    public static final String ARROW = "arrow";
    public static final String BALL = "ball";
    public static final String HEALTH_MANA = "health_mana";
    public static final String MANA = "mana";
    public static final String HEALTH = "health";
    public static final String DAMAGE = "damage";
    public static final String PHYSICAL = "physical";
    public static final String CHAOS = "chaos";
    public static final String ELEMENT = "element";
    public static final String FIRE = "fire";
    public static final String COLD = "cold";
    public static final String LIGHT = "light";
    public static final String ATTACK = "attack";
    public static final String CAST = "spell";
    public static final String ATTRIBUTE = "attribute";
    public static final String SPEED = "speed";
    public static final String CRIT = "crit";
    public static final String RESISTANCE = "resistance";
    public static final String UNIQUE = "unique";
    public static final String SPLIT = "split";
    public static final String HIT = "hit";
    public static final String BOW = "bow";
    public static final String DEFENCES = "defences";
    public static final String AILMENT = "ailment";
    public static final String COOLDOWN = "cooldown";
    public static final String BUFF = "buff";
    public static final String AREA = "area";

    public static Component getNameComponent(String name) {
        return Component.translatable("stat." + name);
    }
}
