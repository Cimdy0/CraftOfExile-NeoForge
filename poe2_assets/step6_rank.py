# -*- coding: utf-8 -*-
"""
Step6-B: 对 36 真冲突组的每个占位（occurrence）打分，产出确定性裁决建议。

判据（skill §10「漏查实现判定」+ 冲突 rank 约定）：
  E  引擎消费：属性名在引擎目录（codec/ helper/ event/ effect/ entity/）被
     `AttributeRegister.<FIELD>` 精确引用 → 该条目真实生效；
  P  Wave-12 占位：属性声明位置晚于 AttributeRegister 的占位批次标记 → 数据先行占位；
  N  `_N` 数字后缀：b3 二次占位（同一 stat 的重复占位），必删候选；
  B3 语句落在 StatGen 的 b3「同义变体」块（L1086-1151）→ 后来者，覆盖前者。

rank 越小越该保留：
  0  非占位属性 + 引擎消费
  1  非占位属性（引擎未消费）
  2  占位属性（无 _N）
  3  占位属性（带 _N）
无属性语句（StatEffect 直接通道 recoup/recoverPctOnEvent/map.put）单独按「引用即生效」处理，
rank 记 0，但同组多条时按 b3 归属决定删谁。
"""
import json
import os
import re

STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
ATTRREG = "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
AUDIT = "poe2_assets/_test/step6_conflicts.json"
OUT_MD = "poe2_assets/_test/step6_rank.md"
OUT_JSON = "poe2_assets/_test/step6_rank.json"

ENGINE_DIRS = [
    "src/main/java/com/cimdy/coe/data/codec",
    "src/main/java/com/cimdy/coe/helper",
    "src/main/java/com/cimdy/coe/event",
    "src/main/java/com/cimdy/coe/effect",
    "src/main/java/com/cimdy/coe/entity",
    "src/main/java/com/cimdy/coe/item",
]
# StatGen 自身是「注册方」不是消费方，必须排除（否则全部属性都算被消费）
EXCLUDE = {os.path.normpath(STATGEN)}

RE_ATTR_USE = re.compile(r"AttributeRegister\.([A-Za-z_][A-Za-z0-9_]*)")
# b3「同义变体」块行号区间（由块注释 "b3 追加" / "组T（批3 同义变体）" 界定，见 StatGen L1108）
B3_RANGE = (1086, 1151)


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def engine_consumed():
    used = set()
    for d in ENGINE_DIRS:
        for root, _dirs, files in os.walk(d):
            for fn in files:
                if not fn.endswith(".java"):
                    continue
                p = os.path.join(root, fn)
                if os.path.normpath(p) in EXCLUDE:
                    continue
                used |= set(RE_ATTR_USE.findall(read(p)))
    return used


def placeholder_attrs():
    """返回（占位属性集合, 占位标记行号）。标记 = AttributeRegister 内 Wave-12 批量落地注释。"""
    lines = read(ATTRREG).splitlines()
    mark = None
    for i, l in enumerate(lines, 1):
        if "Wave-12" in l and ("批量落地" in l or "551" in l):
            mark = i
            break
    ph = set()
    if mark:
        # 实际声明无 final：`public static Holder<Attribute> NAME = registerPercentage(...)`
        RE_DECL = re.compile(r"public static (?:final\s+)?[\w<>\[\], ]+?\s+([A-Z][A-Z0-9_]*)\s*=")
        for i, l in enumerate(lines, 1):
            if i <= mark:
                continue
            m = RE_DECL.search(l)
            if m:
                ph.add(m.group(1))
    return ph, mark


