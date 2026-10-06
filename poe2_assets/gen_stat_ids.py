# -*- coding: utf-8 -*-
"""第 4 步：由 stat_templates.txt 重建 StatIds.java（语料条目）+ 吸收实际生成的 stat_entry 名。

StatIds 是 Stat 路径的 *唯一* 缓存类：既缓存语料驱动的 StatGen 注册 id（coe:<snake>），
也吸收 runData 实际产出的全部 stat_entry 名（coe:<entry_name>），并向外提供 byName(path)
供动态生成点（异常面 / status 面循环、offering 等）经字段缓存引用，替代裸 fromNamespaceAndPath。

处理流水线（顺序不可换）：
  [语料阶段] 同前：拆 CamelCase 粘连标签词 -> MC 术语替换 -> increased->inc -> 极性归一 -> 生成 name/id。
  [stat_entry 阶段] 读 src/generated/.../stat_entry/*.json 文件名 -> sanitize 为常量名 ->
      与语料同 path 者去重跳过（避免重复字段），不同 path 同名者追加 _N 后缀防重 ->
      生成 id("<entry_name>") 字段并入本类。
  byName(path)：按真实 path（含连字符）反查，缺失即抛错（fail-fast）。

StatIds 直接持有全部 stat 路径常量，不再经「Ids -> StatPaths」第二层委托（StatPaths 已移除）。

  # 短语优先于单词，避免 Accuracy/Evasion 的 Rating 先被拆掉
  # 缩写：INCREASED 是 inc 计算符的全拼，统一缩为 inc（与 StatIds 既有 INC_* 命名族一致）。
"""
import os
import glob
import re

TEMPLATES = "poe2_assets/stat_templates.txt"
ENTRY_DIR = "src/generated/resources/data/coe/coe/stat/entry"
OUT = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"

# 代码显式引用的 stat id（非语料驱动）：由 RecoveryStatEntries / DamageStatEntries 等经 StatIds.<NAME>
# 注册为 stat_entry，runData 会为其生成 stat_entry/<name>.json。但 StatIds 自身由 stat_entry 目录生成，
# 二者存在自举依赖——若目录尚未包含这些文件，StatIds 缺字段 → 编译失败 → runData 无法运行 → 死锁。
# 此处显式兜底，保证 StatIds 在任何 checkout 状态下均可编译（runData 生成文件后 Phase 2 自动去重跳过）。
EXTRA_IDS = [
    "extra_physical",
    "converts_all_evasion_rating",
    "recover_life_heavy_stun",
    "recover_life_heavy_stun_heavystun",
    "recover_life_killing_poisoned",
    "recover_life_killing_poisoned_poison",
    "recover_life_minions_is",
    "recover_life_minions_is_minion",
    # 手书特殊 stat entry（内联接线，无对应 stat_entry JSON；代码显式引用，须保留以防 regen 丢失）
    "incision_bleed_chance_on_hitter",
    "gain_elemental_infusion",
    "chance_to_avoid_death_from_hit_damage",
    "archon_buffs_also_grant_inc_critical_damage_bonus",
    "archon_buffs_also_grant_inc_critical_hit_chance",
    # wildsurge_incantation 风暴/植物法术：消耗/持续时间（节点值驱动幅度，手写 StatIds 常量须保留防 regen 丢失）
    "storm_spells_have_50_percent_less_cost",
    "plant_spells_have_50_percent_less_cost",
    "storm_spells_have_75_percent_less_duration",
    "plant_spells_have_75_percent_less_duration",

    "inc_socketed_gem_level",
    "inc_spell_crit_chance",
    "thorns",
    "add_physical_damage_reduction",
    "inc_physical_damage_reduction",
    "damage_taken_gained_as_life",
    "reduced_extra_damage_from_crits",
    "reduced_physical_damage_taken",
    "reduced_ailment_duration",
    "life_gained_from_enemy_death",
    "mana_gained_from_enemy_death",
    "life_gain_per_target",
    "life_gain_per_target_attacks",
    "percent_damage_goes_to_mana",
    "presence_radius",
    "presence_crit_chance",
    "presence_crit_damage",
    "presence_attack_speed",
    "presence_cast_speed",
    "presence_life_regen",
    "presence_damage",
    "presence_cold_damage",
    "presence_fire_damage",
    "presence_lightning_damage",
    "presence_physical_damage",
    "presence_all_elemental_resistances",
    "add_cold_damage_to_attacks",
    "add_fire_damage_to_attacks",
    "add_lightning_damage_to_attacks",
    "add_physical_damage_to_attacks",
    "gain_cold_damage_as_extra",
    "gain_fire_damage_as_extra",
    "gain_lightning_damage_as_extra",
    "gain_physical_damage_as_extra",
    "armour_applies_elemental_damage",
    "evasion_applies_to_deflection",
    "light_radius",
    "trap_and_mine_throw_speed",
    "inc_minion_max_life",
    "inc_damage_with_bow_skills",
    "inc_flammability_magnitude",
    "inc_max_spirit",
    "reduced_attribute_requirements",
    "reduced_chill_duration",
    "reduced_freeze_duration",
    "reduced_ignite_duration",
    "reduced_poison_duration",
    "reduced_bleeding_duration",
    "reduced_shock_duration",
    "belt_charm_duration",
    "belt_flask_recovery_rate",
    "belt_charm_charges_gained",
    "belt_charm_charges_used",
    "belt_flask_charges_gained",
    "belt_flask_charges_used",
    "belt_flask_life_recovery_rate",
    "belt_flask_mana_recovery_rate",
    "flask_recovery_amount",
    "flask_recovery_speed",
    "flask_charges_used",
    "flask_gain_charge",
    "flask_num_charges",
    "flask_recharge_rate",
    "flask_chance_gain_charge_on_kill",
    "flask_charges_gained",
    "flask_grant_guard",
    "flask_recover_life",
    "flask_recover_mana",
    "charm_duration",
    "charm_charges_gained",
    "charm_charges_used",
    "charm_gain_charge",
    "charm_grant_guard",
    "surpassing_chance_additional_arrow",
    "leech_physical_attack_damage_as_life",
    "leech_physical_damage_as_life",
    "leech_physical_damage_as_mana",]

