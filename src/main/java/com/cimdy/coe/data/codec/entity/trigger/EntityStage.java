package com.cimdy.coe.data.codec.entity.trigger;

import com.cimdy.coe.data.codec.entity.EntityDef;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 阶段式实体的<b>单个阶段</b> —— 「有些实体会随时间逐步长大 / 变形」这一通用事实的最小数据单元。
 *
 * <p>一个「阶段序列」由若干本记录按<b>顺序</b>排列而成（见 {@link EntityDef#stages()}）：
 * <pre>{@code
 *   "stages": [ {"id": "sprout", "ticks": 40}, {"id": "grown", "ticks": 60}, {"id": "bloom"} ]
 * }</pre>
 * 序列首尾即语义：下标 0 是<b>初生阶段</b>，末位是<b>成熟阶段</b>（final stage）。
 *
 * <h2>为什么是「通用阶段」而不是「植物成长」</h2>
 * 「唤出来的东西会分阶段变化」在 PoE2 里反复出现：有毒藤蔓的长成、图腾的形态、召唤物的成熟、
 * 投射物的二次阶段 … 它们共享同一件事实 —— <b>一个实体有一条有序的阶段序列，并且会沿序列推进</b>。
 * 本记录只承载这一件事（叫什么 + 停留多久），不含任何植物 / 成长语义，故设施面是通用的：
 * 谁是阶段式实体、每阶段多久、现有几条阶段，全部由数据声明，Java 侧不出现任何类别字面量。
 *
 * @param id    阶段 id（序列内唯一；供未来「处于某阶段」的判定 / 表现分层按名引用）
 * @param ticks 本阶段的停留时长（tick）；&le;0 表示本阶段不驻留（下一次 tick 立即推进）
 */
public record EntityStage(String id, int ticks) {

    /** 缺省阶段停留时长（tick）：与 PoE2「约 1 秒一跳」的成长手感同量级。 */
    public static final int DEFAULT_TICKS = 20;

    public static final Codec<EntityStage> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(EntityStage::id),
            Codec.INT.optionalFieldOf("ticks", DEFAULT_TICKS).forGetter(EntityStage::ticks)
    ).apply(i, EntityStage::new));

    /** 缺省时长的便捷构造（数据 / datagen 只关心阶段名时用）。 */
    public EntityStage(String id) {
        this(id, DEFAULT_TICKS);
    }

    @Override
    public String toString() {
        return id;
    }
}
