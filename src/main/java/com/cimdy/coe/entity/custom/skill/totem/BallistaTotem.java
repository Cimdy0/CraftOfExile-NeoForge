package com.cimdy.coe.entity.custom.skill.totem;

import com.cimdy.coe.data.helper.combat.HitScopeHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Ballista entity. Reuses all SkillTotem stationary-caster behavior (no overrides needed);
 * exists only as a distinct entity type so HitScopeHelper can emit the ballista scope tag by
 * entity identity, bringing the 5 ballista-specific stat effects live without misfiring on
 * ordinary totems (which get TOTEM but not BALLISTA).
 */
public class BallistaTotem extends SkillTotem {
    public BallistaTotem(EntityType<? extends BallistaTotem> type, Level level) {
        super(type, level);
    }
}
