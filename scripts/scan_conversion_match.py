#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Match unwired conversion StatIds against registered DamageConvertStatEffect EFFs.

For each unwired conversion StatId we parse its SEMANTICS (from-elements, to-element,
mode GAIN/REPLACE/NULLIFY, scope/condition) and find candidate EFFs whose (from,to,mode)
signature matches. Scope/condition mismatches are reported separately (wireable via
onTarget/self + existing COND, or need a new tag-scoped EFF variant).
"""
import re, os

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")

# ---------- parse registered convert EFFs ----------
eff_txt = open(os.path.join(GEN, "StatEffectGen.java"), encoding="utf-8").read()
# capture: m.put(StatEffectIds.EFF_X, new DamageConvertStatEffect( ... ));
pat = re.compile(
    r'StatEffectIds\.(EFF_\w*CONVERT\w*),\s*new\s+DamageConvertStatEffect\((.*?)\)\);',
    re.S)
effs = {}  # name -> dict
for name, args in pat.findall(eff_txt):
    fromset = re.search(r'Set\.of\(([^)]*)\)', args)
    fromset = [t.strip() for t in fromset.group(1).split(',')] if fromset else []
    fromset = [t.replace('StatTag.', '') for t in fromset]
    mode_m = re.search(r'Mode\.(\w+)', args)
    mode = mode_m.group(1) if mode_m else '?'
    to_m = re.search(r'ToMode\.(\w+)', args)
    toMode = to_m.group(1) if to_m else 'FIXED'
    # to element: 2nd arg is StatTag.X or ""
    to_el = ''
    tm = re.search(r',\s*StatTag\.(\w+),\s*DamageConvertStatEffect\.Mode', args)
    if tm:
        to_el = tm.group(1)
    # candidates
    cand = re.findall(r'List\.of\(([^)]*StatTag\.[A-Z_]+[^)]*)\)', args)
    candidates = []
    for c in cand:
        candidates += [x.strip().replace('StatTag.', '') for x in c.split(',')]
    # tags (last TagExpr)
    tags_all = re.findall(r'TagExpr\.of\(Set\.of\(([^)]*)\)\)', args)
    tags = []
    if tags_all:
        tags = [t.strip().replace('StatTag.', '') for t in tags_all[-1].split(',') if t.strip()]
    effs[name] = {'from': fromset, 'to': to_el, 'mode': mode,
                  'toMode': toMode, 'candidates': candidates, 'tags': tags}

# ---------- parse wired keys ----------
wired = set()
srcs = [open(os.path.join(GEN, "StatGen.java"), encoding="utf-8").read()]
wdir = os.path.join(GEN, "wirings")
for f in sorted(os.listdir(wdir)):
    if f.startswith("Wire") and f.endswith(".java"):
        srcs.append(open(os.path.join(wdir, f), encoding="utf-8").read())
block = "\n".join(srcs)
for m in re.findall(r'map\.put\(StatIds\.(\w+),', block):
    wired.add(m)

sid_all = set(re.findall(r'public static final Identifier (\w+) =',
                          open(os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"), encoding="utf-8").read()))
unwired = sid_all - wired

# ---------- parse conversion StatId semantics ----------
ELEMS = ['PHYSICAL', 'CHAOS', 'FIRE', 'COLD', 'LIGHTNING']
ALLF = set(ELEMS)

ELEM_SET = set(ELEMS)

def _from_of(phrase):
    """Extract the source element from a '..._DAMAGE' phrase preceding AS_EXTRA/CONVERTED."""
    if 'ELEMENTAL' in phrase:
        return {'FIRE', 'COLD', 'LIGHTNING'}
    if 'PHYSICAL' in phrase:
        return {'PHYSICAL'}
    if 'CHAOS' in phrase:
        return {'CHAOS'}
    if 'COLD' in phrase:
        return {'COLD'}
    if 'LIGHTNING' in phrase:
        return {'LIGHTNING'}
    if 'FIRE' in phrase:
        return {'FIRE'}
    return set(ELEMS)  # bare "DAMAGE" = all 5

def parse_statid(k):
    """Return (from_set, to, mode, scope) parsed from name, with PRECISE source element."""
    s = k
    mode = 'GAIN'
    if 'CONVERTED_TO' in s or s.startswith('LIVINGMOD_CONVERT'):
        mode = 'REPLACE'
    if 'NULLIFY' in s:
        mode = 'NULLIFY'
    # to-element
    to = None
    mto = re.search(r'AS_EXTRA_(\w+)_DAMAGE|CONVERTED_TO_(\w+)_DAMAGE|CONVERT_PHYS_TO_(\w+)', s)
    if mto:
        to = (mto.group(1) or mto.group(2) or mto.group(3)).upper()
    # from-elements: find the phrase before AS_EXTRA / CONVERTED_TO / CONVERT_PHYS_TO
    frm = set(ELEMS)
    if s.startswith('LIVINGMOD_CONVERT_PHYS'):
        frm = {'PHYSICAL'}
    else:
        mfrom = re.search(r'([A-Z0-9_]*?DAMAGE)_AS_EXTRA', s) or re.search(r'([A-Z0-9_]*?DAMAGE)_CONVERTED_TO', s)
        if mfrom:
            frm = _from_of(mfrom.group(1))
    # scope / condition
    scope = []
    if 'AGAINST_FREEZE' in s: scope.append('AGAINST:FREEZE')
    if 'AGAINST_DAZED' in s: scope.append('AGAINST:DAZE')
    if 'AGAINST_SHOCKED' in s: scope.append('AGAINST:SHOCK')
    if 'AGAINST_HEAVY_STUNNED' in s: scope.append('AGAINST:HEAVY_STUN')
    if 'AGAINST_CHILLED' in s: scope.append('AGAINST:CHILL')
    if 'AGAINST_ELECTROCUTED' in s: scope.append('AGAINST:ELECTROCUTE')
    if 'WHILE_ON_CHILLED_GROUND' in s: scope.append('SELF:CHILLED_GROUND')
    if 'WHILE_ON_IGNITED_GROUND' in s: scope.append('SELF:IGNITED_GROUND')
    if 'WHILE_ON_SHOCKED_GROUND' in s: scope.append('SELF:SHOCKED_GROUND')
    if 'WHILE_SHAPESHIFTED' in s: scope.append('SELF:SHAPESHIFTED')
    if 'EMPOWERED_ATTACKS' in s: scope.append('TAG:EMPOWERED')
    if 'MINIONS' in s: scope.append('TAG:COMPANION')
    if 'PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY' in s: scope.append('COND:RECENTLY_CONSUMED_ENDURANCE')
    if 'RANDOM_ELEMENTAL' in s: scope.append('RANDOM:ELEMENTAL')
    return frm, to, mode, scope
    # scope / condition
    scope = []
    if 'AGAINST_FREEZE' in s: scope.append('AGAINST:FREEZE')
    if 'AGAINST_DAZED' in s: scope.append('AGAINST:DAZE')
    if 'AGAINST_SHOCKED' in s: scope.append('AGAINST:SHOCK')
    if 'AGAINST_HEAVY_STUNNED' in s: scope.append('AGAINST:HEAVY_STUN')
    if 'AGAINST_CHILLED' in s: scope.append('AGAINST:CHILL')
    if 'AGAINST_ELECTROCUTED' in s: scope.append('AGAINST:ELECTROCUTE')
    if 'WHILE_ON_CHILLED_GROUND' in s: scope.append('SELF:CHILLED_GROUND')
    if 'WHILE_ON_IGNITED_GROUND' in s: scope.append('SELF:IGNITED_GROUND')
    if 'WHILE_ON_SHOCKED_GROUND' in s: scope.append('SELF:SHOCKED_GROUND')
    if 'WHILE_SHAPESHIFTED' in s: scope.append('SELF:SHAPESHIFTED')
    if 'EMPOWERED_ATTACKS' in s: scope.append('TAG:EMPOWERED')
    if 'MINIONS' in s: scope.append('TAG:COMPANION')
    if 'PER_ENDURANCE_CHARGE_CONSUMED_RECENTLY' in s: scope.append('COND:RECENTLY_CONSUMED_ENDURANCE')
    if 'RANDOM_ELEMENTAL' in s: scope.append('RANDOM:ELEMENTAL')
    return frm, to, mode, scope

conv_kw = re.compile(r'CONVERT|AS_EXTRA|CONVERTED_TO|LIVINGMOD_CONVERT', re.I)
conv_ids = [k for k in sorted(unwired) if conv_kw.search(k)]

print("=== REGISTERED convert EFFs:", len(effs), "===")
print("=== UNWIRED conversion StatIds:", len(conv_ids), "===\n")

# Build signature index: (frozenset(from), to, mode, toMode, tuple(candidates)) -> [eff names]
sig = {}
for n, d in effs.items():
    key = (frozenset(d['from']), d['to'], d['mode'], d['toMode'], tuple(sorted(d['candidates'])))
    sig.setdefault(key, []).append(n)

for k in conv_ids:
    frm, to, mode, scope = parse_statid(k)
    key = (frozenset(frm), to, mode, 'FIXED' if mode != 'NULLIFY' else 'FIXED', ())
    cands = []
    if 'RANDOM:ELEMENTAL' in scope:
        key = (frozenset(frm), '', mode, 'RANDOM', tuple(sorted(['FIRE', 'COLD', 'LIGHTNING'])))
    exact = sig.get(key, [])
    print(f"{k}")
    print(f"   parsed: from={sorted(frm)} to={to} mode={mode} scope={scope}")
    if exact:
        print(f"   MATCH  -> {exact}")
    else:
        print(f"   NO EXACT EFF (need new EFF variant or different signature)")
    print()
