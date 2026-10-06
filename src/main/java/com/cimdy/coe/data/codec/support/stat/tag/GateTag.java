package com.cimdy.coe.data.codec.support.stat.tag;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.RemnantDeliveryExecutor;
import com.cimdy.coe.data.codec.skill.helper.resource.ResourceType;
import com.cimdy.coe.data.gen.provider.GateTagCategoryProvider;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.event.combat.GameEvent;
import com.cimdy.coe.event.skill.SkillEvent;
import com.cimdy.coe.registry.AttributeRegistry;
import java.util.*;
import com.cimdy.coe.data.codec.support.meta.TagHierarchy;
import com.cimdy.coe.data.codec.support.logic.LeafMatcher;
import com.cimdy.coe.data.codec.support.logic.LogicalExprs;
import com.cimdy.coe.data.helper.engine.DamagePipeline;

/**
 * 修饰词标签维（标签本体即 {@code String}）。
 * <p>
 * 标签本体即 {@code String}，任意数据驱动的值
 * （技能名、作者标签、武器需求、消耗类型 …）都能直接作为标签，无需预先登记。
 * 本类保留的常量仅为内置通用标签的便捷别称，实际作用域匹配只看字符串相等
 * （子集包含判定），故新增标签不必改这里。
 * <p>
 * 标签统一「短词、无类目前缀」——不同维度靠命名空间天然区分（{@code tags} 集合本就小、不会撞）：
 * <ul>
 *   <li>释放类型：{@code attack} / {@code spell}；</li>
 *   <li>伤害元素（共存、各自独立）：{@code fire} / {@code cold} / {@code lightning} / {@code chaos} / {@code physical}；</li>
 *   <li>投放形态：{@code projectile} / {@code area} / {@code beam} / {@code aura} / {@code self}
 *       （即 {@code Delivery.getType().id()}，与数据注册名一致，不加 delivery_ 前缀）；范围命中另带 {@code aoe}；</li>
 *   <li>武器 / 命中方式（按持握武器与命中方式推断，直接取名称、不加前缀）：
 *       {@code bow} / {@code crossbow} / {@code spear} / {@code mace} / {@code melee} / {@code ranged}；
 *       投射物命中复用 {@code projectile}，技能源命中复用 {@code spell}；</li>
 *   <li>技能名：技能 id 的 path（如 {@code fireball}）——数据侧 stat 直接写该名字即命中；</li>
 *   <li>武器装备需求：{@code Skill.equipment} 字符串原样（如 {@code bow}，无 equipment_ 前缀）；</li>
 *   <li>消耗类型：资源类型序列化名原样（如 {@code mana} / {@code energy} / {@code spirit} / {@code health}）；</li>
 *   <li>技能类型（数据驱动、无 skill_ 前缀）：{@code warcry} / {@code curse} / {@code mark} …。</li>
 * </ul>
 * 数据侧（{@code ModifierStatEffect} 的 {@code tags} 字段）即一串字符串，与上述一致。
 */
public final class GateTag {

    /**
     * 供 {@link GateExpr} 统一求值用的层级解析器：复用本类的 {@link #childrenOf} 与 proj: 通配语义。
     * 装备类型键侧对应 {@code ItemTypeHierarchy.resolver()}，二者同构，使 Stat 作用域与物品类型
     * 共用一套「与 / 或 / 非 + 父辖子」表达式引擎。
     */
    public static final TagHierarchy HIERARCHY = new TagHierarchy() {
        @Override
        public Set<String> childrenOf(String tag) {
            return GateTag.childrenOf(tag);
        }

        @Override
        public boolean supportsProjectionWildcard() {
            return true;
        }
    };

    // 释放类型
    public static final String ATTACK = "attack";
    public static final String SPELL = "spell";
    /**
     * 引导（channeling）：施放需要长按的技能（{@code HoldInput} 蓄力 / {@code Continuous} 持续释放）在施放全程携带此标签。
     * 使「[Channelling] 类」天赋加成（ARMOR_WHILE_CHANNELLING / WHILE_CHANNELLING_DAMAGE 等）仅在引导该技能期间生效。
     * 引导是「技能标签」，由 {@link com.cimdy.coe.data.codec.skill.core.Skill#getStatTags} 按输入模式产出，
     * 而非实体状态标志——实体状态读值节点经 {@code StatScopes.current} 读取本标签即可判断当前是否处于引导。
     */
    public static final String CHANNELING = "channeling";
    // 伤害元素（共存、各自独立）
    public static final String FIRE = "fire";
    public static final String COLD = "cold";
    public static final String LIGHTNING = "lightning";
    public static final String CHAOS = "chaos";
    public static final String PHYSICAL = "physical";
    public static final String ELEMENTAL = "elemental";

