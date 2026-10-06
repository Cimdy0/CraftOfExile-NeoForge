package com.cimdy.coe.data.helper.combat;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.projectile.ProjectileDeliveryExecutor;
import com.cimdy.coe.event.combat.DamageEvent;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.registry.DamageTypeRegistry;
import com.cimdy.coe.registry.DamageTypeTagRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.context.DamageContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.SkillPhaseInstanceTracker;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.context.SkillDamageSource;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.EntityFlagValue;
import com.cimdy.coe.data.codec.support.stat.engine.StatContext;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.engine.StatScopes;
import com.cimdy.coe.data.codec.support.stat.tag.GateTag;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.entity.custom.skill.totem.BallistaTotem;
import com.cimdy.coe.entity.custom.skill.totem.TrapEntity;
import com.cimdy.coe.entity.custom.skill.totem.MineEntity;
import com.cimdy.coe.event.EventName;
import com.cimdy.coe.event.combat.GameEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;

/**
 * 命中作用域机制：由伤害源推导本次命中的作用域标签，并把同一 {@link StatContext} 推入受害者与攻击者实体。
 *
 * <p>事件类只做派发，标签推导与作用域入栈属机制，收口于本类（{@code DamageEvent} 仅调用）。
 */
public final class HitScopeHelper {
    private HitScopeHelper() {}

    /**
     * 进入一次命中的 stat 作用域：同时在受害者与攻击者实体上 push 同一 {@link StatContext}，
     * 使本次命中内所有属性读取（进攻属性走攻击者快照、防御/状态属性走受害者实时读取）都按命中标签即时求值。
     * 返回合并的 {@link AutoCloseable}，try-with-resources 退出时按逆序出栈。
     */
    public static HitScope enterHitScopes(LivingEntity victim, LivingEntity attacker, StatContext ctx) {
        List<AutoCloseable> closeables = new ArrayList<>();
        // 双方作用域互持对方引用（交互对方，见 StatContext.sourceEntity）：
        // · 受害者持攻击者 → 受害者读取自身属性时聚合攻击者的 target（敌方维度）条目（如「被攻击的敌人抗性降低」）；
        // · 攻击者持受害者 → 攻击者条目上 conditionTarget=TRIGGER_TARGET 的条件按受害者求值
        //   （如「对冰缓/冰冻/感电敌人的伤害提高」经 status:<id> 条件节点读取受害者 StatusEngine 状态），
        //   并对称聚合受害者的 target 条目（如「攻击你的敌人伤害降低」）。
        StatContext victimCtx = (attacker != null && attacker != victim) ? ctx.withSourceEntity(attacker) : ctx;
        // 受害者承伤作用域注入受害者自身主语标签（MINION/TOTEM/ALLY/COMPANION）
        // （仅受害者侧，避免误命中施动方 EFF）。
        closeables.add(StatScopes.enter(victim, mergeWithCurrent(victim, withVictimSubjectTags(victimCtx, victim))));
        if (attacker != null && attacker != victim) {
            closeables.add(StatScopes.enter(attacker, mergeWithCurrent(attacker, ctx.withSourceEntity(victim))));
        }
        return new HitScope(closeables);
    }

    /** 返回并入受害者自身主语标签（MINION/TOTEM/ALLY/COMPANION）的副本（不污染攻击者作用域）。 */
    private static StatContext withVictimSubjectTags(StatContext ctx, LivingEntity victim) {
        if (victim == null) return ctx;
        Set<String> tags = new HashSet<>(ctx.tags());
        addSubjectTags(tags, victim);
        return new StatContext(tags, ctx.skill(), ctx.sourceEntity(), ctx.eventName(), ctx.hitContext());
    }

    /**
     * 命中作用域在推入外层（引导技能 / 技能动作）作用域之上时，需并入外层标签与技能标识，
     * 否则会遮蔽外层的 {@link GateTag#CHANNELING} / 技能名等标签。
     * 统一收敛于 {@link StatScopes#mergeWithCurrent}。
     */
    private static StatContext mergeWithCurrent(LivingEntity living, StatContext ctx) {
        return StatScopes.mergeWithCurrent(living, ctx);
    }

