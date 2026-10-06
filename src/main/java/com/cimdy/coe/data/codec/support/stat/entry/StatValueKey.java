package com.cimdy.coe.data.codec.support.stat.entry;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * 值键（value key）命名空间 {@code stat.value.<ns>.<id>} 的构造与降级入口。
 *
 * <p><b>为什么需要它</b>：结构模板键（{@code stat.condition.*} / {@code stat.amount.*}）已可翻译，
 * 但模板里的 {@code %s} 参数若直接用 {@link ValueExpr#humanize(String)} 产出的<b>裸英文字符串</b>，
 * 注入模板后不会被本地化（中文下渲染成「每 15 Strength」「处于 Ignited 状态时」）。
 * 本类把这些参数统一换成可译键，使「何时 / 每多少什么」里的<b>值本身</b>也走翻译链。
 *
 * <p><b>拆分优于整句键</b>（用户架构裁定 #1/#3/#4）：一个结构能接收多种输入类型（状态 / 实体类别 /
 * 装备类型 / 作用域）时，<b>输入自己带键</b>，由模板组合；不为「while in X」与「if you recently caused X」
 * 每种组合各铸一个整句键。故命名空间按输入类型划分，同一 id 在所有调用点复用：
 * <ul>
 *   <li>{@code status} / {@code status_tag} / {@code status_kind} —— 状态体系；</li>
 *   <li>{@code entity_type} —— 实体类别 tag；</li>
 *   <li>{@code living_state} / {@code movement} —— 战斗 / 移动状态；</li>
 *   <li>{@code event} —— 事件名；</li>
 *   <li>{@code attribute} —— 属性路径；</li>
 *   <li>{@code src} —— per-X 来源种类（{@code count} / {@code nearby_enemy_count} …）；</li>
 *   <li>{@code equip_slot} / {@code equip_out} / {@code accessory_group} —— 装备与饰品读值；</li>
 *   <li>{@code presence_kind} / {@code presence_relation} —— 在场计数维度；</li>
 *   <li>{@code neg} —— 取反前缀（{@code not}）。</li>
 * </ul>
 *
 * <p><b>降级铁律（严禁静默丢语义，也不许把原始 key 甩给玩家）</b>：当 {@link Language} 里没有
 * {@code stat.value.<ns>.<id>} 时，返回 {@code Component.literal(humanize(id))} —— 一段<b>看得见</b>
 * 的 Title-Case 英文（与改造前逐字一致），而不是裸 {@code stat.value...} 键。
 */
public final class StatValueKey {

    /** 值键前缀。 */
    public static final String PREFIX = "stat.value.";

    private StatValueKey() {
    }

    /** 组装 {@code stat.value.<ns>.<id>}；ns / id 任一为空返回 {@code null}（调用方据此判空）。 */
    @Nullable
    public static String key(String ns, String id) {
        if (ns == null || ns.isBlank() || id == null || id.isBlank()) return null;
        return PREFIX + ns + "." + id;
    }

    /**
     * 取值键组件：键存在 → {@code Component.translatable}；键缺失 → 可见的英文 literal 回落。
     *
     * @param ns 命名空间（如 {@code status}）
     * @param id 值 id（snake_case 原始标识，如 {@code ignite}）
     */
    public static Component of(String ns, String id) {
        return of(ns, id, null);
    }

    /**
     * 取值键组件，可指定回落文本。
     *
     * @param ns         命名空间
     * @param id         值 id
     * @param fallbackRaw 键缺失时渲染的裸文本；{@code null}/空则回落 {@link ValueExpr#humanize(String)}
     * @return 可译组件或 literal 回落；ns / id 为空时返回 {@link Component#empty()}
     */
    public static Component of(String ns, String id, @Nullable String fallbackRaw) {
        // flat-key first: any registered stat.value.<ns>.<id> (single-concept / synonym variant,
        // e.g. frozen / dodge / power_charge / energy_shield / mob_effect.fire_resistance) renders directly -> zero regression.
        String k = key(ns, id);
        if (k == null) return Component.empty();
        Language lang = Language.getInstance();
        if (lang != null && !lang.has(k)) {
            // generic split: compound id -> registered word keys (e.g. lightning_resistance -> stat.light + stat.resistance).
            // only when EVERY token has a stat.<token> word key; otherwise humanize fallback. No central registry.
            List<String> parts = splitWords(id);
            if (parts != null) {
                MutableComponent c = Component.translatable(parts.get(0));
                for (int i = 1; i < parts.size(); i++) {
                    c = c.append(Component.translatable("stat.concat_sep")).append(Component.translatable(parts.get(i)));
                }
                return c;
            }
            String fb = (fallbackRaw == null || fallbackRaw.isEmpty())
                    ? ValueExpr.humanize(id)
                    : fallbackRaw;
            if (fb == null || fb.isEmpty()) return Component.empty();
            return Component.literal(fb);
        }
        return Component.translatable(k);
    }

    /**
     * 复合 id -> 单词键序列（通用拆分，无中央注册表）。
     * 把 id 按 _ 拆成 token，若每个 token 都已注册 stat.<token> 单词键，返回拼接序列
     * （以 stat.concat_sep 作语言感知分隔符）；否则 null（交回调用方走 flat 键 / humanize）。
     * 例：lightning_resistance -> [stat.light, stat.resistance]（lightning 归一为 light，复用 stat.light=闪电）。
     */
    public static @Nullable List<String> splitWords(String id) {
        String[] tokens = id.split("_");
        if (tokens.length < 2) return null;
        Language lang = Language.getInstance();
        if (lang == null) return null;
        List<String> out = new ArrayList<>(tokens.length);
        for (String t : tokens) {
            if (t.equals("lightning")) t = "light"; // element word "lightning" reuses stat.light (闪电); no twin key (以结果为一)
            String wk = "stat." + t;
            if (!lang.has(wk)) return null;
            out.add(wk);
        }
        return out;
    }

    /**
     * 复用<b>外部权威键</b>（如 {@code attribute.name.<path>}）：键存在 → 可译组件；
     * 键缺失 → 回落 {@code humanize(fallbackId)} 的 literal（可见英文，绝不漏裸键）。
     *
     * <p><b>为什么需要它</b>：架构裁定 #4 要求「能复用就复用，不铸孪生键」。某些域已有权威本地化键
     * （属性：{@code attribute.name.<path>}，由 {@code AttributeRegistry} 以
     * {@code new RangedAttribute("attribute.name." + name, ...)} 写入 {@link net.minecraft.world.entity.ai.attributes.Attribute#getDescriptionId()}），
     * 此时 {@code stat.value.*} 应让位；但仍需保留「键缺失 → 可见回落」这条铁律。
     *
     * @param authoritativeKey 外部权威键全串（如 {@code attribute.name.strength}）
     * @param fallbackId       键缺失时用于 humanize 的 snake_case id（如 {@code strength}）
     */
    public static Component ofKey(String authoritativeKey, @Nullable String fallbackId) {
        if (authoritativeKey == null || authoritativeKey.isBlank()) return Component.empty();
        Language lang = Language.getInstance();
        if (lang != null && !lang.has(authoritativeKey)) {
            String fb = fallbackId == null ? "" : ValueExpr.humanize(fallbackId);
            return fb.isEmpty() ? Component.empty() : Component.literal(fb);
        }
        return Component.translatable(authoritativeKey);
    }

    /** 权威键是否存在于当前 {@link Language}（供调用方做「优先权威键、否则值键」的二段判定）。 */
    public static boolean has(String authoritativeKey) {
        if (authoritativeKey == null || authoritativeKey.isBlank()) return false;
        Language lang = Language.getInstance();
        return lang != null && lang.has(authoritativeKey);
    }

    /** 取反前缀组件（{@code not} / 中文 {@code 非}）。用于 {@code !tag} / {@code !mode} 的可见表达。 */
    public static Component not() {
        return of("neg", "not", "not");
    }
}
