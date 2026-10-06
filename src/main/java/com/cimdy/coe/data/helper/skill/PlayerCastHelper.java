package com.cimdy.coe.data.helper.skill;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.helper.cast.SkillCast;
import com.cimdy.coe.data.helper.combat.DamageHelper;

import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.skill.core.Skill;
import com.cimdy.coe.data.codec.skill.core.SkillCaster;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.KeyBind;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;

import com.cimdy.coe.data.codec.skill.helper.EmpoweredAttacks;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.network.input.KeyPressPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 玩家按键施放的完整解析链（按键 → 装配槽 → 技能定义 → 作用域 → 施放 + 施放副作用）。
 *
 * <p>原内联于 {@code event.SkillEvent#playerCast} 及三个 {@code try*} 生产者：
 * 按键解析与「施放时授予 X」属于技能机制，不是事件派发；按铁律 #56 迁出，
 * {@code SkillEvent} 仅保留 {@code @SubscribeEvent} 监听与活动实例表。
 */
public final class PlayerCastHelper {

    private PlayerCastHelper() {
    }

    /**
     * 战吼负面施加半径（格）：镜像 {@code MarkHelper.applyMarkToNearby} 的邻近敌方扫描尺度。
     * 仅作 AoE 范围常量，非属性/数值；与 PoE2「战吼影响周围敌人」语义一致。
     */
    private static final double WARCRY_DEBUFF_RADIUS = 8.0;

    /** 玩家按键施放：解析按键绑定的装配槽 → 技能定义 → 校验需求 → 入活动技能列表。 */
    public static void playerCast(Player player, KeyPressPacket keyPress) {
        if (player instanceof ServerPlayer) {
            int key = keyPress.key();
            if (player.hasData(AttachmentRegistry.SKILL_SAVE)) {
                if (key >= 0) {
                    KeyBind.SkillSave save = player.getData(AttachmentRegistry.SKILL_SAVE);
                    KeyBind.EntitySkill keyData = save.getData(key);
                    SkillAssembly assembly = KeyBind.getMergedAssembly(player);
                    int asmIndex = keyData.assemblySlot().getInt();
                    SkillData gem = (asmIndex >= 0 && asmIndex < SkillAssembly.SIZE)
                            ? assembly.get(asmIndex) : SkillData.EMPTY;
                    // 精魂宝石为常驻光环，由 SpiritManager 按装配界面的开关激活，不绑定按键
                    if (!gem.isEmpty() && gem.isSpirit()) {
                        player.sendOverlayMessage(Component.translatable("msg.coe.spirit_aura_enable"));
                        return;
                    }
                    if (keyData.hasBinding() && !gem.isEmpty() && SkillCaster.of(player).canCast(key)) {
                        Optional<SkillInstance> optionalInstance = SkillEvent.activeSkills.stream().filter(i ->
                                i.getContext().getCaster().getUUID().equals(player.getUUID())
                                        && i.getContext().isPress()).findFirst();
                        if (optionalInstance.isEmpty()) {
                            SkillContext context = new SkillContext(player, key);
                            // 使用 SkillData 统一解析技能效果与辅助宝石（getSkill() 已把辅助宝石的
                            // 额外属性需求并入 require，见 SkillLinkResolver.mergeRequire）
                            Skill skill = gem.getSkill();
                            if (skill == null) {
                                player.sendOverlayMessage(Component.translatable("msg.coe.skill_data_missing"));
                                return;
                            }
                            context.skillLevel(gem.getEffectiveSkillLevel());
                            context.quality(gem.getEffectiveQuality());
                            context.supportProfile(gem.buildSupportProfile());
                            // 进入本次施放的作用域：使 canCast / 初始 run 期间读取的带标签天赋即时生效
                            try (var scope = SkillStatScopes.enter(context, skill, gem.id())) {
                                // 需求校验前置（skill.require() 已含辅助宝石追加需求）：区分「属性不足」与其它失败原因
                                if (!skill.require().matches(context)) {
                                    player.sendOverlayMessage(Component.translatable("msg.coe.attr_requirement"));
                                } else if (skill.canCast(context)) {
                                    SkillInstance instance = new SkillInstance(player, skill, context, gem.id());
                                    // 傀儡师(puppet master)：使用 [Command] 技能时按几率获得层数（作用域内读取 puppet_master_chance）
                                    tryGainPuppetMaster(player, skill, context, gem.id());
                                    tryGrantWarcry(player, skill, context, gem.id());
                                    tryConsumeCorpse(player, skill, context, gem.id());
                                    // CAST_SKILL 事件发射点：全工程统一的「技能成功施放」判定点，
                                    // 覆盖法术/攻击/战吼/召唤等所有技能。计数队列写入身份标签 skill_<id>
                                    // （仅进计数队列、不进 fire 作用域），供 distinct 模式按技能类型去重，
                                    // 对应语料「each different Spell/Attack you've Cast/Used」。
                                    // 与 USE_WARCRY（仅战吼、由 tryGrantWarcry 发射）互补不重叠。
                                    // 第三参数由空标签集改为技能 stat 标签（含 "spell"/"attack"/"warcry" 等），
                                    // 使带标签计数队列（A2 通道）可承载「recently cast a Spell」类条件——R3 安全：
                                    // spell 标签经 SkillCast 真实写入，EXACT 匹配可真实触发。
                                    com.cimdy.coe.event.Event.record(player, EventName.CAST_SKILL,
                                            skill.getStatTags(context, gem.id()),
                                            GameEvent.distinctSubjectTag("skill", gem.id().toString()));
                                    // 「使用混沌技能时损失最大生命与能量护盾的 X%」(node 52191)：
                                    // 仅对元素标签含 chaos 的技能生效；按标记幅度同比例直扣当前生命与能量护盾，
                                    // 不经承伤管线（避免把损失当作“受击”触发恢复/护盾吸收等副作用）。
                                    // 语料把该损失归一为负值（-0.03），故取绝对值后按 3% 扣减。
                                    if (skill.elementStatTags().contains(GateTag.CHAOS)) {
                                        double loss = Math.abs(StatEngine.magnitudeOf(player,
                                                "chaos_skill_life_es_loss", Set.of()));
                                        if (loss > 0.0) {
                                            ResourceEventHelper.onSpend(player, GateTag.HEALTH, (float) (player.getMaxHealth() * loss), "skill", null);
                                            ResourceEventHelper.onSpend(player, GateTag.ENERGY_SHIELD, (float) (ResourceRegistry.getMaxEnergyShield(player) * loss), "skill", null);
                                        }
                                    }
                                    skill.runAnimation(context);
                                    SkillEvent.activeSkills.add(instance);
                                } else {
                                    player.sendOverlayMessage(Component.translatable("msg.coe.cast_condition"));
                                }
                            }
                        }
                    } else {
                        // 区分失败原因，避免未绑定 / 空槽也误报「冷却中」
                        if (!keyData.hasBinding()) {
                            player.sendOverlayMessage(Component.translatable("msg.coe.key_unbound"));
                        } else if (gem.isEmpty()) {
                            player.sendOverlayMessage(Component.translatable("msg.coe.slot_empty"));
                        } else {
                            player.sendOverlayMessage(Component.translatable("msg.coe.on_cooldown"));
                        }
                    }
                } else {
                    SkillEvent.cacheKeyPress.add(player.getUUID());
                }
            }
        }
    }

    /**
     * 傀儡师(puppet master)触发点：玩家使用 [Command] 技能时，经 {@code StatEngine.fire} 走
     * GrantOnEventStatEffect 通道（StatGen 中 {@code surpassing_chance_to_gain_a_puppet_master_stack...}
     * 条目）按 {@code puppet_master_chance} 概率叠加一层 Puppet Master（可叠层 buff，
     * 上限由 STATUS_MAX_STACKS{puppet_master} 控制）。授予时长由条目内 {@code inc_puppet_master_duration}
     * 在基础时间属性上缩放（400 × (1 + 修饰符)），缩放属性保留。须在技能施放作用域
     * （SkillStatScopes.enter）内 fire 以读取带标签天赋。
     */
    private static void tryGainPuppetMaster(Player player, Skill skill, SkillContext context, Identifier skillId) {
        if (!(player instanceof ServerPlayer)) return;
        Set<String> statTags = skill.getStatTags(context, skillId);
        if (!statTags.contains("command")) return;
        Set<String> fireTags = new HashSet<>(statTags);
        fireTags.add("puppet_master");
        // 指挥技能走 fire 通道授予 Puppet Master：GrantOnEventStatEffect 按 tags [command, puppet_master] 命中，
        // 与本方法的事件名无关。此处不写 Event 计数队列——全仓（java + json，含 src/generated）无任何
        // recently:/count:/distinct:/time_command 消费者，故 "command" 永不被 EventSubscription 订阅
        // （无 EventHistoryValue / TimerValue 引用，也不在机制白名单），Event.record 会被门控静默丢弃。
        // 若日后需要「近期施放过指挥技能」计数语义，须先补一条引用该键的 stat 条目/条件使其被订阅，再回补 record。
        StatEngine.fire(player, player, StatContext.of(fireTags, skillId), null);
    }

    /**
     * 战吼强化(Warcry Empower)生产者：玩家施放 [warcry] 类技能时，授予接下来 N 次「强化攻击」。
     * 在技能施放作用域（SkillStatScopes.enter）内调用，故 {warcry} 标签天赋即时生效：
     * 基础 1 次 + add_empowered_attacks(roaring_cries/vocal_empowerment 平加) + warcry 强度加成
     * （inc_warcry_power 经 WARCRY 作用域缩放 AILMENT_MAGNITUDE，每 +10% 折算 +1 次）。
     * 被强化的攻击在 {@code DamageHelper.applyDamageMultipliers} 携带 empowered 标签，使 inc_empowered_damage 生效。
     */
    private static void tryGrantWarcry(Player player, Skill skill, SkillContext context, Identifier skillId) {
        if (!(player instanceof ServerPlayer)) return;
        Set<String> statTags = skill.getStatTags(context, skillId);
        if (!statTags.contains("warcry")) return;
        // USE_WARCRY 事件发射点：本方法是全工程唯一的「战吼类技能施放」判定点
        // （USE_WARCRY 若只有声明、无任何调用点，则既有的
        //  RECOVER_OF_MAX_HEALTH_AND_MANA_WHEN_YOU_USE_A_WARCRY（EFF_HEAL_HEALTH_USE_WARCRY_WARCRY）
        //  恒不触发），故此处补上计数队列记录，使「使用战吼时/近期使用过战吼」类条目可读。
        // 额外写入「技能身份标签」skill_<id>（仅进计数队列，不进 fire 作用域），
        // 供 distinct 模式去重统计「使用过几种不同战吼」（每个 warcry 技能 = 1 个身份），
        // 对应语料「for each different Warcry you've used」。前缀 skill_ 与 victim_ 等身份空间隔离。
        com.cimdy.coe.event.Event.record(player, EventName.USE_WARCRY, Set.of(),
                GameEvent.distinctSubjectTag("skill", skillId.toString()));
        // R3 修复 #65：USE_WARCRY 此前只经 Event.record 写入计数队列（仅喂 recently/count 条件），
        // 不触发任何 GrantOnEventStatEffect / ResourceStatEffect（二者须 StatEngine.fire）。补真实发射点。
        // 分两通道，避免 self 效果被每敌人重复触发：
        //   · use_warcry       —— 自身通道：供「使用战吼时回复生命/魔力」即时结算（self 目标，fire 一次即可）；
        //   · use_warcry_enemy —— 敌方通道：对每个附近敌方实体 fire，供「战吼施加点燃/痿顿/致命虚弱」等
        //                         GrantOnEventStatEffect.STATUS（取 evalCtx.triggerTarget()=敌方，self 包装无关）。
        StatEngine.fire(player, player, StatContext.of(Set.of("use_warcry", "warcry"), skillId, EventName.USE_WARCRY, null), null);
        Camp ownerCamp = player.getData(AttachmentRegistry.CAMP);
        if (ownerCamp != Camp.NONE) {
            Level level = player.level();
            // R3 死线修复（#48 同型）：战吼负面半径此前硬编码 WARCRY_DEBUFF_RADIUS=8.0，从不读
            // WARCRY_SKILLS_HAVE_INC_AREA_OF_EFFECT（→ EFFECT_AREA{warcry}，ADD_MULTIPLIED_BASE）。
            // 镜像 AreaDeliveryExecutor.readEffectArea：在「当前作用域 ∪ 缺省{AREA,MELEE,MINION}」内读 EFFECT_AREA 倍率缩放半径。
            double warcryRadius = WARCRY_DEBUFF_RADIUS;
            Set<String> areaTags = new HashSet<>(StatScopes.current(player).tags());
            areaTags.add(GateTag.AREA);
            areaTags.add(GateTag.MELEE);
            areaTags.add(GateTag.MINION);
            try (var scope = StatScopes.enter(player, StatContext.of(areaTags, null))) {
                warcryRadius = WARCRY_DEBUFF_RADIUS * player.getAttributeValue(AttributeRegistry.EFFECT_AREA);
            } catch (Exception ignored) {}
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(warcryRadius))) {
                if (e == player) continue;
                Camp c = e.getData(AttachmentRegistry.CAMP);
                if (c != Camp.NONE && c != ownerCamp) {
                    StatEngine.fire(player, e, StatContext.of(Set.of("use_warcry_enemy", "warcry"), skillId, EventName.USE_WARCRY, null), null);
                }
            }
        }
        double extra = StatEngine.foldDelta(player, AttributeRegistry.ADD_EMPOWERED_ATTACKS);
        double mag = player.getAttributeValue(AttributeRegistry.AILMENT_MAGNITUDE); // 当前 warcry 作用域内读 WARCRY 标签
        int powerExtra = (int) Math.round(Math.max(0.0, mag - 1.0) * 10.0);
        double minPower = player.getAttributeValue(AttributeRegistry.WARCRY_MIN_POWER);
        int floor = (int) Math.round(minPower);
        int total = Math.max(floor, 1 + (int) Math.round(extra) + powerExtra);
        if (total > 0) EmpoweredAttacks.grant(player, total);
    }

    /**
     * 尸体消耗(Corpse Consume)生产者：玩家施放 [corpse] 类技能（如 raise_zombie/consume_corpse）时，
     * 消耗 1 具尸体并授予「近期消耗尸体」窗口增益（详见 {@link ResourceRegistry#consumeAndEmpowerCorpse}）。
     * 在技能施放作用域内调用。消耗失败（无尸体）时静默跳过——不阻断技能本身（最小可用）。
     */
    private static void tryConsumeCorpse(Player player, Skill skill, SkillContext context, Identifier skillId) {
        if (!(player instanceof ServerPlayer)) return;
        Set<String> statTags = skill.getStatTags(context, skillId);
        if (!statTags.contains("corpse")) return;
        ResourceRegistry.consumeAndEmpowerCorpse(player, 1);
    }
}
