package com.cimdy.coe.item.charm;

import com.cimdy.coe.data.codec.support.stat.StatIds;
import com.cimdy.coe.data.codec.item.mod.core.ModStatReader;

import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;

import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.net.NetEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.CharmSave;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.helper.mod.ItemStatContributor;
import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.helper.combat.DamageHelper;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Charm（护符）统一机制中枢（POE2 实证，严格还原）。
 * <p>
 * <b>中央入口原则（用户拍板 ①）</b>：护符不散落 per-feature 回调——所有触发判定收敛在此类，
 * 由两处中央钩子驱动：
 * <ul>
 *   <li>{@link #onStatusApplied}：挂在 {@code StatusEngine.apply} 内部（与 {@code isImmune} 同构），
 *       任何异常/控制/眩晕状态落地前先问「玩家是否装了对应免疫护符」；命中则消耗充能、授予免疫状态并<b>抢占</b>原状态；</li>
 *   <li>{@link #onElementalDamageTaken}：挂在伤害管线受击阶段，元素伤害落地即触发对应抗性护符；</li>
 *   <li>{@link #onKillMonster} / {@link #onKillCharge}：挂在击杀事件，Golden 触发 + 全护符按 Monster Power 补充充能。</li>
 * </ul>
 * 效果本身一律数据驱动落地：把护符的 {@code effects}（引用 {@code coe:charm_*} 模板的 {@link StatEntry}）
 * 经 {@code StatStore} 注入玩家属性层（带时长计时域自动过期），本类只做「事件发生 → 记账 + 注入」，
 * 不含任何硬编码规则分支、不依赖任何注册型 {@code StatusType}。免疫类随之进入 STAT_STORE，
 * 由 {@code StatEngine.isImmune} 按 id 短路后续同类异常，实现「抢占」。
 */
public final class CharmEngine {

    private CharmEngine() {
    }

    /**
     * 本符有效充能上限 = 基底 maxCharges × (1 + 「#% increased Charges」词缀幅度)，向上取整、最小 1。
     * 词缀幅度由 {@link ModStatReader#readRolledPercent} 直接读物品 ITEM_STAT_ENTRIES 取（scope 天然正确，非虚接）；
     * 未带该词缀则回退基底 maxCharges。各充能读写点一律经此而非 charm.maxCharges 静态字段。
     */
    public static int charmMaxCharges(ItemStack stack, CharmItem charm) {
        double inc = ModStatReader.readRolledPercent(stack, StatIds.FLASK_NUM_CHARGES);
        return Math.max(1, (int) Math.ceil(charm.maxCharges * (1.0 + inc)));
    }

    // ─────────────── 状态 → 触发器 映射（onStatusApplied 中央钩子用） ───────────────

    private static CharmTrigger triggerOf(StatusType type) {
        if (type == StatusDefs.IGNITE) return CharmTrigger.IGNITE;
        if (type == StatusDefs.FREEZE) return CharmTrigger.FREEZE;
        if (type == StatusDefs.SHOCK) return CharmTrigger.SHOCK;
        if (type == StatusDefs.BLEED) return CharmTrigger.BLEED;
        if (type == StatusDefs.POISON) return CharmTrigger.POISON;
        if (type == StatusDefs.STUN) return CharmTrigger.STUN;
        if (type == StatusDefs.CHILL || type == StatusDefs.ELECTROCUTE
                || type == StatusDefs.PIN) return CharmTrigger.SLOW;
        return null;
    }

    // ─────────────── 中央钩子 ───────────────

    /**
     * 状态落地前的护符自动触发判定（由 {@code StatusEngine.apply} 在 {@code isImmune} 之后调用）。
     *
     * @return true = 已触发免疫类护符，调用方应<b>抢占</b>原状态施加（原异常被免疫挡下）。
     */
    public static boolean onStatusApplied(LivingEntity target, StatusType type) {
        if (!(target instanceof Player player)) return false;
        CharmTrigger trig = triggerOf(type);
        if (trig == null) return false;
        boolean any = false;
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof CharmItem charm)) continue;
            if (charm.trigger != trig) continue;
            if (triggerCharm(player, i, charm, stack)) any = true;
        }
        return any;
    }

    /** 元素受击触发（伤害管线钩子）。slot：1=混沌 2=火 3=冰 4=雷（对齐 {@code applyDamageReductions} 伤害槽位）。 */
    public static void onElementalDamageTaken(Player player, int slot, float amount) {
        if (amount <= 0) return;
        CharmTrigger trig = switch (slot) {
            case 1 -> CharmTrigger.CHAOS;
            case 2 -> CharmTrigger.FIRE;
            case 3 -> CharmTrigger.COLD;
            case 4 -> CharmTrigger.LIGHTNING;
            default -> null;
        };
        if (trig == null) return;
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (stack.getItem() instanceof CharmItem charm && charm.trigger == trig) {
                triggerCharm(player, i, charm, stack);
            }
        }
    }

    /** 击杀稀有/独特敌人触发（Golden）。近似：击杀任意 Monster 即触发（POE2 为 rare/unique）。 */
    public static void onKillMonster(Player player) {
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (stack.getItem() instanceof CharmItem charm && charm.trigger == CharmTrigger.KILL_RARE_UNIQUE) {
                triggerCharm(player, i, charm, stack);
            }
        }
    }

    // ─────────────── 击杀充能（Monster Power，复用 Flask 分档；× charm_charges_gained） ───────────────

    /**
     * 城镇水井补满：将所有槽位中未满的护符充能置满。
     *
     * @return true = 确有未满护符被补满（调用方据此决定是否播报）
     */
    public static boolean refillAll(Player player) {
        if (player.level().isClientSide()) return false;
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        CharmSave updated = save;
        boolean changed = false;
        boolean anyFilled = false;
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof CharmItem charm)) continue;
            int max = charmMaxCharges(stack, charm);
            int charges = charm.charges(stack);
            if (charges >= max) continue;
            ItemStack copy = stack.copy();
            charm.setCharges(copy, max);
            updated = updated.with(i, copy);
            changed = true;
            anyFilled = true;
        }
        if (changed) player.setData(AttachmentRegistry.CHARM_SAVE, updated);
        return anyFilled;
    }

    // ─────────────── 每 tick 驱动（被动充能） ───────────────

    /**
     * 服务端每 tick 驱动（由 {@code NetEvent.onPlayerFlaskTick} 与 Flask 同点调用）：
     * 护符每秒被动获得充能（POE2「Charms gain {X} charges per Second」）。
     * <p>
     * 按 {@code {charm}} 作用域读零基属性 {@link AttributeRegistry#CHARM_CHARGE_RATE}（无词条时恒 0 → 直接返回，
     * 不产生任何写入），每 tick 累加 {@code 速率/20} 到 {@code CHARM_CHARGE_PROGRESS}，
     * 满 1 即转为整充能、余数留存——小数速率（如 0.4/秒）因此不会被取整抹平。
     */
    public static void tick(Player player) {
        if (player.level().isClientSide()) return;
        // 护符 HUD 激活计时（slot → 剩余 tick）随每 tick 衰减，归零即移除（与 STAT_STORE 计时域同步）。
        Map<Integer, Integer> active = player.getData(AttachmentRegistry.CHARM_ACTIVE);
        if (!active.isEmpty()) {
            Map<Integer, Integer> next = new LinkedHashMap<>();
            for (var en : active.entrySet()) {
                int t = en.getValue() - 1;
                if (t > 0) next.put(en.getKey(), t);
            }
            player.setData(AttachmentRegistry.CHARM_ACTIVE, next);
        }
        double rate = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARGE_RATE);
        if (rate <= 0) return;
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        CharmSave updated = save;
        boolean changed = false;
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof CharmItem charm)) continue;
            int max = charmMaxCharges(stack, charm);
            int charges = charm.charges(stack);
            if (charges >= max) continue;
            ItemStack copy = stack.copy();
            float progress = copy.getOrDefault(DataComponentRegistry.CHARM_CHARGE_PROGRESS.get(), 0f)
                    + (float) (rate / 20.0);
            int whole = (int) progress;
            copy.set(DataComponentRegistry.CHARM_CHARGE_PROGRESS.get(), progress - whole);
            if (whole > 0) charm.setCharges(copy, Math.min(max, charges + whole));
            updated = updated.with(i, copy);
            changed = true;
        }
        if (changed) player.setData(AttachmentRegistry.CHARM_SAVE, updated);
    }

    public static void onKillCharge(Player player, float victimMaxHealth) {
        if (player.level().isClientSide()) return;
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        double gains = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARGES_GAINED);
        int amount = (int) Math.round((victimMaxHealth >= 2000f ? 16 : 4) * Math.max(0.0, gains));
        CharmSave updated = save;
        boolean changed = false;
        for (int i = 0; i < CharmSlots.count(player); i++) {
            ItemStack stack = save.get(i);
            if (!(stack.getItem() instanceof CharmItem charm)) continue;
            int max = charmMaxCharges(stack, charm);
            int charges = charm.charges(stack);
            if (charges >= max) continue;
            int newCharges = Math.min(max, charges + amount);
            // 「#% Chance to gain a Charge when you kill」：本符独立掷骰，命中额外 +1 充能（与确定性击杀补能正交）。
            double chance = ModStatReader.readRolledPercent(stack, StatIds.FLASK_CHANCE_GAIN_CHARGE_ON_KILL);
            if (chance > 0.0 && newCharges < max && player.getRandom().nextDouble() < chance) {
                newCharges = Math.min(max, newCharges + 1);
            }
            if (newCharges <= charges) continue;
            ItemStack copy = stack.copy();
            charm.setCharges(copy, newCharges);
            updated = updated.with(i, copy);
            changed = true;
        }
        if (changed) player.setData(AttachmentRegistry.CHARM_SAVE, updated);
    }

    // ─────────────── 单机触发核心 ───────────────

    private static boolean triggerCharm(Player player, int slotIndex, CharmItem charm, ItemStack stack) {
        // 护符禁用闸门（CANNOT_USE_CHARMS 标记 ≥1.0 → 完全无法使用护符；含连锁，因连锁仅由本方法内发起）。
        if (StatEngine.gateOf(player, "cannot_use_charms", Set.of())) return false;
        // 充能消耗倍率（{charm} 作用域；POE2「X% reduced Charm Charges used」→ 数值 <1 表示减少消耗）
        double usedMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARGES_USED);
        int effUsed = (int) Math.round(charm.chargesUsed * Math.max(0.0, usedMult));
        int charges = charm.charges(stack);
        if (charges < effUsed) return false;
        charm.setCharges(stack, charges - effUsed);
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        player.setData(AttachmentRegistry.CHARM_SAVE, save.with(slotIndex, stack));
        // 效果数据驱动落地：注入玩家属性层（前缀 charm:<slot>:），带时长计时域自动过期；
        // 免疫类随之进入 STAT_STORE → StatEngine.isImmune 按 id 短路后续同类异常（抢占语义）。
        applyCharmEffects(player, slotIndex, charm, stack, charmDuration(player, charm));
        // 护符「Also grants # Guard」：注入后真读 charm_guard 标记幅度（词缀 roll 值，按百分比 /100 归一），
        // 施加限时 GUARD_FROM_FLASK 状态（服务端）——确保该词缀真正生效（非虚接）。
        if (!player.level().isClientSide()) {
            double guardRatio = StatEngine.magnitudeOf(player, "charm_guard", Set.of());
            if (guardRatio > 0.0) {
                StatusEngine.apply(player, StatusDefs.GUARD_FROM_FLASK, null, guardRatio / 100.0,
                        charmDuration(player, charm), 1);
            }
        }
        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.4F, 1.0F);
        }
        // 事件闭环：「使用护符」on-use 事件（携带 {charm} 标签），驱动语料
        // 「Recover X% of maximum Mana when a Charm is used」类即时回复条目（ResourceStatEffect heal 通道）。
        // 置于扣充能与效果落地之后、连锁判定之前：充能不足直接 return false 的空触发不发射；
        // 连锁触发复用本方法，故连锁的那一次同样发射（POE2 语义：连锁也算「使用了一个护符」）。
        GameEvent.emit(EventName.USE_CHARM, player, null, Set.of(GateTag.CHARM));
        // 连锁：charm_activation_chance（「使用护符时 10% 几率再触发一个护符不消耗充能」）
        double chain = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARM_ACTIVATION_CHANCE);
        if (player.getRandom().nextDouble() < chain) triggerChain(player, slotIndex);
        return true;
    }

    /** 效果时长倍率（读 AILMENT_DURATION + {charm} 作用域，对齐 StatGen:873 INC_CHARM_DURATION 接线；POE2「X% increased Charm Effect Duration」）。 */
    private static int charmDuration(Player player, CharmItem charm) {
        double durMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARM_DURATION);
        return (int) Math.round(charm.durationTicks * Math.max(0.0, durMult));
    }

    /** 连锁触发：随机再触发一个其它护符，不消耗其充能。 */
    private static void triggerChain(Player player, int exclude) {
        CharmSave save = player.getData(AttachmentRegistry.CHARM_SAVE);
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < CharmSlots.count(player); i++) {
            if (i == exclude) continue;
            ItemStack stack = save.get(i);
            if (stack.getItem() instanceof CharmItem charm && charm.charges(stack) >= charm.chargesUsed) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) return;
        int pick = candidates.get(player.getRandom().nextInt(candidates.size()));
        ItemStack stack = save.get(pick);
        CharmItem charm = (CharmItem) stack.getItem();
        applyCharmEffects(player, pick, charm, stack, charmDuration(player, charm));
    }

    /**
     * 把护符 {@code effects} 数据驱动注入玩家属性层，并刷新 HUD 激活标记。
     * <ul>
     *   <li>经 {@code StatStore.addModifiers(keyed, duration)} 写入 {@code STAT_STORE}，键前缀
     *       {@code charm:<slot>:}；{@code duration>0} 进入计时域，由 {@code StatStore.tick()} 每 tick
     *       递减、归零自动移除（无需外部回滚，与状态 resync / 属性重建互不干扰）。</li>
     *   <li>幅度随「Charms applied to you have X% increased Effect」（{@code CHARM_EFFECT}，{charm}
     *       作用域）逐实体缩放——复用 {@code StatRead} 的 {@code $value} 注入通道，绝不内联降级。</li>
     *   <li>同步 {@code CHARM_ACTIVE[slot]=duration}，供客户端 {@code renderCharmEffects} 渲染护符物品图标 + 时长。</li>
     * </ul>
     */
    private static void applyCharmEffects(Player player, int slotIndex, CharmItem charm, ItemStack stack, int duration) {
        double effectMult = DamageHelper.readInScopePublic(
                player, Set.of(GateTag.CHARM), AttributeRegistry.CHARM_EFFECT);
        Map<String, StatEntry> keyed = new LinkedHashMap<>();
        int ei = 0;
        for (StatEntry e : charm.effects) {
            keyed.put("charm:" + slotIndex + "#" + (ei++), scaleCharmEffect(e, effectMult));
        }
        // 本护符 rolled 词缀（ITEM_STAT_ENTRIES，由 ModGenerator 经 AdditionType.global 算好）随使用时注入，
        // 复用 charm 效果缩放通道（CHARM_EFFECT），键前缀 charm:<slot>:，带时长计时域自动过期。
        // 词缀 ModStatIds 已专属化为 {charm} 作用域（CHARM_*），与 CharmEngine 各读点对齐 → 真正被消费（非虚接）。
        Map<String, StatEntry> itemMods = stack.get(DataComponentRegistry.ITEM_STAT_ENTRIES);
        if (itemMods != null) {
            for (StatEntry e : itemMods.values()) {
                keyed.put("charm:" + slotIndex + "#" + (ei++), scaleCharmEffect(e, effectMult));
            }
        }
        // 统一经 ItemStatContributor 注入：charm:<slot>: 前缀 + 计时域自动过期（归零移除）。
        ItemStatContributor.inject(player, keyed, duration);
        Map<Integer, Integer> active = new LinkedHashMap<>(player.getData(AttachmentRegistry.CHARM_ACTIVE));
        active.put(slotIndex, duration);
        player.setData(AttachmentRegistry.CHARM_ACTIVE, active);
    }

    /**
     * 按「Charms applied to you have X% increased Effect」缩放护符效果幅度。
     * 仅对引用型（{@code StatRead}）且模板以 {@code $value} 承载幅度的条目生效：把 vars 的
     * {@code value} 乘 {@code m} 后重新生成引用（恒等保留 {@code coe:charm_*} 模板与 {@code $value}
     * 注入通道，绝不用 {@code StatEntry.scaled} 把 StatRead 内联降级——那样会丢失模板与 vars，R3 安全）。
     * 免疫类等无 {@code $value} 的条目原样返回。
     */
    private static StatEntry scaleCharmEffect(StatEntry e, double m) {
        if (m == 1.0) return e;
        if (e instanceof StatRead sr) {
            ExpressionCodec.Value v = sr.vars().get("value");
            if (v != null && v.value().left().isPresent()) {
                double base = v.value().left().get();
                Map<String, ExpressionCodec.Value> vars = new LinkedHashMap<>(sr.vars());
                vars.put("value", ExpressionCodec.of(base * m));
                return new StatRead(sr.ref(), vars);
            }
            return e;
        }
        return e;
    }
}
