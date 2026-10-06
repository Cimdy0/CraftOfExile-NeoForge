# -*- coding: utf-8 -*-
"""计算符（operator）正确性审计（StatGen）。

核心真问题（来自 StatGen.pctAdd 注释）：零基百分比容器属性
（registerPercentage(name, 0.0)，如 AILMENT_CHANCE / BLOCK_CHANCE / RESISTANCES /
AVOID_AILMENT_CHANCE / PROJECTILE_PIERCE_CHANCE ...）上若用 inc()（ADD_MULTIPLIED_BASE），
聚合式 out = original + Σadd + attributeBaseValue * ΣmultBase 中 attributeBaseValue==0，
于是一直贡献 0 —— 数据存在但运行时静默失效（死修饰）。

本审计聚焦：
  A) DEAD inc：inc() / valueMod(ADD_MULTIPLIED_BASE) 作用在零基属性 -> 静默失效（确凿 bug）
  B) 可疑 pctAdd/add 作用在 base>0 且名称含 INC/INCREASED 的属性 -> 可能该用 inc（需复核，低置信）
  C) 信息项：INC_*（缩写后）计算符分布（注意：名字带 INC 不代表必须 inc，
     计算符由属性类型决定：零基容器用 pctAdd/add、chance 用 chance、minion 用 entityMod…）

属性基数来源：AttributeRegister.java 的 registerPercentage/register 调用首参。
vanilla Attributes.*（ATTACK_SPEED 等）基数恒为正，视为安全。
"""
import re, json, os
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
ATTRREG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")

# ---------- 1. 解析 AttributeRegister 基数 ----------
attr_text = open(ATTRREG, encoding="utf-8").read()
# CONST = registerPercentage("name", BASE[, ...])  |  CONST = register("name", BASE)
base_map = {}   # CONST -> float base
pat = re.compile(r"public static Holder<Attribute>\s+(\w+)\s*=\s*register(?:Percentage)?\(\s*\"[^\"]+\"\s*,\s*([0-9.]+)")
for m in pat.finditer(attr_text):
    base_map[m.group(1)] = float(m.group(2))

def attr_base(const):
    return base_map.get(const, None)   # None = 未知/vanilla（视为正基，安全）

# ---------- 2. 解析 StatGen 注册调用 ----------
OPS = ["inc","add","pctAdd","more","chance","faceMod","valueMod","entityMod",
       "conditionMod","regenMod","recoverPctOnEvent","recoverFlatOnEvent","esRechargeMod"]
sg_text = open(STATGEN, encoding="utf-8").read()
lines = sg_text.split("\n")
call_pat = re.compile(r"\b(" + "|".join(OPS) + r")\(map,\s*(?:StatIds\.(\w+)\s*,\s*)?(AttributeRegister|Attributes)\.(\w+)\s*,")

rows = []
for i, line in enumerate(lines, 1):
    m = call_pat.search(line)
    if not m:
        continue
    op, sid, pref, attr = m.group(1), m.group(2), m.group(3), m.group(4)
    operation = None
    if op == "valueMod":
        om = re.search(r"Operation\.(\w+)", line)
        operation = om.group(1) if om else "UNKNOWN"
    rows.append({"line": i, "op": op, "sid": sid, "pref": pref, "attr": attr, "operation": operation})

# ---------- 3. 检查 ----------
dead = []      # A: inc 族作用在零基属性
suspect = []   # B: pctAdd/add 作用在 base>0 的 INC 命名属性
for r in rows:
    if r["pref"] != "AttributeRegister":
        continue  # vanilla 属性视为安全
    b = attr_base(r["attr"])
    is_inc_op = (r["op"] == "inc") or (r["op"] == "valueMod" and r["operation"] == "ADD_MULTIPLIED_BASE")
    is_flat_op = (r["op"] == "add") or (r["op"] == "pctAdd") or (r["op"] == "valueMod" and r["operation"] == "ADD_VALUE")
    if b is None:
        continue
    if is_inc_op and abs(b) < 1e-9:
        dead.append({"line": r["line"], "sid": r["sid"], "attr": r["attr"], "base": b,
                     "op": r["op"], "operation": r["operation"]})
    if is_flat_op and b > 1e-9 and r["sid"] and ("INC" in r["sid"].upper() or "INCREASED" in r["sid"].upper()):
        suspect.append({"line": r["line"], "sid": r["sid"], "attr": r["attr"], "base": b,
                        "op": r["op"], "operation": r["operation"]})