    // 投放形态 / delivery
    public static final String AREA = "area";
    public static final String AOE = "aoe";
    public static final String AURA = "aura";
    /** 快行（PoE2 二级标签 Travel）：位移类技能（Blink）。带此标签的精魂宝石激活时替换闪避翻滚。 */
    public static final String TRAVEL = "travel";
    /** 持续（Persistent）：在技能面板中切换开关、通常需保留精魂才生效（PoE2 次级标签 Persistent）。 */
    public static final String PERSISTENT = "persistent";
    /** 增益（Buff）：赋予自身/友方增益效果（PoE2 主标签 Buff）。 */
    public static final String BUFF = "buff";
    public static final String BEAM = "beam";
    public static final String PROJECTILE = "projectile";
    public static final String ARROW = "arrow";
    public static final String BOW = "bow";
    public static final String CROSSBOW = "crossbow";
    public static final String ONE_HANDED = "one_handed";
    public static final String TWO_HANDED = "two_handed";
    public static final String SWORD = "sword";
    public static final String MACE = "mace";
    public static final String DAGGER = "dagger";
    public static final String FLAIL = "flail";
    public static final String QUARTERSTAFF = "quarterstaff";
    /** 锤击（Slam）武器类型标签：持双手钝器主手时进入 {slam} 作用域，聚合「锤击」类 Stat 条目。 */
    public static final String SLAM = "slam";
    /** 新星（Nova）：以自身/落点为中心的环状扩张伤害形态（PoE2 次级标签 Nova）。 */
    public static final String NOVA = "nova";
    /** 持续（Duration）：技能具有持续时间/限时存在物（PoE2 次级标签 Duration）。 */
    public static final String DURATION = "duration";
    public static final String EMPOWERED = "empowered";
    public static final String ARCANE_SURGE = "arcane_surge";
    public static final String ARCHON = "archon";
    public static final String CHARGE = "charge";
    public static final String RARITY = "rarity";
    public static final String BOLT = "bolt";
    public static final String SELF = "self";
    public static final String PIERCE = "pierce";

    // 状态 / status
    public static final String STATUS = "status";
    public static final String AILMENT = "ailment";
    /** 非伤害性异常（控制类：冰缓/冰冻/感电），与伤害性异常（点燃/流血/中毒）正交。 */
    public static final String NON_DAMAGING = "non_damaging";
    public static final String BLIND = "blind";
    public static final String BLEED = "bleed";
    public static final String CHILL = "chill";
    public static final String DAZE = "daze";
    public static final String EXPOSURE = "exposure";
    public static final String FREEZE = "freeze";
    public static final String HINDER = "hinder";
    public static final String IGNITE = "ignite";
    public static final String IMMOBILISE = "immobilise";
    public static final String INCISION = "incision";
    public static final String MAIM = "maim";
    public static final String PIN = "pin";
    public static final String POISON = "poison";
    public static final String SHOCK = "shock";
    public static final String STUN = "stun";
    /** 重晕（Heavy Stun）状态 / 作用域标签：挂在 DAMAGE 上表达「对重晕敌人的伤害」类词缀（与 stun 并列，叠加而非互斥）。 */
    public static final String HEAVY_STUN = "heavy_stun";
    public static final String BEAST = "beast";
    // 地面机制族（POE2 Ground）：作用域标签，同时作为 GroundTypes.def 各地面 GroundDef.tags() 的自带标签。
    public static final String JAGGED_GROUND = "jagged_ground";
    public static final String IGNITED_GROUND = "ignited_ground";
    public static final String SHOCKED_GROUND = "shocked_ground";
    public static final String CHILLED_GROUND = "chilled_ground";
    public static final String ICE_CRYSTAL = "ice_crystal";
    public static final String WITHERED = "withered";
    public static final String DEBILITATE = "debilitate";
    public static final String SLOW = "slow";

    // 主语 / subject
    public static final String ALLIES = "allies";
    /** 帕鲁式盟友（玩家在据点终端显式标记的随行伙伴）：伤害主语路由标签，由 DamageEvent.buildHitTags 按 ALLY 附件发射。 */
    public static final String ALLY = "ally";
    public static final String BALLISTA = "ballista";
    public static final String BRAND = "brand";
    public static final String COMPANION = "companion";
    public static final String GOLEM = "golem";
    public static final String MINE = "mine";
    public static final String MINION = "minion";
    public static final String TOTEM = "totem";
    public static final String TRAP = "trap";

    // 事件 / event
    public static final String CRIT = "crit";
    public static final String KILL = "kill";
    public static final String LEECHING = "leeching";
    public static final String SHAPESHIFTED = "shapeshifted";
    /** 形态类目标签（形态状态互斥判定 + 形态效果作用域；同 Archon 的 archon 类目）。 */
    public static final String FORM = "form";
    /** 熊形态标签（形态专属效果作用域）。 */
    public static final String BEAR = "bear";
    /** 狼形态标签（形态专属效果作用域）。 */
    public static final String WOLF = "wolf";
    /** 龙形态标签（形态专属效果作用域）。 */
    public static final String WYVERN = "wyvern";
    /** 移动速度标签（Movement Speed 词缀作用域）。 */
    public static final String MOVEMENT = "movement";
    public static final String RECOUP = "recoup";
    public static final String CULLING = "culling";
    public static final String IMPALE = "impale";

