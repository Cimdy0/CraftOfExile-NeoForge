package com.cimdy.coe.data.gen.stat.wirings.wire.flow;

import com.cimdy.coe.data.gen.stat.wirings.StatBuilder;

import com.cimdy.coe.data.codec.support.stat.StatIds;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.PresenceCondition;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

import com.cimdy.coe.data.gen.stat.StatConditionIds;

import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;

import com.cimdy.coe.data.gen.stat.StatTargetIds;

import net.minecraft.resources.Identifier;

import java.util.List;

import java.util.Map;

import static com.cimdy.coe.data.gen.stat.wirings.WiringDsl.*;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/** WireMisc 域接线（355 条）。 */

public final class WireMisc {

    private WireMisc() {

    }

    public static void build(Map<Identifier, StatEntry> map) {

        map.put(StatIds.INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        map.put(StatIds.TO_ACCURACY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        // === C3 registry-driven 安全接线：全量 StatEffectGen 已注册 effect 唯一匹配，零新增 effect 注册 ===

        // （v3 生成器：对未接线 SIMPLE 模板，(属性短语,op) 在 545 条已注册 effect 中唯一命中即接线）

        map.put(StatIds.TO_STRENGTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH));

        map.put(StatIds.TO_DEXTERITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY));

        map.put(StatIds.TO_INTELLIGENCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        map.put(StatIds.TO_MAX_ENDURANCE_CHARGES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENDURANCE_CHARGES_ENDURANCE));

        map.put(StatIds.TO_MAX_POWER_CHARGES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_POWER_CHARGES_POWER));

