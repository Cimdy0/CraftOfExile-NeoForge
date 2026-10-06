package com.cimdy.coe.event;

import com.cimdy.coe.data.codec.skill.phase.event.trigger.ComboEvent;

import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.codec.skill.phase.action.combo.ComboAction;
import com.cimdy.coe.data.helper.status.AilmentHelper;
import com.cimdy.coe.data.helper.combat.DamageHelper;

import com.cimdy.coe.data.helper.hub.ResourceEventHelper;
import com.cimdy.coe.data.codec.support.stat.effect.grant.GrantOnEventStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.resource.ResourceStatEffect;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.LivingStateValue;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.combat.SummonEvent;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.event.world.EntityOperationEvent;
import com.cimdy.coe.data.helper.engine.DamagePipeline;
import com.cimdy.coe.data.helper.hub.ResourceRegistry;

/**
 * 全局事件名常量（统一收口手写 String，避免拼写漂移导致 Context 存储 / Stat 门控 / 表达式读取 三方失配）。
 *
 * <p>所有事件名（Context 计时键、SkillEventBus 触发名、StatEngine.fire 造成判定）都应收口在本类常量，
 * 字段本身即 {@code String}，禁止在调用处手写字符串字面量。</p>
 *
 * <p>统一命名规范（每事件单一名称，不区分技能事件/其他）：
 * <ul>
 *     <li><b>事件本体/元事件</b>（裸名）：如 {@link #CRIT}、{@link #KILL}，
 *         作为 Context.store 键 / SkillEventBus 触发名；</li>
 *     <li><b>造成事件</b>（{@code caused_} 前缀）：如 {@link #CAUSED_CHILL}、{@link #CAUSED_HIT}，
 *         作为「本实体造成 X」时的事件标签（fire 通道，owner=造成方）。</li>
 *     <li><b>承受事件</b>（{@code taken_} 前缀）：如 {@link #TAKEN_CHILL}、{@link #TAKEN_HIT}，
 *         作为「本实体被施加/承受 X」时的事件标签（fire 通道，owner=承受方），
 *         与对应的 {@code caused_} 前缀常量<b>对称、独立可匹配</b>——数据侧写 {@code caused_chill} 命中「造成冰缓」，
 *         写 {@code taken_chill} 命中「被冰缓」，两者字符串不同、互不影响。</li>
 * </ul>
 * 判定/读取（fold 通道）用 GateTag 短词标签（如 {@code chill}），不置于本类。
 * </p>
 */
public final class EventName {
    private EventName() {
    }

    // ───────────────────────────── 计时事件（裸名，Context.store 键） ─────────────────────────────

    public static final String JOIN_LEVEL = "join_level";
    public static final String LEAVE_LEVEL = "leave_level";
    public static final String MOUNT = "mount";
    public static final String DISMOUNT = "dismount";
    public static final String TELEPORT = "teleport";
    public static final String STRUCK_BY_LIGHTNING = "struck_by_lightning";
    public static final String TRAVEL_DIMENSION = "travel_dimension";
    /** 暴击事件（统一名：裸名 crit_hit、计时键 time_crit、元事件 on_crit 均收敛于此）。 */
    public static final String CRIT = "crit";
    public static final String SHIELD_BLOCK = "shield_block";
    public static final String CHANGE_TARGET = "change_target";
    public static final String EQUIPMENT_CHANGE = "equipment_change";
    public static final String USE_ITEM_START = "use_item_start";
    public static final String USE_ITEM = "use_item";
    public static final String USE_ITEM_STOP = "use_item_stop";
    public static final String FALL = "fall";
    public static final String JUMP = "jump";
    public static final String KNOCKBACK = "knockback";
    public static final String HEAL = "heal";
    public static final String USE_TOTEM = "use_totem";
    public static final String EFFECT_ADDED = "effect_added";
    public static final String EFFECT_REMOVED = "effect_removed";
    public static final String EFFECT_EXPIRED = "effect_expired";
    public static final String MOB_SPLIT = "mob_split";
    public static final String CONVERSION = "conversion";
    public static final String DESTROY_BLOCK = "destroy_block";
    public static final String DROWN = "drown";
    public static final String SWAP_ITEMS = "swap_items";
    public static final String GET_PROJECTILE = "get_projectile";
    /**
     * 发射弹药事件：玩家/实体发射投射物（弩箭等）时记录，携带身份标签 {@code ammo:<物品id>}，
     * 供 {@code distinct:fired_ammunition.<secs>:ammo} 去重统计「过去 N 秒内发射过几种不同弹药」
     * （StatId {@code INC_DAMAGE_WITH_CROSSBOWS_FOR_EACH_TYPE_OF_AMMUNITION_FIRED_IN_THE_PAST_SECONDS}）。
     * 接入点：{@code ProjectileInShoot#modifyArgs}（箭矢携带的发射物品即弹药身份）。
     */
    public static final String FIRED_AMMUNITION = "fired_ammunition";
    /**
     * 装填完成事件：弩装填（charge）完成、弹药已上膛的那一刻记录，供
     * 「近期装填过 → 弩箭有概率不消耗弹药」类词缀（StatId {@code BOLTS_FIRED_BY_CROSSBOW_...}）
     * 经 {@code Event.get(shooter).recently(RELOADED, window)} 读取。
     * 接入点：{@code CrossbowReload}（Mixin 注入 {@code CrossbowItem#tryLoadProjectiles} 成功返回处）。
     */
    public static final String RELOADED = "reloaded";
    public static final String TAME = "tame";
    public static final String EXPERIENCE_DROP = "experience_drop";