    // 资源 / resource
    public static final String ENDURANCE = "endurance";
    public static final String ENERGY = "energy";
    /** 能量护盾（ES，与 ENERGY「能量」区分）：承伤吸收层资源标签，
     *  供 {energy_shield, regen} 作用域回复/转化条目匹配。 */
    public static final String ENERGY_SHIELD = "energy_shield";
    /** 承受伤害由蓝量承担（Mind Over Matter）作用域标签，对应 AttributeRegistry.MANA_TAKE_OF_DAMAGE。 */
    public static final String MANA_TAKE = "mana_take";
    public static final String FRENZY = "frenzy";
    public static final String HEALTH = "health";
    public static final String MANA = "mana";
    public static final String POWER = "power";
    public static final String RAGE = "rage";
    public static final String SPIRIT = "spirit";
    /** 荣耀（Glory）资源作用域标签：对应 ResourceType.GLORY（name="glory"）。 */
    public static final String GLORY = "glory";
    /** 连击（Combo）作用域标签：对应 ResourceType.COMBO（name="combo"，ResourceType.java:31），
     *  字面量与资源类型同源、不另起别名，避免两端改名静默失配。
     *  用于 {@code EventName.COMBO_SPENT} 发射端的 tags，使数据侧可按 {@code {combo}} 作用域匹配。 */
    public static final String COMBO = "combo";
    /** 波动（Volatility）资源作用域标签：对应 ResourceType.VOLATILITY（name="volatility"）。击杀/被眩晕获取，积攒至上限触发爆炸。 */
    public static final String VOLATILITY = "volatility";
    /** 急攻（Onslaught）Buff 作用域标签（「X% chance to gain Onslaught on Killing Blow」族）。 */
    public static final String ONSLAUGHT = "onslaught";
    /** 元素灌入（Elemental Infusion）资源作用域标签：对应 ResourceType.INFUSION（name="infusion"）。灌入期间强化元素伤害。 */
    public static final String INFUSION = "infusion";
    /**
     * 灌入残迹（Infusion Remnant）作用域标签：POE2 中消耗灌入层数时于地面留下的残迹，
     * 拾取者获得对应元素强化。天赋语料「Remnants you create have X% increased effect」
     * 即作用于此作用域（读 {@code AILMENT_MAGNITUDE}）。
     * <p><b>生产者</b>：{@code RemnantDeliveryExecutor.spawnRemnant} 生成 {@code coe:remnant} 载体，
     * 本标签为真实可消费的残迹作用域。
     * 仍须注意：错接到 {@code AILMENT}（通用异常）会越界放大全部异常，残迹效果应严格限定在 {@code remnant} 作用域。
     */
    public static final String REMNANT = "remnant";
    /** 异常为<b>本次新施加</b>（目标施加前不处于该异常）的事件标记；区别于「刷新已有异常」。 */
    public static final String FRESH_AILMENT = "fresh_ailment";
    /** 近战形态标签（GameEvent.formTagsOf 生成的 melee，命中资源获取/事件形态判定用）。 */
    public static final String MELEE = "melee";
    /** 武器类型条件标签（持斧类主手时进入 {axe} 作用域，聚合「持斧时」类 Stat 条目，与近战命中标签 "axe" 同源）。 */
    public static final String AXE = "axe";
    /** Mastery 子图：长矛类武器（inc_spear_damage 等条目作用域）。 */
    public static final String SPEAR = "spear";
    /** 法器/副手（Focus）装备标签——<b>当前为惰性占位常量</b>：装备域加成由 {@code equipmentStat("focus", stat)} 上下文节点（Conversions）承载，非 {@code {focus}} 作用域发射点。 */
    public static final String FOCUS = "focus";
    /** 箭袋（Quiver）装备标签——<b>当前为惰性占位常量</b>：装备域加成由 {@code equipmentStat} 上下文节点承载，非 {@code {quiver}} 作用域发射点。 */
    public static final String QUIVER = "quiver";
    /** 头盔（Helmet）装备标签——<b>当前为惰性占位常量</b>：装备域加成由 {@code equipmentStat("helmet"/"head", stat)} 上下文节点承载，非 {@code {helmet}} 作用域发射点。 */
    public static final String HELMET = "helmet";
    /** 戒指/项链（Jewellery）装备标签——<b>当前为惰性占位常量</b>：装备域加成由 {@code equipmentStat} / {@code equipment_bonus_to_minions} 等机制承载，非 {@code {jewellery}} 作用域发射点。 */
    public static final String JEWELLERY = "jewellery";
    /** 已镶嵌珠宝（Jewel）标签——<b>当前为惰性占位常量</b>：由对应宝石/插槽机制承载，非 {@code {jewel}} 作用域发射点。 */
    public static final String JEWEL = "jewel";

