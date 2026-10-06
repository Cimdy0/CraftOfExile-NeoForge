#!/usr/bin/env python3
# 权威全量未接 StatId 清单 + 状态型安全镜像候选筛查
# 方法：parse StatIds.java 全部常量名 → 减去 wirings/StatGen 中全部 StatIds.<NAME> 引用（逐条 grep 核验，避免 comm 误判）
import os, re, subprocess

ROOT = "src/main/java/com/cimdy/coe"
SID = os.path.join(ROOT, "data/codec/support/stat/StatIds.java")
WIRING_DIR = os.path.join(ROOT, "data/gen/stat")

# 1) 全部 StatId 常量名
with open(SID, encoding="utf-8") as f:
    txt = f.read()
names = set(re.findall(r'public static final Identifier (\w+) = id\(', txt))

# 2) 全部已接线引用（wirings + StatGen）
wired = set()
for dp, _, fns in os.walk(WIRING_DIR):
    for fn in fns:
        if fn.endswith(".java"):
            p = os.path.join(dp, fn)
            with open(p, encoding="utf-8") as f:
                for m in re.finditer(r'StatIds\.(\w+)', f.read()):
                    wired.add(m.group(1))

unwired = sorted(names - wired)
print(f"[stat] total StatIds={len(names)}  wired={len(wired & names)}  unwired={len(unwired)}")

# 3) 状态关键词 → 既有 COND（仅列已注册的存在项）
STATUS_MAP = {
    "CHILLED": "COND_CHILL_STATUS",
    "FROZEN": "COND_FREEZE_STATUS",
    "SHOCKED": "COND_SHOCK_STATUS",
    "SHOCK": "COND_SHOCK_STATUS",
    "IGNITED": "COND_IGNITE_STATUS",
    "BURNING": "COND_IGNITE_STATUS",
    "BLINDED": "COND_BLIND_STATUS",
    "HINDERED": "COND_HINDER_STATUS",
    "MAIMED": "COND_MAIM_STATUS",
    "MAIM": "COND_MAIM_STATUS",
    "SLOWED": "COND_SLOW_STATUS",
    "SLOW": "COND_SLOW_STATUS",
    "POISONED": "COND_POISON_STATUS",
    "POISON": "COND_POISON_STATUS",
    "DAZED": "COND_DAZE_STATUS",
    "STUNNED": "COND_HEAVY_STUN_STATUS",
    "STUN": "COND_HEAVY_STUN_STATUS",
    "BLEEDING": "COND_BLEED_STATUS_AGE",
    "ELECTROCUTED": "COND_ELECTROCUTE_STATUS",
    "CHILLED": "COND_CHILL_STATUS",
    "MARKED": "COND_PRESENCE_MARK_STATUS",
    "RARE_OR_UNIQUE": "COND_PRESENCE_RARITY_RARE_UNIQUE",
    "RARE_AND_UNIQUE": "COND_PRESENCE_RARITY_RARE_UNIQUE",
    "LOW_HEALTH": "COND_LIVING_STATE_LOW_LIFE",
    "ON_LOW_HEALTH": "COND_LIVING_STATE_LOW_LIFE",
    "FULL_HEALTH": "COND_FULL_LIFE_LIVING_STATE",
    "ON_FULL_HEALTH": "COND_FULL_LIFE_LIVING_STATE",
    "DUAL_WIELD": "COND_DUAL_WIELD_LIVING_STATE",
    "MOVING": "COND_MOVING_LIVING_STATE",
    "STATIONARY": "COND_MOVEMENT_STATIONARY",
    "LEECHING": "COND_LEECHING_LIVING_STATE",
    "CHANNELLING": "COND_CHANNELLING_LIVING_STATE",
    "SHAPESHIFTED": "COND_LIVING_STATE_SHAPESHIFTED",
    "SURROUNDED": "COND_LIVING_STATE_SURROUNDED",
    "PETRIFIED": "COND_PETRIFY_STATUS",
    "TAUNT": "COND_TAUNT_STATUS",
    "INTIMIDATED": "COND_INTIMIDATE_STATUS",
    "IMPaled": "COND_IMPALE_STATUS",
    "IMPALED": "COND_IMPALE_STATUS",
    "BLIND": "COND_BLIND_STATUS",
}

# 4) 已注册的 COND 集合（从 StatConditionIds.java）
cond_ids = set(re.findall(r'public static final Identifier (COND_\w+) = id\(',
               open(os.path.join(ROOT, "data/gen/stat/StatConditionIds.java"), encoding="utf-8").read()))

# 5) 扫描候选：含状态关键词且对应 COND 已注册
cands = []
for n in unwired:
    for kw, cond in STATUS_MAP.items():
        if kw in n and cond in cond_ids:
            cands.append((n, kw, cond))
            break

print(f"[cand] 状态型安全候选（COND 已注册）: {len(cands)}")
for n, kw, cond in cands:
    print(f"  {n}  ~  {cond}")

# 6) 写未接全量清单
with open("poe2_assets/unwired_all.txt", "w", encoding="utf-8") as f:
    f.write("\n".join(unwired))
print(f"[done] unwired_all.txt 写入 {len(unwired)} 条")
