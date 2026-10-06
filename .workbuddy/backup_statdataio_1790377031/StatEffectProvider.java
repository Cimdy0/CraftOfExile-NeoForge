package com.cimdy.coe.data.gen;

import com.cimdy.coe.data.codec.support.stat.effect.StatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffectRead;
import com.cimdy.coe.data.codec.support.stat.effect.StatEffectTypes;
import com.cimdy.coe.data.gen.stat.StatEffectGen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stat effect 独立数据生成器（CODEC 工厂：内容由 {@link StatEffectGen} 构造为真实 StatEffect 对象，
 * 本类经 {@link StatEffectTypes#CODEC} 编码落盘）。
 *
 * <p>每个唯一 effect 子条目经 {@code StatEffectIds} 持有内容寻址路径，内容（真实 StatEffect 对象）由
 * {@link StatEffectGen#contents()} 提供，本类逐条写入 {@code coe/stat/effect/&lt;name&gt;.json}。
 * 运行时由 {@code LoadEvent#STAT_EFFECT_MAP} 按 id 加载缓存，供 {@code StatEffectRead} 解析。
 */
public class StatEffectProvider implements DataProvider {
    private final PackOutput output;

    public StatEffectProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path dir = CoeDataPaths.coeRoot(output).resolve("stat").resolve("effect");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        // 落盘前一次性自检：effect 实例的 type() 必须在 StatEffectTypes 注册，否则codecFor 返回 UNKNOWN_TYPE，
        // 编码期抛「cannot encode unknown stat effect type」并中断整个 runData。此处把全部漏网之鱼
        // 一次列清，免得每跑一次 rundata 才炸一个。
        List<String> missing = new ArrayList<>();
        for (Map.Entry<Identifier, StatEffect> e : StatEffectGen.contents().entrySet()) {
            if (!StatEffectTypes.isRegistered(e.getValue().type())) missing.add(e.getKey() + " -> " + e.getValue().type());
        }
        for (Map.Entry<Identifier, StatEffect> e : StatEffectRead.all().entrySet()) {
            if (!StatEffectTypes.isRegistered(e.getValue().type())) missing.add(e.getKey() + " -> " + e.getValue().type());
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("以下 effect 未注册类型（须在 StatEffectTypes 补 register）: " + missing);
        }
        // 注册表 id 命名：WiringDsl.self / StatGen.stat / StatBuilder 以 EFF_* 注册表 id 引用 effect，
        // 须按同一注册表 id（e.getKey()）落盘，否则其 effect ref 悬空。缝#29 根因：原 seam#28 误用
        // nameId/partName 命名 effect 文件，致 821 条 self() 条目 effect ref 悬空、运行期
        // StatEffectRead.resolve 抛「未注册的效果」。
        for (Map.Entry<Identifier, StatEffect> e : StatEffectGen.contents().entrySet()) {
            futures.add(StatDataIO.save(cache, dir, e.getKey().getPath(), StatEffectTypes.CODEC, e.getValue()));
        }
        // 内容寻址（partName）命名：StatEffectRead.of 引用的动态 effect（registerModifier / StatGen.read
        // 内联 ModifierStatEffect 等），其条目 effect ref 即 partName，须按 StatEffectRead.all() 落盘以匹配。
        for (Map.Entry<Identifier, StatEffect> e : StatEffectRead.all().entrySet()) {
            futures.add(StatDataIO.save(cache, dir, e.getKey().getPath(), StatEffectTypes.CODEC, e.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Stat Effects";
    }
}
