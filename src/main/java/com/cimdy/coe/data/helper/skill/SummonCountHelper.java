package com.cimdy.coe.data.helper.skill;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.stat.condition.RelationCondition;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.helper.OwnerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 召唤物 / 图腾数量计数（POE2：技能把自身展示为 Status 并实时显示当前召唤物 / 图腾数）。
 *
 * <p><b>事件驱动，而非每 tick 扫描</b>：召唤物的生成与消失都有固定可循的单一路径，
 * 故 +1 / −1 直接挂在这些路径上（见下方「挂载点」），避免逐 tick 全量扫描实体带来的无谓开销。
 *
 * <ul>
 *   <li><b>+1</b>：{@code SpawnAction.spawnMinion} / {@code spawnTotem} 在 {@code addFreshEntity} 之后。</li>
 *   <li><b>−1</b>：四处真销毁点之前 —— {@code SummonEvent.onEntityTick}（限时召唤物到期）、
 *       {@code SkillTotem.tick}（图腾到期）、{@code SkillTotem.die}（图腾被打死）、
 *       以及 {@code SummonEvent.onMinionDeath}（可复生/永久召唤物被击败，真死并登记待复活，复活时由
 *       {@code SpawnAction.spawnMinionEntity} 重新 +1，净零）。</li>
 * </ul>
 *
 * <p><b>低频对账</b>：事件计数会因跨维度 / 区块卸载 / 异常移除（如限时召唤物被正常伤害打死，
 * 走死亡移除而非到期 {@code discard}）而漂移，故 {@link #reconcile} 每 100 tick 重算一次真值写回。
 * 该门控保证「只对真的持有计数的 owner」扫描，不是每 tick 扫描。
 *
 * <p>计数<b>服务端权威</b>（写入侧一律 {@code isClientSide()} 早退），两个附件均已同步，
 * 故读取侧在客户端可直接读到（HUD 展示）。
 */
public final class SummonCountHelper {

    private SummonCountHelper() {
    }

    /** 生成召唤物：计数 +1（服务端权威）。 */
    public static void onSummoned(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) return;
        owner.setData(AttachmentRegistry.SUMMONED_COUNT, Math.max(0, getSummonedCount(owner) + 1));
    }

    /** 召唤物真销毁：计数 −1（下界钳 0）。 */
    public static void onDespawned(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) return;
        owner.setData(AttachmentRegistry.SUMMONED_COUNT, Math.max(0, getSummonedCount(owner) - 1));
    }

    /** 生成图腾：计数 +1（服务端权威）。 */
    public static void onTotemSummoned(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) return;
        owner.setData(AttachmentRegistry.TOTEM_COUNT, Math.max(0, getTotemCount(owner) + 1));
    }

    /** 图腾真销毁：计数 −1（下界钳 0）。 */
    public static void onTotemDespawned(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) return;
        owner.setData(AttachmentRegistry.TOTEM_COUNT, Math.max(0, getTotemCount(owner) - 1));
    }

    /**
     * 统一销毁入口：按实体类型把对应计数 −1。
     * <p>先确认该实体确实是图腾 / 能解析出 owner 的召唤物，任一不满足则静默返回
     * （未归属实体、owner 已不在线、客户端侧均不计入）。
     */
    public static void onMinionRemoved(Entity removed) {
        if (removed == null || removed.level().isClientSide()) return;
        if (removed instanceof SkillTotem totem) {
            onTotemDespawned(totem.getOwner());
            return;
        }
        if (!(removed instanceof LivingEntity living)) return;
        if (OwnerHelper.getOwner(living) instanceof LivingEntity owner) onDespawned(owner);
    }

    /** 当前召唤物数量（已同步，客户端可读）；缺省 / owner 为空时为 0。 */
    public static int getSummonedCount(LivingEntity owner) {
        return owner == null ? 0 : owner.getData(AttachmentRegistry.SUMMONED_COUNT);
    }

    /** 当前图腾数量（已同步，客户端可读）；缺省 / owner 为空时为 0。 */
    public static int getTotemCount(LivingEntity owner) {
        return owner == null ? 0 : owner.getData(AttachmentRegistry.TOTEM_COUNT);
    }

    /**
     * 低频对账（每 100 tick 由 {@code SummonEvent.onSummonCountReconcile} 调用）：
     * 重算真值并写回，校正事件计数漂移。
     * <p>口径复用项目既有实现 —— 召唤物走 {@link RelationCondition#resolveMinions}、
     * 图腾走 {@link RelationCondition#resolveTotems}，避免扫描逻辑与半径 / 归属语义漂移。
     * <p><b>门控</b>：owner 未持有任一计数附件（从未召唤过）或两者皆为 0（既无召唤也无漂移）时直接返回，
     * 不做实体扫描，避免无谓开销。
     */
    public static void reconcile(LivingEntity owner) {
        if (owner == null || owner.level().isClientSide()) return;
        if (!owner.hasData(AttachmentRegistry.SUMMONED_COUNT)
                && !owner.hasData(AttachmentRegistry.TOTEM_COUNT)) return;
        if (getSummonedCount(owner) == 0 && getTotemCount(owner) == 0) return;
        int minions = RelationCondition.resolveMinions(owner).size();
        int totems = RelationCondition.resolveTotems(owner).size();
        // 仅在真值与计数不一致时写回：避免每 5 秒无条件触发一次同步包
        if (minions != getSummonedCount(owner)) owner.setData(AttachmentRegistry.SUMMONED_COUNT, minions);
        if (totems != getTotemCount(owner)) owner.setData(AttachmentRegistry.TOTEM_COUNT, totems);
    }
}
