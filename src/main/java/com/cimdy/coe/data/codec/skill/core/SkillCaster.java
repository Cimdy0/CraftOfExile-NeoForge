package com.cimdy.coe.data.codec.skill.core;

import com.cimdy.coe.data.codec.skill.context.SkillStatScopes;
import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.skill.support.SupportProfile;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 施法者抽象 —— 让「技能」不再只服务于玩家，而是面向任意 {@link LivingEntity}。
 *
 * <p>设计目标：把「玩家按键冷却 / 按键输入」与「实体使用技能」解耦，并使
 * <b>玩家与其他实体共用同一份冷却数据附件与同一套冷却运算</b>。
 *
 * <p>冷却统一存放于任意实体通用的 {@link AttachmentRegistry#SKILL_COOLDOWN}
 * （{@code Map<能力槽, 剩余tick>}），由 {@link #tickAll(MinecraftServer)} 每 tick 统一推进；
 * 玩家不再使用独立的 {@code SkillSave} 冷却字段，二者走完全相同的存储与计算路径。
 * 玩家的「按键槽 -> 装配槽」绑定（{@code SkillSave}）仅作 UI 装配用途，与冷却解耦。
 *
 * <p>任意实体（含玩家）都通过 {@link #of(LivingEntity)} 取得同一个实现：
 * <ul>
 *   <li>冷却读写经 {@code SKILL_COOLDOWN}（玩家与实体同一附件）；</li>
 *   <li>每 tick 冷却递减由 {@link #tickAll(MinecraftServer)} 统一负责（玩家与实体同一运算）。</li>
 * </ul>
 *
 * <p>外部代码（怪物 AI、指令、其他模组）只需调用
 * {@link #cast(LivingEntity, Identifier, int, int, double, SupportProfile, boolean)}
 * 即可让任意实体施放指定技能，无需关心玩家按键 / 装配界面。
 *
 * <p>注：运行时本身的 {@link SkillInstance} / {@link SkillContext} 早已以 {@code LivingEntity} 为施法者，
 * 故本接口主要补齐「统一冷却存储」与「统一施放入口」两块玩家耦合点。
 */
public interface SkillCaster {
    /** 持有冷却且需要每 tick 推进的实体集合（弱引用，自动丢弃已销毁实体）。 */
    WeakHashMap<LivingEntity, Boolean> REGISTRY = new WeakHashMap<>();

    LivingEntity entity();

    /** 指定能力槽（bind）的剩余冷却 tick。 */
    int getCooldown(int bind);

    /** 设置指定能力槽（bind）的冷却 tick（<=0 表示清除）。 */
    void setCooldown(int bind, int ticks);

    /** 当前是否可发起一次新施放（默认：该槽冷却已归零）。 */
    default boolean canCast(int bind) {
        return getCooldown(bind) <= 0;
    }

    /** 每 tick 推进本实体的全部技能冷却。 */
    void tickCooldowns();

    /** 取实体对应的施法者实现（玩家与其他实体共用同一实现）。 */
    static SkillCaster of(LivingEntity entity) {
        return new Impl(entity);
    }

    /**
     * 以本施法者身份施放一个已解析的技能。
     *
     * @param skill   已解析的技能定义（含辅助宝石修正）
     * @param bind    能力槽索引（玩家对应按键槽；实体为自定义能力槽）
     * @param level   技能等级（<=0 视为 1）
     * @param quality 品质
     * @param profile 辅助宝石聚合效果（null 视为 EMPTY）
     * @param hold    是否持续按住（true=不自动松开，适用于光环/持续引导/被触发技能；
     *                false=非玩家实体在首 tick 后自动松开，使一次性技能正常结算并结束）
     * @return 新创建并已进入活动技能列表的实例；施法条件不满足或技能不存在时返回 null
     */
    default SkillInstance cast(Skill skill, int bind, int level, double quality,
                               SupportProfile profile, boolean hold, Identifier skillId) {
        if (skill == null) return null;
        SkillContext sc = new SkillContext(entity(), bind)
                .skillLevel(Math.max(1, level))
                .quality(quality)
                .supportProfile(profile == null ? SupportProfile.EMPTY : profile)
                // 技能参数槽注入：使技能数据里的 $name 槽位（来自 Skill.params / SkillRead.vars）可求值
                .variables(skill.effectiveParams());
        // 非玩家施法者默认单次激活（首 tick 后自动松开）；玩家始终由外部输入驱动
        sc.autoRelease(!hold && !(entity() instanceof Player));
        // 进入本次施放的作用域：使 canCast / 初始 run 期间读取的带标签天赋即时生效
        try (var scope = SkillStatScopes.enter(sc, skill, skillId)) {
            sc.skillLevel(effectiveSkillLevel(entity(), skill, sc, skillId, level));
            if (!skill.canCast(sc)) return null;
            SkillInstance inst = new SkillInstance(entity(), skill, sc, skillId);
            skill.runAnimation(sc);
            SkillEvent.activeSkills.add(inst);
            return inst;
        }
    }

    /**
     * 实体天生技能（词缀赋予 / 召唤物技能）的施放入口 —— 供怪物 AI 等非玩家施法者调用。
     *
     * <p>与 {@link #cast} 的两点差别：
     * <ol>
     *   <li><b>跳过宝石佩戴需求</b>：走 {@code Skill#canCastIgnoringRequire}，实体不持有技能石，
     *       不受等级 / 属性需求门控；装备需求与资源消耗仍然生效。</li>
     *   <li><b>预置 pressTime</b>：非玩家施法者由 autoRelease 在首 tick 松开按键，而技能须
     *       {@code pressTime >= castTime} 才结算 —— 不预置时 pressTime 从 0 起算且首 tick 被重置为 1，
     *       任何 {@code castTime > 1} 的技能都永远够不到阈值（技能阶段 / 伤害 / 动画全不可达，即死线）。
     *       此处把 pressTime 预置为 castTime，使技能在 {@code SkillInstance} 构造当帧即完成阶段初始化，
     *       随后 autoRelease 正常收尾，实例自然结束。</li>
     * </ol>
     *
     * <p>冷却仍走统一通道 {@link AttachmentRegistry#SKILL_COOLDOWN}：调用方应在返回非 null 后
     * 用 {@link #setCooldown} 设定施法节奏（技能自身冷却为 0 时由 AI 兜底）。
     */
    default SkillInstance castGranted(Skill skill, int bind, int level, double quality,
                                      SupportProfile profile, Identifier skillId) {
        if (skill == null) return null;
        if (!canCast(bind)) return null;
        SkillContext sc = new SkillContext(entity(), bind)
                .skillLevel(Math.max(1, level))
                .quality(quality)
                .supportProfile(profile == null ? SupportProfile.EMPTY : profile)
                .variables(skill.effectiveParams());
        sc.autoRelease(!(entity() instanceof Player));
        try (var scope = SkillStatScopes.enter(sc, skill, skillId)) {
            sc.skillLevel(effectiveSkillLevel(entity(), skill, sc, skillId, level));
            sc.pressTime(skill.getCastTime(sc));
            if (!skill.canCastIgnoringRequire(sc)) return null;
            SkillInstance inst = new SkillInstance(entity(), skill, sc, skillId);
            skill.runAnimation(sc);
            SkillEvent.activeSkills.add(inst);
            return inst;
        }
    }

    /**
     * 等效技能等级 = 基础等级 + 「+X to Level of all [Element] Skills」加成。
     *
     * <p>落点：{@code SKILL_LEVEL_BONUS}（零基，ADD_VALUE；按 {chaos}/{cold} 作用域折叠）。
     * 二者由 {@link com.cimdy.coe.data.gen.stat.effect.StatEffectGen} 以 {@code {chaos}} / {@code {cold}}
     * 标签作用域注册，故必须在<b>含元素标签</b>的作用域内读取——非本元素技能读得 0，不会串加。
     * 元素标签只在动作级作用域发射，而等级须在 {@code canCast} 前定稿，故在此用
     * 「技能标签 ∪ 元素标签」的一次性作用域求值（不并入整段施放作用域，避免污染其他读取）。
     */
    private static int effectiveSkillLevel(LivingEntity caster, Skill skill, SkillContext sc,
                                           Identifier skillId, int level) {
        int base = Math.max(1, level);
        if (caster == null || skill == null) return base;
        Set<String> tags = new HashSet<>(skill.getStatTags(sc, skillId));
        tags.addAll(skill.elementStatTags());
        double bonus;
        AutoCloseable scope = StatScopes.enter(caster, StatContext.of(tags, skillId));
        try {
            bonus = caster.getAttributeValue(AttributeRegistry.SKILL_LEVEL_BONUS);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
        return Math.max(1, base + (int) Math.round(bonus));
    }

    /**
     * 公共施放入口：让任意实体按技能 id 施放技能（解析技能定义 + 套用冷却 + 入活动列表）。
     * 供怪物 AI / 指令 / 其他模组调用，使「技能为任何实体提供」落地。
     */
    static SkillInstance cast(LivingEntity caster, Identifier skillId, int bind,
                              int level, double quality, SupportProfile profile, boolean hold) {
        if (caster == null || skillId == null) return null;
        SkillEntry entry = LoadEvent.getSkill(skillId);
        if (entry == null) entry = LoadEvent.getSpirit(skillId);
        if (entry == null) return null;
        return of(caster).cast(entry.resolve(), bind, level, quality, profile, hold, skillId);
    }

    /** 每 tick 推进所有已登记实体的技能冷却（玩家与任意实体统一于此）。 */
    static void tickAll(MinecraftServer server) {
        if (server == null) return;
        for (LivingEntity entity : new ArrayList<>(REGISTRY.keySet())) {
            if (entity == null || !entity.isAlive()
                    || entity.level() == null || entity.level().isClientSide()) {
                REGISTRY.remove(entity);
                continue;
            }
            new Impl(entity).tickCooldowns();
            if (entity.getData(AttachmentRegistry.SKILL_COOLDOWN).isEmpty()) {
                REGISTRY.remove(entity);
            }
        }
    }

    /**
     * 统一实现：冷却存于 {@link AttachmentRegistry#SKILL_COOLDOWN}（{@code Map<能力槽, 剩余tick>}），
     * 玩家与任意非玩家实体共用同一附件、同一运算。
     */
    final class Impl implements SkillCaster {
        private final LivingEntity entity;

        Impl(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public LivingEntity entity() {
            return entity;
        }

        @Override
        public int getCooldown(int bind) {
            return entity.getData(AttachmentRegistry.SKILL_COOLDOWN).getOrDefault(bind, 0);
        }

        @Override
        public void setCooldown(int bind, int ticks) {
            Map<Integer, Integer> map = new HashMap<>(entity.getData(AttachmentRegistry.SKILL_COOLDOWN));
            if (ticks > 0) {
                map.put(bind, ticks);
                REGISTRY.put(entity, Boolean.TRUE);
            } else {
                map.remove(bind);
            }
            entity.setData(AttachmentRegistry.SKILL_COOLDOWN, map);
        }

        @Override
        public void tickCooldowns() {
            Map<Integer, Integer> map = entity.getData(AttachmentRegistry.SKILL_COOLDOWN);
            if (map.isEmpty()) return;
            Map<Integer, Integer> next = new HashMap<>();
            for (Map.Entry<Integer, Integer> e : map.entrySet()) {
                int v = e.getValue() - 1;
                if (v > 0) next.put(e.getKey(), v);
            }
            entity.setData(AttachmentRegistry.SKILL_COOLDOWN, next);
        }
    }
}