# 短语优先于单词，避免 Accuracy/Evasion 的 Rating 先被拆掉
REPLACEMENTS = [
    ("Accuracy Rating", "Accuracy"),
    ("Evasion Rating", "Evasion"),
    ("Armour", "Armor"),
    ("Life", "Health"),
    ("Maximum", "Max"),
]


GLUE = re.compile(r"\b[A-Z][a-z]+(?:[A-Z][a-z]+)+\b")

# 全大写连续段（缩写），如 ESRecharge 里的 "ES"。
ACRONYM_RUN = re.compile(r"[A-Z]{2,}")


def pick_tag_side(inner):
    """[Left|Right] 方括号标签取词规则（D2，语料与节点映射共用同一份）。

    规则（按序）：
      1. 词数多的一侧胜出——展示文本通常比标签 id 展开得更完整。
      2. 词数相等时优先「非缩写粘连」的一侧。
         形如 [ESRecharge|Recharge]：两侧都是 1 个词，旧规则按字符长度取了
         `ESRecharge`，把 *原始标签名* 直接烤进 path
         （energy_shield_does_not_esrecharge）。带全大写缩写段的一侧是 id 而非可读英文，
         必须让位给展示文本 `Recharge`（反向同理）。
         注意 [LifeRecharge|Recharges] 不受影响：普通 CamelCase、无全大写段，
         仍走规则 3，由 split_glue 展开成 `Life Recharge`。
      3. 其余（都是/都不是缩写粘连）维持原字符长度启发式。
    """
    if "|" not in inner:
        return inner
    left, right = inner.split("|", 1)
    if len(right.split()) > len(left.split()):
        return right
    if len(left.split()) > len(right.split()):
        return left
    l_acro, r_acro = bool(ACRONYM_RUN.search(left)), bool(ACRONYM_RUN.search(right))
    if l_acro and not r_acro:
        return right
    if r_acro and not l_acro:
        return left
    return left if len(left) >= len(right) else right


def split_glue(t):
    """拆 CamelCase 粘连标签词：HitDamage -> Hit Damage（小写后接大写处插空格）。"""
    return GLUE.sub(lambda m: re.sub(r"(?<=[a-z])(?=[A-Z])", " ", m.group(0)), t)


