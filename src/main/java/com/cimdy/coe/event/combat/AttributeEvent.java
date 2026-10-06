package com.cimdy.coe.event.combat;

import com.cimdy.coe.data.helper.engine.ground.CreateGroundDriver;
import com.cimdy.coe.data.helper.engine.LivingTickDriver;

import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.event.EventSubscription;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.data.codec.item.AccessorySave;
import com.cimdy.coe.data.helper.stat.AccessoryStatHelper;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.LivingMod;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.addition.GoldState;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.attribute.EntityAttributeLimiter;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.affix.*;
import com.cimdy.coe.data.helper.combat.*;
import com.cimdy.coe.data.helper.hub.*;
import com.cimdy.coe.data.helper.resource.*;
import com.cimdy.coe.data.helper.skill.*;
import com.cimdy.coe.data.helper.stat.*;
import com.cimdy.coe.data.helper.status.*;
import com.cimdy.coe.data.helper.impl.value.EvasionEntropy;
import com.cimdy.coe.data.helper.impl.value.LeechValue;
import com.cimdy.coe.data.helper.impl.aura.MarkBlindAura;
import com.cimdy.coe.data.helper.impl.CurseActivation;
import com.cimdy.coe.data.helper.impl.value.RecoupValue;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


import com.cimdy.coe.data.helper.hub.ResourceRegistry;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class AttributeEvent {
    @SubscribeEvent
    public static void EntityAttributeModificationEvent(EntityAttributeModificationEvent event) {
        List<Holder<Attribute>> attributes = AttributeRegistry.ALL_ATTRIBUTE;

        attributes.forEach(attribute -> {
            // 为所有实体赋予默认属性
            event.getTypes().forEach(type -> {
                event.add(type, attribute);
            });
        });

        event.getTypes().forEach(type -> {
            // 为非玩家实体赋予默认属性： 攻击速度 攻击伤害 交互距离
            if (type != EntityTypes.PLAYER) {
                event.add(type, Attributes.ATTACK_SPEED, 1.0);
                event.add(type, Attributes.ATTACK_DAMAGE, 1.0);
                event.add(type, Attributes.ENTITY_INTERACTION_RANGE, 3.0);
                event.add(type, Attributes.BLOCK_INTERACTION_RANGE, 3.0);
            }

//            event.apply(type, Attributes.KNOCKBACK_RESISTANCE, 1.0);
        });
    }

    // 玩家抗性惩罚 进入末地和下界时 分别使所有抗性降低30% 共60%
    @SubscribeEvent
    public static void PlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel) {
            ResourceKey<Level> dimension = event.getTo();
            // 归一：维度抗性惩罚作用于 RESISTANCES 单一属性（所有元素抗性 -30%）
            AttributeModifierHelper.addModifier(player, List.of(AttributeRegistry.RESISTANCES),
                    new AttributeModifier(Identifier.fromNamespaceAndPath(CraftOfExile.MODID,
                            "player.resistance.correction." + dimension.identifier().getPath()),
                            -0.3, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void PlayerCloneEvent(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();
        if (event.isWasDeath()) {
            oldPlayer.getAttributes().getSyncableAttributes().forEach(instance ->
                    instance.getPermanentModifiers().forEach(modifier ->
                            player.getAttribute(instance.getAttribute()).addPermanentModifier(modifier)));
            player.heal(player.getMaxHealth());
            ResourceRegistry.healMana(player, ResourceRegistry.getMaxMana(player));
            // 玩家初始化补满 ES
            ResourceRegistry.setEnergyShield(player, ResourceRegistry.getMaxEnergyShield(player));
            // 金币跨死亡保留：ADDITION_DATA 走 registerSync（copyOnDeath=false），死亡后整体清零，
            // 故在此单独把旧玩家的金币余额搬运到新玩家（只搬 GOLD 一个键，其余键保持清零语义）。
            GoldState.set(player, GoldState.get(oldPlayer));
            // 饰品栏 Stat 重注：饰品存于自定义附件（非 EquipmentSlot），克隆时不触发
            // LivingEquipmentChangeEvent；且 STAT_STORE 非死亡保留，重生后须按附件重注
            // （前缀 = 全局槽位下标，跨会话稳定）。否则饰品加成在首次死亡后永久消失（死线）。
            RegistryAccess accAccess = player.level().registryAccess();
            AccessorySave accSave = player.getData(AttachmentRegistry.ACCESSORY_SAVE);
            for (AccessorySlotRegistry.SlotDescriptor d : AccessorySlotRegistry.slots(accAccess)) {
                ItemStack s = accSave.get(d.globalIndex());
                if (!s.isEmpty()) AccessoryStatHelper.apply(player, s, d.globalIndex());
            }
        }
    }

    @SubscribeEvent
    public static void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        // 饰品栏 Stat 重注：饰品存于自定义附件（非 EquipmentSlot），登录/重生不会触发
        // LivingEquipmentChangeEvent，故在此按注册表重注全部饰品 Stat（前缀 = 全局槽位下标，跨会话稳定）。
        AccessoryStatHelper.reinjectAll(serverPlayer);
    }

    @SubscribeEvent
    public static void EntityJoinLevelEvent(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity living && living.level() instanceof ServerLevel serverLevel) {
            LivingTickDriver.tick(living, serverLevel);
        }
    }

    @SubscribeEvent
    public static void EntityTickEvent(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && living.level() instanceof ServerLevel serverLevel) {
            // 计时数值推进：仅含临时（带 duration）条目的实体才有成本；无计时实体零开销（tick() 内部直接返回 this）
            if (!living.isDeadOrDying() && living.hasData(AttachmentRegistry.STAT_STORE)) {
                StatStore after = living.getData(AttachmentRegistry.STAT_STORE).tick();
                if (after != living.getData(AttachmentRegistry.STAT_STORE)) {
                    living.setData(AttachmentRegistry.STAT_STORE, after);
                }
            }
            // 事件按需订阅对齐：比对当前 store 引用集与上次贡献，增量 diff 全局订阅表。
            // 对所有实体执行（含无 store / 死亡实体——此时引用集为空，等价撤销其历史订阅，防计数泄漏 M1）；
            // Set 相等即零成本短路；store 变更（setData 散点）最迟 1 tick 收敛。
            StatStore store = living.hasData(AttachmentRegistry.STAT_STORE)
                    ? living.getData(AttachmentRegistry.STAT_STORE) : null;
            ExpressionContext ctx = ExpressionContext.get(living);
            Set<String> prev = ctx.getSubscribedEvents();
            Set<String> cur = store != null ? store.referencedEvents() : Set.of();
            if (!prev.equals(cur)) {
                for (String n : prev) if (!cur.contains(n)) EventSubscription.unsubscribe(n);
                for (String n : cur) if (!prev.contains(n)) EventSubscription.subscribe(n);
                ctx.setSubscribedEvents(cur);
            }
            boolean presence = store != null && store.needsPresenceTracking();
            if (ctx.isPresenceSubscribed() != presence) {
                EventSubscription.adjustPresence(presence ? 1 : -1);
                ctx.setPresenceSubscribed(presence);
            }
            if (!living.isDeadOrDying() && CoeConfig.entityModifierOverload) {
                if (living.hasData(AttachmentRegistry.RARITY_DATA) && serverLevel.getGameTime() % 10 == 0) {
                    Rarity rarity = living.getData(AttachmentRegistry.RARITY);
                    LivingModifier newdata = LoadEvent.LIVING_RARITY_DATA_MAP.get(rarity);
                    LivingModifier oldModifiers = living.getData(AttachmentRegistry.RARITY_DATA);
                    if (!oldModifiers.equals(newdata)) {
                        StatStoreHelper.removeStatEntry(living, "rarity:" + rarity.name());
                        int nIdx = 0;
                        for (StatEntry entry : newdata.stats())
                            StatStoreHelper.addStatEntry(living, "rarity:" + rarity.name() + "#" + (nIdx++),
                                    LivingModRoller.withConditions(entry, newdata.conditions()));
                    }
                    living.setData(AttachmentRegistry.RARITY_DATA, newdata);
                }

                if (living.hasData(AttachmentRegistry.ENTITY_MOD) && serverLevel.getGameTime() % 30 == 0) {
                    WeightedList.Builder<LivingMod.Mod> newMods = LivingModRoller.modWeightedList(living);
                    List<String> oldNames = new ArrayList<>(living.getData(AttachmentRegistry.ENTITY_MOD).names());
                    AtomicInteger i = new AtomicInteger();
                    new ArrayList<>(oldNames).forEach(name -> {
                        LivingMod.Mod oldMod = LoadEvent.LIVING_MOD_MAP.get(name);
                        if (oldMod == null || newMods.getList().stream().noneMatch(m ->
                                m.value().name().equals(name) && m.value().equals(oldMod))) {
                            oldNames.remove(name);
                            // 纯 Stat 模型：从 STAT_STORE 按来源前缀移除该词缀贡献的全部 Stat 条目
                            if (oldMod != null) StatStoreHelper.removeStatEntry(living, oldMod.name());
                            i.getAndIncrement();
                        } else {
                            newMods.remove(oldMod);
                            oldNames.remove(name);
                        }
                    });
                    living.setData(AttachmentRegistry.ENTITY_MOD, new LivingMod.ModSet(oldNames));
                    LivingModRoller.rollMods(serverLevel, living, i.get(), newMods);
                }
            }
        }
    }

    @SubscribeEvent
    public static void MobSplitEvent(MobSplitEvent event) {
        Mob mob = event.getParent();
        event.getChildren().forEach(child -> {
            child.setData(AttachmentRegistry.CAMP, mob.getData(AttachmentRegistry.CAMP));
//            child.setData(AttachmentRegistry.STRONGHOLD_DATA, mob.getData(AttachmentRegistry.STRONGHOLD_DATA));
            // 原 AttachmentRegistry.CHILD 已迁入 AdditionData（key=child）
            AdditionData.addOrReplaceData(child, AdditionKeys.CHILD, 1.0);
            child.setData(AttachmentRegistry.LEVEL, mob.getData(AttachmentRegistry.LEVEL));
            child.setData(AttachmentRegistry.RARITY, mob.getData(AttachmentRegistry.RARITY));
            child.setData(AttachmentRegistry.RARITY_DATA, mob.getData(AttachmentRegistry.RARITY_DATA));
            child.setData(AttachmentRegistry.ENTITY_MOD, mob.getData(AttachmentRegistry.ENTITY_MOD));
            mob.getAttributes().getAttributesToUpdate().forEach(instance ->
                    instance.getPermanentModifiers().forEach(modifier ->
                            child.getAttribute(instance.getAttribute()).addPermanentModifier(modifier)));
        });
    }

}
