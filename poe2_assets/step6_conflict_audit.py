# -*- coding: utf-8 -*-
"""
Step6-A: StatGen.java 同键重复注册审计（真冲突组识别）

背景：step5_exec.py --apply 完成 KEEP/PARK/A/DROP 四分处置后，StatGen 内出现
同一 StatIds.X 被注册两次的情况。其中一部分属 extraDamage 系 helper 的
"派生正常重复"（单常量 → 多 map 键），必须排除；剩余为真冲突（同键 map.put
后者覆盖前者 = 静默语义丢失）。

输出：
  poe2_assets/_test/step6_conflicts.md      人工裁决用报告
  poe2_assets/_test/step6_conflicts.json    机器可读（供修复脚本消费）
"""
import json
import os
import re
import sys

ROOT = "."
STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
CORPUS = "poe2_assets/stat_templates.txt"
OUT_MD = "poe2_assets/_test/step6_conflicts.md"
OUT_JSON = "poe2_assets/_test/step6_conflicts.json"

# extraDamage 系 helper：单 StatIds 常量派生多个 map 键（id.withPath(...+"_from_"+src)）
DERIVED_HELPERS = {
    "extraDamage",
    "extraDamageOnGround",
    "extraDamageVsStatus",
    "extraDamageWhileShapeshifted",
}


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def split_statements(body):
    """切分语句：仅 ()[] 参与平衡判定，`;` 与 `{` `}` 均作边界。

    注意：不能把 `{` 计入 depth——否则类体/方法体使 depth 恒 >0，整个文件被当成
    一条语句（曾踩，审计输出全 0）。
    """
    out = []
    depth = 0
    i = 0
    start = 0
    n = len(body)

    def flush(end):
        nonlocal start
        seg = body[start:end]
        if seg.strip():
            out.append((start, end, seg))
        start = end

    while i < n:
        c = body[i]
        if c == '"':
            i += 1
            while i < n:
                if body[i] == "\\":
                    i += 2
                    continue
                if body[i] == '"':
                    break
                i += 1
        elif c == "'":
            i += 1
            while i < n:
                if body[i] == "\\":
                    i += 2
                    continue
                if body[i] == "'":
                    break
                i += 1
        elif c == "/" and i + 1 < n and body[i + 1] == "/":
            while i < n and body[i] != "\n":
                i += 1
            continue
        elif c == "/" and i + 1 < n and body[i + 1] == "*":
            j = body.find("*/", i + 2)
            i = (j + 2) if j >= 0 else n
            continue
        elif c in "([":
            depth += 1
        elif c in ")]":
            depth -= 1
        elif c in "{}" and depth == 0:
            flush(i + 1)
        elif c == ";" and depth == 0:
            flush(i + 1)
        i += 1
    flush(n)
    return out


def line_of(txt, off):
    return txt.count("\n", 0, off) + 1


def code_start(body, a, b):
    """返回语句内「首个非空白、非注释」字符的绝对偏移。

    split_statements 的 start 是上一个 `;` 之后一位，通常落在**上一行行尾**，
    直接 line_of(start) 会整体偏移 1 行（曾踩：报告 L911 实际 L912）。
    另外语句可能以块/行注释起头，取代码起点才能精确定位待改行。
    """
    i = a
    n = b
    while i < n:
        c = body[i]
        if c in " \t\r\n":
            i += 1
        elif c == "/" and i + 1 < n and body[i + 1] == "/":
            j = body.find("\n", i)
            i = (j + 1) if j >= 0 else n
        elif c == "/" and i + 1 < n and body[i + 1] == "*":
            j = body.find("*/", i + 2)
            i = (j + 2) if j >= 0 else n
        else:
            return i
    return a


