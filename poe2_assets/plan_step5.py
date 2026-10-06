# -*- coding: utf-8 -*-
"""第 5 步（2/2）：把归因结果落成**可执行的重构计划**。

输入
  - 旧 StatIds（1460 条，备份）
  - 新 StatIds（1166 条，第 4 步产物）
  - 7 个消费文件的逐文件引用集

判据（全确定性，无相似度、无近似）
  A    旧注释规范化后与新表某条模板逐字符相等                -> 机械改名
  D1   旧注释 == 某多行块「合并成一行」的规范化形，且该块按
       换行语义可切出 >=2 片                                 -> 一对多拆分
  KEEP 其余失配条目中，**被 StatGen 以外的消费文件引用**者    -> 保留（模组自有子系统真实依赖）
  DROP 其余失配条目中，仅被 StatGen 引用或完全无引用者        -> 删除（仅服务于天赋树注册，随之重建）

「StatGen 以外」= AdditionType(词缀系统) / StatusBehavior / BuffStatusTypes /
CharmStatusTypes / FormStatusTypes / SkillFactory。

产出
  poe2_assets/_test/step5_plan.md          人类可读计划
  poe2_assets/_test/rename.tsv             OLD<TAB>NEW           （A 类机械改名）
  poe2_assets/_test/keep_consts.txt        须保留的旧常量        （手工通道预填）
  poe2_assets/_test/drop_consts.txt        须删除的旧常量
  poe2_assets/_test/d1_blocks.md           一对多拆分待裁决清单
"""
import json, re, importlib.util
from collections import defaultdict

_m = importlib.util.spec_from_file_location("m", "poe2_assets/map_old_stats.py")
m = importlib.util.module_from_spec(_m); _m.loader.exec_module(m)

_et = importlib.util.spec_from_file_location("et", "poe2_assets/extract_templates.py")
et = importlib.util.module_from_spec(_et); _et.loader.exec_module(et)

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
CLEAN = "poe2_assets/poe2_tree_clean.json"
FULL = "poe2_assets/poe2_tree.json"

CONSUMERS = {
    "AdditionType":     "src/main/java/com/cimdy/coe/data/codec/item/text/AdditionType.java",
    "StatusBehavior":   "src/main/java/com/cimdy/coe/data/codec/support/status/StatusBehavior.java",
    "BuffStatusTypes":  "src/main/java/com/cimdy/coe/data/codec/support/status/registry/buff/BuffStatusTypes.java",
    "CharmStatusTypes": "src/main/java/com/cimdy/coe/data/codec/support/status/registry/charm/CharmStatusTypes.java",
    "FormStatusTypes":  "src/main/java/com/cimdy/coe/data/codec/support/status/registry/form/FormStatusTypes.java",
    "SkillFactory":     "src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillFactory.java",
    "StatGen":          "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java",
}
REF = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")


def refs_of(path):
    return set(REF.findall(open(path, encoding="utf-8").read()))


