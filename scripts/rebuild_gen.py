# -*- coding: utf-8 -*-
"""生成层完全重构：子条目（effect/targets/conditions）Ids 类 + 内容注册表 + StatGen 纯 Ids 组装。

数据源（当前产物，作为反推基准）：
  src/generated/resources/data/coe/coe/stat/{effect,targets,condition,entry}/*.json

产出（写入 src/main/java/com/cimdy/coe/data/gen/stat/）：
  StatEffectIds.java / StatEffectGen.java
  StatTargetIds.java / StatTargetGen.java
  StatConditionIds.java / StatConditionGen.java
  StatGen.java（重写：777 条纯 Ids 组装 + 保留 facade API）

子条目数据生成统一走「CODEC 工厂方法」：每个条目经对应模型类的构造器 / 工厂方法在生成期
构造为真实对象；Provider 经各 Codec 编码落盘。所有手写字符串收口为 StatTag / EventName /
状态注册表 .id / ChargeType / ResourceType / GemColor / Labels / TargetSelector 及本模组既有
常量类引用（见各 nodes 包下的 *Modes/*Kinds 常量类），不再出现裸字面量。
"""
import json, os, re, glob, sys

ROOT = "src/generated/resources/data/coe/coe/stat"
OUT = "src/main/java/com/cimdy/coe/data/gen/stat"
MODID = "coe"

# coe:<id> -> AttributeRegister 静态字段名（Holder<Attribute>）
ATTRMAP = {}

# minecraft 属性名 -> Attributes 常量（用于文档/校验，实际生成走 vanillaAttr 取 Holder）
MC_ATTR = {
    "max_health": "MAX_HEALTH",
    "armor": "ARMOR",
    "attack_damage": "ATTACK_DAMAGE",
    "attack_speed": "ATTACK_SPEED",
    "movement_speed": "MOVEMENT_SPEED",
    "flying_speed": "FLYING_SPEED",
    "block_break_speed": "BLOCK_BREAK_SPEED",
    "jump_strength": "JUMP_STRENGTH",
    "scale": "SCALE",
    "sneaking_speed": "SNEAKING_SPEED",
    "submerged_mining_speed": "SUBMERGED_MINING_SPEED",
}

# 收口过程中仍被迫回退到裸字面量的清单（仅供收尾审计；等价性门禁不依赖它）。
RAW_FALLBACK = []


def ffile(name):
    hits = glob.glob("src/**/%s" % name, recursive=True)
    return hits[0] if hits else None


# ----------------------------------------------------------------- 源码扫描：常量收口映射表

def scan_string_constants(path, prefix):
    """扫描 `public static final String NAME = "value";` -> {value: prefix + ".NAME"}。"""
    m = {}
    if not path or not os.path.exists(path):
        return m
    src = open(path, encoding="utf-8").read()
    for mm in re.finditer(r'public static final String (\w+)\s*=\s*"([^"]*)"', src):
        m[mm.group(2)] = "%s.%s" % (prefix, mm.group(1))
    return m


def scan_enum_serialized(path, enum_class):
    """扫描枚举常量 `NAME("serialized"[, ...])` -> {serialized: enum_class.NAME.getSerializedName()}。"""
    m = {}
    if not path or not os.path.exists(path):
        return m
    src = open(path, encoding="utf-8").read()
    for mm in re.finditer(r'([A-Z][A-Z0-9_]*)\s*\(\s*"([^"]+)"', src):
        m[mm.group(2)] = "%s.%s.getSerializedName()" % (enum_class, mm.group(1))
    return m


def scan_status_registries():
    """扫描状态定义持有者 StatusDefs.java：
    `public static final StatusType NAME = StatusType.register(NAME_DEF)`（DEF 经 StatusTypeDef.def("id", ...) 声明）
    与 `presence("id", ...)` 辅助（捷族存在性标记，内部 StatusType.register(StatusTypeDef.def(id, ...))）
    -> (id->"StatusDefs.NAME.id" 字符串引用, id->"StatusDefs.NAME" 对象引用, 类名集合)。"""
    path = "src/main/java/com/cimdy/coe/data/codec/support/status/StatusDefs.java"
    src = open(path, encoding="utf-8").read()
    cls = "StatusDefs"
    # DEF 变量名 -> id（StatusTypeDef.def("id", ...)）
    def_id = {}
    for mm in re.finditer(
            r'public static final StatusTypeDef\s+(\w+)\s*=\s*StatusTypeDef\.def\("([^"]+)"', src):
        def_id[mm.group(1)] = mm.group(2)
    m, obj, classes = {}, {}, {cls}
    # 形式一：StatusType NAME = StatusType.register(NAME_DEF)
    for mm in re.finditer(
            r'public static final StatusType\s+(\w+)\s*=\s*StatusType\.register\((\w+)\)', src):
        name, defvar = mm.group(1), mm.group(2)
        idv = def_id.get(defvar)
        if idv is None:
            continue
        m[idv] = "%s.%s.id" % (cls, name)
        obj[idv] = "%s.%s" % (cls, name)
    # 形式二：presence("id", ...) 辅助（捷族存在性标记）
    for mm in re.finditer(
            r'public static final StatusType\s+(\w+)\s*=\s*presence\("([^"]+)"', src):
        name, idv = mm.group(1), mm.group(2)
        m[idv] = "%s.%s.id" % (cls, name)
        obj[idv] = "%s.%s" % (cls, name)
    return m, obj, classes


