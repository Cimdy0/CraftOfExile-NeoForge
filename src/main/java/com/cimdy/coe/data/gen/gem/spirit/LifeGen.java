package com.cimdy.coe.data.gen.gem.spirit;

import com.cimdy.coe.data.codec.skill.helper.resource.ResourceCost;
import com.cimdy.coe.data.helper.hub.ReservationManager;
import com.cimdy.coe.data.codec.skill.SkillIds;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillEntry;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.gen.gem.skill.SkillPresets;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;

/**
 * 生命预留宝石生成 — 对齐 PoE2「献祭生命」机制（如 Blood Pact / 预留生命的辅助技能）。
 *
 * <p>生命预留宝石本质是预留生命（PRESERVE 类型 {@code ResourceCost(HEALTH)}）的光环/增益技能：
 * 施放时由 {@code ReservationManager} 登记 health 预留（{@code ResourceCost#runCost} 的 CAST 路径
 * 调用 {@code ReservationManager.reserve(caster, "health", N)}），技能停止时释放。
 * 这与精魂宝石（{@code ResourceCost(SPIRIT, PRESERVE)}）共用同一套预留登记通道，仅资源字符串不同。
 *
 * <p>数据驱动迁移（#87）：每颗宝石的完整 Skill 经 {@link SkillPresets#registerPreset} 登记为
 * {@code coe:preset_<gem>} 预设，宝石本体降为 {@code SkillRead(ref=preset_<gem>)} 引用之。
 */
public class LifeGen {
    public static void register(BootstrapContext<SkillEntry> bootstrap) {
        // 血之契约 Blood Pact：30 生命预留（生命预留源，当前无附加常驻增益）。
        // 数值 30 对齐精魂宝石预设（preset_clarity.json 等）的预留量，可后续调参。
        reg(bootstrap, "red/blood_pact",
                SpiritGemFactory.of(GemGen.strReq(), SpiritGemFactory.life(30),
                        List.of(GateTag.AURA, GateTag.BUFF, GateTag.PERSISTENT),
                        List.of(),
                        SpiritGemFactory.selfBuffPhase()));
    }

    /** 预设 id 命名：剥离颜色前缀，仅保留宝石本名（如 {@code red/blood_pact} → {@code preset_blood_pact}）。 */
    private static String presetName(String path) {
        return "preset_" + path.substring(path.indexOf('/') + 1);
    }

    /** 数据驱动迁移（#87）：登记完整 Skill 为预设，宝石本体经 SkillRead 引用之（发射 Read 标签）。 */
    private static void reg(BootstrapContext<SkillEntry> bootstrap, String path, Skill full) {
        SkillPresets.registerPreset(GemGen.id(presetName(path)), full);
        // 生命预留宝石本体路径经 SkillIds 登记（生成期按路径生成），供 SkillIds + SkillRead 引用。
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_SPIRIT_KEY, SkillIds.id(path)),
                SkillPresets.read(presetName(path)).withTier(TIER.getOrDefault(path, 1)));
    }

    /** 等阶（默认 1，生命预留宝石暂无非 1 项）。 */
    private static final Map<String, Integer> TIER = Map.of();
}