    // 吸取（LeechValue 写入，LivingStateValue.leeching 读取）
    // Context 仅存单一门控/存储键 LEECH；资源类型即标签（统一用 GateTag.HEALTH/MANA/ENERGY），不另建 "leech_health" 同义标签：
    //   Event.record(living, LEECH, GateTag.HEALTH) 等；
    //   LivingStateValue.leeching 用 Event.State.recently(LEECH, GateTag.HEALTH, window, PREFIX) 按资源标签分别判定。
    // 来源维度：标签采用 ":" 分层，来源技能标识拼为 "health:<sourceId>"（无来源则不拼），
    //   写入端 LeechValue.add*Value(..., sourceId) 由 DamageHelper.applyLeechEffects(..., skillIdsOf(source)) 透传；
    //   读取端可用 recently(LEECH, "health:coe:fireball", window, EXACT) 按来源独立判定，
    //   支持「不同来源的偷取速率不一致」分别接线。
    public static final String LEECH = "leech";
    /** 补给（recoup）登记事件：由 {@link com.cimdy.coe.data.helper.impl.value.RecoupValue#addRecoup} 在承受伤害触发补给时记录；
     *  资源类型即标签（GateTag.HEALTH/MANA/ENERGY），使「受击补给」可被 recently:/count: 节点读取。 */
    public static final String RECOUP = "recoup";

    // ───────────────────────────── 资源变更（ResourceEventHub 统一写入） ─────────────────────────────
    // 资源类型即标签（GateTag.HEALTH/MANA/ENERGY/RAGE），来源维度以 ":" 分层拼在标签后（如 "rage:melee"）。
    /** 资源回复事件：由 {@link com.cimdy.coe.data.helper.hub.ResourceEventHelper#onHeal} 在实际回复 > 0 时记录。 */
    public static final String RESOURCE_GAIN = "resource_gain";
    /** 资源消耗事件：由 {@link com.cimdy.coe.data.helper.hub.ResourceEventHelper#onSpend} 在实际扣减 > 0 时记录。 */
    public static final String RESOURCE_SPEND = "resource_spend";
    /** 怒气获得计时键：刷新后进入固有流失宽限期（POE2 为 4 秒），期间不掉落怒气。 */
    public static final String RAGE_GAINED = "rage_gained";
    /** 怒气消耗金额事件：由 {@link com.cimdy.coe.data.helper.hub.ResourceEventHelper#onSpend} 在 resource==RAGE 且 actual>0 时记录，
     *  回传值=实际消耗怒气量，供 ResourceRegistry.tickRageRegenFromSpent 经 Event.State.sum 求窗口内总消耗（R3 安全，复用 CONSUME_GLORY 同款带金额通道）。 */
    public static final String RAGE_SPENT = "rage_spent";

    // 招架（LivingStateValue.parrying 读取；由外部战斗系统写入）
    public static final String PARRY = "parry";
    /** 招架消耗标记：招架姿态被「挡下一个」消费后短时活跃，避免一次姿态挡多次。 */
    public static final String PARRY_CONSUMED = "parry_consumed";

