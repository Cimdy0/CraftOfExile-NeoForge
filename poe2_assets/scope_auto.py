#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Automated scope-tag variant scout + builder (Phase 20).

Pattern:  <WIRED_BASE> + a scope word that exists as a StatTag.
Example:  INC_ACCURACY_WITH_BOWS  ==  INC_ACCURACY  restricted to {BOW}.

Two guards make this safe enough to run in bulk:

1. CORPUS GUARD -- the scope word must actually appear in the game-text template.
   A StatId naming hit alone is not accepted (names are noisy; the corpus is the
   authoritative source).

2. ZERO-BASE GUARD -- if the attribute's base is 0.0, an ADD_MULTIPLIED_BASE EFF
   silently contributes nothing (three-laws #3), so such a variant is never built.

Everything else (attribute, operation, mode) is inherited from the wired base EFF,
so no engine change is involved. Results are printed for review before applying.
"""
import io, re, os, json, glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return io.open(p, encoding="utf-8").read()

# ---- StatTags that denote a weapon / skill scope ----
SCOPE_TAGS = ["AXE", "BOW", "CROSSBOW", "MACE", "MELEE", "MINE", "SPEAR", "SPELL", "SWORD",
              "TOTEM", "TRAP", "PROJECTILE", "BALLISTA", "HAZARD", "AREA", "WARCRY",
              "COMPANION", "MINION", "HERALD", "AURA", "CURSE", "GRENADE", "BEAST",
              "STORM", "PLANT", "NOVA", "CHANNELLED"]
PLURAL = {"BOW": "BOWS", "CROSSBOW": "CROSSBOWS", "SPEAR": "SPEARS", "AXE": "AXES",
          "SWORD": "SWORDS", "MACE": "MACES", "MINE": "MINES", "TRAP": "TRAPS",
          "TOTEM": "TOTEMS", "SPELL": "SPELLS", "BEAST": "BEASTS", "COMPANION": "COMPANIONS",
          "MINION": "MINIONS", "HERALD": "HERALDS", "GRENADE": "GRENADES", "NOVA": "NOVAS",
          "PLANT": "PLANTS", "PROJECTILE": "PROJECTILES", "BALLISTA": "BALLISTAS",
          "HAZARD": "HAZARDS", "AREA": "AREAS", "WARCRY": "WARCRIES"}

tags = set()
for f in glob.glob(os.path.join(ROOT, "src", "main", "java", "**", "StatTag.java"), recursive=True):
    tags |= set(re.findall(r'public static final \w+ (\w+) =', rd(f)))
SCOPE_TAGS = [t for t in SCOPE_TAGS if t in tags]

# ---- wired truth table ----
wired_eff = {}
all_wired = set()          # includes group wirings -- a StatGroup-bound stat IS wired
for f in [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, x) for x in sorted(os.listdir(WIR))]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:\w+)\((.*?)\)\);', rd(f), re.S):
        e = re.findall(r'StatEffectIds\.(\w+)', m.group(2))
        all_wired.add(m.group(1))
        if len(e) == 1:
            wired_eff[m.group(1)] = e[0]

eff_text = rd(os.path.join(GEN, "StatEffectGen.java"))
effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eff_text, re.S):
    am = re.search(r'(AttributeRegister|Attributes)\.(\w+)', m.group(2))
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', m.group(2))
    md = re.search(r'ModifierStatEffect\.Mode\.(\w+)', m.group(2))
    tg = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', m.group(2), re.S)
    tagset = frozenset() if not tg else frozenset(re.findall(r'StatTag\.(?:\w+\()?(\w+)', tg.group(1)))
    if am and om:
        effs[m.group(1)] = dict(attr=am.group(2), qual=am.group(1), op=om.group(1),
                                mode=(md.group(1) if md else 'GAIN'), tags=tagset)

base_tbl = json.load(open(os.path.join(ROOT, "poe2_assets", "attr_base_table.json"), encoding="utf-8"))
rows = base_tbl if isinstance(base_tbl, dict) else {r['attribute']: r for r in base_tbl}
def base_of(attr):
    v = rows.get(attr)
    if isinstance(v, dict):
        return v.get('base')
    return v

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - all_wired)
bylen = sorted(wired_eff, key=len, reverse=True)

corpus = json.load(open(os.path.join(ROOT, "poe2_assets", "node_stat_map.json"), encoding="utf-8"))
tmpl = {}
for node, stats in corpus.items():
    for s in stats:
        if s.get("constant") and s.get("template"):
            tmpl.setdefault(s["constant"], set()).add(s["template"])

by_key = {}
for n, v in effs.items():
    by_key.setdefault((v['attr'], v['op'], v['tags']), n)

# Precision guards (learned the hard way -- the first pass was far too permissive):
#   * reject per-count / conditional phrasings; those are MECHANISMS, not scopes
#   * base must be a SINGLE-EFF wiring (a group base would silently drop members)
#   * refuse any base whose TagExpr contains not(...) -- it cannot be rebuilt safely
#   * the scope word must sit near the END of the StatId (a restriction, not a topic)
COND_MARK = ("IF_", "WHILE_", "WHEN_", "RECENTLY", "PER_", "FOR_EACH_", "IN_THE_PAST",
             "UP_TO", "AGAINST_", "HAVEN", "EACH_")
NOT_EXPR = re.compile(r'StatTag\s*\.\s*not\s*\(')

plan, skipped = [], []
for u in unwired:
    if any(m in u for m in COND_MARK):
        continue
    base = next((c for c in bylen if u.startswith(c + "_")), None)
    if not base:
        continue
    if any(m in base for m in COND_MARK):
        continue
    bev_name = wired_eff[base]
    bev = effs.get(bev_name)
    if not bev:
        skipped.append((u, "-", "base EFF is not a ModifierStatEffect"))
        continue
    # check ONLY this EFF's own declaration line -- a windowed search bleeds into
    # the next declaration and rejects everything
    decl = next((ln for ln in eff_text.split("\n")
                 if re.search(r'StatEffectIds\.' + re.escape(bev_name) + r'\s*,', ln)), "")
    if NOT_EXPR.search(decl):
        skipped.append((u, "-", "base TagExpr uses not(...) -- cannot rebuild safely"))
        continue
    tail_toks = [t.lower() for t in u.split("_")[-4:]]
    ut = " ".join(tmpl.get(u, ())).lower()
    for T in SCOPE_TAGS:
        words = [T.lower(), PLURAL.get(T, T).lower()]
        if not any(w in tail_toks for w in words):
            continue
        if not any(re.search(r'\b' + re.escape(w) + r'\b', ut) for w in words):
            skipped.append((u, T, "corpus guard: scope word not in game text"))
            continue
        newtags = frozenset(set(bev['tags']) | {T})
        if newtags == bev['tags']:
            continue
        if bev['op'] == 'ADD_MULTIPLIED_BASE' and base_of(bev['attr']) == 0.0:
            skipped.append((u, T, f"zero-base guard: {bev['attr']} base=0 forbids ADD_MULTIPLIED_BASE"))
            continue
        key = (bev['attr'], bev['op'], newtags)
        if key in by_key:
            plan.append((u, by_key[key], T, base, "reuse"))
        else:
            nm = "EFF_" + bev['op'] + "_ATTRIBUTE_" + bev['attr'] + "".join("_" + t for t in sorted(newtags))
            if nm in effs:
                skipped.append((u, T, "name collision"))
                continue
            by_key[key] = nm
            effs[nm] = dict(attr=bev['attr'], qual=bev['qual'], op=bev['op'], mode=bev['mode'], tags=newtags)
            plan.append((u, nm, T, base, "new"))
        break

print(f"unwired={len(unwired)}")
print(f"\n=== PLAN: {len(plan)} scope variants ({sum(1 for p in plan if p[4]=='new')} new EFF) ===")
for u, eff, T, base, kind in plan:
    src = sorted(tmpl.get(u, ()))[0][:78] if tmpl.get(u) else "<no corpus>"
    print(f"  [{kind:5}] {u}")
    print(f"          base={base} +{T}  -> {eff}")
    print(f"          corpus: {src}")
print(f"\n=== SKIPPED: {len(skipped)} ===")
for u, T, why in skipped[:25]:
    print(f"  {u} (+{T}): {why}")

json.dump({"plan": [dict(sid=u, eff=e, tag=T, base=b, kind=k,
                         corpus=sorted(tmpl.get(u, ()))[:1]) for u, e, T, b, k in plan],
           "skipped": [dict(sid=u, tag=T, why=w) for u, T, w in skipped]},
          open(os.path.join(ROOT, "poe2_assets", "scope_auto.json"), "w"), indent=1, ensure_ascii=False)
print("\nwrote poe2_assets/scope_auto.json")
