# -*- coding: utf-8 -*-
"""第 2 步：天赋生成文件 Stat 文本备注化。

对 data/gen/talent/node/ 下的 minor/notable/keystone/jewel 生成文件：
- 将 TalentNodeDef.of(...) 的效果实参（List.of(TalentNodeGen.attr(...), ...)）替换为 List.of()
- 在每个节点块前以 // [Stat] 注释写入该节点在 poe2_tree_clean.json 中的原文 stat 文本
- 节点结构（id/坐标/名称/图标/links）、现有手工注释全部保留
- MinorNodesAttr.java（attributeNode 属性三选一节点）不处理
- 改写后清理文件中不再使用的 import

实现：先在原始文本上收集全部替换区间，再按位置倒序应用，避免偏移漂移。
"""
import json
import os
import re

BASE = "src/main/java/com/cimdy/coe/data/gen/talent/node"
CLEAN_JSON = "poe2_assets/poe2_tree_clean.json"
SKIP_FILES = {"MinorNodesAttr.java"}

IMPORT_CANDIDATES = [
    "import com.cimdy.coe.data.codec.support.stat.StatIds;",
    "import com.cimdy.coe.data.gen.stat.StatGen;",
    "import com.cimdy.coe.data.gen.talent.TalentNodeGen;",
    "import net.minecraft.world.entity.ai.attributes.AttributeModifier;",
    "import net.minecraft.world.entity.ai.attributes.Attributes;",
    "import net.minecraft.core.Holder;",
    "import com.cimdy.coe.data.AttributeRegister;",
    "import com.cimdy.coe.data.codec.support.limiter.AttributeLimiters;",
    "import com.mojang.datafixers.util.Either;",
]


def split_top_level_args(text, start, end):
    """在 text[start:end] 内按顶层逗号切分参数，返回各参数 (s,e) 区间列表。"""
    args = []
    depth = 0
    in_str = False
    esc = False
    arg_start = start
    i = start
    while i < end:
        c = text[i]
        if in_str:
            if esc:
                esc = False
            elif c == "\\":
                esc = True
            elif c == '"':
                in_str = False
        else:
            if c == '"':
                in_str = True
            elif c in "([":
                depth += 1
            elif c in ")]":
                depth -= 1
            elif c == "," and depth == 0:
                args.append((arg_start, i))
                arg_start = i + 1
        i += 1
    if arg_start < end:
        args.append((arg_start, end))
    return args


def find_matching_paren(text, open_pos):
    """返回与 open_pos 的 '(' 匹配的 ')' 位置。"""
    depth = 0
    in_str = False
    esc = False
    i = open_pos
    n = len(text)
    while i < n:
        c = text[i]
        if in_str:
            if esc:
                esc = False
            elif c == "\\":
                esc = True
            elif c == '"':
                in_str = False
        else:
            if c == '"':
                in_str = True
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    raise ValueError(f"unbalanced parens at {open_pos}")


def stat_comment_lines(stats, indent):
    """把节点 stats 原文转为注释行列表（多行 stat 用续行）。"""
    lines = []
    for s in stats:
        parts = s.split("\n")
        lines.append(f"{indent}// [Stat] {parts[0]}")
        for cont in parts[1:]:
            lines.append(f"{indent}//        {cont}")
    return lines


def process_file(path, node_stats):
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()

    # 收集替换：(pos, old_len, new_text)；倒序应用
    edits = []
    n_commented = 0
    n_emptied = 0
    pattern = re.compile(r"list\.add\(\s*TalentNodeDef\.of\(")

    for m in pattern.finditer(src):
        of_open = src.index("(", m.start() + len("list.add("))
        of_close = find_matching_paren(src, of_open)
        args = split_top_level_args(src, of_open + 1, of_close)
        if len(args) < 6:
            continue
        id_arg = src[args[0][0]:args[0][1]].strip()
        if not (id_arg.startswith('"') and id_arg.endswith('"')):
            continue
        node_id = id_arg[1:-1]
        if node_id not in node_stats:
            continue
        stats = node_stats[node_id]

        # list.add( 行首与缩进
        line_start = src.rfind("\n", 0, m.start()) + 1
        indent = src[line_start:m.start()]

        # 1) 注释插入（在 list.add 行前）
        if stats:
            comment = "\n".join(stat_comment_lines(stats, indent)) + "\n"
            edits.append((line_start, 0, comment))
            n_commented += 1

        # 2) 效果实参置空（保留实参前导空白，避免紧贴逗号）
        eff_s, eff_e = args[5]
        eff_text = src[eff_s:eff_e].strip()
        if eff_text != "List.of()":
            ws_end = eff_s
            while ws_end < eff_e and src[ws_end] in " \t\n\r":
                ws_end += 1
            edits.append((ws_end, eff_e - ws_end, "List.of()"))
            n_emptied += 1

    if not edits:
        return 0, 0

    edits.sort(key=lambda e: e[0], reverse=True)
    for pos, length, new_text in edits:
        src = src[:pos] + new_text + src[pos + length:]

    # 清理不再使用的 import
    for imp in IMPORT_CANDIDATES:
        if imp in src:
            simple = imp.split()[-1].rstrip(";").split(".")[-1]
            body = src.replace(imp, "", 1)
            if not re.search(rf"\b{simple}\b", body):
                src = body

    # 收敛 import 区残留的连续空行
    src = re.sub(r"\n{3,}", "\n\n", src)

    with open(path, "w", encoding="utf-8") as f:
        f.write(src)
    return n_commented, n_emptied


def main():
    with open(CLEAN_JSON, "r", encoding="utf-8") as f:
        clean = json.load(f)
    node_stats = {nid: n.get("stats", []) for nid, n in clean["nodes"].items()}

    total_c = total_e = 0
    files = []
    for root_dir, _, names in os.walk(BASE):
        for name in sorted(names):
            if name.endswith(".java") and name not in SKIP_FILES:
                files.append(os.path.join(root_dir, name))

    for path in files:
        c, e = process_file(path, node_stats)
        rel = os.path.relpath(path, BASE)
        total_c += c
        total_e += e
        print(f"{rel}: 注释节点 {c}, 置空效果 {e}")

    print(f"\n合计: 注释节点 {total_c}, 置空效果 {total_e}")


if __name__ == "__main__":
    main()
