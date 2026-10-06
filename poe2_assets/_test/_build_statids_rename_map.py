# -*- coding: utf-8 -*-
"""为 Option B(模板命名全量重建1166+改名) 建立 live(589)->corpus(1166) 命名映射。

策略：
- 精确名匹配：live_name == corpus_name -> 自身(不改)。
- 概念去 X_ 匹配：corpus_id 去掉前导 x_ 后 == live_id -> live_name -> corpus_name。
- 其余：UNMAPPED，需人工裁定（输出供审阅）。
输出 rename_map.json / unmapped.json，并打印统计。
"""
import os, re, sys, json

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SID = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
sys.path.insert(0, os.path.join(ROOT, "poe2_assets"))
import gen_stat_ids as g

# corpus 权威 1166
raw = [l.rstrip("\n") for l in open(g.TEMPLATES, encoding="utf-8") if l.strip()]
templ = [g.normalize(t) for t in raw]
corpus = []
cname, cid = {}, {}
k = 2
for t in templ:
    bn, bi = g.to_name(t), g.to_id(t)
    nm, eid = bn, bi
    while nm in cname or eid in cid:
        nm, eid = f"{bn}_{k}", f"{bi}_{k}"; k += 1
    cname[nm] = eid; cid[eid] = nm
    corpus.append((nm, eid, t))

# live 589
src = open(SID, encoding="utf-8", errors="ignore").read()
live = re.findall(r"public static final Identifier\s+([A-Z_][A-Z0-9_]*)\s*=\s*id\(\"([a-z0-9_]+)\"\)", src)

corpus_names = set(cname)
corpus_id_to_name = cid
exact, concept, unmapped = {}, {}, []
for lname, lid in live:
    if lname in corpus_names:
        exact[lname] = lname
        continue
    # 概念去 X_：corpus id 去前导 x_
    hit = None
    for ceid, cnm in corpus_id_to_name.items():
        if ceid == lid:
            hit = cnm; break
        if ceid.startswith("x_") and ceid[2:] == lid:
            hit = cnm; break
        if lid.startswith("x_") and lid[2:] == ceid:
            hit = cnm; break
    if hit:
        concept[lname] = hit
    else:
        unmapped.append((lname, lid))

print(f"corpus 条目: {len(corpus)}")
print(f"live 条目: {len(live)}")
print(f"  精确名匹配(不改): {len(exact)}")
print(f"  概念去X_匹配(改名): {len(concept)}")
print(f"  无法自动映射(需裁定): {len(unmapped)}")
print("\n--- 概念去X_匹配样例(前25) ---")
for a, b in list(concept.items())[:25]:
    print(f"  {a} -> {b}")
print("\n--- 无法自动映射(全部, 需人工裁定) ---")
for a, b in unmapped:
    print(f"  {a} = id(\"{b}\")")

json.dump({"exact": exact, "concept": concept, "unmapped": unmapped},
          open(os.path.join(ROOT, "poe2_assets/_test/rename_map.json"), "w"), indent=1)
print("\nwritten poe2_assets/_test/rename_map.json")
