package com.cimdy.coe.event.combat;

import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityRead;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.skill.context.SkillContext.HitContext;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.event.Event;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.TriggerContext;
import com.cimdy.coe.event.skill.SkillEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * 战斗事件「一体描述」收敛对象：把事件名 + 形态标签（近战/投射物/具体投射物种） + 异常分类标签
 * 统一在构造处算好，fold（承伤前读取）与 fire（造成后触发）两侧都从本对象取标签，不再手写拼接。
 *
 * <p><b>关键约束（用户裁定）：</b>
 * <ul>
 *     <li><b>主动/被动独立 Context</b>：本对象只描述事件，不持有 owner。
 *         调用方须分别把 {@code caused} 标签写入攻击者 owner 的 Context、
 *         把 {@code taken} 标签写入受害者 owner 的 Context，两者互不共享。</li>
 *     <li><b>形态维度全为标签</b>：{@code melee} / {@code projectile} / {@code proj:<id>}
 *         全部进 {@code Set<String>}，不另立字段/枚举；被动侧形态复用<b>同一批</b>单标签
 *         （无 taken_* 前缀）——故本类只保留<b>一份</b> {@code formTags}，
 *         攻击侧与被动侧共享，不再维护两份恒等副本。</li>
 *     <li><b>fold 与 fire 同样收敛</b>：形态标签在 fold（读取型作用域）与 fire（触发型 StatContext）两侧
 *         都从同一 {@code GameEvent} 取；且满足「fire 标签集 = 触发事件标签 ∪ fold 形态标签」。
 *         触发维（{@code caused_*}/{@code taken_*} 事件名）与形态维由标签集合天然区分，不串味。</li>
 *     <li><b>造成/承受完全对称</b>：{@link #fireCaused} 与 {@link #fireTaken} 仅 owner/other 方向与
 *         事件名前缀不同，标签构成、计时写入、附加标签通道完全一致。</li>
 * </ul>
 *
 * <p>用法范式（造成/承受对称闭环，一行一侧）：
 * <pre>{@code
 *   Identifier skill = SkillDamageSource.skillIdOf(source);
 *   GameEvent.ailmentCaused(source, direct, EventName.CAUSED_AILMENT, CHILL.id).fireCaused(attacker, victim, skill);
 *   GameEvent.ailmentTaken(source, direct, EventName.TAKEN_AILMENT, CHILL.id).fireTaken(victim, attacker, skill);
 * }</pre>
 */
public final class GameEvent {

    /** 触发事件名（{@code caused_*}/{@code taken_*}，fire 通道门控 + Context 计时键）。 */
    private final String eventName;
    /** 形态标签（{@code melee/projectile/proj:<id>}）：攻击侧与被动侧同源同值，fold 与 fire 共用。 */
    private final Set<String> formTags;
    /** 具体异常名标签（如 ailment id），可为空。分类标签（elemental/ailment/…）不再在此广播，
     *  由 GateTag.match 按标签层级汇总判定（发射端只发关键下位标签）。 */
    private final String ailmentId;
    /** 本次 fire 附带命中上下文（hit_state:* 节点用；非命中事件恒 null）。 */
    private HitContext hitContext;
    /**
     * 去重身份标签：传入计数队列的「对象身份」标签（如 {@code skill_<id>}），
     * 供 {@code distinct} 模式按技能类型去重。与 {@link #ailmentId} 机制对称：后者在造成侧异常事件里
     * 自动拼 {@code victim_<ailmentId>:<uuid>}，前者由调用方显式设置（如战吼的技能 id）。
     * 恒不进 fire 作用域标签集（避免污染 stat 触发匹配）。
     */
    private String extraSubjectTag;

    private GameEvent(String eventName, Set<String> formTags, String ailmentId) {
        this.eventName = eventName;
        this.formTags = formTags;
        this.ailmentId = ailmentId;
    }

    /** 附带命中上下文（使 {@code hit_state:*} 节点在本次 fire 的条件/数值求值中可用）；链式返回自身。 */
    public GameEvent withHit(HitContext hitContext) {
        this.hitContext = hitContext;
        return this;
    }

    /** 追加去重身份标签（如 {@code skill_<id>}，由调用方用 {@link #distinctSubjectTag} 构造）；链式返回自身。 */
    public GameEvent withSubject(String extraSubjectTag) {
        this.extraSubjectTag = extraSubjectTag;
        return this;
    }

    // ───────────────────────────── 工厂（收口事件名 + 形态推导） ─────────────────────────────

    /** 仅事件名 + 异常名（无伤害源形态推导，技能显式入口用）。 */
    public static GameEvent ofPlain(String eventName, @Nullable String ailmentId) {
        return new GameEvent(eventName, Set.of(), ailmentId);
    }

    /**
     * 造成型事件（攻击侧）：从伤害源推导形态标签，事件名 + 形态 一体构造。
     * @param eventName {@code EventName.CAUSED_*}
     * @param source    伤害源（推导形态 / 投射物）
     * @param direct    直接实体（投射物本体；近战为攻击者）
     */
    public static GameEvent caused(DamageSource source, Entity direct, String eventName) {
        return new GameEvent(eventName, formTagsOf(source, direct), null);
    }

    /** 造成型异常事件：在 {@link #caused} 基础上并入异常名（分类标签由 GateTag.match 汇总）。 */
    public static GameEvent ailmentCaused(DamageSource source, Entity direct, String eventName, String ailmentId) {
        return new GameEvent(eventName, formTagsOf(source, direct), ailmentId);
    }

    /** 承受型事件（被动侧）：镜像 {@link #caused}，事件名取 {@code taken_*}。 */
    public static GameEvent taken(DamageSource source, Entity direct, String eventName) {
        return new GameEvent(eventName, formTagsOf(source, direct), null);
    }

    /** 承受型异常事件：镜像 {@link #ailmentCaused}。 */
    public static GameEvent ailmentTaken(DamageSource source, Entity direct, String eventName, String ailmentId) {
        return new GameEvent(eventName, formTagsOf(source, direct), ailmentId);
    }

    // ───────────────────────────── 形态标签推导（fold / fire 共用，全为标签） ─────────────────────────────

    /** 形态标签：<b>载体自报优先</b> —— {@link com.cimdy.coe.entity.EntityDefCarrier} 的
     *  {@link EntityDef#tagSet()} 是形态的<b>真值源</b>（与 {@code EntityRead.statTags()} 同源，
     *  同一份数据两处取，形态标签天然自洽）；非载体才回退实体类判定
     *  （{@code melee} / {@code projectile} / {@code proj:<id>}）。
     *
     *  <p><b>为何不能靠 {@code instanceof} 猜形态</b>：技能在世界里放置出来的一切载体都是同一个
     *  {@code SkillEntity}（箭矢 / 手雷 / 电球 / 危害物 / 驻场道具…，靠 {@code EntityDef} 区分形态），
     *  它<b>不是</b> {@code Projectile}。按 {@code instanceof Projectile} 判定会把技能投射物统统打成
     *  {@code melee}，令「箭矢伤害提高」类词缀失效、「近战伤害提高」反而吃到箭矢伤害。
     *  载体自报即本项目铁律「一 Node 多视图 + 获取方声明所需类型」的落点。
     *
     *  <p>只发关键下位标签；{@code projectile} 作为父类别由 GateTag.match 汇总命中（含 arrow / 各 proj:*）。
     *  攻击侧与被动侧同源同值（按用户裁定，形态标签不另立「承受型」前缀）。 */
    public static Set<String> formTagsOf(DamageSource source, Entity direct) {
        Set<String> tags = new HashSet<>();
        // 载体自报：形态属于定义，不属于实体类。SkillEntity 一族全部走这条分支。
        if (direct instanceof EntityDefCarrier carrier && carrier.def() != null) {
            Set<String> form = carrier.def().tagSet();
            tags.addAll(form);
            // proj:<形态 id>：<b>仅当形态自称 projectile 才发</b>。
            // 否则驻场形态（coe:hazard / coe:caltrop / coe:orb…）会因 proj: 前缀通配
            // 被 {projectile} 词缀命中（手雷同理：它刻意不并入 projectile，见统一 ProjectileDelivery 的 THROWN 形态）。
            if (form.contains(GateTag.PROJECTILE)) {
                EntityRead read = carrier.entityRead();
                if (read != null) tags.add("proj:" + read.ref().getPath());
            }
            return tags;
        }
        // 非载体（玩家近战、原版箭矢与投掷物）：回退实体类判定。
        if (direct instanceof Projectile) {
            tags.add("projectile");
            tags.add("proj:" + direct.getType().toShortString());
        } else {
            tags.add("melee");
        }
        return tags;
    }

    // ───────────────────────────── 分维近期遭遇判定（F2-B） ─────────────────────────────

    /** 进入 recency 维度（taggedTriggers）的白名单：形态 + 元素/异常。不含 taken_hit / 技能 id / 武器 / delivery，避免污染。 */
    public static final Set<String> RECENCY_TAGS = Set.of(
            GateTag.MELEE, GateTag.PROJECTILE, "arrow",
            GateTag.PHYSICAL, GateTag.CHAOS, GateTag.FIRE, GateTag.COLD, GateTag.LIGHTNING, GateTag.ELEMENTAL);

    /** 由命中标签推导分维 recency 标签集：形态（formTagsOf）+ 元素白名单命中项。 */
    public static Set<String> recencyTagsOf(DamageSource source, Entity direct, Set<String> hitTags) {
        Set<String> out = new HashSet<>(formTagsOf(source, direct));
        for (String t : hitTags) if (RECENCY_TAGS.contains(t)) out.add(t);
        return out;
    }

    /** 记录一次命中（攻击/被攻击）：既写 triggers（count/recently 无标签查询、F3 依赖），也写 taggedTriggers（分维 recency）。 */
    public static void recordTaggedHit(LivingEntity owner, String key, Set<String> tags) {
        if (owner == null) return;
        Event.record(owner, key);
        Event.get(owner).record(key, tags);
    }

    // ───────────────────────────── 取标签（fold 与 fire 双通道） ─────────────────────────────

    /**
     * fold（承伤前读取型）作用域标签：仅形态标签，供进入 Stat 作用域即时求值
     * （如「被近战攻击时护甲+X」「对投射物伤害提高」）。
     * 不含触发事件名——fold 只查 Context 不写、不触发。
     */
    public Set<String> formTags() {
        return new HashSet<>(formTags);
    }

    /**
     * fire（造成后触发型）标签集——攻击者 owner 侧：
     * = 触发事件名 ∪ 形态标签 ∪ 异常名/造成型异常键。
     * 满足「fire 标签集 = 触发事件 ∪ fold 形态标签」，使 on-cause 可复合判定
     * （如「造成冰缓 + 对方被近战攻击」）。
     */
    public Set<String> causedTags() {
        return eventTags(EventName.CAUSED_AILMENT);
    }

    /**
     * fire（造成后触发型）标签集——受害者 owner 侧（on-taken）：
     * 与 {@link #causedTags()} 完全对称，仅异常键取 {@code taken_ailment}
     * （造成/承受方向本身已由 {@code eventName} 的 {@code caused_*}/{@code taken_*} 前缀表达）。
     */
    public Set<String> takenTags() {
        return eventTags(EventName.TAKEN_AILMENT);
    }

    // ───────────────────────────── 去重敌计数：受害者身份标签 ─────────────────────────────

    /**
     * 受害者身份标签前缀：{@code victim_<ailmentId>}。完整标签形如 {@code victim_ignite:<uuid>}。
     * <p>供 {@code EventHistoryValue.mode=distinct} 以 PREFIX 匹配聚合同一异常、以完整标签（含 UUID）去重，
     * 实现 PoE2「per Enemy you've Ignited Recently」的<b>去重敌计数</b>。
     * <p><b>为何不复用裸异常标签</b>：{@link #eventTags} 已写入裸 {@code ailmentId}（如 {@code ignite}），
     * 若身份标签也以 {@code ignite:} 开头，PREFIX 匹配会同时命中裸标签，而
     * {@code countTagged} 单次触发内只取首个匹配标签（HashSet 迭代序不定）→ 去重键退化为 {@code "ignite"}，
     * 计数恒为 1。故独立前缀 {@code victim_} 与裸标签空间彻底隔离。
     */
    public static String subjectTagPrefix(String ailmentId) {
        return "victim_" + ailmentId;
    }

    /**
     * 为计数队列附加受害者身份标签（造成侧异常事件专用）。
     * <p>门控三条同时满足才附加：① 本次带具体异常名 ② 有受害者实体 ③ 是<b>造成</b>方向
     * （标签集含 {@link EventName#CAUSED_AILMENT}——承受侧 {@code other} 是攻击者，语义不同故不写）。
     * 不满足时原样返回，零额外分配。
     */
    private Set<String> withSubjectTag(Set<String> tags, @Nullable LivingEntity other) {
        if (ailmentId == null || other == null || !tags.contains(EventName.CAUSED_AILMENT)) return tags;
        Set<String> withSubject = new HashSet<>(tags);
        withSubject.add(subjectTagPrefix(ailmentId) + ":" + other.getUUID());
        return withSubject;
    }

    /**
     * 去重身份标签：{@code prefix + ":" + key}。完整标签形如 {@code victim_ignite:<uuid>} / {@code skill_<id>}。
     * <p>供 {@code EventHistoryValue.mode=distinct} 以 PREFIX 匹配聚合同族、以完整标签去重。
     * 调用方（{@code GameEvent.fire} 的 {@code extraSubjectTag}、或 {@code SkillEvent} 直接写队列）负责构造
     * 语义化前缀（{@code victim_<ailmentId>} 用于去重敌、{@code skill_} 用于去重技能类型），
     * 使身份空间彼此隔离、不会与裸事件/标签混淆。
     */
    public static String distinctSubjectTag(String prefix, String key) {
        return prefix + ":" + key;
    }

    /** 事件标签集共同构成（造成/承受唯一差异 = 异常方向键），消除两份逐字重复的拼接实现。 */
    private Set<String> eventTags(String ailmentKey) {
        Set<String> t = new HashSet<>(formTags);
        t.add(eventName);
        if (ailmentId != null) {
            t.add(ailmentId);
            t.add(ailmentKey);
        }
        return t;
    }

    // ───────────────────────────── 写 Context + fire（造成/承受对称闭环） ─────────────────────────────

    /**
     * 仅写计时键（不 fire）：供无独立 fire 通道的辅助事件（如 {@code caused_crit}/{@code taken_crit}
     * 已并入命中那一次 fire，只需保留自己的 recency 计时）使用。
     */
    public static void recordTimer(@Nullable LivingEntity owner, String key) {
        if (owner != null) ExpressionContext.get(owner).refreshTimer(key);
    }

    /** 写本事件的计时键（合并原 {@code recordCaused}/{@code recordTaken}——二者实现逐字相同，
     *  方向已由 {@code eventName} 的 {@code caused_*}/{@code taken_*} 前缀表达，无须两个方法）。 */
    public void record(@Nullable LivingEntity owner) {
        recordTimer(owner, eventName);
    }

    /**
     * 造成侧事件闭环（一次调用完成「①存 Context 计时 ②fire 触发 on-cause」）：
     * owner = 造成者（事件主语，为空则整体跳过），other = 承接方。
     *
     * @param skill      本次涉及的技能标识（可空；其字符串形式并入标签）
     * @param extraTags  额外作用域标签（元素/暴击等；与 {@link #causedTags()} 求并集）
     */
    public void fireCaused(@Nullable LivingEntity owner, @Nullable LivingEntity other,
                           @Nullable Identifier skill, Set<String> extraTags) {
        fire(owner, other, causedTags(), skill, extraTags, null);
    }

    /** 造成侧事件闭环（携带伤害上下文）。 */
    public void fireCaused(@Nullable LivingEntity owner, @Nullable LivingEntity other,
                           @Nullable Identifier skill, Set<String> extraTags, @Nullable TriggerContext tc) {
        fire(owner, other, causedTags(), skill, extraTags, tc);
    }

    /** 造成侧事件闭环（无额外标签）。 */
    public void fireCaused(@Nullable LivingEntity owner, @Nullable LivingEntity other, @Nullable Identifier skill) {
        fire(owner, other, causedTags(), skill, Set.of(), null);
    }

    /** 造成侧事件闭环（携带额外去重身份标签，如 {@code skill_<id>}）。 */
    public void fireCaused(@Nullable LivingEntity owner, @Nullable LivingEntity other,
                           @Nullable Identifier skill, String extraSubjectTag) {
        this.extraSubjectTag = extraSubjectTag;
        fire(owner, other, causedTags(), skill, Set.of(), null);
    }

    /** 承受侧事件闭环：与 {@link #fireCaused} 完全对称，owner = 承受者、other = 造成者。 */
    public void fireTaken(@Nullable LivingEntity owner, @Nullable LivingEntity other,
                          @Nullable Identifier skill, Set<String> extraTags, @Nullable TriggerContext tc) {
        fire(owner, other, takenTags(), skill, extraTags, tc);
    }

    /** 承受侧事件闭环（无额外标签）。 */
    public void fireTaken(@Nullable LivingEntity owner, @Nullable LivingEntity other, @Nullable Identifier skill) {
        fire(owner, other, takenTags(), skill, Set.of(), null);
    }

    /** 造成/承受两侧唯一的发射实现（计时 + fire），差异仅在传入的标签集与 owner/other 方向。 */
    private void fire(@Nullable LivingEntity owner, @Nullable LivingEntity other,
                      Set<String> tags, @Nullable Identifier skill, Set<String> extraTags,
                      @Nullable TriggerContext tc) {
        if (owner == null) return;
        tags.addAll(extraTags);
        // 计数队列：fire 通道必须同时写 Event.State 计数队列，否则所有走 fireCaused/fireTaken
        // 的事件键（caused_ailment / caused_block / caused_parry / taken_* 等一整族）
        // 在 count:<k> / recently:<k> 查询下恒返 0 且无报错。
        // 三参重载同时写 triggers（count:<k>）与 taggedTriggers（分维 count:<k>:<tag>）——
        // 后者使「近 N 秒造成的中毒次数」这类按异常/元素分维的查询可用（标签 = 本次 fire 作用域标签）。
        // Event.record 内部同样刷新 timer，且自带 isSubscribed 门控（未订阅事件零开销）。
        // 身份标签只进计数队列、不进 fire 作用域（下行 StatContext 仍用原 tags），避免 UUID 污染标签匹配：
        // withSubjectTag 拼 victim_<ailmentId>:<uuid>（造成侧异常事件），extraSubjectTag 由调用方追加（如 skill_<id>）。
        Event.record(owner, eventName, withSubjectTag(tags, other), extraSubjectTag);
        // 事件名 + 命中上下文随作用域传入：StatEngine.fire 经 scope.evalBase 注入求值基座，
        // 使 event:/event_tag:/hit_state:* 节点在 Stat 条件中可用（基座为裸单例时恒 0）
        // 事件对象（本次事件落在谁身上）：造成侧 = other（承接方），承受侧 = owner（承受者本人）。
        // HIT 回调的 Context 由此携带两方，anchor:hit 才能解析出「本次被命中实体」。
        LivingEntity eventEntity = eventName.startsWith("taken_") ? owner : other;
        StatEngine.fire(owner, other, eventEntity, StatContext.of(tags, skill, eventName, hitContext), tc);
    }

    // ───────────────────────────── 通用发射原语（阶段 3：全位置回调） ─────────────────────────────

    /**
     * 通用事件发射（D-8 全覆盖的基石）：任意键、任意 owner/target/标签，计数 + 计时 + fire 一步完成。
     * 不依赖 {@link #eventName()}（可发任意键），数据包经 {@code emit_event} 可监听任意行为。
     *
     * <p>与 {@code publish*}（§4.4 批次 4 的收敛形态）的差异：本原语<b>不要求事件名预先收口</b>——
     * 任意行为点都能发回调，最大化数据驱动；键拼写防护由批次 4.9 的 datagen 键校验兜底（D-4）。
     *
     * @param key   事件键（任意字符串；建议 EventName.* 常量）
     * @param owner 效果持有者（事件主语）
     * @param other 事件目标（可 null）
     * @param tags  附加作用域标签（形态/元素/异常 id 等；事件键本身自动并入）
     * @param skill 本次涉及的技能标识（可空；其字符串形式并入标签）
     * @param value 回传值（count/sum 用；默认 1）
     */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable LivingEntity other,
                            Set<String> tags, @Nullable Identifier skill, int value) {
        emit(key, owner, other, other, tags, skill, value);
    }

    /**
     * 通用发射（<b>显式携带事件对象</b>）：击杀 / 命中一类「事件语义上存在明确对象」的事件用，
     * 事件对象经 {@code StatEngine.fire} 写入 {@code StatEvalContext.eventEntity}
     * （→ {@code ConditionScope.eventEntity}，使 {@code anchor:killed} / {@code anchor:hit} 可解析）。
     */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable LivingEntity other,
                            @Nullable LivingEntity eventEntity, Set<String> tags,
                            @Nullable Identifier skill, int value) {
        if (owner == null) return;
        // 计数 + 计时（Event.record 一次写全；tick 基准）
        Event.record(owner, key, value);
        // fire 触发型效果：事件键 + 附加标签并集；key 即事件名（event: 判定经 scope.evalBase 注入）
        Set<String> fireTags = new HashSet<>(tags);
        fireTags.add(key);
        // emit 无需伤害数值上下文（tc 恒为 null），但回传值 value 经 fire 的 eventValue 透传给效果（如 RESOURCE_SPEND 真实消耗量）
        StatEngine.fire(owner, other, eventEntity, StatContext.of(fireTags, skill, key, null), null, (double) value);
    }

    /** 通用发射（显式携带事件对象，无技能、无回传值，等价 value=1）。 */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable LivingEntity other,
                            @Nullable LivingEntity eventEntity, Set<String> tags) {
        emit(key, owner, other, eventEntity, tags, null, 1);
    }

    /** 通用发射（无技能、无回传值，等价 value=1）。 */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable LivingEntity other, Set<String> tags) {
        emit(key, owner, other, tags, null, 1);
    }

    /** 通用发射（other 可为非生物实体，如投射物 / 方块实体来源的击杀者）。 */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable Entity other,
                            Set<String> tags, int value) {
        emit(key, owner, other instanceof LivingEntity le ? le : null, tags, null, value);
    }

    /** 通用发射（other 可为非生物实体，无回传值）。 */
    public static void emit(String key, @Nullable LivingEntity owner, @Nullable Entity other, Set<String> tags) {
        emit(key, owner, other, tags, 1);
    }

    public String eventName() {
        return eventName;
    }
}
