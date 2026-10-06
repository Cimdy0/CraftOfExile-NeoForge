package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.stat.duration.StatDurationGen;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.support.stat.duration.StatDuration;
import com.cimdy.coe.data.codec.support.stat.duration.StatDurationTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stat duration 子条目落盘生成器：把 {@link StatDurationGen#contents()} 的每一个<b>具名</b>
 * 时长定义写入 {@code stat/duration/<id>.json}。
 *
 * <p>完全镜像 {@link StatEffectProvider} / {@link StatTargetProvider} / {@link StatConditionProvider}：
 * 身份取具名常量（{@code StatDurationIds.DUR_* / INT_*}），不做内容哈希；运行时由
 * {@code LoadEvent} 按 {@code stat/duration} 目录扫描加载，供 {@code Stat} 条目以
 * {@code "duration": "read:coe:<id>"} 引用——但<b>当前工程内没有任何条目以此形态引用</b>，
 * 落盘的具名时长属预留扩展点（只写不读）。
 *
 * <p><b>本段确实是 dispatch 型</b>（与 {@code StatDurationTypes} 的注册表口径一致，不是单record）：
 * {@link StatDurationTypes#CODEC} 是 {@code Codec.STRING.dispatchMap("type", StatDuration::type, …)}
 * 派生，落盘 JSON 形如 {@code {"type":"fixed","kind":"duration","ticks":80}}。
 * 注意<b>不是</b> {@link StatDuration#CODEC}——后者是条目层 {@code duration} 字段用的
 * <b>扁平字符串</b> codec（{@code "duration:120"}），与独立落盘单元的形态不同，不可混用。
 */
public class StatDurationProvider implements DataProvider {
    private final PackOutput output;

    public StatDurationProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // datagen 期把具名 duration 预填进 STAT_DURATION_MAP，使本会话内的运行期读点
        // （StatDurationRead#resolved()）若引用到这些 id 也能命中，与运行期 LoadEvent 扫描
        // stat/duration JSON 的结果一致；id 命名空间不重叠于内联形态，直接 put。
        // 注意：条目编码期并不查这张表——落盘走 StatDuration.flatten()（StatDurationRead 覆写
        // 成 "read:<id>"，不查 map），且 StatEntryProvider.findUnregisteredEffects 会
        // instanceof StatRead 跳过别名条目，data/gen/ 下零处读 STAT_DURATION_MAP。
        // 当前无任何条目以 read: 引用这些 id，故此预填属预留、对现有产出无影响。
        for (Map.Entry<Identifier, StatDuration> e : StatDurationGen.contents().entrySet()) {
            LoadEvent.STAT_DURATION_MAP.put(e.getKey(), e.getValue());
        }
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("duration");
        return saveAll(cache, dir, StatDurationGen.contents());
    }

    private static CompletableFuture<?> saveAll(CachedOutput cache, Path dir,
                                                Map<Identifier, ? extends StatDuration> durations) {
        // 落盘前先校验每个具名 duration 的 type 都能被 StatDurationTypes 反查编码，否则运行时解码会把
        // 未注册的 type 当成 UNKNOWN_TYPE（StatDurationTypes.UNKNOWN_TYPE 的 encode/decode 恒失败），
        // 且在「子部件外置」形态下要等到读取时才炸。
        List<String> unregistered = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatDuration> e : durations.entrySet()) {
            if (!StatDurationTypes.isRegistered(e.getValue().type())) {
                unregistered.add(e.getKey() + " -> " + e.getValue().type());
            }
        }
        if (!unregistered.isEmpty()) {
            throw new IllegalStateException("unregistered stat duration types (missing register(...) in StatDurationTypes):\n  "
                    + String.join("\n  ", unregistered));
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatDuration> e :
                new LinkedHashMap<>(durations).entrySet()) {
            futures.add(DataProvider.saveStable(cache, StatDurationTypes.CODEC, e.getValue(),
                    dir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Durations";
    }
}