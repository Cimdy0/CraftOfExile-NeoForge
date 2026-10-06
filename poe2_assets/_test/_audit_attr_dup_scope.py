# -*- coding: utf-8 -*-
"""
属性字段语义去重 + 作用域焊死判定（纯静态只读，不改码、不读 runData 产物 JSON）

判定三问：
  Q1 是否存在语义重复属性？（同义归一后「维度 + 作用域」键完全相同）
  Q2 属性名里是否焊死了作用域类型？（名含 StatTag token + 维度词 → 应为 维度属性 + {scope}）
  Q3 是否存在同维度多作用域族？（MAGNITUDE 有 ailment/aura/remnant 三份 → 归一为单属性 + scope）

分类（每属性恰属一类）：
  SELF_NAMED   属性名全部 token 都是 scope 类目名，无维度词 → 属性本体（如 ACCURACY / DAMAGE / GLORY），不焊死
  WELDED       名 = scope token + 维度词 → 作用域焊死候选
  PLAIN        名不含任何 StatTag token → 中性机制本体（如 THORNS / EVASION 之外的自有机制）
  MIXED_BASE   名不含 scope token 但含维度词且有同维度焊死兄弟 → 归一目标基属性（如 DAMAGE / RECOVERY_RATE）

输出：
  poe2_assets/_test/attr_dup_scope_audit.json  全量明细
  stdout                                        分组摘要
"""
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path("E:/McModding/Modding/CraftOfExile-NeoForge")
ATTR = ROOT / "src/main/java/com/cimdy/coe/data/AttributeRegister.java"
TAG = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatTag.java"
STATGEN = ROOT / "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
OUT = ROOT / "poe2_assets/_test/attr_dup_scope_audit.json"

# ── 同义归一（保守：仅确定同义，绝不合并算子/维度不同的词）────────────────
SYN = {
    "maximum": "max",
    "armour": "armor",
    "life": "health",
    "resistances": "resistance",
    "ailments": "ailment",
    "remnants": "remnant",
    "charges": "charge",
    "projectiles": "projectile",
    "leeches": "leech",
    "leeched": "leech",
    "deflection": "deflect",
    "regeneration": "regen",
    "regenerate": "regen",
    "contributes": "contribution",
}

# ── 维度词：表达「测什么」的通用量纲，与作用域正交 ─────────────────────
# 关键：维度词在多个作用域下重复出现 => 应归一为「维度属性 + {scope}」
DIMENSION = {
    "chance", "rate", "ratio", "multiplier", "duration", "effect", "magnitude",
    "range", "recovery", "recharge", "generation", "threshold", "stacks",
    "time", "amount", "count", "number", "speed", "buildup", "penetration",
    "efficiency", "adaptation", "response", "contribution", "break", "retain",
    "extra", "double", "add", "added", "max", "base", "current", "gained",
    "used", "activation", "loss", "grace", "reload", "detonation", "pierce",
    "rating", "capacity", "slot", "aptitude", "linger", "reservation",
    "bypasses", "inherent", "no", "equal", "collected", "further", "away",
    "lasts", "of", "over", "strike", "recently", "consumed", "additional",
    "launch", "random", "fire_",  # fire_ 占位避免与元素 fire 混淆（不使用）
}
DIMENSION.discard("fire_")

# ── 明确不可归一（语义上必须独立池 / vanilla 核心，归一会破坏机制）──────
DO_NOT_MERGE = {
    # 资源上限各自独立池：fold 到单属性会让「最大魔力」污染「最大怒气」
    "MAX_MANA", "MAX_RAGE", "MAX_SPIRIT", "MAX_ENERGY_SHIELD", "MAX_DARKNESS",
    "MAX_VOLATILITY", "MAX_INFUSION", "MAX_ENDURANCE_CHARGES",
    "MAX_FRENZY_CHARGES", "MAX_POWER_CHARGES", "META_ENERGY_MAX",
    "MAX_RESISTANCES", "MAX_REGENERATION",
    # 属性点（vanilla-like 三围）
    "STRENGTH", "DEXTERITY", "INTELLIGENCE",
    # 系统/工作类
    "WORK_SPEED", "WORK_APTITUDE", "SKILL_SLOT_COUNT", "ITEM_CAPACITY",
}


