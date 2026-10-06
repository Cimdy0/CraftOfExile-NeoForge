package com.cimdy.coe.event.data;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.Optional;

/**
 * {@link BootstrapContext} 的捕获适配器：把原注册表引导期的 {@code register(...)} 调用记录进一个
 * {@link Map}<{@link Identifier}, T>，使既有的 {@code registerXxx(BootstrapContext<T>)} 生成器无需改写即可
 * 在 Provider 通道下复用。
 *
 * <p>关键：元素键 {@link ResourceKey#identifier()} 往往携带了注册表自身路径前缀（如 {@code item/gem/skill/red_fireball}），
 * 而 {@link CoeDataProvider} 已按 {@code <registryPath>} 建目录，故记录时须<b>剥离</b>该前缀，仅保留裸 id
 * （如 {@code red_fireball}），最终落盘为 {@code data/coe/<registryPath>/<bareId>.json}，与 LoadEvent 的
 * scanResources 前缀（同为 {@code registryPath}）1:1 对齐。若元素键不含该前缀（如稀有度 {@code common}），
 * 则原样保留，行为与注册表序列化一致。</p>
 *
 * <p>{@code lookup(...)} 委托给真实 {@link HolderLookup.Provider}（跨注册表引用，如标签 / 实体类型），
 * 与 BootstrapContext 原始语义一致（本适配器不被任何 helper 实际调用，仅为满足接口的抽象方法）。</p>
 *
 * <p>注：BootstrapContext.register 返回 {@link Holder.Reference}，但 helper 均忽略该返回值，故以占位 owner 生成即可。</p>
 */
public class CapturingBootstrap<T> implements BootstrapContext<T> {
    private final HolderLookup.Provider provider;
    private final String registryPath;
    private final Map<Identifier, T> out;

    @SuppressWarnings("rawtypes")
    private static final HolderOwner DUMMY_OWNER = new HolderOwner() {};

    public CapturingBootstrap(HolderLookup.Provider provider, ResourceKey<Registry<T>> registryKey, Map<Identifier, T> out) {
        this.provider = provider;
        this.registryPath = registryKey.identifier().getPath();
        this.out = out;
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> key, T value, Lifecycle lifecycle) {
        out.put(strip(key.identifier()), value);
        return Holder.Reference.createStandAlone(DUMMY_OWNER, key);
    }

    @Override
    public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
        return provider.lookup(key)
                .map(r -> (HolderGetter<S>) r)
                .orElseThrow(() -> new IllegalStateException("Registry unavailable in lookup provider: " + key.identifier()));
    }

    private Identifier strip(Identifier loc) {
        String path = loc.getPath();
        String prefix = registryPath + "/";
        String rel;
        if (path.startsWith(prefix)) {
            rel = path.substring(prefix.length());
        } else if (path.equals(registryPath)) {
            rel = "";
        } else {
            rel = path;
        }
        return Identifier.fromNamespaceAndPath(loc.getNamespace(), rel);
    }
}
