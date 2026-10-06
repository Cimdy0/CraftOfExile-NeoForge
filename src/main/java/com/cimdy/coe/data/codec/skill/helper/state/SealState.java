package com.cimdy.coe.data.codec.skill.helper.state;

import com.cimdy.coe.data.codec.skill.modifier.impl.control.SealMod;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

/**
 * 封印（Seal）运行时状态 — 玩家附件（{@link AttachmentRegistry#SEAL_STATE}）。
 *
 * <p>对齐 POE2「Unleash / 封印」机制：被封印辅助的技能每次施放累积充能、按阈值叠封印（seals），
 * 持有 ≥1 封印期间施放任意技能都进入 {@code seal} 作用域（见 {@code SkillStatScopes#enter}），
 * 使已接线的 {@code EFF_ADD_MULTIPLIED_BASE_ATTRIBUTE_COE_CAST_SPEED_SEAL}（死线）复活；
 * 存满封印后下一次施放触发释放（自动释放回退模式），按持有封印数重放技能后清零。
 *
 * <p>字段：
 * <ul>
 *   <li>{@code seals} —— 当前持有封印数（≥1 即发射 seal 标签）；</li>
 *   <li>{@code charge} —— 封印充能进度（满 1 叠 1 层）；</li>
 *   <li>{@code maxSeals} —— 封印上限（取自激活的 SealMod）；</li>
 *   <li>{@code gainEveryCastTimeMultiplier} —— 每次施放获得的充能 = 100 / 该值；</li>
 *   <li>{@code repeatPerSealBroken} —— 每破碎（释放）1 枚封印额外重放的次数；</li>
 *   <li>{@code areaIncPerSealBroken} —— 每破碎 1 枚封印，重放那次 +X% 范围（increased，加法）。</li>
 * </ul>
 *
 * <p>服务端权威、不同步（战斗期瞬时状态）、不随死亡复制（重生后封印清空）。
 *
 * <p><b>不实现 {@code ResourcePool}</b>：SealState 仅为封印数据持有者（get / set Save），
 * 无 generate / consume / clear / tick 语义，不构成资源池。
 */
public final class SealState {
    private SealState() {
    }

    /** 单个玩家的封印状态（附件值类型）。 */
    public record Save(
            int seals,
            float charge,
            int maxSeals,
            double gainEveryCastTimeMultiplier,
            int repeatPerSealBroken,
            double areaIncPerSealBroken
    ) {
        public static final Save EMPTY = new Save(0, 0f, 1, 100.0, 1, 0.0);

        public static final Codec<Save> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("seals", 0).forGetter(Save::seals),
                Codec.FLOAT.optionalFieldOf("charge", 0f).forGetter(Save::charge),
                Codec.INT.optionalFieldOf("max_seals", 1).forGetter(Save::maxSeals),
                Codec.DOUBLE.optionalFieldOf("gain_every_cast_time_multiplier", 100.0)
                        .forGetter(Save::gainEveryCastTimeMultiplier),
                Codec.INT.optionalFieldOf("repeat_per_seal_broken", 1).forGetter(Save::repeatPerSealBroken),
                Codec.DOUBLE.optionalFieldOf("area_inc_per_seal_broken", 0.0)
                        .forGetter(Save::areaIncPerSealBroken)
        ).apply(i, Save::new));

        /** 读取玩家封印状态（缺省 EMPTY）。 */
        public static Save get(LivingEntity living) {
            return living.getData(AttachmentRegistry.SEAL_STATE);
        }

        /** 写回玩家封印状态。 */
        public static void set(LivingEntity living, Save save) {
            living.setData(AttachmentRegistry.SEAL_STATE, save);
        }
    }
}
