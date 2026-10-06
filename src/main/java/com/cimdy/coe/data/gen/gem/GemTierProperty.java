package com.cimdy.coe.data.gen.gem;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.GemState;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

/**
 * 未切割宝石等阶选择属性 —— 从统一组件 {@link DataComponentRegistry#MODIFIABLE_STATE} 内的
 * {@link GemState} 读取等阶（tier），供物品模型 {@code minecraft:select} 按等阶切换贴图。
 *
 * <p>取代原 {@code ComponentContents(gem_tier)}：等阶已并入 {@link GemState}（存于 MODIFIABLE_STATE），
 * 不再有独立 {@code GEM_TIER} 组件。本属性从复合组件中提取等阶子字段。</p>
 *
 * <p><b>必注册（关键不变量）：</b>{@code minecraft:select} 模型的 {@code property} 字段走
 * {@code SelectItemModelProperties} 的 id 分派表。未在表中登记的 {@code Type} 编码时会报
 * {@code Element with unknown id}，且该错误抛在 datagen 落盘阶段（{@code ModelProvider.save}），
 * 表现为「整个 rundata 挂掉」而非模型缺失。故本类必须借
 * {@link RegisterSelectItemModelPropertyEvent} 把自己登记为 {@code coe:gem_tier}，
 * 客户端与 datagen（{@link net.minecraft.client.ClientBootstrap#bootstrap()} 内触发）两条路径都会生效。</p>
 */
@EventBusSubscriber(modid = CraftOfExile.MODID, value = Dist.CLIENT)
public class GemTierProperty implements SelectItemModelProperty<Integer> {

    /** 模型中 {@code property} 字段写出的标识：{@code "coe:gem_tier"}。 */
    public static final Identifier ID = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "gem_tier");

    public static final SelectItemModelProperty.Type<GemTierProperty, Integer> TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new GemTierProperty()), Codec.INT);

    @SubscribeEvent
    public static void onRegisterSelectItemModelProperty(RegisterSelectItemModelPropertyEvent event) {
        event.register(ID, TYPE);
    }

    @Override
    public Integer get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof GemState g ? g.tier() : null;
    }

    @Override
    public Codec<Integer> valueCodec() {
        return Codec.INT;
    }

    @Override
    public SelectItemModelProperty.Type<? extends SelectItemModelProperty<Integer>, Integer> type() {
        return TYPE;
    }
}
