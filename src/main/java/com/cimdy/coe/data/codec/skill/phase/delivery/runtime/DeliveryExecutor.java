package com.cimdy.coe.data.codec.skill.phase.delivery.runtime;

import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.delivery.Delivery;

import java.util.UUID;

/**
 * Delivery 的运行时执行器。
 * 将 Delivery 数据记录转化为实际游戏效果（spawn entity / 区域搜索 / 射线检测）。
 */
public interface DeliveryExecutor<D extends Delivery> {
    void execute(D delivery, ActionContext context, UUID casterId, UUID phaseId, SkillPhaseInstance instance);
}
