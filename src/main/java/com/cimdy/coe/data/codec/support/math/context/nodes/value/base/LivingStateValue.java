package com.cimdy.coe.data.codec.support.math.context.nodes.value.base;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.NearbyEnemyCountValue;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.status.def.StatusKind;

import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventRecencyValue;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;
import com.cimdy.coe.helper.DodgeRollHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * 玩家战斗状态读值节点（docs/entity_condition_statistical_affixes.md §1.2）。
 * <p>
 * 各 mode 语义（2026-07 备忘，与 §1.2 文档一致）：
 * <ul>
 *   <li>{@code channelling} —— 引导 = 当前 stat 作用域携带 {@code channeling} 技能标签（施放需要长按的技能），
 *       由 {@link StatScopes#current} 读取当前作用域判定（引导是技能标签，非实体状态标志）。</li>
 *   <li>{@code parrying} —— 招架 = 近期状态存储，读 {@code Event} 缓存的 {@code parry} 事件
 *       （招架记录已接线：{@code EntityOperationEvent} 在招架姿态进入时 {@code Event.record(PARRY)}，
 *       且 PARRY 属事件订阅机制白名单恒记录）。</li>
 *   <li>{@code leeching} —— 吸取 = 三类吸取（Life/Mana/Energy）回调到近期状态，读 {@code leech_health/leech_mana/leech_energy} 任一近期发生即视为吸取中。</li>
 *   <li>{@code dual_wield} —— 双持 = 主手 + 副手皆持武器（斧/剑等），且副手非盾。</li>
 *   <li>{@code holding_shield} —— 副手持盾 = 副手槽为 {@link ShieldItem}。</li>
 *   <li>{@code sprinting} —— 冲刺/奔跑 = 实体正处于奔跑状态（{@code isSprinting()}）。</li>
 *   <li>{@code low_life} —— 残血 = 当前生命 < 50% 最大生命。</li>
 *   <li>{@code low_mana} —— 低魔力 = 当前魔力 < 50% 最大魔力。</li>
 *   <li>{@code blocking} —— 格挡 = 实体当前正处于举盾格挡姿态（{@code LivingEntity#isBlocking()}）。</li>
     *   <li>{@code shapeshifted} —— 变形 = 实体正处于任一形态状态（熊/狼/龙，{@link FormStatusTypes}
     *        经 {@code StatusEngine#has} 判定），用于「变形时伤害提高」类词缀门控。</li>
     *   <li>{@code archon} —— 受 Archon Buff 影响 = 实体当前持有任一带 {@code archon} 标签的状态
     *        （{@link StatusEngine#hasArchon} 中央互斥判定），用于「受 Archon 影响时 X」类词缀门控。</li>
     * </ul>
 * 数据未就绪（无事件回调 / 无技能标志）时返回 0，符合「近期先实现逻辑、没存就是没有」原则。
 */
public record LivingStateValue(String mode, double window) implements EntityNode {
    public static final Codec<LivingStateValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("mode").forGetter(LivingStateValue::mode),
            Codec.DOUBLE.optionalFieldOf("window", 80.0).forGetter(LivingStateValue::window)
    ).apply(instance, LivingStateValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        // 支持 ! 前缀表示「非」语义（如 !recently_took_dot = 近期未承受 DoT），用于 heartstopper 基石的双分支门控。
        // 仅作解析层处理，record 的 mode() getter 仍返回原始（含 !）字符串，序列化可原样往返。
        String m = mode;
        boolean negated = false;
        if (m.startsWith("!")) {
            negated = true;
            m = m.substring(1);
        }
        double result = switch (m) {
            case "channelling" -> StatScopes.current(living).tags().contains(GateTag.CHANNELING) ? 1D : 0D;
            case "parrying" -> new EventRecencyValue("parrying", window).evaluate(scope);
            case "leeching" -> new EventRecencyValue("leeching", window).evaluate(scope);
            case "leeching_mana" -> new EventRecencyValue("leeching_mana", window).evaluate(scope);
            case "dual_wield" -> dualWield(living) ? 1D : 0D;
            case "holding_shield" -> holdingShield(living) ? 1D : 0D;
            case "sprinting" -> living.isSprinting() ? 1D : 0D;
            case "low_life" -> (living.getMaxHealth() > 0
                    && living.getHealth() < 0.5 * living.getMaxHealth()) ? 1D : 0D;
            case "low_mana" -> {
                double max = ResourceRegistry.max(living, "mana");
                yield (max > 0 && ResourceRegistry.current(living, "mana") < 0.5 * max) ? 1D : 0D;
            }
            /** 满魔力：当前魔力 ≥ 最大魔力（与 low_mana 对称）。 */
            case "full_mana" -> {
                double max = ResourceRegistry.max(living, "mana");
                yield (max > 0 && ResourceRegistry.current(living, "mana") >= max) ? 1D : 0D;
            }
            case "blocking" -> living.isBlocking() ? 1D : 0D;
            case "shapeshifted" -> StatusType.all().stream().anyMatch(t -> t.kind == StatusKind.FORM && StatusEngine.has(living, t)) ? 1D : 0D;
            /** archon：受「Archon Buff」影响（任一带 archon 标签的状态生效中，由 StatusEngine 中央互斥判定）。 */
            case "archon" -> StatusEngine.hasArchon(living) ? 1D : 0D;
            case "stationary" -> !moving(living) ? 1D : 0D;
            case "moving" -> moving(living) ? 1D : 0D;
            /** 被包围：PoE2 口径 = 3 米内至少 5 个敌对实体（见 {@link #surrounded}）。 */
            case "surrounded" -> surrounded(living) ? 1D : 0D;
            // ── Stage 2 新增条件模式（忠实基石门控） ──
            /** 满血：当前生命 ≥ 最大生命。 */
            case "full_life" -> (living.getMaxHealth() > 0
                    && living.getHealth() >= living.getMaxHealth()) ? 1D : 0D;
            /** 副手空：副手槽无物品。 */
            case "off_hand_empty" -> offHandEmpty(living) ? 1D : 0D;
            /** 双手皆空：主手与副手槽均无物品（供「徒手攻击视同持长杖」门控）。 */
            case "both_hands_empty" -> bothHandsEmpty(living) ? 1D : 0D;
            /** 主手持单手 martial 武器（剑/斧/锤；本游戏模型中即「单手近战武器」）。 */
            case "one_handed_martial" -> oneHandedMartial(living) ? 1D : 0D;
            /** 主手持斧：斧类近战武器（复用 ItemTypeChecker.isAxe，与 one_handed_martial 同判定源）。 */
            case "wielding_axe" -> wieldingAxe(living) ? 1D : 0D;
            /** 主手持长杖：quarterstaff 武器标签（无 EquipmentGen 类型，仅以 ItemTag 标识，与 DamageEvent 攻击标签同源判定）。 */
            case "wielding_quarterstaff" -> wieldingQuarterstaff(living) ? 1D : 0D;
            /** 主手持近战武器（剑/斧/锤；与 one_handed_martial 同判定源，复用 ItemTypeChecker.isX）。 */
            case "wielding_melee_weapon" -> wieldingMeleeWeapon(living) ? 1D : 0D;
            /** 主手持匕首：dagger 武器标签（无 EquipmentGen 类型，仅以 ItemTag 标识，与 DamageEvent 攻击标签同源判定）。 */
            case "wielding_dagger" -> wieldingDagger(living) ? 1D : 0D;
            /** 主手持双手近战武器：coe:items/two_handed 标签（镜像 wielding_dagger 仅以 ItemTag 标识）。 */
            case "wielding_two_handed_melee" -> wieldingTwoHandedMelee(living) ? 1D : 0D;
            /** 翻滚闪避：由 {@link com.cimdy.coe.helper.DodgeRollHelper#isRolling} 真读翻滚附件状态。
             *  翻滚窗口内（≈0.6s）为 1，否则为 0；非玩家实体无翻滚附件恒为 0。
             *  翻滚系统已落地，故「翻滚时减伤/增伤」「翻滚时免冰缓」等门控现可真实触发（bulwark 基石生效）。 */
            case "dodge_rolling" -> DodgeRollHelper.isRolling(living) ? 1D : 0D;
            /** 近期承受 DoT：近 window tick 内被施加过持续伤害（AilmentHelper.applyDamageDoT 已记录 TAKEN_DOT）。 */
            case "recently_took_dot" -> new EventRecencyValue("recently_took_dot", window).evaluate(scope);
            /** 近期获得过怒气：RageHelper 每次获怒 refreshTimer(RAGE_GAINED)，timer 通道（B）恒活，等价「recently gained rage」；与 WireRecent 标注的 Event-count 死键无涉。 */
            case "recently_gained_rage" -> new EventRecencyValue("recently_gained_rage", window).evaluate(scope);
            /** 全部召唤物均为同伴：见 {@link #allMinionsAreCompanions}（归属枚举 + 逐个判 IS_COMPANION，至少一个）。 */
            case "all_minions_are_companions" -> allMinionsAreCompanions(living) ? 1D : 0D;
            default -> 0D;
        };
        return negated ? (result > 0 ? 0D : 1D) : result;
    }

    /**
     * 持有者的召唤物是否<b>全部</b>为同伴（Companion）。
     * <p>按 {@link RelationCondition#resolveMinions} 的归属口径枚举「你的召唤物」（owner_uuid 等于持有者），
     * 再逐个判 {@code IS_COMPANION} 附件标记——Companion 在本模组是<b>召唤物上的标记</b>而非独立实体类。
     * <p><b>至少一个</b>才可能为真：无召唤物时返回 false，使「全部（空集）为同伴」不成立，
     * 避免无召唤物的配装无条件吃到「if all your Minions are Companions」加成（过度授予）。
     */
    private static boolean allMinionsAreCompanions(LivingEntity living) {
        List<LivingEntity> minions = RelationCondition.resolveMinions(living);
        if (minions.isEmpty()) return false;
        for (LivingEntity minion : minions) {
            // 原 AttachmentRegistry.IS_COMPANION 已迁入 AdditionData（key=is_companion）
            if (AdditionData.getOrDefault(minion, AdditionKeys.IS_COMPANION, 0.0) == 0.0) return false;
        }
        return true;
    }

    /** 移动判定：水平移动向量非零即视为移动中。 */
    private static boolean moving(LivingEntity living) {
        return living.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    /**
     * 被包围判定 —— PoE2 官方口径：<b>{@value #SURROUNDED_RADIUS} 米内至少
     * {@value #SURROUNDED_MIN_ENEMIES} 个敌人</b>。
     * <p>
     * 敌对判定与计数复用 {@link NearbyEnemyCountValue#count}（{@code EntityQuery.sphere} 球形扫描 +
     * 排除自身 + 仅存活 + {@code !isAlliedTo}），与「每个近距离敌人 +X」缩放族同一套口径。
     * <p>
     * 敌对扫描须覆盖<b>全部</b>实体而非仅 {@code level.players()}：只扫玩家时单人存档下恒无敌人，
     * 全部 {@code WHILE_SURROUNDED} 词条会虽已接线却永不触发（静默死线）。
     */
    private static boolean surrounded(LivingEntity living) {
        return NearbyEnemyCountValue.count(living, SURROUNDED_RADIUS) >= SURROUNDED_MIN_ENEMIES;
    }

    /** 「被包围」判定半径（方块 = 米），PoE2 官方定义为 3 米。 */
    public static final double SURROUNDED_RADIUS = 3.0;

    /** 「被包围」所需最少敌人数，PoE2 官方定义为 5 个。 */
    public static final int SURROUNDED_MIN_ENEMIES = 5;

    private static boolean dualWield(LivingEntity living) {
        ItemStack main = living.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack off = living.getItemBySlot(EquipmentSlot.OFFHAND);
        return !main.isEmpty() && !off.isEmpty()
                && !ModifiableStateOps.equipment(off).isShield()
                && !ModifiableStateOps.equipment(off).isBuckler();
    }

    private static boolean holdingShield(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.OFFHAND));
        return state.isShield() || state.isBuckler();
    }

    /** 副手槽为空（无武器 / 盾 / 护符等）。 */
    private static boolean offHandEmpty(LivingEntity living) {
        return living.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }

    /** 主手与副手槽均为空（「双手皆空」）。 */
    private static boolean bothHandsEmpty(LivingEntity living) {
        return living.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
                && living.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }

    /** 主手持单手 martial 武器（剑 / 斧 / 锤；本游戏模型下即「单手近战武器」类别）。 */
    private static boolean oneHandedMartial(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.isSword(state) || ItemTypeChecker.isAxe(state) || ItemTypeChecker.isMace(state);
    }

    /** 主手持斧（斧类近战武器）。 */
    private static boolean wieldingAxe(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.isAxe(state);
    }

    /** 主手持长杖：改按类型链判定（ItemWeight 声明 → EquipmentState.types），与 {@link #wieldingAxe} 同口径。 */
    private static boolean wieldingQuarterstaff(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.hasType(state, EquipmentGen.QUARTERSTAFF);
    }

    /** 主手持近战武器（剑 / 斧 / 锤；与 {@link #oneHandedMartial} 同判定源，复用 ItemTypeChecker.isSword/isAxe/isMace）。 */
    private static boolean wieldingMeleeWeapon(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.isSword(state) || ItemTypeChecker.isAxe(state) || ItemTypeChecker.isMace(state);
    }

    /** 主手持匕首：改按类型链判定（dagger 叶类别），与 {@link #wieldingQuarterstaff} 同口径。 */
    private static boolean wieldingDagger(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.hasType(state, EquipmentGen.DAGGER);
    }

    /** 主手持双手近战武器：改按类型链判定（two_hand 节点），不再依赖可能为空的内容侧标签。 */
    private static boolean wieldingTwoHandedMelee(LivingEntity living) {
        EquipmentState state =ModifiableStateOps.equipment(living.getItemBySlot(EquipmentSlot.MAINHAND));
        return ItemTypeChecker.hasType(state, EquipmentGen.TWO_HAND);
    }

    /**
     * 条件段：{@code stat.condition.living_state = "while %s"}。
     * 取反（{@code "!"} 前缀）拆成「{@code stat.value.neg.not} + 模式值键」两个可复用片段组合，
     * 而非为「not X」每种模式各铸一个整句键（架构裁定 #3/#4）；
     * 键缺失时回落到 humanize 英文（含 {@code "not "} 前缀），英文输出逐字不变。
     */
    @Override
    public List<Component> describeFragment() {
        String m = mode();
        boolean neg = m.startsWith("!");
        String base = neg ? m.substring(1) : m;
        Component value = StatValueKey.of("living_state", base);
        Component arg = neg
                ? Component.empty().append(StatValueKey.not()).append(Component.literal(" ")).append(value)
                : value;
        return List.of(Component.translatable("stat.condition.living_state", arg));
    }
}
