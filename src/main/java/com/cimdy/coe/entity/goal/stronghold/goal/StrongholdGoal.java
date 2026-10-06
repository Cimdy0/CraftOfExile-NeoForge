package com.cimdy.coe.entity.goal.stronghold.goal;

import com.cimdy.coe.data.codec.stronghold.Stronghold;
import com.cimdy.coe.stronghold.energy.StrongholdHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StrongholdGoal extends Goal {
    protected final Mob mob;
    protected Stronghold hold;
    protected GlobalPos terminalPos;

    public StrongholdGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.hold = StrongholdHelper.getHoldByEntity(this.mob);
        this.terminalPos = StrongholdHelper.getEntityHoldPos(this.mob);
        return this.hold != null;
    }
}