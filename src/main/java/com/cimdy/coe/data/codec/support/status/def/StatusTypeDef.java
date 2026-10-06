package com.cimdy.coe.data.codec.support.status.def;

import com.cimdy.coe.data.gen.stat.effect.StatEffectGen;
import com.cimdy.coe.data.codec.support.status.def.StatusType.MagnitudeMeaning;
import java.util.Set;

/**
 * Status 类型定义（数据驱动）：取代原 {@link StatusType} 中散落的「静态常量 + 行为 lambda」注册方式。
 *
 * <p>本记录是纯数据：id / 分类 / 标签 / 时长 / 积蓄规格 / 行为名——行为经
 * {@link #tickBehaviorId()} / {@link #statBehaviorId()} 引用 {@link StatusBehavior} 中有限命名的实现。
 * 新增 / 调整一个状态 = 声明一条本记录（数据），无需编写 Java 注册常量与 lambda；行为实现若已存在则直接复用 id。
 *
 * <p>既可由 Java 工厂（仿 AffixGen / StatEffectGen 的类型安全范式）声明，也可经 Codec 从数据包加载（预留，
 * 故本类型保持为纯数据记录、字段均基础类型 / 枚举 / 既有数据记录）。
 */
public record StatusTypeDef(
        String id,
        StatusKind kind,
        boolean stackable,
        int maxStacks,
        MagnitudeMeaning magnitudeMeaning,
        String tickBehaviorId,
        String statBehaviorId,
        int defaultDurationTicks,
        AilmentSpec ailment,
        Set<String> tags,
        String displayText,
        Boolean showTime) {

    public static Builder def(String id, StatusKind kind) {
        return new Builder(id, kind);
    }

    public static final class Builder {
        private final String id;
        private final StatusKind kind;
        private boolean stackable = false;
        private int maxStacks = 1;
        private MagnitudeMeaning magnitudeMeaning = MagnitudeMeaning.NONE;
        private String tickBehaviorId = "none";
        private String statBehaviorId = "none";
        private int defaultDurationTicks = 80;
        private AilmentSpec ailment = AilmentSpec.NONE;
        private Set<String> tags = Set.of();
        private String displayText = null;
        private Boolean showTime = null;

        Builder(String id, StatusKind kind) {
            this.id = id;
            this.kind = kind;
        }

        public Builder stackable(boolean v) { this.stackable = v; return this; }
        /**
         * 叠层上限；<b>{@code v <= 0} = 无上限</b>（默认 1）。
         *
         * <p>为什么不用 {@code Integer.MAX_VALUE} 之类的「大哨值」当无上限：StatusEngine 的 cap 是
         * {@code maxStacks + capBonus}（{@code status_max_stacks} 加算），哨值 + 正 bonus 会整数溢出成负数，
         * {@code Math.max(1, 负数)} 反而把上限压回 1（等于「恒只有 1 层」）。故选 0 / 负值作显式无上限声明，
         * 由 StatusEngine 直接跳过夹紧（cap = Integer.MAX_VALUE），既不溢出也不参与 capBonus 相加。
         */
        public Builder maxStacks(int v) { this.maxStacks = v; return this; }
        /** 无上限叠层：等价于 {@code stackable(true).maxStacks(0)}，理由见 {@link #maxStacks(int)}（{@code v <= 0} = 无上限）。 */
        public Builder uncapped() { this.stackable = true; this.maxStacks = 0; return this; }
        public Builder mag(MagnitudeMeaning v) { this.magnitudeMeaning = v; return this; }
        public Builder tick(String v) { this.tickBehaviorId = v; return this; }
        public Builder stat(String v) { this.statBehaviorId = v; return this; }
        public Builder duration(int v) { this.defaultDurationTicks = v; return this; }
        public Builder ailment(AilmentSpec v) { this.ailment = v; return this; }
        public Builder tags(String... v) { this.tags = Set.of(v); return this; }
        public Builder tags(Set<String> v) { this.tags = v; return this; }
        public Builder display(String text, boolean showTime) { this.displayText = text; this.showTime = showTime; return this; }

        public StatusTypeDef build() {
            return new StatusTypeDef(id, kind, stackable, maxStacks, magnitudeMeaning,
                    tickBehaviorId, statBehaviorId, defaultDurationTicks, ailment, tags, displayText, showTime);
        }
    }
}
