# -*- coding: utf-8 -*-
"""C 阶段工具：节点 stat 描述 -> StatIds + 提值 的系统性解析器。

对应需求：节点内保留 Stat 描述字段，构建「描述字段 -> Ids」映射，同时抽取数值；
而非逐节点手写接线。

流水线（与 extract_templates.py 同款 normalize，保证描述 -> corpus 模板一致）：
  1. split_stat      : 区分真分隔 / 软折行，切成独立 stat 片。
  2. normalize       : resolve_tags + 极性归一 + 数字 -> {X}。
  3. 模板 -> StatIds : corpus 模板经 to_id 得 path，回查 StatIds.java 取常量名。
  4. 提值            : 从原始 stat 片抽取数值 token（含 % / 符号 / to 区间），输出原始值与归一分数。

输出：
  - node_stat_map.json : {node_id: [{raw, template, path, constant, values:[...]}]}
  - 打印：覆盖率、已/未落 entry 计数、930 待生成模板的频率 Top-N。
"""
import json
import os
import re
import shutil
import sys
from collections import deque, Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_stat_ids as gi  # 复用其二级归一 normalize()

CLEAN_JSON = "poe2_assets/poe2_tree_clean.json"
CORPUS = "poe2_assets/stat_templates.txt"
STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
ENTRY_DIR = "src/generated/resources/data/coe/coe/stat/entry"
OUT_MAP = "poe2_assets/node_stat_map.json"
BAK_MAP = "poe2_assets/node_stat_map.bak.json"
OUT_TEXT_INDEX = "poe2_assets/stat_text_index.json"

DANGLE = {"up to", "to", "and", "or", "of", "per", "the", "a", "an", "with", "for",
          "from", "by", "have", "has", "had", "as", "than", "while", "when", "if",
          "that", "at", "in", "on", "into", "you", "your", "their", "is", "are",
          "be", "been", "also", "each", "every", "other"}
POL = {"increase": "increased", "increased": "increased", "increases": "increased", "increasing": "increased",
       "more": "more", "add": "add", "adds": "add", "added": "add", "adding": "add", "gain": "gain",
       "gains": "gain", "gained": "gain", "reduce": "increased", "reduced": "increased", "reduces": "increased",
       "reducing": "increased", "less": "more", "remove": "add", "removes": "add", "removed": "add",
       "removing": "add", "lose": "gain", "loses": "gain", "lost": "gain", "losing": "gain"}


def resolve_tags(s):
    # 取词规则统一在 gen_stat_ids.pick_tag_side（语料侧 extract_templates 复用同一份）
    return re.sub(r"\[([^\]]*)\]", lambda m: gi.pick_tag_side(m.group(1)), s)


def normalize_polarity(s):
    return re.sub(r"[A-Za-z]+", lambda m: POL.get(m.group(0).lower(), m.group(0)), s)


def replace_numbers(s):
    return re.sub(r"([-+]?)\d+\.?\d*", lambda m: (m.group(1) if m.group(1) else "") + "{X}", s)


def normalize(stat):
    s = resolve_tags(stat)
    s = normalize_polarity(s)
    s = replace_numbers(s)
    return re.sub(r"\s+", " ", s).strip()


BULLET = "•"
# 行内项目符号：行首或空白后的 '•'（连同其后空白）作为切点
BULLET_SEP = re.compile(r"(?:^|\s)" + BULLET + r"\s*")

# 并列连词：用于从共享头部抽取类别作用域（A and/or B <中心词>）
COORD = {"and", "or"}


def detect_scope(text):
    """从「A and/or B <中心词>:」式共享头部抽取 *类别作用域*（D1）。

    作用域（storm-or-plant / chaos-or-bleed）按 **条件** 记录在 record.scope 上，
    绝不 fork 出多个 id（一条描述文本 <-> 一个 StatId）。
    无并列头部时返回 None（该字段为可选字段，不写出）。
    """
    head = re.split(r"[:" + BULLET + r"]", resolve_tags(text), maxsplit=1)[0].strip()
    w = head.split()
    if len(w) < 4:
        return None
    members = [w[0]]
    i = 1
    while i + 1 < len(w) and w[i].lower() in COORD:
        members.append(w[i + 1])
        i += 2
    # 需要有中心词（否则只是列举，不是作用域）、且成员互不重复
    if len(members) < 2 or i >= len(w):
        return None
    if len(set(m.lower() for m in members)) != len(members):
        return None
    return {"any_of": [m.lower() for m in members]}


