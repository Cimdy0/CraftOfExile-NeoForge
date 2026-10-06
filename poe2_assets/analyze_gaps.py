#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""对 c3_gap.json 的 438 SIMPLE 缺口做二分：有注册候选(需 scope 签字) vs 真设计缺口(无 effect)。"""
import json, os, re
from collections import Counter, defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
GAP = os.path.join(ROOT, "poe2_assets/c3_gap.json")

LEADING_NS = {"minecraft","coe","charm","ailment","buff","debuff","form","ground","shrine","puppet","master"}
STOP = {"x","the","a","an","any","to","of","on","you","your","from","with","while","when","if",
        "against","per","for","in","is","increased","more","reduced","less","add","added","gain",
        "gains","rating","and","or"}
BRIDGE = [(r"\blightning\b","light"),(r"\bcritical\b","crit"),(r"\bmaximum\b","max"),(r"\barmour\b","armor")]

def norm(s):
    s=s.lower(); s=re.sub(r"[^a-z0-9 ]"," ",s); return re.sub(r"\s+"," ",s).strip()
def bridge(s):
    for p,r in BRIDGE: s=re.sub(p,r,s)
    return s
def nph(s):
    t=bridge(norm(s)); toks=[w for w in t.split() if w and w not in STOP]
    while toks and toks[0] in LEADING_NS: toks=toks[1:]
    return toks
def seg_tokens(seg):
    toks=seg.split("_")
    while toks and toks[0] in LEADING_NS: toks=toks[1:]
    return [bridge(w) for w in toks if w not in LEADING_NS]

# 全量 effect 的 (op, seg_tokens, const)
effs=[]
for line in open(EFFIDS,encoding="utf-8"):
    m=re.search(r'public static final Identifier (\w+) = id\("([^"]+)"\)',line)
    if not m: continue
    const,path=m.group(1),m.group(2)
    mm=re.search(r"^add_(multiplied_base|multiplied_total|value)_attribute_(.+)$",path)
    if not mm: continue
    effs.append((mm.group(1), seg_tokens(mm.group(2)), const))

gaps=json.load(open(GAP,encoding="utf-8"))
has_cand, no_cand = [], []
for g in gaps:
    toks=nph(g["template"])
    # 去掉首操作符残留后再提属性已在 nph 处理；这里 toks 即属性 token 集
    # 候选：存在注册 effect 其 seg_tokens 是 toks 的超集（含 toks 全部 token）
    cands=[c for op,st,c in effs if all(tk in st for tk in toks)] if toks else []
    if cands:
        has_cand.append((g["constant"],g["template"],g["kind"],toks,cands))
    else:
        no_cand.append((g["constant"],g["template"],g["kind"],toks))

print(f"SIMPLE 缺口总数            : {len(gaps)}")
print(f"  有注册候选(需 scope 签字) : {len(has_cand)}")
print(f"  真设计缺口(无 effect)     : {len(no_cand)}")
print("\n--- 有候选样本(前 25) ---")
for c,t,k,toks,cands in has_cand[:25]:
    print(f"  {c:38s} | {t:42s} | toks={toks} -> {cands[:3]}")
print("\n--- 真设计缺口样本(前 30, 按 token 聚类) ---")
nc=Counter(" ".join(toks) for *_,toks in no_cand)
for ph,n in nc.most_common(30):
    print(f"  {n:4d}  {ph}")

# 有候选里的 op 分布（是否唯一乘算）
multi=[h for h in has_cand if len({c for *_,cs in [h] for c in cs})>1]
print(f"\n有候选中多候选(需选 op/scope) : {len(multi)}")

with open(os.path.join(ROOT,"poe2_assets/c3_gap_classified.json"),"w",encoding="utf-8") as f:
    json.dump({"has_candidate":[{"constant":h[0],"template":h[1],"kind":h[2],"tokens":h[3],"candidates":h[4]} for h in has_cand],
               "no_candidate":[{"constant":n[0],"template":n[1],"kind":n[2],"tokens":n[3]} for n in no_cand]},
              f,ensure_ascii=False,indent=1)
print(f"\n分类 -> poe2_assets/c3_gap_classified.json ({len(has_cand)}/{len(no_cand)})")