    /**
     * 由伤害源推导本次命中的作用域标签。元素标签需伤害组成算出后才能确定（damages 为 null 时跳过）。
     *
     * <p>标签语义：
     * <ul>
     *   <li>{@code attack}：实体来源的非异常进攻行为；{@code spell}：来自技能（技能源）的行为。</li>
     *   <li>武器标签：按攻击者主手物品推断（bow/crossbow/spear/mace + melee/ranged），直接取物品名、无前缀。</li>
     *   <li>命中类型：抛射物→{@code projectile}，技能源→{@code spell}，其余→{@code melee}。</li>
     *   <li>元素标签：由各伤害分量 &gt; 0 决定（physical/chaos/fire/cold/lightning，各自独立共存）。</li>
     * </ul>
     */
    public static Set<String> buildHitTags(DamageSource source, LivingEntity attacker, Entity directEntity, float[] damages) {
        Set<String> tags = new HashSet<>();
        // 主语路由（MINION/TOTEM/ALLY/COMPANION）：按攻击者实际主语分类注入作用域标签。
        addSubjectTags(tags, attacker);
        if (!source.is(DamageTypeTagRegistry.IS_AILMENT)) {
            tags.add(GateTag.ATTACK);
            // 被动捕获标签：本次「被命中」——在承伤前推入受害者作用域（fold 通道）。
            tags.add(EventName.TAKEN_HIT);
            addWeaponTags(tags, attacker);
            // 攻击形态分类（近战 / 投射物 / 具体投射物种）收口到 GameEvent.formTagsOf（单标签形式）。
            tags.addAll(GameEvent.formTagsOf(source, directEntity));
            addDeliveryTags(tags, source, attacker);
            // 命中侧并入技能标签（与技能行为侧同源）：技能名 + 其完整 stat 标签集。
            // 原仅并入「技能名 + spell 门槛」——使以 grenade/hazard/detonator/storm/plant/command 等
            // 技能类别标签门控的伤害/效果，在「异步投射物·范围命中」时恒不匹配（命中时已无技能作用域，
            // 见 ProjectileDeliveryExecutor L98-99「SkillArrow 命中时 已无技能作用域」）。
            // 镜像 SkillStatScopes.enterAction（L68-74）以「技能完整 getStatTags()」为基底：
            // 同步施放时经 enterAction + mergeWithCurrent 已含这些标签（本处被 Set 去重），异步时由本处补全，
            // 使命中读点与同步施放同源、不丢失任何技能级标签。纯加性、Set 去重，与同步路径完全一致，无回归。
            if (source instanceof SkillDamageSource sds && sds.getSkill() != null) {
                SkillInstance si = sds.getSkill();
                tags.add(si.getSkillId().getPath());  // 技能名（数据侧直接写该名即可命中）
                tags.addAll(si.getSkill().getStatTags(si.getContext(), si.getSkillId()));  // 完整技能标签（含技能类别标签 grenade/hazard/…）
            }
        } else {
            // 异常状态（含持续伤害）多重分类标签。
            tags.add(GateTag.AILMENT);
            tags.add(GateTag.DAMAGE_OVER_TIME);
            if (source instanceof SkillDamageSource sds && sds.getAilmentTag() != null) tags.add(sds.getAilmentTag());
            // 通过注册表把 source.type()（原始 DamageType）解析为 ResourceKey，按「点燃/流血/中毒/元素」细分分类
            if (directEntity != null) {
                var reg = directEntity.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
                DamageType srcType = source.type();
                if (reg.getOrThrow(DamageTypeRegistry.IGNITE).value() == srcType) {
                    tags.add(GateTag.IGNITE);
                    tags.add(GateTag.ELEMENTAL);
                    tags.add(GateTag.FIRE);
                } else if (reg.getOrThrow(DamageTypeRegistry.BLEEDING).value() == srcType) {
                    tags.add(GateTag.BLEED);
                    tags.add(GateTag.PHYSICAL);
                } else if (reg.getOrThrow(DamageTypeRegistry.POISON).value() == srcType) {
                    tags.add(GateTag.POISON);
                    tags.add(GateTag.CHAOS);
                } else if (reg.getOrThrow(DamageTypeRegistry.DOT_FIRE).value() == srcType) {
                    tags.add(GateTag.ELEMENTAL);
                    tags.add(GateTag.FIRE);
                } else if (reg.getOrThrow(DamageTypeRegistry.DOT_COLD).value() == srcType) {
                    tags.add(GateTag.ELEMENTAL);
                    tags.add(GateTag.COLD);
                } else if (reg.getOrThrow(DamageTypeRegistry.DOT_LIGHT).value() == srcType) {
                    tags.add(GateTag.ELEMENTAL);
                    tags.add(GateTag.LIGHTNING);
                } else if (reg.getOrThrow(DamageTypeRegistry.DOT_CHAOS).value() == srcType) {
                    tags.add(GateTag.CHAOS);
                } else if (reg.getOrThrow(DamageTypeRegistry.DOT_PHYSICAL).value() == srcType) {
                    tags.add(GateTag.PHYSICAL);
                }
            }
        }
        if (damages != null) {
            if (damages[0] > 0) tags.add(GateTag.PHYSICAL);
            if (damages[1] > 0) tags.add(GateTag.CHAOS);
            if (damages[2] > 0) tags.add(GateTag.FIRE);
            if (damages[3] > 0) tags.add(GateTag.COLD);
            if (damages[4] > 0) tags.add(GateTag.LIGHTNING);
        }
        return tags;
    }

