package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity;

import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 实体类别读值节点（承载「against Demons / Undead / Humanoids / Constructs」等敌种门控）。
 * <p>
 * 类别成员由 <b>datapack entity_type tag</b> 定义（如 {@code coe:undead}），本节点只持
 * {@link TagKey} 引用 —— <b>不在构造期解析</b>，因此 datagen（无 registry access）可直接构造，
 * 且成员表可由数据包自由增删而无需改代码（与项目「数据驱动」一致）。
 * <p>
 * 解析发生在求值期：从 {@code living.level().registryAccess()} 取当次运行的
 * {@code HolderSet.Named} 再判包含 —— 故服务端热重载数据包后即刻生效。
 * <p>
 * 输出 1.0/0.0（命中/未命中），配合条件的「求值 >= 1.0」即布尔判据；
 * 与 {@code onTarget} 组合时 {@code living} 即被判定目标。
 */
public record EntityTypeValue(TagKey<EntityType<?>> tag) implements EntityNode {

    public static final Codec<EntityTypeValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTypeValue::tag)
    ).apply(instance, EntityTypeValue::new));

    /** 便捷构造：按命名空间/路径组装 tag 引用（datagen 用）。 */
    public static EntityTypeValue of(String namespace, String path) {
        return of(Identifier.fromNamespaceAndPath(namespace, path));
    }

    /** 便捷构造：直接以 tag 的 {@link Identifier} 组装（中缀解析用，避免调用方额外 import TagKey）。 */
    public static EntityTypeValue of(Identifier tagId) {
        return new EntityTypeValue(TagKey.create(Registries.ENTITY_TYPE, tagId));
    }

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        return matches(living) ? 1D : 0D;
    }

    /**
     * 运行期判定：从 level 的 registry access 解析 tag 成员集合再判包含（延迟解析，非缓存）。
     * tag 不存在（数据包缺失）时返回 false —— 「无数据即不匹配」，不抛异常、不影响其它节点。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean matches(LivingEntity living) {
        return living.level().registryAccess()
                .lookupOrThrow(Registries.ENTITY_TYPE)
                .get(tag)
                .map(holderSet -> holderSet.contains((Holder) living.getType().builtInRegistryHolder()))
                .orElse(false);
    }

    /**
     * 条件段：{@code stat.condition.entity_type = "against %s"}，
     * 参数为 {@code stat.value.entity_type.<tagPath>}（值键化）；键缺失回落 humanize 英文。
     */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.entity_type",
                StatValueKey.of("entity_type", tag.location().getPath())));
    }
}
