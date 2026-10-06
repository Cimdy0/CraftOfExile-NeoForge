package com.cimdy.coe.event.listener;

import com.cimdy.coe.stronghold.transport.ChunkBindingHandler;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.progression.quest.QuestService;
import com.cimdy.coe.data.codec.progression.quest.QuestType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * 任务进度监听：把原版世界里发生的事翻译成 {@link QuestType} 事件喂给 {@link QuestService}。
 *
 * <p>设计取向是<b>无接取</b>——玩家不需要跑回任务板点「接受」，只要前置条件满足，
 * 击杀 / 采集 / 合成 / 建造 / 探索的过程本身就在推进任务。任务因此变成
 * 「你在玩的时候顺带完成的成长曲线」，而不是打断冒险的清单事务。
 *
 * <p>区块绑定（{@link QuestType#BIND_CHUNK}）不在此处：它由
 * {@code ChunkBindingHandler.tryBind} 在绑定成功后直接上报，避免重复计数。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public class QuestProgressListener {

    /** 击杀：由伤害来源反查击杀者，只有玩家亲手（或其召唤物的主人）击杀才计数。 */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel)) return;

        Player killer = QuestService.resolveKiller(event.getSource().getEntity());
        if (killer == null) return;

        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType());
        QuestService.onEvent(killer, QuestType.KILL, id, 1);
    }

    /** 采集：拾取物品即视为采集，按实际拾取数量计数。 */
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        ItemStack stack = event.getOriginalStack();
        if (stack.isEmpty()) return;
        QuestService.onEvent(player, QuestType.GATHER,
                BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount());
    }

    /** 合成：一次合成事件按产出堆叠数量计数（合成 64 支箭算 64）。 */
    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;
        QuestService.onEvent(player, QuestType.CRAFT,
                BuiltInRegistries.ITEM.getKey(crafted.getItem()), crafted.getCount());
    }

    /** 建造：放置方块。被取消的放置（如界桩绑定失败）不计数。 */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        BlockState state = event.getPlacedBlock();
        QuestService.onEvent(player, QuestType.BUILD,
                BuiltInRegistries.BLOCK.getKey(state.getBlock()), 1);
    }

    /** 探索：抵达某维度。目标 id 即维度 id（如 {@code minecraft:the_nether}）。 */
    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        QuestService.onEvent(player, QuestType.REACH, event.getTo().identifier(), 1);
    }

}