    // 技能类型 / skill_type
    public static final String CURSE = "curse";
    public static final String MARK = "mark";
    public static final String WARCRY = "warcry";
    /** 指挥（Command）技能作用域标签（「X% increased Mana Cost Efficiency of Command Skills」类词缀；
     *  召唤类技能经 withTags("command") 携带，SkillEvent puppet master 叠层亦按此标签检测）。 */
    public static final String COMMAND = "command";
    /** Puppet Master 状态/作用域标签（grant_on_event 授予 puppet_master 状态时作为作用域标签，与 command 并列）。 */
    public static final String PUPPET_MASTER = "puppet_master";
    public static final String HERALD = "herald";
    /** 旗帜（Banner）技能作用域标签（「Banner Skills have X% increased Duration」类词缀）。 */
    public static final String BANNER = "banner";
    /** 尸体（Corpse）技能/资源作用域标签：消耗尸体类技能（raise_zombie/consume_corpse）经自身 tags 字段携带。 */
    public static final String CORPSE = "corpse";
    /** 近期消耗尸体（Consumed a Corpse Recently）窗口标签：RECENTLY_CONSUMED 增益自带，门控「近期消耗尸体」类 Stat 条目。 */
    public static final String RECENTLY_CONSUMED = "recently_consumed";
    /** 手雷（Grenade）技能作用域标签（「X% increased Grenade Damage」类词缀）。 */
    public static final String GRENADE = "grenade";
    /** 祭品（Offering）技能作用域标签（「Offerings have X% increased Maximum Life」类词缀）。 */
    public static final String OFFERING = "offering";
    /** 封印（Seal）技能作用域标签（「Sealed Skills have X% increased Seal gain frequency」类词缀）。 */
    public static final String SEAL = "seal";
    /** 元技能（Meta）作用域标签（「Meta Skills have X% increased Reservation Efficiency」类词缀）。 */
    public static final String META = "meta";
    /** 危险物（Hazard）作用域标签（「X% increased Hazard Damage」类词缀）。 */
    public static final String HAZARD = "hazard";
    /** 风暴（Storm）技能作用域标签：Druid 风暴系技能经自身 {@code tags} 字段携带（Skill.getStatTags 发射），
     * 供「Storm Spells have X」类词缀 / 基石（wildsurge_incantation）作用域门控。 */
    public static final String STORM = "storm";
    /** 植物（Plant）技能作用域标签：Druid 植物系技能经自身 {@code tags} 字段携带（Skill.getStatTags 发射），
     * 供「Plant Spells have X」类词缀 / 基石（wildsurge_incantation）作用域门控。 */
    public static final String PLANT = "plant";

    // ── 技能标签族（Skill.getStatTags 发射 / 技能 tags 字段携带） ──
    /**
     * 引爆（Detonator）技能作用域标签（「Detonator Skills have X」类词缀）。
     * <p><b>产出点（live）</b>：{@code blue/detonate_dead} 经 {@code .withTags(GateTag.DETONATOR)} 携带
     * （POE2 中 Detonate Dead 属 Detonator 标签技能），经 {@code Skill.getStatTags} 进入技能作用域。
     */
    public static final String DETONATOR = "detonator";
    /** 触发类（Trigger）：技能本身是「条件满足时自动触发」的类别（PoE2 次级标签 Trigger）。
     *  与 TRIGGERED 区分：本标签描述技能<b>类别</b>，TRIGGERED 描述某次<b>施放</b>是被触发的。
     */
    public static final String TRIGGER = "trigger";
    /**
     * 被触发（Triggered）技能作用域标签（「Triggered Spells deal X」类词缀）。
     * <p><b>产出点（live）</b>：{@code SkillTriggerHelper.trigger} 用虚拟绑定槽
     * {@code TRIGGER_BIND = Integer.MAX_VALUE} 施放被触发技能，{@code Skill.getStatTags}
     * 依 {@code SkillContext.getBind() == TRIGGER_BIND} 判定并全程附加本标签，
     * 从而「被触发的施放」与「主动施放」在同一技能上可区分。
     */
    public static final String TRIGGERED = "triggered";
    /**
     * 先祖强化（Ancestrally Boosted）攻击作用域标签（「Ancestrally Boosted Attacks deal X」类词缀）。
     * <p><b>机制缺口（惰性）</b>：引擎尚无「重击后下次攻击被先祖强化」的 Warrior 机制与发射点，
     * 本标签先行注册以使数据侧语义正确——与 {@link #REMNANT} 同一处理口径：
     * 宁可暂时惰性，也不可错接到 {@code MELEE}（会把加成放大到全部近战攻击，语义越界）。
     * 天赋节点 {@code ancestral_boosted_area_and_damage} / {@code ancestral_boosted_attack_damage_and_stun} 已存在，
     * 待机制落地后本标签即由该机制发射。
     */
    public static final String ANCESTRAL_BOOST = "ancestral_boost";
    /**
     * 回声（Echo）法术作用域标签（「Echoed Spells have X」类词缀）。
     * <p><b>发射点</b>：回响重复机制由
     * {@code SkillStatScopes#enterEcho} 发射——仅在<b>回响重放出来的那一次</b>施放期间携带，
     * 因此「Echoed Spells have X% increased Area of Effect」只作用于重放、不串到原施放
     * （与 POE2 官方数值一致）。
     */
    public static final String ECHO = "echo";