def _split_bullets(line):
    """-> (项目符号前文本, [项目符号子项...])"""
    parts = BULLET_SEP.split(line)
    return parts[0].strip(), [p.strip() for p in parts[1:] if p.strip()]


def split_stat(stat, detailed=False):
    """把一个 stat 块切成若干独立 stat 片。

    判据（确定性，无相似度/无近似）：
      1. '•' 项目符号（D1）：每个子项是 **独立 mod**，各自成条。
         共享头部（"Storm and Plant Spells:" / "... damage have:"）不单独成条，
         而是原样拼进每个子项前面，使 path 自解释、且不靠节点名补语义。
         类别作用域另记在 record.scope 上，不 fork id。
      2. 行首为小写字母 -> 软折行续行，合并到上一条
         （一条 stat 绝不会以小写单词开头，此为语料硬事实）
      3. 上一行渲染后以 DANGLE 虚词悬空结尾 -> 续行，合并到上一条
         （如「...travel farther, up to」+「40% increased ... after 7 metres」）
      其余 -> 独立 stat，另起一条

    detailed=True 时返回 [(piece, is_bullet_child), ...]，供调用方标注 scope。
    """
    out = []
    flags = []           # 与 out 平行：该片是否由项目符号子项生成（D1）
    header = None        # 当前项目符号组的共享头部（原文，未解标签）
    pending = None       # 尚未定稿的非项目符号片段（可能成为下组的头部）

    def commit(text, is_child=False):
        """按判据 2/3 把一条片段并入 out。"""
        cont = text[0].islower()
        if not cont and out:
            tail = resolve_tags(out[-1]).rstrip().rstrip(",").split()
            if tail:
                last = tail[-1].lower()
                two = " ".join(w.lower() for w in tail[-2:])
                if last in DANGLE or two in DANGLE:
                    cont = True
        if cont and out:
            out[-1] = out[-1] + " " + text
            flags[-1] = flags[-1] or is_child
        else:
            out.append(text)
            flags.append(is_child)

    for raw in stat.split("\n"):
        line = raw.strip()
        if not line:
            continue
        pre, bullets = _split_bullets(line)
        if pre:
            if pending is not None:
                # 上一个非项目符号片段后面没有子项 -> 它是普通 stat，正常定稿
                commit(pending)
                header = None
            pending = pre
        for b in bullets:
            if pending is not None:
                header = pending      # 共享头部被子项吸收，不单独成条
                pending = None
            commit((header + " " + b) if header else b, is_child=True)
    if pending is not None:
        commit(pending)
    if not detailed:
        return out
    return list(zip(out, flags))


def to_id(t):
    s = t.replace("{X}", "x").lower()
    s = re.sub(r"[%+\-•]", " ", s)
    s = re.sub(r"[^a-z0-9 ]", " ", s)
    return "_".join(w for w in s.split() if w and w != "x")


# ---- 抽取数值：返回 [{raw:"16%", value:0.16, unit:"%"}] ----
NUM = re.compile(r"([-+]?\d+\.?\d*)\s*(%|to)?")



NEG_WORDS = r"(?:reduced|reduce|reduces|less|slower|lower|decreased|fewer|removed|removes|lose|loses|lost)"
POS_WORDS = r"(?:increased|increase|increases|more|faster|greater|higher|additional|gained|gain|adds|added)"
# Withdrawn 2026-08-29: an earlier DOUBLE_NEG table tried to skip negation for
# "reduced penalty" / "reduced cost" / "reduced X taken" on the theory that they are
# BENEFITS. That conflates *semantic benefit* with *attribute direction*: after
# normalisation the StatId already reads INC_<X> ("X increased"), so a corpus line
# saying "reduced X" must contribute a NEGATIVE amount. Skipping negation inverted
# them (e.g. "reduced damage taken" became +0.3 instead of -0.3).
# Only genuinely negated phrasings -- where the StatId itself encodes the reduction
# (RED_*/..._REDUCTION) -- would need special handling, and no such case exists yet.
DOUBLE_NEG = [
]


def classify(raw):
    """Polarity of a corpus row: POS / NEG / AMBIGUOUS / NONE."""
    low = raw.lower()
    for pat in DOUBLE_NEG:
        if re.search(pat, low):
            return "AMBIGUOUS"
    has_neg = re.search(r"\b" + NEG_WORDS + r"\b", low) is not None
    has_pos = re.search(r"\b" + POS_WORDS + r"\b", low) is not None
    if has_neg and has_pos: return "AMBIGUOUS"
    if has_neg: return "NEG"
    if has_pos: return "POS"
    return "NONE"

