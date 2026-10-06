package com.cimdy.coe.entity;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * 携带「渲染 id + 渲染尺寸」的实体 —— 立牌渲染器（{@code ModelEntityRender}）的最小契约。
 *
 * <p>凡是「由 {@code EntityDef} 装配、外观由 {@code coe:entity_render} 数据决定」的载体
 * （驻场道具 {@code SkillEntity}、投掷物 {@code coe:grenade}、后续任意新载体）都实现本接口，
 * 即可复用同一套客户端渲染器 —— <b>新增一类载体不必再写一个渲染器</b>。
 *
 * <p>只声明渲染真正需要的两项：
 * <ul>
 *   <li>{@link #getRenderId()} —— 服务端同步下来的 {@code coe:entity_render/<name>}；</li>
 *   <li>{@link #getRadius()} —— 把模型层的相对倍率换算成实际边长（范围类实体外观随范围变化）。</li>
 * </ul>
 * {@code EntityDef} 整体不同步到客户端，客户端仅凭 render id 取渲染定义
 * （见 {@code EntityRenderDefs#ensureClientLoaded}）。
 */
public interface RenderIdCarrier {

    /** 渲染定义 id；为空表示无外观（纯粒子或不可见）。 */
    Optional<Identifier> getRenderId();

    /** 渲染基准尺寸（方块）：驻场道具为触发半径，投掷物为引爆半径。 */
    float getRadius();
}