    // 杂项 / misc
    public static final String ACCURACY = "accuracy";
    public static final String ARMOR = "armor";
    /** 破甲强度加成（{@code armor_break} 作用域，读 ARMOR_BREAK，缩放施加到受击方的 ARMOUR_BREAK 状态幅度）。 */
    public static final String ARMOR_BREAK = "armor_break";
    public static final String BLOCK = "block";
    /** Flask 药水作用域标签（「Recovery from Flasks」类词缀条件）。 */
    public static final String FLASK = "flask";
    /** Charm 护符作用域标签（「Charm 充能/效果」类词缀条件）。 */
    public static final String CHARM = "charm";
    /** Dodge 闪避作用域标签（「X% chance to Dodge」类词缀条件）。 */
    public static final String DODGE = "dodge";
    /** Guard 护盾作用域标签（「Flask grant Guard」类词缀条件）。 */
    public static final String GUARD = "guard";
    public static final String COOLDOWN = "cooldown";
    public static final String COST = "cost";
    public static final String DEFENCE = "defence";
    public static final String DEFLECT = "deflect";
    public static final String EFFICIENCY = "efficiency";
    public static final String EVASION = "evasion";
    public static final String KNOCKBACK = "knockback";
    public static final String LIMIT = "limit";
    public static final String PARRY = "parry";
    public static final String PRESENCE = "presence";
    public static final String RANGE = "range";
    /** 目标完全破甲（StatusInstance.fullyBroken 标记，单 ARMOUR_BREAK 状态两档合一）条件标签：挂在 DAMAGE 上表达「对完全破甲敌人的伤害」类词缀。 */
    public static final String FULLY_BROKEN_ARMOR = "fully_broken_armor";
    /** 目标被电击（ELECTROCUTE）状态条件标签：挂在 DAMAGE 上表达「对被电击敌人的伤害」类词缀。 */
    public static final String ELECTROCUTED = "electrocuted";
    /** 电击（Electrocute）状态 / 作用域标签（与 ELECTROCUTED 取反标签正交）：挂在 DAMAGE 上表达「对被电击敌人的伤害」类词缀。 */
    public static final String ELECTROCUTE = "electrocute";
    public static final String RESERVATION = "reservation";
    public static final String RESISTANCE = "resistance";
    public static final String SURROUNDED = "surrounded";
    public static final String RUNIC_WARD = "runic_ward";
    public static final String THRESHOLD = "threshold";

    // 伤害 / damage
    public static final String DAMAGE = "damage";
    // 持续伤害 / damage over time（与「异常」「元素」「具体异常」正交的又一分类维度）
    public static final String DAMAGE_OVER_TIME = "damage_over_time";

    // 承伤方 / taken（异常时长承伤方放大：{taken, 异常} 作用域，读 AILMENT_DURATION）
    public static final String TAKEN = "taken";
    /** 承伤方被暴击几率（暴击虚弱等）：挂在 TAKEN_CRIT_CHANCE 上表达「对目标暴击几率提升」。 */
    public static final String TAKEN_CRIT = "taken_crit";
    // 回复 / regen（资源自然回复率：{资源, regen} 作用域，读 REGENERATION_RATE/MAX_REGENERATION）
    public static final String REGEN = "regen";
    /**
     * 充能（recharge）：ES 在未受伤窗口后的快速回复，与 {@link #REGEN}（持续再生）是两种独立来源。
     * 二者在 PoE2 中可分别被禁用（「ES does not Recharge」/「cannot recover ES from Regeneration」），
     * 故 sourceTag 必须区分——混用会令两条门控互相错杀（禁用其一即同时废掉另一）。
     */
    public static final String RECHARGE = "recharge";
    /**
     * 腐蚀续航（corruption_endures keystone）标签——<b>当前为惰性占位常量，非真实作用域发射点</b>。
     * <p><b>免死机制不经本作用域</b>：由 {@code MarkerStatEffect.Kind.CORRUPTION_ENDURES}（INERT 标记，落 STAT_STORE）
     * 在 {@code DamagePipeline} 致死前掷骰读取（见 DamagePipeline.java:734/868），<b>不经</b> {@code {corruption}} 作用域 fold。
     * 本常量保留仅为文档 / 未来归一化预留；切勿在 effect/wiring 中以 {@code {corruption}} 作用域消费——
     * 它会恒不匹配（无发射点），等同死线。
     */
    public static final String CORRUPTION = "corruption";

    /** 远距（Defend with X% of Armour against Hit Damage from Enemies further than Xm）：承接远离敌人的承护条件作用域。 */
    public static final String FAR = "far";

    /** 低能量护盾（Defend with X% of Armour while not on Low Energy Shield）：承接未处于低能量护盾时的承护条件作用域。 */
    public static final String LOW_ENERGY = "low_energy";
    /** 数值量（Amount）标签：标记该 Stat 条目的数值幅度本身参与作用域判定（如「按数值量缩放」类机制），语义独立、不与具体维度耦合。 */
    public static final String AMOUNT = "amount";

    // ───────────────────── 标签层级（上位包含） ─────────────────────
    // 父标签 → 子标签集合。父标签出现在 entry（修饰词要求的标签）时，只要当前作用域
    // 含其任一子标签即视为满足（反向上位包含）。
    // 设计原则（用户裁定）：发射/读取端「只发关键下位标签」（fire / ignite / arrow …），
    // 由 GateTag.match 在此汇总判定父类别（elemental / ailment / projectile / damage_over_time …），
    // 不必在每个发射端逐个广播分类标签。
    // 层级表为「数据驱动 + 内置兜底」。
    // BUILTIN 是编译期内置默认值，同时作为 datagen 的数据源（见 GateTagCategoryProvider）与
    // 数据加载失败时的回落。运行期真正生效的是 hierarchy 字段，由 LoadEvent 在数据重载时替换。
    private static final Map<String, Set<String>> BUILTIN = buildBuiltin();

