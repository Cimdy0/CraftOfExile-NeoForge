package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.math.MathExpression;
import com.cimdy.coe.data.codec.support.math.context.AmountSourceNamer;
import com.cimdy.coe.data.codec.support.math.context.ContextNode;
import com.cimdy.coe.data.codec.support.math.context.nodes.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.VarRef;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import com.mojang.datafixers.util.Either;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P3 反汇编回归夹具：覆盖 per-X 表达式数值的可读化。
 *
 * <p>核心被保护的不变量（详见 research/stat_translation_plan.md §P3）：
 * <ul>
 *   <li>{@code INC_DAMAGE_PER_STRENGTH} 等约 80 条 expression-valued 词缀，
 *       反汇编后数字可见 —— 典型产物 {@code +1% per 15 Strength Damage}；</li>
 *   <li>常量数值（约 1579 条）由调用方自行渲染，本类 detect 路径返回 null，绝不改动；</li>
 *   <li>{@code stat.op.*} / {@code stat.condition.*} 等键零回归（本测试不触碰）。</li>
 * </ul>
 *
 * <p>真实数据形态 {@code INC_DAMAGE_PER_STRENGTH} 的 amount 为
 * {@code $value * attribute:coe:strength / $per}（外加 params: value=0.01, per=15.0）。
 * 为避免在纯 JUnit 环境触发 Minecraft 注册表 bootstrap（attribute 表达式经 MathParser 解析会拉起
 * ContextValueTypes.bootstrap → EquipmentValue → BuiltInRegistries，需 FML loader），本测试<b>直接构造等价 AST 节点</b>
 * （Variable/Multiply/Divide/AttributeValue/VarRef），与 MathParser 实际产物完全一致，但零副作用。
 * 常量 / 退化 per 等纯局部表达式也以手工 AST 构造，保证测试在任何无 FML 的 JUnit 运行时都可执行。
 *
 * <p>为在无 Minecraft 客户端运行时解析 {@link Component#getString()}，把一份 en_us 映射注入为当前
 * {@link Language} 单例（仅含本测试用到的键），使翻译键解析为英文模板。
 */
class StatAmountDescriberTest {

    /** en_us 模板（与 LangProvider 中的键一致；attribute.name.coe:damage 为属性注册名，测试内显式注入）。 */
    private static final Map<String, String> EN_US = Map.of(
            "stat.amount.per", "per %s %s",
            "stat.amount.up_to", "up to %s",
            "stat.amount.unknown", "[!] amount",
            "stat_effect.deal", "deal %s",
            // 属性名键：权威形式为 attribute.name.<注册名>（= Attribute#getDescriptionId()，
            // 由 AttributeRegister 以 "attribute.name." + name 注册），无命名空间前缀。
            "attribute.name.damage", "Damage",
            "attribute.name.strength", "Strength"
    );

    @BeforeAll
    static void installLanguage() {
        injectLanguage(EN_US);
    }

    @AfterAll
    static void restoreLanguage() {
        injectLanguage(Map.of());
    }

    // ---------------------------------------------------------------- 数值格式化（纯 JDK）

    @Test
    void numberFormatSignedPositive() {
        assertEquals("+1", StatNumberFormat.formatSigned(1.0, Locale.ROOT));
        assertEquals("+100", StatNumberFormat.formatSigned(100.0, Locale.ROOT));
    }

    @Test
    void numberFormatSignedNegative() {
        assertEquals("-2.5", StatNumberFormat.formatSigned(-2.5, Locale.ROOT));
    }

    @Test
    void numberFormatPlain() {
        assertEquals("15", StatNumberFormat.format(15.0, Locale.ROOT));
        assertEquals("1", StatNumberFormat.format(1.0, Locale.ROOT));
    }

    // ---------------------------------------------------------------- 来源名 humanize（literal，非翻译键）

    @Test
    void humanizeStrengthFromAst() {
        MathExpression expr = perXStrengthAst();
        StatAmountDescriber.PerXInfo info = StatAmountDescriber.detectPerX(expr);
        assertTrue(info != null, "INC_DAMAGE_PER_STRENGTH 必须被识别为 per-X");
        assertEquals("Strength", AmountSourceNamer.humanize(info.srcNode()));
    }

    @Test
    void humanizeTitleCaseContract() {
        // Title-Case 逻辑在 ContextNode.humanize 静态方法（AmountSourceNamer 对 AttributeValue 等复用之）。
        assertEquals("Nearby Enemy Count", ContextNode.humanize("nearby_enemy_count"));
        assertEquals("Low Life", ContextNode.humanize("low_life"));
    }

    // ---------------------------------------------------------------- detectPerX 形状 / 参数覆盖

    @Test
    void detectPerXMatchesRealAst() {
        // 等价真实 AST 形状：Divide( Multiply( Variable($value), Variable(attribute:coe:strength) ), Variable($per) )
        MathExpression expr = perXStrengthAst();
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(expr);
        assertTrue(raw != null, "真实 AST 形状必须被识别");
        // AST 默认值为 0（真实幅度在 params 里），故 baseVal/perVal 此刻为 0。
        assertEquals(0.0, raw.baseVal(), 1e-9);
        assertEquals(0.0, raw.perVal(), 1e-9);
        assertNull(raw.capVal());
        assertEquals("Strength", AmountSourceNamer.humanize(raw.srcNode()));
    }

    @Test
    void resolveValuesOverridesDefaultsFromParams() {
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
        Map<String, ExpressionCodec.Value> params = Map.of(
                "value", new ExpressionCodec.Value(Either.left(0.01)),
                "per", new ExpressionCodec.Value(Either.left(15.0)));
        StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, params);
        assertEquals(0.01, resolved.baseVal(), 1e-9);
        assertEquals(15.0, resolved.perVal(), 1e-9);
        assertEquals("Strength", AmountSourceNamer.humanize(resolved.srcNode()));
    }

    @Test
    void detectPerXRejectsConstant() {
        MathExpression expr = new ExpressionCodec.Constant(5.0);
        assertNull(StatAmountDescriber.detectPerX(expr), "常量表达式不应被识别为 per-X");
    }

    @Test
    void detectPerXRejectsDegeneratePer() {
        // per("1.0") 退化情形：来源为常量 1.0，不是读值节点 → 不匹配。
        MathExpression expr = new ExpressionCodec.BinaryOperation.Divide(
                new ExpressionCodec.BinaryOperation.Multiply(var("value"), new ExpressionCodec.Constant(1.0)),
                var("per"));
        assertNull(StatAmountDescriber.detectPerX(expr), "退化 per(常量来源) 必须回落 [!] amount");
    }

    @Test
    void detectPerXHandlesMinCappedShape() {
        MathExpression expr = perXStrengthCappedAst();
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(expr);
        assertTrue(raw != null, "min 包裹的 per-X 必须被识别");
        assertTrue(raw.capVal() != null, "min 第二参数为 $cap → capVal 必须非空");
        assertEquals("Strength", AmountSourceNamer.humanize(raw.srcNode()));
    }

    // ---------------------------------------------------------------- 渲染片段（经 Language 注入解析）

    @Test
    void describeModifierProducesPerXFragment() {
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
        Map<String, ExpressionCodec.Value> params = Map.of(
                "value", new ExpressionCodec.Value(Either.left(0.01)),
                "per", new ExpressionCodec.Value(Either.left(15.0)));
        StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, params);
        // ADD_MULTIPLIED_BASE → 百分比口径（×100）。
        Component frag = StatAmountDescriber.describeModifier(
                resolved, Locale.ROOT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        assertEquals("+1% per 15 Strength", frag.getString());
    }

    @Test
    void describeModifierAddValueUsesPoints() {
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
        Map<String, ExpressionCodec.Value> params = Map.of(
                "value", new ExpressionCodec.Value(Either.left(5.0)),
                "per", new ExpressionCodec.Value(Either.left(15.0)));
        StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, params);
        // ADD_VALUE → 点数口径（不 ×100）。
        Component frag = StatAmountDescriber.describeModifier(
                resolved, Locale.ROOT, AttributeModifier.Operation.ADD_VALUE);
        assertEquals("+5 per 15 Strength", frag.getString());
    }

    @Test
    void describeDamageProducesDealFragment() {
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
        // describeDamage 走 unsigned format(baseVal)（deal 语义，对应 stat_effect.deal="deal %s"）。
        Map<String, ExpressionCodec.Value> params = Map.of(
                "value", new ExpressionCodec.Value(Either.left(1.0)),
                "per", new ExpressionCodec.Value(Either.left(15.0)));
        StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, params);
        Component frag = StatAmountDescriber.describeDamage(resolved, Locale.ROOT);
        assertEquals("deal 1 per 15 Strength", frag.getString());
    }

    @Test
    void describeFragmentConstantReturnsNull() {
        // 常量数值：调用方（ModifierStatEffect）自行渲染，本路径返回 null。
        ExpressionCodec.Value constant = new ExpressionCodec.Value(Either.left(12.0));
        assertNull(StatAmountDescriber.describeFragment(
                constant, Map.of(), Locale.ROOT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Test
    void describeFragmentFullAssemblyIncludingAttributeName() {
        // 完整装配：per-X 片段 + " " + 属性名（attribute.name.damage → "Damage"）。
        StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
        Map<String, ExpressionCodec.Value> params = Map.of(
                "value", new ExpressionCodec.Value(Either.left(0.01)),
                "per", new ExpressionCodec.Value(Either.left(15.0)));
        StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, params);
        Component frag = StatAmountDescriber.describeModifier(
                resolved, Locale.ROOT, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        Component line = ((MutableComponent) frag)
                .append(Component.literal(" "))
                .append(Component.translatable("attribute.name.damage"));
        assertEquals("+1% per 15 Strength Damage", line.getString());
    }

    // ---------------------------------------------------------------- P4 值键化（stat.value.*）
    //
    // 缺陷背景：来源名曾是裸英文 literal（"Strength"），注入 stat.amount.per 模板后永不本地化，
    // 中文下渲染成「每 15 Strength」。改为 AmountSourceNamer.name → stat.value.<ns>.<id>：
    //   · 键命中   → 渲染译文；
    //   · 键缺失   → 回落 humanize 英文（可见，绝不显示裸键）→ 英文零回归。
    // 以下三条锁死这两个分支。

    @Test
    void nameFallsBackToHumanizedEnglishWhenValueKeyMissing() {
        // 夹具不含 attribute.name.strength（权威属性名键）→ 回落 humanize 英文，与改造前逐字一致。
        StatAmountDescriber.PerXInfo info = StatAmountDescriber.detectPerX(perXStrengthAst());
        assertEquals("Strength", AmountSourceNamer.name(info.srcNode()).getString());
    }

    @Test
    void nameResolvesAuthoritativeAttributeKeyWhenPresent() {
        // 权威键 attribute.name.strength 命中 → 渲染译文（改造前此处恒为裸英文 "Strength"）。
        // 属性域不铸 stat.value.attribute.* 孪生键（裁定 #4），与 ModifierStatEffect 的
        // Attribute#getDescriptionId() 同键。
        Map<String, String> withKey = new HashMap<>(EN_US);
        withKey.put("attribute.name.strength", "力量");
        injectLanguage(withKey);
        try {
            StatAmountDescriber.PerXInfo info = StatAmountDescriber.detectPerX(perXStrengthAst());
            assertEquals("力量", AmountSourceNamer.name(info.srcNode()).getString());
        } finally {
            injectLanguage(EN_US);
        }
    }

    @Test
    void perClauseLocalizesSourceNameThroughAuthoritativeKey() {
        // 端到端：zh 模板「每 %1$s %2$s」+ 权威属性键 → 「每 15 力量」（改造前为「每 15 Strength」）。
        Map<String, String> zh = new HashMap<>(EN_US);
        zh.put("stat.amount.per", "每 %1$s %2$s");
        zh.put("attribute.name.strength", "力量");
        injectLanguage(zh);
        try {
            StatAmountDescriber.PerXInfo raw = StatAmountDescriber.detectPerX(perXStrengthAst());
            StatAmountDescriber.PerXInfo resolved = StatAmountDescriber.resolveValues(raw, Map.of(
                    "value", new ExpressionCodec.Value(Either.left(0.01)),
                    "per", new ExpressionCodec.Value(Either.left(15.0))));
            assertEquals("每 15 力量",
                    StatAmountDescriber.perClause(resolved, Locale.ROOT, true).getString());
        } finally {
            injectLanguage(EN_US);
        }
    }

    // ---------------------------------------------------------------- 辅助：手工构造等价真实 AST

    /** $value 本地变量（VarRef）。 */
    private static ExpressionCodec.Variable var(String name) {
        return new ExpressionCodec.Variable(new VarRef(name), 0.0);
    }

    /** attribute:<ns>:<path> 读值变量（AttributeValue 节点，等价于 MathParser 解析 attribute: 引用）。 */
    private static ExpressionCodec.Variable attr(String ns, String path) {
        return new ExpressionCodec.Variable(
                new AttributeValue(Identifier.fromNamespaceAndPath(ns, path)), 0.0);
    }

    /** 等价真实 AST：Divide( Multiply( $value, attribute:coe:strength ), $per )。 */
    private static MathExpression perXStrengthAst() {
        return new ExpressionCodec.BinaryOperation.Divide(
                new ExpressionCodec.BinaryOperation.Multiply(var("value"), attr("coe", "strength")),
                var("per"));
    }

    /** min( 上述, $cap )：封顶形状。 */
    private static MathExpression perXStrengthCappedAst() {
        return new ExpressionCodec.BinaryOperation.Min(
                perXStrengthAst(), new ExpressionCodec.Variable(new VarRef("cap"), 0.0));
    }

    /** 把一张 {key→文本} 表注入为当前 {@link Language} 单例（无需 Minecraft 客户端）。 */
    private static void injectLanguage(Map<String, String> map) {
        Language lang = new Language() {
            private final Map<String, String> data = new HashMap<>(map);
            @Override
            public String getOrDefault(String key, String def) {
                return data.getOrDefault(key, def);
            }
            @Override
            public boolean has(String key) {
                return data.containsKey(key);
            }
            @Override
            public boolean isDefaultRightToLeft() {
                return false;
            }
            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) {
                return Language.DEFAULT_INSTANCE.getVisualOrder(text);
            }
        };
        Language.inject(lang);
    }
}
