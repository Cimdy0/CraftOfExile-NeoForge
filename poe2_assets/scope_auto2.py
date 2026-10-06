#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generalised scope-variant scout (token-difference form).

scope_auto.py only handled the SUFFIX form  <BASE>_WITH_<SCOPE>. A large family
uses a SUBJECT form instead:

    COMPANIONS_HAVE_INC_MAX_HEALTH          == INC_MAX_HEALTH restricted to {COMPANION}
    ATTACKS_USED_BY_BALLISTAS_HAVE_INC_ATTACK_SPEED
                                            == INC_ATTACK_SPEED restricted to {BALLISTA}
    BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT   == INC_AREA_OF_EFFECT restricted to {BANNER}

Rule: find a wired base B (single-EFF ModifierStatEffect) whose token set is a
SUBSET of the unwired U's token set. The difference must be small and must map
entirely onto existing StatTags -- otherwise the scope is not expressible.

Guards (all inherited from scope_auto.py, learned from earlier false positives):
  * corpus guard -- every extra scope word must appear in the game text
  * condition / per-count markers excluded -- those are mechanisms, not scopes
  * base must be a single-EFF wiring (a group base would drop members)
  * base TagExpr containing not(...) is refused (cannot be rebuilt safely)
  * zero-base guard -- no ADD_MULTIPLIED_BASE on a base==0 attribute
