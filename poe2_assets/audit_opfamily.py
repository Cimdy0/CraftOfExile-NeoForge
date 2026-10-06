#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Op-family + semantics audit for StatGen wirings.

Why this exists: scripts/verify_gen.py only checks structure (uniqueness,
ref-resolution, counts, $value). It does NOT check that a wiring's effect
operation matches the StatId operator prefix (INC_/MORE_/TO_/ADD_). A wrong
op is a *silent* semantic bug that 13/13 still passes. This audit closes that.

Checks:
  A. EFF internal consistency: EFF name prefix op == registered Operation.
  B. Wiring op-family: StatId prefix expected op == referenced EFF's registered op.
     ZERO-BASE AWARE: AttributeRegister 三定律 #3「零基禁 inc」— ADD_MULTIPLIED_BASE
     folds to base*Σ, so on a base==0 attribute it silently evaluates to 0 and the
     entry dies. Switching such a wiring to BASE would be fatal, so those reports
     are auditor FALSE POSITIVES and are reported separately, not as failures.
  C. Isolate wirings that reference the session-introduced EFF/COND constants,
     print full (cond, tgt, eff, op, tags) for manual semantics review.
"""
import os, re, json, sys

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

def _statgen_sources():
    """StatGen.java plus every wirings/Wire*.java partition (wirings were split out
    of StatGen, so parsing only StatGen.java would silently yield zero wirings)."""
    base = os.path.join(GEN, "StatGen.java")
    parts = [read(base)]
    wdir = os.path.join(GEN, "wirings")
    if os.path.isdir(wdir):
        for f in sorted(os.listdir(wdir)):
            if f.startswith("Wire") and f.endswith(".java"):
                parts.append(read(os.path.join(wdir, f)))
    return "\n".join(parts)


def read(p):
    with open(p, "r", encoding="utf-8") as f:
        return f.read()

# ---------- parse StatEffectGen -> EFF constant -> (op, attr, tags, rawclass) ----------
eg = read(os.path.join(GEN, "StatEffectGen.java"))
# each line: m.put(StatEffectIds.NAME, new XxxStatEffect(...))
eff = {}  # name -> dict
put_pat = re.compile(r'm\.put\(StatEffectIds\.(\w+),\s*new\s+(\w+)\((.*)\)\);')
for m in put_pat.finditer(eg):
    name = m.group(1)
    cls = m.group(2)
    body = m.group(3)
    rec = {"class": cls, "op": None, "attr": None, "tags": []}
    if cls == "ModifierStatEffect":
        # ModifierStatEffect(attr, Either.left(AttributeModifier.Operation.OP), Mode, tags)
        am = re.search(r'(?:AttributeRegister|Attributes)\.(\w+)', body)
        if am: rec["attr"] = am.group(1)
        om = re.search(r'AttributeModifier\.Operation\.(\w+)', body)
        if om: rec["op"] = om.group(1)
        # tags: TagExpr.of(Set.of(StatTag.X, ...)) or TagExpr.ALWAYS
        if "TagExpr.ALWAYS" in body:
            rec["tags"] = ["ALWAYS"]
        else:
            tm = re.search(r'TagExpr\.of\(Set\.of\((.*?)\)\)', body)
            if tm:
                rec["tags"] = re.findall(r'StatTag\.(\w+)', tm.group(1))
    eff[name] = rec

# EFF name prefix -> declared op
def name_op(n):
    if n.startswith("EFF_ADD_MULTIPLIED_BASE_"): return "ADD_MULTIPLIED_BASE"
    if n.startswith("EFF_ADD_VALUE_"): return "ADD_VALUE"
    if n.startswith("EFF_ADD_MULTIPLIED_TOTAL_"): return "ADD_MULTIPLIED_TOTAL"
    return None

# ---------- parse StatGen buildEntries -> StatId -> (cond, tgt, eff) ----------
# Wirings were split out of StatGen into stat/wirings/Wire*.java, so buildEntries()
# now only contains `WireXxx.build(map)` calls. Scan ALL sources instead of just the
# method body, otherwise the gate silently parses zero wirings and false-PASSes.
sg = _statgen_sources()
block = sg
wires = []  # (statid, cond, tgt, eff, rawargs)
put_pat2 = re.compile(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget|group|groupSelf|groupSelfWhen)\((.*?)\)\);')
for m in put_pat2.finditer(block):
    sid = m.group(1)
    args = m.group(2)
    # top-level split by comma (respect parens)
    depth = 0; parts = []; cur = ""
    for ch in args:
        if ch in "([{": depth += 1
        elif ch in ")]}": depth -= 1
        if ch == "," and depth == 0:
            parts.append(cur); cur = ""
        else:
            cur += ch
    if cur.strip(): parts.append(cur)
    conds = []; tgts = []; effs = []
    for p in parts:
        ps = p.strip()
        mc = re.search(r'StatConditionIds\.(\w+)', ps)
        mt = re.search(r'StatTargetIds\.(\w+)', ps)
        me = re.search(r'StatEffectIds\.(\w+)', ps)
        if mc: conds.append(mc.group(1))
        if mt: tgts.append(mt.group(1))
        if me: effs.append(me.group(1))
    # 组合条目（groupSelf / groupSelfWhen / group）：N 个成员各自都要做 op 族与死接线检查，
    # 故按成员展开成 N 条审计记录（sid 相同，effect 各异）。
    if effs:
        for e in effs:
            wires.append((sid, conds[0] if conds else None, tgts[0] if tgts else None, e, args))
    else:
        wires.append((sid, conds[0] if conds else None, tgts[0] if tgts else None, None, args))

# ---------- expected op from StatId prefix ----------
def expected_op(name):
    if name.startswith(("INC_", "INCREASED_", "REDUCED_")): return "ADD_MULTIPLIED_BASE"
    if name.startswith(("MORE_", "GREATER_", "LESS_")): return "ADD_MULTIPLIED_TOTAL"
    if name.startswith(("TO_", "ADD_", "ADDITIONAL_")): return "ADD_VALUE"
    return None  # no clear op prefix -> skip op check

# ---------- zero-base awareness (AttributeRegister 三定律 #3: 零基禁 inc) ----------
# ADD_MULTIPLIED_BASE == base * Σmult. If the attribute's registered default base
# is 0, an "increased" wiring wired to BASE silently contributes 0 -> dead entry.
# Such reports are false positives: the current op is the only viable one.
_bt_path = os.path.join(ROOT, "poe2_assets", "attr_base_table.json")
ATTR_BASE = {}
if os.path.isfile(_bt_path):
    ATTR_BASE = {k: v["base"] for k, v in json.load(open(_bt_path, encoding="utf-8")).items()}
# vanilla (net.minecraft.world.entity.ai.attributes.Attributes) registered defaults
VANILLA_BASE = {"ARMOR": 0.0, "ARMOR_TOUGHNESS": 0.0, "ATTACK_DAMAGE": 1.0,
                "ATTACK_SPEED": 1.0, "MOVEMENT_SPEED": 0.1, "MAX_HEALTH": 20.0,
                "KNOCKBACK_RESISTANCE": 0.0, "LUCK": 0.0, "BLOCK_CHANCE": 0.0,
                "MAX_ABSORPTION": 0.0, "JUMP_STRENGTH": 0.42, "SCALE": 1.0,
                "GRAVITY": 0.08, "SAFE_FALL_DISTANCE": 3.0, "FALL_DAMAGE_MULTIPLIER": 1.0}
def attr_base(attr):
    if attr in ATTR_BASE: return ATTR_BASE[attr], "CoE"
    if attr in VANILLA_BASE: return VANILLA_BASE[attr], "vanilla"
    return None, "?"

# ---------- semantics allowlist: prefix says X, corpus/source proves Y ----------
# Each entry was verified one-by-one against the PoE2 corpus or source comments.
# R3 discipline: never bulk-guessed; evidence recorded inline.
SEMANTIC_ALLOWLIST = {
    "ADD_RECENTLY_CONSUMED_DAMAGE":
        "corpus: none; BuffStatusTypes.RECENTLY_CONSUMED comment = 'inc_damage_attack 式增伤'; "
        "EFF tag = TagExpr(DAMAGE & not(TAKEN)) i.e. dealt-damage side -> ADD_MULTIPLIED_BASE is correct",
    "INC_MAX_RAGE":
        "corpus has only '+N to Maximum Rage'(flat) and '100% more Maximum Rage'; no 'increased Maximum Rage' "
        "exists -> no-sourcing; kept aligned with its flat twin TO_MAX_RAGE, R2 says do not touch",
}
SEMANTIC_ALLOWLIST_PREFIX = [
    ("INC_REGENERATE_LIFE_",
     "corpus: 'Regenerate X% of maximum Life per second' is a FLAT additive, not increased "
     "-> ADD_VALUE is correct; the INC_ prefix is a generation-time naming artifact"),
]
def allowlist_reason(sid):
    if sid in SEMANTIC_ALLOWLIST: return SEMANTIC_ALLOWLIST[sid]
    for p, why in SEMANTIC_ALLOWLIST_PREFIX:
        if sid.startswith(p): return why
    return None

# ---------- CHECK A: EFF name vs registered op ----------
print("="*70)
print("CHECK A: EFF name-prefix op vs registered Operation (internal consistency)")
print("="*70)
a_fail = 0
for n, r in eff.items():
    if r["class"] != "ModifierStatEffect": continue
    no = name_op(n)
    if no and r["op"] and no != r["op"]:
        a_fail += 1
        print(f"  MISMATCH {n}: name says {no} but registered {r['op']}  (attr={r['attr']})")
print(f"  -> {a_fail} EFF internal op mismatches")

# ---------- CHECK B: wiring op-family ----------
print("="*70)
print("CHECK B: StatId prefix expected op vs EFF registered op (wiring)")
print("="*70)
b_fail, b_zerobase, b_allow = [], [], []
for sid, cond, tgt, effc, args in wires:
    if effc is None: continue
    r = eff.get(effc)
    if not r: 
        print(f"  [ref-missing] {sid} -> {effc}"); continue
    if r["class"] != "ModifierStatEffect": continue
    exp = expected_op(sid)
    if exp is None: continue
    actual = r["op"]
    if exp == actual: continue
    base, src = attr_base(r["attr"])
    # zero-base: switching to ADD_MULTIPLIED_BASE would make the entry silently dead
    if exp == "ADD_MULTIPLIED_BASE" and base == 0:
        b_zerobase.append((sid, effc, exp, actual, cond, tgt, r["attr"], src)); continue
    why = allowlist_reason(sid)
    if why:
        b_allow.append((sid, effc, exp, actual, cond, tgt, why)); continue
    b_fail.append((sid, effc, exp, actual, cond, tgt))

print(f"  -> {len(b_zerobase)} zero-base EXEMPT (三定律#3: base==0, inc would silently die)")
for sid, effc, exp, actual, cond, tgt, attr, src in sorted(b_zerobase):
    print(f"    {sid:50} attr={attr}({src} base=0)  keeps {actual}")
print(f"\n  -> {len(b_allow)} semantics-allowlisted (corpus/source proves current op is correct)")
for sid, effc, exp, actual, cond, tgt, why in sorted(b_allow):
    print(f"    {sid:50} prefix wants {exp}, keeps {actual}")
    print(f"        evidence: {why}")
print(f"\n  -> {len(b_fail)} ACTIONABLE op-family mismatches:\n")
for sid, effc, exp, actual, cond, tgt in sorted(b_fail):
    print(f"  {sid}")
    print(f"      expects {exp} (prefix) but EFF {effc} is {actual}"
          + (f"  [cond={cond}, tgt={tgt}]" if (cond or tgt) else ""))

# ---------- CHECK C: isolate session-introduced constants ----------
print("="*70)
print("CHECK C: wirings referencing session-introduced EFF/COND constants")
print("="*70)
new_eff = [n for n in eff if "COE_CRIT_CHANCE" in n or "COE_CRIT_DAMAGE" in n]
# session COND constants: those added this session (pure-data extension)
cond_names = set()
cg = read(os.path.join(GEN, "StatConditionGen.java"))
for m in re.finditer(r'm\.put\(StatConditionIds\.(\w+),', cg):
    cond_names.add(m.group(1))
# heuristically the new ones: a set we introduced. Pull from memory of batches:
session_cond = ["COND_BLIND_STATUS","COND_CHILL_STATUS","COND_HINDER_STATUS","COND_IGNITE_STATUS",
    "COND_MAIM_STATUS","COND_POISON_STATUS","COND_SLOW_STATUS","COND_CHANNELLING_LIVING_STATE",
    "COND_LEECHING_LIVING_STATE","COND_MOVING_LIVING_STATE","COND_SPRINTING_LIVING_STATE",
    "COND_DUAL_WIELD_LIVING_STATE","COND_HOLDING_SHIELD_LIVING_STATE"]
print("  New EFFs introduced this session:")
for n in new_eff: print(f"    {n}  op={eff[n]['op']}  attr={eff[n]['attr']}  tags={eff[n]['tags']}")
print("\n  Wirings using session COND (full detail for semantics review):")
cnt = 0
for sid, cond, tgt, effc, args in wires:
    if cond in session_cond:
        cnt += 1
        r = eff.get(effc)
        op = r["op"] if r else "?"
        tags = r["tags"] if r else []
        print(f"    {sid}")
        print(f"        cond={cond}  tgt={tgt}  eff={effc}")
        print(f"        eff_op={op}  eff_attr={r['attr'] if r else '?'}  eff_tags={tags}")
print(f"  -> {cnt} wirings using session COND constants")

# ---------- CHECK D: dead EFF / dead wiring ----------
# CHECK B treats "INC_* -> ADD_MULTIPLIED_BASE" as CORRECT. But if the attribute's
# registered base is 0, fold() computes baseValue * ΣmultBase (StatEngine.fold uses
# living.getAttributeBaseValue, NOT the current value) -> always 0. The wiring looks
# op-correct yet contributes nothing. CHECK B cannot see this; CHECK D can.
print("="*70)
print("CHECK D: DEAD EFF — ADD_MULTIPLIED_BASE on a base==0 attribute (silently contributes 0)")
print("="*70)
dead_effs = [n for n, r in eff.items()
             if r["class"] == "ModifierStatEffect" and r["op"] == "ADD_MULTIPLIED_BASE"
             and attr_base(r["attr"])[0] == 0]
dead_wires = [(sid, effc, cond, tgt) for sid, cond, tgt, effc, _a in wires if effc in dead_effs]
print(f"  -> {len(dead_effs)} dead EFF(s);  {len(dead_wires)} DEAD WIRING(S) using them")
for n in sorted(dead_effs):
    b, src = attr_base(eff[n]["attr"])
    users = [s for s, e, _c, _t in dead_wires if e == n]
    print(f"    [dead EFF] {n}")
    print(f"        attr={eff[n]['attr']}({src} base={b})  tags={eff[n]['tags']}"
          + (f"  USED BY: {', '.join(sorted(users))}" if users else "  (unused — latent trap)"))

# ---------- summary counts ----------
print("="*70)
print("SUMMARY")
print("="*70)
print(f"  EFFs parsed: {len(eff)}  (ModifierStatEffect: {sum(1 for r in eff.values() if r['class']=='ModifierStatEffect')})")
print(f"  Wirings parsed: {len(wires)}")
print(f"  CHECK A fails: {a_fail}")
print(f"  CHECK B actionable: {len(b_fail)}")
print(f"  CHECK B zero-base exempt: {len(b_zerobase)}   (三定律#3: base==0 → inc would die)")
print(f"  CHECK B semantics-allowlisted: {len(b_allow)}   (corpus/source proves current op correct)")
print(f"  CHECK B raw prefix mismatches: {len(b_fail)+len(b_zerobase)+len(b_allow)}")
print(f"  CHECK D dead EFFs: {len(dead_effs)}   dead wirings: {len(dead_wires)}")
print(f"  Session COND wirings: {cnt}")
