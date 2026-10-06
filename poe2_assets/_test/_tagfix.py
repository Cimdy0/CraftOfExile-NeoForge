import re, io, sys
p = "src/main/java/com/cimdy/coe/data/gen/stat/StatGen.java"
s = io.open(p, encoding="utf-8").read()
orig = s

pairs = [
 # vsExposed 系列：补元素 tag
 ("StatIds.INC_AILMENT_DURATION_VS_EXPOSED_FIRE, DebuffStatusTypes.EXPOSURE_FIRE.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_DURATION_VS_EXPOSED_FIRE, DebuffStatusTypes.EXPOSURE_FIRE.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT, StatTag.FIRE)"),
 ("StatIds.INC_AILMENT_DURATION_VS_EXPOSED_COLD, DebuffStatusTypes.EXPOSURE_COLD.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_DURATION_VS_EXPOSED_COLD, DebuffStatusTypes.EXPOSURE_COLD.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT, StatTag.COLD)"),
 ("StatIds.INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING, DebuffStatusTypes.EXPOSURE_LIGHTNING.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_DURATION_VS_EXPOSED_LIGHTNING, DebuffStatusTypes.EXPOSURE_LIGHTNING.id, AttributeRegister.AILMENT_DURATION, StatTag.AILMENT, StatTag.LIGHTNING)"),
 ("StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_FIRE, DebuffStatusTypes.EXPOSURE_FIRE.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_FIRE, DebuffStatusTypes.EXPOSURE_FIRE.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT, StatTag.FIRE)"),
 ("StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_COLD, DebuffStatusTypes.EXPOSURE_COLD.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_COLD, DebuffStatusTypes.EXPOSURE_COLD.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT, StatTag.COLD)"),
 ("StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING, DebuffStatusTypes.EXPOSURE_LIGHTNING.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT)",
  "StatIds.INC_AILMENT_CHANCE_VS_EXPOSED_LIGHTNING, DebuffStatusTypes.EXPOSURE_LIGHTNING.id, AttributeRegister.AILMENT_CHANCE, AttributeModifier.Operation.ADD_VALUE, StatTag.AILMENT, StatTag.LIGHTNING)"),
 # attack area damage：补 attack
 ("StatIds.INCREASED_ATTACK_AREA_DAMAGE, AttributeRegister.DAMAGE, StatTag.DAMAGE, StatTag.AREA)",
  "StatIds.INCREASED_ATTACK_AREA_DAMAGE, AttributeRegister.DAMAGE, StatTag.DAMAGE, StatTag.ATTACK, StatTag.AREA)"),
 # banner 三条：补 banner
 ("StatIds.BANNER_ATTACK_SPEED, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)",
  "StatIds.BANNER_ATTACK_SPEED, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, StatTag.BANNER, StatTag.ATTACK)"),
 ("StatIds.BANNER_CAST_SPEED, AttributeRegister.CAST_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)",
  "StatIds.BANNER_CAST_SPEED, AttributeRegister.CAST_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, StatTag.BANNER)"),
 ("StatIds.BANNER_MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)",
  "StatIds.BANNER_MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, StatTag.BANNER)"),
]
ok = 0
for a, b in pairs:
    n = s.count(a)
    if n != 1:
        print("!! 命中 %d 次，跳过: %s" % (n, a[:70])); continue
    s = s.replace(a, b); ok += 1

io.open(p, "w", encoding="utf-8", newline="").write(s)
print("补 tag 成功 %d / %d" % (ok, len(pairs)))