def load_tags():
    """StatTag 常量 -> 字符串字面值（小写 token 词表）。"""
    txt = TAG.read_text(encoding="utf-8")
    vals = set()
    for m in re.finditer(r'public static final String\s+\w+\s*=\s*"([^"]+)"', txt):
        vals.add(m.group(1))
    # 层级表里出现的字面量子标签（"stun" / "heavy_stun"）
    for m in re.finditer(r'"([a-z_]+)"', txt):
        v = m.group(1)
        if 2 < len(v) < 24:
            vals.add(v)
    return vals


def load_attrs():
    """解析属性声明：常量名 / 注册 id / 类型 / base / 备注。"""
    txt = ATTR.read_text(encoding="utf-8")
    out = []
    decl = re.compile(
        r'public static Holder<Attribute>\s+(\w+)\s*=\s*'
        r'register(Double|Percentage|Boolean)\s*\(\s*"([^"]+)"\s*'
        r'(?:,\s*([-\d.a-z]+))?'          # base
        r'(?:,\s*([-\d.]+))?'             # min
        r'(?:,\s*([-\d.]+))?'             # max
        r'\s*\)'
    )
    lines = txt.splitlines()
    for i, line in enumerate(lines):
        m = decl.search(line)
        if not m:
            continue
        name, kind, rid, base, mn, mx = m.groups()
        note = ""
        if i > 0:
            prev = lines[i - 1].strip()
            if prev.startswith("/**"):
                note = prev.strip("/*").strip("*/").strip()
        try:
            basev = float(base) if base not in (None, "false", "true") else 0.0
        except ValueError:
            basev = 0.0
        out.append({
            "name": name, "kind": kind, "id": rid,
            "base": basev, "min": mn, "max": mx, "note": note,
        })
    return out


def load_statgen_scopes():
    """StatGen 每条语句内 AttributeRegister.X 与同语句 StatTag.Y / 字面量 tags 的共现。"""
    txt = STATGEN.read_text(encoding="utf-8")
    attr2tags = defaultdict(set)
    attr2stmts = defaultdict(int)
    for stmt in txt.split(";"):
        attrs = re.findall(r'AttributeRegister\.([A-Z_0-9]+)', stmt)
        if not attrs:
            continue
        tags = set(re.findall(r'StatTag\.([A-Z_0-9]+)', stmt))
        for a in attrs:
            attr2stmts[a] += 1
            attr2tags[a] |= tags
    return attr2tags, attr2stmts


def stemize(tok, tags):
    """复数/名词化还原到 StatTag token（仅当还原后命中词表才生效，避免臆造）。"""
    t = SYN.get(tok, tok)
    if t in tags:
        return t
    if t.endswith("s") and t[:-1] in tags:
        return t[:-1]
    if t.endswith("ion") and t[:-3] in tags:
        return t[:-3]
    if t.endswith("ing") and t[:-3] in tags:
        return t[:-3]
    return t


