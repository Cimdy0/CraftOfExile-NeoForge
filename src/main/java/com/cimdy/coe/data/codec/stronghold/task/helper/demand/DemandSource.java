package com.cimdy.coe.data.codec.stronghold.task.helper.demand;

import net.minecraft.core.GlobalPos;

public record DemandSource(DemandInfo demand, GlobalPos terminalPos) {}
