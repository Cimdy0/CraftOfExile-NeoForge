#!/usr/bin/env python3
"""StatTag 覆盖审计：被 EFF 消费的 StatTag vs 引擎实际产出的 tag。

判定逻辑：
- 消费侧：StatEffectGen 中 TagExpr.of(Set.of(StatTag.X, "literal", ...)) 引用的所有 tag。
- 产出侧：全局 `tags.add(StatTag.X)` / `tags.add("literal")` / `tags.addAll(...)`。
- 结论：消费侧出现但产出侧从未出现 = 潜在死接线（引擎永不打该标签 → 对应 EFF 永不触发）。
"""
import re, glob, os

ROOT = "src/main/java/com/cimdy/coe"

# ---- 1. 所有 StatTag 常量（name -> string value）----
tag_const = {}
for f in glob.glob(f"{ROOT}/data/codec/support/stat/StatTag.java"):
    for m in re.finditer(r'public static final String (\w+) = "([^"]+)";', open(f, encoding="utf-8").read()):
        tag_const[m.group(1)] = m.group(2)
const2name = {v: k for k, v in tag_const.items()}
print(f"[StatTag] 常量总数 = {len(tag_const)}")

# ---- 2. 消费侧：EFF TagExpr 引用的 tag ----
consumed = set()
for f in glob.glob(f"{ROOT}/data/gen/stat/StatEffectGen.java"):
    txt = open(f, encoding="utf-8").read()
    for m in re.finditer(r'TagExpr\.of\(\s*Set\.of\(([^)]*)\)', txt, re.S):
        body = m.group(1)
        for cm in re.finditer(r'StatTag\.(\w+)', body):
            consumed.add(tag_const.get(cm.group(1), "??"+cm.group(1)))
        for sm in re.finditer(r'"([^"]+)"', body):
            consumed.add(sm.group(1))
print(f"[EFF consume] 引用 tag 数 = {len(consumed)}")

# ---- 3. 产出侧：全局 tags.add / addAll ----
produced = set()
producer_files = set()
for f in glob.glob(f"{ROOT}/**/*.java", recursive=True):
    txt = open(f, encoding="utf-8").read()
    hit = False
    for m in re.finditer(r'\.add\(\s*(?:StatTag\.(\w+)|"([^"]+)")', txt):
        if m.group(1):
            v = tag_const.get(m.group(1))
            if v: produced.add(v)
        elif m.group(2):
            produced.add(m.group(2))
    # addAll(Collection<String>) — 收集被 addAll 的变量名再搜索其构造
    for m in re.finditer(r'\.addAll\(([\w.]+)\)', txt):
        # 同一文件内找该变量定义处的字面量
        var = m.group(1).split(".")[-1]
        for dm in re.finditer(rf'{var}\s*=\s*[^;]*?(?:List\.of|Set\.of|Arrays\.asList)\s*\(([^;]*?)\)', txt, re.S):
            for sm in re.finditer(r'"([^"]+)"', dm.group(1)):
                produced.add(sm.group(1))
    if hit:
        producer_files.add(f)
print(f"[engine produce] 产出 tag 数 = {len(produced)}")

# ---- 4. 差异 ----
never_produced = sorted(c for c in consumed if c not in produced)
print("\n=== 消费但从未产出（潜在死接线）===")
for c in never_produced:
    name = const2name.get(c, "?")
    print(f"  {c}  (StatTag.{name})")

print(f"\n总计潜在死 tag = {len(never_produced)}")
print("\n=== 产出但 EFF 未消费（信息性，可能供 Condition/Target 用）===")
for p in sorted(produced - consumed):
    print(f"  {p}")