# 各常量文件（按名定位，避免硬编码路径）
STAT_TAG_MAP = scan_string_constants(ffile("StatTag.java"), "StatTag")
EVENT_NAME_MAP = scan_string_constants(ffile("EventName.java"), "EventName")
STATUS_MAP, STATUS_OBJ_MAP, STATUS_CLASSES = scan_status_registries()
CHARGE_MAP = scan_enum_serialized(ffile("ChargeType.java"), "ChargeType")
RESOURCE_MAP = scan_enum_serialized(ffile("ResourceType.java"), "ResourceType")
GEM_COLOR_MAP = scan_enum_serialized(ffile("GemColor.java"), "GemColor")
LABELS_MAP = scan_string_constants(ffile("Labels.java"), "Labels")
LIVING_STATE_MODE_MAP = scan_string_constants(ffile("LivingStateModes.java"), "LivingStateModes")
GEM_KIND_MAP = scan_string_constants(ffile("GemKinds.java"), "GemKinds")
GEM_AGG_MAP = scan_string_constants(ffile("GemAggregates.java"), "GemAggregates")
EVENT_HISTORY_MODE_MAP = scan_string_constants(ffile("EventHistoryModes.java"), "EventHistoryModes")
EQUIP_STAT_KIND_MAP = scan_string_constants(ffile("EquipmentStatKinds.java"), "EquipmentStatKinds")
MOVEMENT_MODE_MAP = scan_string_constants(ffile("MovementModes.java"), "MovementModes")

COMPARE_OP_MAP = {
    "==": "ComparisonOperator.EQUAL",
    "!=": "ComparisonOperator.NOT_EQUAL",
    "<": "ComparisonOperator.LESS_THAN",
    "<=": "ComparisonOperator.LESS_THAN_OR_EQUAL",
    ">": "ComparisonOperator.GREATER_THAN",
    ">=": "ComparisonOperator.GREATER_THAN_OR_EQUAL",
}

TARGET_MAP = {
    "self": "TargetSelector.SELF",
    "trigger_target": "TargetSelector.EVENT_TARGET",
    "presence_targets": "TargetSelector.PRESENCE_TARGETS",
    "presence_allies": "TargetSelector.PRESENCE_ALLIES",
    "presence_enemies": "TargetSelector.PRESENCE_ENEMIES",
    "minion": "TargetSelector.MINIONS",
    "totem": "TargetSelector.TOTEMS",
    "companion": "TargetSelector.COMPANIONS",
}


# ----------------------------------------------------------------- 基础助手

def java_str(s):
    out = ['"']
    for ch in s:
        if ch == '"': out.append('\\"')
        elif ch == '\\': out.append('\\\\')
        elif ch == '\n': out.append('\\n')
        elif ch == '\r': out.append('\\r')
        elif ch == '\t': out.append('\\t')
        else: out.append(ch)
    out.append('"')
    return ''.join(out)


def upper(field):
    return field.upper()


