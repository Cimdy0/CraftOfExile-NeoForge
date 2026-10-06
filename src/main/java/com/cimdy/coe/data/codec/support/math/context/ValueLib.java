package com.cimdy.coe.data.codec.support.math.context;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 命名值注册表（值模型统一内核 · 强复用 / 随意引用落点）。
 * <p>
 * 以 {@link Identifier} 为键（非 String）：命名值由<b>数据包经数据生成写入缓存</b>、运行期重载读回，
 * 路径全程以 {@code <namespace>:<path>} 表达（除个别特例外一律 Identifier）。任何位置
 * （condition / amount / 另一个值 / 技能参数 {@code $name}）经 {@link RefExpr} 的 {@code ref:<id>} 随意引用，
 * 一处定义、处处复用，且可跨数据包覆盖（后注册覆盖先注册）。
 * <p>
 * 不做内容寻址：键由作者显式给定，不哈希表达式体；重载时清盘重灌，保证数据包覆盖语义。
 */
public final class ValueLib {
    private static final Map<Identifier, ValueExpr> NAMED = new LinkedHashMap<>();

    private ValueLib() {}

    /** 注册一个命名值（覆盖同键既有项）。数据包重载每轮先 {@link #clear()} 再逐条灌入。 */
    public static void register(Identifier key, ValueExpr expr) {
        NAMED.put(key, expr);
    }

    /** 批量灌入（数据包重载使用）。 */
    public static void loadAll(Map<Identifier, ValueExpr> map) {
        NAMED.putAll(map);
    }

    /** 取命名值；无则返回 {@code null}（{@link RefExpr} 据此回落 0）。 */
    public static ValueExpr get(Identifier key) {
        return NAMED.get(key);
    }

    public static boolean has(Identifier key) {
        return NAMED.containsKey(key);
    }

    /** 重载清盘：移除全部（同键后灌入者覆盖）。 */
    public static void clear() {
        NAMED.clear();
    }
}
