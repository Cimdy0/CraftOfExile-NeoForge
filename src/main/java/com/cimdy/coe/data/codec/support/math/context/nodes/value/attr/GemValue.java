package com.cimdy.coe.data.codec.support.math.context.nodes.value.attr;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SupportData;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.support.math.context.ValueScope;
import com.cimdy.coe.data.codec.support.math.context.node.EntityNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.cimdy.coe.client.ClientLocale;
import com.cimdy.coe.data.codec.support.stat.entry.StatValueKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 装配技能宝石读值节点（合并原 {@code SkillGemValue} 与 {@code SocketValue}）。
 * 统一两类查询，由 {@code aggregate} 区分：
 * <ul>
 *   <li>{@code aggregate = first}（默认，中缀 {@code gem:<id>}）—— 在（限定）装配槽中找首个命中 gemId 的技能石，
 *       按 {@code output} 输出其属性（level / quality / effective_level / effective_quality / sockets /
 *       supports / spirit / aura），未指定 output 则输出 1.0（布尔命中）；</li>
 *   <li>{@code aggregate = count}（中缀 {@code socket:<color>.<mode>}）—— 统计匹配颜色与种类
 *       （skill / support / all）的已镶嵌宝石数；须为玩家（保留原 SocketValue 的 Player 门槛）。</li>
 * </ul>
 */
public record GemValue(
        Optional<List<Integer>> slots,
        Optional<Identifier> gemId,
        Optional<String> color,
        String kind,
        Optional<String> output,
        String aggregate
) implements EntityNode {

    public GemValue {
        if (kind == null) kind = "all";
        if (aggregate == null) aggregate = "first";
    }

    public static final Codec<GemValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.listOf().optionalFieldOf("slots").forGetter(GemValue::slots),
            Identifier.CODEC.optionalFieldOf("gem_id").forGetter(GemValue::gemId),
            Codec.STRING.optionalFieldOf("color").forGetter(GemValue::color),
            Codec.STRING.optionalFieldOf("kind", "all").forGetter(GemValue::kind),
            Codec.STRING.optionalFieldOf("output").forGetter(GemValue::output),
            Codec.STRING.optionalFieldOf("aggregate", "first").forGetter(GemValue::aggregate)
    ).apply(instance, GemValue::new));

    @Override
    public double evaluate(LivingEntity living, ValueScope scope) {
        if (aggregate.equals("count")) return count(living);
        return first(living);
    }

    /** 名词短语：宝石 id path → 值键 {@code stat.value.gem.<path>}（未注册回落 humanize 英文）。 */
    @Override
    public List<Component> describeQuantity(Locale locale) {
        return List.of(gemId.map(id -> StatValueKey.of("gem", id.getPath()))
                .orElseGet(() -> StatValueKey.of("gem", "any")));
    }

    /** 条件从句：{@code stat.condition.gem} = "when socketed with %s"。 */
    @Override
    public List<Component> describeFragment() {
        return List.of(Component.translatable("stat.condition.gem", describeQuantity(ClientLocale.get()).get(0)));
    }

    // 首个命中（非空且匹配 gemId）技能石，按 output 输出属性；无 output → 1.0（原 SkillGemValue 行为）
    private double first(LivingEntity living) {
        SkillAssembly assembly = living.getData(AttachmentRegistry.SKILL_ASSEMBLY);
        int size = assembly.slots().size();
        List<Integer> toCheck = slots.orElse(null);
        for (int i = 0; i < size; i++) {
            if (toCheck != null && !toCheck.contains(i)) continue;
            var gem = assembly.get(i);
            if (gem.isEmpty()) continue;
            if (gemId.isEmpty() || gem.id().equals(gemId.get())) {
                String key = output.orElse("");
                return switch (key) {
                    case "level" -> gem.getSkillLevel();
                    case "quality" -> gem.quality().get();
                    case "effective_level" -> gem.getEffectiveSkillLevel();
                    case "effective_quality" -> gem.getEffectiveQuality();
                    case "sockets" -> gem.getSockets();
                    case "supports" -> (double) gem.supports().stream().filter(sd -> !sd.isEmpty()).count();
                    case "spirit" -> gem.spirit() ? 1.0 : 0.0;
                    case "aura" -> assembly.isAuraEnabled(i) ? 1.0 : 0.0;
                    default -> 1.0;
                };
            }
        }
        return 0.0;
    }

    // 统计匹配颜色与种类（skill/support/all）的已镶嵌宝石数；须为玩家（原 SocketValue 行为）
    private double count(LivingEntity living) {
        if (!(living instanceof Player player)) return 0D;
        if (!player.hasData(AttachmentRegistry.SKILL_ASSEMBLY)) return 0D;
        SkillAssembly assembly = player.getData(AttachmentRegistry.SKILL_ASSEMBLY);
        String targetColor = color.map(String::toLowerCase).orElse(null);
        String k = kind.toLowerCase();
        boolean countSkill = k.equals("all") || k.equals("skill");
        boolean countSupport = k.equals("all") || k.equals("support");
        int count = 0;
        for (SkillData slot : assembly.slots()) {
            if (slot.isEmpty()) continue;
            if (countSkill && matchColor(slot.id(), targetColor)) count++;
            if (countSupport) {
                for (SupportData s : slot.supports()) {
                    if (!s.isEmpty() && matchColor(s.id(), targetColor)) count++;
                }
            }
        }
        return count;
    }

    private static boolean matchColor(Identifier id, String targetColor) {
        if (targetColor == null) return true;
        String seg = firstSegment(id);
        return targetColor.equals(seg);
    }

    private static String firstSegment(Identifier id) {
        String path = id.getPath();
        int slash = path.indexOf('/');
        return slash >= 0 ? path.substring(0, slash) : path;
    }
}
