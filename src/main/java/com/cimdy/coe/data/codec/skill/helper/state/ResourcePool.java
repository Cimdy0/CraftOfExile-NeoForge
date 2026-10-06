package com.cimdy.coe.data.codec.skill.helper.state;

import net.minecraft.world.entity.LivingEntity;

/**
 * 资源池统一接口 —— 抽取「实体级、按 id 分槽、可生成/消耗/衰减」资源槽位的共性，
 * 供中央注册表（ResourceRegistry）统一调度（集中 tick、通用 current/max、生成/消耗校验）。
 *
 * <p>设计约束（严禁强行统一不兼容形态）：
 * <ul>
 *   <li>{@code get} 返回资源<b>当前量</b>（double）。Combo 的富数据由其自身公开 API 提供，适配层仅取 count 作为资源量。</li>
 *   <li>{@code generate} 上限（cap）为<b>可选</b>：传 {@code null} 表示由实现自行决定上限（如 Glory 回落全局 CAP）；传非 null 则实现应尊重该上限。</li>
 *   <li>{@code consume} 返回<b>实际消耗量</b>（double）。</li>
 *   <li>{@code clear} 重置为 0。</li>
 *   <li>{@code tick} 维护衰减/自动重置；无维护语义的实现可为 no-op。</li>
 * </ul>
 *
 * <p>各 State 通过 {@code asResourcePool()} 工厂以<b>适配方式</b>提供本视图，<b>不修改</b>其现有
 * 公开 API 与行为语义（富事件回调等仍由各自真实方法承载）。
 */
public interface ResourcePool {
    /** 读取资源当前量（无记录返回 0）。 */
    double get(LivingEntity living, String id);

    /** 生成资源：增加 increment，封顶尊重 cap（null = 实现自定上限），重置维护计时。 */
    void generate(LivingEntity living, String id, double increment, Double cap, long gameTime);

    /** 消耗资源，返回实际消耗量（double）。 */
    double consume(LivingEntity living, String id, double amount);

    /** 清空资源（重置为 0）。 */
    void clear(LivingEntity living, String id);

    /** 每 tick 维护：衰减、自动重置等（无维护语义的实现可为 no-op）。 */
    void tick(LivingEntity living, long gameTime);
}
