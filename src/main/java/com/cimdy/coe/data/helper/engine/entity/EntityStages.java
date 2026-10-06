package com.cimdy.coe.data.helper.engine.entity;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.trigger.EntityStage;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.entity.EntityDefCarrier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>阶段式实体</b>（通用设施）。
 *
 * <p>PoE2 里「放置出来的东西会沿一条<b>有序阶段序列</b>逐步变化」是反复出现的维度：
 * 有毒藤蔓的长成、图腾的最终形态、召唤物的立即成熟、投射物的二次阶段 …
 * 本设施把它做成一条<b>与具体实体类别无关</b>的通用链路，由两枚 INERT 标记按
 * <b>阶段集身份标签</b>（{@link #stageSetTagOf}）作用域取值：
 * <ul>
 *   <li>{@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}：生成时按概率<b>直接跳到末阶段</b>
 *       （PoE2「植物有 X 概率立即长成」语义）；概率 1.0 = 100%，&gt;1 按 PoE2 拆为
 *       「保底次数 + 余概率」（与 {@link AdditionalSpawn#rollCount} 同口径）；</li>
 *   <li>{@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect}：每次推进到新阶段时，
 *       再按概率<b>立即多推进一阶段</b>（缩短当前阶段的驻留）。</li>
 * </ul>
 *
 * <h2>通用性保证（三条）</h2>
 * <ol>
 *   <li><b>序列来自数据</b>：谁有阶段、有几条、每条多久，全部由 {@link EntityDef#stages()} 声明；
 *       Java 侧不出现任何实体类别 / 阶段名字面量；</li>
 *   <li><b>身份来自标签</b>：标记匹配的 {@code conditionTags} 由 EFF 实例承载，查询集由
 *       {@link #stageSetTagOf} 单一来源产出（形态 tags + 形态 / 实体类型 path + 生成时刻的技能作用域标签），
 *       两侧不各写字面量；</li>
 *   <li><b>未声明阶段 = 零行为变化</b>：{@link #stagesOf} 为空时，掷骰 / 推进 / 跳末阶段一律 no-op。</li>
 * </ol>
 *
 * <h2>运行态存放</h2>
 * 阶段序号与阶段计时复用实体既有的 {@code getPersistentData()}（同 {@code SUMMON} 标记的既有做法），
 * 不新造一套存储；于是任何 {@link EntityDefCarrier} 载体（含不走 behavior tick 的投射物）都能参与，
 * 且跨存档重载自动还原。
 *
 * <h2>读点位置</h2>
 * <ul>
 *   <li>掷骰：实体被装配的<b>公共路径</b> {@code EntityDefCarrier#configure} —— 全部投递入口
 *       （Hazard / Remnant / Orb / Ball / Thrown / Projectile）共用，不在 6 个执行器里各写一遍；</li>
 *   <li>推进：既有的实体 tick 路径（{@code SkillEntity#tick}），不新起 ticker。</li>
 * </ul>
 */
public final class EntityStages {

    private EntityStages() {
    }

    /** 阶段序号持久化键。 */
    private static final String KEY_STAGE = "coe.stage";
    /** 本阶段剩余 tick 持久化键。 */
    private static final String KEY_TIMER = "coe.stage_timer";

    /**
     * 实体的阶段序列（{@link EntityDef#stages()}）。
     *
     * <p>未声明阶段 / 非形态载体 / 形态未装配 → 空列表，全部 API 随之 no-op。
     */
    public static List<EntityStage> stagesOf(Entity e) {
        EntityDef def = defOf(e);
        return def == null ? List.of() : def.stages();
    }

    /** 当前阶段下标（未声明阶段恒 0；越界值按末阶段收敛）。 */
    public static int stageIndexOf(Entity e) {
        List<EntityStage> stages = stagesOf(e);
        if (stages.isEmpty()) return 0;
        int raw = e == null ? 0 : e.getPersistentData().getInt(KEY_STAGE).orElse(0);
        return Math.max(0, Math.min(raw, stages.size() - 1));
    }

    /**
     * 推进到下一阶段。
     *
     * @return 是否真的发生了变化（已在末阶段 → false）
     */
    public static boolean advance(Entity e) {
        List<EntityStage> stages = stagesOf(e);
        if (stages.isEmpty() || e == null) return false;
        int idx = stageIndexOf(e);
        if (idx >= stages.size() - 1) return false;
        writeStage(e, idx + 1);
        return true;
    }

    /**
     * 直接跳到末阶段（PoE2「立即长成」）。
     *
     * @return 是否真的发生了变化（本来就在末阶段 → false）
     */
    public static boolean skipToFinal(Entity e) {
        List<EntityStage> stages = stagesOf(e);
        if (stages.isEmpty() || e == null) return false;
        int last = stages.size() - 1;
        if (stageIndexOf(e) >= last) return false;
        writeStage(e, last);
        return true;
    }

    /**
     * 阶段集身份：实体 → 供标记 {@code conditionTags} 匹配的查询集（单一来源）。
     *
     * <p>三类来源合并，<b>不新造概念</b>：
     * <ol>
     *   <li>形态自带标签（{@link EntityDef#tagSet()}，数据直接声明 {@code plant} / {@code hazard} …）；</li>
     *   <li>形态 id / 实体类型 key 的 path（{@code totem} / {@code orb} …，与 {@link EntityCategoryLimit#categoryOf} 同源）；</li>
     *   <li>生成时刻生成者的<b>技能作用域标签</b>（含技能自带的分类标签，随技能作用域自动带上，无需额外 plumbing）。</li>
     * </ol>
     */
    public static Set<String> stageSetTagOf(Entity e) {
        Set<String> tags = new LinkedHashSet<>();
        if (e == null) return tags;
        EntityDef def = defOf(e);
        if (def != null) {
            tags.addAll(def.tagSet());
            Identifier typePath = def.type();
            if (typePath != null) tags.add(typePath.getPath());
        }
        Identifier entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        if (entityKey != null) tags.add(entityKey.getPath());
        LivingEntity owner = EntityCategoryLimit.ownerOf(e);
        if (owner != null) tags.addAll(StatScopes.current(owner).tags());
        return tags;
    }

    /**
     * <b>生成时读点</b>：持有 {@link com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect} 时，
     * 按概率<b>直接以末阶段生成</b>（跳过整条阶段序列）。
     *
     * <p>由载体 {@code configure}（全部投递入口共用的装配路径）调用一次；重进存档走 {@code restoreDef}，不重掷。
     * 未声明阶段的实体 → 完全 no-op（一条 {@code put} 也不写）。
     */
    public static void applySpawnRoll(Entity self, @Nullable LivingEntity owner) {
        List<EntityStage> stages = stagesOf(self);
        if (stages.isEmpty() || owner == null || self == null) return;
        if (self.level().isClientSide()) return;
        writeStage(self, 0);
        writeTimer(self, stages.get(0).ticks());
        Set<String> tags = stageSetTagOf(self);
        int hits = AdditionalSpawn.rollCount(owner,
                "entity_stage_skip_chance", tags, null);
        if (hits > 0) skipToFinal(self);
    }

    /**
     * <b>每 tick 读点</b>：时间驱动的阶段推进 + 「概率提前推进一阶段」。
     *
     * <p>挂在既有的实体 tick 路径上（宿主 {@code SkillEntity#tick}），每 tick 调用；
     * 未声明阶段 / 已到末阶段 / 本阶段尚未走完 → no-op。
     */
    public static void tick(Entity self) {
        List<EntityStage> stages = stagesOf(self);
        if (stages.isEmpty() || self == null) return;
        if (self.level().isClientSide()) return;
        int idx = stageIndexOf(self);
        if (idx >= stages.size() - 1) return;
        int remaining = self.getPersistentData().getInt(KEY_TIMER).orElse(0);
        if (remaining > 0) {
            writeTimer(self, remaining - 1);
            return;
        }
        advance(self);
        LivingEntity owner = EntityCategoryLimit.ownerOf(self);
        if (owner != null && AdditionalSpawn.rollCount(owner,
                "entity_stage_advance_chance", stageSetTagOf(self), null) > 0) {
            advance(self);
        }
        writeTimer(self, stages.get(stageIndexOf(self)).ticks());
    }

    @Nullable
    private static EntityDef defOf(Entity e) {
        return e instanceof EntityDefCarrier carrier ? carrier.def() : null;
    }

    private static void writeStage(Entity e, int index) {
        e.getPersistentData().putInt(KEY_STAGE, index);
    }

    private static void writeTimer(Entity e, int ticks) {
        e.getPersistentData().putInt(KEY_TIMER, Math.max(0, ticks));
    }
}
