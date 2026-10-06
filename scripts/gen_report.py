# -*- coding: utf-8 -*-
"""Generate the unwired-StatId classification report (A/B/C/D) from _classify.json.
A = clean wire to EXISTING EFF_*; B = new EFF+existing live-read attr; C = new attr+new read;
D = not a stat (behavior/mechanism toggle). R3 safety: never a dead wire.
"""
import json, os, re
ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
C = json.load(open(os.path.join(ROOT, "scripts/_classify.json"), encoding="utf-8"))
A, B, D = C["A"], C["B"], C["D"]
Cc = C.get("C", {})

# Recompute UNWIRED set (must match scan_unwired.py) to dedup consistently.
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")
statids = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', open(SID, encoding="utf-8").read()))
wired = set()
for fp in os.listdir(WIR):
    if fp.startswith("Wire") and fp.endswith(".java"):
        for m in re.finditer(r'map\.put\(StatIds\.(\w+)', open(os.path.join(WIR,fp), encoding="utf-8").read()):
            wired.add(m.group(1))
unwired = [k for k in statids if k not in wired]

# Dedup: priority D < C < B < A, keep only genuine unwired IDs.
_raw = {}
for k in D: _raw[k]="D"
for k in Cc: _raw[k]="C"
for k in B: _raw[k]="B"
for k in A: _raw[k]="A"
cats = {k:v for k,v in _raw.items() if k in set(unwired)}
# rebuild dedicated per-category dicts from cats
A = {k:v for k,v in A.items() if cats.get(k)=="A"}
B = {k:v for k,v in B.items() if cats.get(k)=="B"}
Cc = {k:v for k,v in Cc.items() if cats.get(k)=="C"}
D = [k for k in D if cats.get(k)=="D"]

def wire_file(name):
    n = name.lower()
    if any(k in n for k in ["crit","critical"]): return "WireCrit"
    if any(k in n for k in ["ailment","poison","wither","blind","ignite","bleed","slow","incis","impale","charm","mark","debil","intimid","maim","stun_build","corpse","archon","arcane_surge","glory"]): return "WireAilment"
    if any(k in n for k in ["evasion","armor","block","dodge","deflect","guard","shield","parry"]): return "WireDefense"
    if any(k in n for k in ["mana","energy_shield","regen","flask","reservation","spirit","health_cost","rage","volatil","leech","recover","recoup"]): return "WireResource"
    if any(k in n for k in ["movement_speed","move_speed"]): return "WireSpeed"
    if any(k in n for k in ["totem","presence","banner","minion","companion","offering","summon","puppet"]): return "WireMisc"
    if any(k in n for k in ["socket","socketed","gem"]): return "WireSocket"
    if any(k in n for k in ["per_","for_each","for_seconds_up_to","per_","each_"]): return "WirePerScaling"
    if any(k in n for k in ["while_","if_you_","if_","when_"]): return "WireWhile"
    if any(k in n for k in ["recently"]): return "WireRecent"
    if any(k in n for k in ["damage","projectile","knock","fork","pierce","attack","cast_speed","area_of_effect","accuracy"]): return "WireDamage"
    return "WireMisc"

