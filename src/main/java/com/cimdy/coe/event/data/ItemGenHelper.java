package com.cimdy.coe.event.data;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.item.ItemWeight;
import com.cimdy.coe.data.codec.item.ItemInstance;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.List;

/**
 * 数据生成期辅助：{@code ItemWeight} 类型树的注册。
 *
 * <p>把「父节点声明子类别 / 叶节点声明载体物品」两种形态收成两个具名工厂，
 * 使 {@code CoeDataBootstrap.registerItemType} 只剩树形声明本身，
 * 也把「叶节点一律用显式物品、不用标签」这条纪律固定在唯一入口上。</p>
 */
public final class ItemGenHelper {

    private ItemGenHelper() {
    }

    /** 注册一份 {@code <path>/type} 权重文件（类型键 = {@code type.json} 的父目录名）。 */
    public static void type(BootstrapContext<ItemInstance> bootstrap, String path, List<ItemWeight> weights) {
        bootstrap.register(ResourceKey.create(LoadEvent.ITEM_EQUIPMENT_KEY,
                        Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path + EquipmentGen.TYPE_SUFFIX)),
                ItemWeight.Weights.from(weights));
    }

    /**
     * 父节点形态：以<b>类型键</b>声明直接子类别。
     * 解析（{@code ModGenerator#getItemType}）时由子类别键反查本节点，逐层向上补全整条类型链。
     */
    public static List<ItemWeight> children(String... typeKeys) {
        return Arrays.stream(typeKeys).map(k -> ItemWeight.from(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, k), 1000)).toList();
    }

    /**
     * 叶节点形态：以<b>显式载体物品</b>声明本类别成员。
     * 不使用物品标签——标签需内容侧逐物品打标、易漏，且同一标签常横跨多个类别（解析不可确定）。
     */
    @SafeVarargs
    public static List<ItemWeight> items(Holder<Item>... items) {
        return Arrays.stream(items).map(i -> ItemWeight.from(i, 1000)).toList();
    }
}
