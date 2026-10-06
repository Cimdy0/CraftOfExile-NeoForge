package com.cimdy.coe.data.codec.stronghold.task.helper.state;

import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.ActiveTask;
import com.cimdy.coe.data.codec.stronghold.task.helper.result.TransferResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 据点运行时态（方案 E）。
 * <p>
 * 高频变化、每 tick 可能被修改的状态：供需匹配结果、待处理任务、活跃任务。
 * 与 {@link StrongholdConfigState}（低频配置态）分离，便于未来实现「内存优先 + 增量同步」，
 * 减少每 tick 全量序列化开销。当前序列化仍由 {@link StrongholdState} 统一扁平承载，保持兼容。
 */
public record StrongholdRuntimeState(
        TransferResult transferResult,
        List<ActiveTask> pendingTasks,
        Map<String, List<ActiveTask>> activeTasks
) {
    public static final StrongholdRuntimeState EMPTY = new StrongholdRuntimeState(TransferResult.EMPTY, List.of(), Map.of());

    public static final Codec<StrongholdRuntimeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TransferResult.CODEC.fieldOf("match_result").forGetter(StrongholdRuntimeState::transferResult),
            ActiveTask.CODEC.listOf().fieldOf("active_tasks").forGetter(StrongholdRuntimeState::pendingTasks),
            Codec.unboundedMap(Codec.STRING, ActiveTask.CODEC.listOf())
                    .fieldOf("active_tasks_by_entity").forGetter(StrongholdRuntimeState::activeTasks)
    ).apply(instance, StrongholdRuntimeState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrongholdRuntimeState> STREAM_CODEC = StreamCodec.composite(
            TransferResult.STREAM_CODEC, StrongholdRuntimeState::transferResult,
            ActiveTask.STREAM_CODEC.apply(ByteBufCodecs.list()), StrongholdRuntimeState::pendingTasks,
            ByteBufCodecs.map(HashMap::new,
                    ByteBufCodecs.STRING_UTF8, ActiveTask.STREAM_CODEC.apply(ByteBufCodecs.list())),
            StrongholdRuntimeState::activeTasks,
            StrongholdRuntimeState::new
    );
}
