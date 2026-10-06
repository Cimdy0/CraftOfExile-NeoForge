package com.cimdy.coe.data.codec.stronghold.task.site.basic;

import com.cimdy.coe.data.codec.stronghold.task.site.BasicSite;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.*;
import com.cimdy.coe.data.codec.support.value.RangeAB;
import com.cimdy.coe.client.InputState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;

public abstract class AreaSite extends BasicSite {
    protected Map<String, Optional<BlockPos>> workerPositions;
    protected Optional<BlockPos> posA;
    protected Optional<BlockPos> posB;

    protected AreaSite(
            BlockPos workPos, int maxWorkers, boolean autoAssign,
            int priority, Map<String, Optional<BlockPos>> entityPositions,
            Optional<BlockPos> posA, Optional<BlockPos> posB) {
        super(workPos, maxWorkers, autoAssign, priority);
        this.workerPositions = entityPositions;
        this.posA = posA;
        this.posB = posB;
    }

    @Override
    public InteractionResult getInteractionHint(Level level, BlockPos targetPos) {
        InteractionResult result = super.getInteractionHint(level, targetPos);
        if (result.isChanged()) return result;
        BlockPos a = getPosA().orElse(null);
        BlockPos b = getPosB().orElse(null);
        if (a == null) {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.set_point_a"));
        } else if (targetPos.equals(a)) {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.clear_point_a"));
        } else {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.move_point_a"));
        }
        if (b == null) {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.set_point_b"), ActionKey.Modifier.SHIFT);
        } else if (targetPos.equals(b)) {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.clear_point_b"), ActionKey.Modifier.SHIFT);
        } else {
            result.putHint(InputState.INTERACT_3,
                    Component.translatable("hold.site.coe.move_point_b"), ActionKey.Modifier.SHIFT);
        }
        return result;
    }

    @Override
    public InteractionResult applyAction(Level level, BlockPos lookingPos, KeyMapping key, boolean shiftKeyDown) {
        InteractionResult result = super.applyAction(level, lookingPos, key, shiftKeyDown);
        if (result.isChanged()) return result;
        BlockPos oldA = getPosA().orElse(null);
        BlockPos oldB = getPosB().orElse(null);
        if (key == InputState.INTERACT_3 && !shiftKeyDown) {
            if (oldA == null) {
                SiteBindingFeedback.onPointASet(lookingPos);
                oldA = lookingPos;
            } else if (lookingPos.equals(oldA)){
                SiteBindingFeedback.onPointARemoved(lookingPos);
                oldA = null;
            } else {
                SiteBindingFeedback.onPointAMoved(oldA, lookingPos);
                oldA = lookingPos;
            }
            result.setAction(ActionResult.TOGGLE_AREA_POS);
        } else if (key == InputState.INTERACT_3) {
            if (oldB == null) {
                SiteBindingFeedback.onPointBSet(lookingPos);
                oldB = lookingPos;
            } else if (lookingPos.equals(oldB)) {
                SiteBindingFeedback.onPointBRemoved(lookingPos);
                oldB = null;
            } else {
                SiteBindingFeedback.onPointBMoved(oldB, lookingPos);
                oldB = lookingPos;
            }
            result.setAction(ActionResult.TOGGLE_AREA_POS);
        }
        if (result.isChanged()) withWorkArea(oldA, oldB);

        return result.setSite(this);
    }

    @Override
    public List<RenderEntry> getRenderEntries(Level level) {
        List<RenderEntry> entries = super.getRenderEntries(level);
        getWorkArea().ifPresent(area -> {
            AABB aabb = area.toAABB();
            entries.add(new RenderEntry.AabbEntry(aabb, getWorkAreaColor()));
        });
        if (getPosA().isPresent()) entries.add(new RenderEntry.BlockEntry(getPosA().get(), getPosAColor()));
        if (getPosB().isPresent()) entries.add(new RenderEntry.BlockEntry(getPosB().get(), getPosBColor()));
        return entries;
    }

    protected void withWorkArea(BlockPos a, BlockPos b) {
        this.posA = Optional.ofNullable(a);
        this.posB = Optional.ofNullable(b);
    }

