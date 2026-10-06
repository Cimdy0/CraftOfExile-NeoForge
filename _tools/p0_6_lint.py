#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P0-001 子项 6：lint_colors.py 增加「ui/ 包内 Theme.<CONST> 静态引用」WARN 防回潮规则。

默认关闭（需 --static-refs），且永不进 ERROR / 永不改退出码，避免噪音。
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from b3_apply import apply

LINT = "lint_colors.py"

apply(LINT, [
    # 1) 用法说明
    ("""  python lint_colors.py --warn         # 附 WARN（新硬编码色候选，仅供 review）
  python lint_colors.py --src DIR      # 自定义源码根""",
     """  python lint_colors.py --warn         # 附 WARN（新硬编码色候选，仅供 review）
  python lint_colors.py --src DIR      # 自定义源码根
  python lint_colors.py --static-refs  # 附 WARN：ui/ 包内直接读 Theme.<CONST> 的静态引用
                                       # （换肤对这些引用无效；仅提示，不影响退出码）"""),
    # 2) import collections
    ("""import os
import re
import sys""",
     """import collections
import os
import re
import sys"""),
    # 3) 新正则
    ("""# 字面量出现（6 位或 8 位 ARGB/RGB）
LIT_RE = re.compile(r"\\b(0x[0-9A-Fa-f]{6}|0x[0-9A-Fa-f]{8})\\b")""",
     """# 字面量出现（6 位或 8 位 ARGB/RGB）
LIT_RE = re.compile(r"\\b(0x[0-9A-Fa-f]{6}|0x[0-9A-Fa-f]{8})\\b")
# Theme.<静态常量> 直接引用：不经过 StylePreset，换肤对它无效
STATIC_THEME_RE = re.compile(r"\\bTheme\\.([A-Z][A-Z0-9_]*)\\b")"""),
    # 4) scan 签名与收集
    ("""def scan(src_root, theme_values, warn):
    errors, warns = [], []""",
     """def scan(src_root, theme_values, warn, static_refs=False):
    errors, warns, statics = [], [], []"""),
    ("""                    for m in LIT_RE.finditer(line):
                        lit = norm(m.group(1))
                        if lit in theme_values:
                            errors.append((rel, i, lit, theme_values[lit]))
                        elif warn and len(lit) == 10 and rel.startswith("src/main/java/com/cimdy/coe/ui/"):
                            if "Theme." not in line:
                                warns.append((rel, i, lit))
    return errors, warns""",
     """                    for m in LIT_RE.finditer(line):
                        lit = norm(m.group(1))
                        if lit in theme_values:
                            errors.append((rel, i, lit, theme_values[lit]))
                        elif warn and len(lit) == 10 and rel.startswith("src/main/java/com/cimdy/coe/ui/"):
                            if "Theme." not in line:
                                warns.append((rel, i, lit))
                    if static_refs and rel.startswith("src/main/java/com/cimdy/coe/ui/"):
                        for m in STATIC_THEME_RE.finditer(line):
                            statics.append((rel, i, m.group(1)))
    return errors, warns, statics"""),
    # 5) main：flag + 调用 + 打印
    ("""    warn = "--warn" in sys.argv
    src = None""",
     """    warn = "--warn" in sys.argv
    static_refs = "--static-refs" in sys.argv
    src = None"""),
    ("""    errors, warns = scan(src, theme_values, warn)""",
     """    errors, warns, statics = scan(src, theme_values, warn, static_refs)"""),
    ("""    if errors:
        print(f"\\n{len(errors)} regression(s): bare literals equal to Theme constants.")
        sys.exit(1)""",
     """    if static_refs and statics:
        by_name = collections.Counter(n for _, _, n in statics)
        print(f"\\n[static-refs] {len(statics)} direct Theme.<CONST> reads in ui/ "
              f"({len(by_name)} distinct) -- StylePreset 换肤对这些引用无效:")
        for name, c in by_name.most_common():
            print(f"  {c:4d}  Theme.{name}")
        print("  (WARN only: does not affect exit code)")

    if errors:
        print(f"\\n{len(errors)} regression(s): bare literals equal to Theme constants.")
        sys.exit(1)"""),
])
print("[P0-6] done")
