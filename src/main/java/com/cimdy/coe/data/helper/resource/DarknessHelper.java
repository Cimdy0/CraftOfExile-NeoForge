package com.cimdy.coe.data.helper.resource;

import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.value.Value;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑暗（Darkness）资源（41076 Embrace the Darkness，Acolyte of Chayula 升阶）：
 * 独立承伤预留资源——启用后（{@code NoSpiritBaseDarkness} 属性 >1.0）：
 * <ul>
 *   <li>上限 {@code getMaxDarkness} = (100 + 10×等级) × MAX_DARKNESS 倍率（Base Maximum Darkness is 100 /
 *       +10 to Maximum Darkness per Level）；</li>
 *   <li>承伤时伤害<b>先从 Darkness 预留</b>（{@link #reserve}）：池值减少、登记返还，
 *       溢出部分才扣生命/能量护盾（Damage taken is Reserved from Darkness before being taken from Life
 *       or Energy Shield）；</li>
 *   <li>预留按 {@code DarknessReservationLasts}（默认 5 秒）逐 tick 全额返还（Darkness Reservation lasts
 *       for 5 seconds），返还经 {@link AttributeEvent} 每 tick 驱动（{@link #tickReservation}）。</li>
 * </ul>
 */
public class DarknessHelper {

    /** Base Maximum Darkness is 100。 */
    public static final float BASE_MAX_DARKNESS = 100.0F;
    /** +10 to Maximum Darkness per Level。 */
    public static final float DARKNESS_PER_LEVEL = 10.0F;
    /** Darkness Reservation lasts for 5 seconds（秒）。 */
    public static final float BASE_RESERVATION_SECONDS = 5.0F;

    /** 启用开关：原 NoSpiritBaseDarkness 功能属性已按注册纪律移除（功能性开关不建 FOLD 属性），默认关闭。 */
    public static boolean isDarknessEnabled(LivingEntity living) {
        return false;
    }

    /** 当前等级（LEVEL 附件，缺失回退 1）。 */
    public static int levelOf(LivingEntity living) {
        return living.hasData(AttachmentRegistry.LEVEL) ? living.getData(AttachmentRegistry.LEVEL) : 1;
    }

    /** 最大黑暗值：(100 + 10×等级) × MAX_DARKNESS 倍率。 */
    public static float getMaxDarkness(LivingEntity living) {
        return (BASE_MAX_DARKNESS + DARKNESS_PER_LEVEL * levelOf(living))
                * (float) living.getAttributeValue(AttributeRegistry.MAX_DARKNESS);
    }

    /** 当前黑暗值（未启用恒 0）。 */
    public static float getDarkness(LivingEntity living) {
        return living.getData(AttachmentRegistry.DARKNESS);
    }

    /** 直接设置当前黑暗值（clamp 到 [0, max]）。 */
    public static void setDarkness(LivingEntity living, float amount) {
        living.setData(AttachmentRegistry.DARKNESS, Math.max(0, Math.min(amount, getMaxDarkness(living))));
    }

    /** 满则跳过返还（与 Recoup 的 canRecover 门控一致）。 */
    public static boolean isFullDarkness(LivingEntity living) {
        return getDarkness(living) >= getMaxDarkness(living);
    }

    /**
     * 承伤预留：把 {@code amount} 从 Darkness 池「预留」（池值立即减少、登记按时长返还），
     * 返回实际预留量（≤ amount，池不足时只预留剩余）。调用方须把返回值从承伤中扣除。
     */
    public static float reserve(LivingEntity living, float amount) {
        if (!isDarknessEnabled(living) || amount <= 0) return 0;
        float darkness = getDarkness(living);
        if (darkness <= 0) return 0;
        float reserved = Math.min(darkness, amount);
        setDarkness(living, darkness - reserved);
        // 返还时长 = 5 秒 × DarknessReservationLasts 倍率（tick）
        int duration = Math.max(1, Math.round(BASE_RESERVATION_SECONDS * 20
                * (float) living.getAttributeValue(AttributeRegistry.DARKNESS_RESERVATION_LASTS)));
        List<Value> list = new ArrayList<>(living.getData(AttachmentRegistry.DARKNESS_RESERVATION));
        list.add(new Value(reserved, duration));
        living.setData(AttachmentRegistry.DARKNESS_RESERVATION, List.copyOf(list));
        return reserved;
    }

    /** 每 tick 返还预留（由 AttributeEvent 在 regeneration 作用域内调用）：逐条目递减时长，到期全额返还 Darkness。 */
    public static void tickReservation(LivingEntity living) {
        if (!isDarknessEnabled(living)) return;
        if (!living.hasData(AttachmentRegistry.DARKNESS_RESERVATION)) return;
        List<Value> list = living.getData(AttachmentRegistry.DARKNESS_RESERVATION);
        if (list.isEmpty()) return;
        float restore = 0;
        List<Value> next = new ArrayList<>();
        for (Value v : list) {
            int remain = v.duration() - 1;
            if (remain > 0) {
                next.add(new Value(v.original(), remain));
            } else {
                restore += v.original();
            }
        }
        living.setData(AttachmentRegistry.DARKNESS_RESERVATION, List.copyOf(next));
        if (restore > 0 && !isFullDarkness(living)) {
            setDarkness(living, getDarkness(living) + restore);
        }
    }
}
