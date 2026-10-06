package com.cimdy.coe.data.helper.stat;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.registry.item.AccessorySlotRegistry;
import com.cimdy.coe.data.codec.item.AccessorySave;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.condition.ConditionAnchor;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.helper.mod.AttributeApplicator;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * 饰品栏 Stat 注入/移除（复用装备链路：{@link AttributeLimiterHelper} + {@code ITEM_STAT_ENTRIES}）。
 *
 * <p>饰品物品复用装备数据生成，其 {@code ITEM_STAT_ENTRIES} 在入槽时由
 * {@link #apply} 重烤为 {@code accessory:<globalIndex>} 前缀（与装备的 {@code equip:} 前缀平行），
 * 再经 {@link AttributeLimiterHelper#applyItemLimiter} 注入实体 StatStore；脱槽时按前缀整体移除。
 * 前缀以「全局槽位下标」为唯一标识，跨会话稳定，故登录/重生可安全重注。
 */
public final class AccessoryStatHelper {
    private AccessoryStatHelper() {}

    /**
     * 饰品入槽：把物品 ITEM_STAT_ENTRIES 重烤为 {@code accessory:<globalIndex>} 前缀，注入实体 StatStore。
     * 若物品无词缀条目（如占位 legs/quiver 仅有 MODIFIABLE_STATE 固有属性），则补注入其固有 MODIFIABLE_STATE
     * 本地属性（armor/evasion/energy/block/spirit/攻击三围），使饰品栏在入槽时真实授予固有属性。
     * 构造与 {@link AttributeApplicator} 的本地词条完全一致（ADD_VALUE + GAIN + ALWAYS），落点均为引擎
     * 真读属性 EFF，不造死线（R3）。已有词缀条目的数据生成物品不受影响（避免重复授予）。
     */
    public static void apply(Player player, ItemStack stack, int globalIndex) {
        if (stack.isEmpty()) return;
        String prefix = "accessory:" + globalIndex;
        AttributeApplicator.applyModValue(stack, prefix);
        Map<String, StatEntry> entries = new LinkedHashMap<>(
                stack.getOrDefault(DataComponentRegistry.ITEM_STAT_ENTRIES, Map.of()));
        if (entries.isEmpty()) {
            entries.putAll(intrinsicStatEntries(stack, prefix));
        }
        // 饰品栏 quiver 组：按玩家 #98「inc bonuses gained from Equipped Quiver」缩放其全部 Stat 条目
        double scale = equipmentBonusScale(player, stack, globalIndex);
        if (scale != 1.0) {
            Map<String, StatEntry> scaled = new LinkedHashMap<>();
            for (Map.Entry<String, StatEntry> e : entries.entrySet()) {
                scaled.put(e.getKey(), scaleEntry(e.getValue(), scale));
            }
            entries = scaled;
        }
        // 「已装备某槽位的全部加成改为作用于你的随从」重定向（通用槽位 → 目标重定向设施）：
        // 命中则把该槽位物品的全部条目原样保留、仅把落点换成「你的随从」获取条件。
        entries = redirectToMinions(player, entries, globalIndex);
        stack.set(DataComponentRegistry.ITEM_STAT_ENTRIES, entries);
        AttributeLimiterHelper.applyItemLimiter(player, stack, EquipmentSlot.MAINHAND);
    }

    /**
     * 取某全局槽位下标所属的<b>槽位分组 id</b>（{@code amulet} / {@code legs} / {@code quiver} …）。
     * 取自饰品槽注册表（含内置兜底），故新槽位由数据包声明即可被本设施识别，零 Java 改动。
     */
    private static String slotGroupId(Player player, int globalIndex) {
        for (AccessorySlotRegistry.SlotDescriptor d :
                AccessorySlotRegistry.slots(player.level().registryAccess())) {
            if (d.globalIndex() == globalIndex) return d.groupId().getPath();
        }
        return "";
    }

    /**
     * 槽位加成 → 随从 重定向（通用设施）：佩戴者持有 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}
     * 且其 conditionTags 命中本槽位分组 id 时，把该槽位物品的全部条目的落点换成「你的随从」。
     *
     * <p>槽位身份<b>不写在 Java 里</b>：本方法把「当前槽位分组 id」交给 markerMagnitude 的查询标签集，
     * 由 EFF 实例的 conditionTags（如 {@code {"amulet"}}）决定哪些槽位被重定向。
     * 于是「头盔加成作用于随从 / 腿甲加成作用于随从」只需新增一条同 Kind、换标签的 EFF，零 Java 改动。
     *
     * <p>Phase 2b：Target 收敛为 SELF / TRIGGER_TARGET 两态后，「作用于随从」不再是 Target 的职责，
     * 改挂 {@link RelationCondition}{@code (relation=minion, of=self)} 作为条目的实体获取条件；
     * 数值 / 条件 / 效果全部不变（仍按主人上下文求值），仅收件人改为随从实体集合。
     * StatEngine 在读取时按「master → 召唤体」作用域维把 master 存储中 Condition 取随从集合的条目
     * 聚合到该召唤体的属性读取上（见 {@code StatEngine#aggregate} 的 master 分支）。
     */
    private static Map<String, StatEntry> redirectToMinions(Player player, Map<String, StatEntry> entries,
                                                            int globalIndex) {
        if (entries.isEmpty()) return entries;
        String slotId = slotGroupId(player, globalIndex);
        if (slotId.isEmpty()) return entries;
        if (!StatEngine.gateOf(player, "equipment_bonus_to_minions",
                Set.of(slotId))) return entries;
        RelationCondition toMinions = RelationCondition.of(ConditionAnchor.SELF, RelationCondition.Relation.MINION);
        Map<String, StatEntry> redirected = new LinkedHashMap<>();
        for (Map.Entry<String, StatEntry> e : entries.entrySet()) {
            redirected.put(e.getKey(), e.getValue().withCondition(toMinions));
        }
        return redirected;
    }

    /**
     * 从物品固有 EquipmentRead 模板展开基础属性（armor/evasion/energy/block/spirit/攻击三围）作为饰品前缀 StatEntry。
     * 仅字段非零才生成条目（baseStatEntries 已按模板字段产出 StatRead，未填充字段不出现）。
     * 与 applyModValue 注入的固有条目同源；仅当物品无任何词缀与固有条目时作为兜底，保证饰品栏入槽真实授予固有属性。
     */
    private static Map<String, StatEntry> intrinsicStatEntries(ItemStack stack, String prefix) {
        Map<String, StatEntry> m = new LinkedHashMap<>();
        EquipmentState state =ModifiableStateOps.equipment(stack);
        int idx = 0;
        for (StatEntry e : state.equipment().map(eq -> eq.baseStatEntries()).orElse(List.of())) {
            m.put(prefix + "#" + (idx++), e);
        }
        return m;
    }

    /**
     * 计算某槽位分组「inc bonuses gained from Equipped category」缩放系数（通用设施）。
     * 按全局槽位下标取分组 id（quiver / ring / amulet / …），经 StatEngine.magnitudeOf 读
     * equipment_bonus_mult @ category 的 INERT 标记幅度，返回 (1 + 幅度)；无对应条目或幅度为 0 时返回 1.0（不缩放）。
     * 读点真实（入槽必调用），非死线（R3）。新增类别只需数据包写入 equipment_bonus_mult @ category，零 Java 改动。
     */
    private static double equipmentBonusScale(Player player, ItemStack stack, int globalIndex) {
        String gid = slotGroupId(player, globalIndex);
        if (gid.isEmpty()) return 1.0;
        double mag = StatEngine.magnitudeOf(player, "equipment_bonus_mult", Set.of(gid));
        return mag == 0.0 ? 1.0 : (1.0 + mag);
    }

    /** 缩放单条 StatEntry 数值（仅常量值被缩放；表达式值保持原样以免破坏联动）。 */
    private static StatEntry scaleEntry(StatEntry entry, double scale) {
        ExpressionCodec.Value amt = entry.amount();
        if (amt.value().left().isEmpty()) return entry;
        double d = amt.value().left().get() * scale;
        return entry.withAmount(new ExpressionCodec.Value(Either.left(d)));
    }

    /** 饰品脱槽：按前缀整体移除对应 Stat 条目。 */
    public static void remove(Player player, int globalIndex) {
        if (!player.hasData(AttachmentRegistry.STAT_STORE)) return;
        StatStore store = player.getData(AttachmentRegistry.STAT_STORE);
        player.setData(AttachmentRegistry.STAT_STORE, store.removeBySourcePrefix("accessory:" + globalIndex));
    }

    /** 清空全部饰品 Stat（按全局下标 0..total 移除）。 */
    public static void removeAll(Player player, int total) {
        for (int i = 0; i < total; i++) remove(player, i);
    }

    /**
     * 登录/重生后按附件重注全部饰品 Stat。饰品存于自定义附件（非 EquipmentSlot），
     * 不会触发 {@code LivingEquipmentChangeEvent}，故须在此显式重注。前缀 = 全局槽位下标（稳定）。
     * <p>槽位定义现为服务端配置驱动：经 {@link AccessorySlotRegistry#slots(RegistryAccess)} 读取
     * {@link com.cimdy.coe.event.data.LoadEvent#ACCESSORY_SLOT_MAP} 快照缓存（数据来自 config/coe/slots.json，
     * 原 registry lookup 与数据包路径扫描均已移除）。
     */
    public static void reinjectAll(Player player) {
        if (!player.hasData(AttachmentRegistry.ACCESSORY_SAVE)) return;
        AccessorySave save = player.getData(AttachmentRegistry.ACCESSORY_SAVE);
        AccessorySave updated = save;
        int global = 0;
        for (AccessorySlotRegistry.SlotDescriptor d :
                AccessorySlotRegistry.slots(player.level().registryAccess())) {
            ItemStack stack = updated.get(global);
            if (!stack.isEmpty()) {
                apply(player, stack, global);
                updated = updated.with(global, stack);
            }
            global++;
        }
        player.setData(AttachmentRegistry.ACCESSORY_SAVE, updated);
    }
}
