package com.cimdy.coe.event.stat;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class StatEvent {

    /**
     * 订阅作用域进入事件，以「事件驱动」方式主动刷新受影响实体的 stat 并广播。
     * 关键优化（降低资源消耗）：
     * <ol>
 *   <li>只重算「受本作用域标签影响」的属性——经由 {@link StatStore#tagIndex()} 取该作用域标签的
 *       反向索引交集，而非遍历全部带标签/条件属性（技能每 tick、每次命中都会触发，全量扫描开销大）。</li>
     *   <li>值未变化则跳过广播——与上次广播值比较，避免每 tick 洪泛 {@link StatUpdatedEvent}（UI 抖动 + 订阅者开销）。</li>
     *   <li>属性读取走 {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#fold} 的读时缓存，
     *       同一作用域栈内只折叠一次，后续读取 O(1)。</li>
     * </ol>
     */
    @SubscribeEvent
    public static void onStatContextEnter(StatContextEvent.Enter event) {
        LivingEntity living = event.getEntity();
        if (!(living.level() instanceof ServerLevel)) return;
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return;

        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        ExpressionContext ec = ExpressionContext.get(living);
        Set<String> scopeTags = event.getContext().tags();

        // 仅取「受本作用域标签影响」的属性：其 modifier 至少含一个本作用域标签
        // （推入一个与某 modifier 标签不相交的作用域不会改变该 modifier 的满足态，故可安全跳过）。
        Set<Holder<Attribute>> candidates = new HashSet<>();
        for (String tag : scopeTags) {
            Set<Holder<Attribute>> withTag = store.tagIndex().get(tag);
            if (withTag != null) candidates.addAll(withTag);
        }
        // 纯条件门控属性（无标签，仅 conditions 门控）不在 tagIndex 中，若只刷新标签索引会被遗漏；
        // 进入作用域时这些属性可能由「无标签」激活为「条件满足」，需并入候选集统一重算广播。
        candidates.addAll(store.conditionOnlyIndex());
        if (candidates.isEmpty()) return;

        for (Holder<Attribute> attr : candidates) {
            // 读时缓存：本作用域内首次读取才 fold，之后命中缓存 O(1)
            double value = living.getAttributeValue(attr);
            Double last = ec.getLastBroadcastStat(attr);
            if (last != null && Double.compare(last, value) == 0) continue; // 值未变，跳过广播
            ec.setLastBroadcastStat(attr, value);
            NeoForge.EVENT_BUS.post(new StatUpdatedEvent(living, attr, value));
        }
    }

}
