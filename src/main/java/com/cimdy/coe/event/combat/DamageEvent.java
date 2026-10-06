package com.cimdy.coe.event.combat;

import com.cimdy.coe.data.helper.combat.KillBurstHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.cimdy.coe.data.helper.combat.DeathHelper;
import com.cimdy.coe.data.helper.combat.HitScopeHelper;
import com.cimdy.coe.data.helper.engine.DamagePipeline;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 伤害相关事件<b>派发层</b>：只做「取参数 → 调机制」，不含任何机制实现。
 *
 * <p>机制归属（事件端禁止内联机制，避免事件类膨胀成第二引擎）：
 * <ul>
 *   <li>命中标签推导 / 命中作用域入栈 → {@link HitScopeHelper}；</li>
 *   <li>伤害分阶段结算 → {@link DamagePipeline}；</li>
 *   <li>击杀爆炸 → {@code KillBurstHelper}；死亡全流程 → {@link DeathHelper}。</li>
 * </ul>
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class DamageEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        DeathHelper.onCorpse(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void EntityInvulnerabilityCheckEvent(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            HitScopeHelper.resolveInvulnerability(event.getEntity(), serverLevel, event.getSource(), serverLevel.getRandom());
        }
    }

    @SubscribeEvent
    public static void LivingIncomingDamageEvent(LivingIncomingDamageEvent event) {
        HitScopeHelper.enterIncomingScope(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void LivingDamageEventPre(LivingDamageEvent.Pre event) {
        DamagePipeline.runPre(event);
    }

    @SubscribeEvent
    public static void LivingDeathEvent(LivingDeathEvent event) {
        DeathHelper.onDeath(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void ArmorHurtEvent(ArmorHurtEvent event) {
        DamageSource source = event.getEntity().getLastDamageSource();
        if (source != null && source.is(DamageTypeTagRegistry.IS_AILMENT)) {
            event.setCanceled(true);
        }
    }
}
