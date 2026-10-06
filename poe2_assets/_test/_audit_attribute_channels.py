#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
属性层通道审计（纯读 .java，不读 JSON）。

目标：量化"属性不通过通道直连读取 / 属性未归一直接使用"的范围，输出可复核的分类表。

分类定义：
  R = 消费端 getAttributeValue(AttributeRegister.X) 读取的属性集合
  W = StatGen 通过 ModifierStatEffect（add/inc/more/valueMod/pctAdd/chance/...）接线的属性集合
  (1) 悬空直读  = R - W   ：消费端读但 StatGen 从未写 -> 读的是注册默认值（绕过 Stat 数据驱动）
  (2) 无消费者  = W - R   ：StatGen 写了但消费端从不读 -> 数据先行悬空（含触发型未接 fire 通道）
  (3) 零基 inc 死 = 属性 base=0 且被 inc/addMultBase 接 -> ADD_MULTIPLIED_BASE 贡献恒 0（静默死）
  (4) 作用域焊死命名 = AttributeRegister 常量名含大写作用域 token（SLAM/MACE/FIRE/COLD/...）

注：作用域维度归一后，(4) 在 AttributeRegister 中应已基本消除；本脚本兼查 StatIds/词缀层面的焊死命名。
"""
import re, os, json, sys

ROOT = "E:/McModding/Modding/CraftOfExile-NeoForge"
SRC = os.path.join(ROOT, "src/main/java")
ATTR = os.path.join(SRC, "com/cimdy/coe/data/AttributeRegister.java")
STAGEN = os.path.join(SRC, "com/cimdy/coe/data/gen/stat/StatGen.java")

# 纯作用域维度 token（焊进属性名 = 未归一）；排除子系统机制名（CHARM/FLASK/DARKNESS/THORNS/CRIT/DAMAGE 等为合法子系统）
SCOPE_TOKENS = ["SLAM","MACE","SPEAR","AXE","BOW","CROSSBOW","MELEE","SPELL","ATTACK","FIRE","COLD",
                "LIGHTNING","LIGHT","CHAOS","PHYSICAL","POISON","BLEED","ELEMENTAL","MINION","TOTEM",
                "COMPANION","PRESENCE","PROJECTILE","PARRY","DEFLECT","FORM","BOLT","ARROW","GRENADE",
                "HAZARD","HERALD","STUN","FREEZE","SHOCK","IGNITE","DAZE","BLIND","WARCRY","CURSE",
                "AURA","EXPOSURE","ARCANE_SURGE","WITHERED","BUFF"]

def walk_java():
    for dp,_,fs in os.walk(SRC):
        for f in fs:
            if f.endswith(".java") and not f.endswith(".step6bak") and not f.endswith(".bak"):
                yield os.path.join(dp,f)

# ---------- (A) AttributeRegister 解析 ----------
reg_re = re.compile(r'public static Holder<Attribute>\s+(\w+)\s*=\s*register(Double|Percentage|Boolean)\s*\(\s*"([^"]+)"\s*(,\s*([-\d.]+)(\s*,\s*([-\d.]+))?(\s*,\s*([-\d.]+))?)?\s*\)')
attr_defs = {}  # name -> (type, default)
with open(ATTR, encoding="utf-8") as fh:
    for line in fh:
        m = reg_re.search(line)
        if m:
            name, typ = m.group(1), m.group(2)
            default = float(m.group(5)) if m.group(5) else 0.0
            attr_defs[name] = (typ, default)

# ---------- (B) StatGen 接线属性 ----------
# (add|inc|more|valueMod|pctAdd|chance|regenMod|entityMod|conditionMod|vsExposed|add)\s*\(map,\s*StatIds\.\w+,\s*AttributeRegister\.X
wired = {}  # attr_name -> list of (op, statid)
wire_re = re.compile(r'\b(add|inc|more|valueMod|pctAdd|chance|regenMod|entityMod|conditionMod|vsExposed)\s*\(\s*map\s*,\s*StatIds\.(\w+)\s*,\s*AttributeRegister\.(\w+)')
with open(STAGEN, encoding="utf-8") as fh:
    for ln, line in enumerate(fh, 1):
        for m in wire_re.finditer(line):
            op, sid, an = m.group(1), m.group(2), m.group(3)
            wired.setdefault(an, []).append((op, sid, ln))

# ---------- (C) 消费端直读 ----------
read_re = re.compile(r'getAttributeValue\(\s*AttributeRegister\.(\w+)')
reads = {}  # attr_name -> list of file
for path in walk_java():
    try:
        with open(path, encoding="utf-8") as fh:
            for ln, line in enumerate(fh, 1):
                for m in read_re.finditer(line):
                    an = m.group(1)
                    reads.setdefault(an, []).append(os.path.relpath(path, ROOT)+":"+str(ln))
    except Exception:
        pass

W = set(wired)
R = set(reads)
# 排除原版 Attributes.* (不在 AttributeRegister)
all_known = set(attr_defs)

orphan_read = sorted(R - W)            # 悬空直读
no_consumer = sorted(W - R)            # 无消费者
both = sorted(R & W)

# 零基 inc 静默死
zero_base_inc = []
for an, lst in wired.items():
    typ, default = attr_defs.get(an, ("?", None))
    if default in (0.0, 0):
        for op, sid, ln in lst:
            if op in ("inc","valueMod"):  # valueMod 可能是 ADD_VALUE 或 ADD_MULTIPLIED_BASE，需看参数
                zero_base_inc.append((an, op, sid, ln, typ, default))

# 作用域焊死命名（AttributeRegister 常量名）
scope_welded = sorted([n for n in attr_defs if any(tok in n for tok in SCOPE_TOKENS)])

# ---------- 输出 ----------
def show(title, items):
    print(f"\n=== {title} ({len(items)}) ===")
    for it in items:
        if isinstance(it, tuple):
            print("  ", " | ".join(str(x) for x in it))
        else:
            print("  ", it)

print("属性常量总数(AttributeRegister):", len(attr_defs))
print("StatGen 接线属性数:", len(W))
print("消费端直读属性数:", len(R))
print("既接又读属性数:", len(both))

show("悬空直读 R-W（消费端读但 StatGen 未写，读默认值）", orphan_read)
show("无消费者 W-R（StatGen 写但消费端不读，数据先行悬空）", no_consumer)
# 零基 inc 需剔除 valueMod 实际 op=ADD_VALUE 的情况：无法确定，标注待核
show("疑似零基 inc/valueMod（base=0，需人工确认 op 是否 ADD_MULTIPLIED_BASE）", zero_base_inc)
show("作用域焊死命名（AttributeRegister 常量名含作用域 token）", scope_welded)

# 额外：消费端直读属性中，哪些是"数据先行"注释标注的（无 StatGen 接线）
print("\n--- 悬空直读明细（属性 -> 消费点）---")
for an in orphan_read:
    print(f"  {an}:")
    for loc in reads[an][:6]:
        print("     ", loc)
    if len(reads[an]) > 6:
        print(f"      ...共 {len(reads[an])} 处")
