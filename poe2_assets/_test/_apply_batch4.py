#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch-4 (FIXED): add explicit scope/condition StatTag to subject-scoped Wave-12 StatGen writers.
BUG FIX vs prior run: detect EXISTING tags by parsing StatTag.X / "x" tokens, NOT by bare
substring (the StatIds constant name itself often contains the scope word, e.g.
StatIds.ARROWS_PIERCE... contains "ARROW" -> false skip). Idempotent.
"""
import re

STGEN = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"

EDITS = {
    "ARROWS_PIERCE_AN_ADDITIONAL_TARGET": ["StatTag.ARROW", "StatTag.PIERCE"],
    "GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME": ["StatTag.GRENADE"],
    "IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU": ["StatTag.IGNITE"],
    "ENEMIES_YOU_INFLICT_BLEEDING_ON_CANNOT_REGENERATE_HEALTH": ["StatTag.BLEED"],
    "INVOCATED_SKILLS_HAVE_INCREASED_MAX_ENERGY": ["StatTag.ENERGY"],
    "HEALTH_LEECH_HEALTH_FASTER": ["StatTag.LEECHING"],
    "HEALTH_LEECH_HEALTH_SLOWER": ["StatTag.LEECHING"],
    "TO_LEVEL_OF_ALL_CHAOS_SKILLS": ["StatTag.CHAOS"],
    "TO_LEVEL_OF_ALL_COLD_SKILLS": ["StatTag.COLD"],
    "OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD": ["StatTag.HEALTH", "StatTag.ENERGY_SHIELD"],
    "HEALTH_FLASKS_GAIN_CHARGES_PER_SECOND": ["StatTag.FLASK", "StatTag.HEALTH"],
    "MANA_FLASKS_GAIN_CHARGES_PER_SECOND": ["StatTag.FLASK", "StatTag.MANA"],
    "TO_MAX_LIGHTNING_INFUSIONS": ["StatTag.LIGHTNING", "StatTag.INFUSION"],
    "INCREASED_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS": ["StatTag.IMPALE", "StatTag.SPELL"],
    "PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW": ["StatTag.PLANT"],
    "CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY": ["StatTag.HEALTH", "StatTag.KILL"],
    "REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY": ["StatTag.REMNANT"],
    "SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM": ["StatTag.ELEMENTAL", "StatTag.INFUSION"],
    "WARCRIES_DEBILITATE_ENEMIES": ["StatTag.WARCRY", "StatTag.DEBILITATE"],
    "GAIN_ADDITIONAL_STUN_THRESHOLD_EQUAL_TO_OF_ITEM_ARMOR_ON_EQUIPPED_ARMOR_ITEMS": ["StatTag.STUN", "StatTag.ARMOR"],
    "TO_ALL_MAX_ELEMENTAL_RESISTANCES_IF_YOU_HAVE_AT_LEAST_RED_GREEN_AND_BLUE_SUPPORT_GEMS_SOCKETED": ["StatTag.ELEMENTAL", "StatTag.RESISTANCE", '"rgb_socketed"'],
    "IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR": ["StatTag.MOVEMENT", "StatTag.ARMOR"],
    "INCREASED_HEALTH_COST_OF_SKILLS": ["StatTag.HEALTH", "StatTag.COST", "StatTag.SPELL"],
    "INCREASED_MANA_COST_OF_SKILLS": ["StatTag.MANA", "StatTag.COST", "StatTag.SPELL"],
    "THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR": ['"thorns"', "StatTag.ARMOR"],
    "CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT": ["StatTag.REMNANT"],
    "OF_DAMAGE_TAKEN_BYPASSES_ENERGY_SHIELD": ["StatTag.TAKEN", "StatTag.ENERGY_SHIELD"],
}

def existing_tokens(line):
    """Return set of existing tag names from StatTag.X and \"x\" tokens on the line."""
    out = set()
    for a, b in re.findall(r'StatTag\.(\w+)|"([a-z0-9_]+)"', line):
        out.add(a or b)
    return out

lines = open(STGEN, encoding="utf-8").read().splitlines()
changed = 0
skipped = 0
notfound = []
for sid, tags in EDITS.items():
    hit = False
    for i, l in enumerate(lines):
        if ("StatIds.%s" % sid) in l and "AttributeRegister." in l:
            hit = True
            ext = existing_tokens(l)
            newl = l
            added = []
            for t in tags:
                name = t.split(".")[-1].rstrip('"').lstrip('"') if t.startswith("StatTag.") else t.strip('"')
                if name in ext:
                    continue
                newl = newl.replace(");", ", %s);" % t, 1)
                added.append(name)
                ext.add(name)
            if added:
                lines[i] = newl
                changed += 1
                print("EDIT  %s  +%s" % (sid, added))
            else:
                skipped += 1
                print("SKIP  %s  (already: %s)" % (sid, sorted(ext)))
            break
    if not hit:
        notfound.append(sid)

open(STGEN, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("\nSUMMARY: changed=%d skipped=%d notfound=%d" % (changed, skipped, len(notfound)))
if notfound:
    print("NOT FOUND:", notfound)
