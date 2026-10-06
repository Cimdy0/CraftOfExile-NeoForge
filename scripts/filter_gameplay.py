"""从 scan_hardcoded_const.py --all 产出「gameplay 候选」干净集（排除纯 UI/系统噪声）。

用法：python scripts/filter_gameplay.py   （须在项目根目录运行）
输出：scripts/_hc_gameplay.txt

排除规则（片段匹配路径）：
- /data/gen/            生成管线（JSON 由 datagen 产出，不属手写常量）
- /data/codec/support/stat/  Stat 定义（属性本身的 base 不算死线候选）
- /ui/ /client/ /network/   渲染/网络层常量
- item/accessory/ item/charm/ Slot.java   纯 GUI 槽位坐标（SLOT_X/Y0/STEP），非 gameplay
其余（data/helper、data/codec/skill、data/codec/support/status、event、helper、item 下 gameplay 类、
block/entity/custom 等）均视为 gameplay 候选。
"""
import os
import re
import subprocess
import sys

EXCLUDE_FRAGMENTS = [
    "/data/gen/",
    "/data/codec/support/stat/",
    "/ui/",
    "/client/",
    "/network/",
    "item/accessory/",   # 纯 GUI 槽位坐标
    "item/charm/",       # 纯 GUI 槽位坐标
    "Slot.java",         # 槽位坐标类（AccessorySlot / CharmSlot / FlaskSlot）
]

LINE = re.compile(r"\s*(\S+?):(\d+)\s+([A-Z][A-Z0-9_]*)\s*=\s*(-?[\d.]+)")


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    out = subprocess.check_output(
        [sys.executable, "scripts/scan_hardcoded_const.py", "--all"],
        text=True, cwd=root)
    rows = []
    for line in out.splitlines():
        m = LINE.match(line)
        if not m:
            continue
        path, ln, name, val = m.group(1), int(m.group(2)), m.group(3), m.group(4)
        if any(frag in path for frag in EXCLUDE_FRAGMENTS):
            continue
        rows.append((path, ln, name, val))
    rows.sort()
    dst = os.path.join(root, "scripts", "_hc_gameplay.txt")
    with open(dst, "w", encoding="utf-8") as f:
        for p, i, n, v in rows:
            f.write(f"{p}:{i}  {n} = {v}\n")
    print(f"clean gameplay 候选 {len(rows)} 条 -> scripts/_hc_gameplay.txt")


if __name__ == "__main__":
    main()
