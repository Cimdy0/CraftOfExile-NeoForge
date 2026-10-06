#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""统一生成器：由 node_stat_map.json（天赋树语料 → StatId 对照表）一次性生成

  (A) StatIds.java
        —— 天赋树授予的 stat 常量来自 node_stat_map 的 `constant` 字段（单一真相源），
           应用命名 / 歧义修正（CONSTANT_REMAP / PATH_REMAP）；
        —— 非天赋树孤儿常量（affix / skill / status 等，当前 StatIds 中存在但 node_stat_map
           未授予）原样保留，确保「以 Java 源码为准」铁律下编译不破（不丢引用）。
  (B) 全部天赋节点类（keystone / notable / minor，一视同仁，不单独手改关键石）
        —— 由 node_stat_map 重生成 effects List，使用 TalentNodeGen 工厂方法
           （`TalentNodeGen.stat(StatIds.X, value)` 取代 `Map.of(...)` 样板），
           应用与 (A) 相同的歧义修正（节点 34473 的 `-7% to Chaos Resistance` 合并进
           `TO_CHAOS_RESISTANCE`，极性由 value 符号承载）。

安全网：每个节点重算出的 StatId 集合必须 ⊇ 当前节点已授予集合（old_ids 经 remap 后比较），
否则保留原样并告警（绝不静默丢弃已接线 stat，防 node_stat_map 遗漏导致数据丢失）。

歧义修正说明（审计确认 34 个 `_N` 碰撞中仅 TO_CHAOS_RESISTANCE_2 被天赋树授予）：
  TO_CHAOS_RESISTANCE_2（path to_chaos_resistance_2，`-{X}% to Chaos Resistance`）是 `-{X}%`
  形式生成的重复常量，与 TO_CHAOS_RESISTANCE（path to_chaos_resistance，`+{X}% to Chaos Resistance`）
  在 Wire 中挂同一 EFF（EFF_ADD_VALUE_ATTRIBUTE_CHAOS_COE_RESISTANCES）。按「最大深度、不双桥」
  裁定合并：节点 34473 改为授予 TO_CHAOS_RESISTANCE（value -0.07），删除 TO_CHAOS_RESISTANCE_2
  常量及其 Wire 接线（Wire 侧由主理人手工删除，见脚本日志）。

用法：
  python gen_talent_stats.py --dry     # 仅打印将要改动，不写盘
  python gen_talent_stats.py --apply   # 写盘并重生成（StatIds + 节点类，各带备份）
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

# ---- 歧义修正：仅天赋树授予的那一个 `_N` 碰撞 ----
CONSTANT_REMAP = {"TO_CHAOS_RESISTANCE_2": "TO_CHAOS_RESISTANCE"}
PATH_REMAP = {"to_chaos_resistance_2": "to_chaos_resistance"}
REMAP_OLD_KEYS = set(CONSTANT_REMAP.keys())  # 生成 StatIds 时被删除的常量


# ===========================================================================
# (A) StatIds.java 生成
# ===========================================================================
def load_map():
    return json.load(open(MAP, encoding="utf-8"))


def build_talent_constants(nmap):
    """返回 {name: (path, raw)} —— 天赋树授予的 stat 常量（去重，应用 remap）。

    name 取自 node_stat_map 的 `constant`（已是 normalize 后的 StatIds 名）；
    path 取自 `path`（应用 PATH_REMAP）；raw 取人类可读原文本作注释。
    """
    out = {}
    for nid, recs in nmap.items():
        for r in recs:
            c = r.get("constant")
            p = r.get("path")
            if not c or not p:
                continue
            c = CONSTANT_REMAP.get(c, c)
            p = PATH_REMAP.get(p, p)
            if c not in out:
                out[c] = (p, r.get("raw") or r.get("template") or "")
    return out


def load_existing_statids():
    """按声明顺序返回 [(name, path, [comment_lines])]。"""
    txt = open(STATIDS, encoding="utf-8").read()
    lines = txt.split("\n")
    pat = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')
    entries = []
    for i, ln in enumerate(lines):
        m = pat.search(ln)
        if not m:
            continue
        name, path = m.group(1), m.group(2)
        comments = []
        j = i - 1
        while j >= 0:
            s = lines[j].strip()
            if s.startswith("//"):
                comments.insert(0, s)
                j -= 1
            else:
                break  # 空行 / 其他内容均为上一条分隔，停止
        entries.append((name, path, comments))
    return entries


