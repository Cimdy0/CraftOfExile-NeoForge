package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.tiered.Bonus;
import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceState;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.helper.mod.AttributeApplicator;
import com.cimdy.coe.data.helper.mod.ModGenerator;
import com.cimdy.coe.item.special.EssenceItem;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.core.impl.AffixMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.EssenceMod;
/**
 * 精髓作用操作（PoE2 机制）。
 *
 * <p>两条互斥臂，由 {@link EssenceState#upgrades()} 判定：</p>
 * <ul>
 *   <li><b>前三阶（lesser / normal / greater）</b>：只能作用于<b>魔法</b>物品 —— 升为稀有，
 *       先落一条<b>必定出现</b>的精髓词缀，其余空槽再按可用池随机填补（0 条到补满皆可能）。</li>
 *   <li><b>终阶（perfect / 单阶 corrupted）</b>：只能作用于<b>稀有</b>物品 —— 先移除一条随机词缀，
 *       再添加一条<b>必定出现</b>的精髓词缀（净条数不变）。</li>
 * </ul>
 *
 * <p>精髓词缀直接落 {@code PREFIX}/{@code SUFFIX}（占据一条原生前后缀），符文则不是下缀、不占落位，
 * 两者在本模块里走的是不同分支：一个占前后缀档位，一个不占。</p>
 */
public final class EssenceSocketOps {

    private EssenceSocketOps() {
    }

    /**
     * 是否可作用（不改动栈，客户端预览 / 守卫用）。
     * 宿主须为装备且稀有度与该精髓的臂匹配（升阶臂要 Magic、终阶臂要 Rare），且至少能落一条精髓词缀。
     */
    public static boolean canApply(ItemStack equipmentStack, ItemStack essenceStack) {
        EssenceState essence = essenceOf(essenceStack);
        if (essence == null) {
            return false;
        }
        EquipmentState equipment = equipmentOf(equipmentStack);
        if (equipment == null) {
            return false;
        }
        Rarity rarity = ModifiableStateOps.rarity(equipmentStack);
        boolean rarityMatched = essence.upgrades() ? rarity == Rarity.MAGIC : rarity == Rarity.RARE;
        return rarityMatched && !essenceMods(essence, equipment).isEmpty();
    }

    /** 执行作用（服务端权威）。成功返回 true（调用方负责 consume 精髓）。 */
    public static boolean tryApply(Level level, ItemStack equipmentStack, ItemStack essenceStack) {
        EssenceState essence = essenceOf(essenceStack);
        if (essence == null) {
            return false;
        }
        EquipmentState equipment = equipmentOf(equipmentStack);
        if (equipment == null) {
            return false;
        }
        List<EssenceMod> essenceMods = essenceMods(essence, equipment);
        if (essenceMods.isEmpty()) {
            return false;
        }
        // 精髓词以「范围」声明（min~max），落槽前须按随机源落定数值——否则 Addition.value 为空，
        // tooltip 与属性结算都拿不到实际值（经池抽取的词缀由 addRandomLocationMod 自行 random，不经此路）。
        List<ModifierEntry> rolled = essenceMods.stream()
                .map(m -> (ModifierEntry) m.roll(level.getRandom()))
                .toList();
        // 同位置同冲突组（不论普通词缀还是他族/同族精髓）一律剔除：冲突是下缀固有属性、与附加来源无关
        // （用户定调：所有下缀都存在冲突）。由此普通词缀与精髓词缀共享同一冲突判定（均由 AdditionType 身份派生），避免叠加互斥项。
        List<ModifierEntry> kept = new ArrayList<>();
        for (ModifierEntry entry : ModifiableStateOps.mods(equipmentStack)) {
            if (entry instanceof AffixMod a
                    && rolled.stream().anyMatch(e -> e instanceof AffixMod && AffixMod.conflicts((AffixMod) e, a, equipment))) {
                continue;
            }
            kept.add(entry);
        }
        ModifiableStateOps.setMods(equipmentStack, kept);

        Rarity rarity = ModifiableStateOps.rarity(equipmentStack);
        if (essence.upgrades()) {
            if (rarity != Rarity.MAGIC) {
                return false;
            }
            ModifiableStateOps.setRarity(equipmentStack, Rarity.RARE);
            List<ModifierEntry> mods = new ArrayList<>(ModifiableStateOps.mods(equipmentStack));
            mods.addAll(rolled);
            ModifiableStateOps.setMods(equipmentStack, mods);
            ModGenerator.applyFillRandomDown(level.getRandom(), equipmentStack, 0);
            return true;
        }
        if (rarity != Rarity.RARE) {
            return false;
        }
        if (ModGenerator.getDownModCount(equipmentStack) > 0) {
            ModGenerator.applyRemoveRandomDownMod(level, equipmentStack);
        }
        List<ModifierEntry> mods = new ArrayList<>(ModifiableStateOps.mods(equipmentStack));
        mods.addAll(rolled);
        ModifiableStateOps.setMods(equipmentStack, mods);
        AttributeApplicator.applyModValue(equipmentStack);
        return true;
    }

    /**
     * 构建「必定出现」的精髓词缀（宿主分支命中的加成），未映射的加成静默跳过。
     *
     * <p>冲突默认由 AdditionType 身份派生（同 AdditionType 常量 = 同组，跨命名空间互斥）；
     * 精髓无需求等级概念，词缀等级恒 0。数值保持范围态，由调用方 {@code roll} 落定。</p>
     */
    public static List<EssenceMod> essenceMods(EssenceState essence, ModifiableState state) {
        List<EssenceMod> result = new ArrayList<>();
        Map<String, List<Bonus>> matched = BranchExpr.collect(essence.branches(),
                state.types().stream().map(Identifier::getPath).collect(Collectors.toSet()));
        List<Bonus> bonuses = new ArrayList<>();
        for (List<Bonus> b : matched.values()) bonuses.addAll(b);
        for (Bonus bonus : bonuses) {
            // 落位是词缀自身的维度：每条精髓词缀各取各的（前缀 / 后缀），不再全族一刀切。
            // 精髓词缀必须有落位，缺失即数据声明不完整 —— 报错而非回落（回落即隐式决策）。
            Location location = bonus.location().orElseThrow(() -> new IllegalStateException(
                    "精髓词缀缺落位: " + essence.id() + " / " + bonus.type()));
            // bonus.type() 即 AdditionType 本身（不再经「字符串 → 效果」翻译表）。
            result.add(new EssenceMod(essence.id(), Addition.from(bonus.type(), bonus.min(), bonus.max()),
                    location, Optional.empty()));
        }
        return result;
    }

    private static EssenceState essenceOf(ItemStack essenceStack) {
        if (essenceStack.getItem() instanceof EssenceItem ei) {
            return EssenceRegistry.getById(ei.essenceId());
        }
        return null;
    }

    /** 宿主装备状态（非装备返回 {@code null}）。 */
    private static EquipmentState equipmentOf(ItemStack equipmentStack) {
        ModifiableState state = ModifiableStateOps.get(equipmentStack).orElse(null);
        return state instanceof EquipmentState equipment ? equipment : null;
    }
}
