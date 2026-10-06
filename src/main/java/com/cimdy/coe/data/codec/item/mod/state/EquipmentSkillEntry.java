package com.cimdy.coe.data.codec.item.mod.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 装备赋予的单个技能条目：技能本体 id + 其携带的辅助技能（support gems）列表。
 * 取代了原先 {@code Map<String, Identifier>} 中「技能 = 裸 id」的形态，使装备技能可挂载辅助宝石。
 *
 * <p>JSON 形态（作为 skills 表的值）：
 * <pre>{@code
 * "skills": {
 *   "key": { "id": "coe:preset_x", "supports": ["coe:support_a", "coe:support_b"] }
 * }
 * }</pre>
 * {@code supports} 缺省为空列表。
 */
public record EquipmentSkillEntry(Identifier id, List<Identifier> supports) {
    public static final Codec<EquipmentSkillEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("id").forGetter(EquipmentSkillEntry::id),
            Codec.list(Identifier.CODEC).optionalFieldOf("supports", List.of()).forGetter(EquipmentSkillEntry::supports)
    ).apply(i, EquipmentSkillEntry::new));
}
