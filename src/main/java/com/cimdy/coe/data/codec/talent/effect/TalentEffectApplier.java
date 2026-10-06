package com.cimdy.coe.data.codec.talent.effect;

import com.cimdy.coe.data.codec.talent.data.JewelData;
import com.cimdy.coe.data.codec.talent.data.TalentAllocation;
import com.cimdy.coe.data.codec.talent.data.TalentData;
import com.cimdy.coe.data.codec.talent.node.TalentNodeDef;
import com.cimdy.coe.data.codec.talent.data.TalentState;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeData;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModel;
import com.cimdy.coe.data.codec.talent.tree.TalentTreeModelLoader;
import com.cimdy.coe.data.codec.talent.data.TalentWeaponSet;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.support.addition.AdditionData;
import com.cimdy.coe.data.codec.support.addition.AdditionKeys;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 把已分配的天赋节点聚合为效果，应用到玩家。
 *
 * <p>采用「全量重建」策略，且<b>仅增删 Stat</b>：先按前缀 {@code coe.talent.} 整体移除旧的天赋 Stat 贡献，
 * 再按当前各天赋树已分配集合（含多选一节点的选项 / 珠宝）重新施加。所有天赋加成（属性亚型 + 触发亚型）
 * 统一经 {@link TalentEffect#apply} 以「来源键」写入玩家 {@code STAT_STORE} 附件，
 * 不再依赖原版 AttributeModifier / limiter 跟踪列表——无论加点、改选还是洗点，都不会残留或重复。
 *
 * <p>由于效果类型由 {@link TalentEffectType} 注册表驱动，新增天赋机制只需注册新效果类型，
 * 本类无需任何改动即能施加。所有加成（含原「工具能力」）都以效果形式表达，保持数据驱动一致。
 */
public final class TalentEffectApplier {
    private static final String PREFIX = "coe.talent.";

    private TalentEffectApplier() {
    }

    public static void apply(Player player) {
        // 1. 移除旧的天赋 Stat 贡献（按完整键前缀识别，只清天赋命名空间，不动其它来源）
        StatStore store = player.hasData(AttachmentRegistry.STAT_STORE)
                ? player.getData(AttachmentRegistry.STAT_STORE) : StatStore.empty();
        player.setData(AttachmentRegistry.STAT_STORE, store.removeByKeyPrefix(PREFIX));

        // 2. 遍历所有天赋树：先按版本迁移，再重新施加效果（纯 Stat，由 TalentEffect.apply 写入 STAT_STORE）
        // 当前激活武器组：仅「全局」及「绑定到该组」的节点点亮（其余已分配节点休眠、不提供效果）
        // 原 AttachmentRegistry.ACTIVE_WEAPON_SET 已迁入 AdditionData（key=active_weapon_set，默认 1）
        int activeWeaponSet = (int) AdditionData.getOrDefault(player, AdditionKeys.ACTIVE_WEAPON_SET, 1.0);
        TalentData td = player.hasData(AttachmentRegistry.TALENT_TREE)
                ? player.getData(AttachmentRegistry.TALENT_TREE) : TalentData.EMPTY;
        for (Identifier treeId : new ArrayList<>(td.treeIds())) {
            TalentTreeModel model = TalentTreeModelLoader.get(treeId);
            TalentState st = td.get(treeId).state();
            // 版本迁移：模型版本变动时按策略重置或保留并修正（详见 migrate）
            TalentState migrated = migrate(st, model);
            if (migrated != st) {
                td = td.with(treeId, new TalentTreeData(migrated));
                st = migrated;
            }
            for (TalentAllocation a : st.allocated()) {
                // 武器专精：跳过当前激活快捷栏栏位下休眠的节点（仍分配但失效）
                if (!TalentWeaponSet.active(a.weaponSet(), activeWeaponSet)) continue;
                TalentNodeDef def = model.node(a.id());
                if (def == null) continue;
                List<TalentEffect> effs;
                if (def.hasChoice() && a.choice() >= 0 && a.choice() < def.choices().size()) {
                    effs = List.of(def.choices().get(a.choice()));
                } else {
                    effs = def.effects();
                }
                int idx = 0;
                for (TalentEffect eff : effs) {
                    eff.apply(player, treeId, a.id(), idx);
                    idx++;
                }
                // 珠宝插槽：已插入珠宝的节点，额外施加珠宝授予的效果（复用同一管线，
                // 用独立的 nodeId 命名空间避免与节点自身效果的来源键冲突）。
                if (def.type().socket() && a.hasSocket()
                        && a.socket().getItem() instanceof com.cimdy.coe.item.gem.Jewel) {
                    JewelData jewel = com.cimdy.coe.item.gem.Jewel.dataOf(a.socket());
                    int jidx = 0;
                    for (TalentEffect eff : jewel.effects()) {
                        eff.apply(player, treeId, a.id() + ".jewel", jidx);
                        jidx++;
                    }
                }
            }
        }
        player.setData(AttachmentRegistry.TALENT_TREE, td);

        // 3. 天赋变动无需在此处即时重算：每游戏 tick 的 onLivingTick 会实时全量检验所有 stat 操作（含本次变动）。
    }

    /**
     * 版本迁移：当玩家分配状态的版本与当前模型版本不一致时，按模型策略处理，返回已更新版本号的状态。
     *
     * <ul>
     *   <li>{@code reset_on_update=true}：整树洗点——版本更迭强制重置已选点数。</li>
     *   <li>{@code reset_on_update=false}（默认）：保留玩家选择，仅剔除失效分配
     *       （节点被删/改名、多选一选项越界）。由于效果始终按当前模型全量重算，
     *       节点效果的数值变动会自然「被修正」，无需玩家重选。</li>
     * </ul>
     *
     * 返回的 {@link TalentState#version()} 会被更新为模型当前版本，避免重复迁移。版本一致时原样返回。
     */
    private static TalentState migrate(TalentState state, TalentTreeModel model) {
        int target = model.version();
        if (state.version() == target) return state;
        if (model.resetOnUpdate()) {
            return new TalentState(List.of(), target);
        }
        List<TalentAllocation> pruned = new ArrayList<>();
        for (TalentAllocation a : state.allocated()) {
            TalentNodeDef def = model.node(a.id());
            if (def == null) continue;                                   // 节点已不存在
            if (def.hasChoice() && (a.choice() < 0 || a.choice() >= def.choices().size())) {
                continue;                                                // 多选一选项已失效
            }
            pruned.add(a);
        }
        return new TalentState(pruned, target);
    }
}
