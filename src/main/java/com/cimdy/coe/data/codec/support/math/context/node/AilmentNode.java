package com.cimdy.coe.data.codec.support.math.context.node;

/**
 * 异常（Ailment）域读值节点类别（节点分类）：标记「伤害性 / 控制性异常」相关读值节点。
 * <p>
 * 当前为空标记接口，承接 {@link StatusNode} -> {@link AilmentNode} 分类层级，便于后续按
 * ailment 维度（元素异常种类数、凋零层数等）集中扩展与 instanceof 识别。
 */
public interface AilmentNode extends StatusNode {
}
