#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Apply the generalised scope-variant plan (scope_auto2.json) with two corrections.

Corrections applied here, after manual verification:

1. MINIONS_DEAL_INC_DAMAGE -- the scanner wanted to mint DAMAGE+{MINION} with no
   DAMAGE tag, but EFF_..._DAMAGE_DAMAGE_MINION (DAMAGE+{DAMAGE,MINION}) is already
   used by INC_MINION_DAMAGE, which is the SAME corpus line. Reuse it instead of
   creating a near-duplicate with subtly different scope.

2. INC_ATTACK_AND_CAST_SPEED_WITH_{ELEMENTAL,LIGHTNING}_SKILLS -- these are hybrid
   (attack AND cast speed sharing one X%), so they must be StatGroups, not a single
   EFF. The scanner only emits single-EFF plans, so they are built here by hand.
"""
import io, re, os, json

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
GEN = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat")
WIR = os.path.join(GEN, "wirings")
def rd(p): return io.open(p, encoding="utf-8").read()
def wr(p, s): io.open(p, "w", encoding="utf-8").write(s)

data = json.load(open(os.path.join(ROOT, "poe2_assets", "scope_auto2.json"), encoding="utf-8"))

OVERRIDE_EFF = {
    # reuse the EFF already bound to the identical corpus line
    "MINIONS_DEAL_INC_DAMAGE": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MINION",
}
SKIP = {"INC_ATTACK_AND_CAST_SPEED_WITH_ELEMENTAL_SKILLS",
        "INC_ATTACK_AND_CAST_SPEED_WITH_LIGHTNING_SKILLS"}

DOMAIN = {"ACCURACY": "WireDefense", "ATTACK_SPEED": "WireSpeed", "CAST_SPEED": "WireSpeed",
          "CRIT_CHANCE": "WireDamage", "CRIT_DAMAGE": "WireDamage", "DAMAGE": "WireDamage",
          "EFFECT_AREA": "WireMisc", "PROJECTILE_SPEED": "WireMisc",
          "RESERVATION_EFFICIENCY": "WireResource", "AILMENT_BUILDUP": "WireAilment",
          "AILMENT_MAGNITUDE": "WireAilment", "AILMENT_DURATION": "WireAilment",
          "MAX_HEALTH": "WireResource", "MOVEMENT_SPEED": "WireSpeed",
          "GLORY_GENERATION": "WireResource", "COOLDOWN_RECOVERY_RATE": "WireSpeed"}

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

new_effs = []     # (name, attr, qual, op, mode, tags)
plan = []         # (statId, expr, domain, note)

made = data["new_effs"]
for p in data["plan"]:
    sid = p["sid"]
    if sid in SKIP:
        continue
    if sid in OVERRIDE_EFF:
        eff = OVERRIDE_EFF[sid]
        assert re.search(r'(?<![A-Za-z0-9_])' + re.escape(eff) + r'(?![A-Za-z0-9_])', eff_text), eff
        plan.append((sid, f'self(StatEffectIds.{eff})', DOMAIN.get(effs[eff]["attr"], "WireMisc"),
                     f'复用 {eff}（已用于同义语料）'))
        continue
    if p["kind"] == "new":
        e = made[p["eff"]]
        new_effs.append((p["eff"], e["attr"], e["qual"], e["op"], e["mode"], e["tags"]))
        plan.append((sid, f'self(StatEffectIds.{p["eff"]})', DOMAIN.get(e["attr"], "WireMisc"),
                     f'{p["base"]} 的 {"+".join(p["tags"])} 作用域变体'))
    else:
        eff = p["eff"]
        plan.append((sid, f'self(StatEffectIds.{eff})', DOMAIN.get(effs[eff]["attr"], "WireMisc"),
                     f'{p["base"]} 的 {"+".join(p["tags"])} 作用域变体（复用既有 EFF）'))

# ---- hybrid + scope: build the two scope EFFs, then bind as a StatGroup ----
for tag in ("ELEMENTAL", "LIGHTNING"):
    a = f"EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ATTACK_SPEED_{tag}"
    c = f"EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CAST_SPEED_{tag}"
    for nm, attr, qual in ((a, "ATTACK_SPEED", "Attributes"), (c, "CAST_SPEED", "AttributeRegister")):
        if not re.search(r'(?<![A-Za-z0-9_])' + re.escape(nm) + r'(?![A-Za-z0-9_])', eff_text):
            new_effs.append((nm, attr, qual, "ADD_MULTIPLIED_BASE", "GAIN", [tag]))
    plan.append((f"INC_ATTACK_AND_CAST_SPEED_WITH_{tag}_SKILLS",
                 f"groupSelf(StatEffectIds.{a}, StatEffectIds.{c})", "WireSpeed",
                 f"hybrid：攻速+施法速度共享 X%，并限定 {tag} 技能"))

print(f"new EFFs: {len(new_effs)}   wirings: {len(plan)}")

# ---------- ids ----------
if new_effs:
    p = os.path.join(GEN, "StatEffectIds.java"); t = rd(p)
    add = '    // ── 广义作用域变体（主语式，纯数据）──\n'
    for nm, *_ in new_effs:
        assert re.search(r'(?<![A-Za-z0-9_])' + re.escape(nm) + r'(?![A-Za-z0-9_])', t) is None, nm
        add += f'    public static final Identifier {nm} = id("{nm[len("EFF_"):].lower()}");\n'
    i = t.rstrip().rfind("}")
    wr(p, t[:i] + add + t[i:])
    print("ids +", len(new_effs))

# ---------- registrations ----------
if new_effs:
    p = os.path.join(GEN, "StatEffectGen.java"); t = rd(p)
    add = '        // ── 广义作用域变体（主语式，纯数据）──\n'
    for nm, attr, qual, op, mode, tg in new_effs:
        tags = ("TagExpr.ALWAYS" if not tg
                else "TagExpr.of(Set.of(" + ", ".join(f"StatTag.{x}" for x in sorted(tg)) + "))")
        add += (f'        m.put(StatEffectIds.{nm}, new ModifierStatEffect({qual}.{attr}, '
                f'Either.left(AttributeModifier.Operation.{op}), ModifierStatEffect.Mode.{mode}, {tags}));\n')
    i = t.rfind("        return m;")
    wr(p, t[:i] + add + t[i:])

# ---------- wirings ----------
TAIL = re.compile(r"\n    \}\n\}\s*$")
by = {}
for sid, expr, dom, note in plan:
    by.setdefault(dom, []).append(f'        // {note}\n        map.put(StatIds.{sid}, {expr});')
n = 0
for dom, lines in by.items():
    p = os.path.join(WIR, dom + ".java")
    t = rd(p)
    lines = [l for l in lines if l.split("\n")[1] not in t]
    if not lines:
        continue
    m = TAIL.search(t)
    assert m, dom
    wr(p, t[:m.start()] + "\n" + "\n".join(lines) + "\n" + t[m.start():])
    n += len(lines)
    print(f"  {dom}: +{len(lines)}")
print("wired =", n)

# ---------- gate ----------
if new_effs:
    p = os.path.join(ROOT, "scripts", "verify_gen.py"); t = rd(p)
    cur = int(re.search(r'effect count (\d+)', t).group(1))
    new = cur + len(new_effs)
    t = t.replace(f'("effect count {cur}"', f'("effect count {new}"')
    t = re.sub(rf'len\(effect_ids\)=={cur} and eff_n=={cur}', f'len(effect_ids)=={new} and eff_n=={new}', t)
    wr(p, t)
    print(f"verify_gen effect {cur} -> {new}")
