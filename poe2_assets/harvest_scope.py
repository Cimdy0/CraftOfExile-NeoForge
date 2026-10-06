#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Phase 14: scope-tag variants.

Pattern:  <WIRED_BASE>_WITH_<SCOPE>  e.g. INC_DAMAGE_WITH_BOWS
If an EFF already exists whose attribute == base's attribute, op == base's op, and
whose name carries the scope keyword (BOW / FIRE / SPELL / MINION ...), the stat can
be wired by reusing that EFF -- zero new registration, zero engine change.

Scope words are read off the StatId suffix; condition clauses were already handled
by harvest_v2, so we restrict to scope-y keywords here.
"""
import os, re, json, collections

ROOT = r"E:/McModding/Modding/CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return open(p, encoding="utf-8").read()

wired = {}
for f in [os.path.join(GEN, "StatGen.java")] + [os.path.join(WIR, x) for x in sorted(os.listdir(WIR))]:
    for m in re.finditer(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget)\((.*?)\)\);', rd(f), re.S):
        sid, body = m.group(1), m.group(2)
        cond = tgt = eff = None
        for p in [x.strip() for x in body.split(",")]:
            if p.startswith("StatConditionIds."): cond = p.split(".")[1]
            elif p.startswith("StatTargetIds."): tgt = p.split(".")[1]
            elif p.startswith("StatEffectIds."): eff = p.split(".")[1]
        wired[sid] = dict(cond=cond, tgt=tgt, eff=eff)

effs = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', rd(os.path.join(GEN, "StatEffectGen.java")), re.S):
    am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', m.group(2))
    om = re.search(r'AttributeModifier\.Operation\.(\w+)', m.group(2))
    if am and om:
        tags = frozenset() if "TagExpr.ALWAYS" in m.group(2) else frozenset(
            re.findall(r'StatTag\.(\w+)', (re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', m.group(2), re.S) or [None, ''])[1]))
        effs[m.group(1)] = dict(attr=am.group(1), op=om.group(1), tags=tags)

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - set(wired))
by_len = sorted(wired, key=len, reverse=True)

# scope keywords that may appear as a StatTag
SCOPE_WORDS = ["BOW","CROSSBOW","SPEAR","MACE","AXE","SWORD","DAGGER","STAFF","WAND","FIST","CLAW","FLAIL","TRIDENT",
               "FIRE","COLD","LIGHTNING","CHAOS","PHYSICAL","ELEMENTAL",
               "SPELL","ATTACK","MELEE","RANGED","PROJECTILE","AREA","MINION","TOTEM","TRAP","MINE","COMPANION",
               "HERALD","AURA","CURSE","WARCRY","BANNER","GRENADE","HAZARD","STORM","PLANT","NOVA","BALLISTA"]
FILLER = {"WITH","FROM","FOR","AND","TO","OF","IN","ON","AT","YOUR","YOU","THE","A","AN","SKILLS","SKILL","WEAPONS","WEAPON","DAMAGE","HITS","HIT","ALL","EACH","PER"}

cands = []
for u in unwired:
    base = None
    for c in by_len:
        if u.startswith(c + "_"):
            base = c; break
    if not base: continue
    bw = wired[base]
    if bw["cond"] is not None: continue          # conditional handled elsewhere
    bev = effs.get(bw["eff"] or "")
    if not bev: continue
    suffix = u[len(base):]
    kws = [t for t in suffix.strip("_").split("_") if t and t not in FILLER]
    scope = [k for k in kws if k in SCOPE_WORDS]
    if not scope: continue
    # find EFF with same attr+op whose name carries every scope keyword
    hits = [n for n, e in effs.items()
            if e["attr"] == bev["attr"] and e["op"] == bev["op"] and all(k in n for k in scope)]
    if len(hits) == 1:
        cands.append(dict(sid=u, base=base, scope=scope, eff=hits[0],
                          attr=bev["attr"], op=bev["op"], tags=sorted(effs[hits[0]]["tags"])))

print(f"wired={len(wired)} unwired={len(unwired)}")
print(f"\n=== scope-tag candidates: {len(cands)} ===")
for c in cands:
    print(f"  {c['sid']}")
    print(f"      base={c['base']} scope={c['scope']} -> eff={c['eff']}")
    print(f"      attr={c['attr']} op={c['op']} tags={c['tags']}")
json.dump(cands, open(os.path.join(ROOT, "poe2_assets", "harvest_scope.json"), "w"), indent=1)
print("\nwrote poe2_assets/harvest_scope.json")
