package com.cimdy.coe.registry;

import com.cimdy.coe.CraftOfExile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * UI 附件音效注册（计划附录 D.3）：为 {@code ui.widget.attach.Sfx} 提供自定义 {@link SoundEvent}
 * 挂载点，沿用模组既有 {@link DeferredRegister} 惯用法（与 {@code MobEffectRegistry} 同款）。
 *
 * <p><b>资源已落地（2026-10-05）</b>：{@code assets/coe/sounds.json} 条目 ↔ 本注册键 ↔
 * {@code assets/coe/sounds/sfx/*.ogg} 三链对齐，音频为 <b>Kenney Interface Sounds（CC0）</b>
 * 转码入库（44100Hz mono Ogg Vorbis，本目录保留 {@code KENNEY_CC0_LICENSE.txt}）。
 * {@code Sfx.MOD_DEFAULTS} 默认映射已切到自定义音效，无自定义资源的 Kind 维持原版兜底。
 * 播放时若 .ogg 缺失，MC 声音系统仅记 warn 并静默跳过，不阻断渲染。
 */
public final class SoundEventsRegistry {

    private SoundEventsRegistry() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, CraftOfExile.MODID);

    // ---- 附录 D.3 音效清单（路径 = sounds.json 键，namespace=coe）----
    /** 点击（占位；Pressable 自带 UI_BUTTON_CLICK，默认不重复发声）。 */
    public static final Holder<SoundEvent> SFX_CLICK    = reg("sfx.click");
    /** 悬停（高频，默认关；占位）。 */
    public static final Holder<SoundEvent> SFX_HOVER    = reg("sfx.hover");
    /** 开屏（占位）。 */
    public static final Holder<SoundEvent> SFX_OPEN      = reg("sfx.open");
    /** 关屏（占位）。 */
    public static final Holder<SoundEvent> SFX_CLOSE     = reg("sfx.close");
    /** 切页 / 切 Tab（占位）。 */
    public static final Holder<SoundEvent> SFX_TAB       = reg("sfx.tab");
    /** 滚轮滚动（高频，默认关；占位）。 */
    public static final Holder<SoundEvent> SFX_SCROLL    = reg("sfx.scroll");
    /** 物品取放 put（占位）。 */
    public static final Holder<SoundEvent> SFX_PUT       = reg("sfx.put");
    /** 切割成功 cut_ok（占位；cut_fail 已废除 —— 切割 100% 确定）。 */
    public static final Holder<SoundEvent> SFX_CUT_OK    = reg("sfx.cut_ok");
    /** 禁用点击警告 warn（占位）。 */
    public static final Holder<SoundEvent> SFX_WARN       = reg("sfx.warn");
    /** 无效操作错误 err（占位）。 */
    public static final Holder<SoundEvent> SFX_ERR       = reg("sfx.err");

    private static Holder<SoundEvent> reg(String path) {
        return SOUNDS.register(path,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, path)));
    }
}
