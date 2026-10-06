# -*- coding: utf-8 -*-
"""重新设计 C 任务接线分析：分离「属性缺口」与「条件/作用域缺口」。

核心纠正：此前把「条件/作用域变体」误算成「属性缺口」。
真实维度分解：
  - 属性 (attribute)  -> 对应 StatEffect (EFF_*)；节点属性短语 → EFF SEG 子集匹配。
  - 条件 (condition)  -> 对应 StatCondition (COND_*) 39 条；与属性正交。
  - 作用域 (target)   -> 对应 StatTarget (TGT_*) 14 条；节点几乎全是 SELF_ONLY。

方法（纪律对齐 coe-corpus-stat-wiring）：
  - 不读 runData 产物驱动接线；用 StatGen.java 静态解析的 map.put(StatIds.X) 作为「已接线」真值。
  - 节点属性短语归一严格复用 gen_stat_ids.normalize + mc_replace（Armour→Armor/Life→Health/Maximum→Max/Rating→""）。
  - EFF SEG 仅做命名空间前缀剥离（coe/minecraft/banner/bear_form/chaos），不臆造。
  - 匹配 = 节点属性 token 集合 ⊆ 某 EFF SEG token 集合（同一 op 族内）；零命中 = 真属性缺口。
"""
import os, re, json, sys
from collections import defaultdict, Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_stat_ids as gi

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
NODEMAP = os.path.join(ROOT, "poe2_assets/node_stat_map.json")
EFFIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatEffectIds.java")

# 命名空间前缀（EFF SEG 拼接引入，非属性语义）
NS_PREFIX = {"coe", "minecraft", "banner", "bear_form", "chaos"}

# 条件/作用域短语标记：出现即视为该模板带条件（属性只取首个逗号/标记前的 head）
COND_MARKERS = [
    ",", " while ", " if ", " when ", " against ", " per ", " nearby ",
    " recently ", " during ", " on the ground ", " wielding ", " using ",
    " with ", " for each ", " against ",
]

# op 词 -> EFF 族前缀（用于限定搜索范围）
OP_TO_FAMILY = {
    "mult_base": "add_multiplied_base",
    "mult_total": "add_multiplied_total",
    "value": "add_value",
}


def parse_wired_constants():
    """从 StatGen.java 静态解析已接线 StatIds 常量集合。"""
    txt = open(STATGEN, encoding="utf-8").read()
    return set(re.findall(r"map\.put\(StatIds\.(\w+)\s*,", txt))


# EFF 侧仅剥「真 scope 后缀」（保留元素/类型词 fire/cold/lightning/chaos/physical/elemental/penetration/resistance/damage）
EFF_STRIP = {
    "taken", "spell", "melee", "mace", "curse", "attack", "over_time", "non_damaging",
    "buildup", "rate", "chance", "threshold", "magnitude", "duration", "max", "recovery",
    "recharge", "generation", "loss", "speed", "area", "of_effect", "cast", "charge",
    "charges", "leech", "health", "mana", "energy", "shield", "per", "second",
}

def parse_eff_index():
    """解析 StatEffectIds：op 族 -> [(eff_core_tokens, eff_constant)]。"""
    src = open(EFFIDS, encoding="utf-8").read()
    paths = re.findall(r'static final Identifier (EFF_\w+) = id\("([^"]+)"\)', src)
    idx = defaultdict(list)
    for const, path in paths:
        m = re.match(r'^(?:[0-9]+_[0-9]+_)?(add_[a-z_]+?)_(?:ailment_)?attribute_(.*)$', path)
        if not m:
            fm = re.match(r'^(?:[0-9]+_[0-9]+_)?(add_[a-z_]+)', path)
            fam = fm.group(1) if fm else "other"
            idx[fam].append((tuple(), const))
            continue
        fam = m.group(1)
        seg = m.group(2)
        core = tuple(t for t in seg.split("_") if t and t not in NS_PREFIX and t not in EFF_STRIP)
        idx[fam].append((core, const))
    return idx


