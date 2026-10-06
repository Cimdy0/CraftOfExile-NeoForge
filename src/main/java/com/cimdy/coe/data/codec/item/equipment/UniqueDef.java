package com.cimdy.coe.data.codec.item.equipment;

import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * 传奇装备（唯一）定义（数据驱动，文件化）：位于 {@code coe/unique/<baseItem>/<name>.json}，
 * 自动以父目录 {@code <baseItem>} 作为自身基础（读取该基础装备的模板/固有属性），
 * 再叠加本定义的词缀（additions）、签名（signature，可选）、模型路径（model，可选）。
 *
 * <p>JSON 形态：
 * <pre>{@code
 * {
 *   "lore": "...",
 *   "additions": [ ... ],
 *   "signature": [ ... ],
 *   "model": "coe:item/unique/doomsday"
 * }
 * }</pre>
 */
public record UniqueDef(
        String lore,
        List<Addition> additions,
        Optional<List<Addition>> signature,
        Optional<Identifier> model
) {
    public static final Codec<UniqueDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("lore").forGetter(UniqueDef::lore),
            Addition.CODEC.listOf().optionalFieldOf("additions", List.of()).forGetter(UniqueDef::additions),
            Addition.CODEC.listOf().optionalFieldOf("signature").forGetter(UniqueDef::signature),
            Identifier.CODEC.optionalFieldOf("model").forGetter(UniqueDef::model)
    ).apply(instance, UniqueDef::new));
}
