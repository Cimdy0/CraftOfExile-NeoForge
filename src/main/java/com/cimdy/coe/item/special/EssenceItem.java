package com.cimdy.coe.item.special;

import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceState;
import com.cimdy.coe.data.helper.item.EssenceSocketOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 精髓物品（数据驱动注册，stacksTo(64)）。
 *
 * <p>右键使用：把精髓作用到<b>另一只手</b>的装备。前三阶把<b>魔法</b>装备升为稀有并必定添加一条精髓词缀
 * （空槽再由可用池随机填补）；终阶 / 单阶作用于<b>稀有</b>装备，移除一条随机词缀后添加必定出现的精髓词缀。
 * 精髓词落 {@code Location.PREFIX}/{@code SUFFIX}（与符文落 ENCHANT 的本质区别）。</p>
 *
 * <p>写入由服务端权威执行（{@code use} 服务端分支真正改组件并 consume 一枚精髓）；
 * 客户端仅做"可否作用"预览判定，避免双端重复写入导致组件不同步。</p>
 */
public class EssenceItem extends Item {

    private final String essenceId;

    public EssenceItem(String essenceId, Properties properties) {
        super(properties);
        this.essenceId = essenceId;
    }

    /** 精髓数据 id（如 {@code LifeEssence}），供 {@link EssenceSocketOps} 查表。 */
    public String essenceId() {
        return essenceId;
    }

    /** 显示名：按该族该阶的翻译键拼接（阶前缀 + 族名 + 「精髓」）。 */
    @Override
    public Component getName(ItemStack stack) {
        EssenceState state = EssenceRegistry.getById(this.essenceId);
        return state == null ? super.getName(stack) : state.displayName();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack essence = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);
        if (target.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return EssenceSocketOps.canApply(target, essence) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (EssenceSocketOps.tryApply(level, target, essence)) {
            essence.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
