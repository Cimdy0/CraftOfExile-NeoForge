#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Step6-C: 修复 StatGen.java 中 36 个真冲突组（同键双/三注册 + 属性语义错位）。

策略（全部基于引擎消费/语料核对，非猜测）：
  - DEDUP / _N 占位 / 低 rank 重复 -> 删重复语句（按完整语句跨度，含多行）
  - 劫持项 -> 重指向已存在/新建的正确常量
  - 引擎消费属性所在行 -> 保留，删其同名兄弟

先 dry-run（默认），--apply 才落地。落地前自动备份。
"""
import os, sys, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATGEN = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java")

# (orig_line_1based, expected StatIds constant substring) —— 删除前断言，防行号漂移
DEL = [
    (785,  "INC_ARMOUR_ENERGY"),
    (860,  "X_INCREASED_BUFF_EFFECT_OF_ARCANE_SURGE_ON_YOU"),
    (862,  "X_INCREASED_EFFECT_OF_CHILL_ON_YOU"),
    (912,  "DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_RESISTANCES"),
    (915,  "DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_RESISTANCES"),
    (1086, "X_SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL"),
    (1087, "X_CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION"),
    (1088, "X_CHANCE_FOR_ATTACKS_TO_MAIM_ON_HIT_DAMAGE_AGAINST_POISONED_ENEMIES"),
    (1089, "X_CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS"),
    (1091, "X_INCREASED_ELEMENTAL_AILMENTS_AILMENT_APPLICATION_IF_YOU_HAVE_SHAPESHIFTED_TO_AN_ANIMAL_FORM_RECENTLY"),
    (1092, "X_INCREASED_SHOCK_CHANCE_AGAINST_ELECTROCUTED_ENEMIES"),
    (1093, "X_OF_MAX_HEALTH_STAT_CONVERSION_TO_ENERGY_SHIELD"),
    (1100, "DAMAGE_PENETRATION_X_ELEMENTAL_DAMAGE_RESISTANCES"),
    (1106, "IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED"),
    (1110, "RECOVER_X_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED"),
    (1112, "RECOVER_X_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE"),
    (1139, "SKILLS_HAVE_X_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM"),
    (1142, "ATTACK_DAMAGE_PENETRATES_X_OF_ENEMY_ELEMENTAL_DAMAGE_RESISTANCES"),
    (1145, "MINIONS_HAVE_X_CHANCE_TO_INFLICT_WITHERED_ON_HIT_DAMAGE"),
    (1146, "INC_MINIONS_COOLDOWN_RECOVERY_RATE_COMMAND"),
    (1147, "MINIONS_HAVE_X_INCREASED_CRITICAL_HIT_CHANCE"),
    (1148, "PLANTS_HAVE_A_X_CHANCE_TO_IMMEDIATELY_OVERGROW"),
    (1149, "CONVERTS_ALL_EVASION_RATING"),
    (1150, "THORNS_DAMAGE_HAS_X_CHANCE_TO_IGNORE_ENEMY_ARMOR"),
    (1190, "X_INCREASED_ARCHON_BUFF_DURATION"),
    (1287, "INC_DAMAGE_EMPOWERED_ATTACKS"),
    (1420, "INC_INFUSION_DURATION"),
    (1459, "INC_CHARM_DURATION"),
    (1578, "INC_PROJECTILE_SPEED"),
    (1707, "LIFE_PER_KILL"),
    (1774, "X_OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD"),
    (1792, "X_OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD"),
    (877,  "X_SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL"),
    (885,  "X_CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT"),
]

# (orig_line_1based, old_substr, new_substr) —— 重指向（行数不变，先执行）
REP = [
    (1382, "StatIds.INC_DEXTERITY",                       "StatIds.X_INCREASED_INTELLIGENCE"),
    (1659, "StatIds.INC_MAX_VOLATILITY",                  "StatIds.X_INCREASED_VOLATILITY_EXPLOSION_DELAY"),
    (1541, "StatIds.INC_OFFERING_DURATION",               "StatIds.OFFERING_SKILLS_HAVE_X_INCREASED_BUFF_EFFECT"),
    (330,  "StatIds.X_INCREASED_EFFECT_OF_PUPPET_MASTER", "StatIds.X_INCREASED_DURATION_OF_PUPPET_MASTER"),
    (2423, "StatIds.RAGE_ON_HIT",                         "StatIds.RAGE_ON_HIT_TALENT"),
]

def main():
    apply = "--apply" in sys.argv
    lines = open(STATGEN, encoding="utf-8").read().split("\n")
    N = len(lines)

    # --- 重指向（按原行号，行数不变）---
    rep_done = []
    for ln, old, new in REP:
        assert 1 <= ln <= N, "REP line out of range: %d" % ln
        src = lines[ln-1]
        assert old in src, "REP L%d: 未找到 %s\n  内容: %s" % (ln, old, src)
        assert new not in src, "REP L%d: 目标 %s 已存在?" % (ln, new)
        lines[ln-1] = src.replace(old, new, 1)
        rep_done.append((ln, old, new))

    # --- 删除（按原行号降序；降序下所有已删行均位于当前目标下方，不推移其索引，故 cur = ln）---
    del_sorted = sorted(DEL, key=lambda x: x[0], reverse=True)
    del_done = []
    for ln, expect in del_sorted:
        cur = ln
        assert 1 <= cur <= len(lines), "DEL L%d -> cur %d 越界" % (ln, cur)
        stmt = lines[cur-1]
        assert ("StatIds." + expect) in stmt, \
            "DEL L%d(cur=%d): 期望含 StatIds.%s，实际:\n  %s" % (ln, cur, expect, stmt.strip())
        # 向下扩展到语句结束 ';'（含多行语句）
        end = cur
        while end <= len(lines) and not lines[end-1].rstrip().endswith(";"):
            end += 1
        assert end <= len(lines), "DEL L%d(cur=%d): 未找到语句结束 ';'" % (ln, cur)
        span = end - cur + 1
        for k in range(cur-1, end):
            del_done.append((ln, lines[k].strip()))
        del lines[cur-1:end]

    if not apply:
        print("===== DRY-RUN（不写文件）=====")
        print("重指向 %d 行：" % len(rep_done))
        for ln, old, new in rep_done:
            print("  L%-5d %s -> %s" % (ln, old, new))
        print("删除 %d 条语句（含多行展开，按原行号降序）：" % len(del_done))
        for ln, txt in del_done:
            print("  (原L%d) %s" % (ln, txt[:88]))
        print("\n确认无误后加 --apply 落地。")
        return

    bak = STATGEN + ".step6bak"
    if not os.path.exists(bak):
        shutil.copy(STATGEN, bak)
    open(STATGEN, "w", encoding="utf-8").write("\n".join(lines))
    print("已落地：重指向 %d 行 / 删除 %d 条语句（%d 物理行）；备份 -> %s"
          % (len(rep_done), len(del_sorted), len(del_done), bak))

if __name__ == "__main__":
    main()
