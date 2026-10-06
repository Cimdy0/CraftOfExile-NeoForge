# -*- coding: utf-8 -*-
"""根据 Id 语义预估 StatGen 实现流程，对 StatIds 分类排序（只读规划，不改码）。

分类维度（对应 StatGen 接线方法族，已核源码事实）：
  Layer : FOLD（ModifierStatEffect 读时聚合）| FIRE（触发型 StatEffect 事件执行）
  Flow  : inc / more / pctAdd / flat / chance / condition / regen / entity / esRecharge
        | convert / dtc / recoup / recover / grant / status（FIRE 子类）
  Mech  : 机制族（accuracy/armor/damage/resistance/health/...）
  Scope : 预估作用域（slam/mace/spell/attack/fire/...）

输出：classified_statids.json（全量，按 layer/flow/mech/name 排序）+ 控制台聚合摘要。
标记均为 EST（预估），非事实接线；供 P5 逐机制真实现批次排序使用。
"""
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path("E:/McModding/Modding/CraftOfExile-NeoForge")
STATIDS = ROOT / "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
SRC = ROOT / "src/main/java"
OUT = ROOT / "poe2_assets/_test/classified_statids.json"

DECL = re.compile(r'public static final Identifier (\w+) = id\("([^"]+)"\);')

# ---- 机制族关键词（顺序即优先级）----
MECH_KEYWORDS = [
    ("accuracy", ["ACCURACY"]),
    ("armor", ["ARMOR", "ARMOUR"]),
    ("evasion", ["EVASION"]),
    ("block", ["BLOCK"]),
    ("deflect", ["DEFLECT"]),
    ("ailment_elem", ["BLEED", "POISON", "IGNITE", "FREEZE", "SHOCK", "SCORCH", "CHILL"]),
    ("ailment", ["AILMENT"]),
    ("stun", ["STUN"]),
    ("resistance", ["RESIST", "RESISTANCE"]),
    ("health", ["HEALTH", "LIFE"]),
    ("mana", ["MANA"]),
    ("energy_shield", ["ENERGY_SHIELD", "ENERGY"]),
    ("charge", ["CHARGE", "FRENZY", "ENDURANCE", "POWER"]),
    ("rage", ["RAGE"]),
    ("glory", ["GLORY"]),
    ("charm", ["CHARM"]),
    ("flask", ["FLASK"]),
    ("crit", ["CRIT", "CRITICAL"]),
    ("speed", ["SPEED"]),
    ("range", ["RANGE", "RANGE_OF"]),
    ("attack", ["ATTACK"]),
    ("spell", ["SPELL"]),
    ("damage", ["DAMAGE"]),
    ("attribute", ["STRENGTH", "DEXTERITY", "INTELLIGENCE"]),
    ("minion", ["MINION"]),
    ("totem", ["TOTEM"]),
    ("companion", ["COMPANION"]),
    ("presence", ["PRESENCE"]),
    ("ally", ["ALLIES", "ALLY"]),
    ("ground", ["GROUND"]),
    ("hazard", ["HAZARD"]),
    ("herald", ["HERALD"]),
    ("aura", ["AURA"]),
    ("curse", ["CURSE"]),
    ("warcry", ["WARCY", "WARCRY"]),
    ("movement", ["MOVEMENT", "MOVE"]),
    ("attribute_any", ["ATTRIBUTE"]),
    ("skill", ["SKILL"]),
    ("item", ["ITEM", "EQUIPPED"]),
    ("projectile", ["PROJECTILE"]),
    ("melee", ["MELEE"]),
]

SCOPE_VOCAB = {
    "SLAM", "MACE", "SPEAR", "AXE", "BOW", "CROSSBOW", "BOLT", "ARROW", "QUARTERSTAFF",
    "SPELL", "ATTACK", "CRIT", "CRITICAL", "FIRE", "COLD", "LIGHTNING", "LIGHT", "CHAOS",
    "PHYSICAL", "POISON", "BLEED", "ELEMENTAL", "MELEE", "PROJECTILE", "THORNS", "ARMOR",
    "WARCY", "CURSE", "AURA", "CHARM", "FLASK", "DARKNESS", "KNOCKBACK", "PARRY", "DEFLECT",
    "MINION", "TOTEM", "COMPANION", "PRESENCE", "BEAST", "GRENATE", "HAZARD", "HERALD",
    "FORM", "SHIELD", "STUN", "FREEZE", "SHOCK", "IGNITE", "DAZE", "BLIND", "MARKED",
    "UNAMRED", "DUAL_WIELDING", "ONE_HANDED", "TWO_HANDED",
}

