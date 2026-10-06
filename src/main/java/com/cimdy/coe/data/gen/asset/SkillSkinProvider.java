package com.cimdy.coe.data.gen.asset;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDef;
import com.cimdy.coe.data.codec.entity.render.SkillSkinDefs;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 技能皮肤数据生成器 —— 为 {@link GemGen#SKILL_IDS} 的<b>每一个</b>主动技能，
 * 在 {@code data/<ns>/coe/skill_skin/<path>.json} 落一份 {@link SkillSkinDef}。
 *
 * <p>render 取自 gen 期由 {@link SkillSkinDefs#recordRender} 抓取的「当前底层渲染 id」
 * （与今日外观一致，迁移零视觉回归）；tint / particle / sound 全部留空（沿用默认）。
 * 无实体投递的技能（范围/自身/光束）render 为空，仅占位——此类技能外观不由本机制驱动。
 *
 * <p><b>故 render 为空是正常态，不代表 datagen 失败</b>：本表 137 项中仅 48 项有实体投递，
 * 只有它们录得到底层渲染；其余 89 项投递类型为 area / beam / self，本就无投递物可渲染。
 * 判据是「有无实体投递」，与本机制是否工作无关。
 *
 * <p>与 {@link EntityProvider} 同范式：经 {@code DataProvider.saveStable} 写出，
 * 路径与 {@code LoadEvent} 的 {@code coe/skill_skin} 扫描同源。
 */
public class SkillSkinProvider implements DataProvider {

    private final PackOutput output;

    public SkillSkinProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("skill_skin");
        CompletableFuture<?>[] futures = new CompletableFuture<?>[GemGen.SKILL_IDS.size()];
        int i = 0;
        for (Identifier id : GemGen.SKILL_IDS) {
            Identifier currentRender = SkillSkinDefs.GEN_RENDER.get(id);
            SkillSkinDef def = new SkillSkinDef(
                    Optional.ofNullable(currentRender),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());
            Path file = dir.resolve(id.getPath() + ".json");
            futures[i++] = DataProvider.saveStable(cache, SkillSkinDef.CODEC, def, file);
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "skill_skin";
    }
}
