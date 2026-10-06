#!/usr/bin/env python3
"""Re-scan unwired StatIds for 'against <status> enemies' damage/crit/taken patterns
that map to ALREADY-EXISTING COND primitives (pure-data mirror of BURNING/CHILLED/FROZEN/BLINDED).

The previous Phase 25 scan used a narrow keyword filter and missed the bridge
StatId-keyword -> engine-status (e.g. IMMOBILISED -> PIN, HEAVY_STUNNED -> HEAVY_STUN).
This scan builds that bridge explicitly and only reports candidates whose COND already exists.
"""
import re, subprocess, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STATIDS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/codec/support/stat/StatIds.java")
WIRINGS = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat")
CONDKEEP = os.path.join(ROOT, "src/main/java/com/cimdy/coe/data/gen/stat/StatConditionIds.java")

# StatId-keyword (upper) -> existing COND constant id
KW_TO_COND = {
    "IMMOBILIS": "COND_PIN_STATUS",
    "IMMOBILIZ": "COND_PIN_STATUS",
    "PINNED": "COND_PIN_STATUS",
    "PIN_": "COND_PIN_STATUS",
    "_PIN": "COND_PIN_STATUS",
    "HEAVY_STUNN": "COND_HEAVY_STUN_STATUS",
    "HEAVY_STUN_": "COND_HEAVY_STUN_STATUS",
    "SHOCKED": "COND_SHOCK_STATUS",
    "_SHOCK_": "COND_SHOCK_STATUS",
    "ELECTROCUT": "COND_ELECTROCUTE_STATUS",
    "POISON": "COND_POISON_STATUS",
    "BLEED": "COND_BLEED_STATUS_AGE",
    "CHILL": "COND_CHILL_STATUS",
    "FROZEN": "COND_FREEZE_STATUS",
    "BLIND": "COND_BLIND_STATUS",
    "MAIM": "COND_MAIM_STATUS",
    "SLOW": "COND_SLOW_STATUS",
    "HINDER": "COND_HINDER_STATUS",
    "DAZ": "COND_DAZE_STATUS",
    "IGNIT": "COND_IGNITE_STATUS",
    "BURNING": "COND_IGNITE_STATUS",
    "SCORCH": "COND_SCORCH_STATUS",  # may not exist -> filtered later
}

# which EFF to use by StatId keyword
def eff_for(name):
    n = name
    if "CRIT" in n and "CHANCE" in n:
        return "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE"
    if "TAKEN" in n or "_HAVE_INC_DAMAGE_TAKEN" in n or "TAKE_INC" in n:
        return "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_TAKEN_DAMAGE"
    # default generic increased Damage
    return "EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE"

def parse_statids():
    names = []
    with open(STATIDS, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static final Identifier ([A-Z0-9_]+) = id\(", line)
            if m:
                names.append(m.group(1))
    return names

def wired_set():
    # grep wirings + StatGen for StatIds.NAME
    out = set()
    r = subprocess.run(["grep", "-rhoE", r"StatIds\.[A-Z0-9_]+", WIRINGS],
                       capture_output=True, text=True)
    for tok in r.stdout.split():
        if "." in tok:
            out.add(tok.split(".", 1)[1])
    return out

def existing_conds():
    conds = set()
    with open(CONDKEEP, encoding="utf-8") as f:
        for line in f:
            m = re.search(r"public static final Identifier (COND_[A-Z0-9_]+) = id\(", line)
            if m:
                conds.add(m.group(1))
    return conds

def main():
    all_ids = parse_statids()
    wired = wired_set()
    conds = existing_conds()
    unwired = [n for n in all_ids if n not in wired]
    print(f"total StatIds={len(all_ids)} wired={len(wired)} unwired={len(unwired)}")
    print(f"existing CONDs={len(conds)}")

    cands = []
    for n in unwired:
        matched_cond = None
        for kw, cond in KW_TO_COND.items():
            if kw in n:
                matched_cond = cond
                break
        if matched_cond is None:
            continue
        if matched_cond not in conds:
            # COND not registered (e.g. SCORCH) -> skip for now
            continue
        # Only consider "against/target/gain" style that maps to onTarget(COND, EFF)
        if not (("AGAINST" in n) or ("ENEMIES" in n and ("TAKE" in n or "HAVE" in n or "DEAL" in n))
                or ("ENEMY" in n) or ("TARGET" in n)):
            # still include damage/crit-against style
            pass
        eff = eff_for(n)
        cands.append((n, matched_cond, eff))

    print(f"\n=== CANDIDATES (unwired, mapping to EXISTING COND) = {len(cands)} ===")
    for n, c, e in sorted(cands):
        print(f"{n}\n    -> onTarget({c}, {e})")

if __name__ == "__main__":
    main()