    private static Map<String, Set<String>> buildBuiltin() {
        Map<String, Set<String>> m = new LinkedHashMap<>();
        // 元素：伤害元素（fire/cold/lightning）+ 元素异常（ignite/chill/freeze/shock/electrocuted）
        m.put(ELEMENTAL, Set.of(FIRE, COLD, LIGHTNING, IGNITE, CHILL, FREEZE, SHOCK, ELECTROCUTED));
        // 投射物：箭矢（arrow）+ 全部 proj:<id> 投射物实例（实例 id 不硬编码在此表，
        // 由 match 的 proj: 前缀通配归入 PROJECTILE——proj:skill_arrow 等运行时动态标签自动命中）
        m.put(PROJECTILE, Set.of(ARROW));
        // 异常状态：所有异常 id（含 debuff 中的 control 类）
        m.put(AILMENT, Set.of(IGNITE, POISON, BLEED, CHILL, FREEZE, SHOCK, ELECTROCUTED, PIN,
                STUN, HEAVY_STUN));
        // 伤害性异常（DoT）：点燃/流血/中毒
        m.put(DAMAGE_OVER_TIME, Set.of(IGNITE, POISON, BLEED));
        // 非伤害性异常（控制类）：冰缓/冰冻/感电/电击/钉住/眩晕
        m.put(NON_DAMAGING, Set.of(CHILL, FREEZE, SHOCK, ELECTROCUTED, PIN, STUN, HEAVY_STUN));
        // 防御（D8 决议）：armor/evasion/energy_shield/block/resistance/deflect/parry/guard 的统一父标签，
        // 使"无法 roll 防御"类工作台工艺可一次性覆盖全部防御维度。
        m.put(DEFENCE, Set.of(ARMOR, EVASION, ENERGY_SHIELD, BLOCK, RESISTANCE, DEFLECT, PARRY, GUARD));
        return Collections.unmodifiableMap(m);
    }

    /**
     * 当前生效的父→子表。
     * <p>{@code volatile} + 整体替换：数据在后台线程加载，读取发生在游戏主线程/伤害管线，
     * 二者靠"不可变快照整体替换"而非逐键改写来保证可见性与一致性（读侧无需加锁）。
     */
    private static volatile Map<String, Set<String>> hierarchy = BUILTIN;

    /**
     * 子→父反向索引，与 {@link #hierarchy} 同步整体替换。
     * <p>{@code parentsOf} 每次调用都全表遍历，而 fire 触发索引会对每个事件标签调用它，
     * 属于热路径；本表为构建期算好的缓存，查询降为 O(1)。
     */
    private static volatile Map<String, Set<String>> parents = buildParents(BUILTIN);

    /**
     * 父→<b>全部后代</b>（传递闭包），与 {@link #hierarchy} 同步整体替换。
     *
     * <p>层级<b>深度不限</b>：一个父类别名下挂的子标签自身也可以充当父类别，继续往下分
     * （如「元素」→「火」，而「火」名下再挂别的下位类别）。判定若逐层下探会在热路径上反复递归，
     * 故本表在构建期就把每个标签的<b>全部后代</b>算好，查询降为 O(1)。
     */
    private static volatile Map<String, Set<String>> descendants = buildDescendants(BUILTIN);

    private static Map<String, Set<String>> buildParents(Map<String, Set<String>> children) {
        Map<String, Set<String>> rev = new HashMap<>();
        children.forEach((parent, kids) ->
                kids.forEach(kid -> rev.computeIfAbsent(kid, k -> new HashSet<>()).add(parent)));
        rev.replaceAll((k, v) -> Set.copyOf(v));
        return Collections.unmodifiableMap(rev);
    }

    /**
     * 求「父 → 全部后代」传递闭包。
     *
     * <p>从每个父类别出发广度展开：子标签入队后<b>继续作为父类别再展开一层</b>，
     * 直到不再有新标签——故再深的链也能一次查全，且每个标签至多入队一次（<b>重复查询检查</b>，
     * 链再长也只是走一遍，不会指数级重复展开）。同一标签第二次出队即跳过，故父子表自相矛盾
     * （数据包写环）也不会死循环。
     *
     * <p><b>注意</b>：这里只处理<b>父子级（分类）关系</b>——「元素」辖「火」。
     * 像「火能造成灼烧」这类<b>功能关系</b>不是父子级关系，<b>不能</b>写成层级挂进来，
     * 那需要别的机制（如伤害转换 / 异常挂载）。
     */
    private static Map<String, Set<String>> buildDescendants(Map<String, Set<String>> children) {
        Map<String, Set<String>> out = new HashMap<>();
        for (String parent : children.keySet()) {
            Set<String> acc = new LinkedHashSet<>();
            Deque<String> queue = new ArrayDeque<>(children.getOrDefault(parent, Set.of()));
            while (!queue.isEmpty()) {
                String cur = queue.poll();
                if (!acc.add(cur)) continue; // 自身标记命中（含环回指）即终止
                // 规模闸：病态扇形展开（如把每个标签都挂到每个标签名下）到此为止，
                // 该标签放弃闭包、退回「不分」——总比把内存吃穿强。
                if (acc.size() > MAX_DESCENDANTS) {
                    acc.clear();
                    break;
                }
                Set<String> kids = children.get(cur);
                if (kids != null) queue.addAll(kids);
            }
            acc.remove(parent); // 不该把自己算作自己的后代（数据表写环时兜底）
            out.put(parent, Collections.unmodifiableSet(acc));
        }
        return Collections.unmodifiableMap(out);
    }