"""
import io, re, os, json, glob

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
SUP = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "codec", "support")
WIR = os.path.join(GEN, "wirings")
def rd(p): return io.open(p, encoding="utf-8").read()

tags = set()
for f in glob.glob(os.path.join(ROOT, "src", "main", "java", "**", "StatTag.java"), recursive=True):
    tags |= set(re.findall(r'public static final \w+ (\w+) =', rd(f)))

# singularise a StatId token so it can be matched against a StatTag name
def singular(tok):
    for suf, repl in (("STAVES", "STAFF"), ("IES", "Y"), ("SES", "S"), ("VES", "F")):
        if tok.endswith(suf) and len(tok) > len(suf) + 1:
            return tok[: -len(suf)] + repl
    if tok.endswith("S") and not tok.endswith("SS") and len(tok) > 2:
        return tok[:-1]
    return tok

TOK2TAG = {}
for t in tags:
    TOK2TAG[t] = t
    TOK2TAG[singular(t)] = t
    TOK2TAG[t + "S"] = t
    if t.endswith("Y"):
        TOK2TAG[t[:-1] + "IES"] = t

wired_eff = {}
all_wired = set()          # includes group wirings (N effects) -- a stat bound via
                           # StatGroup IS wired and must never be treated as unwired
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
    return v.get('base') if isinstance(v, dict) else v

statids = set(re.findall(r'public static final Identifier (\w+) =', rd(os.path.join(SUP, "stat", "StatIds.java"))))
unwired = sorted(statids - all_wired)

corpus = json.load(open(os.path.join(ROOT, "poe2_assets", "node_stat_map.json"), encoding="utf-8"))
tmpl = {}
for node, stats in corpus.items():
    for s in stats:
        if s.get("constant") and s.get("template"):
            tmpl.setdefault(s["constant"], set()).add(s["template"])

COND_MARK = ("IF_", "WHILE_", "WHEN_", "RECENTLY", "PER_", "FOR_EACH_", "IN_THE_PAST",
             "UP_TO", "AGAINST_", "HAVEN", "EACH_", "NOT_")
NOT_EXPR = re.compile(r'StatTag\s*\.\s*not\s*\(')
FILLER = {"HAVE", "HAS", "DEAL", "DEALS", "IS", "ARE", "A", "AN", "THE", "OF", "TO", "AND",
          "OR", "WITH", "FOR", "IN", "ON", "AT", "YOU", "YOUR", "USED", "BY", "SKILLS",
          "SKILL", "ALL", "ANY", "GAIN", "GAINS", "THAT", "WHICH", "FROM"}

by_key = {}
for n, v in effs.items():
    by_key.setdefault((v['attr'], v['op'], v['tags']), n)
made = {}

plan, skipped = [], []
for u in unwired:
    if any(m in u for m in COND_MARK):
        continue
    utoks = set(u.split("_")) - FILLER
    ulist = u.split("_")
    ut = " ".join(tmpl.get(u, ())).lower()
    best = None
    for b, bev_name in wired_eff.items():
        if b == u or any(m in b for m in COND_MARK):
            continue
        bev = effs.get(bev_name)
        if not bev:
            continue
        btoks = set(b.split("_")) - FILLER
        if not (btoks < utoks):
            continue
        extra = utoks - btoks
        if not extra or len(extra) > 3:
            continue
        mapped = {TOK2TAG[t] for t in extra if t in TOK2TAG}
        if len(mapped) != len(extra):
            skipped.append((u, b, "extra tokens not expressible as StatTag: " + ",".join(sorted(extra))))
            continue
        if not all(re.search(r'\b' + re.escape(t.lower()) + r'\b', ut) for t in extra):
            skipped.append((u, b, "corpus guard: scope word absent from game text"))
            continue
        decl = next((ln for ln in eff_text.split("\n")
                     if re.search(r'StatEffectIds\.' + re.escape(bev_name) + r'\s*,', ln)), "")
        if NOT_EXPR.search(decl):
            skipped.append((u, b, "base TagExpr uses not(...) -- cannot rebuild"))
            continue
        if bev['op'] == 'ADD_MULTIPLIED_BASE' and base_of(bev['attr']) == 0.0:
            skipped.append((u, b, f"zero-base guard: {bev['attr']} base=0"))
            continue
        cand = (b, frozenset(mapped))
        if best is None or len(b) > len(best[0]):
            best = cand
    if not best:
        continue
    b, mapped = best
    bev = effs[wired_eff[b]]
    newtags = frozenset(set(bev['tags']) | set(mapped))
    if newtags == bev['tags']:
        continue
    key = (bev['attr'], bev['op'], newtags)
    if key in by_key:
        plan.append(dict(sid=u, eff=by_key[key], kind="reuse", base=b,
                         tags=sorted(mapped), corpus=sorted(tmpl.get(u, ()))[:1]))
    else:
        nm = "EFF_" + bev['op'] + "_ATTRIBUTE_" + bev['attr'] + "".join("_" + t for t in sorted(newtags))
        if re.search(r'(?<![A-Za-z0-9_])' + re.escape(nm) + r'(?![A-Za-z0-9_])', eff_text):
            skipped.append((u, b, "name collision " + nm))
            continue
        by_key[key] = nm
        made[nm] = dict(attr=bev['attr'], qual=bev['qual'], op=bev['op'], mode=bev['mode'],
                        tags=sorted(newtags))
        plan.append(dict(sid=u, eff=nm, kind="new", base=b,
                         tags=sorted(mapped), corpus=sorted(tmpl.get(u, ()))[:1]))

print(f"unwired={len(unwired)}")
print(f"\n=== PLAN: {len(plan)} ({sum(1 for p in plan if p['kind']=='new')} new EFF) ===")
for p in plan:
    src = p['corpus'][0][:80] if p['corpus'] else "<no corpus>"
    print(f"  [{p['kind']:5}] {p['sid']}")
    print(f"          base={p['base']} +{p['tags']}  -> {p['eff']}")
    print(f"          corpus: {src}")
print(f"\n=== SKIPPED sample ({len(skipped)}) ===")
seen = set()
for u, b, why in skipped:
    k = why.split(":")[0]
    if k in seen:
        continue
    seen.add(k)
    print(f"  {k}  e.g. {u} (base {b})")

json.dump({"plan": plan, "new_effs": made,
           "skipped": [dict(sid=u, base=b, why=w) for u, b, w in skipped]},
          open(os.path.join(ROOT, "poe2_assets", "scope_auto2.json"), "w"), indent=1, ensure_ascii=False)
print("\nwrote poe2_assets/scope_auto2.json")
