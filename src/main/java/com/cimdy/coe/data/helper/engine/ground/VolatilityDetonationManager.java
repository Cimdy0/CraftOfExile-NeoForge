package com.cimdy.coe.data.helper.engine.ground;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;

import com.cimdy.coe.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 波动（Volatility）引爆生产者：关闭「积满即爆炸」的游戏性循环。
 *
 * <p>POE2 语义：波动积攒至上限后延迟 {@code volatility_detonation_time} 秒爆炸，对周围敌人造成混沌伤害，然后清零。
 * 节点 {@code instability}(43944) 的「爆炸时按几率回收等量能量」挂 {@code volatility_detonated} 事件
 * （detonate 末尾 {@link StatEngine#fire}，见 grant_energy_on_volatility_detonate）。
 *
 * <p>检测点：服务器每 tick 遍历拥有波动资源的玩家；满值且未挂起 → 启动倒计时；低于满值 → 取消挂起。
 * 倒计时归零 → 引爆（半径内敌对实体受混沌伤害）+ 清零波动。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class VolatilityDetonationManager {

    /** 半径内敌对实体每点最大波动造成的混沌伤害（基准 100 满值 ≈ 100 伤害）。 */
    private static final double DAMAGE_PER_VOLATILITY = 1.0;
    /** 爆炸半径（方块）。 */
    private static final double RADIUS = 4.0;

    /** 每实体 UUID → 剩余引爆倒计时（tick）。 */
    private static final Map<UUID, Integer> pending = new HashMap<>();

    private VolatilityDetonationManager() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (var player : level.players()) {
                tickEntity(level, player);
            }
        }
    }

    private static void tickEntity(ServerLevel level, LivingEntity living) {
        UUID id = living.getUUID();
        if (ResourceRegistry.isVolatilityFull(living)) {
            if (!pending.containsKey(id)) {
                double seconds = living.getAttributeValue(AttributeRegistry.VOLATILITY_DETONATION_TIME);
                pending.put(id, (int) Math.max(1, Math.round(seconds * 20.0)));
            }
            int remain = pending.get(id) - 1;
            if (remain <= 0) {
                detonate(level, living);
                pending.remove(id);
            } else {
                pending.put(id, remain);
            }
        } else {
            // 波动低于满值 → 取消挂起（避免离满后残留倒计时）
            pending.remove(id);
        }
    }

    private static void detonate(ServerLevel level, LivingEntity source) {
        double maxV = ResourceRegistry.getMaxVolatility(source);
        float damage = (float) (maxV * DAMAGE_PER_VOLATILITY);
        AABB box = new AABB(
                source.getX() - RADIUS, source.getY() - RADIUS, source.getZ() - RADIUS,
                source.getX() + RADIUS, source.getY() + RADIUS, source.getZ() + RADIUS);
        var src = DamageTypeRegistry.getDamageSource(level, "chaos");
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == source) continue;
            if (!target.isAlive()) continue;
            if (target.isAlliedTo(source)) continue;
            // 不承受波动伤害（Take no Damage from Volatility）：持有 TAKE_NO_VOLATILITY_DAMAGE 标记且幅度 ≥1.0 即免疫，
            // 经 StatEngine.markerMagnitude 真读 STAT_STORE 标记（与 OFFERING_RECENT_IMMUNE 同口径）；节点取消分配立即失效。
            if (StatEngine.markerMagnitude(target, MarkerStatEffect.Kind.TAKE_NO_VOLATILITY_DAMAGE, Set.of()) >= 1.0) continue;
            target.hurt(src, damage);
        }
        // 清零波动
        ResourceRegistry.setVolatility(source, 0);
        // Stat 触发通道：波动爆炸事件（instability「爆炸时按几率返还能量」挂 volatility_detonated 标签，
        // 回传爆炸规模 maxV 供量级参考；fire 深度闸兜底级联）。
        Event.record(source, "volatility_detonated", (int) maxV);
        StatEngine.fire(source, null,
                StatContext.of(Set.of("volatility_detonated"), null, "volatility_detonated", null), null);
    }

}
