package com.cimdy.coe.data.codec.support.meta;

import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * 解码期"当前数据来源"上下文（重构 3-5）。
 *
 * <p><b>为什么需要它</b>：表达式（{@code MathExpression}）深埋在 stat_entry / skill / modifier 等各层
 * codec 内部，解析失败时 {@code MathParser.ParseException} 只知道表达式串本身，不知道它出自哪个 JSON
 * 文件。逐层给 codec 传 {@code Identifier} 会污染全部 codec 签名（且违反"codec 静态工厂构造模式不变"的
 * 红线），因此改用线程局部的来源栈：加载器在解析一个资源前压入其 id，解析结束弹出，
 * 底层解析器只需 {@link #current()} 即可拿到来源。
 *
 * <p><b>线程模型</b>：数据加载走 {@code CompletableFuture.runAsync(..., bgExecutor)}，每个加载器在自己的
 * 线程内完成"压栈 → 解析 → 弹栈"闭环，故用 {@link ThreadLocal} 天然隔离。栈结构支持嵌套来源
 * （例如未来出现"数据文件内联引用另一文件"的场景）。
 *
 * <p><b>使用约定</b>：一律用 {@link #with(Identifier, Supplier)} / {@link #run(Identifier, Runnable)}，
 * 它们用 try-finally 保证弹栈；不要手工配对调用 push/pop。
 */
public final class DataSourceContext {

    private static final ThreadLocal<Deque<String>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private DataSourceContext() {
    }

    /** 在指定来源上下文中执行并返回结果（推荐入口）。 */
    public static <T> T with(Identifier source, Supplier<T> body) {
        return with(source == null ? null : source.toString(), body);
    }

    /** 在指定来源上下文中执行并返回结果（推荐入口）。 */
    public static <T> T with(String source, Supplier<T> body) {
        push(source);
        try {
            return body.get();
        } finally {
            pop();
        }
    }

    /** 在指定来源上下文中执行（无返回值）。 */
    public static void run(Identifier source, Runnable body) {
        with(source, () -> {
            body.run();
            return null;
        });
    }

    /**
     * 当前来源 id；无上下文时返回 {@code null}。
     * 供解析器在构造错误信息时调用，不应影响任何解析结果。
     */
    public static String current() {
        // ArrayDeque 迭代顺序为栈顶→栈底：跳过占位（null 来源压入的空串），取最近的有效来源。
        for (String s : STACK.get()) {
            if (s != null && !s.isEmpty()) return s;
        }
        return null;
    }

    /** 便于拼接日志：有来源时返回 {@code " [来源: coe:xxx]"}，否则空串。 */
    public static String describe() {
        String cur = current();
        return cur == null ? "" : " [来源: " + cur + "]";
    }

    private static void push(String source) {
        // 允许 null：此时压入占位，保证 pop 配对；current() 会跳过占位继续找上层来源。
        STACK.get().push(source == null ? "" : source);
    }

    private static void pop() {
        Deque<String> stack = STACK.get();
        if (!stack.isEmpty()) stack.pop();
        // 栈空时主动清理 ThreadLocal，避免线程池长期持有空容器。
        if (stack.isEmpty()) STACK.remove();
    }
}
