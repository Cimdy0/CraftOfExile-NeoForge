package com.cimdy.coe.data.codec.support.math.context.node;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 求值结果的统一值域 —— Context DSL 节点求值后的四种可能形态，以及它们到四种消费视角的投影。
 * <p>
 * 背景：旧管线里节点只能返回 {@code double}，导致「这个值属于谁」「有几个实体命中」这类信息
 * 无处安放，只能靠 {@code SplitSink} 回调旁路传递，而布尔判断又在各消费端各写一遍
 * （{@code >= 1} / {@code == 0} / {@code <= 0} 语义漂移）。本接口把求值结果收成
 * <b>一个</b>值对象，并<b>一次性</b>定义好它向四种消费视角的投影，取代旁路与重复判断。
 *
 * <h2>四个投影方法</h2>
 * <ul>
 *   <li>{@link #number()} —— 标量视角：给数值运算与 math 侧比较用；</li>
 *   <li>{@link #bool()} —— 布尔视角：给条件判断用；</li>
 *   <li>{@link #entities()} —— 实体视角：给「对哪些实体施加效果」用；</li>
 *   <li>{@link #pairs()} —— 配对视角：给需要「实体 + 各自数值」的一对一施加用。</li>
 * </ul>
 *
 * <h2>为什么要投影而不是各分支各写一份</h2>
 * 每个分支只需对自己的数据形态做一次投影定义，四个视角的定义集中在此处，
 * 新增消费视角时只需在此处补一个方法，而非在 45 个节点实现里各写一遍。
 * 投影<b>允许有损</b>：并非每个分支都能提供每个视角的信息（例如标量没有实体归属），
 * 此时返回空列表而非 {@code null}，调用方无需判空。
 *
 * @see Weighted 「实体-值」对应关系的载体，取代旧 {@code SplitSink} 旁路
 */
public sealed interface ExprValue
        permits ExprValue.Num, ExprValue.Bool, ExprValue.Entities, ExprValue.Pairs {

    /**
     * 标量视角：折叠为单个 {@code double}，供数值运算与比较使用。
     * <p>
     * 无实体信息可提供时返回 {@code 0} 而非 {@code null}。
     */
    double number();

    /**
     * 布尔视角：折叠为真假判断，供条件表达式使用。
     * <p>
     * <b>全库唯一的布尔定义</b>：各分支的具体定义见其实现处的说明，
     * 消费端一律调用本方法，禁止再自行写 {@code >= 1} / {@code != 0} 之类判断。
     */
    boolean bool();

    /**
     * 实体视角：投影出参与本次求值的实体列表，供「对哪些实体施加效果」使用。
     * <p>
     * 无实体信息可提供时返回空列表，恒不为 {@code null}。
     */
    List<LivingEntity> entities();

    /**
     * 配对视角：投影出「实体 + 数值」对列表，供需要逐对分别作用（一对一）的消费端使用。
     * <p>
     * 无对应关系可提供时返回空列表，恒不为 {@code null}。
     */
    List<Weighted> pairs();

    /**
     * 标量工厂：便捷构造 {@link Num}。
     *
     * @param v 数值
     */
    /**
     * <b>全库唯一的布尔判定口径</b>：非零即真（{@code v != 0}）。
     * <p>
     * 本方法是 {@linkplain Num#bool() 标量布尔投影} 与所有「把求值结果当布尔用」
     * 的判定点（如 {@code ValueExpr#test} / 过滤谓词 / 条件入口）共同的唯一落点：
     * 任何消费端都<b>不得</b>自行写 {@code >= 1} / {@code != 0} 之类判断，
     * 否则同一语义会在多处漂移（这正是 S2 要收口的问题）。
     * <p>
     * 口径为「非零」而非「{@code >= 1}」：后者会把 {@code 0.5} 这类有效小数
     * 误判为假、把负数误判为假，与 math 层 {@code Logical} / {@code Not}
     * 节点自身的 {@code != 0} 判定也不一致。
     *
     * @param v 求值得到的标量
     * @return {@code v != 0}
     */
    static boolean truth(double v) {
        return v != 0;
    }
    static ExprValue num(double v) {
        return new Num(v);
    }

    /**
     * 空候选兜底：返回 {@code Num(0.0)}。
     * <p>
     * 语义上「什么都没选中」既不是真也不是假，而是数值零 —— 故复用 {@link Num} 而不另设分支。
     * {@link Num#bool()} 对零返回 {@code false}，因此兜底值同时满足「数值上为 0」
     * 与「布尔上为假」两种消费预期，无需调用方特判。
     */
    static ExprValue none() {
        return new Num(0.0);
    }

    /**
     * 纯标量分支：无实体归属的单个数值。
     * <p>
     * 语义：读到的就是一个数（属性值、等级、距离、计时 tick 等），既不指向实体，
     * 也不存在多个候选 —— 概念上不存在「实体」这一维度。
     *
     * @param value 标量数值
     */
    record Num(double value) implements ExprValue {

        /**
         * {@inheritDoc}
         * <p>
         * 原值直传。
         */
        @Override
        public double number() {
            return value;
        }

        /**
         * {@inheritDoc}
         * <p>
         * <b>本方法是整个值域重构的基石</b>：定义为 {@code value != 0}，而非 {@code >= 1.0}。
         * <p>
         * 旧代码各写各的，常见 {@code >= 1}（把 {@code 0.5} 这种有效小数误判为假）
         * 或 {@code <= 0}（把负数误判为真）。收敛到此处的唯一标准后，
         * 「非零即为真」对全库一致生效；负数与小数均按其真实真值处理。
         */
        @Override
        public boolean bool() {
            return truth(value);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 返回空列表：标量在概念上不携带实体维度，
         * 而非「有实体但恰好为空」—— 消费端据此可区分「不适用」与「适用但零个」。
         */
        @Override
        public List<LivingEntity> entities() {
            return List.of();
        }

        /**
         * {@inheritDoc}
         * <p>
         * 返回空列表，理由同 {@link #entities()}：无归属即无配对。
         */
        @Override
        public List<Weighted> pairs() {
            return List.of();
        }
    }

    /**
     * 布尔分支：一个真假判断，附带其成立时所指向的实体。
     * <p>
     * 语义：判断本身是二值的，但「为什么真」需要一个落点 —— 典型如
     * 「存在已点燃的目标」，真假之外还需知道是<b>哪一个</b>目标被点燃，
     * 以便后续对其施加效果。该落点即 {@code subject}。
     * <p>
     * 约定：<b>真值必须带 subject</b>。理由是「真」若不指向任何实体，
     * 它对下游的实体消费端就毫无用处，却又在布尔上为真，属于自相矛盾的中间态；
     * 与其让该矛盾态流到消费端再被各处判空兜底，不如在构造点就拒绝。
     * 「假」则允许 subject 为 {@code null} —— 假不需要落点。
     *
     * @param value   判断结果
     * @param subject 判断成立所指向的实体；{@code value} 为 {@code false} 时允许为 {@code null}
     */
    record Bool(boolean value, @Nullable LivingEntity subject) implements ExprValue {

        /**
         * 紧凑构造器：校验「真值必须带 subject」。
         *
         * @throws IllegalArgumentException 当 {@code value} 为 {@code true} 而 {@code subject} 为 {@code null} 时
         */
        public Bool {
            if (value && subject == null) throw new IllegalArgumentException("真值必须带 subject");
        }

        /**
         * {@inheritDoc}
         * <p>
         * 以 {@code 1.0 / 0.0} 投影：布尔参与数值运算时（如条件表达式嵌在算式中）
         * 按惯例真即一、假即零。
         */
        @Override
        public double number() {
            return value ? 1.0 : 0.0;
        }

        /**
         * {@inheritDoc}
         * <p>
         * 原值直传：本分支的布尔定义是<b>显式给出</b>的，无需像 {@link Num} 那样从数值推导。
         */
        @Override
        public boolean bool() {
            return value;
        }

        /**
         * {@inheritDoc}
         * <p>
         * 假值投影为空列表而非 {@code List.of(null)}：subject 在假值时本就允许为 {@code null}，
         * 把 null 塞进列表会让消费端拿到含空洞的列表，判空成本转嫁给每个调用方。
         */
        @Override
        public List<LivingEntity> entities() {
            return (value && subject != null) ? List.of(subject) : List.of();
        }

        /**
         * {@inheritDoc}
         * <p>
         * 返回空列表：布尔分支只有一个真假结论，没有「每个实体各自的数值」可言，
         * 数值投影由 {@link #number()} 的 {@code 1.0 / 0.0} 承担。
         */
        @Override
        public List<Weighted> pairs() {
            return List.of();
        }
    }

    /**
     * 实体集合分支：命中的一组实体，无逐个数值。
     * <p>
     * 语义：回答「命中了哪些实体」，不回答「各自的值是多少」——
     * 如范围内实体查询、存活敌对实体计数这类只关心集合本身的场景。
     *
     * @param members 命中的实体列表
     */
    record Entities(List<LivingEntity> members) implements ExprValue {

        /**
         * 紧凑构造器：防御性拷贝为不可变列表，
         * 避免外部持有原列表引用后修改本record 的「成员」视图。
         *
         * @param members 命中的实体列表
         * @throws NullPointerException 当列表本身为 {@code null} 或含 {@code null} 元素时（{@link List#copyOf} 语义）
         */
        public Entities {
            members = List.copyOf(members);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 以<b>数量</b>投影：集合的大小就是它在标量视角下唯一可用的信息
         * （如「范围内有 3 个敌人」直接参与比较）。
         */
        @Override
        public double number() {
            return members.size();
        }

        /**
         * {@inheritDoc}
         * <p>
         * 以<b>非空</b>投影：命中至少一个即视为真，命中零个即为假。
         */
        @Override
        public boolean bool() {
            return !members.isEmpty();
        }

        /**
         * {@inheritDoc}
         * <p>
         * 本分支的主投影：成员列表直传（构造时已 {@link List#copyOf} 不可变）。
         */
        @Override
        public List<LivingEntity> entities() {
            return members;
        }

        /**
         * {@inheritDoc}
         * <p>
         * 返回空列表：本分支不持有逐实体数值，构造不出有意义的 {@link Weighted}。
         * 若强行以成员自身造pair，其 value 无处取值，只能填0，
         * 那会让下游误以为「该实体算出0」而非「本就没有数值」，故此处明确留空。
         */
        @Override
        public List<Weighted> pairs() {
            return List.of();
        }
    }

    /**
     * 配对分支：每个实体各自带一个数值。
     * <p>
     * 语义：既知道命中了哪些实体，又知道<b>每个实体各自</b>的值——
     * 取代旧 {@code SplitSink} 旁路传递的「实体-值对应」关系（见 {@link Weighted}）。
     *
     * @param members 「实体-数值」对列表
     */
    record Pairs(List<Weighted> members) implements ExprValue {

        /**
         * 紧凑构造器：防御性拷贝为不可变列表，避免外部改动本 record 的成员视图。
         *
         * @param members 「实体-数值」对列表
         * @throws NullPointerException 当列表本身为 {@code null} 或含 {@code null} 元素时（{@link List#copyOf} 语义）
         */
        public Pairs {
            members = List.copyOf(members);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 以<b>各项数值之和</b>投影：标量视角下多个配对只能折叠为总量。
         * 需逐个数值时请改用 {@link #pairs()}。
         */
        @Override
        public double number() {
            double sum = 0.0;
            for (Weighted w : members) sum += w.value();
            return sum;
        }

        /**
         * {@inheritDoc}
         * <p>
         * 以<b>非空</b>投影：存在配对即为真。注意判据是<b>对的数量</b>而非数值之和 ——
         * 一个value 为 0 的配对仍然是一个配对（表示「这个实体被算到了，值为零」），
         * 因此 {@code number() == 0} 与 {@code bool() == false} 并不等价，此处按前者语义取非空。
         */
        @Override
        public boolean bool() {
            return !members.isEmpty();
        }

        /**
         * {@inheritDoc}
         * <p>
         * 抽取各pair 的 receiver 并去重（{@link LinkedHashSet} 保序、剔除 {@code null} receiver）：
         * <ul>
         *   <li><b>去重</b>：同一实体可能对应多个 pair（如同一目标被多段求值各出一个数），
         *       若不去重，消费端会对同一实体重复施加效果。
         *       去重依据是 {@link LivingEntity#equals(Object)}，而
         *       {@code net.minecraft.world.entity.Entity.equals} 比较的是<b>实体 id 字段</b>
         *       （并非引用身份）—— 这正是此处想要的：同一实体无论被取出几次、
         *       拿到几个实例，只要 id 相同就折叠为一个。</li>
         *   <li><b>保序</b>：用 {@link LinkedHashSet} 而非 {@code HashSet}，
         *       保持 pair 的原始先后，使「最近的目标」等顺序敏感语义不被破坏；</li>
         *   <li><b>剔除 null</b>：{@link Weighted#receiver()} 允许为 {@code null}
         *       （该数值无实体归属），把 null 放进实体列表会让所有消费端都要判空兜底。</li>
         * </ul>
         */
        @Override
        public List<LivingEntity> entities() {
            LinkedHashSet<LivingEntity> seen = new LinkedHashSet<>();
            for (Weighted w : members) {
                if (w.receiver() != null) seen.add(w.receiver());
            }
            return List.copyOf(seen);
        }

        /**
         * {@inheritDoc}
         * <p>
         * 本分支的主投影：成员列表直传（构造时已 {@link List#copyOf} 不可变），
         * 保留全部配对信息，<b>不做去重</b> —— 数值本身可能有别，丢掉就丢了语义。
         */
        @Override
        public List<Weighted> pairs() {
            return members;
        }
    }
}