def main():
    tags = load_tags()
    attrs = load_attrs()
    sg_tags, sg_stmts = load_statgen_scopes()
    names = {a["name"] for a in attrs}

    recs = []
    for a in attrs:
        raw = [t for t in a["name"].lower().split("_") if t]
        norm = [stemize(t, tags) for t in raw]
        scope_toks, dim_toks, other = [], [], []
        for t in norm:
            if t in tags and t not in DIMENSION:
                scope_toks.append(t)
            elif t in DIMENSION:
                dim_toks.append(t)
            else:
                other.append(t)
        # 分类
        if scope_toks and not dim_toks and not other:
            cls = "SELF_NAMED"
        elif scope_toks:
            cls = "WELDED"
        elif not scope_toks and (dim_toks or other):
            cls = "PLAIN"
        else:
            cls = "PLAIN"
        # 维度键：维度词 + 非作用域残余词（区别机制本体）
        dim_key = "|".join(sorted(dim_toks) + sorted(other))
        scope_key = "|".join(sorted(set(scope_toks)))
        recs.append({
            **a,
            "class": cls,
            "scope_tokens": sorted(set(scope_toks)),
            "dim_tokens": sorted(dim_toks),
            "other_tokens": sorted(other),
            "dim_key": dim_key,
            "scope_key": scope_key,
            "statgen_tags": sorted(sg_tags.get(a["name"], [])),
            "statgen_stmts": sg_stmts.get(a["name"], 0),
            "protected": a["name"] in DO_NOT_MERGE,
        })

    # ── Q1 语义重复：维度键 + 作用域键完全相同 ────────────────────────
    bykey = defaultdict(list)
    for r in recs:
        bykey[(r["dim_key"], r["scope_key"])].append(r["name"])
    dup_groups = [{"dim": k[0], "scope": k[1], "members": v}
                  for k, v in bykey.items() if len(v) > 1]

    # ── Q3 同维度多作用域族：同 dim_key，作用域不同（≥2 个成员）────────
    bydim = defaultdict(list)
    for r in recs:
        if r["dim_key"]:
            bydim[r["dim_key"]].append(r)
    fam = []
    for dk, members in bydim.items():
        if len(members) < 2:
            continue
        scopes = {m["scope_key"] for m in members}
        if len(scopes) < 2:
            continue
        base = [m["name"] for m in members if not m["scope_key"]]
        welded = [m["name"] for m in members if m["scope_key"]]
        if not welded:
            continue
        prot = [m["name"] for m in members if m["protected"]]
        fam.append({
            "dim": dk,
            "base_exists": base,
            "welded": welded,
            "protected": prot,
            "members": [{"name": m["name"], "scope": m["scope_key"],
                         "base_val": m["base"], "statgen_tags": m["statgen_tags"]}
                        for m in sorted(members, key=lambda x: x["name"])],
        })
    fam.sort(key=lambda f: (-len(f["members"]), f["dim"]))

    # ── Q2 焊死明细 + 双重表达（名里焊死 + StatGen 又带同 scope tag）────
    welded = [r for r in recs if r["class"] == "WELDED"]
    double = []
    for r in welded:
        sgl = {t.lower() for t in r["statgen_tags"]}
        overlap = sorted(set(r["scope_tokens"]) & sgl)
        if overlap:
            double.append({"name": r["name"], "overlap": overlap,
                           "statgen_tags": r["statgen_tags"]})

    # ── 附带体检：零基倍率（注释称倍率但 base=0）────────────────────
    zero_mult = [r["name"] for r in recs
                 if r["base"] == 0.0 and ("倍率" in r["note"] or "率" in r["note"])
                 and r["kind"] == "Percentage"]

    payload = {
        "totals": {
            "attributes": len(recs),
            "self_named": sum(1 for r in recs if r["class"] == "SELF_NAMED"),
            "welded": len(welded),
            "plain": sum(1 for r in recs if r["class"] == "PLAIN"),
            "statgen_wired": sum(1 for r in recs if r["statgen_stmts"] > 0),
            "tag_vocab": len(tags),
        },
        "q1_semantic_dup_groups": dup_groups,
        "q3_same_dim_multi_scope_families": fam,
        "q2_welded_double_expression": double,
        "zero_base_multiplier_suspects": zero_mult,
        "records": sorted(recs, key=lambda r: (r["class"], r["dim_key"], r["name"])),
    }
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=1), encoding="utf-8")

    t = payload["totals"]
    print(f"属性 {t['attributes']} | SELF_NAMED {t['self_named']} | WELDED {t['welded']} "
          f"| PLAIN {t['plain']} | StatGen接线 {t['statgen_wired']} | StatTag词表 {t['tag_vocab']}")

    print(f"\n=== Q1 语义重复（维度+作用域键相同）: {len(dup_groups)} 组 ===")
    for g in dup_groups:
        print(f"  [dim={g['dim'] or '∅'} scope={g['scope'] or '∅'}] {g['members']}")

    print(f"\n=== Q3 同维度多作用域族（归一候选）: {len(fam)} 族 ===")
    for f in fam:
        b = f"base={f['base_exists']}" if f["base_exists"] else "base=缺失(需新建)"
        p = f" PROTECTED={f['protected']}" if f["protected"] else ""
        print(f"\n  维度「{f['dim']}」 {b}{p}")
        for m in f["members"]:
            sg = f" sgTags={m['statgen_tags']}" if m["statgen_tags"] else ""
            print(f"    - {m['name']:<44} scope={m['scope'] or '∅':<24} base={m['base_val']}{sg}")

    print(f"\n=== Q2 双重表达（名焊死 + StatGen 同 scope tag）: {len(double)} 处 ===")
    for d in double:
        print(f"  {d['name']:<40} 重叠={d['overlap']} sgTags={d['statgen_tags']}")

    print(f"\n=== 附带体检 · 零基百分比且注释称率/倍率: {len(zero_mult)} 处 ===")
    for n in zero_mult:
        print(f"  {n}")

    print(f"\nJSON -> {OUT}")


if __name__ == "__main__":
    main()
