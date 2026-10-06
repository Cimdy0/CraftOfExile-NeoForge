#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Split StatGen's 881 wirings into semantic partitions.

StatGen.java is 1228 lines of which ~740 are `map.put(StatIds.X, ...)` wirings with
no section structure (84% carry no group comment), so splitting must be driven by the
EFFECT's attribute domain rather than by existing comments.

Result layout:
    stat/wirings/WiringDsl.java    - shared factories (stat/self/onTarget)
    stat/wirings/WireDamage.java   - damage & crit domain
    stat/wirings/WireAilment.java  - ailment domain
    stat/wirings/WireDefense.java  - armour/evasion/resistance/block domain
    stat/wirings/WireResource.java - regen/energy/leech/resource-cap domain
    stat/wirings/WireSpeed.java    - cast/attack/movement speed domain
    stat/wirings/WireMisc.java     - everything else + non-ModifierStatEffect wirings

Safety: the script is a pure text transform with hard gates — the ordered list of
wiring statements must be preserved exactly (order matters for map insertion),
total count must match, and StatGen must end up with zero `map.put(StatIds.` lines.
"""
import os, re, sys

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
PKG = "com.cimdy.coe.data.gen.stat"
GEN = os.path.join(ROOT, "src", "main", "java", PKG.replace(".", "/"))
WIR = os.path.join(GEN, "wirings")
SG = os.path.join(GEN, "StatGen.java")

# ---------- attribute -> partition ----------
DOMAINS = {
    "WireDamage": {
        "DAMAGE", "ATTACK_DAMAGE", "CRIT_CHANCE", "CRIT_DAMAGE", "TAKEN_DAMAGE",
        "TAKEN_CRIT_DAMAGE", "TAKEN_CRIT_CHANCE", "DAMAGE_OVER_TIME_MULTIPLIER",
        "DAMAGE_TAKEN_BYPASSES_ENERGY",
    },
    "WireAilment": {
        "AILMENT_MAGNITUDE", "AILMENT_DURATION", "AILMENT_BUILDUP", "AILMENT_CHANCE",
        "AILMENT_THRESHOLD", "AILMENT_THRESHOLD_ADD", "AILMENT_THRESHOLD_ADAPTATION",
        "AILMENT_DAMAGE_RATE", "AILMENT_DAMAGE_CONTRIBUTION",
    },
    "WireDefense": {
        "ARMOR", "EVASION", "RESISTANCES", "MAX_RESISTANCES", "RESISTANCE_PENETRATION",
        "BLOCK_CHANCE", "MAX_BLOCK", "DEFLECTION_RATING", "ARMOR_BREAK",
        "AVOID_AILMENT_CHANCE", "DODGE_CHANCE", "STUN_RECOVERY",
    },
    "WireSpeed": {"CAST_SPEED", "ATTACK_SPEED", "MOVEMENT_SPEED", "CROSS_BOW_RELOAD_TIME",
                  "COOLDOWN_RECOVERY_RATE"},
    "WireResource": {
        "MAX_REGENERATION", "REGENERATION_RATE", "RECOVERY_RATE", "ENERGY_RECHARGE",
        "BASE_ENERGY_SHIELD_RECHARGE", "CURRENT_ENERGY_SHIELD_RECHARGE",
        "MAX_ENERGY_SHIELD", "MAX_MANA", "MAX_HEALTH", "MAX_RAGE", "MAX_SPIRIT",
        "MAX_VOLATILITY", "META_ENERGY_MAX", "LEECH", "LEECH_RATE", "AMOUNT_LEECHED",
        "LEECHES_RESPONSE", "FLASK_RECOVERY_RATE", "FLASK_CHARGES_GAINED",
        "FLASK_NO_CONSUME_CHANCE", "ARCHON_RECOVERY_RATE", "GUARD_RECOVERY",
    },
}
ORDER = ["WireDamage", "WireAilment", "WireDefense", "WireSpeed", "WireResource", "WireMisc"]


def domain_of(attr):
    for d, attrs in DOMAINS.items():
        if attr in attrs:
            return d
    return "WireMisc"


# ---------- parse EFF -> attribute ----------
eg = open(os.path.join(GEN, "StatEffectGen.java"), encoding="utf-8").read()
eff_attr = {}
for m in re.finditer(r'm\.put\(StatEffectIds\.(\w+),\s*new ModifierStatEffect\((.*?)\)\);', eg, re.S):
    am = re.search(r"(?:AttributeRegister|Attributes)\.(\w+)", m.group(2))
    if am:
        eff_attr[m.group(1)] = am.group(1)

src = open(SG, encoding="utf-8").read()
lines = src.split("\n")

# ---------- collect wiring statements WITH their leading comment block ----------
WIRING_RE = re.compile(r"^\s*map\.put\(StatIds\.(\w+),")
items = []           # (statid, code_line, comment_idxs, line_idx, domain)
pending = []         # list of (idx, text)
i = 0
while i < len(lines):
    ln = lines[i]
    s = ln.strip()
    if WIRING_RE.match(ln):
        m = re.search(r"StatEffectIds\.(\w+)", ln)
        attr = eff_attr.get(m.group(1)) if m else None
        items.append((WIRING_RE.match(ln).group(1), ln,
                      [c[0] for c in pending], i,
                      domain_of(attr) if attr else "WireMisc"))
        pending = []
    elif s.startswith("//"):
        pending.append((i, ln))
    elif s == "" and pending:
        pending.append((i, ln))
    else:
        pending = []
    i += 1

total_wirings = sum(1 for ln in lines if WIRING_RE.match(ln))
if len(items) != total_wirings:
    sys.exit(f"ABORT: collected {len(items)} wirings but file has {total_wirings}")

# ---------- emit partition classes ----------
HEADER = """package {pkg}.wirings;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.Map;

