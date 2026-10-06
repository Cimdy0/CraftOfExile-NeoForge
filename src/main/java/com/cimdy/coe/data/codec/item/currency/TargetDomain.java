package com.cimdy.coe.data.codec.item.currency;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.state.CharmState;
import com.cimdy.coe.data.codec.item.flask.FlaskState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * 通货目标域（数据驱动前置条件的一部分）：声明一枚通货可以作用于哪一类物品。
 *
 * <p>原先 {@link com.cimdy.coe.item.currency.CurrencyApplier} 硬编码「目标一定是装备」——
 * 稀有度 / 类型 / 镜像 / 鉴定校验全部按 {@code EquipmentState} 语义执行，
 * 技能石与药剂无从接入。引入本枚举后，「作用于什么」成为数据：新增作用于新物品域的通货
 * 只需在此增一个常量，无需再改分发器。</p>
 *
 * <p>判定一律以<b>通用载体</b> {@link ModifiableState} 为准（装备 {@code MODIFIABLE_STATE} /
 * 药剂 {@code FLASK_ITEM_STATE} / 护符 {@code CHARM_ITEM_STATE}，经
 * {@code ModifiableStateOps} 分派），不依赖物品类继承：{@code carrier == null} 表示该栈不是任何
 * 可工艺载体。这样「稀有度 / 品质 / 词缀」这些通用维度的门控天然对三类载体同构。</p>
 */
public enum TargetDomain {
    /** 任意装备：沿用既有稀有度 / 类型 / 镜像 / 鉴定校验。 */
    EQUIPMENT {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof EquipmentState;
        }
    },
    /** 仅限武器（磨刀石：POE2「Only usable on martial weapons」）。 */
    WEAPON {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof EquipmentState state && state.isWeapon();
        }
    },
    /** 仅限护甲（护甲碎片：POE2「Only usable on armours」）。 */
    ARMOUR {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof EquipmentState state && state.isArmor();
        }
    },
    /** 武器或护甲（工匠石：POE2「Only usable on weapon and armour equipment」，首饰不在内）。 */
    WEAPON_OR_ARMOUR {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof EquipmentState state && (state.isWeapon() || state.isArmor());
        }
    },
    /** 药剂（玻璃珠：POE2「Only usable on flasks」——不含护符）。 */
    FLASK {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof FlaskState;
        }
    },
    /**
     * 任意「有显式词缀的载体」：装备 ∪ 药剂 ∪ 护符（技能石无显式词缀，不在内）。
     *
     * <p>POE2 实证（poe2wiki Crafting 页）：以下通货的限制条件只写稀有度、不写物品类别，
     * 故对三类载体同构——蜕变（normal）/ 增幅（magic）/ 湮灭（magic or rare）/
     * 混沌（magic or rare）/ 神圣（无限制）；药剂 / 护符的词缀上限为 2（1 前缀 + 1 后缀）。</p>
     *
     * <p>明确<b>不</b>属于本域：炼金石（POE2 原文「Cannot be used on flasks / charms / relics」）、
     * 富豪 / 崇高（需 Rare，而药剂 / 护符上限为 Magic）、机会石（走 Unique 通道）。</p>
     */
    AFFIX_ITEM {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            return carrier instanceof EquipmentState
                    || carrier instanceof FlaskState
                    || carrier instanceof CharmState;
        }
    },
    /** 技能石（棱光石 / 多阶工匠石）。 */
    SKILL_GEM {
        @Override
        public boolean matches(ItemStack stack, ModifiableState carrier) {
            SkillData data = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
            return !data.isEmpty();
        }
    };

    /**
     * 该域是否接受此目标。
     *
     * @param carrier 该栈的通用物品状态；{@code null} 表示不是可工艺载体（无任何状态组件）。
     */
    public abstract boolean matches(ItemStack stack, ModifiableState carrier);

    /** 域不匹配时的提示文本（{@code item.currency.coe.domain.<域>}）。 */
    public Component fail() {
        return Component.translatable("item.currency.coe.domain." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * 该域的判定是否依赖<b>装备类型链</b>（types 已被解析并写入 {@code EquipmentState}）。
     * 依赖时，分发器会先解析并写回类型再判定域——这是装备子集域（武器 / 护甲）的必要前置。
     * 药剂 / 护符域与技能石域不依赖类型链。
     */
    public boolean isEquipmentLike() {
        return this == EQUIPMENT || this == WEAPON || this == ARMOUR || this == WEAPON_OR_ARMOUR;
    }
}
