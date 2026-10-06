# -*- coding: utf-8 -*-
"""把 EssenceFamilies / RuneFamilies 从「逐阶声明」DSL 改写为「按部位逐阶」DSL。

旧：family(...) 内 BonusFamily.builder().tier(branch(k, b...), branch(k, b...))...
新：family(...) 内 BonusFamily.builder().branch(k).tier(b...).tier(b...).build()

全程保留 bonus(...) 原始文本，改写前后用 Counter 多重集比对确保一条不丢。
"""
import re
import sys
import os
from collections import Counter, OrderedDict

BASE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(BASE)

FAMILY_HEAD = re.compile(r'family\(\s*"([^"]+)"\s*,\s*BonusFamily\.builder\(\s*\)')
TIER_OPEN = re.compile(r'\.tier\s*\(')


# ── 词法工具 ────────────────────────────────────────────────────────────
def match_close(s, open_idx):
    """open_idx 指向 '('，返回配对的 ')' 下标。"""
    depth = 0
    i = open_idx
    n = len(s)
    while i < n:
        c = s[i]
        if c == '"':
            i += 1
            while i < n and s[i] != '"':
                if s[i] == '\\':
                    i += 1
                i += 1
        elif c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError('括号不配对 @%d' % open_idx)


def match_str(s, open_idx):
    """open_idx 指向 '('，返回 (args_str, close_idx)。"""
    close = match_close(s, open_idx)
    return s[open_idx + 1:close], close


def split_top(s):
    """按顶层逗号切分（忽略括号 / 字符串内的逗号）。"""
    parts, depth, start, i, n = [], 0, 0, 0, len(s)
    while i < n:
        c = s[i]
        if c == '"':
            i += 1
            while i < n and s[i] != '"':
                if s[i] == '\\':
                    i += 1
                i += 1
        elif c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
        elif c == ',' and depth == 0:
            parts.append(s[start:i].strip())
            start = i + 1
        i += 1
    tail = s[start:].strip()
    if tail:
        parts.append(tail)
    return parts


# ── 解析 / 生成 ─────────────────────────────────────────────────────────
def parse_family(text, from_pos=0):
    """返回 (name, indent, start, end, tiers)；tiers = [ [(key, [bonus...]), ...], ... ]。"""
    head = FAMILY_HEAD.search(text, from_pos)
    if not head:
        raise ValueError('找不到 family(..., BonusFamily.builder())')
    name = head.group(1)
    line_start = text.rfind('\n', 0, head.start()) + 1
    indent = ''
    for ch in text[line_start:]:
        if ch in ' \t':
            indent += ch
        else:
            break
    # 替换区间从行首空白处起（line_start 已指向行首那串空白），
    # emitted 自带 indent，故此处若把 start 推到记号处会与前置空白叠加成双倍缩进。
    start = line_start

    build_at = text.find('.build()', head.end())
    if build_at < 0:
        raise ValueError('%s: 找不到 .build()' % name)

    tiers = []
    pos = head.end()
    while True:
        m = TIER_OPEN.search(text, pos, build_at)
        if not m:
            break
        args_str, _ = match_str(text, m.end() - 1)
        items = []
        for arg in split_top(args_str):
            if not arg.startswith('branch('):
                raise ValueError('%s: .tier 参数非 branch(...) -> %r' % (name, arg[:60]))
            inner, _ = match_str(arg, arg.index('branch(') + len('branch'))
            key_m = re.match(r'^\s*"([^"]*)"', inner)
            if not key_m:
                raise ValueError('%s: branch 缺部位键 -> %r' % (name, arg[:60]))
            # key_m 止于闭合引号，其后紧跟分隔键与 bonus 的逗号 —— 先剥掉再切分。
            rest = inner[key_m.end():].lstrip()
            if rest.startswith(','):
                rest = rest[1:].lstrip()
            bonuses = split_top(rest) if rest else []
            items.append((key_m.group(1), bonuses))
        tiers.append(items)
        pos = m.end()

    return name, indent, start, build_at + len('.build()'), tiers   # start 见上：行首缩进之后


def emit(name, indent, tiers):
    """tier 数 = 该族最高阶号 + 1；某部位缺席某阶时留空 .tier()。"""
    tier_count = len(tiers)
    order = OrderedDict()
    for ti, tier in enumerate(tiers):
        for key, bonuses in tier:
            if key not in order:
                order[key] = [None] * tier_count
            order[key][ti] = bonuses

    lines = ['%sfamily("%s", BonusFamily.builder()' % (indent, name)]
    if not order:
        lines[0] = '%sfamily("%s", BonusFamily.builder().build());' % (indent, name)
        return lines
    for key, per_tier in order.items():
        lines.append('%s.branch("%s")' % (indent + ' ' * 8, key))
        for bonuses in per_tier:
            lines.append('%s.tier(%s)' % (indent + ' ' * 12, ', '.join(bonuses or [])))
    lines.append('%s.build());' % (indent + ' ' * 8))
    return lines


# ── 主流程 ──────────────────────────────────────────────────────────────
def convert(path):
    with open(path, 'r', encoding='utf-8') as f:
        text = f.read()

    before = Counter(re.findall(r'\bbonus\(', text))

    # 先把全部 family 块解析完（解析区间 [start,end) 互不重叠，且 end 后仅剩闭合的 ')'），
    # 再一次性拼接替换 —— 避免「边改边找」时区间漂移或死循环。
    edits, cursor = [], 0
    while cursor < len(text):
        try:
            name, indent, start, end, tiers = parse_family(text, cursor)
        except ValueError:
            break                                # 后面已无 family 块
        if start < cursor:
            raise ValueError('解析区间重叠，中止')
        edits.append((start, end, emit(name, indent, tiers)))
        cursor = end + 2                        # = 替换后该 family 语句的下一个位置

    pieces, prev = [], 0
    for start, end, lines in edits:
        pieces.append(text[prev:start])
        pieces.append('\n'.join(lines))
        prev = end + 2
    pieces.append(text[prev:])
    out = ''.join(pieces)

    out = out.replace('import static com.cimdy.coe.data.codec.item.tiered.BonusFamily.branch;\n', '')

    after = Counter(re.findall(r'\bbonus\(', out))
    if before != after:
        print('  !! bonus 多重集不一致: %d -> %d' % (sum(before.values()), sum(after.values())))
        for k in before - after:
            print('     丢失:', k)
        for k in after - before:
            print('     新增:', k)
        return False
    if re.search(r'(?<![\w.])branch\(', out):
        print('  !! 仍残留 branch(')
        return False

    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(out)
    n = len(re.findall(r'^        family\(', out, re.M))
    print('  OK %s  family=%d  bonus=%d' % (os.path.basename(path), n, sum(after.values())))
    return True


if __name__ == '__main__':
    targets = sys.argv[1:] or [
        os.path.join('src', 'main', 'java', 'com', 'cimdy', 'coe', 'data', 'gen', 'essence', 'EssenceFamilies.java'),
        os.path.join('src', 'main', 'java', 'com', 'cimdy', 'coe', 'data', 'gen', 'rune', 'RuneFamilies.java'),
    ]
    for rel in targets:
        p = rel if os.path.isabs(rel) else os.path.join(ROOT, rel)
        print('转换', os.path.basename(p))
        if not convert(p):
            sys.exit(1)
    print('全部完成')
