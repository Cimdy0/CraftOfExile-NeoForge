package com.cimdy.coe.data.codec.support.math.context;

/**
 * 选择管线输出模式（方案四）：循环结束后一次分发，不进候选热循环。
 * <ul>
 *   <li>{@link #COUNT} —— 匹配候选数量；</li>
 *   <li>{@link #SUM} —— 对每个匹配候选在其作用域内读值并累加；</li>
 *   <li>{@link #PICK} —— 挑一个候选重绑为当前候选，嵌套体以它为参照继续求值；</li>
 *   <li>{@link #EACH} —— 一对一拆分：对每个匹配候选在其作用域内求值，并将
 *       {@code (候选实体, 值)} 逐对发射到 {@link SplitSink}（若消费端已注入），
 *       保留实体↔值对应关系供施加阶段单独作用；标量返回值为各候选值之和
 *       （未注入 sink 时即退化为 SUM 语义）。</li>
 * </ul>
 */
public enum OutputMode {
    COUNT, SUM, PICK, EACH
}