    // ───────────────────────────── 已造成型异常（caused_ailment，Context.store 键 + fire 门控） ─────────────────────────────
    // 语义为「本实体近期『造成』过某异常」——由造成异常者（攻击者/施法者）在异常真实生效后记录，
    // 供 recently/count 表达式查询，并作为 StatEngine.fire 的作用域标签驱动 on-cause 触发效果（如造成冰缓时回血）。
    // 异常具体类型（chill/freeze/shock/electrocute/ignite/poison/bleed/stun）已是标签（来自 StatusType.tagsOf），
    // 故此处仅收口为单一 CAUSED_AILMENT；具体异常类型经标签子集判定（fire 标签集已并入 ailmentId + 分类标签），
    // 不再为每种异常立枚举常量。
    /** 通用「造成异常」事件：作为 {@code StatEngine.fire} 作用域标签与具体异常名（{@code chill} 等）并存，
     * 驱动 on-cause 触发型 Stat 效果（如「造成异常时回血」——数据侧写 tags [caused_ailment, chill] 命中）。
     * 属事件体系（非 Tag 作用域短词），故置于本类而非 {@code GateTag}。 */
    public static final String CAUSED_AILMENT = "caused_ailment";

    // ───────────────────────────── 承受型异常（taken_ailment，与 CAUSED_AILMENT 对称、fire 门控） ─────────────────────────────
    // 语义为「本实体近期『被施加』过某异常」——由承受异常者（受害者）在异常真实生效后记录，
    // 供 recently/count 表达式查询，并作为 StatEngine.fire 的作用域标签驱动 on-taken 触发效果（如被冰缓时回血）。
    // 异常具体类型已是标签，故仅收口为单一 TAKEN_AILMENT（与 CAUSED_AILMENT 对称、独立可匹配）。
    /** 通用「承受异常」事件：与 {@link #CAUSED_AILMENT} 对称，作为 {@code StatEngine.fire} 作用域标签与具体异常名并存，
     * 驱动 on-taken 触发型 Stat 效果（如「被施加异常时回血」——数据侧写 tags [taken_ailment, chill] 命中）。 */
    public static final String TAKEN_AILMENT = "taken_ailment";

    // ───────────────────────────── 元技能能量事件（POE2 能量机制） ─────────────────────────────

    /** 元技能「能量充能」事件：元技能宝石满足触发条件积累 1 点能量时，由持有者作为 {@code StatEngine.fire} 作用域标签携带，
     * 驱动「元技能充能时触发 X」on-cause 效果（如「每次元技能充能回复生命」）。 */
    public static final String CAUSED_META_ENERGY = "caused_meta_energy";
    /** 元技能「满能量触发」事件：元技能能量达到阈值、消耗能量触发链接技能时，由持有者作为 {@code StatEngine.fire} 作用域标签携带，
     * 驱动「元技能触发时 X」on-cause 效果（如「元技能触发时施放 Y」）。 */
    public static final String META_SKILL_TRIGGERED = "meta_skill_triggered";
    /**
     * 波动爆炸事件（Volatility detonated）：volatility 资源蓄满（达上限）触发爆炸时由持有者作为 {@code StatEngine.fire} 作用域标签携带，
     * 驱动「波动爆炸时 X」on-cause 效果（如「波动爆炸对周围敌人施加 X」）。与 {@link #VOLATILITY}（资源充能/获取标签）正交。
     */
    public static final String VOLATILITY_DETONATED = "volatility_detonated";