    /** 内置默认层级（datagen 数据源 + 加载失败回落），顺序稳定。 */
    public static List<StatCategory> builtinDefaults() {
        List<StatCategory> out = new ArrayList<>(BUILTIN.size());
        BUILTIN.forEach((parent, children) -> out.add(StatCategory.of(parent, children)));
        return List.copyOf(out);
    }

    /**
     * 应用数据驱动的层级表（由 {@code LoadEvent} 在数据重载时调用）。
     *
     * <p>以内置表为基底叠加：{@code replace=false} 的条目与内置子集合取并集，
     * {@code replace=true} 则整体替换该父类别。这样数据包只需描述"增量"，
     * 不必把内置的一长串子标签重抄一遍；同时仍保留完全接管的能力。
     *
     * <p>传入空集合等价于 {@link #resetHierarchy()}（回落内置表），
     * 因此即便数据文件全部缺失或解析失败，父类别匹配也不会退化。
     */
    public static void applyHierarchy(Collection<StatCategory> loaded) {
        if (loaded == null || loaded.isEmpty()) {
            resetHierarchy();
            return;
        }
        Map<String, Set<String>> merged = new LinkedHashMap<>(BUILTIN);
        for (StatCategory h : loaded) {
            Set<String> previous = merged.get(h.parent());
            if (h.replace()) {
                merged.put(h.parent(), Set.copyOf(h.children()));
            } else {
                Set<String> union = new LinkedHashSet<>(merged.getOrDefault(h.parent(), Set.of()));
                union.addAll(h.children());
                merged.put(h.parent(), Collections.unmodifiableSet(union));
            }
            // 加载期拦环：本条会把整表带入循环 → 拒绝该条、保留原有取值
            if (cyclic(merged, h.parent())) {
                if (previous == null) {
                    merged.remove(h.parent());
                } else {
                    merged.put(h.parent(), previous);
                }
            }
        }
        Map<String, Set<String>> snapshot = Collections.unmodifiableMap(merged);
        // 先建索引再一起发布，避免读侧看到"新正向表 + 旧索引表"的撕裂状态。
        Map<String, Set<String>> rev = buildParents(snapshot);
        Map<String, Set<String>> desc = buildDescendants(snapshot);
        hierarchy = snapshot;
        parents = rev;
        descendants = desc;
    }

    /** 单个父标签允许保留的后代数量上限；超出即放弃该标签的闭包（退回「不分」）。 */
    private static final int MAX_DESCENDANTS = 512;

    /**
     * 从 {@code root} 出发的 DFS，判断以它为根的这丛层级是否<b>成环</b>。
     *
     * <p>环（数据包写 {@code A:[B]} + {@code B:[A]}）是唯一真正有害的形态：它会让后代闭包
     * 自我包含、语义自相矛盾，故在<b>加载期</b>拦下（该条记录整条退回、保留原有取值），
     * 而不是等运行时兜底。
     *
     * <p><b>深度不用上限卡</b>：层级本就可以无限往下分，物品类型树的查询递归也很深；
     * 终止性由「重复查询检查」（{@code path} 即当前路径集合，回指即判环）保证，
     * 不必也不该用深度阈值去误伤合法数据。
     *
     * <p>内置表 {@link #BUILTIN} 无环，故这里只会拦下数据包带来的问题。
     */
    private static boolean cyclic(Map<String, Set<String>> table, String root) {
        return cyclicFrom(root, table, new HashSet<>());
    }

    private static boolean cyclicFrom(String tag, Map<String, Set<String>> table, Set<String> path) {
        if (!path.add(tag)) return true; // 回指祖先 = 环
        for (String child : table.getOrDefault(tag, Set.of())) {
            if (cyclicFrom(child, table, path)) {
                path.remove(tag);
                return true;
            }
        }
        path.remove(tag);
        return false;
    }

    /** 回落到内置层级表（数据缺失 / 卸载数据包 / 测试复位）。 */
    public static void resetHierarchy() {
        hierarchy = BUILTIN;
        parents = buildParents(BUILTIN);
        descendants = buildDescendants(BUILTIN);
    }

    /**
     * 父标签的<b>全部后代</b>（传递闭包，深度不限；无则空）。
     *
     * <p>与 {@code ItemTypeHierarchy.childrenOf} 同契约：一个父类别名下挂的子标签自身
     * 还可以继续当父类别，往下分任意层，问祖先依然命中深层的后代。
     * 闭包在 {@link #applyHierarchy} 构建期算好（见 {@link #descendants}），查询为 O(1)。
     *
     * <p><b>只管父子级（分类）关系</b>：「元素」辖「火」可以；把「火能造成灼烧」这类
     * <b>功能关系</b>挂进来则不行——那需要伤害转换 / 异常挂载，不是本表的职责。
     *
     * @see #descendantsOf(String)
     */
    public static Set<String> childrenOf(String tag) {
        return descendants.getOrDefault(tag, Set.of());
    }

