#!/usr/bin/env python3
"""ref 一致性复检：stat_entry 内 ref ↔ 三字段缓存类字段 ↔ 落盘文件 三者一致。

约定（本会话收口）：
- 落盘资源文件名即裸 base（coe:<base>），由 LoadEvent 加载器截取文件名作为裸 key 存入
  STAT_EFFECT_MAP / STAT_TARGETS_MAP / STAT_CONDITION_MAP。
- 三字段缓存类（StatEffectPaths / StatTargetsPaths / StatConditionPaths）每个 base 生成
  `public static final Identifier X_XXX = id("<base>");`，并用 byName(base) 反查。
- stat_entry JSON 中 effect/targets/conditions 均为 read 形态，ref 为 `coe:<base>`。

核心不变量：
1. 字段缓存类字段集(A) == 落盘文件集(B)（1:1，无残缺无多余）。
2. stat_entry 引用的 ref 集(C) ⊆ A（无悬空 dangling，否则运行期 byName 抛错）。
"""
import json
import os
import re

ROOT = "src/generated/resources/data/coe/coe"
STAT_SUPPORT = "src/main/java/com/cimdy/coe/data/codec/support/stat"

KINDS = {
    "effect":    ("stat_effect",    "StatEffectPaths.java",    "E_"),
    "targets":   ("stat_targets",   "StatTargetsPaths.java",   "T_"),
    "condition": ("stat_condition", "StatConditionPaths.java", "C_"),
}


def extract_bases_from_class(path):
    bases = set()
    with open(path, encoding="utf-8") as f:
        for line in f:
            for m in re.finditer(r'id\("([0-9A-Za-z_]+)"\)', line):
                bases.add(m.group(1))
    return bases


def extract_filenames(dirpath):
    return {os.path.splitext(f)[0] for f in os.listdir(dirpath) if f.endswith(".json")}


def extract_refs_from_entries():
    refs = {"effect": set(), "targets": set(), "condition": set()}
    d = os.path.join(ROOT, "stat_entry")
    for name in os.listdir(d):
        if not name.endswith(".json"):
            continue
        with open(os.path.join(d, name), encoding="utf-8") as f:
            obj = json.load(f)
        eff = obj.get("effect")
        if isinstance(eff, dict) and isinstance(eff.get("ref"), str) and eff["ref"].startswith("coe:"):
            refs["effect"].add(eff["ref"][4:])
        tgt = obj.get("targets")
        if isinstance(tgt, dict) and isinstance(tgt.get("ref"), str) and tgt["ref"].startswith("coe:"):
            refs["targets"].add(tgt["ref"][4:])
        cond = obj.get("conditions")
        if isinstance(cond, list):
            for el in cond:
                if isinstance(el, dict) and isinstance(el.get("ref"), str) and el["ref"].startswith("coe:"):
                    refs["condition"].add(el["ref"][4:])
    return refs


def main():
    all_ok = True
    refs = extract_refs_from_entries()
    print(f"stat_entry 文件总数: {len(os.listdir(os.path.join(ROOT, 'stat_entry')))}")
    for kind, (datadir, clsfile, _prefix) in KINDS.items():
        A = extract_bases_from_class(os.path.join(STAT_SUPPORT, clsfile))
        B = extract_filenames(os.path.join(ROOT, datadir))
        C = refs[kind]
        print(f"\n=== {kind} ===")
        print(f"  字段缓存类字段数 : {len(A)}")
        print(f"  落盘文件数       : {len(B)}")
        print(f"  stat_entry 引用数: {len(C)}")

        dangling = C - A      # 引用了但字段类未缓存 → 运行期 byName 抛错
        orphan_file = B - A   # 落盘了但字段类未缓存 → 字段类残缺
        dead_cache = A - C    # 缓存了但无条目引用 → 死缓存（可接受，仅记录）

        if A != B:
            print(f"  ✗ 字段类 ≠ 文件 (差集大小 {len(A ^ B)})")
            if orphan_file:
                print(f"    文件缺字段: {sorted(orphan_file)}")
            if A - B:
                print(f"    字段缺文件: {sorted(A - B)}")
            all_ok = False
        else:
            print(f"  ✓ 字段类与文件 1:1 一致")

        if dangling:
            print(f"  ✗ 悬空引用 (ref 不在字段类): {sorted(dangling)}")
            all_ok = False
        else:
            print(f"  ✓ 无悬空引用 (C ⊆ A)")

        if dead_cache:
            print(f"  · 死缓存 ({len(dead_cache)} 个未引用): {sorted(dead_cache)[:8]}{'...' if len(dead_cache) > 8 else ''}")

    print("\n" + ("结果: 全部一致 ✅" if all_ok else "结果: 发现不一致 ❌"))


if __name__ == "__main__":
    main()