def main():
    data = json.load(open(AUDIT, encoding="utf-8"))
    consumed = engine_consumed()
    ph, mark = placeholder_attrs()

    rows = []
    for bucket, groups in data["real"].items():
        for g in groups:
            occ = []
            for x in g["occurrences"]:
                attrs = x.get("attrs") or []
                has_n = any(re.search(r"_\d+$", a) for a in attrs)
                is_ph = any(a in ph for a in attrs)
                is_con = any(a in consumed for a in attrs)
                in_b3 = B3_RANGE[0] <= x["line"] <= B3_RANGE[1]
                if not attrs:
                    rank = 0  # StatEffect 直接通道：引用即生效
                elif not is_ph:
                    rank = 0 if is_con else 1
                else:
                    rank = 3 if has_n else 2
                occ.append({
                    "line": x["line"],
                    "helper": x["helper"],
                    "attrs": attrs,
                    "tags": x.get("tags") or [],
                    "placeholder": is_ph,
                    "suffix_n": has_n,
                    "engine_consumed": is_con,
                    "in_b3_block": in_b3,
                    "rank": rank,
                    "stmt": x["stmt"],
                })
            occ.sort(key=lambda o: (o["rank"], o["in_b3_block"], o["line"]))
            keep = occ[0]
            drop = occ[1:]
            # 纯重复的判据必须是「属性 + 标签 + helper」全同：
            # 只比属性会把 CHARM vs MINION（作用域不同）、valueMod vs inc（值约定 RAW vs DIV100 差 100 倍）
            # 误判为良性重复而误删。
            same_attr = len({(tuple(o["attrs"]), tuple(o["tags"]), o["helper"]) for o in occ}) == 1
            tie = [o for o in occ[1:] if o["rank"] == keep["rank"]]
            if same_attr:
                verdict = "DEDUP"           # 删重复，保留 rank 最优
            elif tie and not keep["attrs"]:
                verdict = "MANUAL"          # 多条 StatEffect 通道，需语义裁决
            elif tie:
                verdict = "MANUAL"          # 同 rank 不同属性：合法多属性 or 劫持
            else:
                verdict = "RANK"            # rank 明确分出胜负
            rows.append({
                "bucket": bucket,
                "const": g["const"],
                "corpus": g["corpus"],
                "manual_channel": g["manual"],
                "verdict": verdict,
                "keep": keep,
                "drop": drop,
                "occurrences": occ,
            })

    rows.sort(key=lambda r: (r["verdict"], r["const"]))
    json.dump(rows, open(OUT_JSON, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    L = ["# Step6-B 冲突组裁决打分\n",
         "占位标记行 = AttributeRegister L%s；引擎消费属性 %d 个。\n" % (mark, len(consumed)),
         "| 判定 | 组数 |", "| --- | --- |"]
    from collections import Counter
    cnt = Counter(r["verdict"] for r in rows)
    for k in sorted(cnt):
        L.append("| %s | %d |" % (k, cnt[k]))
    L.append("")
    for v in ("DEDUP", "RANK", "MANUAL"):
        sub = [r for r in rows if r["verdict"] == v]
        L.append("## %s — %d 组\n" % (v, len(sub)))
        for r in sub:
            L.append("### `%s`%s" % (r["const"], "（手工通道）" if r["manual_channel"] else ""))
            L.append("语料：`%s`" % r["corpus"])
            for o in r["occurrences"]:
                flag = []
                if o["placeholder"]:
                    flag.append("占位")
                if o["suffix_n"]:
                    flag.append("_N")
                if o["engine_consumed"]:
                    flag.append("引擎消费")
                if o["in_b3_block"]:
                    flag.append("b3块")
                mark_ = "**保留**" if o is r["keep"] else "删除"
                L.append("- %s L%d rank=%d `%s` attrs=%s tags=%s [%s]" % (
                    mark_, o["line"], o["rank"], o["helper"], ",".join(o["attrs"]) or "—",
                    ",".join(t.replace("StatTag.", "") for t in o["tags"]) or "—", " ".join(flag) or "—"))
            L.append("")
    open(OUT_MD, "w", encoding="utf-8").write("\n".join(L))

    print("裁决分布：" + " / ".join("%s=%d" % (k, cnt[k]) for k in sorted(cnt)))
    print("引擎消费属性数 %d，占位属性数 %d（标记行 L%s）" % (len(consumed), len(ph), mark))


if __name__ == "__main__":
    main()
