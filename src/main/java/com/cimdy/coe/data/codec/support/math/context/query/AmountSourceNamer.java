package com.cimdy.coe.data.codec.support.math.context.query;

import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExprTypes;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AccessoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.AttributeValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.status.ElementalAilmentTypeCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.attr.EquipmentValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.base.EventHistoryValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.combat.target.NearbyEnemyCountValue;
import com.cimdy.coe.data.codec.support.math.context.nodes.value.entity.presence.PresenceCountValue;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import com.cimdy.coe.registry.AttributeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * per-X 来源节点 → 可读名（P3 / P4 值键化）。覆盖 ~9 种常见来源类型；未知类型回落到
 * {@link ValueExprTypes#toInfix}（可见、不静默丢弃）。
 *
 * <p><b>值键化（P4）</b>：本类曾经返回裸英文字符串（{@code "Strength"} / {@code "Hit Count"}），
 * 作为 {@code %s} 注入 {@code stat.amount.per} 模板后永远不会被本地化。现在改为把来源拆成
 * <b>若干可复用值键片段</b>（{@code stat.value.<ns>.<id>}）再组合成 {@link Component}
 * （架构裁定 #3：输入自己带键、由模板组合，不为每种组合铸整句键）。
 *
 * <p>片段划分（en_us 值恒等于 {@link ValueExpr#humanize(String)} 的输出，故英文逐字不变）：
 * <ul>
 *   <li>{@code attribute:<id>} → <b>复用权威键</b> {@code attribute.name.<path>}
 *       （{@code AttributeRegistry} 以 {@code new RangedAttribute("attribute.name." + name, ...)} 注册，
 *       即 {@code Attribute#getDescriptionId()}），<b>不</b>铸 {@code stat.value.attribute.*} 孪生键；</li>
 *   <li>{@code nearby_enemy_count} → {@code stat.value.src.nearby_enemy_count}；</li>
 *   <li>{@code elemental_ailment_type_count} → {@code stat.value.src.elemental_ailment_type_count}；</li>
 *   <li>{@code equipment_stat} → [槽位]{@code stat.value.equip_slot.<slot>} + [输出]{@code stat.value.equip_out.<out>}
 *       （如 "Head Armor" / 无单槽位时仅 "Equipment Evasion"）；</li>
 *   <li>{@code accessory_stat} → [分组]{@code stat.value.accessory_group.<g>} + [输出]{@code stat.value.equip_out.<out>}
 *       （如 "Legs Evasion"）；</li>
 *   <li>{@code count}/distinct/sum（EventHistoryValue）→ {@code stat.value.event.<name>} + {@code stat.value.src.count}；</li>
 *   <li>{@code presence_count} → [类别]{@code stat.value.presence_kind.<k>} + [关系]{@code stat.value.presence_relation.<r>}
 *       + {@code stat.value.src.count}；两者皆 ANY 时以 {@code stat.value.src.presence} 顶替。</li>
 * </ul>
 *
 * <p>{@link #humanize(ValueExpr)} 保留为字符串口径（旧调用点与回归夹具使用），
 * 与 {@link #name(ValueExpr)} 共用同一份 {@link #parts(ValueExpr)} 映射，二者永不漂移。
 */
public final class AmountSourceNamer {

    private AmountSourceNamer() {
    }

    /** per-X 来源 → 可译组件（值键组合）。 */
    public static Component name(ValueExpr srcNode) {
        List<String[]> parts = parts(srcNode);
        if (parts.isEmpty()) return Component.empty();
        MutableComponent out = null;
        for (String[] p : parts) {
            Component c = component(p);
            if (out == null) {
                out = (MutableComponent) c;
            } else {
                out.append(Component.literal(" ")).append(c);
            }
        }
        return out == null ? Component.empty() : out;
    }

    /**
     * 单个片段 → 组件。<b>二段判定</b>：
     * <ol>
     *   <li>片段带权威键（如 {@code attribute.name.fire_resistance}）且该键在当前 Language 中存在 → 直接用它
     *       （架构裁定 #4：能复用就不铸 {@code stat.value.*} 孪生键）；</li>
     *   <li>否则走 {@code stat.value.<ns>.<id>}，键仍缺失则回落 humanize 英文 literal。</li>
     * </ol>
     */
    private static Component component(String[] p) {
        // 单词拼接优先：复合值键（如 equip_out.lightning_resistance = 闪电+抗性）拆为已有单词键，
        // 避免复用 attribute.name.* 这类冗余复合权威键（裁定：以翻译值为唯一、多单词拼接）。
        List<String> wc = StatValueKey.splitWords(p[1]);
        if (wc != null) {
            MutableComponent c = Component.translatable(wc.get(0));
            for (int i = 1; i < wc.size(); i++) {
                c = c.append(Component.translatable("stat.concat_sep")).append(Component.translatable(wc.get(i)));
            }
            return c;
        }
        String alias = p.length >= 4 ? p[3] : null;
        if (alias != null && !alias.isEmpty() && StatValueKey.has(alias)) {
            return Component.translatable(alias);
        }
        return StatValueKey.of(p[0], p[1], p[2]);
    }

    /**
     * per-X 来源 → 裸英文字符串（与 {@link #name} 同一映射，用于旧调用点 / 回归夹具）。
     * 各片段以空格连接，与本类改造前的输出<b>逐字一致</b>。
     */
    public static String humanize(ValueExpr srcNode) {
        StringBuilder sb = new StringBuilder();
        for (String[] p : parts(srcNode)) {
            if (sb.length() > 0) sb.append(' ');
            String fb = p[2];
            sb.append(fb == null || fb.isEmpty() ? ValueExpr.humanize(p[1]) : fb);
        }
        return sb.toString();
    }

    /**
     * 来源 → 片段三/四元组列表 {@code [ns, id, fallback, 权威键?]}；
     * {@code fallback} 为 {@code null} 时由 {@link ValueExpr#humanize(String)} 兜底，
     * {@code 权威键}（可选）存在时优先复用之（架构裁定 #4）。{@link #name} 与 {@link #humanize} 共用此映射。
     */
    private static List<String[]> parts(ValueExpr srcNode) {
        if (srcNode instanceof AttributeValue av) {
            // 属性不铸 stat.value.attribute.* 孪生键：直接复用 Attribute 自带的
            // descriptionId（AttributeRegistry 以 "attribute.name." + name 注册）。
            String path = av.id().getPath();
            return List.<String[]>of(new String[]{"attribute", path, null, "attribute.name." + path});
        }
        if (srcNode instanceof NearbyEnemyCountValue) {
            return List.<String[]>of(new String[]{"src", "nearby_enemy_count", null});
        }
        if (srcNode instanceof ElementalAilmentTypeCountValue) {
            return List.<String[]>of(new String[]{"src", "elemental_ailment_type_count", null});
        }
        if (srcNode instanceof EquipmentValue ev) {
            List<String[]> out = new ArrayList<>();
            if (ev.slots().isPresent() && ev.slots().get().size() == 1) {
                out.add(new String[]{"equip_slot", ev.slots().get().get(0).getName(), null});
            }
            out.add(outputPart(ev.output().orElse(""), "equipment"));
            return out;
        }
        if (srcNode instanceof AccessoryValue ac) {
            List<String[]> out = new ArrayList<>();
            if (ac.group().isPresent()) {
                out.add(new String[]{"accessory_group", ac.group().get(), null});
            }
            out.add(outputPart(ac.output().orElse(""), "accessory"));
            return out;
        }
        if (srcNode instanceof EventHistoryValue eh) {
            List<String[]> out = new ArrayList<>();
            out.add(new String[]{"event", eh.name(), null});
            if (Set.of("count", "distinct", "sum").contains(eh.mode())) {
                out.add(new String[]{"src", "count", null});
            }
            return out;
        }
        if (srcNode instanceof PresenceCountValue pc) {
            boolean rel = pc.relation() != PresenceCountValue.Relation.ANY;
            boolean kind = !pc.kind().equals(PresenceCountValue.ANY);
            List<String[]> out = new ArrayList<>();
            if (!rel && !kind) {
                out.add(new String[]{"src", "presence", null});
            } else {
                if (kind) out.add(new String[]{"presence_kind", pc.kind(), null});
                if (rel) out.add(new String[]{"presence_relation", pc.relation().getSerializedName(), null});
            }
            out.add(new String[]{"src", "count", null});
            return out;
        }
        // 未知来源：可见回落到中缀串（不静默丢弃）；id 做 sanitize 以适配键字符集。
        String raw = ValueExprTypes.toInfix(srcNode);
        if (raw == null || raw.isEmpty()) return List.of();
        return List.<String[]>of(new String[]{"src", sanitize(raw), raw});
    }

    /**
     * 装备 / 饰品的「输出项」片段： armor / evasion / fire_resistance / item_level …
     * <b>能复用就复用</b>（裁定 #4）：若该输出项本身就是已本地化的属性（{@code attribute.name.<out>} 存在），
     * 直接用权威键；否则落到 {@code stat.value.equip_out.<out>}，再缺失回落 humanize 英文。
     *
     * @param out        输出项 id；为空时退化到 {@code stat.value.src.<defaultId>}（"Equipment" / "Accessory"）
     */
    private static String[] outputPart(String out, String defaultId) {
        if (out.isEmpty()) return new String[]{"src", defaultId, null};
        return new String[]{"equip_out", out, null, "attribute.name." + out};
    }

    /** 中缀串 → 键安全 id（仅保留 {@code [a-z0-9_]}，其余落下划线）。 */
    private static String sanitize(String raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
            else if (c >= 'A' && c <= 'Z') sb.append(Character.toLowerCase(c));
            else sb.append('_');
        }
        return sb.toString();
    }
}
