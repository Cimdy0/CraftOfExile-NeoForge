package com.cimdy.coe.data.codec.entity.behavior;

import com.cimdy.coe.data.codec.entity.EntityDef;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 实体运行时契约 —— 组件（{@link EntityMotion} / {@link EntityBehavior}）<b>能看到的全部世界</b>。
 *
 * <h2>为什么必须有这一层</h2>
 * 「实体 / 行为拆分」要成立，组件就<b>不能认识宿主类</b>：
 * 一旦组件签名里出现宿主实体类，数据 codec 层就反向依赖了实体层，
 * 于是「加一种形态 = 加一份数据」退化为「加一种形态 = 改实体类」—— 拆分名存实亡。
 * 本接口把宿主<b>能提供的能力</b>抽象成一组与形态无关的原语，组件只针对它编程。
 *
 * <h2>能力的三条来源</h2>
 * <ol>
 *   <li><b>形态参数</b>：{@link #radius()} / {@link #boxHeight()} —— 由投递装配，与形态无关；</li>
 *   <li><b>运动原语</b>：{@link #velocity()} / {@link #integrate()} / {@link #blockCollided()} ——
 *       供 {@link EntityMotion} 使用，宿主不解释「这是抛射还是静止」；</li>
 *   <li><b>索敌与效果</b>：{@link #targets(float, String)} / {@link #fireTriggers(LivingEntity)} ——
 *       供 {@link EntityBehavior} 使用，宿主只负责「按 kind 找人、按数据执行效果」。</li>
 * </ol>
 *
 * <h2>运行态：键值化，不设专属字段</h2>
 * 计时器与参数一律走 {@link #timer(String)} / {@link #param(String)} 的<b>键值槽</b>：
 * <ul>
 *   <li>宿主不再持有「引信」「重布置冷却」「脉冲间隔」等只有某一行为会读的字段 ——
 *       那些字段一旦加进宿主，就必然在其余形态上变成无人读的死字段；</li>
 *   <li>键由宿主<b>按组件实例自动分域</b>（见宿主实现），同一行为的多个实例互不踩踏，
 *       组件里只写 {@code rt.timer("fuse")}，不必自己拼前缀；</li>
 *   <li>{@link #motionStopped()} 是唯一保留的<b>具名运动状态</b>：它不是某一运动组件的私有概念，
 *       而是「弹体现在停住了吗」这一通用事实（由运动组件写入、由引信类行为读取）。</li>
 * </ul>
 *
 * <h2>生命周期</h2>
 * 组件<b>不销毁实体</b>，只回答「我完成了吗」：{@code move} / {@code tick} 返回 {@code true}
 * 即由宿主结束本 tick 的后续判定并销毁 —— 销毁权在宿主，组件无权处置宿主。
 *
 * <p><b>刻意不提供 {@code level()} 与 {@code discard()}</b>：世界与销毁都属宿主职权，
 * 组件需要时经 {@link #fireTriggers(LivingEntity)} / {@link #emitRender(float)} 间接使用，
 * 避免把宿主的全部细节开成无人调用的死接口。
 */
public interface EntityRuntime {

    /** 作用半径（格）：由投递装配（含辅助宝石与范围词缀放大）。 */
    float radius();

    /** 包围盒高度（方块）：形态数据给出。 */
    float boxHeight();

    /** 已存活 tick 数（节奏类行为据此取模）。 */
    int tickCount();

    /** 存活是否到顶（tick）；引信类行为据此兜底引爆，避免弹体永驻。 */
    boolean expired();

    /** 施放者（owner）；重载后可能为 null。 */
    @Nullable
    LivingEntity owner();

    // ───────────── 运动原语（供 EntityMotion） ─────────────

    Vec3 velocity();

    void velocity(Vec3 v);

    /** 按当前速度推进一 tick 并解析<b>方块</b>碰撞（实体一律穿过）。 */
    void integrate();

    /** 本 tick 是否撞到方块（落地 / 撞墙）。 */
    boolean blockCollided();

    /** 运动是否已停止（落地 / 撞墙后停住）—— 由运动组件置位，供引信类行为读取。 */
    boolean motionStopped();

    void motionStopped(boolean stopped);

    // ───────────── 索敌与效果（供 EntityBehavior） ─────────────

    /**
     * 范围内<b>全部</b>合目标（AoE 口径：对每个目标各生效一次）。
     *
     * @param kind {@link EntityBehavior#TARGET_SELF} / {@link EntityBehavior#TARGET_ENEMY} / {@code any}
     */
    List<LivingEntity> targets(float radius, String kind);

    /** 范围内<b>最近</b>的一个合目标（接触触发口径：一次只触发一个）。 */
    @Nullable
    LivingEntity target(float radius, String kind);

    /** 对一个目标执行 {@code EntityDef.on_trigger} 的全部效果（周期 / 接触等常规触发）。 */
    void fireTriggers(LivingEntity target);

    /**
     * 对一个目标执行 {@code EntityDef.on_expire} 的全部效果（实体<b>到期 / 引爆</b>时的第二效果通道）。
     *
     * <p>与 {@link #fireTriggers} 相互独立：{@code pulse ⊕ detonate} 这类复合形态中，
     * 周期脉冲走 {@code on_trigger}（如施加曝曬），引爆走 {@code on_expire}（如爆炸伤害），
     * 二者可点燃<b>不同的</b>技能阶段动作（{@code ON_HIT} vs {@code ON_EXPIRE}）。
     * 若实体未定义 {@code on_expire}，宿主应回退到 {@code on_trigger}，以兼容
     * 仅有单一效果通道的既有形态（如手雷：引爆即唯一致命效果）。
     */
    void fireExpire(LivingEntity target);

    /**
     * 对一个目标执行 {@code EntityDef.onAura()} 的全部效果（持续性光环 / 范围状态施加通道）。
     *
     * <p>与 {@link #fireTriggers} / {@link #fireExpire} 相互独立：{@code aura} 行为在其自身小半径内
     * 周期性地对目标施加状态（如日曜球「1 米内点燃」），不占用脉冲 / 引爆的常规通道。
     */
    void fireAura(LivingEntity target);

    /** 按渲染定义的粒子层发射一次表现。 */
    void emitRender(float radius);

    // ───────────── 通用键值运行态 ─────────────

    /** 本组件的计时器（tick）：语义由组件自行解释（引信剩余 / 重布置冷却）。 */
    int timer(String key);

    void timer(String key, int ticks);

    /** 本组件的运行参数（0 = 未设置）：如脉冲间隔覆写（由投递注入的技能层节奏）。 */
    double param(String key);
}