# 属性预估映射（基于 mechanism，粗略 EST）
ATTR_EST = {
    "accuracy": "AttributeRegister.ACCURACY",
    "armor": "AttributeRegister.ARMOR",
    "evasion": "AttributeRegister.EVASION",
    "block": "AttributeRegister.BLOCK_CHANCE",
    "deflect": "AttributeRegister.DEFLECTION_RATING",
    "ailment_elem": "AttributeRegister.AILMENT_*",
    "ailment": "AttributeRegister.AILMENT_CHANCE/THRESHOLD",
    "stun": "AttributeRegister.STUN_*",
    "resistance": "AttributeRegister.MAX_RESISTANCES(pctAdd)",
    "health": "minecraft:max_health",
    "mana": "AttributeRegister.MAX_MANA",
    "energy_shield": "AttributeRegister.MAX_ENERGY_SHIELD",
    "charge": "AttributeRegister.MAX_*_CHARGES",
    "rage": "AttributeRegister.MAX_RAGE",
    "glory": "AttributeRegister.MAX_GLORY",
    "charm": "AttributeRegister.CHARM_SLOT",
    "flask": "AttributeRegister.FLASK_*",
    "crit": "AttributeRegister.CRIT_CHANCE",
    "speed": "AttributeRegister.*_SPEED",
    "range": "AttributeRegister.*_RANGE",
    "attack": "AttributeRegister.DAMAGE(+ATTACK scope)",
    "spell": "AttributeRegister.DAMAGE(+SPELL scope)",
    "damage": "AttributeRegister.DAMAGE",
    "attribute": "AttributeRegister.STRENGTH/DEXTERITY/INTELLIGENCE",
    "minion": "entityMod(minion)",
    "totem": "entityMod(totem)",
    "companion": "entityMod(companion)",
    "presence": "entityMod(presence_allies)",
    "ally": "entityMod(ally)",
    "ground": "StatusValue(ground)",
    "hazard": "AttributeRegister.HAZARD_*",
    "herald": "AttributeRegister.HERALD_*",
    "aura": "AttributeRegister.AURA_*",
    "curse": "AttributeRegister.CURSE_*",
    "warcry": "AttributeRegister.WARCY_*",
    "movement": "AttributeRegister.MOVEMENT_SPEED",
    "attribute_any": "AttributeRegister.*_ATTRIBUTE",
    "skill": "n/a",
    "item": "n/a",
    "projectile": "AttributeRegister.DAMAGE(+PROJECTILE scope)",
    "melee": "AttributeRegister.DAMAGE(+MELEE scope)",
}

FLOW_RANK = {
    "convert": 0, "dtc": 1, "recoup": 2, "leech": 3, "recover": 4,
    "grant": 5, "status": 6,                       # FIRE 子类
    "more": 10, "pctAdd": 11, "inc": 12, "chance": 13,
    "regen": 14, "flat": 15, "condition": 16, "entity": 17,
    "esRecharge": 18, "unknown": 19,
}


def flow_of(name):
    t = set(name.split("_"))
    tl = {x.lower() for x in t}
    # ---- FIRE 触发型优先 ----
    if "AS_EXTRA" in t or "CONVERT" in t or ("EXTRA" in t and "FROM" in t) \
       or (name.startswith("GAIN_OF_") and "AS_EXTRA" in t):
        return "FIRE", "convert"
    if "DAMAGE_TAKEN" in name or ("ARMOR" in t and "APPLIES" in t) \
       or ("TAKEN" in t and "AS" in t and "EXTRA" not in t):
        return "FIRE", "dtc"
    if "RECOUP" in t:
        return "FIRE", "recoup"
    if "LEECH" in t:
        return "FIRE", "leech"
    event_markers = {"KILL", "HIT", "CRIT", "BLOCK", "USE", "CONSUME",
                     "EVENT", "STUNNED", "HEAVY", "MARK"}
    if "RECOVER" in t \
       or (("REGEN" in t or "HEAL" in t or "GAIN" in t) and (t & event_markers)):
        return "FIRE", "recover"
    if "GRANT" in t or "ON_KILL" in t or "ON_HIT" in t or "ON_CRIT" in t \
       or "ON_EVENT" in t or "ON_BLOCK" in t or "CHANCE_ON" in t \
       or ("WHEN" in t and "YOU" in t) or ("PER" in t and "KILL" in t):
        return "FIRE", "grant"
    if ("APPLY" in t and "STATUS" in t) or ("GRANT" in t and "STACK" in t) \
       or name.endswith("_STATUS") or "STATUS" in t and "WHILE" in t:
        return "FIRE", "status"
    # ---- FOLD 层 ----
    if "MORE" in t:
        return "FOLD", "more"
    if "PCT" in t or "PERCENT" in t:
        return "FOLD", "pctAdd"
    if t & {"INC", "INCREASED", "INCR"}:
        return "FOLD", "inc"
    if "CHANCE" in t:
        return "FOLD", "chance"
    if "REGEN" in t:
        return "FOLD", "regen"
    if "ENERGY" in t and "RECHARGE" in t:
        return "FOLD", "esRecharge"
    if t & {"TO", "ADD", "ADDS", "GAIN", "GAINS", "ADDITIONAL"}:
        return "FOLD", "flat"
    if "IF" in t or "WHILE" in t or "WHEN" in t:
        return "FOLD", "condition"
    if t & {"MINION", "TOTEM", "COMPANION", "ALLIES", "PRESENCE", "ALLY"}:
        return "FOLD", "entity"
    return "FOLD", "unknown"


