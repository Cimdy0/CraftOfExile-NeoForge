#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
事件层瘦身检测（静态、禁 runData）：确保「事件端只派发、机制入 helper/engine」。

检查对象：src/main/java/com/cimdy/coe/event/**/*.java 中带 @EventBusSubscriber 的类。

规则：
  A [违规] 机制内联：存在非 @SubscribeEvent 的 static 方法（监听器之外的方法＝机制实现，
     应迁入 com.cimdy.coe.data.helper / helper.engine）。豁免：方法名以 register/on 开头且为 public 的
     登记式入口、以及内部类方法。
  B [违规] 硬编码常量：static final 数值字段（机制常量应入属性/技能数据，见死线谱系 #48）。
  C [告警] 文件行数 > LOC_WARN（事件类膨胀成第二引擎的信号）。

用法：
    python scripts/event_thin_audit.py            # 报告（退出码 0）
    python scripts/event_thin_audit.py --strict   # 有 A/B 类违规时退出码 1（可作硬门禁）
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EVENT_DIR = os.path.join(ROOT, "src", "main", "java", "com", "cimdy", "coe", "event")
LOC_WARN = 250

# 数据生成/登记类不在「派发层」语义内（它们本就是声明式内容）
EXEMPT_FILES = {
    "LoadEvent.java", "GenEvent.java", "EventKeyAuditProvider.java",
}

METHOD_DECL = re.compile(
    r"^\s{4,}(?:public|private|protected)\s+(?:static\s+)?[\w<>\[\],.?\s]+?\s(\w+)\s*\([^;]*\)\s*\{")
CONST_DECL = re.compile(r"^\s*(?:public|private|protected)\s+static\s+final\s+"
                        r"(?:double|float|int|long)\s+(\w+)\s*=")


def scan_file(path):
    """返回 (a_violations, b_violations, loc)"""
    lines = open(path, encoding="utf-8").read().splitlines()
    a, b = [], []
    prev = ""
    prev_prev = ""
    for i, line in enumerate(lines, 1):
        m = METHOD_DECL.match(line)
        if m:
            name = m.group(1)
            annotated = "@SubscribeEvent" in prev or "@SubscribeEvent" in prev_prev
            # 豁免：登记/引导入口（public register*/init*）、内部类缩进更深（>=8 空格）
            indent = len(line) - len(line.lstrip())
            exempt = (name.startswith("register") or name.startswith("init")) and line.strip().startswith("public")
            if not annotated and not exempt and indent < 8:
                a.append((i, name, line.strip()[:80]))
        c = CONST_DECL.match(line)
        if c:
            b.append((i, c.group(1), line.strip()[:80]))
        prev_prev = prev
        prev = line
    return a, b, len(lines)


def main():
    strict = "--strict" in sys.argv
    total_a = total_b = 0
    warns = []
    dispatch_only = []  # 仅因监听器多而膨胀（无内联机制 / 无硬编码常量）——属派发表，不算违规
    for dirpath, _, files in os.walk(EVENT_DIR):
        for fn in sorted(files):
            if not fn.endswith(".java") or fn in EXEMPT_FILES:
                continue
            path = os.path.join(dirpath, fn)
            src = open(path, encoding="utf-8").read()
            if "@EventBusSubscriber" not in src:
                continue
            rel = os.path.relpath(path, ROOT).replace("\\", "/")
            a, b, loc = scan_file(path)
            if loc > LOC_WARN:
                (dispatch_only if (not a and not b) else warns).append((rel, loc))
            if a or b:
                print(f"[A/B] {rel}  (LOC={loc})")
                for i, name, raw in a:
                    print(f"   A 机制内联   L{i}: {name}()  {raw}")
                    total_a += 1
                for i, name, raw in b:
                    print(f"   B 硬编码常量 L{i}: {name}  {raw}")
                    total_b += 1
    if warns:
        print(f"[C] 事件类行数 > {LOC_WARN}（膨胀告警：类内仍有内联机制或常量，应继续抽离）")
        for rel, loc in warns:
            print(f"   C 膨胀       {rel}  LOC={loc}")
    if dispatch_only:
        print(f"[C'] 派发表型大文件（行数 > {LOC_WARN} 但无内联机制/常量，可接受）")
        for rel, loc in dispatch_only:
            print(f"   C' 派发表     {rel}  LOC={loc}")
    print(f"SUMMARY: A_mechanism_inline={total_a} B_hardcoded_const={total_b} C_bloat={len(warns)}"
          f" C_dispatch={len(dispatch_only)}")
    if strict and (total_a or total_b):
        sys.exit(1)


if __name__ == "__main__":
    main()
