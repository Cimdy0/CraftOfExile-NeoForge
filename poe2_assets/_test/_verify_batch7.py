import json, os

D = "src/generated/resources/data/coe/coe/stat_entry"
checks = {
    "add_energy_recharge.json": {"energy"},
    "metres_to_dodge_roll_distance_while_surrounded.json": {"surrounded"},
    "increased_mana_cost_efficiency.json": {"cost"},
    "increased_deflection_rating_while_surrounded.json": {"surrounded"},
    "increased_damage_if_you_have_consumed_a_corpse_recently.json": {"corpse"},
    "add_corpse_retain_chance.json": {"corpse"},
    "inc_meta_energy.json": {"meta"},
    "inc_reservation_efficiency.json": {"reservation"},
}
ok = True
for fn, expect in checks.items():
    p = os.path.join(D, fn)
    if not os.path.exists(p):
        print(f"[MISSING] {fn}"); ok = False; continue
    o = json.load(open(p, encoding="utf-8"))
    tags = set(o.get("tags", []))
    miss = expect - tags
    if miss:
        print(f"[BAD] {fn}: tags={sorted(tags)} missing={sorted(miss)}"); ok = False
    else:
        print(f"[OK] {fn}: tags={sorted(tags)}")
print("RESULT:", "PASS" if ok else "FAIL")
