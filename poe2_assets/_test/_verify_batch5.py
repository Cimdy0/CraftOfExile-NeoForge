import json, os, sys

D = "src/generated/resources/data/coe/coe/stat_entry"

checks = {
    "increased_elemental_ailments_ailment_application_if_you_have_shapeshifted_to_an_animal_form_recently.json": {"elemental", "shapeshifted"},
    "minions_have_chance_to_inflict_withered_on_hit_damage.json": {"minion", "withered"},
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
print("PASS" if ok else "FAIL")
sys.exit(0 if ok else 1)
