package com.cimdy.coe.helper;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.network.input.KeyPressPacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;

public class EntityHelper {
    public static boolean hasEffect(Iterable<MobEffectInstance> allEffects, Holder<MobEffect> mobEffect) {
        for (MobEffectInstance effectInstance : allEffects) {
            if (effectInstance.getEffect().is(mobEffect)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUseItemSkill(Entity entity) {
        return entity.getData(AttachmentRegistry.KEY_PRESS).key() >= 0;
    }

    public static int getUseSkillUseTime(Entity entity) {
        int i = 0;
        for (SkillInstance skillInstance : SkillEvent.activeSkills) {
            if (skillInstance.getContext().getCaster().getUUID().equals(entity.getUUID())
                    && skillInstance.getContext().isPress()) {
                return skillInstance.getSkillCast();
            }
        }
        return i;
    }

    public static boolean isJustReleasedSkillKey(Entity entity) {
        KeyPressPacket press = entity.getData(AttachmentRegistry.KEY_PRESS);
        return press.key() >= 0 || press.released();
    }

    /** 实体当前是否正在使用指定技能（遍历 activeSkills，按 caster 匹配；可有多个并行技能）。 */
    public static boolean isUsingSkill(Entity entity, Identifier skillId) {
        for (SkillInstance skillInstance : SkillEvent.activeSkills) {
            if (skillInstance.getContext().getCaster().getUUID().equals(entity.getUUID())
                    && skillInstance.getSkillId().equals(skillId)) {
                return true;
            }
        }
        return false;
    }
}
