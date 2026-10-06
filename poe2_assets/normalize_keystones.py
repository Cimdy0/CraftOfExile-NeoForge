#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Normalize the two safety-net-preserved keystones in KeystoneNodes01.java from the
multi-value `List.of(TalentNodeGen.stat(Map.of(A, v, B, v, ...)))` form to the same
factory form used by the regenerated nodes: `List.of(TalentNodeGen.stat(A, v), ...)`.
Whitespace-agnostic (uses \s* for indentation) so it works regardless of tabs/spaces.
Run: python normalize_keystones.py
"""
import re

P = r"E:\McModding\Modding\CraftOfExile-NeoForge\src\main\java\com\cimdy\coe\data\gen\talent\node\keystone\KeystoneNodes01.java"
s = open(P, encoding="utf-8").read()

IND = "                        "  # 24 spaces for inner stat lines (matches regenerated nodes)

hp_old = re.compile(
    r"List\.of\(TalentNodeGen\.stat\(Map\.of\(\s*"
    r"StatIds\.CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY, 1\.0,\s*"
    r"StatIds\.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL, 1\.0,\s*"
    r"StatIds\.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS, 0\.01,\s*"
    r"StatIds\.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS, 0\.001\)\)"
)
hp_new = (
    "List.of(\n"
    + IND + "TalentNodeGen.stat(StatIds.CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY, 1.0),\n"
    + IND + "TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_BASE_UNARMED_DAMAGE_PHYSICAL_DAMAGE_REPLACED_WITH_DAMAGE_BASED_ON_THEIR_SKILL_LEVEL, 1.0),\n"
    + IND + "TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_MORE_ATTACK_SPEED_PER_ITEM_EVASION_ON_EQUIPPED_ARMOR_ITEMS, 0.01),\n"
    + IND + "TalentNodeGen.stat(StatIds.UNARMED_ATTACKS_THAT_WOULD_USE_AN_EQUIPPED_QUARTERSTAFF_S_DAMAGE_HAVE_TO_CRITICAL_HIT_CHANCE_PER_ITEM_ENERGY_SHIELD_ON_EQUIPPED_ARMOR_ITEMS, 0.001))"
)
s2, nh = hp_old.subn(hp_new, s)

ws_old = re.compile(
    r"List\.of\(TalentNodeGen\.stat\(Map\.of\(\s*"
    r"StatIds\.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE, 0\.5,\s*"
    r"StatIds\.STORM_AND_PLANT_SPELLS_COST_MORE, -0\.5,\s*"
    r"StatIds\.STORM_AND_PLANT_SPELLS_HAVE_MORE_DURATION, -0\.75\)\)"
)
ws_new = (
    "List.of(\n"
    + IND + "TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_DEAL_MORE_DAMAGE, 0.5),\n"
    + IND + "TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_COST_MORE, -0.5),\n"
    + IND + "TalentNodeGen.stat(StatIds.STORM_AND_PLANT_SPELLS_HAVE_MORE_DURATION, -0.75))"
)
s3, nw = ws_old.subn(ws_new, s2)

print("hollow_palm(64601) matches:", nh, " wildsurge(49363) matches:", nw)
if nh == 1 and nw == 1:
    open(P, "w", encoding="utf-8", newline="\n").write(s3)
    print("WRITTEN")
else:
    print("NO WRITE - unexpected match count, abort")