def main():
    src = read(STATGEN)
    stmts = split_statements(src)

    # 收集每条语句引用的 StatIds 常量 + 使用的 helper
    RE_CONST = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
    RE_CALL = re.compile(r"(?:^|[^\w.$])([a-zA-Z_][\w.]*)\s*\(")
    # 作用域/标签也参与签名：仅比属性会把 CHARM vs MINION 这类语义差异误判成良性重复
    RE_TAG = re.compile(r"(?:StatTag|ResourceType|TargetSelector|ChargeType|EventName)\.[A-Za-z_][\w.()\"]*")

    per_const = {}  # name -> [ {line, helper, stmt} ]
    for a, b, seg in stmts:
        names = RE_CONST.findall(seg)
        if not names:
            continue
        calls = {c.split(".")[-1] for c in RE_CALL.findall(seg)}
        cs = code_start(src, a, b)
        code = src[cs:b]
        m = RE_CALL.search(" " + code)
        helper = m.group(1) if m else "?"
        derived = bool(DERIVED_HELPERS & calls)
        for nm in set(names):
            per_const.setdefault(nm, []).append({
                "line": line_of(src, cs),
                "code_start": cs,
                "stmt_start": a,
                "end": b,
                "helper": helper,
                "derived": derived,
                "tags": sorted(set(RE_TAG.findall(code))),
                "stmt": " ".join(code.split())[:400],
            })

    dup = {k: v for k, v in per_const.items() if len(v) > 1}
    # 排除全部由派生 helper 产生的重复
    real = {}
    derived_only = {}
    for k, v in dup.items():
        if all(x["derived"] for x in v):
            derived_only[k] = v
        else:
            real[k] = v

    # 提取每条语句作用的属性名（AttributeRegister.X / Attributes.X）
    RE_ATTR = re.compile(r"(?:AttributeRegister|Attributes)\.([A-Za-z_][A-Za-z0-9_]*)")
    for k, v in real.items():
        for x in v:
            x["attrs"] = sorted(set(RE_ATTR.findall(x["stmt"])))

    # 语料：常量名 -> 描述模板。
    # 注意：stat_templates.txt 是纯模板行（无 KEY=），常量名由 gen_stat_ids.py 规范化得出。
    # 直接解析 StatIds.java 的「// 模板」+ 下一行常量声明取配对，比重算规范化更可靠。
    # 同时标记该常量是否落在 BEGIN/END MANUAL 段（= KEEP/PARK 手工通道，本无语料）。
    corpus = {}
    manual = set()
    ids_src = read(STATIDS).splitlines()
    in_manual = False
    pending = None
    for line in ids_src:
        s = line.strip()
        if "BEGIN MANUAL" in s:
            in_manual = True
        elif "END MANUAL" in s:
            in_manual = False
        if s.startswith("//") and "MANUAL" not in s:
            pending = s[2:].strip()
            continue
        m = re.search(r"public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=", s)
        if m:
            if pending:
                corpus[m.group(1)] = pending
            if in_manual:
                manual.add(m.group(1))
        pending = None

    # AttributeRegister 占位属性（含 _N 后缀）
    attrreg = read(ATTRREG) if os.path.exists(ATTRREG) else ""
    placeholder_line = None
    for i, l in enumerate(attrreg.splitlines(), 1):
        if "Wave-12" in l and "551" in l:
            placeholder_line = i
            break
    ph_attrs = set()
    if placeholder_line:
        for i, l in enumerate(attrreg.splitlines(), 1):
            if i <= placeholder_line:
                continue
            m = re.search(r"public static final (?:Holder<Attribute>|DeferredHolder<[^>]+>)\s+([A-Z][A-Z0-9_]*)", l)
            if m:
                ph_attrs.add(m.group(1))

    # 分类：same_all（两条属性集合完全相同 → 良性可合并）/ _N 受害 / 多属性合法 / 需裁决
    buckets = {"BENIGN_SAME": [], "PLACEHOLDER_N": [], "MULTI_ATTR": [], "HIJACK_SUSPECT": []}
    for k in sorted(real):
        v = real[k]
        # 签名 = 属性 + 标签/作用域 + helper（helper 决定值约定 RAW/DIV100，必须纳入）
        sigs = [(tuple(x["attrs"]), tuple(x["tags"]), x["helper"]) for x in v]
        attrs_flat = [a for x in v for a in x["attrs"]]
        entry = {
            "const": k,
            "corpus": corpus.get(k, ""),
            "manual": k in manual,
            "occurrences": v,
        }
        entry["placeholder"] = sorted(set(attrs_flat) & ph_attrs)
        if len(set(sigs)) == 1:
            buckets["BENIGN_SAME"].append(entry)
        elif any(re.search(r"_\d+$", a) for a in attrs_flat):
            buckets["PLACEHOLDER_N"].append(entry)
        elif len(set(attrs_flat)) <= 1:
            # 属性相同但 helper/标签不同 → 值约定或作用域分歧，必须人工
            buckets["MULTI_ATTR"].append(entry)
        else:
            buckets["HIJACK_SUSPECT"].append(entry)

    os.makedirs(os.path.dirname(OUT_MD), exist_ok=True)
    with open(OUT_JSON, "w", encoding="utf-8") as f:
        json.dump({"real": buckets, "derived_only": sorted(derived_only)}, f, ensure_ascii=False, indent=1)

    L = []
    L.append("# Step6-A StatGen 同键重复注册审计\n")
    L.append("| 指标 | 值 |")
    L.append("| --- | --- |")
    L.append("| StatIds 引用语句总数 | %d |" % sum(1 for a, b, s in stmts if "StatIds." in s))
    L.append("| 出现 >1 次的常量 | %d |" % len(dup))
    L.append("| 其中 extraDamage 派生正常 | %d |" % len(derived_only))
    L.append("| **真冲突组** | **%d** |" % len(real))
    L.append("")
    for bk, title in (
        ("BENIGN_SAME", "A. 属性集合完全相同（良性重复 → 合并删一条）"),
        ("PLACEHOLDER_N", "B. 含 `_N` 占位属性（受害 → 删 `_N` 版）"),
        ("MULTI_ATTR", "C. 单属性但语句不同（需核对）"),
        ("HIJACK_SUSPECT", "D. 属性不同（合法多属性 or 同名劫持 → 逐条裁决）"),
    ):
        rows = buckets[bk]
        L.append("## %s — %d 组\n" % (title, len(rows)))
        for e in rows:
            L.append("### `%s`" % e["const"])
            tag = "手工通道(KEEP/PARK)" if e["manual"] else "语料常量"
            if e["corpus"]:
                L.append("【%s】语料：`%s`" % (tag, e["corpus"]))
            else:
                L.append("【%s】语料：**（无模板注释）**" % tag)
            for x in e["occurrences"]:
                L.append("- L%d helper=`%s` attrs=%s tags=%s" % (
                    x["line"], x["helper"], x["attrs"] or "—", x["tags"] or "—"))
                L.append("  ```java")
                L.append("  " + x["stmt"])
                L.append("  ```")
            if e.get("placeholder"):
                L.append("- 占位属性（Wave-12 之后注册）：%s" % ", ".join(e["placeholder"]))
            L.append("")
    with open(OUT_MD, "w", encoding="utf-8") as f:
        f.write("\n".join(L))

    print("真冲突组 %d / 派生正常 %d / 重复常量合计 %d" % (len(real), len(derived_only), len(dup)))
    for bk in ("BENIGN_SAME", "PLACEHOLDER_N", "MULTI_ATTR", "HIJACK_SUSPECT"):
        print("  %-16s %d" % (bk, len(buckets[bk])))


if __name__ == "__main__":
    main()
