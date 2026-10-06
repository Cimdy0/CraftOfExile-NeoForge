package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;

public class SiteBindingFeedback {

    private static Player getPlayer() {
        // 专用服务端无 Minecraft 客户端类（不在 classpath），提前返回 null，避免 NoClassDefFoundError
        if (FMLEnvironment.getDist() != Dist.CLIENT) return null;
        return Minecraft.getInstance().player;
    }

    // --- 工作方块 ---
    public static void onWorkBlockBound(BlockPos workBlock, Component siteName) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".site_bound", siteName));
        playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        spawnParticles(workBlock, ParticleTypes.HAPPY_VILLAGER, 7);
    }

    public static void onWorkBlockUnbound(BlockPos workBlock, Component siteName) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".site_unbound", siteName));
        playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.9f, 0.8f);
        spawnParticles(workBlock, ParticleTypes.SMOKE, 5);
    }

    public static void onWorkBlockSwitch(BlockPos workBlock, Component siteName) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".select_site", siteName));
        playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        spawnParticles(workBlock, ParticleTypes.HAPPY_VILLAGER, 7);
    }

    // --- 区域点 A ---
    public static void onPointASet(BlockPos pos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_a_set", pos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8f, 1.8f);
        spawnParticles(pos, ParticleTypes.FLAME, 5);
    }

    public static void onPointARemoved(BlockPos pos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_a_removed", pos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.7f, 0.6f);
        spawnParticles(pos, ParticleTypes.SMOKE, 3);
    }

    public static void onPointAMoved(BlockPos oldPos, BlockPos newPos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_a_moved", oldPos.toShortString(), newPos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8f, 1.6f);
        spawnParticles(oldPos, ParticleTypes.SMOKE, 3);
        spawnParticles(newPos, ParticleTypes.FLAME, 5);
    }

    // --- 区域点 B ---
    public static void onPointBSet(BlockPos pos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_b_set", pos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8f, 1.4f);
        spawnParticles(pos, ParticleTypes.SOUL_FIRE_FLAME, 5);
    }

    public static void onPointBRemoved(BlockPos pos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_b_removed", pos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.7f, 0.8f);
        spawnParticles(pos, ParticleTypes.SMOKE, 3);
    }

    public static void onPointBMoved(BlockPos oldPos, BlockPos newPos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".point_b_moved", oldPos.toShortString(), newPos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8f, 1.2f);
        spawnParticles(oldPos, ParticleTypes.SMOKE, 3);
        spawnParticles(newPos, ParticleTypes.SOUL_FIRE_FLAME, 5);
    }

    // --- 容器 ---
    public static void onStorageBound(BlockPos storagePos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".storage_bound", storagePos.toShortString()));
        playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9f, 1.2f);
        spawnParticles(storagePos, ParticleTypes.HAPPY_VILLAGER, 5);
    }

    public static void onStorageUnbound(BlockPos storagePos) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".storage_unbound", storagePos.toShortString()));
        playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8f, 1.0f);
        spawnParticles(storagePos, ParticleTypes.SMOKE, 3);
    }

    public static void onStorageReplaced(BlockPos removedStorage, BlockPos addedStorage) {
        Player player = getPlayer();
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("hold.site." + CraftOfExile.MODID +
                ".storage_replaced", removedStorage.toShortString(), addedStorage.toShortString()));
        playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9f, 1.2f);
        spawnParticles(removedStorage, ParticleTypes.SMOKE, 3);
        spawnParticles(addedStorage, ParticleTypes.HAPPY_VILLAGER, 5);
    }

    /**
     * 收集所有交互提示的翻译键，返回 {en, zh} 双语文案。
     * 供 MiscLang 以 addPair 注册，确保 en_us 写入英文、zh_cn 写入中文（避免 en_us 被中文污染）。
     */
    public static Map<String, String[]> getTranslationKeys() {
        Map<String, String[]> keys = new LinkedHashMap<>();
        String modid = CraftOfExile.MODID;
        keys.put("hold.site." + modid + ".cancel_selection", new String[]{"Cancel selection", "取消选中"});
        // 站点类型
        keys.put("hold.site." + modid + ".forestry", new String[]{"Forestry Site", "林业站点"});
        keys.put("hold.site." + modid + ".agriculture", new String[]{"Agriculture Site", "农业站点"});
        // 按键
        keys.put("hold.site." + modid + ".site_bound", new String[]{"Site bound: %s", "已绑定站点 %s"});
        keys.put("hold.site." + modid + ".site_unbound", new String[]{"Site unbound: %s", "已解绑站点 %s"});
        keys.put("hold.site." + modid + ".site_selected", new String[]{"Site selected: %s", "已选中站点 %s"});
        keys.put("hold.site." + modid + ".point_a_set", new String[]{"Point A set: %s", "已设定点A %s"});
        keys.put("hold.site." + modid + ".point_a_removed", new String[]{"Point A cleared: %s", "已清除点A %s"});
        keys.put("hold.site." + modid + ".point_a_moved", new String[]{"Point A moved: %s -> %s", "已移动点A %s -> %s"});
        keys.put("hold.site." + modid + ".point_b_set", new String[]{"Point B set: %s", "已设定点B %s"});
        keys.put("hold.site." + modid + ".point_b_removed", new String[]{"Point B cleared: %s", "已清除点B %s"});
        keys.put("hold.site." + modid + ".point_b_moved", new String[]{"Point B moved: %s -> %s", "已移动点B %s -> %s"});
        keys.put("hold.site." + modid + ".storage_bound", new String[]{"Storage bound: %s", "已绑定容器 %s"});
        keys.put("hold.site." + modid + ".storage_unbound", new String[]{"Storage unbound: %s", "已解绑容器 %s"});
        keys.put("hold.site." + modid + ".storage_replaced", new String[]{"Storage rebound: %s -> %s", "已换绑容器 %s -> %s"});

        keys.put("hold.site." + modid + ".bind_site", new String[]{"Bind Site", "绑定站点"});
        keys.put("hold.site." + modid + ".unbind_site", new String[]{"Unbind Site", "解绑站点"});
        keys.put("hold.site." + modid + ".select_site", new String[]{"Select Site", "选中站点"});
        keys.put("hold.site." + modid + ".set_point_a", new String[]{"Set Point A", "设定点A"});
        keys.put("hold.site." + modid + ".clear_point_a", new String[]{"Clear Point A", "清除点A"});
        keys.put("hold.site." + modid + ".move_point_a", new String[]{"Move Point A", "移动点A"});
        keys.put("hold.site." + modid + ".set_point_b", new String[]{"Set Point B", "设定点B"});
        keys.put("hold.site." + modid + ".clear_point_b", new String[]{"Clear Point B", "清除点B"});
        keys.put("hold.site." + modid + ".move_point_b", new String[]{"Move Point B", "移动点B"});
        keys.put("hold.site." + modid + ".bind_storage", new String[]{"Bind Storage", "绑定容器"});
        keys.put("hold.site." + modid + ".unbind_storage", new String[]{"Unbind Storage", "解绑容器"});
        keys.put("hold.site." + modid + ".storage_full", new String[]{"Rebind Storage", "换绑容器"});
        return keys;
    }

    // --- 通用效果辅助 ---
    private static void playSound(SoundEvent sound, float volume, float pitch) {
        Player player = getPlayer();
        if (player == null) return;
        player.level().playSound(player, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void spawnParticles(BlockPos pos, ParticleOptions particle, int count) {
        Player player = getPlayer();
        if (player == null) return;
        Level level = player.level();
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 1.0 + (level.getRandom().nextDouble() - 0.5) * 0.6;
            double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.6;
            level.addParticle(particle, x, y, z, 0, 0, 0);
        }
    }
}