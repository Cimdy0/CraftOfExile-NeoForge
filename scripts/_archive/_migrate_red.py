import io, sys

p = r"E:/McModding/Modding/CraftOfExile-NeoForge/src/main/java/com/cimdy/coe/data/gen/gem/skill/SkillRedGen.java"
s = open(p, encoding="utf-8").read()

repls = [
    # bear_maul
    ('        reg(bootstrap, "red/bear_maul",\n'
     '                SkillFactory.formAttack(GemGen.strReq(), SkillFactory.mana(0),\n'
     '                        "physical", 18, 2, 4, StatTag.BEAR, 5));',
     '        reg(bootstrap, "red/bear_maul",\n'
     '                SkillPresets.read("generic_shapeshift_physical_rage")\n'
     '                        .require(GemGen.strReq()).cost(SkillFactory.mana(0))\n'
     '                        .vars("dmg", ExpressionCodec.ofSkillLevel(18, 2, 4))\n'
     '                        .withTags(StatTag.BEAR));'),
    # wyvern_rend
    ('        reg(bootstrap, "red/wyvern_rend",\n'
     '                SkillFactory.formAttack(GemGen.strReq(), SkillFactory.mana(0),\n'
     '                        "lightning", 15, 2, 3, StatTag.WYVERN));',
     '        reg(bootstrap, "red/wyvern_rend",\n'
     '                SkillPresets.read("generic_shapeshift_lightning")\n'
     '                        .require(GemGen.strReq()).cost(SkillFactory.mana(0))\n'
     '                        .vars("dmg", ExpressionCodec.ofSkillLevel(15, 2, 3))\n'
     '                        .withTags(StatTag.WYVERN));'),
    # banner
    ('        reg(bootstrap, "red/banner",\n'
     '                SkillFactory.areaSpell(GemGen.strReq(), SkillFactory.mana(10),\n'
     '                        "physical", 1, 0, 0, 2.5,\n'
     '                        new BannerAction(ExpressionCodec.of(4.0), ExpressionCodec.of(30.0), ExpressionCodec.of(1.0))));',
     '        reg(bootstrap, "red/banner",\n'
     '                SkillPresets.read("generic_banner")\n'
     '                        .require(GemGen.strReq()).cost(SkillFactory.mana(10))\n'
     '                        .vars("banner_radius", ExpressionCodec.of(4.0))\n'
     '                        .vars("banner_duration", ExpressionCodec.of(30.0))\n'
     '                        .vars("banner_magnitude", ExpressionCodec.of(1.0)));'),
    # consume_corpse
    ('        reg(bootstrap, "red/consume_corpse",\n'
     '                SkillFactory.consumeCorpse(GemGen.strReq(), SkillFactory.mana(1)));',
     '        reg(bootstrap, "red/consume_corpse",\n'
     '                SkillPresets.read("generic_consume_corpse")\n'
     '                        .require(GemGen.strReq()).cost(SkillFactory.mana(1)));'),
    # shockwave_totem
    ('        reg(bootstrap, "red/shockwave_totem",\n'
     '                SkillFactory.totemSkill(GemGen.strReq(), SkillFactory.mana(14),\n'
     '                        "physical", com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofSkillLevel(8, 1, 2),\n'
     '                        300, 20, 8.0, 30.0));',
     '        SkillPresets.registerPreset(GemGen.id("preset_shockwave_totem"),\n'
     '                SkillFactory.totemSkill(GemGen.strReq(), SkillFactory.mana(14),\n'
     '                        "physical", com.cimdy.coe.data.codec.support.math.ExpressionCodec.ofSkillLevel(8, 1, 2),\n'
     '                        300, 20, 8.0, 30.0));\n'
     '        reg(bootstrap, "red/shockwave_totem",\n'
     '                SkillPresets.read("preset_shockwave_totem"));'),
]

for old, new in repls:
    c = s.count(old)
    if c != 1:
        print("FAIL count=%d for:\n%s" % (c, old))
        sys.exit(1)
    s = s.replace(old, new)

open(p, "w", encoding="utf-8").write(s)
print("OK: applied", len(repls), "replacements")