    // ───────────────────────────── 已造成型非异常事件（*_caused，Context.store 键 + fire 门控，P4） ─────────────────────────────
    // 与异常 *_CAUSED 同源，覆盖离散「造成型」on-event：命中 / 暴击 / 格挡 / 招架。
    // 由造成事件者（攻击者/格挡者/招架者）在事件真实发生后记录，供 recently/count 查询 + StatEngine.fire 触发 on-cause 效果。
    /**
     * 造成命中（攻击侧）：fire 门控键 + Context 计时键 + 读取型条件键（如 {@code recently("caused_hit")}）。
     * 裸键 {@code "hit"} 统一为本键，造成/读取双通道同源。
     */
    public static final String CAUSED_HIT = "caused_hit";
    public static final String CAUSED_CRIT = "caused_crit";
    public static final String CAUSED_BLOCK = "caused_block";
    public static final String CAUSED_PARRY = "caused_parry";
    /**
     * 进入招架（轻盾）造成型：玩家右键举起轻盾（进入招架姿态）时由招架者记录，
     * 与读取型 {@code parry}（姿态）正交——本常量只在 fire 通道出现，驱动「进入招架时触发 X」类 on-cause 效果。
     * 接入点位：{@code EntityOperationEvent.onUseItemStart}（isBuckler 分支）。对称重盾 {@code CAUSED_SHIELD_RAISE}。
     */
    public static final String CAUSED_PARRY_RAISE = "caused_parry_raise";
    /** 完美招架（Perfect Parry）造成型：招架者在极短时机窗口内（刚进入招架姿态即挡下下一次打击）成功招架，反制更强。 */
    public static final String CAUSED_PERFECT_PARRY = "caused_perfect_parry";
    /**
     * 举盾（重盾）造成型：玩家右键举起盾牌（开始使用盾牌物品）时由举盾者记录，
     * 与读取型 {@code shield_block}（盾牌成功格挡）正交——本常量只在 fire 通道出现，驱动「举盾时触发 X」类 on-cause 效果。
     * 接入点位：{@code EntityOperationEvent.onUseItemStart}（Forge 精确事件，仅当使用物品为盾牌）。
     */
    public static final String CAUSED_SHIELD_RAISE = "caused_shield_raise";
    /**
     * 剑横扫之刃造成型：原版 {@code Player#attack} 横扫分支执行时由攻击者记录，
     * 接入点位：{@code PlayerAttackSweepMixin}（Mixin 注入横扫专属音效常量 {@code SoundEvents.PLAYER_ATTACK_SWEEP} 的 GETSTATIC，
     * 该常量为横扫分支唯一标志，精确对应原版横扫动作，非行为模糊匹配），驱动「横扫命中时触发 X」类 on-cause 效果。
     */
    public static final String CAUSED_SWEEP = "caused_sweep";

    // ───────────────────────────── 承受型非异常事件（*_taken，与 CAUSED_* 对称、fire 门控） ─────────────────────────────
    // 与异常 *_TAKEN 同源，覆盖离散「承受型」on-event：被命中 / 被暴击 / 被格挡 / 被招架。
    // 由承受事件者（受害者/格挡承受方/招架承受方）在事件真实发生后记录，供 recently/count 查询 + StatEngine.fire 触发 on-taken 效果。
    /** 被命中（承伤）事件：与 {@link #CAUSED_HIT}（造成命中）对称，由受害者记录。
     * 同时作为 Context.store 计时键（math 用 {@code recently("taken_hit")} 查询），供伤害门控。 */
    public static final String TAKEN_HIT = "taken_hit";
    /** 被暴击事件：与 {@link #CAUSED_CRIT} 对称，由受害者记录。 */
    public static final String TAKEN_CRIT = "taken_crit";
    /** 被格挡事件：与 {@link #CAUSED_BLOCK} 对称，由攻击方（被格挡者）记录。 */
    public static final String TAKEN_BLOCK = "taken_block";
    /** 被招架事件：与 {@link #CAUSED_PARRY} 对称，由攻击方（被招架者）记录。 */
    public static final String TAKEN_PARRY = "taken_parry";
    /** 被完美招架事件：与 {@link #CAUSED_PERFECT_PARRY} 对称，由攻击方（被完美招架者）记录。 */
    public static final String TAKEN_PERFECT_PARRY = "taken_perfect_parry";
    /** 闪避事件：由闪避者记录（Dodge 判定命中），供「闪避时触发 X」类效果。 */
    public static final String CAUSED_DODGE = "caused_dodge";
    /** 被闪避事件：与 {@link #CAUSED_DODGE} 对称，由攻击方（被闪避者）记录。 */
    public static final String TAKEN_DODGE = "taken_dodge";
    /**
     * 翻滚动作事件：由翻滚发起者（玩家）在<b>翻滚/空翻成功发起</b>时记录（{@code DodgeRollHelper.tryDodge} 返回非失败时），
     * 供「近期翻滚过 → 增闪避」类词缀（{@code INC_EVASION_IF_YOU_VE_DODGE_ROLLED_RECENTLY}）经 {@code recently(dodge_roll)} 读取。
     * <p>与 {@link #CAUSED_DODGE}（闪避<b>判定命中</b>，由 DamagePipeline 伤害管线发射）正交：
     * 本键是<b>主动翻滚动作</b>（空格/翻滚键），非受击时的闪避几率判定，二者不可混用。
     */
    public static final String DODGE_ROLL = "dodge_roll";

