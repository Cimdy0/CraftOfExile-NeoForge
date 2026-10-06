package com.cimdy.coe.data.codec.item.currency;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.helper.item.QualityOps;
import com.cimdy.coe.data.helper.item.SocketOps;
import com.cimdy.coe.data.helper.mod.ModOps;
import com.cimdy.coe.data.helper.mod.ModGenerator;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 通货操作动词（数据驱动 + 方法直连）。
 * 每个常量自带其实际执行逻辑（{@link #apply}）与守卫（{@link #guard}），
 * {@link com.cimdy.coe.item.currency.CurrencyApplier} 不再需要 switch 分发——
 * 物品数据（{@link Operation}）经本枚举直接指向对应方法，便于新增通货只增一个常量。
 */
public enum OpType {
    NONE {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
        }
    },
    /**
     * 全局随机生成一件装备（知识卷轴）。实际生成 + 入背包在 {@code apply} 内完成，
     * 由 {@link com.cimdy.coe.item.currency.CurrencyApplier} 的 GENERATE 分支触发，跳过目标 / 域 / 稀有度门控。
     */
    GENERATE {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ItemStack generated = ModGenerator.generateRandomEquipment(
                    player.level().getRandom(), player.getData(AttachmentRegistry.LEVEL));
            if (generated.isEmpty()) {
                player.sendSystemMessage(Component.translatable("item.currency.coe.generate.empty"));
                return;
            }
            if (!player.getInventory().add(generated)) {
                player.drop(generated, false);
            }
        }
    },
    UPGRADE_RARITY {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.regenRandomDownWithRarity(random, target, op.targetRarity().orElse(Rarity.RARE),
                    op.minModTier().orElse(0));
        }
    },
    REROLL_MODS {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.regenRandomDown(random, target, op.minModTier().orElse(0));
        }
    },
    REROLL_VALUES {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.randomModValue(random, target);
        }
    },
    ADD_MOD {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            if (!ModOps.isDownNotMaxed(target)) {
                return Component.translatable("item.mod.typecoe.all.maxed");
            }
            // 位置未满 ≠ 有词缀可加：物品的 item class 若无词缀池（或候选全被下限剔除），
            // 消费一枚通货却毫无变化——必须在消耗前拦住。
            return ModOps.hasAvailableMod(target, List.of(Location.PREFIX, Location.SUFFIX), op.minModTier().orElse(0))
                    ? null : Component.translatable("item.mod.typecoe.no.available");
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.addRandomDownMod(random, target, op.minModTier().orElse(0));
        }
    },
    REMOVE_MOD {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            return ModOps.hasMods(target) ? null : Component.translatable("item.state.mods.coe.empty");
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.removeRandomDownMod(player.level(), target);
        }
    },
    /**
     * 替换一条词缀（先移除随机一条、再新增一条）。仅「新增」那条受 {@code minModTier} 下限约束，移除侧不受限。
     * 通用设施：POE2 混沌石「改一条」与高阶改造石「增一减一」共用此动词。
     */
    SWAP_MOD {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            if (!ModOps.hasMods(target)) {
                return Component.translatable("item.state.mods.coe.empty");
            }
            // 先移除后新增：若没有任何可新增的候选，就会「只减不增」地毁掉一条词缀——必须同拦。
            return ModOps.hasAvailableMod(target, List.of(Location.PREFIX, Location.SUFFIX), op.minModTier().orElse(0))
                    ? null : Component.translatable("item.mod.typecoe.no.available");
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.replaceRandomDownMod(random, player.level(), target, op.minModTier().orElse(0));
        }
    },
    SCRUB {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.resetItemToNormal(target);
        }
    },
    CHANCE_UPGRADE {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.chanceUpgrade(random, target);
        }
    },
    MIRROR {
        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            ModOps.applyMirror(player, target);
        }
    },
    /**
     * 增加品质（通用设施）。作用于哪个载体由 {@link Precondition#domain()} 决定：
     * 品质本身只是一个标量，武器 / 护甲 / 药剂 / 技能石在各自读取点换算成不同效果。
     * {@code amount} 为空时按 {@link QualityOps} 的物品等级策略自动取值。
     */
    ADD_QUALITY {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            return QualityOps.isMaxed(target)
                    ? Component.translatable("item.currency.coe.quality.maxed") : null;
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            int amount = op.amount().orElseGet(() -> QualityOps.grantFor(target));
            QualityOps.add(target, amount);
        }
    },
    /**
     * 将技能石辅助槽数<b>置为</b> {@code amount}（POE2 多阶工匠石：置位而非累加，且不可降级）。
     */
    SET_SOCKETS {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            return SocketOps.canSetSkillSockets(target, op.amountOr(SkillData.MAX_SOCKETS))
                    ? null : Component.translatable("item.currency.coe.sockets.invalid");
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            SocketOps.setSkillSockets(target, op.amountOr(SkillData.MAX_SOCKETS));
        }
    },
    /** 给装备开一个物品孔（符文 / 魂核 / 其它 augment 共用；上限由物品类型决定）。 */
    ADD_SOCKET {
        @Override
        public Component guard(Player player, ItemStack target, Operation op) {
            return SocketOps.isMaxed(target)
                    ? Component.translatable("item.currency.coe.socket.maxed") : null;
        }

        @Override
        public void apply(Player player, ItemStack target, Operation op, RandomSource random) {
            SocketOps.add(target);
        }
    };

    /** 执行本次工艺，改写目标物品状态。 */
    public abstract void apply(Player player, ItemStack target, Operation op, RandomSource random);

    /**
     * 守卫：返回失败提示（需阻断且不消耗通货）；{@code null} 表示通过。
     * 默认通过；ADD_MOD / REMOVE_MOD 需校验词缀槽位，品质 / 孔类需校验上限。
     */
    public Component guard(Player player, ItemStack target, Operation op) {
        return null;
    }
}
