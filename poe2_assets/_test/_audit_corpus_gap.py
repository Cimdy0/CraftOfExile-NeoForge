# -*- coding: utf-8 -*-
"""精确量化 corpus 应有 StatIds 量 vs 当前量 vs 被删量。
源 = stat_templates.txt（gen_stat_ids.py 的输入）。复用生成器同源归一逻辑（不重跑生成器）。
"""
import os, re

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
TEMPLATES = os.path.join(ROOT, "poe2_assets/stat_templates.txt")

# ---- 生成器同源归一 ----
REP = [("Accuracy Rating", "Accuracy"), ("Evasion Rating", "Evasion"),
       ("Armour", "Armor"), ("Life", "Health"), ("Maximum", "Max")]
GLUE = re.compile(r"\b[A-Z][a-z]+(?:[A-Z][a-z]+)+\b")

def to_id(s):
    for a, b in REP:
        s = s.replace(a, b)
    s = GLUE.sub(lambda m: " ".join(re.findall(r"[A-Z][a-z]*", m.group(0))), s)
    s = re.sub(r"[^0-9A-Za-z]+", " ", s).strip().lower()
    s = re.sub(r"\s+", "_", s)
    return s

# 当前 StatIds 的 id 串
sid_src = open(SID, encoding="utf-8", errors="ignore").read()
cur_ids = set(re.findall(r'id\("([a-z0-9_]+)"\)', sid_src))
cur_consts = set(re.findall(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(", sid_src))

# corpus 派生 id（每行模板，按 | 取短者）
corpus_ids = {}
with open(TEMPLATES, encoding="utf-8", errors="ignore") as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        parts = [p.strip() for p in line.split("|")]
        tmpl = min(parts, key=len) if parts else line
        i = to_id(tmpl)
        if i:
            corpus_ids[i] = tmpl

print(f"corpus 派生 id 数 (stat_templates.txt 归一后): {len(corpus_ids)}")
print(f"当前 StatIds 常量数: {len(cur_consts)}  (id 串唯一数: {len(cur_ids)})")

missing = sorted(set(corpus_ids) - cur_ids)     # corpus 有、StatIds 无
extra   = sorted(cur_ids - set(corpus_ids))      # StatIds 有、corpus 无（可能是手加/变体）
print(f"\n=== corpus 缺于 StatIds（真实缺失量，应实现而非删除）: {len(missing)} ===")
for i in missing[:40]:
    print(f"   - {i}   <- \"{corpus_ids[i]}\"")
if len(missing) > 40:
    print(f"   ... 共 {len(missing)} 条")

print(f"\n=== StatIds 有但 corpus 无（{len(extra)}，可能为手加/后缀变体，非删除目标）===")
for i in extra[:20]:
    print(f"   + {i}")

# 被删的 78（来自 delete_lists.json，如果存在）
dl = os.path.join(ROOT, "poe2_assets/_test/delete_lists.json")
if os.path.exists(dl):
    import json
    d = json.load(open(dl))
    deleted = d.get("dangling_statids", [])
    deleted_ids = [v for v in deleted]  # delete_lists 存的是 id 串还是常量名？看结构
    print(f"\n=== 上次删除的 dangling StatIds 记录条数: {len(deleted)} ===")
    print("   样例(前10):", deleted[:10])
    # 那些被删的里，有多少其实在 corpus 中（应恢复而非删除）
    deleted_in_corpus = [x for x in deleted if x in corpus_ids or x in cur_ids]
    print(f"   其中能在 corpus/当前 中定位的: {len(deleted_in_corpus)}")