    // ───────── 攻击形态标签（单标签形式，fold/fire 共用，非「承受型」前缀） ─────────
    // 形态标签统一沿用攻击侧 {@code melee} / {@code projectile} / {@code proj:<id>} 单标签形式，
    // fold 传入「被击中」时结合形态标签（如 melee / projectile+proj:<id>），
    // fire 后并入「被击中事件」{@link #TAKEN_HIT} 的作用域（由 GameEvent.takenTags 合并）。
    // 触发维（TAKEN_HIT）与形态维（melee/projectile/proj:<id>）由标签集合天然区分，不串味。
    // 不再保留 taken_melee / taken_projectile / taken_proj:<id> 这类「承受型前缀」常量。

    // ───────────────────────────── P5 扩展域（造成型 / 承受型非伤害事件） ─────────────────────────────
    // 覆盖 EntityOperationEvent 既有裸名事件中具备「造成方 / 承受方」语义的离散 on-event：击退 / 治疗。
    // 命名仍遵循 caused_/taken_ 对称规范；无明确造成方的事件（jump/fall/mount 等行为类）暂不补 fire。

    /** 被击退事件：由被击退者（承受方）在击退发生时记录，
     * 驱动「被击退时触发 X」on-taken 效果。owner=被击退者本人（无需逆源），接入点 {@code EntityOperationEvent} LivingKnockBackEvent。 */
    public static final String TAKEN_KNOCKBACK = "taken_knockback";

    /** 造成治疗事件：由接受治疗者本人（同时是治疗的造成+承受方）记录，驱动「治疗时触发 X」on-cause 效果，
     * 接入点 {@code EntityOperationEvent} LivingHealEvent。 */
    public static final String CAUSED_HEAL = "caused_heal";
    /** 被治疗事件：与 {@link #CAUSED_HEAL} 对称，由接受治疗者本人记录，驱动「被治疗时触发 X」on-taken 效果。 */
    public static final String TAKEN_HEAL = "taken_heal";
    /** 承受持续伤害（DoT）事件：由 DoT 施加点（{@code AilmentHelper.applyDamageDoT} 经 StatusEngine.apply 写 DoT 处）记录，
     * 供「近期开始承受 DoT」条件（heartstopper 基石的 ±50% DoT）经 {@code recently(taken_dot, window)} 读取。
     * 造成型 DoT 事件：与 {@link #TAKEN_DOT} 对称，由施加者（造成方）在 DoT 异常施加时记录 + fire，
     * 驱动「近期造成持续伤害」类 on-cause 效果（与 TAKEN_DOT 形成完整双向事件，A-3 双向回调）。 */
    public static final String CAUSED_DOT = "caused_dot";
    /** 承受型 DoT 事件：与 {@link #CAUSED_DOT} 对称，由承受方（受害者）在 DoT 异常施加时记录（恒记录，见 EventSubscription.MECHANICAL），
     *  供「近期开始承受 DoT」条件（heartstopper 基石的 ±50% DoT）经 {@code recently(taken_dot, window)} 读取。 */
    public static final String TAKEN_DOT = "taken_dot";
    /** 被眩晕事件：由被眩晕者（轻/重晕统一）在眩晕生效时记录，
     * 驱动「近期被眩晕时 X」类词缀（如护盾充能加速的 stunned-recently 条件）。
     * 非机制必需，依赖 stat 条目经 EventHistoryValue 引用（EventRefCollector 订阅）才写入 State 队列。 */
    public static final String TAKEN_STUN = "taken_stun";

