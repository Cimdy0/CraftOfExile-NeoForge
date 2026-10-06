package com.cimdy.coe.ui.widget.attach;

import com.cimdy.coe.registry.SoundEventsRegistry;
import com.cimdy.coe.ui.widget.core.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.EnumMap;
import java.util.Map;

/**
 * 音效附件：事件 → 播放 {@link SoundEvent} 的映射。
 *
 * <p>实现遵循计划四·六 / 附录 D.3：优先复用原版 {@code SoundEvents}（UI_BUTTON_CLICK / UI_TOAST_IN /
 * ITEM_PICKUP 等）；自定义音效（{@code SoundEventsRegistry.SFX_*}）音频 .ogg 已落地
 * （Kenney Interface Sounds · CC0，2026-10-05 转码入库），默认映射（{@link #MOD_DEFAULTS}）即走
 * 自定义音效；个别无自定义资源的 Kind（CONFIRM/CANCEL/FAIL/TOAST/CLEAR/DRAG_START）维持原版兜底。
 *
 * <p>纪律：
 * <ul>
 *   <li>音量 0.3–0.5，默认 0.4；悬停等高频音效 <b>0.15 极轻且默认关</b>（{@link #hover(boolean)}）；
 *       滚轮 {@link Kind#SCROLL} 同属高频，默认关（{@link #scroll(boolean)}）；</li>
 *   <li>皮肤差异 = 每组 pitch 偏移（0.95/1.0/1.1）：由 {@link #skinPitch(float)} 全局注入，
 *       换肤处回调（Theme.apply / StylePreset 桥接）已接线，本文件零改动；</li>
 *   <li>发射 {@link #emit(Node, Kind)} 沿父链向上找挂载的音效附件，无附件 = no-op ——
 *       不改变输入与数据链路，{@code Pressable} 自带的点击音（{@code UI_BUTTON_CLICK}）
 *       与之互不干扰（CLICK 事件在 Pressable 节点上由组件自带音效负责，本附件<b>不重复实现点击音</b>）。</li>
 * </ul>
 */
public final class Sfx implements Attachment {

    /** 音效事件种类（计划附录 D.3 清单 ↔ 四·六 M1 Kind 收敛）。 */
    public enum Kind {
        OPEN, CLOSE,           // 打开/关闭窗口
        SELECT,                // 列表选中
        TAB,                   // 切页 / 切 Tab（D.3 新增：区别于列表 SELECT）
        SCROLL,                // 滚轮滚动（D.3 新增；高频，默认关）
        DRAG_START, DROP,      // 拖放成功（DROP = D.3 put 物品取放）
        CONFIRM, CANCEL,       // 确认/取消
        SUCCESS, FAIL,         // 成功/失败（SUCCESS = D.3 cut_ok 切割成功）
        WARN, ERR,             // D.3 新增：禁用点击警告 / 无效操作错误（cut_fail 已废除）
        TOAST,                 // Toast 出现
        CLEAR,                 // 筛选清除
        HOVER                  // 悬停（高频，默认关，极轻 0.15）
    }

    // 26.2 中 SoundEvents 常量类型混杂：UI_BUTTON_CLICK / NOTE_BLOCK_BASS 为 Holder<SoundEvent>，
    // ITEM_PICKUP / UI_TOAST_IN 为裸 SoundEvent —— 统一包装成 Holder（裸值用 Holder.direct 包）。
    private static final Map<Kind, Holder<SoundEvent>> VANILLA_DEFAULTS = new EnumMap<>(Kind.class);
    static {
        VANILLA_DEFAULTS.put(Kind.OPEN,      SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.CLOSE,     SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.SELECT,    SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.TAB,       SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.SCROLL,    SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.DRAG_START, SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.DROP,      Holder.direct(SoundEvents.ITEM_PICKUP));
        VANILLA_DEFAULTS.put(Kind.CONFIRM,   SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.CANCEL,    SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.SUCCESS,   Holder.direct(SoundEvents.ITEM_PICKUP));
        VANILLA_DEFAULTS.put(Kind.FAIL,      SoundEvents.NOTE_BLOCK_BASS);
        VANILLA_DEFAULTS.put(Kind.WARN,      SoundEvents.NOTE_BLOCK_BASS);
        VANILLA_DEFAULTS.put(Kind.ERR,       SoundEvents.NOTE_BLOCK_BASS);
        VANILLA_DEFAULTS.put(Kind.TOAST,     Holder.direct(SoundEvents.UI_TOAST_IN));
        VANILLA_DEFAULTS.put(Kind.CLEAR,     SoundEvents.UI_BUTTON_CLICK);
        VANILLA_DEFAULTS.put(Kind.HOVER,     SoundEvents.UI_BUTTON_CLICK);
    }

