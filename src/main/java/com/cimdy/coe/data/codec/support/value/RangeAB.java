package com.cimdy.coe.data.codec.support.value;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record RangeAB(
        ExpressionCodec.Value x1, ExpressionCodec.Value y1, ExpressionCodec.Value z1,
        ExpressionCodec.Value x2, ExpressionCodec.Value y2, ExpressionCodec.Value z2
) {
    public static final RangeAB EMPTY = new RangeAB(
            ExpressionCodec.of(0.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.0),
            ExpressionCodec.of(0.0), ExpressionCodec.of(0.0), ExpressionCodec.of(0.0));

    public static final Codec<RangeAB> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("x1", ExpressionCodec.of(0.0)).forGetter(RangeAB::x1),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("y1", ExpressionCodec.of(0.0)).forGetter(RangeAB::y1),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("z1", ExpressionCodec.of(0.0)).forGetter(RangeAB::z1),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("x2", ExpressionCodec.of(0.0)).forGetter(RangeAB::x2),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("y2", ExpressionCodec.of(0.0)).forGetter(RangeAB::y2),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("z2", ExpressionCodec.of(0.0)).forGetter(RangeAB::z2)
    ).apply(instance, RangeAB::new));

    public static RangeAB of(BlockPos a, BlockPos b) {
        return new RangeAB(
                ExpressionCodec.of(a.getX()), ExpressionCodec.of(a.getY()), ExpressionCodec.of(a.getZ()),
                ExpressionCodec.of(b.getX()), ExpressionCodec.of(b.getY()), ExpressionCodec.of(b.getZ()));
    }

    public static Optional<RangeAB> of(Optional<BlockPos> a, Optional<BlockPos> b) {
        if (a.isEmpty() && b.isEmpty()) return Optional.empty();
        if (a.isEmpty()) a = b;
        if (b.isEmpty()) b = a;
        return Optional.of(RangeAB.of(a.get(), b.get()));
    }

    // 从 AABB 构建，AABB 的 max 坐标需要减 1 才能得到方块坐标（因为 AABB 包围盒的 max 是方块坐标+1）
    public static RangeAB of(AABB aabb) {
        return new RangeAB(
                ExpressionCodec.of(Math.floor(aabb.minX)), ExpressionCodec.of(Math.floor(aabb.minY)), ExpressionCodec.of(Math.floor(aabb.minZ)),
                ExpressionCodec.of(Math.ceil(aabb.maxX) - 1), ExpressionCodec.of(Math.ceil(aabb.maxY) - 1), ExpressionCodec.of(Math.ceil(aabb.maxZ) - 1)
        );
    }

    // 转换为包含完整方块的 AABB（最大值加 1）
    public AABB toAABB() {
        return new AABB(Math.min(x1.getValue(), x2.getValue()), Math.min(y1.getValue(), y2.getValue()), Math.min(z1.getValue(), z2.getValue()),
                Math.max(x1.getValue(), x2.getValue()) + 1, Math.max(y1.getValue(), y2.getValue()) + 1, Math.max(z1.getValue(), z2.getValue()) + 1);
    }

    // 以 Vec3 为中心叠加偏移的 AABB（用于技能效果等）
    public AABB toAABB(Vec3 center) {
        AABB base = toAABB();
        return base.move(center.subtract(0.5, 0.5, 0.5)); // 居中适配
    }
}
