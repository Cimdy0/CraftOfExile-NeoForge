package com.cimdy.coe.data.gen.provider;

import com.cimdy.coe.data.gen.stat.effect.StatEffectIds;
import com.cimdy.coe.event.data.LoadEvent;
import com.cimdy.coe.data.gen.source.CoeDataPaths;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.base.StatEffectTypes;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
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
 * Stat effect 子条目落盘生成器：把 {@link StatEffectGen#contents()} 的每一个<b>具名</b>
 * effect 写入 {@code stat/effect/&lt;id&gt;.json}。
 *
 * <p>身份一律取生成期具名常量（{@code StatEffectIds.EFF_*}），<b>不做内容哈希</b>——
 * 文件名即可读 id，便于直接定位与 diff。此前「内容寻址」（按内容算哈希、再写出独立 JSON
 * 供引用来回跳转）的产物已整体移除，本类即其具名化替身。
 *
 * <p>运行时由 {@code LoadEvent} 按 {@code stat/effect} 目录扫描加载，
 * 供 {@code Stat} 条目以 {@code {"entry":"read","ref":"coe:…"}} 引用。
 */
public class StatEffectProvider implements DataProvider {
    private final PackOutput output;

    public StatEffectProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // datagen 期预填具名 effect 到 STAT_EFFECT_MAP：与运行期 LoadEvent 扫描 stat/effect JSON
        // 行为一致，使下游 StatEntryProvider 经 Stat.effect() 解析具名 ref 时能命中，避免预扫抛
        // "Stat 引用了未注册的 effect"。内联 effect 已由 StatEntry.internEffect 在构建条目时登记，
        // 此处仅补登记具名项（id 命名空间不重叠，直接 put）。
        for (Map.Entry<Identifier, StatEffect> e : StatEffectGen.contents().entrySet()) {
            LoadEvent.STAT_EFFECT_MAP.put(e.getKey(), e.getValue());
        }
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("effect");
        return saveAll(cache, dir, StatEffectGen.contents());
    }

    private static CompletableFuture<?> saveAll(CachedOutput cache, Path dir,
                                                Map<Identifier, ? extends StatEffect> effects) {
        // 落盘前先校验每个具名 effect 都能被 StatEffectTypes 反查编码，否则运行时解码会把
        // 未注册的 type 当成 UNKNOWN_TYPE，且在「子部件外置」形态下要等到读取时才炸。
        List<String> unregistered = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatEffect> e : effects.entrySet()) {
            if (!StatEffectTypes.isRegistered(e.getValue().type())) {
                unregistered.add(e.getKey() + " -> " + e.getValue().type());
            }
        }
        if (!unregistered.isEmpty()) {
            throw new IllegalStateException("unregistered stat effect types (missing register(...) in StatEffectTypes):\n  "
                    + String.join("\n  ", unregistered));
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Identifier, ? extends StatEffect> e :
                new LinkedHashMap<>(effects).entrySet()) {
            futures.add(DataProvider.saveStable(cache, StatEffectTypes.MAP_CODEC.codec(), e.getValue(),
                    dir.resolve(e.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Effects";
    }
}
