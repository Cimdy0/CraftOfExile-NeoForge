package com.cimdy.coe.entity.custom.skill.render;

import com.cimdy.coe.entity.custom.skill.SkillEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * {@code coe:prop} / {@code coe:orb} / {@code coe:grenade} / {@code coe:skill_ball} 共用的渲染器。
 *
 * <p><b>为什么只有一个渲染器</b>：这些类型共用同一个注册基类 {@link SkillEntity}，
 * 外观全部由 {@code coe:entity_render} 数据决定（同步的 render id → 模型层），
 * 故绘制逻辑无需按类型分叉 —— 新增一个物件类型只加注册与数据，<b>零渲染代码</b>。
 *
 * <p>绘制本体见泛型基类 {@link ModelEntityRender}（贴图立牌 + 自转 + 按半径缩放）。
 */
public class SkillEntityRender extends ModelEntityRender<SkillEntity> {

    public SkillEntityRender(EntityRendererProvider.Context context) {
        super(context);
    }
}
