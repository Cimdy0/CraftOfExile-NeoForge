package com.cimdy.coe.registry;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TicketTypeRegistry {
    public static DeferredRegister<TicketType> TICKET_TYPE =
            DeferredRegister.create(Registries.TICKET_TYPE, CraftOfExile.MODID);

    public static Holder<TicketType> TICK = TICKET_TYPE.register("tick", () -> new TicketType(
            20L, TicketType.FLAG_KEEP_DIMENSION_ACTIVE, true));
}
