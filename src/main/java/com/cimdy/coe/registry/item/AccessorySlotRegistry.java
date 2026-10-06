package com.cimdy.coe.registry.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.equipment.AccessorySlotDef;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 饰品槽注册表访问入口。
 *
 * <p>槽定义现为服务端配置驱动：数据由 {@code config/coe/slots.json} 经
 * {@link LoadEvent#ACCESSORY_SLOT_MAP} 加载缓存；本类把定义表扁平化为
 * 有序「全局槽位列表」的查询，供背包菜单（构造槽）、容器（容量）、Stat 注入（前缀）复用。
 */
public final class AccessorySlotRegistry {
    private AccessorySlotRegistry() {}

    /** 单槽描述：全局下标 + 所属组 id + 组内局部下标 + 允许放入的类别（物品 types 命中该路径）。 */
    public record SlotDescriptor(int globalIndex, Identifier groupId, int localIndex, Identifier category) {}

    /**
     * 读取饰品槽定义（现为服务端配置驱动：数据由 {@code config/coe/slots.json} 经
     * {@link LoadEvent#ACCESSORY_SLOT_MAP} 加载缓存），扁平化为有序的全局槽位列表
     * （组顺序 = 定义 groupId 路径字典序，组内按 count 展开）。
     *
     * @param access 保留参数以兼容既有调用点（原 registry lookup 入参），配置化后不再使用。
     */
    public static List<SlotDescriptor> slots(RegistryAccess access) {
        List<SlotDescriptor> out = new ArrayList<>();
        List<Identifier> ids = new ArrayList<>(LoadEvent.ACCESSORY_SLOT_MAP.keySet());
        ids.sort(Comparator.comparing(Identifier::getPath));
        int global = 0;
        for (Identifier id : ids) {
            AccessorySlotDef def = LoadEvent.ACCESSORY_SLOT_MAP.get(id);
            if (def == null) continue;
            for (int i = 0; i < def.count(); i++) {
                out.add(new SlotDescriptor(global, id, i, def.category()));
                global++;
            }
        }
        // 兜底：定义表为空（如 slots.json 缺失/解析失败）时，仍提供内置 amulet/legs/quiver 三组槽，
        // 保证饰品栏机制始终可用。此处必须与 SlotConfigLoader 的默认种子（含 amulet）逐条对齐、
        // 顺序也一致：AccessorySave 是位置化存储，若兜底少了 amulet，全局下标会整体位移，
        // 导致 slotGroupId 返回 legs 而非 amulet、amulet 标签类加成静默失效。
        if (out.isEmpty()) {
            out.add(new SlotDescriptor(0,
                    Identifier.parse(CraftOfExile.MODID + "/amulet"), 0,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "accessory/amulet")));
            out.add(new SlotDescriptor(1,
                    Identifier.parse(CraftOfExile.MODID + "/legs"), 0,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "armor/legs")));
            out.add(new SlotDescriptor(2,
                    Identifier.parse(CraftOfExile.MODID + "/quiver"), 0,
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "weapon/off_hand/quiver")));
        }
        return out;
    }

    /** 饰品槽总数（= 全部组的 count 之和）。 */
    public static int totalSlots(RegistryAccess access) {
        return slots(access).size();
    }

    /** 取某分组（legs / quiver 等，按 groupId 路径大小写不敏感匹配）的全部槽位描述。 */
    public static List<SlotDescriptor> groupSlots(RegistryAccess access, String groupName) {
        List<SlotDescriptor> out = new ArrayList<>();
        if (groupName == null || groupName.isEmpty()) return out;
        for (SlotDescriptor d : slots(access)) {
            if (d.groupId().getPath().equalsIgnoreCase(groupName)) out.add(d);
        }
        return out;
    }
}
