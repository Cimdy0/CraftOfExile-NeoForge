package com.cimdy.coe.data.gen.stat;

import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;

import com.cimdy.coe.data.helper.status.AilmentHelper;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;

import com.cimdy.coe.data.codec.support.status.def.StatusDefs;



import com.cimdy.coe.data.helper.engine.ground.GroundFieldManager;



import com.cimdy.coe.CraftOfExile;

import com.cimdy.coe.data.codec.support.rarity.Rarity;

import com.cimdy.coe.data.codec.support.math.ComparisonOperator;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.CompareValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.damage.DamageValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.GemValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskActiveValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskUsesValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.CharmActiveValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityTypeValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.ConstantValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.HealthValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceRarityValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.TargetRarityValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.StatusValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.skill.UsingSkillValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.BindExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueTarget;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.DistanceToNearestEnemyValue;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.EquipmentStatKinds;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode.EventHistoryModes;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.GemAggregates;

import com.cimdy.coe.data.codec.skill.gem.GemColor;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.GemKinds;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode.LivingStateModes;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.mode.MovementModes;

import com.cimdy.coe.data.codec.skill.phase.action.charge.ChargeType;

import com.cimdy.coe.data.codec.support.stat.tag.GateTag;

import com.cimdy.coe.event.combat.AttributeEvent;

import com.cimdy.coe.event.combat.DamageEvent;

import com.cimdy.coe.event.combat.GameEvent;

import com.cimdy.coe.event.skill.SkillEvent;

import org.jspecify.annotations.Nullable;

import com.cimdy.coe.event.Event;

import com.cimdy.coe.event.EventName;

import net.minecraft.resources.Identifier;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.IgnitedBySourceValue;
import com.cimdy.coe.data.helper.engine.DamagePipeline;



/**

 * Stat condition 子条目内容注册表（CODEC 工厂方法：每个 ValueExpr 经对应节点类构造器构造真实对象，

 * Provider 经 ValueExpr.CODEC.listOf() 编码落盘）。全部手写字符串统一为常量引用。

 */

public final class StatConditionGen {

    private StatConditionGen() {}



