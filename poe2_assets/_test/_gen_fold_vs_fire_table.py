#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""基于 _scan_fold_vs_fire.json 生成 FOLD-vs-FIRE 架构轴裁定表。
判定准则：
  FOLD-KEEP  : 连续倍率/资源上限/伤害减免，计算期按作用域读取（ModifierStatEffect 折叠）。
  FOLD-KEEP* : chance 类（q-1: 几率=Stat值），fold 聚合触发几率。
  FIRE-候选  : 事件驱动机制（命中/受击/击杀触发一次性动作或倍率），倍率应走 FIRE 效果自带数值。
  边界       : 伤害期条件减免（与 Dodge/Guard 同构，FOLD 自然；但 q-1 倾向 Effect 数值）→ 待裁定。
  待定       : 孤儿无消费端，需裁定 保留/转FIRE/删。
"""
import json, os

HERE = os.path.dirname(os.path.abspath(__file__))
JSON = os.path.join(HERE, "..", "..", "src", "main", "java", "com", "cimdy", "coe", "data", "_scan_fold_vs_fire.json")
OUT = os.path.join(HERE, "..", "ATTRIBUTE_FOLD_VS_FIRE_AUDIT.md")

with open(JSON, encoding="utf-8") as f:
    rows = json.load(f)

# 显式策划：事件驱动一次性动作/倍率（值应走 FIRE 效果自带数值，而非 FOLD 属性）的候选集。
FIRE_SET = {
    "THORNS", "RECOUP", "MANA_TAKE_OF_DAMAGE", "CULLING_STRIKE_THRESHOLD",
    "CORPSE_RETAIN_CHANCE", "RECENTLY_CONSUMED_DAMAGE", "BANNER_LINGER",
    "PUPPET_MASTER_DURATION", "PUPPET_MASTER_EFFECT", "FOUND_ITEM_NUMBER", "FOUND_ITEM_RARITY",
}
# 边界：chance 触发门 + magnitude 效果值（q-1 同构混合语义），magnitude 应走 FIRE 效果自带数值。
BOUNDARY = {"DEFEND_WITH_ARMOUR", "KNOCKBACK"}

def classify(r):
    ops = set(r["ops"].split(","))
    name = r["name"]
    if name in BOUNDARY:
        return ("边界", "chance 触发门 + magnitude 效果值（q-1 同构混合语义）；magnitude 应走 FIRE 效果自带数值")
    if "chance" in ops:
        return ("FOLD-KEEP*", "chance=Stat值(q-1)：fold 聚合触发几率")
    if name in FIRE_SET:
        orphan = " (孤儿无消费端)" if r["n_consumers"] == 0 else ""
        return ("FIRE-候选", "事件驱动一次性动作/倍率，值应走 FIRE 效果自带数值" + orphan)
    if r["n_consumers"] == 0:
        return ("待定(孤儿)", "无消费端；需裁定 保留/转FIRE/删")
    return ("FOLD-KEEP", "连续倍率/上限/伤害减免，计算期按作用域读取")

labeled = [(r, classify(r)) for r in rows]
order = {"边界": 0, "FIRE-候选": 1, "FIRE-候选?": 2, "待定": 3, "FOLD-KEEP*": 4, "FOLD-KEEP": 5}
labeled.sort(key=lambda x: (order.get(x[1][0], 9), x[0]["name"]))

lines = []
lines.append("# 属性 FOLD-vs-FIRE 架构轴裁定表\n")
lines.append("> 架构事实：`StatEffect` 仅描述语义，数值由 `StatEntry.amount` 持有；仅 `ModifierStatEffect`(FOLD) 强制依赖属性，\n")
lines.append("> 其余 FIRE/DELIVERY/IMMUNE 等亚型经 `entry.amount` 自带数值、无需属性。命中/受击事件已建 `StatEngine.fire` 通道。\n")
lines.append("> 判定准则：连续倍率/上限/伤害减免→FOLD-KEEP；chance→FOLD-KEEP*(Stat值)；事件驱动一次性动作/倍率→FIRE-候选；伤害期条件减免→边界。\n")
lines.append("")
lines.append(f"属性总数={len(rows)}；FIRE-候选={sum(1 for _,l in labeled if l[0].startswith('FIRE'))}；"
             f"边界={sum(1 for _,l in labeled if l[0]=='边界')}；待定(孤儿)={sum(1 for _,l in labeled if l[0].startswith('待定'))}。\n")

lines.append("| # | 属性 | key | base | StatGen op/tags | 消费端 | 裁定 | 理由 |")
lines.append("|---|---|---|---|---|---|---|---|")
for i, (r, (lab, reason)) in enumerate(labeled, 1):
    op = r["ops"] + (f" / {r['tags']}" if r["tags"] else "")
    cons = f"{r['n_consumers']}处" + (f"({r['consumers']})" if r["consumers"] else "")
    lines.append(f"| {i} | {r['name']} | {r['key']} | {r['base']} | {op} | {cons} | **{lab}** | {reason} |")

with open(OUT, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print("written:", OUT)
print("rows:", len(rows))
from collections import Counter
c = Counter(l[0] for _, l in labeled)
print("written:", OUT)
print("rows:", len(rows))
for k, v in c.most_common():
    print(f"  {k}: {v}")
