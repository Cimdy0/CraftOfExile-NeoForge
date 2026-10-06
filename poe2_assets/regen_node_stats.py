#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""一次性把全部天赋节点（keystone/notable/minor）的第 6 参 StatList 从 node_stat_map.json 重生成。

设计（回应「为何不整个数据生成类都重写」）：
  - 一条脚本、单遍处理所有节点类，不逐节点 / 不逐个 StatId 手改。
  - 每个节点的 StatList 完全由 node_stat_map.json（权威语料 → StatId 对照表）重新推导，
    再按 Wire 接线口径决定「无值授予」还是「带值授予」：
      * BAKED  ：Wire 中 `StatIds.X` 的接线写了显式 `.amount("常量")`（不含 $value/$per/$cap）
                 → 条目 amount 已是常量，节点用 `TalentNodeGen.stat(StatIds.X)` 无值授予（消灭冗余 1.0 重注）。
      * MULTI_VAR：`.amount(...)` 含 `$per`/`$cap` → 单值无法表达，跳过（不写，节点保持空，与既有行为一致）。
      * TEMPLATE ：其余（无 `.amount` / `.amount("$value...")` / `.amount(per(...))` / `.param("value",...)`）
                 → 条目 amount 为 `$value` 模板，节点用 `TalentNodeGen.stat(Map.of(StatIds.X, value))` 带值授予。
  - 安全网：对每个节点，重算出的 StatId 集合必须 ⊇ 当前节点已授予的 StatId 集合，否则保留原样并告警
    （绝不静默丢弃已接线 stat，防 node_stat_map 遗漏导致的数据丢失）。
  - 不读 src/generated 任何 JSON：BAKED/MULTI 判定完全来自 Wire*.java 源码；守卫用「StatIds 常量存在
    且已在 Wire 中 map.put（= 运行期确有 entry）」，符合「以 Java 源码为准」铁律。

用法：
  python regen_node_stats.py            # DRY-RUN（只打印将要改动的文件/节点数/安全告警，不写盘）
  python regen_node_stats.py --apply    # 写盘并重生成
