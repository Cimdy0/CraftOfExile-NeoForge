package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentSocket;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.support.value.ModifierValue;
import com.cimdy.coe.data.gen.equipment.EquipmentGen;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 孔（Socket）通用设施 —— 装备物品孔与技能辅助槽共用一套「开孔 / 读槽」口径。
 *
 * <p>POE2 实证：</p>
 * <ul>
 *   <li><b>装备孔（item socket）</b>：可开在武术武器 / 法杖 / 长杖 / 权杖 / 护甲上；
 *       上限 = 双手武器与胸甲 2 个，单手武器与其它护甲 1 个（工匠石 Artificer's Orb 开出）。</li>
 *   <li><b>技能辅助槽</b>：技能石初始 2 槽，上限 5 槽；
 *       次级 / 高阶 / 完美工匠石分别置为 3 / 4 / 5 槽（POE2 无基准工匠石）。</li>
 * </ul>
 *
 * <p>装备孔承载 {@link EquipmentSocket}（类别 + 内容解耦），因此符文 / 魂核 / 未来新 augment
 * 共用同一套孔设施，新增 augment 种类不需要改孔结构。</p>
 */
public final class SocketOps {

    private SocketOps() {
    }

    /** 双手武器 / 胸甲的孔上限；单手武器与其它护甲为 1。 */
    public static final int MAX_TWO_HAND_OR_BODY = 2;
    public static final int MAX_OTHER = 1;

    /** 是否可以开孔：仅武器与护甲（POE2：首饰 / 箭袋等不参与）。 */
    public static boolean canHoldSockets(EquipmentState state) {
        return state.isWeapon() || state.isArmor();
    }

    /**
     * 该装备的孔上限：双手武器 / 胸甲 2，其余 1。
     *
     * <p>判定一律读 {@link EquipmentState#types()}（ItemWeight 类型树解析后缓存进 State 的结果），
     * 不查物品标签——标签由内容侧逐物品打标、易漏，且已由类型树取代其地位。
     * 「双手」由类型链的 {@code two_hand} 段给出，「胸甲」由 {@code chest} 段给出。</p>
     */
    public static int maxSockets(EquipmentState state) {
        return isTwoHanded(state) || isBodyArmour(state) ? MAX_TWO_HAND_OR_BODY : MAX_OTHER;
    }

    public static boolean isTwoHanded(EquipmentState state) {
        return ItemTypeHierarchy.isWithin(EquipmentGen.id(EquipmentGen.TWO_HAND), state.types());
    }

    public static boolean isBodyArmour(EquipmentState state) {
        return ItemTypeHierarchy.isWithin(EquipmentGen.id(EquipmentGen.CHEST), state.types());
    }

    /** 当前已开孔数。 */
    public static int count(EquipmentState state) {
        return state.sockets().size();
    }

    /** 孔数是否已达上限（工匠石的守卫）。 */
    public static boolean isMaxed(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.MODIFIABLE_STATE)) {
            return true;
        }
        EquipmentState state =ModifiableStateOps.equipment(stack);
        return !canHoldSockets(state) || count(state) >= maxSockets(state);
    }

    /** 开一个空 augment 孔（符文 / 魂核 / 其它 augment 共用）。 */
    public static void add(ItemStack stack) {
        if (!stack.has(DataComponentRegistry.MODIFIABLE_STATE)) {
            return;
        }
        EquipmentState state =ModifiableStateOps.equipment(stack);
        if (!canHoldSockets(state) || count(state) >= maxSockets(state)) {
            return;
        }
        List<EquipmentSocket> sockets = new ArrayList<>(state.sockets());
        sockets.add(EquipmentSocket.empty(EquipmentSocket.AUGMENT));
        stack.set(DataComponentRegistry.MODIFIABLE_STATE, state.withSockets(List.copyOf(sockets)));
    }

    /**
     * 将技能石辅助槽数置为 {@code target}（置位而非累加：POE2 工匠石为「Set to N Sockets」）。
     * 保留 {@code change} 分量（外部「+N 槽」加成不被通货覆盖）。
     */
    public static void setSkillSockets(ItemStack stack, int target) {
        SkillData data = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
        if (data.isEmpty()) {
            return;
        }
        int next = SkillData.clampSockets(target);
        stack.set(DataComponentRegistry.SKILL_DATA, new SkillData(
                data.id(),
                data.status(),
                data.level(),
                data.quality(),
                ModifierValue.Int.create(next, data.sockets().change()),
                data.supports(),
                data.spirit()));
    }

    /** 工匠石守卫：目标槽数必须严格大于当前槽数（POE2「只能用于少于 N 槽的技能石」，且不可降级）。 */
    public static boolean canSetSkillSockets(ItemStack stack, int target) {
        SkillData data = stack.getOrDefault(DataComponentRegistry.SKILL_DATA, SkillData.EMPTY);
        if (data.isEmpty()) {
            return false;
        }
        return data.getSockets() < SkillData.clampSockets(target);
    }
}
