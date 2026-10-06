package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 状态 ↔ Stat 桥梁。将带 {@link StatusStatProvider} 的状态，在激活期间把其
 * stat 条目（{@link StatEntry}）注入实体的 STAT_STORE（属性附件），使对应属性在
 * {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#fold} 读值时按
 * StatContext 作用域即时聚合；状态到期自动移除。
 *
 * <p>设计要点（对齐用户约束「buff 用 Stat 而非事件回调」）：
 * <ul>
 *   <li>条目是否落在 STAT_STORE 中，本身即「状态是否激活」的门控；</li>
 *   <li>条件「XX 时」由条目的 tags/conditions 表达（行为 enter 对应 StatContext tag），</li>
 *   <li>不订阅任何事件做瞬时属性叠加，避免旧方案的随机 UUID 每 tick 堆叠 bug。</li>
 * </ul>
 *
 * <p>寻址语义：注入/移除按来源前缀 {@code "status:<type.id>"} 精确寻址，
 * 取代原 {@code removeAll(值相等)} 语义——不再依赖 StatEntry 的 equals（StatRead/StatGroup/
 * 未覆写 equals，值相等会静默失效）；同一状态类型在 {@link StatusStore} 中唯一
 * （Map&lt;StatusType, StatusInstance&gt;），故类型 id 即稳定寻址键。{@code inst.payload}
 * 只存来源前缀字符串，不再存条目列表。
 */
public final class StatusStatBridge {

    private StatusStatBridge() {}

    /** 状态在 STAT_STORE 中的来源前缀（按状态类型稳定寻址；同类型唯一实例）。 */
    private static String sourcePrefix(StatusInstance inst) {
        return "status:" + inst.type.id;
    }

    /** 状态激活时注入其 stat 贡献（幂等：已注入则跳过，更新由 resync 负责）。 */
    public static void add(LivingEntity living, StatusInstance inst) {
        if (inst.type.statProvider == null) return;
        if (inst.payload != null) return;   // 已注入（payload 存来源前缀）
        List<StatEntry> entries = inst.type.statProvider.provide(living, inst);
        String prefix = sourcePrefix(inst);
        if (entries.isEmpty()) { inst.payload = prefix; return; }
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        Map<String, StatEntry> keyed = new LinkedHashMap<>();
        int i = 0;
        for (StatEntry e : entries) keyed.put(prefix + "#" + (i++), e);
        living.setData(AttachmentRegistry.STAT_STORE, store.addModifiers(keyed));
        inst.payload = prefix;
    }

    /** 状态到期时移除其 stat 贡献（按来源前缀寻址，不再依赖值相等）。 */
    public static void remove(LivingEntity living, StatusInstance inst) {
        if (inst.payload == null) return;
        String prefix = (String) inst.payload;
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        living.setData(AttachmentRegistry.STAT_STORE, store.removeBySourcePrefix(prefix));
        inst.payload = null;
    }

    /** 节拍重同步：补回被天赋重建清掉的状态贡献（先删旧、再注入新，无重复；全程增量 API）。 */
    public static void resync(LivingEntity living) {
        StatusStore ss = living.getData(AttachmentRegistry.STATUS_STORE);
        StatStore statStore = living.getData(AttachmentRegistry.STAT_STORE);
        StatStore next = statStore;
        for (StatusInstance inst : ss.instances.values()) {
            if (inst.type.statProvider == null) continue;
            String prefix = sourcePrefix(inst);
            next = next.removeBySourcePrefix(prefix);
            List<StatEntry> entries = inst.type.statProvider.provide(living, inst);
            if (entries.isEmpty()) { inst.payload = prefix; continue; }
            Map<String, StatEntry> keyed = new LinkedHashMap<>();
            int i = 0;
            for (StatEntry e : entries) keyed.put(prefix + "#" + (i++), e);
            next = next.addModifiers(keyed);
            inst.payload = prefix;
        }
        living.setData(AttachmentRegistry.STAT_STORE, next);
    }
}
