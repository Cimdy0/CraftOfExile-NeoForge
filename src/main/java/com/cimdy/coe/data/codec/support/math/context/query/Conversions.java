package com.cimdy.coe.data.codec.support.math.context.query;

import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExprTypes;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

/**
 * 数值转化生成器（value transformation / stat conversion）。
 * <p>
 * 设计要点（与已废弃的触发条件同一套「写变量、读处注入常量」的 {@code Read} 模板范式）：
 * 许多 POE2 机制本质是「把某来源数值按比率/系数转化为另一种属性」，例如
 * 「头盔上的能量护盾 × X 转化为闪避」「X% 的闪避转化为护甲」「X% 的最大能量护盾转化为最大法力」。
 * 这些都只是同一形态 {@code 来源值 × 系数} 的不同实例，故只注册<b>一个</b>通用模板
 * {@code convert_scale}（{@code input * factor}）与 {@code convert_percent}（{@code input * percent / 100}），
 * 在引用处经 {@code Read} 把来源表达式与系数/比率注入即可，无需为每种转化各写一条硬编码表达式。
 * <p>
 * 转化结果经 {@link #conversionEntry(Holder, MathExpression, MathExpression)} 工厂封装为
 * {@link ModifierStatEffect}（{@code ADD_VALUE}），目标属性（如 {@link AttributeRegistry#EVASION}）即获得该转化 Stat。
 * 全部以工厂方法 / {@code new} 构造，<b>不读取任何外部文件</b>。
 */
public final class Conversions {

    private Conversions() {}

    /** 防止 {@link ValueExprTypes#bootstrap()} 被重复调用（其注册表为静态累积）。 */
    private static boolean BOOTED = false;

    /**
     * 将数值转化模板与具名转化表达式注册进全域 {@code EXPRESSION} 注册表。
     * 转化 id 形如 {@code coe:convert_<name>}，可在任意表达式中经 {@code Read(coe:convert_<name>)} 引用。
     */
    public static void register(BootstrapContext<MathExpression> bootstrap,
                                ResourceKey<Registry<MathExpression>> expressionKey) {
        if (!BOOTED) {
            ValueExprTypes.bootstrap();
            BOOTED = true;
        }

        // —— 通用模板：来源值 × 系数（factor 可为常量，也可由引用处注入任意表达式）——
        // input * factor（二者均为 Read 注入变量）
        reg(bootstrap, expressionKey, "convert_scale",
                new ExpressionCodec.BinaryOperation.Multiply(
                        ExpressionCodec.Variable.local("input"),
                        ExpressionCodec.Variable.local("factor")));
        // input * (percent / 100)：「X% 的来源值」语义，percent 注入（可为属性引用，如 evasion_per_helmet_energy）
        reg(bootstrap, expressionKey, "convert_percent",
                new ExpressionCodec.BinaryOperation.Multiply(
                        ExpressionCodec.Variable.local("input"),
                        new ExpressionCodec.BinaryOperation.Divide(
                                ExpressionCodec.Variable.local("percent"),
                                new ExpressionCodec.Constant(100))));

        // —— 具名转化（写变量、读处注入具体来源与比率）——
        // 例：头盔上的能量护盾 × (evasion_per_helmet_energy / 100) → 作为闪避转化量（equipment_stat:head.energy * 比率）
        // 槽名必须用 EquipmentSlot.getName()（如 "head"）；"helmet" 不是合法槽名，会使该转化恒 0
        reg(bootstrap, expressionKey, "convert_helmet_energy_to_evasion",
                convertPercent(equipmentStat("head", "energy"),
                        attributeRef("evasion_per_helmet_energy")));
        // 例：X% 的闪避转化为护甲（ARMOR_FROM_EVASION 满额=1.0）
        reg(bootstrap, expressionKey, "convert_evasion_to_armor",
                convertPercent(attributeRef("evasion"),
                        attributeRef("armor_from_evasion")));
        // 例：把闪避作为额外护甲（EVASION_AS_EXTRA_ARMOR：8% 的闪避 rating 作为额外护甲）
        reg(bootstrap, expressionKey, "convert_evasion_as_extra_armor",
                convertPercent(attributeRef("evasion"),
                        attributeRef("evasion_as_extra_armor")));
    }

    // —— 工厂辅助 ——

    /** 将一条math表达式注册进 EXPRESSION 注册表（id 形如 {@code coe:<id>}）。 */
    private static void reg(BootstrapContext<MathExpression> bootstrap,
                            ResourceKey<Registry<MathExpression>> expressionKey,
                            String id, MathExpression expr) {
        bootstrap.register(ResourceKey.create(expressionKey,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, id)), expr);
    }

    /**
     * 通用「来源值 × 系数」：生成 {@code Read(coe:convert_scale, {input, factor})}。
     * 模板 {@code convert_scale} 内部以 {@link ExpressionCodec.Variable#local(String)} 占位变量承载
     * 来源表达式与系数；调用处经 Read 把二者替换为具体值，从而通用。
     *
     * @param input 来源表达式（如 {@code equipmentStat("helmet","energy")}、{@code attributeRef("evasion")}）
     * @param factor 系数常量
     */
    public static MathExpression convertScale(MathExpression input, double factor) {
        return new ExpressionCodec.Read(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "convert_scale"),
                Map.of(
                        "input", new ExpressionCodec.Value(Either.right(input)),
                        "factor", ExpressionCodec.of(factor)
                ));
    }

    /**
     * 通用「X% 的来源值」：生成 {@code Read(coe:convert_percent, {input, percent})}。
     * {@code percent} 可为属性引用（如 {@code attributeRef("evasion_per_helmet_energy")}），
     * 也可为常量，故「比率」可由装备/天赋动态决定。
     *
     * @param input   来源表达式
     * @param percent 比率表达式（百分比数值，如 50 表示 50%）
     */
    public static MathExpression convertPercent(MathExpression input, MathExpression percent) {
        return new ExpressionCodec.Read(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "convert_percent"),
                Map.of(
                        "input", new ExpressionCodec.Value(Either.right(input)),
                        "percent", new ExpressionCodec.Value(Either.right(percent))
                ));
    }

    /** {@link #convertPercent(MathExpression, MathExpression)} 的常量比率便捷重载。 */
    public static MathExpression convertPercent(MathExpression input, double percent) {
        return convertPercent(input, new ExpressionCodec.Constant(percent));
    }

    /** 构造「装备数值」来源节点：{@code equipment_stat:<slot>.<stat>}（扁平值读 EquipmentState，见 {@link ValueExprTypes}）。 */
    public static MathExpression equipmentStat(String slot, String stat) {
        return ExpressionCodec.Variable.from("equipment_stat",
                Identifier.parse(slot + "." + stat), 0.0);
    }

    /** 构造「属性值」来源/比率节点：{@code attribute:coe:<id>}（引用某属性当前计算值，如比率属性）。 */
    public static MathExpression attributeRef(String id) {
        return ExpressionCodec.Variable.from("attribute",
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, id), 0.0);
    }
}
