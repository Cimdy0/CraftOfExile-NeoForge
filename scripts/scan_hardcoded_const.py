"""扫描引擎侧（非 gen / 非 stat 定义）硬编码数值常量（#48 同型死线候选）。

用法：python scripts/scan_hardcoded_const.py [--top N]
仅报告，不改代码。判定「是否该数据化」由人工完成。
"""
import os
import re
import sys
from collections import Counter

ROOT = os.path.join("src", "main", "java", "com", "cimdy", "coe")
PAT = re.compile(r"static\s+final\s+(?:double|float|int)\s+([A-Z][A-Z0-9_]*)\s*=\s*(-?[0-9]+\.?[0-9]*)")


def main():
    rows = []
    for dp, _d, fs in os.walk(ROOT):
        rel = dp.replace("\\", "/")
        if "/data/gen/" in rel or "/data/codec/support/stat/" in rel:
            continue
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            for i, line in enumerate(open(p, encoding="utf-8", errors="ignore"), 1):
                m = PAT.search(line)
                if m:
                    rows.append((os.path.relpath(p, ROOT), i, m.group(1), m.group(2)))
    print(f"硬编码数值常量 {len(rows)} 条（已排除 data/gen 与 stat 定义）")
    top = int(sys.argv[sys.argv.index("--top") + 1]) if "--top" in sys.argv else 15
    for f, n in Counter(r[0] for r in rows).most_common(top):
        print(f"{n:3d}  {f}")
    if "--all" in sys.argv:
        for f, i, name, val in sorted(rows):
            print(f"    {f}:{i}  {name} = {val}")


if __name__ == "__main__":
    main()
