package com.cimdy.coe.data.codec.support.math;

import com.cimdy.coe.CraftOfExile;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class MathExpressions {
    public static final Identifier RESOURCE_LOCATION =
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "math_expression");
    public static final ResourceKey<Registry<MathExpression.Type<?>>> KEY =
            ResourceKey.createRegistryKey(RESOURCE_LOCATION);

    public static final Registry<MathExpression.Type<?>> REGISTRY =
            new RegistryBuilder<>(KEY).sync(true).defaultKey(RESOURCE_LOCATION).maxId(256).create();

    public static final MathExpression.Type<ExpressionCodec.Constant> CONSTANT =
            register("constant", ExpressionCodec.Constant.CODEC);

    public static final MathExpression.Type<ExpressionCodec.Variable> VARIABLE =
            register("variable", ExpressionCodec.Variable.CODEC);

    public static final MathExpression.Type<ExpressionCodec.Read> READ_EXPRESSION =
            register("read_expression", ExpressionCodec.Read.CODEC);

    public static final MathExpression.Type<ExpressionCodec.UnaryOperation.Negate> NEGATE =
            register("negate", ExpressionCodec.UnaryOperation.Negate.CODEC);

    public static final MathExpression.Type<ExpressionCodec.UnaryOperation.Sqrt> SQRT =
            register("sqrt", ExpressionCodec.UnaryOperation.Sqrt.CODEC);

    public static final MathExpression.Type<ExpressionCodec.UnaryOperation.Cbrt> CBRT =
            register("cbrt", ExpressionCodec.UnaryOperation.Cbrt.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Add> ADD =
            register("add", ExpressionCodec.BinaryOperation.Add.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Subtract> SUBTRACT =
            register("subtract", ExpressionCodec.BinaryOperation.Subtract.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Multiply> MULTIPLY =
            register("multiply", ExpressionCodec.BinaryOperation.Multiply.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Divide> DIVIDE =
            register("divide", ExpressionCodec.BinaryOperation.Divide.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Modulo> MODULO =
            register("modulo", ExpressionCodec.BinaryOperation.Modulo.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Power> POWER =
            register("power", ExpressionCodec.BinaryOperation.Power.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Max> MAX =
            register("max", ExpressionCodec.BinaryOperation.Max.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Min> MIN =
            register("min", ExpressionCodec.BinaryOperation.Min.CODEC);

    public static final MathExpression.Type<ExpressionCodec.BinaryOperation.Comparison> COMPARISON =
            register("comparison", ExpressionCodec.BinaryOperation.Comparison.CODEC);

    public static final MathExpression.Type<ExpressionCodec.ConditionOperation> CONDITION =
            register("condition", ExpressionCodec.ConditionOperation.CODEC);

    public static final MathExpression.Type<ExpressionCodec.Logical> LOGICAL =
            register("logical", ExpressionCodec.Logical.CODEC);

    public static final MathExpression.Type<ExpressionCodec.Not> NOT =
            register("not", ExpressionCodec.Not.CODEC);

    public static final MathExpression.Type<ExpressionCodec.LevelTable> LEVEL_TABLE =
            register("level_table", ExpressionCodec.LevelTable.CODEC);

    private static <T extends MathExpression> MathExpression.Type<T> register(String name, Codec<T> codec) {
        return Registry.register(
                REGISTRY,
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "expression_" + name),
                new MathExpression.Type<>(codec));
    }

}