def num(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return "%d.0" % v
    if isinstance(v, float):
        return repr(v)
    return str(v)


def load_attr_map():
    reg = open("src/main/java/com/cimdy/coe/data/AttributeRegister.java", encoding="utf-8").read()
    m = {}
    for mm in re.finditer(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register\w*\("([^"]+)"', reg):
        m["coe:" + mm.group(2)] = mm.group(1)
    return m


# ----------------------------------------------------------------- 手写条目保留（防 regen 丢条目）
# 手写 EFF/COND/Ids 常量无对应 JSON，regen 默认只产出 JSON 派生条目会静默丢弃它们。
# 故 regen 时读取「即将被覆盖的现有生成文件」，保留其中名称不在派生集合内的条目。

_PRESERVE_FIELD_RE = re.compile(r'public static final Identifier (\w+) = (?:[A-Za-z_]+\.)?id\("([^"]*)"\);')
_PRESERVE_PUT_RE = re.compile(r'm\.put\((\w+)\.(\w+),.*\)\s*;')


def preserve_id_fields(existing_path, derived_names):
    """保留现有 Ids 类中、名称不在派生集合内的字段声明。"""
    out = []
    seen = set()
    if not os.path.isfile(existing_path):
        return out
    for line in open(existing_path, encoding="utf-8"):
        m = _PRESERVE_FIELD_RE.search(line)
        if m and m.group(1) not in derived_names and m.group(1) not in seen:
            seen.add(m.group(1))
            out.append("    " + line.strip())
    return out


def preserve_puts(existing_path, ids_cls, derived_names):
    """保留现有 Gen 类中、EFF/COND 名称不在派生集合内的 m.put 行。"""
    out = []
    seen = set()
    if not os.path.isfile(existing_path):
        return out
    for line in open(existing_path, encoding="utf-8"):
        m = _PRESERVE_PUT_RE.search(line)
        if m and m.group(1) == ids_cls and m.group(2) not in derived_names and m.group(2) not in seen:
            seen.add(m.group(2))
            out.append(line.rstrip("\n"))
    return out


def ids_class(cls, prefix, slugs, doc, subdir):
    lines = ["package com.cimdy.coe.data.gen.stat;", "",
             "import net.minecraft.resources.Identifier;", "",
             "/**", " * " + doc, " * 内容寻址子条目路径缓存：与生成产物 <code>stat/" + subdir + "/&lt;name&gt;.json</code> 1:1，",
             " * 运行期由 LoadEvent 按路径加载缓存，供对应 read 解析。",
             " */",
             "public final class " + cls + " {",
             "    private " + cls + "() {}",
             "    private static Identifier id(String path) {",
             "        return Identifier.fromNamespaceAndPath(\"" + MODID + "\", path);",
             "    }", ""]
    for slug in slugs:
        lines.append("    public static final Identifier " + prefix + upper(slug) + " = id(" + java_str(slug) + ");")
    # 保留手写常量（无对应 JSON，regen 默认会丢弃）
    derived = set(prefix + upper(s) for s in slugs)
    lines += preserve_id_fields(os.path.join(OUT, cls + ".java"), derived)
    lines.append("}")
    return "\n".join(lines)


# ----------------------------------------------------------------- 引用收口助手

def tag_ref(t):
    """标签字面量 -> StatTag / EventName 常量引用（含 consume_/gain_ 工厂与 ! 取反）。"""
    if t.startswith("!"):
        return "StatTag.not(%s)" % tag_ref(t[1:])
    if t.startswith("consume_"):
        suf = t[len("consume_"):]
        if suf in CHARGE_MAP:
            return "EventName.consumeCharge(%s)" % CHARGE_MAP[suf]
        if t in EVENT_NAME_MAP:
            return EVENT_NAME_MAP[t]
        RAW_FALLBACK.append("tag:" + t)
        return java_str(t)
    if t.startswith("gain_"):
        suf = t[len("gain_"):]
        if suf in CHARGE_MAP:
            return "EventName.gainCharge(%s)" % CHARGE_MAP[suf]
        if t in EVENT_NAME_MAP:
            return EVENT_NAME_MAP[t]
        RAW_FALLBACK.append("tag:" + t)
        return java_str(t)
    if t in STAT_TAG_MAP:
        return STAT_TAG_MAP[t]
    if t in EVENT_NAME_MAP:
        return EVENT_NAME_MAP[t]
    RAW_FALLBACK.append("tag:" + t)
    return java_str(t)


def status_ref(idv):
    """状态 id -> 状态注册表 .id 引用（String 语义：grant_on_event 的 status 参数）。"""
    if idv in STATUS_MAP:
        return STATUS_MAP[idv]
    RAW_FALLBACK.append("status:" + idv)
    return java_str(idv)


def status_obj_ref(idv):
    """状态 id -> 状态注册表对象引用（StatusType 语义：ImmuneStatEffect 的 Set<StatusType>）。"""
    if idv in STATUS_OBJ_MAP:
        return STATUS_OBJ_MAP[idv]
    RAW_FALLBACK.append("status_obj:" + idv)
    return java_str(idv)


def el_ref(v):
    """伤害元素 / 减免手段标签 -> StatTag 常量引用（空串保留为空字面量）。"""
    if v == "":
        return java_str("")
    if v in STAT_TAG_MAP:
        return STAT_TAG_MAP[v]
    RAW_FALLBACK.append("el:" + v)
    return java_str(v)


def grant_param_ref(target, param):
    """grant_on_event 的 param：随 target 语义指向不同常量。"""
    if target == "status":
        return status_ref(param)
    if target == "charge":
        if param in CHARGE_MAP:
            return CHARGE_MAP[param]
        RAW_FALLBACK.append("charge:" + param)
        return java_str(param)
    if target == "resource":
        if param in RESOURCE_MAP:
            return RESOURCE_MAP[param]
        RAW_FALLBACK.append("resource:" + param)
        return java_str(param)
    # combo：id 无枚举，保留裸字面量（已与上游确认）
    return java_str(param)


def delivery_ref(v):
    if v in LABELS_MAP:
        return LABELS_MAP[v]
    RAW_FALLBACK.append("delivery:" + v)
    return java_str(v)


# ----------------------------------------------------------------- 表达式助手

def tags_expr(tags):
    if not tags:
        return "TagExpr.ALWAYS"
    return "TagExpr.of(Set.of(%s))" % ", ".join(tag_ref(t) for t in tags)


def set_expr(lst):
    if not lst:
        return "Set.of()"
    return "Set.of(%s)" % ", ".join(java_str(x) for x in lst)


def set_el_expr(lst):
    if not lst:
        return "Set.of()"
    return "Set.of(%s)" % ", ".join(el_ref(x) for x in lst)


def set_tag_expr(lst):
    if not lst:
        return "Set.of()"
    return "Set.of(%s)" % ", ".join(tag_ref(x) for x in lst)


def list_expr(lst):
    if not lst:
        return "List.of()"
    return "List.of(%s)" % ", ".join(java_str(x) for x in lst)


def list_el_expr(lst):
    if not lst:
        return "List.of()"
    return "List.of(%s)" % ", ".join(el_ref(x) for x in lst)


def attr_expr(idv):
    if idv.startswith("coe:"):
        return "AttributeRegister.%s" % ATTRMAP[idv]
    if idv.startswith("minecraft:"):
        return "Attributes.%s" % MC_ATTR[idv[len("minecraft:"):]]
    raise Exception("unknown attr ns: " + idv)


def either_expr(op):
    if op == "set_value":
        return "Either.right(AttributeLimiters.Operation.SET_VALUE)"
    return "Either.left(AttributeModifier.Operation.%s)" % op.upper()


def resource_type_expr(name):
    return "ResourceType.%s" % name.upper()


def status_set_expr(lst):
    if not lst:
        return "Set.of()"
    return "Set.of(%s)" % ", ".join(status_obj_ref(x) for x in lst)


def delivery_params_expr(params):
    if not params:
        return "Map.of()"
    parts = []
    for k, v in params.items():
        if v == "$value":
            val = "ExpressionCodec.VALUE"
        else:
            val = "ExpressionCodec.of(MathParser.parse(%s))" % java_str(v)
        parts.append("%s, %s" % (java_str(k), val))
    return "Map.of(%s)" % ", ".join(parts)


# ----------------------------------------------------------------- 效果表达式（全部收口为常量引用）

def effect_expr(d):
    t = d["type"]
    if t == "attribute":
        mode = "ModifierStatEffect.Mode.REPLACE" if d.get("mode") == "replace" else "ModifierStatEffect.Mode.GAIN"
        return "new ModifierStatEffect(%s, %s, %s, %s)" % (
            attr_expr(d["attribute"]), either_expr(d["operation"]), mode, tags_expr(d.get("tags")))
    if t == "heal":
        direction = "ResourceStatEffect.Direction.COST" if d.get("direction") == "cost" else "ResourceStatEffect.Direction.HEAL"
        return "new ResourceStatEffect(%s, %s, %s)" % (
            resource_type_expr(d["resource"]), direction, tags_expr(d.get("tags")))
    if t == "damage":
        return "new DamageStatEffect(%s)" % tags_expr(d.get("tags"))
    if t == "damage_convert":
        return "new DamageConvertStatEffect(%s, %s, %s, %s, %s, %s, %s, %s)" % (
            set_el_expr(d["from"]),
            el_ref(d["to"]),
            "DamageConvertStatEffect.Mode.%s" % d["mode"].upper(),
            "true" if d.get("hits_only", False) else "false",
            "DamageConvertStatEffect.ToMode.%s" % d.get("to_mode", "fixed").upper(),
            list_el_expr(d.get("candidates", [])),
            tags_expr(d.get("from_context")),
            tags_expr(d.get("tags")))
    if t == "damage_taken_convert":
        return "new DamageTakenConvertStatEffect(%s, %s, %s, %s, %s)" % (
            el_ref(d["reduction"]),
            "DamageTakenConvertStatEffect.Mode.%s" % d["mode"].upper(),
            "DamageConvertStatEffect.ToMode.%s" % d.get("to_mode", "fixed").upper(),
            list_el_expr(d.get("candidates", [])),
            tags_expr(d.get("tags")))
    if t == "resource_convert":
        return "new ResourceConvertStatEffect(%s, %s, %s, %s, %s)" % (
            resource_type_expr(d["from"]),
            resource_type_expr(d["to"]),
            "ResourceConvertStatEffect.Mode.%s" % d.get("mode", "replace").upper(),
            tags_expr(d.get("tags")),
            set_tag_expr(d.get("source_tags", [])))
    if t == "leech":
        return "new LeechStatEffect(%s)" % tags_expr(d.get("tags"))
    if t == "culling":
        return "new CullingStatEffect(%s)" % tags_expr(d.get("tags"))
    if t == "recoup":
        return "new RecoupStatEffect(%s, %s, %s)" % (
            set_tag_expr(d.get("resources", [])), tags_expr(d.get("from_type")), tags_expr(d.get("tags")))
    if t == "grant_on_event":
        da = attr_expr(d["duration_attr"]) if "duration_attr" in d else "null"
        ia = attr_expr(d["intensity_attr"]) if "intensity_attr" in d else "null"
        sa = attr_expr(d["stack_attr"]) if "stack_attr" in d else "null"
        return "new GrantOnEventStatEffect(%s, %s, %s, %s, %s, %s, %s)" % (
            "GrantOnEventStatEffect.GrantTarget.%s" % d["target"].upper(),
            grant_param_ref(d["target"], d["param"]),
            tags_expr(d.get("tags")),
            da, repr(float(d.get("base_duration", 100.0))), ia, sa)
    if t == "charge_convert":
        return "new ChargeConvertStatEffect(%s, %s, %s, %s)" % (
            "ChargeConvertStatEffect.ConvertMode.%s" % d["mode"].upper(),
            CHARGE_MAP.get(d.get("from", ""), java_str(d.get("from", ""))),
            CHARGE_MAP.get(d.get("to", ""), java_str(d.get("to", ""))),
            tags_expr(d.get("tags")))
    if t == "immune":
        return "new ImmuneStatEffect(%s, %s)" % (status_set_expr(d["statuses"]), tags_expr(d.get("tags")))
    if t == "lucky":
        return "new LuckyStatEffect(%s, %s, %s)" % (
            "LuckyStatEffect.RollSubject.%s" % d["subject"].upper(),
            "LuckyStatEffect.RollMode.%s" % d["mode"].upper(),
            tags_expr(d.get("tags")))
    if t == "hit_flag":
        return "new HitFlagStatEffect(%s, %s)" % (
            "HitFlagStatEffect.HitFlag.%s" % d["flag"].upper(), tags_expr(d.get("tags")))
    if t == "requirement":
        return "new RequirementStatEffect(%s)" % ("AttributeModifier.Operation.%s" % d["operation"].upper())
    if t == "consumed_corpse_damage":
        return "new MarkerStatEffect(MarkerStatEffect.Kind.CONSUMED_CORPSE_DAMAGE, List.of(), TagExpr.ALWAYS)"
    if t == "defend":
        return "new MarkerStatEffect(MarkerStatEffect.Kind.DEFEND, %s, TagExpr.ALWAYS)" % list_expr(d.get("condition_tags", []))
    if t == "take_no_volatility_damage":
        return "new MarkerStatEffect(MarkerStatEffect.Kind.TAKE_NO_VOLATILITY_DAMAGE, List.of(), TagExpr.ALWAYS)"
    if t == "delivery":
        return "new DeliveryStatEffect(%s, %s, %s)" % (
            delivery_ref(d["delivery"]), delivery_params_expr(d["params"]), tags_expr(d.get("tags")))
    raise Exception("unknown effect type: " + t)


# ----------------------------------------------------------------- 三个内容注册类

def gen_effects(items):
    imports = [
        "package com.cimdy.coe.data.gen.stat;", "",
        "import com.cimdy.coe.data.AttributeRegister;",
        "import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;",
        "import com.cimdy.coe.data.codec.skill.helper.ResourceType;",
        "import com.cimdy.coe.data.codec.support.Labels;",
        "import com.cimdy.coe.data.codec.support.limiter.AttributeLimiters;",
        "import com.cimdy.coe.data.codec.support.math.ExpressionCodec;",
        "import com.cimdy.coe.data.codec.support.math.MathParser;",
        "import com.cimdy.coe.data.codec.support.stat.StatTag;",
        "import com.cimdy.coe.data.codec.support.stat.TagExpr;",
        "import com.cimdy.coe.data.codec.support.stat.effect.*;",
        "import com.cimdy.coe.data.codec.support.status.StatusType;",
        "import com.cimdy.coe.event.EventName;",
    ]
    for c in sorted(STATUS_CLASSES):
        if c == "StatusDefs":
            imports.append("import com.cimdy.coe.data.codec.support.status.StatusDefs;")
        else:
            imports.append("import com.cimdy.coe.data.codec.support.status.registry.%s.%s;"
                          % (c[:-len("StatusTypes")].lower(), c))
    imports += [
        "import com.mojang.datafixers.util.Either;",
        "import net.minecraft.world.entity.ai.attributes.AttributeModifier;",
        "import net.minecraft.world.entity.ai.attributes.Attributes;",
        "import net.minecraft.resources.Identifier;",
        "import java.util.LinkedHashMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "import java.util.Set;", "",
        "/**",
        " * Stat effect 子条目内容注册表（CODEC 工厂：真实 StatEffect 对象经各模型类构造器构造，",
        " * Provider 经 StatEffectTypes.CODEC 编码落盘，保证与路径 1:1）。全部手写字符串已收口为常量引用。",
        " */",
        "public final class StatEffectGen {",
        "    private StatEffectGen() {}", "",
        "    /** 路径 → 真实 StatEffect 对象（生成期 Provider 经 StatEffectTypes.CODEC 编码落盘）。 */",
        "    public static Map<Identifier, StatEffect> contents() {",
        "        Map<Identifier, StatEffect> m = new LinkedHashMap<>();",
    ]
    for slug, d in items:
        imports.append("        m.put(StatEffectIds.EFF_%s, %s);" % (upper(slug), effect_expr(d)))
    # 保留手写 EFF（无对应 JSON，regen 默认会丢弃）
    derived = set("EFF_" + upper(s) for s, d in items)
    imports += preserve_puts(os.path.join(OUT, "StatEffectGen.java"), "StatEffectIds", derived)
    imports += [
        "        return m;",
        "    }", "",
        "}",
    ]
    return "\n".join(imports)


def target_expr(d):
    assert d.get("type") == "inline", str(d)
    t = d.get("target")
    c = d.get("condition")
    v = d.get("value")
    t_expr = TARGET_MAP.get(t, "null") if t is not None else "null"
    c_expr = TARGET_MAP.get(c, "null") if c is not None else "null"
    v_expr = TARGET_MAP.get(v, "null") if v is not None else "null"
    if t_expr == "null" and c_expr == "null" and v_expr == "null":
        return "StatTargets.EMPTY"
    return "new StatTargetsInline(%s, %s, %s)" % (t_expr, c_expr, v_expr)


def gen_targets(items):
    lines = ["package com.cimdy.coe.data.gen.stat;", "",
             "import com.cimdy.coe.data.codec.support.stat.effect.StatTargets;",
             "import com.cimdy.coe.data.codec.support.stat.effect.StatTargetsInline;",
             "import com.cimdy.coe.data.codec.support.stat.effect.TargetSelector;",
             "import net.minecraft.resources.Identifier;",
             "import java.util.LinkedHashMap;",
             "import java.util.Map;", "",
             "/**",
             " * Stat targets 子条目内容注册表（CODEC 工厂：JSON 经 StatTargets 构造器直接构造真实对象，",
             " * 三段选择器全部引用 TargetSelector 常量，Provider 编码落盘）。",
             " */",
             "public final class StatTargetGen {",
             "    private StatTargetGen() {}", "",
             "    public static Map<Identifier, StatTargets> contents() {",
             "        Map<Identifier, StatTargets> m = new LinkedHashMap<>();"]
    for slug, d in items:
        lines.append("        m.put(StatTargetIds.TGT_%s, %s);" % (upper(slug), target_expr(d)))
    lines += [
        "        return m;",
        "    }", "",
        "}",
    ]
    return "\n".join(lines)


def cond_node_expr(n):
    t = n["type"]
    if t == "living_state":
        mode = LIVING_STATE_MODE_MAP.get(n["mode"], java_str(n["mode"]))
        return "new LivingStateValue(%s, %s)" % (mode, num(n.get("window", 80.0)))
    if t == "status":
        kind = STATUS_MAP.get(n["kind"], java_str(n["kind"]))
        return "new StatusValue(%s, CraftOfExile.EMPTY_LOCATION)" % kind
    if t == "status_age":
        kind = STATUS_MAP.get(n["kind"], java_str(n["kind"]))
        return "new StatusAgeValue(%s, %s)" % (kind, num(n.get("min_ticks", 80.0)))
    if t == "status_tag":
        return "new StatusTagValue(%s)" % java_str(n["tag"])
    if t == "status_kind":
        return "new StatusKindValue(%s)" % java_str(n["kind"])
    if t == "count":
        name = tag_ref(n["name"])
        mode = EVENT_HISTORY_MODE_MAP.get(n.get("mode", "count"), java_str(n.get("mode", "count")))
        if "tag" in n or "match" in n:
            tag = 'Optional.of(%s)' % java_str(n["tag"]) if "tag" in n else "Optional.empty()"
            match = "Event.State.Match.%s" % n.get("match", "exact").upper()
            return "new EventHistoryValue(%s, %s, %s, %s, %s)" % (name, tag, match, mode, num(n.get("window", 80.0)))
        return "new EventHistoryValue(%s, %s, %s)" % (name, mode, num(n.get("window", 80.0)))
    if t == "movement":
        mode = MOVEMENT_MODE_MAP.get(n["mode"], java_str(n["mode"]))
        return "new MovementValue(%s, %s)" % (mode, num(n.get("window", 20.0)))
    if t == "socket":
        color = n.get("color")
        color_arg = "Optional.of(%s)" % GEM_COLOR_MAP[color] if color is not None else "Optional.empty()"
        kind = GEM_KIND_MAP.get(n.get("kind", "all"), java_str(n.get("kind", "all")))
        agg = GEM_AGG_MAP.get(n.get("aggregate", "first"), java_str(n.get("aggregate", "first")))
        return "new GemValue(Optional.empty(), Optional.empty(), %s, %s, Optional.empty(), %s)" % (color_arg, kind, agg)
    if t == "equipment":
        slots = n.get("slots")
        if slots is None:
            slots_arg = "Optional.empty()"
        else:
            slot_list = ", ".join("EquipmentSlot.%s" % s.upper() for s in slots)
            slots_arg = "Optional.of(List.of(%s))" % slot_list
        output = n.get("output")
        output_arg = "Optional.of(%s)" % EQUIP_STAT_KIND_MAP[output] if output is not None else "Optional.empty()"
        agg = "EquipmentValue.Aggregate.%s" % n.get("aggregate", "first").upper()
        return "new EquipmentValue(%s, Optional.empty(), %s, %s)" % (slots_arg, output_arg, agg)
    if t == "compare":
        left = cond_node_expr(n["left"])
        right = cond_node_expr(n["right"])
        op = COMPARE_OP_MAP[n["operator"]]
        return "new CompareValue(%s, %s, %s, %s, %s)" % (left, right, num(n.get("left_scale", 1.0)), num(n.get("right_scale", 1.0)), op)
    if t == "presence_rarity":
        rar = ", ".join("Rarity.%s" % r.upper() for r in n["rarities"])
        enemies = "true" if n.get("enemies_only", True) else "false"
        mode = "PresenceRarityValue.Mode.%s" % n.get("mode", "any").upper()
        return "new PresenceRarityValue(List.of(%s), %s, %s)" % (rar, enemies, mode)
    raise ValueError("unknown condition node type: " + t)


def cond_expr(arr):
    exprs = [cond_node_expr(n) for n in arr]
    if len(exprs) == 1:
        return "List.of(%s)" % exprs[0]
    return "List.of(\n                %s)" % ",\n                ".join(exprs)


def gen_conditions(items):
    imports = [
        "package com.cimdy.coe.data.gen.stat;", "",
        "import com.cimdy.coe.CraftOfExile;",
        "import com.cimdy.coe.data.codec.support.Rarity;",
        "import com.cimdy.coe.data.codec.support.math.ComparisonOperator;",
        "import com.cimdy.coe.data.codec.support.math.context.ContextNode;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.CompareValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.EquipmentValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.EventHistoryValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.GemValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.LivingStateValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.MovementValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.PresenceRarityValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.StatusAgeValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.StatusValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.StatusTagValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.StatusKindValue;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.EquipmentStatKinds;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.EventHistoryModes;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.GemAggregates;",
        "import com.cimdy.coe.data.codec.skill.gem.GemColor;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.GemKinds;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.LivingStateModes;",
        "import com.cimdy.coe.data.codec.support.math.context.nodes.MovementModes;",
        "import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;",
        "import com.cimdy.coe.data.codec.support.stat.StatTag;",
        "import com.cimdy.coe.event.EventName;",
    ]
    if "StatusDefs" in STATUS_CLASSES:
        imports.append("import com.cimdy.coe.data.codec.support.status.StatusDefs;")
    imports += [
        "import net.minecraft.resources.Identifier;",
        "import net.minecraft.world.entity.EquipmentSlot;",
        "import java.util.LinkedHashMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "import java.util.Optional;", "",
        "/**",
        " * Stat condition 子条目内容注册表（CODEC 工厂方法：每个 ContextNode 经对应节点类构造器构造真实对象，",
        " * Provider 经 ContextNode.CODEC.listOf() 编码落盘）。全部手写字符串已收口为常量引用。",
        " */",
        "public final class StatConditionGen {",
        "    private StatConditionGen() {}", "",
        "    public static Map<Identifier, List<ContextNode>> contents() {",
        "        Map<Identifier, List<ContextNode>> m = new LinkedHashMap<>();",
    ]
    for slug, arr in items:
        imports.append("        m.put(StatConditionIds.COND_%s, %s);" % (upper(slug), cond_expr(arr)))
    # 保留手写 COND（无对应 JSON，regen 默认会丢弃）
    derived = set("COND_" + upper(s) for s, arr in items)
    imports += preserve_puts(os.path.join(OUT, "StatConditionGen.java"), "StatConditionIds", derived)
    imports += [
        "        return m;",
        "    }", "",
        "}",
    ]
    return "\n".join(imports)


def amount_expr(a):
    t = a.get("type")
    assert t == "inline", "amount 必须 inline: " + str(a)
    v = a.get("value")
    if isinstance(v, str):
        if v == "$value":
            return "ExpressionCodec.VALUE"
        return "ExpressionCodec.of(MathParser.parse(" + java_str(v) + "))"
    return "ExpressionCodec.of(" + repr(v) + ")"


def preserve_existing(dirp, name):
    """条件为手书注册表：cond_node_expr 未覆盖全部节点类型（charm_active/const/var/...），
    rebuild_gen 无法从 condition JSON 安全重建，故直接保留现有生成文件，避免静默丢失/崩溃。
    EFF/Targets 仍为 JSON 驱动。"""
    p = os.path.join(dirp, name)
    if os.path.isfile(p):
        return open(p, encoding="utf-8").read()
    raise FileNotFoundError("preserve_existing: 文件不存在 " + p)


def main():
    global ATTRMAP
    # ---------- 1. 部件清单 ----------
    eff_slugs = [os.path.basename(f)[:-5] for f in sorted(glob.glob(ROOT + "/effect/*.json"))]
    tgt_slugs = [os.path.basename(f)[:-5] for f in sorted(glob.glob(ROOT + "/targets/*.json"))]
    cond_slugs = [os.path.basename(f)[:-5] for f in sorted(glob.glob(ROOT + "/condition/*.json"))]
    print("parts: effect=%d targets=%d condition=%d" % (len(eff_slugs), len(tgt_slugs), len(cond_slugs)))

    # ---------- 2. 三个 Ids 类 ----------
    write(OUT, "StatEffectIds.java", ids_class("StatEffectIds", "EFF_", eff_slugs,
        "Stat effect 子条目 Ids（路径缓存）", "effect"))
    write(OUT, "StatTargetIds.java", ids_class("StatTargetIds", "TGT_", tgt_slugs,
        "Stat targets 子条目 Ids（路径缓存）", "targets"))
    write(OUT, "StatConditionIds.java", ids_class("StatConditionIds", "COND_", cond_slugs,
        "Stat condition 子条目 Ids（路径缓存）", "condition"))

    # ---------- 3. 三个内容注册类（CODEC 工厂构造真实对象） ----------
    ATTRMAP = load_attr_map()
    eff_items = [(s, json.load(open(ROOT + "/effect/" + s + ".json", encoding="utf-8"))) for s in eff_slugs]
    tgt_items = [(s, json.load(open(ROOT + "/targets/" + s + ".json", encoding="utf-8"))) for s in tgt_slugs]
    cond_items = [(s, json.load(open(ROOT + "/condition/" + s + ".json", encoding="utf-8"))) for s in cond_slugs]
    write(OUT, "StatEffectGen.java", gen_effects(eff_items))
    write(OUT, "StatTargetGen.java", gen_targets(tgt_items))
    # 条件为手书注册表（cond_node_expr 未覆盖全部节点类型），保留现有文件而非从 JSON 重建
    write(OUT, "StatConditionGen.java", preserve_existing(OUT, "StatConditionGen.java"))

    # ---------- 4. StatIds 字段表 ----------
    statids_src = open("src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java", encoding="utf-8").read()
    statids = {p: n for n, p in re.findall(r'public static final Identifier (\w+) = id\("([^"]+)"\);', statids_src)}

    # ---------- 5. StatGen.buildEntries ----------
    entries = []
    skipped = 0
    for f in sorted(glob.glob(ROOT + "/entry/*.json")):
        slug = os.path.basename(f)[:-5]
        d = json.load(open(f, encoding="utf-8"))
        assert d.get("entry") == "inline", slug
        if d["amount"].get("value") != "$value":
            skipped += 1
            continue
        idf = statids.get(slug)
        assert idf, "StatIds 缺字段: " + slug
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
    print("entries generated: %d (skipped external/concrete-amount: %d)" % (len(entries), skipped))

    write(OUT, "StatGen.java", statgen_java("\n".join(entries)))

    # ---------- 收口审计 ----------
    if RAW_FALLBACK:
        from collections import Counter
        cnt = Counter(RAW_FALLBACK)
        print("WARN: %d 处裸字面量仍未被常量收口：" % len(RAW_FALLBACK))
        for k, v in cnt.items():
            print("   %s x%d" % (k, v))
    else:
        print("OK: 全部手写字符串已收口为常量引用，零裸字面量。")


def write(dirp, name, content):
    os.makedirs(dirp, exist_ok=True)
    with open(os.path.join(dirp, name), "w", encoding="utf-8") as fh:
        fh.write(content)
    print("  wrote %s (%d lines)" % (name, len(content.splitlines())))


def statgen_java(entries_body):
    return """package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.AttributeRegister;
import com.cimdy.coe.data.codec.support.limiter.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.MathParser;
import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.codec.support.math.context.ContextNodeRead;
import com.cimdy.coe.data.codec.support.math.context.Conversions;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.support.stat.TagExpr;
import com.cimdy.coe.data.codec.support.stat.effect.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead;
import com.cimdy.coe.data.codec.support.stat.effect.StatTargets;
import com.cimdy.coe.data.codec.support.stat.effect.StatTargetsRead;
import com.cimdy.coe.data.codec.support.stat.entry.Stat;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Stat 条目注册中心（facade，生成层完全重构：子条目路径缓存 + Stat 纯路径组装）。
 */
public class StatGen {
    private static final Map<Identifier, StatEntry> ENTRIES = buildEntries();

    private static final Map<Identifier, StatEntry> EXTERNAL = new LinkedHashMap<>();


    /** 注册一条具体条目；key 冲突且内容不同则报错（避免静默覆盖导致数值漂移）。 */
    public static void register(Identifier id, StatEntry entry) {
        StatEntry prev = EXTERNAL.putIfAbsent(id, entry);
        if (prev != null && !prev.equals(entry)) {
            throw new IllegalStateException("StatGen 条目键冲突且内容不同: " + id);
        }
    }


    /** 全量重构核心助手：注册具体条目并返回其 Read 引用（Stat 四部件经 of() 内容寻址路径化后装配；非 Stat 原样注册）。 */
    public static StatEntry read(Identifier id, StatEntry entry) {
        if (entry instanceof Stat s) {
            ContextNodeRead condRef = ContextNodeRead.of(s.conditions());
            List<ContextNode> conds = condRef == null ? List.of() : List.of(condRef);
            register(id, new Stat(s.amount(), conds,
                    s.targets() == null ? null : StatTargetsRead.of(s.targets()),
                    StatEffectRead.of(s.effect())));
        } else {
            register(id, entry);
        }
        return new StatRead(id);
    }


    /** 便捷助手：注册一条属性修饰条目（常量 amount）并返回 Read 引用。 */
    public static StatEntry modRead(Identifier id, Holder<Attribute> attribute, double amount,
                                   AttributeModifier.Operation op, String... tags) {
        return read(id, ModifierStatEffect.entry(attribute, ExpressionCodec.of(amount),
                Either.left(op), Set.of(tags), List.of()));
    }


    /** 按名称引用 StatGen 中已注册的具体条目（写数据生成用 Read，不重复注册）。 */
    public static StatEntry ref(Identifier id) {
        return new StatRead(id);
    }


    /** 按名称引用 StatGen 中已注册的 $value 模板条目，并注入实际幅度（经 vars 的 "value" 键）。 */
    public static StatEntry ref(Identifier id, double value) {
        return new StatRead(id, Map.of("value", ExpressionCodec.of(value)));
    }


    /**
     * 天赋属性授予（全量重构：写数据生成用 Read，具体条目在此注册为 concrete 条目）。
     */
    public static StatEntry talentAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("talent", attribute, op, tags, null, amount);
        registerModifier(id, attribute, ExpressionCodec.of(amount), op, tags);
        return new StatRead(id);
    }


    /** 天赋属性授予（含来源属性乘子）：数值 = {@code attribute:<source> * amount}（固定表达式，烘进条目）。 */
    public static StatEntry talentAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op,
            Holder<Attribute> sourceAttribute, Set<String> tags) {
        MathExpression source = ExpressionCodec.Variable.from("attribute",
                BuiltInRegistries.ATTRIBUTE.getKey(sourceAttribute.value()), StatGenConsts.ZERO);
        ExpressionCodec.Value value = ExpressionCodec.of(Conversions.convertScale(source, amount));
        Identifier id = attrId("talent", attribute, op, tags, sourceAttribute, amount);
        registerModifier(id, attribute, value, op, tags);
        return new StatRead(id);
    }


    /** 实体 LivingMod 属性授予（数据生成，全量重构）：同 {@link #talentAttr} 范式，前缀 {@code livingmod_}。 */
    public static StatEntry livingModAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("livingmod", attribute, op, tags, null, amount);
        registerModifier(id, attribute, ExpressionCodec.of(amount), op, tags);
        return new StatRead(id);
    }


    /** 技能 ModifierAction 运行时属性授予（全量重构）：$value 模板，幅度经 {@link StatRead#vars} 注入。 */
    public static StatEntry modifierAttr(Holder<Attribute> attribute, double amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        Identifier id = attrId("modifier", attribute, op, tags, null, StatGenConsts.ZERO);
        registerModifier(id, attribute, ExpressionCodec.VALUE, op, tags);
        return new StatRead(id, Map.of("value", ExpressionCodec.of(amount)));
    }


    /** 属性授予类条目稳定键：{@code <prefix>_} + attribute + op + source? + amount(规范化) + 排序后 tags。 */
    private static Identifier attrId(String prefix, Holder<Attribute> attribute,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op,
            Set<String> tags, Holder<Attribute> source, double amount) {
        StringBuilder sb = new StringBuilder(prefix).append('_');
        sb.append(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).toString().replace(':', '_'));
        sb.append('_').append(opKey(op));
        if (source != null) {
            sb.append("_src_").append(BuiltInRegistries.ATTRIBUTE.getKey(source.value()).toString().replace(':', '_'));
        }
        sb.append('_').append(amountKey(amount));
        if (!tags.isEmpty()) {
            sb.append('_').append(String.join("_", tags.stream().sorted().toList()));
        }
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, sb.toString().toLowerCase());
    }


    /** op 的稳定字符串（小写）：op_add_value / lim_set_value 等。 */
    private static String opKey(Either<AttributeModifier.Operation, AttributeLimiters.Operation> op) {
        return op.map(o -> "op_" + o.name().toLowerCase(), l -> "lim_" + l.name().toLowerCase());
    }


    /** amount 的规范化键（去尾零，避免浮点表示不稳定）。 */
    private static String amountKey(double amount) {
        return java.math.BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString().replace('.', '_');
    }


    /** 全部可复用 StatEntry（id → 实例）= 内建核心条目 ⊕ 外部注册条目；运行时由 LoadEvent 直接查表。 */
    public static Map<Identifier, StatEntry> entries() {
        Map<Identifier, StatEntry> all = new LinkedHashMap<>(ENTRIES);
        all.putAll(EXTERNAL);
        return Map.copyOf(all);
    }


    private static Map<Identifier, StatEntry> buildEntries() {
        Map<Identifier, StatEntry> map = new LinkedHashMap<>();
@@ENTRIES@@
        return map;
    }


    /** 纯路径组装（无条件）。 */
    private static Stat stat(Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(), targetsRef, effectRef);
    }


    /** 纯路径组装（单条件）。 */
    private static Stat stat(Identifier condRef, Identifier targetsRef, Identifier effectRef) {
        return stat(List.of(condRef), targetsRef, effectRef);
    }


    /** 纯路径组装（多条件）。 */
    private static Stat stat(List<Identifier> condRefs, Identifier targetsRef, Identifier effectRef) {
        List<ContextNode> conds = condRefs.isEmpty() ? List.of()
                : condRefs.stream().<ContextNode>map(ContextNodeRead::new).toList();
        return new Stat(ExpressionCodec.VALUE, conds, new StatTargetsRead(targetsRef), new StatEffectRead(effectRef));
    }


    /** 动态 concrete 属性修饰条目（talent/livingmod/modifier 系）：effect 部件内容寻址取路径，targets 默认 EMPTY。 */
    private static void registerModifier(Identifier id, Holder<Attribute> attribute, ExpressionCodec.Value amount,
            Either<AttributeModifier.Operation, AttributeLimiters.Operation> op, Set<String> tags) {
        register(id, new Stat(amount, List.of(), StatTargetsRead.of(StatTargets.EMPTY),
                StatEffectRead.of(new ModifierStatEffect(attribute, op, ModifierStatEffect.Mode.GAIN, TagExpr.of(tags)))));
    }


}
""".replace("@@ENTRIES@@", entries_body)


if __name__ == "__main__":
    main()
