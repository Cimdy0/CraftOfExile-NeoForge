package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;

/**
 * 词缀数值范围载体：把词缀 roll 区间（{@link com.cimdy.coe.data.codec.item.mod.identity.Addition#min()}~
 * {@link com.cimdy.coe.data.codec.item.mod.identity.Addition#max()}）注入到描述链，
 * 让「值渲染拆离」方法在范围模式下渲染 {@code value(min-max)}（如 {@code +4(3-5)}）。
 *
 * <p>放在 {@code support.stat.entry} 包下（而非 {@code item.addition}），
 * 以避免 {@code addition} ↔ {@code mod} 的循环依赖：
 * {@code ModifierStatEffect} / {@link ItemTooltipAssembler} 已实现依赖 {@code support.stat.entry}，
 * 但不依赖 {@code item.addition}。
 */
public record ValueRange(double min, double max) {
}
