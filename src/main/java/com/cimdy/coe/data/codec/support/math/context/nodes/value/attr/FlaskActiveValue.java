package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.flask.FlaskEffect;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 药水效果生效中读值节点（承载「during any Flask effect」/「during any Mana Flask effect」类门控）。
 * <p>
 * 读源：玩家附件 {@link AttachmentRegistry#FLASK_ACTIVE}（{@code Map<Integer, FlaskEffect>}），
 * 由 {@code FlaskHelper.tick} 在药水生效期间真实填充、tick 递减至 0 时移除（R3：读点恒有数据驱动，非死线）。
 * <p>
 * {@code resource} 为空串表示任意药水；非空前仅当存在该资源类型（"mana"/"health"）的药水生效中才为真。
 * 数据未就绪（无附件 / 无生效药水）时返回 0，符合「没数据就是没有」原则。
 */
public record FlaskActiveValue(String resource) implements EntityNode {
    public static final Codec<FlaskActiveValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("resource", "").forGetter(FlaskActiveValue::resource)
    ).apply(instance, FlaskActiveValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        Map<Integer, FlaskEffect> active = living.getData(AttachmentRegistry.FLASK_ACTIVE);
        if (active == null || active.isEmpty()) return 0D;
        String r = resource == null ? "" : resource;
        for (FlaskEffect fx : active.values()) {
            if (r.isEmpty() || r.equals(fx.resource())) {
                return 1D;
            }
        }
        return 0D;
    }

    @Override
    public List<Component> describeFragment() {
        String r = resource == null ? "" : resource;
        String key = r.isEmpty() ? "stat.condition.flask_active.any"
                : "stat.condition.flask_active." + r;
        return List.of(Component.translatable(key));
    }
}
