package com.cimdy.coe.data.codec.support.stat.effect.base;
import com.cimdy.coe.data.codec.support.stat.effect.GateTypeIds;

import com.cimdy.coe.entity.custom.skill.visual.SkillBanner;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.skill.helper.cast.ProjectileHelper;
import com.cimdy.coe.data.codec.talent.effect.TalentEffectType;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.helper.stat.AccessoryStatHelper;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.combat.HitScopeHelper;
import com.cimdy.coe.data.helper.skill.PlayerCastHelper;
import com.cimdy.coe.data.codec.support.stat.effect.damage.AggravateBleedingStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.ChargeConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.CreateGroundOnAilmentEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.CreateGroundStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.damage.CullingStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.damage.DamageStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageTakenConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.DeliveryStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.HitFlagStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.ImmuneStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.LeechStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.LuckyStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ManaSpentArchonStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.MultiStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.grant.SyntheticChillStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.damage.PreventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.RageConsumeHealStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.RecoupStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.marker.RequirementStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.convert.ResourceConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.damage.ShockEchoStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.status.StatusStatEffect;
import com.cimdy.coe.data.helper.engine.ground.CreateGroundDriver;

import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.HashMap;
import java.util.Map;
import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.helper.engine.DamagePipeline;
import com.cimdy.coe.data.helper.engine.EquipRules;
import com.cimdy.coe.data.helper.engine.InvocationTicker;

/**
 * Stat 效果类型注册表：每种 {@link StatEffect} 对应一个 MapCodec，通过 JSON "type" 字段分发解码。
 * 新增效果类型只需在此 {@link #register(String, MapCodec)} 即可被数据驱动地解析。
 * 与 {@code TalentEffectType} / {@code ConditionRegistry} 同一注册模式。
 * <p>
 * 注意：效果 codec 只解析<b>身份 / 语义字段与 tags</b>；数值 / 条件 / 选择器由外层
 * {@link StatEntry} 的 codec 平铺解析，二者在同一 JSON 对象层级拼接。
 */
public final class StatEffectTypes {
    private static final Map<String, MapCodec<? extends StatEffect>> REGISTRY = new HashMap<>();

    private StatEffectTypes() {
    }

    public static <T extends StatEffect> void register(String id, MapCodec<T> codec) {
        REGISTRY.put(id, codec);
    }

    /** 未知效果类型：decode/encode 恒失败，使拼写错误在加载期显式暴露（而非静默回退 attribute）。
     *  Encoder/Decoder 为泛型抽象方法接口，不可用 lambda，故用匿名类；经 {@code fieldOf} 升格为 MapCodec。 */
    private static final MapCodec<StatEffect> UNKNOWN_TYPE = new Codec<StatEffect>() {
        @Override
        public <T> DataResult<T> encode(StatEffect input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "cannot encode unknown stat effect type");
        }

