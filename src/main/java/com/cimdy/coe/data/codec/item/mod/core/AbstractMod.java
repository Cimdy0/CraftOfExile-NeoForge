package com.cimdy.coe.data.codec.item.mod.core;
import com.cimdy.coe.data.codec.item.mod.core.impl.AffixMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.RuneMod;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import java.util.List;

/**
 * 词缀基类 —— 承载全部词缀类型的共同维度：<b>一条加成（Addition + 区间 + 值）与词缀等级</b>。

 *
 * <p><b>刻意不含</b>名称与位置：二者不是所有词缀类型都具备（符文不渲染名称、基底与符文没有前后缀位置），
 * 由需要的派生类自行引入（{@link AffixMod} 引入 {@link Location} 与冲突值）。
 * 派生臂：{@link BaseMod}（基底）/ {@link RuneMod}（符文）/ {@link AffixMod}（下缀）</p>
 *
 * <p><b>一条词缀 = 一条 {@link Addition}</b>：需要「多条效果」时由 {@link Addition} 自身的组合类型承载
 * （{@code MultiAddition} 等），不在本基类层面开 {@code List<Addition>}。</p>
 *
 * <p>为何用类而非 record：Java record 不能继承类，而本体系要求「下缀继承基类」与
 * 「普通词缀 / 精髓词缀继承下缀」的多级派生，故基类与派生臂统一为普通类
 * （各自保留 {@code equals/hashCode/toString}）。</p>
 */
public abstract class AbstractMod implements ModifierEntry {

    /** 本条词缀的唯一加成（{@code null} = 无加成，如基底）。 */
    private final Addition addition;
    /** 词缀等级：参与「装备需求等级 = max(基底需求, 最高词缀等级)」。 */
    private final int level;

    protected AbstractMod(Addition addition, int level) {
        this.addition = addition;
        this.level = level;
    }

    public final Addition addition() {
        return addition;
    }

    @Override
    public final List<Addition> additions() {
        return addition == null ? List.of() : List.of(addition);
    }

    @Override
    public final int level() {
        return level;
    }
}
