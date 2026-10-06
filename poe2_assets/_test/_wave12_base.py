#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""精确定位「真·可归一」属性：属性名 = [SCOPE]_[BASE] 或 [BASE]_[SCOPE]，
且 BASE 是另一个已注册属性（不发明基类），且 SCOPE 命中 StatTag 词表。"""
import re, os, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
AR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
TAG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java")

tag_src = open(TAG, encoding="utf-8").read()
tags = set(re.findall(r'public static final String [A-Z_]+ = "([a-z0-9_]+)"', tag_src))

ar_src = open(AR, encoding="utf-8").read().splitlines()
# 全部已注册属性
all_attrs = {}
for l in ar_src:
    m = re.search(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register(\w+)\("([^"]+)"', l)
    if m:
        all_attrs[m.group(1)] = m.group(3)

# Wave-12 块
start = next(i for i, l in enumerate(ar_src) if "批量落地（Wave-12" in l)
end = next(i for i, l in enumerate(ar_src) if "AUTO-GEN POE2 TALENT ATTRIBUTES END" in l)
wave12 = []
for l in ar_src[start:end]:
    m = re.search(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register', l)
    if m:
        wave12.append(m.group(1))

# SCOPE 词表（用于切分）
SCOPES = ["minion","melee","attack","spell","aura","curse","flask","channeling","warcry",
          "totem","companion","maim","poison","bleed","ignite","chill","shock","freeze",
          "withered","blind","dodge","parry","rage","frenzy","shapeshifted","crit","critical",
          "armour","armor","cold","fire","lightning","chaos","physical","elemental","ailment",
          "stun","knockback","leeching","guard","evasion","energy_shield","mana","health",
          "movement","slow","mark","impale","crossbow","bow","mace","spear","sword","trap",
          "mine","ballista","brand","beam","grenade","bolt","thorns","reservation","resistance",
          "puppet","aftershock","remnant","golem","wolf","bear","beast","storm","charge","seal",
          "offering","jewel","helmet","amulet","jewellery","gigantic","herald","incision",
          "presence","ice_crystal","jagged_ground","ignited_ground","chilled_ground",
          "shocked_ground","fully_broken_armor","empowered","arcane_surge","threshold",
          "spirit","focus","form","plant","mine","quiver","one_handed","two_handed"]

def find_base(name):
    """尝试把 name 拆成 SCOPE + BASE，BASE 必须在 all_attrs 中且 != name。"""
    nl = name.lower()
    # 也允许 BASE 在 name 中任意位置（去 scope 段后剩余即 base 候选）
    best = None
    for sc in SCOPES:
        if sc in nl:
            cand = nl.replace(sc, "", 1)
            cand = re.sub(r"_+", "_", cand).strip("_")
            # 找以此 cand 为子串的已注册属性
            for an, key in all_attrs.items():
                kl = key.lower()
                if cand and (cand == kl or cand in kl or kl in cand) and an != name:
                    # 确认 scope 命中 StatTag
                    if sc in tags:
                        return an, sc, cand
    return None

print("===== 真·可归一（BASE 为已注册属性 + SCOPE 命中 StatTag）=====")
found = []
for name in wave12:
    r = find_base(name)
    if r:
        base, sc, cand = r
        found.append((name, base, sc, cand))
        print(f"  {name:42s} -> BASE={base:38s} scope='{sc}' (tag exists)  residual='{cand}'")

print(f"\n[found] {len(found)} 真·可归一候选")
json.dump(found, open(os.path.join(ROOT,"poe2_assets/_test/wave12_base.json"),"w"), ensure_ascii=False, indent=1)