"""
import os
import re
import sys
import json
import glob
import shutil
from datetime import datetime

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
NODEDIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/talent/node")
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIREDIR = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/wirings")
MAP = os.path.join(ROOT, "poe2_assets/node_stat_map.json")

REQUIRED_IMPORTS = [
    "import com.cimdy.coe.data.codec.support.stat.StatIds;",
    "import com.cimdy.coe.data.gen.talent.TalentNodeGen;",
    "import java.util.Map;",
]
ANCESTRAL_CONFLICT = "THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED"

APPLY = "--apply" in sys.argv


# ---------------------------------------------------------------------------
# 加载：StatIds 常量表、node_stat_map、Wire 接线口径
# ---------------------------------------------------------------------------
def load_catalog():
    txt = open(STATIDS, encoding="utf-8").read()
    const_by_path = {p: n for n, p in
                     re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\)', txt)}
    nmap = json.load(open(MAP, encoding="utf-8"))
    return const_by_path, nmap


def scan_wire():
    """返回 (BAKED, MULTI, WIRED) 三个 StatIds 常量名集合。
    BAKED   : `.amount("纯常量")` 且不含 $value/$per/$cap —— 节点无值授予
    MULTI   : `.amount(...)` 含 $per/$cap            —— 单值无法表达，跳过
    WIRED   : 所有在 Wire 中 map.put 过的 StatIds     —— 运行期确有 entry 的守卫
    """
    baked, multi, wired = set(), set(), set()
    for f in glob.glob(os.path.join(WIREDIR, "Wire*.java")):
        txt = open(f, encoding="utf-8", errors="replace").read()
        i = 0
        while True:
            m = txt.find('map.put(StatIds.', i)
            if m < 0:
                break
            eq = txt.find(',', m)
            if eq < 0:
                break
            name = txt[m + len('map.put(StatIds.'):eq].strip()
            # 平衡括号取整条 builder 链：从 map.put( 的 ( 起算，
            # 其匹配 ) 才是整段 StatBuilder...self()....amount()....build() 的终点
            # （若只取逗号后第一个 (，会在 StatBuilder.effect(EFF) 的 ) 处提前截断，丢掉 .amount 链）。
            p = txt.find('(', m)
            if p < 0:
                break
            depth, j = 0, p
            while j < len(txt):
                c = txt[j]
                if c == '(':
                    depth += 1
                elif c == ')':
                    depth -= 1
                    if depth == 0:
                        break
                j += 1
            builder = txt[p + 1:j]
            wired.add(name)
            am = builder.find('.amount(')
            if am >= 0:
                ap = builder.find('(', am)
                depth2, k = 0, ap
                while k < len(builder):
                    c = builder[k]
                    if c == '(':
                        depth2 += 1
                    elif c == ')':
                        depth2 -= 1
                        if depth2 == 0:
                            break
                    k += 1
                amt = builder[ap + 1:k].strip()
                if '$per' in amt or '$cap' in amt:
                    multi.add(name)
                elif amt.startswith('"') and '$value' not in amt:
                    # 纯常量字符串字面量
                    baked.add(name)
                # 其余（"$value..." / 函数调用 per(...) / perRage() / perCapped(...)）保持默认模板，不计入 baked/multi
            i = j + 1
    return baked, multi, wired


# ---------------------------------------------------------------------------
# 解析 TalentNodeDef.of(...) 调用，定位第 6 参（StatList）
# ---------------------------------------------------------------------------
def split_top_args(of_call):
    """of_call 形如 `TalentNodeDef.of(...)`。返回其顶层逗号分隔参数的 (起,止) 区间（相对 of_call）。"""
    inner_start = of_call.find('(') + 1
    inner = of_call[inner_start:-1]
    args = []
    depth = 0
    in_str = False
    esc = False
    cur = 0
    for k, c in enumerate(inner):
        if in_str:
            if esc:
                esc = False
            elif c == '\\':
                esc = True
            elif c == '"':
                in_str = False
            continue
        if c == '"':
            in_str = True
            continue
        if c in '([{':
            depth += 1
            continue
        if c in ')]}':
            depth -= 1
            continue
        if c == ',' and depth == 0:
            args.append((inner_start + cur, inner_start + k))
            cur = k + 1
    args.append((inner_start + cur, inner_start + len(inner)))
    return args


def find_of_blocks(src):
    """返回 [(abs_s, abs_e, of_start, of_end), ...]，每项是 TalentNodeDef.of 的第 6 参区间 + of 调用区间。"""
    res = []
    i = 0
    while True:
        idx = src.find('TalentNodeDef.of(', i)
        if idx < 0:
            break
        p = src.find('(', idx)
        depth, j = 0, p
        while j < len(src):
            c = src[j]
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        of_call = src[idx:j + 1]
        args = split_top_args(of_call)
        if len(args) >= 6:
            a5s, a5e = args[5]
            res.append((idx + a5s, idx + a5e, idx, j))
        i = j + 1
    return res


def parse_node_id(of_text):
    m = re.search(r'"(\d+)"', of_text)
    return m.group(1) if m else None


def parse_old_ids(arg_text):
    return set(re.findall(r'StatIds\.(\w+)', arg_text))


# ---------------------------------------------------------------------------
# 由 node_stat_map 推导某节点的 StatList
# ---------------------------------------------------------------------------
def jnum(v):
    f = float(v)
    s = repr(f)
    return s if ("." in s or "e" in s) else s + ".0"


def build_stats(nid, const_by_path, nmap, baked, multi, wired):
    out = []
    for r in nmap.get(nid, []):
        path = r.get("path")
        if not path:
            continue
        name = const_by_path.get(path)
        if not name or name not in wired:
            continue
        if name in multi:
            continue
        vals = r.get("values") or []
        if name in baked:
            val = None  # 无值授予
        else:
            if vals:
                v = vals[0].get("value")
                if v is None:
                    continue
                val = float(v)
            else:
                val = 1.0
        out.append((name, val))
    seen, ded = set(), []
    for n, v in out:
        if n in seen:
            continue
        seen.add(n)
        ded.append((n, v))
    return ded


def render_statlist(stats):
    """固定缩进：List.of( 与收尾 ) 在 16 空格（与 of( 的 id/name 参数同列），内部 stat(...) 在 24 空格。
    整块以 \\n 开头，直接整体替换第 6 参旧文本（不依赖旧缩进），保证缩进确定性。"""
    if not stats:
        return "\n                List.of()"
    lines = ["\n                List.of("]
    for idx, (name, val) in enumerate(stats):
        comma = "," if idx < len(stats) - 1 else ""
        if name == ANCESTRAL_CONFLICT:
            lines.append("                        // TODO: 语料「within {X} seconds」4 秒时间窗未表达；当前以 flag amount=1.0 承载，时间窗待独立通道")
        if val is None:
            lines.append("                        TalentNodeGen.stat(StatIds.%s)%s" % (name, comma))
        else:
            lines.append("                        TalentNodeGen.stat(Map.of(StatIds.%s, %s))%s" % (name, jnum(val), comma))
    lines.append("                )")
    return "\n".join(lines)


def ensure_imports(lines):
    have = set(l.strip() for l in lines if l.strip().startswith("import "))
    missing = [imp for imp in REQUIRED_IMPORTS if imp not in have]
    if not missing:
        return lines, 0
    out = list(lines)
    idx = 0
    for i, l in enumerate(out):
        if l.strip().startswith("package "):
            idx = i + 1
            break
    for imp in reversed(missing):
        out.insert(idx, imp + "\n")
    return out, len(missing)


# ---------------------------------------------------------------------------
# 处理单个文件
# ---------------------------------------------------------------------------
def process_file(path, const_by_path, nmap, baked, multi, wired, keep_details):
    src = open(path, encoding="utf-8", errors="replace").read()
    blocks = find_of_blocks(src)
    changed = 0
    nodes_changed = 0
    added = 0
    to_novalue = 0
    safety_keeps = 0
    for (abs_s, abs_e, of_start, of_end) in reversed(blocks):
        old_arg = src[abs_s:abs_e]
        old_ids = parse_old_ids(old_arg)
        nid = parse_node_id(src[of_start:of_end])
        stats = build_stats(nid, const_by_path, nmap, baked, multi, wired) if nid else []
        new_ids = set(n for n, _ in stats)
        if not new_ids.issuperset(old_ids):
            safety_keeps += 1
            missing = sorted(old_ids - new_ids)
            keep_details.append((os.path.basename(path), nid, missing))
            continue
        if not stats:
            new_arg = old_arg
        else:
            new_arg = render_statlist(stats)
        if new_arg != old_arg:
            src = src[:abs_s] + new_arg + src[abs_e:]
            changed += 1
            nodes_changed += 1
            added += len(new_ids - old_ids)
            to_novalue += sum(1 for n, v in stats if v is None and n in old_ids)
    new_lines, imp_added = ensure_imports(src.split("\n"))
    src = "\n".join(new_lines)
    return src, changed, nodes_changed, added, to_novalue, safety_keeps, imp_added


def main():
    const_by_path, nmap = load_catalog()
    baked, multi, wired = scan_wire()
    print("StatIds constants: %d   node_stat_map nodes: %d" % (len(const_by_path), len(nmap)))
    print("WIRE: wired=%d  baked(no-value)=%d  multi-var(skip)=%d" % (len(wired), len(baked), len(multi)))
    if APPLY:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S")
        bak_dir = os.path.join(ROOT, "poe2_assets", "_backup_nodeclasses_regen_%s" % ts)
        os.makedirs(bak_dir, exist_ok=True)
    tot_changed = tot_nodes = tot_added = tot_novalue = tot_keep = tot_imp = 0
    keep_details = []
    for cat in ["keystone", "notable", "minor"]:
        for f in sorted(glob.glob(os.path.join(NODEDIR, cat, "*.java"))):
            if ".bak" in os.path.basename(f):
                continue
            new_src, changed, nodes, added, novalue, keep, imp = process_file(
                f, const_by_path, nmap, baked, multi, wired, keep_details)
            tot_changed += changed
            tot_nodes += nodes
            tot_added += added
            tot_novalue += novalue
            tot_keep += keep
            tot_imp += imp
            if APPLY and changed:
                if not os.path.exists(bak_dir):
                    os.makedirs(bak_dir, exist_ok=True)
                shutil.copy2(f, os.path.join(bak_dir, os.path.basename(f)))
                open(f, "w", encoding="utf-8", newline="\n").write(new_src)
            print("  %-26s changed=%4d nodes=%4d added=%4d novalue=%3d keep=%d imp=%d"
                  % (os.path.basename(f), changed, nodes, added, novalue, keep, imp))
    print("=" * 60)
    print("TOTAL changed_files=%d nodes_rewritten=%d stats_added=%d ->novalue=%d safety_keep=%d imports_added=%d"
          % (tot_changed, tot_nodes, tot_added, tot_novalue, tot_keep, tot_imp))
    if tot_keep:
        print("!! WARNING: %d 个节点的 StatList 因安全网（新集合不覆盖旧集合）被保留未改，请人工核查 node_stat_map 遗漏。" % tot_keep)
        for fn, nid, missing in keep_details:
            print("   KEEP %-24s node=%s 旧有但 node_stat_map 缺: %s" % (fn, nid, ", ".join(missing)))
    print("MODE:", "APPLY (已写盘)" if APPLY else "DRY-RUN (未写盘)")


if __name__ == "__main__":
    main()
