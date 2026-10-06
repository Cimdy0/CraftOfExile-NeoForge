package com.cimdy.coe.data.codec.talent.data;
import com.cimdy.coe.event.net.NetEvent;

/**
 * 武器组（快捷栏）绑定工具。
 *
 * <p>仿 POE2 武器专精，但映射到 MC 的快捷栏栏位：玩家可为每个快捷栏栏位（0–8）
 * 分配天赋，作为一套「武器组」。全局节点（{@link #GLOBAL}）恒点亮；绑定到某栏位的节点
 * 仅当该栏位被选中（激活）时点亮，否则休眠（仍分配但失效、不提供效果、不计入点亮连通）。
 * 切换快捷栏栏位即自动切换激活武器组（见 {@code NetEvent} 的玩家 tick 监听）。
 *
 * <p>绑定值约定：{@code 0}=全局；栏位 {@code s}(0–8) 的绑定值为 {@code s+1}(1–9)。
 * 激活值（当前选中的快捷栏栏位对应的绑定值）同理为 0–9。
 */
public final class TalentWeaponSet {
    /** 全局绑定：无论激活哪个栏位都点亮（默认）。 */
    public static final int GLOBAL = 0;
    /** 快捷栏栏位数（0–8）。 */
    public static final int HOTBAR_SLOTS = 9;
    /** 绑定值上限：GLOBAL=0，栏位 s 的绑定值为 s+1（1–9）。 */
    public static final int MAX_BINDING = HOTBAR_SLOTS;

    private TalentWeaponSet() {
    }

    /** 栏位索引 → 绑定值（栏位 0 → 1，栏位 8 → 9）。 */
    public static int forSlot(int slot) {
        return slot + 1;
    }

    /** 绑定值 → 栏位索引（1–9 → 0–8）；全局(0)返回 -1。 */
    public static int slotOf(int binding) {
        return binding - 1;
    }

    /** 是否为全局绑定。 */
    public static boolean isGlobal(int binding) {
        return binding == GLOBAL;
    }

    /**
     * 该绑定在当前激活值（0–9）下是否点亮。
     * 全局节点恒点亮；栏位绑定节点仅当激活值与其一致时点亮。
     */
    public static boolean active(int binding, int activeValue) {
        return binding == GLOBAL || binding == activeValue;
    }

    /** 激活值（0–9）→ 人类可读描述，用于 UI 与 tooltip。 */
    public static String describe(int activeValue) {
        if (activeValue == GLOBAL) return "全局";
        int slot = slotOf(activeValue);
        return (slot >= 0 && slot < HOTBAR_SLOTS) ? "快捷栏 " + (slot + 1) : "全局";
    }
}