# ---- A: full code ----
A_CODE = {
 "TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION":
   'map.put(StatIds.TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION,\n'
   '    self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEFLECTION_RATING_DEFLECT));\n'
   '// EFF defined StatEffectGen.java:273 -> ModifierStatEffect(DEFLECTION_RATING, ADD_MULTIPLIED_BASE, GAIN, {DEFLECT})\n'
   '// Engine read: BlockHelper.java:28  living.getAttributeValue(DEFLECTION_RATING) scales block->deflection dmg prevented',
 "INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY":
   'map.put(StatIds.INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY,\n'
   '    stat(StatConditionIds.COND_NOT_RECENTLY_CAUSED_CRIT, StatTargetIds.TGT_INLINE_SELF,\n'
   '         StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_DAMAGE_CRIT));\n'
   '// EFF defined StatEffectGen.java:112 -> ModifierStatEffect(CRIT_DAMAGE, ADD_MULTIPLIED_BASE, GAIN, {ATTACK,CRIT})\n'
   '// Engine read: DamageHelper.java:385 (CRIT_DAMAGE). COND_NOT_RECENTLY_CAUSED_CRIT exists (StatConditionIds.java:183)',
 "MANA_COSTS_ARE_DOUBLED":
   'map.put(StatIds.MANA_COSTS_ARE_DOUBLED,\n'
   '    self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));  // magnitude -1.0 => cost x2\n'
   '// EFF defined StatEffectGen.java:474 -> ModifierStatEffect(MANA_COST_EFFICIENCY, ADD_VALUE, GAIN)\n'
   '// Engine read: ResourceCost.java:104,118  cost = raw * max(0, 1 - eff)',
 "INC_MANA_COST_OF_SKILLS":
   'map.put(StatIds.INC_MANA_COST_OF_SKILLS,\n'
   '    self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_COST_EFFICIENCY));  // negative magnitude\n'
   '// EFF defined StatEffectGen.java:474 -> ModifierStatEffect(MANA_COST_EFFICIENCY, ADD_VALUE, GAIN)\n'
   '// Engine read: ResourceCost.java:104,118  cost = raw * max(0, 1 - eff)',
 "REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED":
   'map.put(StatIds.REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED,\n'
   '    stat(StatConditionIds.COND_HEAVY_STUN_STATUS, StatTargetIds.TGT_INLINE_SELF,\n'
   '         StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_REGENERATION_HEALTH_REGEN));\n'
   '// EFF defined StatEffectGen.java:453 -> ModifierStatEffect(MAX_REGENERATION, ADD_VALUE, GAIN, {HEALTH,REGEN})\n'
   '// Engine read: HealthHelper.java:22 (MAX_REGENERATION, {health,regen}). COND_HEAVY_STUN_STATUS exists (StatConditionIds.java:51)',
}

