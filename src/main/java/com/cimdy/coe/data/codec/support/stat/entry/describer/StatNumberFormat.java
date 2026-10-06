package com.cimdy.coe.data.codec.support.stat.entry.describer;

import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 数值本地化格式化（P3）：在保留旧 describe 的 {@code trim} / {@code signedNumber} 语义的前提下，
 * 叠加 {@link NumberFormat} 的千分位分组。en / zh 在符号 / 百分号 / trim 上保持一致；
 * CJK 万 / 億 分组（locale=zh）显式 OUT OF SCOPE（TODO），当前统一走 NumberFormat 的 locale 分组。
 *
 * <p>规则：取 trim 后的幅度字符串（与 {@code ModifierStatEffect.trim} 同语义），用
 * {@link NumberFormat#getInstance(Locale)} 做千分位分组；正号 {@code +} 仍由调用方
 * （旧 signedNumber 语义）显式拼接，本类不负责符号。
 */
public final class StatNumberFormat {

    private StatNumberFormat() {
    }

    /** 非负幅度格式化（不附加正号；负号的 {@code -} 由 trim 自带）。 */
    public static String format(double v, Locale locale) {
        return group(trim(v), locale);
    }

    /** 带正号格式化（正数前置 {@code +}，负数沿用 trim 自带 {@code -}）。 */
    public static String formatSigned(double v, Locale locale) {
        return (v >= 0 ? "+" : "") + group(trim(v), locale);
    }

    /** 旧 describe 的 trim 语义：整数去尾零、非整数保留 {@code String.valueOf}。 */
    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    /** 对幅度字符串做 locale 千分位分组；无法解析（如科学计数法）时原样返回。 */
    private static String group(String magnitude, Locale locale) {
        try {
            double d = Double.parseDouble(magnitude);
            return NumberFormat.getInstance(locale).format(d);
        } catch (NumberFormatException e) {
            return magnitude;
        }
    }
}
