package com.cimdy.coe.data.codec.support.status.runtime;

import com.cimdy.coe.data.codec.support.status.def.StatusType;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.Nullable;

/**
     * P2-3 异常转存通道（store/consume）。
     *
     * <p>在既有 ailment 消费出口（{@code StatusEngine.consumeAilment}）上挂载的<b>轻量记账</b>层：
     * 被消费的异常数值（强度/剩余时长/层数）经 {@link #store} 写入实体的 {@link TransferStore} 附件，
     * 供后续 store / consume 消费方（blood_hunt / elemental_sundering 等 POE2「Consume X」族）取用。本类<b>只打通通道</b>，
 * 不实现任何具体技能的转存效果（如血爆、元素爆炸），消费方须自行调用本通道读写。
 * <p>注：iron_ward（储存被护甲减免的物理伤害 → 尖刺新星）与 plague_bearer（储存自身造成的中毒伤害
 * → 释放）在 POE2 中与「消耗异常」无关，<b>不是</b>本通道的消费方（旧注释归属有误，已按语料库更正）。
 *
 * <p>设计约束（来自任务）：
 * <ul>
 *   <li>不新建独立事件总线（AilmentBus）；仅在消费侧挂转存回调，复用既有 Attachment 注册方式；</li>
 *   <li>不破坏 {@code AilmentHelper} 作为「唯一施加入口」的约束——本通道只<b>记账</b>，不施加状态；</li>
 *   <li>纯中转，绝不影响 {@code consumeAilment} 既有消费行为（重施逻辑保持不变）。</li>
 * </ul>
 */
public final class AilmentTransfer {

    private AilmentTransfer() {}

    /** 取/建 holder 的 {@link TransferStore} 附件（复用 AttachmentRegistry.TRANSFER_STORE，非同步风格）。 */
    private static TransferStore store(LivingEntity holder) {
        TransferStore s = holder.getData(AttachmentRegistry.TRANSFER_STORE);
        if (s == TransferStore.EMPTY) {
            s = new TransferStore();
            holder.setData(AttachmentRegistry.TRANSFER_STORE, s);
        }
        return s;
    }

    /**
     * 转存记账（store）：把被消费的异常数值记录到 holder 的 TransferStore（覆盖式写入，
     * 同一异常仅保留最新一次消费记录）。这是 P2-3 通道的唯一写入点，由
     * {@code StatusEngine.consumeAilment} 在 clear 之后挂载。
     *
     * @param holder    记账载体（通常为消耗方 consumer；无消耗方时由调用方退化为被消费目标）
     * @param type      被消费的异常类型
     * @param intensity 强度（由 consumeAilment 取出的 int/四舍五入后的 magnitude）
     * @param remaining 剩余时长（tick）
     * @param stacks    层数
     */
    public static void store(LivingEntity holder, StatusType type, int intensity, int remaining, int stacks) {
        TransferStore s = store(holder);
        s.put(new TransferStore.TransferRecord(type, intensity, remaining, stacks));
    }

    /** 是否存在某异常的转存记录（不消费）。 */
    public static boolean has(LivingEntity holder, StatusType type) {
        return store(holder).has(type);
    }


    /**
     * 取回并清除一条转存记录（一次性消费语义）：返回 null 表示无记录。
     * 供消费方「取走」记账数值后做后续处理（如转护甲 / 扩散）。
     */
    public static TransferStore.@Nullable TransferRecord consume(LivingEntity holder, StatusType type) {
        TransferStore s = store(holder);
        TransferStore.TransferRecord r = s.get(type);
        if (r != null) s.remove(type);
        return r;
    }

}
