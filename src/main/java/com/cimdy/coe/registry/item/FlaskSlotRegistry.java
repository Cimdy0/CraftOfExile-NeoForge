package com.cimdy.coe.registry.item;

import com.cimdy.coe.data.codec.item.equipment.FlaskSlotDef;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.FlaskUsesValue;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 药剂槽注册表访问入口（数据驱动，镜像 {@link AccessorySlotRegistry}）。
 *
 * <p>条目 = 一组药剂槽（count + category）。默认 2 个药剂槽（生命 + 魔力），由服务端配置文件
 * {@code config/coe/slots.json} 的 flask_slots 提供，可被用户配置覆盖；无配置（缺失 / 解析失败）时
 * 走内置兜底（life_flask / mana_flask，与默认种子同口径）。
 * 类别（{@code coe:life_flask} / {@code coe:mana_flask}）指向可放入的物品（物品 types 命中该类别路径）。
 *
 * <p>本类把注册表扁平化为有序「全局槽位列表」供背包/统一窗口的菜单（构造槽）、容器（容量）、
 * 机制逻辑（FlaskHelper / FlaskUsesValue 遍历槽数）复用，使药剂槽数完全数据驱动、不再硬编码。
 */
public final class FlaskSlotRegistry {
    private FlaskSlotRegistry() {
    }

    /**
     * 读取药剂槽定义（现为服务端配置驱动：数据由 {@code config/coe/slots.json} 经
     * {@link LoadEvent#FLASK_SLOT_MAP} 加载缓存），扁平化为有序的全局槽位列表
     * （组顺序 = 定义 groupId 路径字典序，组内按 count 展开）。
     *
     * @param access 保留参数以兼容既有调用点（原 registry lookup 入参），配置化后不再使用。
     */
    public static List<AccessorySlotRegistry.SlotDescriptor> slots(RegistryAccess access) {
        List<AccessorySlotRegistry.SlotDescriptor> out = new ArrayList<>();
        List<Identifier> ids = new ArrayList<>(LoadEvent.FLASK_SLOT_MAP.keySet());
        ids.sort(Comparator.comparing(Identifier::getPath));
        int global = 0;
        for (Identifier id : ids) {
            FlaskSlotDef def = LoadEvent.FLASK_SLOT_MAP.get(id);
            if (def == null) continue;
            for (int i = 0; i < def.count(); i++) {
                out.add(new AccessorySlotRegistry.SlotDescriptor(global, id, i, def.category()));
                global++;
            }
        }
        // 兜底：定义表为空（如 slots.json 缺失/解析失败）时，仍提供内置 2 个药剂槽，
        // 保证药剂机制始终可用。groupId 与 category 均对齐 SlotConfigLoader 的默认种子
        // （life_flask / mana_flask），避免将来按 category 过滤时与配置口径分裂。
        if (out.isEmpty()) {
            out.add(new AccessorySlotRegistry.SlotDescriptor(0,
                    Identifier.parse(CraftOfExile.MODID + "/life_flask"), 0,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "life_flask")));
            out.add(new AccessorySlotRegistry.SlotDescriptor(1,
                    Identifier.parse(CraftOfExile.MODID + "/mana_flask"), 0,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "mana_flask")));
        }
        return out;
    }

    /** 药剂槽总数（= 全部组的 count 之和）。 */
    public static int totalSlots(RegistryAccess access) {
        return slots(access).size();
    }
}
