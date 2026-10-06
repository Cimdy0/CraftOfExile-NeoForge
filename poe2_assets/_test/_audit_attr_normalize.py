#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
属性层根因审计：
  1) 引擎消费覆盖矩阵 —— 每个 AttributeRegister 属性是否被引擎(codec/helper/executor)真实读取
     (skill §10 判定法: AttributeRegister.<FIELD> 精确匹配, 排除 AttributeRegister/StatGen 自身)
  2) 非归一判定 —— 属性名/语义是否焊死作用域或条件(scope/condition-baked)
输出: 占位未消费属性清单 + 非归一属性清单 + 两维度交叉
"""
import re, os, subprocess, json

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # _test -> poe2_assets -> CraftOfExile-NeoForge
SRC = os.path.join(ROOT, "src", "main", "java")
ATTR = os.path.join(SRC, "com", "cimdy", "coe", "data", "AttributeRegister.java")

# 1) parse attributes
attrs = {}  # name -> {id, base, line}
pat = re.compile(r'public static Holder<Attribute>\s+([A-Za-z0-9_]+)\s*=\s*register(Percentage|Double|Flat)?\(\s*"([^"]+)"\s*,\s*([0-9.]+)')
with open(ATTR, encoding="utf-8") as f:
    for i, line in enumerate(f, 1):
        m = pat.search(line)
        if m:
            attrs[m.group(1)] = {"id": m.group(3), "base": float(m.group(4)), "line": i}

names = list(attrs.keys())

# 2) engine consumption: pure-python scan for AttributeRegister.<NAME> across src
import pathlib
src_root = pathlib.Path(SRC)
files = [str(p) for p in src_root.rglob("*.java")]
consumed = {}
for n in names:
    pat_n = re.compile(r"AttributeRegister\." + re.escape(n) + r"\b")
    cnt = 0
    for fp in files:
        if fp.endswith("AttributeRegister.java") or fp.endswith("StatGen.java"):
            continue
        try:
            with open(fp, encoding="utf-8", errors="ignore") as fh:
                for line in fh:
                    if pat_n.search(line):
                        cnt += 1
                        break
        except Exception:
            pass
    consumed[n] = cnt

# 3) non-normalization classification by name/semantic keyword
SCOPE_KW = ["SLAM", "MACE", "SHAPESHIFT", "FORM", "MINION", "TOTEM", "COMPANION",
            "WARCRY", "CURSE", "MARK", "SPELL", "ATTACK", "BANNER", "AURA", "TRAP",
            "GRENADE", "PLANT", "FISSURE", "ORB", "DARKNESS", "ARCHON", "PUPPET",
            "INFUSION", "VOLATILITY", "REMNANT", "CHARM", "FLASK", "QUIVER", "AMULET"]
COND_KW = ["TAKEN", "RED_GREEN", "RECENT", "SHAPESHIFTED", "POISONED", "BLEEDING",
           "WITHERED", "HUMAN", "OVERGROW", "REFLECTED", "ELECTROCUTED", "CRITICALLY",
           "UNAFFECTED", "DEBILITATE", "IMPALES", "LUCKY", "BLINDED", "CANNOT",
           "IGNORE", "ADDITIONAL", "EQUAL", "CAUSE", "INFLICT", "CREATE", "REMOVE",
           "NO_", "WHICH_"]

def is_nonnormal(n):
    scope_hit = [k for k in SCOPE_KW if k in n]
    cond_hit = [k for k in COND_KW if k in n]
    return scope_hit, cond_hit

nonnorm = {}
for n in names:
    s, c = is_nonnormal(n)
    if s or c:
        nonnorm[n] = (s, c)
placeholder = {n: consumed[n] for n in names if consumed[n] == 0}
consumed_attrs = {n: consumed[n] for n in names if consumed[n] > 0}

# cross: non-normalized AND placeholder
cross = {n: nonnorm[n] for n in nonnorm if n in placeholder}

print(f"属性总数: {len(names)}")
print(f"引擎已消费: {len(consumed_attrs)}")
print(f"占位未消费(无引擎读取): {len(placeholder)}")
print(f"非归一(焊死作用域/条件): {len(nonnorm)}")
print(f"非归一 且 占位未消费: {len(cross)}")
print()
print("=== 占位未消费属性 ({}) ===".format(len(placeholder)))
for n in sorted(placeholder, key=lambda x: attrs[x]["line"]):
    flag = " [非归一]" if n in nonnorm else ""
    print(f"  L{attrs[n]['line']:<4} {n:<45} id={attrs[n]['id']:<40} base={attrs[n]['base']}{flag}")
print()
print("=== 非归一属性 ({}) ===".format(len(nonnorm)))
for n in sorted(nonnorm, key=lambda x: attrs[x]["line"]):
    s, c = nonnorm[n]
    cons = "consumed" if n in consumed_attrs else "PLACEHOLDER"
    print(f"  L{attrs[n]['line']:<4} {n:<45} scope={s} cond={c} [{cons}]")
print()
print("=== 严重: 非归一 且 占位未消费 ({}) ===".format(len(cross)))
for n in sorted(cross, key=lambda x: attrs[x]["line"]):
    s, c = cross[n]
    print(f"  L{attrs[n]['line']:<4} {n:<45} scope={s} cond={c}")

out = {
    "total": len(names),
    "consumed": len(consumed_attrs),
    "placeholder": sorted(placeholder.keys()),
    "nonnormal": {n: {"scope": nonnorm[n][0], "cond": nonnorm[n][1]} for n in nonnorm},
    "cross": sorted(cross.keys()),
}
with open(os.path.join(os.path.dirname(__file__), "attr_normalize_audit.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, indent=2, ensure_ascii=False)
print("\nwritten: poe2_assets/_test/attr_normalize_audit.json")
