# -*- coding: utf-8 -*-
"""
step10_classify.py —— Stat 体系「Ids 分类 + 按类别逐项验证 StatGen 实现」

真相来源（三方交叉）：
  1) StatIds.java          —— 678 个常量：name / path / comment（POE2 描述原文）
  2) 生成物 stat_entry/*.json —— StatGen 的实际产出（effect type / attribute / operation / mode / tags / amount）
  3) 规则引擎              —— 由 comment 语义推导「应有的实现形态」，与 (2) 实测比对

分类维度：
  L1  effect type（attribute / damage_convert / immune / heal / ...）
  L2  亚型（attribute: operation×mode；damage_convert: mode；其余按关键字段）
  L3  语义族（Id 名前缀：INC_ / EXTRA_ / ADD_ / MAX_ / CHANCE_ ...）

用法：
  python poe2_assets/step10_classify.py            # 控制台摘要
  python poe2_assets/step10_classify.py --full     # 附逐条明细
"""
import json
import os
import re
import sys
import collections

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
GENDIR = os.path.join(ROOT, "src/generated/resources/data/coe/coe/stat_entry")
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")
ATTRREG = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/AttributeRegister.java")
OUT = os.path.join(ROOT, "poe2_assets/_test/step10_classify.md")

FULL = "--full" in sys.argv

# ---------------------------------------------------------------- 0) 属性基值表
# StatEngine.fold: out = original + add + baseValue * multBase; out *= (1 + multTotal)
#   → base == 0 时 ADD_MULTIPLIED_BASE 贡献恒为 0（静默失效）。
REG_RE = re.compile(
    r"public static Holder<Attribute> ([A-Z][A-Z0-9_]*)\s*=\s*"
    r"register(Double|Percentage|Boolean)\(\"([a-z0-9_]+)\"(?:\s*,\s*([-\d.]+|true|false))?")

# 原版属性基值（EntityAttributeModificationEvent 未覆写，取原版默认；玩家视角）
VANILLA_BASE = {
    "minecraft:max_health": 20.0,
    "minecraft:attack_damage": 2.0,   # 玩家 1.0，通用 2.0；均非 0
    "minecraft:attack_speed": 4.0,
    "minecraft:movement_speed": 0.1,
    "minecraft:armor": 0.0,           # ← 零基！
    "minecraft:scale": 1.0,
    "minecraft:block_break_speed": 1.0,
    "minecraft:flying_speed": 0.4,
    "minecraft:jump_strength": 0.42,
    "minecraft:sneaking_speed": 0.3,
    "minecraft:submerged_mining_speed": 0.2,
}


def load_attr_bases():
    """返回 {'coe:xxx': (kind, base)}；无显式默认值的 register 重载 → base 0.0。"""
    out = {}
    if not os.path.isfile(ATTRREG):
        return out
    for m in REG_RE.finditer(open(ATTRREG, encoding="utf-8").read()):
        _const, kind, path, dv = m.group(1), m.group(2), m.group(3), m.group(4)
        if dv is None:
            base = 0.0
        elif dv == "true":
            base = 1.0
        elif dv == "false":
            base = 0.0
        else:
            base = float(dv)
        out["coe:" + path] = (kind, base)
    out.update({k: ("Vanilla", v) for k, v in VANILLA_BASE.items()})
    return out

DEAD_DECL_RE = re.compile(
    r'public\s+static\s+Holder<Attribute>\s+(\w+)\s*=\s*register\w*\(\s*"([^"]+)"')


