package com.cimdy.coe.ui.widget.component.assoc;

/**
 * {@link Association}{@code <Double>} 的数值特化：让「强度」真正可插值。
 *
 * <p>基类 {@link Association#blend} 对不可插值的类型只能忽略强度、原样传播；
 * 数值类型则可以在「旧值」与「变换后新值」之间按强度线性插值：
 *
 * <ul>
 *   <li>{@code strength == 1} → 足额传播（新值）；</li>
 *   <li>{@code 0 < strength < 1} → 传播 {@code old + (new - old) * strength}，即「部分跟随」；
 *       在连续拖动里表现为「弱耦合」——目标值平滑逼近源，而非瞬间对齐。</li>
 * </ul>
 *
 * <p>Bar / 滑块这类连续数值联动用本类即可得到可演示的「可控加强」手感。
 */
public class DoubleAssociation extends Association<Double> {

    public DoubleAssociation(String name, double initial) {
        super(name, initial);
    }

    @Override
    protected Double blend(Double oldVal, Double mapped) {
        double s = getStrength();
        if (s >= 1.0) return mapped;
        return oldVal + (mapped - oldVal) * s;
    }
}
