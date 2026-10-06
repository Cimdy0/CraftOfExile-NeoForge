"""Batch 29 侦察：实时未接线缺口 + 按「技能标签/机制族」分桶。

输出 poe2_assets/_b29_scout.txt
"""
import re, json, os, collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WDIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")
CORPUS = os.path.join(ROOT, "poe2_assets/poe2_tree_clean.json")

src = open(IDS, encoding="utf-8").read()
consts = re.findall(r'public static final Identifier ([A-Z0-9_]+) = id\("([a-z0-9_]+)"\)', src)
print("StatIds constants:", len(consts))

wired = set()
for fn in os.listdir(WDIR):
    if not fn.endswith(".java"):
        continue
    t = open(os.path.join(WDIR, fn), encoding="utf-8").read()
    wired |= set(re.findall(r'map\.put\(\s*StatIds\.([A-Z0-9_]+)', t))
print("wired map.put keys:", len(wired))

unwired = [(c, p) for c, p in consts if c not in wired]
print("unwired:", len(unwired))

# 语料 raw 描述
raw = {}
try:
    tree = json.load(open(CORPUS, encoding="utf-8"))
    for sid, ent in tree.items():
        if isinstance(ent, dict):
            raw[ent.get("path", "")] = ent.get("raw") or ent.get("text") or ""
except Exception as e:
    print("corpus load fail:", e)

# 技能/机制族令牌
FAMILIES = {
    "detonator": ["DETONATOR"],
    "echo": ["ECHO"],
    "ancestral": ["ANCESTRAL"],
    "trigger": ["TRIGGER"],
    "companion": ["COMPANION"],
    "curse_enemy_target": ["ENEMIES_YOU_CURSE", "ENEMIES_CURSED"],
    "minion_death": ["MINION_DIES", "MINION_DEATH", "WHEN_A_MINION"],
    "warcry": ["WARCRY"],
    "infusion": ["INFUSION"],
    "mark": ["MARK"],
    "hazard": ["HAZARD"],
    "remnant": ["REMNANT"],
    "distinct_count": ["DIFFERENT"],
    "dodge_roll": ["DODGE_ROLL"],
    "es_recharge": ["ENERGY_SHIELD_RECHARGE", "RECHARGE"],
    "brand": ["BRAND"],
    "totem": ["TOTEM"],
    "golem": ["GOLEM"],
    "banner": ["BANNER"],
    "seal": ["SEAL"],
    "offering": ["OFFERING"],
    "grenade": ["GRENADE"],
    "impale": ["IMPALE"],
    "pin": ["PIN"],
    "cull": ["CULL"],
    "fortify": ["FORTIF"],
    "rage": ["RAGE"],
    "glory": ["GLORY"],
    "volatility": ["VOLATILITY"],
}

buckets = collections.defaultdict(list)
for c, p in unwired:
    hit = False
    for fam, toks in FAMILIES.items():
        if any(t in c for t in toks):
            buckets[fam].append((c, p, raw.get(p, "")))
            hit = True
    if not hit:
        buckets["__other__"].append((c, p, raw.get(p, "")))

lines = []
lines.append(f"StatIds={len(consts)}  wired={len(wired)}  unwired={len(unwired)}  coverage={len(wired)/len(consts)*100:.1f}%")
lines.append("")
lines.append("=== 机制族分桶（降序）===")
for fam, items in sorted(buckets.items(), key=lambda kv: -len(kv[1])):
    lines.append(f"\n## {fam}  ({len(items)})")
    for c, p, r in items:
        lines.append(f"  {c}   | {r[:120]}")

open(os.path.join(ROOT, "poe2_assets/_b29_scout.txt"), "w", encoding="utf-8").write("\n".join(lines))
print("written _b29_scout.txt")
