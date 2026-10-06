package com.cimdy.coe.ui.widget.compose.codex;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 类型描述（计划 §一 第 1 层）：声明「这一类条目在界面上渲染成什么样」。
 *
 * <p>由 {@link CodexSource#schema()} 返回，被列表行 / 详情面板消费。
 * 全部取值都是 <b>{@link Function} 注入</b>——类型结构固定、条目动态，每条目的展示内容
 * 在渲染时才从该条目取，组件不预建任何条目视图。
 *
 * <pre>{@code
 * CodexSchema<ItemView> s = CodexSchema.<ItemView>of()
 *         .label(ItemView::name)                       // 列表行 / 详情主名
 *         .icon(ItemView::icon)                        // 列表行图标（可空）
 *         .column(Component.translatable("ui.coe.items"), v -> Component.literal(v.count()))
 *         .field(Component.translatable("ui.coe.info"), ItemView::desc);
 * }</pre>
 *
 * <p>链式 setter 返回 {@code this}，访问器 {@link #label(Object)}/{@link #icon(Object)}/
 * {@link #columns()}/{@link #fields()} 供消费方逐条目取值。
 */
public class CodexSchema<T> {

    private Function<T, Component> labelFn = e -> Component.empty();
    private Function<T, ItemStack> iconFn;   // 可空：不显示图标
    private final List<Column<T>> columns = new ArrayList<>();
    private final List<Field<T>> fields = new ArrayList<>();

    private CodexSchema() {}

    public static <T> CodexSchema<T> of() {
        return new CodexSchema<>();
    }

    // ------------------------------------------------------------------
    // 配置（链式）
    // ------------------------------------------------------------------

    /** 主名取值：列表行文字 / 详情标题。 */
    public CodexSchema<T> label(Function<T, Component> f) {
        this.labelFn = f == null ? (e -> Component.empty()) : f;
        return this;
    }

    /** 图标取值：列表行物品图标；不调则 {@link #icon(Object)} 返回 {@link ItemStack#EMPTY}。 */
    public CodexSchema<T> icon(Function<T, ItemStack> f) {
        this.iconFn = f;
        return this;
    }

    /** 追加一个列表列（表头 + 取值函数）。 */
    public CodexSchema<T> column(Component header, Function<T, Component> value) {
        columns.add(new Column<>(header, value));
        return this;
    }

    /** 追加一个详情字段（标签 + 取值函数）。 */
    public CodexSchema<T> field(Component label, Function<T, Component> value) {
        fields.add(new Field<>(label, value));
        return this;
    }

    // ------------------------------------------------------------------
    // 消费方访问
    // ------------------------------------------------------------------

    /** 取条目的主名（永不返回 null）。 */
    public Component label(T e) {
        Component c = labelFn.apply(e);
        return c == null ? Component.empty() : c;
    }

    /** 取条目的图标（未配置图标 / 取值为 null 时返回 {@link ItemStack#EMPTY}）。 */
    public ItemStack icon(T e) {
        if (iconFn == null || e == null) return ItemStack.EMPTY;
        ItemStack s = iconFn.apply(e);
        return s == null ? ItemStack.EMPTY : s;
    }

    /** 列表列（不可变视图）。 */
    public List<Column<T>> columns() {
        return Collections.unmodifiableList(columns);
    }

    /** 详情字段（不可变视图）。 */
    public List<Field<T>> fields() {
        return Collections.unmodifiableList(fields);
    }

    // ------------------------------------------------------------------
    // 记录类型
    // ------------------------------------------------------------------

    /** 一个列表列：表头 + 取值。 */
    public record Column<T>(Component header, Function<T, Component> value) {}

    /** 一个详情字段：标签 + 取值。 */
    public record Field<T>(Component label, Function<T, Component> value) {}
}
