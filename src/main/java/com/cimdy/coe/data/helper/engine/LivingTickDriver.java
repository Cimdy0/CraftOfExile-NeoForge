package com.cimdy.coe.data.helper.engine;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

import com.cimdy.coe.data.codec.support.stat.effect.grant.CreateGroundStatEffect;
import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.helper.engine.ground.CreateGroundDriver;
import com.cimdy.coe.data.helper.engine.periodic.PeriodicDriver;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.entity.LivingModifier;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

import com.cimdy.coe.data.helper.resource.HealthHelper;

import com.cimdy.coe.data.helper.affix.LivingModRoller;

import com.cimdy.coe.data.helper.stat.StatStoreHelper;

import com.cimdy.coe.data.helper.resource.DarknessHelper;
import com.cimdy.coe.data.helper.impl.value.EvasionEntropy;
import com.cimdy.coe.data.helper.impl.value.LeechValue;
import com.cimdy.coe.data.helper.impl.aura.MarkBlindAura;
import com.cimdy.coe.data.helper.impl.aura.ShieldFrontDebilitateAura;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;
import com.cimdy.coe.data.codec.support.status.def.StatusDefs;
import com.cimdy.coe.data.helper.impl.value.RecoupValue;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * 生物「首次进入世界初始化」+「每 tick 资源 / 光环 / 地面驱动」
 * （原 {@code event.AttributeEvent#EntityJoinLevelEvent} 内联机制，铁律 #56 迁出）。
 */
public final class LivingTickDriver {
    /** 非玩家持武实体基础攻速修正 id。 */
    public static final Identifier ATTACK_SPEED =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "global.weapon.attack_speed");

    private LivingTickDriver() {
    }

    /** 每 tick 入口：区分「首次初始化」与「常规 tick 驱动」。 */
    public static void tick(LivingEntity living, ServerLevel serverLevel) {
        if (!living.hasData(AttachmentRegistry.LEVEL)) {
            firstJoin(living, serverLevel);
        } else if (!living.isDeadOrDying()) {
            drive(living, serverLevel);
        }
    }

    /** 首次出现：玩家初始化阵营/等级/血蓝；怪物按最近玩家等级定级并抽稀有度与词缀。 */
    private static void firstJoin(LivingEntity living, ServerLevel serverLevel) {
        if (living instanceof Player player) {
            player.setData(AttachmentRegistry.CAMP, Camp.PLAYER);
            player.setData(AttachmentRegistry.LEVEL, 1);
            player.getAttribute(Attributes.MAX_HEALTH).addOrReplacePermanentModifier(new AttributeModifier(
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "player.max_health"),
                    30, AttributeModifier.Operation.ADD_VALUE));
            player.heal(player.getMaxHealth());
            ResourceRegistry.healMana(player, ResourceRegistry.getMana(player));
            return;
        }
        Player player = serverLevel.getNearestPlayer(living, 128);
        if (player == null) return;
        living.setData(AttachmentRegistry.CAMP, Camp.MOB);
        living.setData(AttachmentRegistry.LEVEL, player.getData(AttachmentRegistry.LEVEL));
        Rarity rarity = CoeConfig.rarityWeights.getRandom(serverLevel.getRandom()).get();
        LivingModifier rarityModifier = LoadEvent.LIVING_RARITY_DATA_MAP.get(rarity);
        living.setData(AttachmentRegistry.RARITY_DATA, rarityModifier);
        living.setData(AttachmentRegistry.RARITY, rarity);

        // 纯 Stat 模型：稀有度 LivingModifier 的全部 Stat 条目（属性加成 + 转伤）
        // 连同其 conditions 一并存入 STAT_STORE，运行时由 StatEngine.fold 按作用域实时门控。
        int rIdx = 0;
        for (StatEntry entry : rarityModifier.stats())
            StatStoreHelper.addStatEntry(living, "rarity:" + rarity.name() + "#" + (rIdx++),
                    LivingModRoller.withConditions(entry, rarityModifier.conditions()));

        // 每个全局 LivingModifier 的全部 Stat 条目
        int gIdx = 0;
        for (LivingModifier modifier : LoadEvent.LIVING_MODIFIER_LIST) {
            int mIdx = 0;
            for (StatEntry entry : modifier.stats())
                StatStoreHelper.addStatEntry(living, "modifier:" + (gIdx) + "#" + (mIdx++),
                        LivingModRoller.withConditions(entry, modifier.conditions()));
            gIdx++;
        }

        LivingModRoller.rollMods(serverLevel, living,
                LivingModRoller.getModNumber(serverLevel.getRandom(), rarity),
                LivingModRoller.modWeightedList(living));
    }

    /** 常规 tick：攻速/施法修正 → 资源回复 → 偷取/补给 → 熵值/连击/荣耀/元能量/眩晕 → 光环与地面。 */
    private static void drive(LivingEntity living, ServerLevel serverLevel) {
        // 将手持武器的非玩家实体的基础攻速提升到 4.0
        if (!(living instanceof Player)) {
            if (!living.getWeaponItem().isEmpty()
                    && !living.getAttributes().hasModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED)) {
                living.getAttribute(Attributes.ATTACK_SPEED).addPermanentModifier(new AttributeModifier(
                        ATTACK_SPEED, 3.0, AttributeModifier.Operation.ADD_VALUE));
            } else if (living.getWeaponItem().isEmpty()
                    && living.getAttributes().hasModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED)) {
                living.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED);
            }

            if (living.getAttributes().hasAttribute(AttributeRegistry.CAST_SPEED)) {
                float f = (float) (1 / living.getAttributeValue(AttributeRegistry.CAST_SPEED));
                if (living instanceof Creeper creeper && creeper.getSwellDir() * f > 30) {
                    creeper.setSwellDir(30); // 施法速度使苦力怕提前爆炸 苦力怕爆炸时间默认为 30
                }
            }

            // 背包容量
            LivingModRoller.applyCarryCapacity(living);
        }

        // [M3] 每 tick 递增实体 timer 共享 Map（elapsed++，超 20s 存活上限移除）
        // 衰减统一由 ExpressionContext timer 接管
        ExpressionContext ctx = ExpressionContext.get(living);
        ctx.tickStore();
        // 距最近一次受击的 tick 数（从未受击 = 极大值），供 ES 充能启动延迟门控
        int sinceHit = (int) ctx.getTimerElapsed(EventName.TAKEN_HIT, 999999);

        // —— 自然回复 + 偷取 + 补给：进入 Stat 作用域使条件型回复/偷取属性生效 ——
        // 作用域标签 "regeneration" 供 stat entry 匹配（如"静止时魔力回复率提高"）。
        try (AutoCloseable scope = StatScopes.enter(living, StatContext.of("regeneration"))) {

            // 能量护盾（ES）：max>0 时初始化满值（首次），未受伤窗口内自然充能
            if (living.getAttributes().hasAttribute(AttributeRegistry.MAX_ENERGY_SHIELD)
                    && ResourceRegistry.getMaxEnergyShield(living) > 0) {
                if (!living.hasData(AttachmentRegistry.ENERGY_SHIELD)) {
                    ResourceRegistry.setEnergyShield(living, ResourceRegistry.getMaxEnergyShield(living));
                }
                boolean convalescenceActive = StatusEngine.has(living, StatusDefs.CONVALASCENCE);
                if ((sinceHit >= ResourceRegistry.getEnergyShieldRechargeDelay(living) || convalescenceActive)
                        && !ResourceRegistry.isFullEnergyShield(living)
                        && !ResourceRegistry.isEnergyShieldRechargeDisabled(living)) {
                    // 充能启动（边沿）：三个前置条件成立即「在充能」，但语料要的是「充能**开始**」
                    // 而非「充能中」——故用 timer 做边沿检测，只在从未充能转为充能的那一 tick 记一次事件。
                    // timer 活跃期间每 tick 仅续期（不重复记）；受击后停止续期，timer 过期即视为本次充能结束，
                    // 下次重新充能时可再记一次。无新附件、无每 tick 记录膨胀（每 tick emit 会灌满事件队列）。
                    // 驱动 COND_ES_RECHARGE_STARTED_2S（「ES 充能已在过去 2 秒内开始」→ 闪避加成）。
                    if (!ctx.isTimerActive(EventName.ES_RECHARGE_STARTED)) {
                        GameEvent.emit(EventName.ES_RECHARGE_STARTED, living, (LivingEntity) null, Set.of());
                    }
                    ctx.refreshTimer(EventName.ES_RECHARGE_STARTED, 40);
                    ResourceRegistry.tickEnergyShieldRecharge(living);
                }
            }

            if (living.getAttributes().hasAttribute(AttributeRegistry.MAX_RUNIC_WARD)
                    && ResourceRegistry.getMaxRunicWard(living) > 0) {
                if (!living.hasData(AttachmentRegistry.RUNIC_WARD)) {
                    ResourceRegistry.setRunicWard(living, ResourceRegistry.getMaxRunicWard(living));
                }
                if (sinceHit >= ResourceRegistry.getRunicWardRechargeDelay(living)
                        && !ResourceRegistry.isFullRunicWard(living)
                        && !ResourceRegistry.isRunicWardRechargeDisabled(living)) {
                    ResourceRegistry.tickRunicWardRecharge(living);
                }
            }

            if (living.getAttributes().hasAttribute(AttributeRegistry.MAX_MANA) && !ResourceRegistry.isFullMana(living)) {
                // 魔力未满时 自然回复
                ResourceRegistry.tickManaRegeneration(living);
            }

            if (!HealthHelper.isFullHealth(living)) {
                // 生命未满时 自然回复
                HealthHelper.tickHealthRegeneration(living);
            }

            // 怒气固有流失（Inherent Rage Loss：宽限期后按固定速率掉落，经统一资源枢纽）
            if (living.getAttributes().hasAttribute(AttributeRegistry.MAX_RAGE)) {
                ResourceRegistry.tickRageInherentLoss(living);
                ResourceRegistry.tickRageRegenFromSpent(living);
                ResourceRegistry.tickRageRegenPerSecond(living);
            }

            // 黑暗（41076 Embrace the Darkness）：启用时初始化满值 + 每 tick 返还预留
            if (DarknessHelper.isDarknessEnabled(living)) {
                if (!living.hasData(AttachmentRegistry.DARKNESS)) {
                    DarknessHelper.setDarkness(living, DarknessHelper.getMaxDarkness(living));
                }
                DarknessHelper.tickReservation(living);
            }

            // 偷取
            LeechValue.calculateAndUpdateValues(living);

            // 补给（Recoup：受到伤害后随时间恢复）
            RecoupValue.calculateAndUpdateValues(living);
        } catch (Exception ignored) {
            // 作用域关闭失败不应中断 tick 驱动
        }

        // 闪避熵值
        if (living.hasData(AttachmentRegistry.EVASION_ENTROPY)) {
            EvasionEntropy.updata(living);
        }

        // 连击自动重置
        if (living.hasData(AttachmentRegistry.COMBO_TRACKER)) {
            ResourceRegistry.tickCombo(living, serverLevel.getGameTime());
        }
        // 荣耀衰减（超过宽限期后逐 tick 衰减）
        if (living.hasData(AttachmentRegistry.GLORY_TRACKER)) {
            ResourceRegistry.tickGlory(living, serverLevel.getGameTime());
        }
        // 元技能能量衰减（参考荣耀：超过宽限期后逐 tick 回落）
        if (living.hasData(AttachmentRegistry.META_ENERGY_TRACKER)) {
            ResourceRegistry.tickMetaEnergy(living, serverLevel.getGameTime());
        }
        // 眩晕条自然衰减（唯一入口 StunMeter，经统一资源枢纽扣减）
        ResourceRegistry.tickStunDecay(living);

        // 周期施加驱动（PERIODIC 型）：持有 MultiStatEffect 周期条目的实体，按各自节拍把效果重新施加到
        // entry.target() 解析出的每个目标（见 PeriodicDriver）。
        // 【与上方 PresenceAura 并行双跑】AURA 桶退休的第一步：当前无任何数据指向 periodic，
        // 该驱动每 tick 在「无条目」短路处返回，对现有行为零影响；后续步骤迁移接线后再移除
        // 上方的 PresenceAura.tick 调用与 AURA 桶本身。
        PeriodicDriver.tick(living);
        // 标记致盲光环：持有 ENEMIES_NEAR_ENEMIES_YOU_MARK_ARE_BLINDED 的实体，对其标记的敌人周边施加致盲
        MarkBlindAura.tick(living);
        // 地面生成：持有 GROUND 型条目（CreateGroundStatEffect）的实体，周期性在脚下生成地面场
        // （见 CreateGroundDriver，独立管道，不混入 Presence 光环）
        CreateGroundDriver.tick(living);
        // 举盾前方锥形痿顿光环：持标记实体举盾时周期性对正前方锥内敌人施加 DEBILITATE（见 ShieldFrontDebilitateAura）
        ShieldFrontDebilitateAura.tick(living);
    }
}