import static {pkg}.wirings.WiringDsl.*;

/** {note} */
public final class {cls} {{

    private {cls}() {{
    }}

    public static void build(Map<Identifier, StatEntry> map) {{
{body}
    }}
}}
"""

DSL = '''package com.cimdy.coe.data.gen.stat.wirings;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.codec.support.math.context.ContextNodeRead;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead;
import com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead;
import com.cimdy.coe.data.codec.support.stat.entry.Stat;
import com.cimdy.coe.data.gen.stat.StatConditionIds;
import com.cimdy.coe.data.gen.stat.StatEffectIds;
import com.cimdy.coe.data.gen.stat.StatTargetIds;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 接线 DSL：把 StatGen 里最高频的组装形状收成工厂方法。
 * 分布实测：881 条接线中 TGT_SELF_ONLY 占 740（84%）、TGT_COND_ON_TARGET 占 69，
 * 故 self(...) / onTarget(...) 覆盖绝大多数写法，省去重复的 StatTargetIds 限定名。
 */
public final class WiringDsl {

    private WiringDsl() {
    }

    /** 纯路径组装（无条件）。 */
    public static Stat stat(Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(), targetsRef, effectRef);
    }

    /** 纯路径组装（单条件）。 */
    public static Stat stat(Identifier condRef, Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(condRef), targetsRef, effectRef);
    }

    /** 纯路径组装（多条件）。 */
    public static Stat stat(List<Identifier> condRefs, Identifier targetsRef, Identifier effectRef) {
        List<ContextNode> conds = condRefs.isEmpty() ? List.of()
                : condRefs.stream().<ContextNode>map(ContextNodeRead::new).toList();
        return new Stat(ExpressionCodec.VALUE, conds, new StatTargetsRead(targetsRef), new StatEffectRead(effectRef));
    }

    /** 自身生效（TGT_SELF_ONLY）。 */
    public static Stat self(Identifier effectRef) {
        return stat(StatTargetIds.TGT_SELF_ONLY, effectRef);
    }

    /** 自身生效 + 单条件。 */
    public static Stat self(Identifier condRef, Identifier effectRef) {
        return stat(condRef, StatTargetIds.TGT_SELF_ONLY, effectRef);
    }

    /** 对目标生效（TGT_COND_ON_TARGET）：条件在目标侧求值。 */
    public static Stat onTarget(Identifier condRef, Identifier effectRef) {
        return stat(condRef, StatTargetIds.TGT_COND_ON_TARGET, effectRef);
    }
}
'''

os.makedirs(WIR, exist_ok=True)
open(os.path.join(WIR, "WiringDsl.java"), "w", encoding="utf-8").write(DSL)

by_domain = {d: [] for d in ORDER}
for sid, code, cidx, lidx, dom in items:
    by_domain[dom].append((sid, code, cidx, lidx))

counts = {}
for d in ORDER:
    chunk = by_domain[d]
    if not chunk:
        continue
    body = []
    for sid, code, cidx, lidx in chunk:
        for ci in cidx:
            body.append("        " + lines[ci].strip())
        body.append("        " + code.strip())
    open(os.path.join(WIR, d + ".java"), "w", encoding="utf-8").write(
        HEADER.format(pkg=PKG, cls=d, note=f"{d} 域接线（{len(chunk)} 条）。", body="\n".join(body)))
    counts[d] = len(chunk)

print("partitions:")
for d in ORDER:
    if d in counts:
        print(f"  {d:16} {counts[d]}")
print(f"  total           {sum(counts.values())}  (expected {total_wirings})")

# ---------- rewrite StatGen: drop the wirings, delegate to partitions ----------
del_idx = set()
for _sid, _code, cidx, lidx, _dom in items:
    del_idx.update(cidx)
    del_idx.add(lidx)

new_lines = []
calls_inserted = False
for idx, ln in enumerate(lines):
    if idx in del_idx:
        continue
    if (not calls_inserted) and re.match(r"^\s*return map;\s*$", ln):
        for d in ORDER:
            if d in counts:
                new_lines.append(f"        {d}.build(map);")
        new_lines.append("")
        calls_inserted = True
    new_lines.append(ln)

if not calls_inserted:
    sys.exit("ABORT: could not find `return map;` to insert partition calls")

out = "\n".join(new_lines)

if "map.put(StatIds." in out:
    sys.exit(f"ABORT: {out.count('map.put(StatIds.')} wirings remain in StatGen")

# add static import for the DSL + partition references
anchor = "import com.cimdy.coe.data.codec.support.stat.StatIds;"
if anchor not in out:
    sys.exit("ABORT: StatIds import anchor not found")
out = out.replace(
    anchor,
    anchor
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireAilment;"
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireDamage;"
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireDefense;"
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireMisc;"
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireResource;"
    + "\nimport com.cimdy.coe.data.gen.stat.wirings.WireSpeed;",
    1,
)
out = out.replace(
    "import java.util.LinkedHashMap;",
    "import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;\n\nimport java.util.LinkedHashMap;",
    1,
)

open(SG, "w", encoding="utf-8").write(out)
print(f"\nStatGen rewritten: {src.count(chr(10))} -> {out.count(chr(10))} lines")