HEADER = """package com.cimdy.coe.data.codec.support.stat;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Stat 路径唯一缓存词表：集中缓存全部 Stat 引用（StatGen 注册 id 与 stat_entry 名）的 {@link Identifier}。
 * 数据生成（StatGen 注册）与所有引用方（AdditionType / StatusBehavior / SkillFactory / 天赋节点 /
 * 异常面动态生成 / offering 等）统一经本类字段常量 + {@link #byName(String)} 引用，
 * 避免散落的字符串字面量造成拼写漂移与改名遗漏。
 *
 * <p>本类由 poe2_assets/gen_talent_stats.py 生成，勿手改；重跑该脚本即可刷新。
 * 条目两类来源（统一由 node_stat_map.json 派生，单一真相源）：
 *   (1) 天赋树授予的 stat 常量：来自 node_stat_map 的 `constant` 字段（经 [Tag|Text] 取词丰侧 ->
 *       {X} 剔除 -> 极性归一 -> CamelCase 拆分 -> MC 术语替换 生成），注释为该 stat 的 PoE2 原文本；
 *   (2) 非天赋树孤儿常量（affix / skill / status 等）：当前 StatIds 中存在但 node_stat_map 未授予，
 *       原样保留，确保编译不破（不丢引用）。
 * 歧义修正（CONSTANT_REMAP）：TO_CHAOS_RESISTANCE_2 合并进 TO_CHAOS_RESISTANCE（极性由 value 符号承载）。
 * byName(path) 按真实路径（含连字符）反查，缺失即抛错（fail-fast）。
 *
 * <p>StatIds 为 Stat 路径的唯一缓存类，不再经「Ids -> StatPaths」第二层委托（StatPaths 已移除）。
 */
public final class StatIds {
"""

FOOTER = """
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path);
    }

    /**
     * 按 stat_entry 名（即真实路径，含连字符）查缓存路径：供动态生成点（异常面 / status 面循环、offering 等）
     * 经字段缓存引用，替代裸 {@code Identifier.fromNamespaceAndPath(ns, "ailment_buildup_"+t.id)}。
     * 缺失即抛错（fail-fast），避免拼写漂移导致的静默悬空。
     */
    private static final Map<String, Identifier> BY_NAME = new HashMap<>();

    static {
        for (var f : StatIds.class.getDeclaredFields()) {
            if (f.getType() == Identifier.class) {
                try {
                    Identifier id = (Identifier) f.get(null);
                    // 按真实路径（含连字符，如 livingmod_..._op_add_value_-0_8）建索引，
                    // 与字段名脱钩——字段名已 sanitize（'-' -> '_'）以满足 Java 标识符规则。
                    BY_NAME.put(id.getPath(), id);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static Identifier byName(String name) {
        Identifier r = BY_NAME.get(name);
        if (r == null) {
            throw new IllegalStateException("StatIds 未缓存 stat_entry: " + name);
        }
        return r;
    }

}
"""


def emit_statids(orphans, talent, dry):
    lines = [HEADER]
    for (name, path, comments) in orphans:
        for c in comments:
            lines.append("    " + c)
        lines.append('    public static final Identifier %s = id("%s");' % (name, path))
        lines.append("")
    for name in sorted(talent.keys()):
        path, raw = talent[name]
        lines.append("    // %s" % raw)
        lines.append('    public static final Identifier %s = id("%s");' % (name, path))
        lines.append("")
    lines.append(FOOTER)
    content = "\n".join(lines)
    if dry:
        print("[StatIds] orphans=%d  talent=%d  total=%d  (TO_CHAOS_RESISTANCE_2 removed=%s)"
              % (len(orphans), len(talent), len(orphans) + len(talent),
                 "TO_CHAOS_RESISTANCE_2" in REMAP_OLD_KEYS))
        return None
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    bak = os.path.join(ROOT, "poe2_assets", "_backup_statids_gen_%s" % ts)
    shutil.copy2(STATIDS, bak)
    open(STATIDS, "w", encoding="utf-8", newline="\n").write(content)
    print("[StatIds] written %d entries (orphans=%d talent=%d) -> %s"
          % (len(orphans) + len(talent), len(orphans), len(talent), STATIDS))
    print("          backup: %s" % bak)
    return content


# ===========================================================================
# (B) 节点类重生成（复用 regen_node_stats.py 的接线口径分析 + 工厂方法发射）
# ===========================================================================
def scan_wire():
    """返回 (BAKED, MULTI, WIRED) 三个 StatIds 常量名集合。"""
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
                # A stat is only "multi" (formula-driven, cannot take a node-passed
                # value) if its amount scales by $per/$cap AND does NOT also consume
                # $value. Stats such as GAIN_DEFLECTION_RATING_EQUAL_TO_OF_EVASION use
                # "$value * attr / $per" — $per is merely a divisor param (set to 1.0),
                # while $value is the node-granted magnitude. Those MUST be emitted.
                if ('$per' in amt or '$cap' in amt) and '$value' not in amt:
                    multi.add(name)
                elif amt.startswith('"') and '$value' not in amt:
                    baked.add(name)
            i = j + 1
    return baked, multi, wired


