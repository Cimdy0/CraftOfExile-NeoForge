# -*- coding: utf-8 -*-
"""审计旧 StatIds.java.old 的注释可信度。

已发现硬事实：旧文件里存在**注释错配**——例如
    // Damage Penetration 18% Cold Resistance
    INC_AILMENT_CHANCE_VS_EXPOSED_COLD / _FIRE / _LIGHTNING
三条语义各异的常量共用同一条（且与自身语义无关的）注释。

这直接威胁第 5 步：§A「注释精确命中」若建立在错注释上，机械改名会把语义不同的
旧实现合并到同一新常量，造成 map.put 覆盖与静默语义丢失。

本脚本用两条硬判据量化污染面（均不依赖相似度）：
  P1 注释重复：同一注释字符串被 >1 条旧常量共用 -> 至多一条正确。
  P2 实现分歧：§A 碰撞组内各成员的 StatGen 实现「目标属性 + 效果类」不一致
     -> 它们绝不可能是同一条 stat，注释必有错配。
"""
import re, importlib.util, os
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m); _m.loader.exec_module(m)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
OUT = "poe2_assets/_test/old_comment_audit.md"

old = m.parse(OLD)
new = m.parse(NEW)
new_by_text = defaultdict(list)
for c, e, cm in new:
    new_by_text[cm].append(c)

# ---- P1 注释重复 ----
by_comment = defaultdict(list)
for const, eid, comment in old:
    if comment:
        by_comment[comment].append(const)
dup_comment = {k: v for k, v in by_comment.items() if len(v) > 1}
dup_consts = sum(len(v) for v in dup_comment.values())

# ---- §A 碰撞组 ----
rev = defaultdict(list)
for const, eid, comment in old:
    if not comment:
        continue
    k = m.normalize(comment)
    if k in new_by_text:
        rev[new_by_text[k][0]].append(const)
collide = {k: v for k, v in rev.items() if len(v) > 1}

# ---- P2 实现分歧 ----
sg = open(STATGEN, encoding="utf-8").read().split("\n")
HELPER = re.compile(r"\b(inc|more|add|chance|valueMod|flat|pct)\(map,\s*StatIds\.([A-Z][A-Z0-9_]*)")
PUT = re.compile(r"map\.put\(StatIds\.([A-Z][A-Z0-9_]*)")
ATTR = re.compile(r"\b(?:AttributeRegister|Attributes)\.([A-Z][A-Z0-9_]*)\b")
EFF = re.compile(r"\b(\w+StatEffect|CreateGroundOnAilmentEffect)\b")


def impl_sig(const):
    """实现指纹 = (效果类集合, 目标属性集合)。找不到实现返回 None。"""
    pat = re.compile(r"(?:map\.put\(|\b\w+\(map,\s*)StatIds\." + const + r"\b")
    for i, line in enumerate(sg):
        if pat.search(line):
            k, depth = i, 0
            while k < len(sg):
                depth += sg[k].count("(") - sg[k].count(")")
                if depth <= 0 and sg[k].rstrip().endswith(";"):
                    break
                k += 1
            stmt = "\n".join(sg[i:k + 1])
            helper = HELPER.search(stmt)
            eff = frozenset(EFF.findall(stmt))
            attrs = frozenset(ATTR.findall(stmt))
            kind = frozenset([helper.group(1)]) if helper else eff
            return (kind, attrs)
    return None


diverge, agree, noimpl = {}, {}, {}
for newc, consts in collide.items():
    sigs = {c: impl_sig(c) for c in consts}
    known = {c: s for c, s in sigs.items() if s is not None}
    if len(known) < 2:
        noimpl[newc] = sigs
        continue
    attr_sets = {s[1] for s in known.values()}
    if len(attr_sets) > 1:
        diverge[newc] = sigs
    else:
        agree[newc] = sigs

with open(OUT, "w", encoding="utf-8") as f:
    w = f.write
    w("# 旧 StatIds 注释可信度审计\n\n")
    w("## 结论摘要\n\n| 指标 | 数值 |\n|---|---|\n")
    w(f"| 旧条目总数 | {len(old)} |\n")
    w(f"| 有注释条目 | {sum(1 for _,_,c in old if c)} |\n")
    w(f"| **P1 注释重复：重复注释串** | **{len(dup_comment)}** |\n")
    w(f"| **P1 涉及旧常量** | **{dup_consts}** |\n")
    w(f"| §A 碰撞组（多旧 -> 同一新常量） | {len(collide)} |\n")
    w(f"| **P2 组内实现目标属性分歧（注释必错配）** | **{len(diverge)}** |\n")
    w(f"| P2 组内实现一致（真重复，可合并） | {len(agree)} |\n")
    w(f"| P2 组内可比实现不足 | {len(noimpl)} |\n\n")

    w("---\n\n## P2 实现分歧组（**注释错配，禁止机械改名**）\n\n")
    for newc, sigs in sorted(diverge.items()):
        w(f"### `{newc}`\n\n")
        for c, s in sorted(sigs.items()):
            if s is None:
                w(f"- `{c}` — 无实现\n")
            else:
                w(f"- `{c}` — 效果 {sorted(s[0])} / 属性 {sorted(s[1])}\n")
        w("\n")

    w("---\n\n## P1 重复注释（至多一条正确）\n\n")
    for cm, consts in sorted(dup_comment.items(), key=lambda x: -len(x[1]))[:120]:
        w(f"- **{len(consts)}** 条共用 `{cm}`：" + ", ".join(f"`{c}`" for c in consts) + "\n")

    w("\n---\n\n## P2 实现一致组（真重复，保留一条即可）\n\n")
    for newc, sigs in sorted(agree.items()):
        w(f"- `{newc}` <- " + ", ".join(f"`{c}`" for c in sorted(sigs)) + "\n")

print(f"旧条目 {len(old)}")
print(f"P1 重复注释串 {len(dup_comment)}，涉及旧常量 {dup_consts}")
print(f"§A 碰撞组 {len(collide)}")
print(f"P2 实现分歧组 {len(diverge)}（注释错配，禁止机械改名）")
print(f"P2 实现一致组 {len(agree)}（真重复，可合并）")
print(f"P2 实现不足组 {len(noimpl)}")
print(f"报告: {OUT}")
