# -*- coding: utf-8 -*-
"""
A4-v2 单位一致性审计（调用方感知版）。

v1 的缺陷：只看条目自身形态（PercentageAttribute + ADD_VALUE + 裸 $value）就判 100x 放大，
但漏掉了决定性的一维——**谁注入 $value、以什么单位**。

实测事实（本轮核实）：
  * CharmStatusTypes: StatGen.ref(StatIds.CHARM_RESIST_FIRE, 0.25 * effectMult(...))  → 传小数
  * BuffStatusTypes:  StatGen.ref(StatIds.ONSLAUGHT_ATTACK_SPEED, 0.2 * inst.magnitude) → 传小数
  * FormStatusTypes:  StatGen.ref(StatIds.DAMAGE_DEALT_BEAR_PHYSICAL, 0.30)            → 传小数
  * 天赋节点 effects 全树 0 处接线 → 天赋侧尚无任何注入方
故裸 $value + ADD_VALUE 对「已接线且调用方传小数」的条目是**正确**的，不是 bug。

真正的缺陷是**同一属性 + 同一 operation 家族内部约定不一致**：
部分条目烘 $value/100，部分裸 $value。未接线时无症状，接线时必成片错 100 倍。

分级：
  W-OK    已接线，调用方传小数(<1)，条目裸 $value        → 自洽，正确
  W-BAD   已接线，调用方传整数百分点(>=1)，条目裸 $value  → 真 100x 放大
  W-DIV   已接线，条目烘 /100，但调用方传小数            → 反向缩小 100 倍
  U-MIX   未接线，且所属(属性,operation)族内约定不一致    → 接线前必须统一（真缺陷）
  U-UNI   未接线，族内约定一致                          → 仅需在接线时遵守既定约定
"""
import json
import re
import pathlib
from collections import defaultdict

ROOT = pathlib.Path(__file__).resolve().parents[2]
IDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
ENTRY_DIR = ROOT / "src/generated/resources/data/coe/coe/stat_entry"
ATTR_REG = ROOT / "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
SRC = ROOT / "src/main/java"
OUT = ROOT / "poe2_assets/_test/unit_audit2.md"

PCT_DECL = re.compile(
    r'public\s+static\s+(?:final\s+)?Holder<Attribute>\s+(\w+)\s*=\s*registerPercentage\s*\(\s*"([a-z0-9_]+)"')
# ref(StatIds.CONST[, <magnitude expr>])
REF_CALL = re.compile(r'\bref\(\s*StatIds\.(\w+)\s*(?:,\s*([^;]*?))?\)\s*[,)\n]')


def pct_attrs():
    txt = ATTR_REG.read_text(encoding="utf-8")
    return {"coe:" + m.group(2): m.group(1) for m in PCT_DECL.finditer(txt)}


def const_of_path():
    txt = IDS.read_text(encoding="utf-8")
    return {m.group(2): m.group(1) for m in re.finditer(r'(\w+)\s*=\s*id\("([a-z0-9_]+)"\)', txt)}


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


VAR_DECL = re.compile(r'\b(?:double|float|int|long|var)\s+(\w+)\s*=\s*([^;]+);')


def callers():
    """CONST -> [(file, line, magnitude_expr, resolved_expr)]。

    magnitude 常为局部变量（perStack / points / m），必须解析其定义才能判单位——
    否则会把 `perStack * inst.stacks`（perStack = 0.03 * effect，小数）误判为不可知
    而默认放过（v1 正是如此漏掉 inc_minion_damage 的反向缩小 100 倍）。
    """
    out = defaultdict(list)
    for p in SRC.rglob("*.java"):
        if p.name in ("StatIds.java",):
            continue
        try:
            txt = p.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        if "ref(StatIds." not in txt:
            continue
        # 同文件内的局部变量定义表（够用：注入点与定义几乎总在同一 lambda 内）
        vars_ = {m.group(1): m.group(2).strip() for m in VAR_DECL.finditer(txt)}
        for i, ln in enumerate(txt.splitlines(), 1):
            for m in REF_CALL.finditer(ln):
                raw = (m.group(2) or "").strip()
                out[m.group(1)].append((p.name, i, raw, resolve_expr(raw, vars_)))
    return out


LEADING_NUM = re.compile(r'^\(?\s*(-?\d+(?:\.\d+)?)')


def resolve_expr(expr, vars_, depth=0):
    """把表达式里的局部变量递归替换成其定义，直到出现字面量或深度耗尽。"""
    if not expr or depth > 3:
        return expr
    if LEADING_NUM.match(expr):
        return expr
    head = re.match(r'\(?\s*(\w+)', expr)
    if not head:
        return expr
    name = head.group(1)
    if name in vars_:
        return resolve_expr(vars_[name], vars_, depth + 1)
    return expr


def magnitude_class(expr):
    """判断调用方注入幅度的单位：fraction / points / unknown。"""
    if not expr:
        return "none"          # ref(id) 无 vars，条目自带常量
    m = LEADING_NUM.match(expr)
    if not m:
        return "unknown"       # 仍无法定位字面量 → 必须人工判
    v = abs(float(m.group(1)))
    if v == 0:
        return "unknown"
    return "fraction" if v < 1 else "points"


