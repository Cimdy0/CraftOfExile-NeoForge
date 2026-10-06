package com.cimdy.coe.data.codec.skill.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 技能形态注册表（Totem / Trap / Mine 等 PoE2 形态骨架，ADR 阶段 2 再补运行时行为）。
 *
 * <p>数据驱动（铁律 2：分类维度禁硬编码枚举）。内置默认 {totem, trap, mine}，并可由
 * {@code coe/skill_form/<id>.json} 扩展（{@link #load(ResourceManager)} 预留）。形态名即其
 * stat 标签（小写），使 {@link SkillLinkResolver} 的 {@code allow_type} 表达式可针对形态判定兼容性，
 * 且「[形态] 伤害提高」类修饰符可经技能 stat scope 命中。
 *
 * <p>本类只承载「形态是否被识别」这一元信息；图腾/陷阱/地雷的实体生成、部署、引爆等运行时行为
 * 属于独立的机制实现（当前为 no-op 占位），须先有本骨架才能为对应技能写 {@code form} 数据。
 */
public final class SkillForms {

    /** 单条形态定义：{@code id} 为 JSON 取值（小写索引），{@code tag} 为发射进技能 stat scope 的标签。 */
    public record SkillFormDef(String id, String tag) {
        public String statTag() {
            return tag.toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<SkillFormDef> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(SkillFormDef::id),
            Codec.STRING.optionalFieldOf("tag", "").forGetter(SkillFormDef::tag)
    ).apply(i, (id, tag) -> new SkillFormDef(id, tag.isEmpty() ? id : tag)));

    private static final Map<String, SkillFormDef> BY_ID = new HashMap<>();

    static {
        // 内置默认形态（与 PoE2 一致）；数据包可用 coe/skill_form/<id>.json 追加
        register("totem");
        register("trap");
        register("mine");
    }

    private static void register(String id) {
        BY_ID.put(id.toLowerCase(Locale.ROOT), new SkillFormDef(id, id));
    }

    /** 注册/覆盖一个形态定义（小写 id 索引）。 */
    public static void registerDef(SkillFormDef def) {
        BY_ID.put(def.id().toLowerCase(Locale.ROOT), def);
    }

    /** 查询形态定义；未知返回 null（调用方 fail-open）。 */
    @Nullable
    public static SkillFormDef defOf(String id) {
        return id == null ? null : BY_ID.get(id.toLowerCase(Locale.ROOT));
    }

    /** 是否为已登记的技能形态。 */
    public static boolean isForm(String id) {
        return defOf(id) != null;
    }

    /** 装载期从 {@code coe/skill_form/*.json} 扩展内置表（可选；缺失不报错）。当前仅内置默认，故为空实现。 */
    public static void load(ResourceManager manager) {
        // 预留：遍历 coe/skill_form/ 下的 JSON，逐条 registerDef。当前无数据包扩展，保持 no-op。
    }
}
