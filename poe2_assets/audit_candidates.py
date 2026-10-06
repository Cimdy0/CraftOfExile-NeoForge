#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""审计 14 个 has_candidate 缺口：对每个候选 effect 打印真实签名(attr,op,tags) 供人工核验。
选线规则：节点 token 是 SEG token 子集时命中；多候选取最小超集(额外 token 最少)。"""
import json, os, re
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")
EFFGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectGen.java")
CLS = os.path.join(ROOT, "poe2_assets/c3_gap_classified.json")

LEADING_NS = {"minecraft","coe","charm","ailment","buff","debuff","form","ground","shrine","puppet","master"}
STOP = {"x","the","a","an","any","to","of","on","you","your","from","with","while","when","if",
        "against","per","for","in","is","increased","more","reduced","less","add","added","gain",
        "gains","rating","and","or"}
BRIDGE=[(r"\blightning\b","light"),(r"\bcritical\b","crit"),(r"\bmaximum\b","max"),(r"\barmour\b","armor")]
def norm(s): s=s.lower(); s=re.sub(r"[^a-z0-9 ]"," ",s); return re.sub(r"\s+"," ",s).strip()
def bridge(s):
    for p,r in BRIDGE: s=re.sub(p,r,s)
    return s
def nph(s):
    t=bridge(norm(s)); toks=[w for w in t.split() if w and w not in STOP]
    while toks and toks[0] in LEADING_NS: toks=toks[1:]
    return toks
def seg_tokens(seg):
    toks=seg.split("_"); 
    while toks and toks[0] in LEADING_NS: toks=toks[1:]
    return [bridge(w) for w in toks if w not in LEADING_NS]

# 全量 effect -> (op, seg_tokens, const)
effs=[]
for line in open(EFFIDS,encoding="utf-8"):
    m=re.search(r'public static final Identifier (\w+) = id\("([^"]+)"\)',line)
    if not m: continue
    const,path=m.group(1),m.group(2)
    mm=re.search(r"^add_(multiplied_base|multiplied_total|value)_attribute_(.+)$",path)
    if not mm: continue
    effs.append((mm.group(1), seg_tokens(mm.group(2)), const))

# 签名
sig={}
for line in open(EFFGEN,encoding="utf-8"):
    m=re.search(r"StatEffectIds\.(EFF_\w+),\s*new ModifierStatEffect\(",line)
    if not m: continue
    c=m.group(1)
    am=re.search(r"AttributeRegister\.(\w+)",line); om=re.search(r"AttributeModifier\.Operation\.(\w+)",line)
    tm=re.search(r"Set\.of\((.*?)\)",line)
    tags=re.findall(r"StatTag\.(\w+)",tm.group(1)) if tm else []
    tags+=[f"!{t}" for t in re.findall(r"not\(StatTag\.(\w+)\)",tm.group(1))] if tm else []
    sig[c]=(am.group(1) if am else "?", om.group(1) if om else "?", tags)

# 已接线
existing=set()
for line in open(os.path.join(ROOT,"src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"),encoding="utf-8"):
    m=re.search(r"map\.put\(StatIds\.(\w+),",line)
    if m: existing.add(m.group(1))

data=json.load(open(CLS,encoding="utf-8"))
print("=== 14 个 has_candidate 候选审计（多候选取最小超集）===\n")
for h in data["has_candidate"]:
    C,T,kind,toks,cands=h["constant"],h["template"],h["kind"],h["tokens"],h["candidates"]
    if C in existing: 
        print(f"[已接] {C}\n"); continue
    # 选最小超集候选
    best=None;best_extra=99
    for c in cands:
        # 找该 const 的 seg_tokens
        for op,st,cc in effs:
            if cc==c:
                extra=len(st)-len([t for t in toks if t in st])
                if extra<best_extra: best_extra=extra;best=c
                break
    a,o,tg=sig.get(best,("?","?",[]))
    # op 是否匹配节点意图(inc->multiplied_*, add->value)
    want = "multiplied" if "inc" in T.lower() or "increased" in T.lower() else "value"
    opok = want in o.lower()
    print(f"{C}\n  模板: {T}\n  节点tokens: {toks}\n  选线: {best}\n     attr={a} op={o} tags={tg}\n    op意图匹配: {'OK' if opok else 'MISMATCH('+want+')'}  候选数={len(cands)}\n")
