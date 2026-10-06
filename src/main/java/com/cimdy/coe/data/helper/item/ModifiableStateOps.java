package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.state.CharmState;
import com.cimdy.coe.data.codec.item.flask.FlaskState;
import com.cimdy.coe.data.codec.item.state.GemState;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * 物品状态通用载体设施 —— {@link ModifiableState} 四维（物等 / 稀有度 / 品质 / 词缀）在 {@link ItemStack}
 * 上的<b>统一读写</b>唯一入口。
 *
 * <p>背景：装备 / 药剂 / 护符三类状态现已统一存于单一组件 {@link DataComponentRegistry#MODIFIABLE_STATE}
 * （类型 {@code DataComponentType<ModifiableState>}），由 {@code ModifiableStateCodec} 判别编解码。
 * 本类把「取具体子类型」收敛为一次分派，使消费端无需关心组件类型：</p>
 * <ul>
 *   <li>{@link #equipment} —— 取装备状态（非装备 / 缺失返回 {@link EquipmentState#DEFAULT}）。</li>
 *   <li>{@link #flask} / {@link #charm} —— 取药剂 / 护符状态（缺失返回 {@code null}）。</li>
 *   <li>{@link #get} / {@link #set} / {@link #update} —— 通用 {@link ModifiableState} 维度读写。</li>
 *   <li>维度级便捷读写（{@code level/rarity/quality/mods} + {@code setXxx}）。</li>
 * </ul>
 *
 * <p><b>不变式</b>：{@link #setXxx} 系列只对「已携带状态的栈」生效（{@code update} 在读不到状态时
 * 返回 {@code false} 且不创建组件），因此不会把装备状态误挂到药剂 / 护符或普通物品上。</p>
 */
public final class ModifiableStateOps {

    private ModifiableStateOps() {
    }

    /** 读取栈上的物品状态（装备 / 药剂 / 护符任一，无状态返回空）。 */
    public static Optional<ModifiableState> get(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponentRegistry.MODIFIABLE_STATE));
    }

    /** 装备状态（非装备 / 缺失返回 {@link EquipmentState#DEFAULT}，绝不返回 {@code null}）。 */
    public static EquipmentState equipment(ItemStack stack) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof EquipmentState e ? e : EquipmentState.DEFAULT;
    }

    /** 药剂状态（非药剂 / 缺失返回 {@code null}）。 */
    public static FlaskState flask(ItemStack stack) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof FlaskState f ? f : null;
    }

    /** 护符状态（非护符 / 缺失返回 {@code null}）。 */
    public static CharmState charm(ItemStack stack) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof CharmState c ? c : null;
    }

    /** 宝石状态（非宝石 / 缺失返回 {@code null}）。 */
    public static GemState gem(ItemStack stack) {
        ModifiableState s = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return s instanceof GemState g ? g : null;
    }

    /**
     * 写回状态（统一组件，按运行时子类型编码）。
     *
     * @return 是否写成功；{@code state} 为 {@code null} 时返回 {@code false}。
     */
    public static boolean set(ItemStack stack, ModifiableState state) {
        if (state == null) {
            return false;
        }
        stack.set(DataComponentRegistry.MODIFIABLE_STATE, state);
        return true;
    }

    /**
     * 读取 -> 改写 -> 写回同类型状态（保持载体身份不变）。
     *
     * @return 是否发生写入；无状态载体返回 {@code false}（不创建组件）。
     */
    public static boolean update(ItemStack stack, UnaryOperator<ModifiableState> change) {
        ModifiableState current = stack.get(DataComponentRegistry.MODIFIABLE_STATE);
        return current != null && set(stack, change.apply(current));
    }

    // ------------------------------------------------------------ 通用维度读取

    /** 物等（无状态载体返回 0）。 */
    public static int level(ItemStack stack) {
        return get(stack).map(ModifiableState::level).orElse(0);
    }

    /** 稀有度（无状态载体返回 {@link Rarity#NONE}）。 */
    public static Rarity rarity(ItemStack stack) {
        return get(stack).map(ModifiableState::rarity).orElse(Rarity.NONE);
    }

    /** 品质（无状态载体返回 0）。注意技能石的品质走 {@code SKILL_DATA}，不在此列。 */
    public static int quality(ItemStack stack) {
        return get(stack).map(ModifiableState::quality).orElse(0);
    }

    /** 词缀（无状态载体返回空列表）。 */
    public static List<ModifierEntry> mods(ItemStack stack) {
        return get(stack).map(ModifiableState::mods).orElse(List.of());
    }

    // ------------------------------------------------------------ 通用维度写入

    /** 写入品质（自动钳制上限）。无状态载体不动作。 */
    public static boolean setQuality(ItemStack stack, int quality) {
        return update(stack, s -> s.withQuality(quality));
    }

    /** 写入稀有度。无状态载体不动作。 */
    public static boolean setRarity(ItemStack stack, Rarity rarity) {
        return update(stack, s -> s.withRarity(rarity));
    }

    /** 写入词缀（入参先冻结为不可变副本）。无状态载体不动作。 */
    public static boolean setMods(ItemStack stack, List<ModifierEntry> mods) {
        List<ModifierEntry> frozen = List.copyOf(mods);
        return update(stack, s -> s.withMods(frozen));
    }
}