def to_name(t):
    s = t.replace("{X}", "X").lower()
    s = re.sub(r"[%+\-\u2022]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w.upper() for w in s.split() if w)


old = m.parse(OLD)
new = m.parse(NEW)
new_by_text = defaultdict(list)
for c, e, cm in new:
    new_by_text[cm].append(c)
new_text = set(new_by_text)

by_file = {k: refs_of(v) for k, v in CONSUMERS.items()}
all_refs = set().union(*by_file.values())
non_statgen = set().union(*(v for k, v in by_file.items() if k != "StatGen"))

clean = json.load(open(CLEAN, encoding="utf-8"))
full = json.load(open(FULL, encoding="utf-8"))

# D1 素材
joined2pieces = {}
for nid, n in clean["nodes"].items():
    for stat in (n.get("stats") or []):
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            pieces = [m.normalize(p) for p in et.split_stat(stat)]
            if len(pieces) > 1:
                joined2pieces.setdefault(m.normalize(" ".join(lines)), pieces)

# 完整语料（含已剔除节点）
full_lines = set()
for nid, n in (full.get("nodes") or {}).items():
    for stat in (n.get("stats") or []):
        for p in et.split_stat(stat):
            full_lines.add(m.normalize(p))
        lines = [l.strip() for l in stat.split("\n") if l.strip()]
        if len(lines) > 1:
            full_lines.add(m.normalize(" ".join(lines)))

# 处置优先级：A > KEEP > D1 > DROP
# KEEP 之所以优先于 D1：D1 的旧注释是「多行块被错误合并成一行」的产物，
# 该注释并不忠实描述任何单条 stat。若这样的常量又被 StatGen 以外的子系统
# （尤其 AdditionType 词缀系统）引用，则该引用表达的是通用语义（如「增加持续伤害」），
# 与天赋节点原文只是巧合同形。此时按注释拆分会毁掉真实依赖，故一律保留。
A, D1, KEEP, DROP = [], [], [], []
for const, eid, comment in old:
    key = m.normalize(comment) if comment else None
    if key and key in new_text:
        A.append((const, eid, key, new_by_text[key][0]))
        continue
    src = "D2 已剔除语料" if (key and key in full_lines) else "D3 自有引擎"
    users = sorted(k for k, v in by_file.items() if const in v)
    if const in non_statgen:
        KEEP.append((const, eid, comment,
                     src + ("（注：注释为多行合并巧合形）" if key in joined2pieces else ""),
                     users))
        continue
    if key and key in joined2pieces:
        D1.append((const, eid, key, joined2pieces[key]))
        continue
    DROP.append((const, eid, comment, src, users))

# 一致性核对
assert len(A) + len(D1) + len(KEEP) + len(DROP) == len(old), "收敛账不闭合"

matched_new = {n for *_, n in A}
for _, _, _, pieces in D1:
    for p in pieces:
        for c in new_by_text.get(p, []):
            matched_new.add(c)
orphan_new = [(c, e, cm) for c, e, cm in new if c not in matched_new]

# ---- 产出 ----
with open("poe2_assets/_test/rename.tsv", "w", encoding="utf-8") as f:
    for const, eid, key, newc in A:
        if const != newc:
            f.write(f"{const}\t{newc}\n")
with open("poe2_assets/_test/keep_consts.txt", "w", encoding="utf-8") as f:
    for const, eid, comment, src, users in sorted(KEEP):
        f.write(f"{const}\n")
with open("poe2_assets/_test/drop_consts.txt", "w", encoding="utf-8") as f:
    for const, eid, comment, src, users in sorted(DROP):
        f.write(f"{const}\n")

same_name = sum(1 for const, _, _, newc in A if const == newc)
keep_by_src = defaultdict(int)
for *_, src, _ in KEEP:
    keep_by_src[src] += 1
drop_by_src = defaultdict(int)
for *_, src, _ in DROP:
    drop_by_src[src] += 1
keep_by_file = defaultdict(int)
for *_, users in KEEP:
    for u in users:
        keep_by_file[u] += 1

with open("poe2_assets/_test/step5_plan.md", "w", encoding="utf-8") as f:
    w = f.write
    w("# 第 5 步 · 引用重构执行计划\n\n")
    w(f"- 旧 StatIds 条目：**{len(old)}**\n")
    w(f"- 新 StatIds 条目：**{len(new)}**\n")
    w(f"- 被任一消费文件引用的旧常量：**{len(all_refs)}**（其中 StatGen 以外 **{len(non_statgen)}**）\n\n")
    w("| 处置 | 数量 | 判据 | 动作 |\n|---|---|---|---|\n")
    w(f"| A 机械改名 | {len(A)} | 注释规范化后与新表逐字符相等 | 全局替换引用（其中 {same_name} 条新旧同名，零改动） |\n")
    w(f"| D1 一对多拆分 | {len(D1)} | 旧注释=多行块合并形，可切 >=2 片 | 逐块定序映射到多个新常量 |\n")
    w(f"| KEEP 保留 | {len(KEEP)} | 被 StatGen 以外的消费文件引用 | 手工通道预填，不随天赋树重建 |\n")
    w(f"| DROP 删除 | {len(DROP)} | 仅 StatGen 引用或完全无引用 | 删常量 + 删 StatGen 实现 |\n")
    w(f"| **合计** | **{len(A)+len(D1)+len(KEEP)+len(DROP)}** | | |\n\n")
    w(f"新表中尚无任何旧实现对应（引擎待实现）：**{len(orphan_new)}**\n\n")
    w("### KEEP 来源构成\n\n| 语料归因 | 数量 |\n|---|---|\n")
    for k, v in sorted(keep_by_src.items()):
        w(f"| {k} | {v} |\n")
    w("\n### KEEP 被哪些文件引用\n\n| 消费文件 | 引用条数 |\n|---|---|\n")
    for k, v in sorted(keep_by_file.items(), key=lambda x: -x[1]):
        w(f"| {k} | {v} |\n")
    w("\n### DROP 来源构成\n\n| 语料归因 | 数量 |\n|---|---|\n")
    for k, v in sorted(drop_by_src.items()):
        w(f"| {k} | {v} |\n")

    w("\n---\n\n## KEEP 明细（保留，进手工通道）\n\n")
    w("| 旧常量 | 旧 id | 注释 | 归因 | 引用方 |\n|---|---|---|---|---|\n")
    for const, eid, comment, src, users in sorted(KEEP):
        w(f"| `{const}` | `{eid}` | {comment} | {src} | {', '.join(users)} |\n")

    w("\n---\n\n## DROP 明细（删除）\n\n")
    w("| 旧常量 | 归因 | 引用方 |\n|---|---|---|\n")
    for const, eid, comment, src, users in sorted(DROP):
        w(f"| `{const}` | {src} | {', '.join(users) or '（无引用）'} |\n")

with open("poe2_assets/_test/d1_blocks.md", "w", encoding="utf-8") as f:
    w = f.write
    w("# D1 一对多拆分 · 待裁决\n\n")
    w(f"共 {len(D1)} 条旧常量，涉及 {len(joined2pieces)} 个多行块中的 "
      f"{len({k for _,_,k,_ in D1})} 个。\n\n")
    groups = defaultdict(list)
    for const, eid, key, pieces in D1:
        groups[key].append((const, eid))
    for key, consts in sorted(groups.items()):
        pieces = joined2pieces[key]
        w(f"### 合并原文\n\n`{key}`\n\n")
        w(f"**旧常量（{len(consts)}）**\n\n")
        for c, e in sorted(consts):
            users = sorted(k for k, v in by_file.items() if c in v)
            w(f"- `{c}`  (id `{e}`)  引用方：{', '.join(users) or '无'}\n")
        w(f"\n**拆分后新常量（{len(pieces)}，按块内出现顺序）**\n\n")
        for i, p in enumerate(pieces, 1):
            hit = new_by_text.get(p)
            w(f"{i}. {'`'+hit[0]+'`' if hit else '**（新表缺失！）**'} — {p}\n")
        w("\n---\n\n")

print(f"旧 {len(old)} / 新 {len(new)} / 引用 {len(all_refs)}（非 StatGen {len(non_statgen)}）")
print(f"A  机械改名     : {len(A)}  （新旧同名 {same_name}，需实改 {len(A)-same_name}）")
print(f"D1 一对多拆分   : {len(D1)}  （{len({k for _,_,k,_ in D1})} 个块）")
print(f"KEEP 保留       : {len(KEEP)}  " + str(dict(keep_by_src)))
print(f"DROP 删除       : {len(DROP)}  " + str(dict(drop_by_src)))
print(f"收敛: {len(A)}+{len(D1)}+{len(KEEP)}+{len(DROP)} = {len(A)+len(D1)+len(KEEP)+len(DROP)} == {len(old)}")
print(f"新表待实现       : {len(orphan_new)}")
