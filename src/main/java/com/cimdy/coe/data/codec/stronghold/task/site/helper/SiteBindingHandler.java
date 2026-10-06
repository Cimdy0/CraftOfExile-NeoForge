package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import com.cimdy.coe.client.render.HighlightRenderer;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.client.InputState;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import com.cimdy.coe.network.stronghold.BindSite;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SiteBindingHandler {
    @Nullable private static Site cachedSite;
    private static final Set<Object> currentSiteHighlightKeys = new HashSet<>();

    public static boolean handleSite(Level level, BlockPos lookingPos, StrongholdHelper.HoldRef ref) {
        boolean isChange = false;
        if (SiteTypes.isSiteBlock(level, lookingPos)) {
            boolean isLookSelected = cachedSite != null && lookingPos.equals(cachedSite.getSitePos());
            Site lookingSite = ref.hold().holdState().getSite(lookingPos).orElse(null);
            Site newSite = null;
            if (lookingSite == null) { // bind
                newSite = SiteTypes.createSiteForBlock(level, lookingPos);
                if (newSite != null) {
                    isChange = true;
                    ClientPacketDistributor.sendToServer(BindSite.addOrUpdate(ref.pos(), newSite));
                    SiteBindingFeedback.onWorkBlockBound(InputState.getCachedPos(), newSite.getName());
                }
            } else if (!isLookSelected) { // switch
                isChange = true;
                newSite = lookingSite;
                SiteBindingFeedback.onWorkBlockSwitch(InputState.getCachedPos(), newSite.getName());
            }
            if (isChange) SiteBindingHandler.setCachedSite(newSite);
        }
        return isChange;
    }

    public static void handleInteraction(Player player, BlockPos storage, StrongholdHelper.HoldRef ref, KeyMapping key) {
        Level level = player.level();
        if (cachedSite == null) return;
        InteractionResult result = cachedSite.applyAction(level, storage, key, player.isShiftKeyDown());
        Site newSite = result.getSite();
        if (newSite == null) {
            ClientPacketDistributor.sendToServer(BindSite.remove(ref.pos(), cachedSite));
            cachedSite = null;
        } else if (result.isChanged()) {
            cachedSite = newSite;
            ClientPacketDistributor.sendToServer(BindSite.addOrUpdate(ref.pos(), cachedSite));
        }
    }

    public static void renderSite(Level level) {
        Set<Object> newKeys = new HashSet<>();
        Site site = SiteBindingHandler.cachedSite;
        if (site != null) {
            for (RenderEntry entry : site.getRenderEntries(level)) {
                Object key = switch (entry) {
                    case RenderEntry.BlockEntry b -> b.pos();
                    case RenderEntry.AabbEntry a -> a.aabb();
                };
                newKeys.add(key);
                // 如果该条目尚未高亮，则添加（无限持续）
                switch (entry) {
                    case RenderEntry.BlockEntry b -> HighlightRenderer.addHighlightedPosition(b.pos(), b.color(), -1);
                    case RenderEntry.AabbEntry a -> HighlightRenderer.addHighlightedAABB(a.aabb(), a.color(), -1);
                }
            }
        }
        Iterator<Object> it = currentSiteHighlightKeys.iterator();
        while (it.hasNext()) {
            Object oldKey = it.next();
            if (!newKeys.contains(oldKey)) {
                if (oldKey instanceof BlockPos pos) {
                    HighlightRenderer.removeHighlightedPosition(pos);
                } else if (oldKey instanceof AABB aabb) {
                    HighlightRenderer.removeHighlightedAABB(aabb);
                }
                it.remove();
            }
        }
        currentSiteHighlightKeys.clear();
        currentSiteHighlightKeys.addAll(newKeys);
    }

    @Nullable
    public static Site getCachedSite() { return cachedSite; }

    public static void setCachedSite(@Nullable Site cachedSite) {
        SiteBindingHandler.cachedSite = cachedSite;
    }

    public static void clearSelection() {
        cachedSite = null;
    }
}