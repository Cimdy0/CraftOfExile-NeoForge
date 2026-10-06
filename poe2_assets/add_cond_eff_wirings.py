#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Phase 12: build 4 pure-data COND primitives + 1 EFF variant, then wire 4 stats.

All backing primitives are ALREADY REGISTERED (verified):
  DebuffStatusTypes.INCISION (L153), DebuffStatusTypes.PRESENCE_MARK (L160),
  LivingStateModes.LOW_MANA (L17), LivingStateModes.DODGE_ROLLING (L27)
=> per skill §Phase4续 these COND are pure data: constant + one m.put(...), zero engine change.

NEW EFF: TAKEN_DAMAGE has no ADD_MULTIPLIED_TOTAL variant (confirmed by scan);
         TAKEN_DAMAGE base=1.0 non-zero, so a TOTAL variant is legal.

Safety: every edit is verified afterwards (counts must match expectations).
"""
import os, re, sys

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")


def rd(p): return open(p, encoding="utf-8").read()
def wr(p, s): open(p, "w", encoding="utf-8").write(s)

# ---------------- 1) COND constants ----------------
p = os.path.join(GEN, "StatConditionIds.java")
t = rd(p)
NEW_COND = """    // ── 纯数据 COND 原语（LivingStateModes / DebuffStatusTypes 均已注册，零引擎改动）──
    public static final Identifier COND_LOW_MANA_LIVING_STATE = id("low_mana_living_state");
    public static final Identifier COND_DODGE_ROLLING_LIVING_STATE = id("dodge_rolling_living_state");
    public static final Identifier COND_INCISION_STATUS = id("incision_status");
    public static final Identifier COND_PRESENCE_MARK_STATUS = id("presence_mark_status");
"""
assert NEW_COND not in t, "COND constants already present"
t = t.replace("\n}\n", "\n" + NEW_COND + "}\n", 1) if t.rstrip().endswith("}") else t
# precise: insert before the final closing brace
idx = t.rstrip().rfind("}")
t = t[:idx] + NEW_COND + t[idx:]
wr(p, t)
print(f"StatConditionIds: COND refs = {t.count('public static final Identifier COND_')}")

# ---------------- 2) COND registrations ----------------
p = os.path.join(GEN, "StatConditionGen.java")
t = rd(p)
NEW_REG = """        // ── 纯数据 COND 原语：LivingStateModes / DebuffStatusTypes 已注册，仅加数据条目 ──
        m.put(StatConditionIds.COND_LOW_MANA_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.LOW_MANA, 80.0)));
        m.put(StatConditionIds.COND_DODGE_ROLLING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.DODGE_ROLLING, 80.0)));
        m.put(StatConditionIds.COND_INCISION_STATUS, List.of(new StatusValue(DebuffStatusTypes.INCISION.id, CraftOfExile.EMPTY_LOCATION)));
        m.put(StatConditionIds.COND_PRESENCE_MARK_STATUS, List.of(new StatusValue(DebuffStatusTypes.PRESENCE_MARK.id, CraftOfExile.EMPTY_LOCATION)));
"""
assert NEW_REG not in t, "COND registrations already present"
anchor = "        return m;"
i = t.rfind(anchor)
assert i > 0, "return m; not found in StatConditionGen"
t = t[:i] + NEW_REG + t[i:]
wr(p, t)
print(f"StatConditionGen: m.put count = {t.count('m.put(StatConditionIds.')}")

# ---------------- 3) EFF constant (TAKEN_DAMAGE TOTAL) ----------------
p = os.path.join(GEN, "StatEffectIds.java")
t = rd(p)
NEW_EFF_ID = """    /** TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING 的乘算落点：TAKEN_DAMAGE base=1.0 非零基，此前缺 ADD_MULTIPLIED_TOTAL 变体。 */
    public static final Identifier EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE = id("add_multiplied_total_attribute_coe_taken_damage");