    // ───────────────────────────── D-8 全覆盖新增事件（阶段 3） ─────────────────────────────
    /** 死亡受害侧事件：由死者本人记录（与击杀者 KILL 对称），驱动「死亡时触发 X」「近期死亡」类词缀。
     * 接入点 {@code DamageEvent.LivingDeathEvent}。 */
    public static final String TAKEN_DEATH = "taken_death";
    /**
     * 召唤物死亡事件：由<b>召唤物的主人</b>记录（owner=主人，非死者、非击杀者），
     * 驱动语料「if one of your Minions has died Recently」（COND_RECENTLY_MINION_DIED）。
     * <p>接入点 {@code DamageEvent.LivingDeathEvent}（死亡统一发布点，覆盖全部死因——含 DoT / 反射 / 献祭，
     * 而非仅被击杀）；仅当死者能解析出 owner（{@code OwnerHelper.getOwner}）时记录，
     * 故「任何召唤物死亡」而非仅可复生召唤物（{@code SummonEvent.onMinionDeath} 只覆盖后者，不能顶替）。
     * <p>与 {@code MINION_REVIVED}（召唤物复活）对称，同属召唤物生命周期族。
     */
    public static final String MINION_DEATH = "minion_death";
    /**
     * 能量护盾充能启动事件：由持有者在 ES 充能<b>开始</b>的那一 tick 记录（边沿触发，非每 tick）。
     * 驱动语料「increased Evasion Rating if Energy Shield Recharge has started in the past {X} seconds」
     * （COND_ES_RECHARGE_STARTED_2S）。
     * <p>接入点 {@code AttributeEvent} 的 ES 充能分支：三个充能前置条件（距上次受击 ≥ 启动延迟、
     * ES 未满、充能未被禁用）成立即「在充能」；用 {@code ExpressionContext} timer 做边沿检测——
     * timer 不活跃时记一次并刷新 timer，后续 tick 仅续期不重复记，受击停止续期后 timer 自然过期，
     * 下次充能即可再记。故本键语义是「充能开始」而非「充能中」。
     */
    public static final String ES_RECHARGE_STARTED = "es_recharge_started";
    /** 攻击发起事件：由攻击者在伤害结算前记录，驱动「攻击时施加 X」类先行效果。
     * 接入点 {@code DamageEvent} 伤害管线入口（fire 前）。 */
    public static final String ATTACK_START = "attack_start";
    /** 进入区域事件：由进入者记录，驱动「进入区域时触发 X」类效果。
     * 接入点 {@code EntityOperationEvent.onJoinLevel}（复用加入世界语义，经 {@code GameEvent.emit} 记录）。 */
    public static final String ENTER_AREA = "enter_area";

    // ───────────────────────────── Context 计时事件名（refreshTimer store 键，收口散落字面量） ─────────────────────────────
    public static final String CAUSE_DAMAGE = "time_cause_damage";
    /** 击杀事件（统一名：计时键 time_kill、裸键 killed、元事件 on_kill 均收敛于此）。
     * 由击杀事件记录，供 {@code recently}/{@code count}/{@code getTimerElapsed} 查询与技能阶段/条件表达式使用。 */
    public static final String KILL = "kill";

    // ───────────────────────────── 形态（Shapeshift）事件 ─────────────────────────────
    /** 形态切换事件：由 {@code StatusEngine.applyForm} 记录（带新形态标签），供 {@code recently:form_changed} 条件使用。 */
    public static final String FORM_CHANGED = "form_changed";
    /** 变回人类事件：由 {@code StatusEngine.clearForms} 记录，供 {@code recently:form_cleared} 条件使用
     * （语料「Shapeshifted to Human form Recently」类词缀）。 */
    public static final String FORM_CLEARED = "form_cleared";
    /**
     * 被解除钉刺事件：由 {@code StatusEngine.clear}/{@code tick} 在 PIN 状态移除（到期/清除）时发射，
     * owner=施加钉刺者（caster，经 {@code inst.source} 解析），other=被解除钉刺的敌人，
     * 驱动语料「Enemies are Maimed for {X} seconds after becoming Unpinned」。
     * <p>与 {@link #FORM_CLEARED} 同源：以 {@code GameEvent.emit} 真实发射（经 {@code StatEngine.fire}），
     * 使 GrantOnEventStatEffect（UNPINNED 标签）真正触发——仅 {@code Event.record} 不入 fire 通道会致死线。
     */
    public static final String UNPINNED = "unpinned";
    /**
     * 完全破甲事件：由 {@code DamagePipeline#stageArmorBreak} 在目标护甲被削减至 ≤0
     * （{@code StatusInstance.fullyBroken} 由 false 翻转为 true 的<b>跃迁瞬间</b>）时真实发射，
     * owner=造成破甲者（{@code s.attacker}），other=被完全破甲的敌人（{@code s.living}），
     * 驱动语料「Enemies you Fully Armour Break are Maimed / cannot Regenerate Life」
     * （Punctured Lung，poe2db 内部 stat：{@code maim_enemy_on_full_armour_break} /
     * {@code broken_armour_enemies_cannot_regenerate_life}）。
     * <p>与 {@link #UNPINNED} 同源：必须以 {@code StatEngine.fire} 真实发射才不会致死线；
     * 且仅在跃迁沿触发一次（PoE2 语义：处于完全破甲期间再破甲只刷新时长、不重复触发）。
     */
    public static final String FULLY_BROKE_ARMOUR = "fully_broke_armour";

