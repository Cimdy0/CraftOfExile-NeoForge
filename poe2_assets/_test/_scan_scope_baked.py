#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""扫描 AttributeRegister 中「被引擎消费 且 属性名焊死作用域/条件」的候选。
纯 python，不调用 subprocess（避免 managed python 取不到 PATH / env -i 假成功）。"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
ATTR_FILE = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
SRC = os.path.join(ROOT, "src/main/java")

# 作用域/条件焊死关键词（属性名含这些 = 非归一）
SCOPE_KW = ["SLAM","MACE","SHAPESHIFT","MINION","TOTEM","TAKEN","RED_GREEN","FORM",
            "ARROW","ARROWS","BANNER","FISSURE","ORB","CURSE","TRAP","WARCRY","QUIVER",
            "GRENADE","META","COMBO","PARRY","THORNS","IMPAL","PRESENCE","BOW","CROSSBOW",
            "SPEAR","AXE","AXES","SWORD","SWORDS","ONE_HANDED","TWO_HANDED","BALLISTA",
            "HERALD","HAZARD","COMPANION","PUPPET","GIGANTIC","MARK_","_MARK","ELEMENTAL_AILMENT",
            "ELECTROCUT","CHILL","SHOCK","BLEED","POISON","IGNITE","WITHER","STUN_","_STUN",
            "FROZEN","RAGE_","_RAGE","DODGE","FLASK","FOCUS","HELMET","AMULET","STRENGTH",
            "DEXTERITY","FURY","MANA_","_MANA","LIFE_","_LIFE","ES_","_ES","ENERGY_SHIELD_",
            "ANIMAL","HUMAN","RECENT","RECENTLY","CHANNELL"]

def parse_attrs(path):
    names = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register", line)
            if m:
                names.append(m.group(1))
    return names

# 收集全部 java 文件内容（排除声明文件本身）
java_files = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if fn.endswith(".java"):
            java_files.append(os.path.join(dp, fn))

# 消费判定：AttributeRegister.X 出现在非 AttributeRegister.java / 非 StatGen.java 的文件
decl_file = os.path.abspath(ATTR_FILE)
exclude = {os.path.abspath(os.path.join(ROOT,"src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"))}

consumed = {}  # attr -> [files]
for name in set():
    pass

attrs = parse_attrs(ATTR_FILE)
for name in attrs:
    pat = re.compile(r"AttributeRegister\." + re.escape(name) + r"\b")
    hits = []
    for fp in java_files:
        if fp == decl_file or fp in exclude:
            continue
        try:
            with open(fp, encoding="utf-8") as fh:
                for i, line in enumerate(fh, 1):
                    if pat.search(line):
                        hits.append(os.path.relpath(fp, ROOT) + ":" + str(i))
        except Exception:
            pass
    if hits:
        consumed[name] = hits

# 非归一判定
def is_scope_baked(name):
    up = name.upper()
    for kw in SCOPE_KW:
        if kw in up:
            return kw
    return None

result = []
for name in attrs:
    kw = is_scope_baked(name)
    if kw and name in consumed:
        result.append({"attr": name, "scope_kw": kw, "consumed_by": consumed[name]})

result.sort(key=lambda x: x["attr"])
print(f"属性总数={len(attrs)}  被消费={len(consumed)}  消费且非归一={len(result)}")
print("="*80)
for r in result:
    print(f"\n### {r['attr']}  (scope={r['scope_kw']})")
    for h in r["consumed_by"][:6]:
        print(f"   {h}")

out = {"total": len(attrs), "consumed": len(consumed),
       "consumed_scope_baked": [{"attr":r["attr"],"scope_kw":r["scope_kw"],"consumed_by":r["consumed_by"]} for r in result]}
with open(os.path.join(ROOT,"poe2_assets/_test/scan_scope_baked.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)
print("\nwrote scan_scope_baked.json")
