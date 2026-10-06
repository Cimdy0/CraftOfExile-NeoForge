package com.cimdy.coe.event.skill;

import com.cimdy.coe.data.codec.skill.phase.delivery.impl.area.AreaDelivery;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.ReservationChangeEvent;
import com.cimdy.coe.data.codec.skill.phase.event.trigger.SkillCastEvent;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;

import com.cimdy.coe.data.codec.skill.helper.EmpoweredAttacks;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.LingeringAreaTracker;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.PersistentDeliveryManager;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.status.runtime.StatusEngine;

import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.helper.engine.InvocationTicker;
import com.cimdy.coe.network.input.KeyPressPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class SkillEvent {
    //活动 技能实例
    public static final List<SkillInstance> activeSkills = new CopyOnWriteArrayList<>();
    // 缓存伤害前后文
    public static Map<Pair<UUID, UUID>, DamageContext> cacheDamageContext = new ConcurrentHashMap<>();

    // 缓存按键释放
    public static List<UUID> cacheKeyPress = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    public static void SkillCastEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 不再自动绑定技能：技能槽初始化为空，由玩家在技能装配界面自由装配。
            // 给予基础灵魂值池，使灵性宝石的保留（reservation）机制可用
            if (ResourceRegistry.getSpirit(player) <= 0) {
                ResourceRegistry.setSpirit(player, 100f);
            }
        }
    }

    @SubscribeEvent
    public static void onSkillEvent(ServerTickEvent.Post event) {
        List<SkillInstance> toRemove = new CopyOnWriteArrayList<>();
        new CopyOnWriteArrayList<>(activeSkills).forEach(activeSkill -> {
            activeSkills.remove(activeSkill);
            activeSkills.add(activeSkill.tick());

            if (activeSkill.isStop()) {
                toRemove.add(activeSkill);
            }
        });
        activeSkills.removeAll(toRemove); // 批量移除所有空技能实例
        for (SkillInstance finished : toRemove) finished.teardown(); // 释放保留 + 发布 ReservationChangeEvent

        // 推进常驻投递（光束 / 光环）：每个维度分别 tick
        for (ServerLevel level : event.getServer().getAllLevels()) {
            PersistentDeliveryManager.tickAll(level);
            // 持续区域（AreaDelivery Lingering 模式）：到脉冲刻重新扫描并点燃 ON_HIT
            LingeringAreaTracker.tick(level);
        }

        // 精魂光环常驻：自动激活装配的精魂宝石并占用灵魂值保留池
        SpiritManager.tickAll(event.getServer());

        // 调用技能周期触发：持「调用技能改为每 X 秒触发链接法术」标记的玩家，其元技能按间隔自动触发链接法术
        InvocationTicker.tickAll(event.getServer());

        // 统一推进所有实体（玩家 + 任意非玩家实体）的技能冷却（见 SkillCaster.tickAll）
        SkillCaster.tickAll(event.getServer());

        cacheKeyPress.clear();
    }

}
