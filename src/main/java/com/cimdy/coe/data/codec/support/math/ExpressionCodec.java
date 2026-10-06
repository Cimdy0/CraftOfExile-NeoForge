package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.data.codec.support.math.context.nodes.scope.SelectorNode;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.query.SplitSink;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExprTypes;
import com.cimdy.coe.data.codec.support.math.context.nodes.meta.misc.VarRef;
import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.event.data.LoadEvent;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ExpressionCodec {
    //通用值表达式
    public static final Codec<Value> VALUE_CODEC =
            Codec.either(Codec.DOUBLE, MathExpression.CODEC).xmap(Value::new, Value::value);

    /** 通用数值模板：amount = {@code $value} 变量（幅度由 {@code StatRead.vars} 注入）。多数 StatEntry 的默认 amount。 */
    public static final Value VALUE = of(MathParser.parse("$value"));

    public static Value of(double value) {
        return new Value(Either.left(value));
    }

    /** 表达式分支包装：把一条已构建的 math 表达式作为「数值」使用。 */
    public static Value of(MathExpression expression) {
        return new Value(Either.right(expression));
    }

    public static Value ofSkillLevel(double base, double per, double add) {
        return new ExpressionCodec.Value(Either.right(new ExpressionCodec.Read(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "per_level_add_value"),
                Map.of(
                        "base", ExpressionCodec.of(base),
                        "per", ExpressionCodec.of(per),
                        "add", ExpressionCodec.of(add)
                ))));
    }

    /**
     * 按等级查表（严格还原非线性数值）：传入每一级的值（1-based 索引 = 等级），
     * 变量固定为 skill.level，运行时按当前等级取对应值，未命中则取 def（默认 0）。
     * 替代线性 ofSkillLevel。
     * <p>
     * 使用 Read 引用表达式模板 {@code coe:level_table}，将各等级值注入为变量 "1"~"n"，
     * 避免在技能 JSON 中内联庞大的 LevelTable 数据。
     */
    public static Value ofLevelTable(List<Double> values) {
        return ofLevelTable(values, 0.0);
    }

    /**
     * 蓄力缩放（HoldInput 输入）：base * (min + (1 - min) * charge)。
     * charge 为运行时蓄力比例（0~1，由 SkillContext.getChargeAmount 提供），
     * min 为 0 蓄力时的最低倍率。用于 Supercharged Slam 等蓄力技能的伤害随蓄力成长。
     */
    public static Value ofChargeScale(Value base, double min) {
        return new Value(Either.right(new Read(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "charge_scale"),
                Map.of(
                        "base", base,
                        "min", ExpressionCodec.of(min)
                ))));
    }

    public static Value ofLevelTable(List<Double> values, double def) {
        Map<String, Value> vars = new java.util.LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            vars.put(String.valueOf(i + 1), ExpressionCodec.of(values.get(i)));
        }
        // def==0 时模板 level_table 的 def 变量默认即 0，无需注入，调用侧省略 def = 0
        if (def != 0.0) {
            vars.put("def", ExpressionCodec.of(def));
        }
        return new Value(Either.right(new Read(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "level_table"),
                vars
        )));
    }

    public record Value(Either<Double, MathExpression> value) {
        public double getValue(SkillContext context) {
            // 完整求值基座（forSkillContext）：旧轻量 builder 缺 entity/eventState/statScopeStack，
            // 技能动作 amount 里 attribute: / recently: / count: 等节点恒 0/false；现以施法者单例为底座对齐。
            return ExpressionCodec.getValue(value, ExpressionContext.forSkillContext(context));
        }

        public double getValue(LivingEntity living) {
            return ExpressionCodec.getValue(value, ExpressionContext.get(living));
        }

        /** 直接以给定的 ExpressionContext 求值（命中测试 / 自定义上下文场景）。 */
        public double getValue(ExpressionContext ctx) {
            return ExpressionCodec.getValue(value, ctx);
        }

        // ---- 类型转换辅助方法 ----

        /** 取整，用于替代 int 类型字段。 */
        public int getInt(SkillContext context) {
            return (int) Math.round(getValue(context));
        }

        /** 转为 float，用于替代 float 类型字段。 */
        public float getFloat(SkillContext context) {
            return (float) getValue(context);
        }

        /** 布尔转换：>= 1.0 为 true，其余为 false。 */
        public boolean getBool(SkillContext context) {
            return getValue(context) >= 1.0;
        }

        /** 取整，无上下文版本。 */
        public int getInt(LivingEntity living) {
            return (int) Math.round(getValue(living));
        }

        /** 转为 float，无上下文版本。 */
        public float getFloat(LivingEntity living) {
            return (float) getValue(living);
        }

        /** 布尔转换，无上下文版本。 */
        public boolean getBool(LivingEntity living) {
            return getValue(living) >= 1.0;
        }

        // ---- 无上下文求值（运行时无 SkillContext / LivingEntity 的场景，如投射物运行期）----

        /** 无上下文求值，等价于以空 ExpressionContext 求值。 */
        public double getValue() {
            return ExpressionCodec.getValue(value, ExpressionContext.builder().build());
        }

        /** 取整，无上下文版本。 */
        public int getInt() {
            return (int) Math.round(getValue());
        }

        /** 转为 float，无上下文版本。 */
        public float getFloat() {
            return (float) getValue();
        }

        /** 布尔转换，无上下文版本。 */
        public boolean getBool() {
            return getValue() >= 1.0;
        }
    }

    public static double getValue(Either<Double, MathExpression> value, ExpressionContext context) {
        if (value.left().isPresent()) {
            return value.left().get();
        }
        return value.right().get().evaluate(context);
    }

    /**
     * 带一对一拆分通道的求值：把 {@code sink} 注入本次求值的 {@link ValueScope}，
     * 表达式内 EACH 选择器（{@code SelectorNode} mode=each）会对每个匹配候选发射
     * {@code (实体, 值)} 对；常量左值不发射。标量返回值语义不变（EACH 返回各候选值之和）。
     */
    public static double getValue(Either<Double, MathExpression> value, ExpressionContext context,
                                  com.cimdy.coe.data.codec.support.math.context.query.SplitSink sink) {
        if (value.left().isPresent()) {
            return value.left().get();
        }
        com.cimdy.coe.data.codec.support.math.context.ValueScope scope =
                com.cimdy.coe.data.codec.support.math.context.ValueScope.of(context);
        scope.setSplitSink(sink);
        return value.right().get().evaluate(scope);
    }

    //常量
    public record Constant(double value) implements MathExpression {
        @Override
        public Type<?> getType() {
            return MathExpressions.CONSTANT;
        }

        @Override
        public double evaluate(ValueScope scope) {
            return value;
        }

        // 常量节点 Codec
        public static final Codec<Constant> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.fieldOf("value").forGetter(Constant::value)
                ).apply(instance, Constant::new)
        );
    }

    //变量：直接承载一个 ValueExpr（读值节点），不再经旧的 String 类型分派求值。
    // 真实读值由 MathParser / Variable.from 在构造期经 ValueExprTypes.resolve 解析为节点；
    // Read 模板的本地占位变量用 Variable.local（VarRef 节点）引用注入的 data map。
    public record Variable(ValueExpr node, double defaultValue) implements MathExpression {
        /** 真实读值：由变量类型串（构造期）解析为 ValueExpr，不再在求值期做 String 分派。 */
        public static Variable from(String type) {
            return from(type, CraftOfExile.EMPTY_LOCATION, 0.0);
        }

        public static Variable from(String type, double defaultValue) {
            return from(type, CraftOfExile.EMPTY_LOCATION, defaultValue);
        }

        public static Variable from(String type, Identifier id, double defaultValue) {
            ValueExpr n = ValueExprTypes.resolve(type, id, defaultValue);
            if (n == null) throw new IllegalArgumentException("未知变量类型: " + type);
            return new Variable(n, defaultValue);
        }

        /** 注入的本地变量（Read 模板占位符），对应 math 的 {@code $name} 形式。 */
        public static Variable local(String name) {
            return new Variable(new VarRef(name), 0.0);
        }

        @Override
        public Type<?> getType() {
            return MathExpressions.VARIABLE;
        }

        @Override
        public double evaluate(ValueScope scope) {
            // 方案四：AST 全程传 scope，读值节点直接作用于当前候选，不再重建 ValueScope
            double v = node.evaluate(scope);
            return Double.isNaN(v) ? defaultValue : v;
        }

        public static final Codec<Variable> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ValueExpr.CODEC.fieldOf("node").forGetter(Variable::node),
                        Codec.DOUBLE.optionalFieldOf("default", 0.0)
                                .forGetter(Variable::defaultValue)
                ).apply(instance, Variable::new)
        );
    }

    public record Read(
            Identifier id,
            Map<String, Value> value
    ) implements MathExpression {
        public static final Codec<Read> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Read::id),
                Codec.unboundedMap(Codec.STRING, VALUE_CODEC)
                        .fieldOf("values").forGetter(Read::value)
        ).apply(instance, Read::new));

        @Override
        public Type<?> getType() {
            return MathExpressions.READ_EXPRESSION;
        }

        @Override
        public double evaluate(ValueScope scope) {
            ExpressionContext context = scope.root();
            // 循环引用检测（S1）：避免 A→B→A 这类数据配置导致的无限递归
            if (context.isEvaluating(this.id)) {
                throw new IllegalStateException("检测到循环引用: " + this.id);
            }
            context.pushEvaluation(this.id);
            try {
                MathExpression expr = LoadEvent.EXPRESSION_MAP.get(this.id);
                if (expr == null) {
                    throw new IllegalStateException("未注册的表达式: " + this.id);
                }
                // 换根注入变量、保留当前候选指针（Read 模板内读值仍作用于候选）
                return expr.evaluate(scope.withRoot(context.withVariables(value)));
            } finally {
                context.popEvaluation(this.id);
            }
        }
    }

    //一元运算
    public abstract static class UnaryOperation implements MathExpression {
        protected final MathExpression expression;

        protected UnaryOperation(MathExpression expression) {
            this.expression = Objects.requireNonNull(expression);
        }

        MathExpression expression() {
            return expression;
        }

        // 通用序列化结构
        public static <T extends UnaryOperation> Codec<T> createCodec(
                Function<MathExpression, T> constructor,
                Function<T, MathExpression> destructor
        ) {
            return MathExpression.CODEC.xmap(constructor, destructor);
        }

        // 取负
        public static class Negate extends UnaryOperation {
            public Negate(MathExpression expression) {
                super(expression);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.NEGATE;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return -expression.evaluate(scope);
            }

            public static final Codec<Negate> CODEC = createCodec(Negate::new, Negate::expression);
        }

        //开二次方
        public static class Sqrt extends UnaryOperation {
            protected Sqrt(MathExpression expression) {
                super(expression);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.SQRT;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return Math.sqrt(expression.evaluate(scope));
            }

            public static final Codec<Sqrt> CODEC = createCodec(Sqrt::new, Sqrt::expression);
        }

        //开立方
        public static class Cbrt extends UnaryOperation {
            protected Cbrt(MathExpression expression) {
                super(expression);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.CBRT;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return Math.cbrt(expression.evaluate(scope));
            }

            public static final Codec<Cbrt> CODEC = createCodec(Cbrt::new, Cbrt::expression);
        }
    }

    //二元运算
    public abstract static class BinaryOperation implements MathExpression {
        protected final MathExpression left;
        protected final MathExpression right;

        public MathExpression left() { return left; }
        public MathExpression right() { return right; }

        protected BinaryOperation(MathExpression left, MathExpression right) {
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
        }

        //通用序列化结构
        public static <T extends BinaryOperation> Codec<T> createCodec(
                Function<Pair<MathExpression, MathExpression>, T> constructor,
                Function<T, Pair<MathExpression, MathExpression>> destructor
        ) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            MathExpression.CODEC.fieldOf("left")
                                    .forGetter(op -> destructor.apply(op).getFirst()),
                            MathExpression.CODEC.fieldOf("right")
                                    .forGetter(op -> destructor.apply(op).getSecond())
                    ).apply(instance, (l, r) -> constructor.apply(Pair.of(l, r)))
            );
        }

        //加法
        public static class Add extends BinaryOperation {
            public Add(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.ADD;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return left.evaluate(scope) + right.evaluate(scope);
            }

            public static final Codec<Add> CODEC = createCodec(
                    pair -> new Add(pair.getFirst(), pair.getSecond()),
                    add -> Pair.of(add.left, add.right)
            );
        }

        //减法
        public static class Subtract extends BinaryOperation {
            public Subtract(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.SUBTRACT;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return left.evaluate(scope) - right.evaluate(scope);
            }

            public static final Codec<Subtract> CODEC = createCodec(
                    pair -> new Subtract(pair.getFirst(), pair.getSecond()),
                    sub -> Pair.of(sub.left, sub.right)
            );
        }

        //乘法
        public static class Multiply extends BinaryOperation {
            public Multiply(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.MULTIPLY;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return left.evaluate(scope) * right.evaluate(scope);
            }

            public static final Codec<Multiply> CODEC = createCodec(
                    pair -> new Multiply(pair.getFirst(), pair.getSecond()),
                    mul -> Pair.of(mul.left, mul.right)
            );
        }

        //除法（含除零保护）
        public static class Divide extends BinaryOperation {
            public Divide(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.DIVIDE;
            }

            @Override
            public double evaluate(ValueScope scope) {
                double denominator = right.evaluate(scope);
                if (denominator == 0) {
                    throw new ArithmeticException("Division by zero in expression");
                }
                return left.evaluate(scope) / denominator;
            }

            public static final Codec<Divide> CODEC = createCodec(
                    pair -> new Divide(pair.getFirst(), pair.getSecond()),
                    div -> Pair.of(div.left, div.right)
            );
        }

        //取余（含除零保护）
        public static class Modulo extends BinaryOperation {
            public Modulo(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.MODULO;
            }

            @Override
            public double evaluate(ValueScope scope) {
                double denominator = right.evaluate(scope);
                if (denominator == 0) {
                    throw new ArithmeticException("Division by zero in expression");
                }
                return left.evaluate(scope) % denominator;
            }

            public static final Codec<Modulo> CODEC = createCodec(
                    pair -> new Modulo(pair.getFirst(), pair.getSecond()),
                    mod -> Pair.of(mod.left, mod.right)
            );
        }

        //幂运算
        public static class Power extends BinaryOperation {
            public Power(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.POWER;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return Math.pow(left.evaluate(scope), right.evaluate(scope));
            }

            public static final Codec<Power> CODEC = createCodec(
                    pair -> new Power(pair.getFirst(), pair.getSecond()),
                    pow -> Pair.of(pow.left, pow.right)
            );
        }

        //取大
        public static class Max extends BinaryOperation {
            public Max(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.MAX;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return Math.max(left.evaluate(scope), right.evaluate(scope));
            }

            public static final Codec<Max> CODEC = createCodec(
                    pair -> new Max(pair.getFirst(), pair.getSecond()),
                    max -> Pair.of(max.left, max.right)
            );
        }

        //取小
        public static class Min extends BinaryOperation {
            public Min(MathExpression left, MathExpression right) {
                super(left, right);
            }

            @Override
            public Type<?> getType() {
                return MathExpressions.MIN;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return Math.min(left.evaluate(scope), right.evaluate(scope));
            }

            public static final Codec<Min> CODEC = createCodec(
                    pair -> new Min(pair.getFirst(), pair.getSecond()),
                    min -> Pair.of(min.left, min.right)
            );
        }

        public static class Comparison extends BinaryOperation {
            private final ComparisonOperator comparison;
            private final MathExpression value;
            private final MathExpression def;

            public Comparison(
                    MathExpression left, MathExpression right,
                    ComparisonOperator comparison,
                    MathExpression value, MathExpression def
            ) {
                super(left, right);
                this.comparison = comparison;
                this.value = value;
                this.def = def;
            }

            public ComparisonOperator comparison() { return comparison; }
            public MathExpression value() { return value; }
            public MathExpression def() { return def; }

            @Override
            public Type<?> getType() {
                return MathExpressions.COMPARISON;
            }

            @Override
            public double evaluate(ValueScope scope) {
                return comparison.compare(left.evaluate(scope), right.evaluate(scope)) ?
                        value.evaluate(scope) : def.evaluate(scope);
            }

            public static final Codec<Comparison> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MathExpression.CODEC.fieldOf("left")
                            .forGetter(op -> op.left),
                    MathExpression.CODEC.fieldOf("right")
                            .forGetter(op -> op.right),
                    ComparisonOperator.CODEC.fieldOf("comparison")
                            .forGetter(op -> op.comparison),
                    MathExpression.CODEC.fieldOf("value")
                            .forGetter(op -> op.value),
                    MathExpression.CODEC.fieldOf("def")
                            .forGetter(op -> op.def)
            ).apply(instance, Comparison::new));
        }
    }

    public static class ConditionOperation implements MathExpression {
        private final List<Condition> conditions;
        private final MathExpression def;

        public ConditionOperation(List<Condition> conditions, MathExpression def) {
            this.conditions = conditions;
            this.def = def;
        }

        public List<Condition> conditions() { return conditions; }
        public MathExpression def() { return def; }

        @Override
        public Type<?> getType() {
            return MathExpressions.CONDITION;
        }

        @Override
        public double evaluate(ValueScope scope) {
            for (Condition condition : conditions) {
                if (condition.matches(scope)) {
                    return condition.value.evaluate(scope);
                }
            }
            return def.evaluate(scope);
        }

        public final static Codec<ConditionOperation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Condition.CODEC.listOf().fieldOf("conditions")
                        .forGetter(op -> op.conditions),
                MathExpression.CODEC.fieldOf("def")
                        .forGetter(op -> op.def)
        ).apply(instance, ConditionOperation::new));

        public record Condition(
                MathExpression left, MathExpression right,
                ComparisonOperator comparison,
                MathExpression value
        ) {
            public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MathExpression.CODEC.fieldOf("left")
                            .forGetter(op -> op.left),
                    MathExpression.CODEC.fieldOf("right")
                            .forGetter(op -> op.right),
                    ComparisonOperator.CODEC.fieldOf("comparison")
                            .forGetter(op -> op.comparison),
                    MathExpression.CODEC.fieldOf("value")
                            .forGetter(op -> op.value)
            ).apply(instance, Condition::new));

            public boolean matches(ValueScope scope) {
                return this.comparison.compare(this.left.evaluate(scope), this.right.evaluate(scope));
            }
        }
    }

    // 布尔逻辑运算：非零视为真，结果 1/0。用于把多个比较条件简洁组合（a > b && c < d）。
    public record Logical(
            MathExpression left, MathExpression right, boolean and
    ) implements MathExpression {
        @Override
        public Type<?> getType() {
            return MathExpressions.LOGICAL;
        }

        @Override
        public double evaluate(ValueScope scope) {
            boolean l = left.evaluate(scope) != 0;
            boolean r = right.evaluate(scope) != 0;
            return (and ? (l && r) : (l || r)) ? 1.0 : 0.0;
        }

        public static final Codec<Logical> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MathExpression.CODEC.fieldOf("left").forGetter(Logical::left),
                MathExpression.CODEC.fieldOf("right").forGetter(Logical::right),
                Codec.BOOL.fieldOf("and").forGetter(Logical::and)
        ).apply(instance, Logical::new));
    }

    // 布尔取反：非零变零、零变一（!expr）。
    public record Not(MathExpression expression) implements MathExpression {
        @Override
        public Type<?> getType() {
            return MathExpressions.NOT;
        }

        @Override
        public double evaluate(ValueScope scope) {
            return expression.evaluate(scope) != 0 ? 0.0 : 1.0;
        }

        public static final Codec<Not> CODEC = MathExpression.CODEC.xmap(Not::new, Not::expression);
    }

    // 查表 当 variable 求值结果等于某条 Pair 左值时取右值，否则取 def。
    public record LevelTable(
            MathExpression variable, List<TableEntry> table,
            MathExpression def) implements MathExpression {

        /** 查表中的单条映射：key 为匹配键，value 为匹配后的取值。 */
        public record TableEntry(MathExpression key, MathExpression value) {
            public static final Codec<TableEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MathExpression.CODEC.fieldOf("key").forGetter(TableEntry::key),
                    MathExpression.CODEC.fieldOf("value").forGetter(TableEntry::value)
            ).apply(instance, TableEntry::new));
        }

        public static final Codec<LevelTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MathExpression.CODEC.fieldOf("variable").forGetter(LevelTable::variable),
                TableEntry.CODEC.listOf().fieldOf("table").forGetter(LevelTable::table),
                MathExpression.CODEC.optionalFieldOf("def", new Constant(0.0)).forGetter(LevelTable::def)
        ).apply(instance, LevelTable::new));

        @Override
        public Type<?> getType() {
            return MathExpressions.LEVEL_TABLE;
        }

        @Override
        public double evaluate(ValueScope scope) {
            double key = variable.evaluate(scope);
            for (TableEntry e : table) {
                if (Math.abs(e.key().evaluate(scope) - key) < 1e-9) {
                    return e.value().evaluate(scope);
                }
            }
            return def.evaluate(scope);
        }
    }

}