    /** 添加一个实体 (无工作位置) */
    public void withAssignedEntity(UUID entityId) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.put(entityId.toString(), Optional.empty());
        setWorkerPositions(newWorkerPositions);
    }

    /** 移除一个实体 */
    public void withoutEntity(UUID entityId) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.remove(entityId.toString());
        setWorkerPositions(newWorkerPositions);
    }

    /** 设置或清除某实体的工作位置 */
    public void withEntityWorkPosition(UUID entityId, Optional<BlockPos> pos) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.put(entityId.toString(), pos);
        setWorkerPositions(newWorkerPositions);
    }

    /** 设置某实体的具体工作位置 */
    public void withEntityWorkPosition(UUID entityId, BlockPos pos) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.put(entityId.toString(), Optional.of(pos));
        setWorkerPositions(newWorkerPositions);
    }

    /** 清除某实体的工作位置 (保留实体) */
    public void clearEntityWorkPosition(UUID entityId) {
        withEntityWorkPosition(entityId, Optional.empty());
    }

    /** 获取某实体的当前工作位置 */
    public Optional<BlockPos> getEntityWorkPosition(UUID entityId) {
        return workerPositions.getOrDefault(entityId.toString(), Optional.empty());
    }

    /** 检查某位置是否已被任何实体占用 */
    public boolean isPositionOccupied(BlockPos pos) {
        return workerPositions.values().stream().anyMatch(opt -> opt.isPresent() && opt.get().equals(pos));
    }

    public void setWorkerPositions(Map<String, Optional<BlockPos>> workerPositions) {
        this.workerPositions = workerPositions;
    }

    public int getAssignedWorkers() {
        return this.workerPositions.size();
    }
    @Override public void addWorker(UUID uuid) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.put(uuid.toString(), Optional.empty());
        setWorkerPositions(newWorkerPositions);
    }
    @Override public void removeWorker(UUID uuid) {
        Map<String, Optional<BlockPos>> newWorkerPositions = new HashMap<>(workerPositions);
        newWorkerPositions.remove(uuid.toString());
        setWorkerPositions(newWorkerPositions);
    }
    @Override public List<UUID> getWorkers() {
        return workerPositions.keySet().stream().map(UUID::fromString).toList();
    }

    public int getWorkAreaColor() {
        return 0xFFFFFF00;
    }
    public int getPosAColor() {
        return 0xFFFF0000;
    }
    public int getPosBColor() {
        return 0xFF00FFFF;
    }
    public Optional<BlockPos> getPosA() {
        return posA;
    }
    public Optional<BlockPos> getPosB() {
        return posB;
    }
    @Override public Optional<RangeAB> getWorkArea() {
        return RangeAB.of(posA, posB);
    }
    public Map<String, Optional<BlockPos>> getWorkerPositions() {
        return workerPositions;
    }

    protected static <T extends AreaSite> RecordCodecBuilder<T, Optional<BlockPos>> posACodec() {
        return BlockPos.CODEC.optionalFieldOf("pos_a").forGetter(T::getPosA);
    }
    protected static <T extends AreaSite> RecordCodecBuilder<T, Optional<BlockPos>> posBCodec() {
        return BlockPos.CODEC.optionalFieldOf("pos_b").forGetter(T::getPosB);
    }
    protected static <T extends AreaSite> RecordCodecBuilder<T, Map<String, Optional<BlockPos>>> workerPositionsCodec() {
        return Codec.unboundedMap(Codec.STRING, BlockPos.CODEC.optionalFieldOf("pos").codec())
                .fieldOf("entity").forGetter(T::getWorkerPositions);
    }
    protected static <T extends AreaSite> StreamCodec<ByteBuf, Optional<BlockPos>> posStreamCodec() {
        return ByteBufCodecs.optional(BlockPos.STREAM_CODEC);
    }

    protected static <T extends AreaSite> StreamCodec<ByteBuf, Map<String, Optional<BlockPos>>> workPositionsStreamCodec() {
        return ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.optional(BlockPos.STREAM_CODEC));
    }
}