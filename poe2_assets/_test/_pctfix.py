# -*- coding: utf-8 -*-
"""
A4 修复：把 P0 命中（PercentageAttribute + ADD_VALUE + 裸 $value）从 add() 迁到 pctAdd()。

依据 StatGen.pctAdd 自身 javadoc：
  「add() 的 amount 是裸 $value，而这些容器以小数存放比例（0.05 = 5%）。
    天赋节点传的是整数百分点（5），直接 ADD_VALUE 会放大 100 倍。」
本次只改调用的 helper 名（add -> pctAdd），不动属性/标签/条件，最小变更。

用法：
  python _pctfix.py          # 只报告，不写
  python _pctfix.py --apply  # 落盘
"""
import json
import re
import sys
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
IDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
GEN = ROOT / "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
ENTRY_DIR = ROOT / "src/generated/resources/data/coe/coe/stat_entry"
ATTR_REG = ROOT / "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
SRC = ROOT / "src/main/java"

PCT_DECL = re.compile(
    r'public\s+static\s+Holder<Attribute>\s+(\w+)\s*=\s*registerPercentage\s*\(\s*"([a-z0-9_]+)"')
SKIP_PARTS = ("data\\gen\\", "data/gen/")
SKIP_NAMES = {"AttributeRegister.java", "StatIds.java"}


def pct_attrs():
    out = {}
    for m in PCT_DECL.finditer(ATTR_REG.read_text(encoding="utf-8")):
        out["coe:" + m.group(2)] = m.group(1)
    return out


def live_attrs(consts):
    used = set()
    for p in SRC.rglob("*.java"):
        sp = str(p)
        if any(s in sp for s in SKIP_PARTS) or p.name in SKIP_NAMES:
            continue
        try:
            t = p.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        for aid, c in consts.items():
            if aid not in used and re.search(r'\b' + re.escape(c) + r'\b', t):
                used.add(aid)
    return used


def path_to_const():
    out = {}
    for m in re.finditer(r'(\w+)\s*=\s*id\("([a-z0-9_]+)"\)', IDS.read_text(encoding="utf-8")):
        out[m.group(2)] = m.group(1)
    return out


def descriptions():
    desc, pending = {}, None
    for ln in IDS.read_text(encoding="utf-8").splitlines():
        s = ln.strip()
        if s.startswith("//"):
            pending = s[2:].strip()
            continue
        m = re.search(r'=\s*id\("([a-z0-9_]+)"\)', s)
        if m:
            desc[m.group(1)] = pending or ""
            pending = None
    return desc


def main():
    apply = "--apply" in sys.argv
    consts = pct_attrs()
    live = live_attrs(consts)
    p2c = path_to_const()
    desc = descriptions()

    # 收集 P0：percentage 属性 + add_value + 裸 $value + 活属性 + 描述含 %
    targets = []
    for p in sorted(ENTRY_DIR.glob("*.json")):
        d = json.loads(p.read_text(encoding="utf-8"))
        if d.get("type") != "attribute" or d.get("operation") != "add_value":
            continue
        attr, amt = d.get("attribute"), str(d.get("amount", ""))
        if attr not in consts or "$value" not in amt:
            continue
        if "/ 100" in amt.replace("/100", "/ 100"):
            continue
        if attr not in live or "%" not in desc.get(p.stem, ""):
            continue
        targets.append((p.stem, p2c.get(p.stem), attr))

    text = GEN.read_text(encoding="utf-8")
    lines = text.split("\n")
    hit, miss = [], []
    for path, const, attr in targets:
        if not const:
            miss.append((path, "StatIds 无对应常量", ""))
            continue
        # 定位 add(map, StatIds.CONST, ...) 调用（允许跨行参数）
        pat = re.compile(r'(?<![\w.])add\(\s*map\s*,\s*StatIds\.' + re.escape(const) + r'\s*,')
        found = [i for i, ln in enumerate(lines) if pat.search(ln)]
        if len(found) == 1:
            hit.append((found[0], path, const, attr))
        else:
            # 记录实际形态，供人工处理
            alt = [i for i, ln in enumerate(lines) if ("StatIds." + const) in ln]
            shape = lines[alt[0]].strip()[:120] if alt else "未找到引用"
            miss.append((path, f"add(map,...) 匹配 {len(found)} 处", shape))

    print(f"P0 目标 {len(targets)} 条 → add()->pctAdd() 可自动改 {len(hit)}，需人工 {len(miss)}")
    for path, why, shape in miss:
        print(f"  [人工] {path[:52]:54s} {why}  | {shape}")

    if apply and hit:
        for idx, path, const, attr in hit:
            lines[idx] = re.sub(r'(?<![\w.])add\(\s*map\s*,', 'pctAdd(map,', lines[idx], count=1)
        GEN.write_text("\n".join(lines), encoding="utf-8")
        print(f"\n已改写 {len(hit)} 处 add() -> pctAdd()")
    elif hit:
        print("\n--- 预览（前 40 条）---")
        for idx, path, const, attr in hit[:40]:
            print(f"  L{idx+1}: {const}  [{attr}]")
        print("\n加 --apply 落盘")


if __name__ == "__main__":
    main()
