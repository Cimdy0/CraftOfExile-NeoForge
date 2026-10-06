# -*- coding: utf-8 -*-
"""分析：把 CamelCase 粘连标签词拆成空格分词（HitDamage -> Hit Damage）后的影响面。

粘连来源：POE 语料 [TagName|DisplayText]，第 3 步「取词丰侧」在 DisplayText 更短时
保留了 TagName（如 [HitDamage|Hit] -> HitDamage），语意最全但拼写粘连，
经 to_name() 会产出不可读常量 HITDAMAGE（而非 HIT_DAMAGE）。

拆分后与语料中已空格化的同义写法（[ElementalDamage|Elemental Damage] -> Elemental Damage）
会自然合并，暴露第 3 步漏掉的重复条目。
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

GLUE = re.compile(r"\b[A-Z][a-z]+(?:[A-Z][a-z]+)+\b")


def split_glue(t):
    return GLUE.sub(lambda m: re.sub(r"(?<=[a-z])(?=[A-Z])", " ", m.group(0)), t)


def mc(t, ci=False):
    for a, b in REPL:
        t = re.sub(re.escape(a), b, t, flags=re.IGNORECASE) if ci else t.replace(a, b)
    return t


def to_name(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w.upper() for w in s.split() if w)


raw = [l.rstrip("\n") for l in open(TEMPLATES, encoding="utf-8") if l.strip()]

# 现状（方案A：仅大写替换，不拆粘连）
cur = defaultdict(list)
for t in raw:
    cur[to_name(mc(t))].append(t)

# 方案C：大小写不敏感替换 + 拆粘连
new = defaultdict(list)
for t in raw:
    new[to_name(mc(split_glue(t), ci=True))].append(t)

print(f"模板总数            : {len(raw)}")
print(f"现状唯一条目 (A)     : {len(cur)}")
print(f"拆分+大小写归一 (C)  : {len(new)}")
print(f"因归一而合并的条目数 : {len(cur) - len(new)}")

merged = {k: v for k, v in new.items() if len(v) > 1}
print(f"\n=== 合并组（{len(merged)} 组）===")
for k, v in sorted(merged.items()):
    print(f"  [{k}]")
    for x in v:
        print(f"      {x}")

print("\n=== 粘连拆分改名示例（前 15）===")
n = 0
for t in raw:
    a, c = to_name(mc(t)), to_name(mc(split_glue(t), ci=True))
    if a != c and GLUE.search(t):
        print(f"  {t}")
        print(f"     现: {a}")
        print(f"     新: {c}")
        n += 1
    if n >= 15:
        break
