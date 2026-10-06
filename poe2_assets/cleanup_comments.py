#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Strip process-log noise from StatGen.java comments (COMMENT LINES ONLY).

Kept   : semantic group headers, law-level constraints (zero-base, dead-EFF, polarity TODO).
Removed: "Phase N" stage labels, fragile line-number refs (L578 etc.), provenance scripts.

SCOPE RULE (hard-won, 2026-08-29):
  These rewrites MUST apply to comment lines only. Applying them to whole-file text
  previously caused two separate corruptions:
    1. `\\s{2,}`  -> `\\s` matches newlines, collapsing a 1211-line file into 28 lines.
    2. `\\(\\s*\\)` -> deletes EMPTY PARENTHESES, turning `new LinkedHashMap<>()`
       into `new LinkedHashMap<>;` (compile error).
  Hence: iterate line by line, touch only lines whose stripped form starts with
  `//`, `*` or `/*`.
"""
import re, sys, os

ROOT = r"E:\McModding\Modding\CraftOfExile-NeoForge"
P = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "data", "gen", "stat", "StatGen.java")

# markers that must survive no matter what (law-level / pending-obligation comments)
PROTECT = ("零基", "死 EFF", "极性", "TODO", "三定律", "⚠")


def is_comment(s: str) -> bool:
    return s.startswith("//") or s.startswith("*") or s.startswith("/*")


def clean(line: str) -> str:
    if any(k in line for k in PROTECT):
        # still strip the noisy bits, but never blank the line out
        out = re.sub(r"Phase\s*\d+\s*[—–\-:]+\s*", "", line)
        out = re.sub(r"\bL\d+(?:/L\d+)*\b", "", out)
        return out
    out = re.sub(r"Phase\s*\d+\s*[—–\-:]+\s*", "", line)
    out = re.sub(r"Phase\s*\d+\s*[—–\-:]*\s*", "", out)
    out = re.sub(r"[（(]\s*L\d+(?:\s*/\s*L\d+)*\s*[)）]", "", out)
    out = re.sub(r"\bL\d+(?:/L\d+)*\b", "", out)
    out = re.sub(r"（\s*）", "", out)          # full-width only; NEVER touch ASCII ()
    out = re.sub(r"[，,]\s*[，,]", "，", out)
    out = re.sub(r"[，,]\s*）", "）", out)
    out = re.sub(r"）\s*[，,]", "）", out)
    out = re.sub(r"[ \t]{2,}", " ", out)
    out = re.sub(r"[ \t]+$", "", out)
    return out


t = open(P, encoding="utf-8").read()
lines = t.split("\n")
orig_lines = len(lines)
orig_phase = len(re.findall(r"Phase\s*\d", t))
orig_ln = len(re.findall(r"\bL\d+(?:/L\d+)*\b", t))

out = [clean(ln) if is_comment(ln.strip()) else ln for ln in lines]
res = "\n".join(out)

# safety gates
if len(res.split("\n")) != orig_lines:
    print(f"ABORT: line count changed {orig_lines} -> {len(res.split(chr(10)))}")
    sys.exit(1)
if res.count("LinkedHashMap<>(") != t.count("LinkedHashMap<>("):
    print("ABORT: empty-paren constructor call was altered")
    sys.exit(1)
if res.count("map.put(StatIds.") != t.count("map.put(StatIds."):
    print("ABORT: wiring count changed")
    sys.exit(1)

open(P, "w", encoding="utf-8").write(res)
print(f"Phase labels     : {orig_phase} -> {len(re.findall(r'Phase\s*\d', res))}")
print(f"Line-number refs : {orig_ln} -> {len(re.findall(r'\bL\d+(?:/L\d+)*\b', res))}")
print(f"Lines            : {orig_lines} -> {len(res.split(chr(10)))}")
print(f"Wiring count     : {res.count('map.put(StatIds.')} (unchanged)")
