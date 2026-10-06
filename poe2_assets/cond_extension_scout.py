#!/usr/bin/env python3
"""Scout: which unwired conditional StatIds become satisfiable by adding
status/living-state COND primitives that are PURE DATA (StatusValue / LivingStateValue)
against already-registered StatusTypes / LivingStateModes.

Only proposes matches where BOTH:
  - the condition maps to an existing registry entry with NO current COND, AND
  - the subject maps to a PROVEN EFF constant already used in StatGen (from existing AGAINST/WHILE wirings).
This keeps everything within R3 (no fabricated effect semantics).
"""
import re, json, collections

ROOT = "src/main/java/com/cimdy/coe"
SID  = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN  = f"{ROOT}/data/gen/stat/StatGen.java"
CONDS= f"{ROOT}/data/gen/stat/StatConditionIds.java"

consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
cond_consts = set(re.findall(r'public static final Identifier (COND_\w+) =', open(CONDS, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

# ---- Status keyword -> (StatusType expr, proposed COND name) ----
# StatusType ids from AilmentStatusTypes / DebuffStatusTypes.
STATUS = {
    "IGNITED":   ("AilmentStatusTypes.IGNITE",    "COND_IGNITE_STATUS"),
    "IGNITE":    ("AilmentStatusTypes.IGNITE",    "COND_IGNITE_STATUS"),
    "POISONED":  ("AilmentStatusTypes.POISON",    "COND_POISON_STATUS"),
    "CHILLED":   ("AilmentStatusTypes.CHILL",     "COND_CHILL_STATUS"),
    "BLINDED":   ("DebuffStatusTypes.BLIND",      "COND_BLIND_STATUS"),
    "MAIMED":    ("DebuffStatusTypes.MAIM",       "COND_MAIM_STATUS"),
    "SLOWED":    ("DebuffStatusTypes.SLOW",       "COND_SLOW_STATUS"),
    "HINDERED":  ("DebuffStatusTypes.HINDER",     "COND_HINDER_STATUS"),
    "WITHERED":  ("DebuffStatusTypes.WITHER",     "COND_WITHER_STATUS"),
    "IMPALED":   ("DebuffStatusTypes.IMPALE",     "COND_IMPALE_STATUS"),
    "INTIMIDATED":("DebuffStatusTypes.INTIMIDATE","COND_INTIMIDATE_STATUS"),
    "DEBILITATED":("DebuffStatusTypes.DEBILITATE","COND_DEBILITATE_STATUS"),
    "ARMOUR_BROKEN":("DebuffStatusTypes.ARMOUR_BROKEN_FULLY","COND_ARMOUR_BROKEN_STATUS"),
    "TAUNTED":   ("DebuffStatusTypes.TAUNT",      "COND_TAUNT_STATUS"),
    "CORRUPTED_BLOOD":("DebuffStatusTypes.CORRUPTED_BLOOD","COND_CORRUPTED_BLOOD_STATUS"),
    "PETRIFIED": ("DebuffStatusTypes.PETRIFY",    "COND_PETRIFY_STATUS"),
    "INCISED":   ("DebuffStatusTypes.INCISION",   "COND_INCISION_STATUS"),
    "AGGRAVATED":("DebuffStatusTypes.AGGRAVATED", "COND_AGGRAVATED_STATUS"),
}
# ---- Living-state keyword -> (LivingStateModes expr, proposed COND name) ----
LIVING = {
    "CHANNELLING": ("LivingStateModes.CHANNELLING", "COND_CHANNELLING_LIVING_STATE"),
    "CHANNELING":  ("LivingStateModes.CHANNELLING", "COND_CHANNELLING_LIVING_STATE"),
    "LEECHING":    ("LivingStateModes.LEECHING",    "COND_LEECHING_LIVING_STATE"),
    "DUAL_WIELD":  ("LivingStateModes.DUAL_WIELD",  "COND_DUAL_WIELD_LIVING_STATE"),
    "HOLDING_SHIELD":("LivingStateModes.HOLDING_SHIELD","COND_HOLDING_SHIELD_LIVING_STATE"),
    "SPRINTING":   ("LivingStateModes.SPRINTING",   "COND_SPRINTING_LIVING_STATE"),
    "MOVING":      ("LivingStateModes.MOVING",      "COND_MOVING_LIVING_STATE"),
    "LOW_MANA":    ("LivingStateModes.LOW_MANA",    "COND_LOW_MANA_LIVING_STATE"),
    "DODGE_ROLLING":("LivingStateModes.DODGE_ROLLING","COND_DODGE_ROLLING_LIVING_STATE"),
}

# ---- Subject prefix -> proven EFF constant (verified present in StatGen wirings) ----
# (op family handled separately by name token)
EFF = {
    "DAMAGE_GEN":   "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "DAMAGE_ATTACK":"EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "DAMAGE_MELEE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE",
    "DAMAGE_PROJ":  "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE",
    "CRIT_CHANCE":  "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE",
    "CRIT_DMG_VAL": "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE",
    "ACC_INC":      "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY",
    "ACC_VAL":      "EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY",
    "EVASION":      "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION",
    "MOVE_SPD":     "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED",
    "ATK_SPD":      "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED",
    "MANA_REGEN":   "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN",
    "DEFLECT":      "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT",
}

def subject_eff(name):
    """Return (eff_const, note) for a stat name, or (None, reason)."""
    n = name
    if "CRIT_DAMAGE" in n or "CRITICAL_DAMAGE" in n:
        return (EFF["CRIT_DMG_VAL"], "flat crit dmg")
    if "CRIT_CHANCE" in n or "CRITICAL_HIT_CHANCE" in n:
        return (EFF["CRIT_CHANCE"], "crit chance")
    if "ACCURACY" in n:
        if n.startswith("TO_") or "TO_ACCURACY" in n:
            return (EFF["ACC_VAL"], "flat accuracy")
        return (EFF["ACC_INC"], "inc accuracy")
    if "MELEE_DAMAGE" in n:
        return (EFF["DAMAGE_MELEE"], "melee dmg")
    if "DEFLECTION" in n:
        return (EFF["DEFLECT"], "deflection")
    if "MANA_REGENERATION" in n or "MANA_REGEN" in n:
        return (EFF["MANA_REGEN"], "mana regen")
    if "EVASION" in n:
        return (EFF["EVASION"], "evasion")
    if "MOVEMENT_SPEED" in n:
        # penalty-from-skills scoping => skip (generic EFF can't capture scope)
        if "PENALTY" in n:
            return (None, "move-penalty-scope")
        return (EFF["MOVE_SPD"], "move spd")
    if "ATTACK_SPEED" in n:
        return (EFF["ATK_SPD"], "atk spd")
    if "PROJECTILE" in n and "DAMAGE" in n:
        return (EFF["DAMAGE_PROJ"], "proj dmg")
    if "ATTACK_DAMAGE" in n or "ATTACKS_DEAL" in n or "ATTACKS_HAVE" in n:
        return (EFF["DAMAGE_ATTACK"], "attack dmg")
    if "DAMAGE" in n:
        return (EFF["DAMAGE_GEN"], "generic dmg")
    if "ARMOR" in n and "EVASION" in n:
        return (None, "composite-armor-evasion")
    if "AND" in n and ("ARMOR" in n or "EVASION" in n or "ENERGY" in n):
        return (None, "composite-subject")
    return (None, "no-subject-eff")

def detect_cond(name):
    """Return (cond_name, tgt, backing) or None for status/living conditional stats."""
    # AGAINST <STATUS> ENEMIES  (target enemy)
    m = re.search(r'AGAINST_([A-Z_]+?)_ENEMIES', name)
    if m:
        kw = m.group(1)
        if kw in STATUS:
            return (STATUS[kw][1], "TGT_COND_ON_TARGET", STATUS[kw][0])
        # some use AGAINST_X where X is the status w/o _ENEMIES handled below
    # _AGAINST_<STATUS> (e.g. GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_VS_FREEZE uses _VS_)
    m = re.search(r'_VS_([A-Z_]+)$', name)
    if m:
        kw = m.group(1)
        if kw in STATUS:
            return (STATUS[kw][1], "TGT_COND_ON_TARGET", STATUS[kw][0])
    # WHILE <STATE>  (self)
    m = re.search(r'_WHILE_([A-Z_]+?)(?:_|$)', name)
    if m:
        kw = m.group(1)
        if kw in LIVING:
            return (LIVING[kw][1], "TGT_SELF_ONLY", LIVING[kw][0])
    return None

proposals = []
need_cond = {}   # cond_name -> backing expr
for name in unwired:
    det = detect_cond(name)
    if not det:
        continue
    cname, tgt, backing = det
    if cname in cond_consts:
        # already has a COND -> if so, why unwired? skip (something else blocks)
        continue
    eff, note = subject_eff(name)
    if eff is None:
        proposals.append((name, cname, tgt, backing, "NEED_EFF", note))
        need_cond.setdefault(cname, backing)
        continue
    proposals.append((name, cname, tgt, backing, eff, note))
    need_cond.setdefault(cname, backing)

print(f"Unwired scanned={len(unwired)}  proposals={len(proposals)}")
print(f"New COND primitives needed={len(need_cond)}")
print("\n== NEW COND primitives (backing) ==")
for c, b in sorted(need_cond.items()):
    print(f"  {c:42} {b}")

print("\n== PROPOSALS (stat -> COND, TGT, EFF) ==")
wired_props = [p for p in proposals if p[4] != "NEED_EFF"]
for p in sorted(wired_props):
    print(f"  map.put(StatIds.{p[0]},\n      stat(StatConditionIds.{p[1]}, StatTargetIds.{p[2]}, StatEffectIds.{p[4]}));")
need = [p for p in proposals if p[4] == "NEED_EFF"]
print(f"\n== Proposals needing a NEW EFF / skipped ({len(need)}) ==")
for p in need:
    print(f"  {p[0]:60} cond={p[1]:38} note={p[5]}")

json.dump({"proposals": proposals, "new_conds": need_cond},
          open("poe2_assets/cond_extension_scout.json", "w"), indent=1)
print("\nSaved poe2_assets/cond_extension_scout.json")
