package com.cimdy.coe.data.codec.entity;

import com.cimdy.coe.data.codec.support.stat.effect.convert.DamageConvertStatEffect;
import com.cimdy.coe.data.codec.support.stat.effect.modifier.ModifierStatEffect;
import com.cimdy.coe.event.combat.AttributeEvent;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.data.codec.skill.gem.SkillData;
import com.cimdy.coe.data.codec.skill.gem.SkillAssembly;
import com.cimdy.coe.data.codec.support.attribute.AttributeLimiters;
import com.cimdy.coe.data.codec.support.math.context.node.ValueExpr;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.data.codec.support.stat.entry.StatRead;
import com.cimdy.coe.data.gen.stat.StatGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 怪物随机词缀内容（由 LIVING_MOD_MAP 加权抽选）。
 * 完全归一为与 {@link LivingModifier} 同源的纯 Stat 模型：
 * <ul>
 *   <li>{@code stats}——本词缀贡献的全部 Stat 条目（属性加成 + 转伤等），实体出生时合并进 STAT_STORE，
 *       由 {@code StatEngine.fold} 即时聚合（与天赋 / 状态 / 技能 / LivingModifier 同源）；</li>
 *   <li>{@code conditions}——本词缀生效的判定条件（维度 / 群系 / 实体类型等），以 {@link ValueExpr} 表达，
 *       运行时由 Stat 系统按事件作用域实时门控（{@code scope} 经 GateExpr#matches / StatEngine#conditionsHold 对任一条件节点求值 0 即判不生效），
 *       无需独立的实体谓词通道；</li>
 *   <li>{@code weight}——加权抽选权重。</li>
 *   <li>{@code skills}——本词缀赋予的技能（固定 id 或随机池），在被抽中时固化进实体的
 *       {@link com.cimdy.coe.registry.world.AttachmentRegistry#SKILL_ASSEMBLY}，由
 *       {@link com.cimdy.coe.entity.goal.MobSkillCastGoal} 驱动施放。</li>
 * </ul>
 * 词缀名 {@code name} 不属于本 record（词缀内容）：它上移到 {@link Mod} 包装层（数据 JSON 的 {@code name} 键即在此层），
 * 用于实体身上附件的渲染展示与抽选标识。
 * 不设 {@code predicate} / {@code convert} / {@code name} 字段：
 * 条件判定走 {@code conditions}，伤害转换由 {@code DamageConvertStatEffect} 条目表达，名称归入 {@link Mod}。
 */
public record LivingMod(
        List<StatEntry> stats, int weight, List<ValueExpr> conditions, List<ModSkill> skills
) {

    public static final Codec<LivingMod> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StatEntry.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(mod -> mod.stats),
            Codec.INT.fieldOf("weight").forGetter(mod -> mod.weight),
            ValueExpr.CODEC.listOf().optionalFieldOf("conditions", List.of())
                    .forGetter(mod -> mod.conditions),
            ModSkill.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(mod -> mod.skills)
    ).apply(instance, LivingMod::new));

    /** 固定技能条目：{@code skill} 为技能 id（如 {@code coe:fireball}）。 */
    public static ModSkill fixed(Identifier skill, int level, int quality) {
        return new ModSkill(Optional.of(skill), List.of(), level, quality, false);
    }

    /** 随机技能条目：从 {@code pool} 按 weight 加权抽 1 个。 */
    public static ModSkill random(List<PoolEntry> pool, int level, int quality) {
        return new ModSkill(Optional.empty(), List.copyOf(pool), level, quality, false);
    }

    /** 随机技能池单项。 */
    public static PoolEntry pool(Identifier skill, int weight) {
        return new PoolEntry(skill, weight);
    }

    /**
     * 词缀赋予的技能条目：<b>固定</b>（{@code skill} 指定 id）或 <b>随机</b>（{@code pool} 内按 weight 抽 1 个）。
     * {@code level} / {@code quality} 决定装配强度；{@code spirit} 标记精魂宝石（常驻光环，不绑定施放）。
     *
     * <p>输入模式限制：实体无按键输入，仅 {@code Click} 型技能可完整结算
     * （{@code HoldInput} / {@code Continuous} 需持续按住，实体侧无法驱动），词缀请勿引用这类技能。
     */
    public record ModSkill(
            Optional<Identifier> skill,
            List<PoolEntry> pool,
            int level,
            int quality,
            boolean spirit
    ) {
        public static final Codec<ModSkill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("skill").forGetter(ModSkill::skill),
                PoolEntry.CODEC.listOf().optionalFieldOf("pool", List.of()).forGetter(ModSkill::pool),
                Codec.INT.optionalFieldOf("level", 1).forGetter(ModSkill::level),
                Codec.INT.optionalFieldOf("quality", 0).forGetter(ModSkill::quality),
                Codec.BOOL.optionalFieldOf("spirit", false).forGetter(ModSkill::spirit)
        ).apply(instance, ModSkill::new));

        public boolean isEmpty() {
            return skill.isEmpty() && pool.isEmpty();
        }

        /** 解析出实际技能 id：固定项直接返回；随机池用给定随机源加权抽 1。 */
        public Optional<Identifier> resolve(RandomSource random) {
            if (skill.isPresent()) return skill;
            int total = 0;
            for (PoolEntry entry : pool) total += Math.max(0, entry.weight());
            if (total <= 0) return Optional.empty();
            int roll = random.nextInt(total);
            for (PoolEntry entry : pool) {
                roll -= Math.max(0, entry.weight());
                if (roll < 0) return Optional.of(entry.skill());
            }
            return Optional.of(pool.get(pool.size() - 1).skill());
        }

        /** 装配成实体技能槽可用的 SkillData（等级/品质直接烘进技能石数据）。 */
        public SkillData toSkillData(Identifier id) {
            return SkillData.builder(id)
                    .setOriginalLevel(Math.max(1, level))
                    .setOriginalQuality(Math.max(0, quality))
                    .spirit(spirit)
                    .build();
        }
    }

    /** 随机技能池单项（{@code weight <= 0} 视为 0，即永不抽中）。 */
    public record PoolEntry(Identifier skill, int weight) {
        public static final Codec<PoolEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("skill").forGetter(PoolEntry::skill),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(PoolEntry::weight)
        ).apply(instance, PoolEntry::new));
    }

    /**
     * 单个加权抽选项（name + 已解析词缀内容）。构造器顺序：(name, mod)。
     * {@code name} 在数据 JSON 的顶层 {@code name} 键，与 {@code mod} 并列，用于渲染 / 抽选标识。
     */
    public record Mod(String name, LivingMod mod) {
        public static final Codec<Mod> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Mod::name),
                LivingMod.CODEC.fieldOf("mod").forGetter(Mod::mod)
        ).apply(instance, Mod::new));
    }

    /**
     * 怪物身上的词缀容器（由 ENTITY_MOD 附件持有）：仅保存已应用词缀的 name 列表。
     * 具体词缀数据（LivingMod.Mod）统一从 LIVING_MOD_MAP 按 name 取回，避免冗余存储。
     */
    public record ModSet(List<String> names) {
        public static final ModSet EMPTY = new ModSet(List.of());
        public static final Codec<ModSet> CODEC =
                Codec.STRING.listOf().xmap(ModSet::new, ModSet::names);

        public ModSet add(String name) {
            List<String> list = new ArrayList<>(this.names);
            list.add(name);
            return new ModSet(list);
        }

        public ModSet remove(String name) {
            List<String> list = new ArrayList<>(this.names);
            list.remove(name);
            return new ModSet(list);
        }
    }

    /**
     * 把一批词缀声明的技能固化进实体的 {@link AttachmentRegistry#SKILL_ASSEMBLY}。
     *
     * <p><b>稳定性（关键）</b>：只在实体装配表仍为空时写入，写满即永久保持。
     * 词缀列表本身每 30 tick 会重新加权抽选（见 {@code AttributeEvent}），若每轮都重写技能，
     * 实体的技能集会每 1.5 秒抖动一次；本方法的「非空即跳过」使技能<b>一旦赋予就不再变化</b>——
     * 后续轮次无论抽到什么词缀，装配表都已非空，直接短路返回。
     *
     * <p>技能数据不进 {@link Mod}（那里只存 name 列表，内容统一按 name 从
     * {@code LIVING_MOD_MAP} 取回）；写入装配表的是解析后的 {@link SkillData}，
     * 故实体存档不冗余携带词缀内容。
     *
     * @return 本次实际写入的技能槽位数（0 表示未写入）
     */
    public static int grantSkills(LivingEntity living, RandomSource random, List<Mod> mods) {
        SkillAssembly assembly = living.getData(AttachmentRegistry.SKILL_ASSEMBLY);
        if (!assembly.isEmpty()) return 0;
        int slot = 0;
        for (Mod mod : mods) {
            for (ModSkill modSkill : mod.mod().skills()) {
                if (slot >= SkillAssembly.SIZE) break;
                if (modSkill.isEmpty()) continue;
                Optional<Identifier> id = modSkill.resolve(random);
                if (id.isEmpty()) continue;
                assembly = assembly.set(slot++, modSkill.toSkillData(id.get()));
            }
        }
        if (slot == 0) return 0;
        living.setData(AttachmentRegistry.SKILL_ASSEMBLY, assembly);
        return slot;
    }

    public static class Builder {
        private final List<StatEntry> stats = new ArrayList<>();
        private int weight = 1;
        private final List<ValueExpr> conditions = new ArrayList<>();
        private final List<ModSkill> skills = new ArrayList<>();

        public Builder(int weight) {
            this.weight = weight;
        }

        /**
         * 原版运算（加法 / 增幅 / 更多）：构造属性条目。id 仅调试标识（Stat 模型下无需），语义由 StatEntry 承载。
         * <p>全量重构（写数据生成用 Read，具体条目写入 StatGen）：委托 {@link StatGen#livingModAttr}
         * 注册 concrete 条目并返回其 {@link StatRead} 引用。
         */
        public Builder addModifier(Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation operation) {
            this.stats.add(StatGen.livingModAttr(attribute, amount, Either.left(operation), Set.of()));
            return this;
        }

        /**
         * 带事件标签的原版运算：条目只在该标签作用域内生效，用于归一属性（如 RESISTANCES 按元素标签区分火/寒/电/混沌）。
         * <p>全量重构：委托 {@link StatGen#livingModAttr}。
         */
        public Builder addModifier(Holder<Attribute> attribute, Identifier id, double amount,
                                   AttributeModifier.Operation operation, Set<String> tags) {
            this.stats.add(StatGen.livingModAttr(attribute, amount, Either.left(operation), tags));
            return this;
        }

        /**
         * 限制器运算（SET_VALUE / 钳制等）：同样经 ModifierStatEffect 的 Either.right 路径，无独立 limiter 轨道。
         * <p>全量重构：委托 {@link StatGen#livingModAttr}。
         */
        public Builder addModifier(Holder<Attribute> attribute, Identifier id, double amount, AttributeLimiters.Operation operation) {
            this.stats.add(StatGen.livingModAttr(attribute, amount, Either.right(operation), Set.of()));
            return this;
        }

        /** 生效条件（维度 / 群系 / 实体类型等），表达为 ValueExpr，运行时由 Stat 系统门控。 */
        public Builder condition(ValueExpr condition) {
            this.conditions.add(condition);
            return this;
        }

        public Builder conditions(List<ValueExpr> conditions) {
            this.conditions.addAll(conditions);
            return this;
        }

        /** 直接追加一条技能条目。 */
        public Builder addSkill(ModSkill modSkill) {
            this.skills.add(modSkill);
            return this;
        }

        public LivingMod build() {
            return new LivingMod(List.copyOf(stats), weight, List.copyOf(conditions), List.copyOf(skills));
        }
    }

    public static Builder builder(int weight) {
        return new Builder(weight);
    }
}
