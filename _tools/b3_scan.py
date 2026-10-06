#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""B3 只读盘点：ui/ 全树裸 8 位 0x 字面量分布（剥离注释与字符串后统计）。"""
import os, re, sys, collections

HERE = r"E:\McModding\Modding\CraftOfExile-NeoForge"
UI_ROOT = os.path.join(HERE, "src", "main", "java", "com", "cimdy", "coe", "ui")
THEME_FILE = os.path.join(UI_ROOT, "widget", "render", "Theme.java")
PRESET_FILE = os.path.join(UI_ROOT, "widget", "render", "StylePreset.java")

LINE_COMMENT_RE = re.compile(r"//.*")
STR_RE = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'')
LIT8_RE = re.compile(r"\b(0x[0-9A-Fa-f]{8})\b")
THEME_DEF_RE = re.compile(r"public\s+static\s+final\s+int\s+(\w+)\s*=\s*(0x[0-9A-Fa-f]{6,8})")
RET_RE = re.compile(r"return\s+0x[0-9A-Fa-f]+")
DEF_RE = re.compile(r"public\s+static\s+final\s+int\s+\w+\s*=\s*0x[0-9A-Fa-f]+")


def strip_line(line, in_block):
    """returns (stripped, still_in_block)"""
    if in_block:
        end = line.find("*/")
        if end == -1:
            return "", True
        line = line[end + 2:]
        in_block = False
    line = LINE_COMMENT_RE.sub("", line)
    line = STR_RE.sub('""', line)
    bi = line.find("/*")
    if bi != -1:
        rest = line[bi + 2:]
        ei = rest.find("*/")
        if ei != -1:
            line = line[:bi] + rest[ei + 2:]
        else:
            line = line[:bi]
            in_block = True
    return line, in_block


def load_theme_values():
    vals = {}
    for f in (THEME_FILE, PRESET_FILE):
        with open(f, "r", encoding="utf-8", errors="replace") as fh:
            for line in fh:
                m = THEME_DEF_RE.search(line)
                if m:
                    vals.setdefault(m.group(2).lower(), m.group(1))
    return vals


def main():
    theme_vals = load_theme_values()
    per_pkg = collections.Counter()      # pkg -> count
    per_file = collections.Counter()     # relpath -> count
    per_val = collections.Counter()      # value -> count
    val_pkgs = collections.defaultdict(collections.Counter)  # value -> pkg counter
    total = 0
    theme_hits = 0

    for dirpath, _, files in os.walk(UI_ROOT):
        rel_dir = os.path.relpath(dirpath, UI_ROOT).replace("\\", "/")
        if rel_dir == ".":
            rel_dir = "(ui)"
        if rel_dir == "widget/render" or rel_dir.startswith("widget/render/"):
            continue
        for fn in sorted(files):
            if not fn.endswith(".java"):
                continue
            path = os.path.join(dirpath, fn)
            rel = os.path.relpath(path, HERE).replace("\\", "/")
            in_block = False
            with open(path, "r", encoding="utf-8", errors="replace") as fh:
                for raw in fh:
                    line = raw.rstrip("\n").rstrip("\r")
                    line, in_block = strip_line(line, in_block)
                    if DEF_RE.search(line) or RET_RE.search(line):
                        continue
                    for m in LIT8_RE.finditer(line):
                        lit = m.group(1).lower()
                        total += 1
                        per_pkg[rel_dir] += 1
                        per_file[rel] += 1
                        per_val[lit] += 1
                        val_pkgs[lit][rel_dir] += 1
                        if lit in theme_vals:
                            theme_hits += 1

    print("=" * 78)
    print(f"TOTAL bare 8-digit 0x literals in ui/ (excl. widget/render): {total}")
    print(f"  of which equal to a Theme/StylePreset constant value: {theme_hits}")
    print(f"  unique values: {len(per_val)}")
    print("=" * 78)

    print("\n--- PER PACKAGE (desc) ---")
    for pkg, c in per_pkg.most_common():
        print(f"{c:5d}  {pkg}")

    print("\n--- TOP 30 FREQUENT VALUES ---")
    print(f"{'count':>5}  {'value':<12} {'theme?':<7} packages")
    for v, c in per_val.most_common(30):
        tn = theme_vals.get(v, "-")
        pkgs = ", ".join(f"{p}({n})" for p, n in val_pkgs[v].most_common(4))
        print(f"{c:5d}  0x{v[2:].upper():<10} {tn:<7} {pkgs}")

    print("\n--- TOP 25 FILES ---")
    for f, c in per_file.most_common(25):
        print(f"{c:5d}  {f}")

    # component/ subtree detail
    print("\n--- component/ subtree FILE DETAIL ---")
    for f, c in sorted(per_file.items()):
        if "/widget/component" in f:
            print(f"{c:5d}  {f}")


if __name__ == "__main__":
    main()