def extract_values(piece, negate=False):
    sign = -1.0 if negate else 1.0
    out = []
    for m in NUM.finditer(piece):
        num = float(m.group(1))
        unit = m.group(2)
        norm = num / 100.0 if unit == "%" else num
        out.append({"raw": (m.group(1) + (unit or "")), "value": sign * norm, "unit": unit or ""})
    return out


def load_statids_comment_map():
    """comment(=归一化模板文本) -> (常量名, path)。

    gen_stat_ids 在碰撞时会给 path 与常量名追加 _N 后缀，故不能由 to_id 反推；
    直接解析 StatIds.java：语料条目的注释就是归一化模板文本（与 normalize() 同款），
    逐行配对 `// <template>` 与其下的 `Identifier NAME = id("PATH")`。
    """
    cmap = {}
    if not os.path.isfile(STATIDS):
        return cmap
    txt = open(STATIDS, encoding="utf-8").read()
    lines = txt.splitlines()
    for i, line in enumerate(lines):
        m = re.match(r"\s*//\s*(.*?)\s*$", line)
        if not m:
            continue
        nxt = lines[i + 1] if i + 1 < len(lines) else ""
        fm = re.match(r"\s*public static final Identifier (\w+) = id\(\"([^\"]+)\"\)", nxt)
        if fm:
            cmap[m.group(1)] = (fm.group(1), fm.group(2))
    return cmap


def load_statids_path_map():
    """path -> 常量名（按真实 path 反查，含 _N 后缀者原样收录）。

    语料/Java 尚未收录的新条目（如 D1 拆出的项目符号子项）在此查不到，
    由调用方回落到 gen_stat_ids.sanitize(to_id(...))——即语料阶段会生成的名字。
    """
    pmap = {}
    if not os.path.isfile(STATIDS):
        return pmap
    for line in open(STATIDS, encoding="utf-8"):
        m = re.match(r'\s*public static final Identifier (\w+) = id\("([^"]+)"\)', line)
        if m:
            pmap.setdefault(m.group(2), m.group(1))
    return pmap


# Windows 文件名上限：len(path) + len(".json") <= 255，超长会在 datagen 静默失败
MAX_PATH_LEN = 255 - len(".json")


