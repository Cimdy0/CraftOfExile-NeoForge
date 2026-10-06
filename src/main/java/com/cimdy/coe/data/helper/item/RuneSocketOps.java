package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentSocket;
import com.cimdy.coe.data.codec.item.mod.state.EquipmentState;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.tiered.Bonus;
import com.cimdy.coe.data.codec.item.tiered.Tier;
import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.state.RuneState;
import com.cimdy.coe.data.helper.mod.AttributeApplicator;
import com.cimdy.coe.item.special.RuneItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Optional;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
import com.cimdy.coe.data.codec.item.mod.core.impl.RuneMod;
/**
 * 符文镶嵌操作（通用设施，复用原生词缀管线）。
 *
 * <p>镶嵌规则（PoE2 实证）：符文填入装备的<b>空 augment 孔</b>；加成按宿主分支结算——
 * 取 {@code other}（全局通用行）+ 宿主具体分支（martial_weapon / wand_or_staff / armour），
 * 各条 {@link Bonus#type()} 即原生 {@link AdditionType}，作为 {@code RUNE} 层词条落定（符文不是下缀，
 * 不占前后缀档位，也无落位），仍与原生词缀走同一条 {@link AttributeApplicator} 结算管线
 * （{@code mod} 字段仅作来源标记，不进 tooltip）。</p>
 *
 * <p><b>数值口径</b>：符文的 min~max <b>不是取值范围</b>，而是「附加 X 到 Y 点伤」这一伤害区间本身，
 * 故按既定中值口径取 {@code (min+max)/2} 直接落定，<b>不做随机</b>（与精髓词缀作为前后缀需参与 roll 不同）。</p>
 *
 * <p>Masterwork 符文：升级插槽中已有的非 Masterwork / 非 Perfect 符文到下一阶
 * （Lesser→Base→Greater→Perfect），重写该孔 content 并重算 ENCHANT 条目。</p>
 */
public final class RuneSocketOps {

    private RuneSocketOps() {
    }

    /** 是否可镶嵌（不改动栈，客户端预览 / 守卫用）。 */
    public static boolean canSocket(ItemStack equipmentStack, ItemStack runeStack) {
        RuneState rune = runeOf(runeStack);
        if (rune == null) {
            return false;
        }
        EquipmentState equipment =ModifiableStateOps.equipment(equipmentStack);
        if (equipment == null || !SocketOps.canHoldSockets(equipment)) {
            return false;
        }
        if (isMasterwork(rune)) {
            return findUpgradableSocket(equipment).isPresent();
        }
        return findEmptySocket(equipment).isPresent();
    }

    /** 执行镶嵌（服务端权威）。成功返回 {@code true}（调用方负责 consume 符文）。 */
    public static boolean trySocket(ItemStack equipmentStack, ItemStack runeStack) {
        RuneState rune = runeOf(runeStack);
        if (rune == null) {
            return false;
        }
        EquipmentState equipment =ModifiableStateOps.equipment(equipmentStack);
        if (equipment == null || !SocketOps.canHoldSockets(equipment)) {
            return false;
        }

        if (isMasterwork(rune)) {
            return upgradeRune(equipmentStack, equipment);
        }

        int idx = findEmptySocket(equipment).orElse(-1);
        if (idx < 0) {
            return false;
        }

        String runeId = rune.id();
        writeSocketContent(equipmentStack, equipment, idx, runeId);

        List<ModifierEntry> mods = new ArrayList<>(ModifiableStateOps.mods(equipmentStack));
        Map<String, List<Bonus>> matched = BranchExpr.collect(rune.branches(),
                equipment.types().stream().map(Identifier::getPath).collect(Collectors.toSet()));
        for (List<Bonus> b : matched.values()) {
            for (Bonus bonus : b) {
                addBonus(mods, runeId, rune, bonus);
            }
        }

        ModifiableStateOps.setMods(equipmentStack, mods);
        AttributeApplicator.applyModValue(equipmentStack);
        return true;
    }

    /** 一条符文加成落 RUNE 层：数值取 min~max 中值（符文双值是点伤区间而非取值范围，不随机）。 */
    private static void addBonus(List<ModifierEntry> mods, String runeId, RuneState rune, Bonus bonus) {
        // bonus.type() 即 AdditionType 本身（不再经「字符串 → 效果」翻译表）：
        // 引用写错在编译期即暴露，而非运行时静默跳过。
        Addition addition = RuneMod.midValue(Addition.from(bonus.type(), bonus.min(), bonus.max()));
        mods.add(new RuneMod(runeId, addition, rune.levelReq()));
    }

