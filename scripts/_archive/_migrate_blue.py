import io, sys

p = r"E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillBlueGen.java"
s = open(p, encoding="utf-8").read()

repls = [
    # raise_zombie -> generic_summon (defaults count=2/duration=600 match original; explicit for clarity)
    ('        reg(bootstrap, "blue/raise_zombie",\n'
     '                SkillFactory.summonSkill(GemGen.intReq(), SkillFactory.mana(18),\n'
     '                        "minecraft:zombie", 2, 600).withTags("command", "corpse"));',
     '        reg(bootstrap, "blue/raise_zombie",\n'
     '                SkillPresets.read("generic_summon")\n'
     '                        .require(GemGen.intReq()).cost(SkillFactory.mana(18))\n'
     '                        .vars("summon_count", ExpressionCodec.of(2.0))\n'
     '                        .vars("summon_duration", ExpressionCodec.of(600.0))\n'
     '                        .withTags("command", "corpse"));'),
    # elemental_weakness -> generic_curse (curse range 24 / duration 100 match original)
    ('        reg(bootstrap, "blue/elemental_weakness",\n'
     '                SkillFactory.curseSkill(GemGen.intReq(), SkillFactory.mana(14), 24.0, 100));',
     '        reg(bootstrap, "blue/elemental_weakness",\n'
     '                SkillPresets.read("generic_curse")\n'
     '                        .require(GemGen.intReq()).cost(SkillFactory.mana(14))\n'
     '                        .vars("curse_range", ExpressionCodec.of(24.0))\n'
     '                        .vars("curse_duration", ExpressionCodec.of(100.0)));'),
]

for old, new in repls:
    c = s.count(old)
    if c != 1:
        print("FAIL count=%d for:\n%s" % (c, old))
        sys.exit(1)
    s = s.replace(old, new)

open(p, "w", encoding="utf-8").write(s)
print("OK: applied", len(repls), "replacements")
