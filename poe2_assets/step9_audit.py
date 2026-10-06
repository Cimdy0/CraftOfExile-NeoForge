#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Step 9（修正版）：严格排查 StatIds ↔ StatGen ↔ Effect 接线（描述→Ids→StatGen 1:1:1 不变量）。

StatGen 注册形态多样（map.put / valueMod / inc / more / add / chance / conditionMod /
dtcReroute / extraDamage / recoup / recoverPctOnEvent / penetrate / grantOnEvent），
本版扫描 StatGen.java 内全部 `StatIds.X` token 作为「已接线」判定。

检测维度：
  A. 孤儿 Id（Effect 未实现）：StatIds 声明但全仓零引用（既不在 StatGen，也不在任何 gen 被消费）。
  B. 重复 StatGen：同一 Id 在 StatGen 出现 ≥2 次（重复生成）。
  C. 共享注释（备注错接 / 重复生成）：同一条注释挂在 ≥2 个 Id 上。
  D. 重复 path：两个 Id 的 id("...") 字符串相同。
  E. 悬空 StatGen：StatGen 引用的 StatIds.X 未在 StatIds 定义。
  F. 仅 StatGen 未消费（有 StatGen 但全仓零引用，可能死实现）。
输出：控制台摘要 + poe2_assets/_test/step9_audit.md 详细报告。
"""
import os, re, collections

ROOT = "src/main/java"
SID = os.path.join(ROOT, "com/cimdy/coe/data/codec/support/stat/StatIds.java")
SGEN = os.path.join(ROOT, "com/cimdy/coe/data/gen/stat/StatGen.java")

decl_re = re.compile(r'public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=\s*id\(\s*"([^"]*)"\s*\)\s*;')
usage_re = re.compile(r'StatIds\.([A-Z][A-Z0-9_]*)')
HELPER_RE = re.compile(r'\b(map\.put|valueMod|conditionMod|inc|more|add|chance|dtcReroute|extraDamage|recoup|recoverPctOnEvent|penetrate|grantOnEvent|talentAttr)\b')

# ---------- 1) 解析 StatIds ----------
ids = {}            # NAME -> {path, comment, line}
comment_buf = []
with open(SID, encoding="utf-8") as f:
    for i, line in enumerate(f):
        s = line.strip()
        if s.startswith("//"):
            comment_buf.append(s[2:].strip())
            continue
        m = decl_re.search(line)
        if m:
            name, path = m.group(1), m.group(2)
            ids[name] = {"path": path, "comment": " / ".join(comment_buf), "line": i + 1}
            comment_buf = []
        else:
            comment_buf = []

# ---------- 2) 解析 StatGen（全部 StatIds.X 形态）----------
sg_count = collections.Counter()          # NAME -> 在 StatGen 出现次数
sg_helpers = collections.defaultdict(set) # NAME -> 用到的注册 helper 集合
with open(SGEN, encoding="utf-8") as f:
    for line in f:
        for m in re.finditer(r'StatIds\.([A-Z][A-Z0-9_]*)', line):
            name = m.group(1)
            sg_count[name] += 1
            for h in HELPER_RE.findall(line):
                sg_helpers[name].add(h)
wired = set(sg_count)

# ---------- 3) 全仓用法扫描 ----------
usage = collections.Counter()
for dp, _, fs in os.walk(ROOT):
    for fn in fs:
        if not fn.endswith(".java"):
            continue
        if ".step" in fn or fn.endswith(".bak"):
            continue
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="ignore").read()
        for m in usage_re.finditer(text):
            usage[m.group(1)] += 1

# ---------- 4) 计算各项违规 ----------
defined = set(ids)
with_sg = sorted(wired & defined)
without_sg = sorted([n for n in ids if n not in wired])     # 无 StatGen 条目
orphans = sorted([n for n in ids if usage[n] == 0])         # A. 全仓零引用
sg_only_dead = sorted([n for n in wired if usage[n] == 0])  # F. 仅 StatGen 死实现
dup_sg = sorted([n for n, c in sg_count.items() if c >= 2 and n in defined])  # B. 重复 StatGen
dangling = sorted([n for n in sg_count if n not in defined]) # E. 悬空 StatGen

path_groups = collections.defaultdict(list)
for n, d in ids.items():
    path_groups[d["path"]].append(n)
dup_path = {p: ns for p, ns in path_groups.items() if len(ns) >= 2}  # D. 重复 path

comment_groups = collections.defaultdict(list)
for n, d in ids.items():
    c = d["comment"].strip()
    if not c or c.upper().startswith("TODO") or len(c) < 6:
        continue
    comment_groups[c].append(n)
shared_comment = {c: ns for c, ns in comment_groups.items() if len(ns) >= 2}  # C. 共享注释

# ---------- 4b) 共享注释 triage：连贯簇（同前缀）vs 疑似错接 ----------
def common_prefix_segs(names):
    segs = [n.split("_") for n in names]
    if not segs:
        return []
    pref = segs[0]
    for s in segs[1:]:
        i = 0
        while i < len(pref) and i < len(s) and pref[i] == s[i]:
            i += 1
        pref = pref[:i]
    return pref

triage = []  # (comment, names, common_prefix_segs, effect_types, suspect)
for c, ns in shared_comment.items():
    cp = common_prefix_segs(ns)
    effs = sorted({h for n in ns for h in sg_helpers.get(n, set())})
    suspect = len(cp) < 2
    triage.append((c, ns, cp, effs, suspect))

coherent = [t for t in triage if not t[4]]
suspect_groups = [t for t in triage if t[4]]

# ---------- 4c) 36 无 StatGen 接线的消费位置 ----------
consume_loc = {}
for n in without_sg:
    locs = set()
    for dp, _, fs in os.walk(ROOT):
        for fn in fs:
            if not fn.endswith(".java") or ".step" in fn or fn.endswith(".bak"):
                continue
            p = os.path.join(dp, fn)
            if p == SID or p == SGEN:
                continue
            txt = open(p, encoding="utf-8", errors="ignore").read()
            if ("StatIds.%s" % n) in txt:
                locs.add(p.replace(ROOT + "/", ""))
    consume_loc[n] = sorted(locs)

# ---------- 5) 输出 ----------
total = len(ids)
print("=" * 72)
print("Step 9 严格排查报告（修正版：StatGen 含全部注册形态）")
print("=" * 72)
print("StatIds 常量总数            : %d" % total)
print("  其中在 StatGen 有接线     : %d" % len(with_sg))
print("  其中无 StatGen 接线       : %d" % len(without_sg))
print("-" * 72)
print("A. 孤儿 Id（全仓零引用）           : %d" % len(orphans))
print("B. 重复 StatGen（同 Id ≥2 次）     : %d" % len(dup_sg))
print("C. 共享注释（备注错接/重复生成）   : %d 组" % len(shared_comment))
print("     ├─ 连贯簇（同前缀 keystone 子效果，疑似正常）: %d 组" % len(coherent))
print("     └─ 疑似错接（跨前缀/语义不符，需修）         : %d 组" % len(suspect_groups))
print("D. 重复 path（id 字符串相同）      : %d 组" % len(dup_path))
print("E. 悬空 StatGen（Id 未定义）        : %d" % len(dangling))
print("F. 仅 StatGen 死实现（全仓零引用） : %d" % len(sg_only_dead))
print("G. 无 StatGen 接线但被消费（非孤儿）: %d" % len([n for n in without_sg if consume_loc.get(n)]))
print("=" * 72)

print("\n### C2. 疑似错接组（跨前缀 / 语义不符，优先排查）###")
for c, ns, cp, effs, _ in sorted(suspect_groups, key=lambda t: -len(t[1])):
    print("\n  注释: %s" % c)
    print("    公共前缀: %s" % ("_".join(cp) if cp else "(无)"))
    for n in ns:
        print("    - %s  StatGen×%d[%s]" % (n, sg_count.get(n, 0), ",".join(sorted(sg_helpers.get(n, []))) or "-"))

def dump(title, items, cap=40, fmt=None):
    print("\n### %s（%d）###" % (title, len(items)))
    for it in items[:cap]:
        print("  -", (fmt(it) if fmt else it))
    if len(items) > cap:
        print("  ...（另有 %d 项，详见报告）" % (len(items) - cap))

dump("A. 孤儿 Id（全仓零引用）", orphans)
dump("B. 重复 StatGen", dup_sg, fmt=lambda n: "%s ×%d [%s]" % (n, sg_count[n], ",".join(sorted(sg_helpers.get(n, [])))))
dump("F. 仅 StatGen 死实现", sg_only_dead)

print("\n### C. 共享注释（备注错接/重复生成）###")
for c, ns in sorted(shared_comment.items(), key=lambda kv: -len(kv[1])):
    print("\n  注释: %s" % c)
    for n in ns:
        w = "StatGen×%d[%s]" % (sg_count.get(n, 0), ",".join(sorted(sg_helpers.get(n, []))) or "-")
        print("    - %s  %s" % (n, w))

print("\n### D. 重复 path ###")
for p, ns in sorted(dup_path.items()):
    print("  path: %s -> %s" % (p, ns))

print("\n### E. 悬空 StatGen ###")
for n in dangling:
    print("  - %s" % n)

# ---------- 6) 写详细报告 ----------
os.makedirs("poe2_assets/_test", exist_ok=True)
rep = []
rep.append("# Step 9 严格排查报告（修正版）\n")
rep.append("## 摘要\n")
rep.append("- StatIds 常量总数: %d（StatGen 接线 %d，无接线 %d）" % (total, len(with_sg), len(without_sg)))
rep.append("- A. 孤儿 Id（全仓零引用）: %d" % len(orphans))
rep.append("- B. 重复 StatGen: %d" % len(dup_sg))
rep.append("- C. 共享注释（备注错接/重复生成）: %d 组（连贯簇 %d / 疑似错接 %d）" % (len(shared_comment), len(coherent), len(suspect_groups)))
rep.append("- D. 重复 path: %d 组" % len(dup_path))
rep.append("- E. 悬空 StatGen: %d" % len(dangling))
rep.append("- F. 仅 StatGen 死实现: %d\n" % len(sg_only_dead))

rep.append("## A. 孤儿 Id（全仓零引用）\n")
for n in orphans:
    rep.append("- `%s` (L%d) — %s" % (n, ids[n]["line"], ids[n]["comment"]))
rep.append("\n## B. 重复 StatGen\n")
for n in dup_sg:
    rep.append("- `%s` ×%d [%s]" % (n, sg_count[n], ",".join(sorted(sg_helpers.get(n, [])))))
rep.append("\n## C. 共享注释（备注错接/重复生成）\n")
rep.append("- 连贯簇（同前缀 keystone 子效果，疑似正常）: %d 组" % len(coherent))
rep.append("- 疑似错接（跨前缀/语义不符，需修）: %d 组\n" % len(suspect_groups))
rep.append("### C1. 连贯簇（疑似正常，仅记录）\n")
for c, ns, cp, effs, _ in sorted(coherent, key=lambda t: -len(t[1])):
    rep.append("\n**注释**: %s  （公共前缀: `%s`）\n" % (c, "_".join(cp) if cp else "(无)"))
    for n in ns:
        rep.append("- `%s` StatGen×%d[%s]" % (n, sg_count.get(n, 0), ",".join(sorted(sg_helpers.get(n, []))) or "-"))
rep.append("\n### C2. 疑似错接组（优先排查）\n")
for c, ns, cp, effs, _ in sorted(suspect_groups, key=lambda t: -len(t[1])):
    rep.append("\n**注释**: %s  （公共前缀: `%s`）\n" % (c, "_".join(cp) if cp else "(无)"))
    for n in ns:
        rep.append("- `%s` StatGen×%d[%s]" % (n, sg_count.get(n, 0), ",".join(sorted(sg_helpers.get(n, []))) or "-"))
rep.append("\n## D. 重复 path\n")
for p, ns in sorted(dup_path.items()):
    rep.append("- `%s` -> %s" % (p, ns))
rep.append("\n## E. 悬空 StatGen\n")
for n in dangling:
    rep.append("- `%s`" % n)
rep.append("\n## F. 仅 StatGen 死实现\n")
for n in sg_only_dead:
    rep.append("- `%s`" % n)
rep.append("\n## 附1：无 StatGen 接线的 Id（%d）\n" % len(without_sg))
for n in without_sg:
    locs = consume_loc.get(n, [])
    rep.append("- `%s` (L%d) 全仓引用=%d 消费于: %s — %s" % (n, ids[n]["line"], usage[n], ", ".join(locs) or "(无外部消费!)", ids[n]["comment"]))
rep.append("\n## 附2：B. 重复 StatGen（同 Id ≥2 次）\n")
for n in dup_sg:
    rep.append("- `%s` ×%d [%s]" % (n, sg_count[n], ",".join(sorted(sg_helpers.get(n, [])))))

open("poe2_assets/_test/step9_audit.md", "w", encoding="utf-8").write("\n".join(rep))
print("\n[报告已写入] poe2_assets/_test/step9_audit.md")
