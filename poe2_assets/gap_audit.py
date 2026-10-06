#!/usr/bin/env python3
"""Gap audit: classify all UNWIRED StatIds by what blocks wiring.

Output: poe2_assets/gap_audit.json + summary printed.
Categories:
  A. conditional stat  -> needs a Condition primitive (COND_*), bucketed by concept
  B. unconditional stat -> needs an Effect variant (EFF_*) matching its (attr, scope),
                           OR already has a plausible EFF but was rejected by matcher hygiene.
  C. meta/non-numeric  -> e.g. YOU_CAN_*, CANNOT_*, GRANTS_AURA (engine behavior, not a ModifierStatEffect)
"""
import re, json, collections

ROOT = "src/main/java/com/cimdy/coe"
SID = f"{ROOT}/data/codec/support/stat/StatIds.java"
GEN = f"{ROOT}/data/gen/stat/StatGen.java"
CONDS = f"{ROOT}/data/gen/stat/StatConditionIds.java"

consts = dict(re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', open(SID, encoding="utf-8").read()))
wired = set(re.findall(r'map\.put\(StatIds\.(\w+),', open(GEN, encoding="utf-8").read()))
cond_consts = set(re.findall(r'public static final Identifier (COND_\w+) =', open(CONDS, encoding="utf-8").read()))
unwired = [n for n in consts if n not in wired]

COND_WORDS = ("AGAINST", "WHILE_", "_WHILE_", "IF_", "_IF_", "RECENTLY", "WHEN", "_PER_",
              "FOR_EACH", "ENEMIES", "YOU_CAN", "CANNOT_", "GRANTS_", "APPLY_", "ADD_",
              "WITHERED", "CURSE_WHEN", "CHANCE_TO")
META_PREFIX = ("YOU_CAN", "CANNOT", "YOU_HAVE_NO", "YOU_CAN_EQUIP", "YOU_CAN_HAVE", "YOU_CAN_APPLY",
               "YOU_CAN_WIELD", "GRANTS_", "APPLY_", "ADD_A_", "ADD_IGNITE", "BLIND_ENEMIES",
               "ENEMIES_ARE", "ENEMIES_BLINDED", "BURNING_ENEMIES", "BLEEDING_YOU",
               "UNWITHERED", "RECOVER_OF", "CONSUME_ALL", "EACH_TOTEM", "ENEMIES_AFFECTED",
               "DAMAGE_WITH_HITS_IS_LUCKY", "KNOCKS_BACK", "SKILLS_GAIN", "HEALTH_LEECH",
               "HEALTH_LOSS", "META_SKILLS", "MINIONS_REVIVING", "MINIONS_DEAL", "COMPANIONS_DEAL",
               "ALLIES_IN_YOUR", "OFFERINGS", "WHEN_", "CULLING_STRIKE", "AREA_SKILLS",
               "PROJECTILES_HAVE_CHANCE", "BOLTS_FIRED")

def is_meta(n):
    for p in META_PREFIX:
        if n.startswith(p) or ("_"+p) in n:
            return True
    return False

def cond_concept(n):
    """Heuristic: extract the leading condition clause before the base stat."""
    # cut at first base-stat token
    for cut in ("INC_", "TO_", "MORE_", "ADD_", "GAIN_", "CHANCE_", "MAX_", "YOU_"):
        if n.startswith(cut):
            body = n[len(cut):]
            break
    else:
        body = n
    # for AGAINST X ENEMIES
    m = re.search(r'AGAINST_([A-Z_]+?)_ENEMIES', n)
    if m:
        return ("AGAINST_STATUS", m.group(1))
    if "AGAINST" in n and "ENEMIES" in n:
        return ("AGAINST_OTHER", "x")
    if "_WHILE_" in n or n.endswith("_WHILE"):
        seg = n.split("_WHILE_")[-1]
        return ("WHILE_STATE", seg.split("_")[0])
    if "_IF_" in n:
        seg = n.split("_IF_")[-1]
        return ("IF_COND", seg.split("_")[0])
    if "RECENTLY" in n:
        return ("RECENTLY", "x")
    if "PER_" in n:
        return ("PER_RESOURCE", "x")
    if "FOR_EACH" in n:
        return ("FOR_EACH", "x")
    if "WHEN" in n:
        return ("WHEN_EVENT", "x")
    if "ENEMIES" in n:
        return ("ENEMY_SCOPE", "x")
    return ("UNCONDITIONAL", "x")

cats = collections.defaultdict(list)
meta = []
for n in unwired:
    if is_meta(n):
        meta.append(n)
        continue
    c, sub = cond_concept(n)
    cats[(c, sub)].append(n)

print(f"Total StatIds={len(consts)} wired={len(wired)} unwired={len(unwired)}")
print(f"Meta/non-numeric (not ModifierStatEffect): {len(meta)}")
print(f"Conditional buckets: {sum(len(v) for k,v in cats.items() if k[0]!='UNCONDITIONAL')}")
print(f"Unconditional (non-meta): {len(cats.get(('UNCONDITIONAL','x'),[]))}")

# summarize top condition concepts
print("\n== Condition concept buckets (size>=3) ==")
concept_count = collections.Counter()
for (c, sub), v in cats.items():
    if c in ("UNCONDITIONAL",):
        continue
    concept_count[(c, sub)] = len(v)
for (c, sub), cnt in concept_count.most_common(40):
    print(f"  {c:16} {sub:22} {cnt}")

# unconditional buckets by attribute family
print("\n== Unconditional unwired by prefix ==")
pre = collections.Counter()
for n in cats.get(('UNCONDITIONAL','x'), []):
    pre[n.split('_')[0]] += 1
for k, c in pre.most_common():
    print(f"  {k:14} {c}")

audit = {
    "total_unwired": len(unwired),
    "meta_non_numeric": meta,
    "conditional_buckets": {f"{c}|{sub}": v for (c, sub), v in cats.items() if c != "UNCONDITIONAL"},
    "unconditional": cats.get(('UNCONDITIONAL','x'), []),
}
json.dump(audit, open("poe2_assets/gap_audit.json", "w"), indent=1)
print("\nSaved poe2_assets/gap_audit.json")
