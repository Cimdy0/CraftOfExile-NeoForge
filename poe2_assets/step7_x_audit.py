# -*- coding: utf-8 -*-
"""Step 7: 审计 X_（数值占位）前缀一致性。

判定：
  - 常量名以 X_ 开头  ==  "应携带数值"（由模板 {X} 派生）
  - StatGen 注册时经 inc/more/add/chance/valueMod/penetrate/extraDamage*/recoup/
    recoverPctOnEvent 等 value-helper 注册  ==  "实际携带数值"
  - 仅经 map.put(ConstantStatEffect / TagStatEffect / ImmuneStatEffect / FlatFlag...) 或
    纯 tag 接线  ==  "不携带数值（常量/标记型）"

输出四类：
  OK_X_VALUE      : X_ 且 value-helper       （一致）
  OK_NOX_FLAG     : 非X_ 且 非value-helper   （一致）
  MIS_X_NOVALUE   : X_ 但 仅非value 注册     （疑似应去 X_）
  MIS_NOX_VALUE   : 非X_ 但 value-helper     （疑似应加 X_）
"""
import re, os

STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"

VALUE_HELPERS = {
    "inc", "more", "add", "chance", "valueMod", "penetrate",
    "extraDamage", "extraDamageOnGround", "extraDamageVsStatus",
    "extraDamageWhileShapeshifted", "recoup", "recoverPctOnEvent",
    "dtcReroute", "regen", "recover", "convert", "gainOnEvent",
    "spendOnEvent", "addToResource",
}
# 这些 map.put 形态视为"非数值"（常量/标记/免疫/纯 tag）
NONVALUE_PATTERNS = [
    "ConstantStatEffect", "TagStatEffect", "ImmuneStatEffect",
    "FlatFlagStatEffect", "FlagStatEffect", "BooleanStatEffect",
]


def split_statements(src):
    """按顶层语句切分（括号平衡 + 字符串/注释跳过）。"""
    out, depth, i, start, n = [], 0, 0, 0, len(src)
    while i < n:
        c = src[i]
        if c == '"':
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2; continue
                if src[i] == '"':
                    break
                i += 1
        elif c == "'":
            i += 1
            while i < n:
                if src[i] == '\\':
                    i += 2; continue
                if src[i] == "'":
                    break
                i += 1
        elif c == '/' and i + 1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n':
                i += 1
            continue
        elif c == '/' and i + 1 < n and src[i+1] == '*':
            j = src.find("*/", i+2)
            i = (j+2) if j >= 0 else n
            continue
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == ';' and depth == 0:
            seg = src[start:i+1]
            if seg.strip():
                out.append(seg)
            start = i + 1
        i += 1
    if src[start:].strip():
        out.append(src[start:])
    return out


def line_of(txt, off):
    return txt[:off].count("\n") + 1


RE_CONST = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)\b")
RE_CALL = re.compile(r"(?:^|[^.\w$])([A-Za-z_][\w.]*)\s*\(")


def main():
    src = open(STATGEN, encoding="utf-8").read()
    # per-constant -> set of (helper, value?)
    per = {}
    for seg in split_statements(src):
        names = RE_CONST.findall(seg)
        if not names:
            continue
        calls = {c.split(".")[-1] for c in RE_CALL.findall(seg)}
        helper = None
        for c in RE_CALL.findall(seg):
            base = c.split(".")[-1]
            if base in VALUE_HELPERS:
                helper = base
                break
        is_value = bool(VALUE_HELPERS & calls)
        is_nonvalue_put = ("map.put" in calls) and any(p in seg for p in NONVALUE_PATTERNS)
        # 仅常量/标记型（非 value-helper，且 map.put 指向非数值 effect，或纯 tag）
        only_flag = (not is_value) and (is_nonvalue_put or ("TagStatEffect" in seg) or ("map.put" not in calls and not is_value))
        for nm in set(names):
            per.setdefault(nm, {"value": False, "flag": False, "helpers": set(), "lines": []})
            d = per[nm]
            d["helpers"].add(helper or "(unknown)")
            if is_value:
                d["value"] = True
            if only_flag:
                d["flag"] = True
            # 记录行号
            d["lines"].append(line_of(src, src.find(seg)))

    # 分类
    ok_x_val, ok_nox_flag, mis_x_noval, mis_nox_val = [], [], [], []
    for nm, d in per.items():
        has_x = nm.startswith("X_")
        if d["value"]:
            if has_x:
                ok_x_val.append(nm)
            else:
                mis_nox_val.append((nm, sorted(d["helpers"]), d["lines"][:3]))
        else:
            # 不携带数值
            if has_x:
                mis_x_noval.append((nm, sorted(d["helpers"]), d["lines"][:3]))
            else:
                ok_nox_flag.append(nm)

    print("=== 接线常量按 X_ 前缀 × 是否数值 分类 ===")
    print("OK  X_+数值      : %d" % len(ok_x_val))
    print("OK  非X_+非数值  : %d" % len(ok_nox_flag))
    print("MIS X_但无数值   : %d" % len(mis_x_noval))
    print("MIS 非X_但有数值 : %d" % len(mis_nox_val))

    with open("poe2_assets/_test/step7_x_audit.md", "w", encoding="utf-8") as f:
        f.write("# Step 7 — X_ 数值前缀一致性审计\n\n")
        f.write("| 类别 | 含义 | 数量 |\n| --- | --- | --- |\n")
        f.write("| OK_X_VALUE | X_ 且经数值 helper 注册（一致） | %d |\n" % len(ok_x_val))
        f.write("| OK_NOX_FLAG | 非X_ 且非数值注册（一致） | %d |\n" % len(ok_nox_flag))
        f.write("| MIS_X_NOVALUE | X_ 前缀但仅常量/标记型注册（疑似应去 X_） | %d |\n" % len(mis_x_noval))
        f.write("| MIS_NOX_VALUE | 非X_ 但经数值 helper 注册（疑似应加 X_） | %d |\n\n" % len(mis_nox_val))

        f.write("## MIS_X_NOVALUE（X_ 但无数值，候选去 X_）\n")
        for nm, hs, ls in sorted(mis_x_noval):
            f.write("- `%s`  helper=%s  L%s\n" % (nm, ",".join(hs), ls))
        f.write("\n## MIS_NOX_VALUE（非X_ 但有数值，候选加 X_）\n")
        for nm, hs, ls in sorted(mis_nox_val):
            f.write("- `%s`  helper=%s  L%s\n" % (nm, ",".join(hs), ls))

    print("报告已写 poe2_assets/_test/step7_x_audit.md")


if __name__ == "__main__":
    main()
