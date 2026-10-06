package com.cimdy.coe.data.codec.stronghold.task.site;

import com.cimdy.coe.data.codec.stronghold.task.site.helper.ActionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.InteractionResult;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.RenderEntry;
import com.cimdy.coe.data.codec.stronghold.task.site.helper.SiteBindingFeedback;
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

import java.util.ArrayList;
import java.util.List;

public abstract class BasicSite implements Site {
    protected final BlockPos sitePos;
    protected int maxWorkers;
    protected boolean autoAssign;
    protected int priority;

    protected BasicSite(BlockPos sitePos, int maxWorkers, boolean autoAssign, int priority) {
        this.sitePos = sitePos;
        this.maxWorkers = maxWorkers;
        this.autoAssign = autoAssign;
        this.priority = priority;
    }

    @Override
    public InteractionResult applyAction(Level level, BlockPos lookingPos, KeyMapping key, boolean shiftKeyDown) {
        InteractionResult result = Site.super.applyAction(level, lookingPos, key, shiftKeyDown);
        if (result.isChanged()) return result;
        if (key == InputState.INTERACT_1) {
            if (lookingPos.equals(sitePos)) {
                SiteBindingFeedback.onWorkBlockUnbound(sitePos, this.getName());
                result.setAction(ActionResult.TOGGLE_WORK_POS);
                return result.setSite(null); // 返回 null 表示删除站点
            }
        }
        return result.setSite(this);
    }

    @Override
    public InteractionResult getInteractionHint(Level level, BlockPos targetPos) {
        InteractionResult result = Site.super.getInteractionHint(level, targetPos);
        if (result.isChanged()) return result;
        if (sitePos.equals(targetPos)) {
            result.putHint(InputState.INTERACT_1, Component.translatable(
                    "hold.site.coe.unbind_site"));
            result.setAction(ActionResult.TOGGLE_WORK_POS);
        }
        return result;
    }

    @Override
    public List<RenderEntry> getRenderEntries(Level level) {
        List<RenderEntry> entries = new ArrayList<>();
        entries.add(new RenderEntry.BlockEntry(getSitePos(), getWorkBlockColor()));
        return entries;
    }

    public int getWorkBlockColor() {
        return 0xFF00AAFF;
    }
    @Override public int getPriority() {
        return priority;
    }
    @Override public BlockPos getSitePos() { return sitePos; }
    @Override public int getMaxWorkers() { return maxWorkers; }
    @Override public boolean isAutoAssign() {
        return autoAssign;
    }
    @Override public void setPriority(int priority) {
        this.priority = priority;
    }
    @Override public void setAutoAssign(boolean autoAssign) {
        this.autoAssign = autoAssign;
    }
    @Override public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    protected static <T extends BasicSite> RecordCodecBuilder<T, BlockPos> sitePosCodec() {
        return BlockPos.CODEC.fieldOf("site_pos").forGetter(T::getSitePos);
    }
    protected static <T extends BasicSite> RecordCodecBuilder<T, Integer> maxWorkersCodec() {
        return Codec.INT.fieldOf("max_workers").forGetter(T::getMaxWorkers);
    }
    protected static <T extends BasicSite> RecordCodecBuilder<T, Boolean> autoAssignCodec() {
        return Codec.BOOL.fieldOf("auto_assign").forGetter(T::isAutoAssign);
    }
    protected static <T extends BasicSite> RecordCodecBuilder<T, Integer> priorityCodec() {
        return Codec.INT.fieldOf("priority").forGetter(T::getPriority);
    }
    protected static StreamCodec<ByteBuf, BlockPos> sitePosStreamCodec() {
        return BlockPos.STREAM_CODEC;
    }
    protected static StreamCodec<ByteBuf, Integer> maxWorkersStreamCodec() {
        return ByteBufCodecs.INT;
    }
    protected static StreamCodec<ByteBuf, Boolean> autoAssignStreamCodec() {
        return ByteBufCodecs.BOOL;
    }
    protected static  StreamCodec<ByteBuf, Integer> priorityStreamCodec() {
        return ByteBufCodecs.INT;
    }
}
