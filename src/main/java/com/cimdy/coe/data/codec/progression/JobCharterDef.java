package com.cimdy.coe.data.codec.progression;

import com.cimdy.coe.data.codec.stronghold.task.work.WorkType;
import com.cimdy.coe.data.codec.stronghold.task.work.WorkTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 工人工种授权（数据驱动地把 stronghold 里硬编码的 {@code WorkType} 外置成 JSON）。
 *
 * <p>整合包作者用此声明：某工种在 {@code min_level} 级、且已解锁 {@code requires_tech} 科技后，
 * 可由 {@code WorkTypeRegistry.availableFor(player, hold)} 放行。无需改 Java 即可重排整条解锁曲线。
 */
public record JobCharterDef(Identifier id, String workType, int minLevel, List<Identifier> requiresTech, int unlockCost) {

    public static final Codec<JobCharterDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("work_type", "").forGetter(JobCharterDef::workType),
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(JobCharterDef::minLevel),
            Identifier.CODEC.listOf().optionalFieldOf("requires_tech", List.of()).forGetter(JobCharterDef::requiresTech),
            Codec.INT.optionalFieldOf("unlock_cost", 0).forGetter(JobCharterDef::unlockCost)
    ).apply(instance, (workType, minLevel, requiresTech, unlockCost) ->
            new JobCharterDef(null, workType, minLevel, requiresTech, unlockCost)));

    /**
     * 网络同步编解码（含 id，使客户端获得与服务端一致的授权表）。
     * 与 {@link #CODEC} 不同：这里把 id 也编进流——JSON 里 id 由文件名注入，故 {@code CODEC} 不含 id；
     * 但经 {@code SetSync} 下发的同类定义（任务 / 科技 / 研究）都以 {@code Codec} 形态作为 {@code NETWORK_CODEC}
     * 字段并含 id，此处保持一致，便于 {@code ByteBufCodecs.fromCodec(...listOf())} 复用。
     */
    public static final Codec<JobCharterDef> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(JobCharterDef::id),
            Codec.STRING.optionalFieldOf("work_type", "").forGetter(JobCharterDef::workType),
            Codec.INT.optionalFieldOf("min_level", 1).forGetter(JobCharterDef::minLevel),
            Identifier.CODEC.listOf().optionalFieldOf("requires_tech", List.of()).forGetter(JobCharterDef::requiresTech),
            Codec.INT.optionalFieldOf("unlock_cost", 0).forGetter(JobCharterDef::unlockCost)
    ).apply(instance, JobCharterDef::new));

    /** 载入时由文件名注入 id（JSON 不含 id 字段）。 */
    public JobCharterDef withId(Identifier id) {
        return new JobCharterDef(id, workType, minLevel, requiresTech, unlockCost);
    }

    /** 最简工厂。 */
    public static JobCharterDef of(String workType, int minLevel, List<Identifier> requiresTech) {
        return new JobCharterDef(null, workType, minLevel, requiresTech, 0);
    }
}
