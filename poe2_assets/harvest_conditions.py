#!/usr/bin/env python3
"""Data-driven condition-clause matcher (SCOUT ONLY — never writes StatGen).

Strategy:
 1. Parse StatGen.buildEntries() to extract the EXACT wiring map:
       StatIds.NAME -> (EFF constant, COND id or None, TGT id)
 2. For each unwired StatIds name, strip a trailing condition clause
    (AGAINST_*/VS_*/WHILE_*/ON_*/IF_*) to recover the base subject, then
    look the base subject up in the wired map.
 3. If the base is already wired with a known EFF, AND the stripped clause maps
    to an EXISTING COND primitive, the unwired stat is a clean wire:
       - reuse the base's exact EFF (op-family consistent by construction)
       - reuse the base's TGT for self-side clauses (WHILE/ON_self)
       - use TGT_COND_ON_TARGET for AGAINST/VS (target-side) clauses
 4. Emit candidates. No fabrication: every candidate reuses a real EFF + real COND.
"""
import re, json

GEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
IDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
UNWIRED = "poe2_assets/unwired_buckets.json"

# ---- 1. parse StatGen wiring map ----
with open(GEN, encoding="utf-8") as f:
    gen = f.read()

# map.put(StatIds.NAME, stat(...))
put_re = re.compile(r"map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\(([^)]*)\)\)")
wired = {}  # name -> (eff, cond, tgt)
for m in put_re.finditer(gen):
    name = m.group(1)
    inner = m.group(2)
    eff = re.search(r"StatEffectIds\.(\w+)", inner)
    tgt = re.search(r"StatTargetIds\.(\w+)", inner)
    conds = re.findall(r"StatConditionIds\.(\w+)", inner)
    wired[name] = (
        eff.group(1) if eff else None,
        conds,                      # list; empty if none
        tgt.group(1) if tgt else None,
    )

# ---- 2. verify these StatIds actually exist (constant defined) ----
with open(IDS, encoding="utf-8") as f:
    ids_src = f.read()
def ids_exists(name):
    return re.search(r"\b" + re.escape(name) + r"\s*=", ids_src) is not None

# ---- 3. condition-clause -> COND primitive map ----
# Order matters: longer / more specific first.
COND_MAP = [
    # target-side AGAINST / VS  (-> TGT_COND_ON_TARGET)
    ("AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH", "COND_LIVING_STATE_LOW_LIFE", "TGT"),
    ("AGAINST_ENEMIES_THAT_ARE_ON_FULL_HEALTH", "COND_FULL_LIFE_LIVING_STATE", "TGT"),
    ("AGAINST_FROZEN_ENEMIES", "COND_FREEZE_STATUS", "TGT"),
    ("AGAINST_DAZED_ENEMIES", "COND_DAZE_STATUS", "TGT"),
    ("AGAINST_BLEEDING_ENEMIES", "COND_BLEED_STATUS_AGE", "TGT"),
    ("AGAINST_SHOCKED_ENEMIES", "COND_SHOCK_STATUS", "TGT"),
    ("AGAINST_HEAVY_STUNNED_ENEMIES", "COND_HEAVY_STUN_STATUS", "TGT"),
    ("AGAINST_STUNNED_ENEMIES", "COND_HEAVY_STUN_STATUS", "TGT"),
    ("AGAINST_MAIMED_ENEMIES", "COND_MAIM_STATUS", "TGT"),
    ("AGAINST_BLINDED_ENEMIES", "COND_BLIND_STATUS", "TGT"),
    ("AGAINST_IGNITED_ENEMIES", "COND_IGNITE_STATUS", "TGT"),
    ("AGAINST_CHILLED_ENEMIES", "COND_CHILL_STATUS", "TGT"),
    ("AGAINST_POISONED_ENEMIES", "COND_POISON_STATUS", "TGT"),
    ("AGAINST_SLOWED_ENEMIES", "COND_SLOW_STATUS", "TGT"),
    ("AGAINST_HINDERED_ENEMIES", "COND_HINDER_STATUS", "TGT"),
    ("AGAINST_ELECTROCUTED_ENEMIES", "COND_ELECTROCUTE_STATUS", "TGT"),
    ("AGAINST_PINNED_ENEMIES", "COND_PIN_STATUS", "TGT"),
    # NOTE: removed wildcard fallbacks (AGAINST_, AGAINST_ENEMIES_THAT_ARE) — they
    # mis-assigned COND_FREEZE_STATUS to enemy-type clauses (DEMONS/UNDEAD/MARKED/...) that
    # have NO COND primitive. Only EXACT status clauses below are real belts.
    # Phase 8 extension: RECENTLY / RARE-UNIQUE clauses whose COND primitives have a
    # PROVEN StatGen precedent (verified self-side, TGT_SELF_ONLY / TGT_INLINE_SELF).
    ("IF_YOU_VE_KILLED_RECENTLY", "COND_COUNT_KILL_RECENTLY", "SELF"),       # L970/A1050 precedent
    ("IF_YOU_HAVE_KILLED_RECENTLY", "COND_COUNT_KILL_RECENTLY", "SELF"),
    ("AGAINST_RARE_OR_UNIQUE_ENEMIES", "COND_PRESENCE_RARITY_RARE_UNIQUE", "SELF"),  # L927/1043 precedent (TGT_SELF_ONLY!)
    ("AGAINST_RARE_AND_UNIQUE_ENEMIES", "COND_PRESENCE_RARITY_RARE_UNIQUE", "SELF"),
    # self-side WHILE / ON  (-> reuse base TGT)
    ("WHILE_CHANNELLING", "COND_CHANNELLING_LIVING_STATE", "SELF"),
    ("WHILE_LEECHING", "COND_LEECHING_LIVING_STATE", "SELF"),
    ("WHILE_MOVING", "COND_MOVING_LIVING_STATE", "SELF"),
    ("WHILE_STATIONARY", "COND_LIVING_STATE_STATIONARY", "SELF"),
    ("WHILE_ON_FULL_HEALTH", "COND_FULL_LIFE_LIVING_STATE", "SELF"),
    ("WHILE_ON_LOW_HEALTH", "COND_LIVING_STATE_LOW_LIFE", "SELF"),
    ("WHILE_PARRYING", "COND_LIVING_STATE_PARRYING", "SELF"),
    ("WHILE_SHAPESHIFTED", "COND_LIVING_STATE_SHAPESHIFTED", "SELF"),
    ("WHILE_SURROUNDED", "COND_LIVING_STATE_SURROUNDED", "SELF"),
    ("WHILE_DUAL_WIELDING", "COND_DUAL_WIELD_LIVING_STATE", "SELF"),
    ("WHILE_HOLDING_A_SHIELD", "COND_HOLDING_SHIELD_LIVING_STATE", "SELF"),
    ("ON_CHILLED_GROUND", "COND_ON_CHILLED_GROUND_STATUS", "SELF"),
    ("ON_JAGGED_GROUND", "COND_ON_JAGGED_GROUND_STATUS", "SELF"),
    ("ON_IGNITED_GROUND", "COND_ON_IGNITED_GROUND_STATUS", "SELF"),
    ("ON_SHOCKED_GROUND", "COND_ON_SHOCKED_GROUND_STATUS", "SELF"),
    ("ON_FULL_HEALTH", "COND_FULL_LIFE_LIVING_STATE", "SELF"),
    ("ON_LOW_HEALTH", "COND_LIVING_STATE_LOW_LIFE", "SELF"),
    ("ON_FULL_MANA", "NONE", "SELF"),   # no mana COND -> skip
    ("ON_LOW_MANA", "NONE", "SELF"),    # no mana COND -> skip
    ("ON_FULL_ENERGY_SHIELD", "NONE", "SELF"),
]

