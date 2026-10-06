package com.cimdy.coe.entity.custom.skill.render;

import com.cimdy.coe.data.codec.entity.render.EntityRenderDefs;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer;
import com.cimdy.coe.data.codec.entity.render.EntityRenderLayer.ModelDef;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * 立牌渲染器的客户端渲染状态（驻场道具 / 投掷物 / 后续任意 {@code RenderIdCarrier} 通用）。
 *
 * <p>只携带<b>渲染真正需要</b>的三项：渲染基准尺寸（用于把模型的相对倍率换算成实际边长）、
 * 累计自转角、以及已解析的模型层定义。{@code EntityDef} 整体不同步到客户端 ——
 * 客户端仅凭同步下来的 render id 取渲染定义（见 {@code EntityRenderDefs#ensureClientLoaded}）。
 *
 * <p>模型层在 {@code extractRenderState} 阶段一次性解析好，避免每帧重复查表。
 */
public class ModelEntityRenderState extends EntityRenderState {

    /** 渲染基准尺寸（方块），用于把 {@link EntityRenderLayer.ModelDef#scale()} 倍数换算为实际边长。 */
    public float radius = 1.5F;
    /** 累计自转角（度），含 partialTick 以消除自转的帧间跳动。 */
    public float spinDegrees = 0.0F;
    /** 已解析的模型层；无模型层（纯粒子外观）时为 null。 */
    @Nullable
    public ModelDef model;
    /**
     * 技能皮肤 / 渲染定义染色（ARGB）；为 null 表示无染色覆盖（沿用贴图原色）。
     * 取值优先皮肤 {@code SkillSkinDef.tint}（按技能 id 锁定），其次回落 {@link ModelDef#tint()}
     * （按渲染定义）。见 {@code ModelEntityRender#extractRenderState}。
     */
    @Nullable
    public Integer tint;
}
