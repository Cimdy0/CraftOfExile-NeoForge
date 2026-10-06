package com.cimdy.coe.data.gen.source;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;

/** 本模组自定义数据根目录工具：标准 datapack 命名空间 {@code data/coe/} 下的 {@code coe/} 子根。 */
public final class CoeDataPaths {
    private CoeDataPaths() {
    }

    /**
     * 本模组自定义数据根目录：{@code <输出>/coe/coe}
     * （命名空间 coe 下的 coe 子根，天赋树、配方站点等数据均落于此）。
     */
    public static Path coeRoot(PackOutput output) {
        return namespaceRoot(output).resolve("coe");
    }

    /**
     * 命名空间根：{@code <输出>/coe}（无 {@code coe/} 子前缀的常规数据，
     * 即命名空间 {@code coe} 的直接子项根（常规命名空间级数据；物品族数据已迁至 coeRoot）。
     */
    public static Path namespaceRoot(PackOutput output) {
        return output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(CraftOfExile.MODID);
    }
}
