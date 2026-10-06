package com.cimdy.coe.stronghold.energy;

import com.cimdy.coe.stronghold.area.StrongholdRepository;
import com.cimdy.coe.data.codec.stronghold.StrongholdGroup;
import com.cimdy.coe.data.codec.stronghold.SaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 末影能量账本服务：负责据点组内末影能量的增减、查询与跨维传送费用计算。
 * 从原 God Class {@link com.cimdy.coe.stronghold.energy.StrongholdHelper} 拆分而来，逻辑完全一致。
 */
public final class EnderEnergyLedger {
    public static final int ENERGY_PER_DIMENSION = 10;
    public static final int ENERGY_PER_GLOBAL = 30;

    private EnderEnergyLedger() {
    }

    public static boolean addEnderEnergy(ServerLevel level, BlockPos pos, double amount) {
        return modifyEnderEnergy(level, new GlobalPos(level.dimension(), pos), amount);
    }

    public static boolean reduceEnderEnergy(ServerLevel level, BlockPos pos, double amount) {
        return modifyEnderEnergy(level, new GlobalPos(level.dimension(), pos), -amount);
    }

    /** 跨维度增减能量（服务端专用） */
    public static boolean modifyEnderEnergy(ServerLevel level, GlobalPos pos, double delta) {
        if (delta == 0) return false;
        SaveData data = StrongholdRepository.getHoldData(level);
        Optional<StrongholdGroup> groupOpt = StrongholdRepository.findHoldGroupContainingPos(level, pos);
        if (groupOpt.isEmpty()) return false;
        StrongholdGroup group = groupOpt.get();
        double newEnergy = Math.max(0, group.enderEnergy() + delta);
        String groupName = findGroupName(data, group);
        if (groupName == null) return false;
        StrongholdRepository.setHoldData(level, data.setGroup(groupName, new StrongholdGroup(group.owners(), group.holds(), newEnergy)));
        return true;
    }

    // ==================== 能量查询（客户端安全） ====================

    public static double getHoldEnergy(Level level, GlobalPos pos) {
        return StrongholdRepository.findHoldGroupContainingPos(level, pos).map(StrongholdGroup::enderEnergy).orElse(0.0);
    }

    public static double getHoldEnergy(Level level, BlockPos pos) {
        return getHoldEnergy(level, new GlobalPos(level.dimension(), pos));
    }

    public static boolean hasEnderEnergy(Level level, GlobalPos pos, double amount) {
        return StrongholdRepository.findHoldGroupContainingPos(level, pos).map(g -> g.enderEnergy() >= amount).orElse(false);
    }

    public static boolean hasEnderEnergy(Level level, BlockPos pos, double amount) {
        return hasEnderEnergy(level, new GlobalPos(level.dimension(), pos), amount);
    }

    public static double getEnergyCost(ResourceKey<Level> dim1, ResourceKey<Level> dim2) {
        return dim1.equals(dim2) ? ENERGY_PER_DIMENSION : ENERGY_PER_GLOBAL;
    }

    /**
     * 距离相关计费（用户需求：与距离无关的设计可改为距离相关）。
     *
     * <p>公式：基础（同维 {@link #ENERGY_PER_DIMENSION}=10 / 跨维 {@link #ENERGY_PER_GLOBAL}=30）
     * + 每 64 格曼哈顿距离 +1（向上取整）。同维最小 10、跨维最小 30 保持既有下限，
     * 长距离线性递增；异维时坐标曼哈顿距离仍可比较（两终端在各自维度的相对位置）。
     */
    public static double getEnergyCost(GlobalPos from, GlobalPos to) {
        double base = from.dimension().equals(to.dimension()) ? ENERGY_PER_DIMENSION : ENERGY_PER_GLOBAL;
        long dist = manhattan(from.pos(), to.pos());
        return base + Math.ceil(dist / 64.0);
    }

    /** 曼哈顿距离（long 防溢出；26.2 BlockPos getter 返回 int）。 */
    public static long manhattan(BlockPos a, BlockPos b) {
        return Math.abs((long) a.getX() - b.getX())
                + Math.abs((long) a.getY() - b.getY())
                + Math.abs((long) a.getZ() - b.getZ());
    }

    @Nullable
    private static String findGroupName(SaveData data, StrongholdGroup target) {
        for (var entry : data.groups().entrySet())
            if (entry.getValue().equals(target)) return entry.getKey();
        return null;
    }
}
