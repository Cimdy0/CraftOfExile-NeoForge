package com.cimdy.coe.item.special;

import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.state.RuneState;
import com.cimdy.coe.data.helper.item.RuneSocketOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 符文物品（数据驱动注册，stacksTo(64)）。
 *
 * <p>右键使用：把符文镶嵌到<b>另一只手</b>的装备（武器 / 护甲）的空 augment 孔，
 * 加成按宿主分支结算并落装备 ENCHANT 层。Masterwork 符文则升级插槽中已有的符文到下一阶。</p>
 *
 * <p>写入由服务端权威执行（{@code use} 服务端分支真正改组件并 consume 一枚符文）；
 * 客户端仅做"可否镶嵌"预览判定，避免双端重复写入导致组件不同步。</p>
 */
public class RuneItem extends Item {

    private final String runeId;

    public RuneItem(String runeId, Properties properties) {
        super(properties);
        this.runeId = runeId;
    }

    /** 符文数据 id（如 {@code Lesser_Adept_Rune}），供 {@link RuneSocketOps} 查表。 */
    public String runeId() {
        return runeId;
    }

    /** 显示名：按该族该阶的翻译键拼接（阶前缀 + 族名 + 「符文」）。 */
    @Override
    public Component getName(ItemStack stack) {
        RuneState state = RuneRegistry.getById(this.runeId);
        return state == null ? super.getName(stack) : state.displayName();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack rune = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        if (target.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return RuneSocketOps.canSocket(target, rune) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (RuneSocketOps.trySocket(target, rune)) {
            rune.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