def main():
    tree = json.load(open(CLEAN_JSON, encoding="utf-8"))
    nodes = tree["nodes"]
    corpus = set(l.strip() for l in open(CORPUS, encoding="utf-8") if l.strip())
    # StatIds.java 注释 = gen_stat_ids.normalize(corpus行)；comment -> (常量名, 真实path含_N后缀)
    cm = load_statids_comment_map()
    pm = load_statids_path_map()

    # ---- 备份 + 取旧表用于 diff ----
    if os.path.isfile(OUT_MAP) and not os.path.isfile(BAK_MAP):
        shutil.copyfile(OUT_MAP, BAK_MAP)
        print(f"backup -> {BAK_MAP}")
    old_map = {}
    if os.path.isfile(BAK_MAP) and not os.path.isfile(OUT_MAP):
        old_map = json.load(open(BAK_MAP, encoding="utf-8"))
    elif os.path.isfile(OUT_MAP):
        old_map = json.load(open(OUT_MAP, encoding="utf-8"))
    old_index = {(nid, r.get("raw")): r.get("path")
                 for nid, recs in old_map.items() for r in recs}

    node_map = {}
    total = 0
    matched = 0
    with_entry = 0
    tpl_freq = Counter()          # 需要生成 entry 的模板频率
    tpl_constant = {}             # 模板 -> 常量名（用于后续 entry 生成）
    scope_count = 0
    too_long = []
    for nid, n in nodes.items():
        st = n.get("stats")
        if not isinstance(st, list):
            continue
        recs = []
        for s in st:
            if not s or not s.strip():
                continue
            for piece, is_child in split_stat(s, detailed=True):
                total += 1
                key = normalize(piece)
                pol = classify(piece)            # = stat_templates 行形式
                if key in corpus:
                    matched += 1
                g = gi.normalize(key)             # = StatIds.java 注释形式（二级归一）
                canon = gi.to_id(g)               # 生成器算出的规范 id（含 D3 折叠）
                np_ = cm.get(g)                   # (常量名, 真实 path)
                actual_path = np_[1] if np_ else None
                constant = np_[0] if np_ else None
                # D3：Java 仍停留在折叠前旧 id（== 未折叠 to_id）-> 采用修正后的 canonical
                if actual_path is not None and actual_path == gi.to_id_uncollapsed(g) \
                        and actual_path != canon:
                    actual_path = canon
                    constant = pm.get(canon) or gi.sanitize(canon)
                if actual_path is None:
                    # 语料/Java 未收录（D1 拆出的子项等）：用规范 id，常量名取
                    # gen_stat_ids 语料阶段会生成的名字
                    actual_path = canon
                    constant = pm.get(canon) or gi.sanitize(canon)
                if len(actual_path) > MAX_PATH_LEN:
                    too_long.append((nid, actual_path))
                has = os.path.isfile(os.path.join(ENTRY_DIR, actual_path + ".json"))
                if has:
                    with_entry += 1
                else:
                    tpl_freq[key] += 1
                    tpl_constant.setdefault(key, constant)
                rec = {
                    "raw": piece,
                    "template": key,
                    "path": actual_path,
                    "constant": constant,
                    "has_entry": has,
                    # 原始英文描述始终随行保留：Java 侧后续以注释形式回填靠它（D4）
                    "values": extract_values(piece, negate=(pol == "NEG")),
                }
                if is_child:
                    sc = detect_scope(piece)
                    if sc:
                        rec["scope"] = sc        # 类别作用域作条件记录，不 fork id
                        scope_count += 1
                recs.append(rec)
        if recs:
            node_map[nid] = recs

    json.dump(node_map, open(OUT_MAP, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    # ---- 描述文本索引：path -> {raw, template, nodes} ----
    text_index = {}
    for nid, recs in node_map.items():
        for r in recs:
            p = r.get("path")
            if not p:
                continue
            e = text_index.setdefault(p, {"raw": r["raw"], "template": r["template"], "nodes": []})
            if nid not in e["nodes"]:
                e["nodes"].append(nid)
    json.dump(text_index, open(OUT_TEXT_INDEX, "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)

    print(f"total node-stat strings      : {total}")
    print(f"matched to corpus(Ids)       : {matched} ({100*matched/total:.1f}%)")
    print(f"  -> has $value entry        : {with_entry} ({100*with_entry/total:.1f}%)  [可直接引用]")
    print(f"  -> 需生成 $value entry      : {matched-with_entry} ({100*(matched-with_entry)/total:.1f}%)")
    print(f"unique 待生成模板            : {len(tpl_freq)}")
    print(f"node_stat_map.json written   : {len(node_map)} nodes")
    print(f"stat_text_index.json written : {len(text_index)} paths")
    print(f"records with scope           : {scope_count}")
    if too_long:
        print(f"!! path 超 {MAX_PATH_LEN}+5 上限（datagen 会静默失败）: {len(too_long)}")
        for nid, p in too_long[:10]:
            print(f"   {nid} {len(p)} {p}")

    # ---- before/after diff ----
    new_index = {(nid, r["raw"]): r.get("path")
                 for nid, recs in node_map.items() for r in recs}
    changed = Counter((old_index[k], new_index[k])
                      for k in new_index if k in old_index and old_index[k] != new_index[k])
    added = [k for k in new_index if k not in old_index]
    gone = [k for k in old_index if k not in new_index]
    sample = {}
    for k in new_index:
        if k in old_index and old_index[k] != new_index[k]:
            sample.setdefault((old_index[k], new_index[k]), k[0])
    print(f"\n--- diff: {sum(changed.values())} changed / {len(added)} added / {len(gone)} removed ---")
    # 旧 path 为 None 的一批：语料/Java 未收录，现按规范 id 补齐，逐条列新 path
    for (o, nv), c in changed.most_common():
        if o is None:
            print(f"  {c:3d}x  (unmatched) -> {nv}   [{sample[(o, nv)]}]")
    for (o, nv), c in changed.most_common():
        if o is not None:
            print(f"  {c:3d}x  {o} -> {nv}   [{sample[(o, nv)]}]")
    for nid, raw in added:
        print(f"  + [{nid}] {new_index[(nid, raw)]}")
        print(f"      raw: {raw[:100]}")
    for nid, raw in gone:
        print(f"  - [{nid}] {old_index[(nid, raw)]}")
        print(f"      raw: {raw[:100]}")

    print("\n--- 待生成模板 频率 Top 30（按节点出现次数） ---")
    for tpl, cnt in tpl_freq.most_common(30):
        print(f"  {cnt:4d}x  {tpl}   [{tpl_constant.get(tpl,'???')}]")


if __name__ == "__main__":
    main()
