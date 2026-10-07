package com.cimdy.coe.data.codec.skill.phase.delivery.impl.area;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * 区域子形态枚举 —— 把范围爆发 / 持续危害 / 残留拾取 / 光束 / 波 五种几何在<b>同一份记录</b>上以 {@code geometry} 轴区分（D4丙 对称归并）。
 *
 * <p>五个子形态共用同一维护路径（同一实体引用 + 同一执行器 {@link AreaDeliveryExecutor}），
 * 仅在运行时按 {@code geometry} 分流到各自的行为分支（范围爆发 / 持续危害 / 残留拾取 / 光束 / 波）。
 */
public enum AreaGeometry implements StringRepresentable {
    AREA, HAZARD, REMNANT, BEAM, WAVE;

    public static final Codec<AreaGeometry> CODEC = StringRepresentable.fromEnum(AreaGeometry::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
