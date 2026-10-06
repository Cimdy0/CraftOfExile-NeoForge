# -*- coding: utf-8 -*-
"""分析：术语替换改为大小写不敏感（归一到 MC 规范写法）后的影响面。

对比两套方案：
  A（当前已落盘）：仅替换首字母大写形式（Maximum/Life/Armour/...）
  B（大小写不敏感）：maximum|Maximum -> Max，life|Life -> Health，...

输出改名条目数、因归一而合并（碰撞）的条目对。
"""
import re
from collections import defaultdict

TEMPLATES = "poe2_assets/stat_templates.txt"

REPL = [
    ("Accuracy Rating", "Accuracy"),
    ("Evasion Rating", "Evasion"),
    ("Armour", "Armor"),
    ("Life", "Health"),
    ("Maximum", "Max"),
]


def mc_a(t):
    for a, b in REPL:
        t = t.replace(a, b)
    return t


def mc_b(t):
    for a, b in REPL:
        t = re.sub(re.escape(a), b, t, flags=re.IGNORECASE)
    return t


def to_name(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w.upper() for w in s.split() if w)


raw = [l.rstrip("\n") for l in open(TEMPLATES, encoding="utf-8") if l.strip()]

changed = []
for t in raw:
    a, b = mc_a(t), mc_b(t)
    if to_name(a) != to_name(b):
        changed.append((t, to_name(a), to_name(b)))

print(f"templates: {len(raw)}")
print(f"方案B 会改名的条目: {len(changed)}")

# 方案B 下的碰撞（即大小写差异造成的重复语义条目）
buckets = defaultdict(list)
for t in raw:
    buckets[to_name(mc_b(t))].append(t)
dupB = {k: v for k, v in buckets.items() if len(v) > 1}

bucketsA = defaultdict(list)
for t in raw:
    bucketsA[to_name(mc_a(t))].append(t)
dupA = {k: v for k, v in bucketsA.items() if len(v) > 1}

print(f"方案A 碰撞组: {len(dupA)}   方案B 碰撞组: {len(dupB)}")
print("\n=== 方案A 现有碰撞组 ===")
for k, v in dupA.items():
    print(f"  [{k}]")
    for x in v:
        print(f"      {x}")

print("\n=== 方案B 新增碰撞组（大小写导致的同义重复）===")
for k, v in dupB.items():
    if k not in dupA:
        print(f"  [{k}]")
        for x in v:
            print(f"      {x}")

print("\n=== 方案B 改名示例（前 20）===")
for t, a, b in changed[:20]:
    print(f"  {t}")
    print(f"     A: {a}")
    print(f"     B: {b}")
