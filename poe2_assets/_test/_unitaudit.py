# -*- coding: utf-8 -*-
"""
A4 单位一致性审计：PercentageAttribute（分数语义，1.0 = 100%）上若用
ADD_VALUE 配裸 $value，则写入值放大 100 倍。

判据链（全部来自工程内实证，非推断）：
  1. AttributeRegister.MAX_RESISTANCES = registerPercentage("max_resistances", 0.75)
     —— 75% 的默认上限存为 0.75，故 PercentageAttribute 以分数存储。
  2. compat/jade/MobsInfo.java: getAttributeValue(RESISTANCES) * 100 —— 显示时才 ×100。
  3. DamageAction.java: r = min(r, max); denom = 1f - r;  注释「抗性≈100% 时防除零」
     —— r 为 1.0=100% 的分数。
=> PercentageAttribute + ADD_VALUE 必须写分数（$value / 100）；裸 $value 放大 100 倍。
"""
import json
import re
import pathlib
from collections import defaultdict

ROOT = pathlib.Path(__file__).resolve().parents[2]
ATTR_REG = ROOT / "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
ENTRY_DIR = ROOT / "src/generated/resources/data/coe/coe/stat_entry"
IDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
OUT = ROOT / "poe2_assets/_test/unit_audit.md"

# 属性种类：registerPercentage / registerDouble / registerBoolean
DECL = re.compile(
    r'public\s+static\s+Holder<Attribute>\s+(\w+)\s*=\s*'
    r'register(Percentage|Double|Boolean)\s*\(\s*"([a-z0-9_]+)"'
)


def load_attr_kinds():
    kinds = {}
    text = ATTR_REG.read_text(encoding="utf-8")
    for m in DECL.finditer(text):
        const, kind, name = m.group(1), m.group(2), m.group(3)
        kinds["coe:" + name] = (kind, const)
    return kinds


def load_descriptions():
    """StatIds 中每个 id 的注释即 POE2 原描述。"""
    desc = {}
    lines = IDS.read_text(encoding="utf-8").splitlines()
    pending = None
    for ln in lines:
        s = ln.strip()
        if s.startswith("//"):
            pending = s[2:].strip()
            continue
        m = re.search(r'=\s*id\("([a-z0-9_]+)"\)', s)
        if m:
            desc[m.group(1)] = pending or ""
            pending = None
    return desc


SRC = ROOT / "src/main/java"
SKIP_PARTS = ("data\\gen\\", "data/gen/")
SKIP_NAMES = {"AttributeRegister.java", "StatIds.java"}


def load_live_attrs(kinds):
    """有运行时读取器的属性（排除 data/gen/**、AttributeRegister、StatIds）。"""
    const_of = {aid: const for aid, (_k, const) in kinds.items()}
    used = set()
    for p in SRC.rglob("*.java"):
        sp = str(p)
        if any(s in sp for s in SKIP_PARTS) or p.name in SKIP_NAMES:
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        for aid, const in const_of.items():
            if aid in used:
                continue
            if re.search(r'\b' + re.escape(const) + r'\b', text):
                used.add(aid)
    return used


def main():
    kinds = load_attr_kinds()
    desc = load_descriptions()
    live = load_live_attrs(kinds)

    # 违规分组：(属性, 操作) -> [(条目, amount, 描述)]
    bad = defaultdict(list)
    ok = defaultdict(int)
    for p in sorted(ENTRY_DIR.glob("*.json")):
        try:
            d = json.loads(p.read_text(encoding="utf-8"))
        except Exception:
            continue
        if d.get("type") != "attribute":
            continue
        attr = d.get("attribute")
        op = d.get("operation")
        amt = str(d.get("amount", ""))
        kind = kinds.get(attr, (None, None))[0]
        if kind != "Percentage":
            continue
        # 常量（1.0 等布尔式开关）不参与单位判定
        if "$value" not in amt:
            continue
        has_div = "/ 100" in amt.replace("/100", "/ 100")
        if op == "add_value" and not has_div:
            bad[(attr, op)].append((p.stem, amt, desc.get(p.stem, "")))
        else:
            ok[(attr, op)] += 1

    total_bad = sum(len(v) for v in bad.values())

    # 分级：P0 = 属性有活读取器 且 描述含百分号（真实 100× 放大，玩家可感知）
    #       P1 = 活读取器但描述无百分号（需人工判定语义）
    #       P2 = 死属性（无消费方，单位错误暂不显形，随消费逻辑实现一并修）
    p0, p1, p2 = [], [], []
    for (attr, op), items in bad.items():
        is_live = attr in live
        for name, amt, ds in items:
            row = (attr, name, amt, ds)
            if is_live and "%" in ds:
                p0.append(row)
            elif is_live:
                p1.append(row)
            else:
                p2.append(row)
    lines = [
        "# A4 单位一致性审计：PercentageAttribute + ADD_VALUE 必须写分数",
        "",
        "## 判据（工程内实证）",
        "",
        "1. `AttributeRegister.MAX_RESISTANCES = registerPercentage(\"max_resistances\", 0.75)`"
        " —— 75% 上限存为 `0.75`，确认分数存储。",
        "2. `compat/jade/MobsInfo.java:119` `getAttributeValue(RESISTANCES) * 100` —— 仅显示时 ×100。",
        "3. `DamageAction.java:279-284` `r = min(r, max); denom = 1f - r;`"
        " 注释「抗性≈100% 时防除零」 —— `r` 为 1.0=100% 的分数。",
        "",
        f"**结论：`ADD_VALUE` 配裸 `$value` 放大 100 倍。命中 {total_bad} 条**"
        f"（P0 活属性+百分比描述 **{len(p0)}**；P1 活属性待判 {len(p1)}；P2 死属性 {len(p2)}）。",
        "",
    ]

    def table(title, rows, note):
        lines.append(f"## {title}（{len(rows)} 条）")
        lines.append("")
        lines.append(note)
        lines.append("")
        lines.append("| 属性 | stat 条目 | amount | POE2 描述 |")
        lines.append("|---|---|---|---|")
        for attr, name, amt, ds in sorted(rows):
            lines.append(f"| `{attr}` | `{name}` | `{amt}` | {ds} |")
        lines.append("")

    table("P0 必修：活属性 + 百分比描述 → 实际放大 100 倍", p0,
          "同属性已有条目写 `/100`，且运行时确有读取器消费 → 玩家可感知的数值错误。")
    table("P1 待判：活属性但描述无百分号", p1,
          "需人工确认描述语义是分数还是计数（如「+{X} 次」类应走 registerDouble）。")
    table("P2 缓修：死属性（无运行时读取器）", p2,
          "单位错误当前不显形，建议随该属性消费逻辑实现时一并纠正。")

    lines += ["## 同属性同操作的合规计数（对照）", "", "| 属性 | 操作 | 合规条目数 |", "|---|---|---:|"]
    for (attr, op), n in sorted(ok.items(), key=lambda kv: -kv[1])[:25]:
        lines.append(f"| `{attr}` | `{op}` | {n} |")

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"A4 命中（PercentageAttribute + add_value + 裸 $value）: {total_bad}"
          f"  → P0={len(p0)}  P1={len(p1)}  P2={len(p2)}")
    print("--- P0 必修明细 ---")
    for attr, name, amt, ds in sorted(p0):
        print(f"  {attr:28s} {name[:46]:48s} {ds[:52]}")
    print(f"report -> {OUT}")


if __name__ == "__main__":
    main()
