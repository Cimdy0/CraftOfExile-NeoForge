package com.cimdy.coe.item.currency;

import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.currency.CurrencyOperation;
import com.cimdy.coe.data.codec.item.currency.OpType;
import com.cimdy.coe.data.codec.item.currency.Precondition;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.helper.item.EquipGuard;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.data.helper.mod.ModOps;
import com.cimdy.coe.data.helper.mod.ModGenerator;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

/**
 * 通货统一分发器。原先散落在各 orb 中的样板（镜像/鉴定/稀有度/类型 校验 + 消耗 + 音效）
 * 在此集中实现；具体工艺只改写目标物品的通用状态（装备 {@link EquipmentState} / 药剂 / 护符），
 * 随后由现有 Stat 层重算实体属性。
 *
 * <p>操作分发完全数据驱动：{@link OpType} 每个常量自带 {@code apply}/{@code guard} 方法，
 * 本类不再需要 switch——新增通货只需在 {@link OpType}/{@link CurrencyType} 增常量。</p>
 *
 * <p><b>载体通用性</b>：门控一律读 {@link ModifiableState} 通用载体（经
 * {@link com.cimdy.coe.data.helper.item.ModifiableStateOps} 分派），不再硬编码 {@code MODIFIABLE_STATE}——
 * 否则药剂 / 护符的真实稀有度会被忽略，其词缀与稀有度类通货永远无法生效。
 * 仅「镜像 / 鉴定标记」与「类型链」两项是装备专有维度，非装备载体按缺省（未镜像 / 已有类型链）处理。</p>
 */
public final class CurrencyApplier {

    private CurrencyApplier() {
    }

    public static boolean apply(CurrencyOperation op, ItemStack carriedItem, Slot slot, ClickAction action, Player player) {
        if (carriedItem.isEmpty() || slot.getItem().isEmpty() || action != ClickAction.SECONDARY) {
            return false;
        }
        // 知识卷轴：全局随机生成一件装备（数据驱动 —— 仅写入基底属性，不附词缀）；
        // 忽略目标物品，产物直接入背包；物等 = 玩家等级。
        if (op.operation().type() == OpType.GENERATE) {
            op.operation().type().apply(player, slot.getItem(), op.operation(), player.level().getRandom());
            carriedItem.shrink(1);
            playusedSound(player);
            return true;
        }
        if (op.operation().type() == OpType.NONE) {
            return false;
        }

        ItemStack target = slot.getItem();
        Precondition p = op.precondition();

        // 装备类域：先解析并写回类型（武器 / 护甲域的判定依赖 types，交由数据侧的掉落权重表解析）
        if (p.domain().isEquipmentLike() && p.requireType()) {
            List<Identifier> types = ModOps.getItemType(target);
            if (types.isEmpty()) {
                player.sendSystemMessage(Component.translatable("item.state.type.coe.unknown"));
                return true;
            }
            ModOps.setType(target, types);
        }

        // 通用载体：装备 / 药剂 / 护符经 ModifiableStateOps 按组件分派读取。
        // 稀有度、镜像、鉴定这些门控从此对三类载体同构——过去一律读 MODIFIABLE_STATE 默认值，
        // 导致药剂 / 护符的真实稀有度被忽略（永远判为 NONE）。
        ModifiableState carrier = ModifiableStateOps.get(target).orElse(null);
        EquipmentState equipment = carrier instanceof EquipmentState eq ? eq : null;

        // 已装备（穿着 / 佩戴 / 持握）的物品一律不可工艺：与 POE2 一致，须先卸下
        if (EquipGuard.isEquipped(player, slot)) {
            player.sendSystemMessage(Component.translatable("item.currency.coe.equipped"));
            return true;
        }

        // 目标域：装备 / 武器 / 护甲 / 药剂 / 护符 / 任意词缀载体 / 技能石——非本域物品一律拒绝
        // （数据驱动，不加分支）
        if (!p.domain().matches(target, carrier)) {
            player.sendSystemMessage(p.domain().fail());
            return true;
        }

        // 镜像 / 鉴定是装备专有的标记维度（markers 承载于 EquipmentState）；非装备载体无此二标记，
        // 视作「未镜像 / 未鉴定」，因此这两项门控对药剂 / 护符天然放行。
        if (p.blockMirror() && equipment != null && equipment.mirror()) {
            player.sendSystemMessage(Component.translatable("item.state.coe.mirror"));
            return true;
        }
        if (p.requireUnidentified() && equipment != null && equipment.wisdom()) {
            player.sendSystemMessage(Component.translatable("item.state.coe.un_wisdom"));
            return true;
        }
        // 稀有度为通用维度：读载体自身的档位（药剂 / 护符上限 Magic——炼金石不可用，故无 Rare）。
        if (!p.allowedRarities().isEmpty() && (carrier == null || !p.allowedRarities().contains(carrier.rarity()))) {
            player.sendSystemMessage(rarityFail(carrier == null ? Rarity.NONE : carrier.rarity()));
            return true;
        }

        OpType type = op.operation().type();
        Component guardFail = type.guard(player, target, op.operation());
        if (guardFail != null) {
            player.sendSystemMessage(guardFail);
            return true;
        }

        type.apply(player, target, op.operation(), player.level().getRandom());

        carriedItem.shrink(1);
        playusedSound(player);
        return true;
    }

    private static Component rarityFail(Rarity r) {
        return switch (r) {
            case COMMON -> Component.translatable("item.state.coe.un_common");
            case MAGIC -> Component.translatable("item.state.coe.un_magic");
            case RARE -> Component.translatable("item.state.coe.un_rare");
            case UNIQUE -> Component.translatable("item.state.coe.unique");
            case NONE -> Component.translatable("item.state.coe.below_magic");
        };
    }

    private static void playusedSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
