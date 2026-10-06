# -*- coding: utf-8 -*-
"""第 3 步：从清洗后的天赋树抽取并规范化 Stat 模板。

对每条 stat 文本：
0. 先按「换行语义」把一个 stat 块切成若干独立 stat（见 split_stat）。
   POE 语料里的 \n 有两种含义，必须区分，否则要么把多条 stat 拼成怪胎，要么把一条 stat 切碎：
     - 真分隔：一个节点挂多条独立 stat
     - 软折行：POE 为 UI 渲染宽度插入的换行（同一条 stat 被断成两行）
1. [TagId|DisplayText] -> 取单词量更多一侧；平手时优先「非缩写粘连」的展示文本侧
   （见 gi.pick_tag_side —— 平手按字符长度会取到标签 id，把 ESRecharge 之类烤进 path，即 D2）
2. 极性归一：reduced->increased / less->more / remove|removed->add / lose->gain
   （负向翻正，统一小写规范形，便于去重合并 +/- 对）
3. 数字（含带符号、小数、N to M 区间）-> {X}，保留 + - % to 等连接符；拼写数字不动
4. 合并空白、去重、排序 -> poe2_assets/stat_templates.txt
"""
import json, re
import os
import sys
from collections import deque

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_stat_ids as gi

# 不能作为一条 stat 结尾的虚词（闭合词表，非相似度判定）。
# 若上一行渲染后以这些词悬空结尾，则下一行必为软折行续行。
DANGLE = {
    "up to", "to", "and", "or", "of", "per", "the", "a", "an", "with", "for",
    "from", "by", "have", "has", "had", "as", "than", "while", "when", "if",
    "that", "at", "in", "on", "into", "you", "your", "their", "is", "are",
    "be", "been", "also", "each", "every", "other",
}

CLEAN_JSON = "poe2_assets/poe2_tree_clean.json"
OUT = "poe2_assets/stat_templates.txt"

POL = {
    # 保持正向规范形
    "increase": "increased", "increased": "increased", "increases": "increased", "increasing": "increased",
    "more": "more",
    "add": "add", "adds": "add", "added": "add", "adding": "add",
    "gain": "gain", "gains": "gain", "gained": "gain",
    # 负向翻正
    "reduce": "increased", "reduced": "increased", "reduces": "increased", "reducing": "increased",
    "less": "more",
    "remove": "add", "removes": "add", "removed": "add", "removing": "add",
    "lose": "gain", "loses": "gain", "lost": "gain", "losing": "gain",
}


def resolve_tags(s):
    # 取词规则统一在 gen_stat_ids.pick_tag_side（与 resolve_node_stats 同一份，防 D2 复发）
    return re.sub(r"\[([^\]]*)\]", lambda m: gi.pick_tag_side(m.group(1)), s)


def normalize_polarity(s):
    return re.sub(r"[A-Za-z]+",
                  lambda m: POL.get(m.group(0).lower(), m.group(0)), s)


def replace_numbers(s):
    return re.sub(r"([-+]?)\d+\.?\d*",
                  lambda m: (m.group(1) if m.group(1) else "") + "{X}", s)


def split_stat(stat):
    """把一个 stat 块按换行语义切成若干独立 stat。

    判据（三条，全确定性，无相似度/无近似）：
      1. 行首为小写字母 -> 软折行续行，合并到上一条
         （一条 stat 绝不会以小写单词开头，此为语料硬事实）
      2. 行首为 '•'     -> 项目符号子句，合并到上一条
         （形如「Storm and Plant Spells:」+「• deal 50% more damage」，属同一条逻辑 stat）
         【口径已变更】节点映射侧（resolve_node_stats，D1）改为「每个 • 子项独立成条，
         共享头部拼进各子项、类别作用域记在 record.scope」。本语料侧暂保持合并，
         下次重生成 stat_templates.txt 时应同步为拆分口径，否则 D1 拆出的模板在语料里查不到
         （resolve_node_stats 已对未收录模板回落到规范 id，不会丢 path，但语料不覆盖）。
      3. 上一行渲染后以 DANGLE 虚词悬空结尾 -> 续行，合并到上一条
         （如「...travel farther, up to」+「40% increased ... after 7 metres」）
      其余 -> 独立 stat，另起一条
    """
    out = []
    for raw in stat.split("\n"):
        line = raw.strip()
        if not line:
            continue
        cont = line[0].islower() or line[0] == "\u2022"
        if not cont and out:
            tail = resolve_tags(out[-1]).rstrip().rstrip(",").split()
            if tail:
                last = tail[-1].lower()
                two = " ".join(w.lower() for w in tail[-2:])
                if last in DANGLE or two in DANGLE:
                    cont = True
        if cont and out:
            out[-1] = out[-1] + " " + line
        else:
            out.append(line)
    return out


def normalize(stat):
    s = resolve_tags(stat)
    s = normalize_polarity(s)
    s = replace_numbers(s)
    s = re.sub(r"\s+", " ", s).strip()
    return s


def main():
    with open(CLEAN_JSON, "r", encoding="utf-8") as f:
        clean = json.load(f)
    nodes = clean["nodes"]

    visited = set()
    q = deque(["root"])
    while q:
        nid = q.popleft()
        if nid in visited:
            continue
        visited.add(nid)
        n = nodes.get(nid)
        if n:
            for o in n.get("out", []) + n.get("in", []):
                if o not in visited:
                    q.append(o)

    templates = set()
    raw_count = 0
    for nid, n in nodes.items():
        if nid in visited and not n.get("ascendancyId") and not n.get("isMastery"):
            for stat in n.get("stats", []):
                # 按换行语义切分（区分真分隔与软折行），再各自归一
                for piece in split_stat(stat):
                    raw_count += 1
                    templates.add(normalize(piece))

    templates = sorted(templates)
    with open(OUT, "w", encoding="utf-8") as f:
        for t in templates:
            f.write(t + "\n")

    print(f"raw stat lines: {raw_count}")
    print(f"unique raw (before norm): check skipped")
    print(f"normalized unique templates: {len(templates)}")
    print(f"written to {OUT}")
    print("\n--- sample (first 25) ---")
    for t in templates[:25]:
        print("  " + t)


if __name__ == "__main__":
    main()
