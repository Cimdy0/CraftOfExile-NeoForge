package com.cimdy.coe.network.stronghold;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BindSite(
            GlobalPos terminalPos, Site site, boolean remove
    ) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BindSite> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "bind_site"));

        public static final StreamCodec<RegistryFriendlyByteBuf, BindSite> STREAM_CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, BindSite::terminalPos,
                Site.STREAM_CODEC, BindSite::site,
                ByteBufCodecs.BOOL, BindSite::remove,
                BindSite::new
        );

        public static CustomPacketPayload addOrUpdate(GlobalPos terminalPos, Site site) {
            return new BindSite(terminalPos, site, false);
        }

        public static CustomPacketPayload remove(GlobalPos terminalPos, Site site) {
            return new BindSite(terminalPos, site, true);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
