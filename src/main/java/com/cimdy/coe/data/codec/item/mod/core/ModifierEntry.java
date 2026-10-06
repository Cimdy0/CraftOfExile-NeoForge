package com.cimdy.coe.data.codec.item.mod.core;
import com.cimdy.coe.data.codec.item.mod.core.impl.BaseMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.CommonMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.EssenceMod;
import com.cimdy.coe.data.codec.item.mod.core.impl.RuneMod;
import com.cimdy.coe.data.codec.item.addition.impl.MultiAddition;
import com.cimdy.coe.data.codec.item.mod.identity.Addition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;
import java.util.Optional;

/**
 * 修饰词统一入口类型：所有修饰系统（普通词缀 / 附魔 / 工作台工艺排除）都流经同一个
 * {@code ModifierEntry} 列表与一条 {@code ModifierPipeline}（见 {@code ModifierStage}）。
 *
 * <p>本项目在 unnamed module 下编译（无 module-info.java），密封类的 permitted 子类必须同包，
 * 而本类型的臂横跨 {@code mod} 与 {@code vanilla} 两个包，故采用普通接口。臂（实现类）：
 * <ul>
 *   <li>{@link CommonMod} — 普通词缀（已落地）；</li>
 *   <li>{@link EnchantModifier.Value} — 附魔（共生层，行为不变）；</li>
 * </ul>
 *
 * <h2>多态效应暴露：{@link #additions()}</h2>
 * 各臂自实现（普通词缀返回其 {@code additions}、附魔返回其 {@code effects}），
 * 消费端经本接口多态访问器提取，不对臂做运行时类型判别。新增臂 = 覆写本方法，消费端零改动。
 *
 * <p>后续工艺系统通过新增 {@code ModifierStage} 臂接入管线，无需改动本类型的核心。
 */
public interface ModifierEntry {

    /**
     * 分发式 MapCodec：按 {@code type} 字段（注册表名）派发到 {@link ModifierEntries} 中登记的
     * 各臂 Codec。复用 vanilla 的 {@code Codec.STRING.dispatchMap} 注册派遣机制
     * （与 {@code StatEntry} / {@code MathExpression} 同一套范式），而非手写 instanceof 编解码。
     */
    MapCodec<ModifierEntry> MAP_CODEC = Codec.STRING.dispatchMap("type",
            e -> ModifierEntries.nameOf(e.getType()),
            ModifierEntries::codecFor);

    /** 扁平 Codec：多态词缀统一形态，由 {@link #MAP_CODEC} 派发。 */
    Codec<ModifierEntry> CODEC = MAP_CODEC.codec();

    /**
     * 本修饰词贡献的 {@link Addition} 列表（多态暴露）。
     *
     * <p>默认空 —— 载体型臂（如未来的纯标记臂）可无 Addition；
     * 有实际加成语义的臂（普通词缀 / 附魔）覆写返回自身持有的词缀列表。
     */
    default List<Addition> additions() {
        return List.of();
    }

    /**
     * 本修饰词的<b>唯一</b> {@link Addition}（无加成时为 {@code null}）。
     *
     * <p>一条词缀就是一条加成 —— 需要「多条效果」时由 {@link Addition} 自身的组合类型承载
     * （{@code MultiAddition} 等），而非在本接口层面开 {@code List<Addition>}。
     * 本接口仅暴露唯一加成；{@link #additions()} 返回其单元素列表（无加成时为空）。</p>
     */
    default Addition addition() {
        List<Addition> list = additions();
        return list.isEmpty() ? null : list.get(0);
    }

    /** 数值：{@code Addition#getValue()}（无加成时为 0）。 */
    default double value() {
        Addition a = addition();
        return a == null ? 0.0 : a.getValue();
    }

    /**
     * 词缀等级：该条词缀自身的需求等级。
     * 装备需求等级 = max(基底需求等级, 全部词缀等级的最大值)（见 {@code EquipmentState#getRequireLevel}）。
     */
    default int level() {
        return 0;
    }

    /**
     * 词缀层（{@link ModLayer}）：渲染分组与「是否计入前后缀档位」的判据。
     * 默认 {@link ModLayer#PREFIX}（历史数据中前后缀词缀占绝大多数）。
     */
    default ModLayer layer() {
        return ModLayer.PREFIX;
    }

    /**
     * 渲染名（POE 风格词缀头）。返回空表示<b>不渲染名称行</b>（符文即如此）。
     * 默认空 —— 各臂按需覆写：普通词缀出阶名词、精髓词缀出「精髓」、基底出「基底」。
     */
    default Optional<MutableComponent> name() {
        return Optional.empty();
    }

    /**
     * 分发类型：每个可序列化臂（{@link CommonMod} / {@link RuneMod} / {@link EssenceMod} / {@link BaseMod}）
     * 覆写为 {@link ModifierEntries} 中登记的自身 {@link Type}；非序列化臂
     * （如 {@link com.cimdy.coe.data.codec.vanilla.EnchantModifier.Value}）不经由本 Codec 落盘，沿用默认 {@code null}。
     */
    default Type<?> getType() {
        return null;
    }

    /** 分发类型载体：每个可序列化臂在 {@link ModifierEntries} 登记一个自身 {@link MapCodec}。 */
    record Type<T extends ModifierEntry>(MapCodec<T> codec) {
    }
}
