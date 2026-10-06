package com.cimdy.coe.data.codec.item.mod.state;

import com.cimdy.coe.data.codec.item.state.CommonModifiableState;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeChecker;
import com.cimdy.coe.data.codec.item.mod.identity.SlotResolver;
import com.cimdy.coe.data.codec.item.mod.value.ModValue;
import com.cimdy.coe.data.codec.support.rarity.Rarity;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.item.ItemTooltipAssembler;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.support.value.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;
/**
 * 装备物品状态 —— 继承 {@link CommonModifiableState}，在通用维度（物等 / 稀有度 / 品质 / 词缀）之上
 * 叠加装备专属维度（类型链 / 标记 / 基底读取 / 孔位）。
 *
 * <p>由 {@code record} 改为普通类以承载 {@link CommonModifiableState} 基类（Java record 不能继承类），
 * 并保持与原 record 完全一致的 {@link #CODEC}、{@link #DEFAULT}、{@code equals/hashCode/toString} 语义。</p>
 *
 * <p><b>单轨 API</b>：仅保留唯一的规范 8 参构造器；全部「写入并返回新状态」的改写方法统一命名为
 * {@code withXxx}（与 {@link CommonModifiableState} 的通用契约一致）。旧 (wisdom, mirror) 布尔签名与
 * 缺品质 / 缺孔位的兼容构造器已全部移除，调用点一律走规范构造器。</p>
 */
public class EquipmentState extends CommonModifiableState {

    private final Map<String, Boolean> markers;
    private final Optional<EquipmentRead> equipment;
    private final List<EquipmentSocket> sockets;

    /** 标记 key 常量：避免散落字符串字面量。 */
    public static final String MARKER_WISDOM = "wisdom";
    public static final String MARKER_MIRROR = "mirror";

    /**
     * 唯一构造器（与旧 record 组件顺序一致，供 CODEC 与全部调用点使用）。
     * {@code types / mods} 为共享维度，已提升至 {@link CommonModifiableState} 基类持有，此处仅转发。
     */
    public EquipmentState(int level, Rarity rarity, List<Identifier> types, Map<String, Boolean> markers,
                          Optional<EquipmentRead> equipment, List<ModifierEntry> mods, int quality,
                          List<EquipmentSocket> sockets) {
        // 基底属性（phase 3a）作为 BaseMod 实例统一收入 mods：剥离传入列表中已有的 BaseMod（避免叠加 / 双重计数），
        // 再按「基底 + 品质」重新派生当前装备的基底条目追加尾部。所有构造路径（codec 解码 / withXxx / 初始化）
        // 都经此单一入口，保证 mods 列表始终含且只含一份基底表达。
        super(level, rarity, types, quality, rebuildMods(equipment, quality, mods));
        this.markers = markers;
        this.equipment = equipment;
        this.sockets = sockets;
    }

    /**
     * 把基底属性并回 mods：剥离已有 BaseMod（可能来自旧存档 / 传入副本），再按当前装备 + 品质重新派生。
     * 基底随装备模板与品质变化，故每次构造都重算；无装备（理论上不会发生）时保持传入列表不变。
     */
    private static List<ModifierEntry> rebuildMods(Optional<EquipmentRead> equipment, int quality, List<ModifierEntry> mods) {
        List<ModifierEntry> withoutBase = new ArrayList<>(mods.size() + 1);
        for (ModifierEntry m : mods) {
            if (!(m instanceof BaseMod)) {
                withoutBase.add(m);
            }
        }
        equipment.ifPresent(eq -> withoutBase.add(new BaseMod(eq.baseStatEntries(quality), 0)));
        return List.copyOf(withoutBase);
    }

    /** 装备基础属性（关联符键控快照）：由 {@link #equipment} 解析。 */
    public Base base() {
        return equipment.map(EquipmentRead::base).orElse(Base.EMPTY);
    }

