#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""PER_/FOR_EACH 线性缩放族 scout。

从 node_stat_map.json 提取「per / for each / every / up to a max」形态且尚未接线的
StatIds 常量，输出常量名 + 语料原文 + 模板，供逐条人工核验后接线。

R3 纪律：本脚本只做 scout（列清单 + 附原文），不生成接线代码、不猜 divisor。
divisor / percent 必须由人从 raw 原文读取后写入 Java。
"""
import json
import re
import os
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(ROOT)

MAP = os.path.join(ROOT, "node_stat_map.json")
WIRINGS = os.path.join(PROJ, "src", "main", "java", "com", "cimdy", "coe",
                       "data", "gen", "stat", "wirings")

# 缩放形态关键词（作用于归一 path）
SCALE_PAT = re.compile(r"(_per_|^per_|_for_each_|^for_each_|_every_|^every_|_up_to_)")


def wired_constants():
    """扫 wirings/*.java，收集所有已出现的 StatIds.XXX 常量名。"""
    names = set()
    pat = re.compile(r"StatIds\.([A-Z0-9_]+)")
    for fn in os.listdir(WIRINGS):
        if not fn.endswith(".java"):
            continue
        with open(os.path.join(WIRINGS, fn), encoding="utf-8") as f:
            names.update(pat.findall(f.read()))
    return names


def main():
    with open(MAP, encoding="utf-8") as f:
        by_node = json.load(f)

    wired = wired_constants()

    # 按常量去重，聚合所有 raw 原文 + 解析出的 values（含单位）
    agg = {}
    for stats in by_node.values():
        for e in stats:
            const = e.get("constant")
            path = e.get("path") or ""
            if not const or not SCALE_PAT.search(path):
                continue
            rec = agg.setdefault(const, {
                "constant": const,
                "path": path,
                "template": e.get("template"),
                "raws": [],
                "values": [],
                "wired": const in wired,
            })
            raw = e.get("raw")
            if raw and raw not in rec["raws"]:
                rec["raws"].append(raw)
                rec["values"].append([(v.get("value"), v.get("unit"))
                                      for v in e.get("values", [])])

    unwired = sorted([v for v in agg.values() if not v["wired"]],
                     key=lambda x: x["path"])
    already = sorted([v for v in agg.values() if v["wired"]], key=lambda x: x["path"])

    out = {
        "total_scale_family": len(agg),
        "wired": len(already),
        "unwired": len(unwired),
        "unwired_entries": unwired,
        "wired_constants": [v["constant"] for v in already],
    }
    dst = os.path.join(ROOT, "per_scaling_scout.json")
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)

    print(f"scale family total={len(agg)} wired={len(already)} unwired={len(unwired)}")
    print(f"-> {dst}")
    # 分组打印：按 raw 是否含 attribute 名区分主属性缩放子族
    ATTRS = ("[Strength]", "[Dexterity]", "[Intelligence]", "maximum Mana",
             "[Accuracy]", "maximum Life", "[Evasion]", "[Armour]", "[EnergyShield]")
    print("\n=== 属性缩放子族（raw 含属性名，纯数据可解）===")
    for v in unwired:
        if any(a in r for r in v["raws"] for a in ATTRS):
            print(f"  {v['constant']}")
            for r, vals in zip(v["raws"], v["values"]):
                print(f"      raw: {r}   values={vals}")
    print("\n=== 其余缩放子族（需计数/装备读值节点核验）===")
    for v in unwired:
        if not any(a in r for r in v["raws"] for a in ATTRS):
            print(f"  {v['constant']}")
            for r, vals in zip(v["raws"], v["values"]):
                print(f"      raw: {r}   values={vals}")


if __name__ == "__main__":
    main()
