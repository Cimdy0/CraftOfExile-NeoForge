package com.cimdy.coe.data.codec.item.mod.core.impl;

import com.cimdy.coe.data.codec.item.mod.core.AbstractMod;
import com.cimdy.coe.data.codec.item.mod.core.ModLayer;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.state.ModifiableState;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import net.minecraft.resources.Identifier;
import com.cimdy.coe.data.codec.item.mod.identity.Location;
import com.cimdy.coe.data.codec.item.mod.identity.ModConflict;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 下缀（前缀 / 后缀）—— 占据装备一条原生前后缀槽的词缀，在基类之上引入 <b>位置（{@link Location}）</b>
 * 与 <b>冲突路径（{@code conflict}，命名空间限定 {@link Identifier}，可空）</b>。
 *
 * <h2>冲突管辖</h2>
 * 冲突路径默认空（{@link Optional#empty()}），此时以本条词缀的<b>已注册身份</b>作种子：
 * 由输入方以一条已注册的 {@code AdditionRead} 构造的 {@link Addition}，其身份即该引用器的注册路径
 * {@code ref}（{@code ADDITION_MAP} 键），经 {@link Addition#identity} 直接取得，不经反向推导。
 * 同一注册路径 = 同一身份 = 同一冲突组（例如普通生命词缀与精髓 body 复用同一 {@code ModAdditions} 常量
 * 即得同一路径 → 已在同一冲突表即同表互斥）。
 * <p>{@link com.cimdy.coe.data.codec.item.addition.impl.MultiAddition} 组合类型以<b>主词（首 Part）</b>递归派生身份
 * （主词为已注册 {@code AdditionRead} 即取其 ref）；组合为空或主词亦无注册身份时回落为空，不参与冲突排除。</p>
 * 手动填写时 {@code conflict} 即冲突路径，可填「表路径」{@code coe:affix/conflict/<group>}
 * 或「AdditionType 路径」{@code coe:<addition>}，统一经 {@link ModConflict#resolveConflict} 解析为冲突目标。
 *
 * <p>派生：{@code CommonMod}（普通词缀，名称 = 阶名词）/
 * {@link EssenceMod}（精髓词缀，名称 = 「精髓」，冲突默认派生）。</p>
 */
public abstract class AffixMod extends AbstractMod implements RangedMod {

    private final Location location;
    /** 冲突路径（手动填写，命名空间限定 {@link Identifier}）；空 {@link Optional} 表示由 Addition 注册身份派生。 */
    private final Optional<Identifier> conflict;

    protected AffixMod(Addition addition, int level, Location location, Optional<Identifier> conflict) {
        super(addition, level);
        this.location = location == null ? Location.PREFIX : location;
        this.conflict = conflict;
    }

    /** 位置（前缀 / 后缀）：决定占哪条槽、是否与同组下缀互斥。 */
    public Location location() {
        return location;
    }

    /**
     * 冲突路径（手动填写，命名空间限定）：可填「表路径」（{@code coe:affix/conflict/<group>}）
     * 或「AdditionType 路径」（{@code coe:<addition>}）；空 {@link Optional} 表示「由 Addition 注册身份派生」。
     */
    public Optional<Identifier> conflict() {
        return conflict;
    }

    /**
     * 冲突目标表集合（部位感知，<b>判定权威入口</b>）：同 {@link #conflictTargets()}，
     * 但只取「对给定载体部位成立」的冲突表——未生效部位上的表不参与解析，
     * 从而避免全局表带来的<b>过度约束</b>。
     *
     * <p>返回<b>集合</b>而非单一路径：同一 Addition 可同部位归属多张表（源库不同词缀条目共享 stat），
     * 只取一张会漏判互斥。无注册身份且未显式填写路径时返回空集（不参与冲突排除）。
     *
     * @param carrier 载体；{@code null} 表示跳过部位判定（等价 {@link #conflictTargets()}）
     */
    public Set<Identifier> conflictTargets(ModifiableState carrier) {
        return (conflict.isPresent() ? conflict : addition().identity())
                .map(seed -> ModConflict.resolveConflicts(seed, carrier))
                .orElseGet(Set::of);
    }

    /** 冲突目标表集合（无载体：跳过部位判定）。 */
    public Set<Identifier> conflictTargets() {
        return conflictTargets(null);
    }

    /**
     * 冲突目标路径（部位感知）：取 {@link #conflictTargets} 中最先注册的一张。
     * <b>仅用于展示</b>（如图鉴打印组名）；互斥判定须走集合版 {@link #conflictTargets}。
     *
     * @param carrier 载体；{@code null} 表示跳过部位判定（等价 {@link #conflictTarget()}）
     */
    public Optional<Identifier> conflictTarget(ModifiableState carrier) {
        Set<Identifier> targets = conflictTargets(carrier);
        return targets.isEmpty() ? Optional.empty() : Optional.of(targets.iterator().next());
    }

    /**
     * 冲突目标路径（命名空间限定 {@link Identifier}，无载体）：冲突判定只有「查表」这一种方法 ——
     * 空轨以本条 Addition 的注册身份（{@link Addition#identity}）作种子，非空轨以填写路径作种子；
     * 二者统一解析为冲突目标表集合（表路径直接命中 / 成员命中所属表 / 否则自身路径），此处取其中最先注册的一张。
     * 无注册身份且未显式填写路径的 Addition 不参与冲突排除，返回 {@link Optional#empty()}。
     */
    public Optional<Identifier> conflictTarget() {
        return conflictTarget(null);
    }

    /**
     * 同位置内同冲突目标互斥判定：候选下缀与本条既有下缀是否互斥。
     * <b>冲突是下缀的固有属性，与附加来源无关</b> —— 普通词缀（候选池抽取）与精髓词缀（强制作用）共享同一
     * 冲突判定（均由 Addition 注册身份派生）。故经候选池加普通词缀时既须排除同目标的既有普通词缀，
     * 也须排除同目标的既有精髓词缀；反之亦然。符文无前后缀位置故不参与。
     */
    public static boolean conflicts(AffixMod candidate, AffixMod existing) {
        return conflicts(candidate, existing, null);
    }

    /**
     * 同位置内互斥判定（部位感知）：两词缀同前后缀位置、且<b>目标表集合相交</b>即互斥
     * （共享任意一张生效表 = 同族 = 互斥）。
     *
     * <p>判据取相交而非相等：一条词缀可归属多张表，相等判据会漏判「一方是另一方所属表之一」的情形。
     * 任一方目标集为空（无注册身份且未显式填写路径）即不参与排除。
     *
     * @param carrier 载体，用于筛选「本部位生效」的冲突表；{@code null} 表示跳过部位判定
     */
    public static boolean conflicts(AffixMod candidate, AffixMod existing, ModifiableState carrier) {
        if (existing.location() != candidate.location()) {
            return false;
        }
        Set<Identifier> existingTargets = existing.conflictTargets(carrier);
        if (existingTargets.isEmpty()) {
            return false;
        }
        Set<Identifier> candidateTargets = candidate.conflictTargets(carrier);
        for (Identifier table : existingTargets) {
            if (candidateTargets.contains(table)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ModLayer layer() {
        return location == Location.SUFFIX ? ModLayer.SUFFIX : ModLayer.PREFIX;
    }

    /** 位置写回：roll 命中后由管线按候选池键注入。 */
    public abstract AffixMod withLocation(Location location);

    @Override
    public abstract AffixMod withAddition(Addition addition);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof AffixMod that
                && getClass() == that.getClass()
                && location == that.location
                && level() == that.level()
                && Objects.equals(addition(), that.addition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), location, level(), addition());
    }
}
