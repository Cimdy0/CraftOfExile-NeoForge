package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 护符效果在身门控（承载「while you have an active Charm」类门控）。
 * <p>
 * 读源：玩家同步附件 {@link AttachmentRegistry#CHARM_ACTIVE}（slot → 剩余 tick），由
 * {@code CharmEngine} 在护符触发时写入、每 tick 衰减——任一槽位仍剩余 tick 即判定「有护符在身」。
 * 护符效果本身由 {@code CharmEngine} 经 {@code StatStore} 真实注入玩家属性层（R3：读点恒有数据驱动，非死线）。
 * <p>
 * 当前仅支持「任意护符在身」（{@code kind} 预留作未来子作用域分流，默认 {@code "any"}）。
 * 数据未就绪（无护符在身）时返回 0，符合「没数据就是没有」原则。
 */
public record CharmActiveValue() implements EntityNode {
    public static final Codec<CharmActiveValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("kind", "any").forGetter(c -> "any")
    ).apply(instance, ignore -> new CharmActiveValue()));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        if (!(living instanceof Player player)) return 0D;
        Map<Integer, Integer> active = player.getData(AttachmentRegistry.CHARM_ACTIVE);
        for (int t : active.values()) {
            if (t > 0) return 1D;
        }
        return 0D;
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.charm_active"));
    }
}