# ---- First batch (A + highest-confidence clean B) ----
FIRST = [
 # (name, wire_file, proposed StatEffectIds/StatEffectGen snippet or note)
 ("TO_AMOUNT_OF_HIT_DAMAGE_PREVENTED_BY_DEFLECTION","WireDefense","EXISTING EFF — see A code"),
 ("INC_CRITICAL_DAMAGE_BONUS_IF_YOU_VE_DEALT_A_NON_CRITICAL_HIT_RECENTLY","WireCrit","EXISTING EFF — see A code"),
 ("MANA_COSTS_ARE_DOUBLED","WireResource","EXISTING EFF — see A code"),
 ("INC_MANA_COST_OF_SKILLS","WireResource","EXISTING EFF — see A code"),
 ("REGENERATE_OF_MAX_HEALTH_OVER_SECOND_WHEN_STUNNED","WireResource","EXISTING EFF — see A code"),
 ("NEVER_DEAL_CRITICAL_HITS","WireCrit",
   "NEW EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE_SET_ZERO = ModifierStatEffect(CRIT_CHANCE, "
   "Either.right(AttributeLimiters.Operation.SET_VALUE), Mode.REPLACE); engine read DamageHelper.java:160"),
 ("INVOCATED_SPELLS_HAVE_INC_CRITICAL_HIT_CHANCE","WireCrit",
   "NEW EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_CHANCE_INVOCATION = ModifierStatEffect(CRIT_CHANCE, ADD_VALUE, GAIN, "
   "{SPELL,INVOCATION}); engine read DamageHelper.java:160 — verify INVOCATION StatTag/scope exists"),
 ("INVOCATION_SPELLS_HAVE_INC_CRITICAL_DAMAGE_BONUS","WireCrit",
   "NEW EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE_INVOCATION = ModifierStatEffect(CRIT_DAMAGE, ADD_VALUE, GAIN, "
   "{SPELL,INVOCATION}); engine read DamageHelper.java:385 — verify INVOCATION StatTag/scope exists"),
 ("HIT_DAMAGE_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_YOU","WireCrit",
   "NEW EFF on TAKEN_CRIT_CHANCE (existing attr, read DamagePipeline) + tag {TAKEN}; "
   "reuse EFF_ADD_VALUE_ATTRIBUTE_COE_TAKEN_CRIT_CHANCE (StatEffectGen.java:508) scoped"),
 ("HIT_DAMAGE_AGAINST_YOU_HAVE_INC_CRITICAL_DAMAGE_BONUS","WireCrit",
   "NEW EFF on TAKEN_CRIT_DAMAGE (StatEffectGen.java:306/508) + tag {TAKEN}"),
 ("COMPANIONS_HAVE_A_CHANCE_TO_POISON_ON_HIT_DAMAGE","WireAilment",
   "NEW EFF on AILMENT_CHANCE (base 0, read AilmentHelper.java:751) onTarget TGT_INLINE_MINION (exists), tag {poison}; "
   "needs new minion-scoped AILMENT_CHANCE EFF variant"),
 ("CRITICAL_HITS_WITH_DAGGERS_HAVE_A_CHANCE_TO_POISON_THE_ENEMY","WireAilment",
   "NEW EFF on AILMENT_CHANCE {poison} + CRIT scope + NEW COND_WIELDING_DAGGER (does NOT exist yet); "
   "read AilmentHelper.java:751"),
 ("INC_AREA_OF_EFFECT_IF_YOU_HAVE_STUNNED_AN_ENEMY_RECENTLY","WireDamage",
   "NEW EFF on EFFECT_AREA (read AreaDeliveryExecutor.java:174) + NEW COND_COUNT_RECENTLY_CAUSED_STUN "
   "(does NOT exist; only CAUSED_HIT/PARRY/BLOCK/CRIT exist)"),
 ("KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF","WireDamage",
   "NEW EFF on KNOCKBACK_CHANCE (read DamageAction.java:333) + COND_WIELDING_QUARTERSTAFF (exists) + COND_CRIT_HIT (exists)"),
 ("CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE","WireDamage",
   "NEW EFF on KNOCKBACK_CHANCE (read DamageAction.java:333) + COND_BLEED_STATUS_AGE (exists, target bleeding)"),
 ("INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY","WireDefense",
   "NEW EFF on EVASION (read StateCalculator.java:43) + NEW COND (dodge-rolled-recently; only "
   "COND_DODGE_ROLLING_LIVING_STATE 'currently rolling' exists, not 'recently')"),
 ("INC_BLOCK_RECOVERY","WireDefense",
   "NEW EFF on a BLOCK_RECOVERY attribute (no such attr yet -> borderline B/C; engine BlockHelper.java:26) — "
   "flagged; may need C-grade new attr"),
 ("MORE_KNOCKBACK_DISTANCE_FOR_BLOCKED_HIT_DAMAGE","WireDamage",
   "NEW EFF on KNOCKBACK (read EntityOperationEvent.java:237) + COND_BLOCKING_LIVING_STATE (exists)"),
 ("INC_DAMAGE_FOR_EACH_TYPE_OF_ELEMENTAL_AILMENT_ON_ENEMY","WirePerScaling",
   "NEW EFF on DAMAGE (read DamageHelper.java:69) per-count(ailment-type) onTarget ENEMY; per-count mechanic"),
 ("INC_DAMAGE_FOR_EACH_HAZARD_TRIGGERED_RECENTLY_UP_TO","WirePerScaling",
   "NEW EFF on DAMAGE (read DamageHelper.java:69) per-count(hazard)"),
 ("INVOCATED_SKILLS_HAVE_INC_MAX_ENERGY","WireMisc",
   "NEW EFF on META_ENERGY_MAX (read MetaEnergyState.java) + INVOCATION scope — verify invocation scope "
   "(StatTag INVOCATION may need authoring; skill-assembly handles invocation)"),
 ("SURPASS_CHANCE_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE","WireDamage",
   "B/caveat: reuse EFF_ADD_VALUE_ATTRIBUTE_COE_LAUNCH_ADDITIONAL_PROJECTILE (StatEffectGen.java:434) as FLAT count "
   "(engine reads (int) ProjectileHelper.java:207); true 'chance' needs new PROJECTILE_CHANCE attr (=>C)"),
]