def mc_replace(t):
    """MC 术语替换（大小写不敏感）。须在 split_glue 之后调用。"""
    for a, b in REPLACEMENTS:
        t = re.sub(re.escape(a), b, t, flags=re.IGNORECASE)
    return t


def normalize(t):
    t = mc_replace(split_glue(t))
    # 缩写：INCREASED 是 inc 计算符的全拼，统一缩为 inc（与 StatIds 既有 INC_* 命名族一致）。
    # 仅对独立单词 increased 替换，避免误伤其他词；to_name/to_id 随后统一大小写。
    t = re.sub(r"\bincreased\b", "inc", t, flags=re.IGNORECASE)
    return t


def _tokens(t):
    """模板 -> id token 序列（大小写归一、标点剔除、数值占位符 {X} 剔除）。

    注意 XM（"X metres" 缩写）是独立 token，不等于占位符 X，保留。
    """
    s = t.replace("{X}", "x").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return [w for w in s.split() if w and w != "x"]


def dedupe_adjacent(words):
    """折叠相邻重复 token（D3）。

    语料形如 `Life [LifeRecharge|Recharges] ...`：标签取词侧 `LifeRecharge` 经
    split_glue -> `Life Recharge`，随后 MC 术语替换把前置 `Life` 与 `LifeRecharge`
    里的 `Life` 双双换成 `Health`，得到 `Health Health Recharge`——同一个词被说了两遍。
    落在 id 上就是 `health_health_recharge_...`。id 只承载语义，重复前缀不携带新语义，
    故折叠为 `health_recharge_...`。

    只折叠 *相邻* 重复，非相邻的重复 token 一律保留（那可能是真实语义，如
    "damage ... damage" 分属不同短语）。
    """
    out = []
    for w in words:
        if not out or out[-1] != w:
            out.append(w)
    return out


def to_name(t):
    words = dedupe_adjacent(_tokens(t))
    return "_".join(w.upper() for w in words)


def to_id(t):
    return "_".join(dedupe_adjacent(_tokens(t)))


def to_id_uncollapsed(t):
    """D3 折叠前的 to_id 结果。

    只用于判定「Java 侧仍停留在修复前旧 id」的记录（旧 path == 本函数结果 且
    != to_id 结果时，说明该 path 是本次修复要纠正的对象），不要把修复扩散到
    Java 侧因碰撞加 _N 后缀等正常差异上。
    """
    return "_".join(_tokens(t))


def sanitize(name):
    """把 stat_entry 名转为合法 Java 常量名：全大写、'-' -> '_'，首字符为数字加前缀 '_'（防重在调用方处理）。"""
    s = name.upper().replace("-", "_")
    if s and s[0].isdigit():
        s = "_" + s
    return s


HEADER = """package com.cimdy.coe.data.codec.support.stat;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Stat 路径唯一缓存词表：集中缓存全部 Stat 引用（StatGen 注册 id 与 stat_entry 名）的 {@link Identifier}。
 * 数据生成（StatGen 注册）与所有引用方（AdditionType / StatusBehavior / SkillFactory / 天赋节点 /
 * 异常面动态生成 / offering 等）统一经本类字段常量 + {@link #byName(String)} 引用，
 * 避免散落的字符串字面量造成拼写漂移与改名遗漏。
 *
 * <p>本类由 poe2_assets/gen_stat_ids.py 生成，勿手改；重跑 runData + 本脚本即可刷新。
 * 条目两类来源：
 *   (1) 语料（stat_templates.txt，源自 poe2_tree.json 清洗）经 [Tag|Text] 取词丰侧 -> {X} 剔除 ->
 *       极性归一 -> CamelCase 拆分 -> MC 术语替换 后生成；
 *   (2) runData 实际产出的 stat_entry/*.json 文件名，吸收为 id("&lt;entry_name&gt;") 字段，覆盖全部 stat 条目。
 * 两类同 path 者去重（语料优先），byName(path) 按真实路径（含连字符）反查，缺失即抛错。
 *
 * <p>StatIds 为 Stat 路径的唯一缓存类，不再经「Ids -> StatPaths」第二层委托（StatPaths 已移除）。
 */
public final class StatIds {
"""