    /** 装备基础属性：模板（字段声明）+ {@link EquipmentRead}（值）结合后的键控快照。 */
    public record Base(
            Map<String, ExpressionCodec.Value> stats,
            Map<String, ExpressionCodec.Value> reqs,
            Map<String, ModValue> implicit,
            List<SkillData> skills) {
        public static final Base EMPTY = new Base(Map.of(), Map.of(), Map.of(), List.of());
    }

    public static final EquipmentState DEFAULT =
            new EquipmentState(1, Rarity.NONE, List.of(), Map.of(), Optional.empty(), List.of(), 0, List.of());

    public static final Codec<EquipmentState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(EquipmentState::level),
            Rarity.CODEC.fieldOf("rarity").forGetter(EquipmentState::rarity),
            Identifier.CODEC.listOf().fieldOf("type").forGetter(EquipmentState::types),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("markers").forGetter(EquipmentState::markers),
            EquipmentRead.CODEC.optionalFieldOf("equipment").forGetter(EquipmentState::equipment),
            ModifierEntry.CODEC.listOf().fieldOf("mods").forGetter(EquipmentState::mods),
            Codec.INT.optionalFieldOf("quality", 0).forGetter(EquipmentState::quality),
            EquipmentSocket.CODEC.listOf().optionalFieldOf("sockets", List.of()).forGetter(EquipmentState::sockets)
    ).apply(instance, EquipmentState::new));

    // ---- 自身维度访问器 ----
    public Map<String, Boolean> markers() { return markers; }
    public Optional<EquipmentRead> equipment() { return equipment; }
    public List<EquipmentSocket> sockets() { return sockets; }

    // ---- 需求（键控 reqs：l / str / dex / int）----
    private int reqInt(String key) {
        ExpressionCodec.Value v = base().reqs().get(key);
        return v == null ? 0 : (int) Math.round(v.value().left().orElse(0.0));
    }

    public boolean hasOnlyStrength() { return has("str") && !has("dex") && !has("int"); }
    public boolean hasOnlyDexterity() { return !has("str") && has("dex") && !has("int"); }
    public boolean hasOnlyIntelligence() { return !has("str") && !has("dex") && has("int"); }
    public boolean hasStrength() { return has("str"); }
    public boolean hasDexterity() { return has("dex"); }
    public boolean hasIntelligence() { return has("int"); }
    public boolean hasStrengthAndDexterity() { return has("str") && has("dex") && !has("int"); }
    public boolean hasStrengthAndIntelligence() { return has("str") && !has("dex") && has("int"); }
    public boolean hasDexterityAndIntelligence() { return !has("str") && has("dex") && has("int"); }
    public boolean hasAllAttributes() { return has("str") && has("dex") && has("int"); }
    public boolean hasAnyAttributes() { return has("str") || has("dex") || has("int"); }
    public boolean has(String key) { return base().reqs().containsKey(key); }

    public int getRequireLevel() {
        // 装备需求等级 = max(基底需求等级, 全部词缀等级的最大值)；词缀等级恒 ≤ 物等（符文也参与取大）。
        int base = reqInt("l");
        int maxMod = mods().stream().mapToInt(ModifierEntry::level).max().orElse(0);
        return Math.max(base, maxMod);
    }
    public int getRequireStrength() { return reqInt("str"); }
    public int getRequireDexterity() { return reqInt("dex"); }
    public int getRequireIntelligence() { return reqInt("int"); }

    // ---- Type checks (delegated to ItemTypeChecker) ----
    public boolean isWeapon() { return ItemTypeChecker.isWeapon(this); }
    public boolean isArmor() { return ItemTypeChecker.isArmor(this); }
    public boolean isSword() { return ItemTypeChecker.isSword(this); }
    public boolean isBow() { return ItemTypeChecker.isBow(this); }
    public boolean isCrossBow() { return ItemTypeChecker.isCrossBow(this); }
    public boolean isMainHand() { return ItemTypeChecker.isMainHand(this); }
    public boolean isOffHand() { return ItemTypeChecker.isOffHand(this); }
    public boolean isShield() { return ItemTypeChecker.isShield(this); }
    public boolean isBuckler() { return ItemTypeChecker.isBuckler(this); }
    public boolean isHead() { return ItemTypeChecker.isHead(this); }
    public boolean isChest() { return ItemTypeChecker.isChest(this); }
    public boolean isLegs() { return ItemTypeChecker.isLegs(this); }
    public boolean isFeet() { return ItemTypeChecker.isFeet(this); }

    public boolean hasAttackDamage() { return ItemTypeChecker.hasAttackDamage(this); }
    public boolean hasAttackSpeed() { return ItemTypeChecker.hasAttackSpeed(this); }
    public boolean hasAttackCritChance() { return ItemTypeChecker.hasAttackCritChance(this); }
    public boolean hasWeaponSpirit() { return ItemTypeChecker.hasWeaponSpirit(this); }
    public boolean hasArmor() { return ItemTypeChecker.hasArmor(this); }
    public boolean hasEvasion() { return ItemTypeChecker.hasEvasion(this); }
    public boolean hasEnergy() { return ItemTypeChecker.hasEnergy(this); }
    public boolean hasArmorAndEvasion() { return ItemTypeChecker.hasArmorAndEvasion(this); }
    public boolean hasArmorAndEnergy() { return ItemTypeChecker.hasArmorAndEnergy(this); }
    public boolean hasEvasionAndEnergy() { return ItemTypeChecker.hasEvasionAndEnergy(this); }
    public boolean hasBlockChance() { return ItemTypeChecker.hasBlockChance(this); }
    public boolean hasArmorSpirit() { return ItemTypeChecker.hasArmorSpirit(this); }

    /** 结界（Runic Ward）分层判定。 */
    public boolean hasWard() { return ItemTypeChecker.hasWard(this); }

    // ---- Slot (delegated to SlotResolver) ----
    public EquipmentSlotGroup getSlot() { return SlotResolver.getSlot(this); }

    // ---- Initialization (delegated to StateInitializer) ----
    public void applyInitialization(RandomSource random, ItemStack stack, int level) {
        StateInitializer.applyInitialization(this, random, stack, level);
    }

    // ---- State calculations (delegated to StateCalculator) ----
    public EquipmentState reset() { return StateCalculator.reset(this); }

    public double getBaseAttackDamage() { return StateCalculator.getBaseAttackDamage(this); }
    public double getBaseAttackCritChance() { return StateCalculator.getBaseAttackCritChance(this); }
    public double getBaseAttackSpeed() { return StateCalculator.getBaseAttackSpeed(this); }
    public double getBaseReloadTime() { return StateCalculator.getBaseReloadTime(this); }
    public double getReloadTime() { return StateCalculator.getReloadTime(this); }
    public boolean hasReloadTime() { return StateCalculator.hasReloadTime(this); }
    public double getAttackDamage() { return StateCalculator.getAttackDamage(this); }
    public double getAttackCritChance() { return StateCalculator.getAttackCritChance(this); }
    public double getAttackSpeed() { return StateCalculator.getAttackSpeed(this); }
    public boolean attackDamageChanged() { return StateCalculator.attackDamageChanged(this); }
    public boolean attackSpeedChanged() { return StateCalculator.attackSpeedChanged(this); }
    public boolean attackCritChanceChanged() { return StateCalculator.attackCritChanceChanged(this); }
    public boolean spiritChanged() { return StateCalculator.spiritChanged(this); }
    public boolean reloadTimeChanged() { return StateCalculator.reloadTimeChanged(this); }
    public EquipmentState setLevelRequirement(int levelRequirement) { return StateCalculator.setLevelRequirement(this, levelRequirement); }
    public int getBaseLevelRequirement() { return StateCalculator.getBaseLevelRequirement(this); }
    public boolean armorChanged() { return StateCalculator.armorChanged(this); }
    public boolean evasionChanged() { return StateCalculator.evasionChanged(this); }
    public boolean energyChanged() { return StateCalculator.energyChanged(this); }
    public double getArmor() { return StateCalculator.getArmor(this); }
    public double getEvasion() { return StateCalculator.getEvasion(this); }
    public double getEnergy() { return StateCalculator.getEnergy(this); }
    public double getBaseArmor() { return StateCalculator.getBaseArmor(this); }
    public double getBaseEvasion() { return StateCalculator.getBaseEvasion(this); }
    public double getBaseEnergy() { return StateCalculator.getBaseEnergy(this); }
    public double getSpirit() { return StateCalculator.getSpirit(this); }
    public double getAttackBlock() { return StateCalculator.getAttackBlock(this); }

    // ---- 标记访问（统一走 markers Map，未来新标记无需加字段）----
    public boolean wisdom() { return markers.getOrDefault(MARKER_WISDOM, false); }
    public boolean mirror() { return markers.getOrDefault(MARKER_MIRROR, false); }

    // ---- 单轨改写 API：统一 withXxx（写入并返回新状态）----

    @Override
    public EquipmentState withType(List<Identifier> types) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, sockets);
    }

    @Override
    public EquipmentState withRarity(Rarity rarity) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, sockets);
    }

    public EquipmentState withWisdom(boolean wisdom) {
        Map<String, Boolean> m = new HashMap<>(markers);
        m.put(MARKER_WISDOM, wisdom);
        return new EquipmentState(level, rarity, types, Map.copyOf(m), equipment, mods, quality, sockets);
    }

    public EquipmentState withMirror(boolean mirror) {
        Map<String, Boolean> m = new HashMap<>(markers);
        m.put(MARKER_MIRROR, mirror);
        return new EquipmentState(level, rarity, types, Map.copyOf(m), equipment, mods, quality, sockets);
    }

    /** 协变改写词缀（共享维度，实现自 {@link CommonModifiableState#withMods}）。 */
    @Override
    public EquipmentState withMods(List<ModifierEntry> mods) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, sockets);
    }

    /** 品质（0–20）：通用标量，钳制权威统一在 {@link CommonModifiableState} 构造器。 */
    @Override
    public EquipmentState withQuality(int quality) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, sockets);
    }

    @Override
    public EquipmentState withLevel(int level) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, sockets);
    }

    /** 装备专有初始化：写入类型链 + 智慧标记 + 需求计算（仅在稀有度为缺省 NONE 时触发）。 */
    @Override
    public EquipmentState initialize(RandomSource random, ItemStack stack, int level, Rarity rarity, List<Identifier> types) {
        EquipmentState state = withType(types);
        if (state.rarity().getId() == 0) {
            state = state.withWisdom(true);
            state = state.withRarity(rarity);
            state.applyInitialization(random, stack, level);
        }
        return state;
    }

    public EquipmentState withSockets(List<EquipmentSocket> sockets) {
        return new EquipmentState(level, rarity, types, markers, equipment, mods, quality, List.copyOf(sockets));
    }

    public void addRequireComponent(List<Component> tooltip, Player player) {
        ItemTooltipAssembler.addAttributeRequirementTooltip(tooltip, player,
                new int[]{getRequireStrength(), getRequireDexterity(), getRequireIntelligence(), getRequireLevel()},
                new boolean[]{false, false, false, false});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EquipmentState that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return markers.equals(that.markers) && equipment.equals(that.equipment)
                && sockets.equals(that.sockets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), markers, equipment, sockets);
    }

    @Override
    public String toString() {
        return "EquipmentState{level=" + level + ", rarity=" + rarity + ", types=" + types + ", markers=" + markers
                + ", equipment=" + equipment + ", mods=" + mods + ", quality=" + quality + ", sockets=" + sockets + "}";
    }
}
