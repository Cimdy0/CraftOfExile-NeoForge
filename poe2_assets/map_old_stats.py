# -*- coding: utf-8 -*-
"""第 5 步（1/2）：建立旧 StatIds 常量 -> 新 StatIds 常量的**精确**映射表。

原则（用户要求）：不做模糊近似匹配。只有旧注释经与第 3/4 步**同源**的规范化管道处理后
与新模板逐字符相等，才判定为映射；其余全部进入失配清单，逐条列出供人工裁决。

规范化管道（与 extract_templates.py + gen_stat_ids.py 完全同源）：
  [Tag|Text] 取词丰侧 -> 换行转空格 -> 极性归一 -> 数字归一 {X}
  -> CamelCase 粘连词拆分 -> MC 术语替换（大小写不敏感）-> 空白合并

输出 poe2_assets/_test/stat_mapping.md：
  §A 自动映射（旧 -> 新，精确命中）
  §B 失配且**被代码引用**（必须人工裁决，否则编译不过）
  §C 失配且无引用（可直接废弃）
  §D 新条目中尚无任何旧实现对应（引擎待实现）
"""
import re
from collections import defaultdict

OLD = "poe2_assets/_backup/stat/StatIds.java.old"
NEW = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
REFS = "poe2_assets/_test/referenced_consts.txt"
OUT = "poe2_assets/_test/stat_mapping.md"

POL = {
    "increase": "increased", "increased": "increased", "increases": "increased", "increasing": "increased",
    "more": "more",
    "add": "add", "adds": "add", "added": "add", "adding": "add",
    "gain": "gain", "gains": "gain", "gained": "gain",
    "reduce": "increased", "reduced": "increased", "reduces": "increased", "reducing": "increased",
    "less": "more",
    "remove": "add", "removes": "add", "removed": "add", "removing": "add",
    "lose": "gain", "loses": "gain", "lost": "gain", "losing": "gain",
}

REPLACEMENTS = [
    ("Accuracy Rating", "Accuracy"),
    ("Evasion Rating", "Evasion"),
    ("Armour", "Armor"),
    ("Life", "Health"),
    ("Maximum", "Max"),
]

GLUE = re.compile(r"\b[A-Z][a-z]+(?:[A-Z][a-z]+)+\b")


def resolve_tags(s):
    def repl(m):
        inner = m.group(1)
        if "|" in inner:
            left, right = inner.split("|", 1)
            if len(right.split()) > len(left.split()):
                return right
            if len(left.split()) > len(right.split()):
                return left
            return left if len(left) >= len(right) else right
        return inner
    return re.sub(r"\[([^\]]*)\]", repl, s)


def normalize_polarity(s):
    return re.sub(r"[A-Za-z]+", lambda m: POL.get(m.group(0).lower(), m.group(0)), s)


def replace_numbers(s):
    return re.sub(r"([-+]?)\d+\.?\d*", lambda m: (m.group(1) or "") + "{X}", s)


def split_glue(t):
    return GLUE.sub(lambda m: re.sub(r"(?<=[a-z])(?=[A-Z])", " ", m.group(0)), t)


def mc_replace(t):
    for a, b in REPLACEMENTS:
        t = re.sub(re.escape(a), b, t, flags=re.IGNORECASE)
    return t


def normalize(stat):
    s = resolve_tags(stat)
    s = s.replace("\n", " ")
    s = normalize_polarity(s)
    s = replace_numbers(s)
    s = mc_replace(split_glue(s))
    return re.sub(r"\s+", " ", s).strip()


ENTRY = re.compile(r'public static final Identifier ([A-Z][A-Z0-9_]*) = id\("([a-z0-9_]+)"\);')


def parse(path):
    """解析 StatIds.java -> [(const, id, comment)]，注释取紧邻上一行的 // 内容。"""
    out = []
    pending = None
    for line in open(path, encoding="utf-8"):
        s = line.strip()
        if s.startswith("//"):
            pending = s[2:].strip()
            continue
        m = ENTRY.search(line)
        if m:
            out.append((m.group(1), m.group(2), pending or ""))
            pending = None
        elif s and not s.startswith("*") and not s.startswith("/*"):
            pending = None
    return out


