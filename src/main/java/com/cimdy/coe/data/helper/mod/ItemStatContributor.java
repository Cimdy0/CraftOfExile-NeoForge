package com.cimdy.coe.data.helper.mod;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一「物品词缀 → StatStore」的<b>消费脊梁</b>。
 *
 * <p>装备 / 药剂 / 护符的词缀全部经由 {@code ITEM_STAT_ENTRIES}（由 {@code AttributeApplicator} 算好）落到
 * StatStore，但此前三者在各自中枢里各自重写了「重键前缀 + 注入 + 到期移除」的三步模式，造成「独立却交叉」的
 * 维护痛点。本类把这套脊梁收口为单一入口：重键前缀、合并额外条目、按 duration 注入 / 按前缀移除，
 * 装备（持久 + 移速惩罚）、药剂（计时）、护符（计时 + 缩放 + 固有 effects）各自只提供差异化参数。</p>
 *
 * <p>护符因需对「固有 effects + 词缀」二者施加 CHARM_EFFECT 缩放，走 {@link #inject} 直接注入已完整键控的
 * 条目；装备 / 药剂走 {@link #contribute} 标准路径（自动重键 ITEM_STAT_ENTRIES）。</p>
 */
public final class ItemStatContributor {

    private ItemStatContributor() {
    }

    /**
     * 标准消费：读取物品 {@code ITEM_STAT_ENTRIES}，以 {@code scope.sourcePrefix()} 重键
     * （{@code prefix + "#" + 序号}），合并 {@code scope.extra()}，注入 StatStore
     * （{@code duration} 同 scope 语义：&le;0 持久、&gt;0 计时自动过期）。
     */
    public static void contribute(LivingEntity living, ItemStack stack, ModContributionScope scope) {
        Map<String, StatEntry> itemMods = stack.get(DataComponentRegistry.ITEM_STAT_ENTRIES);
        if ((itemMods == null || itemMods.isEmpty()) && scope.extra().isEmpty()) {
            return;
        }
        Map<String, StatEntry> keyed = new LinkedHashMap<>();
        if (itemMods != null) {
            int i = 0;
            for (StatEntry e : itemMods.values()) {
                keyed.put(scope.sourcePrefix() + "#" + (i++), e);
            }
        }
        keyed.putAll(scope.extra());
        inject(living, keyed, scope.durationTicks());
    }

    /**
     * 直接注入一组已完整键控的条目（如护符把 charm.effects + 词缀经 CHARM_EFFECT 缩放后拼好的 keyed map）。
     * {@code durationTicks>0} 进计时域、归零自动移除；{@code <=0} 持久。
     */
    public static void inject(LivingEntity living, Map<String, StatEntry> keyed, int durationTicks) {
        if (keyed == null || keyed.isEmpty()) {
            return;
        }
        StatStore store = living.hasData(AttachmentRegistry.STAT_STORE)
                ? living.getData(AttachmentRegistry.STAT_STORE)
                : StatStore.empty();
        living.setData(AttachmentRegistry.STAT_STORE, store.addModifiers(keyed, durationTicks));
    }

    /** 按来源前缀移除该物品贡献的全部条目（与计时自动过期互补，避免事件漏触发残留）。 */
    public static void revoke(LivingEntity living, String sourcePrefix) {
        if (!living.hasData(AttachmentRegistry.STAT_STORE)) {
            return;
        }
        StatStore store = living.getData(AttachmentRegistry.STAT_STORE);
        living.setData(AttachmentRegistry.STAT_STORE, store.removeBySourcePrefix(sourcePrefix));
    }
}