def load_dead_attrs():
    """返回无运行时读取器的属性 id 集合（coe:xxx）。

    判据：排除 data/gen/**、AttributeRegister.java、StatIds.java 后，
    全工程 .java 无该属性常量名引用 → 写入的 modifier 无消费方（死属性）。
    """
    if not os.path.isfile(ATTRREG):
        return frozenset()
    decl = {}  # const -> coe:id
    for m in DEAD_DECL_RE.finditer(open(ATTRREG, encoding="utf-8").read()):
        decl[m.group(1)] = "coe:" + m.group(2)
    if not decl:
        return frozenset()

    src_root = os.path.join(ROOT, "src", "main", "java")
    pat = re.compile(r"\b(" + "|".join(
        sorted(decl.keys(), key=len, reverse=True)) + r")\b")
    referenced = set()
    for dirpath, _dirnames, filenames in os.walk(src_root):
        if "/data/gen" in dirpath.replace("\\", "/"):
            continue
        for fn in filenames:
            if not fn.endswith(".java") or fn in ("AttributeRegister.java", "StatIds.java"):
                continue
            try:
                s = open(os.path.join(dirpath, fn), encoding="utf-8").read()
            except OSError:
                continue
            referenced.update(m.group(1) for m in pat.finditer(s))
    return frozenset(aid for const, aid in decl.items() if const not in referenced)


# ---------------------------------------------------------------- 1) 解析 StatIds
CONST_RE = re.compile(r"public static final Identifier ([A-Z][A-Z0-9_]*)\s*=\s*id\(\"([a-z0-9_]+)\"\)")


def load_statids():
    """返回 [(name, path, comment)]，comment 取常量声明上方最近的连续 // 注释块末行。"""
    lines = open(STATIDS, encoding="utf-8").read().splitlines()
    out = []
    for i, ln in enumerate(lines):
        m = CONST_RE.search(ln)
        if not m:
            continue
        name, path = m.group(1), m.group(2)
        # 向上收集连续注释行
        cmt = []
        j = i - 1
        while j >= 0 and lines[j].strip().startswith("//"):
            cmt.append(lines[j].strip()[2:].strip())
            j -= 1
        cmt.reverse()
        out.append((name, path, " ".join(cmt).strip(), i + 1))
    return out


# ---------------------------------------------------------------- 2) 载入生成物
def load_generated():
    m = {}
    if not os.path.isdir(GENDIR):
        return m
    for f in os.listdir(GENDIR):
        if not f.endswith(".json"):
            continue
        try:
            m[f[:-5]] = json.load(open(os.path.join(GENDIR, f), encoding="utf-8"))
        except Exception:
            pass
    return m


# ---------------------------------------------------------------- 3) 分类
FIVE = ("physical", "fire", "cold", "lightning", "chaos")

SEM_FAMILY = [
    ("EXTRA_*_FROM_*", re.compile(r"^EXTRA_[A-Z]+_FROM_[A-Z]+$")),
    ("EXTRA_*", re.compile(r"^EXTRA_")),
    ("INC_*", re.compile(r"^INC_")),
    ("ADD_*", re.compile(r"^ADD_")),
    ("MAX_*", re.compile(r"^MAX_")),
    ("MORE_*", re.compile(r"^MORE_")),
    ("LESS_*", re.compile(r"^LESS_")),
    ("REDUCED_*", re.compile(r"^REDUCED_")),
    ("CHANCE_*", re.compile(r"CHANCE")),
    ("IMMUNE_*", re.compile(r"IMMUNE")),
    ("*_RESISTANCE", re.compile(r"RESISTANCE")),
    ("*_PER_*", re.compile(r"_PER_")),
    ("RECOUP/LEECH/REGEN", re.compile(r"RECOUP|LEECH|REGEN")),
    ("AILMENT/STATUS", re.compile(r"IGNITE|FREEZE|SHOCK|BLEED|POISON|CHILL|STUN|ELECTROCUT")),
    ("SKILL_SCOPED", re.compile(r"HERALD|BANNER|GRENADE|HAZARD|CURSE|MARK|WARCRY|META|MINION|OFFERING|TOTEM|BRAND|TRAP")),
]


def semantic_family(name):
    for label, rx in SEM_FAMILY:
        if rx.search(name):
            return label
    return "OTHER"


