#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Wave-12 广域归一分析器。
方法（用户指定）：
  1) 先在作用域中检查是否存在对应的词汇（StatTag 词表）；
  2) 再根据引用的 StatIds 的对应语意对其进行归一（条件/复合属性 → 并入基础属性 + 条件/作用域标签）。
输出：每个 Wave-12 属性的 StatGen 接线（StatIds + 当前标签）、作用域/条件关键词命中 StatTag 情况。
"""
import re, json, os

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
AR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
SG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
TAG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java")

# ---- 1. 提取 StatTag 词表 ----
tag_src = open(TAG, encoding="utf-8").read()
tags = set(re.findall(r'public static final String [A-Z_]+ = "([a-z0-9_]+)"', tag_src))
# 也抓自由串风格（双引号内小写_）
print(f"[StatTag] 词表大小 = {len(tags)}")

# ---- 2. 提取 Wave-12 块属性（264 行注释之后，到 AUTO-GEN END 之前） ----
ar_src = open(AR, encoding="utf-8").read().splitlines()
start = None
for i, l in enumerate(ar_src):
    if "批量落地（Wave-12" in l:
        start = i
        break
end = None
for i, l in enumerate(ar_src):
    if "AUTO-GEN POE2 TALENT ATTRIBUTES END" in l:
        end = i
        break
block = ar_src[start:end]
attrs = []
for l in block:
    m = re.search(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register(\w+)\("([^"]+)"', l)
    if m:
        attrs.append((m.group(1), m.group(2), m.group(3)))
print(f"[Wave-12] 块属性数 = {len(attrs)} (行 {start+1}..{end+1})")

# ---- 3. 解析 StatGen 接线：每行 map.put/inc/chance/add(StatIds.X, AttributeRegister.Y, tags...) ----
sg_src = open(SG, encoding="utf-8").read().splitlines()
# 收集 AttributeRegister 字段名集合，便于判断 writer 是否指向 Wave-12 属性
ar_names = {a[0] for a in attrs}

wiring = {}  # attr_name -> list of (method, statids, tags[])
# 逐行扫描，捕获 writer 调用
line_re = re.compile(
    r'\b(inc|chance|add|valueMod|incMod|regenMod|entityMod|esRechargeMod|map\.put)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*([^;]*?)\)\s*;'
)
for ln in sg_src:
    m = line_re.search(ln)
    if not m:
        continue
    method, statids, rest = m.group(1), m.group(2), m.group(3)
    # 在 rest 中找 AttributeRegister.X 与 StatTag.Y / 自由串
    ar_refs = re.findall(r'AttributeRegister\.(\w+)', rest)
    tag_refs = re.findall(r'StatTag\.(\w+)', rest)
    free = re.findall(r'"([a-z0-9_]+)"', rest)
    # 目标属性：取第一个 AttributeRegister 引用（writer 主体）；若是 ImmuneStatEffect 之类则无 AR 引用
    target = ar_refs[0] if ar_refs else None
    wiring.setdefault(target, []).append({
        "method": method, "statids": statids,
        "tags_const": tag_refs, "tags_free": free
    })

# ---- 4. 对每个 Wave-12 属性，归类作用域/条件关键词命中 StatTag ----
# 关键词词典：属性名 segment → StatTag
SEG2TAG = {
    "minion": "minion", "minions": "minion",
    "melee": "melee", "ranged": None, "projectile": "projectile",
    "attack": "attack", "spell": "spell", "aura": "aura",
    "curse": "curse", "flask": "flask", "channel": "channeling", "channeling": "channeling",
    "warcry": "warcry", "totem": "totem", "companion": "companion",
    "maim": "maim", "poison": "poison", "bleed": "bleed", "ignite": "ignite", "chill": "chill",
    "shock": "shock", "freeze": "freeze", "wither": "withered", "withered": "withered",
    "blind": "blind", "dodge": "dodge", "parry": "parry", "block": None,
    "rage": "rage", "frenzy": "frenzy", "shapeshift": "shapeshifted", "shapeshifted": "shapeshifted",
    "crit": "crit", "critical": "crit", "armour": "armor", "armor": "armor",
    "cold": "cold", "fire": "fire", "lightning": "lightning", "chaos": "chaos", "physical": "physical",
    "elemental": "elemental", "ailment": "ailment", "stun": "stun", "knockback": "knockback",
    "leech": "leeching", "leeching": "leeching", "guard": "guard", "evasion": "evasion",
    "energy": "energy", "energy_shield": "energy_shield", "mana": "mana", "health": "health",
    "movement": "movement", "slow": "slow", "mark": "mark", "impale": "impale",
    "crossbow": "crossbow", "bow": "bow", "mace": "mace", "spear": "spear", "sword": "sword",
    "trap": "trap", "mine": "mine", "ballista": "ballista", "brand": "brand", "beam": "beam",
    "grenade": "grenade", "bolt": "bolt", "thorn": "thorns" if False else "thorns",
    "reservation": "reservation", "resistance": "resistance", "puppet": "companion",
    "aftershock": None, "remnant": "remnant", "golem": "golem", "wolf": "wolf", "bear": "bear",
    "beast": "beast", "storm": "storm", "charge": "charge", "seal": "seal", "offering": "offering",
    "jewel": "jewel", "helmet": "helmet", "amulet": "jewellery", "jewellery": "jewellery",
}
# 修正：thorns 不在词表？检查
if "thorns" not in tags:
    SEG2TAG.pop("thorns", None)

def seg_hits(name_lower):
    hits = {}
    for seg, tag in SEG2TAG.items():
        if seg in name_lower:
            hits[seg] = tag
    return hits

rows = []
for name, rtype, key in attrs:
    w = wiring.get(name, [])
    # 作用域/条件关键词命中
    hits = seg_hits(key)
    # 是否已在 StatGen 带标签
    already_tagged = any((r["tags_const"] or r["tags_free"]) for r in w)
    rows.append({
        "attr": name, "register": rtype, "key": key,
        "wirings": w,
        "scope_hits": hits,
        "already_tagged": already_tagged,
        "n_wirings": len(w),
    })

# ---- 5. 输出分类 ----
print("\n===== A. 已在 StatGen 带作用域/条件标签（已归一 or 条件显式化）=====")
for r in rows:
    if r["already_tagged"]:
        tags_now = []
        for w in r["wirings"]:
            tags_now += [f"StatTag.{t}" for t in w["tags_const"]] + [f'"{t}"' for t in w["tags_free"]]
        print(f"  {r['attr']:42s} <- {r['wirings'][0]['statids']:55s} tags={tags_now}")

print("\n===== B. 作用域/条件关键词命中 StatTag，但 StatGen 尚无标签（候选归一）=====")
cand = [r for r in rows if r["scope_hits"] and not r["already_tagged"]]
for r in cand:
    print(f"  {r['attr']:42s} key={r['key']:38s} hits={list(r['scope_hits'].items())} nW={r['n_wirings']}")

print("\n===== C. 无作用域/条件关键词命中（独立语义属性，可能不宜折叠）=====")
indep = [r for r in rows if not r["scope_hits"]]
for r in indep:
    wstr = r['wirings'][0]['statids'] if r['wirings'] else "(无接线)"
    print(f"  {r['attr']:42s} key={r['key']:38s} statids={wstr}")

# 落盘
out = {
    "tags": sorted(tags),
    "n_attrs": len(attrs),
    "already_tagged": [r["attr"] for r in rows if r["already_tagged"]],
    "candidates": [{"attr": r["attr"], "key": r["key"], "hits": r["scope_hits"],
                   "wirings": r["wirings"]} for r in cand],
    "independent": [{"attr": r["attr"], "key": r["key"],
                    "statids": (r["wirings"][0]["statids"] if r["wirings"] else None)}
                   for r in indep],
}
with open(os.path.join(ROOT, "poe2_assets/_test/wave12_broad.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
print(f"\n[done] candidates={len(cand)} independent={len(indep)} already_tagged={len([r for r in rows if r['already_tagged']])}")
