import os

BASE = "src/main/java/com/cimdy/coe/data/gen/stat"
SGC = "StatGenConsts."

# 各文件专属替换（old, new）
PER_FILE = {
    "CoreStatEntries.java": [
        ('Set.of("command", "puppet_master"), AttributeRegister.PUPPET_MASTER_DURATION, 400.0)',
         'Set.of(' + SGC + 'TAG_COMMAND, ' + SGC + 'TAG_PUPPET_MASTER), AttributeRegister.PUPPET_MASTER_DURATION, ' + SGC + 'PUPPET_MASTER_DURATION)'),
        ('StatGenHelpers.add(map, StatIds.INC_EFFECT_OF_PUPPET_MASTER, AttributeRegister.PUPPET_MASTER_EFFECT, "puppet_master");',
         'StatGenHelpers.add(map, StatIds.INC_EFFECT_OF_PUPPET_MASTER, AttributeRegister.PUPPET_MASTER_EFFECT, ' + SGC + 'TAG_PUPPET_MASTER);'),
        ('StatGenHelpers.add(map, StatIds.INC_DURATION_OF_PUPPET_MASTER, AttributeRegister.PUPPET_MASTER_DURATION, "puppet_master");',
         'StatGenHelpers.add(map, StatIds.INC_DURATION_OF_PUPPET_MASTER, AttributeRegister.PUPPET_MASTER_DURATION, ' + SGC + 'TAG_PUPPET_MASTER);'),
        ('0.0, Set.of(StatTag.ELEMENTAL),', SGC + 'ZERO, Set.of(StatTag.ELEMENTAL),'),
    ],
    "DamageStatEntries.java": [
        (', 0.0, Set.of', ', ' + SGC + 'ZERO, Set.of'),
        ('chargeEntry(ChargeType.FRENZY, 0.0,', 'chargeEntry(ChargeType.FRENZY, ' + SGC + 'ZERO,'),
        ('allyGrantEntry(0.0, Set.of', 'allyGrantEntry(' + SGC + 'ZERO, Set.of'),
        (', 0.0)', ', ' + SGC + 'ZERO)'),
        ('LeechStatEffect.entry(1.0,', 'LeechStatEffect.entry(' + SGC + 'FULL,'),
        ('MathParser.parse("$value*20")', 'MathParser.parse("$value*" + ' + SGC + 'MOVEMENT_STATIONARY_WINDOW)'),
    ],
    "RecoveryStatEntries.java": [
        (', 0.0, Set.of', ', ' + SGC + 'ZERO, Set.of'),
        ('comboEntry("default", 0.0,', 'comboEntry(' + SGC + 'COMBO_DEFAULT, ' + SGC + 'ZERO,'),
        ('ExpressionCodec.of(1.0)', 'ExpressionCodec.of(' + SGC + 'FULL)'),
        ('ExpressionCodec.of(0.0)', 'ExpressionCodec.of(' + SGC + 'ZERO)'),
        ('100.0', SGC + 'CONVERT_FULL'),
    ],
    "StatGenHelpers.java": [
        (', 0.0, Set.of', ', ' + SGC + 'ZERO, Set.of'),
    ],
    "StatGen.java": [
        ('0.0', SGC + 'ZERO'),
    ],
}

# 全域替换（应用到全部 5 个文件；不含 StatGenConsts 本身）
GLOBAL = [
    ('new LivingStateValue("surrounded", 0.0)', SGC + 'LIVING_SURROUNDED_NOW'),
    ('new LivingStateValue("shapeshifted", 80.0)', SGC + 'LIVING_SHAPESHIFTED'),
    ('new LivingStateValue("low_life", 80.0)', SGC + 'LIVING_LOW_LIFE'),
    ('new LivingStateValue("blocking", 80.0)', SGC + 'LIVING_BLOCKING'),
    ('new LivingStateValue("parrying", 80.0)', SGC + 'LIVING_PARRYING'),
    ('new LivingStateValue("surrounded", 80.0)', SGC + 'LIVING_SURROUNDED'),
    ('new LivingStateValue("recently_took_dot", 80.0)', SGC + 'LIVING_RECENTLY_TOOK_DOT'),
    ('new LivingStateValue("stationary", 80.0)', SGC + 'LIVING_STATIONARY'),
    ('new LivingStateValue("archon", 80.0)', SGC + 'LIVING_ARCHON'),
    ('new LivingStateValue("!full_life", 80.0)', SGC + 'LIVING_NOT_FULL_LIFE'),
    ('new EventHistoryValue(EventName.KILL, "recently", 80.0)', SGC + 'EVENTHIST_KILL_RECENTLY'),
    ('new EventHistoryValue(EventName.USE_ITEM, "recently", 80.0)', SGC + 'EVENTHIST_USE_ITEM_RECENTLY'),
    ('new EventHistoryValue(EventName.TAKEN_HIT, "recently", 80.0)', SGC + 'EVENTHIST_TAKEN_HIT_RECENTLY'),
    ('new EventHistoryValue(EventName.USE_TOTEM, "recently", 80.0)', SGC + 'EVENTHIST_USE_TOTEM_RECENTLY'),
    ('new EventHistoryValue("taken_stun", "recently", 80.0)', SGC + 'EVENTHIST_TAKEN_STUN_RECENTLY'),
    ('new StatusAgeValue("bleed", 80.0)', SGC + 'STATUS_AGE_BLEED'),
    ('new MovementValue("stationary", 20.0)', SGC + 'MOVEMENT_STATIONARY'),
    ('"gain_frenzy"', SGC + 'TAG_GAIN_FRENZY'),
    ('"gain_endurance"', SGC + 'TAG_GAIN_ENDURANCE'),
    ('"gain_power"', SGC + 'TAG_GAIN_POWER'),
    ('"gain_charge"', SGC + 'TAG_GAIN_CHARGE'),
    ('"volatility_detonated"', SGC + 'TAG_VOLATILITY_DETONATED'),
    ('"command"', SGC + 'TAG_COMMAND'),
    ('"puppet_master"', SGC + 'TAG_PUPPET_MASTER'),
]

TARGETS = ["CoreStatEntries.java", "DamageStatEntries.java", "RecoveryStatEntries.java",
           "StatGenHelpers.java", "StatGen.java"]

for fn in TARGETS:
    path = os.path.join(BASE, fn)
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    orig = text
    # 专属替换
    for old, new in PER_FILE.get(fn, []):
        cnt = text.count(old)
        if cnt == 0:
            print(f"[WARN] {fn}: 专属未命中 -> {old!r}")
        text = text.replace(old, new)
    # 全域替换
    for old, new in GLOBAL:
        cnt = text.count(old)
        if cnt:
            text = text.replace(old, new)
    if text != orig:
        with open(path, "w", encoding="utf-8") as f:
            f.write(text)
        print(f"[OK] {fn}: 已写回 ({len(orig)-len(text)} 字节变化)")
    else:
        print(f"[SKIP] {fn}: 无变化")
print("DONE")
