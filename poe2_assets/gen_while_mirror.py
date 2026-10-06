#!/usr/bin/env python
# Mirror known-good WHILE-conditional wirings: same subject => same EFF; only COND changes.
# R3: emit ONLY when (StatIds + COND + TGT + EFF) all exist in source, AND subject EFF is pre-verified.
import re, json

ROOT = "src/main/java/com/cimdy/coe"
SID = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN = f"{ROOT}/data/gen/stat/StatGen.java"
EFFIDS = f"{ROOT}/data/gen/stat/StatEffectIds.java"
CONDIDS = f"{ROOT}/data/gen/stat/StatConditionIds.java"
TGTIDS = f"{ROOT}/data/gen/stat/StatTargetIds.java"

consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

# Pre-verified subject -> EFF constant (all present in StatEffectIds, confirmed from existing wired entries)
SUBJ_EFF = {
    "INC_ARMOR": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ARMOR",
    "INC_ACCURACY": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY",
    "INC_EVASION": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_EVASION",
    "INC_MANA_REGENERATION_RATE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REGENERATION_RATE_MANA_REGEN",
    "INC_DAMAGE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "INC_STUN_BUILDUP": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN",
    "INC_AILMENT_DAMAGE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_DAMAGE_RATE",  # need verify
}

# Natural-language state phrase (as it appears after _WHILE_ in stat name) -> COND constant
# Only map phrases that have a REAL existing COND primitive. "NONE" = no primitive => not satisfiable.
STATE_COND = {
    "SHAPESHIFTED": "COND_LIVING_STATE_SHAPESHIFTED",
    "YOU_ARE_SHAPESHIFTED": "COND_LIVING_STATE_SHAPESHIFTED",
    "SURROUNDED": "COND_LIVING_STATE_SURROUNDED",
    "ON_LOW_HEALTH": "COND_LIVING_STATE_LOW_LIFE",
    "ON_FULL_HEALTH": "COND_FULL_LIFE_LIVING_STATE",
    "PARRYING": "COND_LIVING_STATE_PARRYING",
    "STATIONARY": "COND_LIVING_STATE_STATIONARY",
    "ACTIVELY_BLOCKING": "COND_BLOCKING_LIVING_STATE",
    "AFFECTED_BY_AN_ARCHON_BUFF": "COND_ARCHON_LIVING_STATE",
    "FROZEN": "COND_FREEZE_STATUS",
    "BLEEDING": "COND_BLEED_STATUS_AGE",
    "ON_CHILLED_GROUND": "COND_ON_CHILLED_GROUND_STATUS",
    "ON_IGNITED_GROUND": "COND_ON_IGNITED_GROUND_STATUS",
    "ON_SHOCKED_GROUND": "COND_ON_SHOCKED_GROUND_STATUS",
}

def exist(name, blob):
    return re.search(r'\b' + re.escape(name) + r'\b', blob) is not None

eff_blob = open(EFFIDS, encoding="utf-8").read()
cond_blob = open(CONDIDS, encoding="utf-8").read()
tgt_blob = open(TGTIDS, encoding="utf-8").read()

cands = []
for n in unwired:
    m = re.match(r'(INC_\w+)_WHILE_([A-Z0-9_]+)$', n)
    if not m:
        continue
    subj, state = m.group(1), m.group(2)
    eff = SUBJ_EFF.get(subj)
    cond = STATE_COND.get(state)
    if eff is None or cond is None:
        continue
    # verify all constants exist
    if not exist(eff, eff_blob):
        print(f"[skip-EFF-missing] {n} eff={eff}"); continue
    if not exist(cond, cond_blob):
        print(f"[skip-COND-missing] {n} cond={cond}"); continue
    if "TGT_SELF_ONLY" not in tgt_blob:
        print("[fatal] TGT_SELF_ONLY missing"); break
    if n not in consts:
        continue
    cands.append((n, cond, eff))

print(f"\nCandidates: {len(cands)}")
for n, cond, eff in cands:
    print(f"  {n}  ->  stat({cond}, TGT_SELF_ONLY, {eff})")