        @Override
        public <T> DataResult<com.mojang.datafixers.util.Pair<StatEffect, T>> decode(
                com.mojang.serialization.DynamicOps<T> ops, T input) {
            return DataResult.error(() -> "unknown stat effect type");
        }
    }.fieldOf("type");

    public static MapCodec<? extends StatEffect> codecFor(String id) {
        MapCodec<? extends StatEffect> codec = REGISTRY.get(id);
        // HashMap.getOrDefault 在「键存在但值为 null」时返回 null（不回落 default），
        // 经 KeyDispatchCodec.getCodec 透传成 elementEncoder=null 抛 NPE。
        // 运行期各效果 codec 已就绪，注册期若取到 null 则回落 UNKNOWN_TYPE（编码期显式报错而非崩溃）。
        return codec != null ? codec : UNKNOWN_TYPE;
    }

    /** 类型是否已注册（含 UNKNOWN_TYPE 之外的全部）：供 datagen 落盘前一次性自检，
     *  避免「每跑一次 rundata 才炸一个未注册 effect 类型」的逐个试错。 */
    public static boolean isRegistered(String id) {
        return REGISTRY.containsKey(id);
    }

    /** 分发 MapCodec：依据 "type" 字段选择对应效果，字段与条目其余部分平铺同层。 */
    public static final MapCodec<StatEffect> MAP_CODEC = Codec.STRING.dispatchMap(
            "type",
            StatEffect::type,
            StatEffectTypes::codecFor
    );

    /** 分发编解码（独立对象形式），供不依附条目的场景使用。 */
    public static final Codec<StatEffect> CODEC = MAP_CODEC.codec();

    static {
        // 属性亚型，兼两种形态：不给 duration = 常驻折叠（读时重估门控）；
        // 给 duration = 临时属性授予（fire 过筛 → 物化无条件临时条目 → getAttributeValue 直读，
        // 条件判定固定在事件时刻、读取端不重估，到期由 StatStore.tick() 自灭）。
        register("attribute", ModifierStatEffect.CODEC);
        register("heal", ResourceStatEffect.CODEC);
        register("damage", DamageStatEffect.CODEC);
        register("status", StatusStatEffect.CODEC);
        register("resource_convert", ResourceConvertStatEffect.CODEC);
        // 伤害类型转换（含随机/轮流元素，由 DamageConvertStatEffect.toMode 统一：FIXED/RANDOM/CYCLE）。
        register("damage_convert", DamageConvertStatEffect.CODEC);
        // 承伤转化（含随机/轮流元素，由 DamageTakenConvertStatEffect.toMode 统一：FIXED/RANDOM/CYCLE）。
        register("damage_taken_convert", DamageTakenConvertStatEffect.CODEC);
        // 伤害事件驱动的资源回复（原管线硬编码的「外接机制」已抽离为 Effect，管道返回值现在只修饰伤害）
        register("leech", LeechStatEffect.CODEC);
        register("recoup", RecoupStatEffect.CODEC);
        // 造成异常时于承受方位置投放地面（Ground 子系统事件驱动创建）：「on [造成 shock/freeze] 落 [地面]」
        register("create_ground_on_ailment", CreateGroundOnAilmentEffect.CODEC);
        // 事件授予（chance → 状态/充能/连击/资源）：「X% chance on [事件] to [授予]」
        register("grant_on_event", GrantOnEventStatEffect.CODEC);
        register("aggravate_bleeding", AggravateBleedingStatEffect.CODEC);
        // 充能获得事件改写（chance → 转换/满层/盟友同获）：「Gain [B] instead of [A] / gain max / Allies gain the same」
        register("charge_convert", ChargeConvertStatEffect.CODEC);
        // 受击转能量已并入 recoup（resources 可多选含 energy，同一「承伤按比例补给」机制），不再单列 energy_gain 效果类型
        // 注：不在此注册 ailment_on_event / gain_charge 类效果——异常施加已收敛到 AilmentHelper /
        //   StatusEngine.apply 中央入口，充能授予走 ChargedState，无需独立触发效果类型。
        // 基石/核心天赋机制（fold 型查询，非触发）：状态免疫 / 双掷取优劣 / 命中绝对短路
        register("immune", ImmuneStatEffect.CODEC);
        register("lucky", LuckyStatEffect.CODEC);
        register("hit_flag", HitFlagStatEffect.CODEC);
        // 斩杀阈值（事件驱动一次性判定）：命中伤害达阈值%最大生命时击杀目标，值走 FIRE 效果自带数值（无 FOLD 属性）
        register("culling", CullingStatEffect.CODEC);
        // 投送修饰（fold 型查询）：额外投射物/穿透/链/叉/范围，供 SupportProfile 聚合（装备/天赋/珠宝词缀）
        register("delivery", DeliveryStatEffect.CODEC);
        // 复合施加（PERIODIC 型 tick 驱动）：持有任意数量的真实子效果（List<StatEffect>），由 PeriodicDriver
        // 每 interval_ticks / interval_expr 把复合条目的统一时长透传给各状态子效果、逐个调用子效果自身的
        // onTrigger 重新施加（取代原 PeriodicStatEffect 的 sealed Apply 两臂与 PresenceAuraStatEffect AURA 桶）。
        register("multi", MultiStatEffect.CODEC);
        register("synthetic_chill", SyntheticChillStatEffect.CODEC);
        // 地面生成（GROUND 型 tick 驱动）：词缀佩戴者每 interval_ticks 在脚下生成地面场（见 CreateGroundDriver）。
        // 命名区分技能动作 create_ground（ActionTypes，一次性投放）与触发型 create_ground_on_ailment。
        register("create_ground_self", CreateGroundStatEffect.CODEC);
        // ── 统一惰性标记（MarkerStatEffect，INERT 型，不进 StatStore 索引）────────────
        // 单一载体：身份 = kind（同时是 JSON type 串），消费语义 = MarkerStatEffect.Slot（= Variant + 渲染单位）。
        //
        // 【为什么必须循环注册】按 MarkerStatEffect.registeredKinds()（即 KIND_SLOTS 的键集）一次性注册，
        // 语义声明与 codec 注册共用一张表，不存在「只改一处」的中间态。若改为在下方手写 register("<id>", ...)：
        //   · 只改表不注册 → datagen 落盘期抛「cannot encode unknown stat effect type」中断 runData；
        //   · 只注册不改表 → 消费端按 variant 过滤时该 kind 落到 default Slot，语义静默错位。
        //
        // 缺任何一注册时，StatEffectGen 生成的实例走 UNKNOWN_TYPE，datagen 落盘期即抛
        // 「cannot encode unknown stat effect type」并中断整个 runData（故注册必须与表同步）。
        for (String markerKind : MarkerStatEffect.registeredKinds()) {
            register(markerKind, MarkerStatEffect.CODEC);
        }
        // 偏转命中减伤（INERT 标记：DEFLECT 簇 #32/#33，仅消费端 DamagePipeline#stageBlock 经 StatEngine.scaledMagnitude 读条目 amount 折叠；无属性）
        register("prevent", PreventStatEffect.CODEC);
        // 需求修正（inert：不进 StatStore，仅词缀生成器读取写 ItemRequire）
        register("requirement", RequirementStatEffect.CODEC);
        // 异常修正（积蓄/时长/强度/层数）已归一为「属性亚型」（type=attribute），
        // 由生成侧派生标签（ailment + 异常名 + 分类标签 + 语义面），运行时经 AilmentHelper 按作用域聚合读取，
        // 无需独立效果类型。
        // 变回人形时消耗全部怒气并按「每点怒气恢复 X% 最大生命」回血；复用 FORM_CLEARED fire（owner=玩家）
        register("rage_consume_heal", RageConsumeHealStatEffect.CODEC);
        // 电击回响：造成电击时按概率将 SHOCK 施加到被电击敌人邻域的其他敌人；复用 caused_ailment+shock fire（owner=攻击者）
        register("shock_echo", ShockEchoStatEffect.CODEC);
        // 消耗 X% 最大魔力→执政官：复用 RESOURCE_SPEND 火（owner=玩家，真实耗魔量经 eventValue 透传），施加既有 ELEMENTAL_ARCHON 状态
        register("mana_spent_archon", ManaSpentArchonStatEffect.CODEC);
    }
}
