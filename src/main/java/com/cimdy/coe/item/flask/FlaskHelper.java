package com.cimdy.coe.item.flask;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.item.mod.core.ModStatReader;

import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.flask.FlaskEffect;
import com.cimdy.coe.data.codec.item.flask.FlaskState;
import com.cimdy.coe.data.codec.item.flask.FlaskSave;
import com.cimdy.coe.registry.item.FlaskSlotRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;

import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.helper.mod.ItemStatContributor;
import com.cimdy.coe.data.helper.mod.ModContributionScope;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.helper.OwnerHelper;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * Flask 统一机制中枢（POE2 实证，严格还原）：
 * <ul>
 *   <li><b>消耗充能发动</b>：按键/右键使用消耗 {@code chargesPerUse × FLASK_CHARGES_USED} 充能
 *       （可被 {@code FLASK_NO_CONSUME_CHANCE} 豁免；充能不足则不发动）；</li>
 *   <li><b>持续恢复</b>：登记 {@code flask_active}，每 tick 回复 {@code perTick}，
 *       总量 = {@code recoveryAmount × FLASK_RECOVERY_RATE}，时长 = {@code durationTicks ÷ RECOVERY_RATE}
 *       （POE2「Recovery Rate」加速恢复而非增量，「Recovery from Flasks」才增量）；
 *       <b>目标资源已满 → 效果立即移除</b>；</li>
 *   <li><b>被动充能</b>：每 3 秒 +1 充能（× {@code FLASK_CHARGES_GAINED}），并叠加
 *       每秒 {@code FLASK_CHARGE_RATE}（{flask, 资源} 作用域）的小数累积；</li>
 *   <li><b>击杀充能</b>：击杀按 Monster Power 补充（本 mod 分档：最大生命 ≥2000 视精英/首领 +8，否则 +2）；</li>
 *   <li><b>回复通道唯一</b>：全部经 {@link ResourceEventHelper#onHeal}（sourceTag = {@code "flask"}），
 *       兼容资源转化/溢出/Guard 等回调（「回复 = 资源」最大兼容原则）。</li>
 * </ul>
 * 本类只做「事件发生 → 记账」；所有可调数值一律经 Stat 作用域读取，不含任何硬编码规则分支。
 */
public final class FlaskHelper {

    /** POE2 基础充能恢复间隔：3 秒。 */
    public static final int RECHARGE_TICKS = 60;

    /** 手持饮用（非槽位）所占用的活跃效果键。 */
    public static final int HAND_KEY = -1;

    private FlaskHelper() {
    }

    /**
     * 本瓶有效充能上限 = 基底 maxCharges × (1 + 「#% increased Charges」词缀幅度)，向上取整、最小 1。
     * 词缀幅度由 {@link ModStatReader#readRolledPercent} 直接读物品 ITEM_STAT_ENTRIES 取（scope 天然正确，非虚接）；
     * 未带该词缀则回退基底 maxCharges。各充能读写点一律经此而非 flask.maxCharges 静态字段。
     */
    public static int flaskMaxCharges(ItemStack stack, FlaskItem flask) {
        double inc = ModStatReader.readRolledPercent(stack, StatIds.FLASK_NUM_CHARGES);
        return Math.max(1, (int) Math.ceil(flask.maxCharges * (1.0 + inc)));
    }

    /**
     * 玩家持有期充能上限：在 {@link #flaskMaxCharges(ItemStack, FlaskItem)} 物品词缀上限基础上，
     * 叠加「额外药剂充能球数」标记幅度（{@code flask_num_charges}，{@code {flask}} 作用域，
     * 由 {@link StatEngine#markerMagnitude} 折叠玩家 STAT_STORE 中该标记条目幅度之和），
     * 消除该标记虚接。UI/展示侧（无持有者上下文）仍走两参版本。
     */
    public static int flaskMaxCharges(Player player, ItemStack stack, FlaskItem flask) {
        return flaskMaxCharges(stack, flask)
                + (int) StatEngine.markerMagnitude(player, MarkerStatEffect.Kind.FLASK_NUM_CHARGES, Set.of());
    }

    // ───────────────────────────── 使用 ─────────────────────────────

    /**
     * 饮用核心：充能检查 → 豁免判定 → 扣充能 → 登记持续恢复。
     *
     * @return true = 成功发动（调用方需把 stack 的充能变更写回落点）
     */
    public static boolean drink(Player player, ItemStack stack, FlaskItem flask, int activeKey) {
        if (flask.resource == null || stack.isEmpty()) return false;
        int charges = stack.getOrDefault(DataComponentRegistry.FLASK_CHARGES.get(), flaskMaxCharges(player, stack, flask));
        // 充能消耗倍率（{flask, 资源} 作用域；POE2「X% increased/reduced Flask Charges used」→ 与 CHARM_CHARGES_USED 同口径）。
        // 带资源标签读取：通用条目（GateExpr {flask}）与按资源限定条目（{flask, mana} = 「…used from Mana Flasks」）同时命中。
        double usedMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.CHARGES_USED);
        int effUsed = Math.max(0, (int) Math.round(flask.chargesPerUse * Math.max(0.0, usedMult)));
        if (charges < effUsed) return false;

        // 不消耗几率（{flask} 作用域）
        double noConsume = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK), AttributeRegistry.FLASK_NO_CONSUME_CHANCE);
        if (player.getRandom().nextDouble() >= noConsume) {
            stack.set(DataComponentRegistry.FLASK_CHARGES.get(), charges - effUsed);
        }

        // 回复总量倍率（{flask, 资源} 作用域）、恢复速率倍率（{flask, 资源} 作用域）与持续时间倍率（{flask, 资源} 作用域，
        // 与全局 RECOVERY_RATE 正交，叠乘于基底时长）。
        double amountMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.FLASK_RECOVERY_RATE);
        double rateMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.RECOVERY_RATE);
        double durMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.FLASK_DURATION);
        // 即时回复比例（"X% of Recovery applied Instantly" 词缀，{flask, 资源} 作用域）。
        double instantMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.FLASK_INSTANT_RECOVERY);
        // 品质（玻璃珠）：POE2「每 1% 品质 = 1% more 局部生命 / 魔力回复」。
        // 品质是逐栈数据（载体 FLASK_ITEM_STATE），故不可用物品静态字段，须按栈读取。
        FlaskState flaskState = ModifiableStateOps.flask(stack);
        float baseRecovery = flaskState != null ? flaskState.qualityScaledRecovery() : flask.recoveryAmount;
        // 完整 boosted 回复量（含 FLASK_RECOVERY_RATE 倍率）：供 Guard 计算与即时/持续拆分共用。
        float total = baseRecovery * (float) Math.max(0.0, amountMult);
        // 即时部分占总回复比例，饮用瞬间一次性注入（已含 FLASK_RECOVERY_RATE 倍率，经统一回复通道）。
        float instantAmount = total * (float) Math.max(0.0, instantMult);
        // 持续回复基数（不含 FLASK_RECOVERY_RATE）：每 tick 由 tickActiveEffects 重新读取该倍率，
        // 使「回复被 Stat 修饰」可随运行时属性变化动态生效（与 instant 解耦，避免重复乘算）。
        float restBase = baseRecovery * (float) (1.0 - Math.max(0.0, instantMult));
        int duration = Math.max(1, (int) Math.round(flask.durationTicks / Math.max(0.01, rateMult * durMult)));
        float basePerTick = duration > 0 ? restBase / duration : restBase;
        // 两条「回复满后行为」标记：饮用瞬间按 {flask, 资源} 作用域读取（即「加上资源类型」），
        // 烘焙进 FlaskEffect，运行时 tick 不再回查 Stat（只是标记效果）。
        boolean notRemovedWhenFull = StatEngine.gateOf(
                player, "flask_recovery_not_removed_when_full", Set.of(GateTag.FLASK, flask.resource));
        boolean canOverflow = StatEngine.gateOf(
                player, "flask_recovery_can_overflow", Set.of(GateTag.FLASK, flask.resource));

        // 即时回复（一次性），经统一回复通道（sourceTag = "flask"），兼容资源转化/溢出/Guard 等回调。
        if (instantAmount > 0f && !player.level().isClientSide()) {
            ResourceEventHelper.onHeal(player, flask.resource, instantAmount, "flask", null);
        }

        Map<Integer, FlaskEffect> active = new HashMap<>(player.getData(AttachmentRegistry.FLASK_ACTIVE));
        active.put(activeKey, new FlaskEffect(flask.resource, basePerTick, duration, notRemovedWhenFull, canOverflow));
        player.setData(AttachmentRegistry.FLASK_ACTIVE, Map.copyOf(active));

        // 本瓶 rolled 词缀（ITEM_STAT_ENTRIES，由 ModGenerator 经 AdditionType.global 算好）饮用时注入 STAT_STORE：
        // 键前缀 flask:<activeKey>:，带本瓶持续期计时域（duration）自动过期。条目 GateExpr（{flask, 资源}）与
        // 上方各读点对齐 → 词缀真正被消费（非虚接）。作用域由条目自身承载，无需在此区分资源。
        // 统一经 ItemStatContributor 注入：重键 ITEM_STAT_ENTRIES（前缀 flask:<activeKey>:）+ 计时过期。
        ItemStatContributor.contribute(player, stack, ModContributionScope.timed("flask:" + activeKey + ":", duration));

        // 「生命药剂授予 Guard」（HEALTH_FLASKS_APPLIED_TO_YOU_GRANT_GUARD...，node 15617 = 4 秒 / 8%）：
        // 按 StatId 门控，ratio 由标记幅度承载（0.08）。Guard 吸收比例 = ratio × 每秒回血 ÷ 最大生命；
        // 时长 = 4 秒 = 80 tick。经 StatusDefs.GUARD_FROM_FLASK 施加，StatusStatBridge 在激活期注入 STAT_STORE，到期自动移除（服务端）。
        if (!player.level().isClientSide() && flask.resource.equals(GateTag.HEALTH)) {
            double ratio = StatEngine.magnitudeOf(player, "flask_guard", Set.of());
            if (ratio > 0.0) {
                float maxHealth = player.getMaxHealth();
                if (maxHealth > 0) {
                    double perSecond = (total / (double) duration) * 20.0;
                    double guardFrac = ratio * perSecond / maxHealth;
                    int guardTicks = 80;
                    StatusEngine.apply(player, StatusDefs.GUARD_FROM_FLASK, null, guardFrac, guardTicks, 1);
                }
            }
        }

        if (player.level() instanceof ServerLevel) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.6F, 1.0F);
        }
        // 事件闭环：「使用药剂」on-use 事件（携带 {flask, 资源} 标签），驱动语料
        // 「Recover X% of maximum Life when you use a Mana Flask」类即时回复条目
        // （ResourceStatEffect heal 通道，tags [use_flask, mana] 精确区分药剂资源类型）。
        // 置于扣充能与恢复登记之后：只有真实发动的那一次才发射，充能不足的空按键不触发。
        GameEvent.emit(EventName.USE_FLASK, player, null, Set.of(GateTag.FLASK, flask.resource));
        return true;
    }

    /** 手持饮用（右键入口）。 */
    public static void drinkInHand(Player player, ItemStack stack, FlaskItem flask) {
        drink(player, stack, flask, HAND_KEY);
    }

    /** 按键使用指定 Flask 槽（0 = 生命药剂槽，1 = 魔力药剂槽）。 */
    public static boolean useSlot(Player player, int slotIndex) {
        if (player.level().isClientSide()) return false;
        if (slotIndex < 0 || slotIndex >= FlaskSlotRegistry.totalSlots(player.level().registryAccess())) return false;
        FlaskSave save = player.getData(AttachmentRegistry.FLASK_SAVE);
        ItemStack stack = save.get(slotIndex).copy();
        if (!(stack.getItem() instanceof FlaskItem flask)) return false;
        if (!drink(player, stack, flask, slotIndex)) return false;
        player.setData(AttachmentRegistry.FLASK_SAVE, save.with(slotIndex, stack));
        return true;
    }

    /** 「全部使用」按键：按槽序依次使用所有 Flask 槽。 */
    public static void useAll(Player player) {
        for (int i = 0; i < FlaskSlotRegistry.totalSlots(player.level().registryAccess()); i++) {
            useSlot(player, i);
        }
    }

    /**
     * 城镇水井补满：将所有槽位中未满的药剂充能置满。
     *
     * @return true = 确有未满药剂被补满（调用方据此决定是否播报）
     */
    public static boolean refillAll(Player player) {
        if (player.level().isClientSide()) return false;
        FlaskSave save = player.getData(AttachmentRegistry.FLASK_SAVE);
        FlaskSave updated = save;
        boolean changed = false;
        boolean anyFilled = false;
        for (int i = 0; i < FlaskSlotRegistry.totalSlots(player.level().registryAccess()); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof FlaskItem flask)) continue;
            int charges = stack.getOrDefault(DataComponentRegistry.FLASK_CHARGES.get(), flaskMaxCharges(player, stack, flask));
            if (charges >= flaskMaxCharges(player, stack, flask)) continue;
            ItemStack copy = stack.copy();
            copy.set(DataComponentRegistry.FLASK_CHARGES.get(), flaskMaxCharges(player, stack, flask));
            updated = updated.with(i, copy);
            changed = true;
            anyFilled = true;
        }
        if (changed) player.setData(AttachmentRegistry.FLASK_SAVE, updated);
        return anyFilled;
    }

    // ───────────────────────────── 每 tick 驱动 ─────────────────────────────

    /**
     * 服务端每 tick 驱动（PlayerTickEvent）：
     * ① 活跃 Flask 效果持续恢复（满资源即移除）；② 充能被动恢复（每 3 秒 +1 × FLASK_CHARGES_GAINED）。
     */
    public static void tick(Player player) {
        if (player.level().isClientSide()) return;
        tickActiveEffects(player);
        tickRecharge(player);
    }

    private static void tickActiveEffects(Player player) {
        Map<Integer, FlaskEffect> current = player.getData(AttachmentRegistry.FLASK_ACTIVE);
        if (current.isEmpty()) return;
        Map<Integer, FlaskEffect> active = new HashMap<>(current);
        boolean changed = false;
        // 生命药剂恢复重定向至盟友开关（HEALTH_RECOVERY_FROM_YOUR_FLASKS_ALSO_APPLIES_TO_YOUR_COMPANIONS）：
        // 值>0 时，每 tick 对持有者生命药剂恢复的同时，按相同 perTick 治疗其全部盟友（IS_COMPANION 且归属=持有者）。
        boolean redirectToCompanions =
                StatEngine.gateOf(player, "flask_life_recovery_share", Set.of(GateTag.COMPANION));
        for (var it = active.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, FlaskEffect> e = it.next();
            FlaskEffect fx = e.getValue();
            if (fx.remainingTicks() <= 0) {
                it.remove();
                // 精确兜底移除本瓶注入的词缀条目（键前缀 flask:<key>:），与计时自动过期互补，避免事件漏触发残留。
                ItemStatContributor.revoke(player, "flask:" + e.getKey() + ":");
                changed = true;
                continue;
            }
            // 回复速率倍率（FLASK_RECOVERY_RATE）已在 drink() 的 duration 中一次性并入：
            // duration = flask.durationTicks / (rateMult × durMult) → 速率越高、持续越短、每 tick 回复越高，
            // 但总回复量恒定（效率 = 每秒回血量随速率线性上升，符合「缩短时间、提高量效率」）。
            // 故此处 perTick 直接取 basePerTick，不再二次叠乘 FLASK_RECOVERY_RATE（否则会变成 rate²，过放大）。
            float perTick = fx.perTick();
            // 实际回复 ≤ 0（资源已满）→ POE2「资源填满时 Flask 效果移除」，但受两条词缀门控：
            // 任一门控激活 → 即便实际回复 ≤ 0 也保留效果继续走完持续期。
            ResourceEventHelper.HealResult result =
                    ResourceEventHelper.onHeal(player, fx.resource(), perTick, "flask", null);
            // 同伴重定向：仅生命资源（"health"）药剂触发，且开关启用；每 tick 额外治疗持有者盟友
            if (redirectToCompanions && fx.resource().equals(GateTag.HEALTH)) {
                healCompanions(player, perTick);
            }
            // 实际回复 ≤ 0（资源已满）→ POE2「资源填满时 Flask 效果移除」，但受 FlaskEffect 上的两条标记门控
            // （饮用瞬间按 {flask, 资源} 作用域烘焙，即「加上资源类型」）：
            //  · notRemovedWhenFull —— 回复满后不停止（"not removed when Unreserved Life is Filled"）；
            //  · canOverflow —— 回复可溢出上限（资源已满仍视为有效）。
            // 任一门控激活 → 即便实际回复 ≤ 0 也保留效果继续走完持续期。
            if (result.actual() <= 0 && !fx.notRemovedWhenFull() && !fx.canOverflow()) {
                it.remove();
                // 资源填满提前移除效果时，同步精确移除本瓶注入的词缀条目（键前缀 flask:<key>:）。
                ItemStatContributor.revoke(player, "flask:" + e.getKey() + ":");
                changed = true;
                continue;
            }
            e.setValue(fx.tickDown());
            changed = true;
        }
        if (changed) player.setData(AttachmentRegistry.FLASK_ACTIVE, Map.copyOf(active));
    }

    /**
     * 将生命药剂恢复量按 perTick 治疗持有者的全部盟友（IS_COMPANION 且归属=持有者）。
     * 与 {@code TargetSelector#resolveCompanions}（该方法已删除，现行走 {@link TargetSelector#resolve}） 同源判定，
     * 即「你的盟友」= 带 IS_COMPANION 附件且 owner_uuid=持有者 的实体。
     */
    private static void healCompanions(Player player, float amount) {
        if (amount <= 0) return;
        for (Entity en : player.level().getEntities(player, player.getBoundingBox().inflate(64.0))) {
            if (en instanceof net.minecraft.world.entity.LivingEntity le
                    // 原 AttachmentRegistry.IS_COMPANION 已迁入 AdditionData（key=is_companion）
                    && AdditionData.getOrDefault(le, AdditionKeys.IS_COMPANION, 0.0) != 0.0
                    && player.equals(OwnerHelper.getOwner(le))) {
                ResourceEventHelper.onHeal(le, GateTag.HEALTH, amount, "flask", null);
            }
        }
    }

    private static void tickRecharge(Player player) {
        FlaskSave save = player.getData(AttachmentRegistry.FLASK_SAVE);
        long now = player.level().getGameTime();
        FlaskSave updated = save;
        boolean changed = false;
        for (int i = 0; i < FlaskSlotRegistry.totalSlots(player.level().registryAccess()); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof FlaskItem flask)) continue;
            int charges = stack.getOrDefault(DataComponentRegistry.FLASK_CHARGES.get(), flaskMaxCharges(player, stack, flask));
            if (charges >= flaskMaxCharges(player, stack, flask)) continue;
            // 每秒被动充能（POE2「Flasks gain X charges per second」，与下方 3 秒 +1 的基础恢复叠加）：
            // 按 {flask, 资源} 作用域读零基速率 FLASK_CHARGE_RATE，每 tick 累加 速率/20 于进度组件，
            // 满 1 即转为整充能、余数留存——小数速率（如 0.2/秒）因此不会被取整抹平。
            double rate = DamageHelper.readInScopePublic(
                    player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.CHARGE_RATE);
            if (rate > 0) {
                float progress = stack.getOrDefault(DataComponentRegistry.FLASK_CHARGE_PROGRESS.get(), 0f)
                        + (float) (rate / 20.0);
                int whole = (int) progress;
                ItemStack accrued = stack.copy();
                accrued.set(DataComponentRegistry.FLASK_CHARGE_PROGRESS.get(), progress - whole);
                if (whole > 0) {
                    charges = Math.min(flaskMaxCharges(player, stack, flask), charges + whole);
                    accrued.set(DataComponentRegistry.FLASK_CHARGES.get(), charges);
                }
                stack = accrued;
                updated = updated.with(i, accrued);
                changed = true;
                if (charges >= flaskMaxCharges(player, stack, flask)) continue;
            }
            long last = stack.getOrDefault(DataComponentRegistry.FLASK_LAST_CHARGE.get(), 0L);
            if (last > now) last = 0L; // 存档跨世界/时间回退保护
            if (now - last < RECHARGE_TICKS) {
                if (last == 0L) {
                    ItemStack stamped = stack.copy();
                    stamped.set(DataComponentRegistry.FLASK_LAST_CHARGE.get(), now);
                    updated = updated.with(i, stamped);
                    changed = true;
                }
                continue;
            }
            // 逐槽按 {flask, 资源} 作用域读取（不跨槽缓存）：通用条目与按资源限定条目
            // （{flask, health} = 「Health Flask Charges gained」/{flask, mana} = 「Mana Flask…」）各自命中。
            double gains = DamageHelper.readInScopePublic(
                    player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.CHARGES_GAINED);
            ItemStack copy = stack.copy();
            copy.set(DataComponentRegistry.FLASK_LAST_CHARGE.get(), now);
            copy.set(DataComponentRegistry.FLASK_CHARGES.get(),
                    Math.min(flaskMaxCharges(player, stack, flask), charges + Math.max(1, (int) Math.round(gains))));
            updated = updated.with(i, copy);
            changed = true;
        }
        if (changed) player.setData(AttachmentRegistry.FLASK_SAVE, updated);
    }

    // ───────────────────────────── 击杀充能 ─────────────────────────────

    /**
     * 击杀补充 Flask 充能（POE2 Monster Power：击杀怪物按强度补充充能；
     * 本 mod 按被杀实体最大生命分档：≥2000 视为精英/首领 +8，否则 +2，再 × FLASK_CHARGES_GAINED）。
     */
    public static void onKill(Player player, float victimMaxHealth) {
        if (player.level().isClientSide()) return;
        FlaskSave save = player.getData(AttachmentRegistry.FLASK_SAVE);
        int base = victimMaxHealth >= 2000f ? 8 : 2;
        FlaskSave updated = save;
        boolean changed = false;
        for (int i = 0; i < FlaskSlotRegistry.totalSlots(player.level().registryAccess()); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof FlaskItem flask)) continue;
            int max = flaskMaxCharges(player, stack, flask);
            int charges = stack.getOrDefault(DataComponentRegistry.FLASK_CHARGES.get(), max);
            if (charges >= max) continue;
            // 逐槽按 {flask, 资源} 读取充能获得倍率：按资源限定的条目（Health/Mana Flask 变体）才能正确分流。
            double gains = DamageHelper.readInScopePublic(
                    player, Set.of(GateTag.FLASK, flask.resource), AttributeRegistry.CHARGES_GAINED);
            int amount = (int) Math.round(base * Math.max(0.0, gains));
            int newCharges = Math.min(max, charges + amount);
            // 「#% Chance to gain a Charge when you kill」：本瓶独立掷骰，命中额外 +1 充能（与确定性击杀补能正交）。
            double chance = ModStatReader.readRolledPercent(stack, StatIds.FLASK_CHANCE_GAIN_CHARGE_ON_KILL);
            if (chance > 0.0 && newCharges < max && player.getRandom().nextDouble() < chance) {
                newCharges = Math.min(max, newCharges + 1);
            }
            // 「击杀时获得充能球几率」标记（flask_chance_gain_charge_on_kill，{flask} 作用域，由 StatEngine
            // 折叠玩家 STAT_STORE 中该标记条目幅度）作为独立额外来源，各自掷骰命中额外 +1 充能，消除该标记虚接。
            double markerChance = StatEngine.markerMagnitude(player, MarkerStatEffect.Kind.FLASK_CHANCE_GAIN_CHARGE_ON_KILL, Set.of());
            if (markerChance > 0.0 && newCharges < max && player.getRandom().nextDouble() < markerChance) {
                newCharges = Math.min(max, newCharges + 1);
            }
            if (newCharges <= charges) continue;
            ItemStack copy = stack.copy();
            copy.set(DataComponentRegistry.FLASK_CHARGES.get(), newCharges);
            updated = updated.with(i, copy);
            changed = true;
        }
        if (changed) player.setData(AttachmentRegistry.FLASK_SAVE, updated);
    }
}
