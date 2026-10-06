package com.cimdy.coe.data.helper.hub;

import com.cimdy.coe.CraftOfExile;
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
 * 数据包装义资源加载器 —— 在资源重载时扫描 {@code data/<ns>/resource/*.json}，
 * 解析为 {@link ResourceDescriptor} 并注册为 {@link ResourceEntry}。
 * 使数据包可零 Java 改动即新增资源（经 {@link ResourceSource} 绑定既有数据源，由 {@link BoundResourcePool} 透传读写）。
 */
@EventBusSubscriber(modid = CraftOfExile.MODID)
public final class ResourceDataLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier KEY = Identifier.parse(CraftOfExile.MODID + "/resource_descriptor");
    private static final String ROOT = "resource";

    @SubscribeEvent
    public static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        // 资源描述符（data/<ns>/resource/*.json）经本监听器注册为 ResourceEntry；
        // KEY 仅用于重载排序/查重，与数据包路径无关。
        event.addListener(KEY, (ResourceManagerReloadListener) ResourceDataLoader::load);
    }

    /** 扫描 data/<ns>/resource/*.json，解析 ResourceDescriptor 并注册为 ResourceEntry。 */
    private static void load(ResourceManager rm) {
        Map<Identifier, Resource> resources = rm.listResources(ROOT, rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> e : resources.entrySet()) {
            try (InputStreamReader r = new InputStreamReader(e.getValue().open())) {
                ResourceDescriptor.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(r))
                        .result().ifPresent(d -> ResourceRegistry.register(d.toEntry()));
            } catch (Exception ex) {
                LOGGER.warn("Failed to load resource descriptor {}", e.getKey(), ex);
            }
        }
    }
}
