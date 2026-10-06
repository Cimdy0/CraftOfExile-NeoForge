package com.cimdy.coe.data.helper.item;

import com.cimdy.coe.data.codec.support.stat.tag.GateExpr;
import com.cimdy.coe.data.codec.item.mod.identity.ItemTypeHierarchy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分支键表达式匹配器（精髓 / 符文共用）。
 *
 * <p>把原先硬编码的 {@code weapon_1h} / {@code weapon_2h} / {@code caster} / {@code martial_weapon} 等
 * 分支键，升级为与 {@link GateExpr} 同构的「与 / 或 / 非」作用域表达式（像 Stat 的作用域一样可自由组合），
 * 彻底数据驱动、不再写死部位。</p>
 *
 * <p>分支键语法（每个 JSON 键是一个表达式字符串，对宿主 {@code EquipmentState.types()} 类型链求值）：</p>
 * <ul>
 *   <li>{@code "*"} —— 通配（恒真，等价于 {@link GateExpr#ALWAYS}），对应原 {@code other} 全局分支；</li>
 *   <li>{@code wand|staff} —— 法杖或长杖（| = OR）；</li>
 *   <li>{@code weapon, !wand, !staff} —— 武器且非 wand/staff（, = AND，! = NOT）；</li>
 *   <li>{@code one_hand, !wand, !staff, !sceptre} —— 单手且非法器（排除 caster）；</li>
 *   <li>{@code armor} / {@code accessory} / {@code two_hand} / {@code wand|staff|sceptre} 等同理。</li>
 * </ul>
 *
 * <p>{@link #collect(Map, Set)} 返回所有命中宿主作用域的分支及其加成列表，供 SocketOps 合并结算。</p>
 */
public final class BranchExpr {

    private BranchExpr() {
    }

    /**
     * 按宿主作用域收集所有命中的分支。
     *
     * @param branches 分支键 -> 该阶加成列表（{@code Bonus}）
     * @param scope    宿主叶类型集合（{@code EquipmentState.types()}），经 {@code ItemTypeHierarchy} 解析器做父辖子求值
     * @param <B>      加成类型
     * @return 命中的分支键 -> 加成列表（保留原插入顺序）
     */
    public static <B> Map<String, List<B>> collect(Map<String, List<B>> branches, Set<String> scope) {
        Map<String, List<B>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<B>> e : branches.entrySet()) {
            String key = e.getKey();
            GateExpr expr = "*".equals(key) ? GateExpr.ALWAYS : GateExpr.of(key);
            if (expr.matches(scope, ItemTypeHierarchy.resolver())) {
                out.put(key, e.getValue());
            }
        }
        return out;
    }
}
