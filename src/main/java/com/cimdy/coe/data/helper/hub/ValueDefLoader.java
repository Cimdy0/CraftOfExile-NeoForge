package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.ValueLib;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * 命名值数据加载器（值模型统一内核 · 数据生成入口）：扫描 {@code data/<ns>/value_defs/*.json}，
 * 每个文件解析为一个 {@link ValueExpr}，以<b>省略固定分类前缀的叶名键</b>注册进 {@link ValueLib}，
 * 供 {@code ref:<id>} 随意引用。
 * <p>键约定（用户 2026-10-06 裁定）：缓存键必须是 {@code modid:xxx}（仅叶名），<b>不得</b>
 * {@code modid:coe/xxx/.../xxx}（带数据生成的固定分类前缀）。本加载器扫描根即 {@code value_defs/}，
 * 该前缀是加载器已知项，故键省略之；键不会重复（按类别隔离）也不应重复，{@code coe:fire_explosion}
 * 足以表达，无需 {@code coe:value_defs/fire_explosion}。</p>
 * <p>不做内容寻址——键由作者显式给定（即文件名），不哈希表达式体；重载时先清盘再逐条灌入，同键后注册覆盖
 * （数据包覆盖语义）。命名值由数据包写入缓存、运行期读回，全链路以 Identifier 表达路径。</p>
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class ValueDefLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier KEY = Identifier.parse(CraftOfExile.MODID + "/value_defs");
    private static final String ROOT = "value_defs";

    @SubscribeEvent
    public static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(KEY, (ResourceManagerReloadListener) ValueDefLoader::load);
    }

    /** 扫描 data/<ns>/value_defs/*.json，解析为 ValueExpr 并以省略分类前缀的叶名键灌入 ValueLib。 */
    private static void load(ResourceManager rm) {
        ValueLib.clear();
        Map<Identifier, Resource> resources = rm.listResources(ROOT, rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> e : resources.entrySet()) {
            try (InputStreamReader r = new InputStreamReader(e.getValue().open())) {
                ValueExpr.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(r))
                        .result().ifPresent(vexpr -> ValueLib.register(stripJson(e.getKey()), vexpr));
            } catch (Exception ex) {
                LOGGER.warn("Failed to load value def {}", e.getKey(), ex);
            }
        }
    }

    /**
     * 键 = 文件真实路径去掉<b>固定分类前缀（ROOT=value_defs/）</b>与 .json 后缀，
     * 如 {@code data/coe/value_defs/fire_explosion.json → coe:fire_explosion}。
     * 分类前缀由本加载器已知（扫描根即 value_defs），缓存键省略之，符合「modid:xxx」键约定。
     */
    private static Identifier stripJson(Identifier id) {
        String p = id.getPath();
        String prefix = ROOT + "/";
        if (p.startsWith(prefix)) p = p.substring(prefix.length());
        if (p.endsWith(".json")) p = p.substring(0, p.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(id.getNamespace(), p);
    }
}