"""
assert NEW_EFF_ID not in t, "EFF id already present"
idx = t.rstrip().rfind("}")
t = t[:idx] + NEW_EFF_ID + t[idx:]
wr(p, t)
print(f"StatEffectIds: EFF refs = {t.count('public static final Identifier EFF_')}")

# ---------------- 4) EFF registration ----------------
p = os.path.join(GEN, "StatEffectGen.java")
t = rd(p)
NEW_EFF_REG = """        // 承伤侧乘算：TAKEN_DAMAGE base=1.0 非零基，供 TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING 接线。
        m.put(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE, new ModifierStatEffect(AttributeRegister.TAKEN_DAMAGE, Either.left(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), ModifierStatEffect.Mode.GAIN, TagExpr.ALWAYS));
"""
assert NEW_EFF_REG not in t, "EFF registration already present"
i = t.rfind("        return m;")
assert i > 0, "return m; not found in StatEffectGen"
t = t[:i] + NEW_EFF_REG + t[i:]
wr(p, t)
print(f"StatEffectGen: m.put count = {t.count('m.put(StatEffectIds.')}")

# ---------------- 5) wirings ----------------
WIRINGS = {
    "WireAilment.java": """        // 对受 Incision 影响的敌人 —— 你施加的流血幅度 increased（非承受侧；EFF 标签含 not(TAKEN)）
        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_BLEEDING_YOU_INFLICT_AGAINST_ENEMIES_AFFECTED_BY_INCISION, onTarget(StatConditionIds.COND_INCISION_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_BLEED_COE_AILMENT_MAGNITUDE_TAKEN));
""",
    "WireDamage.java": """        // 对被标记的敌人 —— increased 暴击率（COND_PRESENCE_MARK_STATUS 纯数据原语 + 目标侧求值）
        map.put(StatIds.INC_CRITICAL_HIT_CHANCE_AGAINST_MARKED_ENEMIES, onTarget(StatConditionIds.COND_PRESENCE_MARK_STATUS, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));
""",
    "WireDefense.java": """        // 低魔力时闪电抗性穿透（flat，RESISTANCE_PENETRATION 零基属性只走 ADD_VALUE）
        map.put(StatIds.DAMAGE_PENETRATION_LIGHTNING_RESISTANCES_IF_ON_LOW_MANA, self(StatConditionIds.COND_LOW_MANA_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCE_PENETRATION_LIGHTNING_RESISTANCE));
        // 闪避翻滚时受到更多/更少伤害（承伤侧乘算）
        // ⚠️ 极性待办：语料为 "Take 30% less Damage from Hits while Dodge Rolling"（负向），
        //    而归一化把 less→more 且未变号（value=+0.3）。op 族按 StatId 取 TOTAL，方向由注入侧决定，见 polarity_audit.json。
        map.put(StatIds.TAKE_MORE_DAMAGE_FROM_HITS_WHILE_DODGE_ROLLING, self(StatConditionIds.COND_DODGE_ROLLING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_TAKEN_DAMAGE));
""",
}
TAIL = re.compile(r"\n    \}\n\}\s*$")
for fname, block in WIRINGS.items():
    p = os.path.join(WIR, fname)
    t = rd(p)
    if "map.put(StatIds." in block and block.strip().split("\n")[-1] in t:
        print(f"  skip (already present): {fname}")
        continue
    m = TAIL.search(t)
    assert m, f"cannot locate build() tail in {fname}"
    t = t[:m.start()] + "\n" + block + t[m.start():]
    wr(p, t)
    print(f"  wired: {fname}")

# ---------------- 6) gate counts ----------------
p = os.path.join(ROOT, "scripts", "verify_gen.py")
t = rd(p)
t = t.replace('("condition count 52"', '("condition count 56"')
t = t.replace("len(cond_ids)==52 and cond_n==52", "len(cond_ids)==56 and cond_n==56")
t = t.replace('("effect count 554"', '("effect count 555"')
t = t.replace("len(effect_ids)==554 and eff_n==554", "len(effect_ids)==555 and eff_n==555")
wr(p, t)
print("verify_gen counts -> condition 56, effect 555")
print("\nALL EDITS APPLIED")
