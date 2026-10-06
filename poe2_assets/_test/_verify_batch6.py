import json, os, sys

D = "src/generated/resources/data/coe/coe/stat_entry"

checks = {
    # fn -> (expected attribute register string, expected scope tags)
    "to_critical_hit_chance_of_herald_skills.json": ("crit_chance", {"herald"}),
    "increased_parry_range.json": ("parry_range", {"parry"}),
    "increased_stun_recovery.json": ("stun_recovery", {"stun"}),
    "to_stun_threshold_per_strength.json": ("stun_threshold_strength", {"stun"}),
    "blind_enemies_when_they_stun_you.json": ("blind_they_stun", {"blind", "stun"}),
    "chance_for_attacks_to_maim_on_hit_damage_against_poisoned_enemies.json": ("attacks_maim_poisoned", {"attack", "maim"}),
}
# stale: deleted critical_herald id must not yield a file named after its id
stale_name = "critical_herald.json"
ok = True
for fn, (exp_attr, expect) in checks.items():
    p = os.path.join(D, fn)
    if not os.path.exists(p):
        print(f"[MISSING] {fn}"); ok = False; continue
    o = json.load(open(p, encoding="utf-8"))
    attr = (o.get("attribute") or "").replace("coe:", "")
    tags = set(o.get("tags", []))
    miss = expect - tags
    if attr != exp_attr or miss:
        print(f"[BAD] {fn}: attribute={attr!r} (exp {exp_attr!r}) tags={sorted(tags)} missing={sorted(miss)}"); ok = False
    else:
        print(f"[OK] {fn}: attribute={attr} tags={sorted(tags)}")
sp = os.path.join(D, stale_name)
if os.path.exists(sp):
    print(f"[STALE] {stale_name} still exists (deleted attr residue)"); ok = False
else:
    print(f"[OK] no stale {stale_name}")
print("PASS" if ok else "FAIL")
sys.exit(0 if ok else 1)
