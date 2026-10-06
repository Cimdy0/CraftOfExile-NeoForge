# -*- coding: utf-8 -*-
"""
A4-v3：词缀注入面（AdditionType → stat_entry）单位一致性审计。

_unitaudit2.py 只扫了 `StatGen.ref(StatIds.X, mag)` 这一个注入面，
漏掉了体量大得多的第二个注入面——**装备词缀**：
    AdditionType.XXX = from(Kind.GLOBAL, id(StatIds.YYY))     // 词缀 -> stat 条目
    Mod.from(n, AdditionType.XXX, ..., WithoutTier.from(lo, hi, ...))  // roll 范围
    → AdditionType.Effect.toStatEntry(rolled) 把 rolled 注入 vars["value"]

实测（SuffixMod:193）：INC_OFFERING_DAMAGE_MOD 的 roll 是 0.18~0.20 —— **小数**。
若被引用的条目烘 `$value/100`，则该词缀实际效果缩小 100 倍。

本脚本交叉三方，输出真实错配清单。
"""
import json
import re
import pathlib
from collections import defaultdict

ROOT = pathlib.Path(__file__).resolve().parents[2]
IDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
ENTRY_DIR = ROOT / "src/generated/resources/data/coe/coe/stat_entry"
ADDITION = ROOT / "src/main/java/com/cimdy/coe/data/codec/item/text/AdditionType.java"
ATTR_REG = ROOT / "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
MOD_DIR = ROOT / "src/main/java/com/cimdy/coe/data/gen/equipment/mod"
OUT = ROOT / "poe2_assets/_test/affix_unit_audit.md"

# AdditionType XXX = from(Kind.K, id(StatIds.YYY))  /  from(Kind.K, StatIds.YYY)
ADD_DECL = re.compile(
    r'AdditionType\s+(\w+)\s*=\s*from\(\s*Kind\.(\w+)\s*,\s*(?:id\(\s*)?StatIds\.(\w+)')
# Mod.from(n, AdditionType.XXX, ...) 到下一个 Mod.from / registerXxx 之间的 roll 字面量
MOD_BLOCK = re.compile(
    r'Mod\.from\(\s*\d+\s*,\s*AdditionType\.(\w+)\b(.*?)(?=Mod\.from\(|register\w+Mod\(|\Z)',
    re.S)
NUM = re.compile(r'from\(\s*(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)')


def const_to_path():
    txt = IDS.read_text(encoding="utf-8")
    return {m.group(1): m.group(2) for m in re.finditer(r'(\w+)\s*=\s*id\("([a-z0-9_]+)"\)', txt)}


def addition_map():
    """AdditionType 名 -> (Kind, StatIds 常量名)"""
    txt = ADDITION.read_text(encoding="utf-8")
    return {m.group(1): (m.group(2), m.group(3)) for m in ADD_DECL.finditer(txt)}


def roll_ranges():
    """AdditionType 名 -> [(file, lo, hi)]，取各 tier 的 from(lo, hi, ...) 字面量。"""
    out = defaultdict(list)
    for p in sorted(MOD_DIR.rglob("*.java")):
        txt = p.read_text(encoding="utf-8", errors="ignore")
        for m in MOD_BLOCK.finditer(txt):
            name, body = m.group(1), m.group(2)
            for n in NUM.finditer(body):
                lo, hi = float(n.group(1)), float(n.group(2))
                out[name].append((p.name, lo, hi))
    return out


def pct_attrs():
    txt = ATTR_REG.read_text(encoding="utf-8")
    return {"coe:" + m.group(2) for m in re.finditer(
        r'Holder<Attribute>\s+(\w+)\s*=\s*registerPercentage\s*\(\s*"([a-z0-9_]+)"', txt)}


def entry_amounts():
    """stat path -> (amount, attribute, operation, div100)"""
    out = {}
    for p in ENTRY_DIR.glob("*.json"):
        try:
            d = json.loads(p.read_text(encoding="utf-8"))
        except Exception:
            continue
        amt = str(d.get("amount", ""))
        out[p.stem] = (amt, d.get("attribute", ""), d.get("operation", ""),
                       "/100" in amt.replace(" ", ""))
    return out


MULT_OPS = {"add_multiplied_base", "add_multiplied_total"}