def l2_subtype(j):
    t = j.get("type")
    if t == "attribute":
        op = j.get("operation", "?")
        mode = j.get("mode", "gain")
        return f"{op}/{mode}"
    if t in ("damage_convert", "random_convert"):
        return j.get("mode", "?")
    if t == "damage_taken_convert":
        return f"{j.get('reduction','?')}->{j.get('to', j.get('taken_as','?'))}"
    if t == "immune":
        st = j.get("statuses") or j.get("status") or "?"
        return ",".join(st) if isinstance(st, list) else str(st)
    if t in ("heal", "resource_convert", "leech", "recoup"):
        r = j.get("resource") or j.get("resources") or "?"
        return ",".join(r) if isinstance(r, list) else str(r)
    if t == "delivery":
        return str(j.get("aspect", j.get("kind", "?")))
    if t in ("chance_on_event", "grant_on_event"):
        return str(j.get("grant", j.get("status", "?")))
    if t == "lucky":
        return str(j.get("aspect", "?"))
    if t == "hit_flag":
        return str(j.get("flag", "?"))
    return "-"


# ---------------------------------------------------------------- 4) 规则引擎
# 每条规则：(编号, 描述, 触发条件(comment/name), 期望校验(j)->bool|None, 严重度)
PCT_INC = re.compile(r"\{X\}%\s+(increased|reduced)\b", re.I)
PCT_MORE = re.compile(r"\{X\}%\s+(more|less)\b", re.I)
FLAT_TO = re.compile(r"^\+?\{X\}\s+to\s+", re.I)
AS_EXTRA = re.compile(r"as\s+Extra\s+\w+\s+Damage", re.I)
TAKEN_AS = re.compile(r"Damage\s+taken\s+as\b", re.I)
IMMUNE_TO = re.compile(r"\bImmune\s+to\b", re.I)
LEECH_RE = re.compile(r"\bLeech(es|ed)?\b", re.I)
RECOUP_RE = re.compile(r"\bRecouped?\b", re.I)
PRESENCE_RE = re.compile(r"\bPresence\b", re.I)
CONVERTED_TO = re.compile(r"Converted\s+to\b", re.I)
CANNOT_RE = re.compile(r"\bCannot\b", re.I)
HAS_VAR = re.compile(r"\{X\}")


# amount 恒 1 属设计（数值载于 effect 自身字段 / 布尔旗标），S1 不适用
AMT_CONST_BY_DESIGN = {"delivery", "hit_flag", "lucky", "immune", "requirement",
                       "charge_convert", "create_ground_on_ailment"}


