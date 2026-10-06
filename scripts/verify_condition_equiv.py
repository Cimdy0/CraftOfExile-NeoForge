#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校验「StatConditionGen 改构造器」后，runData 重新生成的 condition JSON
与改造前基线（scripts/.cond_baseline）规范化（键排序 + 数组元素排序）后逐文件等价。

文件名本身即内容寻址（partName = 内容词元），故文件名集合相同 + 规范化内容相同 ⇒ 字节等价。
"""
import os
import json

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "scripts", ".cond_baseline")
NEW = os.path.join(ROOT, "src", "generated", "resources", "data", "coe", "coe", "stat", "condition")


def canonicalize(o):
    if isinstance(o, dict):
        out = {}
        for k in sorted(o.keys()):
            out[k] = canonicalize(o[k])
        return out
    if isinstance(o, list):
        items = [canonicalize(x) for x in o]
        items.sort(key=lambda x: json.dumps(x, sort_keys=True, ensure_ascii=False))
        return items
    return o


def main():
    base_files = {f for f in os.listdir(BASE) if f.endswith(".json")}
    new_files = {f for f in os.listdir(NEW) if f.endswith(".json")}

    missing = base_files - new_files
    extra = new_files - base_files
    common = base_files & new_files

    mismatch = []
    for f in sorted(common):
        with open(os.path.join(BASE, f), encoding="utf-8") as fh:
            b = canonicalize(json.load(fh))
        with open(os.path.join(NEW, f), encoding="utf-8") as fh:
            n = canonicalize(json.load(fh))
        if json.dumps(b, sort_keys=True, ensure_ascii=False) != json.dumps(n, sort_keys=True, ensure_ascii=False):
            mismatch.append(f)

    print(f"baseline 文件数 = {len(base_files)}")
    print(f"regenerated 文件数 = {len(new_files)}")
    print(f"共同文件数 = {len(common)}")
    print(f"缺失(改造后消失) = {sorted(missing) if missing else 0}")
    print(f"多余(改造后新增) = {sorted(extra) if extra else 0}")
    print(f"规范化内容不一致 = {mismatch if mismatch else 0}")

    if not missing and not extra and not mismatch:
        print("RESULT: ALL CONDITION ENTRIES EQUIVALENT (构造器改造零回归)")
    else:
        print("RESULT: MISMATCH DETECTED")
        raise SystemExit(1)


if __name__ == "__main__":
    main()