    // ───────────────────────────── 消耗品使用 / 资源消耗事件（on-use 回复族门控） ─────────────────────────────
    // POE2 语料存在成族的「使用 X 时回复 Y% 最大生命/魔力」条目（药剂/护符/荣耀/充能球）。
    // 这些条目属事件触发型即时回复（ResourceStatEffect heal 通道），而非持续型修饰符，
    // 故其门控必须是真实发射的事件键 + 细分标签，而不是各自独立的占位属性。

    /**
     * 使用药剂事件：由 {@code FlaskHelper.drink} 在饮用真实生效（已扣充能、已登记持续恢复）后发射，
     * 附带 {@code {flask, <资源>}} 标签。故语料「Recover X% of maximum Life when you use a Mana Flask」
     * 数据侧写 tags {@code [use_flask, mana]} 即可精确命中「用魔力药剂」而不误触生命药剂。
     */
    public static final String USE_FLASK = "use_flask";
    /**
     * 使用护符事件：由 {@code CharmEngine.triggerCharm} 在护符真实发动（充能已扣、效果已落地）后发射，
     * 附带 {@code {charm}} 标签，驱动语料「Recover X% of maximum Mana when a Charm is used」。
     * 连锁触发（{@code charm_activation_chance}）复用同一入口，故连锁那一次同样发射。
     */
    public static final String USE_CHARM = "use_charm";
    /**
     * 消耗荣耀事件：由 {@code ResourceRegistry.GlorySave.consume} 在实际消耗量 &gt; 0 时发射，
     * 回传 {@code value} = 实际消耗点数，使语料「Recover X% of maximum Life per Glory consumed」
     * 能按点数（而非按次数）结算。
     */
    public static final String CONSUME_GLORY = "consume_glory";
    /**
     * 消耗连击层数事件：由 {@code ResourceRegistry.ComboSave.consume} 在实际消耗量 &gt; 0 时发射，
     * 回传 {@code value} = 实际消耗层数，使语料「Skills deal X% increased Damage per Combo consumed, up to Y%」
     * 能按<b>层数</b>（而非按次数）结算。
     * <p>必须在 {@code consume} 内发射而非两个调用点：{@code SkillContext.consumeCombo} 传 {@code bus=null}
     * （连 ComboEvent 都不发），仅 {@code ComboAction} 走总线；且 {@code ComboEvent} 不带消耗量
     * （只有剩余层数，{@code totalSteps=-1} 是消耗哨兵），无法承载 sum 语义。
     * <p>读取端一律用 {@code sum:} 而非 {@code count:}——一次消耗多层只写一条记录，
     * count 会静默欠授予（同 {@code consume_endurance} 先例）。
     */
    public static final String COMBO_SPENT = "combo_spent";

    // ───────────────────────────── 充能球事件（键由类型拼接，收口为工厂方法防漂移） ─────────────────────────────
    // 发射端 ResourceRegistry.generateCharge/consume 按 ChargeType.getSerializedName() 拼键，
    // 消费端（StatGen 条目 tags）不得手写 "consume_power" 字面量：两端一旦改名即静默失配。
    // 故此处收口为工厂方法，两端共用同一拼接实现。

    /** 充能球「获得」事件键（{@code gain_<type>}）：与 {@code ResourceRegistry.generateCharge} 发射端同源。 */
    public static String gainCharge(String chargeType) {
        return "gain_" + chargeType;
    }

    /** 充能球「消耗」事件键（{@code consume_<type>}）：与 {@code ResourceRegistry.consumeCharge} 发射端同源。 */
    public static String consumeCharge(String chargeType) {
        return "consume_" + chargeType;
    }

    /** 通用「获得任意充能球」事件键（与具体类型键并行发射）。 */
    public static final String GAIN_CHARGE = "gain_charge";
    /** 通用「消耗任意充能球」事件键（与具体类型键并行发射）。 */
    public static final String CONSUME_CHARGE = "consume_charge";