# ---------- 4. 信息项：INC_* 计算符分布 ----------
inc_rows = [r for r in rows if r["sid"] and r["sid"].upper().startswith("INC")]
inc_op_dist = Counter(r["op"] for r in inc_rows)

op_dist = Counter(r["op"] for r in rows)
fam = Counter()
for r in rows:
    if r["op"] == "inc": fam["INC"] += 1
    elif r["op"] == "valueMod": fam["INC" if r["operation"]=="ADD_MULTIPLIED_BASE" else "ADD"] += 1
    elif r["op"] in ("esRechargeMod","faceMod"): fam["INC"] += 1
    elif r["op"] == "add": fam["ADD"] += 1
    elif r["op"] == "pctAdd": fam["PCTADD"] += 1
    elif r["op"] == "more": fam["MORE"] += 1
    elif r["op"] == "chance": fam["CHANCE"] += 1
    else: fam["OTHER"] += 1

# ---------- 5. 报告 ----------
rep = []
rep.append("# 计算符正确性审计（StatGen）\n")
rep.append("## 0. 概览\n")
rep.append(f"- 解析注册调用：{len(rows)}")
rep.append(f"- 计算符分布：{dict(op_dist)}")
rep.append(f"- 运算族分布：{dict(fam)}")
rep.append(f"- 属性基数表覆盖：{len(base_map)} 个 AttributeRegister 常量")
rep.append(f"- 缩写后 INC_* 条目：{len(inc_rows)}")
rep.append("")
rep.append("## 1. 确凿问题 A：inc/valueMod(ADD_MULTIPLIED_BASE) 作用在零基属性 = 静默失效\n")
if dead:
    rep.append(f"**{len(dead)} 处（运行时死修饰，建议改 pctAdd 或 add）：**\n")
    for d in dead:
        rep.append(f"- L{d['line']} `{d['sid']}` -> `{d['op']}`"
                   + (f"({d['operation']})" if d['operation'] else "")
                   + f"  on `{d['attr']}`(base={d['base']})")
else:
    rep.append("✅ 未发现 inc 族作用在零基属性（无静默失效死修饰）。\n")
rep.append("")
rep.append("## 2. 需复核 B：pctAdd/add 作用在 base>0 且名称含 INC 的属性\n")
if suspect:
    rep.append(f"**{len(suspect)} 处（可能该用 inc，低置信，建议人工核对）：**\n")
    for s in suspect:
        rep.append(f"- L{s['line']} `{s['sid']}` -> `{s['op']}` on `{s['attr']}`(base={s['base']})")
else:
    rep.append("✅ 未发现（或均合理）。\n")
rep.append("")
rep.append("## 3. 信息项：INC_* 计算符分布（名字带 INC 不等于必须 inc）\n")
rep.append(f"- 分布：{dict(inc_op_dist)}")
rep.append("- 说明：计算符由属性注册基数决定。零基容器（AILMENT_CHANCE/BLOCK_CHANCE/RESISTANCES…）")
rep.append("  必须用 pctAdd/add（inc 会静默失效）；chance 类用 chance；minion 类用 entityMod；")
rep.append("  regen/condition 类用专用 helper。故 INC_* 用非 inc 运算族多数为正确设计。\n")
rep.append("")
rep.append("## 4. 零基属性清单（inc 高危对象）\n")
zero = sorted([(k,v) for k,v in base_map.items() if abs(v) < 1e-9])
rep.append(f"- 共 {len(zero)} 个零基常量：")
rep.append("  " + ", ".join(f"{k}({v})" for k,v in zero))

out_md = "\n".join(rep)
out_dir = os.path.dirname(os.path.abspath(__file__))
open(os.path.join(out_dir, "operator_audit.md"), "w", encoding="utf-8").write(out_md)
json.dump({"total": len(rows), "dead": dead, "suspect": suspect,
           "inc_op_dist": dict(inc_op_dist), "zero_base": [k for k,_ in zero]},
          open(os.path.join(out_dir, "operator_audit.json"), "w", encoding="utf-8"),
          indent=2, ensure_ascii=False)
print(out_md)
print("\n[written]", os.path.join(out_dir, "operator_audit.md"))