def split_clause(name):
    up = name.upper()
    for kw, cond, side in COND_MAP:
        idx = up.find(kw)
        if idx != -1:
            base = name[:idx].rstrip("_")   # strip trailing underscore(s) -> exact base subject
            clause = name[idx:].lstrip("_")
            return base, clause, cond, side
    return name, None, None, None

# ---- 4. load unwired ----
with open(UNWIRED, encoding="utf-8") as f:
    data = json.load(f)
def flat(x):
    if isinstance(x, str): return [x]
    if isinstance(x, list):
        out = []
        for e in x: out.extend(flat(e))
        return out
    return [str(x)]
unwired = []
for k, v in data.items():
    for item in v:
        for n in flat(item):
            unwired.append((k, n))

# ---- 5. match ----
candidates = []
already = []   # unwired name that is in fact already wired (sanity)
for bucket, name in unwired:
    if name in wired:
        already.append(name)
        continue
    base, clause, cond, side = split_clause(name)
    if clause is None:
        continue
    if cond == "NONE":
        continue
    if base not in wired:
        continue
    eff, conds, tgt = wired[base]
    if eff is None:
        continue
    # target-side clause -> TGT_COND_ON_TARGET; self-side -> reuse base TGT
    out_tgt = "TGT_COND_ON_TARGET" if side == "TGT" else (tgt or "TGT_SELF_ONLY")
    if not ids_exists(name):
        continue  # safety: stat constant must exist
    candidates.append({
        "name": name, "bucket": bucket, "base": base,
        "eff": eff, "cond": cond, "tgt": out_tgt,
        "base_tgt": tgt, "base_conds": conds,
    })

# de-dup
seen = set()
uniq = []
for c in candidates:
    if c["name"] in seen: continue
    seen.add(c["name"]); uniq.append(c)

print(f"Wired stat entries parsed : {len(wired)}")
print(f"Unwired names total       : {len(unwired)}")
print(f"Already-wired (in StatGen): {len(already)}  (these are false-unwired; snapshot stale)")
print(f"CLEAN candidates          : {len(uniq)}")
print()
for c in sorted(uniq, key=lambda x: x["name"]):
    flag = "TARGET-SIDE" if c["tgt"] == "TGT_COND_ON_TARGET" else "SELF-SIDE"
    print(f"[{flag}] {c['name']}")
    print(f"      base={c['base']}  EFF={c['eff']}  COND={c['cond']}  TGT={c['tgt']}")
