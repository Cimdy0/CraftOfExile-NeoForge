package com.cimdy.coe.data.codec.talent.effect;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Stat 型天赋效果：持有一组 stat <b>条目</b>（{@link StatEntry} = 数值 + 条件 + 选择器 + 效果）。
 * <ul>
 *   <li>属性亚型条目（效果为 {@link ModifierStatEffect}）与触发亚型条目（heal / damage / status / ailment_*）
 *       统一并入玩家 {@link StatStore} 附件——属性在读属性时按作用域即时聚合，
 *       触发型在事件发生时经标签 + 条件过筛执行。</li>
 * </ul>
 * 全量重建由 {@link TalentEffectApplier} 在调用各效果前清空单一附件；本效果仅负责追加自身内容。
 * {@code icon} 供「多选一」选项选中后替换节点图标。
 */
public record StatTalentEffect(List<StatEntry> entries, Optional<String> icon) implements TalentEffect {

    public static final MapCodec<StatTalentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatEntry.CODEC.listOf().fieldOf("effects").forGetter(StatTalentEffect::entries),
            Codec.STRING.optionalFieldOf("icon").forGetter(StatTalentEffect::icon)
    ).apply(instance, StatTalentEffect::new));

    /** 无图标便捷构造。 */
    public StatTalentEffect(List<StatEntry> entries) {
        this(entries, Optional.empty());
    }

    /** 便捷工厂：一组条目。 */
    public static StatTalentEffect ofEntries(List<StatEntry> entries) {
        return new StatTalentEffect(new ArrayList<>(entries));
    }

    /** 便捷工厂：单条属性 + 常量数值，ADD_VALUE。 */
    public static StatTalentEffect of(Holder<Attribute> attribute, double amount) {
        return of(attribute, amount, Either.left(AttributeModifier.Operation.ADD_VALUE));
    }

    /** 便捷工厂：单条属性 + 常量数值 + 指定操作。 */
    public static StatTalentEffect of(Holder<Attribute> attribute, double amount,
                                      Either<AttributeModifier.Operation, AttributeLimiters.Operation> op) {
        return ofEntries(List.of(entry(attribute, amount, op)));
    }

    /** 构造一条常量属性条目（无标签、无条件）。
     * <p>全量重构：委托 {@link StatGen#talentAttr} 注册 concrete 条目并返回其 {@link StatRead} 引用。 */
    public static StatEntry entry(Holder<Attribute> attribute, double amount,
                                  Either<AttributeModifier.Operation, AttributeLimiters.Operation> op) {
        return StatGen.talentAttr(attribute, amount, op, java.util.Set.of());
    }

    @Override
    public String type() {
        return "stat";
    }

    @Override
    public void apply(Player player, Identifier treeId, String nodeId, int index) {
        if (entries.isEmpty()) return;
        // 以稳定来源键写入 STAT_STORE：coe.talent.<tree>.<node>.<index>#<entryIdx>
        // 重建时 TalentEffectApplier 按 "coe.talent." 前缀整体移除，无需跟踪列表。
        String base = "coe.talent." + treeId + "." + nodeId + "." + index;
        Map<String, StatEntry> additions = new LinkedHashMap<>();
        int i = 0;
        for (StatEntry e : entries) additions.put(base + "#" + (i++), e);
        StatStore store = player.hasData(AttachmentRegistry.STAT_STORE)
                ? player.getData(AttachmentRegistry.STAT_STORE) : StatStore.empty();
        player.setData(AttachmentRegistry.STAT_STORE, store.addModifiers(additions));
    }

    @Override
    public List<Component> describe() {
        return describe(Locale.ROOT);
    }

    @Override
    public List<Component> describe(Locale locale) {
        List<Component> result = new ArrayList<>();
        for (StatEntry e : entries) {
            result.addAll(e.describe(locale));
        }
        return result;
    }

    @Override
    public Optional<String> icon() {
        return icon;
    }
}