        map.put(StatIds.TO_MAX_FRENZY_CHARGES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_FRENZY_CHARGES_FRENZY));

        // 「+X to Level of all Chaos/Cold Skills」——落点 SKILL_LEVEL_BONUS_CHAOS/COLD 平加（ADD_VALUE），

        // 经技能 stat 标签（chaos/cold）门控，仅对同元素技能在施放作用域内叠加至技能等级（R3 非死线）。

        map.put(StatIds.TO_LEVEL_OF_ALL_CHAOS_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SKILL_LEVEL_BONUS_CHAOS));

        map.put(StatIds.TO_LEVEL_OF_ALL_COLD_SKILLS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SKILL_LEVEL_BONUS_COLD));

        // 元素灌入重做（#26）：火/冰/雷各为独立可叠层 Status，每元素上限复用 MAX_INFUSION（默认 1）。

        // 「+X to maximum Fire/Cold/Lightning Infusions」三条均落点 MAX_INFUSION 平加（ADD_VALUE），

        // 因 per-element 上限共用同一属性（prefer reusing MAX_INFUSION，R3 安全：三条语义等价、不造重复原语）。

        map.put(StatIds.TO_MAX_FIRE_INFUSIONS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_INFUSION));

        map.put(StatIds.TO_MAX_COLD_INFUSIONS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_INFUSION));

        map.put(StatIds.TO_MAX_LIGHTNING_INFUSIONS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_INFUSION));

        // 「+X to maximum number of Elemental Infusions」——落点 MAX_TOTAL_ELEMENTAL_INFUSIONS 平加（ADD_VALUE），

        // 作为火/冰/雷/维金之和的共享总闸门（InfusionHelper 写入口钳制）。

        map.put(StatIds.TO_MAX_NUMBER_OF_ELEMENTAL_INFUSIONS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_TOTAL_ELEMENTAL_INFUSIONS));

        // 元素灌入「授予」抽离为数据层 StatusStatEffect（Aspect.STACKS）：残片外的技能等来源经

        // GAIN_ELEMENTAL_INFUSION 条目授予元素灌入（泛型词条无元素上下文，落点固定为 FIRE 池；

        // 若需按技能元素授予，应在数据侧改用具体元素残迹/技能词条，本接线保持功能可用）。

        map.put(StatIds.GAIN_ELEMENTAL_INFUSION, self(StatEffectIds.EFF_STATUS_INFUSION_FIRE_STACKS));

        map.put(StatIds.ACCURACY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        // ACCURACY_IS_DOUBLED → 命中值翻倍（「doubled」= 总增 100%，ADD_MULTIPLIED_BASE +1.0 ⇒ total × (1+1.0) = ×2）。

        // 语料无 $value 占位（该条目无数值参数），故按「完全破甲致残 / 禁再生」同款 flag 范式以固定 amount 承载常量语义，非近似。

        map.put(StatIds.ACCURACY_IS_DOUBLED, StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY).self().amount("1.0").build());

        map.put(StatIds.CANNOT_USE_CHARMS, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CANNOT_USE_CHARMS).self().amount("1.0").build());

        // 无法翻滚或冲刺：零基标记属性，条目固定 amount=1.0；读点在 DodgeRollHelper.tryDodge 翻滚发起闸门

        map.put(StatIds.CANNOT_DODGE_ROLL_OR_SPRINT, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CANNOT_DODGE_ROLL_OR_SPRINT).self().amount("1.0").build());

        // 翻滚不可免伤：零基标记属性，条目固定 amount=1.0；读点在 DodgeRollHelper.onHit 翻滚前段免伤窗口闸门

        map.put(StatIds.DODGE_ROLL_CANNOT_AVOID_DAMAGE, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DODGE_ROLL_CANNOT_AVOID_DAMAGE).self().amount("1.0").build());

        // 怒气无效：零基标记属性，条目固定 amount=1.0；读点在 ResourceEventHub.onHeal 怒气获取闸门（RAGE 资源整笔作废 → 怒气恒 0 → 其加成自然失效）

        map.put(StatIds.NO_RAGE_EFFECT, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_NO_RAGE_EFFECT).self().amount("1.0").build());

        // 除生命偷取外不得恢复生命：复用 ResourceEventHub.isRecoveryBlocked 集中门控（regen/flask/recoup/stat 来源均拦，leech 放行）。

        map.put(StatIds.CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CANNOT_RECOVER_HEALTH_OTHER_THAN_FROM_HEALTH_LEECH).self().amount("1.0").build());

        // 偏斜命中不可对承受方施加流血：复用 AilmentHelper.applyBleedingAilment 真读闸门（deflected 标志 ≥1.0 → 跳过流血），R3 安全，非死线。

        map.put(StatIds.DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_BLEEDING_ON_YOU, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEFLECTED_CANNOT_INFLICT_BLEEDING).self().amount("1.0").build());

        map.put(StatIds.DEFLECTED_HIT_DAMAGE_CANNOT_INFLICT_MAIM_ON_YOU, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEFLECTED_CANNOT_INFLICT_MAIM).self().amount("1.0").build());

        // 光环不影响盟友：零基标记属性，条目固定 amount=1.0；读点在 PersistentDeliveryManager.computeTargets 盟友分支闸门

        map.put(StatIds.YOUR_AURA_BUFFS_DO_NOT_AFFECT_ALLIES, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AURA_BUFFS_DO_NOT_AFFECT_ALLIES).self().amount("1.0").build());

        // 眩晕你者致盲：零基标记属性，条目固定 amount=1.0；读点 DamagePipeline.stageHealth（被眩晕瞬间对眩晕者施加 BLIND）。

        map.put(StatIds.BLIND_ENEMIES_WHEN_THEY_STUN_YOU, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BLIND_ENEMIES_WHEN_THEY_STUN_YOU).self().amount("1.0").when(StatConditionIds.COND_STUN_STATUS).build());

        // 命中致死免死几率：零基属性，数值直接来自词缀（amount 由 StatGen 提供，故不写死）；读点 DamagePipeline.stageHealth 死亡规避掷骰。

        map.put(StatIds.CHANCE_TO_AVOID_DEATH_FROM_HIT_DAMAGE, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHANCE_TO_AVOID_DEATH).self().build());

        // 秘能波动改生命回复：零基标记属性，条目固定 amount=1.0；读点 StatusDefs.ARCANE_SURGE（flag≥1 改授 INC_HEALTH_REGENERATION_RATE 代替 ARCANE_SURGE_REGEN）。

        map.put(StatIds.ARCANE_SURGE_GRANTS_MORE_HEALTH_REGENERATION_RATE_INSTEAD_OF_MANA_REGENERATION_RATE, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ARCANE_SURGE_GRANTS_HEALTH_REGEN).self().amount("1.0").build());

        // 变回人形获秘能波动：FORM_CLEARED 事件 → 授予 ARCANE_SURGE 状态（复用既有 StatusDefs.ARCANE_SURGE，R3 安全）。

        map.put(StatIds.GAIN_ARCANE_SURGE_WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_AFTER_BEING_SHAPESHIFTED_FOR_AT_LEAST_SECONDS, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_FORM_CLEARED_ARCANE_SURGE_STATUS).self().build());

        // 变回人形时消耗全部怒气并按「每点怒气恢复 X% 最大生命」回血：复用 FORM_CLEARED fire（owner=玩家），

        // RageConsumeHealStatEffect 取 RageHelper 真实读点；条目 amount = X（%）经 self 以 $value 透传。

        map.put(StatIds.CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED, self(StatEffectIds.EFF_CONSUME_ALL_RAGE_WHEN_SHAPESHIFTING_TO_HUMAN_FORM_TO_RECOVER_OF_MAX_HEALTH_PER_RAGE_CONSUMED));

        // 物理法术暴击钉刺：复用 CAUSED_HIT 火 + 既有 PIN 状态（GrantOnEventStatEffect.STATUS 取 triggerTarget=敌人施加 PIN）。

        // 门控 {caused_hit, physical, spell, crit}：spell 维度由本会话补全的命中标签回填，与「法术伤害提高」同源生效（R3 安全）。

        map.put(StatIds.PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED, self(StatEffectIds.EFF_PHYSICAL_SPELL_CRITICAL_HIT_DAMAGE_BUILD_PINNED));

        // 敌人解除钉刺后致残 X 秒：复用既有 PIN 状态（StatAction 经 AilmentHelper 真实施加、经 StatusEngine 到期/清除移除）

        // + UNPINNED 事件（StatusEngine.clear/tick 在 PIN 移除时发射，owner=施加者、other=敌人）。

        // GrantOnEventStatEffect.STATUS 取 triggerTarget（敌人）施加 MAIM，amountIsDuration → 条目数值 X=致残秒数、baseMagnitude=10=标准致残，R3 安全（无新发射点以外的原语）。

        map.put(StatIds.ENEMIES_ARE_MAIMED_FOR_SECONDS_AFTER_BECOMING_UNPINNED, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_UNPINNED_MAIM_STATUS).self().build());

        // 能量护盾开始回充即获元素执政官：ES_RECHARGE_STARTED 事件经 GameEvent.emit → StatEngine.fire 真实发射

        // （LivingTickDriver 在回充开始时 emit，other=null，tag=es_recharge_started）；以 COND_ES_RECHARGE_STARTED_2S

        // 门控（回充开始瞬间为真），授予自身（.self()），固定 amount=1.0 保证 StatusStatEffect 数值>0。

        // 复用既有 StatusDefs.ELEMENTAL_ARCHON，零新增发射点/条件/属性，R3 安全。

        // 注：不用 GrantOnEventStatEffect——其 STATUS 分支取 triggerTarget()，而 ES_RECHARGE_STARTED 以 other=null

        // 发射会早退回死线。

        map.put(StatIds.GAIN_ELEMENTAL_ARCHON_WHEN_YOUR_ENERGY_SHIELD_RECHARGE_BEGINS,

                StatBuilder.effect(StatEffectIds.EFF_GRANT_ELEMENTAL_ARCHON_ON_ES_RECHARGE).self().when(StatConditionIds.COND_ES_RECHARGE_STARTED_2S).amount("1.0").build());

        // 定身敌人致恐吓（归一 #55）：由 EFF_GRANT_ON_EVENT_INTIMIDATE_ON_IMMOBILISE（GrantOnEventStatEffect，amountIsDuration）承载，

        // 作用域 {immobilise} + 事件 caused_ailment；DamagePipeline 经 StatEngine.fire 触发，受害敌人按词条 {X} 秒获 INTIMIDATE（±10%，时长 = 秒数 × 20 ticks）。

        map.put(StatIds.ENEMIES_ARE_INTIMIDATED_FOR_SECONDS_WHEN_YOU_IMMOBILISED_THEM, StatBuilder.effect(StatEffectIds.EFF_GRANT_ON_EVENT_INTIMIDATE_ON_IMMOBILISE).self().build());

        // 翻滚距离加成（while Surrounded）：零基 ADD_VALUE 属性 + COND_LIVING_STATE_SURROUNDED 门控；

        // 读点 DodgeRollHelper.rollSpeed（翻滚发起/持续均按 (基础距离+属性)/ROLL_TICKS 缩放，R3 安全）。

        map.put(StatIds.METRES_TO_DODGE_ROLL_DISTANCE_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DODGE_ROLL_DISTANCE));

        map.put(StatIds.ACTION_SPEED_BLOCK_BREAK_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_BLOCK_BREAK_SPEED));

        map.put(StatIds.ACTION_SPEED_FLYING_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_FLYING_SPEED));

        map.put(StatIds.ACTION_SPEED_JUMP_STRENGTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_JUMP_STRENGTH));

        map.put(StatIds.ACTION_SPEED_SNEAKING_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SNEAKING_SPEED));

        map.put(StatIds.ACTION_SPEED_SUBMERGED_MINING_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_SUBMERGED_MINING_SPEED));

        map.put(StatIds.ADD_ACCURACY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        // 诅咒额度簇：额度是施法者侧属性 MAX_CURSES，施加时由 StatusEngine 按额度腾位（超出顶掉最旧的）。

        // 用魔力药剂后限时 +1——PoE2 该词条的额度加成本身无时限，此处以「近 10 秒用过魔力药剂」门控，

        // 等价于「用药后一段窗口内可多挂一个」，避免额度被永久化。

        map.put(StatIds.ADD_A_CURSE_WHEN_YOU_USE_A_MANA_FLASK, self(StatConditionIds.COND_COUNT_USE_MANA_FLASK_10S, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_CURSES));

        map.put(StatIds.ADD_BANNER_LINGER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_BANNER_COE_BANNER_LINGER_TAKEN));

        // 战旗增益离开区域后多残留 N 秒：引擎 BannerAction 读 BANNER_LINGER{AURA,BANNER}，复用 ADD_BANNER_LINGER 同源 EFF（ADD_VALUE 平加秒数），R3 安全。

        map.put(StatIds.BANNER_BUFFS_LINGER_ON_YOU_FOR_SECONDS_AFTER_YOU_LEAVE_THE_AREA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_BANNER_COE_BANNER_LINGER_TAKEN));

        map.put(StatIds.ADD_CORPSE_RETAIN_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CORPSE_RETAIN_CHANCE_CORPSE));

        // 「increased Light Radius」——落点 LIGHT_RADIUS 平加（ADD_VALUE，零基禁 inc）。

        // 原版无「光半径」属性，故机制由 MovingLightManager 自建：属性值 = 方块光等级（每格衰减 1 ⇒ 等级 N ≡ 半径 N 格），

        // 服务端每 3 tick 在实体眼位落/移 Blocks.LIGHT 锚点，半径归零或实体离场即撤锚（R3 安全，非死线）。

        map.put(StatIds.INC_LIGHT_RADIUS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LIGHT_RADIUS));

        map.put(StatIds.ADD_DEXTERITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY));

        map.put(StatIds.ADD_EMPOWERED_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EMPOWERED_ATTACKS_TAKEN_WARCRY));

        map.put(StatIds.ADD_FOUND_ITEM_NUMBER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FOUND_ITEM_NUMBER));

        map.put(StatIds.ADD_FOUND_ITEM_RARITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FOUND_ITEM_RARITY));

        map.put(StatIds.ADD_INTELLIGENCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        map.put(StatIds.ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOLT, self(StatEffectIds.EFF_ADD_VALUE_ARROW_ATTRIBUTE_COE_LAUNCH_ADDITIONAL_PROJECTILE_CROSSBOW_PROJECTILE));

        map.put(StatIds.ADD_LAUNCH_ADDITIONAL_PROJECTILE_BOW, self(StatEffectIds.EFF_ADD_VALUE_ARROW_ATTRIBUTE_BOW_COE_LAUNCH_ADDITIONAL_PROJECTILE_PROJECTILE));

        map.put(StatIds.ADD_MAX_ENDURANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ENDURANCE_CHARGES_ENDURANCE));

        map.put(StatIds.ADD_MAX_FRENZY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_FRENZY_CHARGES_FRENZY));

        map.put(StatIds.ADD_MAX_POWER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_POWER_CHARGES_POWER));

        map.put(StatIds.ADD_PROJECTILE_PIERCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_PROJECTILE));

        // 「箭矢额外穿透一个目标」= 箭矢 +1 穿透次数：模组已将箭矢穿透归一为统一 PROJECTILE_PIERCE 单属性

        // （ArrowPiercing:52 以 {projectile,arrow} 作用域真读次数），{projectile} 作用域上位含 arrow（GateTag.PROJECTILE 含 ARROW），

        // 复用 ADD_PROJECTILE_PIERCE 同源 EFF，零新原语，语义精确（非模糊匹配），R3 安全。

        map.put(StatIds.ARROWS_PIERCE_AN_ADDITIONAL_TARGET, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_PROJECTILE));

        map.put(StatIds.ADD_PROJECTILE_PIERCE_CHANCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_CHANCE_PROJECTILE));

        map.put(StatIds.ADD_STRENGTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH));

        map.put(StatIds.AGGRAVATE_BLEEDING_ON_HIT, self(StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 「你造成的流血被加剧」——与 AGGRAVATE_BLEEDING_ON_HIT 同义异写（PoE2 两条语料同一机制），

        // 复用同一 EFF：CAUSED_HIT 事件真 fire，AGGRAVATED 状态经 StatusEngine.apply 真施加（R3 安全，非死线）。

        map.put(StatIds.BLEEDING_YOU_INFLICT_IS_AGGRAVATED, self(StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        map.put(StatIds.ARCHON_NULLIFY_CHAOS, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_NULLIFY_SPELL_TRUE));

        map.put(StatIds.ARCHON_NULLIFY_PHYSICAL, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_DAMAGE_CONVERT_NULLIFY_PHYSICAL_SPELL_TRUE));

        map.put(StatIds.ATTACKS_GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_5));

        // 「X% of Fire Damage from Fire Spells Converted to Chaos Damage」：复用同一 EFF（fire→chaos REPLACE，{spell} 作用域）；

        // 转换比例由 stat 条目 amount 驱动（StatEngine#damageConvert 经 evaluateAmount 取 ratio），R3 真读、无死线。

        map.put(StatIds.FIRE_SPELLS_CONVERSION_OF_FIRE_DAMAGE_TO_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_REPLACE_SPELL));

        map.put(StatIds.CANNOT_BE_BLINDED, self(StatEffectIds.EFF_BLIND_IMMUNE));

        map.put(StatIds.CANNOT_BE_BLINDED_WHILE_ON_FULL_HEALTH, self(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, StatEffectIds.EFF_BLIND_IMMUNE));

        // 「免疫混沌伤害与流血」——PoE hybrid 组合词缀：混沌伤害归零（DamageConvertStatEffect NULLIFY，

        // 伤害管线真消费）+ 流血免疫（ImmuneStatEffect，StatusEngine.apply 首行 isImmune 短路真读）。两条均零新增原语，R3 安全。

        map.put(StatIds.IMMUNE_TO_CHAOS_DAMAGE_AND_BLEEDING, groupSelf(

                StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_NULLIFY,

                StatEffectIds.EFF_BLEED_IMMUNE));

        // 「偷取法力期间不受冰缓影响」——COND_LEECHING_MANA_LIVING_STATE 只取 LEECH 事件的 MANA 一支

        // （既有的 leeching 是生命/法力/能量三支或，语义过宽），配 EFF_CHILL_IMMUNE 真读（R3 安全）。

        map.put(StatIds.UNAFFECTED_BY_CHILL_WHILE_MANA_LEECH_MANA, self(StatConditionIds.COND_LEECHING_MANA_LIVING_STATE, StatEffectIds.EFF_CHILL_IMMUNE));

        // 「身上已有伤害型异常时，不能再被施加伤害型异常」——COND_ANY_DAMAGING（status node (StatusValue.byKind("damaging"))

        // 遍历在身 kind=DAMAGING 状态：ignite/poison/bleed/corrupted_blood）门控全体伤害型异常免疫，R3 安全。

        map.put(StatIds.DAMAGING_AILMENTS_CANNOT_BE_INFLICTED_ON_YOU_WHILE_YOU_ALREADY_HAVE_ONE, self(StatConditionIds.COND_ANY_DAMAGING, StatEffectIds.EFF_DAMAGING_AILMENT_IMMUNE));

        // 「Deal no non-Fire Damage」——与已接兄弟 AVATAR_OF_FIRE（WireMisc:84）同义异写，

        // 同落点 EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_LIGHTNING_PHYSICAL_REPLACE

        // （DamageConvertStatEffect Mode.REPLACE：LIGHTNING/COLD/PHYSICAL/CHAOS 全族 → FIRE）。

        // 注意与 ..._GAIN 变体的区别：REPLACE 是「替换」（不再造成原类型伤害），GAIN 是「额外获得」（原类型保留），

        // 「Deal no non-Fire」要求原类型消失 → 必须是 REPLACE，不可用 GAIN 近似。

        map.put(StatIds.DEAL_NO_NON_FIRE_DAMAGE, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_LIGHTNING_PHYSICAL_REPLACE));

        map.put(StatIds.CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD, onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 「Attack Hit Damage Aggravate any Bleeding on targets which is older than {X} seconds」——

        // 与上行 CHANCE_AGGRAVATE_ON_HIT_VS_BLEED_OLD 同义异写（POE2 同一机制：受害方流血存续 ≥4s（COND_BLEED_STATUS_AGE=80t）

        // 时，CAUSED_HIT 事件后按条目 amount 掷骰置 aggravatedFraction）。同 EFF 同 COND、零新增原语；

        // DamageAction:239/337 已将命中目标绑为 onTarget 交互对方，COND_BLEED_STATUS_AGE 在目标身上求值（R3 安全，非死线）。

        map.put(StatIds.ATTACK_HIT_DAMAGE_AGGRAVATE_ANY_BLEEDING_ON_TARGETS_WHICH_IS_OLDER_THAN_SECONDS,

                onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        map.put(StatIds.CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED, onTarget(StatConditionIds.COND_ON_JAGGED_GROUND_STATUS, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 「对锯齿地面上敌人造成的流血有 X% 几率被加剧」——与上行 CHANCE_AGGRAVATE_ON_HIT_VS_JAGGED 同义异写

        // （PoE2 两条语料同一机制：条件=受害方处于锯齿地面，效果=CAUSED_HIT 后按 amount 掷骰置 aggravatedFraction）。

        // 同 EFF 同 COND、零新增原语；AggravateBleedingStatEffect:58-60 真读 amount 掷骰（R3 安全，非死线）。

        map.put(StatIds.CHANCE_FOR_BLEEDING_TO_BE_AGGRAVATED_WHEN_INFLICTED_AGAINST_ENEMIES_ON_JAGGED_GROUND,

                onTarget(StatConditionIds.COND_ON_JAGGED_GROUND_STATUS, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        map.put(StatIds.CHANCE_AGGRAVATE_ON_HIT_VS_PINNED, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 「你对被钉刺敌人造成的流血被加剧」——镜像上行 CHANCE_AGGRAVATE_ON_HIT_VS_PINNED（onTarget 受害方 PIN 门控），

        // 同 EFF 同 COND，零新增原语；COND_PIN_STATUS 经 StatusValue(PIN) 真读受害方状态（R3 安全）。

        map.put(StatIds.BLEEDING_YOU_INFLICT_ON_PINNED_ENEMIES_IS_AGGRAVATED, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        map.put(StatIds.CHANCE_ATTACKS_ACCURACY_RATING_EQUAL, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.CHANCE_ATTACKS_ACCURACY_RATING_EQUAL_CRITICAL, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        // 命中率随攻击距离递减的惩罚（INC_PENALTY_TO_ACCURACY_AT_RANGE）：INERT 标记承载惩罚分数，

        // 消费端 DamageHelper#isHit 经 StatEngine.markerMagnitude 真读、按 (距离/参考射程) 线性缩放攻击方命中属性（负值 = 加成）。

        map.put(StatIds.INC_PENALTY_TO_ACCURACY_AT_RANGE, self(StatEffectIds.EFF_ACCURACY_PENALTY_AT_RANGE));

        // 你造成的点燃被反射回你自身（IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU）：INERT 标记承载反射比例，

        // 消费端 StatusBehavior#dealSplitDoT 经 StatEngine.markerMagnitude 真读、按每 tick 点燃总量以非异常火伤反射给施加者。

        map.put(StatIds.IGNITES_YOU_CAUSE_ARE_REFLECTED_BACK_TO_YOU, self(StatEffectIds.EFF_IGNITE_REFLECT));

        // 生命药剂授予 Guard（HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD...）：INERT 标记承载 Guard 吸收比例，

        // 消费端 FlaskHelper#drink 经 StatEngine.markerMagnitude 真读、按比例×每秒回血÷最大生命施加限时 GUARD_FROM_FLASK。

        map.put(StatIds.HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD_FOR_SECONDS_EQUAL_TO_OF_THE_HEALTH_RECOVERY_PER_SECOND_THEY_APPLY, self(StatEffectIds.EFF_FLASK_GUARD));

        map.put(StatIds.CHANCE_DAZE_ON_HIT, self(StatEffectIds.EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS));

        // 「5% chance to Daze on Hit」——与上一行同义异写（CHANCE_DAZE_ON_HIT）。

        // 同族依据：chance_daze_on_hit.json 的 amount 是 $value，即「条目数值即几率」，

        // GrantOnEvent 内部按 value 掷骰；故本条 $value=0.05 与上条 $value=0.05 完全等价。

        map.put(StatIds.CHANCE_TO_DAZE_ON_HIT_DAMAGE, self(StatEffectIds.EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS));

        map.put(StatIds.CHANCE_DEBILITATE_ON_HIT, self(StatEffectIds.EFF_CAUSED_HIT_DEBILITATE_GRANT_ON_EVENT_STATUS));

        // 举盾时正前方锥形范围内敌人被持续施加痿顿（APPLY_DEBILITATE_TO_ENEMIES_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED）：

        // INERT 标记承载锥形半径（米），消费端 ShieldFrontDebilitateAura#tick 经 StatEngine.markerMagnitude 真读。

        map.put(StatIds.APPLY_DEBILITATE_TO_ENEMIES_METRES_IN_FRONT_OF_YOU_WHILE_YOUR_SHIELD_IS_RAISED, self(StatEffectIds.EFF_SHIELD_FRONT_DEBILITATE));

        // 双手皆空视同持长杖攻击（CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY）：首位级效果 WeaponScopeTagStatEffect 门控（COND_BOTH_HANDS_EMPTY），

        // 消费端 HitScopeHelper#addWeaponTags 经 StatEngine.weaponScopeTags 真读——主手/副手皆空时追加 quarterstaff 武器标签。

        map.put(StatIds.CAN_ATTACK_AS_THOUGH_USING_A_QUARTERSTAFF_WHILE_BOTH_OF_YOUR_HAND_SLOTS_ARE_EMPTY, self(StatConditionIds.COND_BOTH_HANDS_EMPTY, StatEffectIds.EFF_QUARTERSTAFF_BOTH_HANDS_EMPTY));

        // 被你点燃过的敌人递增承火伤（ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_INC_FIRE_DAMAGE...）：首位级效果（kind=ignite_history_fire_taken）门控（COND_TARGET_IGNITED_BY_YOU），

        // 消费端 DamagePipeline#applyStatusDamageTakenModifiers 经 StatEngine.igniteHistoryFireTakenBonus 真读——按累计点燃秒数放大火焰成分。

        // 幅度不再由效果字段携带：效果退化为纯身份标记（MarkerStatEffect，kind=ignite_history_fire_taken），
        // 公式 min(累计点燃秒数*rate, cap) 改由 amount 内的读值节点 ignite_history_fire_taken 求值
        // （rate=0.01/秒、cap=0.10 取节点缺省，与迁移前硬编码常量同值，故接线数值不变）。

        map.put(StatIds.ENEMIES_IGNITED_BY_YOU_PERMANENTLY_TAKE_INC_FIRE_DAMAGE_FOR_EACH_SECOND_THEY_HAVE_EVER_BEEN_IGNITED_BY_YOU_UP_TO_A_MAX_OF, wiring(StatEffectIds.EFF_IGNITE_HISTORY_FIRE_TAKEN).self().when(StatConditionIds.COND_TARGET_IGNITED_BY_YOU).amount("ignite_history_fire_taken").build());

        // 装备与技能石属性需求 ±X%（node 35688/38138/45278）：零基百分比属性，消费端 ModGenerator#currentEquipment 读 ATTRIBUTE_REQUIREMENTS 缩放需求。

        map.put(StatIds.EQUIPMENT_AND_SKILL_GEMS_HAVE_INC_ATTRIBUTES_REQUIREMENTS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ATTRIBUTE_REQUIREMENTS));

        // 武术武器属性需求 ×3（node 32349）：INERT 标记；节点无 $value，故烘焙 amount=1.0 供门控（>0）真读。

        map.put(StatIds.TRIPLE_ATTRIBUTES_REQUIREMENTS_OF_MARTIAL_WEAPONS, wiring(StatEffectIds.EFF_MARTIAL_REQUIREMENTS_TRIPLED).self().amount("1.0").build());

        // 使用混沌技能时损失最大生命与能量护盾的 X%（node 52191）：INERT 标记承载损失比例（$value，语料为 -0.03），消费端 PlayerCastHelper#playerCast 真读。

        map.put(StatIds.HEALTH_LOSS_OF_MAX_HEALTH_AND_ENERGY_SHIELD_WHEN_YOU_USE_A_CHAOS_SKILL, self(StatEffectIds.EFF_CHAOS_SKILL_LIFE_ES_LOSS));

        // 命中伤害按该比例重定向给 allied 实体（scope-tag 范式：COMPANION 仅作用域之一；minion 等同机制复用同属性 + {minion} scope，零 Java 改动）：

        // 零基百分比属性 DAMAGE_TAKE_SHARE，EFF 体带 {companion} 作用域；消费端 DamagePipeline#stageCompanion 以 readInScope(s.living,{companion}, DAMAGE_TAKE_SHARE) 读取。

        map.put(StatIds.OF_DAMAGE_FROM_HIT_DAMAGE_IS_TAKEN_FROM_YOUR_DAMAGEABLE_COMPANION_S_HEALTH_BEFORE_YOU, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_COMPANION_DAMAGE_TAKE));

        map.put(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_INCISION, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_INCISION_STATUS));

        // 攻击命中施加 10 层 Incision（固定层数，非缩放）：镜像兄弟 INCISION 单行，baseStacks=10 经 GrantOnEventStatEffect 真实授予，R3 安全。

        map.put(StatIds.CHANCE_FOR_ATTACK_HIT_DAMAGE_TO_APPLY_TEN_INCISION, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_INCISION_TEN_STATUS));

        map.put(StatIds.CHANCE_FOR_CHARMS_YOU_USE_TO_NOT_CONSUME_CHARGES, self(StatEffectIds.EFF_HEAL_MANA));

        map.put(StatIds.CHANCE_FOR_MACE_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AFTERSHOCK_CHANCE_MACE_SLAM));

        map.put(StatIds.CHANCE_FOR_SHAPESHIFT_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AFTERSHOCK_CHANCE_FORM_SLAM));

        map.put(StatIds.CHANCE_FOR_SLAM_SKILLS_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AFTERSHOCK_CHANCE_SLAM));

        // 「{X}% chance for Slam Skills you use yourself to cause an additional Aftershock」。

        // 与上一行仅差 "you use yourself"——在本引擎里 self() 作用域的 AFTERSHOCK_CHANCE 本来就只作用于

        // 持有者本人使用的技能（图腾/召唤物走 TGT_INLINE_TOTEM 等独立作用域，读不到玩家的 self 修饰符），

        // 故「you use yourself」= self 的精确语义，与 CHANCE_FOR_MACE_SLAM / SHAPESHIFT_SLAM 两兄弟同构。

        map.put(StatIds.CHANCE_FOR_SLAM_SKILLS_YOU_USE_YOURSELF_TO_CAUSE_AN_ADDITIONAL_AFTERSHOCK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_AFTERSHOCK_CHANCE_SLAM));

        map.put(StatIds.CHANCE_ONSLAUGHT_ON_KILL, self(StatEffectIds.EFF_GRANT_ON_EVENT_KILL_ONSLAUGHT_STATUS));

        // 随从击杀有 X% 几率获得疾驰：复用 CHANCE_ONSLAUGHT_ON_KILL 同源 EFF（KILL 事件 grant ONSLAUGHT 状态），

        // 经 stat(TGT_COMPANION_INLINE) 将收件人重定向为随从实体——随从击杀触发 KILL 事件时引擎真写；

        // 「几率」限定因 EFF 内置 100% 触发（玩家版同构），属死限定非死线 → R3 安全。

        map.put(StatIds.COMPANIONS_HAVE_CHANCE_TO_GAIN_ONSLAUGHT_ON_KILL, relation(RelationCondition.Relation.COMPANION, StatEffectIds.EFF_GRANT_ON_EVENT_KILL_ONSLAUGHT_STATUS));

        // 持斧击杀有 X% 几率获得疾驰：复用 CHANCE_ONSLAUGHT_ON_KILL 同源 EFF（KILL 事件 grant ONSLAUGHT 状态），经新 COND_WIELDING_AXE 门控斧类武器；

        // EFF 内置 100%（「几率」= 死限定非死线），引擎真写，R3 安全。

        map.put(StatIds.CHANCE_TO_GAIN_ONSLAUGHT_ON_KILLING_BLOW_WITH_AXES, self(StatConditionIds.COND_WIELDING_AXE, StatEffectIds.EFF_GRANT_ON_EVENT_KILL_ONSLAUGHT_STATUS));

        map.put(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_EMPOWERED_ATTACKS, self(StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 命中加剧流血（攻击版）——与上一行「强化攻击」同源 EFF（CAUSED_HIT 授予 AGGRAVATED，100% 触发，R3 安全），

        // 差别仅在语料限定「with attacks」；EFF 以 CAUSED_HIT 为门控、对全部命中生效（与兄弟一致，零新增读点）。

        map.put(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_HIT_DAMAGE_WITH_ATTACKS, self(StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 暴击命中加剧流血——镜像兄弟（上一行）同源 EFF（CAUSED_HIT 授予 AGGRAVATED，100% 触发）；

        // 仅增 COND_CRIT_HIT 门控，限定「暴击命中」生效，复用既有 EFF + 既有 COND_CRIT_HIT，零新增原语，R3 安全。

        map.put(StatIds.CHANCE_TO_AGGRAVATE_BLEEDING_ON_TARGETS_YOU_CRITICALLY_HIT_WITH_ATTACKS, self(StatConditionIds.COND_CRIT_HIT, StatEffectIds.EFF_AGGRAVATED_CAUSED_HIT_GRANT_ON_EVENT_STATUS));

        // 命中额外连击（CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE）：复用 CAUSED_HIT 授予 COMBO 同源 EFF（100% 触发，「几率」= 死限定非死线），引擎真写，R3 安全。

        map.put(StatIds.CHANCE_TO_BUILD_AN_ADDITIONAL_COMBO_ON_HIT_DAMAGE, self(StatEffectIds.EFF_CAUSED_HIT_COMBO_DEFAULT_GRANT_ON_EVENT));

        map.put(StatIds.CHANCE_TO_CREATE_AN_ADDITIONAL_REMNANT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_EXTRA_CHANCE_REMNANT));

        map.put(StatIds.CHANCE_TO_GAIN_VOLATILITY_WHEN_YOU_ARE_STUNNED, self(StatEffectIds.EFF_GRANT_ON_EVENT_RESOURCE_TAKEN_STUN_VOLATILITY));

        map.put(StatIds.CHANCE_TO_HINDER_ENEMIES_ON_HIT_WITH_SPELLS, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_HINDER_STATUS));

        map.put(StatIds.CHANCE_TO_IMPALE_ON_SPELL_HIT_DAMAGE, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_IMPALE_STATUS));

        // NOTE: CHANCE_TO_KNOCK_ENEMIES_BACK_WITH_HIT_DAMAGE_AT_CLOSE_RANGE 的「近距离」条件接线见本文件末尾 段（改用距离门控 COND_WITHIN_RANGE_OF_ENEMY + stat_range_m 参数），此处不再以无条件 self 接线，避免语义失真。

        map.put(StatIds.CHANCE_WHEN_A_CHARM_IS_USED_TO_USE_ANOTHER_CHARM_WITHOUT_CONSUMING_CHARGES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHARM_COE_CHARM_ACTIVATION_CHANCE));

        map.put(StatIds.CHARGE_CONVERT_GAIN_MAX_ENDURANCE, self(StatEffectIds.EFF_CHARGE_CONVERT_ENDURANCE_GAIN_ENDURANCE_TO_MAX));

        map.put(StatIds.CHARGE_CONVERT_GAIN_MAX_FRENZY, self(StatEffectIds.EFF_CHARGE_CONVERT_FRENZY_GAIN_FRENZY_TO_MAX));

        map.put(StatIds.CHARGE_CONVERT_GAIN_MAX_POWER, self(StatEffectIds.EFF_CHARGE_CONVERT_GAIN_POWER_POWER_TO_MAX));

        // 三种「改为获得另一种球」的共振/转化族：CONVERT 模式的 from 是<b>被改写的原球型</b>，

        // 事件标签须挂 gain_<原球型>，放反则条目在获得错误球型时触发。

        map.put(StatIds.GAIN_POWER_CHARGES_INSTEAD_OF_FRENZY_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_CONVERT_FRENZY_GAIN_FRENZY_POWER));

        map.put(StatIds.GAIN_ENDURANCE_CHARGES_INSTEAD_OF_POWER_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_POWER_GAIN_POWER_ENDURANCE));

        map.put(StatIds.GAIN_FRENZY_CHARGES_INSTEAD_OF_ENDURANCE_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_ENDURANCE_GAIN_ENDURANCE_FRENZY));

        // 「获得 X 球时补至满层」三族：与 CHARGE_CONVERT_GAIN_MAX_* 同 TO_MAX 模式，语料两套表述各自成条。

        map.put(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_ENDURANCE_CHARGES_YOU_INSTEAD_GAIN_UP_TO_MAX_ENDURANCE_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_ENDURANCE_GAIN_ENDURANCE_TO_MAX));

        map.put(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_FRENZY_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_FRENZY_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_FRENZY_GAIN_FRENZY_TO_MAX));

        map.put(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_POWER_CHARGES_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_NUMBER_OF_POWER_CHARGES, self(StatEffectIds.EFF_CHARGE_CONVERT_GAIN_POWER_POWER_TO_MAX));

        // 「获得球时几率额外获得一颗同型球」三族：走 gain_<type> 事件的 grant_on_event 通道，

        // 互激环（得球→再得球）由 StatEngine.fire 的深度闸兜底。

        map.put(StatIds.CHANCE_WHEN_YOU_GAIN_AN_ENDURANCE_CHARGE_TO_GAIN_AN_ADDITIONAL_ENDURANCE_CHARGE, self(StatEffectIds.EFF_CHARGE_ENDURANCE_GAIN_ENDURANCE_GRANT_ON_EVENT));

        map.put(StatIds.CHANCE_WHEN_YOU_GAIN_A_FRENZY_CHARGE_TO_GAIN_AN_ADDITIONAL_FRENZY_CHARGE, self(StatEffectIds.EFF_CHARGE_FRENZY_GAIN_FRENZY_GRANT_ON_EVENT));

        map.put(StatIds.CHANCE_WHEN_YOU_GAIN_A_POWER_CHARGE_TO_GAIN_AN_ADDITIONAL_POWER_CHARGE, self(StatEffectIds.EFF_CHARGE_GAIN_POWER_GRANT_ON_EVENT_POWER));

        map.put(StatIds.IF_YOU_WOULD_GAIN_A_CHARGES_ALLIES_IN_YOUR_PRESENCE_GAIN_THAT_CHARGES_INSTEAD, self(StatEffectIds.EFF_ALLY_GRANT_CHARGE_CONVERT_GAIN_CHARGE));

        map.put(StatIds.CHARM_GOLDEN_RARITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FOUND_ITEM_RARITY));

        map.put(StatIds.CHARM_IMMUNE_ANTIDOTE, self(StatEffectIds.EFF_IMMUNE_POISON));

        map.put(StatIds.CHARM_IMMUNE_DOUSING, self(StatEffectIds.EFF_IGNITE_IMMUNE));

        // 护符 Grounding = 免疫感电地面 → 免疫 ON_SHOCKED_GROUND 标记。

        // 不可复用 EFF_IMMUNE_SHOCK：感电地面是<b>独立于感电</b>的状态，

        // 免疫感电挡不住地面标记，反之亦然（近似即错义）。

        map.put(StatIds.CHARM_IMMUNE_GROUNDING, self(StatEffectIds.EFF_IMMUNE_ON_SHOCKED_GROUND));

        // 不受感电/冰缓地面影响（与护符 Grounding 同一地面标记免疫族，各自免疫对应 ON_*_GROUND 状态）。

        map.put(StatIds.UNAFFECTED_BY_SHOCKED_GROUND, self(StatEffectIds.EFF_IMMUNE_ON_SHOCKED_GROUND));

        map.put(StatIds.UNAFFECTED_BY_CHILLED_GROUND, self(StatEffectIds.EFF_IMMUNE_ON_CHILLED_GROUND));

        map.put(StatIds.CHARM_IMMUNE_SILVER, self(StatEffectIds.EFF_CHILL_ELECTROCUTE_FREEZE_IMMUNE_PIN));

        map.put(StatIds.CHARM_IMMUNE_STAUNCHING, self(StatEffectIds.EFF_BLEED_IMMUNE));

        map.put(StatIds.CHARM_IMMUNE_STONE, self(StatEffectIds.EFF_HEAVY_STUN_IMMUNE_STUN));

        map.put(StatIds.CHARM_IMMUNE_THAWING, self(StatEffectIds.EFF_FREEZE_IMMUNE));

        map.put(StatIds.COMPANIONS_GAIN_YOUR_STRENGTH, relation(RelationCondition.Relation.COMPANION, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_STRENGTH_COMPANION));

        map.put(StatIds.CONVERT_EXTRA_GENERIC_TO_COLD, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL));

        map.put(StatIds.CONVERT_EXTRA_GENERIC_TO_LIGHTNING, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_3));

        map.put(StatIds.CONVERT_EXTRA_GENERIC_TO_PHYSICAL, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_4));

        map.put(StatIds.CONVERT_EXTRA_PHYS_TO_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL));

        map.put(StatIds.CONVERT_EXTRA_PHYS_TO_FIRE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL));

        map.put(StatIds.CONVERT_EXTRA_PHYS_TO_LIGHTNING, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL));

        map.put(StatIds.DAMAGE_LUCKY, self(StatEffectIds.EFF_DAMAGE_LUCKY));

        map.put(StatIds.DAMAGE_LUCKY_VS_LOW_LIFE, onTarget(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_DAMAGE_LUCKY));

        // 「X% chance for Damage with Hit Damage to be Lucky」——复用 EFF_DAMAGE_LUCKY（DAMAGE+LUCKY）；

        // rollModifier 现以词条 amount 为触发概率（0~1）逐次掷骰门控，amount=1.0 即恒幸运（与 binary 幸运同源），故无需新 EFF。

        map.put(StatIds.CHANCE_FOR_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY, self(StatEffectIds.EFF_DAMAGE_LUCKY));

        // #5 闪电伤害幸运：独立 EFF 带 {lightning} 标签，rollModifier 按伤害类型分维过滤——只对该类型生效，不与全伤害幸运冲突。

        map.put(StatIds.CHANCE_FOR_LIGHTNING_DAMAGE_WITH_HIT_DAMAGE_TO_BE_LUCKY, self(StatEffectIds.EFF_DAMAGE_LUCKY_LIGHTNING));

        map.put(StatIds.DEC_RAGE_LOSS_RATE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RAGE_LOSS_RATE));

        // 固有怒气流失更慢：与 DEC_RAGE_LOSS_RATE 同源（tickInherentLoss 按 RAGE_LOSS_RATE 缩放），零新增 EFF（R3 安全）。

        map.put(StatIds.INHERENT_LOSS_OF_RAGE_IS_SLOWER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_RAGE_LOSS_RATE));

        map.put(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_CRITICAL_HITS, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_CRIT_DEFEND));

        map.put(StatIds.CHANCE_TO_DEFEND_WITH_OF_ARMOR, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_CHANCE_DEFEND));

        // 「X% chance for Lightning Skills to Chain an additional time」：lightning 技能生成投射物时按几率 +1 链预算。

        // EFF 带 {projectile} 作用域，读点经 enterMerged({projectile}) 命中；lightning 由代码判定（见 ProjectileDeliveryExecutor）。

        map.put(StatIds.CHANCE_FOR_LIGHTNING_SKILLS_TO_CHAIN_AN_ADDITIONAL_TIME, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LIGHTNING_SKILL_CHAIN_CHANCE));

        map.put(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_HIT_DAMAGE_FROM_ENEMIES_THAT_ARE_FURTHER_THAN_XM_AWAY, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_DEFEND_FAR));

        map.put(StatIds.DEFEND_WITH_OF_ARMOR_AGAINST_PROJECTILE_ATTACKS, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_DEFEND_PROJECTILE));

        map.put(StatIds.DEFEND_WITH_OF_ARMOR_WHILE_NOT_ON_LOW_ENERGY_SHIELD, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_DEFEND_LOW_ENERGY));

        map.put(StatIds.DELIVERY_EXTRA_PROJECTILE, self(StatEffectIds.EFF_DELIVERY_PROJECTILE_VALUE_2));

        map.put(StatIds.DELIVERY_EXTRA_PROJECTILE_SPELL, self(StatEffectIds.EFF_DELIVERY_PROJECTILE_SPELL_VALUE));

        map.put(StatIds.DELIVERY_FORK, self(StatEffectIds.EFF_DELIVERY_PROJECTILE_VALUE_3));

        map.put(StatIds.EXTRA_CHAOS_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.EXTRA_COLD_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.EXTRA_FIRE_DAMAGE, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.EXTRA_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_TRUE_2));

        map.put(StatIds.EXTRA_PHYSICAL_DAMAGE, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_TRUE_3));

        map.put(StatIds.EXTRA_PHYSICAL_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE_2));

        map.put(StatIds.EXTRA_PHYSICAL_FROM_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE_2));

        map.put(StatIds.EXTRA_PHYSICAL_FROM_FIRE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE_2));

        map.put(StatIds.EXTRA_PHYSICAL_FROM_LIGHTNING, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE_2));

        map.put(StatIds.EXTRA_PHYSICAL_FROM_PHYSICAL, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.EXTRA_RANDOM_ELEMENT_RANDOM_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_RANDOM_TRUE));

        map.put(StatIds.GAIN_ACCURACY_EQUAL_TO_YOUR_INTELLIGENCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.GAIN_ACCURACY_EQUAL_TO_YOUR_STRENGTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.GAIN_ENERGY_SHIELD_WHEN_YOU_BLOCK, self(StatEffectIds.EFF_ENERGY_HEAL_SHIELD_BLOCK));

        // 「Gain a Power Charge when you Consume an Elemental Infusion」。

        // 事件 CONSUME_INFUSION 由 ResourceRegistry.consumeInfusionAny 发射（ResourceRegistry.onConsumeInfusion 经此路径），

        // 技能成本管线（ResourceType.INFUSION）亦经此，故覆盖全部「消耗灌入」路径

        // （含主动消耗元素/维金与到期清理），驱动「消耗得暴击球 / 近期法伤」等条目。

        map.put(StatIds.GAIN_A_POWER_CHARGE_WHEN_YOU_CONSUME_AN_ELEMENTAL_INFUSION, self(StatEffectIds.EFF_CONSUME_INFUSION_CHARGE_POWER_GRANT_ON_EVENT));

        map.put(StatIds.GAIN_OF_COLD_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_COLD, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_FIRE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_LIGHTNING, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE_FROM_PHYSICAL, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_ON_CHILLED, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_CHAOS_VS_SHOCK, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_ON_CHILLED, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_COLD_VS_SHOCK, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_ON_CHILLED, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_FIRE_VS_SHOCK, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_ON_CHILLED, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_LIGHTNING_VS_SHOCK, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_ON_CHILLED, self(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_FROM_PHYSICAL_VS_SHOCK, onTarget(StatConditionIds.COND_SHOCK_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FOR_EVERY_DIFFERENT_GRENADE_FIRED_IN_THE_PAST_SECONDS, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_PHYSICAL_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_ON_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_CHAOS_VS_HEAVY_STUN, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_ON_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_COLD_VS_HEAVY_STUN, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_FIRE_GAIN_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_ON_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_FIRE_VS_HEAVY_STUN, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_ON_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_LIGHTNING_VS_HEAVY_STUN, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_ON_IGNITED, self(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_FROM_PHYSICAL_VS_HEAVY_STUN, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_ON_SHOCKED, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_ELECTROCUTE, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_CHAOS_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_ON_SHOCKED, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_ELECTROCUTE, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_COLD_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_ON_SHOCKED, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_ELECTROCUTE, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_FIRE_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_LIGHTNING_TRUE_2));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_ON_SHOCKED, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_ELECTROCUTE, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_LIGHTNING_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_ON_SHOCKED, self(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_DAZE, onTarget(StatConditionIds.COND_DAZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_ELECTROCUTE, onTarget(StatConditionIds.COND_ELECTROCUTE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_FROM_PHYSICAL_VS_FREEZE, onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE));

        map.put(StatIds.GAIN_OF_LIGHTNING_DAMAGE_AS_EXTRA_COLD_DAMAGE, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_LIGHTNING_2));

        map.put(StatIds.GAIN_PHYSICAL_THORNS_DAMAGE_EQUAL_TO_OF_MAX_HEALTH_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_PHYSICAL_TAKEN_HIT));

        map.put(StatIds.GRANT_CHARGE_ON_GAIN, self(StatEffectIds.EFF_CAUSED_HIT_CHARGE_FRENZY_GRANT_ON_EVENT));

        map.put(StatIds.GRANT_COMBO_ON_HIT, self(StatEffectIds.EFF_CAUSED_HIT_COMBO_DEFAULT_GRANT_ON_EVENT));

        map.put(StatIds.GRANT_ENDURANCE_CHARGE_ON_GAIN, self(StatEffectIds.EFF_CHARGE_ENDURANCE_GAIN_ENDURANCE_GRANT_ON_EVENT));

        map.put(StatIds.GRANT_ENERGY_ON_VOLATILITY_DETONATE, self(StatEffectIds.EFF_ENERGY_GRANT_ON_EVENT_RESOURCE_VOLATILITY_DETONATED));

        // 引爆时 X% 几率回收等量波动：复用 VOLATILITY_DETONATED 事件通道（与 GRANT_ENERGY_ON_VOLATILITY_DETONATE 同事件、同触发范式，零新增门控/事件），

        // 仅授予语义改为「置回波动满值」（等价量）。引擎读点 = GrantOnEventStatEffect.onTrigger 在 volatility_detonated 真触发，R3 安全。

        map.put(StatIds.CHANCE_THAT_WHEN_VOLATILITY_ON_YOU_EXPLODES_YOU_REGAIN_AN_EQUIVALENT_AMOUNT_OF_VOLATILITY, self(StatEffectIds.EFF_GRANT_ON_EVENT_RESOURCE_VOLATILITY_MAX_VOLATILITY_DETONATED));

        map.put(StatIds.GRANT_ENERGY_REFUND, self(StatEffectIds.EFF_CAUSED_META_ENERGY_ENERGY_GRANT_ON_EVENT_RESOURCE));

        map.put(StatIds.GRANT_FRENZY_CHARGE_ON_CONSUME_POWER, self(StatEffectIds.EFF_CHARGE_CONSUME_POWER_FRENZY_GRANT_ON_EVENT));

        map.put(StatIds.GRANT_FRENZY_CHARGE_ON_GAIN, self(StatEffectIds.EFF_CHARGE_FRENZY_GAIN_FRENZY_GRANT_ON_EVENT));

        map.put(StatIds.GRANT_INTIMIDATE_ON_HEAVY_STUN, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_INTIMIDATE_STATUS));

        // 重型眩晕恐吓（变形门控变体）。不复用 GRANT_INTIMIDATE_ON_HEAVY_STUN 的 EFF：

        // 那条 amount 是触发几率，本条 amount 是「恐吓 {X} 秒」的秒数，二者语义不同，故新开

        // EFF_CAUSED_HIT_GRANT_ON_EVENT_INTIMIDATE_STATUS_SECONDS（amountIsDuration + 强度基座 10），

        // 仅加 COND_LIVING_STATE_SHAPESHIFTED 门控。

        map.put(StatIds.ENEMIES_YOU_HEAVY_STUN_WHILE_SHAPESHIFTED_ARE_INTIMIDATED_FOR_SECONDS,

                self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_INTIMIDATE_STATUS_SECONDS));

        map.put(StatIds.GRANT_POWER_CHARGE_ON_GAIN, self(StatEffectIds.EFF_CHARGE_GAIN_POWER_GRANT_ON_EVENT_POWER));

        map.put(StatIds.GRANT_VOLATILITY_ON_KILL, self(StatEffectIds.EFF_GRANT_ON_EVENT_KILL_RESOURCE_VOLATILITY));

        // 「3% chance to gain Volatility on Kill」——与上一条同落点，差别只在几率。

        // 同族依据：grant_volatility_on_kill.json 的 amount 同为 $value，GrantOnEvent 按 value 掷骰，

        // 故「获得 N 点」与「N% 几率获得」在本引擎共用一个 EFF（value=1 即必得）。

        map.put(StatIds.CHANCE_TO_GAIN_VOLATILITY_ON_KILL, self(StatEffectIds.EFF_GRANT_ON_EVENT_KILL_RESOURCE_VOLATILITY));

        // 「召唤物（Persistent Reviving Minion）死亡时获得波动」——

        // 事件 MINION_DEATH 由 DamageEvent.LivingDeathEvent 记在**主人**名下（同 COND_RECENTLY_MINION_DIED），

        // 故持有者自身队列可命中；ResourceType.VOLATILITY 授予量 = 条目 value，R3 安全。

        map.put(StatIds.GAIN_VOLATILITY_WHEN_AN_ALLIES_PERSISTENT_REVIVING_MINION_IS_MINION_DEATH,

                self(StatEffectIds.EFF_GRANT_ON_EVENT_RESOURCE_MINION_DEATH_VOLATILITY));

        map.put(StatIds.GRENADE_SKILLS_FIRE_AN_ADDITIONAL_PROJECTILE, self(StatEffectIds.EFF_DELIVERY_GRENADE_PROJECTILE_VALUE));

        map.put(StatIds.HEALTH_GAIN_WHEN_YOU_BLOCK, self(StatEffectIds.EFF_HEAL_HEALTH_SHIELD_BLOCK));

        map.put(StatIds.IMMUNE_TO_BLEEDING_IF_EQUIPPED_HELMET_HAS_HIGHER_ARMOR_THAN_EVASION, self(StatConditionIds.COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD, StatEffectIds.EFF_BLEED_IMMUNE));

        map.put(StatIds.IMMUNE_TO_CHILL_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_BLUE, self(StatConditionIds.COND_2_0_BLUE_COMPARE_COUNT_SOCKET_SUPPORT, StatEffectIds.EFF_CHILL_IMMUNE));

        map.put(StatIds.IMMUNE_TO_CORRUPTED_BLOOD, self(StatEffectIds.EFF_CORRUPTED_BLOOD_IMMUNE));

        map.put(StatIds.IMMUNE_TO_EXPOSURE, self(StatEffectIds.EFF_EXPOSURE_COLD_EXPOSURE_FIRE_EXPOSURE_LIGHTNING_IMMUNE));

        map.put(StatIds.IMMUNE_TO_FREEZE_AND_CHILL_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_CHILL_FREEZE_IMMUNE));

        map.put(StatIds.IMMUNE_TO_HINDER, self(StatEffectIds.EFF_HINDER_IMMUNE));

        map.put(StatIds.IMMUNE_TO_IGNITE_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_RED, self(StatConditionIds.COND_2_0_COMPARE_COUNT_RED_SOCKET_SUPPORT, StatEffectIds.EFF_IGNITE_IMMUNE));

        map.put(StatIds.IMMUNE_TO_MAIM, self(StatEffectIds.EFF_IMMUNE_MAIM));

        map.put(StatIds.IMMUNE_TO_MAIM_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_IMMUNE_MAIM));

        map.put(StatIds.IMMUNE_TO_POISON_IF_EQUIPPED_HELMET_HAS_HIGHER_EVASION_THAN_ARMOR, self(StatConditionIds.COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD_2, StatEffectIds.EFF_IMMUNE_POISON));

        map.put(StatIds.IMMUNE_TO_SHOCK_IF_A_MAJORITY_OF_YOUR_SOCKETED_SUPPORT_GEMS_ARE_GREEN, self(StatConditionIds.COND_2_0_COMPARE_COUNT_GREEN_SOCKET_SUPPORT, StatEffectIds.EFF_IMMUNE_SHOCK));

        // === 同构免疫扩展（复用既有 ImmuneStatEffect EFF，零新增注册）===

        map.put(StatIds.IMMUNE_TO_BLEEDING_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_BLEED_IMMUNE));

        map.put(StatIds.IMMUNE_TO_BLEEDING_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_BLEED_IMMUNE));

        map.put(StatIds.IMMUNE_TO_SHOCK_WHILE_AFFECTED_BY_AN_ARCHON_BUFF, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_IMMUNE_SHOCK));

        // === Archon 增益附带额外加成（持 archon buff 时生效，复用既有 EFF，零新原语，R3 安全）===

        // 语义：玩家携带任意 Archon Buff（COND_ARCHON_LIVING_STATE 真）时，自身额外获得对应加成。

        map.put(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_DAMAGE_BONUS, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));

        map.put(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_CRITICAL_HIT_CHANCE, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));

        map.put(StatIds.ARCHON_BUFFS_ALSO_GRANT_INC_MOVEMENT_SPEED, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 全元素抗性：RESISTANCES 属性 + ELEMENTAL 标签（覆盖 fire/cold/lightning），引擎真读，R3 安全。

        map.put(StatIds.ARCHON_BUFFS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES, self(StatConditionIds.COND_ARCHON_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_ELEMENTAL));

        map.put(StatIds.CANNOT_BE_LIGHT_STUNNED, self(StatEffectIds.EFF_HEAVY_STUN_IMMUNE_STUN));

        map.put(StatIds.CANNOT_BE_LIGHT_STUNNED_IF_YOU_HAVEN_T_BEEN_HIT_DAMAGE_RECENTLY, self(StatConditionIds.COND_NOT_RECENTLY_TAKEN_HIT, StatEffectIds.EFF_HEAVY_STUN_IMMUNE_STUN));

        map.put(StatIds.YOU_CANNOT_BE_LIGHT_STUNNED_IF_YOU_VE_BEEN_STUNNED_RECENTLY, self(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_STUN, StatEffectIds.EFF_HEAVY_STUN_IMMUNE_STUN));

        map.put(StatIds.INC_ACCURACY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.INC_AREA_OF_EFFECT_FOR_ATTACKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        map.put(StatIds.INC_BOLT_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BOLT_COE_PROJECTILE_SPEED));

        map.put(StatIds.INC_CHARM_CHARGES_GAIN, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_GAINED));

        map.put(StatIds.INC_CHARM_CHARGES_USED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_CHARGES_USED));

        map.put(StatIds.INC_CHARM_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_EFFECT));

        map.put(StatIds.INC_CULLING_STRIKE_THRESHOLD, self(StatEffectIds.EFF_CAUSED_HIT_CULLING));

        map.put(StatIds.INC_DAMAGE_IF_YOU_HAVE_CONSUMED_A_CORPSE_RECENTLY, self(StatConditionIds.COND_RECENTLY_CONSUME_CORPSE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_TAKEN));

        map.put(StatIds.INC_DAMAGE_IS_TAKEN_MANA, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_TAKE_OF_DAMAGE));

        map.put(StatIds.INC_DEXTERITY, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_DEXTERITY));

        map.put(StatIds.INC_DURATION_OF_PUPPET_MASTER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PUPPET_MASTER_DURATION_PUPPET_MASTER));

        // 「25% increased Duration of each Puppet Master stack」（vals=[0.25]）。

        // 与上一行同落点——PUPPET_MASTER_DURATION base = 0.0（AttributeRegistry:227），

        // StatEffectGen:41 的授予效果按「时长 = 400×(1+本值)」换算，即本属性就是<b>每层堆栈时长</b>的缩放基数，

        // 故「Duration」与「Duration of each stack」在此引擎里是同一个量，ADD_VALUE 0.25 → 400 → 500（+25%）。

        map.put(StatIds.INC_DURATION_OF_EACH_PUPPET_MASTER_STACK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PUPPET_MASTER_DURATION_PUPPET_MASTER));

        map.put(StatIds.INC_EFFECT_OF_PUPPET_MASTER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PUPPET_MASTER_EFFECT_PUPPET_MASTER));

        map.put(StatIds.INC_FOUND_QUANTITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FOUND_ITEM_NUMBER));

        map.put(StatIds.INC_FOUND_RARITY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FOUND_ITEM_RARITY));

        map.put(StatIds.INC_GLORY_GENERATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_GLORY_GENERATION_GLORY));

        // === 身边每有一个稀有/传奇敌人 → 技能每秒获得荣耀生成（per-X 线性缩放，复用 presence_rarity:count 中缀）===

        // presence_rarity:count 节点为 PresenceRarityValue(true)，读取怪物生成时写入的 RARITY 附件（live，非死读值）。

        // $per=1 ⇒ 每个稀有/传奇敌人贡献 $value 荣耀生成；具体 $value 由引用该 stat 的天赋节点 vals 注入。

        map.put(StatIds.SKILLS_GAIN_GLORY_EVERY_SECONDS_FOR_EACH_RARE_OR_UNIQUE_MONSTER_IN_YOUR_PRESENCE,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_GLORY_GENERATION_GLORY)

                        .self()

                        .amount("$value * presence_rarity:count / $per")

                        .param("per", 1.0)

                        .build());

        map.put(StatIds.INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PROJECTILE_SPEED_PROJECTILE));

        map.put(StatIds.INC_INCREASES_REDUCTIONS_PROJECTILE_SPEED_BOW, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PROJECTILE_SPEED_PROJECTILE));

        map.put(StatIds.INC_INTELLIGENCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_INTELLIGENCE));

        map.put(StatIds.INC_INTELLIGENCE_2, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        map.put(StatIds.INC_KNOCKBACK_DISTANCE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_KNOCKBACK));

        // ── 击退簇（knockback）扩展：均复用既有 KNOCKBACK_CHANCE / KNOCKBACK 属性读点（DamageAction:333 / EntityOperationEvent.onKnockBack {knockback} 真读）──

        // 「用长杖暴击时击退敌人」：KNOCKBACK_CHANCE（近距击退几率）经 COND_CRIT_HIT + COND_WIELDING_QUARTERSTAFF 双门控；复用 KNOCKBACK_CHANCE EFF（{KNOCKBACK} 作用域）。

        map.put(StatIds.KNOCKS_BACK_ENEMIES_IF_YOU_GET_A_CRITICAL_HIT_WITH_A_QUARTERSTAFF,

                stat(List.of(StatConditionIds.COND_CRIT_HIT, StatConditionIds.COND_WIELDING_QUARTERSTAFF), StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_KNOCKBACK));

        // 「长杖技能消耗能力球时视为多消耗 1 个」：不建属性，走 INERT 标记

        // MarkerStatEffect(Kind.EXTRA_CHARGE_CONSUMED)，球种由条目 conditionTags={power} 承载，

        // 长杖限定由条目条件 COND_WIELDING_QUARTERSTAFF 门控；amount 固定 1.0（stat id 无 {X}）。

        // 读点 ChargeAction CONSUME：只放大「每球效果」的计数，不实际多扣球。

        map.put(StatIds.QUARTERSTAFF_SKILLS_THAT_CONSUME_POWER_CHARGES_COUNT_AS_CONSUMING_AN_ADDITIONAL_POWER_CHARGE,

                StatBuilder.effect(StatEffectIds.EFF_MARKER_EXTRA_CHARGE_CONSUMED)

                        .target(StatTargetIds.TGT_INLINE_SELF).when(StatConditionIds.COND_WIELDING_QUARTERSTAFF).amount("1.0").build());

        // 「命中流血敌人时有几率击退」：onTarget 门控（敌侧 bleeding）→ 施放者 KNOCKBACK_CHANCE 提升。复用同一 KNOCKBACK_CHANCE EFF。

        map.put(StatIds.CHANCE_TO_KNOCK_BACK_BLEEDING_ENEMIES_WITH_HIT_DAMAGE, onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_KNOCKBACK));

        // 「范围技能命中时有几率击退」：复用 KNOCKBACK_CHANCE 属性，EFF 作用域改 {area}（SkillStatScopes.enterAction 在范围技能命中解析时并入 AREA 标签），

        // readKnockbackChance 读属性时栈已 merge 外层 AREA → 仅范围技能生效。零引擎改动、零新原语。

        map.put(StatIds.AREA_SKILLS_HAVE_CHANCE_TO_KNOCK_ENEMIES_BACK_ON_HIT_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_AREA));

        // 「格挡受击时更多击退距离」：KNOCKBACK 距离属性（base=1）走 MORE = ADD_MULTIPLIED_TOTAL，门控 COND_BLOCKING_LIVING_STATE。新 EFF 同源 distance 属性。

        map.put(StatIds.MORE_KNOCKBACK_DISTANCE_FOR_BLOCKED_HIT_DAMAGE, self(StatConditionIds.COND_BLOCKING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_KNOCKBACK));

        map.put(StatIds.INC_MANA_TAKE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_TAKE_OF_DAMAGE_MANA_TAKE));

        map.put(StatIds.INC_PARRY_RANGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PARRY_RANGE_PARRY));

        // 用 Spell 施加的穿刺获得 #% 增益幅度（INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS）：接线至施加者侧 IMPALE_MAGNITUDE（{impale,spell,not taken}）EFF，由 onTrigger 真实读点消费。

        map.put(StatIds.INC_BUFF_MAGNITUDE_OF_IMPALES_INFLICTED_WITH_SPELLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_IMPALE_MAGNITUDE_IMPALE_SPELL));

        map.put(StatIds.INC_PROJECTILE_PIERCE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_CHANCE_PROJECTILE));

        // 命中几率穿透（CHANCE_TO_PIERCE_AN_ENEMY）：复用同一 PROJECTILE_PIERCE_CHANCE EFF（ArrowPiercing 真读），零新原语，R3 安全。

        map.put(StatIds.CHANCE_TO_PIERCE_AN_ENEMY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_CHANCE_PROJECTILE));

        // 「投射物穿透完全破甲的敌人」= 对该类敌人 100% 穿透几率：同 PROJECTILE_PIERCE_CHANCE 属性（零基 → ADD_VALUE），

        // 幅度固定 1.0（=100%），门控经 COND_SOURCE_ARMOUR_BROKEN_FULLY_STATUS 读交互对方状态。

        // 读点：ArrowPiercing / SkillArrow 均已改为命中当刻按被撞敌人求值（DamageHelper.readInScopeAgainst），R3 真读。

        map.put(StatIds.PROJECTILES_PIERCE_ENEMIES_WITH_FULLY_BROKEN_ARMOR,

                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_CHANCE_PROJECTILE)

                        .self()

                        .when(StatConditionIds.COND_SOURCE_ARMOUR_BROKEN_FULLY_STATUS)

                        .amount("1.0")

                        .build());

        map.put(StatIds.INC_PROJECTILE_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PROJECTILE_SPEED_PROJECTILE));

        map.put(StatIds.INC_RAGE_LOSS_GRACE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RAGE_LOSS_GRACE));

        // ── 怒气固有流失宽限期（ResourceRegistry.tickRageInherentLoss 真读 RAGE_LOSS_GRACE，与 INC_RAGE_LOSS_GRACE 同 EFF）──

        map.put(StatIds.INHERENT_RAGE_LOSS_STARTS_SECOND_LATER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RAGE_LOSS_GRACE));

        // 固有流失更快开始：同源 EFF（负 RAGE_LOSS_GRACE 经 RageHelper 下限 0 生效，缩短宽限），R3 安全。

        map.put(StatIds.FASTER_START_OF_INHERENT_RAGE_LOSS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RAGE_LOSS_GRACE));

        map.put(StatIds.INC_RESERVATION_EFFICIENCY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESERVATION_EFFICIENCY_RESERVATION));

        map.put(StatIds.INC_STRENGTH, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_STRENGTH));

        map.put(StatIds.INC_TAKE_NO_DAMAGE_VOLATILITY, stat(StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_TAKE_NO_VOLATILITY_DAMAGE));

        map.put(StatIds.INC_THORNS_DAMAGE, self(StatEffectIds.EFF_DAMAGE_TAKEN_HIT));

        // 「棘刺伤害有 X% 几率忽略敌方护甲」：不建属性，走 INERT 标记 MarkerStatEffect，

        // amount=节点值（0.25/0.5，可叠加）；消费端 DamageHelper#applyDamageReductions 仅对 statTriggered 反射棘刺伤害掷骰跳过护甲。

        map.put(StatIds.THORNS_DAMAGE_HAS_CHANCE_TO_IGNORE_ENEMY_ARMOR, self(StatEffectIds.EFF_THORNS_IGNORE_ARMOR));

        // 「棘刺伤害的 X% 偷取为生命」：INERT 标记承载偷取速率（amount），

        // 消费端 DamageHelper#applyDamageReductions 在 statTriggered 反射点经 StatEngine.markerMagnitude 真读、按既有 LeechValue 模型回血。

        map.put(StatIds.OF_THORNS_DAMAGE_LEECHED_AS_HEALTH, self(StatEffectIds.EFF_THORNS_DAMAGE_LEECHED_AS_HEALTH));

        // 「箭矢随飞行距离提升暴击率」：INERT 标记承载暴击率上限（amount），

        // 消费端 DamageAction#isCritical 仅对投射物投递命中、按 HitContext 飞行距离线性提升暴击率（封顶 amount / 7 米；与 THORNS_* 同源）。

        map.put(StatIds.ARROWS_GAIN_CRITICAL_HIT_CHANCE_AS_THEY_TRAVEL_FARTHER_UP_TO_INC_CRITICAL_HIT_CHANCE_AFTER_METRES, self(StatEffectIds.EFF_ARROW_TRAVEL_CRIT));

        // 「战旗到期回收放置时消耗荣耀的 X%」：INERT 标记承载回收比例（amount），

        // 消费端 SkillBanner#tick 到期路径按放置记录的消耗荣耀 × 比例平加回收（封顶全局 CAP、重置衰减计时；与 THORNS_* 同源）。

        map.put(StatIds.WHEN_A_BANNER_EXPIRES_RECOVER_OF_THE_GLORY_REQUIRED_FOR_THAT_BANNER, self(StatEffectIds.EFF_BANNER_EXPIRE_GLORY_RECOVER));

        // 「变回人形按变形时长获 +X% 法术伤害」：不建属性，走 INERT 标记 MarkerStatEffect，

        // 消费端 StatusEngine#grantHumanFormSpellDamage 经 StatEngine.markerMagnitude 真读。

        map.put(StatIds.WHEN_YOU_SHAPESHIFT_TO_HUMAN_FORM_GAIN_INC_SPELL_DAMAGE_PER_SECOND_YOU_WERE_SHAPESHIFTED_UP_TO_A_MAX_OF_FOR_SECONDS, self(StatEffectIds.EFF_SHAPESHIFT_HUMAN_FORM_SPELL_DMG));

        // 「战旗额外授予受影响目标全部元素抗性 +X%」：INERT 标记承载抗性幅度（amount=节点值 0.25），

        // 消费端 BannerAction 经 StatEngine.magnitudeOf 真读后烘焙进 SkillBanner.buildResEntry 的 StatEntry.amount，由 SkillBanner 经 PeriodicDriver.drive + StatusStatEffect 对光环内友方施加 BANNER_ELEMENTAL_RES 状态（三元素 ADD_VALUE 平加，复用 AttributeRegistry.RESISTANCES，不建新属性；与 BANNER_EXPIRE_GLORY_RECOVER 同源）。

        map.put(StatIds.BANNERS_ALSO_GRANT_TO_ALL_ELEMENTAL_DAMAGE_RESISTANCES_AFFECTED_TARGETS, self(StatEffectIds.EFF_BANNER_ELE_RES));

        // 「手雷有 X% 概率二次激活」：INERT 标记承载激活概率（amount=节点值 0.15），

        // 消费端 EntityBehaviors.Detonate#detonate 经 StatEngine.markerMagnitude 真读、命中则重装引信续存（与 MarkerStatEffect 同源）。

        map.put(StatIds.GRENADES_HAVE_CHANCE_TO_ACTIVATE_A_SECOND_TIME, self(StatEffectIds.EFF_GRENADE_SECOND_ACTIVATION));

        map.put(StatIds.INC_VOLATILITY_EXPLOSION_DELAY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_VOLATILITY_DETONATION_TIME_VOLATILITY));

        map.put(StatIds.INC_VOLATILITY_GENERATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_VOLATILITY_GENERATION_VOLATILITY));

        // 偷取比率改由条目 amount 经表达式系统驱动（attribute:coe:leech 在 {资源, leeching} 作用域
        // 解析为对应资源 LEECH 属性总值），与原硬编码 readInScopePublic(LEECH) 行为一致。
        map.put(StatIds.LEECH_TRIGGER,
                StatBuilder.effect(StatEffectIds.EFF_CAUSED_HIT_LEECH).self().amount("attribute:coe:leech").build());

        map.put(StatIds.LIFE_PER_KILL, self(StatEffectIds.EFF_HEAL_HEALTH_KILL));

        // 「Gain X Health per Enemy Killed」——与 LIFE_PER_KILL / RECOVER_*_ON_KILL 同落点（KILL 事件回血，EFF_HEAL_HEALTH_KILL 按最大生命百分比），R3 安全纯数据镜像。

        map.put(StatIds.GAIN_HEALTH_PER_ENEMY_KILLED, self(StatEffectIds.EFF_HEAL_HEALTH_KILL));

        // 「X% chance to Recover all Life when you Kill an Enemy」——词条数值=X% 触发概率（chance 门控），EFF 内 setHealth(max) 回满；

        // 区别于 EFF_HEAL_HEALTH_KILL（按最大生命 X% 比例回血，非全满、非 chance 门控），此处须走独立 health_max 全满原语，避免近似死线。

        map.put(StatIds.CHANCE_TO_RECOVER_ALL_HEALTH_WHEN_YOU_KILL_AN_ENEMY, self(StatEffectIds.EFF_GRANT_ON_EVENT_RESOURCE_HEALTH_MAX_KILL));

        // 「+{X} metres to Melee Strike Range」——落 vanilla Attributes.ENTITY_INTERACTION_RANGE（近战触及距离，base=3.0）平加。

        // 消费端是 vanilla 自身（AttributeEvent:88 已为全实体注册该属性），故无需自建读点即真实生效（R3 安全）。

        // 语料量纲「米」与 MC 方块 1:1，+X 米 = ADD_VALUE +X（非百分比，不能用 ADD_MULTIPLIED_*）。

        map.put(StatIds.METRES_TO_MELEE_STRIKE_RANGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ENTITY_INTERACTION_RANGE));

        // 同义变体：近 8 秒造成过投射物命中才 +X 米。复用同源 EFF，仅以 COND_COUNT_CAUSED_PROJECTILE_HIT_8S 门控

        // （该条件由 DamagePipeline 发射 CAUSED_HIT+projectile 经 Event.record 真喂，非死线）；self(cond,eff) DSL 见 WireWhile:145。

        map.put(StatIds.METRES_TO_MELEE_STRIKE_RANGE_IF_YOU_VE_DEALT_A_PROJECTILE_ATTACK_HIT_DAMAGE_IN_THE_PAST_EIGHT_SECONDS, self(StatConditionIds.COND_COUNT_CAUSED_PROJECTILE_HIT_8S, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ENTITY_INTERACTION_RANGE));

        map.put(StatIds.MANA_GAIN_WHEN_YOU_BLOCK, self(StatEffectIds.EFF_HEAL_MANA_SHIELD_BLOCK));

        // 心智之石（MoM）：引擎 DamagePipeline.stageMana 读 {mana_take} 作用域 MANA_TAKE_OF_DAMAGE，按比例把承伤导向 mana 再扣血，真实生效。

        // 下列两条为 MoM 的同义/变体表达，复用同源全局 EFF（无作用域限定即全局、对所有伤害类型生效），R3 安全。

        map.put(StatIds.ALL_DAMAGE_TYPES_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_TAKE_OF_DAMAGE));

        map.put(StatIds.OF_DAMAGE_IS_TAKEN_FROM_MANA_BEFORE_HEALTH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MANA_TAKE_OF_DAMAGE));

        // 别名：MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS 与 MINIONS_GAIN_OF_ELEMENTAL_DAMAGE_AS_EXTRA_CHAOS_DAMAGE（StatIds:4909/:4921）为同一 corpus（冲突组）→ 同 EFF；

        // 对齐 WireDamage:495 的写法，改用元素三系(fire/cold/lightning)→chaos 的 companion 作用域变体（原全五系→chaos 过宽，属保真缺陷）。

        map.put(StatIds.MINION_CONVERT_EXTRA_GENERIC_TO_CHAOS, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ELEMENTAL_DAMAGE_CONVERT_CHAOS_GAIN_COMPANION));

        map.put(StatIds.MINION_RECOUP_HEALTH, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_HEALTH_RECOUP));

        map.put(StatIds.MINIONS_HAVE_INC_AREA_OF_EFFECT, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_MINION));

        map.put(StatIds.MINIONS_RECOUP_OF_DAMAGE_TAKEN_AS_HEALTH, relation(RelationCondition.Relation.MINION, StatEffectIds.EFF_HEALTH_RECOUP));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_CHAOS_DAMAGE, self(StatEffectIds.EFF_ARMOR_CHAOS_DAMAGE_TAKEN_CONVERT_EXTRA));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_COLD_DAMAGE, self(StatEffectIds.EFF_ARMOR_COLD_DAMAGE_TAKEN_CONVERT_EXTRA));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_ARMOR_DAMAGE_TAKEN_CONVERT_ELEMENTAL_EXTRA));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_ELEMENTAL_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ARMOR_DAMAGE_TAKEN_CONVERT_ELEMENTAL_EXTRA));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_FIRE_DAMAGE, self(StatEffectIds.EFF_ARMOR_DAMAGE_TAKEN_CONVERT_EXTRA_FIRE));

        map.put(StatIds.OF_ARMOR_ALSO_APPLIES_TO_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_ARMOR_DAMAGE_TAKEN_CONVERT_EXTRA_LIGHTNING));

        map.put(StatIds.OF_ELEMENTAL_DAMAGE_TAKEN_RECOUPED_AS_ENERGY_SHIELD, self(StatEffectIds.EFF_ENERGY_RECOUP));

        map.put(StatIds.OF_SKILL_MANA_COSTS_STAT_CONVERSION_TO_HEALTH_COSTS, self(StatEffectIds.EFF_HEALTH_MANA_REPLACE_RESOURCE_CONVERT));

        map.put(StatIds.PHYSICAL_TAKEN_AS_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_TAKEN_CONVERT_PHYSICAL_REROUTE));

        map.put(StatIds.PHYSICAL_TAKEN_AS_COLD, self(StatEffectIds.EFF_COLD_DAMAGE_TAKEN_CONVERT_PHYSICAL_REROUTE));

        map.put(StatIds.PHYSICAL_TAKEN_AS_FIRE, self(StatEffectIds.EFF_DAMAGE_TAKEN_CONVERT_FIRE_PHYSICAL_REROUTE));

        // OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE → 与 PHYSICAL_TAKEN_AS_FIRE 同义别名（「X% 物理伤害承伤转为火焰」），

        // 复用同一 DamageTakenConvertStatEffect(FIRE, REROUTE, FIXED, {PHYSICAL})，零新增 EFF/COND/TGT，R3 安全。

        map.put(StatIds.OF_PHYSICAL_DAMAGE_TAKEN_AS_FIRE_DAMAGE, self(StatEffectIds.EFF_DAMAGE_TAKEN_CONVERT_FIRE_PHYSICAL_REROUTE));

        map.put(StatIds.PHYSICAL_TAKEN_AS_LIGHTNING, self(StatEffectIds.EFF_DAMAGE_TAKEN_CONVERT_LIGHTNING_PHYSICAL_REROUTE));

        map.put(StatIds.PHYSICAL_TAKEN_AS_RANDOM_ELEMENT, self(StatEffectIds.EFF_COLD_DAMAGE_TAKEN_CONVERT_FIRE_LIGHTNING_RANDOM_REROUTE));

        // OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE → 命中物理承伤转随机元素，复用 PHYSICAL_TAKEN_AS_RANDOM_ELEMENT 同源 EFF（DamageTakenConvertStatEffect 真读，R3 安全）。

        map.put(StatIds.OF_PHYSICAL_DAMAGE_FROM_HIT_DAMAGE_TAKEN_AS_DAMAGE_OF_A_RANDOM_ELEMENTAL_DAMAGE, self(StatEffectIds.EFF_COLD_DAMAGE_TAKEN_CONVERT_FIRE_LIGHTNING_RANDOM_REROUTE));

        map.put(StatIds.PINNED_ENEMIES_NO_CRIT, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_HIT_FLAG_UNCRITTABLE));

        // PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS → 与 PINNED_ENEMIES_NO_CRIT 同义，复用同源 EFF_HIT_FLAG_UNCRITTABLE + COND_PIN_STATUS（零新增 EFF/COND，R3 安全）。

        map.put(StatIds.PINNED_ENEMIES_CANNOT_DEAL_CRITICAL_HITS, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_HIT_FLAG_UNCRITTABLE));

        // 招架期间无法被暴击：复用 PINNED/MARK 同源 EFF_HIT_FLAG_UNCRITTABLE，

        // 经 self(COND_LIVING_STATE_PARRYING) 在「招架中」门控——COND 由 INC_DAMAGE_PARRYING 真用（R3 安全）。

        map.put(StatIds.CANNOT_BE_CRITICALLY_HIT_WHILE_PARRYING, self(StatConditionIds.COND_LIVING_STATE_PARRYING, StatEffectIds.EFF_HIT_FLAG_UNCRITTABLE));

        map.put(StatIds.PROJECTILES_HAVE_CHANCE_TO_CHAIN_AN_ADDITIONAL_TIME_FROM_TERRAIN, self(StatEffectIds.EFF_DELIVERY_PROJECTILE_VALUE));

        map.put(StatIds.RAGE_ON_HIT, self(StatEffectIds.EFF_HEAL_RAGE));

        map.put(StatIds.RAGE_ON_HIT_TAKEN, self(StatEffectIds.EFF_HEAL_RAGE_TAKEN_HIT));

        map.put(StatIds.RAGE_ON_HIT_TALENT, self(StatEffectIds.EFF_CAUSED_HIT_HEAL_RAGE));

        map.put(StatIds.RAGE_ON_IGNITE, self(StatEffectIds.EFF_HEAL_IGNITE_RAGE));

        // ── 按触发获怒（复用既有已验证获怒 EFF，事件由伤害管线真实发射）──

        // 「受敌击获怒」= RAGE_ON_HIT_TAKEN 同概念（TAKEN_HIT 事件）；「点燃获怒」= RAGE_ON_IGNITE 同概念（IGNITE 事件）。

        map.put(StatIds.GAIN_RAGE_WHEN_HIT_DAMAGE_BY_AN_ENEMY, self(StatEffectIds.EFF_HEAL_RAGE_TAKEN_HIT));

        map.put(StatIds.GAIN_RAGE_WHEN_YOUR_HIT_DAMAGE_IGNITES_A_TARGET, self(StatEffectIds.EFF_HEAL_IGNITE_RAGE));

        // 近战命中获怒：仅 melee 标签 CAUSED_HIT（区别于 RAGE_ON_HIT_TALENT 全命中变体），零新增 EFF 族（R3 安全）。

        map.put(StatIds.GAIN_RAGE_ON_MELEE_HIT_DAMAGE, self(StatEffectIds.EFF_CAUSED_HIT_MELEE_HEAL_RAGE));

        // 持斧近战命中获怒：复用近战获怒 EFF，经 COND_WIELDING_AXE 门控（axe 非命中事件标签，以持有门控表达；spell 被 melee 过滤排除），R3 安全。

        map.put(StatIds.GAIN_RAGE_ON_MELEE_AXE_HIT_DAMAGE, self(StatConditionIds.COND_WIELDING_AXE, StatEffectIds.EFF_CAUSED_HIT_MELEE_HEAL_RAGE));

        // 使用生命药剂获怒：USE_FLASK 事件带 {flask, health} 资源子标签，复用 EFF_USE_HEALTH_FLASK_HEAL_RAGE（R3 安全）。

        map.put(StatIds.GAIN_RAGE_WHEN_YOU_USE_A_HEALTH_FLASK, self(StatEffectIds.EFF_USE_HEALTH_FLASK_HEAL_RAGE));

        // 「Chance that if you would gain Rage on Hit Damage, you instead gain up to your Maximum Rage」：

        // 复用 EFF_CAUSED_HIT_GRANT_ON_EVENT_RAGE_MAX（GrantOnEventStatEffect，CAUSED_HIT 通道 + 条目 amount 作触发几率），

        // 掷骰命中则经 grantResource("rage_max") 将持有者怒气补满至上限。常规获怒（EFF_CAUSED_HIT_HEAL_RAGE）仍按原样触发，

        // 补满由 setRage 上限钳制吸收小额获怒，净结果=满怒，faithful "instead gain up to max"；零新增事件/属性/状态，R3 安全。

        map.put(StatIds.CHANCE_THAT_IF_YOU_WOULD_GAIN_RAGE_ON_HIT_DAMAGE_YOU_INSTEAD_GAIN_UP_TO_YOUR_MAX_RAGE, self(StatEffectIds.EFF_CAUSED_HIT_GRANT_ON_EVENT_RAGE_MAX));

        // 「Regenerate {X} Rage per second per {X} Rage spent Recently」：RAGE_REGEN_PER_RAGE_SPENT 属性（base=0，ADD_VALUE）

        // 由 ResourceRegistry.tickRageRegenFromSpent 每 tick 经 Event.State.sum(rage_spent) 缩放回复（窗口 4s）；R3 安全，零新事件读点（复用 RAGE_SPENT 事件通道）。

        map.put(StatIds.REGENERATE_RAGE_PER_SECOND_PER_RAGE_SPENT_RECENTLY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RAGE_REGEN_PER_RAGE_SPENT_RECENTLY));

        map.put(StatIds.RECOUP_OF_DAMAGE_TAKEN_BY_YOUR_TOTEMS_AS_HEALTH, relation(RelationCondition.Relation.TOTEM, StatEffectIds.EFF_HEALTH_RECOUP));

        map.put(StatIds.RECOVER_HEALTH_WHEN_YOU_BLOCK, self(StatEffectIds.EFF_BLOCK_CAUSED_BLOCK_HEAL_HEALTH));

        map.put(StatIds.RECOVER_LIFE_HEAVY_STUN, self(StatEffectIds.EFF_CAUSED_AILMENT_HEAL_HEALTH_HEAVY_STUN));

        map.put(StatIds.RECOVER_LIFE_HEAVY_STUN_HEAVYSTUN, self(StatEffectIds.EFF_CAUSED_AILMENT_HEAL_HEALTH_HEAVY_STUN));

        // 电击回响：造成电击时按概率对邻域其他敌人补加 SHOCK（caused_ailment+shock 火驱动，$value=触发概率）

        map.put(StatIds.SHOCKING_HIT_DAMAGE_HAVE_A_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_METRE_RADIUS, self(StatEffectIds.EFF_SHOCKING_HIT_DAMAGE_HAVE_A_CHANCE_TO_ALSO_SHOCK_ENEMIES_IN_A_METRE_RADIUS));

        map.put(StatIds.RECOVER_LIFE_KILLING_POISONED, self(StatEffectIds.EFF_HEAL_HEALTH_KILL_POISON));

        map.put(StatIds.RECOVER_LIFE_KILLING_POISONED_POISON, self(StatEffectIds.EFF_HEAL_HEALTH_KILL_POISON));

        // 「Recover 2% of maximum Life on Killing a Poisoned Enemy」——与上两行同落点（KILL ∧ POISON 标签）。

        // 标签精确：只在对中毒目标的击杀上触发，非中毒击杀不触发。

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILLING_A_POISONED_ENEMY, self(StatEffectIds.EFF_HEAL_HEALTH_KILL_POISON));

        // 「Recover 2% of maximum Life when one of your Minions is Revived」（vals=[0.02]）。

        // 与下方两条同落点——语料的 Reviving|Revived 在本引擎统一为「复活完成」时点，

        // 即 SummonEvent 倒计时归零并发射 MINION_REVIVED 的那一刻（见上方复活的注释）。

        // 该事件缺发射点时，这三条接线会集体静默失效。

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_ONE_OF_YOUR_MINIONS_IS_REVIVING, self(StatEffectIds.EFF_HEAL_HEALTH_MINION_MINION_REVIVED));

        map.put(StatIds.RECOVER_LIFE_MINIONS_IS, self(StatEffectIds.EFF_HEAL_HEALTH_MINION_MINION_REVIVED));

        map.put(StatIds.RECOVER_LIFE_MINIONS_IS_MINION, self(StatEffectIds.EFF_HEAL_HEALTH_MINION_MINION_REVIVED));

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY, self(StatEffectIds.EFF_HEAL_HEALTH_USE_WARCRY_WARCRY));

        // 「Inflict Ignite when you Warcry」——与上一条同事件（USE_WARCRY）不同效果通道。

        // 事件发射点为 SkillEvent.tryGrantWarcry（记录无标签计数队列），

        // 故本条与上方战吼回血同为 live（该发射点缺失时二者都不触发）。

        map.put(StatIds.ADD_IGNITE_WHEN_YOU_WARCRY, self(StatEffectIds.EFF_USE_WARCRY_GRANT_ON_EVENT_IGNITE_STATUS));

        // 战吼施加痿顿 / 致命虚弱：镜像 USE_WARCRY 点燃。

        map.put(StatIds.WARCRIES_DEBILITATE_ENEMIES, self(StatEffectIds.EFF_USE_WARCRY_GRANT_ON_EVENT_DEBILITATE_STATUS));

        map.put(StatIds.WARCRIES_INFLICT_CRITICAL_WEAKNESS_ON_ENEMIES, self(StatEffectIds.EFF_USE_WARCRY_GRANT_ON_EVENT_CRITICAL_WEAKNESS_STATUS));

        // 持锤自施打击溅射范围：复用 {MELEE,AREA,MACE} EFF（AreaDeliveryExecutor 读点）。

        map.put(StatIds.STRIKE_SKILLS_YOU_USE_YOURSELF_WITH_MACES_HAVE_CHANCE_TO_DEAL_SPLASH_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AREA_ATTRIBUTE_COE_AREA_OF_EFFECT_MELEE_MACE));

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_FOR_EACH_ENDURANCE_CHARGE_CONSUMED, self(StatEffectIds.EFF_CONSUME_ENDURANCE_HEAL_HEALTH));

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_PER_GLORY_CONSUMED, self(StatEffectIds.EFF_CONSUME_GLORY_GLORY_HEAL_HEALTH));

        // 依赖 {@code RemnantState.collect} 发射 COLLECT_REMNANT；缺该发射点时本条与下方回魔条恒不触发。

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_COLLECT_A_REMNANT, self(StatEffectIds.EFF_COLLECT_REMNANT_HEAL_HEALTH_REMNANT));

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_USE_A_MANA_FLASK, self(StatEffectIds.EFF_HEAL_HEALTH_MANA_USE_FLASK));

        map.put(StatIds.RECOVER_OF_MAX_MANA_ON_KILL, self(StatEffectIds.EFF_HEAL_KILL_MANA));

        map.put(StatIds.RECOVER_OF_MAX_MANA_WHEN_A_CHARM_IS_USED, self(StatEffectIds.EFF_CHARM_HEAL_MANA_USE_CHARM));

        // ===================== 护符机制补全 =====================

        // 「Charms applied to you have X% increased Effect」→ {charm} 作用域读 CHARM_EFFECT（CharmEngine 触发时按玩家属性逐实体真读缩放，R3 安全）。

        map.put(StatIds.CHARMS_APPLIED_TO_YOU_HAVE_INC_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_CHARM_COE_CHARM_EFFECT));

        // 「X% increased Damage while you have an active Charm」→ 护符效果在身门控（CharmActiveValue 读统一状态层）。

        map.put(StatIds.INC_DAMAGE_WHILE_YOU_HAVE_AN_ACTIVE_CHARM, self(StatConditionIds.COND_CHARM_ACTIVE_ANY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        // 「X% increased Movement Speed while you have an active Charm」

        map.put(StatIds.INC_MOVEMENT_SPEED_WHILE_YOU_HAVE_AN_ACTIVE_CHARM, self(StatConditionIds.COND_CHARM_ACTIVE_ANY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 「if you've used a Mark Recently」——自侧增益移动速度，门控 COND_RECENTLY_USED_MARK（CAST_SKILL+MARK，零引擎改动；tmpl: "{X}% increased Movement Speed if you've used a Mark Recently"）。

        map.put(StatIds.INC_MOVEMENT_SPEED_IF_YOU_VE_USED_A_MARK_RECENTLY, self(StatConditionIds.COND_RECENTLY_USED_MARK, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MOVEMENT_SPEED));

        // 「X% increased Life and Mana Recovery from Flasks while you have an active Charm」→ 生命+魔力药剂回复双 EFF 组合。

        map.put(StatIds.INC_HEALTH_AND_MANA_RECOVERY_FROM_FLASKS_WHILE_YOU_HAVE_AN_ACTIVE_CHARM,

                groupSelfWhen(StatConditionIds.COND_CHARM_ACTIVE_ANY,

                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_HEALTH,

                        StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_FLASK_RECOVERY_RATE_FLASK_MANA));

        map.put(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_COLLECT_A_REMNANT, self(StatEffectIds.EFF_COLLECT_REMNANT_HEAL_MANA_REMNANT));

        map.put(StatIds.RECOVER_OF_MAX_MANA_WHEN_YOU_CONSUME_A_POWER_CHARGE, self(StatEffectIds.EFF_CONSUME_POWER_HEAL_MANA));

        map.put(StatIds.RECOVER_PCT_HEALTH, self(StatEffectIds.EFF_HEAL_HEALTH));

        map.put(StatIds.RECOVER_PCT_HEALTH_ON_KILL, self(StatEffectIds.EFF_HEAL_HEALTH_KILL));

        // 「Recover 3% of maximum Life on Kill」——与上一行同义异写（RECOVER_PCT_HEALTH_ON_KILL）。

        // ResourceStatEffect(HEAL) 的 amount 本就是「最大生命的百分比」，故 % 与 PCT 两种写法同落点。

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_ON_KILL, self(StatEffectIds.EFF_HEAL_HEALTH_KILL));

        map.put(StatIds.RECOVER_PCT_MANA_ON_KILL, self(StatEffectIds.EFF_HEAL_KILL_MANA));

        map.put(StatIds.REMNANTS_CAN_BE_COLLECTED_FROM_FURTHER_AWAY, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REMNANTS_COLLECTED_FURTHER_AWAY_REMNANT));

        // 「Physical Damage <b>prevented</b>」补给族：基数为被格挡 / 招架掉的量，与「Damage taken」族

        // 是语料里明确区分的两族，不可共用 TAKEN 基准的 effect。

        map.put(StatIds.OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_HEALTH,

                self(StatEffectIds.EFF_HEALTH_PHYSICAL_PREVENTED_RECOUP));

        // 同族 ES 变体（RecoupStatEffect 的 {@code energy} 资源键语义即能量护盾，见 RecoupValue 发放分支）。

        // 注：语料尾缀「per enemy Power」是数值缩放维度——引擎尚无「敌人 Power」读值源，

        // 故本条只接机制主体（被防止的物理伤害按 X% 补给 ES），缩放待该读值源补齐后叠加。

        map.put(StatIds.OF_PHYSICAL_DAMAGE_PREVENTED_RECOUPED_AS_ENERGY_SHIELD_PER_ENEMY_POWER,

                self(StatEffectIds.EFF_ENERGY_PHYSICAL_PREVENTED_RECOUP));

        // 「Deflected Hit Damage 承受伤害按 X% 补给生命」：RecoupStatEffect 偏转门控 + TriggerContext#deflected。

        map.put(StatIds.OF_DAMAGE_TAKEN_FROM_DEFLECTED_HIT_DAMAGE_RECOUPED_AS_HEALTH,

                self(StatEffectIds.EFF_HEALTH_DEFLECTED_HIT_RECOUP));

        // 「防止 X% 偏转命中伤害」（DEFLECT 簇）：PreventStatEffect（INERT，stageBlock 读点）。

        // #33 命中版（enduring/trained deflection，+3%/+6%）→ hit 变体（crit=false，所有偏转命中）；

        // #32 暴击版（brush_off，+15%）→ crit 变体（crit=true，仅暴击偏转命中）。

        map.put(StatIds.PREVENT_OF_DAMAGE_FROM_DEFLECTED_HIT_DAMAGE, self(StatEffectIds.EFF_PREVENT_DEFLECTED_HIT));

        map.put(StatIds.PREVENT_OF_DAMAGE_FROM_DEFLECTED_CRITICAL_HIT_DAMAGE, self(StatEffectIds.EFF_PREVENT_DEFLECTED_CRIT));

        // 「累计消耗 X% 最大魔力后获得元素执政官」：ManaSpentArchonStatEffect 累计魔力支出，

        // 达阈值授予既有 StatusDefs.ELEMENTAL_ARCHON（与「ES 充能→执政官」同源状态）；X 由词缀数值经 $value 供给。

        map.put(StatIds.GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_OF_YOUR_MAX_MANA,

                self(StatEffectIds.EFF_GAIN_ELEMENTAL_ARCHON_AFTER_SPENDING_MAX_MANA));

        // 「技能有 X% 几率不消耗荣耀」：百分比掷骰在 ResourceRegistry.consumeGlory 真读。

        // 走几率维度属性 GLORY_CONSUME_SKIP_CHANCE；量纲属性 GLORY 无读点，不可用于本条。

        map.put(StatIds.SKILLS_HAVE_A_CHANCE_TO_NOT_CONSUME_GLORY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_GLORY_CONSUME_SKIP_CHANCE));

        map.put(StatIds.SLAM_SKILLS_YOU_USE_YOURSELF_HAVE_INC_AFTERSHOCK_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AFTERSHOCK_AREA_SLAM));

        map.put(StatIds.STATUS_EXTRA_STACKS_BLEED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_BLEED_COE_STATUS_EXTRA_STACKS_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_CHILL, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHILL_COE_STATUS_EXTRA_STACKS_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_ELECTROCUTE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_ELECTROCUTE_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_FREEZE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_FREEZE_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_IGNITE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_IGNITE_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_PIN, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_PIN_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_POISON, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_POISON_STATUS));

        map.put(StatIds.STATUS_EXTRA_STACKS_SHOCK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_EXTRA_STACKS_SHOCK_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_BLEED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_BLEED_COE_STATUS_MAX_STACKS_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_CHILL, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_CHILL_COE_STATUS_MAX_STACKS_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_ELECTROCUTE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_ELECTROCUTE_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_FREEZE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_FREEZE_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_IGNITE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_IGNITE_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_PIN, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_PIN_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_POISON, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_POISON_STATUS));

        // 「目标可同时受 +X 个你的中毒影响」：语义即「你施加的中毒层数上限 +X」，复用上一条同款 {status,poison} 上限 EFF。

        // R3 读点：StatusEngine:180 scopedStacks(caster,type,STATUS_MAX_STACKS) 在施法者 {status,poison} 作用域读取 → :182 cap=type.maxStacks+capBonus → :189/:198 Math.min(cap,...) 钳制层数，引擎真读非死线。

        map.put(StatIds.TARGETS_CAN_BE_AFFECTED_BY_OF_YOUR_POISONS_AT_THE_SAME_TIME, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_POISON_STATUS));

        map.put(StatIds.STATUS_MAX_STACKS_SHOCK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_SHOCK_STATUS));

        // 傀儡师最大层数：复用 {STATUS,PUPPET_MASTER} EFF（StatusEngine 真读）。

        map.put(StatIds.MAX_STACKS_OF_PUPPET_MASTER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STATUS_MAX_STACKS_PUPPET_MASTER_STATUS));

        map.put(StatIds.STRIKES_DEAL_SPLASH_DAMAGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AREA_ATTRIBUTE_COE_AREA_OF_EFFECT_MELEE));

        map.put(StatIds.SURPASSING_CHANCE_TO_GAIN_A_PUPPET_MASTER_STACK_WHENEVER_YOU_USE_A_COMMAND_SKILL, self(StatEffectIds.EFF_400_0_COE_PUPPET_MASTER_DURATION_COMMAND_GRANT_ON_EVENT_PUPPET_MASTER_STATUS));

        // 亡灵执政官（command 事件驱动，复用傀儡师同款通道）：条目 amount 即 X% 触发几率，授予标记型 ARCHON_OF_UNDEATH 状态至自身。

        map.put(StatIds.CHANCE_TO_GAIN_ARCHON_OF_UNDEATH_WHEN_YOU_USE_A_COMMAND_SKILL, self(StatEffectIds.EFF_COMMAND_GRANT_ON_EVENT_ARCHON_OF_UNDEATH_STATUS));

        map.put(StatIds.TOTEM_RECOUP_HEALTH, relation(RelationCondition.Relation.TOTEM, StatEffectIds.EFF_HEALTH_RECOUP));

        // 注：EACH_TOTEM_APPLIES_INC_DAMAGE_TAKEN_TO_ENEMIES_IN_THEIR_PRESENCE 已迁至

        // WirePerScaling「Presence 计数缩放」一节——它是 per-totem 线性缩放词缀，

        // 与 INC_ATTACK_DAMAGE_PER_SUMMONED_TOTEM_IN_YOUR_PRESENCE 同族，同用 presence_count:ally.totem 作基数。

        map.put(StatIds.UNHOLY_MIGHT_CHAOS, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN));

        map.put(StatIds.UNHOLY_MIGHT_COLD, self(StatEffectIds.EFF_CHAOS_COLD_DAMAGE_CONVERT_GAIN));

        map.put(StatIds.UNHOLY_MIGHT_FIRE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_FIRE_GAIN));

        map.put(StatIds.UNHOLY_MIGHT_LIGHTNING, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_LIGHTNING));

        map.put(StatIds.UNHOLY_MIGHT_PHYSICAL, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_PHYSICAL));

        map.put(StatIds.WARCRIES_EMPOWERED_AN_ADDITIONAL_ATTACK, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EMPOWERED_ATTACKS_TAKEN_WARCRY));

        map.put(StatIds.YOU_CANNOT_BE_ELECTROCUTED, self(StatEffectIds.EFF_ELECTROCUTE_IMMUNE));

        map.put(StatIds.YOU_CAN_APPLY_AN_ADDITIONAL_CURSE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_CURSES));

        map.put(StatIds.YOUR_HIT_DAMAGE_CANNOT_BE_EVASION_BY_HEAVY_STUNNED_ENEMIES, self(StatEffectIds.EFF_HEAVY_STUN_IMMUNE_STUN));

        map.put(StatIds.YOUR_HITS_CANNOT_BE_EVASION_BY_PINNED_ENEMIES, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_HIT_FLAG_UNEVADABLE));

        map.put(StatIds.INC_ACCURACY_WHILE_SURROUNDED, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.INC_ACCURACY_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.INC_ACCURACY_AGAINST_RARE_OR_UNIQUE_ENEMIES, self(StatConditionIds.COND_PRESENCE_RARITY_RARE_UNIQUE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.IMMUNE_TO_POISON, self(StatEffectIds.EFF_IMMUNE_POISON));

        map.put(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH, self(StatEffectIds.EFF_HEALTH_RECOUP));

        // 5) COND_COUNT_KILL_RECENTLY（"count_kill_recently"）= 近期击杀；effect 为 generic AoE。

        map.put(StatIds.INC_AREA_OF_EFFECT_IF_YOU_VE_KILLED_RECENTLY, self(StatConditionIds.COND_COUNT_KILL_RECENTLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        // 6) COND_LIVING_STATE_SURROUNDED（与 INC_*_WHILE_SURROUNDED 同族）+ generic AoE。

        map.put(StatIds.INC_SURROUNDED_AREA_OF_EFFECT, self(StatConditionIds.COND_LIVING_STATE_SURROUNDED, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        // 双手近战武器击晕近期 → generic AoE：COND_RECENTLY_CAUSED_STUN（CAUSED_AILMENT+stun 真 fire）+ COND_WIELDING_TWO_HANDED_MELEE（GateTag.TWO_HANDED 真读取）。

        map.put(StatIds.INC_AREA_OF_EFFECT_IF_YOU_VE_STUNNED_AN_ENEMY_WITH_A_TWO_HANDED_MELEE_WEAPON_RECENTLY,

                stat(List.of(StatConditionIds.COND_RECENTLY_CAUSED_STUN, StatConditionIds.COND_WIELDING_TWO_HANDED_MELEE),

                        StatTargetIds.TGT_INLINE_SELF, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT));

        // GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE → 镜像 FROM_CHAOS sibling（行 435 区）用 chaos 转伤 generic EFF。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_CHAOS_DAMAGE, self(StatEffectIds.EFF_CHAOS_DAMAGE_CONVERT_GAIN_TRUE));

        // GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE → 镜像 FROM_COLD sibling 用 cold 转伤 generic EFF。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE, self(StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        // GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE → 镜像 FROM_FIRE sibling 用 fire 转伤 generic EFF。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE, self(StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        // GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE → 镜像 FROM_LIGHTNING sibling 用 lightning 转伤 generic EFF。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        // GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE → 镜像 FROM_PHYSICAL sibling（行 515 EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_PHYSICAL_TRUE 同族）用 physical 转伤 generic EFF（行 508）。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        // EXTRA_PHYSICAL → GAIN_OF_DAMAGE_AS_EXTRA_PHYSICAL_DAMAGE 的语料异写（同义：Gain X% of Damage as Extra Physical Damage），复用同一转伤 EFF（StatEffectGen:625 真读 PHYSICAL→GAIN），R3 安全。

        map.put(StatIds.EXTRA_PHYSICAL, self(StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_PHYSICAL_TRUE));

        // BUFFS_ON_YOU_EXPIRE_SLOWER → 自身 buff 时长倍率（StatusEngine 中央 apply 按 StatusKind.BUFF 缩放），引擎真读 BUFF_DURATION_MULT，R3 安全。

        map.put(StatIds.BUFFS_ON_YOU_EXPIRE_SLOWER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_BUFF_DURATION_MULT));

        // DEBUFFS_ON_YOU_EXPIRE_FASTER → 自身 debuff 时长倍率（<1 缩短=更快消失），引擎真读 DEBUFF_DURATION_MULT，R3 安全。

        map.put(StatIds.DEBUFFS_ON_YOU_EXPIRE_FASTER, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DEBUFF_DURATION_MULT));

        // INC_BLIND_DURATION → 你施加的致盲时长倍率（StatusEngine 中央 apply 按 {blind, !taken} 缩放 AILMENT_DURATION），引擎真读，R3 安全。

        map.put(StatIds.INC_BLIND_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_BLIND));

        // INC_HINDER_DURATION → 你施加的阻碍时长倍率（StatusEngine 中央 apply 按 {hinder, !taken} 缩放 AILMENT_DURATION），引擎真读，R3 安全。

        map.put(StatIds.INC_HINDER_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_AILMENT_ATTRIBUTE_COE_AILMENT_DURATION_HINDER));

        // TO_ACCURACY_AGAINST_BLEEDING_ENEMIES → ADD_VALUE ACCURACY + COND_BLEED_STATUS_AGE。

        map.put(StatIds.TO_ACCURACY_AGAINST_BLEEDING_ENEMIES, onTarget(StatConditionIds.COND_BLEED_STATUS_AGE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        // SKILLS_GAIN_COMBO_SLOWER → 连击变慢：COMBO_SLOWDOWN 属性（百分比，base=0）经 ResourceRegistry.buildCombo 分数累积读点，

        // 每次构建累积 (1−slow) 层进度、满 1.0 才 +1 层（slow=0 行为不变；slow=0.5 等效每两次 +1），引擎真读，R3 安全。

        map.put(StatIds.SKILLS_GAIN_COMBO_SLOWER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_COMBO_SLOWDOWN));

        // WARCRIES_HAVE_A_MINIMUM_OF_POWER → 战吼最低威力：WARCRY_MIN_POWER 属性（base=0）经 SkillEvent.tryGrantWarcry 作强化攻击次数下限 floor，

        // 下限=0 行为不变，引擎真读，R3 安全。

        map.put(StatIds.WARCRIES_HAVE_A_MINIMUM_OF_POWER, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_WARCRY_MIN_POWER));

        // HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS → 生命药剂恢复重定向至盟友：FLASK_LIFE_RECOVERY_SHARE 属性（base=0）经 FlaskHelper 每 tick 对生命药剂额外治疗持有者盟友，

        // 重定向开关=0 行为不变（无该 Stat 时属性恒 0），引擎真读，R3 安全。

        map.put(StatIds.HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_FLASK_LIFE_RECOVERY_TO_COMPANIONS));

        // ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_... → 法术暴击破甲：ARMOR_BREAK_SPELL_CRIT_MAGNITUDE 属性（base=0）经 DamagePipeline.stagePostReduction

        // 在「法术 + 暴击」命中时把破甲值设为 值×本次物理伤害（覆盖常规公式）；攻击方 {armor_break} 作用域真读取，R3 安全。

        map.put(StatIds.ARMOR_BREAK_ARMOR_ON_CRITICAL_HIT_WITH_SPELLS_EQUAL_TO_OF_PHYSICAL_DAMAGE_DEALT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ARMOR_BREAK_SPELL_CRIT_MAGNITUDE));

        // 战斗状态 WHILE（自身状态）→ TGT_SELF_ONLY + 通用属性族

        map.put(StatIds.INC_ACCURACY_WHILE_MOVING, self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        // ── ：双持 / 持盾 / 形变 / 生命态 / 状态自作用 条件接线（纯数据 COND + 既有证明 EFF）──

        map.put(StatIds.INC_ACCURACY_WHILE_DUAL_WIELDING, self(StatConditionIds.COND_DUAL_WIELD_LIVING_STATE, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY));

        map.put(StatIds.OF_DAMAGE_TAKEN_RECOUPED_AS_HEALTH_WHILE_CHANNELLING, self(StatConditionIds.COND_CHANNELLING_LIVING_STATE, StatEffectIds.EFF_HEALTH_RECOUP));

        // 注：INC_DAMAGE_WHILE_YOU_HAVE_A_TOTEM 不接 —— EFF_DAMAGE_TOTEM 的 {TOTEM} 标签语义为“图腾自身伤害”，

        // 与“持图腾时自身伤害提升”不符（缺 presence 条件原语，见上文 companion/totem 推迟注释）→ 推迟至条件原语补齐。

        // ── "对低/满血敌人" 安全带：COND 原语(LivingStateValue) + TGT_COND_ON_TARGET 目标侧求值 ──

        // 机制同 DAMAGE_LUCKY_VS_LOW_LIFE（COND_LIVING_STATE_LOW_LIFE + TGT_COND_ON_TARGET 已证明可用）。

        // COND_FULL_LIFE_LIVING_STATE 与 COND_LIVING_STATE_LOW_LIFE 同构（StatConditionGen ，仅 LivingStateModes 枚举不同）

        // 故 FULL_LIFE 镜像同样走 TGT_COND_ON_TARGET 目标侧求值，对称性成立（纯数据条件 + 目标重定向）。

        // op 族：INC_* → ADD_MULTIPLIED_BASE；LUCKY 为 flag 效应无 op 期望；BUILDUP 走 AILMENT_BUILDUP 属性(STUN 标签)。

        map.put(StatIds.DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH, onTarget(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, StatEffectIds.EFF_DAMAGE_LUCKY));

        // ── 扩展 AGAINST/VS_<STATUS> 族（COND 原语 + TGT_COND_ON_TARGET，复用既有 base EFF，零新增 EFF 变体/零新增 COND）──

        // 全部为已接线族的镜像：base EFF 取自 ADD_AILMENT_CHANCE_MAIM/INC_CHANCE_TO_SHOCK/INC_IGNITE_BUFF_MAGNITUDE/INC_POISON_DURATION_TAKEN

        // 与 DAMAGE_LUCKY_VS_LOW_LIFE 同形；仅将条件原语切到对应状态、作用域走 TGT_COND_ON_TARGET（目标侧求值已证明可用）。

        // op 族：INC_* → ADD_MULTIPLIED_BASE；CHANCE/MAIM-CHANCE/SHOCK-CHANCE/LUCKY 为 ADD_VALUE 或 flag 效应（与基线同源，0 新增 CHECK-B）。

        // 1) LUCKY vs 重晕敌人 —— 1:1 镜像 DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_ENEMIES_THAT_ARE_ON_LOW_HEALTH（见上段）。

        map.put(StatIds.DAMAGE_WITH_HITS_IS_LUCKY_AGAINST_HEAVY_STUNNED_ENEMIES, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_DAMAGE_LUCKY));

        // ── 自侧 WHILE/ON 在场条件族（COND 原语 + TGT_SELF_ONLY/TGT_INLINE_SELF，复用既有 base EFF 精确镜像，零新增 EFF/零新增 COND）──

        // 由 harvest_conditions.py 数据驱动匹配：剥离条件子句后 base subject 必为已接线 StatIds，EFF 精确复用其基线注册，

        // 条件子句仅映射到已存在 COND 原语（leeching/shapeshifted/surrounded/low-life）。

        // 严格跳过 C5 缺口项：INC_STUN_THRESHOLD_*/INC_BLOCK_CHANCE_WHILE_* 的 base EFF 为 ADD_VALUE(flat)，与 INC_ 前缀 op 族不符 -> 属 Bucket E，不臆造接线。

        // 1) 变形时获得额外元素伤害（convert-gain 效应复用，TGT_SELF_ONLY）。

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_COLD_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_COLD_DAMAGE_CONVERT_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_FIRE_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_FIRE_GAIN_TRUE));

        map.put(StatIds.GAIN_OF_DAMAGE_AS_EXTRA_LIGHTNING_DAMAGE_WHILE_SHAPESHIFTED, self(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, StatEffectIds.EFF_DAMAGE_CONVERT_GAIN_LIGHTNING_TRUE));

        // 3) 对稀有/传奇敌人 INC 斩杀阈值 —— 复用 INC_CULLING_STRIKE_THRESHOLD 同源 CullingStatEffect，切 COND_PRESENCE_RARITY_RARE_UNIQUE + TGT_SELF_ONLY（ 同构）。

        map.put(StatIds.INC_CULLING_STRIKE_THRESHOLD_AGAINST_RARE_OR_UNIQUE_ENEMIES, self(StatConditionIds.COND_PRESENCE_RARITY_RARE_UNIQUE, StatEffectIds.EFF_CAUSED_HIT_CULLING));

        // 对被标记的敌人 —— INC 斩杀阈值（onTarget + COND_PRESENCE_MARK_STATUS + CullingStatEffect，镜像稀有/传奇斩杀；PRESENCE_MARK 由引擎对 Presence 内敌人持续施加，R3 安全）

        map.put(StatIds.CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK, onTarget(StatConditionIds.COND_PRESENCE_MARK_STATUS, StatEffectIds.EFF_CAUSED_HIT_CULLING));

        // INC_CULLING_STRIKE_THRESHOLD_AGAINST_IMMOBILISED_ENEMIES → 斩杀阈值提高针对被定身敌人（onTarget + COND_PIN_STATUS + 同源 CullingStatEffect，镜像 CULLING_STRIKE_AGAINST_ENEMIES_YOU_MARK；PIN 状态由引擎对受害方真施加，R3 安全）。

        map.put(StatIds.INC_CULLING_STRIKE_THRESHOLD_AGAINST_IMMOBILISED_ENEMIES, onTarget(StatConditionIds.COND_PIN_STATUS, StatEffectIds.EFF_CAUSED_HIT_CULLING));

        // HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE → 造成重晕的命中带斩杀（onTarget + COND_HEAVY_STUN_STATUS + 同源 CullingStatEffect）。

        // 范式同 GRANT_INTIMIDATE_ON_HEAVY_STUN：以「目标带 HEAVY_STUN 状态」判定替代逐命中重晕事件判定（引擎无 per-hit 重晕事件钩子），

        // HEAVY_STUN 由 AilmentHelper/StunMeter 真施加 + CullingStatEffect 真读，非死线。零新增 EFF/COND，R3 安全。

        map.put(StatIds.HITS_THAT_HEAVY_STUN_ENEMIES_HAVE_CULLING_STRIKE, onTarget(StatConditionIds.COND_HEAVY_STUN_STATUS, StatEffectIds.EFF_CAUSED_HIT_CULLING));

        // CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE → 对野兽且自身同伴在场时命中带斩杀。

        // onTarget：beast 门控按目标侧（敌）求值（EntityTypeValue 经 coe:beast datapack tag 延迟解析，R3 安全）；

        // 同条 when(COND_SOURCE_HAS_COMPANION_PRESENCE) 经 SourceScopeNode 切到自侧（玩家）查 summon 同伴在场，

        // 解决「目标侧条目上的自侧门控」双作用域（#33）。复用同源 CullingStatEffect，零新增机制。

        map.put(StatIds.CULLING_STRIKE_AGAINST_BEASTS_WHILE_YOUR_COMPANION_IS_IN_YOUR_PRESENCE,

                stat(List.of(StatConditionIds.COND_ENEMY_TYPE_BEAST, StatConditionIds.COND_SOURCE_HAS_COMPANION_PRESENCE),

                        StatTargetIds.TGT_COND_ON_TARGET, StatEffectIds.EFF_CAUSED_HIT_CULLING));

        // 对被标记的敌人 —— 无法造成暴击（onTarget + COND_PRESENCE_MARK_STATUS，复用 PINNED_ENEMIES_NO_CRIT 同源 EFF_HIT_FLAG_UNCRITTABLE）

        map.put(StatIds.ENEMIES_YOU_MARK_CANNOT_DEAL_CRITICAL_HITS, onTarget(StatConditionIds.COND_PRESENCE_MARK_STATUS, StatEffectIds.EFF_HIT_FLAG_UNCRITTABLE));

        // hybrid：全属性 +X（flat，三属性均为零基，故走 ADD_VALUE）

        map.put(StatIds.TO_ALL_ATTRIBUTES, groupSelf(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        // 「To any Attributes」= 语料异写，与 TO_ALL_ATTRIBUTES 同义同落点（三属性 ADD_VALUE 平加），R3 安全纯数据镜像。

        map.put(StatIds.TO_ANY_ATTRIBUTES, groupSelf(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        // hybrid：全属性 +X%（increased，复用 INC_STRENGTH/DEX/INT 同三 EFF 的 MULTIPLIED_TOTAL 变体，零新原语），R3 安全。

        map.put(StatIds.INC_ATTRIBUTES, groupSelf(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_STRENGTH, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_DEXTERITY, StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_INTELLIGENCE));

        // hybrid：+X 力量与敏捷（flat ADD_VALUE）

        map.put(StatIds.TO_STRENGTH_AND_DEXTERITY, groupSelf(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY));

        // hybrid：+X 力量与智慧（flat ADD_VALUE）

        map.put(StatIds.TO_STRENGTH_AND_INTELLIGENCE, groupSelf(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_STRENGTH, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        // hybrid：+X 敏捷与智慧（flat ADD_VALUE）

        map.put(StatIds.TO_DEXTERITY_AND_INTELLIGENCE, groupSelf(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_DEXTERITY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INTELLIGENCE));

        // 领域内友军造成伤害提高（target 走 PRESENCE_ALLIES）

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_DEAL_INC_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        // 领域内友军攻速提高

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_ATTACK_SPEED, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        // 领域内友军施法速度提高

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_CAST_SPEED, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED));

        // 领域内友军暴击率提高

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_CRITICAL_HIT_CHANCE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));

        // 领域内友军 +X 命中（flat ADD_VALUE）

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_TO_ACCURACY, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ACCURACY));

        // 领域内友军荣耀获取提高

        map.put(StatIds.ALLIES_IN_YOUR_PRESENCE_HAVE_INC_GLORY_GENERATION, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_PRESENCE_GRANT_COE_GLORY_GENERATION));

        // 变形中：存在范围内的友军获得猛攻 —— 集合关系（在范围内且持有者变形中即拥有，离开范围/变回人形即失效）。
        // 猛攻语料无数值（「have Onslaught」是二元存在），故数量在接线层固定为默认 20 层：
        // 猛攻现在按层数计量（每层 +1% 攻击速度 / +1% 施法速度 / +0.5% 移动速度，无上限），
        // 20 层 ⇒ 双速 +20% / 移速 +10%；EFF_PRESENCE_AURA_ONSLAUGHT 的 aspect 已改为 STACKS（条目数值 = 层数），
        // 故这里必须写 20（层数）而非强度百分比——沿用天赋节点给的 1.0 会按 1 层生效（静默失效）。
        map.put(StatIds.COMPANIONS_IN_YOUR_PRESENCE_HAVE_ONSLAUGHT_WHILE_YOU_ARE_SHAPESHIFTED,
                StatBuilder.effect(StatEffectIds.EFF_PRESENCE_AURA_ONSLAUGHT)
                        .self()
                        .when(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED)
                        .amount(ExpressionCodec.of(20.0))
                        .build()
                        .withConditionRef(StatConditionIds.presenceId(PresenceCountValue.Relation.ALLY)));

        // 领域内敌人冷却回复降低（语料为 reduced，值已取负）

        map.put(StatIds.ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_COOLDOWN_RECOVERY_RATE, presence(PresenceCountValue.Relation.ENEMY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_COOLDOWN_RECOVERY_RATE));

        // 领域内敌人生命回复降低（语料为 reduced，值已取负）

        map.put(StatIds.ENEMIES_IN_YOUR_PRESENCE_HAVE_INC_HEALTH_REGENERATION_RATE, presence(PresenceCountValue.Relation.ENEMY, StatEffectIds.EFF_PRESENCE_GRANT_COE_REGENERATION_RATE));

        // 幸运：偏斜几率幸运（复用 EFF_DEFLECT_LUCKY）

        map.put(StatIds.CHANCE_TO_DEFLECT_IS_LUCKY, self(StatEffectIds.EFF_DEFLECT_LUCKY));

        // 幸运：闪避几率不幸（复用 EFF_EVADE_LUCKY_UNLUCKY）

        map.put(StatIds.CHANCE_TO_EVASION_IS_UNLUCKY, self(StatEffectIds.EFF_EVADE_LUCKY_UNLUCKY));

        // 反向幸运：受害方持有「X% chance for Damage of Enemies that Hit you to be Unlucky」→ 攻击方伤害 UNLUCKY。

        // 走独立 VICTIM_DAMAGE 维度（不与持有者进攻性 DAMAGE_LUCKY 碰撞）；读点见 DamageHelper#calculateDamageDeviation（受害方持有）。

        map.put(StatIds.CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY, self(StatEffectIds.EFF_VICTIM_DAMAGE_UNLUCKY));

        // 引导 N 秒后施加诅咒：零基属性 ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS（值=秒），SkillInstance#tick 真读触发（R3 安全，非死线）。

        map.put(StatIds.ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS, self(StatConditionIds.COND_CHANNELLING_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ADD_A_CURSE_AFTER_CHANNELLING_FOR_SECONDS));

        // 处决：弓类攻击带处决（无数值，属标志型效果）

        map.put(StatIds.BOW_ATTACKS_HAVE_CULLING_STRIKE, self(StatEffectIds.EFF_CULLING_BOW));

        // 投掷技能范围

        map.put(StatIds.INC_GRENADE_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_GRENADE));

        // 危害区域范围

        map.put(StatIds.INC_HAZARD_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AREA_OF_EFFECT_HAZARD));

        // 「危害物触发后有 X% 几率重新布置」——零基几率属性 HAZARD_REARM_CHANCE（base=0 → ADD_VALUE 平加）。

        // 消费端 行为组件 contact 的 rearm 判定（:89）真读并按 chance 掷骰，R3 安全。

        // 注：本效果走属性名授予，故词缀 / 传奇等所有来源均可取用。

        map.put(StatIds.HAZARDS_HAVE_CHANCE_TO_REARM_AFTER_THEY_ARE_TRIGGERED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_HAZARD_REARM_CHANCE));

        // 领域范围提高（PRESENCE_RANGE，base=6 非零→BASE）

        map.put(StatIds.INC_PRESENCE_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PRESENCE_RANGE));

        // 图腾放置范围提高（PRESENCE_RANGE，复用 INC_PRESENCE_AREA_OF_EFFECT 同源 EFF；EntityOperationEvent:85 真读持有者 presence_range 属性，R3 安全）

        map.put(StatIds.INC_TOTEM_PLACEMENT_RANGE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PRESENCE_RANGE));

        // 图腾数量上限翻倍（+1 到施法者 MAX_TOTEMS，base=1 非零基→ADD_VALUE；SpawnAction.canSpawnTotem 真读该属性并强制上限，R3 安全）

        map.put(StatIds.YOUR_TOTEM_LIMIT_IS_DOUBLED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_TOTEMS));

        // Orb 技能 +X 到上限（MAX_ORB，base=1 非零基→ADD_VALUE；OrbDeliveryExecutor.canSpawnOrb 真读该属性并强制上限，R3 安全）

        map.put(StatIds.ORB_SKILLS_HAVE_TO_LIMIT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_ORB));

        // 近战攻击技能 +X 到图腾数量上限（MAX_TOTEMS，base=1 非零基→ADD_VALUE；引擎所有 SkillTotem 共用单一全局图腾池，canSpawnTotem 真读，R3 安全）

        map.put(StatIds.MELEE_ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_TOTEMS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_TOTEMS));

        // 攻击技能 +X 到弩图腾数量上限（ballista 图腾同属 SkillTotem、计入同一全局池；复用 MAX_TOTEMS，R3 安全）

        map.put(StatIds.ATTACK_SKILLS_HAVE_TO_MAX_NUMBER_OF_SUMMONED_BALLISTA_TOTEMS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_TOTEMS));

        // 已放置战旗数量上限（MAX_BANNERS，base=1 非零基→ADD_VALUE；BannerAction.canPlaceBanner 真读该属性并强制上限，R3 安全）

        map.put(StatIds.TO_MAX_NUMBER_OF_PLACED_BANNERS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_BANNERS));

        // 同伴数量上限 +1（MAX_COMPANIONS，base=1 非零基→ADD_VALUE；SpawnAction.canSpawnCompanion 真读该属性，R3 安全）。

        // 语料「two Companions of different types」的「不同类型」约束由 canSpawnCompanion 按 EntityType 判重实现

        // （POE2 同伴按种类唯一；模组以 EntityType 作为「种类」的自然标识，与 summon 路径同源，无需额外类型字段）。

        // 本条只负责把数量上限由 1 抬到 2，类型判重无条件生效、与上限正交。

        map.put(StatIds.YOU_CAN_HAVE_TWO_COMPANIONS_OF_DIFFERENT_TYPES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MAX_COMPANIONS));

        // 临时召唤物技能 +X 到已召唤数量上限（MINION_LIMIT，base=1 非零基→ADD_VALUE；SpawnAction.canSpawnMinion 真读该属性，R3 安全）。

        // 语义：全局池、base=1，镜像图腾/同伴范式；仅约束<b>非同伴</b>召唤物——同伴走 MAX_COMPANIONS 独立计数（见 canSpawnMinion 排除）。

        map.put(StatIds.TEMPORARY_MINION_SKILLS_HAVE_TO_LIMIT_OF_MINIONS_SUMMONED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MINION_LIMIT));

        // 残迹效果提高（REMNANT_MAGNITUDE，base=1 非零→BASE）

        map.put(StatIds.REMNANTS_YOU_CREATE_HAVE_INC_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_REMNANT_MAGNITUDE));

        // 残迹二次触发几率（REMNANT_DOUBLE_CHANCE，base=0 → ADD_VALUE）

        map.put(StatIds.CHANCE_FOR_REMNANTS_YOU_CREATE_TO_GRANT_THEIR_EFFECTS_TWICE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_DOUBLE_CHANCE));

        // 残迹元素互换三兄弟（零基 flag，创建点 RemnantState#remapElement 真读；R3 安全）

        map.put(StatIds.CREATE_FIRE_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_COLD, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_COLD_TO_FIRE).self().amount("1.0").build());

        map.put(StatIds.CREATE_COLD_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_LIGHTNING, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_LIGHTNING_TO_COLD).self().amount("1.0").build());

        map.put(StatIds.CREATE_LIGHTNING_ELEMENTAL_INFUSION_REMNANTS_INSTEAD_OF_FIRE, StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_REMNANT_FIRE_TO_LIGHTNING).self().amount("1.0").build());

        // 击杀爆炸几率（燃烧中 / 强化攻击）：读点 DamageEvent#tryBurstOnKill —— 条件走「燃烧状态 + 强化攻击标记」判定，

        // 数值走百分比掷骰（非硬编码 flag），伤害 = 受害者最大生命 × 1/10，锁敌（`Camp.PLAYER` 排除非敌对）。

        map.put(StatIds.BURNING_ENEMIES_YOU_KILL_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_BURNING_EXPLODE_ON_KILL_CHANCE));

        map.put(StatIds.ENEMIES_YOU_KILL_WITH_EMPOWERED_ATTACKS_HAVE_A_CHANCE_TO_EXPLODE_DEALING_A_TENTH_OF_THEIR_MAX_HEALTH_AS_FIRE_DAMAGE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EMPOWERED_EXPLODE_ON_KILL_CHANCE));

        // 收集元素灌入时按几率额外获得一层同种灌入：百分比掷骰在 InfusionHelper#gain 授予点真读（R3 安全）。

        map.put(StatIds.CHANCE_WHEN_COLLECTING_AN_ELEMENTAL_INFUSION_TO_GAIN_AN_ADDITIONAL_ELEMENTAL_INFUSION_OF_THE_SAME_TYPE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ADDITIONAL_INFUSION_CHANCE));

        // 弩装填即时完成几率：百分比掷骰在 ProjectileInEnchant#modifyCrossbowChargingTime 真读（R3 安全）。

        map.put(StatIds.CHANCE_WHEN_YOU_RELOAD_A_CROSSBOW_TO_BE_IMMEDIATE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CROSSBOW_IMMEDIATE_RELOAD_CHANCE));

        // 「消耗豁免」族：技能消耗充能球 / 元素灌入时按几率不扣除、但仍计为已消耗（事件照常发射）。

        // 读点分别为 ChargedState#consume（充能扣除唯一通道）与 InfusionHelper#consumeAny（技能资源路径灌入消耗唯一入口），R3 安全。

        map.put(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_CHARGES_BUT_STILL_COUNT_AS_CONSUMING_THEM, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CHARGE_CONSUME_SKIP_CHANCE));

        map.put(StatIds.SKILLS_HAVE_CHANCE_TO_NOT_ADD_ELEMENTAL_INFUSIONS_BUT_STILL_COUNT_AS_CONSUMING_THEM, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INFUSION_CONSUME_SKIP_CHANCE));

        // 元技能（Invocation / Trigger）能量族：被调用法术按几率半耗；触发技能按几率返还一半已耗能量。

        // 读点均为 MetaSkillAction#execute 的能量消耗点（唯一 invocation 能量消费者），R3 安全。

        map.put(StatIds.INVOKED_SPELLS_HAVE_CHANCE_TO_CONSUME_HALF_AS_MUCH_ENERGY, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INVOKED_HALF_ENERGY_CHANCE));

        map.put(StatIds.CHANCE_FOR_TRIGGER_SKILLS_TO_REFUND_HALF_OF_ENERGY_SPENT, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TRIGGER_ENERGY_REFUND_CHANCE));

        // 命中「忽略非负元素抗性」（对冰冻敌人）：零基标记属性，条件挂 conditionTarget=冰冻状态

        // （属性 id 不含条件词，归一化 #55）；读点 DamageHelper#applyDamageReductions 以 readInScopeAgainst

        // 绑定受害者求值，命中方持标记时有效抗性取 min(eff, 0)，R3 安全。

        map.put(StatIds.HIT_DAMAGE_IGNORE_NON_NEGATIVE_ELEMENTAL_DAMAGE_RESISTANCES_OF_FROZEN_ENEMIES,

                onTarget(StatConditionIds.COND_FREEZE_STATUS, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_IGNORE_NON_NEGATIVE_ELEMENTAL_RESISTANCE));

        // 命中「按几率把敌方怪物元素抗性取反」：百分比掷骰在 DamageHelper#applyDamageReductions 真读；

        // 「敌方怪物」由受害方阵营（Camp.MOB）在引擎侧门控，非属性 id 承载（归一化），R3 安全。

        map.put(StatIds.HIT_DAMAGE_HAVE_CHANCE_TO_TREAT_ENEMY_MONSTER_ELEMENTAL_DAMAGE_RESISTANCES_VALUES_AS_INVERTED,

                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_INVERT_ELEMENTAL_RESISTANCE_CHANCE));

        // 生命偷取共享给同伴：零基标记属性，读点 DamageHelper#applyLeechResource（生命资源写入后按同量写给同伴），R3 安全。

        map.put(StatIds.LEECHING_HEALTH_FROM_YOUR_HIT_DAMAGE_CAUSES_YOUR_COMPANION_TO_ALSO_LEECH_THE_SAME_AMOUNT_OF_HEALTH,

                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_COMPANION_LEECH_SHARE).self().amount("1.0").build());

        // ≥2 种不同活跃奉献时的 inc 随从伤害：数值走 OFFERING_DAMAGE_BONUS（零基百分比），

        // 「≥2 种奉献」门控在 OfferingAction 施加点（countDistinctTypes ≥ 2）真读；原硬编码 +50% 已移除（R3 安全）。

        map.put(StatIds.INC_MINION_DAMAGE_WHILE_YOU_HAVE_AT_LEAST_TWO_DIFFERENT_ACTIVE_OFFERINGS,

                self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_OFFERING_BONUS_MINION_DAMAGE));

        // 图腾每个保留 X 精魂：零基平值属性，SpiritManager#tickPlayer 按在场图腾数覆盖写入 ReservationManager（R3 安全）。

        map.put(StatIds.TOTEMS_RESERVE_SPIRIT_EACH, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_TOTEM_SPIRIT_RESERVATION));

        // INC_AREA_OF_EFFECT 的 TOTEM 作用域变体

        map.put(StatIds.INC_AREA_OF_EFFECT_FOR_SKILLS_USED_BY_TOTEMS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_TOTEM));

        // INC_PROJECTILE_SPEED 的 SPELL 作用域变体

        map.put(StatIds.INC_PROJECTILE_SPEED_FOR_SPELL_SKILLS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_PROJECTILE_SPEED_PROJECTILE_SPELL));

        // INC_ACCURACY 的 MELEE 作用域变体（复用既有 EFF）

        map.put(StatIds.INC_ACCURACY_WITH_TWO_HANDED_MELEE_WEAPONS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_ACCURACY_MELEE));

        // INC_AREA_OF_EFFECT 的 BANNER 作用域变体

        map.put(StatIds.BANNER_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_BANNER));

        // INC_AREA_OF_EFFECT 的 COMPANION 作用域变体

        map.put(StatIds.COMPANIONS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_COMPANION));

        // INC_AREA_OF_EFFECT 的 HERALD 作用域变体

        map.put(StatIds.HERALD_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_HERALD));

        // INC_AREA_OF_EFFECT 的 OFFERING 作用域变体

        map.put(StatIds.OFFERING_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_OFFERING));

        // 近期创造的祭品无法被伤害：零基 flag 属性 + EFF 平加，OfferingSpike#hurtServer 真读 ≥1.0 即生效（R3 安全，非死线）。

        map.put(StatIds.OFFERINGS_CANNOT_BE_DAMAGED_IF_THEY_HAVE_BEEN_CREATED_RECENTLY, StatBuilder.effect(StatEffectIds.EFF_MARKER_OFFERING_RECENT_IMMUNE).self().amount("1.0").build());

        // 免疫元素要害诅咒：不建属性，走既有中央免疫通道 ImmuneStatEffect

        // （StatusEngine.apply → StatEngine.isImmune 按 StatusType.id 短路）；诅咒「技能即状态」的

        // StatusType 已在 StatusDefs 静态预注册（StatusType.dynamic 幂等取既有实例，运行时行为不变）。

        map.put(StatIds.UNAFFECTED_BY_ELEMENTAL_WEAKNESS, StatBuilder.effect(StatEffectIds.EFF_IMMUNE_CURSE_ELEMENTAL_WEAKNESS).self().amount("1.0").build());

        // INC_AREA_OF_EFFECT 的 SPELL 作用域变体

        map.put(StatIds.SPELL_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_SPELL));

        // INC_AREA_OF_EFFECT 的 WARCRY 作用域变体

        map.put(StatIds.WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_WARCRY));

        // ===================== INC_AREA_OF_EFFECT 的新增技能标签变体 =====================

        // 「Detonator Skills have X% increased Area of Effect」——{DETONATOR}（标签 live，见 WireDamage 注释）。

        map.put(StatIds.DETONATOR_SKILLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_DETONATOR));

        // 「Echoed Spells have X% increased Area of Effect」——{ECHO, SPELL}。

        // 标签已活化：Echo 重复机制落地，echo 标签由 SkillStatScopes#enterEcho 发射，

        // 仅在回响重放的那一次施放期间携带；spell 标签由外层技能作用域提供，二者合并后与本条目的

        // {ECHO, SPELL} 匹配。

        map.put(StatIds.ECHOED_SPELLS_HAVE_INC_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_ECHO_SPELL));

        // 「X% increased Area of Effect of Ancestrally Boosted Attacks」——{ANCESTRAL_BOOST, ATTACK}（标签惰性，同上）。

        // 天赋节点 ancestral_boosted_area_and_damage 已存在，待 Ancestral Boost 机制落地即生效。

        map.put(StatIds.INC_AREA_OF_EFFECT_OF_ANCESTRALLY_BOOSTED_ATTACKS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_ANCESTRAL_BOOST_ATTACK));

        // 「X% increased Parry Hit Damage Area of Effect」——{PARRY}（标签 live，见 WireDamage#140 的 DAMAGE_DAMAGE_PARRY {damage,parry} 同族）。

        map.put(StatIds.INC_PARRY_HIT_DAMAGE_AREA_OF_EFFECT, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_EFFECT_AREA_PARRY));

        // === 距离簇接线（到最近敌对实体距离门控；阈值取自条目 stat_range_m 参数）===

        // 阈值语义：PoE2 源数据中 within/further_than 的距离为词条 $value 外的第二项参数；本架构单值条目下

        // 以 stat_range_m 参数承载该距离（bindParams 注入条件作用域，VarRef 读取），按源数据常见值取 2m/3m/6m。

        // 投影物伤害：近身(2m)/远距(6m) 双变体（onTarget：条件按敌人侧求值 → 敌我互距）

        map.put(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_WITHIN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        map.put(StatIds.PROJECTILES_DEAL_INC_DAMAGE_WITH_HITS_AGAINST_ENEMIES_FURTHER_THAN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE)

                        .self().when(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(6.0)).build());

        // 投影物暴击伤害加成：近身(2m)/远距(6m) 双变体

        map.put(StatIds.PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_WITHIN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        map.put(StatIds.PROJECTILES_HAVE_INC_CRITICAL_DAMAGE_BONUS_AGAINST_ENEMIES_FURTHER_THAN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE)

                        .self().when(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(6.0)).build());

        // 投影物暴击几率（远距 6m）

        map.put(StatIds.PROJECTILES_HAVE_INC_CRITICAL_HIT_CHANCE_AGAINST_ENEMIES_FURTHER_THAN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE)

                        .self().when(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(6.0)).build());

        // 投影物穿透几率（3m 内，源数据 "within 3m distance of you"）

        map.put(StatIds.CHANCE_FOR_PROJECTILES_TO_PIERCE_ENEMIES_WITHIN_XM_DISTANCE_OF_YOU,

                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_PROJECTILE_PIERCE_CHANCE_PROJECTILE)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(3.0)).build());

        // 近距离（Close Range）：命中精准 / 近战伤害 / 击退几率。

        // PoE2 官方关键词 "Close Range" = 「距角色 2 米内」。

        // 本簇 within/further 字面阈值须与此统一取 2.0——否则 nearby_enemy_count:2 的

        // 「每个近距离敌人」缩放族与门控口径不自洽。

        map.put(StatIds.INC_ACCURACY_AT_CLOSE_RANGE,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_ACCURACY)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        map.put(StatIds.INC_MELEE_DAMAGE_WITH_HIT_DAMAGE_AT_CLOSE_RANGE,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_MELEE)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        map.put(StatIds.CHANCE_TO_KNOCK_ENEMIES_BACK_WITH_HIT_DAMAGE_AT_CLOSE_RANGE,

                StatBuilder.effect(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_KNOCKBACK_CHANCE_KNOCKBACK)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        // 距离门控一律走 self()（conditionTarget=SELF=玩家）：DistanceToNearestEnemyValue 以「被求值实体」为中心，

        // 故须由玩家侧求距（玩家→最近敌人）才符合「距敌人 Xm」语义；onTarget 会以敌人为中心 → 误测敌→敌距离。

        // 效果仍作用于玩家自身属性（TGT_SELF_ONLY / target=null 与 onTarget 同效），仅修正距离中心。

        // 眩晕蓄积（通用 STUN 标签，base=1）：within 2m

        //（PoE2 源 "40% increased Stun Buildup against enemies within 2 metres"）。

        map.put(StatIds.INC_STUN_BUILDUP_AGAINST_ENEMIES_WITHIN_METRES,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_AILMENT_BUILDUP_STUN)

                        .self().when(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(2.0)).build());

        // ── CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM（纯数据·距离门控，零新增 COND/EFF）

        // 复用 EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS（GrantOnEventStatEffect：onTrigger 读 entry.amount() 作触发概率，

        // CAUSED_HIT 事件触发时对 triggerTarget 施加 DAZE 状态；amount=$value=25% 即触发几率，live 非死线）。

        // 加 self(COND_FURTHER_THAN_RANGE_OF_ENEMY).param("stat_range_m", 6.0) 距离门控（PoE2 源 "further than 6m"）。

        // 距离中心取 SELF（玩家→最近敌人），与全簇一致；DAZE 由 CAUSED_HIT 通道授予，机制完备。

        map.put(StatIds.CHANCE_TO_INFLICT_DAZE_WITH_HIT_DAMAGE_AGAINST_ENEMIES_FURTHER_THAN_XM,

                StatBuilder.effect(StatEffectIds.EFF_CAUSED_HIT_DAZE_GRANT_ON_EVENT_STATUS)

                        .self().when(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(6.0)).build());

        // ── GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY（复用 CAUSED_AILMENT+heavy_stun 事件，CHARGE 授予 ENDURANCE）

        // 引擎读点：AilmentHelper.applyHeavyStun 经 fireAilment 携带 caused_ailment+heavy_stun 标签的 scope fire，

        // GrantOnEventStatEffect(CHARGED) 在持有者（造成重晕方）侧真授予耐力球；rarity 门控现用 COND_TARGET_RARITY_RARE_UNIQUE

        // （TargetRarityValue 解析被重晕事件目标的 rarity=稀有/传奇，精确命中语料「heavy stun a rare or unique enemy」）。

        map.put(StatIds.GAIN_AN_ENDURANCE_CHARGE_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY,

                StatBuilder.effect(StatEffectIds.EFF_CAUSED_AILMENT_HEAVY_STUN_GRANT_ENDURANCE_CHARGE)

                        .self().when(StatConditionIds.COND_TARGET_RARITY_RARE_UNIQUE).build());

        // ── RECOVER_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY

        // 复用既已 live 的 EFF_CAUSED_AILMENT_HEAL_HEALTH_HEAVY_STUN（ResourceStatEffect HEAL health，

        // CAUSED_AILMENT+heavy_stun 事件触发；RECOVER_LIFE_HEAVY_STUN/HEAVYSTUN 已证明该通道 live，R3 安全），

        // 仅补 rarity 门控 COND_TARGET_RARITY_RARE_UNIQUE（TargetRarityValue 解析被重晕事件目标的 rarity=稀有/传奇，

        // 与耐力球变体同口径，精确命中语料）。纯 data 接线，零新增 EFF/引擎原语。

        map.put(StatIds.RECOVER_OF_MAX_HEALTH_WHEN_YOU_HEAVY_STUN_A_RARE_OR_UNIQUE_ENEMY,

                StatBuilder.effect(StatEffectIds.EFF_CAUSED_AILMENT_HEAL_HEALTH_HEAVY_STUN)

                        .self().when(StatConditionIds.COND_TARGET_RARITY_RARE_UNIQUE).build());

        // ── THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED

        // 归一（#55）：原「零基 flag 属性 + AilmentHelper#applyHeavyStun 硬读」转 fire 通道 GrantOnEventStatEffect(ANCESTRAL_BOOST)。

        // 事件 caused_ailment+heavy_stun（owner=造成重晕方 caster，applyHeavyStun:760 fireAilment 发射）真触发；

        // AncestralBoostAttacks.grant(caster,1) 复用 live 计数 + DamageHelper#consumeOne 消耗，零新增原语。

        // rarity 门控改用 COND_TARGET_RARITY_RARE_UNIQUE（TargetRarityValue 解析被重晕事件目标的 rarity=稀有/传奇，

        // 与耐力球/回血变体同口径，精确命中语料）。amount=1.0 → 触发必发生。非死线。

        map.put(StatIds.THE_NEXT_ATTACK_YOU_USE_WITHIN_SECONDS_AFTER_HEAVY_STUNNING_A_RARE_OR_UNIQUE_ENEMY_IS_ANCESTRALLY_BOOSTED,

                StatBuilder.effect(StatEffectIds.EFF_CAUSED_AILMENT_HEAVY_STUN_GRANT_ANCESTRAL_BOOST)

                        .self().when(StatConditionIds.COND_TARGET_RARITY_RARE_UNIQUE).amount("1.0").build());

        // ── CHANCE_TO_GAIN_ARCANE_SURGE_WHEN_YOU_DEAL_A_CRITICAL_HIT（复用 CAUSED_CRIT 事件 + 既有 ARCANE_SURGE 状态）

        // 引擎读点：DamagePipeline:718 暴击时 Event.record(attacker, CAUSED_CRIT) 已 fire；GrantOnEventStatEffect(STATUS) 在持有者侧真授予

        // StatusDefs.ARCANE_SURGE（magnitude 1 / 100ticks），该状态已注册且由 COND_ARCANE_SURGE_STATUS 等真实读取，R3 安全。

        // 条目 amount 默认 $value = 触发几率（0~1），无需 amountIsMagnitude。纯增量、零新增 COND。

        map.put(StatIds.CHANCE_TO_GAIN_ARCANE_SURGE_WHEN_YOU_DEAL_A_CRITICAL_HIT,

                StatBuilder.effect(StatEffectIds.EFF_CHANCE_TO_GAIN_ARCANE_SURGE_ON_CRIT).self().build());

        // ── 矛类投影物「空场增伤」（距离簇补齐，新增 PROJECTILE+SPEAR 作用域 EFF）

        // 语料原文：「40% increased Projectile Damage with Spears while there are no Enemies within 3m」。

        // 「3m 内无敌人」= 最近敌人距离 > 3m，正是 COND_FURTHER_THAN_RANGE_OF_ENEMY 的语义

        //（DistanceToNearestEnemyValue 空场返回 NO_ENEMY_SENTINEL=1e9 ⇒ 无敌人时条件为真，与「no Enemies」一致，

        // 不需要额外的「空场」条件原语）。距离中心取 SELF，与全簇一致。

        // EFF 走 PROJECTILE+SPEAR 组合（GateExpr{DAMAGE, PROJECTILE, SPEAR}，镜像 MELEE_SPEAR），

        // 不退接较宽的 PROJECTILE——那会放大全部投影物伤害。

        map.put(StatIds.INC_PROJECTILE_DAMAGE_WITH_SPEARS_WHILE_THERE_ARE_NO_ENEMIES_WITHIN_XM,

                StatBuilder.effect(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE_DAMAGE_PROJECTILE_SPEAR)

                        .self().when(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY)

                        .param("stat_range_m", ExpressionCodec.of(3.0)).build());

        // WARCRIES_HAVE_CHANCE_TO_EMPOWERED_ADDITIONAL_ATTACKS → 复用 WARCRY 作用域增攻 EFF（SkillEvent:202 真读 {warcry}）

        map.put(StatIds.WARCRIES_HAVE_CHANCE_TO_EMPOWERED_ADDITIONAL_ATTACKS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_EMPOWERED_ATTACKS_TAKEN_WARCRY));

        // CHANCE_FOR_SPELL_SKILLS_TO_FIRE_ADDITIONAL_PROJECTILES：语料是<b>双参数</b>（几率 + 个数）

        // → groupSelf 挂 2 个 EFF，各领一个 base=0 属性；读点落在
        // ProjectileDeliveryExecutor#chanceExtraProjectiles（技能侧真实消费点，经
        // StatScopes.enterMerged 并入技能标签，非法术技能不会误吃本加成）。

        // 陷阱：ProjectileHelper#getArrowCount / #getBallCount 全仓<b>无调用方</b>，不可作为读点；

        // 另本条 EFF 的 GateExpr 必须是活跃读点作用域 {projectile,arrow,bow|crossbow} 的子集，否则永不命中；

        // 「{X}% chance」是 0~1 小数，不可 ADD_VALUE 写进<b>整数个数</b>属性。

        map.put(StatIds.CHANCE_FOR_SPELL_SKILLS_TO_FIRE_ADDITIONAL_PROJECTILES, groupSelf(

                StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SPELL_ADDITIONAL_PROJECTILE_CHANCE,

                StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SPELL_ADDITIONAL_PROJECTILE_COUNT));

        // PROJECTILE_ATTACKS_HAVE_A_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING → 移动中按几率 +2 投射物。

        // 读点 ProjectileInEnchant#modifyProjectileCount（mixin 到 EnchantmentHelper.processProjectileCount，

        // 是当前全仓<b>唯一活跃</b>的额外投射物读点——ProjectileHelper#getArrowCount 无调用方，属死代码，勿接）。

        // 「while moving」在读点侧按水平移动向量判定（与 LivingStateValue#moving 同源），

        // <b>不</b>走 stat COND：额外投射物数量在发射瞬间结算，条件改由 stat 门控会缺失作用域。

        map.put(StatIds.PROJECTILE_ATTACKS_HAVE_A_CHANCE_TO_FIRE_TWO_ADDITIONAL_PROJECTILES_WHILE_MOVING,

                self(StatConditionIds.COND_MOVING_LIVING_STATE, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MOVING_ADDITIONAL_PROJECTILE_CHANCE));

        // 「超越几率」额外发射投射物：与「移动中」同款零基 ADD_VALUE EFF（领 SURPASS_ADDITIONAL_PROJECTILE_CHANCE）；

        // 读点 ProjectileInEnchant#modifyProjectileCount 按 floor+frac 产出额外投射物（>100% 保证 +1 再掷小数）。

        map.put(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_ARROW, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SURPASS_ADDITIONAL_PROJECTILE_CHANCE));

        map.put(StatIds.SURPASS_CHANCE_TO_FIRE_AN_ADDITIONAL_PROJECTILE, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SURPASS_ADDITIONAL_PROJECTILE_CHANCE));

        // 「你标记的敌人附近的敌人被致盲」：零基 ADD_VALUE flag 属性（领 MARK_BLINDED）→ MarkBlindAura#tick 每轮

        // 找「我标记的敌人」(PRESENCE_MARK·source=我) → 其周边敌对实体施加 BLIND；全现成基元，R3 安全。

        map.put(StatIds.ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_MARK_BLINDED));

        // 诅咒激活延迟·翻倍：零基 ADD_VALUE 领 CURSE_ACTIVATION_DELAY_MORE +1.0（×2）；CurseActivation#enqueue 读点缩放延迟。

        map.put(StatIds.DOUBLE_ACTIVATION_DELAY_OF_CURSES, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CURSE_ACTIVATION_DELAY_MORE));

        // 诅咒激活频率提升（更快施放）：零基 ADD_VALUE 领 CURSE_ACTIVATION_FREQUENCY 加百分比；延迟 /(1+频率)。

        map.put(StatIds.FASTER_CURSE_ACTIVATION, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_CURSE_ACTIVATION_FREQUENCY));

        // 「调用技能改为每 X 秒触发链接法术」：INERT 标记承载触发间隔秒数（amount=节点值，规范 2.0），

        // 消费端 InvocationTicker#tickAll 经 StatEngine.markerMagnitude 真读，按秒数折算 tick 周期触发元技能链接的法术。

        map.put(StatIds.INVOCATION_SKILLS_INSTEAD_TRIGGER_SPELLS_EVERY_SECONDS, self(StatEffectIds.EFF_INVOCATION_TRIGGER_INTERVAL));

        // 弹药有概率不消耗（StatIds.BOLTS_FIRED_BY_CROSSBOW_...）：INERT 标记承载不消耗概率（$value，语料 0.1），

        // 武器类别作用域烘焙在标记实例（GateTag.CROSSBOW），「近期装填过」走条目条件 COND_RECENTLY_RELOADED；

        // 消费端 ProjectileHelper#useAmmo 经 StatEngine.markerMagnitude 真读（读点按持有武器类别传标签集）。

        map.put(StatIds.BOLTS_FIRED_BY_CROSSBOW_ATTACKS_HAVE_CHANCE_TO_NOT_EXPEND_AMMUNITION_IF_YOU_VE_RELOADED_RECENTLY, self(StatConditionIds.COND_RECENTLY_RELOADED, StatEffectIds.EFF_AMMO_NOT_EXPENDED_CHANCE));

        // 消耗敌人某异常时按概率重新施加（StatIds.CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT）：INERT 标记承载重施概率（$value），

        // 异常身份烘焙在标记实例（StatusDefs.SHOCK.id），消费端 StatusEngine#consumeAilment 经 StatEngine.markerMagnitude 真读（当前无调用方，机制就位待接入消费技能）。

        map.put(StatIds.CHANCE_ON_CONSUMING_A_SHOCK_ON_AN_ENEMY_TO_REAPPLY_IT, self(StatEffectIds.EFF_AILMENT_REAPPLY_ON_CONSUME));

        // 「生命偷取效果在未保留生命填满时不被移除」（StatIds.HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED）：

        // INERT 门控标记（amount=节点值，≥1 即生效），资源身份烘焙在 EFF 实例 conditionTags={health}，

        // 消费端 ResourceEventHub#onHeal 步骤 3 真读——资源已满时若「该资源有保留 且 未保留部分已满」则

        // 跳过 CALLBACK.onResourceFull（保留偷取实例）。通用机制，不焊死生命；无属性。

        map.put(StatIds.HEALTH_LEECH_EFFECTS_ARE_NOT_ADD_WHEN_RESERVATION_HEALTH_IS_FILLED,

                self(StatEffectIds.EFF_LEECH_RETAIN_WHILE_UNRESERVED_FULL));

        // 「忽略全部来自已装备护甲的移动速度惩罚」（StatIds.IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR）：

        // INERT 门控标记（amount=节点值，≥1 即生效），消费端 AttributeLimiterHelper#applyItemLimiter

        // 在产出「装备来源移速惩罚」条目时真读——命中则整条跳过，护甲只是该设施的第一个来源；无属性。

        map.put(StatIds.IGNORE_ALL_MOVEMENT_PENALTIES_FROM_EQUIP_ARMOR,

                self(StatEffectIds.EFF_IGNORE_EQUIPPED_MOVEMENT_PENALTY));

        // 「已装备护身符的全部加成改为作用于你的随从」（StatIds.ALL_BONUSES_FROM_EQUIPPED_AMULET_...）：

        // INERT 门控标记（amount=节点值，≥1 即生效），槽位身份烘焙在 EFF 实例 conditionTags={"amulet"}，

        // 消费端 AccessoryStatHelper#apply 真读——命中则把该槽位物品全部条目的三段选择器重定向为

        // TargetSelector.MINIONS（TGT_INLINE_MINION 内联形态），由 StatEngine 的 master→召唤体 作用域维聚合。

        // 通用「槽位加成重定向给随从」机制：换标签即可复用于头盔/腿甲等槽位；无属性。

        map.put(StatIds.ALL_BONUSES_FROM_EQUIPPED_AMULET_APPLY_TO_YOUR_MINIONS_INSTEAD_OF_YOU,

                self(StatEffectIds.EFF_EQUIPMENT_BONUS_TO_MINIONS));

        // 「裂隙技能 +X 到上限」（StatIds.FISSURE_SKILLS_HAVE_TO_LIMIT）：INERT 标记承载平加上限量（$value，语料 1），

        // 类别身份烘焙在 EFF 实例 conditionTags={"fissure"}（= EntityCategoryLimit.FISSURE 的 path，与消费端同源），

        // 消费端 EntityCategoryLimit#limit 真读——与类别登记属性 MAX_FISSURE（base=1）求和，故 +1 → 2。

        // 通用「按实体类别的数量上限」机制：未来「箭塔/陷阱/残片 上限 +N」只换标签，零 Java 改动。

        map.put(StatIds.FISSURE_SKILLS_HAVE_TO_LIMIT, self(StatEffectIds.EFF_ENTITY_CATEGORY_LIMIT));

        // 「创造裂隙的技能有 X 概率额外创造一个裂隙」（StatIds.SKILLS_WHICH_CREATE_FISSURES_...）：

        // INERT 标记承载概率（$value，语料 0.2 = 20%），类别身份同样烘焙在 EFF 实例 conditionTags={"fissure"}；

        // 消费端 AdditionalSpawn#rollExtra 经 StatEngine.markerMagnitude 真读，在 SpawnAction 生成循环内

        // 以 owner.getRandom() 掷骰（与 EntityBehaviors#detonate 手雷二次激活同口径）。

        // 配对 MarkerStatEffect 承载「每次命中的额外个数」（未接线 → 按 1）。机制就位、待 fissure 实体。

        map.put(StatIds.SKILLS_WHICH_CREATE_FISSURES_HAVE_A_CHANCE_TO_CREATE_AN_ADDITIONAL_FISSURE,

                self(StatEffectIds.EFF_ADDITIONAL_SPAWN_CHANCE));

        // 「放置图腾不需要充能」（StatIds.NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS）：INERT 门控标记（

        // amount>0 即豁免），上下文身份烘焙在 EFF 实例 conditionTags={GateTag.TOTEM="totem"}，

        // 消费端 ChargedState#consume 真读——本次消耗携带的上下文标签（数据侧 ChargeAction.tags）⊇ 实例标签即豁免：

        // 照常 publish ChargeChangeEvent 并返回 real，但不扣球（与 CHARGE_CONSUME_SKIP_CHANCE 并列）。

        // 通用「充能消耗豁免」机制，不焊死图腾；数据侧须在放置图腾技能的 ChargeAction 上补 tags: ["totem"]。

        map.put(StatIds.NO_CHARGES_REQUIREMENT_FOR_PLACING_TOTEMS, self(StatEffectIds.EFF_CHARGE_COST_EXEMPT));

        // 「植物有 X 概率立即长成」（StatIds.PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW）：

        // INERT 标记承载概率（$value，1.0 = 100%，>1 按 PoE2 拆保底次数 + 余概率），

        // 受体身份烘焙在 EFF 实例 conditionTags={GateTag.PLANT="plant"}（与技能 toxic_growth 的

        // .withTags(GateTag.PLANT) 同源，经技能作用域标签自动进入查询集），消费端

        // EntityStages#applySpawnRoll 真读——在<b>全部投递入口共用的装配路径</b>（EntityDefCarrier#configure）

        // 以 owner.getRandom() 掷骰，命中则跳过整条阶段序列直接以末阶段出现。

        // 落点是通用「阶段式实体」设施而非植物专属代码：图腾直接以最终形态出现 / 召唤物立即成熟 /

        // 投射物二次阶段复用同 Kind 只换标签，零 Java 改动；实体未声明阶段序列 → 恒 no-op。

        // 【机制就位、待数据】当前无任何植物实体（亦无任何实体声明阶段序列），

        // 数据侧需在 {@code coe/entity/*.json} 给 plant 形态补 "stages": [...] 后方显形。

        map.put(StatIds.PLANTS_HAVE_A_CHANCE_TO_IMMEDIATELY_OVERGROW,

                self(StatEffectIds.EFF_ENTITY_STAGE_SKIP_CHANCE));

        // 「持 talisman 时仍可装备 sceptre」（StatIds.YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN）：

        // INERT 门控标记（amount>0 即豁免），<b>规则身份烘焙在 EFF 实例 conditionTags</b>

        // （= EquipRules.OTHER_HAND_CONFLICT 的 path，与数据侧 coe/equip_rule/other_hand_conflict.json 同名），

        // 消费端 EquipRules#blocked 在持握判定路径上真读——命中规则且未豁免则按「未装备」处理。

        // 通用「装备 / 持握规则豁免」机制，不焊死任何物品类别；无属性。

        // 【语料说明】原文中 "non-Item Rarity" 修饰来源不可考，按「不臆造玩法」只取可确定部分：

        // 解除一条「另一手类别冲突」约束；稀有度 / 词缀维度已在规则侧以可选标签字段预留，待内容侧给 tag 后填数据闭合。

        map.put(StatIds.YOU_CAN_EQUIP_A_NON_ITEM_RARITY_SCEPTRE_WHILE_WIELDING_A_TALISMAN,

                self(StatEffectIds.EFF_EQUIP_RULE_EXEMPT_OTHER_HAND));

        // 「可单手持双手 剑/斧/锤」（StatIds.YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND）：

        // INERT 门控标记（amount>0 即豁免），规则身份 = EquipRules.BOTH_HANDS_OCCUPANCY 的 path

        // （数据侧 coe/equip_rule/both_hands_occupancy.json：「占双槽」规则）。

        // 规则语义方向：双手武器本身永不被顶（始终可用），改为<b>另一手物品</b>被顶掉；

        // 持有本标记后另一手物品不再被顶 → 达成「单手持双手武器」。无属性。

        map.put(StatIds.YOU_CAN_WIELD_TWO_HANDED_AXES_MACES_AND_SWORDS_IN_ONE_HAND,

                self(StatEffectIds.EFF_EQUIP_RULE_EXEMPT_BOTH_HANDS));

        // ── wildsurge_incantation 风暴/植物法术：消耗/持续时间（self(EFF)，幅度由节点值驱动）──

        // 节点值：50% less cost = -0.5，75% less duration = -0.75（见 KeystoneNodes01#wildsurge_incantation）。

        // duration EFF 走 BUFF_DURATION_MULT（ADD_MULTIPLIED_TOTAL，base=1.0）→ 值 -0.75 ⇒ 持续 ×0.25（75% less），方向正确。

        // cost EFF 走 MANA_COST_EFFICIENCY（ADD_VALUE，base=0）→ 读点 ResourceCost:120 `raw * max(0, 1 - eff)`；

        //   节点值 +0.5 ⇒ eff=+0.5 ⇒ 消耗 ×0.5（即 50% less cost），方向正确（已修正：原规范误写 -0.5）。

        // ── 词缀（Mod）域接线 ──

        // 1) 「#% increased Spirit」（权杖局部）：MAX_SPIRIT（base=0，但有 ADD_MAX_SPIRIT 平加来源，故 inc 走 TOTAL），

        //    与既有 StatIds.INC_SPIRIT / MORE_SPIRIT 同落点（WireResource:104/137）。

        map.put(StatIds.INC_MAX_SPIRIT, self(StatEffectIds.EFF_ADD_MULTIPLIED_TOTAL_ATTRIBUTE_COE_MAX_SPIRIT));

        // 2) 「Minions have #% increased maximum Life」：MAX_HEALTH {minion} 作用域（与 StatIds.MINIONS_HAVE_INC_MAX_HEALTH 同落点）。

        map.put(StatIds.INC_MINION_MAX_LIFE, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_MAX_HEALTH_MINION));

        // 3) 「+# to Level of all <近战/弓/法术/陷阱/随从/投射物> Skills」：SKILL_LEVEL_BONUS（base=0，ADD_VALUE，无元素标签=全技能），

        //    SkillCaster.effectiveSkillLevel:162 真读（#241 新增通用变体；既有 CHAOS/COLD 变体带元素标签，覆盖不到这些语族）。

        map.put(StatIds.INC_SOCKETED_GEM_LEVEL, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SKILL_LEVEL_BONUS));

        // 4) 「#% reduced Attribute Requirements」（局部装备需求）：ATTRIBUTE_REQUIREMENTS（base=0，ADD_VALUE），

        //    ModGenerator#currentEquipment:49 按 max(0, 1 + 值) 缩放需求真读（"reduced" 由条目负值承载）。

        map.put(StatIds.REDUCED_ATTRIBUTE_REQUIREMENTS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_ATTRIBUTE_REQUIREMENTS));

        // 5) 「#% reduced Duration of Bleeding on You」：AILMENT_DURATION {ailment, taken, bleed}（ADD_MULTIPLIED_BASE，base=1.0），

        //    "reduced" 由条目负值承载；与 StatIds 同族流血时长变体同落点。

        map.put(StatIds.REDUCED_BLEEDING_DURATION, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_BLEED_COE_AILMENT_DURATION_TAKEN));

        // 6) 「+#% Surpassing chance to fire an additional Arrow」：SURPASS_ADDITIONAL_PROJECTILE_CHANCE（base=0，ADD_VALUE）。

        map.put(StatIds.SURPASSING_CHANCE_ADDITIONAL_ARROW, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_SURPASS_ADDITIONAL_PROJECTILE_CHANCE));

        // 7) 「#% increased Trap and Mine Throwing Speed」：与 StatIds.INC_TRAP_THROW_SPEED 同落点

        //    （CAST_SPEED {trap} 作用域，WireSpeed:66）——本引擎把「陷阱/地雷投掷速度」建模为 trap 作用域技能速度。

        map.put(StatIds.TRAP_AND_MINE_THROW_SPEED, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED_TRAP));

        // 8) ── 威压（Presence）光环族：全部经 TGT_INLINE_PRESENCE_ALLIES 施加到在场盟友 ──

        //    与既有 StatIds.ALLIES_IN_YOUR_PRESENCE_* 系列（WireMisc:880-888）逐一同源，零新增原语。

        //    a) 「Allies in your Presence deal #% increased Damage」→ DAMAGE {damage}

        map.put(StatIds.PRESENCE_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_DAMAGE));

        //    b) 「Allies in your Presence have #% increased Attack Speed」→ 原版 ATTACK_SPEED

        map.put(StatIds.PRESENCE_ATTACK_SPEED, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_MINECRAFT_ATTACK_SPEED));

        map.put(StatIds.PRESENCE_CAST_SPEED, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED));

        //    d) 「Allies in your Presence have #% increased Critical Hit Chance」→ CRIT_CHANCE

        map.put(StatIds.PRESENCE_CRIT_CHANCE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_CHANCE));

        //    e) 「Allies in your Presence have #% increased Critical Damage Bonus」→ CRIT_DAMAGE

        map.put(StatIds.PRESENCE_CRIT_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CRIT_DAMAGE));

        //    f) 「Allies in your Presence have +#% to all Elemental Resistances」→ 三元素 RESISTANCES 共享同一 X%：

        //       盟友承伤时抗性读取作用域已带 {fire}/{cold}/{lightning}，故三条各自命中，合起来即「全元素抗性」。

        map.put(StatIds.PRESENCE_ALL_ELEMENTAL_RESISTANCES, group(

                presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_FIRE),

                presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_COLD),

                presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_RESISTANCES_LIGHTNING)));

        //    g) 「#% increased Presence Area of Effect」→ PRESENCE_RANGE（base=6.0，ADD_MULTIPLIED_BASE），

        //       PresenceAura 范围读取点真读（本引擎以「半径」承载 Presence AoE）。

        map.put(StatIds.PRESENCE_RADIUS, self(StatEffectIds.EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_PRESENCE_RANGE));

        //    h) 「Allies in your Presence Regenerate #% of your Maximum Life per second」→ 在场友军 MAX_REGENERATION {health, regen}，

        //       与 base StatIds.ALLIES_IN_YOUR_PRESENCE_REGENERATE_OF_YOUR_MAX_HEALTH_PER_SECOND（WireResource:38）同源、同一 EFF。

        //       已在 WireResource:43 接线（StatIds.PRESENCE_LIFE_REGEN → TGT_INLINE_PRESENCE_ALLIES），此处不再重复。

        //       数值形态=最大生命比例/秒（MAX_REGENERATION 默认 0.05=5%，LIVINGMOD 0.02、WireRecent vals=[0.01,10.0] 同口径），

        //       HealthHelper.getHealthRegeneration 真读 → R4 安全、非虚接。

        // ── 以下 6 条当前<b>无任何词缀引用</b>（仅 ModStatIds 声明），接线纯粹为防未来引用时成为死线 ──

        // 9) 「Allies in your Presence deal # to # added Attack <元素> Damage」：与 ADD_*_DAMAGE_TO_ATTACKS 同族 EFF，

        //    仅把作用域换成在场盟友（TGT_INLINE_PRESENCE_ALLIES）。

        map.put(StatIds.PRESENCE_COLD_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COLD_MINECRAFT_ATTACK_DAMAGE));

        map.put(StatIds.PRESENCE_FIRE_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_FIRE_MINECRAFT_ATTACK_DAMAGE));

        map.put(StatIds.PRESENCE_LIGHTNING_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_LIGHTNING_MINECRAFT_ATTACK_DAMAGE));

        map.put(StatIds.PRESENCE_PHYSICAL_DAMAGE, presence(PresenceCountValue.Relation.ALLY, StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_MINECRAFT_ATTACK_DAMAGE_PHYSICAL));
        //    c) 「Allies in your Presence have #% increased Cast Speed」→ CAST_SPEED


        // 11) 「#% increased Light Radius」：LIGHT_RADIUS（ADD_VALUE，base=0）。

        map.put(StatIds.LIGHT_RADIUS, self(StatEffectIds.EFF_ADD_VALUE_ATTRIBUTE_COE_LIGHT_RADIUS));

    }

}