def main():
    old = parse(OLD)
    new = parse(NEW)
    refs = {l.strip() for l in open(REFS, encoding="utf-8") if l.strip()}

    # 新条目：规范化模板（=注释原文，已是规范化产物）-> 常量名
    new_by_text = defaultdict(list)
    for const, eid, comment in new:
        new_by_text[comment].append(const)

    auto, unmatched = [], []
    for const, eid, comment in old:
        if not comment:
            unmatched.append((const, eid, comment, "无注释"))
            continue
        key = normalize(comment)
        hits = new_by_text.get(key)
        if hits:
            auto.append((const, eid, comment, key, hits))
        else:
            unmatched.append((const, eid, comment, key))

    matched_new = {h for *_, hits in auto for h in hits}
    orphan_new = [(c, e, cm) for c, e, cm in new if c not in matched_new]

    ref_unmatched = [u for u in unmatched if u[0] in refs]
    noref_unmatched = [u for u in unmatched if u[0] not in refs]

    with open(OUT, "w", encoding="utf-8") as f:
        w = f.write
        w("# 旧 StatIds -> 新 StatIds 精确映射报告\n\n")
        w(f"- 旧条目总数：**{len(old)}**\n")
        w(f"- 新条目总数：**{len(new)}**\n")
        w(f"- 被代码引用的旧常量：**{len(refs)}**\n\n")
        w("| 分类 | 数量 | 处置 |\n|---|---|---|\n")
        w(f"| A 精确命中（可自动改名） | {len(auto)} | 直接替换引用 |\n")
        w(f"| B 失配且被引用 | {len(ref_unmatched)} | **须人工裁决** |\n")
        w(f"| C 失配且无引用 | {len(noref_unmatched)} | 可直接废弃 |\n")
        w(f"| D 新条目无旧实现 | {len(orphan_new)} | 引擎待实现 |\n\n")

        w("---\n\n## §B 失配且被代码引用（必须裁决，否则编译不过）\n\n")
        w("| 旧常量 | 旧 id | 旧注释 | 规范化后（新表中无此模板） |\n|---|---|---|---|\n")
        for const, eid, comment, key in sorted(ref_unmatched):
            w(f"| `{const}` | `{eid}` | {comment} | {key} |\n")

        w("\n---\n\n## §A 精确命中（自动映射）\n\n")
        w("| 旧常量 | 新常量 | 模板 |\n|---|---|---|\n")
        for const, eid, comment, key, hits in sorted(auto):
            w(f"| `{const}` | `{hits[0]}` | {key} |\n")

        w("\n---\n\n## §C 失配且无引用（可废弃）\n\n")
        for const, eid, comment, key in sorted(noref_unmatched):
            w(f"- `{const}` — {comment}\n")

        w("\n---\n\n## §D 新条目尚无旧实现（引擎待实现）\n\n")
        for const, eid, comment in orphan_new:
            w(f"- `{const}` — {comment}\n")

    print(f"旧条目 {len(old)} / 新条目 {len(new)} / 引用 {len(refs)}")
    print(f"A 精确命中     : {len(auto)}")
    print(f"B 失配且被引用 : {len(ref_unmatched)}   <-- 须人工裁决")
    print(f"C 失配无引用   : {len(noref_unmatched)}")
    print(f"D 新条目无实现 : {len(orphan_new)}")
    print(f"\n报告: {OUT}")

    # 落一份机器可读映射，供第 5 步(2/2) 自动改名使用
    with open("poe2_assets/_test/auto_rename.tsv", "w", encoding="utf-8") as f:
        for const, eid, comment, key, hits in auto:
            f.write(f"{const}\t{hits[0]}\n")


if __name__ == "__main__":
    main()
