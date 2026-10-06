package com.cimdy.coe.data.codec.skill.phase.action.damage;
import com.cimdy.coe.data.codec.skill.phase.action.Action;
import com.cimdy.coe.data.codec.skill.phase.action.ActionContext;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;

import com.cimdy.coe.data.codec.support.value.Value;
import com.cimdy.coe.data.helper.hub.SpiritManager;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.context.SkillContext;
import com.cimdy.coe.data.codec.support.math.ExpressionCodec;
import com.cimdy.coe.data.codec.support.stat.engine.StatStore;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 属性修改动作 — 修改目标实体的属性值（增益/减益），经 Stat 体系落地。
 * <p>
 * 不对 {@code AttributeInstance} 直接写入 {@link AttributeModifier} + {@code BuffManager}，
 * 而通过 {@link StatStore} 写入 {@link ModifierStatEffect}，叠加结果由
 * {@link com.cimdy.coe.data.codec.support.stat.engine.StatEngine#fold} 在属性读取路径即时聚合。
 * 临时 buff（duration &gt; 0）由 {@link StatStore} 内建计时域（{@code addModifier(key, entry, ticks)}
 * + {@code StatStore.tick()}）到期自动移除。
 * <ul>
 *   <li>operation: 0=ADD_VALUE, 1=ADD_MULTIPLIED_TOTAL, 2=ADD_MULTIPLIED_BASE</li>
 *   <li>value: 修改量（必须经由 ExpressionCodec.Value 表达，支持随技能等级/品质缩放）</li>
 *   <li>duration: 有效期(tick)。&gt;0 时在到期自动从 StatStore 移除；&le;0 视为常驻</li>
 * </ul>
 */
public record ModifierAction(
        Optional<Identifier> attributeId,
        ExpressionCodec.Value operation,
        ExpressionCodec.Value value,
        ExpressionCodec.Value duration,
        List<String> tags
) implements Action {
    /** 兼容旧调用：attributeId 允许为 null，无标签。 */
    public ModifierAction(Identifier attributeId, ExpressionCodec.Value operation,
                          ExpressionCodec.Value value, ExpressionCodec.Value duration) {
        this(Optional.ofNullable(attributeId), operation, value, duration, List.of());
    }

    /**
     * 带语义标签的属性增益 —— 归一属性按标签区分维度，无标签则无法表达具体维度。
     * 例：抗性归一为 {@code coe:resistances}（base=0），元素由 {@code fire}/{@code cold}… 标签区分；
     * 伤害归一为 {@code coe:damage}（base=1），元素同样由标签区分。
     * 与 {@link StatGen#modifierAttr} 的 tags 语义一致（多项 = AND）。
     */
    public ModifierAction(Identifier attributeId, ExpressionCodec.Value operation,
                          ExpressionCodec.Value value, ExpressionCodec.Value duration, String... tags) {
        this(Optional.ofNullable(attributeId), operation, value, duration, List.of(tags));
    }

    public static final Codec<ModifierAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.optionalFieldOf("attribute").forGetter(ModifierAction::attributeId),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("operation", ExpressionCodec.of(0.0))
                    .forGetter(ModifierAction::operation),
            ExpressionCodec.VALUE_CODEC.fieldOf("value").forGetter(ModifierAction::value),
            ExpressionCodec.VALUE_CODEC.optionalFieldOf("duration", ExpressionCodec.of(0.0))
                    .forGetter(ModifierAction::duration),
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(ModifierAction::tags)
    ).apply(i, ModifierAction::new));

    @Override public Action.Type<?> getType() { return ActionTypes.MODIFIER; }

    /**
     * 写入 {@link StatStore} 的去重来源键 —— 由「属性 id + operation + tags」<b>共同</b>派生
     * （对齐 {@link StatAction#modId()} 的内容派生范式）。
     *
     * <p><b>为何必须含 tags</b>：抗性 / 伤害等<b>归一</b>属性共用同一个 attribute
     * （如 {@code coe:resistances}），元素维度只由 tags 区分。若 modId 只由属性 id 派生，
     * 四颗 Purity 光环（fire / cold / lightning / chaos，PoE2 中本就可同时开启）会算出<b>同一个键</b>，
     * 而光环每 tick 重放会先按该键前缀删除再写入 —— 后重放者删掉前者，
     * 最终只剩最后一颗光环的抗性生效（真玩法失效）。
     *
     * <p><b>为何必须含 operation</b>：加法（op=0）与乘算基础（op=2）是语义不同的两条增益
     * （例：精魂 wind_dancer 的 {@code evasion op=2} 与技能「钢铁看守」的 {@code evasion op=0}），
     * 同键同样会互相删除。
     *
     * <p><b>为何用 '/' 分段</b>：{@link StatStore#removeByKeyPrefix} 是 {@code startsWith} <b>前缀</b>删除。
     * '/' 是 {@link Identifier} 路径合法字符；各段以 '/' 硬分隔后，
     * {@code .../op0/fire} 不会成为 {@code .../op0/fireball} 的前缀，
     * 无标签的 {@code .../op0} 也不会误删带标签的同属性条目。
     *
     * <p><b>纯函数</b>：同内容恒得同键 —— 精魂光环每 tick 重放为「覆盖刷新」而非叠加
     * （不撑爆属性值 / 不泄漏条目），此语义不得破坏。
     */
    public static Identifier auraModId(Identifier attributeId, int operation, List<String> tags) {
        StringBuilder sb = new StringBuilder("mod_")
                .append(attributeId.toString().replace(':', '_'))
                .append("/op").append(operation);
        if (tags != null && !tags.isEmpty()) {
            // 排序 + 去重：同组标签的书写顺序差异不得派生出两个不同的键（否则重复重放变成叠加）
            List<String> sorted = new ArrayList<>(new LinkedHashSet<>(tags));
            Collections.sort(sorted);
            for (String t : sorted) {
                if (t == null || t.isEmpty()) continue;
                sb.append('/');
                appendSanitized(sb, t);
            }
        }
        return Identifier.fromNamespaceAndPath(CraftOfExile.MODID, sb.toString());
    }

    /** 标签清洗为 Identifier 路径合法字符 {@code [a-z0-9_.-]}（其余一律归 '_'），避免派生键抛异常。 */
    private static void appendSanitized(StringBuilder sb, String tag) {
        for (int i = 0; i < tag.length(); i++) {
            char c = Character.toLowerCase(tag.charAt(i));
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '.' || c == '-';
            sb.append(ok ? c : '_');
        }
    }

    /**
     * 本动作的来源键 —— 写入侧（{@link #execute}）与回收侧（{@link com.cimdy.coe.data.helper.hub.SpiritManager}
     * 光环停用时）<b>必须调用同一个方法</b>，否则两侧派生漂移 ⇒ 常驻条目泄漏成永久残留。
     *
     * @return 来源键；属性缺省时返回 {@code null}（对应 {@link #execute} 的早退分支，不写入）
     */
    public Identifier modId(SkillContext skillCtx) {
        if (attributeId.isEmpty()) return null;
        int op = (int) Math.round(operation.getFloat(skillCtx));
        return auraModId(attributeId.get(), op, tags);
    }

    /** timed 条目的模板身份 tag：StatGen.modifierAttr 按 attribute+op+tag 唯一注册 $value 模板（幂等）。 */
    public static String entryTag(Identifier modId) {
        return "_timed:" + modId;
    }

    @Override
    public void execute(ActionContext context) {
        if (attributeId.isEmpty()) return;
        if (!(context.target().orElse(context.caster()) instanceof LivingEntity target)) return;
        SkillContext skillCtx = context.skill() != null ? context.skill().getContext() : null;

        Identifier attrId = attributeId.get();
        ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, attrId);
        Holder<Attribute> holder = target.level().registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE)
                .get(key)
                .orElse(null);
        if (holder == null) return;

        int op = (int) Math.round(operation.getFloat(skillCtx));
        double val = value.getFloat(skillCtx);
        double dur = duration.getFloat(skillCtx);

        AttributeModifier.Operation modifierOp = switch (op) {
            case 1 -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            case 2 -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };

        // 来源键含 op + tags：同属性不同维度/运算的增益互不剔除（见 auraModId 注释）
        Identifier modId = modId(skillCtx);

        // 构建属性条目（预求值常量，带上唯一标识 tag 用于 StatGen 模板身份区分）
        String entryTag = entryTag(modId);
        // 语义标签（元素/形态）与模板身份标签合并：StatGen 按 attribute+op+tags 唯一注册模板，
        // 故不同元素的同属性增益各自成条目（如 resistances+fire 与 resistances+cold 互不影响）。
        Set<String> tagSet = new java.util.LinkedHashSet<>(tags);
        tagSet.add(entryTag);
        // 全量重构（写数据生成用 Read，具体条目写入 StatGen）：经 StatGen.modifierAttr 注册 $value 模板
        // （按 attribute+op+tags 唯一，幂等一次）并返回注入 val 的 StatRead 引用；运行期避免 per-val 条目无限增长。
        StatEntry entry = StatGen.modifierAttr(
                holder,
                val,                             // 实际幅度：执行瞬间已按技能上下文求值，经 vars 注入
                Either.left(modifierOp),
                tagSet                           // tags: 语义标签 + 模板身份标识
        );

        // 写入 StatStore（与 StatAction 共用同一个附件）——增量 API（2-4）：
        // 以稳定来源键 timed:<modId> 精确寻址，同键 put 覆盖（光环每 tick 刷新覆盖而非叠加，
        // 覆盖同时重置计时域时长）；duration > 0 走 StatStore 内建计时域到期自灭
        StatStore current = target.hasData(AttachmentRegistry.STAT_STORE)
                ? target.getData(AttachmentRegistry.STAT_STORE)
                : StatStore.empty();
        int ticks = dur > 0 ? (int) Math.min(dur, Integer.MAX_VALUE) : 0;
        target.setData(AttachmentRegistry.STAT_STORE,
                current.addModifier("timed:" + modId, entry, ticks));
    }
}
