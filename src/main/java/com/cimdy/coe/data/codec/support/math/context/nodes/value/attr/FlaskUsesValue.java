package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.flask.FlaskSave;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;
import com.cimdy.coe.item.flask.FlaskItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 药剂剩余「可用次数」读值节点（承载「while you have no Health Flask uses left」类门控）。
 * <p>
 * 读源：玩家附件 {@link AttachmentRegistry#FLASK_SAVE} 槽内物品的充能数据组件
 * （{@code FLASK_CHARGES}），可用次数 = {@code ⌊充能 ÷ 单次消耗⌋}，与
 * {@code FlaskHelper.drink} 的「充能不足则不发动」判据同源——充能由
 * {@code FlaskHelper.tickRecharge}/{@code onKill} 真实增减（R3：读点恒有数据驱动，非死线）。
 * <p>
 * {@code resource} 为空串表示所有药剂槽合计；非空则只计该资源类型（{@code "health"}/{@code "mana"}）的槽。
 * 无附件 / 槽内无药剂时返回 0，符合「没数据就是没有」原则——即「没有可用次数」，
 * 故「no uses left」门控（比较 {@code < 1}）在空槽时同样成立，与 POE2 一致。
 */
public record FlaskUsesValue(String resource) implements EntityNode {
    public static final Codec<FlaskUsesValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("resource", "").forGetter(FlaskUsesValue::resource)
    ).apply(instance, FlaskUsesValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        FlaskSave save = living.getData(AttachmentRegistry.FLASK_SAVE);
        if (save == null) return 0D;
        String r = resource == null ? "" : resource;
        int uses = 0;
        for (int i = 0; i < FlaskSlotRegistry.totalSlots(living.level().registryAccess()); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof FlaskItem flask)) continue;
            if (!r.isEmpty() && !r.equals(flask.resource())) continue;
            int per = Math.max(1, flask.chargesPerUse());
            uses += flask.charges(stack) / per;
        }
        return uses;
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        String r = (resource == null || resource.isEmpty()) ? "all" : resource;
        return List.of(Component.empty()
                .append(StatValueKey.of("flask_uses.resource", r))
                .append(Component.literal(" "))
                .append(StatValueKey.of("flask_uses.unit", "flask")));
    }

    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.flask_uses", describeQuantity(ClientLocale.get()).get(0)));
    }
}
