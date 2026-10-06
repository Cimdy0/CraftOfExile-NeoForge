package com.cimdy.coe.data.codec.support.math;

import com.mojang.serialization.Codec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public enum ComparisonOperator implements StringRepresentable {
    EQUAL("==", 0),
    NOT_EQUAL("!=", 1),
    LESS_THAN("<", 2),
    LESS_THAN_OR_EQUAL("<=", 3),
    GREATER_THAN(">", 4),
    GREATER_THAN_OR_EQUAL(">=", 5);

    // 越界 ID 静默回退到 EQUAL(id=0)。注意：这会把无效比较运算符变成相等比较，
    // 可能掩盖配置错误，但保证了反序列化时的健壮性（N2）。
    public static final IntFunction<ComparisonOperator> BY_ID =
            ByIdMap.continuous(ComparisonOperator::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ComparisonOperator> CODEC =
            StringRepresentable.fromEnum(ComparisonOperator::values);
    private final String comparison;
    private final int id;

    ComparisonOperator(String comparison, int value) {
        this.comparison = comparison;
        this.id = value;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.comparison;
    }

    private static final Map<String, ComparisonOperator> BY_SYMBOL = new HashMap<>();

    static {
        for (ComparisonOperator op : values()) {
            BY_SYMBOL.put(op.comparison, op);
        }
    }

    // 通过静态映射 O(1) 查找符号（N3），与 StringRepresentable.byName 互补
    public static ComparisonOperator fromSymbol(String comparison) {
        ComparisonOperator op = BY_SYMBOL.get(comparison);
        if (op == null) {
            throw new IllegalArgumentException("未知的比较运算符: " + comparison);
        }
        return op;
    }

    public boolean compare(int a, int b) {
        return switch (this) {
            case EQUAL -> a == b;
            case NOT_EQUAL -> a != b;
            case LESS_THAN -> a < b;
            case LESS_THAN_OR_EQUAL -> a <= b;
            case GREATER_THAN -> a > b;
            case GREATER_THAN_OR_EQUAL -> a >= b;
        };
    }

    public boolean compare(float a, float b) {
        return switch (this) {
            case EQUAL -> Math.abs(a - b) < 1e-6;
            case NOT_EQUAL -> Math.abs(a - b) >= 1e-6;
            case LESS_THAN -> a < b;
            case LESS_THAN_OR_EQUAL -> a <= b;
            case GREATER_THAN -> a > b;
            case GREATER_THAN_OR_EQUAL -> a >= b;
        };
    }

    public boolean compare(double a, double b) {
        return switch (this) {
            case EQUAL -> Math.abs(a - b) < 1e-12;
            case NOT_EQUAL -> Math.abs(a - b) >= 1e-12;
            case LESS_THAN -> a < b;
            case LESS_THAN_OR_EQUAL -> a <= b;
            case GREATER_THAN -> a > b;
            case GREATER_THAN_OR_EQUAL -> a >= b;
        };
    }

    @Override
    public String toString() {
        return comparison;
    }

}
