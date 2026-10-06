package com.cimdy.coe.data.codec.item.currency;

import com.cimdy.coe.data.codec.support.rarity.Rarity;

import java.util.Optional;

/**
 * 通货操作（数据驱动）。仅靠类型 + 少量可选参数描述一次工艺，
 * 具体执行委托给 {@link OpType} 常量自带的 {@code apply}/{@code guard}，
 * 由 {@link com.cimdy.coe.item.currency.CurrencyApplier} 统一分发。
 * 不手算任何属性——只改写物品状态，随后由现有 Stat 层重算。
 *
 * <p>{@code amount} 是通用「幅值」槽位，语义随动词而定：
 * 品质类 = 计划增量（为空则按 {@code QualityOps} 的物品等级策略自动取值）、
 * 技能槽类 = 目标槽位数、开孔类 = 不使用。</p>
 */
public record Operation(
        OpType type,
        Optional<Rarity> targetRarity,
        Optional<Integer> addMods,
        Optional<Boolean> keepExisting,
        Optional<Integer> removeMods,
        Optional<Integer> minModTier,
        Optional<Integer> amount) {

    private static Operation of(OpType type) {
        return new Operation(type, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static Operation of(OpType type, int value) {
        return new Operation(type, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), value > 0 ? Optional.of(value) : Optional.empty());
    }

    private static Operation of(OpType type, Rarity rarity, int minModTier) {
        return new Operation(type, Optional.of(rarity), Optional.empty(), Optional.empty(),
                Optional.empty(), minModTier > 0 ? Optional.of(minModTier) : Optional.empty(), Optional.empty());
    }

    /** 带词缀等级下限的操作（minModTier）。 */
    private static Operation ofTier(OpType type, int minModTier) {
        return new Operation(type, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), minModTier > 0 ? Optional.of(minModTier) : Optional.empty(), Optional.empty());
    }

    public static Operation none() {
        return of(OpType.NONE);
    }

    public static Operation upgrade(Rarity r) {
        return of(OpType.UPGRADE_RARITY, r, 0);
    }

    public static Operation rerollMods() {
        return of(OpType.REROLL_MODS);
    }

    public static Operation rerollValues() {
        return of(OpType.REROLL_VALUES);
    }

    public static Operation addMod() {
        return of(OpType.ADD_MOD);
    }

    public static Operation removeMod() {
        return of(OpType.REMOVE_MOD);
    }

    public static Operation scrub() {
        return of(OpType.SCRUB);
    }

    /** 知识卷轴：全局随机生成一件装备（仅基底属性，无词缀）。 */
    public static Operation generate() {
        return of(OpType.GENERATE);
    }

    public static Operation chance() {
        return of(OpType.CHANCE_UPGRADE);
    }

    public static Operation mirror() {
        return of(OpType.MIRROR);
    }

    /** 替换一条词缀（混沌石「改一条」/ 高阶改造「增一减一」）；不带词缀等级下限。 */
    public static Operation swapMod() {
        return swapMod(0);
    }

    /** 替换一条词缀，新增那条受 {@code minModTier} 词缀等级下限约束（POE2 分阶通货）。 */
    public static Operation swapMod(int minModTier) {
        return ofTier(OpType.SWAP_MOD, minModTier);
    }

    /** 升阶（含词缀等级下限）：阶动作与基础一致，仅新增词缀受 {@code minModTier} 约束。 */
    public static Operation upgrade(Rarity r, int minModTier) {
        return of(OpType.UPGRADE_RARITY, r, minModTier);
    }

    /** 新增一条词缀，受 {@code minModTier} 词缀等级下限约束。 */
    public static Operation addMod(int minModTier) {
        return ofTier(OpType.ADD_MOD, minModTier);
    }

    /** 重掷词缀，新增词缀受 {@code minModTier} 词缀等级下限约束。 */
    public static Operation rerollMods(int minModTier) {
        return ofTier(OpType.REROLL_MODS, minModTier);
    }

    // ---- 品质 / 孔（通用设施，按目标域自动分派）----

    /** 增加品质：增量按目标物品的策略自动取值（装备 / 药剂按物品等级递减，技能石恒 5%）。 */
    public static Operation addQuality() {
        return of(OpType.ADD_QUALITY);
    }

    /** 增加品质：固定增量（如棱光石恒给技能石 5%）。 */
    public static Operation addQuality(int amount) {
        return of(OpType.ADD_QUALITY, amount);
    }

    /** 将技能石辅助槽数置为 {@code target}（POE2 多阶工匠石：3 / 4 / 5）。 */
    public static Operation setSkillSockets(int target) {
        return of(OpType.SET_SOCKETS, target);
    }

    /** 给装备开一个物品孔（符文 / 魂核 / 其它 augment 共用；上限由物品类型决定）。 */
    public static Operation addSocket() {
        return of(OpType.ADD_SOCKET);
    }

    /** 通用幅值：语义随动词而定（见类注释）。 */
    public int amountOr(int fallback) {
        return amount.orElse(fallback);
    }
}
