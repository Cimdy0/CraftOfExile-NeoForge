package com.cimdy.coe.data.codec.support.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * ADR-003 阶段1 · 伤害类型定义表（单一定义源，对抗「推论 B 静默失效」）。
 *
 * <p>承载一个伤害类型的全部元数据：槽位（{@code slot}，与 {@code damages[]} 下标对齐，
 * 0=物理 1=混沌 2=火 3=冰 4=电）、stat 作用域标签（{@code stat_tag}）、DoT 变体
 * （{@code dot}）、附加作用域标签（{@code extra_tags}）、ailment / bypass_energy 标记。
 *
 * <p>与 {@link com.cimdy.coe.data.codec.support.stat.tag.GateTag} 同源的设计：内置默认值只在
 * Java 侧定义一处（{@link DamageTypes#builtinDefaults()}），datagen 负责导出为数据包内容，
 * 从而既保证「无数据包也能跑」，又让新增伤害类型可被数据包扩展——不再是 8 个文件里写死的 "5"。
 *
 * <p>阶段1 只做定义表 + 装载 + 失败可见；下游 switch（{@code DamageAction}/{@code StatusBehavior}/
 * {@code DamagePipeline}）的查表改造属阶段2，不在本文件范围。
 */
public record DamageTypeDef(
        String id,
        int slot,
        String statTag,
        @Nullable Identifier dot,
        Set<String> extraTags,
        boolean bypassEnergyShield,
        boolean ailment,
        boolean synthetic
) {
    public static final Codec<DamageTypeDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(DamageTypeDef::id),
            Codec.INT.fieldOf("slot").forGetter(DamageTypeDef::slot),
            Codec.STRING.fieldOf("stat_tag").forGetter(DamageTypeDef::statTag),
            Identifier.CODEC.optionalFieldOf("dot").forGetter(d -> Optional.ofNullable(d.dot())),
            Codec.STRING.listOf().<Set<String>>xmap(list -> new HashSet<>(list), set -> new ArrayList<>(set))
                    .optionalFieldOf("extra_tags", Set.of()).forGetter(DamageTypeDef::extraTags),
            Codec.BOOL.optionalFieldOf("bypass_energy_shield", false).forGetter(DamageTypeDef::bypassEnergyShield),
            Codec.BOOL.optionalFieldOf("ailment", false).forGetter(DamageTypeDef::ailment),
            Codec.BOOL.optionalFieldOf("synthetic", false).forGetter(DamageTypeDef::synthetic)
    ).apply(i, (id, slot, statTag, dotOpt, extraTags, bes, ailment, synthetic) ->
            new DamageTypeDef(id, slot, statTag, dotOpt.orElse(null), extraTags, bes, ailment, synthetic)));

    /** 该伤害类型发射进技能 stat scope 的全部标签（stat 标签 + 附加作用域标签，小写、去重）。 */
    public Set<String> scopeTags() {
        Set<String> tags = new LinkedHashSet<>();
        if (statTag != null && !statTag.isEmpty()) tags.add(statTag.toLowerCase(Locale.ROOT));
        for (String t : extraTags) tags.add(t.toLowerCase(Locale.ROOT));
        return tags;
    }
}
