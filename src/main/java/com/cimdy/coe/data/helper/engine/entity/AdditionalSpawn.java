package com.cimdy.coe.data.helper.engine.entity;

import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>额外生成掷骰</b>（通用设施）。
 *
 * <p>PoE2 里「X% 概率额外生成一个」是反复出现的维度：额外裂隙 / 额外投射物 / 额外图腾 / 额外残片 …
 * 本设施把它做成一条<b>与具体类别无关</b>的通用链路，由两个 INERT 标记配合：
 * <ul>
 *   <li>{@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}：概率（1.0 = 100%）。
 *       &gt;1 按 PoE2 语义拆为「保底次数 + 余概率」（如 2.5 → 必得 2 次 + 50% 再 1 次）；</li>
 *   <li>{@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}：每次命中的额外个数（未设按 1）。</li>
 * </ul>
 *
 * <p><b>类别身份由 {@code conditionTags} 承载</b>（经 {@link EntityCategoryLimit#tagsOf} 单一来源产出），
 * 于是「裂隙额外生成」「图腾额外生成」「残片额外生成」共用同一设施、只换标签，零 Java 改动。
 *
 * <p>掷骰口径与既有的 {@code EntityBehaviors#detonate}（手雷二次激活）一致：
 * 取施法者 {@code getRandom()} 的 {@code nextDouble()}。
 */
public final class AdditionalSpawn {

    private AdditionalSpawn() {
    }

    /**
     * 掷骰：本次生成应<b>额外</b>生成几个。
     *
     * @param owner    施法者（标记读取主体）
     * @param category 被生成实体的类别（{@link EntityCategoryLimit#categoryOf} / {@link EntityCategoryLimit#categoryFor} 产出）
     * @param rng      随机源；null 时回退 {@code owner.getRandom()}
     * @return 额外个数（0 = 未命中）
     */
    public static int rollExtra(LivingEntity owner, Identifier category, RandomSource rng) {
        java.util.Set<String> tags = EntityCategoryLimit.tagsOf(category);
        int hits = rollCount(owner, "additional_spawn_chance", tags, rng);
        if (hits <= 0) return 0;
        double magnitude = StatEngine.magnitudeOf(owner,
                "additional_spawn_count", tags);
        int per = magnitude > 0.0 ? Math.max(1, (int) Math.floor(magnitude + 0.5)) : 1;
        return hits * per;
    }

    /**
     * <b>通用「几次命中」掷骰</b>：取一笔概率标记幅度，按 PoE2 语义拆为「<b>保底次数 + 余概率</b>」，返回命中次数。
     *
     * <p><b>为什么单独抽出</b>：PoE2 的概率类标记（额外生成 / 阶段跳过 / 提前推进 …）若各写一套
     * {@code floor}/{@code rest} 语义，迟早出现「一处认为 1.0 是 100%，另一处认为 1.0 要再掷一次」的分叉。
     * 故「>1 的拆解」只在本方法一处存在，消费端（{@link #rollExtra} /
     * {@link EntityStages#applySpawnRoll} / {@link EntityStages#tick}）一律复用。
     *
     * @param owner 施法者（标记读取主体）；null → 0
     * @param kind  概率标记种类（幅度语义必须是「1.0 = 100%」）
     * @param tags  身份查询集（须 ⊇ 目标 EFF 实例的 conditionTags）
     * @param rng   随机源；null 时回退 {@code owner.getRandom()}
     * @return 命中次数（0 = 未命中；chance=2.5 → 必得 2 次 + 50% 再 1 次）
     */
    public static int rollCount(LivingEntity owner, String type,
                                java.util.Set<String> tags, RandomSource rng) {
        if (owner == null) return 0;
        return StatEngine.rollCount(owner, type, tags, rng);
    }
}
