#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Repair verify_gen.py: a previous patch wrote literal \\n inside a raw string,
collapsing the comment and the put_pat assignment onto one line (so put_pat became
undefined). Split it back into proper lines."""
import io

P = "scripts/verify_gen.py"
src = io.open(P, encoding="utf-8").read()

lines = src.split("\n")
out = []
fixed = False
for ln in lines:
    if "组合条目" in ln and "put_pat = re.compile" in ln and "\\n" in ln:
        # keep indentation of the original line
        indent = ln[: len(ln) - len(ln.lstrip())]
        out.append(indent + "# 组合条目（PoE hybrid 词缀）写法必须一并列出：漏掉会让这些接线对全部检查不可见")
        out.append(indent + "# （正是技能 §11.10 记载的「门禁静默失效」模式）。")
        out.append(indent + r"put_pat = re.compile(r'map\.put\(StatIds\.(\w+),\s*(?:stat|self|onTarget|group|groupSelf|groupSelfWhen)\((.*?)\)\);')")
        fixed = True
    else:
        out.append(ln)

if not fixed:
    raise SystemExit("garbled line not found - already fixed?")

io.open(P, "w", encoding="utf-8").write("\n".join(out))
print("repaired")