def mechanism_of(name):
    t = set(name.split("_"))
    for mech, kws in MECH_KEYWORDS:
        if t & set(kws):
            return mech
    return "other"


def scope_of(name):
    t = set(name.split("_"))
    return sorted(t & SCOPE_VOCAB)


def load_constants():
    lines = STATIDS.read_text(encoding="utf-8").splitlines()
    out = []
    for i, ln in enumerate(lines):
        m = DECL.search(ln)
        if not m:
            continue
        out.append((m.group(1), m.group(2)))
    return out


def collect_refs():
    refs = {}
    pat = re.compile(r"StatIds\.([A-Z][A-Z0-9_]*)")
    for p in SRC.rglob("*.java"):
        sp = str(p).replace("\\", "/")
        if "support/stat/StatIds.java" in sp or ".step" in sp or ".bak" in sp or ".pre_xstrip" in sp:
            continue
        try:
            text = p.read_text(encoding="utf-8")
        except Exception:
            continue
        for mm in pat.finditer(text):
            refs.setdefault(mm.group(1), 0)
            refs[mm.group(1)] += 1
    return refs


def main():
    consts = load_constants()
    refs = collect_refs()

    rows = []
    for name, sid in consts:
        layer, flow = flow_of(name)
        mech = mechanism_of(name)
        scope = scope_of(name)
        attr = ATTR_EST.get(mech, "n/a")
        rows.append({
            "name": name, "id": sid, "layer": layer, "flow": flow,
            "mech": mech, "scope": scope, "attr_est": attr,
            "ref": refs.get(name, 0),
        })

    # 排序：layer(FIRE 先) -> flow_rank -> mech -> name
    rows.sort(key=lambda r: (0 if r["layer"] == "FIRE" else 1, FLOW_RANK[r["flow"]],
                             r["mech"], r["name"]))

    # 聚合
    c_layer = Counter(r["layer"] for r in rows)
    c_flow = Counter((r["layer"], r["flow"]) for r in rows)
    c_mech = Counter(r["mech"] for r in rows)
    c_ref = Counter("wired" if r["ref"] > 0 else "reserved" for r in rows)

    print("=" * 82)
    print("StatIds 按 StatGen 实现流程分类排序（预估，只读）")
    print("=" * 82)
    print(f"常量总数            : {len(rows)}")
    print(f"已接(wired)/reserved : {c_ref['wired']} / {c_ref['reserved']}")
    print(f"Layer 分布           : FIRE={c_layer['FIRE']}  FOLD={c_layer['FOLD']}")
    print()
    print("--- Flow 分布（layer/flow : 计数）---")
    for (lay, fl), n in sorted(c_flow.items(), key=lambda x: (0 if x[0][0] == 'FIRE' else 1, FLOW_RANK[x[0][1]])):
        print(f"  {lay:5}/{fl:11} : {n}")
    print()
    print("--- Mechanism 分布（top 30）---")
    for mech, n in c_mech.most_common(30):
        print(f"  {mech:16} : {n}")
    print()

    print("--- 每层每 flow 样本（前 6，含 mech/scope/ref）---")
    shown = defaultdict(int)
    for r in rows:
        key = (r["layer"], r["flow"])
        if shown[key] >= 6:
            continue
        shown[key] += 1
        print(f"  [{r['layer']}/{r['flow']}] {r['name']}  mech={r['mech']} scope={r['scope']} ref={r['ref']}")

    OUT.write_text(json.dumps(rows, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"\n[written sorted] {OUT}  ({len(rows)} 行)")


if __name__ == "__main__":
    main()
