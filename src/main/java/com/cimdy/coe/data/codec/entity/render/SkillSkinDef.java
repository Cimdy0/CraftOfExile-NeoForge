package com.cimdy.coe.data.codec.entity.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * 技能皮肤定义 —— 把「技能外观」从<b>伤害类型</b>解耦、改为<b>按技能 id 锁定</b>的桥接层。
 *
 * <p>历史问题：{@link com.cimdy.coe.entity.custom.skill.SkillArrow} 的描边与
 * {@link com.cimdy.coe.data.codec.entity.EntityRead} 的渲染引用都曾由伤害类型（{@code coe:fire}）
 * 派生 —— 于是「冰霜箭被转为火焰伤害」后外观仍跟着火焰走，违背「外观属于技能」的预期。
 * 本定义让每个技能（{@code coe:skill_skin/<skillPath>.json}）声明一份外观覆盖：
 * <ul>
 *   <li>{@code render} —— 底层实体渲染定义 id（如 {@code coe:fire}）；空 = 沿用投递默认；</li>
 *   <li>{@code tint} —— ARGB 描边/贴图染色；空 = 无覆盖；</li>
 *   <li>{@code particle} / {@code sound} —— 覆盖本技能施放粒子/音效；空 = 沿用 {@code Skill.castParticle/castSound}。</li>
 * </ul>
 * 字段顺序稳定（render → tint → particle → sound），与 JSON 写入顺序一致。
 */
public record SkillSkinDef(
        Optional<Identifier> render,
        Optional<Integer> tint,
        Optional<Identifier> particle,
        Optional<Identifier> sound
) {

    /** CODEC 工厂方法（项目约定：数据生成经 CODEC 工厂方法落盘）。 */
    public static final Codec<SkillSkinDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("render").forGetter(SkillSkinDef::render),
            Codec.INT.optionalFieldOf("tint").forGetter(SkillSkinDef::tint),
            Identifier.CODEC.optionalFieldOf("particle").forGetter(SkillSkinDef::particle),
            Identifier.CODEC.optionalFieldOf("sound").forGetter(SkillSkinDef::sound)
    ).apply(i, SkillSkinDef::new));

    /** 空皮肤（无覆盖）：render / tint / particle / sound 全空。 */
    public static SkillSkinDef empty() {
        return new SkillSkinDef(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