    /**
     * 主语路由：按攻击者实体身份注入作用域标签，供「随从/图腾/盟友/伙伴的 X 提高」类加成命中。
     *
     * <p>判定信号全部取自既有、权威的实体标记（不发明新状态）：{@code SkillTotem} → TOTEM；
     * {@code IS_COMPANION} → COMPANION(+MINION)；{@code SUMMON_KEY} 持久标记 → MINION；{@code ALLY} → ALLY。
     * <b>不可</b>用 {@code OWNER_UUID} 判 minion —— 工人实体写有随机 OWNER_UUID，会误判。
     */
    public static void addSubjectTags(Set<String> tags, LivingEntity attacker) {
        if (attacker == null) return;
        if (attacker instanceof SkillTotem) {
            // MINE 地雷是独立主语：仅注入 mine 标签，不挂 totem/ballista/trap（与陷阱/箭塔区分，避免误命中彼此 SINGLE 效果）
            if (attacker instanceof MineEntity) {
                tags.add(GateTag.MINE);
                return;
            }
            tags.add(GateTag.TOTEM);
            // BALLISTA 仅由箭塔实体注入：普通图腾（SkillTotem 但非 BallistaTotem）不具此标签，
            // 故 3 条 SINGLE-{ballista} 效果（crit_chance/crit_damage/ailment_buildup）不会误命中普通图腾。
            if (attacker instanceof BallistaTotem) tags.add(GateTag.BALLISTA);
            // TRAP 仅由陷阱实体注入：普通图腾（SkillTotem 但非 TrapEntity）不具此标签，
            // 故 3 条 SINGLE-{trap} 效果（damage/crit_chance/cast_speed）不会误命中普通图腾/箭塔。
            if (attacker instanceof TrapEntity) tags.add(GateTag.TRAP);
            return;
        }
        if (AdditionData.getOrDefault(attacker, AdditionKeys.IS_COMPANION, 0.0) != 0.0) {
            tags.add(GateTag.COMPANION);
            tags.add(GateTag.MINION);
            return;
        }
        if (AdditionData.getOrDefault(attacker, AdditionKeys.SUMMONED, 0.0) != 0.0) {
            tags.add(GateTag.MINION);
            return;
        }
        if (AdditionData.getOrDefault(attacker, AdditionKeys.ALLY, 0.0) != 0.0) {
            tags.add(GateTag.ALLY);
        }
        // 地雷 / 魔像 / 烙印：技能生成对应实体时写入 AdditionKeys 标记，此处按标记发射作用域标签，
        // 使「地雷 / 魔像 / 烙印的 X 提高」「其命中触发」类词缀可经数据驱动命中（实体类型未注册时恒为空，不误命中）。
        if (AdditionData.getOrDefault(attacker, AdditionKeys.MINE, 0.0) != 0.0) {
            tags.add(GateTag.MINE);
            return;
        }
        if (AdditionData.getOrDefault(attacker, AdditionKeys.GOLEM, 0.0) != 0.0) {
            tags.add(GateTag.GOLEM);
            return;
        }
        if (AdditionData.getOrDefault(attacker, AdditionKeys.BRAND, 0.0) != 0.0) {
            tags.add(GateTag.BRAND);
            return;
        }
    }

    /**
     * 命中侧并入投放形态标签：技能伤害源携带 phaseId 时，反查该技能阶段的 {@code Delivery}，
     * 把其自述标签（projectile / area / beam / aura / self）加入本次命中作用域。
     */
    private static void addDeliveryTags(Set<String> tags, DamageSource source, LivingEntity attacker) {
        if (attacker == null) return;
        if (!(source instanceof SkillDamageSource sds) || sds.getPhaseId() == null) return;
        SkillPhaseInstance inst = SkillPhaseInstanceTracker.find(attacker.getUUID(), sds.getPhaseId());
        if (inst != null && inst.getPhase().delivery().isPresent()) {
            tags.addAll(inst.getPhase().delivery().get().statTags());
        }
    }