def detect_op(tpl):
    """返回 (op_key, op_label)。极性词归并为 op；reduced/less 视为负向但复用同 effect 族。"""
    t = tpl.lower()
    if re.search(r"\bincreased\b|\binc\b", t):
        return "mult_base", "increased"
    if re.search(r"\bmore\b", t):
        return "mult_total", "more"
    if re.search(r"\breduced\b", t):
        return "mult_base", "reduced"      # 负向 inc，复用 multiplied_base effect
    if re.search(r"\bless\b", t):
        return "mult_total", "less"        # 负向 more
    if re.search(r"\badd\b|\bgain\b|\badditional\b|\bto\b|\+", t):
        return "value", "add"
    return None, "unknown"


def extract_attr_head(tpl):
    """取属性 head：切掉条件/作用域标记后的首段，含 has_condition 标记。"""
    head = tpl
    has_cond = False
    for mk in COND_MARKERS:
        i = head.lower().find(mk)
        if i >= 0:
            has_cond = True
            head = head[:i]
    return head.strip(), has_cond


# 动词 / actor / 作用域限定词（非属性语义，剥离后剩余核心属性名词去匹配）
VERB_SCOPE = {
    # 动词
    "deal", "deals", "dealt", "dealing", "have", "has", "had", "gain", "gains", "gained",
    "recover", "recovers", "regenerate", "regenerates", "grant", "grants", "granted",
    "converts", "convert", "converting", "penetrates", "penetrate", "ignore", "ignores",
    "costs", "cost", "spend", "spends", "use", "uses", "using", "wield", "wielding",
    "inflict", "inflicts", "inflicted", "trigger", "triggers", "triggered", "cast", "casts",
    "is", "are", "be", "been", "get", "gets", "take", "takes", "taking",
    # actor / 作用域限定词（不建独立属性，走 TGT/COND/scope）
    "minions", "minion", "totems", "totem", "spells", "spell", "projectiles", "projectile",
    "allies", "ally", "companions", "companion", "channelling", "channeling", "triggered",
    "ancestral", "ancestrally", "aura", "auras", "banner", "herald", "heralds", "mark",
    "marks", "offering", "offerings", "invocation", "invocated", "invocations",
    "you", "your", "yours", "me", "my", "nearby", "while", "if", "when", "per", "recently",
    "during", "against", "on", "of", "as", "extra", "for", "each", "with", "the", "a", "an",
    "to", "and", "or", "in", "at", "by", "from", "into", "their", "they", "he", "she",
    "this", "that", "these", "those", "its", "his", "her", "our", "we", "us",
}


def head_to_attr_tokens(head):
    """head -> 核心属性 token 集合（剥离 op/极性/数字/动词/actor-scope 限定词，保留语义属性名词）。"""
    # 去 op 词
    head = re.sub(r"\b(increased|more|reduced|less|add|adds|added|additional|less)\b",
                  " ", head, flags=re.IGNORECASE)
    head = re.sub(r"\{X\}|\+|\%", " ", head)
    head = re.sub(r"[^a-zA-Z ]", " ", head)
    # 二级归一（mc_replace）
    head = gi.mc_replace(head)
    toks = [w.lower() for w in head.split() if w and w.lower() not in VERB_SCOPE]
    return toks


def match_eff(op_key, attr_toks, eff_idx):
    """同一 op 族内做子集匹配：node 核心 token ⊆ eff 核心 token。
    对 'damage' 同时尝试保留/剥离两种变体（damage 即可作动词可作名词）。"""
    fam_prefix = OP_TO_FAMILY[op_key]
    variants = [tuple(attr_toks)]
    if "damage" in attr_toks:
        variants.append(tuple(t for t in attr_toks if t != "damage"))
    cands = []
    seen = set()
    for fam, entries in eff_idx.items():
        if not fam.startswith(fam_prefix):
            continue
        for core_toks, const in entries:
            if not core_toks:
                continue
            core_set = set(core_toks)
            for v in variants:
                if v and set(v).issubset(core_set):
                    if const not in seen:
                        cands.append((const, core_toks))
                        seen.add(const)
                    break
    cands.sort(key=lambda c: len(c[1]))
    return cands