    /** {@link #childrenOf(String)} 的直白别名（语义同为全部后代）。 */
    public static Set<String> descendantsOf(String tag) {
        return childrenOf(tag);
    }

    /** 子标签 → 父标签集合（层级表反向，供 fire 触发索引做父类别展开，B-2.7 配套）。 */
    public static Set<String> parentsOf(String child) {
        return parents.getOrDefault(child, Set.of());
    }

    /**
     * 普通项判定器：直接包含 / 父辖子上位包含 / {@code proj:} 前缀通配。
     *
     * <p>组合语义（与 / 或 / 非）不在本类内实现，而是交给通用谓词机制
     * {@link com.cimdy.coe.data.codec.support.logic.LogicalExprs#flatAll}——与装备部位维度
     * （{@code SlotExpr}）同源。本类只保留上述叶子判定。
     */
    private static final LeafMatcher<Set<String>> MATCHER = (scope, tag) -> matchLeaf(tag, scope);

    /**
     * 排除项判定器：只做「直接包含 或 父辖子」，<b>不</b>启用 {@code proj:} 通配。
     *
     * <p>排除位<b>不</b>启用通配：{@code !proj:xxx} <b>不</b>拦截任意投射物实例
     * （只有正向的 {@code proj:xxx} 才通配）。若此处误用 {@link #MATCHER}，取反会反向放大通配范围。
     */
    private static final LeafMatcher<Set<String>> NEGATED_MATCHER = (scope, tag) -> containsOrChildOf(tag, scope);

    /**
     * 排除标签构造器：返回带 {@code !} 前缀的标签，语义为「作用域不得包含该标签（及其子标签）」。
     * 用于区分进攻侧 / 承受侧——进攻侧异常条目统一加 {@code not(TAKEN)}，使其不命中承受侧读取
     * （作用域含 taken），避免「你造成的异常幅度」错误放大「施加在你身上的异常」。
     */
    public static String not(String tag) {
        return "!" + tag;
    }

    /**
     * 作用域标签 {@code scopeTags} 是否满足 entry 要求的 {@code required} 标签集。
     * <ul>
     *   <li>精确匹配：{@code required} 中的普通标签必须被 {@code scopeTags} 直接包含；</li>
     *   <li>上位包含：{@code required} 中的父类别标签（见 {@link #childrenOf}），只要
     *       {@code scopeTags} 含其任一子标签即满足——实现「元素加成火/电/冰」「异常加成所有异常」等。</li>
     * </ul>
     * 例：entry 要求 {elemental}，作用域含 {fire} → 命中；作用域含 {ignite} → 命中。
     *
     * <p><b>机制归属</b>：这里的「与 / 或 / 非」是通用谓词机制
     * （{@link com.cimdy.coe.data.codec.support.logic.LogicalExpr}）在标签维度上的扁平形态——
     * 多项之间是与、父标签辖子是或、{@code !} 前缀是不是。求值本体在
     * {@link com.cimdy.coe.data.codec.support.logic.LogicalExprs#flatAll}，与 {@link GateExpr}、
     * {@code SlotExpr} 共用同一套算子语义；本类只保留叶子判定（{@link #MATCHER} / {@link #NEGATED_MATCHER}）。
     */
    public static boolean match(Set<String> scopeTags, Set<String> required) {
        return LogicalExprs.flatAll(required, MATCHER, NEGATED_MATCHER).matches(scopeTags);
    }

    /**
     * 普通项命中：直接包含 / 父辖子上位包含 / {@code proj:} 前缀通配。
     *
     * <p>即 {@code GateExpr.matchLeaf} 在 Stat 内置层级下的等价形态，逐分支一致
     * （含「父标签名下任一子标签命中 <b>或</b> 作用域含任意 {@code proj:} 实例即算命中」这一
     * P1-1 通配规则对<b>父标签</b>同样生效的细节）。
     */
    private static boolean matchLeaf(String tag, Set<String> scope) {
        if (scope.contains(tag)) return true;
        // 上位包含：查「全部后代」而非「直接子集合」，故层级可以继续往下分（深度不限）
        Set<String> kids = descendants.getOrDefault(tag, Set.of());
        if (!kids.isEmpty()) {
            for (String t : scope) {
                if (kids.contains(t)) return true;
            }
            return false;
        }
        // P1-1：proj:<id> 前缀通配——实例 id 不进层级表，运行时动态命中
        if (tag.startsWith("proj:")) {
            for (String t : scope) {
                if (t.startsWith("proj:")) return true;
            }
        }
        return false;
    }

    /**
     * 直接包含 或 上位包含（不含 {@code proj:} 通配；供排除项使用）。
     *
     * <p>上位包含同样走「全部后代」，与 {@link #matchLeaf} 的正向判定<b>严格对称</b>——
     * 否则层级一旦往下分，{@code !元素} 拦不住深层下位标签，而正向「元素」却命中它，排除位会被架空。
     */
    private static boolean containsOrChildOf(String tag, Set<String> scope) {
        if (scope.contains(tag)) return true; // 直接包含（含父标签字面量，如 scope 显式含 "projectile"）
        Set<String> kids = descendants.getOrDefault(tag, Set.of());
        for (String t : scope) {
            if (kids.contains(t)) return true;
        }
        return false;
    }
}
