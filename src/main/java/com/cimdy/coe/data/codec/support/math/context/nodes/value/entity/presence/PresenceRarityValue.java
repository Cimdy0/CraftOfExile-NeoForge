package com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence;

import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.PresenceDependent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.meta.Camp;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.cimdy.coe.event.Event;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;
import java.util.Locale;

/**
 * Presence 范围内成员的<b>稀有度聚合</b>读值节点：驱动「身边有稀有 / 传奇敌人时 X」一族词缀。
 *
 * <p><b>与 {@link PresenceValue} 的语义分工</b>：{@code PresenceValue} 是<b>单候选</b>判定——
 * 回答「<i>当前被判断的这一个</i>候选实体是否在窗口内进入 / 离开了追踪者范围」，用于目标筛选场景。
 * 本节点是<b>聚合</b>判定——回答「追踪者范围内<i>是否存在任一</i>满足稀有度条件的成员 / 有几个」，
 * 属于持有者自身的状态门控，与候选无关。二者模型不可互相替代：把聚合语义塞进单候选节点会导致
 * 「谁是候选」在自身门控场景下无定义（fold 路径的 condEntity 即持有者本人，恒不命中敌人）。
 *
 * <p>成员集合取 {@code Event.State.presenceMembers()}（当前在范围内的全部 UUID），经所在 {@link Level}
 * 还原为实体后依次过滤：
 * <ol>
 *   <li>排除追踪者自身；</li>
 *   <li>{@code enemiesOnly = true} 时按阵营过滤，规则与
 *       {@code TargetSelector.resolvePresenceEnemies} 完全一致——成员阵营
 *       {@code != Camp.NONE && != 追踪者阵营}；追踪者自身为 {@code NONE}（未分配阵营）时
 *       无敌方可言，直接返回 0；</li>
 *   <li>成员稀有度（{@code AttachmentRegistry.RARITY}，怪物生成时由 {@code GenEvent} 写入，缺省 {@code NONE}）
 *       须落在 {@link #rarities} 内。</li>
 * </ol>
 *
 * <p>实现 {@link PresenceDependent}，故 {@code EventRefCollector} 扫到本节点即自动开启
 * Presence 全域扫描——不实现该接口的话扫描不会启用，{@code presenceMembers()} 恒为空、节点静默恒返 0。
 */
public record PresenceRarityValue(
        List<Rarity> rarities,
        boolean enemiesOnly,
        boolean count
) implements EntityNode, PresenceDependent {

    public static final Codec<PresenceRarityValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rarity.CODEC.listOf().fieldOf("rarities").forGetter(PresenceRarityValue::rarities),
            Codec.BOOL.optionalFieldOf("enemies_only", Boolean.TRUE).forGetter(PresenceRarityValue::enemiesOnly),
            Codec.BOOL.optionalFieldOf("count", false).forGetter(PresenceRarityValue::count)
    ).apply(instance, PresenceRarityValue::new));

    /** 「身边存在稀有或传奇敌人」：RARE / UNIQUE 任一在场即 1（count=false → 存在性判定）。 */
    public static PresenceRarityValue rareOrUniqueEnemy() {
        return new PresenceRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE), true, false);
    }

    /** 「身边每有一个稀有或传奇敌人」：count=true 计数模式，返回实际个数（用于「for each rare/unique monster +X」线性缩放）。 */
    public static PresenceRarityValue rareOrUniqueEnemyCount() {
        return new PresenceRarityValue(List.of(Rarity.RARE, Rarity.UNIQUE), true, true);
    }

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        // 追踪者 = Presence 记录的属主（词缀持有者自身）。fold 路径下 living 亦为持有者，
        // 但仍以 scope.root() 为准，与 PresenceValue 保持同一取值口径。
        if (!(scope.root().getEntity() instanceof LivingEntity tracker)) return 0D;
        Camp trackerCamp = tracker.getData(AttachmentRegistry.CAMP);
        if (enemiesOnly && trackerCamp == Camp.NONE) return 0D;

        Level level = tracker.level();
        Event.State state = Event.get(tracker);
        int c = 0;
        for (UUID id : state.presenceMembers()) {
            if (!(level.getEntity(id) instanceof LivingEntity member) || member == tracker) continue;
            if (enemiesOnly) {
                Camp cc = member.getData(AttachmentRegistry.CAMP);
                if (cc == Camp.NONE || cc == trackerCamp) continue;
            }
            if (!rarities.contains(member.getData(AttachmentRegistry.RARITY))) continue;
            if (!count) return 1D;   // 存在性判定（原 Mode.ANY）：首个命中即 1
            c++;
        }
        return count ? c : 0D;       // 计数模式返回个数；存在性模式未命中返回 0
    }

    @Override
    public List<Component> describeQuantity(Locale locale) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rarities.size(); i++) {
            if (i > 0) sb.append('_');
            sb.append(rarities.get(i).getSerializedName());
        }
        Component rarity = StatValueKey.of("presence_rarity.rarity", sb.toString());
        Component out = enemiesOnly
                ? Component.empty().append(rarity).append(Component.literal(" ")).append(StatValueKey.of("presence_rarity.scope", "enemy"))
                : rarity;
        return List.of(out);
    }

}