def unit_of(ranges, op, is_pct):
    """判 roll 单位。

    单看幅度会误判，必须结合 operation 与属性类型：
      * 乘法算子（add_multiplied_*）的乘数天然是<b>小数</b>（0.25 = +25%，1.19 = +119%），
        故即使 roll 达 1.19 仍是 fraction，不是「整数百分点」；
      * add_value 落在<b>非</b> percentage 属性上 = 平加实数（+550 命中 / +214 生命），
        与百分比单位无关 → flat，豁免；
      * add_value 落在 percentage 属性上才需按幅度判 fraction / points。
    """
    vals = [v for _, lo, hi in ranges for v in (abs(lo), abs(hi)) if v != 0]
    if not vals:
        return "unknown"
    if op in MULT_OPS:
        return "fraction"
    if not is_pct:
        return "flat"
    return "fraction" if max(vals) < 1 else ("points" if min(vals) >= 1 else "mixed")


def main():
    c2p = const_to_path()
    pct = pct_attrs()
    adds = addition_map()
    rolls = roll_ranges()
    ents = entry_amounts()

    rows, no_roll = [], []
    for add_name, (kind, const) in sorted(adds.items()):
        path = c2p.get(const)
        if not path or path not in ents:
            continue
        amt, attr, op, div100 = ents[path]
        if "$value" not in amt:
            continue                      # 条目数值已烘常量，词缀 roll 不参与
        rng = rolls.get(add_name, [])
        if not rng:
            no_roll.append((add_name, path, amt, attr))
            continue
        unit = unit_of(rng, op, attr in pct)
        if div100 and unit in ("fraction", "flat"):
            verdict = "BUG-DIV"          # 条目 /100 + 调用方传小数/平加 → 缩小 100 倍
        elif (not div100) and unit == "points":
            verdict = "BUG-MUL"          # 条目裸 $value + 整数百分点 → 放大 100 倍
        elif unit in ("mixed", "unknown"):
            verdict = "CHECK"
        else:
            verdict = "OK"
        lo = min(min(l, h) for _, l, h in rng)
        hi = max(max(l, h) for _, l, h in rng)
        rows.append((verdict, add_name, path, amt, attr, op, unit, lo, hi, rng[0][0]))

    tally = defaultdict(int)
    for r in rows:
        tally[r[0]] += 1
    print(f"AdditionType 引用 $value 模板条目：{len(rows) + len(no_roll)} 条"
          f"（有 roll 表 {len(rows)}，无 roll 表 {len(no_roll)}）")
    for k in ("BUG-DIV", "BUG-MUL", "CHECK", "OK"):
        print(f"  {k:8s} {tally[k]:4d}")

    lines = ["# A4-v3 词缀注入面单位审计（AdditionType → stat_entry）", "",
             "词缀 roll 值经 `AdditionType.Effect.toStatEntry(rolled)` 注入 `vars[\"value\"]`，",
             "故条目 amount 约定必须与 roll 表单位一致。", "",
             "| 判定 | 条数 | 含义 |", "|---|---|---|",
             f"| BUG-DIV | {tally['BUG-DIV']} | 条目烘 `/100` + roll 传小数 → **缩小 100 倍** |",
             f"| BUG-MUL | {tally['BUG-MUL']} | 条目裸 `$value` + roll 传整数点 → **放大 100 倍** |",
             f"| CHECK | {tally['CHECK']} | roll 单位混杂/不可判 → 人工 |",
             f"| OK | {tally['OK']} | 自洽 |", ""]

    for k in ("BUG-DIV", "BUG-MUL", "CHECK", "OK"):
        sel = [r for r in rows if r[0] == k]
        if not sel:
            continue
        lines += [f"## {k}（{len(sel)} 条）", "",
                  "| AdditionType | stat id | amount | 属性 | op | roll 单位 | roll 范围 | 定义文件 |",
                  "|---|---|---|---|---|---|---|---|"]
        for v, a, path, amt, attr, op, unit, lo, hi, f in sel:
            lines.append(f"| `{a}` | `{path}` | `{amt}` | `{attr}` | {op} | {unit} | {lo}~{hi} | {f} |")
        lines.append("")

    if no_roll:
        lines += [f"## 无 roll 表（{len(no_roll)} 条，未被任何 Mod 引用或 roll 形态未识别）", "",
                  "| AdditionType | stat id | amount | 属性 |", "|---|---|---|---|"]
        for a, path, amt, attr in no_roll:
            lines.append(f"| `{a}` | `{path}` | `{amt}` | `{attr}` |")
        lines.append("")

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"\n报告 → {OUT}")

    for k in ("BUG-DIV", "BUG-MUL", "CHECK"):
        sel = [r for r in rows if r[0] == k]
        if sel:
            print(f"\n--- {k} ---")
            for v, a, path, amt, attr, op, unit, lo, hi, f in sel[:50]:
                print(f"  {a[:32]:34s} {path[:34]:36s} {amt[:12]:13s} {unit:8s} {lo}~{hi}")


if __name__ == "__main__":
    main()
