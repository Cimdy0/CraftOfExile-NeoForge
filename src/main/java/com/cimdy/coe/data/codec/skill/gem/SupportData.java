package com.cimdy.coe.data.codec.skill.gem;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.skill.support.SupportGem;
import com.cimdy.coe.data.codec.item.state.GemState;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 辅助技能石存储数据 — 存储 Identifier 引用辅助宝石定义，以及自身等级。
 * 运行时通过 {@link LoadEvent.GenEvent#ITEM_SUPPORT_GEM_MAP} 查找完整定义，
 * 配合自身等级按 math 表达式计算等级加成效果。
 */
public record SupportData(Identifier id, ModifierValue.Int level) {
    public static final SupportData EMPTY = new SupportData(CraftOfExile.EMPTY_LOCATION,
            ModifierValue.Int.create(1));

    public static final Codec<SupportData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SupportData::id),
            ModifierValue.Int.CODEC.optionalFieldOf("level",
                    ModifierValue.Int.create(1)).forGetter(SupportData::level)
    ).apply(instance, SupportData::new));

    public boolean isEmpty() {
        return id.equals(CraftOfExile.EMPTY_LOCATION);
    }

    /** 将本辅助数据还原为辅助宝石物品（用于从插槽卸下时放回物品栏）。 */
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(ItemRegistry.SUPPORT_GEM.get());
        stack.set(DataComponents.ITEM_MODEL, id);
        stack.set(DataComponents.ITEM_NAME, SupportGem.getDisplayName(id));
        stack.set(DataComponentRegistry.SUPPORT_DATA, this);
        // 同时持久化到统一物品状态组件（itemdata）：辅助宝石作为完整宝石物品参与通用维度
        // （等阶 tier / 稀有度 / 词缀 / 品质）管线，与未切割宝石的 GemState 一致。
        SupportGem def = GemCategory.getSupportGem(id);
        int tier = def != null ? def.tier() : 1;
        stack.set(DataComponentRegistry.MODIFIABLE_STATE.get(), GemState.create(List.of(), tier, 0, List.of()));
        return stack;
    }

    public int getLevel() {
        return level.get();
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static class Builder {
        private final Identifier id;
        private ModifierValue.Int level = ModifierValue.Int.create(1);

        private Builder(Identifier id) {
            this.id = id;
        }

        public Builder level(ModifierValue.Int level) {
            this.level = level;
            return this;
        }

        public Builder setOriginalLevel(int originalLevel) {
            this.level = this.level.setOriginal(originalLevel);
            return this;
        }

        public SupportData build() {
            return new SupportData(id, level);
        }
    }
}
