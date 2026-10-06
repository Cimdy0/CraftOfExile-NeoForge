package com.cimdy.coe.data.codec.stronghold.task.site;

import com.cimdy.coe.data.codec.stronghold.task.impl.work.enchant.ConvertType;
import com.cimdy.coe.data.codec.stronghold.task.helper.dispatch.TaskGenerator;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.InteractionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.RenderEntry;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Site {
    default Site getSite() { return this; }
    BlockPos getSitePos();
    Component getName();
    Component getDescription();
    int getPriority();
    boolean canSetAssign();
    boolean isAutoAssign();
    void setPriority(int priority);
    void setAutoAssign(boolean autoAssign);

    default boolean validate(ServerLevel level) { return true; }
    Predicate<BlockState> getBlockPredicate();

    /**
     * 该站点对应的原版配方类型（用于把站点接入「注册表驱动的通用合成管线」）。
     * 返回非 null 时，{@code TaskGenerator.generateCraftTasks} 会据此把站点存储坐标注册为
     * 该配方类型的生产点，从而让工人系统覆盖所有原版配方（合成/冶炼/烟熏/烘焙/切石/锻造等）。
     * 无对应配方的自定义站点（如堆肥/研磨）返回 null，由 {@code ConvertType} 另行处理。
     */
    default RecipeType<?> getRecipeType() { return null; }

    default boolean hasWorkArea() { return getWorkArea().isPresent(); }
    default Optional<RangeAB> getWorkArea() { return Optional.empty(); }
    default int getMaxStorage() { return 0; }
    default int getAssignedStorage() { return getStorages().size(); }
    default List<BlockPos> getStorages() { return List.of(); }
    default void setStorages(List<BlockPos> storages) {}

    int getMaxWorkers();
    void setMaxWorkers(int maxWorkers);
    default List<UUID> getWorkers() { return List.of(); }
    default int getAssignedWorker() { return getWorkers().size(); }
    default void addWorker(UUID uuid) {}
    default void removeWorker(UUID uuid) {}

    default InteractionResult applyAction(Level level, BlockPos lookingPos, KeyMapping key, boolean shiftKeyDown) {
        return new InteractionResult(this);
    }
    default InteractionResult getInteractionHint(Level level, BlockPos targetPos) { return new InteractionResult(null); }
    default List<RenderEntry> getRenderEntries(Level level) { return new ArrayList<>(); }

    static Site create(BlockPos pos) {
        return null;
    }
    StreamCodec<RegistryFriendlyByteBuf, ? extends Site> getStreamCodec();
    Type<?> getType();

    record Type<T extends Site>(
            Codec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
            Predicate<BlockState> blockPredicate,
            Function<BlockPos, T> factory
    ) {}

    Codec<Map<Type<?>, Site>> MAP_CODEC = Codec.dispatchedMap(
            SiteTypes.REGISTRY.byNameCodec(),
            Site.Type::codec);

    Codec<Site> CODEC = MAP_CODEC.flatXmap(
            map -> {
                if (map.size() != 1) {
                    return DataResult.error(() -> "Expected single site entry");
                }
                return DataResult.success(map.values().iterator().next());
            },
            site -> {
                Type<?> type = site.getType();
                return DataResult.success(ImmutableMap.of(type, site));
            }
    );

    StreamCodec<RegistryFriendlyByteBuf, Site> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Site decode(RegistryFriendlyByteBuf buf) {
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            StreamCodec<RegistryFriendlyByteBuf, ? extends Site> codec = SiteTypes.STREAM_CODECS.get(id);
            if (codec == null) {
                throw new IllegalArgumentException("Unknown site type: " + id);
            }
            return codec.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Site site) {
            Identifier id = SiteTypes.REGISTRY.getKey(site.getType());
            if (id == null) {
                throw new IllegalArgumentException("Unregistered site type: " + site.getType());
            }
            Identifier.STREAM_CODEC.encode(buf, id);
            @SuppressWarnings("unchecked")
            StreamCodec<RegistryFriendlyByteBuf, Site> codec =
                    (StreamCodec<RegistryFriendlyByteBuf, Site>) SiteTypes.STREAM_CODECS.get(id);
            codec.encode(buf, site);
        }
    };
}