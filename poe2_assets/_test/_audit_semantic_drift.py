#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
语义漂移审计：description -> StatIds -> Attribute 链路。

对每个 AttributeRegister 属性：
  - 解析其注册常量名 / key 字符串 / 上方 javadoc 注解
  - 解析 StatGen 中它的全部接线（op / 所接 StatIds / scope tags）
  - 取 StatIds 的 id("...") 字符串作为「PoE2 语义锚」（即蛇形英文原义）
  - 启发式标记语义漂移：
      1. 命名漏义：某条接线的 id-string 关键词，属性名/key 中一个都不含
      2. 概念混淆：单属性承载 2+ 个互不相干的 StatIds（共享词 < 2）
      3. 注解矛盾：注解说的机制（几率/倍率/穿透…）与接线 op/tags 不符
输出 JSON（machine） + 控制台候选表（human）。
"""
import json
import re
import os

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe"
ATTR = os.path.join(ROOT, "data", "AttributeRegister.java")
STATGEN = os.path.join(ROOT, "data", "gen", "stat", "StatGen.java")
STATIDS = os.path.join(ROOT, "data", "codec", "support", "stat", "StatIds.java")


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


# ---------------- AttributeRegister ----------------
attr_src = read(ATTR)
# 常量声明
decl_re = re.compile(
    r"public static Holder<Attribute>\s+(\w+)\s*=\s*register\w+\(\s*\"([^\"]+)\"",
)
attrs = {}  # const -> {key, comment, line}
for m in decl_re.finditer(attr_src):
    const, key = m.group(1), m.group(2)
    ln = attr_src[: m.start()].count("\n") + 1
    attrs[const] = {"key": key, "comment": "", "line": ln}
# 上方 javadoc 注解
for const in list(attrs.keys()):
    start = attr_src.find("public static Holder<Attribute> " + const + " =")
    blk = attr_src[:start]
    jd = blk.rfind("/**")
    if jd != -1:
        end = blk.find("*/", jd)
        if end != -1 and end <= len(blk):
            cmt = attr_src[jd : end + 2]
            # 取每行末尾中文语义（最后一个 * 后的内容）
            txt = cmt.replace("/**", "").replace("*/", "")
            txt = "\n".join(l.split("*", 1)[1].strip() if "*" in l else l.strip() for l in txt.splitlines())
            attrs[const]["comment"] = txt.strip()


# ---------------- StatIds ----------------
ids_src = read(STATIDS)
ids = {}  # const -> idstring
for m in re.finditer(r"public static final Identifier\s+(\w+)\s*=\s*id\(\s*\"([^\"]+)\"", ids_src):
    ids[m.group(1)] = m.group(2)


# ---------------- StatGen wiring ----------------
sg_src = read(STATGEN)
wire_re = re.compile(
    r"(\w+)\(\s*map,\s*StatIds\.(\w+),\s*AttributeRegister\.(\w+)((?:\s*,\s*[^)]*)*)\)",
)
wirings = {}  # attr_const -> [ {op, statId, idstring, tags, raw} ]
for m in wire_re.finditer(sg_src):
    op, sid_const, attr_const = m.group(1), m.group(2), m.group(3)
    rest = m.group(4)
    tags = re.findall(r"StatTag\.(\w+)", rest)
    idstring = ids.get(sid_const, "<UNKNOWN:" + sid_const + ">")
    wirings.setdefault(attr_const, []).append(
        {"op": op, "statId": sid_const, "idstring": idstring, "tags": tags}
    )


# ---------------- 漂移启发式 ----------------
def words(s):
    return set(w for w in re.split(r"[_\s]+", s.lower()) if len(w) > 1)


STOP = {"of", "to", "with", "the", "x", "on", "in", "from", "that", "are", "is", "and",
        "or", "by", "for", "as", "at", "per", "vs", "while", "not", "than", "further",
        "enemies", "enemy", "you", "your", "hitdamage", "hits", "damage", "hit"}

candidates = []
for const, info in attrs.items():
    ws = wirings.get(const, [])
    if not ws:
        continue  # 孤儿属于 miswire 审计，此处只处理有接线的
    name_words = words(const) | words(info["key"])
    # 每条接线：属性名是否覆盖其语义词
    lost = []          # 命名漏义
    for w in ws:
        sem = words(w["idstring"]) - STOP
        missing = sem - name_words
        if missing:
            lost.append({"statId": w["statId"], "idstring": w["idstring"],
                         "missing": sorted(missing)})
    # 概念混淆：接线之间共享词 < 2
    conflated = False
    if len(ws) >= 2:
        shared = set.intersection(*[words(w["idstring"]) - STOP for w in ws])
        if len(shared) < 2:
            conflated = True
    # 注解矛盾（粗略）：注解含「几率」但无一条接线 op=chance 且无 chance 词
    cmt = info["comment"]
    note_chance = "几率" in cmt or "chance" in cmt.lower()
    has_chance = any(w["op"] == "chance" or "chance" in w["idstring"] for w in ws)
    note_mult = "倍率" in cmt or "magnitude" in cmt.lower() or "multiplier" in cmt.lower()
    has_mult = any(w["op"] in ("inc", "pctAdd", "addMultipliedBase", "addMultipliedTotal")
                  for w in ws)
    cmt_mismatch = (note_chance and not has_chance) or (note_mult and not has_mult)
    if lost or conflated or cmt_mismatch:
        candidates.append({
            "const": const, "key": info["key"], "line": info["line"],
            "comment": cmt[:120],
            "wirings": ws,
            "lost_meaning": lost,
            "conflated": conflated,
            "comment_mismatch": cmt_mismatch,
        })

out = {
    "total_attrs": len(attrs),
    "wired_attrs": sum(1 for c in attrs if c in wirings),
    "candidates": candidates,
}
with open(os.path.join(os.path.dirname(__file__), "semantic_drift_audit.json"), "w", encoding="utf-8") as f:
    json.dump(out, f, ensure_ascii=False, indent=2)

# 控制台：候选表
print(f"属性总数={len(attrs)}  有接线={out['wired_attrs']}  候选漂移={len(candidates)}\n")
for c in candidates:
    flags = []
    if c["lost_meaning"]:
        flags.append("命名漏义")
    if c["conflated"]:
        flags.append("概念混淆")
    if c["comment_mismatch"]:
        flags.append("注解矛盾")
    print(f"### {c['const']}  (key={c['key']}, L{c['line']})  标记={','.join(flags)}")
    if c["comment"]:
        print(f"    注解: {c['comment']}")
    for w in c["wirings"]:
        tag = ("{" + ",".join(w["tags"]) + "}") if w["tags"] else "{}"
        print(f"    {w['op']:14} {w['statId']:48} {tag:24} <- {w['idstring']}")
    for lm in c["lost_meaning"]:
        print(f"    [漏义] {lm['statId']} 属性名未覆盖词: {lm['missing']}")
    print()
