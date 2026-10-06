import io, sys

p = r"E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillGreenGen.java"
s = open(p, encoding="utf-8").read()

# add ExpressionCodec import if not present (needed for ofSkillLevel on bloodhound_s_mark)
if "import com.cimdy.coe.data.codec.support.math.ExpressionCodec;" not in s:
    s = s.replace(
        "import static com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofLevelTable;",
        "import com.cimdy.coe.data.codec.support.math.ExpressionCodec;\nimport static com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofLevelTable;",
        1)

repls = [
    # wolf_rake -> generic_shapeshift_physical (default dmg 12/2/3 matches original)
    ('        reg(bootstrap, "green/wolf_rake",\n'
     '                SkillFactory.formAttack(GemGen.dexReq(), SkillFactory.mana(0),\n'
     '                        "physical", 12, 2, 3, StatTag.WOLF));',
     '        reg(bootstrap, "green/wolf_rake",\n'
     '                SkillPresets.read("generic_shapeshift_physical")\n'
     '                        .require(GemGen.dexReq()).cost(SkillFactory.mana(0))\n'
     '                        .withTags(StatTag.WOLF));'),
    # bloodhound_s_mark -> generic_mark (preserve skill-level damage scaling 15.4/1/3.8)
    ('        reg(bootstrap, "green/bloodhound_s_mark",\n'
     '                SkillFactory.areaSpell(GemGen.dexReq(), SkillFactory.mana(9), "physical", 15.4, 1, 3.8, 2.5)\n'
     '                        .withTags(StatTag.MARK));',
     '        reg(bootstrap, "green/bloodhound_s_mark",\n'
     '                SkillPresets.read("generic_mark")\n'
     '                        .require(GemGen.dexReq()).cost(SkillFactory.mana(9))\n'
     '                        .vars("mark_damage", ExpressionCodec.ofSkillLevel(15.4, 1, 3.8)));'),
]

for old, new in repls:
    c = s.count(old)
    if c != 1:
        print("FAIL count=%d for:\n%s" % (c, old))
        sys.exit(1)
    s = s.replace(old, new)

open(p, "w", encoding="utf-8").write(s)
print("OK: applied", len(repls), "replacements")
