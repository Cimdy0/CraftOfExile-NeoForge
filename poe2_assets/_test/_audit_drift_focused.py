#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""聚焦漂移审查：对每个有接线属性，按「接线间核心名词差异度 + 命名覆盖度」打分，
输出真正值得人工复核的漂移候选（语义混淆 / 命名偏离）。"""
import json, re, os

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe"
ATTR = os.path.join(ROOT, "data", "AttributeRegister.java")
STATGEN = os.path.join(ROOT, "data", "gen", "stat", "StatGen.java")
STATIDS = os.path.join(ROOT, "data", "codec", "support", "stat", "StatIds.java")

def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()

attr_src = read(ATTR)
decl_re = re.compile(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(\s*\"([^\"]+)\"")
attrs = {}
for m in decl_re.finditer(attr_src):
    const, key = m.group(1), m.group(2)
    start = attr_src.find("public static Holder<Attribute> " + const + " =")
    blk = attr_src[:start]; jd = blk.rfind("/**")
    cmt = ""
    if jd != -1:
        end = blk.find("*/", jd)
        if end != -1:
            txt = attr_src[jd:end+2].replace("/**","").replace("*/","")
            cmt = "\n".join(l.split("*",1)[1].strip() if "*" in l else l.strip() for l in txt.splitlines()).strip()
    attrs[const] = {"key": key, "comment": cmt}

ids_src = read(STATIDS)
ids = {m.group(1): m.group(2) for m in re.finditer(r"public static final Identifier\s+(\w+)\s*=\s*id\(\s*\"([^\"]+)\"", ids_src)}

sg_src = read(STATGEN)
wire_re = re.compile(r"(\w+)\(\s*map,\s*StatIds\.(\w+),\s*AttributeRegister\.(\w+)((?:\s*,\s*[^)]*)*)\)")
wirings = {}
for m in wire_re.finditer(sg_src):
    op, sid, attr = m.group(1), m.group(2), m.group(3)
    tags = re.findall(r"StatTag\.(\w+)", m.group(4))
    wirings.setdefault(attr, []).append({"op": op, "statId": sid,
        "idstring": ids.get(sid, "<UNK:"+sid+">"), "tags": tags})

# 归一词表：去算子/语法词 + 拼写归一
STOP = {"of","to","with","the","x","on","in","from","that","are","is","and","or","by","for",
        "as","at","per","vs","while","not","than","further","enemies","enemy","you","your",
        "hitdamage","hits","damage","hit","have","a","chance","skills","consume","ve","flask",
        "if","been","recently","any","affected","low","stationary","surrounded","second","max",
        "add","inc","more","pct","value","mod","regen","equal","they","their","when","during",
        "modifier","can","be","its","this","these","those","who","whom","which","it"}
NORM = {"armor":"armour","armour":"armour","shapes":"shapeshift","shapeshifted":"shapeshift",
        "regenerate":"regen","regeneration":"regen","ignite":"ignite","poison":"poison"}

def toks(s):
    out = set()
    for w in re.split(r"[_\s]+", s.lower()):
        w = NORM.get(w, w)
        if len(w) > 2 and w not in STOP:
            out.add(w)
    return out

def core_noun(idstring):
    """取 id-string 中最长的非停用词作为核心名词。"""
    ts = toks(idstring)
    if not ts:
        return ""
    return max(ts, key=len)

review = []
for const, info in attrs.items():
    ws = wirings.get(const, [])
    if len(ws) < 1:
        continue
    cores = [core_noun(w["idstring"]) for w in ws]
    distinct_cores = set(cores)
    name_toks = toks(const) | toks(info["key"])
    # 命名覆盖：属性名是否含任一接线核心名词
    covered = any(c and c in name_toks for c in distinct_cores)
    # 语义混淆：2+ 接线且核心名词互异
    conflated = len(ws) >= 2 and len(distinct_cores) >= 2
    # 注解是否只描述了一个接线（漏描其余）
    score = (0
             + (2 if conflated else 0)
             + (3 if not covered else 0)
             + (1 if len(ws) >= 3 else 0))
    if score >= 2:
        review.append({
            "const": const, "key": info["key"], "comment": info["comment"][:80],
            "score": score, "conflated": conflated, "covered": covered,
            "cores": sorted(distinct_cores),
            "wirings": [{"op": w["op"], "id": w["idstring"], "tags": w["tags"]} for w in ws],
        })

review.sort(key=lambda r: -r["score"])
with open(os.path.join(os.path.dirname(__file__), "drift_focused.json"), "w", encoding="utf-8") as f:
    json.dump(review, f, ensure_ascii=False, indent=2)

print(f"聚焦候选={len(review)}（按漂移分降序）\n")
for r in review:
    tags = []
    if r["conflated"]: tags.append("语义混淆")
    if not r["covered"]: tags.append("命名偏离")
    print(f"### {r['const']}  (key={r['key']})  分={r['score']}  {','.join(tags)}")
    print(f"    注解: {r['comment']}")
    print(f"    核心名词: {r['cores']}")
    for w in r["wirings"]:
        tg = ("{"+",".join(w["tags"])+"}") if w["tags"] else "{}"
        print(f"      {w['op']:10} {tg:22} {w['id']}")
    print()
