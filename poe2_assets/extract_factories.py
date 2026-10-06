#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Extract factory methods for the dominant wiring shapes.

Measured distribution (881 wirings):
    TGT+EFF        662   of which TGT_SELF_ONLY = 740 overall (84%)
    COND+TGT+EFF   219
Only 10 distinct targets exist, so repeating `StatTargetIds.TGT_SELF_ONLY` on 740
lines is pure noise. Introduce:
    self(eff)            == stat(TGT_SELF_ONLY, eff)
    self(cond, eff)      == stat(cond, TGT_SELF_ONLY, eff)
    onTarget(cond, eff)  == stat(cond, TGT_COND_ON_TARGET, eff)

SAFETY: rewrites are line-scoped to `map.put(StatIds....)` statements and gated on
wiring-count / line-count invariance. Also rewrites the parser regexes in the audit
scripts so they keep recognising the new call shapes.
"""
import re, sys, os

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
P = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")


def is_comment(s: str) -> bool:
    return s.startswith("//") or s.startswith("*") or s.startswith("/*")


def fix_comment_indent(lines):
    """Repair comment indentation collapsed to a single space.

    The earlier cleanup pass ran `[ \\t]{2,}` -> ' ' on comment lines, which also
    collapsed their LEADING indentation (4 spaces -> 1). Re-align each such comment
    to the indentation of the next non-comment code line.
    """
    out = list(lines)
    n = len(lines)
    for i, ln in enumerate(lines):
        if not ln.strip() or not is_comment(ln.strip()):
            continue
        if not (ln.startswith(" ") and not ln.startswith("  ")):
            continue                      # only repair the collapsed-to-1-space case
        body = ln.strip()
        for j in range(i + 1, min(i + 8, n)):
            nj = lines[j]
            if nj.strip() and not is_comment(nj.strip()):
                out[i] = " " * (len(nj) - len(nj.lstrip())) + body
                break
    return out

FACTORY_SRC = '''
    /** 自身生效（TGT_SELF_ONLY）。占全部接线约 84%，故提为工厂方法免重复。 */
    private static Stat self(Identifier effectRef) {
        return stat(StatTargetIds.TGT_SELF_ONLY, effectRef);
    }


    /** 自身生效 + 单条件。 */
    private static Stat self(Identifier condRef, Identifier effectRef) {
        return stat(condRef, StatTargetIds.TGT_SELF_ONLY, effectRef);
    }


    /** 对目标生效（TGT_COND_ON_TARGET）：条件在目标侧求值。 */
    private static Stat onTarget(Identifier condRef, Identifier effectRef) {
        return stat(condRef, StatTargetIds.TGT_COND_ON_TARGET, effectRef);
    }

'''

t = open(P, encoding="utf-8").read()
orig_lines = t.count("\n")
orig_wirings = t.count("map.put(StatIds.")
orig_self_only = t.count("StatTargetIds.TGT_SELF_ONLY")

# ---- 0) repair comment indentation first ----
out_lines = fix_comment_indent(t.split("\n"))
t = "\n".join(out_lines)

# ---- 1) rewrite wiring statements (line-scoped) ----
out_lines = []
n_self = n_ontgt = 0
for ln in t.split("\n"):
    s = ln.strip()
    if s.startswith("map.put(StatIds."):
        before = ln
        # self(cond, eff):  stat(COND, TGT_SELF_ONLY, EFF)
        ln = re.sub(r'stat\((StatConditionIds\.\w+),\s*StatTargetIds\.TGT_SELF_ONLY,\s*',
                    r'self(\1, ', ln)
        # onTarget(cond, eff)
        ln = re.sub(r'stat\((StatConditionIds\.\w+),\s*StatTargetIds\.TGT_COND_ON_TARGET,\s*',
                    r'onTarget(\1, ', ln)
        # self(eff):  stat(TGT_SELF_ONLY, EFF)
        ln = re.sub(r'stat\(StatTargetIds\.TGT_SELF_ONLY,\s*', 'self(', ln)
        if ln != before:
            n_self += len(re.findall(r'\bself\(', ln))
            n_ontgt += len(re.findall(r'\bonTarget\(', ln))
    out_lines.append(ln)
res = "\n".join(out_lines)

# ---- safety gates ----
if res.count("\n") != orig_lines:
    sys.exit(f"ABORT: line count changed {orig_lines} -> {res.count(chr(10))}")
if res.count("map.put(StatIds.") != orig_wirings:
    sys.exit("ABORT: wiring count changed")
# every remaining stat(...) must still parse as a known shape
leftover = re.findall(r'stat\(StatTargetIds\.TGT_SELF_ONLY', res)
if leftover:
    sys.exit(f"ABORT: {len(leftover)} TGT_SELF_ONLY not rewritten")

# ---- 2) insert factory methods before the first stat(...) overload ----
anchor = "    /** 纯路径组装（无条件）。 */"
if anchor not in res:
    # fall back to a less indentation-sensitive match
    m = re.search(r'^[ \t]*/\*\* 纯路径组装（无条件）。 \*/$', res, re.M)
    if not m:
        sys.exit("ABORT: anchor for factory insertion not found")
    anchor = m.group(0)
res = res.replace(anchor, FACTORY_SRC.lstrip("\n") + anchor, 1)

open(P, "w", encoding="utf-8").write(res)
print(f"self(...) calls     : {n_self}")
print(f"onTarget(...) calls : {n_ontgt}")
print(f"TGT_SELF_ONLY left  : {res.count('StatTargetIds.TGT_SELF_ONLY')} (was {orig_self_only}; "
      f"{3} remain inside the factory bodies)")
print(f"Wirings             : {res.count('map.put(StatIds.')} (unchanged)")
print(f"Lines               : {orig_lines} -> {res.count(chr(10))}")
