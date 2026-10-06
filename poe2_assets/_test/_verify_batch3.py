#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Batch-3 验收：被改动 StatIds 的生成物是否落点正确 + 旧属性零残留。"""
import json, os, glob, sys

GEN = "E:/McModding/Modding/CraftOfExile-NeoForge/src/generated/resources/data/coe/coe/stat_entry"
files = {os.path.basename(f)[:-5]: f for f in glob.glob(os.path.join(GEN, "*.json"))}

def load(sid):
    f = files.get(sid)
    if not f:
        return None
    return json.load(open(f, encoding="utf-8"))

checks = [
    # sid, 期望 attribute, 期望包含的 tag
    ("attacks_have_to_critical_hit_chance", "coe:crit_chance", ["attack"]),
    ("your_minions_are_gigantic", None, ["minion"]),
    ("all_bonuses_from_equipped_amulet_apply_to_your_minions_instead_of_you", None, ["minion", "jewellery"]),
    ("max_stacks_of_puppet_master", None, ["companion"]),
]

ok = True
for sid, exp_attr, exp_tags in checks:
    j = load(sid)
    if j is None:
        print(f"[FAIL] 缺少生成物: {sid}.json")
        ok = False
        continue
    attr = j.get("attribute", "")
    tags = j.get("tags", [])
    errs = []
    if exp_attr and attr != exp_attr:
        errs.append(f"attribute={attr} 期望 {exp_attr}")
    for t in exp_tags:
        if t not in tags:
            errs.append(f"缺 tag {t} (现有 {tags})")
    if errs:
        print(f"[FAIL] {sid}: {'; '.join(errs)}")
        ok = False
    else:
        print(f"[ OK ] {sid}: attr={attr} tags={tags}")

# 旧属性零残留：任何生成物都不应再指向 coe:attacks_critical
stale = []
for sid, f in files.items():
    j = json.load(open(f, encoding="utf-8"))
    if j.get("attribute") == "coe:attacks_critical":
        stale.append(sid)
if stale:
    print(f"[FAIL] 旧属性 coe:attacks_critical 残留于: {stale}")
    ok = False
else:
    print("[ OK ] 旧属性 coe:attacks_critical 零残留")

print("\nRESULT:", "PASS" if ok else "FAIL")
sys.exit(0 if ok else 1)