def main():
    pct = pct_attrs()
    p2c = const_of_path()
    desc = descriptions()
    call = callers()

    # 收集所有 percentage 属性上的 $value 模板条目
    rows = []
    for p in sorted(ENTRY_DIR.glob("*.json")):
        try:
            d = json.loads(p.read_text(encoding="utf-8"))
        except Exception:
            continue
        if d.get("type") != "attribute":
            continue
        attr, amt, op = d.get("attribute"), str(d.get("amount", "")), d.get("operation", "")
        if attr not in pct or "$value" not in amt:
            continue
        norm = amt.replace(" ", "")
        div100 = "/100" in norm
        rows.append({
            "path": p.stem, "const": p2c.get(p.stem), "attr": attr, "op": op,
            "amount": amt, "div100": div100,
            "desc": desc.get(p.stem, ""),
            "callers": call.get(p2c.get(p.stem) or "\0", []),
        })

    # 家族 = (attr, op)，统计族内约定分布
    fam = defaultdict(lambda: {"div": 0, "bare": 0})
    for r in rows:
        k = (r["attr"], r["op"])
        fam[k]["div" if r["div100"] else "bare"] += 1

    buckets = defaultdict(list)
    for r in rows:
        cs = r["callers"]
        if cs:
            classes = {magnitude_class(rv) for _, _, _, rv in cs}
            if r["div100"]:
                # 条目烘 /100 → 调用方必须传整数百分点；传小数即反向缩小 100 倍
                tag = "W-DIV" if "fraction" in classes else (
                    "W-UNK" if classes <= {"unknown", "none"} else "W-OK")
            else:
                # 条目裸 $value → 调用方必须传小数；传整数百分点即放大 100 倍
                tag = "W-BAD" if "points" in classes else (
                    "W-UNK" if classes <= {"unknown"} else "W-OK")
        else:
            k = fam[(r["attr"], r["op"])]
            tag = "U-MIX" if (k["div"] > 0 and k["bare"] > 0) else "U-UNI"
        r["tag"] = tag
        buckets[tag].append(r)

    order = ["W-BAD", "W-DIV", "W-UNK", "U-MIX", "W-OK", "U-UNI"]
    print(f"percentage 属性上的 $value 模板条目：{len(rows)} 条")
    for t in order:
        print(f"  {t:6s} {len(buckets[t]):4d}")

    lines = ["# A4-v2 单位一致性审计（调用方感知）", "",
             f"percentage 属性 + `$value` 模板条目共 **{len(rows)}** 条。", "",
             "| 分级 | 条数 | 含义 |", "|---|---|---|",
             "| W-BAD | %d | 已接线且调用方传整数百分点 + 条目裸 `$value` → **真 100x 放大** |" % len(buckets["W-BAD"]),
             "| W-DIV | %d | 已接线且调用方传小数 + 条目烘 `/100` → **反向缩小 100 倍** |" % len(buckets["W-DIV"]),
             "| W-UNK | %d | 已接线但幅度为不可解析表达式 → **须人工判** |" % len(buckets["W-UNK"]),
             "| U-MIX | %d | 未接线，且 (属性,operation) 族内约定不一致 → **接线前须统一** |" % len(buckets["U-MIX"]),
             "| W-OK  | %d | 已接线且单位自洽 |" % len(buckets["W-OK"]),
             "| U-UNI | %d | 未接线但族内约定一致 |" % len(buckets["U-UNI"]),
             ""]

    lines += ["## 家族约定分布（div100 / bare）", "",
              "| 属性 | operation | `/100` | 裸 `$value` | 是否分裂 |", "|---|---|---|---|---|"]
    for (a, o), v in sorted(fam.items(), key=lambda kv: -(min(kv[1]['div'], kv[1]['bare']))):
        split = "**是**" if v["div"] and v["bare"] else "否"
        lines.append(f"| `{a}` | {o} | {v['div']} | {v['bare']} | {split} |")
    lines.append("")

    for t in order:
        if not buckets[t]:
            continue
        lines += [f"## {t}（{len(buckets[t])} 条）", "",
                  "| id | 属性 | op | amount | 调用方 | 描述 |", "|---|---|---|---|---|---|"]
        for r in buckets[t]:
            cs = "; ".join(f"{f}:{l}(`{e[:20]}`→`{rv[:20]}`)" for f, l, e, rv in r["callers"][:2]) or "—"
            lines.append(f"| `{r['path']}` | `{r['attr']}` | {r['op']} | `{r['amount']}` | {cs} | {r['desc'][:52]} |")
        lines.append("")

    OUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"\n报告 → {OUT}")

    # 控制台直出真 bug 与需统一项
    for t in ("W-BAD", "W-DIV", "W-UNK"):
        if buckets[t]:
            print(f"\n--- {t} ---")
            for r in buckets[t]:
                cs = "; ".join(f"{f}:{l} {e[:24]} => {rv[:24]}" for f, l, e, rv in r["callers"][:1]) or "未接线"
                print(f"  {r['path'][:40]:42s} {r['attr'][:24]:26s} {r['amount'][:12]:13s} {cs}")


if __name__ == "__main__":
    main()