def split_top_args(of_call):
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
        path = PATH_REMAP.get(path, path)  # 歧义修正：to_chaos_resistance_2 -> to_chaos_resistance
        name = const_by_path.get(path)
        if not name or name not in wired:
            continue
        if name in multi:
            continue
        vals = r.get("values") or []
        if name in baked:
            val = None
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
    """工厂方法发射：无值 -> TalentNodeGen.stat(StatIds.X)；带值 -> TalentNodeGen.stat(StatIds.X, value)。
    消灭 Map.of(...) 样板。固定缩进同 regen_node_stats.py（16 / 24 空格）。"""
    if not stats:
        return "\n                List.of()"
    lines = ["\n                List.of("]
    for idx, (name, val) in enumerate(stats):
        comma = "," if idx < len(stats) - 1 else ""
        if val is None:
            lines.append("                        TalentNodeGen.stat(StatIds.%s)%s" % (name, comma))
        else:
            lines.append("                        TalentNodeGen.stat(StatIds.%s, %s)%s" % (name, jnum(val), comma))
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


def process_file(path, const_by_path, nmap, baked, multi, wired, keep_details):
    src = open(path, encoding="utf-8", errors="replace").read()
    blocks = find_of_blocks(src)
    changed = nodes_changed = added = to_novalue = safety_keeps = 0
    for (abs_s, abs_e, of_start, of_end) in reversed(blocks):
        old_arg = src[abs_s:abs_e]
        old_ids = parse_old_ids(old_arg)
        old_ids_remapped = set(CONSTANT_REMAP.get(x, x) for x in old_ids)  # 安全网按 remap 后比较
        nid = parse_node_id(src[of_start:of_end])
        stats = build_stats(nid, const_by_path, nmap, baked, multi, wired) if nid else []
        new_ids = set(n for n, _ in stats)
        if not new_ids.issuperset(old_ids_remapped):
            safety_keeps += 1
            missing = sorted(old_ids_remapped - new_ids)
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
            added += len(new_ids - old_ids_remapped)
            to_novalue += sum(1 for n, v in stats if v is None and n in old_ids_remapped)
    new_lines, imp_added = ensure_imports(src.split("\n"))
    src = "\n".join(new_lines)
    return src, changed, nodes_changed, added, to_novalue, safety_keeps, imp_added


# ===========================================================================
# main
# ===========================================================================
def main():
    nmap = load_map()
    talent = build_talent_constants(nmap)
    existing = load_existing_statids()
    orphans = [(n, p, c) for (n, p, c) in existing
               if n not in REMAP_OLD_KEYS and n not in talent]

    # (A) StatIds —— 先写盘（--apply）或在内存构建 const_by_path（--dry 共用）
    emit_statids(orphans, talent, dry=not APPLY)
    const_by_path = {}
    for (name, path, _c) in orphans:
        const_by_path[path] = name
    for name in talent:
        const_by_path[talent[name][0]] = name

    # (B) 节点类
    baked, multi, wired = scan_wire()
    print("WIRE: wired=%d  baked(no-value)=%d  multi-var(skip)=%d" % (len(wired), len(baked), len(multi)))
    if APPLY:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S")
        bak_dir = os.path.join(ROOT, "poe2_assets", "_backup_nodeclasses_gen_%s" % ts)
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
                shutil.copy2(f, os.path.join(bak_dir, os.path.basename(f)))
                open(f, "w", encoding="utf-8", newline="\n").write(new_src)
            print("  %-26s changed=%4d nodes=%4d added=%4d novalue=%3d keep=%d imp=%d"
                  % (os.path.basename(f), changed, nodes, added, novalue, keep, imp))
    print("=" * 60)
    print("TOTAL changed_files=%d nodes_rewritten=%d stats_added=%d ->novalue=%d safety_keep=%d imports_added=%d"
          % (tot_changed, tot_nodes, tot_added, tot_novalue, tot_keep, tot_imp))
    if tot_keep:
        print("!! WARNING: %d 个节点的 effects 因安全网（新集合不覆盖 remap 后旧集合）被保留未改，请人工核查 node_stat_map 遗漏。" % tot_keep)
        for fn, nid, missing in keep_details:
            print("   KEEP %-24s node=%s remap后旧有但 node_stat_map 缺: %s" % (fn, nid, ", ".join(missing)))
    print("MODE:", "APPLY (已写盘)" if APPLY else "DRY-RUN (未写盘)")
    if APPLY:
        print("NOTE: 若合并了 TO_CHAOS_RESISTANCE_2，请主理人同步删除 WireDefense.java 中"
              " `map.put(StatIds.TO_CHAOS_RESISTANCE_2, ...)` 一行（其 StatIds 常量已删除）。")


if __name__ == "__main__":
    main()
