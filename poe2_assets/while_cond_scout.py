#!/usr/bin/env python3
"""Comprehensive scout: wire unwired conditional StatIds whose condition maps to a
registry-backed COND primitive (existing or 4 new living-state CONDs) AND whose subject
maps to a PROVEN EFF constant already used in StatGen.

R3-safe: every candidate is emitted as a ready map.put line; the resolver only returns an
EFF if that exact constant literally exists in StatEffectIds.java. The operator family
(INC_=MULTIPLIED_BASE, TO_/ADD_=ADD_VALUE, MORE_=MULTIPLIED_TOTAL) must match the EFF variant.
Composite-condition / composite-subject / mechanism stats are explicitly skipped.
"""
import re, json

ROOT = "src/main/java/com/cimdy/coe"
SID   = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN   = f"{ROOT}/data/gen/stat/StatGen.java"
CONDS = f"{ROOT}/data/gen/stat/StatConditionIds.java"
EFFS  = f"{ROOT}/data/gen/stat/StatEffectIds.java"

consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired  = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
cond_consts = set(re.findall(r'public static final Identifier (COND_\w+) =', open(CONDS, encoding="utf-8").read()))
eff_consts  = set(re.findall(r'(EFF_[A-Z0-9_]+)', open(EFFS, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

# ---- condition phrase -> (COND name, target) ----
# Existing CONDs use TGT_SELF_ONLY; status CONDs used for self-scope (WHILE_X) also TGT_SELF_ONLY.
PHRASE = {
    # existing living-state CONDs
    "WHILE_SHAPESHIFTED":            ("COND_LIVING_STATE_SHAPESHIFTED", "TGT_SELF_ONLY"),
    "WHILE_SURROUNDED":              ("COND_LIVING_STATE_SURROUNDED", "TGT_SELF_ONLY"),
    "WHILE_MOVING":                  ("COND_MOVING_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_STATIONARY":              ("COND_LIVING_STATE_STATIONARY", "TGT_SELF_ONLY"),
    "WHILE_PARRYING":                ("COND_LIVING_STATE_PARRYING", "TGT_SELF_ONLY"),
    "WHILE_ON_LOW_HEALTH":           ("COND_LIVING_STATE_LOW_LIFE", "TGT_SELF_ONLY"),
    "WHILE_ON_FULL_HEALTH":          ("COND_FULL_LIFE_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_ACTIVELY_BLOCKING":       ("COND_BLOCKING_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_CHANNELLING":             ("COND_CHANNELLING_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_LEECHING":                ("COND_LEECHING_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_SPRINTING":               ("COND_SPRINTING_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_AFFECTED_BY_AN_ARCHON_BUFF": ("COND_ARCHON_LIVING_STATE", "TGT_SELF_ONLY"),
    "WHILE_ON_CHILLED_GROUND":       ("COND_ON_CHILLED_GROUND_STATUS", "TGT_SELF_ONLY"),
    "WHILE_ON_IGNITED_GROUND":       ("COND_ON_IGNITED_GROUND_STATUS", "TGT_SELF_ONLY"),
    "WHILE_ON_SHOCKED_GROUND":       ("COND_ON_SHOCKED_GROUND_STATUS", "TGT_SELF_ONLY"),
    "WHILE_ON_JAGGED_GROUND":        ("COND_ON_JAGGED_GROUND_STATUS", "TGT_SELF_ONLY"),
    # self-status CONDs (same primitive as enemy-scope, self target)
    "WHILE_IGNITED":   ("COND_IGNITE_STATUS", "TGT_SELF_ONLY"),
    "WHILE_CHILLED":   ("COND_CHILL_STATUS", "TGT_SELF_ONLY"),
    "WHILE_SHOCKED":   ("COND_SHOCK_STATUS", "TGT_SELF_ONLY"),
    "WHILE_FROZEN":    ("COND_FREEZE_STATUS", "TGT_SELF_ONLY"),
    "WHILE_POISONED":  ("COND_POISON_STATUS", "TGT_SELF_ONLY"),
    "WHILE_BLEEDING":  ("COND_BLEED_STATUS_AGE", "TGT_SELF_ONLY"),
    "WHILE_BLINDED":   ("COND_BLIND_STATUS", "TGT_SELF_ONLY"),
    "WHILE_MAIMED":    ("COND_MAIM_STATUS", "TGT_SELF_ONLY"),
    "WHILE_SLOWED":    ("COND_SLOW_STATUS", "TGT_SELF_ONLY"),
    "WHILE_HINDERED":  ("COND_HINDER_STATUS", "TGT_SELF_ONLY"),
    "WHILE_DAZED":     ("COND_DAZE_STATUS", "TGT_SELF_ONLY"),
    "WHILE_HEAVY_STUNNED": ("COND_HEAVY_STUN_STATUS", "TGT_SELF_ONLY"),
    "WHILE_ELECTROCUTED": ("COND_ELECTROCUTE_STATUS", "TGT_SELF_ONLY"),
    "WHILE_PINNED":    ("COND_PIN_STATUS", "TGT_SELF_ONLY"),
    "WHILE_EXPOSED_TO_FIRE":     ("COND_EXPOSURE_FIRE_STATUS", "TGT_SELF_ONLY"),
    "WHILE_EXPOSED_TO_COLD":     ("COND_EXPOSURE_COLD_STATUS", "TGT_SELF_ONLY"),
    "WHILE_EXPOSED_TO_LIGHTNING":("COND_EXPOSURE_LIGHTNING_STATUS", "TGT_SELF_ONLY"),
    # NEW living-state CONDs (need adding: backing LivingStateModes.*)
    "WHILE_DUAL_WIELDING":   ("COND_DUAL_WIELD_LIVING_STATE", "TGT_SELF_ONLY", "LivingStateModes.DUAL_WIELD"),
    "WHILE_HOLDING_A_SHIELD":("COND_HOLDING_SHIELD_LIVING_STATE", "TGT_SELF_ONLY", "LivingStateModes.HOLDING_SHIELD"),
    "WHILE_ON_LOW_MANA":     ("COND_LOW_MANA_LIVING_STATE", "TGT_SELF_ONLY", "LivingStateModes.LOW_MANA"),
    "WHILE_DODGE_ROLLING":   ("COND_DODGE_ROLLING_LIVING_STATE", "TGT_SELF_ONLY", "LivingStateModes.DODGE_ROLLING"),
}

# ---- operator family -> op-suffix + allowed EFF op-token ----
# INC_  -> MULTIPLIED_BASE ; TO_/ADD_ -> ADD_VALUE ; MORE_ -> MULTIPLIED_TOTAL
def op_family(name):
    if name.startswith("MORE_"):
        return "MULTIPLIED_TOTAL"
    if name.startswith("TO_") or name.startswith("ADD_"):
        return "ADD_VALUE"
    if name.startswith("INC_"):
        return "MULTIPLIED_BASE"
    return None

# ---- subject -> EFF constant (proven; verified present in StatEffectIds) ----
# Only MULTIPLIED_BASE entries listed; ADD_VALUE/MULTIPLIED_TOTAL resolved by op_family if present.
SUBJ = {
    "ACCURACY":                 "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY",
    "AREA_OF_EFFECT":           "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT",
    "ATTACK_SPEED":             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED",
    "CAST_SPEED":               "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED",
    "CHAOS_DAMAGE":             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHAOS_COE_DAMAGE_DAMAGE",
    "COLD_DAMAGE":              "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_COLD_DAMAGE",
    "CRIT_CHANCE":              "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE",
    "CRITICAL_HIT_CHANCE":      "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE",
    "CRIT_DAMAGE":              "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE",
    "CRITICAL_DAMAGE":          "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE",
    "ELEMENTAL_DAMAGE":         "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL",
    "ELEMENTAL":                "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_ELEMENTAL",
    "ENERGY_SHIELD_RECHARGE_RATE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ENERGY_RECHARGE_ENERGY",
    "EVASION":                  "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION",
    "FIRE_DAMAGE":              "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_FIRE",
    "LIGHTNING_DAMAGE":         "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_LIGHTNING",
    "MANA_REGENERATION_RATE":   "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN",
    "MANA_REGEN":               "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN",
    "MELEE_DAMAGE":             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE",
    "MINION_DAMAGE":            "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION",
    "MOVEMENT_SPEED":           "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED",
    "PHYSICAL_DAMAGE":          "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PHYSICAL",
    "PROJECTILE_DAMAGE":        "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE",
    "SPELL_DAMAGE":             "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_SPELL",
    "ATTACK_DAMAGE":            "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "ATTACKS_DEAL":             "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "ATTACKS_HAVE":             "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "HEALTH_LEECH":             "EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING",
    "AMOUNT_OF_HEALTH_HEALTH_LEECH": "EFF_ADD_MULTIPLIED_BASE_AMOUNT_ATTRIBUTE_COE_AMOUNT_LEECHED_HEALTH_LEECHING",
    "DAMAGE":                   "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
}

ADD_VALUE_OVERRIDE = {  # subjects where TO_/ADD_ maps to a known ADD_VALUE EFF
    "ACCURACY": "EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY",
    "ARMOR":    "EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ARMOR",
}
RECOUP_EFF = "EFF_HEALTH_RECOUP"

SKIP_SUBSTR = ("_AND_", "AND_", "PENALTY", "DAMAGE_TAKEN", "TAKE_MORE_DAMAGE",
               "GAIN_OF_DAMAGE_AS_EXTRA", "CONVERT", "IMMUNE_TO", "CANNOT_BE",
               "CANNOT_", "CULLING", "APPLY_", "ENEMIES_YOU", "COMPANIONS_",
               "YOU_CAN_", "INVOCATION", "METRES_TO", "RECENTLY_TOOK", "MAX_RAGE",
               "HAS_A_CHANCE", "WIELDING_", "EQUIP_", "AFFECTED_BY_HERALD",
               "AFFECTED_BY_ANY_DAMAGING", "YOU_HAVE_A_TOTEM", "YOU_HAVE_ENERGY_SHIELD",
               "YOU_HAVE_AN_ACTIVE_CHARM", "YOU_HAVE_ARCANE_SURGE", "YOUR_COMPANION",
               "YOU_HAVE_NO_HEALTH", "YOU_HAVE_AN_ALLIES", "OFF_HAND_EMPTY",
               "WITH_SPEARS", "REGENERATE_OF_MAX_HEALTH", "HEALTH_REGENERATION_RATE",
               "HEALTH_RECOVERY_RATE", "BLOCK_CHANCE", "STUN_THRESHOLD", "SKILL_SPEED",
               "MANA_COST_EFFICIENCY",
               # composite / unknown-condition markers (drop silently-handled extra conditions)
               "AGAINST", "ENEMIES", "ENEMY", "_VS_" )

def subject_eff(name):
    if "RECOUPED_AS_HEALTH" in name or name.endswith("_RECOUP"):
        return (RECOUP_EFF, "recoup") if RECOUP_EFF in eff_consts else (None, "no-recoup-eff")
    op = op_family(name)
    if op is None:
        return (None, "no-op")
    for subj, base in SUBJ.items():
        if subj in name:
            if op == "ADD_VALUE" and subj in ADD_VALUE_OVERRIDE:
                eff = ADD_VALUE_OVERRIDE[subj]
            else:
                # build expected EFF by swapping op-token; but we only have the MULTIPLIED_BASE proven name.
                # For INC (MULTIPLIED_BASE) the proven name is exactly `base`.
                # For TO_/ADD_ we need an ADD_VALUE variant; for MORE_ a MULTIPLIED_TOTAL variant.
                if op == "MULTIPLIED_BASE":
                    eff = base
                else:
                    # derive variant name by token replace; only accept if it literally exists
                    eff = base.replace("ADD_MULTIPLIED_BASE", "ADD_VALUE" if op == "ADD_VALUE" else "ADD_MULTIPLIED_TOTAL")
            if eff in eff_consts:
                return (eff, f"{subj}/{op}")
            return (None, f"no-{op}-eff-for-{subj}")
    return (None, "no-subject")

proposals = []
need_new_cond = {}
for name in unwired:
    # permit recoup stats (otherwise killed by DAMAGE_TAKEN skip token)
    is_recoup = ("RECOUPED_AS_HEALTH" in name) or name.endswith("_RECOUP")
    # skip mechanisms / composites / unsupported
    if not is_recoup and any(s in name for s in SKIP_SUBSTR):
        continue
    matched = [(ph, PHRASE[ph]) for ph in PHRASE if ph in name]
    if len(matched) != 1:
        continue  # 0 or composite condition
    ph, info = matched[0]
    cname = info[0]; tgt = info[1]
    if len(info) == 3:
        backing = info[2]
        need_new_cond[cname] = backing
    elif cname not in cond_consts:
        continue  # COND doesn't exist and isn't in our new list
    eff, note = subject_eff(name)
    if eff is None:
        continue
    proposals.append((name, cname, tgt, eff, note))

print(f"unwired={len(unwired)}  proposals={len(proposals)}  new_conds={len(need_new_cond)}")
print("\n== NEW COND primitives to add ==")
for c, b in sorted(need_new_cond.items()):
    print(f"  {c}  ->  {b}")
print(f"\n== {len(proposals)} WIRINGS ==")
for p in sorted(proposals):
    print(f"  map.put(StatIds.{p[0]},\n      stat(StatConditionIds.{p[1]}, StatTargetIds.{p[2]}, StatEffectIds.{p[3]})); // {p[4]}")
json.dump({"proposals": proposals, "new_conds": need_new_cond},
          open("poe2_assets/while_cond_scout.json", "w"), indent=1)
print("\nSaved poe2_assets/while_cond_scout.json")
