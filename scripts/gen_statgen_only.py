# -*- coding: utf-8 -*-
"""仅重建 StatGen.java（buildEntries），不触碰 EFF/COND/TGT Ids/Gen。

背景：rebuild_gen.py 从 effect/target/condition JSON 重建 EFF/COND/TGT 时，其命名与接线对齐的
green 源（CFR 恢复）存在漂移，故 EFF/COND/TGT 保留 green 源不动。StatGen 仅依赖 StatIds
（树相关，PNT 节点移除后须重建）与 entry JSON，故此处单独重建它，避免使用全量 rebuild_gen
污染 green 的 EFF/COND/TGT。

复用 rebuild_gen 的 statgen_java() 模板与 upper() 命名函数，保证 StatIds/EFF/TGT/COND 引用
名称与 green Ids 类完全一致。
"""
import os
import json
import glob
import re
import importlib.util

spec = importlib.util.spec_from_file_location("rg", "scripts/rebuild_gen.py")
rg = importlib.util.module_from_spec(spec)
spec.loader.exec_module(rg)

ROOT = rg.ROOT
OUT = rg.OUT
upper = rg.upper

STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"


def main():
    statids_src = open(STATIDS, encoding="utf-8").read()
    statids = {p: n for n, p in re.findall(
        r'public static final Identifier (\w+) = id\("([^"]+)"\);', statids_src)}

    entries = []
    skipped = 0
    group_skipped = 0
    missing = []
    for f in sorted(glob.glob(ROOT + "/entry/*.json")):
        slug = os.path.basename(f)[:-5]
        d = json.load(open(f, encoding="utf-8"))
        # group 入口为内联子条目组合，由别处装配，不在此注册
        if d.get("entry") != "inline":
            group_skipped += 1
            continue
        # 具象数值（$value 之外的常量/表达式）走 stat_entry 路径，不在 buildEntries 内
        if d["amount"].get("value") != "$value":
            skipped += 1
            continue
        idf = statids.get(slug)
        if not idf:
            missing.append(slug)
            continue
        conds = d.get("conditions", [])
        cond_refs = ["StatConditionIds.COND_" + upper(c["ref"].split(":")[1]) for c in conds]
        tgt = "StatTargetIds.TGT_" + upper(d["targets"]["ref"].split(":")[1])
        eff = "StatEffectIds.EFF_" + upper(d["effect"]["ref"].split(":")[1])
        if len(cond_refs) == 0:
            call = "stat(" + tgt + ", " + eff + ")"
        elif len(cond_refs) == 1:
            call = "stat(" + cond_refs[0] + ", " + tgt + ", " + eff + ")"
        else:
            call = "stat(List.of(" + ", ".join(cond_refs) + "), " + tgt + ", " + eff + ")"
        entries.append("        map.put(StatIds." + idf + ", " + call + ");")

    if missing:
        print("WARN: %d entry slugs 缺少 StatIds 字段：" % len(missing))
        for m in missing[:30]:
            print("   ", m)
    print("StatGen entries: %d (skipped concrete-amount: %d, group: %d, missing-StatIds: %d)"
          % (len(entries), skipped, group_skipped, len(missing)))

    content = rg.statgen_java("\n".join(entries))
    outp = os.path.join(OUT, "StatGen.java")
    with open(outp, "w", encoding="utf-8") as fh:
        fh.write(content)
    print("wrote", outp)


if __name__ == "__main__":
    main()