    /** 按攻击者主手物品推断武器作用域标签（最佳努力）。标签即物品名，无前缀。 */
    private static void addWeaponTags(Set<String> tags, LivingEntity attacker) {
        if (attacker == null) return;
        var weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            // 「双手皆空时，视同持长杖攻击」(node 64601)：空手 + 副手亦空 + 持有 INERT 标记 → 追加 quarterstaff 武器标签。
            if (attacker.getOffhandItem().isEmpty()
                    && StatEngine.collectGateTags(attacker, "quarterstaff_both_hands_empty").contains("quarterstaff")) {
                tags.add("quarterstaff");
            }
            tags.add("melee");
            return;
        }
        // 武器族标签一律由「类型链 + 族别名」推导（ItemWeight 声明 → 缓存入 EquipmentState.types）。
        // 旧写法混用 vanilla instanceof / 物品标签：CoE 载体物品既非 vanilla 子类、标签亦常为空，
        // 导致 spear / mace / dagger 等标签恒不发射（静默死接线）。
        EquipmentState itemState =ModifiableStateOps.equipment(weapon);
        if (!itemState.types().isEmpty()) {
            for (String family : ItemTypeChecker.WEAPON_FAMILIES) {
                if (ItemTypeChecker.hasType(itemState, family)) tags.add(family);
            }
            // 单手 / 双手：直接读类型链（ONE_HAND / TWO_HAND）
            if (ItemTypeHierarchy.isWithin(EquipmentGen.id(EquipmentGen.ONE_HAND), itemState.types())) tags.add(GateTag.ONE_HANDED);
            if (ItemTypeHierarchy.isWithin(EquipmentGen.id(EquipmentGen.TWO_HAND), itemState.types())) tags.add(GateTag.TWO_HANDED);
        } else {
            // 兜底：无类型的非 CoE 物品（如原版弓 / 弩 / 三叉戟）仍按 vanilla 形态推断
            if (weapon.getItem() instanceof BowItem) {
                tags.add(GateTag.BOW);
            } else if (weapon.getItem() instanceof CrossbowItem) {
                tags.add(GateTag.CROSSBOW);
            } else if (weapon.getItem() instanceof TridentItem) {
                tags.add("spear");
            } else if (weapon.getItem() instanceof MaceItem) {
                tags.add(GateTag.MACE);
            }
        }
        if (weapon.is(Tags.Items.RANGED_WEAPON_TOOLS)) {
            tags.add("ranged");
        } else {
            tags.add("melee");
        }
    }

    /**
     * 无敌检查（{@code EntityInvulnerabilityCheckEvent}）机制：在本次命中作用域内判定「是否命中」，
     * 未命中则标记无敌并缓存 {@link DamageContext}。事件端只传参调用。
     */
    public static void resolveInvulnerability(Entity entity, ServerLevel serverLevel, DamageSource source, RandomSource random) {
        LivingEntity victim = entity instanceof LivingEntity l ? l : null;
        LivingEntity attacker = source.getEntity() instanceof LivingEntity l ? l : null;
        if (victim == null) return;
        Set<String> tags = buildHitTags(source, attacker, source.getDirectEntity(), null);
        try (var scope = enterHitScopes(victim, attacker, StatContext.of(tags, SkillDamageSource.skillIdOf(source)))) {
            boolean isHit = DamageHelper.isHit(entity, source, random);
            entity.setInvulnerable(!isHit);
            entity.invulnerableTime = isHit ? 0 : 20;
            if (entity instanceof LivingEntity target) {
                if (source.getEntity() instanceof LivingEntity living) {
                    DamageContext.cacheDamageContext(source, living, new DamageContext()
                            .Hit(new EntityHitResult(target)).Evasion(!isHit));
                }
            }
        }
    }

    /**
     * 承伤事件（{@code LivingIncomingDamageEvent}）机制：进入本次命中作用域，供后续伤害结算的
     * Stat 条目即时求值（本作用域内不读值）。
     */
    public static void enterIncomingScope(LivingEntity living, DamageSource source) {
        LivingEntity attacker = source.getEntity() instanceof LivingEntity l ? l : null;
        Set<String> tags = buildHitTags(source, attacker, source.getDirectEntity(), null);
        try (var scope = enterHitScopes(living, attacker, StatContext.of(tags, SkillDamageSource.skillIdOf(source)))) {
            // 空作用域保持：作用域激活本身用于支撑后续结算，这里不执行属性读值。
        }
    }

    /** 合并多个实体作用域的 AutoCloseable；close() 不抛受检异常，便于 try-with-resources 直接使用。 */
    public static final class HitScope implements AutoCloseable {
        private final List<AutoCloseable> closeables;

        HitScope(List<AutoCloseable> closeables) {
            this.closeables = closeables;
        }

        @Override
        public void close() {
            for (int i = closeables.size() - 1; i >= 0; i--) {
                try {
                    closeables.get(i).close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