# group B/C/D by wire file
def group(d):
    g = {}
    for k in d:
        g.setdefault(wire_file(k), []).append(k)
    return g

Bb = group(B); Cc_g = group(Cc); Dd = group(D)

out = []
out.append("# Unwired StatId Classification Report — CraftOfExile-NeoForge")
out.append("")
out.append("**Scope:** 245 unwired StatIds (constant in `StatIds.java`, no `map.put` in any `Wire*.java`).")
out.append("**Method:** `python scripts/scan_unwired.py` (245) + `scripts/classify_unwired.py` (single-category adjudication, R3-safe).")
out.append("")
out.append("## Totals (deduped, one category per StatId)")
out.append("")
out.append("| Category | Count | Meaning |")
out.append("|---|---|---|")
out.append("| **A** | %d | Clean wire to an EXISTING `StatEffectIds.EFF_*` (live engine read). |" % len(A))
out.append("| **B** | %d | Needs a NEW `EFF_*` + `ModifierStatEffect`, but the underlying attribute already has a live engine read. |" % len(B))
out.append("| **C** | %d | Needs a NEW attribute in `AttributeRegister` AND a new engine read point. |" % len(Cc))
out.append("| **D** | %d | Not a stat — behavior / engine-mechanism toggle. Stays unwired. |" % len(D))
out.append("| **Total** | 245 | |")
out.append("")

# A section
out.append("## A — Clean wires to EXISTING EFF_* (copy-paste ready)")
out.append("")
for name in A:
    out.append("### `%s`" % name)
    out.append("")
    out.append("```java")
    out.append(A_CODE[name])
    out.append("```")
    out.append("")
out.append("**A count = %d.**" % len(A))
out.append("")

# First batch
out.append("## First-batch recommendation (wire first — highest confidence, lowest risk)")
out.append("")
out.append("Target: A (5) + clean B (17) = 22 IDs. Prefer `WireCrit`/`WireResource`/`WireDefense` where EFF/attr already exist.")
out.append("")
out.append("| # | StatId | Wire file | Action |")
out.append("|---|---|---|---|")
for i,(name,wf,note) in enumerate(FIRST,1):
    out.append("| %d | `%s` | %s | %s |" % (i, name, wf, note))
out.append("")
out.append("For the 5 A entries use the exact code in the A section. For the B entries, add the new `EFF_*` to "
           "`StatEffectIds.java` + `StatEffectGen.java` following the cited existing-EFF pattern, then "
           "`map.put(StatIds.X, self(StatEffectIds.EFF_NEW))` (or `stat(COND,TGT,EFF_NEW)`).")
out.append("")

# B section (grouped)
out.append("## B — New EFF_* over an existing live-read attribute (%d total)" % len(B))
out.append("")
for wf in sorted(Bb):
    out.append("### %s (%d)" % (wf, len(Bb[wf])))
    out.append("")
    for k in sorted(Bb[wf]):
        out.append("- `%s` — %s" % (k, B[k]))
    out.append("")

# C section
out.append("## C — Needs new attribute + new engine read (%d total)" % len(Cc))
out.append("")
for wf in sorted(Cc_g):
    out.append("### %s (%d)" % (wf, len(Cc_g[wf])))
    out.append("")
    for k in sorted(Cc_g[wf]):
        out.append("- `%s` — %s" % (k, Cc[k]))
    out.append("")

# D section
out.append("## D — Not a stat / behavior toggle (%d total)" % len(D))
out.append("")
for wf in sorted(Dd):
    out.append("### %s (%d)" % (wf, len(Dd[wf])))
    out.append("")
    for k in sorted(Dd[wf]):
        out.append("- `%s`" % k)
    out.append("")

open(os.path.join(ROOT,"scripts/classification_report.md"),"w",encoding="utf-8").write("\n".join(out))
print("wrote scripts/classification_report.md  (%d lines)" % len(out))
