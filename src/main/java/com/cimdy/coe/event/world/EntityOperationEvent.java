package com.cimdy.coe.event.world;

import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.collision.Stop;
import com.cimdy.coe.data.codec.skill.modifier.impl.damage.Exposure;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.EventSubscription;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.data.codec.skill.phase.action.damage.DamageAction;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.core.MovementValue;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.stat.StatScopeReader;
import com.cimdy.coe.helper.DodgeRollHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 实体基本操作回调：订阅 NeoForge「实体事件层级」中除伤害类以外的几乎所有事件，统一转发到 {@link Event} 的近期行为存储，
 * 供 Context 的 {@code EventHistoryValue} / {@code MovementValue} 等节点按事件名读取
 * （如 {@code "use_item"}、{code "jump"}、{@code "mount"}、{@code "teleport"}）。
 * <p>
 * 同时承接 {@link Event} 原有的「移动 / 坐标采样 / Presence」转发逻辑（从 Event 迁移至此），使本类成为
 * 「实体行为 → 近期状态存储」的唯一入口。
 * <p>
 * 类名与层级均直接对应 NeoForge 源码（{@code net.neoforged.neoforge.event.entity} 及其子包），已逐一核对：
 * <ul>
 *   <li>实体层（{@code EntityEvent} 子类，{@code getEntity()} 返回 {@link Entity}，本类仅取 {@link LivingEntity}）：
 *       {@link EntityJoinLevelEvent}、{@link EntityLeaveLevelEvent}、{@link EntityMountEvent}、
 *       {@link EntityTeleportEvent}（含 TeleportCommand/EnderPearl/ItemConsumption 等全部子类）、
 *       {@link EntityStruckByLightningEvent}、{@link EntityTravelToDimensionEvent}。</li>
 *   <li>生物层（{@code LivingEvent} 子类，{@code getEntity()} 返回 {@link LivingEntity}）：
 *       {@link LivingChangeTargetEvent}、{@link LivingEquipmentChangeEvent}、
 *       {@link LivingEntityUseItemEvent.Start}/{@code Finish}/{@code Stop}、{@link LivingFallEvent}、
 *       {@link LivingJumpEvent}（在 {@code LivingEvent} 内）、{@link LivingKnockBackEvent}、
 *       {@link LivingHealEvent}、{@link LivingShieldBlockEvent}、{@link LivingUseTotemEvent}、
 *       {@link MobEffectEvent.Added}/{@code Remove}/{@code Expired}、{@link MobSplitEvent}、
 *       {@link LivingDestroyBlockEvent}、{@link LivingDrownEvent}、{@link LivingSwapItemsEvent.Hands}、
 *       {@link LivingGetProjectileEvent}、{@link AnimalTameEvent}、{@link LivingConversionEvent.Post}、
 *       {@link LivingExperienceDropEvent}。</li>
 *   <li>玩家层（{@code PlayerEvent} 子类）：{@link CriticalHitEvent}（玩家暴击攻击）。</li>
 * </ul>
 * <p>
 * 高频事件不进入存储：{@link EntityTickEvent.Pre} 仅做坐标采样（不记录）；
 * {@link LivingEntityUseItemEvent.Tick} 不订阅，避免每 tick 产生无意义记录；
 * {@link net.neoforged.neoforge.event.entity.living.LivingBreatheEvent} 每 tick 触发，同样不订阅。
 * <p>
 * 伤害类事件（{@code LivingDamageEvent} / {@code LivingIncomingDamageEvent} / {@code ArmorHurtEvent} /
 * {@code EntityInvulnerabilityCheckEvent}）以及其派生的 {@code LivingDeathEvent} / {@code LivingDropsEvent}
 * 由 {@code DamageEvent} 统一处理，本类不订阅，避免重复持有。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class EntityOperationEvent {

    // ---- 移动 / 坐标采样 / Presence（从 Event 迁移）----
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity instanceof LivingEntity living) {
            Event.State st = Event.get(living);
            st.sample(living);
            // Presence 维护：stat 作用域基于最大范围的实体实现——
            // 不再限定玩家。凡「需 Presence 追踪」的实体（StatStore.needsPresenceTracking：引用 presence
            // 节点或持有 PRESENCE_* target 条目 / aura 光环条目，如玩家 Exposure 光环、随从/怪物光环源）
            // 均维护自身 Presence 进/出记录，使任何实体类型都能作为光环源与光环目标
            //（实体类型本身即是作用域判断维度——PRESENCE_ENEMIES 等选择器对任意持有者解析）。
            // 按需开关：仅当有 stat 条目引用 presence 才执行每 tick 扫描；无引用恒关闭（零开销）。
            if (EventSubscription.isPresenceNeeded()
                    && living.hasData(AttachmentRegistry.STAT_STORE)
                    && living.getData(AttachmentRegistry.STAT_STORE).needsPresenceTracking()) {
                // 半径一律取属性（PRESENCE_RANGE 已挂全体实体，不再保留硬编码 fallback，避免与属性默认值分歧）
                st.updatePresence(living, living.getAttributeValue(AttributeRegistry.PRESENCE_RANGE),
                        Event.State.PRESENCE_MEMORY_TICKS);
            }
        }
    }

    // ---- 等级 / 骑乘 / 传送 / 跨维 ----
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity living) {
            Event.record(living, EventName.JOIN_LEVEL);
            // D-8：进入区域事件（复用加入世界语义，零额外检测成本）——「进入区域时触发 X」类效果。
            GameEvent.emit(EventName.ENTER_AREA, living, living, Set.of());
        }
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity living) {
            Event.record(living, EventName.LEAVE_LEVEL);
            // 订阅清理（M1：防计数泄漏）——撤销本实体经 tick 对齐贡献的全部事件订阅与 presence 计数
            EventSubscription.cleanup(living);
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.getEntity() instanceof LivingEntity living)
            Event.record(living, event.isMounting() ? EventName.MOUNT : EventName.DISMOUNT);
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity living) Event.record(living, EventName.TELEPORT);
    }

    @SubscribeEvent
    public static void onStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof LivingEntity living) Event.record(living, EventName.STRUCK_BY_LIGHTNING);
    }

    @SubscribeEvent
    public static void onTravelDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof LivingEntity living) Event.record(living, EventName.TRAVEL_DIMENSION);
    }

    // ---- 攻击 / 格挡（非伤害本体）----
    @SubscribeEvent
    public static void onCritHit(CriticalHitEvent event) {
        Event.record(event.getEntity(), EventName.CRIT);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        Event.record(event.getEntity(), EventName.SHIELD_BLOCK);
    }

    // ---- 目标 / 装备 / 状态 ----
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        Event.record(event.getEntity(), EventName.CHANGE_TARGET);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        Event.record(event.getEntity(), EventName.EQUIPMENT_CHANGE);
    }

    // ---- 使用物品（Start/Finish/Stop 记录，Tick 不订阅）----
    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        Event.record(event.getEntity(), EventName.USE_ITEM_START);
        // 举盾 / 招架 造成型事件闭环：玩家右键使用 offhand 物品时，由使用者作为 owner 触发 fire 通道。
        // 行为由数据驱动判定（EquipmentState.types 含 shield / buckler），不再依赖 instanceof ShieldItem，
        // 以免继承 ShieldItem 的法器 / 轻盾「白嫖」到重盾举盾事件。
        LivingEntity e = event.getEntity();
        ItemStack stack = event.getItem();
        EquipmentState state =ModifiableStateOps.equipment(stack);
        if (state.isBuckler()) {
            // 轻盾：进入招架（Parry）姿态——写入 PARRY timer（招架姿态缓存，供 DamagePipeline.stageParry 读取）。
            // 进入招架触发独立造成型事件 CAUSED_PARRY_RAISE（对称重盾 CAUSED_SHIELD_RAISE）；
            // PARRY_CAUSED 仅保留给 stageParry「成功挡下打击」时触发，避免进入/成功双 fire。
            Event.record(e, EventName.PARRY);
            // 招架姿态 PARRY 以无限上限刷新：该 timer 仅在进入时写入一次、无续期机制，
            // 若用默认 20s 上限会在长时间保持后被 tickStore 移除，导致姿态判定失效。
            ExpressionContext.get(e).refreshTimerInfinite(EventName.PARRY);
            Event.record(e, EventName.CAUSED_PARRY_RAISE);
            StatEngine.fire(e, null, StatContext.of(Set.of(EventName.CAUSED_PARRY_RAISE), null,
                    EventName.CAUSED_PARRY_RAISE, null), null);
        } else if (state.isShield()) {
            // 重盾：普通举盾。
            Event.record(e, EventName.CAUSED_SHIELD_RAISE);
            StatEngine.fire(e, null, StatContext.of(Set.of(EventName.CAUSED_SHIELD_RAISE), null,
                    EventName.CAUSED_SHIELD_RAISE, null), null);
        }
    }

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        Event.record(event.getEntity(), EventName.USE_ITEM);
    }

    @SubscribeEvent
    public static void onUseItemStop(LivingEntityUseItemEvent.Stop event) {
        Event.record(event.getEntity(), EventName.USE_ITEM_STOP);
        // 松开右键结束招架：清除无限上限的 PARRY timer，避免残留误判为「仍在招架」。
        LivingEntity e = event.getEntity();
        EquipmentState state =ModifiableStateOps.equipment(event.getItem());
        if (state.isBuckler()) {
            ExpressionContext.get(e).clearTimer(EventName.PARRY);
        }
    }

    // ---- 动作 / 位移 ----
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity e = event.getEntity();
        Event.record(e, EventName.FALL);
        // 空翻落地保护：空中发起的空翻（翻滚+空格）在保护窗口内坠落，减免绝大部分掉落伤害（≈90%）。
        if (e instanceof ServerPlayer sp && DodgeRollHelper.consumeFlipLanding(sp)) {
            event.setDistance(event.getDistance() * 0.1);
        }
    }

    @SubscribeEvent
    public static void onJump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        Event.record(event.getEntity(), EventName.JUMP);
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        Event.record(event.getEntity(), EventName.KNOCKBACK);
        // 被动触发闭环：被击退者（event.getEntity()）记录「被击退」并 fire on-taken 效果。
        // 本事件主语即承受方，无需逆源。
        LivingEntity living = event.getEntity();
        ExpressionContext.get(living).refreshTimer(EventName.TAKEN_KNOCKBACK);
        StatEngine.fire(living, living, StatContext.of(Set.of(EventName.TAKEN_KNOCKBACK), null,
                EventName.TAKEN_KNOCKBACK, null), null);
        // 击退距离倍率（KNOCKBACK 属性 inc 部分：increased Knockback Distance）：
        // 在 {knockback} 作用域读 KNOCKBACK（base=1.0，inc 折叠为总倍率），缩放本次击退强度。
        // 与 DAMAGE/CAST_SPEED 同构——连续倍率经 fold 通道在计算期读取，不另建 FIRE 效果。
        // 注：近距命中击退「几率」已拆至 KNOCKBACK_CHANCE（Stat值），由 DamageAction 命中近距时掷骰，
        // 本处仅承载「距离倍率」，避免 chance/inc 混读同一属性（q-1 严格拆分）。
        double mag = StatScopeReader.knockbackMult(living);
        if (mag > 0 && mag != 1.0) {
            event.setStrength((float) (event.getStrength() * mag));
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        Event.record(event.getEntity(), EventName.HEAL);
        // 治疗：造成方与承受方同为接受治疗者本人。补 caused_heal + taken_heal 对称（owner=本人，target=本人）。
        LivingEntity living = event.getEntity();
        ExpressionContext.get(living).refreshTimer(EventName.CAUSED_HEAL);
        ExpressionContext.get(living).refreshTimer(EventName.TAKEN_HEAL);
        StatEngine.fire(living, living, StatContext.of(Set.of(EventName.CAUSED_HEAL, EventName.TAKEN_HEAL), null,
                EventName.CAUSED_HEAL, null), null);
    }

    @SubscribeEvent
    public static void onUseTotem(LivingUseTotemEvent event) {
        Event.record(event.getEntity(), EventName.USE_TOTEM);
    }

    // ---- 效果 / 转换 / 分裂 ----
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        Event.record(event.getEntity(), EventName.EFFECT_ADDED);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        Event.record(event.getEntity(), EventName.EFFECT_REMOVED);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        Event.record(event.getEntity(), EventName.EFFECT_EXPIRED);
    }

    @SubscribeEvent
    public static void onMobSplit(MobSplitEvent event) {
        // MobSplitEvent 直接继承 Event（非 EntityEvent），实体经 getParent() 取得（返回 Mob，即 LivingEntity）。
        Event.record(event.getParent(), EventName.MOB_SPLIT);
    }

    @SubscribeEvent
    public static void onConversion(LivingConversionEvent.Post event) {
        Event.record(event.getEntity(), EventName.CONVERSION);
    }

    // ---- 方块 / 环境交互 ----
    @SubscribeEvent
    public static void onDestroyBlock(LivingDestroyBlockEvent event) {
        Event.record(event.getEntity(), EventName.DESTROY_BLOCK);
    }

    @SubscribeEvent
    public static void onDrown(LivingDrownEvent event) {
        Event.record(event.getEntity(), EventName.DROWN);
    }

    @SubscribeEvent
    public static void onSwapItems(LivingSwapItemsEvent.Hands event) {
        Event.record(event.getEntity(), EventName.SWAP_ITEMS);
    }

    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        Event.record(event.getEntity(), EventName.GET_PROJECTILE);
    }

    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        Event.record(event.getEntity(), EventName.TAME);
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        Event.record(event.getEntity(), EventName.EXPERIENCE_DROP);
    }
}
