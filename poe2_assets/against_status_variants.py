#!/usr/bin/env python3
"""Comprehensive but SAFE scan: all damage/crit *prefix* variants that target
'enemies with <existing-status>'. Maps prefix -> existing EFF, status-word -> existing COND.
Excludes non-damage mechanics (chance-to-inflict, convert, duration, buildup, threshold,
culling, magnitude, immune, mark, recently, distance, ailment, buff)."""
import re, subprocess, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIRINGS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat")
CONDKEEP = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatConditionIds.java")

PREFIX_EFF = {
    "INC_DAMAGE_WITH_HITS_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "INC_DAMAGE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "INC_MELEE_DAMAGE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE",
    "INC_ATTACK_DAMAGE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_DAMAGE_DAMAGE",
    "PROJECTILES_DEAL_INC_DAMAGE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE",
    "INC_CRITICAL_HIT_CHANCE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE",
    "INC_ATTACK_CRITICAL_HIT_CHANCE_AGAINST": "EFF_ADD_MULTIPLIED_BASE_ATTACK_ATTRIBUTE_COE_CRIT_CHANCE",
    "TO_CRITICAL_DAMAGE_BONUS_AGAINST": "EFF_ADD_VALUE_ATTRIBUTE_COE_CRIT_DAMAGE",
}
STATUS_COND = {
    "IMMOBILISED": "COND_PIN_STATUS", "PINNED": "COND_PIN_STATUS",
    "HEAVY_STUNNED": "COND_HEAVY_STUN_STATUS", "HEAVY_STUN": "COND_HEAVY_STUN_STATUS",
    "SHOCKED": "COND_SHOCK_STATUS", "ELECTROCUTED": "COND_ELECTROCUTE_STATUS",
    "POISONED": "COND_POISON_STATUS", "BLEEDING": "COND_BLEED_STATUS_AGE",
    "CHILLED": "COND_CHILL_STATUS", "FROZEN": "COND_FREEZE_STATUS",
    "BLINDED": "COND_BLIND_STATUS", "MAIMED": "COND_MAIM_STATUS",
    "SLOWED": "COND_SLOW_STATUS", "HINDERED": "COND_HINDER_STATUS",
    "DAZED": "COND_DAZE_STATUS", "IGNITED": "COND_IGNITE_STATUS",
    "BURNING": "COND_IGNITE_STATUS",
}
EXCLUDE = re.compile(r"(CHANCE|CONVERT|GAIN_OF|GAIN_|BUILDUP|DURATION|THRESHOLD|CULLING|MAGNITUDE|"
                     r"IMMUNE|CANNOT|_MARK_|RECENTLY|FURTHER|WITHIN_|AILMENT|STATUS_|CHARGE|RAGE|"
                     r"SUPPRESS|SCOURGE|WHILE_|IF_YOU|PER_|FOR_EACH|_BUFF|IGNITES|POISONS|BLEEDS|"
                     r"MAIMS|SLOWS|BLINDS|DAZES|SHOCKS|FREEZES|BURNS|ENEMIES_ARE|ENEMIES_YOU|"
                     r"ENEMIES_NEAR|GRANT|SCORCH|PETRIF|IMPALE|WITHER|INTIMIDAT|DEBILITAT|AGGRAVAT|"
                     r"ARMOR_BREAK|ARMOUR_BREAK)")

def parse_statids():
    out = []
    with open(STATIDS, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static final Identifier ([A-Z0-9_]+) = id\(", line)
            if m: out.append(m.group(1))
    return out

def wired_set():
    out = set()
    r = subprocess.run(["grep", "-rhoE", r"StatIds\.[A-Z0-9_]+", WIRINGS], capture_output=True, text=True)
    for tok in r.stdout.split():
        if "." in tok: out.add(tok.split(".", 1)[1])
    return out

def existing_conds():
    out = set()
    with open(CONDKEEP, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static final Identifier (COND_[A-Z0-9_]+) = id\(", line)
            if m: out.add(m.group(1))
    return out

def main():
    all_ids = parse_statids()
    wired = wired_set()
    conds = existing_conds()
    unwired = [n for n in all_ids if n not in wired]
    cands = []
    for n in unwired:
        if EXCLUDE.search(n):
            continue
        for pref, eff in PREFIX_EFF.items():
            if not n.startswith(pref + "_"):
                continue
            tail = n[len(pref)+1:]
            # tail should be <STATUS>_ENEMIES
            m = re.match(r"([A-Z]+)_ENEMIES$", tail)
            if not m:
                continue
            sw = m.group(1)
            if sw in STATUS_COND and STATUS_COND[sw] in conds:
                cands.append((n, STATUS_COND[sw], eff))
            break
    print(f"total={len(all_ids)} wired={len(wired)} unwired={len(unwired)}")
    print(f"\n=== CLEAN prefix-variant damage/crit against existing-status = {len(cands)} ===")
    for n, c, e in sorted(cands):
        print(f"map.put(StatIds.{n}, onTarget(StatConditionIds.{c}, StatEffectIds.{e}));")

if __name__ == "__main__":
    main()
