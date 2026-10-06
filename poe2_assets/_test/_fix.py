# -*- coding: utf-8 -*-
"""A1 静默失效修复：按 name → 新调用形态 精确整行替换 StatGen.java。"""
import re, io, sys
P = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
src = open(P, encoding="utf-8").read()
OP = "AttributeModifier.Operation."

# (name, 原形态正则(捕获缩进+参数), 新行模板)  —— 只改运算/属性，不动 tags
EDITS = []

# ---- G1a: inc(...) → pctAdd(...)  [Percentage 零基容器，/100 + ADD_VALUE] ----
G1_INC = ["ATTACKS_WITH_ONE_HANDED_MARTIAL_WEAPON_HAVE_INCREASED_CHANCE_TO_INFLICT_AILMENTS",
          "INC_AILMENT_CHANCE", "INC_FOUND_QUANTITY", "INC_FOUND_RARITY",
          "INC_DAMAGE_IS_TAKEN_MANA", "INC_MANA_TAKE", "INC_PROJECTILE_PIERCE",
          "TO_CHAOS_RESISTANCE_2"]
for n in G1_INC:
    EDITS.append((n, rf"\binc\((map, StatIds\.{n},)", r"pctAdd(\1"))

# ---- G1b: valueMod(..., ADD_MULTIPLIED_BASE[, tags]) → pctAdd(..., [tags]) ----
for n in ["INC_BLOCK_CHANCE", "INC_RESERVATION_EFFICIENCY"]:
    EDITS.append((n, rf"valueMod\(map, StatIds\.{n}, (\S+?), {re.escape(OP)}ADD_MULTIPLIED_BASE\)",
                  rf"pctAdd(map, StatIds.{n}, \1)"))

# ---- G1c: sbMod(..., ADD_MULTIPLIED_BASE, ...) → ADD_VALUE（裸 $value，注入口径已是小数）----
for n in ["CHARM_RESIST_CHAOS", "CHARM_RESIST_COLD", "CHARM_RESIST_FIRE", "CHARM_RESIST_LIGHTNING",
          "CHARM_GOLDEN_RARITY", "TAKEN_CRIT"]:
    EDITS.append((n, rf"(sbMod\(map, StatIds\.{n}, \S+?, {re.escape(OP)})ADD_MULTIPLIED_BASE",
                  r"\1ADD_VALUE"))

# ---- G1d: entityMod(...) → 显式 ADD_VALUE 重载 ----
for n in ["TOTEMS_GAIN_TO_ALL_MAX_ELEMENTAL_RESISTANCES", "INC_MINION_ELEMENTAL_RESIST"]:
    EDITS.append((n, rf"entityMod\(map, StatIds\.{n}, (\S+?), (TargetSelector\.\w+)",
                  rf"entityMod(map, StatIds.{n}, \1, {OP}ADD_VALUE, \2"))

# ---- G1e: vsExposed(...) → 显式 ADD_VALUE 重载 ----
for n in ["INC_AILMENT_CHANCE_VS_EXPOSED_COLD", "INC_AILMENT_CHANCE_VS_EXPOSED_FIRE",
          "INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING"]:
    EDITS.append((n, rf"(vsExposed\(map, StatIds\.{n}, \S+?, )(AttributeRegister\.\w+)",
                  rf"\1\2, {OP}ADD_VALUE"))

# ---- G2/G3a: valueMod(..., ADD_MULTIPLIED_BASE[, tags]) → more(...)  [实数零基，乘总量] ----
for n in ["INC_DEXTERITY", "INCREASED_INTELLIGENCE", "INC_STRENGTH", "INC_MAX_ENERGY", "INC_ARMOUR"]:
    EDITS.append((n, rf"valueMod\(map, StatIds\.{n}, (\S+?), {re.escape(OP)}ADD_MULTIPLIED_BASE\)",
                  rf"more(map, StatIds.{n}, \1)"))
for n in ["INC_ARMOUR_ENERGY", "INC_ARMOUR_EVASION"]:
    EDITS.append((n, rf"valueMod\(map, StatIds\.{n}, (\S+?), {re.escape(OP)}ADD_MULTIPLIED_BASE, (.+?)\)",
                  rf"more(map, StatIds.{n}, \1, \2)"))

# ---- G2b: inc → more ----
EDITS.append(("INCREASED_SPIRIT", r"\binc\((map, StatIds\.INCREASED_SPIRIT,)", r"more(\1"))

# ---- G2c: entityMod → ADD_MULTIPLIED_TOTAL ----
EDITS.append(("COMPANIONS_GAIN_YOUR_STRENGTH",
              r"entityMod\(map, StatIds\.COMPANIONS_GAIN_YOUR_STRENGTH, (\S+?), (TargetSelector\.\w+)",
              rf"entityMod(map, StatIds.COMPANIONS_GAIN_YOUR_STRENGTH, \1, {OP}ADD_MULTIPLIED_TOTAL, \2"))