    public static Map<Identifier, List<ValueExpr>> contents() {

        Map<Identifier, List<ValueExpr>> m = new LinkedHashMap<>();

        m.put(StatConditionIds.COND_0_0_LIVING_STATE_SURROUNDED, List.of(new LivingStateValue(LivingStateModes.SURROUNDED, 0.0)));

        m.put(StatConditionIds.COND_2_0_BLUE_COMPARE_COUNT_SOCKET_SUPPORT, List.of(new CompareValue(new GemValue(Optional.empty(), Optional.empty(), Optional.of(GemColor.BLUE.getSerializedName()), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), 2.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_2_0_COMPARE_COUNT_GREEN_SOCKET_SUPPORT, List.of(new CompareValue(new GemValue(Optional.empty(), Optional.empty(), Optional.of(GemColor.GREEN.getSerializedName()), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), 2.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_2_0_COMPARE_COUNT_RED_SOCKET_SUPPORT, List.of(new CompareValue(new GemValue(Optional.empty(), Optional.empty(), Optional.of(GemColor.RED.getSerializedName()), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), new GemValue(Optional.empty(), Optional.empty(), Optional.empty(), GemKinds.SUPPORT, Optional.empty(), GemAggregates.COUNT), 2.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_ARCHON_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.ARCHON, 80.0)));

        m.put(StatConditionIds.COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD, List.of(new CompareValue(new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of(EquipmentStatKinds.ARMOR), EquipmentValue.Aggregate.FIRST), new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of(EquipmentStatKinds.EVASION), EquipmentValue.Aggregate.FIRST), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_ARMOR_COMPARE_EQUIPMENT_EVASION_HEAD_2, List.of(new CompareValue(new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of(EquipmentStatKinds.EVASION), EquipmentValue.Aggregate.FIRST), new EquipmentValue(Optional.of(List.of(EquipmentSlot.HEAD)), Optional.empty(), Optional.of(EquipmentStatKinds.ARMOR), EquipmentValue.Aggregate.FIRST), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_BLEED_STATUS_AGE, List.of(StatusValue.byAgeThreshold(StatusDefs.BLEED.id, 80.0 / 20.0)));

        m.put(StatConditionIds.COND_BLOCKING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.BLOCKING, 80.0)));

        m.put(StatConditionIds.COND_CONSUME_POWER_COUNT_RECENTLY, List.of(EventHistoryValue.recently(EventName.consumeCharge(ChargeType.POWER.getSerializedName()))));

        m.put(StatConditionIds.COND_COUNT_KILL_RECENTLY, List.of(EventHistoryValue.recently(GateTag.KILL)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_HIT, List.of(EventHistoryValue.recently(EventName.TAKEN_HIT)));

        // 「近期装填过」（弩装填完成 EventName.RELOADED，默认 4s 窗口）：供「弹药有概率不消耗」等条目条件门控；

        // 与 COND_COUNT_RECENTLY_TAKEN_HIT 同构，复用既有 EventHistoryValue.recently 原语，零引擎改动。

        m.put(StatConditionIds.COND_RECENTLY_RELOADED, List.of(EventHistoryValue.recently(EventName.RELOADED)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_TAKEN_STUN, List.of(EventHistoryValue.recently(EventName.TAKEN_STUN)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_USE_ITEM, List.of(EventHistoryValue.recently(EventName.USE_ITEM)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_USE_TOTEM, List.of(EventHistoryValue.recently(EventName.USE_TOTEM)));

        m.put(StatConditionIds.COND_DAZE_STATUS, List.of(new StatusValue(StatusDefs.DAZE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_ELECTROCUTE_STATUS, List.of(new StatusValue(StatusDefs.ELECTROCUTE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_EXPOSURE_COLD_STATUS, List.of(new StatusValue(StatusDefs.EXPOSURE_COLD.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_EXPOSURE_FIRE_STATUS, List.of(new StatusValue(StatusDefs.EXPOSURE_FIRE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_EXPOSURE_LIGHTNING_STATUS, List.of(new StatusValue(StatusDefs.EXPOSURE_LIGHTNING.id, CraftOfExile.EMPTY_LOCATION)));

        // ── 任意曝光（作用域标签引用）：持有任意带 "exposure" 标签的状态即满足，解锁通用 WITH_EXPOSURE 族 ──

        m.put(StatConditionIds.COND_ANY_EXPOSURE, List.of(StatusValue.byTag("exposure")));

        // ── 任意异常（作用域标签引用）：持有任意带 "ailment" 标签状态即满足，解锁通用 AGAINST_AILMENTS 族 ──

        m.put(StatConditionIds.COND_ANY_AILMENT, List.of(StatusValue.byTag("ailment")));

        // ── 任意元素异常（作用域标签引用）：持有任意带 "elemental" 标签状态即满足 ──

        m.put(StatConditionIds.COND_ANY_ELEMENTAL_AILMENT, List.of(StatusValue.byTag("elemental")));

        // ── 任意诅咒（作用域 kind 引用）：StatusValue.byKind("curse") 匹配任意 kind=CURSE 状态 ──

        m.put(StatConditionIds.COND_ANY_CURSE, List.of(StatusValue.byKind("curse")));

        // ── 诅咒存续比例门控（实体节点）：目标身任意 CURSE 状态存续比例 ≥ 0.5 即满足，解锁 M2「诅咒时长到期后增诅咒幅度」族 ──

        m.put(StatConditionIds.COND_CURSE_DURATION_EXPIRED, List.of(StatusValue.byCurseFraction(0.5)));

        // ── 无元素异常（作用域标签引用取反）：敌人持任意 "elemental" 标签状态即不满足 ──

        m.put(StatConditionIds.COND_NO_ELEMENTAL_AILMENT, List.of(StatusValue.byTag("!elemental")));

        // ── 任意伤害性状态（作用域 kind 引用）：StatusValue.byKind("damaging") 匹配任意 kind=DAMAGING 状态（ignite/bleed/poison/corrupted_blood）──

        m.put(StatConditionIds.COND_ANY_DAMAGING, List.of(StatusValue.byKind("damaging")));

        m.put(StatConditionIds.COND_FREEZE_STATUS, List.of(new StatusValue(StatusDefs.FREEZE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_FULL_LIFE_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.FULL_LIFE, 80.0)));

        m.put(StatConditionIds.COND_FULL_LIFE_LIVING_STATE_2, List.of(new LivingStateValue(LivingStateModes.NOT_FULL_LIFE, 80.0)));

        m.put(StatConditionIds.COND_HEAVY_STUN_STATUS, List.of(StatusValue.byFlag(StatusValue.Flag.HEAVY)));

        // ── 轻晕（stun 状态在身）：镜像 COND_HEAVY_STUN_STATUS，复用既有 StatusDefs.STUN，零引擎改动 ──

        m.put(StatConditionIds.COND_STUN_STATUS, List.of(new StatusValue(StatusDefs.STUN.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_ARMOUR_BROKEN_FULLY_STATUS, List.of(StatusValue.byFlag(StatusValue.Flag.FULLY_BROKEN)));

        // 交互对方完全破甲：BindExpr(SOURCE) 包装完全破甲档节点（单 ARMOUR_BREAK 状态两档合一后由 StatusInstance.fullyBroken 识别）

        m.put(StatConditionIds.COND_SOURCE_ARMOUR_BROKEN_FULLY_STATUS,

                List.of(new BindExpr(ValueTarget.SOURCE, StatusValue.byFlag(StatusValue.Flag.FULLY_BROKEN))));

        m.put(StatConditionIds.COND_LIVING_STATE_LOW_LIFE, List.of(new LivingStateValue(LivingStateModes.LOW_LIFE, 80.0)));

        m.put(StatConditionIds.COND_LIVING_STATE_OFF_HAND_EMPTY_ONE_HANDED_MARTIAL, List.of(

                new LivingStateValue(LivingStateModes.OFF_HAND_EMPTY, 80.0),

                new LivingStateValue(LivingStateModes.ONE_HANDED_MARTIAL, 80.0)));

        // 双手皆空（主手 + 副手均无物品）：镜像 off_hand_empty，供「徒手攻击视同持长杖」族门控（子句 b/c）。

        m.put(StatConditionIds.COND_BOTH_HANDS_EMPTY, List.of(new LivingStateValue(LivingStateModes.BOTH_HANDS_EMPTY, 80.0)));

        m.put(StatConditionIds.COND_LIVING_STATE_PARRYING, List.of(new LivingStateValue(LivingStateModes.PARRYING, 80.0)));

        m.put(StatConditionIds.COND_LIVING_STATE_RECENTLY_TOOK_DOT, List.of(new LivingStateValue(LivingStateModes.RECENTLY_TOOK_DOT, 80.0)));

        m.put(StatConditionIds.COND_RECENTLY_GAINED_RAGE, List.of(new LivingStateValue(LivingStateModes.RECENTLY_GAINED_RAGE, 0.0)));

        // 召唤物全部为同伴（window 不参与判定，留 80.0 与其它 living_state 节点同风格）

        m.put(StatConditionIds.COND_ALL_MINIONS_ARE_COMPANIONS, List.of(new LivingStateValue(LivingStateModes.ALL_MINIONS_ARE_COMPANIONS, 80.0)));

        m.put(StatConditionIds.COND_LIVING_STATE_SHAPESHIFTED, List.of(new LivingStateValue(LivingStateModes.SHAPESHIFTED, 80.0)));

        // ── 自侧变形态（跨实体读值）：onTarget 条目条件按受击方求值，故自侧状态经 BindExpr(SOURCE) 换到交互对方（=攻击者）读取 ──

        m.put(StatConditionIds.COND_SOURCE_LIVING_STATE_SHAPESHIFTED,

                List.of(new BindExpr(ValueTarget.SOURCE, new LivingStateValue(LivingStateModes.SHAPESHIFTED, 80.0))));

        // ── 形态纯正（per-form 状态 COND；复用已注册 StatusDefs.BEAR/WOLF/WYVERN，零引擎改动）──

        // 对齐 archon「持有即常驻」改写为「形态存活才授予」：form 属性加成经 self(COND_FORM_X, EFF) 门控，

        // 防御性纯正（避免 form 属性被非形态源授予时越界生效），与 COND_ARCANE_SURGE_STATUS 同构。

        m.put(StatConditionIds.COND_FORM_BEAR, List.of(new StatusValue(StatusDefs.BEAR.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_FORM_WOLF, List.of(new StatusValue(StatusDefs.WOLF.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_FORM_WYVERN, List.of(new StatusValue(StatusDefs.WYVERN.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_LIVING_STATE_STATIONARY, List.of(new LivingStateValue(LivingStateModes.STATIONARY, 80.0)));

        m.put(StatConditionIds.COND_LIVING_STATE_SURROUNDED, List.of(new LivingStateValue(LivingStateModes.SURROUNDED, 80.0)));

        m.put(StatConditionIds.COND_MOVEMENT_STATIONARY, List.of(new MovementValue(MovementModes.STATIONARY, 20.0)));

        // 「站在 X 地面」= 地面施加的真实状态，经 Status 节点读取（不依赖任何存在性 / 标记层）：

        // ignited/shocked/chilled 地面由 GroundFieldManager.applyGround 持续施加对应异常（本会话③），

        // jagged 地面由 GroundTypes.def 把 jagged_ground 状态作为真实效果载荷施加。

        m.put(StatConditionIds.COND_ON_CHILLED_GROUND_STATUS, List.of(new StatusValue("on_chilled_ground", CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_ON_IGNITED_GROUND_STATUS, List.of(new StatusValue("on_ignited_ground", CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_ON_JAGGED_GROUND_STATUS, List.of(new StatusValue("jagged_ground", CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_ON_SHOCKED_GROUND_STATUS, List.of(new StatusValue("on_shocked_ground", CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_PIN_STATUS, List.of(new StatusValue(StatusDefs.PIN.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_PRESENCE_RARITY_RARE_UNIQUE, List.of(new PresenceRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE), true, false)));

        // ── 事件「触发目标」稀有度（真值门控）：读 caused_ailment+heavy_stun 事件所作用的那一只敌人（被重晕者）

        //    的 RARITY——由 AilmentHelper.applyHeavyStun 经 StatScopes.enterMerged(caster, …withSourceEntity(victim))

        //    把被重晕敌人绑定为事件交互对方，TargetRarityValue 取其 RARITY。精确取代「身边有稀有敌」的近似（R3 真读）。──

        m.put(StatConditionIds.COND_TARGET_RARITY_RARE_UNIQUE, List.of(TargetRarityValue.rareOrUniqueTarget()));

        // ── 「目标被你点燃」条件（COND_TARGET_IGNITED_BY_YOU）：

        //    读受害方 IGNITE_HISTORY 附件按施加者 UUID 累计秒数是否 >0（IgnitedBySourceValue 无状态节点）。──

        m.put(StatConditionIds.COND_TARGET_IGNITED_BY_YOU, List.of(new IgnitedBySourceValue()));

        m.put(StatConditionIds.COND_SHOCK_STATUS, List.of(new StatusValue(StatusDefs.SHOCK.id, CraftOfExile.EMPTY_LOCATION)));

        // ── ARCANE_SURGE 状态 COND（纯数据；复用已注册 StatusDefs.ARCANE_SURGE）──

        m.put(StatConditionIds.COND_ARCANE_SURGE_STATUS, List.of(new StatusValue(StatusDefs.ARCANE_SURGE.id, CraftOfExile.EMPTY_LOCATION)));

        // ── 状态/战斗状态 COND 原语（纯数据；StatusValue / LivingStateValue 复用既有注册 StatusType / LivingStateModes）──

        m.put(StatConditionIds.COND_BLIND_STATUS, List.of(new StatusValue(StatusDefs.BLIND.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_CHILL_STATUS, List.of(new StatusValue(StatusDefs.CHILL.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_HINDER_STATUS, List.of(new StatusValue(StatusDefs.HINDER.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_IGNITE_STATUS, List.of(new StatusValue(StatusDefs.IGNITE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_MAIM_STATUS, List.of(new StatusValue(StatusDefs.MAIM.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_POISON_STATUS, List.of(new StatusValue(StatusDefs.POISON.id, CraftOfExile.EMPTY_LOCATION)));

        // ── 取反状态 COND（纯数据；CompareValue 复用既有节点）：StatusValue(POISON) 未持有时输出 0，

        //    EQUAL 0 → 1.0（真），承载「against enemies that are not Poisoned」门控，与 COND_POISON_STATUS 镜像 ──

        m.put(StatConditionIds.COND_NOT_POISON_STATUS, List.of(new CompareValue(

                new StatusValue(StatusDefs.POISON.id, CraftOfExile.EMPTY_LOCATION),

                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.EQUAL)));

        // ── 凋零「包含 / 不包含」COND：直接吃 StatusValue 的 hasStatus / lacksStatus 原语（output:flag → 1/0），
        //    不必再像 COND_NOT_POISON_STATUS 那样用 CompareValue(状态标量, 0, EQUAL) 绕行。
        //    其中「未凋零」是修饰语门控，见 StatIds.java:5077 语料
        //    「Unwithered enemies are [Withered] for 8 seconds when they enter your [Presence]」──
        //    Presence 光环无节拍时每刻驱动，缺此门控会在半秒内把 10 层凋零叠满。
        //    Identifier 构造沿用既有写法 Identifier.fromNamespaceAndPath(CraftOfExile.MODID, <StatusType>.id)
        //    （出处：SkillFactory.java:373 / SpiritGemFactory.java:216，消费侧由 StatusType.byId(id.getPath()) 反解）。 ──
        m.put(StatConditionIds.COND_WITHERED_STATUS, List.of(StatusValue.hasStatus(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, StatusDefs.WITHER.id))));

        m.put(StatConditionIds.COND_NOT_WITHERED_STATUS, List.of(StatusValue.lacksStatus(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, StatusDefs.WITHER.id))));

        m.put(StatConditionIds.COND_SLOW_STATUS, List.of(new StatusValue(StatusDefs.SLOW.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_CHANNELLING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.CHANNELLING, 80.0)));

        m.put(StatConditionIds.COND_LEECHING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.LEECHING, 80.0)));

        m.put(StatConditionIds.COND_LEECHING_MANA_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.LEECHING_MANA, 80.0)));

        m.put(StatConditionIds.COND_MOVING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.MOVING, 80.0)));

        m.put(StatConditionIds.COND_SPRINTING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.SPRINTING, 80.0)));

        // ── 双持 / 持盾（纯数据；LivingStateModes 已注册）──

        m.put(StatConditionIds.COND_DUAL_WIELD_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.DUAL_WIELD, 80.0)));

        m.put(StatConditionIds.COND_HOLDING_SHIELD_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.HOLDING_SHIELD, 80.0)));

        // ── 武器持有（纯数据；LivingStateModes.WIELDING_AXE 已注册，判定经 ItemTypeChecker.isAxe）──

        m.put(StatConditionIds.COND_WIELDING_AXE, List.of(new LivingStateValue(LivingStateModes.WIELDING_AXE, 80.0)));

        m.put(StatConditionIds.COND_WIELDING_QUARTERSTAFF, List.of(new LivingStateValue(LivingStateModes.WIELDING_QUARTERSTAFF, 80.0)));

        m.put(StatConditionIds.COND_WIELDING_MELEE_WEAPON, List.of(new LivingStateValue(LivingStateModes.WIELDING_MELEE_WEAPON, 80.0)));

        // ── 武器持有：匕首（纯数据；LivingStateModes.WIELDING_DAGGER 已注册，判定经 GateTag.DAGGER）──

        m.put(StatConditionIds.COND_WIELDING_DAGGER, List.of(new LivingStateValue(LivingStateModes.WIELDING_DAGGER, 80.0)));

        // ── 纯数据 COND 原语：LivingStateModes / DebuffStatusTypes 已注册，仅加数据条目 ──

        m.put(StatConditionIds.COND_LOW_MANA_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.LOW_MANA, 80.0)));

        // ── 通用取反 COND 原语：LivingStateValue 在解析层处理 ! 前缀，零引擎改动 ──

        m.put(StatConditionIds.COND_NOT_LOW_MANA_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.NOT_LOW_MANA, 80.0)));

        m.put(StatConditionIds.COND_FULL_MANA_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.FULL_MANA, 80.0)));

        m.put(StatConditionIds.COND_DODGE_ROLLING_LIVING_STATE, List.of(new LivingStateValue(LivingStateModes.DODGE_ROLLING, 80.0)));

        m.put(StatConditionIds.COND_WIELDING_TWO_HANDED_MELEE, List.of(new LivingStateValue(LivingStateModes.WIELDING_TWO_HANDED_MELEE, 80.0)));

        m.put(StatConditionIds.COND_INCISION_STATUS, List.of(new StatusValue(StatusDefs.INCISION.id, CraftOfExile.EMPTY_LOCATION)));

        // ── 标记状态 COND（纯数据；StatusValue 解析 mark 读取敌身 Mark 标记，由 Mark 技能施放时经 MarkHelper.applyMarkToNearby 施加；

        //    onTarget 接线据此对敌施加 inc 伤害/暴击/致盲/无法暴击。与 PRESENCE_MARK（图腾承伤标记）正交，零新增事件订阅，R3 安全）──

        m.put(StatConditionIds.COND_PRESENCE_MARK_STATUS, List.of(new StatusValue("mark", CraftOfExile.EMPTY_LOCATION)));

        // ── 危害目标侧标记 COND（纯数据；StatusValue 读敌身 HAZARD_MARK 状态，窗口=状态时长 80t=4s）──

        // 触发时（on_trigger） 对 target 施 HAZARD_MARK（owner≠null 即「你的」危害），敌持该状态即满足条件；

        // onTarget 接线据此对敌施加 inc 护甲/闪避。与 PRESENCE_MARK 同源纯标记范式，R3 安全（零新增事件订阅）。

        m.put(StatConditionIds.COND_HAZARD_MARKED, List.of(new StatusValue(StatusDefs.HAZARD_MARK.id, CraftOfExile.EMPTY_LOCATION)));

        // ── presence 进/出范围近期 COND（纯数据；PresenceValue 按候选敌 UUID 查追踪者 presence 记录，onTarget 求值即得「该敌近期进/出你范围」）──

        m.put(StatConditionIds.COND_RECENTLY_ENTERED_PRESENCE, List.of(new PresenceValue("entered", 80.0)));

        m.put(StatConditionIds.COND_RECENTLY_LEFT_PRESENCE, List.of(new PresenceValue("left", 80.0)));

        // ── 事件历史 COND：EventName.CAUSED_CRIT / CAUSED_PARRY / consumeCharge(...) 均已注册 ──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_CRIT, List.of(EventHistoryValue.recently(EventName.CAUSED_CRIT)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_PARRY, List.of(EventHistoryValue.recently(EventName.CAUSED_PARRY)));

        m.put(StatConditionIds.COND_CONSUME_FRENZY_COUNT_RECENTLY, List.of(EventHistoryValue.recently(EventName.consumeCharge(ChargeType.FRENZY.getSerializedName()))));

        m.put(StatConditionIds.COND_CONSUME_ENDURANCE_COUNT_RECENTLY, List.of(EventHistoryValue.recently(EventName.consumeCharge(ChargeType.ENDURANCE.getSerializedName()))));

        // ── 事件历史 COND（续）：EventName 均已注册 ──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_BLOCK, List.of(EventHistoryValue.recently(EventName.CAUSED_BLOCK)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_AILMENT, List.of(EventHistoryValue.recently(EventName.CAUSED_AILMENT)));

        // ── 造成异常分类型近期 COND（CAUSED_AILMENT 带具体异常标签）──

        // AilmentHelper.fireAilment 在施加轻晕 / 钉刺时真 fire {caused_ailment, <id>}，

        // 由 GameEvent.fire 经 Event.record 写入攻击者 Event.State 的 taggedTriggers（与 CAUSED_HIT+melee 同通道），R3 真实读点。

        m.put(StatConditionIds.COND_RECENTLY_CAUSED_STUN,

                List.of(EventHistoryValue.recently(EventName.CAUSED_AILMENT, StatusDefs.STUN.id)));

        m.put(StatConditionIds.COND_RECENTLY_CAUSED_PIN,

                List.of(EventHistoryValue.recently(EventName.CAUSED_AILMENT, StatusDefs.PIN.id)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_META_SKILL_TRIGGERED, List.of(EventHistoryValue.recently(EventName.META_SKILL_TRIGGERED)));

        // ── 事件历史 COND（续）：形变 / 获得能量球 ──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_FORM_CHANGED, List.of(EventHistoryValue.recently(EventName.FORM_CHANGED)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_GAIN_CHARGE, List.of(EventHistoryValue.recently(EventName.GAIN_CHARGE)));

        // ── 事件历史 COND（续）：发起攻击 ──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_ATTACK_START, List.of(EventHistoryValue.recently(EventName.ATTACK_START)));

        // ── 事件历史 COND（续）：近期施放过法术（CAST_SKILL 事件带 "spell" 标签，A2 通道 EXACT 匹配）──

        m.put(StatConditionIds.COND_CAST_SPELL_RECENTLY, List.of(EventHistoryValue.recently(EventName.CAST_SKILL, GateTag.SPELL)));

        // ── 近期施放过「标记」技能（CAST_SKILL 事件携带 MARK 标签，SkillGreenGen:78 .withTags(MARK)）──

        m.put(StatConditionIds.COND_RECENTLY_USED_MARK, List.of(EventHistoryValue.recently(EventName.CAST_SKILL, GateTag.MARK)));

        // ── 事件历史 COND（续）：主动击中 / 形变清除 —— 窗口由 EventHistoryValue.recently 提供（4s=80tick）──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_HIT, List.of(EventHistoryValue.recently(EventName.CAUSED_HIT)));

        m.put(StatConditionIds.COND_COUNT_RECENTLY_FORM_CLEARED, List.of(EventHistoryValue.recently(EventName.FORM_CLEARED)));

        // ── 近期翻滚过（语料 "if you've Dodge Rolled Recently"）：DODGE_ROLL 事件由 DodgeRollHelper.tryDodge 发射，recently() 窗口 80tick ──

        m.put(StatConditionIds.COND_RECENTLY_DODGE_ROLLED, List.of(EventHistoryValue.recently(EventName.DODGE_ROLL)));

        m.put(StatConditionIds.COND_NOT_RECENTLY_TAKEN_HIT, List.of(EventHistoryValue.notRecently(EventName.TAKEN_HIT)));

        // 镜像 COND_NOT_RECENTLY_TAKEN_HIT：近期未受眩晕 → 眩晕门槛提升（复用既有 stun threshold EFF，零新机制）

        m.put(StatConditionIds.COND_NOT_RECENTLY_TAKEN_STUN, List.of(EventHistoryValue.notRecently(EventName.TAKEN_STUN)));

        // ── 距离簇：到最近敌对实体距离门控；阈值取自条目 stat_range_m 参数（bindParams 注入 → VarRef 读取）──

        // within Xm：distance(living, nearestEnemy) <= stat_range_m

        m.put(StatConditionIds.COND_WITHIN_RANGE_OF_ENEMY,

                List.of(new CompareValue(new DistanceToNearestEnemyValue(), new VarRef("stat_range_m"), 1.0, 1.0, ComparisonOperator.LESS_THAN_OR_EQUAL)));

        // further than Xm：distance(living, nearestEnemy) > stat_range_m

        m.put(StatConditionIds.COND_FURTHER_THAN_RANGE_OF_ENEMY,

                List.of(new CompareValue(new DistanceToNearestEnemyValue(), new VarRef("stat_range_m"), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // ── Presence 存在性门控：presence_count 计数 > 0 ──

        // PresenceCountValue 是 PresenceDependent，实现该接口即由 EventRefCollector 自动开启 Presence 全域扫描。

        m.put(StatConditionIds.COND_PRESENCE_TOTEM,

                List.of(new CompareValue(new PresenceCountValue(PresenceCountValue.Relation.ALLY, PresenceCountValue.TOTEM),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_PRESENCE_ALLY,

                List.of(new CompareValue(new PresenceCountValue(PresenceCountValue.Relation.ALLY, PresenceCountValue.ANY),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // presence_count:ally.summoned > 0 —— Companion 在本模组忠实映射为盟友召唤物（minion 写 coe:summoned 键）。

        m.put(StatConditionIds.COND_PRESENCE_ALLY_SUMMONED,

                List.of(new CompareValue(new PresenceCountValue(PresenceCountValue.Relation.ALLY, PresenceCountValue.SUMMONED),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // 「你的同伴在你身旁」：与 COND_PRESENCE_ALLY_SUMMONED 同义，但经 BindExpr(SOURCE) 切到交互对方（自侧/玩家）求值，

        // 供 onTarget 条目的「目标侧 beast + 自侧 companion」复合门控（#33）。非命中场景 sourceEntity 为 null → 返回 0，安全兜底。

        m.put(StatConditionIds.COND_SOURCE_HAS_COMPANION_PRESENCE,

                List.of(new BindExpr(ValueTarget.SOURCE, new CompareValue(

                        new PresenceCountValue(PresenceCountValue.Relation.ALLY, PresenceCountValue.SUMMONED),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN))));

        // ── 能量护盾在场 / 满盾：当前 ES > 0 / ES >= 上限 ──

        m.put(StatConditionIds.COND_HAS_ENERGY_SHIELD,

                List.of(new CompareValue(new HealthValue("energy"), new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        m.put(StatConditionIds.COND_FULL_ENERGY_SHIELD,

                List.of(new CompareValue(new HealthValue("energy"), new HealthValue("max_energy"), 1.0, 1.0, ComparisonOperator.GREATER_THAN_OR_EQUAL)));

        // ── 暴击命中：damage.crit > 0 ──

        // DamageValue 单参构造走 CONTEXT 源 → 中缀 damage.crit → DamageContext.isCrit()（伤害管线判定后写入）。

        // 无伤害上下文（非伤害阶段）时 DamageValue 返回 0，条件自然为假，不会误触发。

        m.put(StatConditionIds.COND_CRIT_HIT,

                List.of(new CompareValue(new DamageValue("crit"), new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // ── 已镶嵌辅助宝石数：socket:<color>.support >= socket_n ──

        // GemValue 的 aggregate=count：遍历装配槽，按颜色（id 首段）与种类（support）计数；非玩家返回 0。

        // 阈值参数化（VarRef("socket_n")）沿用距离簇范式——同一 COND 服务阈值 5 与 10 两组条目。

        m.put(StatConditionIds.COND_SOCKET_RED_SUPPORT, List.of(socketSupportCond("red")));

        m.put(StatConditionIds.COND_SOCKET_BLUE_SUPPORT, List.of(socketSupportCond("blue")));

        m.put(StatConditionIds.COND_SOCKET_GREEN_SUPPORT, List.of(socketSupportCond("green")));

        // ── 近 8 秒造成过近战命中 ──

        m.put(StatConditionIds.COND_COUNT_CAUSED_MELEE_HIT_8S,

                List.of(new CompareValue(

                        new EventHistoryValue(EventName.CAUSED_HIT, Optional.of(GateTag.MELEE),

                                Event.State.Match.EXACT, EventHistoryModes.COUNT, EventHistoryValue.ticks(8.0)),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // ── 近期（4s）造成过近战命中：与上式同形，窗口改 4s（语料 "recently"）──

        m.put(StatConditionIds.COND_COUNT_RECENTLY_CAUSED_MELEE_HIT,

                List.of(new CompareValue(

                        new EventHistoryValue(EventName.CAUSED_HIT, Optional.of(GateTag.MELEE),

                                Event.State.Match.EXACT, EventHistoryModes.COUNT, EventHistoryValue.ticks(4.0)),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // ── 近 8 秒造成过弹射命中：标签改 projectile ──

        m.put(StatConditionIds.COND_COUNT_CAUSED_PROJECTILE_HIT_8S,

                List.of(new CompareValue(

                        new EventHistoryValue(EventName.CAUSED_HIT, Optional.of(GateTag.PROJECTILE),

                                Event.State.Match.EXACT, EventHistoryModes.COUNT, EventHistoryValue.ticks(8.0)),

                        new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN)));

        // ── 近期未造成暴击 ──

        m.put(StatConditionIds.COND_NOT_RECENTLY_CAUSED_CRIT, List.of(EventHistoryValue.notRecently(EventName.CAUSED_CRIT)));

        // ── 近期（4s）被该元素伤害命中：per-element 扇出，供「每种近期承受过的元素伤害类型 +X% 最大抗性」──

        // 数据由 DamagePipeline:860 recordTaggedHit(TAKEN_HIT, recencyTags) 写入，RECENCY_TAGS 白名单含各元素。

        // 直连 List.of(EventHistoryValue...)：RECENTLY 布尔模式（问「是否发生过」，非 COUNT），与 :281 同形。

        m.put(StatConditionIds.COND_RECENTLY_TAKEN_HIT_FIRE, List.of(EventHistoryValue.recently(EventName.TAKEN_HIT, GateTag.FIRE)));

        m.put(StatConditionIds.COND_RECENTLY_TAKEN_HIT_COLD, List.of(EventHistoryValue.recently(EventName.TAKEN_HIT, GateTag.COLD)));

        m.put(StatConditionIds.COND_RECENTLY_TAKEN_HIT_LIGHTNING, List.of(EventHistoryValue.recently(EventName.TAKEN_HIT, GateTag.LIGHTNING)));

        // ── 参数化时间窗口簇 ──

        // 与 COND_COUNT_CAUSED_MELEE_HIT_8S 同形：count 模式 + 显式窗口（秒 → tick）。

        // 三点通道核验见 StatConditionIds 中本簇的注释（发射点 / 无标签队列 / 自动订阅）。

        m.put(StatConditionIds.COND_COUNT_CAUSED_CRIT_8S,

                List.of(countWithinSecs(EventName.CAUSED_CRIT, 8.0)));

        m.put(StatConditionIds.COND_COUNT_USE_FLASK_10S,

                List.of(countWithinSecs(EventName.USE_FLASK, 10.0)));

        // 魔力药剂专用：USE_FLASK 发射时把 flask.resource 并入 fireTags（{flask, <resource>}），

        // 故按 {mana} 子标签过滤即可与生命药剂区分。

        m.put(StatConditionIds.COND_COUNT_USE_MANA_FLASK_10S,

                List.of(countWithinSecs(EventName.USE_FLASK, 10.0, GateTag.MANA)));

        m.put(StatConditionIds.COND_COUNT_CONSUME_GLORY_20S,

                List.of(countWithinSecs(EventName.CONSUME_GLORY, 20.0)));

        // DoT 门控走 recently / not_recently（只问「是否发生过」，不问次数），

        // 窗口为语料的 1 秒——recently() 便捷构造固定 4 秒不可用，须显式给窗口。

        m.put(StatConditionIds.COND_RECENTLY_TAKEN_DOT_1S,

                List.of(new EventHistoryValue(EventName.TAKEN_DOT, EventHistoryModes.RECENTLY, EventHistoryValue.ticks(1.0))));

        m.put(StatConditionIds.COND_NOT_RECENTLY_TAKEN_DOT_1S,

                List.of(new EventHistoryValue(EventName.TAKEN_DOT, "not_recently", EventHistoryValue.ticks(1.0))));

        m.put(StatConditionIds.COND_NOT_RECENTLY_ATTACK_START,

                List.of(EventHistoryValue.notRecently(EventName.ATTACK_START)));

        // ── 灌入消耗门控 ──

        // 语料 "Recently" 正对应 recently() 便捷构造的固定 4 秒窗口，无需显式给 ticks。

        m.put(StatConditionIds.COND_RECENTLY_CONSUME_INFUSION,

                List.of(EventHistoryValue.recently(EventName.CONSUME_INFUSION)));

        // ── 尸体消耗门控 ──

        m.put(StatConditionIds.COND_RECENTLY_CONSUME_CORPSE,

                List.of(EventHistoryValue.recently(EventName.CONSUME_CORPSE)));

        // ── 召唤物死亡门控 ──

        // 发射点 DamageEvent.LivingDeathEvent（记在主人名下），走 GameEvent.emit 无标签队列 → recently() 可命中。

        m.put(StatConditionIds.COND_RECENTLY_MINION_DIED,

                List.of(EventHistoryValue.recently(EventName.MINION_DEATH)));

        // ── ES 充能启动门控（2 秒窗口；发射点 AttributeEvent ES 充能分支，边沿触发）──

        m.put(StatConditionIds.COND_ES_RECHARGE_STARTED_2S,

                List.of(EventHistoryValue.recently(EventName.ES_RECHARGE_STARTED, EventHistoryValue.ticks(2.0))));

        // ── 药水效果生效中门控（FlaskActiveValue 读 FLASK_ACTIVE 附件；R3 读点由 FlaskHelper.tick 真实填充）──

        m.put(StatConditionIds.COND_FLASK_ACTIVE_ANY, List.of(new FlaskActiveValue("")));

        m.put(StatConditionIds.COND_FLASK_ACTIVE_MANA, List.of(new FlaskActiveValue("mana")));

        m.put(StatConditionIds.COND_FLASK_ACTIVE_HEALTH, List.of(new FlaskActiveValue("health")));

        // ── 药剂剩余次数门控（FlaskUsesValue 读 FLASK_SAVE 各槽充能 ÷ 单次消耗）──

        // 「no uses left」= 剩余次数 < 1。空槽 / 未装药剂时 FlaskUsesValue 返 0，同样 < 1 → 条件成立，

        // 与 PoE2「没得喝就是 0 次」一致（见 FlaskUsesValue 类注释）。

        m.put(StatConditionIds.COND_NO_HEALTH_FLASK_USES_LEFT, List.of(new CompareValue(

                new FlaskUsesValue("health"), new ConstantValue(1.0), 1.0, 1.0, ComparisonOperator.LESS_THAN)));

        // ── 护符效果在身 ──

        m.put(StatConditionIds.COND_CHARM_ACTIVE_ANY, List.of(new CharmActiveValue()));

        // ── 敌种类别 ──

        // 成员表由 datapack entity_type tag 定义（data/coe/tags/entity_type/{undead,humanoid,demon,construct}.json），

        // 节点只持 tag 引用、求值期延迟解析 —— 类别成员可自由增删而无需改代码（四类互斥）。

        // 求值：EntityTypeValue.matches(living) → 1.0/0.0；配合 onTarget 时 living 即被判定目标（R3 安全）。

        m.put(StatConditionIds.COND_ENEMY_TYPE_UNDEAD, List.of(EntityTypeValue.of(CraftOfExile.MODID, "undead")));

        m.put(StatConditionIds.COND_ENEMY_TYPE_HUMANOID, List.of(EntityTypeValue.of(CraftOfExile.MODID, "humanoid")));

        m.put(StatConditionIds.COND_ENEMY_TYPE_DEMON, List.of(EntityTypeValue.of(CraftOfExile.MODID, "demon")));

        m.put(StatConditionIds.COND_ENEMY_TYPE_CONSTRUCT, List.of(EntityTypeValue.of(CraftOfExile.MODID, "construct")));

        // beast：复用既建 data/coe/tags/entity_type/beast.json（17 实体），同族延迟解析、可热更、R3 安全。

        m.put(StatConditionIds.COND_ENEMY_TYPE_BEAST, List.of(EntityTypeValue.of(CraftOfExile.MODID, "beast")));

        // ── 捷在身门控：5 个单捷按状态 id；any 按 kind=HERALD（新增捷只需在 HeraldStatusTypes 加一行）──

        m.put(StatConditionIds.COND_ANY_HERALD, List.of(StatusValue.byKind("herald")));

        m.put(StatConditionIds.COND_HERALD_OF_ASH, List.of(new StatusValue(StatusDefs.HERALD_OF_ASH.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_HERALD_OF_ICE, List.of(new StatusValue(StatusDefs.HERALD_OF_ICE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_HERALD_OF_THUNDER, List.of(new StatusValue(StatusDefs.HERALD_OF_THUNDER.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_HERALD_OF_PLAGUE, List.of(new StatusValue(StatusDefs.HERALD_OF_PLAGUE.id, CraftOfExile.EMPTY_LOCATION)));

        m.put(StatConditionIds.COND_HERALD_OF_BLOOD, List.of(new StatusValue(StatusDefs.HERALD_OF_BLOOD.id, CraftOfExile.EMPTY_LOCATION)));

        // ── 使用元素技能门控（引擎原语，复用 SkillEvent.activeSkills 实时数据源，R3 安全）──

        // element=cold/fire 遍历技能定义递归判定含对应元素 DamageAction；element=any 仅判 caster 在场 activeSkills。

        m.put(StatConditionIds.COND_USING_COLD_SKILL, List.of(new UsingSkillValue("cold")));

        m.put(StatConditionIds.COND_USING_FIRE_SKILL, List.of(new UsingSkillValue("fire")));

        m.put(StatConditionIds.COND_USING_SKILL, List.of(new UsingSkillValue("any")));

        // ── 归一 B 类：条件节点（复用 COND_CHANNELLING / COND_RECENTLY_CAUSED_STUN 范式，仅加数据条目）──

        // NO_REGEN_ON_BLEED：仅「近期对敌施加过流血」时授予（镜像 COND_RECENTLY_CAUSED_STUN：事件=CAUSED_AILMENT，标签=bleed）。

        m.put(StatConditionIds.COND_INFLICT_BLEED, List.of(EventHistoryValue.recently(EventName.CAUSED_AILMENT, StatusDefs.BLEED.id)));

        return m;

    }



    /**

     * 「近 {@code seconds} 秒内发生过 {@code event} 至少一次」——{@code COUNT} 模式 + 显式窗口。

     * <p>窗口按语料实测值逐条给定（8 / 10 / 20 秒），故抽成参数化工厂而非各写一个常量 COND：

     * 三处唯一差异就是事件名与窗口长度，形状完全一致。

     */

    private static ValueExpr countWithinSecs(String event, double seconds) {

        return countWithinSecs(event, seconds, null);

    }



    /** 带子标签过滤的事件计数（{@code tag} 非 null 时按 PREFIX 匹配，如 USE_FLASK + {mana}）。 */

    private static ValueExpr countWithinSecs(String event, double seconds, @Nullable String tag) {

        return new CompareValue(

                new EventHistoryValue(event, Optional.ofNullable(tag),

                        tag == null ? Event.State.Match.EXACT : Event.State.Match.PREFIX,

                        EventHistoryModes.COUNT, EventHistoryValue.ticks(seconds)),

                new ConstantValue(0.0), 1.0, 1.0, ComparisonOperator.GREATER_THAN);

    }



    /**

     * 「已镶嵌 {@code <color>} 辅助宝石数 {@code >= socket_n}」——{@code GemValue} 的 count 聚合与阈值参数比较。

     * <p>阈值走条目参数而非硬编码，使同一 COND 可服务语料里 5 与 10 两组阈值

     * （{@code at least 5 Blue Support Gems} / {@code at least 10 Red Support Gems}）。

     */

    private static ValueExpr socketSupportCond(String color) {

        return new CompareValue(

                new GemValue(Optional.empty(), Optional.empty(), Optional.of(color), "support", Optional.empty(), GemAggregates.COUNT),

                new VarRef("socket_n"), 1.0, 1.0, ComparisonOperator.GREATER_THAN_OR_EQUAL);

    }



    /**

     * 具名条件集的<b>对象解析</b>：把 {@code StatConditionIds.COND_*} 具名常量解成真实的

     * 条件节点列表（接线层全程按对象流转，身份由具名常量保证，无内容哈希）。

     * 未注册即抛错（fail-fast）。

     *

     * <p><b>条件段不落盘</b>：调用方拿到列表后交给 {@link com.cimdy.coe.data.codec.support.math.context.node.ValueExpr#of}，

     * 压成<b>单个</b>复式节点（

     * {@link com.cimdy.coe.data.codec.support.math.context.nodes.scope.ValueExprGroup}）

     * 直接内联进条目，故没有 {@code stat/condition/*.json} 这一层产物。

     */

    public static List<ValueExpr> conditions(Identifier id) {

        List<ValueExpr> cs = contents().get(id);

        if (cs == null) {

            throw new IllegalStateException("未注册的 stat_condition id: " + id);

        }

        return cs;

    }



}