FOOTER = """
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }

    /**
     * 按 stat_entry 名（即真实路径，含连字符）查缓存路径：供动态生成点（异常面 / status 面循环、offering 等）
     * 经字段缓存引用，替代裸 {@code Identifier.fromNamespaceAndPath(ns, "ailment_buildup_"+t.id)}。
     * 缺失即抛错（fail-fast），避免拼写漂移导致的静默悬空。
     */
    private static final Map<String, Identifier> BY_NAME = new HashMap<>();

    static {
        for (var f : StatIds.class.getDeclaredFields()) {
            if (f.getType() == Identifier.class) {
                try {
                    Identifier id = (Identifier) f.get(null);
                    // 按真实路径（含连字符，如 livingmod_..._op_add_value_-0_8）建索引，
                    // 与字段名脱钩——字段名已 sanitize（'-' -> '_'）以满足 Java 标识符规则。
                    BY_NAME.put(id.getPath(), id);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static Identifier byName(String name) {
        Identifier r = BY_NAME.get(name);
        if (r == null) {
            throw new IllegalStateException("StatIds 未缓存 stat_entry: " + name);
        }
        return r;
    }

}
"""


def main(dry_run=False):
    raw = [l.rstrip("\n") for l in open(TEMPLATES, encoding="utf-8") if l.strip()]
    # 拆粘连标签词 -> MC 术语替换
    templ = [normalize(t) for t in raw]

    used_paths = set()
    used_names = {}
    entries = []
    collisions = 0

    # ---- Phase 0: 显式兜底 stat id（破解 runData <-> StatIds 自举死锁）----
    for path in EXTRA_IDS:
        name = sanitize(path)
        if name in used_names or path in used_paths:
            continue
        used_names[name] = True
        used_paths.add(path)
        entries.append((name, path, f"explicit code-referenced stat id -> coe:{path}", False))

    # ---- Phase 1: 语料条目 ----
    for t in templ:
        base_name = to_name(t)
        base_id = to_id(t)
        name = base_name
        eid = base_id
        k = 2
        while name in used_names or eid in used_paths:
            name = f"{base_name}_{k}"
            eid = f"{base_id}_{k}"
            k += 1
            collisions += 1
        used_names[name] = True
        used_paths.add(eid)
        entries.append((name, eid, t, False))

    # ---- Phase 2: stat_entry 文件名吸收 ----
    se_count = 0
    se_dup = 0
    se_added = 0
    if os.path.isdir(ENTRY_DIR):
        files = sorted(glob.glob(os.path.join(ENTRY_DIR, "*.json")))
        se_count = len(files)
        for f in files:
            path = os.path.basename(f)[:-5]  # 剥 .json
            if path in used_paths:
                # 语料已产出同名同路径条目，跳过（避免重复字段）
                se_dup += 1
                continue
            name = sanitize(path)
            cand = name
            k = 2
            while cand in used_names:
                cand = f"{name}_{k}"
                k += 1
                collisions += 1
            used_names[cand] = True
            used_paths.add(path)
            entries.append((cand, path, f"stat_entry/{path}.json -> coe:{path}", True))
            se_added += 1
    else:
        print(f"WARN: 未找到 {ENTRY_DIR}，请先 runData 生成 stat_entry")

    if dry_run:
        print(f"templates:        {len(templ)}")
        print(f"stat_entry files: {se_count} (dup-skipped: {se_dup}, added: {se_added})")
        print(f"total entries:    {len(entries)}")
        print(f"collisions:       {collisions}")
        print("\n--- sample corpus first 6 ---")
        n = 0
        for name, eid, c, is_se in entries:
            if not is_se:
                print(f"  {name} = id(\"{eid}\")  // {c}")
                n += 1
                if n >= 6:
                    break
        print("\n--- sample stat_entry first 6 ---")
        n = 0
        for name, eid, c, is_se in entries:
            if is_se:
                print(f"  {name} = id(\"{eid}\")  // {c}")
                n += 1
                if n >= 6:
                    break
        return

    lines = [HEADER]
    for name, eid, c, is_se in entries:
        lines.append(f"    // {c}")
        lines.append(f"    public static final Identifier {name} = id(\"{eid}\");")
        lines.append("")
    lines.append(FOOTER)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"written {len(entries)} entries to {OUT} "
          f"(corpus={len(templ)}, stat_entry added={se_added}, dup-skipped={se_dup}, collisions={collisions})")


if __name__ == "__main__":
    import sys
    main(dry_run="--dry" in sys.argv)