# ---- G3b: sbMod/sbModC armor → ADD_MULTIPLIED_TOTAL ----
EDITS.append(("ARMOR_REDUCTION",
              rf"(sbMod\(map, StatIds\.ARMOR_REDUCTION, \S+?, {re.escape(OP)})ADD_MULTIPLIED_BASE",
              r"\1ADD_MULTIPLIED_TOTAL"))
EDITS.append(("FORM_BEAR_ARMOR",
              rf"(sbModC\(map, StatIds\.FORM_BEAR_ARMOR, \S+?, [\d.]+, {re.escape(OP)})ADD_MULTIPLIED_BASE",
              r"\1ADD_MULTIPLIED_TOTAL"))

# ---- G4a: 穿透 5 条 → RESISTANCE_PENETRATION + pctAdd + elemental/resistance tags ----
PEN = {
 "ATTACK_DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES":
   "StatTag.RESISTANCE, StatTag.ELEMENTAL, StatTag.ATTACK",
 "DAMAGE_PENETRATES_ELEMENTAL_DAMAGE_RESISTANCES_FOR_EACH_TIME_YOU_VE_USED_A_SKILL_THAT_REQUIRES_GLORY_IN_THE_PAST_SECONDS":
   "StatTag.RESISTANCE, StatTag.ELEMENTAL, StatTag.GLORY",
 "DAMAGE_PENETRATES_OF_ENEMY_ELEMENTAL_RESISTANCES": "StatTag.RESISTANCE, StatTag.ELEMENTAL",
 "DAMAGE_PENETRATION_ELEMENTAL_DAMAGE_RESISTANCES": "StatTag.RESISTANCE, StatTag.ELEMENTAL",
 "DAMAGE_PENETRATION_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES_WHILE_SHAPESHIFTED":
   "StatTag.RESISTANCE, StatTag.ELEMENTAL",
}
for n, tags in PEN.items():
    EDITS.append((n, rf"inc\(map, StatIds\.{n}, AttributeRegister\.ADDED_DAMAGE(, [^)]*)?\)",
                  rf"pctAdd(map, StatIds.{n}, AttributeRegister.RESISTANCE_PENETRATION, {tags})"))

# ---- G4b: "deals Damage {X}% faster" → AILMENT_DAMAGE_RATE (base=1.0，inc 有效) ----
EDITS.append(("BLEEDING_YOU_INFLICT_DEALS_DAMAGE_FASTER",
  r"inc\(map, StatIds\.BLEEDING_YOU_INFLICT_DEALS_DAMAGE_FASTER, AttributeRegister\.ADDED_DAMAGE\)",
  "inc(map, StatIds.BLEEDING_YOU_INFLICT_DEALS_DAMAGE_FASTER, AttributeRegister.AILMENT_DAMAGE_RATE, StatTag.AILMENT, StatTag.BLEED)"))
EDITS.append(("DAMAGING_AILMENTS_DEAL_DAMAGE_FASTER",
  r"inc\(map, StatIds\.DAMAGING_AILMENTS_DEAL_DAMAGE_FASTER, AttributeRegister\.ADDED_DAMAGE\)",
  "inc(map, StatIds.DAMAGING_AILMENTS_DEAL_DAMAGE_FASTER, AttributeRegister.AILMENT_DAMAGE_RATE, StatTag.AILMENT)"))

# ---- G4c: Thorns ----
EDITS.append(("GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED",
  r"inc\(map, StatIds\.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED, AttributeRegister\.ADDED_DAMAGE\)",
  "inc(map, StatIds.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED, AttributeRegister.THORNS, StatTag.PHYSICAL)"))

# ---- penetrate helper: inc → pctAdd（一处修 4 条 PENETRATE_*）----
EDITS.append(("<helper:penetrate>",
  r"inc\(map, id, AttributeRegister\.RESISTANCE_PENETRATION, StatTag\.RESISTANCE, element\)",
  "pctAdd(map, id, AttributeRegister.RESISTANCE_PENETRATION, StatTag.RESISTANCE, element)"))

ok, fail = [], []
for name, pat, rep in EDITS:
    new, cnt = re.subn(pat, rep, src)
    if cnt == 0:
        fail.append(name)
    else:
        ok.append((name, cnt)); src = new
open(P, "w", encoding="utf-8").write(src)
print(f"applied={len(ok)}  failed={len(fail)}")
for n, c in ok: print(f"  OK   x{c}  {n}")
for n in fail: print(f"  FAIL       {n}")