    /**
     * 模组默认映射：原版兜底之上，把已落地的 Kenney CC0 自定义音效挂到对应 Kind
     * （2026-10-05 ogg 入库后启用；sounds.json 键 ↔ SoundEventsRegistry.SFX_* ↔ assets/coe/sounds/sfx/*.ogg 三链对齐）。
     * 无自定义资源的 Kind（CONFIRM/CANCEL/FAIL/TOAST/CLEAR/DRAG_START）保持原版。
     */
    private static final Map<Kind, Holder<SoundEvent>> MOD_DEFAULTS = new EnumMap<>(Kind.class);
    static {
        MOD_DEFAULTS.putAll(VANILLA_DEFAULTS);
        MOD_DEFAULTS.put(Kind.SELECT,  SoundEventsRegistry.SFX_CLICK);
        MOD_DEFAULTS.put(Kind.HOVER,   SoundEventsRegistry.SFX_HOVER);
        MOD_DEFAULTS.put(Kind.OPEN,    SoundEventsRegistry.SFX_OPEN);
        MOD_DEFAULTS.put(Kind.CLOSE,   SoundEventsRegistry.SFX_CLOSE);
        MOD_DEFAULTS.put(Kind.TAB,     SoundEventsRegistry.SFX_TAB);
        MOD_DEFAULTS.put(Kind.SCROLL,  SoundEventsRegistry.SFX_SCROLL);
        MOD_DEFAULTS.put(Kind.DROP,    SoundEventsRegistry.SFX_PUT);
        MOD_DEFAULTS.put(Kind.SUCCESS, SoundEventsRegistry.SFX_CUT_OK);
        MOD_DEFAULTS.put(Kind.WARN,    SoundEventsRegistry.SFX_WARN);
        MOD_DEFAULTS.put(Kind.ERR,     SoundEventsRegistry.SFX_ERR);
    }

    /** 悬停音量（D.3：悬停极轻 0.15）。 */
    private static final float HOVER_VOLUME = 0.15F;

    /**
     * 全局皮肤 pitch 偏移（D.3：每款皮肤一组 0.95 / 1.0 / 1.1）。
     * 默认 1.0F = 不偏移。桥接约定：StylePreset.sfxPitch() 落地后，换肤处回调
     * {@code Sfx.skinPitch(preset.sfxPitch())} 即可全量生效。
     */
    private static float skinPitch = 1.0F;

    /** 设置全局皮肤 pitch 偏移（0.95 / 1.0 / 1.1）；clamp 到合理区间防炸音。 */
    public static void skinPitch(float p) {
        skinPitch = Math.max(0.5F, Math.min(2.0F, p));
    }

    private final Map<Kind, Holder<SoundEvent>> sounds = new EnumMap<>(Kind.class);
    private float volume = 0.4F;
    private boolean enabled = true;
    private boolean hoverEnabled = false;
    private boolean scrollEnabled = false;

    private Sfx() {}

    /** 全部使用模组默认映射（自定义已落地 + 原版兜底）的音效附件。 */
    public static Sfx of() {
        Sfx s = new Sfx();
        s.sounds.putAll(MOD_DEFAULTS);
        return s;
    }

    /** 仅挂载指定事件（其余事件不发声）；取模组默认映射（自定义已落地 + 原版兜底）。 */
    public static Sfx of(Kind... kinds) {
        Sfx s = new Sfx();
        for (Kind k : kinds) {
            if (k != null) s.sounds.put(k, MOD_DEFAULTS.get(k));
        }
        return s;
    }

    /** 自定义事件 → 音效映射（需调用方已注册 Holder；找不到时降级为原版同语义）。 */
    public Sfx map(Kind kind, Holder<SoundEvent> sound) {
        if (kind != null) sounds.put(kind, sound == null ? VANILLA_DEFAULTS.get(kind) : sound);
        return this;
    }

    public Sfx volume(float v) { this.volume = Math.max(0F, Math.min(1F, v)); return this; }

    public Sfx enabled(boolean b) { this.enabled = b; return this; }

    /** 开启悬停音效（默认关闭 —— 高频音效默认关，计划四·六 / D.3）。 */
    public Sfx hover(boolean b) { this.hoverEnabled = b; return this; }

    /** 开启滚轮音效（默认关闭 —— 高频音效默认关，D.3）。 */
    public Sfx scroll(boolean b) { this.scrollEnabled = b; return this; }

    /** 本附件是否会对该事件发声。 */
    public boolean plays(Kind kind) {
        if (!enabled || !sounds.containsKey(kind)) return false;
        if (kind == Kind.HOVER) return hoverEnabled;
        if (kind == Kind.SCROLL) return scrollEnabled;
        return true;
    }

    // ------------------------------------------------------------------
    // 发射
    // ------------------------------------------------------------------

    /**
     * 沿父链向上寻找挂载了本事件的音效附件并播放；找不到 / 禁用 / 失败一律静默。
     * 组件与 Screen 在业务回调里调用（不改变输入分发，纯附加）。
     */
    public static void emit(Node node, Kind kind) {
        if (node == null || kind == null) return;
        for (Node n = node; n != null; n = n.parent) {
            Sfx s = find(n);
            if (s != null && s.plays(kind)) {
                s.play(kind, s.sounds.get(kind));
                return;
            }
        }
    }

    /** 从节点的附件列表中取 Sfx（无则 null）。 */
    public static Sfx find(Node node) {
        if (node == null || node.attachments() == null) return null;
        for (Attachment a : node.attachments()) {
            if (a instanceof Sfx s) return s;
        }
        return null;
    }

    private void play(Kind kind, Holder<SoundEvent> se) {
        if (se == null) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getSoundManager() == null) return;
            float v = (kind == Kind.HOVER) ? HOVER_VOLUME : volume; // 悬停极轻
            float pitch = 1.0F * skinPitch;                          // 皮肤 pitch 偏移（0.95/1.0/1.1）
            mc.getSoundManager().play(SimpleSoundInstance.forUI(se.value(), pitch, v));
        } catch (RuntimeException ignored) { /* 音效失败静默降级 */ }
    }
}
