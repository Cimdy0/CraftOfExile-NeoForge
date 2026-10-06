package com.cimdy.coe.data.helper.skill;

import com.cimdy.coe.data.helper.stat.AttributeLimiterHelper;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentSkillEntry;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.cimdy.coe.data.helper.item.ModifiableStateOps;

/**
 * 装备授予技能（数据驱动 {@code coe:equipment_template.skills}）的注入与合并设施。
 *
 * <p>与属性条目（{@link AttributeLimiterHelper}）完全对称：
 * 每件已穿戴装备的授予技能以「来源前缀」{@code equip:<slot>:<uuid>} 为键归入
 * {@link AttachmentRegistry#EQUIPMENT_SKILLS}，脱下/脱槽时按前缀整体移除，互不残留。
 *
 * <p>前缀与属性条目共用同一套计算（{@link AttributeLimiterHelper#equipPrefixOf}），
 * 保证同一件装备的属性与技能在同一来源键下绑定/解绑。
 *
 * <p>玩家与任意实体共用同一附件。消费端经 {@link #mergedAssembly(LivingEntity)} 取
 * 「手动装配表(SKILL_ASSEMBLY) + 装备授予技能」的合并视图：手动槽位索引保持不变，
 * 装备技能填入手动表中空闲的槽位（按序），从而经统一的 {@code SkillCaster} 通道施放；
 * 非玩家实体（无手动装配）由 {@code MobSkillCastGoal} 直接消费合并视图。
 */
public final class EquipmentSkillHelper {

    private EquipmentSkillHelper() {
    }

    /** 从装备物品读出授予的技能（已解析为 SkillData，含辅助技能），无 MODIFIABLE_STATE 或非装备物品返回空。 */
    public static List<SkillData> skillDataOf(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.MODIFIABLE_STATE)) return List.of();
        EquipmentState state = ModifiableStateOps.equipment(stack);
        return state.base().skills();
    }

    /** 把单条装备技能条目解析为可直接装配的 SkillData（默认等级 1 / 品质 0 / 非精魂，附加载入的辅助宝石）。 */
    public static SkillData resolve(EquipmentSkillEntry entry) {
        // D4: 程序化构造可能传入 null id，SkillData.builder(null) 会 NPE；调用方把 EMPTY 视为空槽。
        if (entry.id() == null) return SkillData.EMPTY;
        List<SupportData> supports = new ArrayList<>();
        for (Identifier sup : entry.supports()) {
            if (sup == null) continue;
            supports.add(SupportData.builder(sup).build());
        }
        // D3: 钳到辅助槽上限，使「生效辅助数」与「UI 渲染格数(getSockets，上限 MAX_SOCKETS)」一致，避免显示与实战不一致。
        if (supports.size() > SkillData.MAX_SOCKETS) {
            supports.subList(SkillData.MAX_SOCKETS, supports.size()).clear();
        }
        return SkillData.builder(entry.id())
                .setOriginalLevel(1).setOriginalQuality(0)
                // 辅助槽数与实际 supports 数对齐：resolveSupports() 不按 sockets 截断（数据给几个就生效几个），
                // 而 UI 是按 getSockets() 渲染格子的，故槽数需 ≥ supports 数，否则出现
                // 「格子画不下但运行期全部生效」的显示与实战不一致。无辅助时保持默认 2。
                .sockets(ModifierValue.Int.create(Math.max(2, supports.size())))
                .spirit(false)
                .supports(supports)
                .build();
    }

    /** 穿戴：把本装备授予的技能以来源前缀写入 EQUIPMENT_SKILLS。 */
    public static void grant(LivingEntity living, ItemStack stack, EquipmentSlot slot) {
        List<SkillData> data = skillDataOf(stack);
        if (data.isEmpty()) return;
        String prefix = AttributeLimiterHelper.equipPrefixOf(stack, slot);
        if (prefix == null) return;
        Map<String, List<SkillData>> map = new LinkedHashMap<>(
                living.getData(AttachmentRegistry.EQUIPMENT_SKILLS));
        map.put(prefix, data);
        living.setData(AttachmentRegistry.EQUIPMENT_SKILLS, Map.copyOf(map));
    }

    /** 脱下：按来源前缀移除本装备授予的技能。 */
    public static void revoke(LivingEntity living, ItemStack stack, EquipmentSlot slot) {
        String prefix = AttributeLimiterHelper.equipPrefixOf(stack, slot);
        if (prefix == null) return;
        Map<String, List<SkillData>> map = living.getData(AttachmentRegistry.EQUIPMENT_SKILLS);
        if (!map.containsKey(prefix)) return;
        Map<String, List<SkillData>> next = new LinkedHashMap<>(map);
        next.remove(prefix);
        living.setData(AttachmentRegistry.EQUIPMENT_SKILLS, Map.copyOf(next));
    }

    /** 取本实体全部装备授予的技能（按来源前缀顺序展平，不裁剪容量）。 */
    public static List<SkillData> grantedSkills(LivingEntity living) {
        List<SkillData> out = new ArrayList<>();
        for (List<SkillData> list : living.getData(AttachmentRegistry.EQUIPMENT_SKILLS).values()) {
            out.addAll(list);
        }
        return List.copyOf(out);
    }

    /**
     * 合并视图：手动装配表(SKILL_ASSEMBLY) 槽位原样保留，装备授予技能填入手动表中
     * 空闲的槽位（按序），超过 {@link SkillAssembly#SIZE} 的装备技能被舍弃。
     * 不修改任何附件——每次调用重建视图。
     */
    public static SkillAssembly mergedAssembly(LivingEntity living) {
        SkillAssembly manual = living.getData(AttachmentRegistry.SKILL_ASSEMBLY);
        List<SkillData> slots = new ArrayList<>(manual.slots());
        while (slots.size() < SkillAssembly.SIZE) slots.add(SkillData.EMPTY);
        List<SkillData> equipment = grantedSkills(living);
        int eq = 0;
        for (int i = 0; i < SkillAssembly.SIZE && eq < equipment.size(); i++) {
            if (slots.get(i).isEmpty()) {
                slots.set(i, equipment.get(eq++));
            }
        }
        return new SkillAssembly(List.copyOf(slots), manual.auraEnabled());
    }
}
