package com.cimdy.coe.data.codec.item.addition.impl;

import com.cimdy.coe.data.codec.item.addition.AdditionRead;
import com.cimdy.coe.data.codec.item.addition.AdditionType;
import com.cimdy.coe.data.codec.item.addition.ContributionSink;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 多子词缀组合（{@code type = "multi"}）—— 把 N 个 {@link AdditionType} 组合为一条词缀。
 *
 * <p>每个子 Addition 经 {@link Part} 携带一个<b>可选绑定键（value slot）</b>：
 * <ul>
 *   <li><b>绑定键非空</b>：该子 Addition 占独立值槽（键即槽名），单独 roll；碰撞键由调用方负责唯一，
 *       或用 {@link #of} 自动去重加 {@code "_2"} 后缀。</li>
 *   <li><b>绑定键为 null / 空</b>：该子 Addition 进入「共用值组」、<b>不占独立槽</b>，与组内其它未绑键子
 *       Addition 共用同一个 roll 值——即原 {@code SecondStatAddition} 的「单值驱动双 Stat」语义。</li>
 * </ul>
 *
 * <p>取代原 {@code SecondStatAddition}：全部不绑键（{@link #shared}）即「共用值」；
 * 自动 / 显式绑键（{@link #of} / {@code new MultiAddition(List.of(...))} / {@link Part#key}）即「独立值槽」。</p>
 */
public record MultiAddition(List<Part> parts) implements AdditionType {

    /** 子 Addition + 其绑定键（null / 空 = 共用主值）。 */
    public record Part(AdditionType addition, String key) {

        public static Part of(AdditionType a) {
            return new Part(a, null);
        }

        public static Part key(AdditionType a, String k) {
            return new Part(a, k);
        }

        /** 是否共用值（未绑键）。 */
        public boolean shared() {
            return key == null || key.isEmpty();
        }

        public static final MapCodec<Part> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                AdditionType.MAP_CODEC.fieldOf("addition").forGetter(Part::addition),
                Codec.STRING.optionalFieldOf("key").forGetter(p -> Optional.ofNullable(p.key))
        ).apply(instance, (add, keyOpt) -> new Part(add, keyOpt.orElse(null))));
    }

    /**
     * 独立值槽组合：每子 Addition 自动绑键、各自 roll。
     *
     * <p><b>只能做静态工厂、不能做构造器</b>：本 record 的规范构造器擦除签名为 {@code (List)}，
     * 若再声明 {@code MultiAddition(List<AdditionType>)} 会因<b>擦除冲突</b>被判定为「无效的规范构造器」
     * （规范构造器不得含显式 {@code this(...)} 调用、形参与组件类型须一致）。
     */
    public static MultiAddition autoBind(List<AdditionType> additions) {
        return new MultiAddition(bindParts(additions));
    }

    public static MultiAddition of(AdditionType... additions) {
        return autoBind(List.of(additions));
    }

    /**
     * <b>生成期安全</b>的独立值槽组合：<b>按位绑键</b>（第 1 个子 Addition 绑 {@code "value"}、
     * 第 2 个绑 {@code "value_2"}、……），<b>不读取子 Addition 的值槽、不触发任何解析</b>。
     *
     * <p>存在理由：{@link #autoBind} 需经 {@link AdditionType#valueSlots()} / {@code forSlot} 询问
     * 每个子 Addition 的槽名；当子 Addition 是 {@link AdditionRead}（引用型）时，这会触发
     * {@code resolve()} —— 而 {@code ADDITION_MAP} 由数据包填充，<b>数据生成期必然为空</b>
     * （additions 本身正是本次要生成的文件），故生成期只能用本工厂。</p>
     *
     * <p>适用前提：各子 Addition <b>均为单槽</b>（绝大多数词缀如此；与 {@link #autoBind} 对单槽的
     * 结果一致）。多槽子 Addition（如 {@code min}/{@code max} 双占位）请改用 {@link #autoBind}
     * —— 那只在<b>运行期</b>（引用已解析）才可用。</p>
     */
    public static MultiAddition keyed(AdditionType... additions) {
        List<Part> out = new ArrayList<>();
        for (int i = 0; i < additions.length; i++) {
            out.add(new Part(additions[i], i == 0 ? "value" : "value_" + (i + 1)));
        }
        return new MultiAddition(List.copyOf(out));
    }

    /** 共用值组合：所有子 Addition 不绑键，共用同一 roll 值（等价原 {@code SecondStatAddition}）。 */
    public static MultiAddition shared(AdditionType... additions) {
        List<Part> ps = new ArrayList<>();
        for (AdditionType a : additions) ps.add(Part.of(a));
        return new MultiAddition(ps);
    }

    private static List<Part> bindParts(List<AdditionType> additions) {
        List<Part> out = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (AdditionType a : additions) {
            for (String s : a.valueSlots()) {
                String k = s;
                while (used.contains(k)) k = k + "_2";
                used.add(k);
                out.add(new Part(a.forSlot(s), k));
            }
        }
        return out;
    }

    public static final MapCodec<MultiAddition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Part.MAP_CODEC.codec().listOf().fieldOf("parts").forGetter(MultiAddition::parts)
    ).apply(instance, MultiAddition::new));

    @Override
    public String type() {
        return "multi";
    }

    private static final String SHARED_SLOT = "value";

    @Override
    public List<String> valueSlots() {
        List<String> slots = new ArrayList<>();
        boolean hasShared = false;
        for (Part p : parts) {
            if (p.shared()) hasShared = true;
            else slots.add(p.key);
        }
        if (hasShared) slots.add(SHARED_SLOT);
        return slots;
    }

    @Override
    public AdditionType forSlot(String slot) {
        for (Part p : parts) {
            if (!p.shared() && p.key.equals(slot)) return p.addition;
        }
        if (SHARED_SLOT.equals(slot)) return sharedLeaf();
        return this;
    }

    private AdditionType sharedLeaf() {
        List<AdditionType> shared = new ArrayList<>();
        for (Part p : parts) if (p.shared()) shared.add(p.addition);
        return new AdditionType() {
            @Override
            public String type() {
                return "multi_shared";
            }

            @Override
            public void contribute(ContributionSink sink, double rolled) {
                for (AdditionType a : shared) a.contribute(sink, rolled);
            }

            @Override
            public List<Component> describe(double rolled, Locale locale) {
                List<Component> out = new ArrayList<>();
                for (AdditionType a : shared) out.addAll(a.describe(rolled, locale));
                return out;
            }
        };
    }

    @Override
    public void contribute(ContributionSink sink, double rolled) {
        // 兜底（未知槽）路径：扇出到所有子 Addition。正常路径经 forSlot 收窄后逐槽调用。
        for (Part p : parts) p.addition.contribute(sink, rolled);
    }

    @Override
    public List<Component> describe(double rolled, Locale locale) {
        List<Component> out = new ArrayList<>();
        for (Part p : parts) out.addAll(p.addition.describe(rolled, locale));
        return out;
    }

    @Override
    public List<AdditionType.Effect> effects() {
        List<AdditionType.Effect> out = new ArrayList<>();
        for (Part p : parts) out.addAll(p.addition.effects());
        return out;
    }
}
