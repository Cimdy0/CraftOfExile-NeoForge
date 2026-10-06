package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.entity.EntityDef;

import com.cimdy.coe.data.codec.skill.phase.delivery.runtime.executor.area.AreaDeliveryExecutor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.support.stat.engine.StatEngine;
import com.cimdy.coe.data.codec.support.stat.effect.marker.MarkerStatEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 内置行为组件实现 —— 与 {@link EntityMotions} 同构：新增一种行为 = 加一个注册实现 + 数据包写一条。
 *
 * <p>每个行为只回答「<b>何时对谁执行 {@code on_trigger}</b>」，执行什么完全由
 * {@code EntityDef.on_trigger} 数据决定（默认点燃技能阶段的 ON_HIT）。
 *
 * <p>运行态一律经 {@link EntityRuntime#timer(String)} 存取（键由宿主按实例分域），
 * 故同一形态配多个行为、或同一行为配多次，都互不干扰。
 */
public final class EntityBehaviors {

    /** 重布置冷却的计时器键。 */
    private static final String KEY_REARM = "rearm";
    /** 引信剩余的计时器键。 */
    private static final String KEY_FUSE = "fuse";

    private EntityBehaviors() {
    }

    /**
     * 无自主行为（{@code coe:none}）—— 触发由外部系统负责。
     *
     * <p>与 {@link EntityMotions.External} 配对用于原版箭矢：其命中走原版 {@code AbstractArrow}
     * 回调 + 投递的 {@code DeliveryRuntimeContext}，本行为体系不参与。
     */
    public record None() implements EntityBehavior {
        public static final None INSTANCE = new None();
        public static final Codec<None> CODEC = MapCodec.unit(INSTANCE).codec();

        @Override
        public EntityBehavior.Type<?> getType() {
            return EntityBehaviorTypes.NONE;
        }

        @Override
        public boolean tick(EntityRuntime rt) {
            return false;
        }
    }

    /**
     * 接触触发（{@code coe:contact}）—— <b>等目标走进来</b>：每 tick 在作用半径内找目标，
     * 命中即执行 {@code on_trigger}，随后按「是否重新布置」决定保留（进入冷却）还是销毁。
     *
     * @param target          触发对象：{@link EntityBehavior#TARGET_SELF}（施放者本人，残片）/
     *                        {@link EntityBehavior#TARGET_ENEMY}（敌人，危害物）/
     *                        {@link EntityBehavior#TARGET_ANY}（任意生物）
     * @param rearm           触发后是否保留（true = 持续驻场）
     * @param rearmCooldown   保留时的冷却（tick）：防同一目标停留范围内时每 tick 反复触发
     * @param rearmChanceAttr 按属性几率决定是否保留（危害物 {@code hazard_rearm_chance}）
     */
    public record Contact(String target, boolean rearm, int rearmCooldown,
                          Optional<Identifier> rearmChanceAttr) implements EntityBehavior {
        public static final Codec<Contact> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.optionalFieldOf("target", EntityBehavior.TARGET_SELF).forGetter(Contact::target),
                Codec.BOOL.optionalFieldOf("rearm", false).forGetter(Contact::rearm),
                Codec.INT.optionalFieldOf("rearm_cooldown", 20).forGetter(Contact::rearmCooldown),
                Identifier.CODEC.optionalFieldOf("rearm_chance_attr").forGetter(Contact::rearmChanceAttr)
        ).apply(i, Contact::new));

        @Override
        public EntityBehavior.Type<?> getType() {
            return EntityBehaviorTypes.CONTACT;
        }

        @Override
        public boolean tick(EntityRuntime rt) {
            // 重新布置冷却中：完全不判定触发（防同一目标停留范围内时每 tick 反复触发）
            int cooldown = rt.timer(KEY_REARM);
            if (cooldown > 0) {
                rt.timer(KEY_REARM, cooldown - 1);
                return false;
            }
            LivingEntity target = rt.target(rt.radius(), this.target);
            if (target == null) return false;
            rt.fireTriggers(target);
            if (shouldRearm(rt)) {
                rt.timer(KEY_REARM, Math.max(1, rearmCooldown));
                return false;
            }
            return true;
        }

        /** 恒定保留 / 属性几率保留；都不成立则一次性（触发即销毁）。 */
        private boolean shouldRearm(EntityRuntime rt) {
            if (rearm) return true;
            Identifier attr = rearmChanceAttr.orElse(null);
            if (attr == null) return false;
            LivingEntity caster = rt.owner();
            if (caster == null) return false;
            // 几率存属性 + Java 侧掷骰：表达式系统刻意无随机原语（fold 反复求值会让面板值与实际结算不一致）
            double chance = BuiltInRegistries.ATTRIBUTE.get(attr)
                    .map(Holder.Reference::getDelegate)
                    .map(a -> caster.getAttributeValue(a))
                    .orElse(0.0);
            return chance > 0 && caster.getRandom().nextDouble() < chance;
        }
    }

    /**
     * 周期脉冲（{@code coe:pulse}）—— 每 {@code interval} tick 对作用半径内<b>每个</b>目标
     * 各执行一次 {@code on_trigger}。
     *
     * <p><b>这是 Orb 型与球型共用的行为</b>（同行为、不同运动）：
     * <ul>
     *   <li>{@code static + pulse} = <b>Orb</b>：原地驻场，周期放能（POE2 风暴漩涡「每 4 秒发射一次」、
     *       日曜球「每 1.4 秒脉冲一次」）；</li>
     *   <li>{@code ballistic + pulse} = <b>球</b>：边飞边放能（POE2 电球「每 0.2 秒放电一次」）。</li>
     * </ul>
     *
     * <p><b>为什么是「每个目标各一次」</b>：范围伤害在引擎里的口径是「对每个目标各点燃一次 ON_HIT」
     * （同 {@code AreaDeliveryExecutor} 与引爆），而 {@code EntityTriggerContext} 携带单个 target，
     * 故 AoE 在此展开为 N 次触发。
     *
     * <p><b>半径不在本组件</b>：作用半径是<b>运行值</b>（由投递计算，含辅助宝石与范围词缀放大），
     * 经装配写入载体；本组件只读它 —— 在此再声明一个静态半径会立刻变成无人读的死字段。
     * <b>间隔在组件里</b>：节奏是形态自身的属性（电球 0.2s / 风暴漩涡 4s），
     * 但允许投递按技能覆写（同一形态、不同技能不同节奏）。
     *
     * @param interval 脉冲间隔（tick）；&le;0 表示不脉冲
     * @param target   作用对象（敌人 / 施放者 / 任意）
     */
    public record Pulse(int interval, String target) implements EntityBehavior {
        public static final Codec<Pulse> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("interval", 0).forGetter(Pulse::interval),
                Codec.STRING.optionalFieldOf("target", EntityBehavior.TARGET_ENEMY).forGetter(Pulse::target)
        ).apply(i, Pulse::new));

        @Override
        public EntityBehavior.Type<?> getType() {
            return EntityBehaviorTypes.PULSE;
        }

        @Override
        public boolean tick(EntityRuntime rt) {
            // 节奏：投递可覆写（技能层参数），缺省用形态自带的间隔
            double override = rt.param(EntityBehavior.PARAM_PULSE_INTERVAL);
            int every = override > 0 ? (int) override : interval;
            if (every <= 0 || rt.tickCount() % every != 0) return false;

            float radius = rt.radius();
            List<LivingEntity> targets = rt.targets(radius, target);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                rt.fireTriggers(t);
            }
            // 放能表现：仅在确实命中目标时按作用半径发射一次粒子（无目标时不刷，避免空转）
            rt.emitRender(radius);
            return false;
        }
    }

    /**
     * 引信引爆（{@code coe:detonate}）—— 停住（落地/撞墙）后按 {@code fuse} 倒计时，
     * 归零时对半径内每个目标各执行一次 {@code on_trigger} 并销毁。
     *
     * <p><b>为什么引爆属于行为而非运动</b>：运动只管「怎么飞、撞到怎么办」，
     * 「撞到之后要不要炸、多久后炸、炸谁」是另一回事 —— 换成 {@code pulse} 就是飞行球，
     * 换成 {@code contact} 就是落地后等人踩，运动组件一行不动。
     *
     * @param fuse   引信 tick 数（停住后开始递减）
     * @param target 作用对象（敌人 / 施放者 / 任意）
     */
    public record Detonate(int fuse, String target) implements EntityBehavior {
        public static final Codec<Detonate> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("fuse", 10).forGetter(Detonate::fuse),
                Codec.STRING.optionalFieldOf("target", EntityBehavior.TARGET_ENEMY).forGetter(Detonate::target)
        ).apply(i, Detonate::new));

        @Override
        public EntityBehavior.Type<?> getType() {
            return EntityBehaviorTypes.DETONATE;
        }

        /** 装填引信（仅新生成时调用一次；读档由持久化的计时器还原，不重新装填）。 */
        @Override
        public void init(EntityRuntime rt) {
            rt.timer(KEY_FUSE, Math.max(0, fuse));
        }

        @Override
        public boolean tick(EntityRuntime rt) {
            // 未停住（仍在飞行）：只有存活到顶才兜底引爆，防止弹体飞出边界后永驻
            if (!rt.motionStopped()) {
                if (!rt.expired()) return false;
                detonate(rt);
                return true;
            }
            int remaining = rt.timer(KEY_FUSE);
            if (remaining > 0) {
                rt.timer(KEY_FUSE, remaining - 1);
                return false;
            }
            return detonate(rt);
        }

        /** 引爆：对半径内每个目标点燃 {@code on_expire}，并放射一次粒子。
         *  @return {@code true} = 销毁实体；{@code false} = 保留（命中「二次激活」掷骰成功，重装引信后续存）。 */
        private boolean detonate(EntityRuntime rt) {
            float radius = rt.radius();
            for (LivingEntity t : rt.targets(radius, target)) {
                // 引爆走 on_expire 第二通道，与周期脉冲（on_trigger）区分：
                // 同是 phase_on_hit 触发效果，但点燃 ON_EXPIRE 技能阶段动作（如爆炸伤害），
                // 而非 ON_HIT（如曝曬）。未定义 on_expire 时 SkillEntity 会回退 on_trigger，手雷行为不变。
                rt.fireExpire(t);
            }
            // 爆炸表现：引爆瞬间按全半径发射一次粒子（无目标时亦有可见反馈）
            rt.emitRender(radius);
            // 「手雷有 X% 概率二次激活」（GAP-FIX#20）：命中后按 owner 的 INERT 标记掷骰，命中则重装引信续存。
            // 仅在正常引信引爆路径生效；飞行超时兜底路径恒销毁（不授予二次激活，见 tick()）。
            LivingEntity owner = rt.owner();
            if (owner != null) {
                double chance = StatEngine.rollChance(owner, "grenade_second_activation", Set.of());
                if (chance > 0.0 && owner.getRandom().nextDouble() < chance) {
                    rt.timer(KEY_FUSE, Math.max(1, fuse));
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 光环（{@code coe:aura}）—— 每 {@code interval} tick 对<b>自身小半径</b>内每个目标各执行一次
     * {@code on_aura} 通道（通常施加一个持续性状态，如日曜球「1 米内点燃」）。
     *
     * <p><b>与 pulse 的区别</b>：pulse 复用实体的主作用半径（由投递装配、含范围词缀）并点燃 {@code on_trigger}
     * （伤害通道）；aura 自带独立小半径（如 1m）并点燃 {@code on_aura}（状态通道），二者是不同真值的独立机制。
     *
     * <p><b>半径在本组件</b>：aura 半径是「状态施加范围」这一形态属性，与实体主作用半径无关，
     * 故作为数据字段声明（由数据包设定），而非读 {@code rt.radius()} —— 复用主半径会丢失「小半径点燃」语义。
     *
     * @param radius   光环半径（格）：状态施加范围（如 1.0）
     * @param interval 间隔（tick）：&le;0 表示不触发
     * @param target   作用对象
     * @param status   施加的状态 id（须在 StatusDefs 注册）
     * @param duration 状态持续时长（tick）
     */
    public record Aura(double radius, int interval, String target, Identifier status, double duration) implements EntityBehavior {
        public static final Codec<Aura> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("radius", 1.0).forGetter(Aura::radius),
                Codec.INT.optionalFieldOf("interval", 20).forGetter(Aura::interval),
                Codec.STRING.optionalFieldOf("target", EntityBehavior.TARGET_ENEMY).forGetter(Aura::target),
                Identifier.CODEC.fieldOf("status").forGetter(Aura::status),
                Codec.DOUBLE.optionalFieldOf("duration", 1200.0).forGetter(Aura::duration)
        ).apply(i, Aura::new));

        @Override
        public EntityBehavior.Type<?> getType() {
            return EntityBehaviorTypes.AURA;
        }

        @Override
        public boolean tick(EntityRuntime rt) {
            if (interval <= 0 || rt.tickCount() % interval != 0) return false;
            float r = (float) radius;
            List<LivingEntity> targets = rt.targets(r, target);
            if (targets.isEmpty()) return false;
            for (LivingEntity t : targets) {
                rt.fireAura(t);
            }
            rt.emitRender(r);
            return false;
        }
    }
}
