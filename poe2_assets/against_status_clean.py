#!/usr/bin/env python3
"""Strict scan: only the CLEAN 'increased Damage / Crit Chance against <status> enemies'
mirror of the already-wired BURNING line. Avoids chance-to-inflict / convert / duration /
magnitude false positives. Maps StatId status-word -> EXISTING COND (no new registration)."""
import re, subprocess, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIRINGS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat")
CONDKEEP = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatConditionIds.java")

# status word (in StatId) -> existing COND constant
SW_TO_COND = {
    "IMMOBILISED": "COND_PIN_STATUS",
    "PINNED": "COND_PIN_STATUS",
    "HEAVY_STUNNED": "COND_HEAVY_STUN_STATUS",
    "SHOCKED": "COND_SHOCK_STATUS",
    "ELECTROCUTED": "COND_ELECTROCUTE_STATUS",
    "POISONED": "COND_POISON_STATUS",
    "BLEEDING": "COND_BLEED_STATUS_AGE",
    "CHILLED": "COND_CHILL_STATUS",
    "FROZEN": "COND_FREEZE_STATUS",
    "BLINDED": "COND_BLIND_STATUS",
    "MAIMED": "COND_MAIM_STATUS",
    "SLOWED": "COND_SLOW_STATUS",
    "HINDERED": "COND_HINDER_STATUS",
    "DAZED": "COND_DAZE_STATUS",
    "IGNITED": "COND_IGNITE_STATUS",
    "BURNING": "COND_IGNITE_STATUS",
}

EFF_DMG = "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE"
EFF_CRIT = "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE"

def parse_statids():
    out = []
    with open(STATIDS, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static final Identifier ([A-Z0-9_]+) = id\(", line)
            if m: out.append(m.group(1))
    return out

def wired_set():
    out = set()
    r = subprocess.run(["grep", "-rhoE", r"StatIds\.[A-Z0-9_]+", WIRINGS],
                       capture_output=True, text=True)
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

    # strict patterns
    dmg_pat = re.compile(r"^INC_DAMAGE(_WITH_HITS)?_AGAINST_([A-Z]+)_ENEMIES$")
    crit_pat = re.compile(r"^INC_CRITICAL_HIT_CHANCE_AGAINST_([A-Z]+)_ENEMIES$")

    dmg_cands, crit_cands = [], []
    for n in unwired:
        m = dmg_pat.match(n)
        if m:
            sw = m.group(2)
            if sw in SW_TO_COND and SW_TO_COND[sw] in conds:
                dmg_cands.append((n, SW_TO_COND[sw]))
            continue
        m = crit_pat.match(n)
        if m:
            sw = m.group(1)
            if sw in SW_TO_COND and SW_TO_COND[sw] in conds:
                crit_cands.append((n, SW_TO_COND[sw]))

    print(f"total={len(all_ids)} wired={len(wired)} unwired={len(unwired)}")
    print(f"\n=== CLEAN DAMAGE-AGAINST (onTarget COND, {EFF_DMG}) = {len(dmg_cands)} ===")
    for n, c in sorted(dmg_cands):
        print(f"map.put(StatIds.{n}, onTarget(StatConditionIds.{c}, StatEffectIds.{EFF_DMG}));")
    print(f"\n=== CLEAN CRIT-AGAINST (onTarget COND, {EFF_CRIT}) = {len(crit_cands)} ===")
    for n, c in sorted(crit_cands):
        print(f"map.put(StatIds.{n}, onTarget(StatConditionIds.{c}, StatEffectIds.{EFF_CRIT}));")

if __name__ == "__main__":
    main()
