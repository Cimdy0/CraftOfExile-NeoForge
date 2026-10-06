# -*- coding: utf-8 -*-
"""
Step6-C: 为「被劫持条目」探测正确的重指向目标常量。

对每个候选常量名/关键词，报告三项事实：
  1) StatIds 是否已定义该常量（含其语料注释）；
  2) StatGen 是否已注册（map 键存在 → 重指向会撞键，须先确认是否同语义）；
  3) 除 StatGen/StatIds 外还有谁引用（节点/词缀/技能 → 重指向会影响这些消费方）。

用法：python poe2_assets/step6_probe_targets.py [关键词...]
不带参数时跑内置的 14 组 MANUAL 探测清单。
"""
import os
import re
import sys

STATGEN = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
STATIDS = "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java"
SRC_ROOT = "src/main/java/com/cimdy/coe"

DEFAULT_QUERIES = [
    # LIFE_PER_KILL：定额 vs 百分比 需要各自的宿主常量
    "ON_KILL", "PER_ENEMY_KILLED", "RECOVER_X_HEALTH",
    # 元素承伤返还：ES / Health / Mana 三资源
    "RECOUPED_AS",
    # RAGE_ON_HIT：熊形常量 vs 天赋 $value
    "RAGE_ON_MELEE", "RAGE",
    # INC_DEXTERITY 被劫持为 INTELLIGENCE
    "INC_INTELLIGENCE", "INC_STRENGTH", "INC_DEXTERITY", "INCREASED_ATTRIBUTES",
    # 波动爆炸延迟
    "VOLATILITY",
    # 供品增益幅度
    "OFFERING",
    # 傀儡师：effect / duration / chance 三属性
    "PUPPET_MASTER",
    # 元素灌入层数上限
    "ELEMENTAL_INFUSION", "MAX_NUMBER_OF",
    # 护甲/闪避/能量护盾
    "ARMOUR_ENERGY", "INC_MAX_ENERGY", "INC_EVASION",
    # 投射物速度
    "PROJECTILE_SPEED",
    # 残迹
    "REMNANT",
    # 魅饰 vs 随从
    "CHARM_DURATION", "MINION_DURATION",
    # 强化攻击
    "EMPOWERED",
    # Archon
    "ARCHON",
]


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def statids_map():
    """常量名 -> 语料注释。"""
    out = {}
    pending = None
    for line in read(STATIDS).splitlines():
        s = line.strip()
        if s.startswith("//"):
            pending = s[2:].strip()
            continue
        m = re.search(r"public static final Identifier\s+([A-Z][A-Z0-9_]*)\s*=", s)
        if m:
            out[m.group(1)] = pending or ""
        pending = None
    return out


def statgen_registered():
    """StatGen 中被引用（= 已注册/派生）的常量名 -> 出现行号列表。"""
    src = read(STATGEN).splitlines()
    out = {}
    for i, l in enumerate(src, 1):
        for nm in re.findall(r"StatIds\.([A-Z][A-Z0-9_]*)", l):
            out.setdefault(nm, []).append(i)
    return out


def external_refs():
    """StatGen/StatIds 之外的引用：常量名 -> [文件:行]。"""
    out = {}
    skip = {os.path.normpath(STATGEN), os.path.normpath(STATIDS)}
    for root, _d, files in os.walk(SRC_ROOT):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(root, fn)
            if os.path.normpath(p) in skip:
                continue
            for i, l in enumerate(read(p).splitlines(), 1):
                for nm in re.findall(r"StatIds\.([A-Z][A-Z0-9_]*)", l):
                    out.setdefault(nm, []).append("%s:%d" % (fn, i))
    return out


def main():
    queries = sys.argv[1:] or DEFAULT_QUERIES
    ids = statids_map()
    reg = statgen_registered()
    ext = external_refs()

    for q in queries:
        hits = sorted(k for k in ids if q in k)
        print("\n=== 关键词 %s → %d 个常量 ===" % (q, len(hits)))
        for k in hits[:24]:
            lines = reg.get(k, [])
            e = ext.get(k, [])
            print("  %-78s reg=%s ext=%d%s" % (
                k[:78],
                ",".join("L%d" % x for x in lines) or "未注册",
                len(e),
                (" [" + ",".join(e[:3]) + "]") if e else ""))
            if ids[k]:
                print("      // %s" % ids[k][:150])
        if len(hits) > 24:
            print("  ...（另 %d 个省略）" % (len(hits) - 24))


if __name__ == "__main__":
    main()