def check_rules(name, path, cmt, j, bases, dead_attrs=frozenset()):
    """返回 [(rule, severity, msg)]；j 为生成 JSON（可能为 None）。"""
    v = []
    if j is None:
        v.append(("R0", "P0", "无生成物：StatGen 未注册 → 死 Stat"))
        return v
    t = j.get("type")
    op = j.get("operation")
    amt = j.get("amount")
    attr = str(j.get("attribute", ""))
    base_info = bases.get(attr)
    base = base_info[1] if base_info else None

    # ============ A3（P0）：属性无运行时读取器 → 写入 modifier 无任何消费方 ============
    # 判据见 _test/_deadattr.py：排除 data/gen/**、AttributeRegister.java、StatIds.java 后
    # 全工程 .java 无该属性常量引用 → 该 attribute 路线恒不生效（死属性）。
    if t == "attribute" and attr in dead_attrs:
        v.append(("A3", "P0",
                  f"属性 {attr} 全工程无运行时读取器 → 修饰写入后无消费方，**恒不生效**"))

    # ============ A1（P0 硬不变量）：零基属性上 ADD_MULTIPLIED_BASE 静默失效 ============
    # StatEngine.fold: out = original + add + baseValue * multBase → base==0 时贡献恒 0
    if t == "attribute" and op == "add_multiplied_base":
        if base_info is None:
            v.append(("A0", "P3", f"属性 {attr} 未在 AttributeRegister 解析到基值（无法校验）"))
        elif base == 0.0:
            v.append(("A1", "P0",
                      f"零基属性 {attr}(base=0) 上用 add_multiplied_base → baseValue*multBase 恒 0，**修饰静默失效**"))

    # ============ A2：非零基属性 + '% increased' 却用 add_value（应为 multiplied_base）============
    if t == "attribute" and op == "add_value" and base_info and base != 0.0:
        if PCT_INC.search(cmt):
            v.append(("A2", "P1",
                      f"非零基属性 {attr}(base={base}) 描述为 '{{X}}% increased' 却用 add_value（应为 add_multiplied_base）"))

    # R1 {X}% increased/reduced → attribute + add_multiplied_base
    #   例外：零基属性必须用 add_value（乘零归零），由 A1/A2 覆盖，此处不重复报
    if PCT_INC.search(cmt) and not PCT_MORE.search(cmt) and not AS_EXTRA.search(cmt):
        if t != "attribute":
            v.append(("R1", "P1", f"描述为 '{{X}}% increased/reduced' 但 effect={t}（应为 attribute）"))
        elif op not in ("add_multiplied_base",) and not (base == 0.0):
            v.append(("R1", "P1", f"描述为 '{{X}}% increased/reduced' 但 operation={op}（应为 add_multiplied_base）"))

    # R2 {X}% more/less → add_multiplied_total
    if PCT_MORE.search(cmt):
        if t == "attribute" and op != "add_multiplied_total":
            v.append(("R2", "P1", f"描述为 '{{X}}% more/less' 但 operation={op}（应为 add_multiplied_total）"))

    # R3 "+{X} to ..." 平面加值 → add_value
    if FLAT_TO.search(cmt) and "%" not in cmt.split(" to ")[0]:
        if t == "attribute" and op != "add_value":
            v.append(("R3", "P1", f"描述为 '+{{X}} to ...' 平面加值但 operation={op}（应为 add_value）"))

    # R4 "as Extra <T> Damage" → damage_convert / random_convert，mode=gain
    if AS_EXTRA.search(cmt):
        if t not in ("damage_convert", "random_convert"):
            v.append(("R4", "P1", f"描述为 'as Extra ... Damage' 但 effect={t}（应为 damage_convert/random_convert）"))
        elif j.get("mode") != "gain":
            v.append(("R4", "P1", f"'as Extra' 语义应 mode=gain，实测 mode={j.get('mode')}"))

    # R5 "Damage taken as" → damage_taken_convert
    if TAKEN_AS.search(cmt):
        if t not in ("damage_taken_convert", "random_taken_convert"):
            v.append(("R5", "P1", f"描述为 'Damage taken as' 但 effect={t}（应为 damage_taken_convert）"))

    # R6 "Immune to" → immune
    if IMMUNE_TO.search(cmt):
        if t != "immune":
            v.append(("R6", "P1", f"描述含 'Immune to' 但 effect={t}（应为 immune）"))

    # R7 Leech / Recoup 通道
    if LEECH_RE.search(cmt) and t not in ("leech", "attribute"):
        v.append(("R7", "P2", f"描述含 Leech 但 effect={t}"))
    if RECOUP_RE.search(cmt) and t not in ("recoup", "attribute"):
        v.append(("R7", "P2", f"描述含 Recoup 但 effect={t}"))

    # R8 Presence 光环
    if PRESENCE_RE.search(cmt) and t not in ("presence_aura", "attribute"):
        v.append(("R8", "P2", f"描述含 Presence 但 effect={t}"))

    # R9 "Converted to" → damage_convert replace
    if CONVERTED_TO.search(cmt) and not AS_EXTRA.search(cmt):
        if t in ("damage_convert", "random_convert") and j.get("mode") != "replace":
            v.append(("R9", "P1", f"'Converted to' 应 mode=replace，实测 {j.get('mode')}"))

    # S1 参数化一致性：描述含 {X} → amount 应为 $value（可变幅度）
    #   例外：delivery/hit_flag/lucky/immune/... 的 amount 恒为 1.0 属设计，数值载于 effect 自身字段
    if HAS_VAR.search(cmt):
        if isinstance(amt, (int, float)) and t not in AMT_CONST_BY_DESIGN:
            v.append(("S1", "P2", f"描述含 {{X}} 可变幅度但 amount 为常量 {amt}（应为 $value）"))
    else:
        if cmt and isinstance(amt, str) and "$value" in amt and not CANNOT_RE.search(cmt):
            v.append(("S1", "P3", f"描述无 {{X}}（固定效果）但 amount=$value"))

    # S2 EXTRA_<T>_FROM_<S> 结构一致性
    m = re.match(r"^EXTRA_([A-Z]+)_FROM_([A-Z]+)$", name)
    if m and t in ("damage_convert",):
        want_to, want_from = m.group(1).lower(), m.group(2).lower()
        if j.get("to") != want_to:
            v.append(("S2", "P0", f"命名 to={want_to} 但实测 to={j.get('to')}"))
        if list(j.get("from") or []) != [want_from]:
            v.append(("S2", "P0", f"命名 from=[{want_from}] 但实测 from={j.get('from')}"))

    # S3 标签覆盖：Id 名含技能/伤害类别关键词，tags 应包含
    KEY = ["fire", "cold", "lightning", "chaos", "physical", "spell", "attack", "projectile",
           "melee", "minion", "herald", "banner", "grenade", "hazard", "curse", "mark",
           "warcry", "totem", "brand", "trap", "elemental"]
    if t == "attribute":
        tags = set(j.get("tags") or [])
        low = name.lower()
        for k in KEY:
            # 仅检查作为独立词段出现的关键词，避免 'coldest' 之类误判
            if re.search(r"(^|_)" + k + r"(_|$)", low) and k not in tags:
                # 属性本身即该维度时（如 attribute=coe:fire_resistance）不算缺标签
                attr = str(j.get("attribute", ""))
                if k not in attr:
                    v.append(("S3", "P3", f"Id 含 '{k}' 但 tags={sorted(tags)} 未含（attribute={attr}）"))
                    break
    return v


