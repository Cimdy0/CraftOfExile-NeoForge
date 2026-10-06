package com.cimdy.coe.data.helper.combat;


import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.CoeConfig;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.cimdy.coe.data.codec.vanilla.DamageTypeConvert;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.helper.AncestralBoostAttacks;
import com.cimdy.coe.data.codec.skill.helper.EmpoweredAttacks;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.meta.DamageConvert;
import com.cimdy.coe.data.codec.support.math.ExpressionContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.damage.DamageTypes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.entity.EntityDefCarrier;
import com.cimdy.coe.data.codec.support.stat.effect.status.HitFlagStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.LuckyStatEffect;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.data.codec.support.stat.target.TargetSelector;
import com.cimdy.coe.data.helper.impl.value.EvasionEntropy;
import com.cimdy.coe.data.helper.impl.value.LeechValue;
import com.cimdy.coe.event.EventName;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class DamageHelper {
    /**
     * 伤害转化链顺序（POE2）：物理 → 闪电 → 冰霜 → 火焰 → 混沌。
     * <b>只能正向</b>（可跳级，如物理直接转火，但不能反向：混沌不可转回火/冰/电/物理）。
     * 链序决定伤害加成的应用顺序（先转化的类型先加成，转化后的伤害享受目标类型加成）。
     * <p>实现说明：{@link DamageConvert} 数据结构不动（GenEvent 数据兼容），本链序仅作为
     * 「调用处链式预处理」约束——归一化加成（{@link #applyDamageMultipliers}）按此顺序遍历。
     */
    private static final String[] CONVERSION_ORDER = {
            GateTag.PHYSICAL, GateTag.LIGHTNING, GateTag.COLD, GateTag.FIRE, GateTag.CHAOS};

    /** 转化链序 → 伤害槽位下标（damages[]: 0=物理 1=混沌 2=火 3=冰 4=电，见 {@code DamageTypeRegistry.DAMAGE_TYPE_TAGS}）。 */
    private static final int[] CONVERSION_SLOT_INDEX = {0, 4, 3, 2, 1};

    /** 「命中随距离递减」参考射程（米）：引擎无既有距离-命中曲线，此常量定义之——
     *  距离 ≥ 该值时施加全额惩罚比例（0 至该值之间线性递增，见 {@code INC_PENALTY_TO_ACCURACY_AT_RANGE} 消费端）。 */
    private static final double ACCURACY_RANGE_REF_METRES = 30.0;

    /**
     * 归一化伤害倍率加成（按转化链序）：五种伤害统一由 {@code DAMAGE} 单属性承载，类型由标签作用域表达——
     * 按转化链顺序（物理→电→冰→火→混沌）遍历，每类在 {类型标签, damage} 作用域内读 {@code DAMAGE} 聚合各类型加成。
     * 与 PHYSICAL/CHAOS/FIRE/COLD/LIGHT_DAMAGE 5 独立属性互斥（不并存）。
     * <p>注：额外附加伤害不走 EXTRA_*_DAMAGE 独立属性，
     * 正确范式为 DamageConvertStatEffect(gain)/词缀挂 DAMAGE 标签条目。
     *
     * @param attacker 攻击方（倍率读取端，实时 + 类型标签作用域）
     * @param damages  伤害数组 [物理, 混沌, 火, 冰, 电]，就地修改
     */
    public static void applyDamageMultipliers(LivingEntity attacker, float[] damages) {
        if (attacker == null) return;
        // 战吼强化：本次攻击是否携带 empowered 标签（在伤害倍率中央点消耗一次强化计数）
        boolean empowered = EmpoweredAttacks.consumeOne(attacker);
        // 落到瞬态标记：计数已在此扣减，击杀事件（同 tick 稍后）无法再经 get() 判定本次是否强化攻击。
        EmpoweredAttacks.markLastHit(attacker, empowered);
        // 先祖强化：本次攻击是否携带 ancestral_boost 标签（同一点消耗一次计数，与 empowered 同源）。
        // 与 empowered 同一中央点扣减，保证「先祖强化攻击」的 {damage, ancestral_boost, attack} 伤害加成即时生效。
        boolean ancestralBoost = AncestralBoostAttacks.consumeOne(attacker);
        // 「同伴伤害加成亦作用于自身」开关（ALLY_DAMAGE_APPLIES_TO_SELF + {companion}>0）：以 {companion} 作用域读 coe:damage，
        // 捕获持有者自身的 coe:damage{companion,damage} 增/减伤乘区（复用 COMPANIONS_DEAL_INC_DAMAGE 同源聚合，
        // 其条目 target=self_only、效果标签 {companion,damage}；正常攻击不带 companion 标签故不会串入普通乘区），
        // 使其亦作用于持有者自身伤害。增伤与减伤同源（减伤=负向 add_multiplied_base），一并覆盖。
        boolean companionDmgToYou = StatEngine.gateOf(attacker, "ally_damage_applies_to_self", Set.of(GateTag.COMPANION));
        for (int i = 0; i < CONVERSION_ORDER.length; i++) {
            Set<String> tags = new HashSet<>(Set.of(CONVERSION_ORDER[i], GateTag.DAMAGE));
            if (empowered) tags.add(GateTag.EMPOWERED);
            if (ancestralBoost) tags.add(GateTag.ANCESTRAL_BOOST);
            double mult = readInScopePublic(attacker, tags, AttributeRegistry.DAMAGE);
            if (companionDmgToYou) {
                // 扩展作用域是正常作用域的超集：已含正常乘区，额外捕获 companion 增/减伤；
                // 直接替换 mult 即得完整乘区（无需除法拆分，避免除零/浮点误差）。
                Set<String> ext = new java.util.HashSet<>(tags);
                ext.add(GateTag.COMPANION);
                mult = readInScopePublic(attacker, ext, AttributeRegistry.DAMAGE);
            }
            damages[CONVERSION_SLOT_INDEX[i]] *= (float) mult;
        }
    }

    /**
     * 归一化抗性/护甲减免：四种元素（混沌/火/冰/电）各遍历一遍 {@code DamageTypeConvert.applyReduction}
     * （元素标签作用域内读 RESISTANCES/MAX_RESISTANCES + 攻击方穿透），护甲单独处理。
     * 取代旧的 FIRE/COLD/LIGHT/CHAOS_RESISTANCE + MAX_*_RESISTANCE 8 独立属性。
     *
     * @param data     受击方 AdditionData 快照（护甲读此快照，须在命中作用域内取得才带本次加成）
     * @param living   受击方（抗性读取端，实时 + 元素作用域）
     * @param attacker 攻击方（穿透读取端；可为 null = 无穿透）
     * @param damages  伤害数组 [物理, 混沌, 火, 冰, 电]，就地修改
     */
    public static void applyDamageReductions(AdditionData data, LivingEntity living, LivingEntity attacker, DamageSource source, float[] damages) {
        // 攻击方穿透（归一属性 RESISTANCE_PENETRATION，按元素标签作用域读取；穿透为单一语义属性，元素标签已足以区分，无需 penetration 冗余标签）
        float penFire = attacker == null ? 0f : (float) readInScopePublic(attacker, Set.of(GateTag.FIRE), AttributeRegistry.RESISTANCE_PENETRATION);
        float penCold = attacker == null ? 0f : (float) readInScopePublic(attacker, Set.of(GateTag.COLD), AttributeRegistry.RESISTANCE_PENETRATION);
        float penLight = attacker == null ? 0f : (float) readInScopePublic(attacker, Set.of(GateTag.LIGHTNING), AttributeRegistry.RESISTANCE_PENETRATION);
        float penChaos = attacker == null ? 0f : (float) readInScopePublic(attacker, Set.of(GateTag.CHAOS), AttributeRegistry.RESISTANCE_PENETRATION);

        // 棘刺伤害忽略敌方护甲（thorns_damage_has_chance_to_ignore_enemy_armor，归一 chance·ARMOR）：
        // 持有者侧 MarkerStatEffect 标记承载几率（节点值 0.25/0.5，可叠加），仅对 statTriggered 的反射棘刺伤害生效
        // （直接攻击 statTriggered=false，不触发——避免越界放大全部物理伤害）。掷骰成功则跳过护甲减免（敌方护甲视为 0）。
        double thornsArmorIgnoreChance = 0.0;
        if (attacker != null && source instanceof SkillDamageSource sds && sds.isStatTriggered()) {
            thornsArmorIgnoreChance = StatEngine.rollChance(attacker, "thorns_ignore_armor", java.util.Set.of());
            // 棘刺伤害按速率偷取生命（OF_THORNS_DAMAGE_LEECHED_AS_HEALTH，GAP-FIX#15）：标记 amount = 偷取速率（占棘刺伤害比例，规范 10%）。
            // 仅对 statTriggered 反射棘刺伤害生效（直接攻击 statTriggered=false，不触发——避免越界放大全部伤害偷取）。
            // 复用既有 LeechValue 模型（maxHealth × LEECHES_RESPONSE × LEECH_RATE / 20 流率，按 amount 时长），与 applyLeechResource 同源。
            double thornsLeechRate = StatEngine.magnitudeOf(attacker, "thorns_damage_leeched_as_health", java.util.Set.of());
            if (thornsLeechRate > 0.0) {
                float thornsTotal = 0f;
                for (float d : damages) thornsTotal += d;
                if (thornsTotal > 0f) {
                    float maxHealth = attacker.getMaxHealth();
                    float amount = (float) (thornsLeechRate * thornsTotal);
                    amount *= (float) readInScopePublic(attacker, Set.of(GateTag.HEALTH, GateTag.LEECHING, "amount"), AttributeRegistry.AMOUNT_LEECHED);
                    if (amount > 0f) {
                        double response = readInScopePublic(attacker, Set.of(GateTag.HEALTH, GateTag.LEECHING), AttributeRegistry.LEECHES_RESPONSE);
                        float ratePerSec = maxHealth * (float) response * (float) attacker.getAttributeValue(AttributeRegistry.LEECH_RATE);
                        float recoverPerTick = ratePerSec / 20f;
                        int durationTicks = Math.max(1, (int) Math.ceil(amount / recoverPerTick));
                        LeechValue.addHealthValue(attacker, recoverPerTick, durationTicks);
                    }
                }
            }
        }
        if (thornsArmorIgnoreChance <= 0.0 || attacker.getRandom().nextDouble() >= Math.min(1.0, thornsArmorIgnoreChance)) {
            damages[0] = DamageTypeConvert.applyArmorReduction(data, damages[0]);
        }
        // 物理伤害减免（fraction，base=0）：在护甲减免之后叠加乘算；victim 主语标签（MINION/TOTEM）由命中承伤作用域注入，
        // readInScopePublic 经 enterMerged 自动并入，使 {TAKEN,PHYSICAL,MINION}/{TAKEN,PHYSICAL,TOTEM} 命中对应受害方。
        double physReduction = readInScopePublic(living, Set.of(GateTag.TAKEN, GateTag.PHYSICAL), AttributeRegistry.PHYSICAL_DAMAGE_REDUCTION);
        physReduction = Math.max(0.0, Math.min(physReduction, 1.0));
        damages[0] *= (1.0F - (float) physReduction);
        // 抗性取值模式（攻击方条目的两类改写，R3 真读）：
        //  ① 忽略非负元素抗性（IGNORE_NON_NEGATIVE_ELEMENTAL_RESISTANCE，零基标记）——“对冰冻目标才生效”
        //     由条目条件 COND_FREEZE_STATUS + conditionTarget 承载，故此处须用 readInScopeAgainst 绑定受害者求值，
        //     否则目标侧条件恒不成立（死线 #37 同型）。
        //  ② 元素抗性取反（INVERT_ELEMENTAL_RESISTANCE_CHANCE，百分比掷骰）——POE2 语料限定「敌方怪物」，
        //     由受害方阵营（Camp.MOB）在引擎侧门控（属性 id 不承载条件词，归一化 #55）。
        DamageTypeConvert.ResMode resMode = DamageTypeConvert.ResMode.NORMAL;
        if (attacker != null) {
            boolean ignoreNonNeg = StatEngine.gateOf(attacker, "ignore_non_negative_elemental_resistance", Set.of(GateTag.ELEMENTAL));
            boolean invert = false;
            double invertChance = readInScopePublic(attacker, Set.of(GateTag.ELEMENTAL),
                    AttributeRegistry.INVERT_ELEMENTAL_RESISTANCE_CHANCE);
            if (invertChance > 0 && living.getData(AttachmentRegistry.CAMP) == Camp.MOB) {
                invert = living.getRandom().nextDouble() < Math.max(0.0, Math.min(1.0, invertChance));
            }
            if (invert) resMode = DamageTypeConvert.ResMode.INVERTED;
            else if (ignoreNonNeg) resMode = DamageTypeConvert.ResMode.NON_NEGATIVE_ONLY;
        }
        // 透传 attacker 作交互对方（sourceEntity）：使攻击方挂载的「敌人抗性降低 X%」类条目
        // 在受害者读抗性时经跨实体聚合分支生效。attacker==null 时退化为无跨实体条目（既有行为）。
        damages[1] = DamageTypeConvert.applyReduction(living, GateTag.CHAOS, penChaos, damages[1], attacker, resMode);
        damages[2] = DamageTypeConvert.applyReduction(living, GateTag.FIRE, penFire, damages[2], attacker, resMode);
        damages[3] = DamageTypeConvert.applyReduction(living, GateTag.COLD, penCold, damages[3], attacker, resMode);
        damages[4] = DamageTypeConvert.applyReduction(living, GateTag.LIGHTNING, penLight, damages[4], attacker, resMode);
    }

    /** 在标签作用域内实时读取某属性（与 AilmentHelper.withScope 同源；entity 为空返回 0）。
     *  <b>并入当前栈顶外层标签</b>（{@link StatScopes#enterMerged}）：命中结算内读取时自动保留
     *  spell / attack / channeling 等外层行为标签，使「with Spells」类条目不因子作用域遮蔽而失效。 */
    public static double readInScopePublic(LivingEntity entity, Set<String> tags, Holder<Attribute> attr) {
        if (entity == null) return 0.0;
        AutoCloseable scope = com.cimdy.coe.data.codec.support.stat.engine.StatScopes.enterMerged(entity,
                StatContext.of(tags, null));
        try {
            return entity.getAttributeValue(attr);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 在标签作用域内实时读取某属性，并把 {@code against} 绑为该作用域的<b>交互对方</b>
     * （{@link StatContext#sourceEntity()}）——使「针对处于某状态的敌人」类条目
     * （条件经 {@code source.*} 节点读对方状态）在读取当刻按该敌人真实状态求值。
     *
     * <p>范式与 {@code DamagePipeline} 维度C 的外发转换一致：交互对方在命中当刻绑定，
     * 而非发射/初始化时缓存，否则目标侧条件恒不成立（静默死）。
     * {@code against} 为空时退化为 {@link #readInScopePublic}（按持有者读，无跨实体条件生效）。
     */
    public static double readInScopeAgainst(LivingEntity entity, Set<String> tags,
                                            Holder<Attribute> attr, LivingEntity against) {
        if (entity == null) return 0.0;
        if (against == null) return readInScopePublic(entity, tags, attr);
        AutoCloseable scope = com.cimdy.coe.data.codec.support.stat.engine.StatScopes.enter(entity,
                com.cimdy.coe.data.codec.support.stat.engine.StatScopes.mergeWithCurrent(entity,
                        StatContext.of(tags, null).withSourceEntity(against)));
        try {
            return entity.getAttributeValue(attr);
        } finally {
            try {
                scope.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isCriticalHit(LivingEntity attacker, Entity target, RandomSource random) {
        if (!(target instanceof LivingEntity victim)) return false;
        // 命中标志短路（HitFlagStatEffect）：受击方「不可被暴击」→ 强制不暴击。
        // 「绝不暴击」已收敛为属性通道（CRIT_CHANCE set_value 0，fold clamp 绝对压制），无需此处短路。
        if (StatEngine.hasHitFlag(victim, HitFlagStatEffect.HitFlag.UNCRITTABLE)) return false;
        // 暴击双掷（LuckyStatEffect{CRIT}，攻击方持有）：LUCKY 任一命中即暴击，UNLUCKY 两次皆中才暴击
        int crit = getCritChance(attacker);
        // 承伤方被暴击几率（暴击虚弱等）：受害者侧 CRIT_CHANCE @{taken} 叠加到攻击方暴击率（fold 归并 TAKEN_CRIT_CHANCE -> CRIT_CHANCE @{taken}；跨实体读取）。
        crit += (int)(readInScopePublic(victim, Set.of(GateTag.TAKEN), AttributeRegistry.CRIT_CHANCE) * 100);
        boolean first = random.nextInt(100) < crit;
        LuckyStatEffect.RollMode luck = StatEngine.rollModifier(attacker, LuckyStatEffect.RollSubject.CRIT);
        if (luck == LuckyStatEffect.RollMode.LUCKY) return first || random.nextInt(100) < crit;
        if (luck == LuckyStatEffect.RollMode.UNLUCKY) return first && random.nextInt(100) < crit;
        return first;
    }

    /** 暴击率归一：CRIT_CHANCE 单属性 + {attack, crit} 标签作用域（替代 ATTACK_CRIT_CHANCE/DEFINITELY_CRIT）。 */
    public static int getCritChance(LivingEntity attacker) {
        if (attacker == null) return 0;
        return (int) (readInScopePublic(attacker, Set.of(GateTag.ATTACK, GateTag.CRIT), AttributeRegistry.CRIT_CHANCE) * 100);
    }

    public static float[] calculateDamages(LivingDamageEvent.Pre event, AdditionData data, Entity entity, ServerLevel serverLevel, DamageSource source) {
        DamageConvert.MixedDamage mixedDamage = getMixedDamage(event, data, entity, source);
        float[] damages = new float[DamageTypes.SLOT_CAPACITY];
        damages[0] = mixedDamage.physical();
        damages[1] = mixedDamage.chaos();
        damages[2] = mixedDamage.fire();
        damages[3] = mixedDamage.cold();
        damages[4] = mixedDamage.light();

        // 附伤归原版 ATTACK_DAMAGE：ADD_*_DAMAGE 经 valueMod ADD_VALUE 接 Attributes.ATTACK_DAMAGE，
        // getMixedDamage 以 ATTACK_DAMAGE 为基、元素比例由武器分布推导；不设逐元素 fold 通道。

        RandomSource random = serverLevel.getRandom();
        // 计算伤害偏移（攻击方携带 LuckyStatEffect{DAMAGE} 时双掷取优/劣）
        LivingEntity attackerEntity = entity instanceof LivingEntity le ? le : null;
        // 受害方（受击实体）：供反向幸运读取（CHANCE_FOR_DAMAGE_OF_ENEMIES_HIT_DAMAGE_YOU_TO_BE_UNLUCKY）。
        LivingEntity victim = event.getEntity() instanceof LivingEntity lv ? lv : null;
        // #5 伤害类型分维：逐元素掷骰时携带该元素槽位标签（physical/chaos/fire/cold/lightning），
        // 仅匹配类型的幸运修饰参与（全伤害幸运标签恒真，仍全匹配）。
        damages[0] = calculateDamageDeviation(damages[0], random, CoeConfig.physicalDeviation, attackerEntity, victim, slotDamageTags(0));
        damages[1] = calculateDamageDeviation(damages[1], random, CoeConfig.chaosDeviation, attackerEntity, victim, slotDamageTags(1));
        damages[2] = calculateDamageDeviation(damages[2], random, CoeConfig.fireDeviation, attackerEntity, victim, slotDamageTags(2));
        damages[3] = calculateDamageDeviation(damages[3], random, CoeConfig.coldDeviation, attackerEntity, victim, slotDamageTags(3));
        damages[4] = calculateDamageDeviation(damages[4], random, CoeConfig.lightDeviation, attackerEntity, victim, slotDamageTags(4));

        return damages;
    }

    /**
     * 格挡判定。{@code amount} 为本次原始伤害量：格挡成功免伤但<b>仍累积眩晕条</b>，
     * 累积量按伤害比例计入 {@link ResourceRegistry}。
     */
    public static boolean isBlock(Entity entity, DamageSource source, RandomSource random, float amount) {
        if (entity instanceof LivingEntity living && source.getEntity() instanceof LivingEntity target
                && isAttackSkillSource(source)) {
            return BlockHelper.isAttackBlock(living, target, random, amount);
        }
        return false;
    }

    /**
     * 本次伤害是否来自「攻击类技能」——POE2 规则：<b>攻击掷命中/闪避、可被格挡；法术必中、不可被格挡</b>。
     * <p>用途：技能伤害源修复「施法者丢失」后 {@code source.getEntity()} 不再恒为 null，
     * 若不给闪避/格挡加攻击门控，法术会开始掷闪避并被格挡——与现状（法术恒必中、不可格挡）不符，属回归。
     * 故 {@link #shouldCheckHitOrBlock}（闪避）与 {@link #isBlock}（格挡）均以本判定收口。
     * <p>非技能源恒返回 {@code true}：原版近战/远程攻击、以及无技能伤害源
     * （{@code EffectApplier} / {@code StatusBehavior} 施加的 DoT、Stat 触发伤害）维持既有判定，行为不变。
     */
    private static boolean isAttackSkillSource(DamageSource source) {
        if (!(source instanceof SkillDamageSource sds)) return true;
        SkillInstance skill = sds.getSkill();
        if (skill == null) return true;
        return skill.getSkill().getLabels(skill.getContext()).contains(GateTag.ATTACK);
    }

    /**
     * 各槽位的基础伤害维度标签（类型 + 元素归属），全部取自 {@link GateTag}。
     * 只发关键下位标签（physical/chaos/fire/cold/lightning），{@code elemental} 由 GateExpr
     * 父标签上位包含判定（见 {@link GateTag#childrenOf}）；供伤害槽位匹配（damage_taken_convert /
     * recoup.from_type 等）与承伤转换公共复用。
     */
    public static Set<String> slotDamageTags(int slot) {
        return switch (slot) {
            case 0 -> Set.of(GateTag.PHYSICAL);
            case 1 -> Set.of(GateTag.CHAOS);
            case 2 -> Set.of(GateTag.FIRE);
            case 3 -> Set.of(GateTag.COLD);
            case 4 -> Set.of(GateTag.LIGHTNING);
            default -> Set.of();
        };
    }

    public static boolean isHit(Entity entity, DamageSource source, RandomSource random) {
        if (shouldCheckHitOrBlock(entity, source)) {
            LivingEntity living = (LivingEntity) entity;
            LivingEntity target = (LivingEntity) source.getEntity();
            // 命中不可闪避（HitFlagStatEffect{UNEVADABLE}，攻击方持有）：受击方闪避判定整体短路为命中
            if (target != null && StatEngine.hasHitFlag(target, HitFlagStatEffect.HitFlag.UNEVADABLE)) {
                return true;
            }
            Entity directEntity = source.getDirectEntity();
            AdditionData data = AdditionData.get(target, directEntity);
            // 闪避判断
            if (!living.hasData(AttachmentRegistry.EVASION_ENTROPY)) {
                EvasionEntropy.initialization(living, random);
            } else if (EvasionEntropy.isSameTickHit(living)) {
                return true;
            }

            int evasion = EvasionEntropy.getEntropy(living);
            int hitChance = 0;
            if (target != null) {
                // 命中率随攻击距离递减（INC_PENALTY_TO_ACCURACY_AT_RANGE）：攻击方持有该 INERT 标记时，
                // 按 (距离 / 参考射程) 线性放大惩罚比例后缩放攻击方命中；pen<0 → 反之成为加成。
                // 参考射程 ACCURACY_RANGE_REF_METRES 为引擎常量（引擎无既有距离-命中曲线）。仅在此真实命中判定点生效，避免与 HitScopeHelper 重复应用。
                double accuracy = data.getAttributeValue(AttributeRegistry.ACCURACY);
                double pen = StatEngine.magnitudeOf(target, "accuracy_penalty_at_range", Set.of());
                if (pen != 0.0) {
                    double dist = target.distanceTo(living);
                    double frac = Math.min(1.0, dist / ACCURACY_RANGE_REF_METRES);
                    accuracy *= (1.0 - pen * frac);
                }
                hitChance = Math.min(95, getHitChance(
                        data.getAttributeValue(AttributeRegistry.EVASION),
                        Math.max(0.0, accuracy)));
            }

            evasion += hitChance;
            boolean primaryHit = evasion >= 100;
            // 闪避双掷（LuckyStatEffect{EVADE}，受击方持有；熵累计始终按主掷结算，双掷仅在概率空间叠加）：
            //   LUCKY=主掷命中且补掷命中才算命中（更难被打中）；UNLUCKY=主掷命中或补掷命中即算命中（更容易被打中）
            boolean hit = primaryHit;
            LuckyStatEffect.RollMode luck = StatEngine.rollModifier(living, LuckyStatEffect.RollSubject.EVADE);
            if (luck == LuckyStatEffect.RollMode.LUCKY) {
                hit = primaryHit && random.nextInt(100) < hitChance;
            } else if (luck == LuckyStatEffect.RollMode.UNLUCKY) {
                hit = primaryHit || random.nextInt(100) < hitChance;
            }
            // 熵扣减只随主掷：主掷未到 100 时不扣（与无修饰路径完全一致），避免双掷翻转把熵扣成负值
            if (primaryHit) evasion -= 100;
            EvasionEntropy.apply(living, evasion);
            return hit;
        }
        return true;
    }

    public static boolean shouldCheckHitOrBlock(Entity entity, DamageSource source) {
        return !source.is(DamageTypeTagRegistry.IS_AILMENT) &&
                entity instanceof LivingEntity && source.getEntity() instanceof LivingEntity &&
                isAttackSkillSource(source);
    }

    public static int getHitChance(double evasion, double accuracy) {
        return (int) (accuracy / (accuracy + Math.pow(evasion / 4.0, 0.8)) * 100);
    }

    /** 可偷取资源键（与 {@link LeechValue} 存储键、{@link com.cimdy.coe.data.helper.hub.ResourceEventHelper} 索引键同构）。 */
    private static final java.util.List<String> LEECH_RESOURCES = java.util.List.of(
            GateTag.HEALTH, GateTag.MANA, GateTag.ENERGY);

    /**
     * 偷取计算入口：<b>资源键已归一</b>——偷取量不按资源拆分属性，
     * 统一由 {@code LEECH} 单属性 + {资源标签} 作用域承载，由 Stat 条目按标签聚合各资源偷取。
     * 每资源独立走一次流程：在 {资源, leech} 作用域读 {@code LEECH}（通用全伤害偷取）+
     * {资源, leech, physical} 作用域读 {@code PHYSICAL_DAMAGE_LEECHES}（物理专有偷取）。
     * <p>
     * 速率模型（对齐 POE2「总量由伤害决定、速率决定计时」）：
     *   {@code amount}   = 物理专有 + 通用全伤害（经 {@code AMOUNT_LEECHED} 缩放）的总偷取量
     *   {@code ratePerSec}= {@code maxResource * LEECHES_RESPONSE * LEECH_RATE}（每秒恢复流率；
     *                        {@code LEECH_RATE} 统一偷取速率倍率，{@code LEECHES_RESPONSE} 流率基数）
     *   {@code recoverPerTick} = ratePerSec / 20；{@code durationTicks} = ceil(amount / recoverPerTick)
     *
     * @param damages  各类型伤害分额 [物理, 混沌, 火, 冰, 电]
     * @param attacker 攻击方（偷取属性读取端，实时 + 资源标签作用域）
     * @param target   偷取<b>受益人</b>（回复写入 {@code LEECH} 附件的实体；POE2 恒为命中方自身）
     * @param sourceId 本次偷取来源技能标识（{@code null} 表示无技能来源，如 DOT / 环境）
     */
    /**
     * 偷取计算入口：资源键已归一——偷取比率不再由本方法硬编码读取 LEECH 属性，
     * 而由传入的 {@code entry.amount()}（经 ExpressionCodec 表达式系统，如 {@code attribute:coe:leech}）
     * 在 {资源, leeching} / {资源, leeching, physical} 作用域分别求值驱动，与原 readInScopePublic(LEECH) 行为一致。
     *
     * @param entry    触发本次偷取的条目（其 amount 即偷取比率；可为 {@code attribute:coe:leech} 或 {@code $value} 等表达式）
     * @param damages  各类型伤害分额 [物理, 混沌, 火, 冰, 电]
     * @param attacker 攻击方（偷取属性读取端 / 表达式求值端，实时 + 资源标签作用域）
     * @param target   偷取<b>受益人</b>（回复写入 {@code LEECH} 附件的实体；POE2 恒为命中方自身）
     * @param sourceId 本次偷取来源技能标识（{@code null} 表示无技能来源，如 DOT / 环境）
     */
    public static void applyLeechEffects(StatEntry entry, float[] damages, LivingEntity attacker,
                                          LivingEntity target, Identifier sourceId) {
        float totalDamage = 0;
        for (float d : damages) totalDamage += d;
        if (totalDamage <= 0) return;
        float physical = damages[0];
        float maxHealth = target.getMaxHealth();
        if (attacker == null) return;

        // 每资源独立走一次流程（资源键驱动，新增资源仅需在 LEECH_RESOURCES 登记）
        for (String resource : LEECH_RESOURCES) {
            applyLeechResource(entry, resource, totalDamage, physical, maxHealth, attacker, target, sourceId);
        }
    }

    /**
     * 单资源偷取流程：{资源, leech} 作用域经 {@code entry.amount()} 求值 LEECH → 算 amount → 算 rate → 算 duration → 写入 LeechValue。
     * 原表达式 {@code attribute:coe:leech} 在 StatScopes 作用域内求值即等价于原 readInScopePublic(attacker, tags, LEECH)。
     */
    private static void applyLeechResource(StatEntry entry, String resource, float totalDamage, float physical, float maxHealth,
                                           LivingEntity attacker, LivingEntity target, Identifier sourceId) {
        // 通用全伤害偷取比率：在 {资源, leeching} 作用域经 entry.amount() 求值（attribute:coe:leech 等表达式）。
        Set<String> leechTags = Set.of(resource, GateTag.LEECHING);
        double generic = 0;
        try (var s = StatScopes.enterMerged(attacker, StatContext.of(leechTags, null))) {
            generic = entry.evaluateAmount(ExpressionContext.get(attacker));
        } catch (Exception ignored) {
            generic = 0;
        }
        // 物理专有偷取比率：同一表达式在 {资源, leeching, physical} 作用域下读取（物理已用标签区分，不再有独立物理属性）
        double physicalLeech = 0;
        try (var s = StatScopes.enterMerged(attacker,
                StatContext.of(Set.of(resource, GateTag.LEECHING, GateTag.PHYSICAL), null))) {
            physicalLeech = entry.evaluateAmount(ExpressionContext.get(attacker));
        } catch (Exception ignored) {
            physicalLeech = 0;
        }
        float amount = (float) (physicalLeech * physical + generic * totalDamage);
        amount *= (float) readInScopePublic(attacker, Set.of(resource, GateTag.LEECHING, "amount"),
                AttributeRegistry.AMOUNT_LEECHED);
        if (amount <= 0) return;
        // 流率基数（LEECHES_RESPONSE，{资源, leech} 作用域）+ 统一偷取速率倍率（LEECH_RATE，全局）
        double response = readInScopePublic(attacker, leechTags, AttributeRegistry.LEECHES_RESPONSE);
        float ratePerSec = maxHealth * (float) response
                * (float) attacker.getAttributeValue(AttributeRegistry.LEECH_RATE);
        float recoverPerTick = ratePerSec / 20f;
        int durationTicks = Math.max(1, (int) Math.ceil(amount / recoverPerTick));
        LeechValue.addValue(target, resource, recoverPerTick, durationTicks, sourceId);
        // 同伴共享生命偷取（LEECH_SHARE + {companion}，零基标记）：按同一 (每 tick 量, 剩余 tick)
        // 向持有者的同伴各写一份（POE2「你的同伴也获得等量生命偷取」）。
        if (resource.equals(GateTag.HEALTH)
                && StatEngine.gateOf(attacker, "leech_share", Set.of(GateTag.LEECHING, GateTag.COMPANION))) {
            for (LivingEntity companion : RelationCondition.resolveCompanions(attacker)) {
                LeechValue.addValue(companion, resource, recoverPerTick, durationTicks, sourceId);
            }
        }
    }

    public static DamageConvert.MixedDamage getMixedDamage(
            LivingDamageEvent.Pre event, AdditionData data, Entity entity, DamageSource source) {
        float original = event.getOriginalDamage();
        if (entity != null && !source.is(DamageTypeTagRegistry.IS_AILMENT)) {
            Entity directEntity = event.getSource().getDirectEntity();

            // 获取基础攻击力
            original = (float) data.getAttributeValue(Attributes.ATTACK_DAMAGE);

            // 徒手攻击视同持长杖的基础伤害按技能等级（StatIds.UNARMED_ATTACKS_...QUARTERSTAFF...）：
            // 双手皆空（entity = source.getEntity() = 攻击者）且持 UNARMED_QUARTERSTAFF_SKILL_DAMAGE 标记时，
            // 以当前技能等级推导基础物理伤害替换 vanilla 徒手基础伤害（语料无显式每级系数 → 系数 1.0）。
            if (entity instanceof LivingEntity unarmedAttacker
                    && unarmedAttacker.getMainHandItem().isEmpty()
                    && unarmedAttacker.getOffhandItem().isEmpty()
                    && StatEngine.gateOf(unarmedAttacker, "unarmed_quarterstaff_skill_damage", Set.of())) {
                original = (float) (ExpressionContext.get(unarmedAttacker).getSkillLevel() * 1.0);
            }

            // 根据直接伤害来源获取混合伤害
            if (directEntity != null) {
                // 载体自报：技能投射物由 SkillEntity 承载（非 Projectile），
                // 按实体类分支会让它恒落 else（拿不到箭头专属转换）。
                // 改为按 EntityDef.tagSet() 取配方：与 GameEvent.formTagsOf 同一口径。
                if (directEntity instanceof EntityDefCarrier carrier && carrier.def() != null) {
                    Set<String> formTags = carrier.def().tagSet();
                    if (formTags.contains(GateTag.ARROW)) {
                        return DamageConvert.MixedDamage.fromCarrier(directEntity, formTags, original);
                    }
                    return DamageConvert.MixedDamage.from(directEntity, original);
                }
                if (directEntity instanceof Arrow arrow) {
                    return DamageConvert.MixedDamage.from(arrow, original);
                } else if (directEntity instanceof AbstractThrownPotion potion) {
                    return DamageConvert.MixedDamage.from(potion, original);
                } else {
                    return DamageConvert.MixedDamage.from(directEntity, original);
                }
            } else {
                return entity.getData(AttachmentRegistry.ENTITY_DAMAGE_CONVERT).getMixedDamage(original);
            }
        } else {
            return DamageConvert.MixedDamage.from(source, original);
        }
    }

    public static float calculateDamageDeviation(float damage, RandomSource random, double deviation) {
        return calculateDamageDeviation(damage, random, deviation, null, null, Set.of());
    }

    /**
     * 伤害区间偏移（含攻击方 DAMAGE 双掷修饰）：无攻击方（DoT / 环境伤害）时退化为单掷。
     * LUCKY=双掷取较大值（更接近区间上沿），UNLUCKY=双掷取较小值（更接近下沿）。
     *
     * @param damageTags 当前伤害类型的槽位标签（#5 伤害类型分维）：非空时仅匹配类型的幸运修饰参与；
     *                   空集合 = 不按类型过滤（旧 4 参行为）。
     */
    public static float calculateDamageDeviation(float damage, RandomSource random, double deviation,
                                                 @Nullable LivingEntity attacker, @Nullable LivingEntity victim, Set<String> damageTags) {
        if (damage <= 0) return damage;
        // MORE_MAX_LIGHTNING_DAMAGE: first-class StatEffect, raises only the deviation range upper bound (maxMult),
        // not whole lightning damage (DAMAGE attribute channel cannot express "max only"). No attacker (DoT/env) -> 1.0;
        // only when damageTags contains "lightning" does StatEngine.moreMaxLightningDamage return non-zero; other types keep maxMult = 1.0.
        double maxMult = attacker == null ? 1.0 : 1.0 + StatEngine.magnitudeOf(attacker, "more_max_lightning_damage", damageTags == null ? Set.of() : damageTags);
        float first = rollDamageDeviation(damage, random, deviation, maxMult);
        LuckyStatEffect.RollMode luck = attacker == null
                ? null : StatEngine.rollModifier(attacker, LuckyStatEffect.RollSubject.DAMAGE, damageTags);
        // 反向幸运：受害方持有「X% chance for Damage of Enemies that Hit you to be Unlucky」→ 强制攻击方伤害 UNLUCKY。
        // 走独立 VICTIM_DAMAGE 维度（不与受害方进攻性 DAMAGE_LUCKY 碰撞）；仅命中有受害方时生效。
        if (luck != LuckyStatEffect.RollMode.UNLUCKY && victim != null
                && StatEngine.rollModifier(victim, LuckyStatEffect.RollSubject.VICTIM_DAMAGE, damageTags) == LuckyStatEffect.RollMode.UNLUCKY) {
            luck = LuckyStatEffect.RollMode.UNLUCKY;
        }
        if (luck == LuckyStatEffect.RollMode.LUCKY) {
            return Math.max(first, rollDamageDeviation(damage, random, deviation, maxMult));
        }
        if (luck == LuckyStatEffect.RollMode.UNLUCKY) {
            return Math.min(first, rollDamageDeviation(damage, random, deviation, 1.0));
        }
        return first;
    }

    /** 原 5 参（不按伤害类型分维，行为不变）：委托空作用域标签集合、无受害方反向读取。 */
    public static float calculateDamageDeviation(float damage, RandomSource random, double deviation,
                                                 @Nullable LivingEntity attacker, Set<String> damageTags) {
        return calculateDamageDeviation(damage, random, deviation, attacker, null, damageTags);
    }

    /** 原 4 参（不按伤害类型分维，无受害方）：委托 6 参 victim=null。 */
    public static float calculateDamageDeviation(float damage, RandomSource random, double deviation,
                                                 @Nullable LivingEntity attacker) {
        return calculateDamageDeviation(damage, random, deviation, attacker, null, Set.of());
    }

    /** 单次伤害区间掷骰。 */
    private static float rollDamageDeviation(float damage, RandomSource random, double deviation) {
        return rollDamageDeviation(damage, random, deviation, 1.0);
    }

    /**
     * 单次伤害区间掷骰（带上沿倍率）。{@code maxMult} 仅抬升区间<b>上沿</b>（{@code 100 + deviation*100}），
     * 下沿（{@code 100 - deviation}）不变——用于「更多最大闪电伤害」等只提升最大值的修饰。
     * {@code maxMult == 1.0} 时与旧行为逐位等价。上沿须严格大于下沿，否则 {@link RandomSource#nextInt} 抛异常。
     */
    private static float rollDamageDeviation(float damage, RandomSource random, double deviation, double maxMult) {
        int lower = (int) (100 - deviation);
        int upper = (int) ((100 + deviation * 100) * maxMult);
        if (upper <= lower) upper = lower + 1;
        return random.nextInt(lower, upper) * 0.01F * damage;
    }

    /** 暴击伤害归一：CRIT_DAMAGE 单属性 + {attack, crit} 标签作用域（替代 ATTACK_CRIT_DAMAGE）。 */
    public static void applyCriticalDamage(LivingEntity attacker, float[] damages) {
        if (attacker == null) return;
        double critDamage = readInScopePublic(attacker, Set.of(GateTag.ATTACK, GateTag.CRIT), AttributeRegistry.CRIT_DAMAGE);
        for (int i = 0; i < damages.length; i++) {
            damages[i] *= (float) critDamage;
        }
    }

    /** 承暴击伤害倍率归一：TAKEN_CRIT_DAMAGE 单属性 + {taken, crit} 标签作用域（与攻击方 CRIT_DAMAGE 独立维度）。
     *  受害者侧跨实体读取：所有暴击命中（含技能源）均生效——{@code X% increased Critical Damage taken}。
     *  与 {@link #applyCriticalDamage} 拆分：攻击方倍率仅原版攻击路径施加，避免技能源双重乘。 */
    public static void applyTakenCriticalDamage(LivingEntity victim, float[] damages) {
        if (victim == null) return;
        double takenCritDamage = readInScopePublic(victim, Set.of(GateTag.TAKEN, GateTag.CRIT), AttributeRegistry.CRIT_DAMAGE);
        if (takenCritDamage != 1.0) for (int i = 0; i < damages.length; i++) {
            damages[i] *= (float) takenCritDamage;
        }
    }

    public static boolean isFight(LivingEntity living) {
        ExpressionContext ctx = ExpressionContext.get(living);
        return ctx.isTimerActive(EventName.TAKEN_HIT) || ctx.isTimerActive(EventName.CAUSE_DAMAGE);
    }
}
