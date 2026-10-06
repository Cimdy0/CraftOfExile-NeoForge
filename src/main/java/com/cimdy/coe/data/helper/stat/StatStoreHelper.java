package com.cimdy.coe.data.helper.stat;

import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.event.combat.status.EffectEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import net.minecraft.world.entity.LivingEntity;

/**
 * STAT_STORE 写入口（实体 stat 条目的加 / 删）。
 *
 * <p>原为 {@code AttributeEvent} 内的静态工具方法——按分层纪律（事件类只做派发，
 * 机制归 {@code data/helper/**}）迁出，供事件层与其他子系统（如 {@code EffectEvent}）共用。
 */
public final class StatStoreHelper {

    private StatStoreHelper() {}

    /** 把一条 Stat 条目以显式来源键合并进实体 STAT_STORE（与天赋 / 状态 / 技能同源）。默认永久。 */
    public static void addStatEntry(LivingEntity living, String key, StatEntry entry) {
        addStatEntry(living, key, entry, 0);
    }

    /**
     * 合并一条 Stat 条目并（可选）登记持续时长（tick）：
     * {@code ticks <= 0} 永久（仅事件显式移除），{@code ticks > 0} 同时走计时自动过期，
     * 与事件显式移除互补——任一方触发都能让条目消失，防止事件漏检测导致 stat 永久不过期。
     */
    public static void addStatEntry(LivingEntity living, String key, StatEntry entry, int ticks) {
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) {
            living.setData(AttachmentRegistry.STAT_STORE, StatStore.empty());
        }
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        living.setData(AttachmentRegistry.STAT_STORE, store.addModifier(key, entry, ticks));
    }

    /** 回滚：从 STAT_STORE 按来源前缀移除该来源贡献的全部 Stat 条目（键匹配 '#' 之前的字符）。 */
    public static void removeStatEntry(LivingEntity living, String key) {
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        living.setData(AttachmentRegistry.STAT_STORE, store.removeBySourcePrefix(key));
    }
}
