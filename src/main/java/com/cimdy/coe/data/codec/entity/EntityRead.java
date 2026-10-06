package com.cimdy.coe.data.codec.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 实体引用 —— 对 {@link EntityDef}（实体预设）的引用 + 可选覆写。
 *
 * <p>同 {@code SkillRead} 范式：一份 {@link EntityDef} 是可复用的<b>预设</b>，
 * 技能 / 投递在引用它时可覆写<b>外观</b>（{@code render}）与<b>名称</b>（{@code name}），
 * 从而「同一类实体、不同外观」无需各写一份完整定义 —— 这正是本层要制造的
 * 「新实体错觉」：玩家看到不同的东西，底层却是同一个 {@code EntityType}。
 *
 * <pre>{@code
 *   // 魔力残片：沿用通用残片行为，只换成奥术蓝外观与专属名字
 *   {"ref": "coe:remnant", "render": "coe:arcane_blue", "name": {"translate": "entity.coe.mana_remnant"}}
 * }</pre>
 *
 * @param ref    实体定义 id（引用 {@code coe:entity/<name>}）
 * @param render 渲染定义覆写；为空时沿用 {@link EntityDef} 自带的 {@code render}
 * @param name   显示名覆写；为空时沿用 {@link EntityDef} 自带的 {@code name}
 */
public record EntityRead(Identifier ref, Optional<Identifier> render, Optional<Component> name) {

    public static final Codec<EntityRead> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("ref").forGetter(EntityRead::ref),
            Identifier.CODEC.optionalFieldOf("render").forGetter(EntityRead::render),
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(EntityRead::name)
    ).apply(i, EntityRead::new));

    /** 纯引用（无覆写）：直接沿用预设的外观与名称。 */
    public static EntityRead of(Identifier ref) {
        return new EntityRead(ref, Optional.empty(), Optional.empty());
    }

    /** 仅覆写外观：最常见的用法（同一行为、不同视觉）。 */
    public static EntityRead withRender(Identifier ref, Identifier render) {
        return new EntityRead(ref, Optional.of(render), Optional.empty());
    }

    /**
     * 仅覆写外观（实例版）：返回新 {@link EntityRead}，render 置为给定 id，ref / name 沿用。
     * 供技能皮肤在装配期把投递的 render 引用改写为<b>技能 id</b>
     * （见 {@link com.cimdy.coe.data.codec.skill.core.Skill#withDeliveryRender} 与
     * 各投递类的 {@code withRender}）。
     */
    public EntityRead withRender(Identifier render) {
        return new EntityRead(ref, Optional.of(render), name);
    }

    /**
     * 取被引用实体定义的<b>形态标签</b> —— 投递 {@code statTags()} 的<b>真值源</b>。
     *
     * <p><b>为什么标签必须由实体定义提供</b>：形态标签（{@code hazard} / {@code remnant} /
     * {@code grenade} / {@code projectile}）是<b>形态</b>的属性，不是投递类型的属性 ——
     * 同一个投递可以指向不同形态（手雷 / 燃烧瓶），硬编码在投递里等于把分类维度写死，
     * 违反「分类维度禁硬编码」铁律。故此处按引用读出形态标签，
     * 换一份 {@code coe:entity} 数据即可换一套标签作用域，零 Java 改动。
     *
     * @param fallback 解析失败（id 写错 / 数据未加载）或形态未声明标签时的兜底集合 ——
     *                 缺这一步会让「数据写错」静默表现为「词缀全部失效」，难以定位
     */
    public static Set<String> statTags(EntityRead ref, Set<String> fallback) {
        if (ref == null) return fallback;
        Set<String> entityTags = ref.resolve()
                .map(EntityDef::tagSet)
                .orElse(Set.of());
        // 实体标签（元素 / 类型：cold…）与投递自述标签（分类维度：area/aoe/grenade…）应当<b>叠加</b>而非互斥：
        // 二者作用于同一命中上下文，合并才能让「冰霜 AoE 球」同时命中被冰霜与范围词缀放大。
        // 仅当实体完全无标签时回落投递兜底（covers coe:orb 等无标签形态），保持原语义。
        if (entityTags.isEmpty()) return fallback;
        Set<String> merged = new LinkedHashSet<>(entityTags);
        merged.addAll(fallback);
        return Set.copyOf(merged);
    }

    /**
     * 解析出运行时实体定义：取预设本体，再按 {@code render} / {@code name} 覆写外观与名称。
     *
     * @return 解析结果；预设缺失（id 写错 / 数据未生成）时返回空——
     *         调用方应跳过生成并在日志提示，而非静默生成一个无定义实体。
     */
    public Optional<EntityDef> resolve() {
        return EntityDefs.get(ref).map(def -> {
            // 技能皮肤：投递的 render 引用可能是「技能 id」，须先经 SkillSkinDefs 还原为底层渲染定义 id。
            EntityDef applied = render.map(SkillSkinDefs::resolveRender).map(def::withRender).orElse(def);
            return name.map(applied::withName).orElse(applied);
        });
    }
}
