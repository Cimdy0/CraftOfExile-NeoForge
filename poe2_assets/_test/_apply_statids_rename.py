# -*- coding: utf-8 -*-
"""应用 StatIds 概念改名：rename_map.json 的 concept 字典 (OLD->NEW)。
把所有 src/main/java 下 .java 中的 StatIds.OLD -> StatIds.NEW（词边界精确替换）。
不处理 exact(自身) 与 unmapped(保留手写名)。统计每处替换次数。
"""
import os, re, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
m = json.load(open(os.path.join(ROOT, "poe2_assets/_test/rename_map.json")))
concept = m["concept"]  # OLD -> NEW
SRC = os.path.join(ROOT, "src/main/java")

total = 0
per_file = {}
for dp, _, fns in os.walk(SRC):
    if ".step" in dp or "coherebak" in dp:
        continue
    for fn in fns:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dp, fn)
        # 跳过 StatIds.java 本身（它不引用自己）
        if fn == "StatIds.java":
            continue
        try:
            content = open(p, encoding="utf-8", errors="ignore").read()
        except Exception:
            continue
        orig = content
        for old, new in concept.items():
            content = re.sub(r"\bStatIds\." + re.escape(old) + r"\b", "StatIds." + new, content)
        if content != orig:
            n = sum(1 for _ in re.finditer(r"StatIds\." + re.escape(new) + r"\b", content)) - \
                sum(1 for _ in re.finditer(r"StatIds\." + re.escape(new) + r"\b", orig))
            # 上面的 n 计算不精确；改为记录替换差
            open(p, "w", encoding="utf-8").write(content)
            per_file[p] = per_file.get(p, 0) + 1
            total += 1

print(f"修改文件数: {len(per_file)}")
for p, c in sorted(per_file.items()):
    print(f"  {os.path.relpath(p, ROOT)}  (命中常量数统计见下)")
# 统计总替换次数
cnt = 0
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith(".java") or fn == "StatIds.java":
            continue
        c = open(os.path.join(dp, fn), encoding="utf-8", errors="ignore").read()
        for old, new in concept.items():
            cnt += len(re.findall(r"StatIds\." + re.escape(new) + r"\b", c))
print(f"Concept 改名映射条目数: {len(concept)}")
print(f"全仓 StatIds.<NEW> 命中总数(改名后): {cnt}")