# ---------------------------------------------------------------- 5) 主流程
def main():
    ids = load_statids()
    gen = load_generated()
    bases = load_attr_bases()
    dead_attrs = load_dead_attrs()

    rows = []
    for name, path, cmt, ln in ids:
        j = gen.get(path)
        # extraDamage 基座：本体无 json，但展开为 path_from_<五源>
        expansions = sorted(k for k in gen if k.startswith(path + "_from_"))
        if j is None and expansions:
            j = dict(gen[expansions[0]])
            j["__expanded__"] = len(expansions)
        t = j.get("type") if j else None
        rows.append({
            "name": name, "path": path, "cmt": cmt, "line": ln, "json": j,
            "l1": t or "<MISSING>",
            "l2": l2_subtype(j) if j else "-",
            "l3": semantic_family(name),
            "viol": check_rules(name, path, cmt, j, bases, dead_attrs),
        })

    # 分类统计
    by_l1 = collections.Counter(r["l1"] for r in rows)
    by_l3 = collections.Counter(r["l3"] for r in rows)
    l2_of = collections.defaultdict(collections.Counter)
    for r in rows:
        l2_of[r["l1"]][r["l2"]] += 1

    viol = [r for r in rows if r["viol"]]
    sev = collections.Counter(s for r in rows for _, s, _ in r["viol"])
    by_rule = collections.Counter(ru for r in rows for ru, _, _ in r["viol"])

    L = []
    L.append("# Stat Ids 分类 + StatGen 实现逐类验证（step10）\n")
    L.append(f"- StatIds 常量：**{len(ids)}**")
    L.append(f"- 生成 stat_entry JSON：**{len(gen)}**")
    L.append(f"- 有实现（含展开）：**{sum(1 for r in rows if r['json'])}** / 无实现：**{sum(1 for r in rows if not r['json'])}**")
    L.append(f"- 触发规则的条目：**{len(viol)}**；违规项按严重度：" +
             ", ".join(f"{k}={v}" for k, v in sorted(sev.items())))
    L.append("")

    L.append("## L1 分类：按 effect type\n")
    L.append("| effect type | 条目数 | 运行时通道 |")
    L.append("|---|---:|---|")
    CHAN = {
        "attribute": "FOLD（StatEngine.fold 读属性时聚合）",
        "damage_convert": "DAMAGE_CONVERT（伤害类型重路由，减免前）",
        "random_convert": "DAMAGE_CONVERT（随机元素目标）",
        "damage_taken_convert": "DAMAGE_TAKEN_CONVERT（承伤重路由）",
        "random_taken_convert": "DAMAGE_TAKEN_CONVERT（随机元素）",
        "heal": "FIRE（事件触发资源回复）",
        "damage": "FIRE（事件触发造成伤害）",
        "status": "FIRE（事件触发施加状态）",
        "immune": "IMMUNE（StatusEngine.apply 短路）",
        "lucky": "LUCKY（DamageHelper 双掷）",
        "hit_flag": "HIT_FLAG（DamageHelper 短路）",
        "delivery": "DELIVERY（SupportProfile 投送参数）",
        "presence_aura": "AURA（Presence 范围 tick）",
        "requirement": "INERT（仅词缀生成器读）",
        "resource_convert": "RESOURCE_CONVERT（扣费前重定向）",
        "leech": "FIRE（伤害事件偷取）",
        "recoup": "FIRE（承伤按比例补给）",
        "chance_on_event": "FIRE（概率授予状态）",
        "grant_on_event": "FIRE（事件授予）",
        "charge_convert": "FIRE（充能获得改写）",
        "create_ground_on_ailment": "FIRE（造成异常落地面）",
        "<MISSING>": "**无 —— 死 Stat**",
    }
    for k, v in by_l1.most_common():
        L.append(f"| `{k}` | {v} | {CHAN.get(k,'?')} |")
    L.append("")

    L.append("## L2 分类：各 effect type 的亚型分布\n")
    for k, _ in by_l1.most_common():
        L.append(f"### `{k}`\n")
        L.append("| 亚型 | 条目数 |")
        L.append("|---|---:|")
        for s, c in l2_of[k].most_common():
            L.append(f"| `{s}` | {c} |")
        L.append("")

    L.append("## L3 分类：语义族\n")
    L.append("| 语义族 | 条目数 |")
    L.append("|---|---:|")
    for k, v in by_l3.most_common():
        L.append(f"| {k} | {v} |")
    L.append("")

    # ---- 属性基值 × 运算 交叉矩阵（硬不变量覆盖面）----
    L.append("## 属性基值 × 运算 交叉校验（StatEngine.fold 硬不变量）\n")
    L.append("> `out = original + add + baseValue * multBase; out *= (1 + multTotal)`  \n"
             "> → **base == 0 时 `add_multiplied_base` 贡献恒为 0**，修饰静默失效。\n")
    cross = collections.Counter()
    zero_base_mb = collections.defaultdict(list)   # 零基 + multiplied_base（真 bug）
    zero_base_av = collections.Counter()           # 零基 + add_value（正确做法）
    for r in rows:
        j = r["json"]
        if not j or j.get("type") != "attribute":
            continue
        a = str(j.get("attribute", ""))
        o = j.get("operation")
        bi = bases.get(a)
        bucket = "未知" if bi is None else ("base=0" if bi[1] == 0.0 else "base≠0")
        cross[(bucket, o)] += 1
        if bucket == "base=0" and o == "add_multiplied_base":
            zero_base_mb[a].append(r["name"])
        if bucket == "base=0" and o == "add_value":
            zero_base_av[a] += 1
    L.append("| 属性基值 | operation | 条目数 | 判定 |")
    L.append("|---|---|---:|---|")
    VERDICT = {
        ("base=0", "add_value"): "✅ 正确（零基唯一可行写法）",
        ("base=0", "add_multiplied_base"): "❌ **静默失效**",
        ("base=0", "add_multiplied_total"): "⚠️ 仅当已有 add 贡献时才生效",
        ("base≠0", "add_value"): "⚠️ 平面加值（需描述为 `+{X} to`）",
        ("base≠0", "add_multiplied_base"): "✅ 正确（`{X}% increased`）",
        ("base≠0", "add_multiplied_total"): "✅ 正确（`{X}% more`）",
    }
    for (b, o), c in sorted(cross.items(), key=lambda x: (x[0][0], -x[1])):
        L.append(f"| {b} | `{o}` | {c} | {VERDICT.get((b, o), '—')} |")
    L.append("")
    if zero_base_mb:
        L.append("### ❌ A1 命中：零基属性上的 add_multiplied_base\n")
        L.append("| 属性(base=0) | 条目数 | Id 列表 |")
        L.append("|---|---:|---|")
        for a, names in sorted(zero_base_mb.items(), key=lambda x: -len(x[1])):
            L.append(f"| `{a}` | {len(names)} | " + ", ".join(f"`{n}`" for n in sorted(names)) + " |")
        L.append("")
    else:
        L.append("### ✅ A1 无命中：不存在零基属性上的 add_multiplied_base\n")

    # ---- A3：死属性（attribute 无运行时读取器）----
    a3 = collections.defaultdict(list)
    for r in rows:
        for ru, _s, _m in r["viol"]:
            if ru == "A3":
                a3[str(r["json"].get("attribute", ""))].append(r["name"])
    L.append("## 死属性交叉校验（attribute 是否有运行时读取器）\n")
    L.append("> 判据：排除 `data/gen/**`、`AttributeRegister.java`、`StatIds.java` 后，"
             "全工程 `.java` 无该属性常量引用 → 写入的 AttributeModifier 无任何消费方。\n")
    L.append(f"- AttributeRegister 中无运行时读取器的属性总数：**{len(dead_attrs)}**")
    L.append(f"- 其中被 stat_entry 实际引用（A3 命中）：**{len(a3)}** 个属性 / "
             f"**{sum(len(v) for v in a3.values())}** 条 Stat\n")
    if a3:
        L.append("| 死属性 | 条目数 | Id 列表 |")
        L.append("|---|---:|---|")
        for a, names in sorted(a3.items(), key=lambda x: (-len(x[1]), x[0])):
            show = ", ".join(f"`{n}`" for n in sorted(names)[:8])
            if len(names) > 8:
                show += f" … 共 {len(names)}"
            L.append(f"| `{a}` | {len(names)} | {show} |")
        L.append("")
        L.append("> **定性区分**：A 类 = 该效果已有可用 effect 通道（immune / hit_flag / recoup / "
                 "chance_on_event 等）却错用占位属性 → 属**错接**，应收口到活通道；"
                 "B 类 = 属性归属正确、仅运行时消费逻辑未实现 → 属**功能缺口**，保留属性路线。\n")
    else:
        L.append("### ✅ A3 无命中：所有被引用的属性均有运行时读取器\n")

    L.append("## 规则命中统计\n")
    RULE_DESC = {
        "A0": "属性未在 AttributeRegister 解析到基值（无法校验乘式）",
        "A1": "**硬不变量**：零基属性(base=0) + add_multiplied_base → 贡献恒 0，修饰静默失效",
        "A2": "非零基属性 + `{X}% increased` 却用 add_value（幅度语义错）",
        "A3": "**死属性**：attribute 指向无运行时读取器的属性 → 修饰无消费方，恒不生效",
        "R0": "无生成物 = StatGen 未注册（死 Stat）",
        "R1": "`{X}% increased/reduced` 必须 attribute + add_multiplied_base",
        "R2": "`{X}% more/less` 必须 attribute + add_multiplied_total",
        "R3": "`+{X} to ...` 平面加值必须 add_value",
        "R4": "`as Extra <T> Damage` 必须 damage_convert 且 mode=gain",
        "R5": "`Damage taken as` 必须 damage_taken_convert",
        "R6": "`Immune to` 必须 immune",
        "R7": "Leech / Recoup 应走对应通道",
        "R8": "Presence 应走 presence_aura",
        "R9": "`Converted to` 必须 mode=replace",
        "S1": "描述 `{X}` ↔ amount `$value` 参数化一致",
        "S2": "`EXTRA_<T>_FROM_<S>` 命名 ↔ from/to 结构一致",
        "S3": "Id 名中的类别关键词应出现在 tags",
    }
    L.append("| 规则 | 含义 | 命中数 |")
    L.append("|---|---|---:|")
    for k in sorted(by_rule, key=lambda x: (-by_rule[x], x)):
        L.append(f"| `{k}` | {RULE_DESC.get(k,'')} | {by_rule[k]} |")
    L.append("")

    # 按类别逐项列出违规
    L.append("## 逐类别验证明细（仅列违规）\n")
    for k, _ in by_l1.most_common():
        vs = [r for r in rows if r["l1"] == k and r["viol"]]
        tot = by_l1[k]
        L.append(f"### `{k}` —— {tot} 条，违规 {len(vs)} 条\n")
        if not vs:
            L.append("✅ 全部通过规则校验。\n")
            continue
        L.append("| Id | 描述(注释) | 实测 | 问题 |")
        L.append("|---|---|---|---|")
        for r in vs:
            j = r["json"] or {}
            actual = f"{j.get('type')}"
            if j.get("type") == "attribute":
                actual += f" {j.get('attribute')} {j.get('operation')}"
                if j.get("mode"):
                    actual += f" mode={j.get('mode')}"
            elif j.get("type") in ("damage_convert", "random_convert"):
                actual += f" {j.get('from')}→{j.get('to')} {j.get('mode')}"
            msgs = "<br>".join(f"`{ru}`/{sv} {ms}" for ru, sv, ms in r["viol"])
            c = (r["cmt"] or "(无注释)").replace("|", "\\|")[:110]
            L.append(f"| `{r['name']}` | {c} | {actual} | {msgs} |")
        L.append("")

    if FULL:
        L.append("## 附：全量分类表\n")
        L.append("| Id | L1 | L2 | L3 | 描述 |")
        L.append("|---|---|---|---|---|")
        for r in sorted(rows, key=lambda x: (x["l1"], x["l2"], x["name"])):
            c = (r["cmt"] or "").replace("|", "\\|")[:80]
            L.append(f"| `{r['name']}` | {r['l1']} | `{r['l2']}` | {r['l3']} | {c} |")

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    open(OUT, "w", encoding="utf-8").write("\n".join(L))

    # 控制台摘要
    print(f"StatIds={len(ids)}  generated={len(gen)}  implemented={sum(1 for r in rows if r['json'])}"
          f"  missing={sum(1 for r in rows if not r['json'])}")
    print("--- L1 ---")
    for k, v in by_l1.most_common():
        print(f"{v:5d}  {k}")
    print("--- severity ---", dict(sorted(sev.items())))
    print("--- rules ---")
    for k in sorted(by_rule, key=lambda x: (-by_rule[x], x)):
        print(f"{by_rule[k]:5d}  {k}  {RULE_DESC.get(k,'')}")
    print(f"\nreport -> {OUT}")


if __name__ == "__main__":
    main()