    /** Masterwork：升级插槽中首个可升阶的符文。 */
    private static boolean upgradeRune(ItemStack equipmentStack, EquipmentState equipment) {
        int idx = findUpgradableSocket(equipment).orElse(-1);
        if (idx < 0) {
            return false;
        }
        Identifier oldContent = equipment.sockets().get(idx).content().orElse(null);
        if (oldContent == null) {
            return false;
        }
        String oldRuneId = oldContent.getPath();
        RuneState oldRune = RuneRegistry.getById(oldRuneId);
        if (oldRune == null) {
            return false;
        }
        Tier nextTier = nextTier(oldRune.tier());
        if (nextTier == null) {
            return false; // Perfect 不可再升
        }
        String newRuneId = RuneState.idOf(oldRune.name(), nextTier);
        RuneState newRune = RuneRegistry.getById(newRuneId);
        if (newRune == null) {
            return false;
        }

        // 移除旧符文条目：新体系走 RuneMod.source，旧存档为 CommonMod（mod 字段即来源 id）
        List<ModifierEntry> mods = new ArrayList<>();
        for (ModifierEntry entry : ModifiableStateOps.mods(equipmentStack)) {
            String src = entry instanceof RuneMod rune ? rune.source()
                    : (entry instanceof CommonMod nm ? nm.mod() : null);
            if (!oldRuneId.equals(src)) {
                mods.add(entry);
            }
        }
        // 写新符文条目（other + 宿主分支）
        Map<String, List<Bonus>> matched = BranchExpr.collect(newRune.branches(),
                equipment.types().stream().map(Identifier::getPath).collect(Collectors.toSet()));
        for (List<Bonus> b : matched.values()) {
            for (Bonus bonus : b) {
                addBonus(mods, newRuneId, newRune, bonus);
            }
        }

        writeSocketContent(equipmentStack, equipment, idx, newRuneId);
        ModifiableStateOps.setMods(equipmentStack, mods);
        AttributeApplicator.applyModValue(equipmentStack);
        return true;
    }

    private static void writeSocketContent(ItemStack stack, EquipmentState equipment, int idx, String runeId) {
        List<EquipmentSocket> sockets = new ArrayList<>(equipment.sockets());
        sockets.set(idx, EquipmentSocket.of(EquipmentSocket.AUGMENT,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, runeId)));
        stack.set(DataComponentRegistry.MODIFIABLE_STATE, equipment.withSockets(sockets));
    }

    private static Optional<Integer> findEmptySocket(EquipmentState equipment) {
        for (int i = 0; i < equipment.sockets().size(); i++) {
            if (equipment.sockets().get(i).isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static Optional<Integer> findUpgradableSocket(EquipmentState equipment) {
        for (int i = 0; i < equipment.sockets().size(); i++) {
            Optional<Identifier> content = equipment.sockets().get(i).content();
            if (content.isEmpty()) {
                continue;
            }
            RuneState s = RuneRegistry.getById(content.get().getPath());
            if (s == null || isMasterwork(s) || nextTier(s.tier()) == null) {
                continue;
            }
            return Optional.of(i);
        }
        return Optional.empty();
    }

    private static RuneState runeOf(ItemStack runeStack) {
        if (runeStack.getItem() instanceof RuneItem ri) {
            return RuneRegistry.getById(ri.runeId());
        }
        return null;
    }

    /**
     * 下一阶（升阶用）：{@code LESSER → NORMAL → GREATER → PERFECT}；
     * 完美（{@link Tier#PERFECT}）与特殊级（{@link Tier#SPECIAL}）不可再升，返回 {@code null}。
     * 阶序语义由 {@link Tier} 承载，不再靠字符串字面量比较。
     */
    private static Tier nextTier(Tier tier) {
        return switch (tier) {
            case LESSER -> Tier.NORMAL;
            case NORMAL -> Tier.GREATER;
            case GREATER -> Tier.PERFECT;
            default -> null; // PERFECT / SPECIAL：不可再升
        };
    }

    /** Masterwork 升级符文：按名称判定（单阶族，不走阶序）。 */
    private static boolean isMasterwork(RuneState rune) {
        return rune.name().getPath().equalsIgnoreCase("masterwork");
    }
}
