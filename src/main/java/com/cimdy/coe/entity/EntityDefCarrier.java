package com.cimdy.coe.entity;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.entity.EntityDef;
import com.cimdy.coe.data.codec.entity.EntityRead;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 能被 {@link EntityDef} 装配的实体 —— 即「技能在世界中放置出来的东西」的通用接收端。
 *
 * <p><b>投递不再硬编码实体类</b>。生成流程统一为：
 * <ol>
 *   <li>按 {@link EntityDef#type()} 从 {@code ENTITY_TYPE} 注册表实例化实体；</li>
 *   <li>若该实体实现本接口，则把定义装配进去（owner / 阶段 / 时长 / 半径 / 形态）。</li>
 * </ol>
 * 于是新增一类载体（驻场道具 → 投射物 → 投掷物 …）= 让其实体类实现本接口，
 * 投递侧<b>零改动</b>。
 *
 * <p><b>施法者契约（重要）</b>：只有 {@code LivingEntity} 会「使用技能」。
 * 本实体<b>不是施法者</b>——它只持有 owner，触发时以 owner 作为 caster 回调技能阶段。
 * 这与投射物「产生效果时去找 owner」是同一条规则：实体是形态，owner 才是施法者。
 *
 * <h2>为什么装配的是 {@link EntityRead} 而不是 {@link EntityDef}</h2>
 * {@link EntityDef} 是<b>解析结果</b>（预设 ⊕ render 覆写 ⊕ name 覆写），无法反推出它「从哪来」；
 * 而 {@link EntityRead}（引用 + 覆写）是可序列化的<b>来源</b>——存档重载后按它重新
 * {@link EntityRead#resolve()} 即可还原完全相同的形态，包括技能侧的外观覆写
 * （「同一实体类型、不同技能不同外观」的错觉跨存档依然成立）。
 * 若只持久化 {@link EntityDef}：要么丢覆写，要么把解析结果原样搬进存档、与数据表脱钩。
 *
 * <p>故契约固定为「<b>装配收 read，存 read，重载时按 read 重解析</b>」，
 * 由本接口的 {@link #saveDef(ValueOutput)} / {@link #restoreDef(ValueInput)} 默认实现统一承担，
 * 载体只需实现 {@link #entityRead()} 与 {@link #applyRead(EntityRead)} 两个存取点。
 */
public interface EntityDefCarrier {

    /**
     * 装配实体定义。
     *
     * @param owner     施放者（本实体的 owner；后续一切效果的 caster 都是它）
     * @param phaseId   技能阶段 UUID（用于反查阶段实例并点燃触发器）
     * @param lifetime  存活时长（tick）
     * @param radius    触发 / 作用半径（格）
     * @param read      实体引用（含 render / name 覆写）；载体自行 {@link EntityRead#resolve()}
     */
    void configure(LivingEntity owner, @Nullable UUID phaseId, int lifetime, float radius, EntityRead read);

    /** 本载体的实体引用（持久化用）；未装配时为 null。 */
    @Nullable
    EntityRead entityRead();

    /** 已解析的实体定义；未装配或解析失败时为 null。 */
    @Nullable
    EntityDef def();

    /**
     * 按实体引用<b>重新装配形态</b> —— 装配与存档重载共用同一条路径，
     * 保证「新装配」与「重载还原」的形态（render id 同步、customName、粒子定义）完全一致。
     *
     * <p>注意：重载后 {@code phaseId} 不保留（阶段实例已不存在），故本方法不接收它——
     * 重载的实体只保留形态与计时，不再回调技能阶段（与既有设计一致）。
     */
    void applyRead(EntityRead read);

    /**
     * 把实体引用写入存档（载体 {@code addAdditionalSaveData} 内调用）。
     * 未装配时不写，避免留下无法解析的空引用。
     */
    default void saveDef(ValueOutput output) {
        EntityRead read = this.entityRead();
        if (read != null) output.store("Def", EntityRead.CODEC, read);
    }

    /**
     * 从存档还原形态（载体 {@code readAdditionalSaveData} 内调用）。
     *
     * <p>解析失败（数据表被删 / 改名）时<b>只记日志不抛异常</b> —— 单个陈旧实体不该让整个存档读档失败；
     * 还原后 {@link #def()} 仍为 null，载体的既有 null 分支（不触发 / 不发射粒子）会让它安静地到期消失。
     */
    default void restoreDef(ValueInput input) {
        input.read("Def", EntityRead.CODEC).ifPresent(read -> {
            if (read.resolve().isEmpty()) {
                CraftOfExile.LOGGER.warn("实体定义 {} 已不存在，该实体将无形态地到期消失", read.ref());
            }
            this.applyRead(read);
        });
    }
}