def main():
    wired = parse_wired_constants()
    eff_idx = parse_eff_index()
    nmap = json.load(open(NODEMAP, encoding="utf-8"))

    # 聚合：template -> 是否接线（按 constant 是否在 StatGen 已接线集合）
    seen = {}   # template -> dict(count, wired, has_cond, op, attr_toks, cands)
    for nid, recs in nmap.items():
        for r in recs:
            tpl = r.get("template")
            if not tpl or tpl.startswith("{X}"):
                continue
            const = r.get("constant")
            is_wired = (const in wired) or r.get("has_entry") is True
            if is_wired:
                continue
            op_key, op_label = detect_op(tpl)
            head, has_cond = extract_attr_head(tpl)
            attr_toks = head_to_attr_tokens(head)
            key = (tpl)
            d = seen.get(key)
            if d is None:
                d = {"op": op_key, "attr": attr_toks, "has_cond": has_cond, "n": 0,
                     "cands": match_eff(op_key, attr_toks, eff_idx) if op_key else []}
                seen[key] = d
            d["n"] += 1

    total = len(seen)
    covered = [t for t, d in seen.items() if d["cands"]]            # 属性已有 EFF
    gap = [t for t, d in seen.items() if not d["cands"]]            # 真属性缺口
    cond_templates = [t for t, d in seen.items() if d["has_cond"]] # 带条件短语

    covered_cond = [t for t in covered if seen[t]["has_cond"]]
    covered_pure = [t for t in covered if not seen[t]["has_cond"]]
    gap_cond = [t for t in gap if seen[t]["has_cond"]]
    gap_pure = [t for t in gap if not seen[t]["has_cond"]]

    print(f"未接线 distinct 模板总数            : {total}")
    print(f"  ├ 属性已有 EFF（可接线，0 新effect）: {len(covered)}")
    print(f"  │    ├ 其中带条件短语（需配 COND） : {len(covered_cond)}")
    print(f"  │    └ 纯属性（直接 stat(tgt,eff)）: {len(covered_pure)}")
    print(f"  └ 真属性缺口（无匹配 EFF）          : {len(gap)}")
    print(f"       ├ 带条件短语                   : {len(gap_cond)}")
    print(f"       └ 纯属性                       : {len(gap_pure)}")
    print(f"  带条件短语模板合计                  : {len(cond_templates)}")

    # 真属性缺口：列出 distinct 属性 token 集合（设计面）
    gap_attrs = Counter()
    for t in gap:
        d = seen[t]
        gap_attrs[" ".join(d["attr"]) or "<none>"] += d["n"]
    print(f"\n=== 真属性缺口 distinct 属性短语（按出现频次）===")
    for attr, cnt in gap_attrs.most_common(60):
        print(f"  {cnt:4d}x  [{attr}]")

    # 纯属性可接线样例
    print(f"\n=== 纯属性可接线样例（前 20）===")
    for t in covered_pure[:20]:
        d = seen[t]
        print(f"  op={d['op']:9s} attr={' '.join(d['attr']):28s} -> {d['cands'][0][0]}")

    # 带条件可接线样例
    print(f"\n=== 带条件可接线样例（前 15）===")
    for t in covered_cond[:15]:
        d = seen[t]
        print(f"  attr={' '.join(d['attr']):24s} cond? {d['has_cond']} -> {d['cands'][0][0]}")

    # 落盘
    out = {
        "total_unwired": total,
        "covered_attr": sorted(covered),
        "gap_attr": sorted(gap),
        "covered_pure": sorted(covered_pure),
        "covered_cond": sorted(covered_cond),
        "gap_pure": sorted(gap_pure),
        "gap_cond": sorted(gap_cond),
        "gap_attrs_freq": gap_attrs.most_common(),
    }
    json.dump(out, open(os.path.join(ROOT, "poe2_assets", "design_wiring_out.json"), "w"),
              ensure_ascii=False, indent=1)
    print("\nwritten poe2_assets/design_wiring_out.json")


if __name__ == "__main__":
    main()