    // ─────────────── 机制待接入的事件键（键先收口，发射点随机制落地） ───────────────
    // 以下三键对应的机制（战吼相位 / 残片拾取 / 召唤物复活）尚未实现，故暂无发射点。
    // 但对应语料条目已按 heal 通道 + 本键接线（而非各立占位属性）：
    // 机制落地时只需在对应位置补一行 GameEvent.emit（战吼可由技能相位的 emit_event 动作在数据侧发），
    // 条目即自动生效，无需回改 Stat 数据层。

    /**
     * 使用战吼事件：驱动语料「Recover X% of maximum Life and Mana when you use a Warcry」。
     * 预期发射点为战吼技能相位——可直接由数据侧 {@code emit_event} 动作发本键（无需 Java 改动），
     * 附带 {@code {warcry}} 标签。
     */
    public static final String USE_WARCRY = "use_warcry";
    /**
     * 拾取残片事件：驱动语料「Recover X% of Maximum Life/Mana when you collect a Remnant」。
     * 发射点：{@code RemnantState.collect}。附带 {@code {remnant}} 标签。
     */
    public static final String COLLECT_REMNANT = "collect_remnant";
    /**
     * 消耗元素灌入事件：驱动语料「Gain a Power Charge when you Consume an Elemental Infusion」
     * 与「increased Spell Damage if you've Consumed an Elemental Infusion Recently」。
     * <p>发射点：{@code ResourceRegistry.onConsumeInfusion}——该方法是灌入层数的<b>唯一</b>消耗入口
     * （{@code ResourceType.INFUSION} 的技能成本管线经 {@code ResourceType:108} 调此），
     * 故在此发射可覆盖全部消耗路径。附带 {@code {infusion}} 标签。
     */
    public static final String CONSUME_INFUSION = "consume_infusion";
    /** 消耗尸体事件：驱动语料 "if you've Consumed a Corpse Recently"。发射点 ResourceRegistry.consumeAndEmpowerCorpse。 */
    public static final String CONSUME_CORPSE = "consume_corpse";
    /**
     * 法术回响事件：驱动语料「{X}% increased Cast Speed per Spell Echoed Recently, up to {X}%」。
     * <p>发射点：{@code EchoState#tick}——每次<b>回响重放</b>法术时记录一次（原施放不记，
     * 一次回响 = 一次记录，故 Echo 3 次的技能每次施放累计 3 条）。
     * 限定<b>法术</b>：回响机制本身对任意技能生效（战吼的 Echoing Cry 也走同一引擎），
     * 而本事件仅在重放的技能携带 {@code spell} 标签时记录，与语料 "Spell Echoed" 对齐
     * （计数的 {@code count:echoed_spell} 无标签分维能力，故在事件名层区分而非标签层）。
     */
    public static final String ECHOED_SPELL = "echoed_spell";
    /**
     * 召唤物复活事件：驱动语料「Recover X% of maximum Life when one of your Minions is Revived」。
     * 预期发射点为召唤物复活处（owner=主人，other=被复活的召唤物），附带 {@code {minion}} 标签。
     * 与属性 {@code REVIVE_SPEED}（复活速度）同属复活机制 backlog。
     */
    public static final String MINION_REVIVED = "minion_revived";
    /**
     * 施放技能事件：驱动「each different Spell/Attack you've Cast/Used」去重缩放族。
     * <p>发射点：{@code SkillEvent} 技能<b>成功施放</b>处（{@code skill.canCast(context)} 通过后），
     * 覆盖全部技能（法术/攻击/战吼/召唤等），与 {@code USE_WARCRY}（仅战吼）互补、不重叠。
     * 计数队列经 {@code Event.record} 的 4 参重载写入身份标签 {@code skill_<id>}
     * （仅进计数队列、不进 fire 作用域），供 {@code distinct:cast_skill:skill} 按技能类型去重。
     * <p>语义：只要成功施放即记一次（无论是否命中/造成伤害），对应 PoE2「you've Cast/Used a …」——
     * 与实际命中解耦，故不可复用 {@code CAUSED_HIT}（后者只在命中时发射、会漏掉未命中施放）。
     */
    public static final String CAST_SKILL = "cast_skill";
    /**
     * 危害物触发事件：驱动语料「increased Damage for each Hazard you've Triggered Recently」等。
     * <p>发射点：{@code 触发时（on_trigger）}——危害物被敌人穿越触发（并自我摧毁/重新布置）时发射，
     * owner=危害物的主人（施法者）。附带 {@code {hazard}} 标签语义，供 recent 计数类条目读取。
     */
    public static final String HAZARD_TRIGGERED = "hazard_triggered";


}
